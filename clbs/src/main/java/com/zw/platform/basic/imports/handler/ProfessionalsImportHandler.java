package com.zw.platform.basic.imports.handler;

import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.domain.ProfessionalDO;
import com.zw.platform.basic.dto.ProfessionalDTO;
import com.zw.platform.basic.dto.imports.ProfessionalImportDTO;
import com.zw.platform.basic.repository.NewProfessionalsDao;
import com.zw.platform.basic.service.impl.ProfessionalServiceImpl;
import com.zw.platform.util.imports.lock.BaseImportHandler;
import com.zw.platform.util.imports.lock.ImportModule;
import com.zw.platform.util.imports.lock.ImportTable;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author lijie
 * @version 1.0
 * @date 2020/11/4 9:32
 */
@Slf4j
@AllArgsConstructor
public class ProfessionalsImportHandler extends BaseImportHandler {
    private final NewProfessionalsDao newProfessionalsDao;

    /**
     * 导入的从业人员
     */
    private final  List<ProfessionalImportDTO> excelDataList;
    /**
     * 存入数据库的从业人员
     */
    private final  List<ProfessionalDO> professionalDOS;

    @Override
    public ImportModule module() {
        return ImportModule.PROFESSIONAL;
    }

    @Override
    public ImportTable[] tables() {
        return new ImportTable[] { ImportTable.ZW_M_PROFESSIONALS_INFO};
    }

    @Override
    public boolean uniqueValid() {
        // 数据库已经存在的从业人员和身份证号对应关系集合
        Map<String, Set<String>> alreadyExistNameAndIdentityListMap =
            newProfessionalsDao.getProfessionalsByNames(null).stream().collect(Collectors
                .groupingBy(ProfessionalDO::getName,
                    Collectors.mapping(ProfessionalDO::getIdentity, Collectors.toSet())));
        // 数据库已存在的身份证号集合
        Set<String> alreadyExistIdentitySet = newProfessionalsDao.getAlreadyExistIdentitySet(null);
        // 错误条数
        int errorNum = 0;
        for (ProfessionalImportDTO professional : excelDataList) {
            String name = professional.getName();
            String identity = professional.getIdentity();
            boolean identityIsNotBlank = StringUtils.isNotBlank(identity);
            if (alreadyExistNameAndIdentityListMap.containsKey(name)) {
                if (!identityIsNotBlank) {
                    errorNum++;
                    professional.setErrorMsg("姓名已存在，请输入身份证号进行区分");
                    continue;
                }
                if (alreadyExistNameAndIdentityListMap.get(name).contains(identity)) {
                    professional.setErrorMsg("姓名已存在");
                    errorNum++;
                    continue;
                }
            }
            if (identityIsNotBlank && alreadyExistIdentitySet.contains(identity)) {
                errorNum++;
                professional.setErrorMsg("身份证号已存在");
            }
        }

        progressBar.setTotalProgress(professionalDOS.size() * 2);

        return  errorNum == 0;
    }

    @Override
    public boolean addMysql() {
        partition(professionalDOS, newProfessionalsDao::addProfessionalsByBatch);
        return true;
    }

    @Override
    public void addOrUpdateRedis() {
        if (professionalDOS == null || professionalDOS.size() == 0) {
            return;
        }

        List<String> ids = new ArrayList<>();
        Map<RedisKey, Map<String, String>> proInfoMap = new HashMap<>();
        Map<String, String> proFuzzyMap = new HashMap<>();
        for (ProfessionalDO professionalDO : professionalDOS) {
            ids.add(professionalDO.getId());
            ProfessionalDTO professionalDTO = new ProfessionalDTO();
            BeanUtils.copyProperties(professionalDO, professionalDTO);
            Map<String, String> map = ProfessionalServiceImpl.setValueToMap(professionalDTO);
            proInfoMap.put(RedisKeyEnum.PROFESSIONAL_INFO.of(professionalDO.getId()), map);

            String hashKey = ProfessionalServiceImpl.constructFuzzySearchKey(professionalDO.getName(),
                professionalDO.getIdentity(), professionalDO.getState());
            proFuzzyMap.put(hashKey, professionalDO.getId());
        }

        RedisHelper.batchAddToHash(proInfoMap);
        RedisHelper.addToListTop(RedisKeyEnum.PROFESSIONAL_SORT_ID.of(), ids);
        RedisHelper.addToSet(RedisKeyEnum.ORGANIZATION_PROFESSIONAL_ID.of(professionalDOS.get(0).getOrgId()), ids);
        RedisHelper.addToHash(RedisKeyEnum.FUZZY_PROFESSIONAL.of(), proFuzzyMap);

    }

}

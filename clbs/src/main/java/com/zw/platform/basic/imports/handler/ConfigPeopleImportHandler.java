package com.zw.platform.basic.imports.handler;

import com.zw.platform.basic.constant.GenderEnum;
import com.zw.platform.basic.constant.MonitorTypeEnum;
import com.zw.platform.basic.constant.Vehicle;
import com.zw.platform.basic.domain.PeopleDO;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.MonitorBaseDTO;
import com.zw.platform.basic.dto.PeopleDTO;
import com.zw.platform.basic.imports.ConfigImportHolder;
import com.zw.platform.basic.service.PeopleService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.util.AssembleUtil;
import com.zw.platform.util.imports.lock.BaseImportHandler;
import com.zw.platform.util.imports.lock.ImportModule;
import com.zw.platform.util.imports.lock.ImportTable;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 信息配置人员信息导入
 * @author zhangjuan
 */
@Slf4j
public class ConfigPeopleImportHandler extends BaseImportHandler {
    private final ConfigImportHolder importHolder;
    private final PeopleService peopleService;
    /**
     * excel中是否存在需要新增的人员
     */
    private int peopleCount;
    private final List<String> delPeopleIds;
    private final List<PeopleDTO> peopleList;

    public ConfigPeopleImportHandler(ConfigImportHolder importHolder, PeopleService peopleService) {
        this.importHolder = importHolder;
        this.peopleService = peopleService;
        this.delPeopleIds = new ArrayList<>();
        this.peopleCount = 0;
        this.peopleList = new ArrayList<>();
    }

    @Override
    public ImportModule module() {
        return ImportModule.CONFIG;
    }

    @Override
    public int stage() {
        return 0;
    }

    @Override
    public ImportTable[] tables() {
        return new ImportTable[] { ImportTable.ZW_M_PEOPLE_INFO };
    }

    @Override
    public boolean uniqueValid() {
        if (importHolder.getImportPeopleNum() == 0) {
            progressBar.setTotalProgress(1);
            return true;
        }

        Map<String, String> orgMap = importHolder.getOrgMap();
        Map<String, MonitorBaseDTO> peopleMap =
            AssembleUtil.collectionToMap(importHolder.getExistPeopleList(), MonitorBaseDTO::getName);

        int errorCount = 0;
        for (BindDTO bindDTO : importHolder.getImportList()) {
            if (!Objects.equals(MonitorTypeEnum.PEOPLE.getType(), bindDTO.getMonitorType())) {
                continue;
            }
            bindDTO.setOrgId(orgMap.get(bindDTO.getOrgName()));
            MonitorBaseDTO baseDTO = peopleMap.get(bindDTO.getName());
            if (Objects.isNull(baseDTO)) {
                peopleCount++;
                continue;
            }

            //校验监控对象是否已经绑定 对讲信息导入时即当对讲绑定字段不为空时，可以是绑定状态
            if (Objects.isNull(bindDTO.getIntercomBindType()) && Objects
                .equals(Vehicle.BindType.HAS_BIND, baseDTO.getBindType())) {
                bindDTO.setErrorMsg("【人员: " + bindDTO.getName() + "】已绑定");
                errorCount++;
                continue;
            }

            //人员已经存在，未绑定，但用户没有权限操作
            if (StringUtils.isBlank(importHolder.getOrgIdNameMap().get(baseDTO.getOrgId()))) {
                errorCount++;
                bindDTO.setErrorMsg("物人员已存在，不能重复导入");
                continue;
            }

            //校验db里的监控对象企业和导入的企业是否一致,若一致，赋值监控对象ID和监控对象的组织ID
            if (Objects.equals(bindDTO.getOrgId(), baseDTO.getOrgId())) {
                bindDTO.setId(baseDTO.getId());
                continue;
            }
            //若db里的监控对象企业和导入的企业不一致，则删除原来的人员，新增导入的人员
            delPeopleIds.add(baseDTO.getId());
            peopleCount++;
        }
        progressBar.setTotalProgress(1 + peopleCount * 3 / 2);
        //释放本地缓存
        importHolder.setExistPeopleList(null);
        return errorCount == 0;
    }

    @SneakyThrows
    private void deleteOldPeople(List<String> removingPeopleIds) {
        if (CollectionUtils.isNotEmpty(removingPeopleIds)) {
            peopleService.batchDel(removingPeopleIds);
        }
    }

    @Override
    public boolean addMysql() {
        deleteOldPeople(delPeopleIds);

        if (peopleCount == 0) {
            return true;
        }
        //1. 封装人员信息对象
        final String username = SystemHelper.getCurrentUsername();
        final Date createDate = new Date();
        List<PeopleDO> peopleDOList = new ArrayList<>();
        for (BindDTO bindDTO : importHolder.getImportList()) {
            if (!Objects.equals(MonitorTypeEnum.PEOPLE.getType(), bindDTO.getMonitorType())) {
                continue;
            }

            if (StringUtils.isNotBlank(bindDTO.getId())) {
                continue;
            }
            PeopleDTO peopleDTO = build(bindDTO);
            PeopleDO peopleDO = new PeopleDO(peopleDTO);
            peopleDO.setCreateDataUsername(username);
            peopleDO.setCreateDataTime(createDate);
            bindDTO.setId(peopleDO.getId());
            this.peopleList.add(peopleDTO);
            peopleDOList.add(peopleDO);
        }

        //2.存储数据到mysql
        partition(peopleDOList, peopleService::addBatch);
        return true;
    }

    private PeopleDTO build(BindDTO bindDTO) {
        PeopleDTO people = new PeopleDTO();
        people.setName(bindDTO.getName());
        people.setGender(GenderEnum.MALE.getCode());
        people.setOrgId(bindDTO.getOrgId());
        people.setOrgName(bindDTO.getOrgName());
        people.setJobId("default");
        people.setIsIncumbency(1);
        if (StringUtils.isNotBlank(bindDTO.getIntercomDeviceNumber())) {
            people.setIsIncumbency(2);
        }
        people.setMonitorType(MonitorTypeEnum.PEOPLE.getType());
        return people;

    }

    @Override
    public void addOrUpdateRedis() {
        peopleService.addOrUpdateRedis(peopleList, null);
    }
}

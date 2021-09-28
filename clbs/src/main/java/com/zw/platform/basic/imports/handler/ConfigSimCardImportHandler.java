package com.zw.platform.basic.imports.handler;

import com.zw.platform.basic.domain.SimCardDO;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.SimCardDTO;
import com.zw.platform.basic.imports.ConfigImportHolder;
import com.zw.platform.basic.rediscache.SimCardRedisCache;
import com.zw.platform.basic.repository.SimCardNewDao;
import com.zw.platform.basic.service.SimCardService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.util.AssembleUtil;
import com.zw.platform.util.BSJFakeIPUtil;
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
import java.util.stream.Collectors;

/**
 * 信息配置sim卡的导入
 * @author zhangjuan
 */
@Slf4j
public class ConfigSimCardImportHandler extends BaseImportHandler {
    private final ConfigImportHolder holder;
    private final SimCardService simCardService;
    private final SimCardNewDao simCardDao;
    /**
     * excel中是否存在需要新增的sim卡
     */
    private int simCardCount;
    private final List<String> removingSimIds = new ArrayList<>();
    private final List<SimCardDO> simCardList = new ArrayList<>();

    public ConfigSimCardImportHandler(ConfigImportHolder holder, SimCardService simCardService,
        SimCardNewDao simCardDao) {
        this.holder = holder;
        this.simCardService = simCardService;
        this.simCardDao = simCardDao;
        this.simCardCount = 0;
    }

    @Override
    public ImportModule module() {
        return ImportModule.CONFIG;
    }

    @Override
    public int stage() {
        return 2;
    }

    @Override
    public ImportTable[] tables() {
        return new ImportTable[] { ImportTable.ZW_M_SIM_CARD_INFO };
    }

    @Override
    public boolean uniqueValid() {
        Map<String, String> orgNameIdMap = holder.getOrgMap();
        Map<String, String> orgIdNameMap = holder.getOrgIdNameMap();
        Map<String, SimCardDTO> existSimCardMap = getExistSimCard();
        int errorCount = 0;
        for (BindDTO bindDTO : holder.getImportList()) {
            SimCardDTO existSim = existSimCardMap.get(bindDTO.getSimCardNumber());
            bindDTO.setSimCardOrgId(orgNameIdMap.get(bindDTO.getOrgName()));
            //sim卡在数据库中不存在，进行添加
            if (Objects.isNull(existSim)) {
                simCardCount++;
                continue;
            }

            // SIM卡在平台中存在并且车辆已经被绑定 对讲信息导入时即当对讲绑定字段不为空时，可以是绑定状态
            if (Objects.isNull(bindDTO.getIntercomBindType()) && existSim.getConfigId() != null) {
                bindDTO.setErrorMsg("【sim卡号: " + bindDTO.getSimCardNumber() + "】已绑定");
                errorCount++;
                continue;
            }

            //SIM卡已存在，未绑定，但用户没有操作权限（用户对SIM卡所属企业不可见）
            if (StringUtils.isBlank(orgIdNameMap.get(existSim.getOrgId()))) {
                bindDTO.setErrorMsg("SIM卡已存在，不能重复导入");
                errorCount++;
                continue;
            }
            if (Objects.equals(existSim.getOrgId(), bindDTO.getSimCardOrgId())) {
                bindDTO.setSimCardId(existSim.getId());
                bindDTO.setRealSimCardNumber(existSim.getRealId());
                continue;
            }

            removingSimIds.add(existSim.getId());
            simCardCount++;
        }
        holder.setExistSimMap(null);
        progressBar.setTotalProgress(1 + simCardCount * 3);
        return errorCount == 0;
    }

    private Map<String, SimCardDTO> getExistSimCard() {
        //对讲信息列表导入时特有逻辑，在对讲导入校验时会进行查询，这里是避免二次查询
        if (holder.getExistSimMap() != null) {
            return holder.getExistSimMap();
        }
        List<BindDTO> configs = holder.getImportList();
        List<String> simNumberList =
            configs.size() > 1000 ? null : configs.stream().map(BindDTO::getSimCardNumber).collect(Collectors.toList());
        List<SimCardDTO> simCardList = simCardDao.getByNumbers(simNumberList);
        return AssembleUtil.collectionToMap(simCardList, SimCardDTO::getSimcardNumber);

    }

    @SneakyThrows
    private void deleteOldSimCard(List<String> removingSimIds) {
        if (CollectionUtils.isNotEmpty(removingSimIds)) {
            simCardService.deleteBatch(removingSimIds);
        }
    }

    @Override
    public boolean addMysql() {
        deleteOldSimCard(removingSimIds);
        if (simCardCount <= 0) {
            return true;
        }

        final Date createDate = new Date();
        final String username = SystemHelper.getCurrentUsername();
        for (BindDTO bindDTO : holder.getImportList()) {
            if (StringUtils.isNotBlank(bindDTO.getSimCardId())) {
                continue;
            }

            SimCardDO simCardDO = new SimCardDO();
            simCardDO.setCreateDataTime(createDate);
            simCardDO.setCreateDataUsername(username);
            simCardDO.setRealId(bindDTO.getRealSimCardNumber());
            simCardDO.setFakeIP(BSJFakeIPUtil.integerMobileIPAddress(bindDTO.getSimCardNumber()));
            simCardDO.setSimcardNumber(bindDTO.getSimCardNumber());
            simCardDO.setOrgId(bindDTO.getSimCardOrgId());
            simCardDO.setIsStart(1);
            simCardDO.setFlag(1);
            simCardDO.setOperator("中国移动");
            simCardDO.setMonthlyStatement("01");
            simCardDO.setCorrectionCoefficient("100");
            simCardDO.setForewarningCoefficient("90");
            bindDTO.setSimCardId(simCardDO.getId());
            this.simCardList.add(simCardDO);
        }

        partition(this.simCardList, simCardDao::addByBatch);
        return true;
    }

    @Override
    public void addOrUpdateRedis() {
        if (simCardCount <= 0) {
            return;
        }
        SimCardRedisCache.addImportCache(this.simCardList);
    }
}

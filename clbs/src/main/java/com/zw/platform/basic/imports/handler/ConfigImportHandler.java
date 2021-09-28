package com.zw.platform.basic.imports.handler;

import com.google.common.base.Joiner;
import com.zw.platform.basic.constant.DateFormatKey;
import com.zw.platform.basic.constant.MonitorTypeEnum;
import com.zw.platform.basic.constant.Vehicle;
import com.zw.platform.basic.domain.ConfigDO;
import com.zw.platform.basic.domain.ConfigProfessionalDO;
import com.zw.platform.basic.domain.GroupMonitorDO;
import com.zw.platform.basic.domain.LifecycleDO;
import com.zw.platform.basic.domain.ProfessionalDO;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.imports.ConfigImportHolder;
import com.zw.platform.basic.repository.NewConfigDao;
import com.zw.platform.basic.repository.NewProfessionalsDao;
import com.zw.platform.basic.service.ConfigService;
import com.zw.platform.basic.service.GroupMonitorService;
import com.zw.platform.basic.service.LifecycleService;
import com.zw.platform.util.AssembleUtil;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.imports.lock.BaseImportHandler;
import com.zw.platform.util.imports.lock.ImportModule;
import com.zw.platform.util.imports.lock.ImportTable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 信息配置-绑定关系导入
 * @author create by zhangjuan on 2020/11/12.
 */
@Slf4j
public class ConfigImportHandler extends BaseImportHandler {
    private final ConfigImportHolder holder;
    private final NewConfigDao configDao;
    private final GroupMonitorService groupMonitorService;
    private final NewProfessionalsDao professionalsDao;
    private final LifecycleService lifecycleService;
    private final ConfigService configService;
    private static final Pattern PROFESSIONAL_SPLITTER = Pattern.compile("[,，]");

    public ConfigImportHandler(ConfigImportHolder holder, NewConfigDao configDao, LifecycleService lifecycleService,
        GroupMonitorService groupMonitorService, NewProfessionalsDao professionalsDao, ConfigService configService) {
        this.holder = holder;
        this.configDao = configDao;
        this.groupMonitorService = groupMonitorService;
        this.professionalsDao = professionalsDao;
        this.lifecycleService = lifecycleService;
        this.configService = configService;
    }

    @Override
    public ImportModule module() {
        return ImportModule.CONFIG;
    }

    @Override
    public int stage() {
        return 4;
    }

    @Override
    public ImportTable[] tables() {
        return new ImportTable[] { ImportTable.ZW_M_CONFIG, ImportTable.ZW_M_ASSIGNMENT_VEHICLE };
    }

    @Override
    public boolean addMysql() {
        List<BindDTO> importList = holder.getImportList();

        //封装服务周期
        List<LifecycleDO> lifecycleList = getLifecycleList(importList);

        List<ConfigDO> configList = new ArrayList<>();
        String curDate = DateUtil.formatDate(new Date(), DateFormatKey.YYYY_MM_DD_HH_MM_SS);
        importList.forEach(bindDTO -> {
            if (!Objects.equals(bindDTO.getBindType(), Vehicle.BindType.HAS_BIND)) {
                bindDTO.setBindDate(curDate);
                configList.add(new ConfigDO(bindDTO));
            }
        });
        //信息配置
        partition(configList, configDao::addByBatch);

        //服务周期 ==lifecycleService为空是对讲特有逻辑，对讲对象导入时没有服务周期，
        if (lifecycleService != null) {
            partition(lifecycleList, lifecycleService::addByBatch);
        }

        //从业人员
        List<ConfigProfessionalDO> professionalList = getProfessional(importList);
        partition(professionalList, configDao::bindProfessional);

        // 分组与监控对象
        partition(holder.getNewGroupMonitorList(), this::addGroupMonitor);
        return true;
    }

    @Override
    public void addOrUpdateRedis() {
        configService.addOrUpdateRedis(holder.getImportList());
    }

    private boolean addGroupMonitor(List<GroupMonitorDO> groupMonitorList) {
        return groupMonitorService.add(groupMonitorList, false);
    }

    private List<LifecycleDO> getLifecycleList(List<BindDTO> importList) {
        List<LifecycleDO> lifecycleList = new ArrayList<>();
        importList.forEach(bindDTO -> {
            if (StringUtils.isNotBlank(bindDTO.getBillingDate()) && StringUtils.isNotBlank(bindDTO.getExpireDate())) {
                Date billingDate = DateUtil.getStringToDate(bindDTO.getBillingDate(), DateFormatKey.YYYY_MM_DD);
                Date expireDate = DateUtil.getStringToDate(bindDTO.getExpireDate(), DateFormatKey.YYYY_MM_DD);
                LifecycleDO lifecycleDO = new LifecycleDO(billingDate, expireDate);
                bindDTO.setServiceLifecycleId(lifecycleDO.getId());
                lifecycleList.add(lifecycleDO);
            }
        });
        return lifecycleList;
    }

    private List<ConfigProfessionalDO> getProfessional(List<BindDTO> importList) {
        //对讲特有逻辑，对讲信息列表导入时没有从业人员到导入，可以直接跳过
        if (professionalsDao == null) {
            return null;
        }
        List<ProfessionalDO> professionalList = professionalsDao.getProfessionalsByNames(null);
        Map<String, String> professionalMap =
            AssembleUtil.collectionToMap(professionalList, ProfessionalDO::getName, ProfessionalDO::getId);
        List<ConfigProfessionalDO> configProfessionalList = new ArrayList<>();
        for (BindDTO bindDTO : importList) {
            String professionals = bindDTO.getProfessionalNames();
            if (StringUtils.isBlank(professionals)) {
                continue;
            }
            if (!Objects.equals(MonitorTypeEnum.VEHICLE.getType(), bindDTO.getMonitorType())) {
                continue;
            }
            Set<String> proArr = new HashSet<>(Arrays.asList(PROFESSIONAL_SPLITTER.split(professionals)));
            List<String> professionalNames = new ArrayList<>();
            List<String> professionalIds = new ArrayList<>();
            String configId = bindDTO.getConfigId();
            for (String professionalName : proArr) {
                String professionalId = professionalMap.get(professionalName);
                if (StringUtils.isNotBlank(professionalId)) {
                    professionalIds.add(professionalId);
                    professionalNames.add(professionalName);
                    configProfessionalList.add(new ConfigProfessionalDO(configId, professionalId));
                }
            }
            bindDTO.setProfessionalNames(Joiner.on(",").join(professionalNames));
            bindDTO.setProfessionalIds(Joiner.on(",").join(professionalIds));
        }
        return configProfessionalList;
    }
}

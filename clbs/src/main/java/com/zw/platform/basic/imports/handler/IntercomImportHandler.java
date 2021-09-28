package com.zw.platform.basic.imports.handler;

import com.google.common.collect.ImmutableMap;
import com.zw.platform.basic.constant.MonitorTypeEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.domain.ConfigDO;
import com.zw.platform.basic.domain.IntercomDO;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.imports.IntercomImportDTO;
import com.zw.platform.basic.imports.ConfigImportHolder;
import com.zw.platform.basic.repository.GroupMonitorDao;
import com.zw.platform.basic.repository.IntercomDao;
import com.zw.platform.basic.repository.NewConfigDao;
import com.zw.platform.basic.service.PeopleService;
import com.zw.platform.util.AssembleUtil;
import com.zw.platform.util.CommonUtil;
import com.zw.platform.util.FuzzySearchUtil;
import com.zw.platform.util.imports.lock.BaseImportHandler;
import com.zw.platform.util.imports.lock.ImportModule;
import com.zw.platform.util.imports.lock.ImportTable;
import com.zw.talkback.domain.intercom.info.OriginalModelInfo;
import com.zw.talkback.repository.mysql.OriginalModelDao;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 信息配置-绑定关系导入
 * @author create by zhangjuan on 2020/11/12.
 */
@Slf4j
public class IntercomImportHandler extends BaseImportHandler {
    private final ConfigImportHolder holder;
    private final PeopleService peopleService;
    private final IntercomDao intercomDao;
    private final List<IntercomImportDTO> importDTOList;
    private final OriginalModelDao originalModelDao;
    private final GroupMonitorDao groupMonitorDao;
    private final NewConfigDao configDao;

    private List<IntercomDO> intercomList;
    private List<String> peopleIds;
    private List<ConfigDO> updateConfigList;

    public IntercomImportHandler(ConfigImportHolder holder, List<IntercomImportDTO> importDTOList,
        PeopleService peopleService, IntercomDao intercomDao, OriginalModelDao originalModelDao,
        GroupMonitorDao groupMonitorDao, NewConfigDao configDao) {
        this.holder = holder;
        this.peopleService = peopleService;
        this.intercomDao = intercomDao;
        this.importDTOList = importDTOList;
        this.originalModelDao = originalModelDao;
        this.groupMonitorDao = groupMonitorDao;
        this.configDao = configDao;
    }

    @Override
    public ImportModule module() {
        return ImportModule.INTERCOM;
    }

    @Override
    public int stage() {
        return 5;
    }

    @Override
    public ImportTable[] tables() {
        return new ImportTable[] { ImportTable.ZW_M_CONFIG, ImportTable.ZW_M_ASSIGNMENT_VEHICLE,
            ImportTable.ZW_M_PEOPLE_INFO, ImportTable.ZW_M_INTERCOM_INFO };
    }

    @Override
    public boolean addMysql() {
        buildDbData();
        //信息列表入库
        partition(this.intercomList, intercomDao::addByBatch);

        //修改人员的离职状态
        partition(peopleIds, this::updateIncumbency);

        //更新监控对象分组的旋钮按钮
        partition(holder.getUpdateGroupMonitorList(), groupMonitorDao::updateKnobNo);

        //更新信息配置表里的对讲信息ID
        partition(updateConfigList, configDao::updateIntercomInfoId);
        return true;
    }

    private boolean updateIncumbency(Collection<String> peopleIds) {
        return peopleService.updateIncumbency(peopleIds, 2);
    }

    private void buildDbData() {
        List<OriginalModelInfo> modelList = originalModelDao.getOriginalModelList();
        Map<String, Long> modelMap =
            AssembleUtil.collectionToMap(modelList, OriginalModelInfo::getModelId, OriginalModelInfo::getIndex);
        //新增对讲列表
        intercomList = new ArrayList<>();
        //人员更新在离职状态
        peopleIds = new ArrayList<>();

        //更新config信息配置表的对讲信息ID
        updateConfigList = new ArrayList<>();
        List<String> numbers = holder.getNumbers();
        for (int i = 0; i < importDTOList.size(); i++) {
            IntercomImportDTO importDTO = importDTOList.get(i);
            BindDTO bindDTO = holder.getImportList().get(i);
            bindDTO.setCallNumber(numbers.get(i));
            bindDTO.setIntercomDeviceNumber(importDTO.getOriginalModel() + importDTO.getDeviceNumber());
            IntercomDO intercomDO = new IntercomDO(bindDTO, importDTO);
            intercomDO.setOriginalModelId(modelMap.get(importDTO.getOriginalModel()));
            intercomDO.setCustomerCode(holder.getCustomCode());
            intercomList.add(intercomDO);

            if (Objects.equals(MonitorTypeEnum.PEOPLE.getType(), bindDTO.getMonitorType())) {
                peopleIds.add(bindDTO.getId());
            }

            ConfigDO configDO = new ConfigDO();
            configDO.setId(bindDTO.getConfigId());
            configDO.setIntercomInfoId(intercomDO.getId());
            updateConfigList.add(configDO);
        }

    }

    @Override
    public void addOrUpdateRedis() {
        //对讲信息列表排序列表
        List<String> sortIdList = new ArrayList<>();
        //对讲信息模糊搜索缓存的field-value 映射Map
        int initialCapacity = CommonUtil.ofMapCapacity(holder.getImportList().size());
        Map<String, String> fuzzyMap = new HashMap<>(initialCapacity);
        //维护监控对象对讲相关信息的缓存map 监控对象信息redisKey - 对讲相关信息的key-value
        Map<RedisKey, Map<String, String>> monitorInfoMap = new HashMap<>(initialCapacity);
        for (BindDTO bindDTO : holder.getImportList()) {
            String monitorId = bindDTO.getId();
            sortIdList.add(monitorId);

            String intercomDevice = bindDTO.getIntercomDeviceNumber();
            String fuzzyField = FuzzySearchUtil
                .buildField(bindDTO.getMonitorType(), bindDTO.getName(), intercomDevice, bindDTO.getSimCardNumber());
            String fuzzyValue = FuzzySearchUtil.buildValue(monitorId, bindDTO.getDeviceId(), bindDTO.getSimCardId());
            fuzzyMap.put(fuzzyField, fuzzyValue);

            Map<String, String> updateInfoMap = ImmutableMap
                .of("intercomInfoId", bindDTO.getIntercomInfoId(), "intercomDeviceNumber", intercomDevice, "callNumber",
                    bindDTO.getCallNumber());
            monitorInfoMap.put(RedisKeyEnum.MONITOR_INFO.of(monitorId), updateInfoMap);
        }
        //维护顺序缓存
        RedisHelper.addToListTop(RedisKeyEnum.INTERCOM_SORT_LIST.of(), sortIdList);
        //维护模糊搜索缓存
        RedisHelper.addToHash(RedisKeyEnum.FUZZY_INTERCOM.of(), fuzzyMap);
        //维护监控对象的对讲信
        RedisHelper.batchAddToHash(monitorInfoMap);
    }
}

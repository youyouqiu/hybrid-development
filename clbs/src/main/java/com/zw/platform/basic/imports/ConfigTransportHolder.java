package com.zw.platform.basic.imports;

import com.google.common.collect.ImmutableMap;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.domain.CargoGroupVehicleDO;
import com.zw.platform.basic.domain.ConfigDO;
import com.zw.platform.basic.domain.DeviceDO;
import com.zw.platform.basic.domain.GroupDO;
import com.zw.platform.basic.domain.GroupMonitorDO;
import com.zw.platform.basic.domain.LifecycleDO;
import com.zw.platform.basic.domain.SimCardDO;
import com.zw.platform.basic.domain.VehicleDO;
import com.zw.platform.basic.dto.DeviceDTO;
import com.zw.platform.basic.dto.MonitorBaseDTO;
import com.zw.platform.basic.dto.SimCardDTO;
import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.platform.basic.dto.VehicleTypeDTO;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.util.imports.ProgressDetails;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 信息配置货运导入中间临时变量
 * @author zhangjuan
 */
@Data
public class ConfigTransportHolder {
    //*******************************************用于参数校验的临时参数**************************************************/
    /**
     * 货运是否开启
     */
    private boolean cargoReportSwitch;

    /**
     * 分组最大的监控对象数量
     */
    private Integer groupMaxMonitorNum;

    /**
     * 导入进度
     */
    private ProgressDetails progress;
    /**
     * 已经存在的车辆数据： 车牌号-监控对象
     */
    private Map<String, MonitorBaseDTO> existVehicleMap;

    /**
     * 已经存在的sim卡
     */
    private Map<String, SimCardDTO> existSimMap;
    /**
     * 已经存在的终端列表
     */
    private Map<String, DeviceDTO> existDeviceMap;

    private Map<String, OrganizationLdap> orgNameMap;

    private Map<String, OrganizationLdap> orgIdMap;

    private Map<String, VehicleTypeDTO> typeNameMap;

    private Set<String> importOrgIds = new HashSet<>();

    /**
     * 分组下监控对象的数量 分组ID -- 监控对象数量
     */
    private Map<String, Integer> groupMonitorNumMap = new HashMap<>();

    /**
     * 组织id-分组数量
     */
    private Map<String, Integer> orgGroupNumMap = new HashMap<>();

    /**
     * 组织id-分组最大序号
     */
    private Map<String, Integer> orgGroupMaxOrderNumMap = new HashMap<>();

    /**
     * 组织下货运分组 组织Id -分组Id、分组名称
     */
    private Map<String, Map<String, String>> orgTransportGroupMap = new HashMap<>();

    /**
     * 分组超限的组织
     */
    private Set<String> orgNameSet = new HashSet<>();

    //*********************************************Redis相关超做数据**************************************************/
    /**
     * 需要修改的车辆Id--即已经存在的车辆
     */
    private Set<String> updateVehicleIds = new HashSet<>();

    /**
     * 终端和SIM卡需要删除的模糊搜索缓存
     */
    private Set<String> delFuzzyFields = new HashSet<>();

    /**
     * 终端和SIM卡需要删除的企业下未绑定缓存
     */
    private Map<RedisKey, Collection<String>> delOrgUnbindMap = new HashMap<>(16);

    /**
     * 企业与终端或SIM卡的绑定关系缓存Map
     */
    private Map<RedisKey, Collection<String>> orgSimOrDeviceMap = new HashMap<>(16);

    /**
     * 新增终端
     */
    private List<String> newDeviceIds = new ArrayList<>();

    /**
     * 新增SIM卡
     */
    private List<String> newSimCardIds = new ArrayList<>();

    /**
     * 新增信息配置顺序缓存
     */
    private List<String> sortConfigList = new ArrayList<>();

    /**
     * 分组与监控对象缓存
     */
    private Map<RedisKey, Collection<String>> groupMonitorSetMap = new HashMap<>();

    /**
     * 维护终端类型分类的监控对象id和name的映射关系缓存
     */
    Map<RedisKey, Map<String, String>> protocolMap = new HashMap<>();

    //*********************************************db相关操作数据**************************************************/
    /**
     * 新增终端
     */
    private List<DeviceDO> deviceList = new ArrayList<>();

    /**
     * 新增sim卡
     */
    private List<SimCardDO> simCardList = new ArrayList<>();

    /**
     * 货运数据
     */
    private List<CargoGroupVehicleDO> cargoVehicleList = new ArrayList<>();

    /**
     * 新增车辆
     */
    private List<VehicleDO> newVehicleList = new ArrayList<>();

    /**
     * 新增分组
     */
    private List<GroupDO> newGroupList = new ArrayList<>();

    /**
     * 新增分组与监控对象的关系
     */
    private List<GroupMonitorDO> groupMonitorList = new ArrayList<>();

    /**
     * 新增信息配置
     */
    private List<ConfigDO> configList = new ArrayList<>();

    /**
     * 新增周期服务数据
     */
    private List<LifecycleDO> lifecycleList = new ArrayList<>();

    /**
     * 视频通道号
     */
    private Map<String, String> vehicleVideoChannelMap = new HashMap<>();

    public void addUpdateVehicleId(String vehicleId) {
        this.updateVehicleIds.add(vehicleId);
    }

    public void addDelFuzzyFields(String field) {
        this.delFuzzyFields.add(field);
    }

    public void addDelOrgUnbind(String orgId, String id, String type) {
        RedisKey redisKey = Objects.equals(type, "sim") ? RedisKeyEnum.ORG_UNBIND_SIM_CARD.of(orgId) :
            RedisKeyEnum.ORG_UNBIND_DEVICE.of(orgId);
        Collection<String> ids = this.delOrgUnbindMap.getOrDefault(redisKey, new HashSet<>());
        ids.add(id);
        this.delOrgUnbindMap.put(redisKey, ids);
    }

    private void addOrgSimOrDeviceMap(String orgId, String id, String type) {
        RedisKey redisKey;
        if (Objects.equals(type, "sim")) {
            redisKey = RedisKeyEnum.ORG_SIM_CARD.of(orgId);
            this.newSimCardIds.add(id);
        } else {
            redisKey = RedisKeyEnum.ORG_DEVICE.of(orgId);
            this.newDeviceIds.add(id);
        }
        Collection<String> ids = this.orgSimOrDeviceMap.getOrDefault(redisKey, new HashSet<>());
        ids.add(id);
        this.orgSimOrDeviceMap.put(redisKey, ids);
    }

    public void addDevice(DeviceDO deviceDO) {
        addOrgSimOrDeviceMap(deviceDO.getOrgId(), deviceDO.getId(), "device");
        this.deviceList.add(deviceDO);
    }

    public void addSimCard(SimCardDO simCardDO) {
        addOrgSimOrDeviceMap(simCardDO.getOrgId(), simCardDO.getId(), "sim");
        this.simCardList.add(simCardDO);
    }

    public void addLifecycle(LifecycleDO lifecycleDO) {
        this.lifecycleList.add(lifecycleDO);
    }

    public void addCargoGroupVehicle(CargoGroupVehicleDO cargoGroupVehicleDO) {
        this.cargoVehicleList.add(cargoGroupVehicleDO);
    }

    public void addVehicle(VehicleDO vehicleDO) {
        this.newVehicleList.add(vehicleDO);
    }

    public void addVideoChannelMap(String vehicleId, String terminalTypeId) {
        vehicleVideoChannelMap.put(vehicleId, terminalTypeId);
    }

    public void addOrgId(String orgId) {
        this.importOrgIds.add(orgId);
    }

    public void putOrgTransportGroup(String orgId, String groupId, String groupName) {
        Map<String, String> transportGroupMap = this.orgTransportGroupMap.getOrDefault(orgId, new HashMap<>(16));
        transportGroupMap.put(groupId, groupName);
        this.orgTransportGroupMap.put(orgId, transportGroupMap);
    }

    public void addOrgGroupCount(String orgId) {
        Integer orgGroupCount = this.orgGroupNumMap.getOrDefault(orgId, 0);
        this.orgGroupNumMap.put(orgId, orgGroupCount + 1);
    }

    public Integer getOrgGroupOrderNum(String orgId) {
        Integer orgOrderNum = orgGroupMaxOrderNumMap.get(orgId);
        orgOrderNum = Objects.isNull(orgOrderNum) ? 0 : orgOrderNum + 1;
        orgGroupMaxOrderNumMap.put(orgId, orgOrderNum);
        return orgOrderNum;
    }

    public Map<String, String> getCanUseGroup(String orgId) {
        Map<String, String> transportGroupMap = orgTransportGroupMap.get(orgId);
        if (transportGroupMap == null) {
            return null;
        }
        for (Map.Entry<String, String> entry : transportGroupMap.entrySet()) {
            Integer monitorNum = groupMonitorNumMap.getOrDefault(entry.getKey(), 0);
            if (monitorNum < groupMaxMonitorNum) {
                return ImmutableMap.of("groupId", entry.getKey(), "groupName", entry.getValue());
            }
        }

        return null;
    }

    public void addGroup(GroupDO groupDO) {
        //新增入db库的分组数据
        newGroupList.add(groupDO);
        //维护组织下货运分组
        putOrgTransportGroup(groupDO.getOrgId(), groupDO.getId(), groupDO.getName());
        //维护组织下的分组数量
        Integer groupNum = orgGroupNumMap.getOrDefault(groupDO.getOrgId(), 0);
        orgGroupNumMap.put(groupDO.getOrgId(), groupNum + 1);
    }

    public void addGroupMonitor(GroupMonitorDO groupMonitorDO) {
        groupMonitorList.add(groupMonitorDO);
        RedisKey redisKey = RedisKeyEnum.GROUP_MONITOR.of(groupMonitorDO.getGroupId());

        Collection<String> monitorIds = groupMonitorSetMap.getOrDefault(redisKey, new HashSet<>());
        monitorIds.add(groupMonitorDO.getVehicleId());
        groupMonitorSetMap.put(redisKey, monitorIds);
    }

    public void addConfig(ConfigDO configDO) {
        configList.add(configDO);
        sortConfigList.add(configDO.getMonitorId());
    }

    public void addProtocolMap(VehicleDTO vehicleDTO) {
        RedisKey redisKey = RedisKeyEnum.MONITOR_PROTOCOL.of(vehicleDTO.getDeviceType());
        Map<String, String> monitorMap = protocolMap.getOrDefault(redisKey, new HashMap<>(groupMonitorList.size()));
        monitorMap.put(vehicleDTO.getId(), vehicleDTO.getName());
        protocolMap.put(redisKey, monitorMap);
    }
}

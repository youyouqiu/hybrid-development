package com.zw.platform.basic.imports.handler;

import com.zw.platform.basic.constant.DateFormatKey;
import com.zw.platform.basic.constant.MonitorTypeEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.constant.Vehicle;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.domain.CargoGroupVehicleDO;
import com.zw.platform.basic.domain.ConfigDO;
import com.zw.platform.basic.domain.DeviceDO;
import com.zw.platform.basic.domain.GroupDO;
import com.zw.platform.basic.domain.GroupMonitorDO;
import com.zw.platform.basic.domain.LifecycleDO;
import com.zw.platform.basic.domain.SimCardDO;
import com.zw.platform.basic.domain.UserGroupDO;
import com.zw.platform.basic.domain.VehicleDO;
import com.zw.platform.basic.dto.DeviceDTO;
import com.zw.platform.basic.dto.GroupDTO;
import com.zw.platform.basic.dto.GroupMonitorDTO;
import com.zw.platform.basic.dto.MonitorBaseDTO;
import com.zw.platform.basic.dto.SimCardDTO;
import com.zw.platform.basic.dto.UserGroupDTO;
import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.platform.basic.dto.VehicleTypeDTO;
import com.zw.platform.basic.dto.imports.ConfigTransportImportDTO;
import com.zw.platform.basic.imports.ConfigTransportHolder;
import com.zw.platform.basic.repository.DeviceNewDao;
import com.zw.platform.basic.repository.NewConfigDao;
import com.zw.platform.basic.repository.SimCardNewDao;
import com.zw.platform.basic.service.GroupMonitorService;
import com.zw.platform.basic.service.GroupService;
import com.zw.platform.basic.service.LifecycleService;
import com.zw.platform.basic.service.UserGroupService;
import com.zw.platform.basic.service.VehicleService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.enums.PlateColor;
import com.zw.platform.domain.basicinfo.enums.VehicleColor;
import com.zw.platform.dto.constant.VehicleConstant;
import com.zw.platform.service.realTimeVideo.VideoParamSettingService;
import com.zw.platform.util.BSJFakeIPUtil;
import com.zw.platform.util.FuzzySearchUtil;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.imports.lock.BaseImportHandler;
import com.zw.platform.util.imports.lock.ImportModule;
import com.zw.platform.util.imports.lock.ImportTable;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cglib.beans.BeanCopier;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * ???????????? -- ????????????
 * @author zhangjuan
 */
public class ConfigTransportHandler extends BaseImportHandler {

    private final VehicleService vehicleService;
    private final GroupService groupService;
    private final GroupMonitorService groupMonitorService;
    private final UserGroupService userGroupService;
    private final DeviceNewDao deviceNewDao;
    private final SimCardNewDao simCardDao;
    private final NewConfigDao configDao;
    private final LifecycleService lifecycleService;
    private final VideoParamSettingService videoParamSettingService;
    /**
     * ??????????????????
     */
    private static final int GROUP_MAX_COUNT = 100;

    @Setter
    private List<ConfigTransportImportDTO> importList;

    @Setter
    private ConfigTransportHolder holder;

    private List<VehicleDTO> vehicleList;

    /**
     * ???????????????????????????????????????????????????
     */
    private List<UserGroupDTO> userGroupList;

    public ConfigTransportHandler(VehicleService vehicleService, GroupService groupService,
        GroupMonitorService groupMonitorService, UserGroupService userGroupService, DeviceNewDao deviceNewDao,
        SimCardNewDao simCardDao, NewConfigDao configDao, LifecycleService lifecycleService,
        VideoParamSettingService videoParamSettingService) {
        this.vehicleService = vehicleService;
        this.groupService = groupService;
        this.groupMonitorService = groupMonitorService;
        this.userGroupService = userGroupService;
        this.deviceNewDao = deviceNewDao;
        this.simCardDao = simCardDao;
        this.configDao = configDao;
        this.lifecycleService = lifecycleService;
        this.videoParamSettingService = videoParamSettingService;
    }

    @Override
    public ImportModule module() {
        return ImportModule.CONFIG;
    }

    @Override
    public ImportTable[] tables() {
        return new ImportTable[] { ImportTable.ZW_M_VEHICLE_INFO, ImportTable.ZW_M_ASSIGNMENT,
            ImportTable.ZW_M_ASSIGNMENT_USER, ImportTable.ZW_M_SIM_CARD_INFO, ImportTable.ZW_M_DEVICE_INFO,
            ImportTable.ZW_M_CONFIG, ImportTable.ZW_M_ASSIGNMENT_VEHICLE, ImportTable.ZW_M_VIDEO_CHANNEL_SETTING };
    }

    @Override
    public boolean uniqueValid() {
        prepareGroupData();
        buildData(importList);
        holder.getProgress().addProgress(5);
        return true;
    }

    @Override
    public boolean addMysql() {
        //????????????
        partition(holder.getNewGroupList(), groupService::addByBatch);
        holder.getProgress().addProgress(7);

        //????????????????????????????????????????????????????????????????????????????????????????????????
        this.userGroupList = groupService.getNewGroupOwnUser(holder.getNewGroupList());
        List<UserGroupDO> tempList = userGroupList.stream().map(UserGroupDO::new).collect(Collectors.toList());
        partition(tempList, userGroupService::batchAddToDb);
        holder.getProgress().addProgress(7);

        //????????????
        partition(holder.getNewVehicleList(), vehicleService::addBatch);
        holder.getProgress().addProgress(7);

        //????????????????????????
        partition(holder.getCargoVehicleList(), vehicleService::addCargoGroupVehicle);
        holder.getProgress().addProgress(7);

        //????????????
        partition(holder.getDeviceList(), deviceNewDao::addDeviceByBatch);
        holder.getProgress().addProgress(7);

        //??????SIM???
        partition(holder.getSimCardList(), simCardDao::addByBatch);
        holder.getProgress().addProgress(7);

        //??????????????????
        partition(holder.getConfigList(), configDao::addByBatch);
        holder.getProgress().addProgress(7);

        //??????????????????
        partition(holder.getLifecycleList(), lifecycleService::addByBatch);
        holder.getProgress().addProgress(7);

        //??????????????????????????????????????????
        partition(holder.getGroupMonitorList(), this::addGroupMonitor);
        holder.getProgress().addProgress(7);

        //?????????????????????
        videoParamSettingService.addBatchVideoChannelParam(holder.getVehicleVideoChannelMap());
        holder.getProgress().addProgress(7);
        return true;
    }

    private boolean addGroupMonitor(List<GroupMonitorDO> groupMonitorList) {
        return groupMonitorService.add(groupMonitorList, false);
    }

    @Override
    public void addOrUpdateRedis() {
        //???????????????????????????????????????
        groupService.addToRedis(holder.getNewGroupList());
        userGroupService.batchAddToRedis(this.userGroupList);

        //??????????????????????????????
        RedisHelper.addToListTop(RedisKeyEnum.DEVICE_SORT_LIST.of(), holder.getNewDeviceIds());
        //sim???????????????
        RedisHelper.addToListTop(RedisKeyEnum.SIM_CARD_SORT_LIST.of(), holder.getNewSimCardIds());
        //????????????????????????sim????????????
        RedisHelper.batchAddToSet(holder.getOrgSimOrDeviceMap());

        //????????????????????????????????????SIM?????????
        RedisHelper.batchDelSet(holder.getDelOrgUnbindMap());

        //???????????????sim?????????????????????????????????
        RedisHelper.hdel(RedisKeyEnum.FUZZY_MONITOR_DEVICE_SIMCARD.of(), holder.getDelFuzzyFields());
        holder.getProgress().addProgress(5);

        //??????????????????
        vehicleService.addOrUpdateRedis(vehicleList, holder.getUpdateVehicleIds());
        holder.getProgress().addProgress(10);

        //??????????????????????????????--??????????????????????????????????????????????????????????????????????????????
        RedisHelper.addToListTop(RedisKeyEnum.CONFIG_SORT_LIST.of(), holder.getSortConfigList());
        RedisHelper.batchAddToSet(holder.getGroupMonitorSetMap());
        RedisHelper.batchAddToHash(holder.getProtocolMap());
        holder.getProgress().addProgress(5);
    }

    private void buildData(List<ConfigTransportImportDTO> configList) {
        BeanCopier vehicleCopier = BeanCopier.create(ConfigTransportImportDTO.class, VehicleDTO.class, false);
        BeanCopier deviceCopier = BeanCopier.create(VehicleDTO.class, DeviceDO.class, false);
        List<VehicleDTO> vehicleList = new ArrayList<>();
        VehicleDTO vehicle;
        for (ConfigTransportImportDTO importDTO : configList) {
            //??????????????????
            vehicle = buildVehicle(importDTO, vehicleCopier);
            //????????????
            if (!handleGroup(vehicle)) {
                continue;
            }
            //??????????????????
            handleDevice(vehicle, importDTO, deviceCopier);
            //??????SIM?????????
            handleSimCard(vehicle);
            //??????????????????
            handleLifecycle(vehicle);
            vehicleList.add(vehicle);
        }
        //???????????????????????????
        vehicleService.setAdministrativeDivision(vehicleList);

        for (VehicleDTO vehicleDTO : vehicleList) {
            //??????????????????
            handleVehicle(vehicleDTO);
            //?????????????????????
            handleVideoChanelParam(vehicleDTO);
            //??????????????????
            handleConfig(vehicleDTO);
        }
        this.vehicleList = vehicleList;
    }

    private void handleConfig(VehicleDTO vehicle) {
        //????????????????????????????????????
        GroupMonitorDO groupMonitorDO =
            new GroupMonitorDO(vehicle.getId(), vehicle.getMonitorType(), vehicle.getGroupId());
        holder.addGroupMonitor(groupMonitorDO);
        //??????db???????????????
        vehicle.setVehiclePassword("000000");
        ConfigDO configDO = new ConfigDO(vehicle);
        holder.addConfig(configDO);
        //???????????????????????????????????????
        holder.addProtocolMap(vehicle);
    }

    private boolean handleGroup(VehicleDTO vehicle) {
        //??????????????????????????????Id
        String orgId = vehicle.getOrgId();
        Map<String, String> groupMap = holder.getCanUseGroup(orgId);
        String groupId;
        String groupName;
        //??????????????????????????????????????????
        if (groupMap == null || groupMap.isEmpty()) {
            if (holder.getOrgGroupNumMap().getOrDefault(orgId, 0) >= GROUP_MAX_COUNT) {
                if (!holder.getOrgNameSet().contains(vehicle.getOrgName())) {
                    holder.getOrgNameSet().add(vehicle.getOrgName());
                }
                return false;
            }
            groupName = "??????????????????" + holder.getOrgGroupOrderNum(orgId);
            groupId = UUID.randomUUID().toString();
            GroupDO groupDO = new GroupDO();
            groupDO.setId(groupId);
            groupDO.setOrgId(orgId);
            groupDO.setName(groupName);
            groupDO.setCreateDataTime(new Date());
            groupDO.setCreateDataUsername(SystemHelper.getCurrentUsername());
            groupDO.setFlag(1);
            groupDO.setTypes("0");
            holder.addGroup(groupDO);
        } else {
            groupId = groupMap.get("groupId");
            groupName = groupMap.get("groupName");
        }
        //????????????????????????????????????
        Integer monitorCount = holder.getGroupMonitorNumMap().getOrDefault(groupId, 0);
        holder.getGroupMonitorNumMap().put(groupId, monitorCount + 1);

        //??????????????????????????????????????????
        vehicle.setGroupId(groupId);
        vehicle.setGroupName(groupName);
        return true;
    }

    private void prepareGroupData() {
        //????????????????????????????????????
        List<GroupDTO> groupList = groupService.getGroupsByOrgIds(holder.getImportOrgIds());
        if (CollectionUtils.isEmpty(groupList)) {
            return;
        }

        //?????????????????????ID
        Set<String> groupIds = new HashSet<>();
        //????????????????????????????????????
        Set<String> userOwnGroupIds = userGroupService.getGroupMap().keySet();
        for (GroupDTO groupDTO : groupList) {
            String orgId = groupDTO.getOrgId();
            holder.addOrgGroupCount(orgId);
            String groupName = groupDTO.getName();
            if (!groupName.startsWith("??????????????????") || groupName.length() <= 6) {
                continue;
            }
            Integer orgGroupMaxOrderNum = holder.getOrgGroupMaxOrderNumMap().getOrDefault(orgId, 0);
            String orderNumStr = groupName.substring(6, groupName.length());
            Integer orderNum = Integer.valueOf(orderNumStr);
            if (orderNum > orgGroupMaxOrderNum) {
                holder.getOrgGroupMaxOrderNumMap().put(orgId, orderNum);
            }
            if (userOwnGroupIds.contains(groupDTO.getId())) {
                holder.putOrgTransportGroup(orgId, groupDTO.getId(), groupName);
            }
            groupIds.add(groupDTO.getId());
        }

        groupIds.retainAll(userOwnGroupIds);

        //???????????????????????????????????????????????????
        List<GroupMonitorDTO> list = groupMonitorService.getByGroupIds(groupIds);
        Map<String, Integer> groupMonitorNum =
            list.stream().collect(Collectors.groupingBy(GroupMonitorDTO::getGroupId, Collectors.summingInt(x -> 1)));
        holder.getGroupMonitorNumMap().clear();
        holder.getGroupMonitorNumMap().putAll(groupMonitorNum);
    }

    private void handleVideoChanelParam(VehicleDTO vehicle) {
        String terminalTypeId = vehicle.getTerminalTypeId();
        String monitorId = vehicle.getId();
        if (StringUtils.isNotEmpty(terminalTypeId)) {
            holder.addVideoChannelMap(monitorId, terminalTypeId);
        }
    }

    private void handleVehicle(VehicleDTO vehicle) {
        if (StringUtils.isBlank(vehicle.getId())) {
            holder.addVehicle(VehicleDO.build(vehicle, true));
        }

        //????????????
        if (holder.isCargoReportSwitch()) {
            holder.addCargoGroupVehicle(new CargoGroupVehicleDO(vehicle, "add"));
        }
    }

    private void handleLifecycle(VehicleDTO vehicle) {
        if (StringUtils.isBlank(vehicle.getExpireDate())) {
            return;
        }
        LifecycleDO lifecycleDO = new LifecycleDO(vehicle.getBillingDate(), vehicle.getExpireDate());
        vehicle.setServiceLifecycleId(lifecycleDO.getId());
        holder.addLifecycle(lifecycleDO);
    }

    private void handleSimCard(VehicleDTO vehicle) {
        SimCardDTO simCardDTO = holder.getExistSimMap().get(vehicle.getSimCardNumber());
        if (Objects.nonNull(simCardDTO)) {
            vehicle.setSimCardId(simCardDTO.getId());
            vehicle.setRealSimCardNumber(simCardDTO.getRealId());
            holder.addDelFuzzyFields(FuzzySearchUtil.buildSimCardField(simCardDTO.getSimcardNumber()));
            holder.addDelOrgUnbind(vehicle.getOrgId(), simCardDTO.getId(), "sim");
        } else {
            SimCardDO simCardDO = new SimCardDO();
            simCardDO.setIsStart(1);
            simCardDO.setFlag(1);
            simCardDO.setOperator("????????????");
            simCardDO.setOrgId(vehicle.getOrgId());
            simCardDO.setMonthlyStatement("01");
            simCardDO.setCorrectionCoefficient("100");
            simCardDO.setForewarningCoefficient("90");
            simCardDO.setSimcardNumber(vehicle.getSimCardNumber());
            simCardDO.setFakeIP(BSJFakeIPUtil.integerMobileIPAddress(vehicle.getSimCardNumber()));
            vehicle.setSimCardId(simCardDO.getId());
            holder.addSimCard(simCardDO);
        }

    }

    private void handleDevice(VehicleDTO vehicle, ConfigTransportImportDTO importDTO, BeanCopier deviceCopier) {
        DeviceDTO deviceDTO = holder.getExistDeviceMap().get(importDTO.getDeviceNumber());
        if (Objects.nonNull(deviceDTO)) {
            vehicle.setDeviceType(deviceDTO.getDeviceType());
            vehicle.setFunctionalType(deviceDTO.getFunctionalType());
            vehicle.setTerminalTypeId(deviceDTO.getTerminalTypeId());
            vehicle.setTerminalType(deviceDTO.getTerminalType());
            vehicle.setTerminalManufacturer(deviceDTO.getTerminalManufacturer());
            vehicle.setDeviceId(deviceDTO.getId());
            vehicle.setIsVideo(deviceDTO.getIsVideo());
            vehicle.setManufacturerId(deviceDTO.getManufacturerId());
            holder.addDelFuzzyFields(FuzzySearchUtil.buildDeviceField(deviceDTO.getDeviceNumber()));
            holder.addDelOrgUnbind(vehicle.getOrgId(), deviceDTO.getId(), "device");
        } else {
            vehicle.setIsVideo(1);
            vehicle.setDeviceType("1");
            vehicle.setFunctionalType("2");
            vehicle.setTerminalTypeId("default");
            vehicle.setTerminalManufacturer("[f]F3");
            vehicle.setTerminalType("F3-default");
            DeviceDO deviceDO = new DeviceDO();
            String deviceId = deviceDO.getId();
            deviceCopier.copy(vehicle, deviceDO, null);
            deviceDO.setId(deviceId);
            deviceDO.setCreateDataUsername(SystemHelper.getCurrentUsername());
            deviceDO.setCreateDataTime(new Date());
            deviceDO.setDeviceName(importDTO.getDeviceName());
            deviceDO.setManuFacturer(importDTO.getManuFacturer());
            deviceDO.setFlag(1);
            deviceDO.setIsStart(1);
            vehicle.setDeviceId(deviceId);
            holder.addDevice(deviceDO);
        }
    }

    private VehicleDTO buildVehicle(ConfigTransportImportDTO importDTO, BeanCopier vehicleCopier) {
        VehicleDTO vehicle = new VehicleDTO();
        vehicleCopier.copy(importDTO, vehicle, null);
        vehicle.setName(importDTO.getBrand());
        vehicle.setDeviceOrgId(vehicle.getOrgId());
        vehicle.setSimCardOrgId(vehicle.getOrgId());
        vehicle.setExpireDate(importDTO.getExpireTimeStr());
        vehicle.setMonitorType(MonitorTypeEnum.VEHICLE.getType());
        vehicle.setBindType(Vehicle.BindType.HAS_BIND);
        vehicle.setBindDate(DateUtil.formatDate(new Date(), DateFormatKey.YYYY_MM_DD_HH_MM_SS));
        vehicle.setDeviceOrgId(vehicle.getOrgId());
        vehicle.setSimCardOrgId(vehicle.getOrgId());
        //Str?????????????????????
        dealStrSuffixField(vehicle);

        MonitorBaseDTO monitor = holder.getExistVehicleMap().get(vehicle.getName());
        if (Objects.nonNull(monitor)) {
            vehicle.setId(monitor.getId());
            holder.addUpdateVehicleId(monitor.getId());
            return vehicle;
        }
        //????????????????????????
        VehicleTypeDTO vehicleType = holder.getTypeNameMap().get(vehicle.getVehicleTypeName());
        vehicle.setVehicleCategoryId(vehicleType.getCategoryId());
        vehicle.setVehicleCategoryName(vehicleType.getCategory());
        vehicle.setVehicleType(vehicleType.getId());
        vehicle.setIsStart(1);
        vehicle.setOperatingState(VehicleConstant.OPERATING_STATE_WORK);
        vehicle.setStandard(Vehicle.Standard.FREIGHT_TRANSPORT);

        return vehicle;
    }

    private void dealStrSuffixField(VehicleDTO vehicle) {
        //??????????????????
        String phoneCheckStr = vehicle.getPhoneCheckStr();
        Integer phoneCheck =
            StringUtils.isNotBlank(phoneCheckStr) ? Objects.equals("?????????", phoneCheckStr) ? 0 : 1 : null;
        vehicle.setPhoneCheck(phoneCheck);
        //????????????
        vehicle.setPlateColor(PlateColor.getCodeOrDefaultByName(vehicle.getPlateColorStr()));
        //????????????
        vehicle.setVehicleColor(VehicleColor.getCodeOrDefaultByName(vehicle.getVehicleColorStr()));

        //??????????????????
        String firstOnlineTimeStr = vehicle.getFirstOnlineTimeStr();
        if (StringUtils.isNotBlank(firstOnlineTimeStr)) {
            vehicle.setFirstOnlineTime(DateUtil.getStringToDate(firstOnlineTimeStr, DateFormatKey.YYYY_MM_DD_HH_MM_SS));
        }
        //??????????????????
        String vehicleProductionDateStr = vehicle.getVehicleProductionDateStr();
        if (StringUtils.isNotBlank(vehicleProductionDateStr)) {
            vehicle
                .setVehicleProductionDate(DateUtil.getStringToDate(vehicleProductionDateStr, DateFormatKey.YYYY_MM_DD));
        }

        //??????????????????
        String purchaseWayStr = vehicle.getPurchaseWayStr();
        Integer purchaseWay =
            StringUtils.isNotBlank(purchaseWayStr) ? Objects.equals(purchaseWayStr, "???????????????") ? 1 : 0 : null;
        vehicle.setPurchaseWay(purchaseWay);

        //??????????????????
        String validEndDateStr = vehicle.getValidEndDateStr();
        if (StringUtils.isNotBlank(validEndDateStr)) {
            vehicle.setValidEndDate(DateUtil.getStringToDate(validEndDateStr, DateFormatKey.YYYY_MM));
        }

        //?????????????????????
        String licenseIssuanceDateStr = vehicle.getLicenseIssuanceDateStr();
        if (StringUtils.isNotBlank(licenseIssuanceDateStr)) {
            vehicle.setValidEndDate(DateUtil.getStringToDate(licenseIssuanceDateStr, DateFormatKey.YYYY_MM_DD));
        }

        if (StringUtils.isNotBlank(vehicle.getExpireDate())) {
            vehicle.setBillingDate(DateUtil.formatDate(new Date(), DateFormatKey.YYYY_MM_DD));
        }

    }
}

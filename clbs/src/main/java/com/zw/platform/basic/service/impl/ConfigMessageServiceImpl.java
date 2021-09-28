package com.zw.platform.basic.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import com.zw.platform.basic.constant.DateFormatKey;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.MonitorTypeEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.constant.Vehicle;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.domain.ConfigDO;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.ConfigDTO;
import com.zw.platform.basic.dto.DeviceDTO;
import com.zw.platform.basic.dto.PeopleDTO;
import com.zw.platform.basic.dto.ThingDTO;
import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.platform.basic.event.VehicleUpdateEvent;
import com.zw.platform.basic.repository.NewConfigDao;
import com.zw.platform.basic.service.ConfigMessageService;
import com.zw.platform.basic.service.DeviceService;
import com.zw.platform.basic.service.IpAddressService;
import com.zw.platform.domain.infoconfig.form.MonitorInfo;
import com.zw.platform.domain.netty.BindInfo;
import com.zw.platform.domain.netty.DeviceUnbound;
import com.zw.platform.domain.topspeed_entering.DeviceRegister;
import com.zw.platform.push.handler.common.WebSocketMessageDispatchCenter;
import com.zw.platform.push.mqserver.ZMQFencePub;
import com.zw.platform.repository.modules.DeviceRegisterDao;
import com.zw.platform.service.oilsubsidy.ForwardVehicleManageService;
import com.zw.platform.util.AssembleUtil;
import com.zw.platform.util.BSJFakeIPUtil;
import com.zw.platform.util.CommonUtil;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.MsgUtil;
import com.zw.platform.util.common.DateUtil;
import com.zw.protocol.msg.Message;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import com.zw.protocol.util.ProtocolTypeUtil;
import com.zw.ws.common.MessageEncapsulationHelper;
import com.zw.ws.entity.MessageType;
import com.zw.ws.entity.vehicle.ClientVehicleInfo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 信息配置与其他端的的消息通信处理
 * @author zhangjuan
 */
@Service
public class ConfigMessageServiceImpl implements ConfigMessageService, IpAddressService {
    @Autowired
    private DeviceService deviceService;

    @Autowired
    private MonitorFactory monitorFactory;

    @Autowired
    private ForwardVehicleManageService forwardVehicleManageService;

    /**
     * 暂时先使用老的Dao类，后续若需要再进行修改
     */
    @Autowired
    private DeviceRegisterDao deviceRegisterDao;

    @Autowired
    private NewConfigDao configDao;

    @Autowired
    private WebSocketMessageDispatchCenter wsMessageDispatcher;

    @Override
    public BindInfo getSendToF3Data(ConfigDTO curBindDTO, BindDTO oldBindDTO) {
        MonitorInfo monitorInfo = monitorFactory.create(curBindDTO.getMonitorType()).getF3Data(curBindDTO.getId());
        if (Objects.isNull(monitorInfo)) {
            return null;
        }
        monitorInfo.setAuthCode(curBindDTO.getAuthCode());
        monitorInfo.setSimcardNumber(curBindDTO.getSimCardNumber());
        monitorInfo.setDeviceId(curBindDTO.getDeviceId());
        monitorInfo.setDeviceNumber(curBindDTO.getDeviceNumber());
        DeviceDTO deviceDTO = deviceService.findById(curBindDTO.getDeviceId());
        if (Objects.nonNull(deviceDTO)) {
            if (Objects.nonNull(deviceDTO.getInstallTime())) {
                monitorInfo.setInstallTime(deviceDTO.getInstallTime().getTime());
            }
            monitorInfo.setDeviceType(deviceDTO.getDeviceType());
            monitorInfo.setInstallCompany(deviceDTO.getInstallCompany());
            monitorInfo.setTelephone(deviceDTO.getTelephone());
            monitorInfo.setComplianceRequirements(deviceDTO.getComplianceRequirements());
            monitorInfo.setContacts(deviceDTO.getContacts());
        }
        monitorInfo.setPhone(curBindDTO.getSimCardNumber());
        monitorInfo.setSimcardNumber(curBindDTO.getSimCardNumber());
        monitorInfo.setAssignmentId(curBindDTO.getGroupId());
        monitorInfo.setAssignmentName(curBindDTO.getGroupName());
        monitorInfo.setProfessionalsName(curBindDTO.getProfessionalNames());
        monitorInfo.setTerminalType(curBindDTO.getTerminalType());
        monitorInfo.setTerminalManufacturer(curBindDTO.getTerminalManufacturer());
        monitorInfo.setAccessNetwork(curBindDTO.getAccessNetwork());

        BindInfo bindInfo = new BindInfo();
        bindInfo.setDeviceId(curBindDTO.getDeviceId());
        String deviceType = curBindDTO.getDeviceType();
        String identification =
            getIdentification(curBindDTO.getDeviceNumber(), deviceType, curBindDTO.getSimCardNumber());
        if ("8".equals(deviceType)) {
            monitorInfo.setFakeIp(identification);
        }
        bindInfo.setIdentification(identification);
        bindInfo.setDeviceType(deviceType);

        BindDTO oldBind = Objects.isNull(oldBindDTO) ? curBindDTO : oldBindDTO;
        String oldIdentification =
            getIdentification(oldBind.getDeviceNumber(), oldBind.getDeviceType(), oldBind.getSimCardNumber());
        bindInfo.setOldIdentification(oldIdentification);
        bindInfo.setOldDeviceType(oldBind.getDeviceType());
        bindInfo.setMonitorInfo(JSONObject.parseObject(JSONObject.toJSONString(monitorInfo)));
        String uniqueNumber = curBindDTO.getUniqueNumber();
        if (StringUtils.isNotBlank(uniqueNumber)) {
            String[] sign = uniqueNumber.split("（");
            if (sign.length == 2) {
                // 根据唯一标识查询制造商id和终端型号
                DeviceRegister drInfo = deviceRegisterDao.getRegisterInfo(sign[0].trim());
                if (Objects.nonNull(drInfo)) {
                    bindInfo.setManufacturerId(drInfo.getManufacturerId());
                    bindInfo.setDeviceModelNumber(drInfo.getDeviceModelNumber());
                }
            }
        }
        bindInfo.setAuthCode(curBindDTO.getAuthCode());
        return bindInfo;
    }

    @Override
    public boolean sendToF3(ConfigDTO curBindDTO, BindDTO oldBindDTO) {
        if (Objects.nonNull(oldBindDTO)) {
            if (!isSend(curBindDTO, oldBindDTO)) {
                return true;
            }
            String deviceId = curBindDTO.getDeviceId();
            String oldDeviceId = oldBindDTO.getDeviceId();
            String monitorId = curBindDTO.getId();
            String oldMonitorId = oldBindDTO.getId();
            if (!Objects.equals(deviceId, oldDeviceId) || !Objects.equals(monitorId, oldMonitorId)) {
                RedisHelper.delete(HistoryRedisKeyEnum.ALARM_PUSH_SET_MONITOR_ID.of(oldMonitorId));
                RedisHelper.delete(HistoryRedisKeyEnum.ALARM_PUSH_SET_DEVICE_ID.of(oldDeviceId));
                WebSubscribeManager.getInstance().sendMsgToAll("3_" + oldDeviceId, ConstantUtil.WEB_ALARM_REMOVE);
            }

            if (!Objects.equals(monitorId, oldMonitorId)) {
                RedisHelper.delete(HistoryRedisKeyEnum.MONITOR_STATUS.of(oldMonitorId));
                RedisHelper.delete(HistoryRedisKeyEnum.MONITOR_LOCATION.of(oldMonitorId));
            }

        }
        BindInfo bindInfo = getSendToF3Data(curBindDTO, oldBindDTO);
        String curIdentify = bindInfo.getIdentification() + bindInfo.getDeviceType();
        String oldIdentify = bindInfo.getOldIdentification() + bindInfo.getOldDeviceType();
        //删除标识发生改变，删除监控对象的状态缓存
        if (!Objects.equals(curIdentify, oldIdentify)) {
            RedisHelper.delete(HistoryRedisKeyEnum.MONITOR_STATUS.of(curBindDTO.getId()));
            if (Objects.nonNull(oldBindDTO)) {
                RedisHelper.delete(HistoryRedisKeyEnum.MONITOR_STATUS.of(oldBindDTO.getId()));
            }
        }
        //油补809新增相关字段数据组装
        forwardVehicleManageService.initOilBindInfos(Collections.singletonList(bindInfo));
        RedisHelper.delete(HistoryRedisKeyEnum.DEVICE_BIND.of(bindInfo.getOldDeviceId(), bindInfo.getOldDeviceType()));
        WebSubscribeManager.getInstance().sendMsgToAll(bindInfo, ConstantUtil.WEB_DEVICE_BOUND);

        if (Objects.isNull(oldBindDTO)) {
            Set<String> subVehicle = new HashSet<>();
            subVehicle.add(curBindDTO.getId());
            WebSubscribeManager.getInstance().updateSubStatus(subVehicle);
        }
        return true;
    }

    private boolean isSend(BindDTO curBindDTO, BindDTO oldBindDTO) {
        if (Objects.isNull(oldBindDTO)) {
            return true;
        }
        //当是修改时 --只有修改了监控对象、终端以及SIM卡才下发到协议端
        boolean monitorChange = !Objects.equals(oldBindDTO.getName(), curBindDTO.getName());
        boolean deviceChange = !Objects.equals(oldBindDTO.getDeviceNumber(), curBindDTO.getDeviceNumber());
        boolean simChange = !Objects.equals(oldBindDTO.getSimCardNumber(), curBindDTO.getSimCardNumber());
        boolean groupIsChange = !Objects.equals(oldBindDTO.getGroupName(), curBindDTO.getGroupName());
        boolean professionalIsChange =
            !Objects.equals(oldBindDTO.getProfessionalNames(), curBindDTO.getProfessionalNames());
        if (!(monitorChange || deviceChange || simChange || groupIsChange || professionalIsChange)) {
            return false;
        }
        return true;
    }

    @Override
    public void sendToWeb(BindDTO curBindDTO, BindDTO oldBindDTO) {
        String oldMonitorId = oldBindDTO.getId();
        if (!RedisHelper.isContainsKey(HistoryRedisKeyEnum.MONITOR_STATUS.of(oldMonitorId))) {
            return;
        }

        //修改了监控对象、终端以、SIM卡、分组、从业人员等信息才下发（信息配置需要重新下发到协议端），才进行通知
        if (!isSend(curBindDTO, oldBindDTO)) {
            return;
        }

        List<ClientVehicleInfo> clientVehicleList = buildClientVehicleInfo(oldBindDTO.getId(), oldBindDTO.getName());
        final Message msg = MsgUtil.getMsg(MessageType.BS_CLIENT_REQUEST_VEHICLE_CACHE_UP_INTO, clientVehicleList);
        wsMessageDispatcher.pushCacheStatusNew(msg);
    }

    @EventListener
    public void listenVehicleUpdateEvent(VehicleUpdateEvent vehicleUpdateEvent) {
        List<VehicleDTO> curVehicleList = vehicleUpdateEvent.getCurVehicleList();

        //获取车辆对应的终端信息
        List<String> deviceIds =
            curVehicleList.stream().filter(o -> StringUtils.isNotBlank(o.getDeviceId())).map(VehicleDTO::getDeviceId)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(deviceIds)) {
            return;
        }
        List<DeviceDTO> deviceList = deviceService.getDeviceListByIds(deviceIds);
        Map<String, DeviceDTO> deviceMap = AssembleUtil.collectionToMap(deviceList, DeviceDTO::getId);

        //绑定的车辆
        List<String> monitorIds = new ArrayList<>();
        List<BindInfo> bindInfoList = new ArrayList<>();
        List<RedisKey> redisKeys = new ArrayList<>();
        for (VehicleDTO vehicleDTO : curVehicleList) {
            if (!Objects.equals(vehicleDTO.getBindType(), Vehicle.BindType.HAS_BIND)) {
                continue;
            }
            MonitorInfo monitorInfo = monitorFactory.getVehicleService().getF3Data(vehicleDTO);
            BindInfo bindInfo = new BindInfo();
            DeviceDTO deviceDTO = deviceMap.get(vehicleDTO.getDeviceId());
            if (Objects.nonNull(deviceDTO)) {
                if (Objects.nonNull(deviceDTO.getInstallTime())) {
                    monitorInfo.setInstallTime(deviceDTO.getInstallTime().getTime());
                }
                monitorInfo.setInstallCompany(deviceDTO.getInstallCompany());
                monitorInfo.setTelephone(deviceDTO.getTelephone());
                monitorInfo.setDeviceType(deviceDTO.getDeviceType());
                monitorInfo.setComplianceRequirements(deviceDTO.getComplianceRequirements());
                monitorInfo.setContacts(deviceDTO.getContacts());
                bindInfo.setManufacturerId(deviceDTO.getManufacturerId());
                bindInfo.setDeviceModelNumber(deviceDTO.getDeviceModelNumber());
            }
            bindInfo.setDeviceId(vehicleDTO.getDeviceId());
            String deviceType = vehicleDTO.getDeviceType();
            String identification =
                getIdentification(vehicleDTO.getDeviceNumber(), deviceType, vehicleDTO.getSimCardNumber());
            if ("8".equals(deviceType)) {
                monitorInfo.setFakeIp(identification);
            }
            bindInfo.setIdentification(identification);
            bindInfo.setDeviceType(deviceType);

            bindInfo.setOldIdentification(identification);
            bindInfo.setOldDeviceType(deviceType);
            bindInfo.setMonitorInfo(JSONObject.parseObject(JSONObject.toJSONString(monitorInfo)));

            bindInfo.setAuthCode(vehicleDTO.getAuthCode());
            bindInfoList.add(bindInfo);
            monitorIds.add(vehicleDTO.getId());
            redisKeys.add(HistoryRedisKeyEnum.DEVICE_BIND.of(bindInfo.getOldDeviceId(), bindInfo.getOldDeviceType()));
        }

        if (CollectionUtils.isEmpty(bindInfoList)) {
            return;
        }

        forwardVehicleManageService.initOilBindInfos(bindInfoList);
        RedisHelper.delete(HistoryRedisKeyEnum.MONITOR_LOCATION.of(monitorIds));
        RedisHelper.delete(redisKeys);
        for (BindInfo bindInfo : bindInfoList) {
            WebSubscribeManager.getInstance().sendMsgToAll(bindInfo, ConstantUtil.WEB_DEVICE_BOUND);
        }
    }

    /**
     * 构建客户端监控对象信息
     * @param monitorId   监控对象ID
     * @param monitorName 监控对象名称
     * @return 信息
     */
    private List<ClientVehicleInfo> buildClientVehicleInfo(String monitorId, String monitorName) {
        ClientVehicleInfo clientVehicleInfo = new ClientVehicleInfo();
        clientVehicleInfo.setVehicleId(monitorId);
        clientVehicleInfo.setVehicleStatus(3);
        clientVehicleInfo.setSpeed("0");
        clientVehicleInfo.setBrand(monitorName);
        clientVehicleInfo.setLatestGpsDate(new Date());
        return Collections.singletonList(clientVehicleInfo);
    }

    @Override
    public DeviceUnbound getDeviceUnbound(String deviceId, String deviceNum, String deviceType, String simCardNum) {
        String identification = getIdentification(deviceNum, deviceType, simCardNum);
        DeviceUnbound deviceUnbound = new DeviceUnbound();
        deviceUnbound.setIdentification(identification);
        deviceUnbound.setDeviceId(deviceId);
        deviceUnbound.setDeviceType(deviceType);
        return deviceUnbound;
    }

    @Override
    public void sendToWeb(List<BindDTO> deleteBindList) {
        List<String> webSocketMsgList = new ArrayList<>();
        Set<String> monitorIds = new HashSet<>();
        for (BindDTO bindDTO : deleteBindList) {
            //维护web端监控对象的相关订阅
            if (RedisHelper.isContainsKey(HistoryRedisKeyEnum.MONITOR_STATUS.of(bindDTO.getId()))) {
                List<ClientVehicleInfo> list = buildClientVehicleInfo(bindDTO.getId(), bindDTO.getName());
                String msg = MessageEncapsulationHelper
                    .webSocketMessageEncapsulation(list, MessageType.BS_CLIENT_REQUEST_VEHICLE_CACHE_UP_INTO);
                webSocketMsgList.add(msg);
                monitorIds.add(bindDTO.getId());
            }
        }
        wsMessageDispatcher.batchUnbindCacheStatusNew(webSocketMsgList, monitorIds);
    }

    @Override
    public void sendUnBindToF3(Collection<DeviceUnbound> deviceUnBindList) {
        if (CollectionUtils.isEmpty(deviceUnBindList)) {
            return;
        }
        // 推送终端下线和解绑指令
        for (DeviceUnbound deviceUnbound : deviceUnBindList) {
            WebSubscribeManager.getInstance()
                .sendMsgToAll(deviceUnbound.getDeviceId(), ConstantUtil.WEB_DEVICE_OFF_LINE);
            WebSubscribeManager.getInstance().sendMsgToAll(deviceUnbound, ConstantUtil.WEB_DEVICE_UNBOUND);
        }
    }

    @Override
    public void sendUnBindToStorm(Set<String> vehicleIds, Set<String> peopleIds, Set<String> thingIds) {
        Set<String> monitorIds = new HashSet<>();
        if (CollectionUtils.isNotEmpty(vehicleIds)) {
            ZMQFencePub.pubChangeFence("1," + StringUtils.join(vehicleIds, ","));
            monitorIds.addAll(vehicleIds);
        }
        if (CollectionUtils.isNotEmpty(peopleIds)) {
            ZMQFencePub.pubChangeFence("2");
            monitorIds.addAll(peopleIds);
        }
        if (CollectionUtils.isNotEmpty(thingIds)) {
            monitorIds.addAll(thingIds);
            ZMQFencePub.pubChangeFence("17");
        }
        if (CollectionUtils.isNotEmpty(monitorIds)) {
            ZMQFencePub.pubChangeFence("16,0," + StringUtils.join(monitorIds, ","));
        }
    }

    @Override
    public void sendToStorm(String monitorType, String monitorId) {
        switch (monitorType) {
            case "0":
                ZMQFencePub.pubChangeFence("1," + monitorId);
                break;
            case "1":
                ZMQFencePub.pubChangeFence("2");
                break;
            case "2":
                ZMQFencePub.pubChangeFence("17");
                break;
            default:
                break;
        }
    }

    @Override
    public void sendToStorm(BindDTO curBindDTO, BindDTO oldBindDTO) {
        switch (curBindDTO.getMonitorType()) {
            case "0":
                String curVehicleId = curBindDTO.getId();
                String beforeVehicleId = oldBindDTO.getId();
                if (Objects.equals(curVehicleId, beforeVehicleId)) {
                    ZMQFencePub.pubChangeFence("1," + curVehicleId);
                } else {
                    ZMQFencePub.pubChangeFence("1," + beforeVehicleId + "," + curVehicleId);
                    ZMQFencePub.pubChangeFence("16,0," + beforeVehicleId);
                }
                break;
            case "1":
                ZMQFencePub.pubChangeFence("2");
                break;
            case "2":
                ZMQFencePub.pubChangeFence("17");
                break;
            default:
                break;
        }
    }

    @Override
    public void sendToF3(Collection<String> monitorIdSet) {
        if (CollectionUtils.isEmpty(monitorIdSet)) {
            return;
        }
        List<BindDTO> bindList = new ArrayList<>();
        for (MonitorTypeEnum typeEnum : MonitorTypeEnum.values()) {
            if (monitorIdSet.size() == bindList.size()) {
                break;
            }
            bindList.addAll(monitorFactory.create(typeEnum.getType()).getByIds(monitorIdSet));
        }

        List<String> deviceIds = new ArrayList<>();
        Map<RedisKey, Map<String, String>> monitorRedisKeyMap =
            new HashMap<>(CommonUtil.ofMapCapacity(monitorIdSet.size()));
        for (BindDTO bindDTO : bindList) {
            if (StringUtils.isNotBlank(bindDTO.getDeviceId())) {
                deviceIds.add(bindDTO.getDeviceId());
            }
            if (StringUtils.isNotBlank(bindDTO.getGroupId()) && StringUtils.isNotBlank(bindDTO.getGroupName())) {
                Map<String, String> groupMap =
                    ImmutableMap.of("groupId", bindDTO.getGroupId(), "groupName", bindDTO.getGroupName());
                monitorRedisKeyMap.put(RedisKeyEnum.MONITOR_INFO.of(bindDTO.getId()), groupMap);
            }
        }
        //更新监控对象信息分组
        RedisHelper.batchAddToHash(monitorRedisKeyMap);
        if (deviceIds.isEmpty()) {
            return;
        }
        List<DeviceDTO> deviceList = deviceService.getDeviceListByIds(deviceIds);
        Map<String, DeviceDTO> deviceMap = AssembleUtil.collectionToMap(deviceList, DeviceDTO::getId);
        Map<String, Integer> accessNetworkMap = AssembleUtil
            .collectionToMap(configDao.getByMonitorIds(monitorIdSet), ConfigDO::getMonitorId,
                ConfigDO::getAccessNetwork);

        //绑定的监控对象
        List<String> monitorIds = new ArrayList<>();
        List<BindInfo> bindInfoList = new ArrayList<>();
        List<RedisKey> redisKeys = new ArrayList<>();
        for (BindDTO bindDTO : bindList) {
            if (!Objects.equals(bindDTO.getBindType(), Vehicle.BindType.HAS_BIND)) {
                continue;
            }
            MonitorInfo monitorInfo;
            if (bindDTO instanceof VehicleDTO) {
                VehicleDTO vehicleDTO = (VehicleDTO) bindDTO;
                monitorInfo = monitorFactory.getVehicleService().getF3Data(vehicleDTO);
            } else {
                monitorInfo = new MonitorInfo();
                monitorInfo.setMonitorType(Integer.valueOf(bindDTO.getMonitorType()));
                monitorInfo.setMonitorId(bindDTO.getId());
                monitorInfo.setMonitorName(bindDTO.getName());
                monitorInfo.setGroupId(bindDTO.getOrgId());
                monitorInfo.setGroupName(bindDTO.getGroupName());
                if (bindDTO instanceof PeopleDTO) {
                    PeopleDTO peopleDTO = (PeopleDTO) bindDTO;
                    monitorInfo.setIdentity(peopleDTO.getIdentity());
                }
                if (bindDTO instanceof ThingDTO) {
                    ThingDTO thingDTO = (ThingDTO) bindDTO;
                    monitorInfo.setLabel(thingDTO.getLabel());
                    monitorInfo.setMaterial(thingDTO.getMaterial());
                    monitorInfo.setModel(thingDTO.getModel());
                    monitorInfo.setWeight(thingDTO.getWeight() == null ? null : String.valueOf(thingDTO.getWeight()));
                    monitorInfo.setSpec(thingDTO.getSpec());
                    Date productDate = thingDTO.getProductDate();
                    String productDateStr = Objects.isNull(productDate) ? null :
                        DateUtil.getDateToString(productDate, DateFormatKey.YYYY_MM_DD);
                    monitorInfo.setProductDate(productDateStr);
                }
            }
            monitorInfo.setAccessNetwork(accessNetworkMap.get(bindDTO.getId()));
            BindInfo bindInfo = new BindInfo();
            DeviceDTO deviceDTO = deviceMap.get(bindDTO.getDeviceId());
            buildBindInfo(bindInfo, monitorInfo, bindDTO, deviceDTO);
            bindInfoList.add(bindInfo);
            monitorIds.add(bindDTO.getId());
            redisKeys.add(HistoryRedisKeyEnum.DEVICE_BIND.of(bindInfo.getOldDeviceId(), bindInfo.getOldDeviceType()));
        }

        if (CollectionUtils.isEmpty(bindInfoList)) {
            return;
        }

        forwardVehicleManageService.initOilBindInfos(bindInfoList);
        RedisHelper.delete(HistoryRedisKeyEnum.MONITOR_LOCATION.of(monitorIds));
        RedisHelper.delete(redisKeys);
        for (BindInfo bindInfo : bindInfoList) {
            WebSubscribeManager.getInstance().sendMsgToAll(bindInfo, ConstantUtil.WEB_DEVICE_BOUND);
        }

    }

    private void buildBindInfo(BindInfo bindInfo, MonitorInfo monitorInfo, BindDTO bindDTO, DeviceDTO deviceDTO) {
        monitorInfo.setAuthCode(bindDTO.getAuthCode());
        monitorInfo.setSimcardNumber(bindDTO.getSimCardNumber());
        monitorInfo.setDeviceId(bindDTO.getDeviceId());
        monitorInfo.setDeviceNumber(bindDTO.getDeviceNumber());
        monitorInfo.setPhone(bindDTO.getSimCardNumber());
        monitorInfo.setSimcardNumber(bindDTO.getSimCardNumber());
        monitorInfo.setAssignmentId(bindDTO.getGroupId());
        monitorInfo.setAssignmentName(bindDTO.getGroupName());
        monitorInfo.setTerminalType(bindDTO.getTerminalType());
        monitorInfo.setTerminalManufacturer(bindDTO.getTerminalManufacturer());
        if (Objects.nonNull(deviceDTO)) {
            if (Objects.nonNull(deviceDTO.getInstallTime())) {
                monitorInfo.setInstallTime(deviceDTO.getInstallTime().getTime());
            }
            monitorInfo.setComplianceRequirements(deviceDTO.getComplianceRequirements());
            monitorInfo.setInstallCompany(deviceDTO.getInstallCompany());
            monitorInfo.setTelephone(deviceDTO.getTelephone());
            monitorInfo.setDeviceType(deviceDTO.getDeviceType());
            monitorInfo.setContacts(deviceDTO.getContacts());
            bindInfo.setManufacturerId(deviceDTO.getManufacturerId());
            bindInfo.setDeviceModelNumber(deviceDTO.getDeviceModelNumber());
        }
        bindInfo.setDeviceId(bindDTO.getDeviceId());
        String deviceType = bindDTO.getDeviceType();
        String identification = getIdentification(bindDTO.getDeviceNumber(), deviceType, bindDTO.getSimCardNumber());
        if ("8".equals(deviceType)) {
            monitorInfo.setFakeIp(identification);
        }
        bindInfo.setIdentification(identification);
        bindInfo.setDeviceType(deviceType);

        bindInfo.setOldIdentification(identification);
        bindInfo.setOldDeviceType(deviceType);
        bindInfo.setMonitorInfo(JSONObject.parseObject(JSONObject.toJSONString(monitorInfo)));

        bindInfo.setAuthCode(bindDTO.getAuthCode());

    }

    private String getIdentification(String deviceNum, String deviceType, String simCardNum) {
        String identification;
        if ("8".equals(deviceType)) {
            identification = BSJFakeIPUtil.integerMobileIPAddress(simCardNum);
        } else if (ProtocolTypeUtil.checkAllDeviceType(deviceType)) {
            identification = simCardNum;
        } else {
            identification = deviceNum;
        }
        return identification;
    }
}

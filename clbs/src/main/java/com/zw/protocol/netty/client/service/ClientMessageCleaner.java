/*
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved. This software is the confidential and proprietary information
 * of ZhongWei, Inc. You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with ZhongWei.
 */

package com.zw.protocol.netty.client.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.dto.PeopleDTO;
import com.zw.platform.basic.service.F3MessageService;
import com.zw.platform.basic.service.PeopleService;
import com.zw.platform.domain.basicinfo.form.OBDVehicleDataInfo;
import com.zw.platform.push.common.SimpMessagingTemplateUtil;
import com.zw.platform.push.controller.SubscibeInfo;
import com.zw.platform.push.controller.SubscibeInfoCache;
import com.zw.platform.push.handler.common.WebSocketMessageDispatchCenter;
import com.zw.platform.repository.modules.ParameterDao;
import com.zw.platform.service.functionconfig.LineService;
import com.zw.platform.service.monitoring.RealTimeService;
import com.zw.platform.service.obdManager.OBDVehicleTypeService;
import com.zw.platform.service.oil.PositionalService;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.common.AddressUtil;
import com.zw.platform.util.common.DelayedEventTrigger;
import com.zw.platform.util.spring.InitData;
import com.zw.protocol.msg.Message;
import com.zw.protocol.msg.bd.BdtdLocationMessageBody;
import com.zw.protocol.msg.t808.T808Message;
import com.zw.protocol.msg.t808.body.LocationInfo;
import com.zw.protocol.msg.t808.body.T808GpsInfo;
import com.zw.ws.OutputControlSendStatusDO;
import com.zw.ws.common.WebSocketMessageType;
import com.zw.ws.entity.t808.location.AddressParam;
import com.zw.ws.entity.t808.location.RecievedLocationMessage;
import com.zw.ws.entity.vehicle.ClientVehicleInfo;
import com.zw.ws.entity.vehicle.VehiclePositionalInfo;
import com.zw.ws.entity.vehicle.VehicleStatus;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

@Component
@Log4j2
public class ClientMessageCleaner {

    @Autowired
    private WebSocketMessageDispatchCenter wsMessageDispatcher;

    @Autowired
    private RealTimeService realTimeService;

    @Autowired
    LineService lineService;

    @Autowired
    private PositionalService positionalService;

    @Autowired
    private F3MessageService f3MessageService;

    @Autowired
    private PeopleService peopleService;

    @Autowired
    private InitData initData;

    @Autowired
    private DelayedEventTrigger trigger;

    @Autowired
    private SimpMessagingTemplateUtil simpMessagingTemplateUtil;

    @Autowired
    private ParameterDao parameterDao;

    @Autowired
    private OBDVehicleTypeService obdVehicleTypeService;

    /**
     * 手环位置信息
     */
    public void bdtdLocationMessage(String locationJson) throws Exception {
        RecievedLocationMessage<BdtdLocationMessageBody> peopleLocationMessage =
            JSON.parseObject(locationJson, new TypeReference<RecievedLocationMessage<BdtdLocationMessageBody>>() {
            });
        String deviceId = peopleLocationMessage.getData().getMsgBody().getUserCode();
        final String x = String.valueOf(peopleLocationMessage.getData().getMsgBody().getLatitude());
        final String y = String.valueOf(peopleLocationMessage.getData().getMsgBody().getLongitude());
        final String time = String.valueOf(peopleLocationMessage.getData().getMsgBody().getVtime());
        PeopleDTO people = peopleService.getByDeviceNum(deviceId);
        peopleLocationMessage.getData().getMsgBody().setId(people.getId());
        peopleLocationMessage.getData().getMsgBody().setMonitorObject(people.getName());
        peopleLocationMessage.getData().getMsgBody().setSIMCard(people.getSimCardNumber());
        peopleLocationMessage.getData().getMsgBody().setGroup(people.getGroupName());
        peopleLocationMessage.getData().getMsgBody().setPushAlarmSet("32");
        Integer stateInfo = getCacheState(people.getId());
        peopleLocationMessage.getData().getMsgBody().setStateInfo(stateInfo);
        // ======================================//
        AddressParam addressParam = new AddressParam();
        addressParam.setGps_latitude(x);
        addressParam.setGps_longitude(y);
        addressParam.setDecID(people.getId());
        addressParam.setTime(time);
        String formattedAddress = AddressUtil.inverseAddress(y, x).getFormattedAddress();
        peopleLocationMessage.getData().getMsgBody().setFormattedAddress(formattedAddress);
        wsMessageDispatcher.pushMessageToAllClient(peopleLocationMessage.getDesc().getDId(), peopleLocationMessage,
            WebSocketMessageType.VEHICLE_LOCATION);
    }

    /**
     * 获取缓存状态
     */
    public Integer getCacheState(String vid) {
        String csInfo = RedisHelper.getString(HistoryRedisKeyEnum.MONITOR_STATUS.of(vid));
        Integer stateInfo = 3;
        if (csInfo != null) {
            ClientVehicleInfo clientVehicleInfo = JSON.parseObject(csInfo, ClientVehicleInfo.class);
            stateInfo = clientVehicleInfo.getVehicleStatus();
        }
        return stateInfo;
    }

    /**
     * 车辆位置信息查询应答
     * @author Liubangquan
     */
    public void getVehicleLastLocation(String reviceMsg) {
        // 发送到客户端
        wsMessageDispatcher.pushVehicleLocationToClient(reviceMsg);
    }

    /**
     * 1025视频资源列表应答
     */
    public void getResourceList(String msg) throws Exception {
        wsMessageDispatcher.pushResourceListToClient(msg);
    }

    /**
     * 102F视频资源日期应答
     */
    public void getResourceDateList(String msg) {
        wsMessageDispatcher.pushResourceDateListToClient(msg);
    }

    public void getLocationInfo(Message message) {
        String monitorId = message.getDesc().getMonitorId();
        //从原始新中获取监控808信息
        T808Message t808Message = JSON.parseObject(message.getData().toString(), T808Message.class);
        JSONObject messageInfo = JSON.parseObject(t808Message.getMsgBody().toString(), JSONObject.class);
        JSONObject gpsInfo = messageInfo.getJSONObject("gpsInfo");
        LocationInfo info = JSON.parseObject(t808Message.getMsgBody().toString(), LocationInfo.class);
        if (gpsInfo != null) {
            convertGpsInfo(info, gpsInfo);
        }
        t808Message.setMsgBody(info);
        message.setData(t808Message);

        //处理位置信息里面的数据
        f3MessageService.buildWebLocationMsg(info, monitorId, false);

        // 推送处理OBD数据
        String deviceId = message.getDesc().getDeviceId();
        final OBDVehicleDataInfo obdVehicleDataInfo = obdVehicleTypeService.convertStreamToObdInfo(info);
        info.setObdObjStr(JSON.toJSONString(obdVehicleDataInfo));
        Optional.ofNullable(info.getObd())
                .map(o -> o.getJSONArray("streamList"))
                .filter(CollectionUtils::isNotEmpty)
                .ifPresent(o -> wsMessageDispatcher.pushMessageToAllClient(
                        deviceId, obdVehicleDataInfo, WebSocketMessageType.MONITOR_OBD_INFO));

        // 存储状态信息
        if (ConstantUtil.T808_VEHICLE_CONTROL_ACK == message.getDesc().getMsgID()) {
            // 如果是0x0500
            if (!RedisHelper.isContainsKey(HistoryRedisKeyEnum.MONITOR_STATUS.of(monitorId))) {
                info.setStateInfo(VehicleStatus.NOT_LOCATE);
            }
            Integer msgNoAck = messageInfo.getInteger("msgSNAck");
            trigger.cancelEvent(monitorId + "," + msgNoAck);
            SubscibeInfo subscibeInfo = SubscibeInfoCache.getInstance().getUserNameByMsgSnDid(msgNoAck, deviceId);
            if (subscibeInfo != null) {
                SubscibeInfoCache.getInstance().delTable(subscibeInfo);
                String sessionId = subscibeInfo.getSessionId();
                String directiveId = subscibeInfo.getDirectiveId();
                if (StringUtils.isNotBlank(sessionId) && StringUtils.isNotBlank(directiveId)) {
                    int status = messageInfo.getIntValue("result");
                    parameterDao.updateStatusById(directiveId, status);
                    simpMessagingTemplateUtil.sendStatusMsgBySessionId(sessionId, ConstantUtil.WEBSOCKET_OUTPUT_CONTROL,
                        new OutputControlSendStatusDO(monitorId, status));
                }
            }
        } else {
            info.setStateInfo(messageInfo.getInteger("vehicleStatus"));
        }

        // 推送
        t808Message.setMsgBody(info);
        message.setData(t808Message);
        wsMessageDispatcher.pushMessageToAllClient(deviceId, message, WebSocketMessageType.VEHICLE_LOCATION);
        if (Objects.equals(message.getDesc().getMessageType(), 0)) {
            // 向车辆位置信息列表添加最新信息
            VehiclePositionalInfo vehiclePositionalInfo =
                new VehiclePositionalInfo(info, message.getDesc().getMonitorName(), monitorId);
            initData.addVehiclePositionalInfo(vehiclePositionalInfo);
        }
    }

    private void convertGpsInfo(LocationInfo info, JSONObject gpsInfo) {
        T808GpsInfo t808GpsInfo = JSON.parseObject(gpsInfo.toJSONString(), T808GpsInfo.class);
        info.setStatus(t808GpsInfo.getStatus());
        info.setGpsTime(t808GpsInfo.getTime());
        info.setUploadtime(t808GpsInfo.getUploadtime());
        info.setAcc((int) (t808GpsInfo.getStatus() & 0x1));
        info.setProtocolType(t808GpsInfo.getProtocolType());
        info.setGpsSpeed(t808GpsInfo.getSpeed());
        info.setLatitude(t808GpsInfo.getLatitude());
        info.setLongitude(t808GpsInfo.getLongitude());
        info.setAltitude(t808GpsInfo.getAltitude());
        info.setDirection(t808GpsInfo.getDirection());
        info.setMileageSensor(t808GpsInfo.getMileageSensor());
        info.setOilExpend(t808GpsInfo.getOilExpend());
        info.setOilMass(t808GpsInfo.getOilMass());
        info.setTemperatureSensor(t808GpsInfo.getTemperatureSensor());
        info.setGpsAttachInfoList(t808GpsInfo.getGpsAttachInfoList());
        info.setSignalState(1L);
        info.setSignalStrength(t808GpsInfo.getSignalStrength());
        info.setBatteryVoltage(Double.parseDouble(t808GpsInfo.getBatteryVoltage().toString()));
    }

    /**
     * 设置卫星颗数
     * @deprecated 4.3.7
     */
    public static void setSatellitesNumber(LocationInfo info, JSONArray gpsAttachInfoList) {
        if (CollectionUtils.isEmpty(gpsAttachInfoList)) {
            return;
        }
        Optional<Object> optional = gpsAttachInfoList.stream()
            .filter(gpsAttachInfo -> Objects.equals(((JSONObject) gpsAttachInfo).getInteger("gpsAttachInfoID"), 0x31))
            .findFirst();
        if (optional.isPresent()) {
            JSONObject gpsAttr = (JSONObject) optional.get();
            info.setSatellitesNumber(gpsAttr.getInteger("GNSSNumber"));
        }
    }

    /**
     * 单位转换
     * @deprecated 4.3.7 移动到f3MessageReceiveService.unitConversion
     */
    public static void unitConversion(LocationInfo info) {
        // 油量传感器
        JSONArray oilMass = info.getOilMass();
        if (oilMass != null && oilMass.size() > 0) {
            for (int i = 0, len = oilMass.size(); i < len; i++) {
                JSONObject oilMassJsonObj = oilMass.getJSONObject(i);
                //燃油温度
                Double oilTem = oilMassJsonObj.getDouble("oilTem");
                if (oilTem != null) {
                    String oilTemStr =
                        String.valueOf(BigDecimal.valueOf(oilTem).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
                    oilTemStr =
                        oilTemStr.endsWith(".0") ? oilTemStr.substring(0, oilTemStr.lastIndexOf(".0")) : oilTemStr;
                    oilMassJsonObj.put("oilTem", oilTemStr);
                }
                //环境温度
                Double envTem = oilMassJsonObj.getDouble("envTem");
                if (envTem != null) {
                    String envTemStr =
                        String.valueOf(BigDecimal.valueOf(envTem).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
                    envTemStr =
                        envTemStr.endsWith(".0") ? envTemStr.substring(0, envTemStr.lastIndexOf(".0")) : envTemStr;
                    oilMassJsonObj.put("envTem", envTemStr);
                }
            }
        }
        // 温度传感器
        JSONArray temperatureSensor = info.getTemperatureSensor();
        if (temperatureSensor != null && temperatureSensor.size() > 0) {
            for (int i = 0, len = temperatureSensor.size(); i < len; i++) {
                JSONObject temperatureSensorJsonObj = temperatureSensor.getJSONObject(i);
                Integer temperature = temperatureSensorJsonObj.getInteger("temperature");
                if (temperature != null) {
                    String temperatureStr = String.valueOf(temperature / 10.0);
                    temperatureStr =
                        temperatureStr.endsWith(".0") ? temperatureStr.substring(0, temperatureStr.lastIndexOf(".0")) :
                            temperatureStr;
                    temperatureSensorJsonObj.put("temperature", temperatureStr);
                }
            }
        }
    }

}

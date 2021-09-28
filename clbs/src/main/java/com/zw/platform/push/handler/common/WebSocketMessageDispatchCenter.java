package com.zw.platform.push.handler.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.service.F3MessageService;
import com.zw.platform.controller.realTimeVideo.ResourceListController;
import com.zw.platform.domain.enmu.ProtocolEnum;
import com.zw.platform.domain.infoconfig.form.MonitorInfo;
import com.zw.platform.domain.realTimeVideo.ResourceListBean;
import com.zw.platform.domain.realTimeVideo.VideoFTPForm;
import com.zw.platform.domain.reportManagement.form.LogSearchForm;
import com.zw.platform.push.common.SimpMessagingTemplateUtil;
import com.zw.platform.push.controller.InstanceMessageController;
import com.zw.platform.push.controller.SubscibeInfo;
import com.zw.platform.push.controller.SubscibeInfoCache;
import com.zw.platform.push.controller.UserCache;
import com.zw.platform.push.controller.UserCacheA;
import com.zw.platform.push.controller.UserCacheS;
import com.zw.platform.service.basicinfo.DeviceService;
import com.zw.platform.service.monitoring.impl.RealTimeServiceImpl;
import com.zw.platform.service.obdManager.OBDVehicleTypeService;
import com.zw.platform.service.oil.PositionalService;
import com.zw.platform.service.realTimeVideo.ResourceListService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.common.AddressUtil;
import com.zw.platform.util.common.AlarmTypeUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MonitorUtils;
import com.zw.platform.util.spring.InitData;
import com.zw.protocol.msg.Message;
import com.zw.protocol.msg.RtpMessage;
import com.zw.protocol.msg.t808.T808Message;
import com.zw.protocol.msg.t808.T808MsgBody513;
import com.zw.protocol.msg.t808.T808MsgHead;
import com.zw.protocol.msg.t808.body.LocationInfo;
import com.zw.protocol.msg.t808.body.T808GpsInfo;
import com.zw.protocol.netty.client.service.ClientMessageCleaner;
import com.zw.ws.common.WebSocketMessageType;
import com.zw.ws.entity.t808.location.AddressParam;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Created by jiangxiaoqiang on 2016/10/19.
 */
@Component
@Log4j2
public class WebSocketMessageDispatchCenter {
    /**
     * 日期转换格式
     */
    private static final String DATE_FORMAT1 = "yyyyMMddHHmmss";

    private static final String DATE_FORMAT2 = "yyyy-MM-dd";

    private static Logger logger = LogManager.getLogger(WebSocketMessageDispatchCenter.class);

    /**
     * 音视频磁盘信息
     */
    private static final String WEBSOCKET_DISK_INFO = "/topic/diskInfo";

    /**
     * 位置信息查询应答
     */
    private static final String WEBSOCKET_ADDRESS_REALINFO_URL = "/topic/realLocation";

    private static final String WEBSOCKET_ADDRESS_REALINFOP_URL = "/topic/realLocationP";

    private static final String WEBSOCKET_ADDRESS_REALINFOS_URL = "/topic/realLocationS";

    /**
     * 实时视频资源列表应答
     */
    private static final String WEBSOCKET_RESOURCELIST_URL = "/topic/video/history/day";

    /**
     * 实时视频资源日期列表应答
     */
    private static final String WEBSOCKET_RESOURCEDATELIST_URL = "/topic/video/history/month";

    /**
     * 实时视频资源日期app列表应答
     */
    private static final String APP_WEBSOCKET_RESOURCEDATELIST_URL = "/topic/appResourceDateList";

    /**
     * OBD故障码
     */
    public static final String WEBSOCKET_OBD_FAULT = "/topic/obdFault";

    /**
     * 平台巡检应答
     */
    public static final String PLATFORM_INSPECTION_ACK_URL = "/topic/inspection";

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private RealTimeServiceImpl realTimeServiceImpl;

    @Autowired
    private RealTimeServiceImpl realTime;

    @Autowired
    private SimpMessagingTemplateUtil simpMessagingTemplateUtil;

    @Autowired
    private LogSearchService logSearchService;
    @Autowired
    private ClientMessageCleaner clientMessageCleaner;

    @Autowired
    private ResourceListService resourceListService;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private InitData initData;

    @Autowired
    private PositionalService positionalService;

    @Autowired
    private F3MessageService f3MessageService;

    @Autowired
    private OBDVehicleTypeService obdVehicleTypeService;

    /**
     * 构造函数
     */
    public WebSocketMessageDispatchCenter() {
    }

    /**
     * 推送消息给所有客户端
     */
    public void pushMessageToAllClient(String id, Object message, int wsMessageType) {
        switch (wsMessageType) {
            case WebSocketMessageType.VEHICLE_LOCATION:
                simpMessagingTemplateUtil.sendT808Position(message, id);
                break;
            case WebSocketMessageType.VEHICLE_ALARM:
                simpMessagingTemplateUtil.sendT808Alarm(message, id);
                break;
            case WebSocketMessageType.VEHICLE_STATUS:
                simpMessagingTemplateUtil.sendT808Status(message, id);
                break;
            case WebSocketMessageType.RISK_LOCATION:
                simpMessagingTemplateUtil.sendRiskPosition(message, id);
                break;
            case WebSocketMessageType.VEHICLE_ALARM_GLOBAL:
                simpMessagingTemplateUtil.sendGlobalAlarm(message, id);
                break;
            case WebSocketMessageType.VEHICLE_MEDIA:
                RtpMessage rtpMessage = JSON.parseObject(message.toString(), RtpMessage.class);
                JSONObject msgBody = JSON.parseObject(rtpMessage.getMsgBody().toString());
                // 存储ftp存储记录
                if (msgBody != null) {
                    Integer channelNum = msgBody.getInteger("channelNumber");
                    String vehicleId = msgBody.getString("vehicleId");
                    // 音视频关闭，并且为实时视频请求视频的时候存储ftp记录
                    try {
                        Date startTime = DateUtils.parseDate("20" + msgBody.getString("startTime"), DATE_FORMAT1);
                        Date endTime = DateUtils.parseDate("20" + msgBody.getString("endTime"), DATE_FORMAT1);
                        String ftpUrl = msgBody.getString("path");
                        String fileName = ftpUrl.substring(ftpUrl.lastIndexOf("/") + 1, ftpUrl.indexOf("."));
                        VideoFTPForm videoFTPForm =
                            VideoFTPForm.builder().vehicleId(vehicleId).url(ftpUrl).name(fileName)
                                .channelNumber(channelNum).startTime(startTime).endTime(endTime).alarmType(0L).type(1)
                                .uploadTime(new Date()).build();
                        resourceListService.insertFTPRecord(videoFTPForm);
                    } catch (ParseException e) {
                        log.info("时间转换异常！");
                    }
                    simpMessagingTemplate
                        .convertAndSendToUser("admin", ConstantUtil.WEB_SOCKET_T808_MEDIAINFO, message);
                }
                break;
            case WebSocketMessageType.SPECIAL_REPORT:
                // TODO 这里只有联动策略中的, 实时视频会触发, 这里直接取message的data即可, 后端组装好数据
                simpMessagingTemplateUtil.sendSpecialReport(message, id);
                break;
            case WebSocketMessageType.MONITOR_OBD_INFO:
                simpMessagingTemplateUtil.sendObdInfo(message, id);
                break;
            case WebSocketMessageType.VEHICLE_SOSALARM:
                simpMessagingTemplateUtil.sendSOSAlarm(message, id);
                break;
            default:
                log.error("不支持的WebSocket推送消息类型，websocketMessageType：" + wsMessageType);
                break;
        }
    }

    /**
     * 推送缓存的状态信息
     */
    public synchronized void pushCacheStatusInfo(String user, String content) {
        pushInfoImpl(user, ConstantUtil.WEB_SOCKET_T808_STATUS, content);
    }

    public void pushCacheStatusNew(Message message) {
        if (Objects.isNull(message)) {
            return;
        }

        Object messageData = message.getData();
        if (Objects.nonNull(messageData)) {
            JSONObject data;
            if (messageData instanceof JSONArray) {
                data = ((JSONArray) messageData).getJSONObject(0);
            } else {
                data = JSON.parseArray(JSON.toJSONString(messageData)).getJSONObject(0);
            }
            String vehicleId = data.getString("vehicleId");
            Integer status = data.getInteger("vehicleStatus");
            //返回车辆的分组信息用于实时监控组织树信息动态更新
            BindDTO config = MonitorUtils.getBindDTO(vehicleId);
            if (config != null) {
                String[] assignmentIds = config.getGroupId() == null ? null : config.getGroupId().split(",");
                data.put("assignmentIds", assignmentIds);
            } else {
                data.put("assignmentIds", new String[0]);
            }
            JSONArray jsonArray = new JSONArray();
            jsonArray.add(data);
            message.setData(jsonArray);

            initData.changeVehicleStatus(vehicleId, status);
            if (StringUtils.isNotEmpty(vehicleId)) {
                pushMessageToAllClient(vehicleId, message, WebSocketMessageType.VEHICLE_STATUS);
            }
        }
    }

    /**
     * 批量解除车辆绑定状态
     * @param webSocketMessageList webSocketMessageList
     * @param monitorIds           监控对象ID
     */
    public void batchUnbindCacheStatusNew(List<String> webSocketMessageList, Set<String> monitorIds) {
        initData.batchUnbindVehicleStatus(monitorIds);
        for (String msg : webSocketMessageList) {
            JSONObject jsonObject = JSON.parseObject(msg);
            JSONArray clientVehicleList = jsonObject.getJSONArray("data");
            if (CollectionUtils.isNotEmpty(clientVehicleList)) {
                String vehicleId = clientVehicleList.getJSONObject(0).getString("vehicleId");
                pushMessageToAllClient(vehicleId, jsonObject, WebSocketMessageType.VEHICLE_STATUS);
            }
        }
    }

    /**
     * 推送给所有用户
     */
    private void pushInfoImpl(String url, String content) {
        if (simpMessagingTemplate != null) {
            simpMessagingTemplate.convertAndSend(url, content);
        }
    }

    /**
     * 推送给指定用户
     */
    public void pushInfoImpl(String user, String url, String content) {
        if (simpMessagingTemplate != null) {
            simpMessagingTemplate.convertAndSendToUser(user, url, content);
        }
    }

    /**
     * 推送给指定sessionId
     */
    public void pushMsgToUser(String sessionId, String destination, String content) {
        if (simpMessagingTemplate != null) {
            final SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
            headerAccessor.setLeaveMutable(true);
            headerAccessor.setSessionId(sessionId);
            simpMessagingTemplate
                .convertAndSendToUser(sessionId, destination, content, headerAccessor.getMessageHeaders());
        }
    }

    /**
     * 推送磁盘信息
     */
    public void pushFtpDiskToClient(String message) {
        pushInfoImpl(WEBSOCKET_DISK_INFO, message);
    }

    /**
     * 推送资源列表数据信息
     * @author wangjianyu
     */

    public void pushResourceListToClient(String message) throws Exception {
        // 记录日志
        addResourceListLog(message);
        JSONObject jsonObject = JSON.parseObject(message);
        JSONObject data = jsonObject.getJSONObject("data");
        JSONObject msgHead = data.getJSONObject("msgHead");
        String deviceType = msgHead.getString("deviceType");
        JSONObject msgBody = data.getJSONObject("msgBody");
        String ack = msgBody.getString("msgSn");
        String deviceId = jsonObject.getJSONObject("desc").getString("deviceId");
        String vid = jsonObject.getJSONObject("desc").getString("monitorId");
        JSONArray resourcesList = msgBody.getJSONArray("resourcesList");
        JSONArray resourcesListNew = new JSONArray();
        for (Object object : resourcesList) {
            JSONObject resource = JSONObject.parseObject(object.toString());
            int channelNum;
            if (ProtocolEnum.T808_2011_1078.getDeviceType().equals(deviceType)) {
                //1078报批稿
                ResourceListBean res = ResourceListController.T808_2011_1078_CACHE.getIfPresent(vid);
                if (res == null) {
                    continue;
                }
                resource.put("channelNum", res.getChannlNumer());
                resource.put("videoType", res.getVideoType());
                channelNum = Integer.parseInt(res.getChannlNumer());
            } else {
                channelNum = resource.getInteger("channelNum");
            }
            //逻辑通道号给前端
            resource.put("logicChannel", channelNum);
            resourcesListNew.add(resource);
        }
        msgBody.put("resourcesList", resourcesListNew);
        data.put("msgBody", msgBody);
        data.put("msgSn", ack);
        jsonObject.put("data", data);
        if (StringUtils.isNotBlank(ack)) {
            String content = jsonObject.toJSONString();
            pushInfoImpl("/topic/forward/resource/" + vid, content);
            SubscibeInfo info = SubscibeInfoCache.getInstance().getUserNameByMsgSnDid(ack, deviceId);
            if (info != null) {
                String sessionId = info.getSessionId();
                if (StringUtils.isNotBlank(sessionId)) {
                    pushMsgToUser(sessionId, WEBSOCKET_RESOURCELIST_URL,
                        JSON.toJSONString(new JsonResultBean(jsonObject)));
                }
                // 移除
                SubscibeInfoCache.getInstance().delTable(info);
            }
        }
    }

    /**
     * 推送资源列表数据信息
     * @author lijie
     */
    public void pushResourceDateListToClient(String message) {
        JSONObject jsonObject = JSON.parseObject(message);
        String deviceId = jsonObject.getJSONObject("desc").getString("deviceId");
        Long monthData = jsonObject.getJSONObject("data").getJSONObject("msgBody").getLong("monthData");
        String msgSnAck = jsonObject.getJSONObject("data").getJSONObject("msgBody").getString("msgSNAck");
        if (StringUtils.isNotBlank(msgSnAck)) {
            SubscibeInfo info = SubscibeInfoCache.getInstance().getUserNameByMsgSnDid(msgSnAck, deviceId);
            if (info != null) {
                String userName = info.getUserName();
                String sessionId = info.getSessionId();
                if (StringUtils.isNotBlank(userName) && StringUtils.isNotBlank(sessionId)) {
                    pushMsgToUser(sessionId, WEBSOCKET_RESOURCEDATELIST_URL,
                        JSON.toJSONString(new JsonResultBean(monthData)));
                    pushMsgToUser(sessionId, APP_WEBSOCKET_RESOURCEDATELIST_URL,
                        getDateList(monthData, info.getAdasRiskInfos(), msgSnAck).toJSONString());
                }
                // 移除
                SubscibeInfoCache.getInstance().delTable(info);
            }
        }
    }

    private JSONObject getDateList(Long monthData, String dateMoth, String msgSNAck) {
        try {
            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            if (monthData == 0) {
                return null;
            }
            StringBuilder result = new StringBuilder(Long.toBinaryString(monthData));
            int mothDateLength = result.length();
            if (mothDateLength < 31) {
                for (int i = 0; i < 31 - mothDateLength; i++) {
                    result.insert(0, "0");
                }
            }
            for (int n = 0; n < result.length(); n++) {
                if (result.charAt(n) == '1') {
                    if (31 - n > 9) {
                        jsonArray.add(0, dateMoth + "-" + (31 - n) + "");
                    } else {
                        jsonArray.add(0, dateMoth + "-" + "0" + (31 - n));
                    }
                }
            }
            jsonObject.put("monthData", jsonArray);
            jsonObject.put("msgSn", msgSNAck);
            return jsonObject;
        } catch (Exception e) {
            log.error("920f日期转换异常", e);
            return null;
        }
    }

    private void addResourceListLog(String message) throws Exception {
        JSONObject jsonObject = JSON.parseObject(message);
        JSONObject data = jsonObject.getJSONObject("data");
        JSONObject msgBody = data.getJSONObject("msgBody");
        JSONObject desc = jsonObject.getJSONObject("desc");

        String brand = desc.getString("monitorName");
        String vehicleId = desc.getString("monitorId");
        String[] vehicleInfo = logSearchService.findCarMsg(vehicleId);
        // 获取终端组织id
        String deviceGroupId = deviceService.findGroupIdByNumber(desc.getString("deviceNumber"));
        JSONArray resourcesList = null;
        if (msgBody != null) {
            resourcesList = msgBody.getJSONArray("resourcesList");
        }
        LogSearchForm form = new LogSearchForm();
        form.setEventDate(new Date());
        form.setLogSource("1");
        form.setModule("");
        form.setGroupId(deviceGroupId);
        form.setMonitoringOperation("监控对象(" + brand + ")_终端上传资源列表");
        form.setBrand(brand);
        if (resourcesList != null) {
            form.setMessage(getMessageContent(resourcesList));
        }
        if (StringUtils.isNotBlank(vehicleInfo[1])) {
            form.setPlateColor(Integer.valueOf(vehicleInfo[1]));
        }
        try {
            logSearchService.addLogBean(form);
        } catch (Exception e) {
            logger.error("存储资源列表日志添加失败！");
        }
    }

    private String getMessageContent(JSONArray resourcesList) throws ParseException {
        StringBuilder content = new StringBuilder();
        for (int i = 0, len = resourcesList.size(); i < len; i++) {
            JSONObject resource = resourcesList.getJSONObject(i);
            content.append("通道号:").append(resource.getString("channelNum")).append("<br/>");
            content.append("开始时间:").append(DateFormatUtils
                .format(DateUtils.parseDate("20" + resource.getString("startTime"), "yyyyMMddHHmmss"),
                    "yyyy-MM-dd HH:mm:ss")).append("<br/>");
            content.append("结束时间:").append(DateFormatUtils
                .format(DateUtils.parseDate("20" + resource.getString("endTime"), "yyyyMMddHHmmss"),
                    "yyyy-MM-dd HH:mm:ss")).append("<br/>");
            content.append("报警类型:").append(AlarmTypeUtil.getAlarmName(resource.getString("alarm"))).append("<br/>");
            content.append("文件大小（M）:").append(resource.getLong("fileSize") / (1024 * 1024)).append("<br/>");
            content.append("资源类型:").append(transeFormVideoType(resource.getString("videoType"))).append("<br/>");
            content.append("码流类型:").append(transeFormStreamType(resource.getString("streamType"))).append("<br/>");
            content.append("存储器类型:").append(transeFormStorageType(resource.getString("storageType"))).append("<br/>");

        }

        return content.toString();

    }

    private String transeFormVideoType(String videoType) {
        String result = "";
        if ("0".equals(videoType)) {
            result = "音视频";
        } else if ("1".equals(videoType)) {
            result = "音频";
        } else if ("2".equals(videoType)) {
            result = "视频";

        }
        return result;

    }

    private String transeFormStreamType(String streamType) {
        String result = "";
        if ("1".equals(streamType)) {
            result = "主码流";
        } else if ("2".equals(streamType)) {
            result = "子码流";
        }
        return result;

    }

    private String transeFormStorageType(String storageType) {
        String result = "";
        if ("1".equals(storageType)) {
            result = "主存储器";
        } else if ("2".equals(storageType)) {
            result = "灾备存储器";
        }
        return result;
    }

    /**
     * 推送车辆位置信息
     */
    public void pushVehicleLocationToClient(String message) {
        JSONObject jsonObject = JSON.parseObject(message);
        String ackN = jsonObject.getJSONObject("data").getJSONObject("msgBody").getString("msgSNAck");
        String userN = UserCache.getUserInfo(ackN);
        String userS = UserCacheS.getUserInfo(ackN);
        if (userN != null && !userN.equals("")) {
            pushInfoImpl(userN, WEBSOCKET_ADDRESS_REALINFOP_URL, getPlatFormInfo(message, false).toString());
            UserCache.remove(ackN);
        } else if (userS != null && !userS.equals("")) {
            pushInfoImpl(userS, WEBSOCKET_ADDRESS_REALINFOS_URL, getPlatFormInfo(message, true).toString());
            UserCacheS.remove(ackN);
        } else {
            pushInfoImpl(InstanceMessageController.userName, WEBSOCKET_ADDRESS_REALINFO_URL,
                getPlatFormInfo(message, false).toString());
        }
        //APP点名下发应答推送
        String sessionId = UserCacheA.getSessionId(ackN);
        if (sessionId != null && !sessionId.equals("")) {
            pushMsgToUser(sessionId, WEBSOCKET_ADDRESS_REALINFO_URL, getPlatFormInfo(message, false).toString());
        }

    }

    private JSONObject getPlatFormInfo(String message, Boolean isRealTimeMonitoring) {
        Message messages = JSON.parseObject(message, Message.class);
        T808Message t808Message = JSON.parseObject(messages.getData().toString(), T808Message.class);
        T808MsgBody513 gpsInfos = JSON.parseObject(t808Message.getMsgBody().toString(), T808MsgBody513.class);
        T808GpsInfo info = JSON.parseObject(gpsInfos.getGpsInfo().toString(), T808GpsInfo.class);
        T808MsgHead head = t808Message.getMsgHead();
        String deviceId = messages.getDesc().getDeviceId();
        String monitorId = messages.getDesc().getMonitorId();
        LocationInfo locationInfo = new LocationInfo();
        locationInfo.setMonitorInfo(new MonitorInfo());
        // 协议类型
        locationInfo.setProtocolType(info.getProtocolType());
        f3MessageService.getMonitorDetail(monitorId, locationInfo, null, null);
        MonitorInfo monitorInfo = locationInfo.getMonitorInfo();
        // 终端类型
        monitorInfo.setDeviceType(info.getProtocolType());

        AddressParam addressParam = new AddressParam();
        addressParam.setGps_latitude(String.valueOf(info.getLatitude()));
        addressParam.setGps_longitude(String.valueOf(info.getLongitude()));
        addressParam.setDecID(deviceId);
        addressParam.setTime(info.getTime());
        long timeO = 0L;
        if (addressParam.getTime().length() != 10) {
            try {
                timeO = DateUtils.parseDate("20" + addressParam.getTime(), DATE_FORMAT1).getTime() / 1000;
            } catch (ParseException e) {
                logger.error("时间解析异常" + e);
            }
        } else {
            timeO = Long.parseLong(addressParam.getTime());
        }

        final String formattedAddress =
                AddressUtil.inverseAddress(info.getLongitude(), info.getLatitude()).getFormattedAddress();

        if (info.getGpsAttachInfoList() != null && info.getGpsAttachInfoList().size() > 0) {
            int gpsAttachInfoID;
            for (Object object : info.getGpsAttachInfoList()) {
                JSONObject jsonObj = JSON.parseObject(JSON.toJSONString(object));
                gpsAttachInfoID = jsonObj.getIntValue("gpsAttachInfoID");
                switch (gpsAttachInfoID) {
                    case 0x01:
                        locationInfo.setGpsMileage(jsonObj.getDoubleValue("mileage"));
                        break;
                    case 0x02:
                        locationInfo.setGpsOil(jsonObj.getDoubleValue("oil"));
                        break;
                    case 0x03:
                        locationInfo.setGrapherSpeed(jsonObj.getDoubleValue("speed"));
                        break;
                    case 0x25:
                        locationInfo.setSignalState(jsonObj.getLong("signalState"));
                        break;
                    case 0x31:
                        locationInfo.setSatellitesNumber(jsonObj.getInteger("GNSSNumber"));
                        break;
                    default:
                        break;
                }
            }
        }
        Integer cacheState = clientMessageCleaner.getCacheState(messages.getDesc().getMonitorId());

        JSONObject obj = new JSONObject();
        obj.put("desc", messages.getDesc());
        JSONObject data = new JSONObject();
        locationInfo.setGpsTime(info.getTime());
        locationInfo.setUploadtime(info.getUploadtime());
        locationInfo.setAcc(info.getStatus().intValue() & 0x1);
        locationInfo.setLatitude(info.getLatitude());
        locationInfo.setLongitude(info.getLongitude());
        locationInfo.setDirection(info.getDirection());
        locationInfo.setGlobalAlarmSet(info.getPushAlarmSet());
        locationInfo.setAltitude(info.getAltitude());
        locationInfo.setGpsSpeed(info.getSpeed());
        locationInfo.setStatus(info.getStatus());
        locationInfo.setIoSignalData(info.getIoSignalData());
        locationInfo.setOilExpend(info.getOilExpend());
        locationInfo.setPositionDescription(formattedAddress);
        locationInfo.setOilMass(info.getOilMass());
        locationInfo.setLoadsInfo(info.getLoadInfos());
        locationInfo.setMileageSensor(info.getMileageSensor());
        locationInfo.setLoadInfos(info.getLoadInfos());
        locationInfo.setElecData(info.getElectricityCheck());
        locationInfo.setTyreInfos(info.getTyreInfos());
        locationInfo.setTemperatureSensor(info.getTemperatureSensor());
        locationInfo.setTemphumiditySensor(info.getTemphumiditySensor());
        locationInfo.setWorkHourSensor(info.getWorkHourSensor());
        locationInfo.setPositiveNegative(info.getPositiveNegative());
        locationInfo.setSignalStrength(info.getSignalStrength());
        Integer batteryVoltage = info.getBatteryVoltage();
        locationInfo.setBatteryVoltage(batteryVoltage != null ? Double.valueOf(batteryVoltage) : null);
        locationInfo.setDurationTime(info.getDurationTime());
        if (cacheState != null) {
            locationInfo.setStateInfo(cacheState);
        } else {
            locationInfo.setStateInfo(3);
        }
        locationInfo.setMsgSNAck(gpsInfos.getMsgSNAck());
        //设置obd信息
        Object obdObj = isRealTimeMonitoring
                ? obdVehicleTypeService.convertStreamToObdInfo(locationInfo)
                : obdVehicleTypeService.convertStreamToObdList(locationInfo);
        locationInfo.setObdObjStr(JSONObject.toJSONString(obdObj));
        //设置里程和油耗
        f3MessageService.getCurDayOilAndMile(locationInfo, monitorId);
        // 单位转换
        f3MessageService.unitConversion(locationInfo);
        data.put("msgHead", head);
        data.put("msgBody", JSONObject.toJSON(locationInfo));
        obj.put("data", data);

        return obj;
    }

    /**
     * 推送状态信息
     */
    public void pushDeviceReportLogToClient(String message) {
        pushInfoImpl(ConstantUtil.WEBSOCKET_DEVICE_REPORT_LOG_TOPIC, message);
    }

    /**
     * 推送全局报警被处理通知
     */
    public void sendGlobalAlarmHandleNotice(String vehicleId) {
        simpMessagingTemplateUtil.sendGlobalAlarmHandleNotice(vehicleId);
    }
}

package com.zw.adas.push.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.zw.adas.domain.riskManagement.AdasRiskEventInfo;
import com.zw.adas.service.elasticsearch.AdasElasticSearchService;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.domain.basicinfo.ProfessionalsInfo;
import com.zw.platform.domain.connectionparamsset_809.PlantParam;
import com.zw.platform.domain.netty.ServerParamList;
import com.zw.platform.domain.reportManagement.T809AlarmFileListAck;
import com.zw.platform.domain.reportManagement.WarnMsgFileInfo;
import com.zw.platform.service.connectionparamsset_809.impl.ConnectionParamsSetServiceImpl;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.MsgUtil;
import com.zw.protocol.msg.Message;
import com.zw.protocol.msg.t809.T809Message;
import com.zw.protocol.msg.t809.body.SupervisionAlarmInfo;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import com.zw.protocol.util.ProtocolTypeUtil;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class AdasSubcibeTable {

    private static final Logger logger = LogManager.getLogger(AdasSubcibeTable.class);
    @Autowired
    AdasElasticSearchService elasticSearchService;
    @Autowired
    ServerParamList serverParamList;
    @Autowired
    ConnectionParamsSetServiceImpl connectionParamsSetService;
    @Value("${9208.catche.time.out}")
    private Integer timeOut;
    @Value("${1241.catche.time.out}")
    private Integer timeOut2;
    private Cache<String, Object> manualCache;
    /**
     * 人脸识别缓存（0702插卡信息存入缓存，0801接收照片，如果缓存存在，主动推送1241)
     */
    private Cache<String, ProfessionalsInfo> faceRecognitionCache;
    @Getter
    private Cache<String, Set<String>> remoteUpgradeCache;

    @PostConstruct
    private void init() {
        this.manualCache = Caffeine.newBuilder().expireAfterWrite(timeOut.longValue(), TimeUnit.SECONDS)
            .removalListener(((key, value, cause) -> sendAlarmFileListAuto((AdasRiskEventInfo) value))).build();
        this.faceRecognitionCache =
            Caffeine.newBuilder().expireAfterWrite(timeOut2.longValue(), TimeUnit.SECONDS).build();
        this.remoteUpgradeCache =
            Caffeine.newBuilder().expireAfterWrite(timeOut2.longValue(), TimeUnit.SECONDS).build();
    }

    public void put(String key, Object object) {
        manualCache.put(key, object);
    }

    public void remove(String key) {
        manualCache.invalidate(key);
    }

    public Object get(String key) {
        return manualCache.getIfPresent(key);
    }

    public void putFaceRecognitionCache(String key, ProfessionalsInfo str) {
        faceRecognitionCache.put(key, str);
    }

    public void removeFaceRecognitionCache(String key) {
        faceRecognitionCache.invalidate(key);
    }

    public ProfessionalsInfo getFaceRecognitionCache(String key) {
        return faceRecognitionCache.getIfPresent(key);
    }

    //桂标上报1407报警附件信息
    private void sendAlarmFileListAuto(AdasRiskEventInfo adasRiskEventInfo) {
        try {
            if (adasRiskEventInfo == null || adasRiskEventInfo.getProtocolType() == null) {
                // 协议非桂标，退出
                return;
            }
            String protocolType = adasRiskEventInfo.getProtocolType() + "";
            if (!(ProtocolTypeUtil.GUANG_XI_PROTOCOL_808_2013.equals(protocolType)
                || ProtocolTypeUtil.T809_YU_PROTOCOL_809_2013.equals(protocolType))) {
                return;

            }

            String vehicleId = adasRiskEventInfo.getVehicleId();
            List<PlantParam> platformIp = connectionParamsSetService.getMonitorPlatform(vehicleId);
            if (CollectionUtils.isEmpty(platformIp)) {
                return;
            }
            final Map<String, String> vehicleInfo =
                RedisHelper.getHashMap(RedisKeyEnum.MONITOR_INFO.of(vehicleId), "name", "deviceNumber", "plateColor");

            if (vehicleInfo == null) {
                return;
            }
            if (ProtocolTypeUtil.GUANG_XI_PROTOCOL_808_2013.equals(protocolType)) {
                sendGui(adasRiskEventInfo, platformIp, vehicleInfo);
            } else {
                sendYu(adasRiskEventInfo, platformIp, vehicleInfo);
            }

        } catch (Exception e) {
            logger.error("桂豫标报警附件主动上报异常！", e);
        }

    }

    private void sendYu(AdasRiskEventInfo adasRiskEventInfo, List<PlantParam> platformIp,
        Map<String, String> vehicleInfo) {
        // 监控对象标识
        String brand = vehicleInfo.get("name");
        String deviceNumber = vehicleInfo.get("deviceNumber");
        // 车牌颜色
        Integer color = Integer.parseInt(vehicleInfo.get("plateColor"));

        T809AlarmFileListAck fileListAck = adasRiskEventInfo.getFileListAck();
        List<WarnMsgFileInfo> fileList = adasRiskEventInfo.getFileList();
        fileListAck.setFileCount(fileList.size());
        fileListAck.setFileInfos(fileList);
        fileListAck.setMediaInfo(adasRiskEventInfo.getAlarmSign());
        int dataLength = 17;
        for (WarnMsgFileInfo warnMsgFileInfo : fileListAck.getFileInfos()) {
            dataLength += warnMsgFileInfo.getFileNameLength() + 7 + warnMsgFileInfo.getFileUrlLengh();
        }
        for (PlantParam param : platformIp) {
            String serverIp = param.getIp(); // IP地址
            Integer msgGNSSCenterId = param.getCenterId(); // 接入码
            SupervisionAlarmInfo supervisionAlarmInfo = new SupervisionAlarmInfo(); // 消息体接入码
            supervisionAlarmInfo.setDataType(ConstantUtil.T809_UP_PREVENTION_MSG_FILELIST_REQ);
            supervisionAlarmInfo.setDataLength(dataLength); // 后续数据长度(数据部分字段长度相加)
            supervisionAlarmInfo.setVehicleNo(brand);
            supervisionAlarmInfo.setVehicleColor(color);
            supervisionAlarmInfo.setData(MsgUtil.objToJson(fileListAck));
            T809Message alarmFileAck = MsgUtil
                .getT809Message(ConstantUtil.T809_UP_PREVENTION_MSG, serverIp, msgGNSSCenterId, supervisionAlarmInfo);
            WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809())
                .writeAndFlush(getT809Message(param, alarmFileAck, deviceNumber));
        }
    }

    private void sendGui(AdasRiskEventInfo adasRiskEventInfo, List<PlantParam> platformIp,
        Map<String, String> vehicleInfo) {
        // 监控对象标识
        String brand = vehicleInfo.get("name");
        String deviceNumber = vehicleInfo.get("deviceNumber");
        // 车牌颜色
        Integer color = Integer.parseInt(vehicleInfo.get("plateColor"));
        T809AlarmFileListAck fileListAck = new T809AlarmFileListAck();
        fileListAck.setInfoId(adasRiskEventInfo.getAlarmId());
        fileListAck.setFileCount(adasRiskEventInfo.getFileList().size());
        fileListAck.setFileInfos(adasRiskEventInfo.getFileList());
        int dataLength = 17;
        for (WarnMsgFileInfo warnMsgFileInfo : fileListAck.getFileInfos()) {
            dataLength += warnMsgFileInfo.getFileNameLength() + 7 + warnMsgFileInfo.getFileUrlLengh();
        }

        for (PlantParam param : platformIp) {
            Integer protocolType = param.getProtocolType();
            if (!ProtocolTypeUtil.T809_GUI_PROTOCOL_809_2013.equals(String.valueOf(protocolType))) {
                continue;
            }
            T809Message alarmFileAck = getAlarmT809Message(brand, color, param, dataLength, fileListAck,
                ConstantUtil.T809_UP_WARN_MSG_FILELIST_AUTO);
            // 推送消息
            WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809())
                .writeAndFlush(getT809Message(param, alarmFileAck, deviceNumber));
        }
    }

    private T809Message getAlarmT809Message(String brand, Integer color, PlantParam param, Integer dataLength,
        Object fileListAck, Integer dataType) {
        String serverIp = param.getIp(); // IP地址
        Integer msgGNSSCenterId = param.getCenterId(); // 接入码
        SupervisionAlarmInfo supervisionAlarmInfo = new SupervisionAlarmInfo(); // 消息体接入码
        supervisionAlarmInfo.setDataType(dataType);
        supervisionAlarmInfo.setDataLength(dataLength); // 后续数据长度(数据部分字段长度相加)
        supervisionAlarmInfo.setVehicleNo(brand);
        supervisionAlarmInfo.setVehicleColor(color);
        supervisionAlarmInfo.setData(MsgUtil.objToJson(fileListAck));
        return MsgUtil.getT809Message(ConstantUtil.T809_UP_WARN_MSG, serverIp, msgGNSSCenterId, supervisionAlarmInfo);
    }

    private Message getT809Message(PlantParam param, T809Message message, String deviceNumber) {
        Message t809Message = MsgUtil.getMsg(ConstantUtil.T809_UP_WARN_MSG, message).assembleDesc809(param.getId());
        t809Message.getDesc().setDeviceNumber(deviceNumber);
        return t809Message;
    }
}

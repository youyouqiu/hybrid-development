package com.zw.platform.service.realTimeVideo.impl;

import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.domain.realTimeVideo.CloudTerrace;
import com.zw.platform.domain.realTimeVideo.CloudTerraceForm;
import com.zw.platform.service.basicinfo.VehicleService;
import com.zw.platform.service.realTimeVideo.CloudTerraceService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.MsgUtil;
import com.zw.platform.util.common.Customer;
import com.zw.protocol.msg.t808.T808Message;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 云台控制
 */
@Service
public class CloudTerraceServiceImpl implements CloudTerraceService {

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private LogSearchService logSearchService;

    private static Logger log = LogManager.getLogger(CloudTerraceServiceImpl.class);

    private static final String VIDEO_MODULE = "REALTIMEVIDEO";

    @Override
    public void sendParam(CloudTerraceForm form, String ipAddress) throws Exception {
        final Map<String, String> vehicleInfo = RedisHelper.getHashMap(RedisKeyEnum.MONITOR_INFO.of(
                form.getVehicleId(), "name", "plateColor", "deviceId", "simCardNumber", "deviceType"));
        if (vehicleInfo != null) {
            String brand = vehicleInfo.get("name");
            String plateColor = vehicleInfo.get("plateColor");
            String simcardNumber = vehicleInfo.get("simCardNumber");
            String deviceId = vehicleInfo.get("deviceId");
            StringBuffer logMessage = new StringBuffer();// 日志语句
            // 获得要下发的消息ID
            Integer msgId = getMessageId(form, logMessage, brand);
            // 获得要发送的808消息
            T808Message message = getT808Message(simcardNumber, form, msgId, vehicleInfo);
            WebSubscribeManager.getInstance().sendMsgToAll(message, msgId, deviceId);
            log.info(">========" + brand + "下发" + msgId + "成功=============<");
            logSearchService.addLog(ipAddress, logMessage.toString(), "3", VIDEO_MODULE, brand, plateColor);
        }
    }

    private Integer getMessageId(CloudTerraceForm form, StringBuffer logMessage, String brand) {
        Integer command = 0;
        String control = "";
        logMessage.append("监控对象(").append(brand).append(")_通道号").append(form.getChannelNum()).append("云台操作：");
        Integer type = form.getType();
        switch (type) {
            case 0:
                command = ConstantUtil.T808_ROTATING_CONTROL;
                String direction = getDirection(form.getControl());
                logMessage.append(direction);
                break;
            case 1:
                command = ConstantUtil.T808_FOCAL_LENGTH_CONTROL;
                control = getFocalLengthControl(form.getControl());
                logMessage.append(control);
                break;
            case 2:
                command = ConstantUtil.T808_APERTURE_CONTROL;
                control = getApertureControl(form.getControl());
                logMessage.append(control);
                break;
            case 3:
                command = ConstantUtil.T808_WIPER_CONTROL;
                control = getWiperControl(form.getControl());
                logMessage.append(control);
                break;
            case 4:
                command = ConstantUtil.T808_INFRARED_FILL_LIGHT_CONTROL;
                control = getInfraredFillLight(form.getControl());
                logMessage.append(control);
                break;
            case 5:
                command = ConstantUtil.T808_ZOOM_CONTROL;
                control = getZoom(form.getControl());
                logMessage.append(control);
                break;
            default:
                break;

        }
        return command;
    }

    private String getZoom(Integer control) {
        return control.equals(0) ? "变倍调大" : "变倍调小";
    }

    private String getInfraredFillLight(Integer control) {
        return control.equals(0) ? "红外光补光停止" : "红外光补光启动";
    }

    private String getWiperControl(Integer control) {
        return control.equals(0) ? "雨刷停止" : "雨刷启动";
    }

    private String getApertureControl(Integer control) {
        return control.equals(0) ? "光圈调大" : "光圈调小";
    }

    private String getFocalLengthControl(Integer control) {
        return control.equals(0) ? "焦距调大" : "焦距调小";
    }

    private String getDirection(Integer control) {
        String direction = "";
        switch (control) {

            case 0:
                direction = "停止";
                break;
            case 1:
                direction = "上";
                break;
            case 2:
                direction = "下";
                break;
            case 3:
                direction = "左";
                break;
            case 4:
                direction = "右";
                break;
            default:
                break;

        }
        return direction;
    }

    private T808Message getT808Message(String simcardNumber, CloudTerraceForm form, Integer msgId, Map vehicleInfo) {
        // 生成流水号
        Customer c = new Customer();
        Integer msgSN = Integer.valueOf(c.getCustomerID());
        // 获取下发消息体
        CloudTerrace cloudTerrace = getCloudTerrace(form);
        T808Message message =
            MsgUtil.get808Message(simcardNumber, msgId, msgSN, cloudTerrace, vehicleInfo);
        return message;
    }

    private CloudTerrace getCloudTerrace(CloudTerraceForm form) {
        CloudTerrace cloudTerrace = new CloudTerrace();
        cloudTerrace.setChannelNum(form.getChannelNum());
        cloudTerrace.setControl(form.getControl());
        cloudTerrace.setSpeed(form.getSpeed());
        return cloudTerrace;
    }
}

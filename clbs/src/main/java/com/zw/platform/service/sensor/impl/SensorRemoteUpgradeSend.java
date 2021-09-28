package com.zw.platform.service.sensor.impl;

import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.param.SensorRemoteUpgrade;
import com.zw.platform.push.controller.SubscibeInfo;
import com.zw.platform.push.controller.SubscibeInfoCache;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.MsgUtil;
import com.zw.platform.util.SendHelper;
import com.zw.protocol.msg.t808.T808Message;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import com.zw.ws.entity.t808.oil.T808_0x8900;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.Collections;

/**
 * 传感器远程升级下发
 * @author zhouzongbo on 2019/1/30 14:06
 */
public class SensorRemoteUpgradeSend implements Serializable {
    private static final long serialVersionUID = 8177600105737328149L;

    public static final Logger logger = LogManager.getLogger(SensorRemoteUpgrade.class);

    private SendHelper sendHelper;

    public SensorRemoteUpgradeSend(SendHelper sendHelper) {
        this.sendHelper = sendHelper;
    }

    /**
     * 擦出终端升级数据存储区
     * @param upgrade      sensorRemoteUpgrade
     * @param serialNumber 流水号
     */
    public String sendEraseTerminalUpgradeData(SensorRemoteUpgrade upgrade, String vehicleId, String deviceId,
        String simCardNumber, String parameterName, String paramType, Integer serialNumber) throws Exception {
        //获取最后一次下发的编号
        String paramId = sendHelper.getLastSendParamID(vehicleId, parameterName, paramType);
        if (serialNumber != null) {
            int status = 4;
            paramId =
                sendHelper.updateParameterStatus(paramId, serialNumber, status, vehicleId, paramType, parameterName);

            commonSubscribe(upgrade, deviceId, simCardNumber, serialNumber);
        } else {
            int status = 5;
            serialNumber = 0;
            paramId = sendHelper.updateParameterStatus(null, serialNumber, status, vehicleId, paramType, parameterName);
        }
        return paramId;
    }

    /**
     * 公共订阅
     * @param deviceId      deivceId
     * @param simCardNumber simCardNumber
     * @param upgrade       upgrade
     * @param msgSN         msgSN
     */
    private void commonSubscribe(SensorRemoteUpgrade upgrade, String deviceId, String simCardNumber, Integer msgSN) {
        // 订阅通用应答
        SubscibeInfo info =
            new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, msgSN, ConstantUtil.T808_DEVICE_GE_ACK);
        SubscibeInfoCache.getInstance().putTable(info);

        T808_0x8900 t8900Param = new T808_0x8900();
        t8900Param.setType(0xF8);
        t8900Param.setSensorDatas(Collections.singletonList(upgrade));
        t8900Param.setSum(1);

        T808Message message = MsgUtil
            .get808Message(simCardNumber, ConstantUtil.T808_PENETRATE_DOWN, msgSN, t8900Param, upgrade.getDeviceType());
        WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_PENETRATE_DOWN, deviceId);
    }

    /**
     * 下发升级数据
     * @param upgrade sensorRemoteUpgrade
     */
    public void sendIssueUpgradeData(SensorRemoteUpgrade upgrade, String vehicleId, String deviceId,
        String simCardNumber, String parameterName, String paramType, Integer serialNumber) {
        commonSubscribe(upgrade, deviceId, simCardNumber, serialNumber);
    }

    /**
     * 下发总数据校验命令
     * @param upgrade sensorRemoteUpgrade
     */
    public void sendTotalDataValidation(SensorRemoteUpgrade upgrade, String deviceId, String simCardNumber,
        Integer msgSN) {
        commonSubscribe(upgrade, deviceId, simCardNumber, msgSN);
    }

    /**
     * 下发开始外设升级命令
     * @param upgrade sensorRemoteUpgrade
     */
    public void sendStartPerpheralUpgrade(SensorRemoteUpgrade upgrade, String deviceId, String simCardNumber,
        Integer msgSN) {
        commonSubscribe(upgrade, deviceId, simCardNumber, msgSN);
    }

    /**
     * 下发结束升级文件命令
     * @param upgrade      upgrade
     * @param serialNumber
     */
    public void sendEndUpgradeFile(SensorRemoteUpgrade upgrade, String vehicleId, String deviceId, String simCardNumber,
        String parameterName, String paramType, Integer serialNumber, String paramId) {
        if (serialNumber != null && StringUtils.isNotEmpty(paramId)) {
            sendHelper.updateParameterStatus(paramId, serialNumber, 9, vehicleId, paramType, parameterName);
            commonSubscribe(upgrade, deviceId, simCardNumber, serialNumber);
        } else {
            serialNumber = 0;
            sendHelper.updateParameterStatus(null, serialNumber, 9, vehicleId, paramType, parameterName);
        }
    }
}
package com.zw.platform.service.sendTxt.impl;

import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.domain.core.SendParam;
import com.zw.platform.domain.vas.monitoring.form.T808_0x8202;
import com.zw.platform.push.handler.device.DeviceHelper;
import com.zw.platform.service.core.F3SendStatusProcessService;
import com.zw.platform.service.sendTxt.F3SendTxtService;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.MsgUtil;
import com.zw.platform.util.SendHelper;
import com.zw.protocol.msg.t808.T808Message;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import com.zw.ws.entity.t808.parameter.ParamItem;
import com.zw.ws.entity.t808.parameter.T808_0x8103;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * Title:
 * <p>
 * Copyright: Copyright (c) 2016
 * <p>
 * Company: ZhongWei
 * <p>
 * team: ZhongWeiTeam
 * @version 1.0
 * @author nixiangqian
 * @since 2017年07月20日 11:16
 */
@Service
public class F3SendTxtServiceImpl implements F3SendTxtService {
    @Autowired
    private SendHelper sendHelper;

    @Autowired
    private F3SendStatusProcessService f3SendStatusProcessService;

    /**
     * 参数设置下发, 下发8103
     * @param vehicleId     车id
     * @param parameterName 设置id
     * @param paramType     下发参数类型
     * @param isOvertime    是否进行超时  true 超时 false 不超过
     */
    @Override
    public String setF3SetParam(String vehicleId, String parameterName, List<ParamItem> params, String paramType,
        boolean isOvertime) {
        //获取最后一次下发的编号
        String paramId = sendHelper.getLastSendParamID(vehicleId, parameterName, paramType);
        // 获取车辆及设备信息
        final RedisKey key = RedisKeyEnum.MONITOR_INFO.of(vehicleId);
        final Map<String, String> vehicleInfo = RedisHelper.getHashMap(key, "deviceId", "simCardNumber", "deviceType");
        Integer msgSN = null;
        if (vehicleInfo != null) {
            String deviceId = vehicleInfo.get("deviceId");
            String simcardNumber = vehicleInfo.get("simCardNumber");

            // 序列号
            msgSN = DeviceHelper.serialNumber(vehicleId);
            if (msgSN != -1) { // 设备在线
                // 下发参数
                paramId = sendHelper.updateParameterStatus(paramId, msgSN, 4, vehicleId, paramType, parameterName);
                if (isOvertime) {
                    SendParam sendParam = new SendParam();
                    sendParam.setMsgSNACK(msgSN);
                    sendParam.setParamId(paramId);
                    sendParam.setVehicleId(vehicleId);
                    f3SendStatusProcessService.updateSendParam(sendParam, 1);
                }
                // 绑定下发
                T808_0x8103 t8080x8103 = new T808_0x8103();
                t8080x8103.setPackageSum(params.size());
                t8080x8103.setParametersCount(params.size());
                t8080x8103.setParamItems(params);

                T808Message message = MsgUtil
                    .get808Message(simcardNumber, ConstantUtil.T808_SET_PARAM, msgSN, t8080x8103, vehicleInfo);
                WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.F3_CLBS_SET_PARAM, deviceId);
                return String.valueOf(msgSN);
            } else { // 设备未注册
                msgSN = 0;// 绑定下发
                sendHelper.updateParameterStatus(paramId, msgSN, 5, vehicleId, paramType, parameterName);
            }
        }
        return String.valueOf(msgSN);
    }

    @Override
    public String setParametersTrace(String vehicleId, String parameterName, T808_0x8202 t8080x8202, String paramType,
        boolean isOvertime) {
        //获取最后一次下发的编号
        String paramId = sendHelper.getLastSendParamID(vehicleId, parameterName, paramType);
        // 获取车辆及设备信息
        final RedisKey key = RedisKeyEnum.MONITOR_INFO.of(vehicleId);
        final Map<String, String> vehicleInfo = RedisHelper.getHashMap(key, "deviceId", "simCardNumber", "deviceType");
        Integer msgSN = null;
        if (vehicleInfo != null) {
            String deviceId = vehicleInfo.get("deviceId");
            String simcardNumber = vehicleInfo.get("simCardNumber");
            // 序列号
            msgSN = DeviceHelper.serialNumber(vehicleId);
            if (msgSN != -1) { // 设备在线
                // 下发参数
                paramId = sendHelper.updateParameterStatus(paramId, msgSN, 4, vehicleId, paramType, parameterName);
                if (isOvertime) {
                    SendParam sendParam = new SendParam();
                    sendParam.setMsgSNACK(msgSN);
                    sendParam.setParamId(paramId);
                    sendParam.setVehicleId(vehicleId);
                    f3SendStatusProcessService.updateSendParam(sendParam, 1);
                }
                // 绑定下发
                T808Message message = MsgUtil
                    .get808Message(simcardNumber, ConstantUtil.T808_INTERIM_TRACE, msgSN, t8080x8202, vehicleInfo);
                WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.F3_CLBS_SET_TRACE, deviceId);
                return String.valueOf(msgSN);
            } else { // 设备未注册
                msgSN = 0;// 绑定下发
                sendHelper.updateParameterStatus(paramId, msgSN, 5, vehicleId, paramType, parameterName);
            }
        }
        return String.valueOf(msgSN);
    }
}

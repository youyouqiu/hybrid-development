package com.zw.platform.service.sendTxt.impl;

import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.domain.systems.Directive;
import com.zw.platform.push.handler.device.DeviceHelper;
import com.zw.platform.repository.modules.ParameterDao;
import com.zw.platform.service.sendTxt.AsoSendTxtService;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.MsgUtil;
import com.zw.platform.util.SendHelper;
import com.zw.protocol.msg.t808.T808Message;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import com.zw.ws.entity.aso.ASOFixedPoint;
import com.zw.ws.entity.aso.ASOFrequency;
import com.zw.ws.entity.aso.ASORestart;
import com.zw.ws.entity.aso.ASOTransparent;
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
 * @since 2017年07月28日 14:10
 */
@Service
public class AsoSendTxtServiceImpl implements AsoSendTxtService {
    @Autowired
    private ParameterDao parameterDao;

    @Autowired
    private SendHelper sendHelper;

    @Override
    public void sendFixedPoint(String vehicleId, ASOFixedPoint fixedPoint) {
        //获取最后一次下发的编号
        String parameterName = "ASO_" + vehicleId + "_0219";
        String paramType = "ASO_0219";
        String paramId = getLastSendParamID(vehicleId, parameterName, paramType);
        // 获取车辆及设备信息
        final RedisKey key = RedisKeyEnum.MONITOR_INFO.of(vehicleId);
        final Map<String, String> vehicleInfo = RedisHelper.getHashMap(key, "deviceId", "simCardNumber", "deviceType");
        if (vehicleInfo != null) {
            String deviceId = vehicleInfo.get("deviceId");
            String simcardNumber = vehicleInfo.get("simCardNumber");
            // 序列号
            int msgSN = DeviceHelper.deviceSerialNumber(vehicleId);
            // 下发参数
            sendHelper.updateParameterStatus(paramId, msgSN, 4, vehicleId, paramType, parameterName);
            T808Message message = MsgUtil
                .get808Message(simcardNumber, ConstantUtil.T808_PENETRATE_DOWN, msgSN, fixedPoint,
                    vehicleInfo);
            WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.ASO_CLBS_FIXED_POINT, deviceId);
        }
    }

    @Override
    public void sendFrequency(String vehicleId, ASOFrequency frequency) {
        //获取最后一次下发的编号
        String parameterName = "ASO_" + vehicleId + "_0218";
        String paramType = "ASO_0218";
        String paramId = getLastSendParamID(vehicleId, parameterName, paramType);
        // 获取车辆及设备信息
        final RedisKey key = RedisKeyEnum.MONITOR_INFO.of(vehicleId);
        final Map<String, String> vehicleInfo = RedisHelper.getHashMap(key, "deviceId", "simCardNumber", "deviceType");
        if (vehicleInfo != null) {
            String deviceId = vehicleInfo.get("deviceId");
            String simcardNumber = vehicleInfo.get("simCardNumber");
            // 序列号
            int msgSn = DeviceHelper.deviceSerialNumber(vehicleId);
            // 下发参数
            sendHelper.updateParameterStatus(paramId, msgSn, 4, vehicleId, paramType, parameterName);
            T808Message message = MsgUtil
                .get808Message(simcardNumber, ConstantUtil.T808_PENETRATE_DOWN, msgSn, frequency,
                    vehicleInfo);
            WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.ASO_CLBS_FREQUENCY, deviceId);
        }
    }

    @Override
    public void sendTransparent(String vehicleId, ASOTransparent transparent) {
        //获取最后一次下发的编号
        String parameterName = "ASO_" + vehicleId + "_020A";
        String paramType = "ASO_020A";
        String paramId = getLastSendParamID(vehicleId, parameterName, paramType);
        // 获取车辆及设备信息

        final RedisKey key = RedisKeyEnum.MONITOR_INFO.of(vehicleId);
        final Map<String, String> vehicleInfo = RedisHelper.getHashMap(key, "deviceId", "simCardNumber", "deviceType");

        if (vehicleInfo != null) {
            String deviceId = vehicleInfo.get("deviceId");
            String simcardNumber = vehicleInfo.get("simCardNumber");
            // 序列号，绕过离线判断（超待设备允许离线状态下发（超待设备的上报频率低，会被平台判定离线））
            int msgSn = DeviceHelper.deviceSerialNumber(vehicleId);
            // 下发参数
            sendHelper.updateParameterStatus(paramId, msgSn, 4, vehicleId, paramType, parameterName);
            T808Message message = MsgUtil
                .get808Message(simcardNumber, ConstantUtil.T808_PENETRATE_DOWN, msgSn, transparent,
                    vehicleInfo);
            WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.ASO_CLBS_TRANSPARENT, deviceId);
        }
    }

    @Override
    public void sendRestart(String vehicleId) {
        //获取最后一次下发的编号
        String parameterName = "ASO_" + vehicleId + "_021A";
        String paramType = "ASO_021A";
        String paramId = getLastSendParamID(vehicleId, parameterName, paramType);
        // 获取车辆及设备信息
        final RedisKey key = RedisKeyEnum.MONITOR_INFO.of(vehicleId);
        final Map<String, String> vehicleInfo = RedisHelper.getHashMap(key, "deviceId", "simCardNumber", "deviceType");
        if (vehicleInfo != null) {
            String deviceId = vehicleInfo.get("deviceId");
            String simcardNumber = vehicleInfo.get("simCardNumber");
            // 序列号
            int msgSn = DeviceHelper.deviceSerialNumber(vehicleId);
            // 下发参数
            sendHelper.updateParameterStatus(paramId, msgSn, 4, vehicleId, paramType, parameterName);
            T808Message message = MsgUtil
                .get808Message(simcardNumber, ConstantUtil.T808_PENETRATE_DOWN, msgSn, new ASORestart(),
                    vehicleInfo);
            WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.ASO_CLBS_RESTART, deviceId);
        }
    }

    /**
     * 根据车辆、下发参数编号、下发类型获取最后一次下发的编号
     * @param vehicleId 车辆编号
     * @param paramid   下发参数编号
     * @param type      下发类型获
     */
    private String getLastSendParamID(String vehicleId, String paramid, String type) {
        List<Directive> paramlist = parameterDao.findParameterByType(vehicleId, paramid, type); // 6:报警
        Directive param;
        if (paramlist != null && !paramlist.isEmpty()) {
            param = paramlist.get(0);
            return param.getId();
        }
        return "";
    }
}

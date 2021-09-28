/*
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved. This software is the confidential and proprietary information
 * of ZhongWei, Inc. You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with ZhongWei.
 */

package com.zw.ws.impl;

import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.vas.oilmgt.FuelVehicle;
import com.zw.platform.push.cache.ParamSendingCache;
import com.zw.platform.push.cache.SendModule;
import com.zw.platform.push.cache.SendTarget;
import com.zw.platform.push.controller.SubscibeInfo;
import com.zw.platform.push.controller.SubscibeInfoCache;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.MsgUtil;
import com.zw.protocol.msg.t808.T808Message;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import com.zw.ws.entity.t808.oil.OilSensorParam;
import com.zw.ws.entity.t808.parameter.ParamItem;
import com.zw.ws.entity.t808.parameter.T808_0x8103;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class WsOilConsumeService {
    /**
     * 协议上所有参数值加起来的长度
     */
    private static final int PROTOCOL_ALL_PARAMETER_LENGTH = 30;
    /**
     * 看协议
     */
    private static final int FIRST_SENSOR_ID = 0xF345;

    @Autowired
    ParamSendingCache paramSendingCache;

    public void sendOilBenchmarkCommand(BindDTO vehicleInfo, FuelVehicle fuelVehicle, Integer transNo) {
        try {
            sendOilBenchmarkCommandImpl(vehicleInfo, fuelVehicle, transNo);
        } catch (Exception e) {
            log.error("下发油耗参数遇到错误", e);
        }
    }

    private void sendOilBenchmarkCommandImpl(BindDTO vehicleInfo, FuelVehicle fuelVehicle, Integer transNo) {
        T808_0x8103 benchmark = new T808_0x8103();
        ParamItem paramItem = new ParamItem();
        paramItem.setParamLength(PROTOCOL_ALL_PARAMETER_LENGTH);
        OilSensorParam oilSensorParam = new OilSensorParam();
        if (fuelVehicle != null) {
            oilSensorParam
                .setInertiaCompEn(fuelVehicle.getInertiaCompEn() != null ? fuelVehicle.getInertiaCompEn() : 1);
            oilSensorParam.setSmoothing(fuelVehicle.getFilterFactor() != null ? fuelVehicle.getFilterFactor() : 2);
            oilSensorParam.setAutoInterval(StringUtils.isNotBlank(fuelVehicle.getAutoUploadTime())
                ?
                Integer.parseInt(fuelVehicle.getAutoUploadTime()) : 1);
            oilSensorParam.setOutputCorrectionK(StringUtils.isNotBlank(fuelVehicle.getOutputCorrectionK())
                ?
                Integer.parseInt(fuelVehicle.getOutputCorrectionK()) : 100);
            oilSensorParam.setOutputCorrectionB(StringUtils.isNotBlank(fuelVehicle.getOutputCorrectionB())
                ?
                Integer.parseInt(fuelVehicle.getOutputCorrectionB()) : 100);
            oilSensorParam.setRange(0); // 传感器长度： 油耗 类置 0
            oilSensorParam.setOilType(
                StringUtils.isNotBlank(fuelVehicle.getFuelSelect()) ? Integer.parseInt(fuelVehicle.getFuelSelect()) :
                    1); // 燃油选择
            oilSensorParam.setMeasureFun(1); // 测量方案 油耗类默认设置1（但油量计）
        }
        paramItem.setParamValue(oilSensorParam);
        paramItem.setParamId(FIRST_SENSOR_ID);
        benchmark.getParamItems().add(paramItem);
        benchmark.setParametersCount(benchmark.getParamItems().size());
        if (StringUtils.isNotBlank(vehicleInfo.getSimCardNumber())) {
            //订阅消息
            SubscibeInfo info = new SubscibeInfo(SystemHelper.getCurrentUsername(), vehicleInfo.getDeviceId(), transNo,
                ConstantUtil.T808_DEVICE_GE_ACK, 1);
            SubscibeInfoCache.getInstance().putTable(info);
            info = new SubscibeInfo(SystemHelper.getCurrentUsername(), vehicleInfo.getDeviceId(), transNo,
                ConstantUtil.T808_DATA_PERMEANCE_REPORT);
            SubscibeInfoCache.getInstance().putTable(info);

            T808Message message = MsgUtil
                .get808Message(vehicleInfo.getSimCardNumber(), ConstantUtil.T808_SET_PARAM, transNo, benchmark,
                    vehicleInfo.getDeviceType());
            WebSubscribeManager.getInstance()
                .sendMsgToAll(message, ConstantUtil.T808_SET_PARAM, vehicleInfo.getDeviceId());
            String userName = SystemHelper.getCurrentUsername();
            paramSendingCache.put(userName, transNo, vehicleInfo.getSimCardNumber(),
                SendTarget.getInstance(SendModule.FUEL_CONSUMPTION));
        }
    }

}

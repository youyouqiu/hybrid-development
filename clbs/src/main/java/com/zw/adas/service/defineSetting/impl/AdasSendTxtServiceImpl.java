package com.zw.adas.service.defineSetting.impl;

import com.zw.adas.service.defineSetting.AdasSendTxtService;
import com.zw.platform.basic.constant.MonitorTypeEnum;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.util.common.MonitorHelper;
import com.zw.platform.domain.core.SendParam;
import com.zw.platform.push.cache.ParamSendingCache;
import com.zw.platform.push.cache.SendModule;
import com.zw.platform.push.cache.SendTarget;
import com.zw.platform.push.controller.SubscibeInfo;
import com.zw.platform.push.controller.SubscibeInfoCache;
import com.zw.platform.push.handler.device.DeviceHelper;
import com.zw.platform.service.core.F3SendStatusProcessService;
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

/**
 * <p> Title: <p> Copyright: Copyright (c) 2016 <p> Company: ZhongWei <p> team: ZhongWeiTeam
 * @version 1.0
 * @author: nixiangqian
 * @date 2017年07月20日 11:16
 */
@Service
public class AdasSendTxtServiceImpl implements AdasSendTxtService {
    @Autowired
    private SendHelper sendHelper;

    @Autowired
    private MonitorHelper monitorHelper;
    @Autowired
    private F3SendStatusProcessService f3SendStatusProcessService;

    @Autowired
    private ParamSendingCache paramSendingCache;

    /**
     * 下发8103
     * @param vehicleId     车id
     * @param parameterName 设置id
     * @param paramType     下发参数类型
     * @param isOvertime    是否进行超时 true 超时 false 不超过
     * @Description: 参数设置下发
     */
    @Override
    public String sendF3SetParam(String vehicleId, String parameterName, List<ParamItem> params, String paramType,
        boolean isOvertime, String userName) throws Exception {
        // 获取最后一次下发的编号
        String paramId = sendHelper.getLastSendParamID(vehicleId, parameterName, paramType);
        // 获取车辆及设备信息
        BindDTO bindDTO = monitorHelper.getBindDTO(vehicleId, MonitorTypeEnum.VEHICLE);
        String deviceId = bindDTO.getDeviceId();
        String simCardNumber = bindDTO.getSimCardNumber();
        String deviceNumber = bindDTO.getDeviceNumber();
        // 序列号
        Integer msgSN = DeviceHelper.getRegisterDevice(vehicleId, deviceNumber);
        if (msgSN != null) { // 设备已经注册
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
            // 先订阅再下发
            T808Message message = MsgUtil
                .get808Message(simCardNumber, ConstantUtil.T808_SET_PARAM, msgSN, t8080x8103, bindDTO.getDeviceType());

            SubscibeInfo subscibeInfo = new SubscibeInfo(userName, deviceId, msgSN, ConstantUtil.T808_DEVICE_GE_ACK);
            SubscibeInfoCache.getInstance().putTable(subscibeInfo);
            paramSendingCache.put(userName, msgSN, simCardNumber, SendTarget.getInstance(SendModule.ACTIVE_SECURITY));
            subscibeInfo = new SubscibeInfo(userName, deviceId, msgSN, ConstantUtil.T808_PENETRATE_UP);
            SubscibeInfoCache.getInstance().putTable(subscibeInfo);
            WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_SET_PARAM, deviceId);
            return String.valueOf(msgSN);
        } else { // 设备未注册
            msgSN = 0;// 绑定下发
            sendHelper.updateParameterStatus(paramId, msgSN, 5, vehicleId, paramType, parameterName);
        }
        return String.valueOf(msgSN);
    }
}

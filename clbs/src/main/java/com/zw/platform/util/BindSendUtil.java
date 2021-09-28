package com.zw.platform.util;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.domain.infoconfig.form.MonitorInfo;
import com.zw.platform.domain.netty.BindInfo;
import com.zw.platform.service.infoconfig.ConfigService;
import com.zw.platform.service.oilsubsidy.ForwardVehicleManageService;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * @Author: zjc
 * @Description:绑定下发通用工具类
 * @Date: create in 2021/1/6 14:25
 */
@Component
public class BindSendUtil {

    @Autowired
    private ForwardVehicleManageService forwardVehicleManageService;

    @Autowired
    private ConfigService configService;

    /**
     * 单车下发的一些通用方法包装（下发绑定信息到协议端）
     * @param bindInfo
     * @param monitorInfo
     */
    public void sendBindInfo(BindInfo bindInfo, MonitorInfo monitorInfo) {
        bindInfo.setMonitorInfo(JSONObject.parseObject(JSONObject.toJSONString(monitorInfo)));
        sendBindInfos(Collections.singletonList(bindInfo));
    }

    /**
     * 单车下发的一些通用方法包装（下发绑定信息到协议端）
     * @param bindInfo
     */
    public void sendBindInfo(BindInfo bindInfo) {
        sendBindInfos(Collections.singletonList(bindInfo));
    }

    /**
     * 多车下发的一些通用方法包装（下发绑定信息到协议端）
     * @param bindInfos
     */
    public void sendBindInfos(List<BindInfo> bindInfos) {
        //油补809新增相关字段数据组装
        forwardVehicleManageService.initOilBindInfos(bindInfos);
        //组装4.3.7入网状态下发
        configService.assembleNetWork(bindInfos);
        for (BindInfo bindInfo : bindInfos) {
            //协议端要求的
            RedisHelper.delete(
                HistoryRedisKeyEnum.DEVICE_BIND.of(bindInfo.getOldIdentification(), bindInfo.getOldDeviceType()));
            WebSubscribeManager.getInstance().sendMsgToAll(bindInfo, ConstantUtil.WEB_DEVICE_BOUND);
        }
    }

}

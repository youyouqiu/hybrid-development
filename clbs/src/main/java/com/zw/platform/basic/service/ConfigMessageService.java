package com.zw.platform.basic.service;

import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.ConfigDTO;
import com.zw.platform.domain.netty.BindInfo;
import com.zw.platform.domain.netty.DeviceUnbound;
import com.zw.platform.util.common.BusinessException;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 监控对象消息服务
 * @author zj
 */
public interface ConfigMessageService {
    /**
     * 获取下发到协议端的绑定信息
     * @param curBindDTO 当前绑定信息
     * @param oldBindDTO 历史的绑定信息 可为空
     * @return 下发到协议端的信息
     */
    BindInfo getSendToF3Data(ConfigDTO curBindDTO, BindDTO oldBindDTO);

    /**
     * 单个监控对象绑定信息下发到F3
     * @param curConfigDTO 当前绑定信息
     * @param oldBindDTO   修改前的绑定休息 新增可以为空
     * @return 是否下发
     * @throws BusinessException 异常
     */
    boolean sendToF3(ConfigDTO curConfigDTO, BindDTO oldBindDTO);

    /**
     * 通知web端 -- 修改时使用
     * @param curBindDTO 当前绑定信息
     * @param oldBindDTO 修改前的绑定休息
     */
    void sendToWeb(BindDTO curBindDTO, BindDTO oldBindDTO);

    /**
     * 构建监控对象解绑实体
     * @param deviceId      deviceId
     * @param deviceNumber  deviceNumber
     * @param deviceType    deviceType
     * @param simCardNumber simCardNumber
     * @return 构建监控对象解绑实体
     */
    DeviceUnbound getDeviceUnbound(String deviceId, String deviceNumber, String deviceType, String simCardNumber);

    /**
     * 通知到web端 -- 解绑时使用
     * @param deleteBindList 解绑的监控对象
     */
    void sendToWeb(List<BindDTO> deleteBindList);

    /**
     * 发送解绑信息到F3
     * @param deviceUnBindList 解绑信息
     */
    void sendUnBindToF3(Collection<DeviceUnbound> deviceUnBindList);

    /**
     * 通知storm
     * @param vehicleIds 车辆Id集合
     * @param peopleIds  人员ID集合
     * @param thingIds   物品ID集合
     */
    void sendUnBindToStorm(Set<String> vehicleIds, Set<String> peopleIds, Set<String> thingIds);

    /**
     * 通知storm
     * @param monitorType 监控对象类型
     * @param monitorId   监控对象ID
     */
    void sendToStorm(String monitorType, String monitorId);

    /**
     * 通知storm
     * @param curBindDTO 当前修改的信息配置
     * @param oldBindDTO 修改前的信息配置
     */
    void sendToStorm(BindDTO curBindDTO, BindDTO oldBindDTO);

    /**
     * 分组专用
     * 批量下发监控对象信息到F3 -- 增对的是终端和SIM卡未做改变的监控对象
     * @param monitorIds 监控对象Id集合
     */
    void sendToF3(Collection<String> monitorIds);
}

package com.zw.adas.domain.define.setting;

import com.zw.platform.basic.domain.BaseKvDo;
import lombok.Data;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

/**
 * @Author: zjc
 * @Description: 主动安全列表参数
 * @Date: create in 2020/11/25 10:22
 */
@Data
public class AdasSettingListDo {
    /**
     * 车辆运行监测
     */
    private Integer vehicleOperation;
    /**
     * 上下客及超员
     */
    private Integer overcrowd;

    /**
     * 不知道干什么用的
     */
    private Integer analytical;
    /**
     * 驾驶员行为下发状态
     */
    private Integer driverBehavior;
    /**
     * 胎压监测下发状态
     */
    private Integer tirePressure;
    /**
     * 前向监测
     */
    private Integer forward;
    /**
     * 盲区监测下发状态
     */
    private Integer blindDetection;
    /**
     * 激烈驾驶下发状态
     */
    private Integer intenseDriving;
    /**
     *
     */
    private Integer driverComparison;
    /**
     * 协议类型编号
     */
    private String bindId;
    /**
     * 车辆在线状态
     */
    private int onLineStatus;
    /**
     * 分组名称
     */
    private String groupName;
    /**
     * 下发时间
     */
    private Date createDataTime;
    /**
     * 车辆id
     */
    private String vehicleId;
    /**
     * 京标驾驶行为
     */
    private Integer jingDriverBehavior;

    /**
     * 黑标驾驶员驾驶行为下发状态
     */
    private Integer heiDriverDrivingBehavior;
    /**
     * 黑标驾驶员身份识别下发状态
     */
    private Integer heiDriverIdentification;
    /**
     * 黑标设备失效监测下发状态
     */
    private Integer heiEquipmentFailureMonitoring;
    /**
     * 黑标车辆运行监测下发状态
     */
    private Integer heiVehicleOperationMonitoring;

    /**
     * 车牌号
     */
    private String brand;

    public void initSendStatus(Map<String, BaseKvDo<String, Integer>> sendStatus) {
        //设置下发状态
        Optional.ofNullable(getStatus(38, sendStatus)).ifPresent(e -> heiDriverIdentification = e.getFirstVal());
        Optional.ofNullable(getStatus(39, sendStatus)).ifPresent(e -> heiVehicleOperationMonitoring = e.getFirstVal());
        Optional.ofNullable(getStatus(40, sendStatus)).ifPresent(e -> heiDriverDrivingBehavior = e.getFirstVal());
        Optional.ofNullable(getStatus(41, sendStatus)).ifPresent(e -> heiEquipmentFailureMonitoring = e.getFirstVal());

        Optional.ofNullable(getStatus(51, sendStatus)).ifPresent(e -> jingDriverBehavior = e.getFirstVal());
        Optional.ofNullable(getStatus(52, sendStatus)).ifPresent(e -> vehicleOperation = e.getFirstVal());
        Optional.ofNullable(getStatus(64, sendStatus)).ifPresent(e -> forward = e.getFirstVal());
        Optional.ofNullable(getStatus(65, sendStatus)).ifPresent(e -> driverBehavior = e.getFirstVal());
        Optional.ofNullable(getStatus(66, sendStatus)).ifPresent(e -> tirePressure = e.getFirstVal());
        Optional.ofNullable(getStatus(67, sendStatus)).ifPresent(e -> blindDetection = e.getFirstVal());
        Optional.ofNullable(getStatus(68, sendStatus)).ifPresent(e -> overcrowd = e.getFirstVal());
        Optional.ofNullable(getStatus(70, sendStatus)).ifPresent(e -> intenseDriving = e.getFirstVal());
        Optional.ofNullable(getStatus(0xE9, sendStatus)).ifPresent(e -> driverComparison = e.getFirstVal());
    }

    private BaseKvDo<String, Integer> getStatus(int num, Map<String, BaseKvDo<String, Integer>> sendStatusMap) {
        String key = String.format("%s%s_ADAS", vehicleId, num);
        return sendStatusMap.get(key);

    }
}

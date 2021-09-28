package com.zw.platform.domain.vas.workhourmgt;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author jiangxiaoqiang
 * @date 2016/10/14
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class WorkHourStatistics {
    private static final long serialVersionUID = 1L;

    /**
     * 车辆id
     */
    private byte[] vehicleId;

    /**
     * 位置数据更新时间
     */
    private long vTime;

    /**
     * 位置数据更新时间
     */
    private String vTimeStr;

    /**
     * 经度
     */
    private String longtitude;

    /**
     * 纬度
     */
    private String latitude;

    /**
     * 速度
     */
    private String speed;

    /**
     * 车牌号
     */
    private String monitorName;

    /**
     * 工时检查方式   0:电压比较式  1:油耗阈值式  2:油耗波动式
     */
    private Integer workInspectionMethod;

    /**
     * 工作状态   0:停机 1:工作 2:待机(油耗波动式专有)
     */
    private Integer workingPosition;

    /**
     * 持续时长
     */
    private Long continueTime;

    private String continueTimeStr;

    /**
     * 波动值
     */
    private Double fluctuateValue;

    /**
     * 检测数据
     */
    private Double checkData;

    /**
     * 车辆id
     */
    private String monitorId;

    /**
     * 状态
     */
    private String status;
    /**
     * Acc 状态
     */
    private Integer acc;
    /**
     * 定位状态
     */
    private String locationStatus;
    /**
     * 车牌颜色
     */
    private String plateColor;

    /**
     * 分组名称
     */
    private String groupName;

    /**
     * 行驶里程
     */
    private String gpsMile;

    /**
     * 里程传感器车速
     */
    private Double mileageSpeed;

    /**
     * 里程传感器累积里程
     */
    private Double mileageTotal;

    /**
     * 位置
     */
    private String address;
    /**
     * 位置坐标(纬度,经度)
     */
    private String positionCoordinates;

}

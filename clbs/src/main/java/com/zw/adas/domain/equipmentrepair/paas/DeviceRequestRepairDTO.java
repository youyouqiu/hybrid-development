package com.zw.adas.domain.equipmentrepair.paas;

import lombok.Data;

/**
 * 终端设备维修
 * @author zhangjuan
 */
@Data
public class DeviceRequestRepairDTO {
    /**
     * 企业id
     */
    private String groupId;

    /**
     * 监控对象id
     */
    private String monitorId;

    /**
     * 报修时间(格式:yyyyMMdddHHmmss)
     */
    private String time;

    /**
     * 故障类型
     * 0:主存储器异常
     * 1:备用存储器异常
     * 2:卫星信号异常
     * 3:通信信息号异常
     * 4:备用电池欠压
     * 5:备用电池失效
     * 6:IC卡从业资格证模块故障
     */
    private Integer type;

    /**
     * 监控对象名称
     */
    private String monitorName;

    /**
     * 分组名称
     */
    private String assignmentName;

    /**
     * 企业名称
     */
    private String groupName;

    /**
     * 车牌颜色
     * 1:蓝色 2:黄色 3:黑色 4:白色 5:绿色 9:其他 93:黄绿色 94:渐变绿色
     */
    private Integer plateColor;

    /**
     * 车辆类型
     */
    private String vehicleType;

    /**
     * 终端id
     */
    private String deviceId;

    /**
     * 终端厂商
     */
    private String terminalVendor;

    /**
     * 终端型号
     */
    private String terminalType;

    /**
     * 终端编号
     */
    private String deviceNumber;

    /**
     * 故障处理状态  0:未确认 1:已确认 2:已完成 3:误报
     */
    private Integer handleStatus;

    /**
     * 修理日期(格式:yyyyMMdd)
     */
    private String repairDate;

    /**
     * 主键(企业id_报修时间_故障类型_车辆id)
     */
    private String primaryKey;

    /**
     * 备注
     */
    private String remark;
}

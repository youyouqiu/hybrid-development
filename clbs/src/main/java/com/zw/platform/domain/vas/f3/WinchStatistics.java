package com.zw.platform.domain.vas.f3;

import lombok.Data;

/**
 * 正反转统计实体类
 */
@Data
public class WinchStatistics {

    private String monitorId;//车辆id

    private String monitorName; //车牌号

    private long vTime;//时间

    private Integer winchStatus;//正反转传感器旋转状态 01-停转 02-运行

    private String status;

    private Integer winchOrientation;//正反转传感器旋转方向 01-顺时针旋转 02-逆时针旋转

    private String orientation;

    private Integer winchSpeed; //正反转传感器旋转速度

    private Double gpsMile;//行驶里程

    private Double speed;//速度

    private String address;//位置

    private String longtitude;//经度

    private String latitude;//纬度

    private String positionCoordinates;//位置坐标(纬度,经度)

    private Integer winchTime; //正反转传感器累计运行时间

    private Integer winchCounter; //正反转传感器累计脉冲数量

    private Integer winchRotateTime;//正反转传感器旋转方向持续时间
}

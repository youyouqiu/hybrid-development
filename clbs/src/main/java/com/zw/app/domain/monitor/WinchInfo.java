package com.zw.app.domain.monitor;

import lombok.Data;


/**
 * 监控对象正反转数据
 */
@Data
public class WinchInfo {
    /**
     * 时间
     */
    private String time;

    /**
     * 旋转方向
     */
    private Integer orientation;

    /**
     * 旋转速度
     */
    private String rotationSpeed;

    /**
     * 旋转状态
     */
    private String  rotationStatus;

    /**
     * 累计运行时间
     */
    private String workTime;

    /**
     * 累计脉冲数据量
     */
    private String pulseCount;

    /**
     * 旋转方向持续时间
     */
    private String rotationTime;
}

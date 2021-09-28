package com.zw.platform.domain.vas.alram;

import lombok.Data;

import java.io.Serializable;

/**
 * @author penghj
 * @version 1.0
 * @date 2018/6/29 16:30
 */
@Data
public class IoAlarmSettingInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 车id
     */
    private String vehicleId;
    /**
     * 报警推送（0、无 1、局部 2、全局）
     */
    private Integer alarmPush;
    /**
     * 字典标识
     */
    private String pos;
    /**
     * 是否异常报警标识 0:高电平为异常报警 1:低电平为异常报警
     */
    private String parameterValue;
}

package com.zw.ws.entity.device;

import lombok.Data;

import java.io.Serializable;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/11/5 9:29
 */
@Data
public class DeviceWakeUpEntity implements Serializable {
    private static final long serialVersionUID = -3503941099141630099L;
    /**
     * 监控对象id
     */
    private String monitorId;
    /**
     * 唤醒时长 分钟
     */
    private Integer wakeUpDuration;
}

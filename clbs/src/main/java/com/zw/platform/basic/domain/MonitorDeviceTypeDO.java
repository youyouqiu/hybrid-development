package com.zw.platform.basic.domain;

import lombok.Data;

/**
 * 绑定监控对象协议类型
 * @author penghj
 * @version 1.0
 * @date 2021/1/29 10:33
 */
@Data
public class MonitorDeviceTypeDO {
    /**
     * 监控对象id
     */
    private String monitorId;
    /**
     * 协议类型
     */
    private String deviceType;
}

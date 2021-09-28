package com.zw.platform.domain.infoconfig.dto;

import lombok.Data;

/**
 * 信息配置监控对象信息
 * @author create by zhouzongbo on 2020/9/8.
 */
@Data
public class ConfigMonitorDTO {

    /**
     * 监控对象ID
     */
    private String monitorId;

    /**
     * 监控对象名称
     */
    private String monitorName;

    /**
     * 信息配置ID
     */
    private String configId;

    /**
     * 车和物表中已经维护了这个字段
     */
    private String groupId;

    private String simCardId;

    private String simCardNumber;

    private String deviceId;

    private String deviceNumber;
}

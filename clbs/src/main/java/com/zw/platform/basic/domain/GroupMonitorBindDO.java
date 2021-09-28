package com.zw.platform.basic.domain;

import lombok.Data;

/**
 * 分组-监控对象绑定信息
 * @author penghj
 * @version 1.0
 * @date 2021/2/3 14:12
 */
@Data
public class GroupMonitorBindDO {
    /**
     * 分组Id
     */
    private String groupId;

    /**
     * 分组名称
     */
    private String groupName;

    /**
     * 分组所属企业id
     */
    private String orgId;

    /**
     * 监控对象Id
     */
    private String moId;

    /**
     * 监控对象名称
     */
    private String moName;

    /**
     * 监控对象别名
     */
    private String aliases;

    /**
     * 监控对象类型 0:车，1：人，3：物
     */
    private String monitorType;

    /**
     * 车牌颜色
     */
    private Integer plateColor;

    /**
     * 终端id
     */
    private String deviceId;

    /**
     * 终端编号
     */
    private String deviceNumber;

    /**
     * 终端类型
     */
    private String deviceType;

    /**
     * sim卡id
     */
    private String simCardId;

    /**
     * sim卡编号
     */
    private String simCardNumber;
}

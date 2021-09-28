package com.zw.platform.basic.domain;

import lombok.Data;

import java.util.Date;

/**
 * 分组信息及分组下的监控对象数量
 * @author penghj
 * @version 1.0
 * @date 2021/2/3 10:30
 */
@Data
public class GroupMonitorCountDo {
    private String id;
    /**
     * 分组名称
     */
    private String name;
    /**
     * 分组下监控对象数量
     */
    private String monitorCount;
    /**
     * 描述
     */
    private String description;
    /**
     * 标记
     */
    private int flag;
    /**
     * 创建数据时间
     */
    private Date createDataTime;
    /**
     * 创建用户
     */
    private String createDataUsername;
    /**
     * 联系人
     */
    private String contacts;
    /**
     * 联系电话
     */
    private String telephone;
    /**
     * 是否录音， 1：录音 0：不
     */
    private String soundRecording;
    /**
     * 对讲群组id
     */
    private String intercomGroupId;
    /**
     * 组呼号码
     */
    private String groupCallNumber;
    /**
     * 分组类型（分组0，群组1）
     */
    private String types;

    /**
     * 组织ID
     */
    private String orgId;
}

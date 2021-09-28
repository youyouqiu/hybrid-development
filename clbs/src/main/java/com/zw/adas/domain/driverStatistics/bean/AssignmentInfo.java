package com.zw.adas.domain.driverStatistics.bean;

import lombok.Data;


@Data
public class AssignmentInfo {
    /**
     * 所属企业名称
     */
    private String groupName;

    private String iconSkin;

    /**
     * 所属企业id
     */
    private String groupId;

    /**
     * 分组名称
     */
    private String name;

    /**
     * 分组父级id
     */
    private String pid;

    /**
     * 分组id
     */
    private String id;

    /**
     * 分组类型
     */
    private String type;

    /**
     * 分组类监控对象数量
     */
    private Integer count;
}

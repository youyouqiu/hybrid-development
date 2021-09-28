package com.zw.talkback.domain.basicinfo;

import lombok.Data;

import java.io.Serializable;

@Data
public class ClusterInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 分组id
     */
    private String id;
    /**
     * 该分组下的监控对象数量
     */
    private int vehicleNumber;
    /**
     * 分组名称
     */
    private String name;

    /**
     * 监控对象id
     */
    private String monitorId;

    /**
     * 对讲对象id
     */
    private Long interlocutorId;

    /**
     * 监控对象名称
     */
    private String monitorName;

    private  short types;
}

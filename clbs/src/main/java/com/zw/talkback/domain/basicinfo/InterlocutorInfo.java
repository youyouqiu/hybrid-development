package com.zw.talkback.domain.basicinfo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * @author penghj
 * @version 1.0
 * @date 2019/8/6 15:43
 */
@Data
public class InterlocutorInfo implements Serializable {

    private static final long serialVersionUID = 8202231609005659861L;

    /**
     * id
     */
    private String id = UUID.randomUUID().toString();

    /**
     * 监控对象id
     */
    private String monitorId;

    /**
     * 监控对象名称
     */
    private String monitorName;

    /**
     * 监控对象类型
     */
    private String monitorType;

    /**
     * 分组id
     */
    private String assignmentId;

    /**
     * 对讲对象id
     */
    private Long interlocutorId;

    /**
     * 个呼号码
     */
    private Integer userNumber;

    /**
     * 硬件旋钮个数 null或小于0:无组旋钮用户 大于0:带组旋钮用户
     */
    private Integer knobNum;

    /**
     * 已有的旋钮编号
     */
    private String knobNos;

    /**
     * 旋钮编号
     */
    private Integer knobNo;

    /**
     * 创建时间
     */
    private Date createDataTime;

    /**
     * 创建人
     */
    private String createDataUsername;

}

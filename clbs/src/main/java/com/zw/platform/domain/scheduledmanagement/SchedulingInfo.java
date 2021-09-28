package com.zw.platform.domain.scheduledmanagement;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 排班信息
 * @author penghj
 * @version 1.0
 * @date 2019/11/6 16:21
 */
@Data
public class SchedulingInfo implements Serializable {
    private static final long serialVersionUID = -4683436312942114555L;

    /**
     * 排班id
     */
    private String id;

    /**
     * 排班名字
     */
    private String scheduledName;

    /**
     * 排班开始时间
     */
    private Date startDate;

    /**
     * 排班结束时间
     */
    private Date endDate;

    /**
     * 排班开始时间str类型
     */
    private String startDateStr;

    /**
     * 排班结束时间str类型
     */
    private String endDateStr;

    /**
     * 日期重复类型
     */
    private String dateDuplicateType;

    /**
     * 备注
     */
    private String remark;

    /**
     * 所属企业id
     */
    private String groupId;

    /**
     * 企业名字
     */
    private String groupName;

    /**
     * 排班状态（1未开始,2已结束,3执行中）
     */
    private int status;

    /**
     * 排班创建时间
     */
    private Date createDataTime;

    /**
     * 排班创建人
     */
    private String createDataUsername;

    /**
     * 排班修改时间
     */
    private transient Date updateDataTime;

    /**
     * 排班修改创建人
     */
    private transient String updateDataUsername;

    /**
     * 判断是否是强制结束（1代表强制结束，0为没有强制结束）
     */
    private int isMandatoryTermination;

    private String startTime;

    private String endTime;

}


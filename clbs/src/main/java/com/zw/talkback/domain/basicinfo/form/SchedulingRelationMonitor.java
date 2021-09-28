package com.zw.talkback.domain.basicinfo.form;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 报表：排班管理监控对象
 * @author penghj
 * @version 1.0
 * @date 2019/11/7 17:51
 */
@Data
public class SchedulingRelationMonitor implements Serializable {
    private static final long serialVersionUID = -2292351196648274030L;

    private String id;

    /**
     * 排班id
     */
    private String scheduledInfoId;

    /**
     * 排班名称
     */
    private String scheduledName;

    /**
     * 监控对象id
     */
    private String monitorId;

    /**
     * 监控对象名称
     */
    private String monitorName;

    /**
     * 开始日期
     */
    private Date startDate;

    /**
     * 结束日期
     */
    private Date endDate;

    /**
     * 日期重复类型
     */
    private String dateDuplicateType;

    /**
     * 工作状态（是否在职 在职1 离职0  新增默认在职）
     */
    private Integer isIncumbency;
}

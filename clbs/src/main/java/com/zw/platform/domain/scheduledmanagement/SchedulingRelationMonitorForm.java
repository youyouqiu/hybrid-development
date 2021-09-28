package com.zw.platform.domain.scheduledmanagement;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;

import java.util.Date;

/**
 * 排班和监控对象关联关系
 * @author penghj
 * @version 1.0
 * @date 2019/11/6 16:33
 */
@Data
public class SchedulingRelationMonitorForm extends BaseFormBean {
    private static final long serialVersionUID = -3436719630379906631L;

    /**
     * 排班id
     */
    private String scheduledInfoId;

    /**
     * 监控对象id
     */
    private String monitorId;

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

}

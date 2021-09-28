package com.zw.platform.domain.taskmanagement;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

/**
 * 指派信息
 * @author penghj
 * @version 1.0
 * @date 2019/11/6 16:18
 */
@Data
public class DesignateInfo extends BaseFormBean {
    private static final long serialVersionUID = 3294885146224886822L;

    /**
     * 指派名称
     */
    private String designateName;

    /**
     * 任务id
     */
    private String taskId;

    private String taskName;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date startDate;

    private String startDateStr;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endDate;

    private String endDateStr;

    /**
     * 日期重复类型（周一至周日（1至7）逗号分隔，8：每天；）
     */
    private String dateDuplicateType;

    private String remark;

    private String groupId;

    private String groupName;

    private List<DesignateMonitorInfo> designatePeopleInfos;

    private String designatePeopleInfosStr;

    /**
     * 任务完成状态
     */
    private String status;

    /**
     * 任务项开始时间
     */
    private String startTime;

    private String endTime;

    private String peopleId;

    private String peopleNumber;

    private Integer forcedEnd;
}

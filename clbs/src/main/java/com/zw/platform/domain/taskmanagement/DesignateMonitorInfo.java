package com.zw.platform.domain.taskmanagement;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * 任务指派和监控对象关系
 * @author penghj
 * @version 1.0
 * @date 2019/11/6 16:19
 */
@Data
public class DesignateMonitorInfo implements Serializable {
    private static final long serialVersionUID = -1490578074767065575L;

    private String id = UUID.randomUUID().toString();

    private String designateInfoId;

    private String peopleId;

    private String peopleName;

    private Date startDate;

    private Date endDate;

    private String dateDuplicateType;

    private Integer flag = 1;

    private String startTime;

    private String endTime;
}

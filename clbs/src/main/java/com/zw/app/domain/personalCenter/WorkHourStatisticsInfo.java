package com.zw.app.domain.personalCenter;

import lombok.Data;

import java.io.Serializable;

/**
 * @author penghj
 * @version 1.0
 * @date 2019/7/12 16:08
 */
@Data
public class WorkHourStatisticsInfo implements Serializable {
    private static final long serialVersionUID = -5219908629643151550L;
    /**
     * 监控对象id
     */
    private byte[] monitorIdByte;
    private String monitorId;
    /**
     * 时间
     */
    private Long day;
    /**
     * 监控对象名称
     */
    private String monitorName;
    /**
     * 工作时间
     */
    private Long workTime;
    /**
     * 停机时间
     */
    private Long stopTime;
    /**
     * 待机时间
     */
    private Long awaitTime;
}

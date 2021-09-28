package com.zw.lkyw.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/1/17 9:38
 */
@Data
public class MonitorAlarmCount implements Serializable {
    private static final long serialVersionUID = 4220787220277962900L;
    /**
     * 监控对象id
     */
    private String monitorId;
    /**
     * 监控对象名称
     */
    private String monitorName;
    /**
     * 报警类型对应的总报警数量和已处理报警数量
     */
    private List<AlarmNumberCount> numInfo;
}

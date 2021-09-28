package com.zw.platform.domain.reportManagement.query;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 大数据月表查询参数类
 * @author hujun
 * @date 2018/9/27 9:34
 */
@Data
public class BigDataReportQuery implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<byte[]> monitorIds;// 监控对象ids

    private long startTime;// 开始时间

    private long endTime;// 结束时间

    private String month;// 查询年月 201809

    private byte[] monitor; // 监控对象id 针对单车查询

    private List<Integer> alarmTypes; //报警类型

    /**
     * 里程
     */
    private Double mile;
}

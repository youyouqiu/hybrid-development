package com.zw.platform.domain.reportManagement;

import lombok.Data;


@Data
public class TotalMileage {

    private byte[] monitorId;

    private Long day;

    private Double gpsMile;

    private String monitorIdStr;

    /**
     * 日期
     */
    private String time;
}

package com.zw.platform.domain.BigDataReport;

import lombok.Data;

import java.io.Serializable;


@Data
public class DateBean implements Serializable {
    private Long startTime;

    private Long endTime;
}

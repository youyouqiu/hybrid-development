package com.zw.platform.domain.riskManagement.form;

import lombok.Data;


@Data
public class EventForm {
    private String idStr;

    private byte[] id;

    private String eventNumber;

    private Long eventTime;

    private String eventTimeStr;

    private String riskEvent;

    private String brand;

    private String riskType;

    private Integer riskLevel;

    private Long warnTime;

    private String warnTimeStr;

    private Long fileTime;

    private String fileTimeStr;

    private String address;

    private String driverName;

    private Integer status;

    private String dealer;

    private String job;

    private Long dealTime;

    private String dealTimeStr;

    private Integer visitTimes;

    private Integer result;

    private String riskNumber;

    private String vehicleId;

    private String riskId;

}

package com.zw.platform.domain.riskManagement.form;

import lombok.Data;


@Data
public class RiskForm {
    private String address;

    private String brand;

    private Long dealTime;//需转

    private String dealTimeStr;

    private String dealUser;

    private String driver;

    private Long fileTime;//需转

    private String fileTimeStr;

    private String formattedAddress;

    private byte[] id;

    private String idStr;

    private String job;

    private Integer riskLevel;

    private String riskNumber;

    private Integer riskResult;

    private String riskType;

    private Integer status;

    private String vehicleId;

    private Integer visitTimes;

    private Long warTime;//需转

    private String warTimeStr;

    private String driverIds;//司机ids

    private String visit1;

    private String visit2;

    private String visit3;

    private String visit4; //归档的回访

}

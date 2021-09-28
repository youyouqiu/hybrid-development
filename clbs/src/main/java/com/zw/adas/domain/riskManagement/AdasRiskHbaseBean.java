package com.zw.adas.domain.riskManagement;

import lombok.Data;

@Data
public class AdasRiskHbaseBean {

    private Integer riskLevel;

    private String riskNumber;

    private String driver;

    private String dealer;

    private Integer riskResult;

    private String brand;

    private String vehicleId;

}

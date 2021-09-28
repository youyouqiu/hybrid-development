package com.zw.platform.domain.riskManagement;

import lombok.Data;

import java.io.Serializable;

@Data
public class RiskEventShortInfo implements Serializable {

    private String riskEventId;

    private Integer alarmType;
}

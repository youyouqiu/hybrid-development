package com.zw.platform.domain.reportManagement.form;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
public class RiskEvidenceBean {
    private String brand;
    private String riskType;
}

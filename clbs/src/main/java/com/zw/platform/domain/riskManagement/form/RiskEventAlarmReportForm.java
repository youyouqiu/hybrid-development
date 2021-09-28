package com.zw.platform.domain.riskManagement.form;

import java.util.Date;

import com.zw.platform.util.common.BaseFormBean;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by PengFeng on 2017/8/25  15:51
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class RiskEventAlarmReportForm extends BaseFormBean {
    private String eventNumber;
    private Date eventTime;
    private String riskEvent;
    private String riskType;
}

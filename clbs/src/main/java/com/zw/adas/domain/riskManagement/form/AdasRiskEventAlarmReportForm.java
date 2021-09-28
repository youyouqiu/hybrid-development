package com.zw.adas.domain.riskManagement.form;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * Created by PengFeng on 2017/8/25  15:51
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AdasRiskEventAlarmReportForm extends BaseFormBean {
    private String eventNumber;
    private Date eventTime;
    private String riskEvent;
    private String riskType;
}

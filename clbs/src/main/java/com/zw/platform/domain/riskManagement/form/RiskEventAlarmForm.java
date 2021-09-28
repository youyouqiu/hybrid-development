package com.zw.platform.domain.riskManagement.form;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * Created by PengFeng on 2017/8/25  15:51
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class RiskEventAlarmForm extends BaseFormBean {
    private String eventNumber;

    private String eventTime;

    private String riskEvent;

    private byte[] idbyte;

    private String riskType;
}

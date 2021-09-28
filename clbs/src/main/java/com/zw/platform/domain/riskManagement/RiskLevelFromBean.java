package com.zw.platform.domain.riskManagement;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by PengFeng on 2017/8/17  17:39
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class RiskLevelFromBean extends BaseFormBean {
    private String riskLevel;
    private String description;
    private String riskValue;
}

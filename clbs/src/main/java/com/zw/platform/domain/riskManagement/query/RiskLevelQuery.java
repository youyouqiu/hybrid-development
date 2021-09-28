package com.zw.platform.domain.riskManagement.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * Created by PengFeng on 2017/8/16  15:48
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class RiskLevelQuery extends BaseQueryBean implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * id
     */
    private String id;
    /**
     * risk_level
     */
    private String level;
    /**
     * risk_value
     */
    private String value;
    private String description;
    private String flag;
    private String createTime;
    private String createUser;
    private String updataTime;
    private String updataUser;
}

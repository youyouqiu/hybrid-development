package com.zw.adas.domain.riskManagement.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 风险时间 Created by Tdz on 2017/8/16.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AdasRiskEventQuery extends BaseQueryBean implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 风险事件信息
     */
    private String id;

    /**
     * 风险事件
     */
    private String riskEvent;

    /**
     * 风险类型
     */
    private String riskType;

    /**
     * 事件编号
     */
    private String eventNumber;

    private Date eventTime;
    /**
     * 风险描述
     */
    private String description;

    /**
     * 功能id
     */
    private int functionId;

    private Short flag;

    private Date createDataTime;

    private String createDataUsername;

    private Date updateDataTime;

    private String updateDataUsername;

    private byte[] eventId;
}

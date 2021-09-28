package com.zw.adas.domain.riskManagement.form;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;


/**
 * 风险处理
 *
 * @author  Tdz
 **/
@Data
public class AdasRiskVisitReportForm extends BaseFormBean {
    /**
     * 风险ID
     */
    private String riskId;

    /**
     * 风险预警信息准确性
     */
    private String warningAccuracy;

    /**
     * 预警后车辆及驾驶员状态情况
     */
    private String warnAfterStatus;

    /**
     * 风控管理人 人工 干预 情况
     */
    private String interventionPersonnel;

    /**
     * 风控管理人员人工干预后配合情况
     */
    private String interventionAfterStatus;

    /**
     * 本次风险预警后危险状态情况
     */
    private String warningLevel;

    /**
     * 详细内容
     */
    private String content;

    /**
     * 回访理由
     */
    private String reason;

    /**
     * 是否回访  0不回访  1 回访
     */
    private Short isvisit;

    /**
     * 回访时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date visitTime;

    /**
     * 回访顺序，标明是第几次回访
     */
    private Integer visitOrder;

    /**
     * 处理人
     */
    private String dealId;

    /**
     * 司机
     */
    private String driverId;

    /**
     * 司机名称
     */
    private String driverName;

    /**
     * 风控结果
     */
    private int riskResult;

}

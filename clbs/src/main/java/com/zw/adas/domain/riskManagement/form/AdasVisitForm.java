package com.zw.adas.domain.riskManagement.form;

import lombok.Data;


@Data
public class AdasVisitForm {
    /**
     * 风险ID
     */
    private byte[] riskId;

    private String riskIdStr;

    /**
     * 风险预警信息准确性
     */
    private Short warningAccuracy;

    /**
     * 预警后车辆及驾驶员状态情况
     */
    private Short warnAfterStatus;

    /**
     * 风控管理人 人工 干预 情况
     */
    private Short interventionPersonnel;

    /**
     * 风控管理人员人工干预后配合情况
     */
    private Short interventionAfterStatus;

    /**
     * 本次风险预警后危险状态情况
     */
    private Short warningLevel;

    /**
     * 详细内容
     */
    private String content;

    private Long visitTime;

    private String visitTimeStr;

    private String driverName;

    private String reason;

}

package com.zw.platform.domain.riskManagement.form;


import com.zw.platform.domain.multimedia.form.MediaForm;
import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;


/**
 * 风险处理 @author  Tdz
 * @create 2017-08-24 13:53
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class RiskVisitForm extends BaseFormBean {
    private String vistId;

    /**
     * 风险ID
     */
    private String riskId;

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

    /**
     * 回访理由
     */
    private String reason;

    /**
     * 是否回访 0不回访 1 回访
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

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date fileTime;// 结束时间

    private String dealId;// 处理人

    private String driverId;// 司机

    private int riskResult;// 风控结果

    private String riskNum;

    private List<MediaForm> mediaForm;

    /**
     * 文件名
     */
    private String fileNames;

}

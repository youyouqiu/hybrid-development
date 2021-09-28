package com.zw.platform.domain.riskManagement;

import com.alibaba.fastjson.annotation.JSONField;
import com.zw.platform.domain.leaderboard.RISKRESULT;
import com.zw.platform.domain.riskManagement.form.RiskCampaignForm;
import com.zw.platform.util.ConstantUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;


/**
 * 用于elasticSearch 索引
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RiskEsBean extends BaseEsBean {

    /*车辆id*/
    @JSONField(name = "vehicle_id")
    private String vehicleId;

    /*报警时间*/
    @JSONField(name = "warning_time", format = "yyyy-MM-dd HH:mm:ss")
    private Date warningTime;

    /*事件编号*/
    @JSONField(name = "risk_number")
    private String riskNumber;

    /*风险类型*/
    @JSONField(name = "risk_type")
    private String riskType;

    /*风险等级*/
    @JSONField(name = "risk_level")
    private Integer riskLevel;

    private String brand;

    private String driver;

    /*未处理,处理中...*/
    private Integer status;

    /*处理人*/
    private String dealer;

    /*回访次数*/
    @JSONField(name = "visit_times")
    private Integer visitTimes;

    /*归档结果*/
    @JSONField(name = "risk_result")
    private Integer riskResult;

    @JSONField(name = "address")
    private String address;

    @JSONField(name = "accurate_flag")
    private Short accurateFlag;

    @JSONField(name = "supervise_flag")
    private Short superviseFlag;

    @JSONField(name = "deal_time", format = "yyyy-MM-dd HH:mm:ss")
    private Date dealTime;

    public RiskEsBean() {

    }

    public RiskEsBean(String id, String vehicleId, Date warningTime, String riskNumber, String riskType,
                      Integer riskLevel, String brand, String driver, Integer status, String dealer, Integer visitTimes,
                      Integer riskResult) {
        super.setId(id);
        this.vehicleId = vehicleId;
        this.warningTime = warningTime;
        this.riskNumber = riskNumber;
        this.riskType = riskType;
        this.riskLevel = riskLevel;
        this.brand = brand;
        this.driver = driver;
        this.status = status;
        this.dealer = dealer;
        this.visitTimes = visitTimes;
        this.riskResult = riskResult;
    }

    public RiskEsBean(String id, String driver, String dealer, Integer status, Integer visitTimes, Integer riskResult) {
        super.setId(id);
        this.driver = driver;
        this.status = status;
        this.dealer = dealer;
        this.visitTimes = visitTimes;
        this.riskResult = riskResult;
    }

    public static RiskEsBean fromRiskForm(RiskCampaignForm form) {
        RiskEsBean riskEsBean = new RiskEsBean();
        riskEsBean.setId(form.getId());
        riskEsBean.setDealer(form.getDealId());
        riskEsBean.setDealTime(new Date());
        int riskResult = form.getRiskResult();
        if (riskResult != ConstantUtil.RISK_RESULT_ACCEPT && riskResult != ConstantUtil.RISK_RESULT_REJECT
            && riskResult != ConstantUtil.RISK_RESULT_ACCIDENT) {
            riskResult = ConstantUtil.RISK_RESULT_UNTREATED;
        }
        riskEsBean.setRiskResult(riskResult);
        riskEsBean.setVisitTimes(form.getVisitTimes());
        riskEsBean.setDriver(form.getDriverName());
        return riskEsBean;
    }

    public static RiskEsBean getInstance(String riskId, Integer status, String dealer, Date dealTime) {
        RiskEsBean riskEsBean = new RiskEsBean();
        riskEsBean.setId(riskId);
        riskEsBean.status = status;
        riskEsBean.dealer = dealer;
        riskEsBean.dealTime = dealTime;
        riskEsBean.riskResult = RISKRESULT.SUCCESS_FILE.getCode();
        return riskEsBean;
    }
}

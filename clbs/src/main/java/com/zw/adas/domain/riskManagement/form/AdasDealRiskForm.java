package com.zw.adas.domain.riskManagement.form;

import lombok.Data;

import java.util.Date;

/***
 @Author zhengjc
 @Date 2019/3/8 9:40
 @Description 风险处理实体
 @version 1.0
 **/
@Data
public class AdasDealRiskForm {
    /**
     * 风险id
     */
    private String riskId;

    /**
     * 处理人
     */
    private String dealer;

    /**
     * 处理时间
     */
    private Date dealTime;

    private Long dealTimeVal;

    /**
     * 处理状态
     */
    private Integer status;

    /**
     *******************************
     **** 风险处理的相关的详情信息*****
     *******************************
     */

    /**
     * 风险信息是否准确:0不准确 1准确
     */
    private Integer accuracy;

    /**
     * 预警后车辆及驾驶员状态情况:0异常 1正常
     */
    private Integer driverStatus;

    /**
     * 是否人工干预:0否 1是
     */
    private Integer intervention;

    /**
     * 驾驶员是否配合:0否 1是
     */
    private Integer cooperation;

    /**
     * 风险预警后危险状态情况:0未解除 1已解除
     */
    private Integer warnStatus;

    /**
     * 处理结果 0:事故未发生 1事故已发生
     */
    private Integer riskResult;

    /**
     * 处理选择的那个司机
     */
    private String driver;
    /**
     * 驾驶员id
     */
    private String driverId;

    /**
     * 处理选择的那个司机名称
     */
    private String driverName;

    /**
     * 其他
     */
    private String other;

    /**
     * 插卡录入
     */
    private Integer lockType;

    /**
     * 从业资格证号
     */
    private String driverNumber;



    public void init() {
        dealTime = new Date();
        dealTimeVal = dealTime.getTime();
    }
}

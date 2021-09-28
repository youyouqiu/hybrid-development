package com.zw.adas.domain.riskManagement;

import lombok.Data;

import java.util.Date;

/***
 @Author zhengjc
 @Date 2019/7/22 20:09
 @Description 主动安全处理实体信息
 @version 1.0
 **/
@Data
public class AdasDealInfo {
    private Integer status;
    private String dealer;
    private Date dealTime;

    /**
     * 风险id
     */
    private String riskId;
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

    private String driverNumber;

    /**
     * 2019中位协议存储HBase添加字段
     */
    private String handleType;
    /**
     * 其他
     */
    private String other;

    public static AdasDealInfo of(Integer status, String dealer, Date dealTime, Integer riskResult) {
        AdasDealInfo adasDealInfo = new AdasDealInfo();
        adasDealInfo.status = status;
        adasDealInfo.dealer = dealer;
        adasDealInfo.dealTime = dealTime;
        adasDealInfo.riskResult = riskResult;
        return adasDealInfo;
    }

}

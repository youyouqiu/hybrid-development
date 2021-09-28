package com.zw.platform.domain.basicinfo;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @author zhouzongbo on 2018/5/10 9:30
 */
@Data
public class VehicleInsuranceInfo implements Serializable {
    private static final long serialVersionUID = -9157665033923721393L;

    private String id;

    /**
     * 车辆id
     */
    private String vehicleId;

    /**
     * 车牌号
     */
    private String brand;
    /**
     * 保险单号
     */
    private String insuranceId;

    /**
     * 保险类型
     */
    private String insuranceType;

    /**
     * 保险公司
     */
    private String company;

    /**
     * 保险开始时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date startTime;

    /**
     * 保险到期时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endTime;

    private String endTimeStr;
    private String startTimeStr;
    /**
     * 提前提醒天数
     */
    private Short preAlert;

    /**
     * 保险金额
     */
    private Integer amountInsured;

    /**
     * 折扣率(%)
     */
    private String discount;

    /**
     * 实际费用
     */
    private String actualCost;

    /**
     * 代理人
     */
    private String agent;

    /**
     * 电话
     */
    private String phone;

    /**
     * 备注
     */
    private String remark;
}

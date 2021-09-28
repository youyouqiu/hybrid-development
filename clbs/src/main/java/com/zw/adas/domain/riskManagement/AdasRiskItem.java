package com.zw.adas.domain.riskManagement;

import lombok.Data;

import java.util.Date;

/**
 * @author Chen Feng
 * @version 1.0 2018/5/10
 */
@Data
public class AdasRiskItem {
    private String id;

    private byte[] riskId;

    private String vehicleId;// 车辆id

    private String brand;// 车牌号

    private String riskNumber;// 风险编号

    private String riskLevel;// 风险等级

    private String riskType;// 风险类型

    private String status;// 风险状态

    private String riskStatus;

    private Date warningTime; // 开始时间

    private Integer videoFlag;

    private Integer picFlag;

    private String weather;

    private String picHtml;

    private String videoHtml;

    private String address;

    private String speed;

    /**
     * 从业资格证号
     */
    private String certificationCode;

    /**
     * 驾驶证号
     */
    private String driverLicenseNo;

    /**
     * 驾驶员姓名
     */
    private String driverName;
    private String driverNames;

}

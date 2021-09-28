package com.zw.platform.domain.vas.oilmgt;

import lombok.Data;

/**
 * Created by Tdz on 2016/9/23.
 */
@Data
public class OilMileageCount {
    /**
     * 车辆ID
     */
    private int vehicleId;
    /**
     * 车牌号
     */
    private String carLicense;
    /**
     * 车牌颜色
     */
    private String plateColor;
    /**
     * 车组名
     */
    private String groupName;
    /**
     * 开始时间
     */
    private String startTime;
    /**
     * 结束时间
     */
    private String endTime;
    /**
     * 开始里程
     */
    private double startMileage = -1;
    /**
     * 结束里程
     */
    private double endMileage = -1;
    /**
     * 行使里程
     */
    private double mileageCount;
    /**
     * 平均时速
     */
    private double averageSpeed;

    /**
     * 行驶时间
     */
    private String driveTime;
    /**
     * 平均油耗
     */
    private double averageOil;
    /**
     * 开始位置
     */
    private String startLoc;
    /**
     * 结束位置
     */
    private String endLoc;
    /**
     * 加/漏油量
     */
    private int oilChange = 0;
    /**
     * 油耗
     */
    private double consumeOil;
    /**
     * 开始油量
     */
    private double startOil;
    /**
     * 结束油量
     */
    private double endOil;


}

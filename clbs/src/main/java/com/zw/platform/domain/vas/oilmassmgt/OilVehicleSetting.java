package com.zw.platform.domain.vas.oilmassmgt;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * TODO 油箱车辆关联表
 * <p>Title: OilVehicleSetting.java</p>
 * <p>Copyright: Copyright (c) 2016</p>0#柴油
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @author: wangying
 * @date 2016年10月26日上午9:12:41
 * @version 1.0
 */
@Data
public class OilVehicleSetting implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 油箱与车辆关联
     */
    private String id;

    /**
     * 油箱id
     */
    private String oilBoxId;

    /**
     * 车辆id
     */
    private String vehicleId;

    /**
     *  油箱类型  油箱1   油箱2
     */
    private String oilBoxType;

    /**
     * 自动上传时间
     */
    private String automaticUploadTime;
    /**
     * 自动上传时间
     */
    private String automaticUploadTimeStr;

    /**
     * 输出修正系数K
     */
    private String outputCorrectionCoefficientK;

    /**
     * 输出修正系数B
     */
    private String outputCorrectionCoefficientB;

    /**
     *  液位报警阈值
     */
    private String liquidAlarmThreshold;

    /**
     *  油箱型号
     */
    private String type;

    /**
     *  油箱形状
     */
    private String shape;

    /**
     *  油箱形状
     */
    private String shapeStr;

    /**
     *  长度
     */
    private String boxLength;

    /**
     *  宽度
     */
    private String width;

    /**
     *  高度
     */
    private String height;

    /**
     *  壁厚
     */
    private String thickness;
    
    /**
     * 下圆角半径
     */
    private String buttomRadius;

    /**
     * 上圆角半径
     */
    private String topRadius;

    /**
     *  加油时间阈值
     */
    private String addOilTimeThreshold;

    /**
     * 加油量阈值
     */
    private String addOilAmountThreshol;

    /**
     *  漏油时间阈值
     */
    private String seepOilTimeThreshold;

    /**
     *  漏油油量阈值
     */
    private String seepOilAmountThreshol;

    /**
     *  理论容积
     */
    private String theoryVolume;

    /**
     *  油箱容量
     */
    private String realVolume;

    /**
     *  标定数组
     */
    private String calibrationSets;

    /**
     *  传感器型号
     */
    private String sensorType;

    private String sensorNumber;

    /**
     *  传感器长度
     */
    private String sensorLength;

    /**
     *  量程
     */
    private String measuringRange;

    /**
     *  上盲区
     */
    private String upperBlindZone;

    /**
     *  下盲区
     */
    private String lowerBlindArea;

    /**
     *  滤波系数
     */
    private String filteringFactor;

    /**
     *  滤波系数str
     */
    private String filteringFactorStr;

    /**
     *  波特率
     */
    private String baudRate;

    /**
     *  波特率Str
     */
    private String baudRateStr;

    /**
     *  奇偶校验
     */
    private Integer oddEvenCheck;

    /**
     *  奇偶校验Str
     */
    private String oddEvenCheckStr;

    /**
     *  补偿使能
     */
    private Integer compensationCanMake;


    /**
     *  补偿使能Str
     */
    private String compensationCanMakeStr;

    /**
     *  燃油
     */
    private String fuelOil;

    /**
     *  车辆类型
     */
    private String vehicleType;

    /**
     * 车牌号
     */
    private String brand;

    /**
     * 组织
     */
    private String groups;

    private String groupId;

    /**
     * 下发状态
     */
    private Integer status;

    private Integer flag;

    private Date createDataTime;

    private String createDataUsername;

    private Date updateDataTime;

    private String updateDataUsername;

    /**
     *  cheid
     *
     */
    private String vId;

    private String checkFlag = "1";

    private String settingParamId; // 油箱下发id

    private String calibrationParamId; // 标定下发id

    private String transmissionParamId; // 通讯参数下发id

}

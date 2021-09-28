package com.zw.api2.swaggerEntity;

import io.swagger.annotations.ApiParam;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;


/**
 * TODO 油箱车辆关联表
 * <p>Title: OilVehicleSetting.java</p>
 * <p>Copyright: Copyright (c) 2016</p>0#柴油
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 *
 * @version 1.0
 * @author: wangying
 * @date 2016年10月26日上午9:12:41
 */
@Data
public class SwaggerOilVehicleSetting implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 车辆id
     */
    @ApiParam(value = "车辆id", required = true)
    private String vehicleId;

    /**
     * 传感器型号
     */
    @ApiParam(value = "传感器型号", required = true)
    private String sensorType;

    /**
     * 油箱id
     */
    @ApiParam(value = "油箱id", required = true)
    private String oilBoxId;

    /**
     * 油箱与车辆关联
     */
    @ApiParam(value = "油箱与车辆关联id", required = true)
    private String id;

    /**
     * 车牌号
     */
    @ApiParam(value = "车牌号", required = true)
    private String brand;

    @ApiParam(value = "传感器编号", required = true)
    private String sensorNumber;

    /**
     * 油箱类型  油箱1   油箱2
     */
    @ApiParam(value = "油箱类型  油箱1   油箱2", required = true)
    private String oilBoxType;

    /**
     * 补偿使能Str
     */
    @ApiParam(value = " 补偿使能Str", required = true)
    private String compensationCanMakeStr;

    /**
     * 滤波系数str
     */
    @ApiParam(value = "滤波系数str", required = true)
    private String filteringFactorStr;

    /**
     * 自动上传时间
     */
    @ApiParam(value = "自动上传时间", required = true)
    private String automaticUploadTimeStr;

    /**
     * 输出修正系数K
     */
    @ApiParam(value = "输出修正系数K", required = true)
    private String outputCorrectionCoefficientK;

    /**
     * 输出修正系数B
     */
    @ApiParam(value = "输出修正系数B", required = true)
    private String outputCorrectionCoefficientB;

    /**
     * 传感器长度
     */
    @ApiParam(value = "传感器长度", required = true)
    private String sensorLength;

    /**
     * 燃油
     */
    @ApiParam(value = "燃油类型", required = true)
    private String fuelOil;

    /**
     * 油箱形状
     */
    @ApiParam(value = "油箱形状", required = true)
    private String shapeStr;

    /**
     * 长度
     */
    @ApiParam(value = "油箱长度", required = true)
    private String boxLength;

    /**
     * 宽度
     */
    @ApiParam(value = "油箱宽度", required = true)
    private String width;

    /**
     * 高度
     */
    @ApiParam(value = "油箱高度", required = true)
    private String height;

    /**
     * 加油时间阈值
     */
    @ApiParam(value = "加油时间阈值", required = true)
    private String addOilTimeThreshold;

    /**
     * 加油量阈值
     */
    @ApiParam(value = "加油量阈值", required = true)
    private String addOilAmountThreshol;

    /**
     * 漏油时间阈值
     */
    @ApiParam(value = "漏油时间阈值", required = true)
    private String seepOilTimeThreshold;

    /**
     * 漏油油量阈值
     */
    @ApiParam(value = "漏油油量阈值", required = true)
    private String seepOilAmountThreshol;
}

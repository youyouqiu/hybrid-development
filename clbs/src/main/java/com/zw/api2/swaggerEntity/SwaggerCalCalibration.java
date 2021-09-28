package com.zw.api2.swaggerEntity;

import io.swagger.annotations.Api;
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
public class SwaggerCalCalibration implements Serializable {
    private static final long serialVersionUID = 1L;

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
     * 壁厚
     */
    @ApiParam(value = "油箱壁厚", required = true)
    private String thickness;

    /**
     * 油箱形状
     */
    @ApiParam(value = "油箱形状", required = true)
    private String shape;

    /**
     * 传感器长度
     */
    @ApiParam(value = " 传感器长度", required = true)
    private String sensorLength;

    /**
     * 标定数组
     */
    @ApiParam(value = " 标定数组", required = true)
    private String calibrationSets;

    /**
     * 油箱容量
     */
    @ApiParam(value = " 油箱容量")
    private String realVolume;

    /**
     * 理论容积
     */
    @ApiParam(value = " 理论容积", required = true)
    private String theoryVolume;

    /**
     * 油箱与车辆关联
     */
    @ApiParam(value = "油箱与车辆关联", required = true)
    private String id;

    /**
     * 油箱类型  油箱1   油箱2
     */
    @ApiParam(value = "油箱类型  油箱1   油箱2", required = true)
    private String oilBoxType;

    /**
     * 下圆角半径
     */
    @ApiParam(value = "下圆角半径", required = true)
    private String buttomRadius;

    /**
     * 上圆角半径
     */
    @ApiParam(value = "上圆角半径", required = true)
    private String topRadius;

}

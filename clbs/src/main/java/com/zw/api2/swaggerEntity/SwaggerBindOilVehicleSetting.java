package com.zw.api2.swaggerEntity;

import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import io.swagger.annotations.ApiParam;
import lombok.Data;
import org.apache.bval.constraints.NotEmpty;

import javax.validation.constraints.Pattern;


@Data
public class SwaggerBindOilVehicleSetting {
    /**
     * 油箱与车辆关联
     */
    @NotEmpty(message = "【油箱1与车辆关联id】不能为空！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @ApiParam(value = "【油箱1与车辆关联id】不能为空！", required = true)
    private String id;

    /**
     * 油箱2与车辆关联
     */
    @ApiParam(value = "邮箱2与车辆绑定id(未绑定邮箱2不填,若绑定邮箱2必填)")
    private String id2;

    /**
     * 车牌号
     */
    @ApiParam(value = "车牌号")
    private String brand;

    /**
     * 车辆id
     */
    @NotEmpty(message = "【车辆id】不能为空！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @ApiParam(value = "【车辆id】不能为空！", required = true)
    private String vehicleId;

    /**
     * 油箱id
     */
    @NotEmpty(message = "【油箱1id】不能为空！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @ApiParam(value = "【油箱1id】不能为空！", required = true)
    private String oilBoxId;

    /**
     * 油箱形状
     */
    @ApiParam(value = "油箱形状,只能输入1,2,3,4;其中1:长方体,2:圆柱形,3:D形,4:椭圆形！")
    private String shape;

    /**
     * 长度
     */
    @ApiParam(value = "长度")
    private String boxLength;

    /**
     * 宽度
     */
    @ApiParam(value = "宽度")
    private String width;

    /**
     * 高度
     */
    @ApiParam(value = "高度")
    private String height;

    /**
     * 壁厚
     */
    @ApiParam(value = "壁厚")
    private String thickness;

    /**
     * 下圆角半径
     */
    @ApiParam(value = "下圆角半径")
    private String buttomRadius;

    /**
     * 上圆角半径
     */
    @ApiParam(value = "上圆角半径")
    private String topRadius;

    /**
     * 理论容积
     */
    @ApiParam(value = "理论容积")
    private String theoryVolume;

    /**
     * 油箱容量
     */
    @ApiParam(value = "油箱容量")
    private String realVolume;

    /**
     * 传感器型号
     */
    @NotEmpty(message = "【油箱1油杆id】不能为空！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @ApiParam(value = "【油箱1油杆id】不能为空！", required = true)
    private String sensorType;

    /**
     * 传感器长度
     */
    @ApiParam(value = "传感器长度")
    private String sensorLength;

    /**
     * 标定数组
     */
    @ApiParam(value = "标定数组 油箱1")
    private String calibrationSets;

    /**
     * 油量测量高度（高度1,高度2......）
     */
    @ApiParam(value = "油量测量高度（高度1,高度2......）")
    private String oilLevelHeights = "";

    /**
     * 油量值（值1,值2......）
     */
    @ApiParam(value = "油量值（值1,值2......）")
    private String oilValues = "";

    /**
     * 自动上传时间
     */
    @Pattern(message = "【自动上传时间1】输入错误，只能输入01,02,03,04;其中01:被动,02:10s,03:20s,04:30s！",
        regexp = "^[0][1-4]{1}$", groups = {
        ValidGroupAdd.class, ValidGroupUpdate.class})
    @ApiParam(value = "【自动上传时间1】输入错误，只能输入01,02,03,04;其中01:被动,02:10s,03:20s,04:30s！")
    private String automaticUploadTime;

    /**
     * 输出修正系数K
     */
    @ApiParam(value = "输出修正系数K")
    private String outputCorrectionCoefficientK;

    /**
     * 输出修正系数B
     */
    @ApiParam(value = "输出修正系数B")
    private String outputCorrectionCoefficientB;

    /**
     * 加油时间阈值
     */
    @ApiParam(value = "加油时间阈值")
    private String addOilTimeThreshold;

    /**
     * 加油量时间阈值
     */
    @ApiParam(value = "加油量时间阈值")
    private String addOilAmountThreshol;

    /**
     * 漏油时间阈值
     */
    @ApiParam(value = "漏油时间阈值")
    private String seepOilTimeThreshold;

    /**
     * 漏油油量时间阈值
     */
    @ApiParam(value = "漏油油量时间阈值")
    private String seepOilAmountThreshol;

    /**
     * 油箱id
     */
    @ApiParam(value = "2号油箱id")
    private String oilBoxId2;

    /**
     * 油箱形状
     */
    @ApiParam(value = "2号油箱形状,只能输入1,2,3,4;其中1:长方体,2:圆柱形,3:D形,4:椭圆形！")
    private String shape2;


    /**
     * 长度
     */
    @ApiParam(value = "2号油箱形状,只能输入1,2,3,4;其中1:长方体,2:圆柱形,3:D形,4:椭圆形！")
    private String boxLength2;

    /**
     * 宽度
     */
    @ApiParam(value = "2号油箱形状宽度")
    private String width2;

    /**
     * 高度
     */
    @ApiParam(value = "2号油箱形状高度")
    private String height2;

    /**
     * 壁厚
     */
    @ApiParam(value = "2号油箱形状壁厚")
    private String thickness2;

    /**
     * 下圆角半径
     */
    @ApiParam(value = "2号油箱形状下圆角半径")
    private String buttomRadius2;

    /**
     * 上圆角半径
     */
    @ApiParam(value = "2号油箱形状上圆角半径")
    private String topRadius2;

    /**
     * 理论容积
     */
    @ApiParam(value = "2号油箱理论容积")
    private String theoryVolume2;

    /**
     * 油箱容量
     */
    @ApiParam(value = "2号油箱容量")
    private String realVolume2;

    /**
     * 传感器型号
     */
    @NotEmpty(message = "【油箱油杆id】不能为空！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @ApiParam(value = "如果存在2号油箱油杆id不能为空！")
    private String sensorType2;

    /**
     * 传感器长度
     */
    @ApiParam(value = "2号油箱传感器长度")
    private String sensorLength2;

    /**
     * 标定数组
     */
    @ApiParam(value = "标定数组 油箱2")
    private String calibrationSets2;

    /**
     * 油量测量高度（高度1,高度2......）
     */
    @ApiParam(value = "2号油箱油量测量高度（高度1,高度2......）")
    private String oilLevelHeights2 = "";

    /**
     * 油量值（值1,值2......）
     */
    @ApiParam(value = "2号油箱油量值（值1,值2......）")
    private String oilValues2 = "";

    /**
     * 自动上传时间
     */
    @Pattern(message = "【自动上传时间】输入错误，只能输入01,02,03,04;其中01:被动,02:10s,03:20s,04:30s！",
        regexp = "^[0][1-4]{1}$", groups = {
        ValidGroupAdd.class, ValidGroupUpdate.class})
    @ApiParam(value = "2号油箱自动上传时间，只能输入01,02,03,04;其中01:被动,02:10s,03:20s,04:30s")
    private String automaticUploadTime2;

    /**
     * 输出修正系数K
     */
    @ApiParam(value = "2号油箱输出修正系数K")
    private String outputCorrectionCoefficientK2;

    /**
     * 输出修正系数B
     */
    @ApiParam(value = "2号油箱输出修正系数B")
    private String outputCorrectionCoefficientB2;

    /**
     * 加油时间阈值
     */
    @ApiParam(value = "2号油箱加油时间阈值")
    private String addOilTimeThreshold2;

    /**
     * 加油量时间阈值
     */
    @ApiParam(value = "2号油箱加油量时间阈值")
    private String addOilAmountThreshol2;

    /**
     * 漏油时间阈值
     */
    @ApiParam(value = "2号油箱漏油时间阈值")
    private String seepOilTimeThreshold2;

    /**
     * 漏油油量时间阈值
     */
    @ApiParam(value = "2号油箱漏油油量时间阈值")
    private String seepOilAmountThreshol2;

}

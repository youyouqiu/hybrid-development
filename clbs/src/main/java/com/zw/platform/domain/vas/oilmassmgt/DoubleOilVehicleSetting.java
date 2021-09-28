package com.zw.platform.domain.vas.oilmassmgt;


import java.io.Serializable;
import java.util.Date;

import javax.validation.constraints.Pattern;

import org.apache.bval.constraints.NotEmpty;

import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;

import lombok.Data;


/**
 * TODO 油箱车辆关联表 <p>Title: OilVehicleSetting.java</p> <p>Copyright: Copyright (c) 2016</p> <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @author: wangying
 * @date 2016年10月26日上午9:12:41
 * @version 1.0
 */
@Data
public class DoubleOilVehicleSetting implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 油箱与车辆关联
     */
    @NotEmpty(message = "【油箱1与车辆关联id】不能为空！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    private String id;

    /**
     * 油箱id
     */
    @NotEmpty(message = "【油箱1id】不能为空！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    private String oilBoxId;

    /**
     * 车辆id
     */
    @NotEmpty(message = "【车辆id】不能为空！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    private String vehicleId;

    /**
     * 油箱类型 油箱1 油箱2
     */
    @NotEmpty(message = "【油箱类型1】不能为空！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @Pattern(message = "【油箱类型1】填值错误！", regexp = "^[1]{1}$", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    private String oilBoxType;

    /**
     * 自动上传时间
     */
    @Pattern(message = "【自动上传时间1】输入错误，只能输入01,02,03,04;其中01:被动,02:10s,03:20s,04:30s！",
        regexp = "^[0][1-4]{1}$", groups = {
        ValidGroupAdd.class, ValidGroupUpdate.class})
    private String automaticUploadTime;

    /**
     * 输出修正系数K
     */
    private String outputCorrectionCoefficientK;

    /**
     * 输出修正系数B
     */
    private String outputCorrectionCoefficientB;

    /**
     * 液位报警阈值
     */
    private String liquidAlarmThreshold;

    /**
     * 油箱型号
     */
    private String type;

    /**
     * 油箱形状
     */
    private String shape;

    private String shapeStr = "";

    /**
     * 长度
     */
    private String boxLength;

    /**
     * 宽度
     */
    private String width;

    /**
     * 高度
     */
    private String height;

    /**
     * 壁厚
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
     * 加油时间阈值
     */
    private String addOilTimeThreshold;

    /**
     * 加油量时间阈值
     */
    private String addOilAmountThreshol;

    /**
     * 漏油时间阈值
     */
    private String seepOilTimeThreshold;

    /**
     * 漏油油量时间阈值
     */
    private String seepOilAmountThreshol;

    /**
     * 理论容积
     */
    private String theoryVolume;

    /**
     * 油箱容量
     */
    private String realVolume;

    /**
     * 标定数组
     */
    private String calibrationSets;

    /**
     * 油量测量高度（高度1,高度2......）
     */
    private String oilLevelHeights = "";

    /**
     * 油量值（值1,值2......）
     */
    private String oilValues = "";

    /**
     * 传感器型号
     */
    @NotEmpty(message = "【油箱1油杆id】不能为空！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    private String sensorType;

    /**
     * 传感器型号
     */
    private String sensorNumber;

    /**
     * 传感器长度
     */
    private String sensorLength;

    /**
     * 量程
     */
    private String measuringRange;

    /**
     * 上盲区
     */
    private String upperBlindZone;

    /**
     * 下盲区
     */
    private String lowerBlindArea;

    /**
     * 滤波系数
     */
    private String filteringFactor;

    /**
     * 波特率
     */
    private String baudRate;

    /**
     * 奇偶校验
     */
    private Integer oddEvenCheck;

    /**
     * 补偿使能
     */
    private Integer compensationCanMake;

    /**
     * 车辆类型
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
     * 车id
     */
    private String vid;

    /**
     * 油箱与车辆关联
     */
    private String id2;

    /**
     * 油箱id
     */
    private String oilBoxId2;

    /**
     * 油箱类型 油箱1 油箱2
     */
    @Pattern(message = "【油箱类型2】填值错误！", regexp = "^\\s*$|^[2]{1}$", groups = {ValidGroupAdd.class,
        ValidGroupUpdate.class})
    private String oilBoxType2;

    /**
     * 自动上传时间
     */
    @Pattern(message = "【自动上传时间】输入错误，只能输入01,02,03,04;其中01:被动,02:10s,03:20s,04:30s！",
        regexp = "^[0][1-4]{1}$", groups = {
        ValidGroupAdd.class, ValidGroupUpdate.class})
    private String automaticUploadTime2;

    /**
     * 输出修正系数K
     */
    private String outputCorrectionCoefficientK2;

    /**
     * 输出修正系数B
     */
    private String outputCorrectionCoefficientB2;

    /**
     * 液位报警阈值
     */
    private String liquidAlarmThreshold2;

    /**
     * 油箱型号
     */
    private String type2;

    /**
     * 油箱形状
     */
    private String shape2;

    private String shape2Str = "";

    /**
     * 长度
     */
    private String boxLength2;

    /**
     * 宽度
     */
    private String width2;

    /**
     * 高度
     */
    private String height2;

    /**
     * 壁厚
     */
    private String thickness2;
    
    /**
     * 下圆角半径
     */
    private String buttomRadius2;
    
    /**
     * 上圆角半径
     */
    private String topRadius2;

    /**
     * 加油时间阈值
     */
    private String addOilTimeThreshold2;

    /**
     * 加油量时间阈值
     */
    private String addOilAmountThreshol2;

    /**
     * 漏油时间阈值
     */
    private String seepOilTimeThreshold2;

    /**
     * 漏油油量时间阈值
     */
    private String seepOilAmountThreshol2;

    /**
     * 理论容积
     */
    private String theoryVolume2;

    /**
     * 油箱容量
     */
    private String realVolume2;

    /**
     * 标定数组
     */
    private String calibrationSets2;

    /**
     * 油量测量高度（高度1,高度2......）
     */
    private String oilLevelHeights2 = "";

    /**
     * 油量值（值1,值2......）
     */
    private String oilValues2 = "";

    /**
     * 传感器型号
     */
    private String sensorType2;

    /**
     * 传感器型号
     */
    private String sensorNumber2;

    /**
     * 传感器长度
     */
    private String sensorLength2;

    /**
     * 量程
     */
    private String measuringRange2;

    /**
     * 上盲区
     */
    private String upperBlindZone2;

    /**
     * 下盲区
     */
    private String lowerBlindArea2;

    /**
     * 滤波系数
     */
    private String filteringFactor2;

    /**
     * 波特率
     */
    private String baudRate2;

    /**
     * 奇偶校验
     */
    private Integer oddEvenCheck2;

    /**
     * 补偿使能
     */
    private Integer compensationCanMake2;

    private String newId2;

    /**
     * 设置主油箱
     * @param setting 油箱设置
     */
    public void assembleMainTank(OilVehicleSetting setting) {
        this.setVehicleId(setting.getVehicleId());
        this.setBrand(setting.getBrand());
        this.setId(setting.getId());
        this.setOilBoxId(setting.getOilBoxId());
        this.setOilBoxType(setting.getOilBoxType());
        this.setAutomaticUploadTime(setting.getAutomaticUploadTime());
        this.setOutputCorrectionCoefficientK(setting.getOutputCorrectionCoefficientK());
        this.setOutputCorrectionCoefficientB(setting.getOutputCorrectionCoefficientB());
        this.setType(setting.getType());
        this.setShape(setting.getShape());
        this.setBoxLength(setting.getBoxLength());
        this.setWidth(setting.getWidth());
        this.setHeight(setting.getHeight());
        this.setThickness(setting.getThickness());
        this.setButtomRadius(setting.getButtomRadius());
        this.setTopRadius(setting.getTopRadius());
        this.setAddOilTimeThreshold(setting.getAddOilTimeThreshold());
        this.setAddOilAmountThreshol(setting.getAddOilAmountThreshol());
        this.setSeepOilTimeThreshold(setting.getSeepOilTimeThreshold());
        this.setSeepOilAmountThreshol(setting.getSeepOilAmountThreshol());
        this.setTheoryVolume(setting.getTheoryVolume());
        this.setRealVolume(setting.getRealVolume());
        this.setCalibrationSets(setting.getCalibrationSets());
        this.setSensorType(setting.getSensorType());
        this.setSensorNumber(setting.getSensorNumber());
        this.setSensorLength(setting.getSensorLength());
        this.setMeasuringRange(setting.getMeasuringRange());
        this.setUpperBlindZone(setting.getUpperBlindZone());
        this.setLowerBlindArea(setting.getLowerBlindArea());
        this.setFilteringFactor(setting.getFilteringFactor());
        this.setBaudRate(setting.getBaudRate());
        this.setOddEvenCheck(setting.getOddEvenCheck());
        this.setCompensationCanMake(setting.getCompensationCanMake());
    }

    /**
     * 设置副油箱
     * @param setting 油箱设置
     */
    public void assembleAuxiliaryTank(OilVehicleSetting setting) {
        this.setVehicleId(setting.getVehicleId());
        this.setBrand(setting.getBrand());
        this.setId2(setting.getId());
        this.setOilBoxId2(setting.getOilBoxId());
        this.setOilBoxType2(setting.getOilBoxType());
        this.setAutomaticUploadTime2(setting.getAutomaticUploadTime());
        this.setOutputCorrectionCoefficientK2(setting.getOutputCorrectionCoefficientK());
        this.setOutputCorrectionCoefficientB2(setting.getOutputCorrectionCoefficientB());
        this.setType2(setting.getType());
        this.setShape2(setting.getShape());
        this.setBoxLength2(setting.getBoxLength());
        this.setWidth2(setting.getWidth());
        this.setHeight2(setting.getHeight());
        this.setThickness2(setting.getThickness());
        this.setButtomRadius2(setting.getButtomRadius());
        this.setTopRadius2(setting.getTopRadius());
        this.setAddOilTimeThreshold2(setting.getAddOilTimeThreshold());
        this.setAddOilAmountThreshol2(setting.getAddOilAmountThreshol());
        this.setSeepOilTimeThreshold2(setting.getSeepOilTimeThreshold());
        this.setSeepOilAmountThreshol2(setting.getSeepOilAmountThreshol());
        this.setTheoryVolume2(setting.getTheoryVolume());
        this.setRealVolume2(setting.getRealVolume());
        this.setCalibrationSets2(setting.getCalibrationSets());
        this.setSensorType2(setting.getSensorType());
        this.setSensorNumber2(setting.getSensorNumber());
        this.setSensorLength2(setting.getSensorLength());
        this.setMeasuringRange2(setting.getMeasuringRange());
        this.setUpperBlindZone2(setting.getUpperBlindZone());
        this.setLowerBlindArea2(setting.getLowerBlindArea());
        this.setFilteringFactor2(setting.getFilteringFactor());
        this.setBaudRate2(setting.getBaudRate());
        this.setOddEvenCheck2(setting.getOddEvenCheck());
        this.setCompensationCanMake2(setting.getCompensationCanMake());
    }
}

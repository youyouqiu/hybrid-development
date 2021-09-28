package com.zw.api2.swaggerEntity;

import io.swagger.annotations.ApiParam;
import lombok.Data;

import java.io.Serializable;


/**
 * 
 * <p>Title: 流量传感器与车的绑定表实体</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @author: wangying
 * @date 2016年9月19日上午9:13:36
 * @version 1.0
 */
@Data
public class SwaggerFuelVehicleForm implements Serializable {
	private static final long serialVersionUID = 1L;
    /**
     * 油耗传感器与车辆关联
     */
    @ApiParam(value = "油耗车辆设置id",required = true)
    private String id;
    
    /**
     * 车辆id
     */
    @ApiParam(value = "车辆id",required = true)
    private String vId;
    
    /**
     * 下发参数id
     */
    @ApiParam(value = "下发参数id",required = true)
    private String paramId;

    /**
     * 通讯参数下发id
     */
    @ApiParam(value = "通讯参数下发id",required = true)
    private String transmissionParamId;
    
    /**
     * 流量传感器id
     */
    @ApiParam(value = "流量传感器id",required = true)
    private String oilWearId;
    
    /**
     *  车辆id
     */
    @ApiParam(value = "车辆id",required = true)
    private String vehicleId;
    
    /**
     *  车辆类型
     */
    @ApiParam(value = "车辆类型",required = true)
    private String vehicleType;

    @ApiParam(value = "监控对象类型",required = true)
    private Integer monitorType;
    
    /**
     * 车牌号
     */
    @ApiParam(value = "车牌号",required = true)
    private String brand;
    
    /**
     * 组织
     */
    @ApiParam(value = "组织",required = true)
    private String groups;
    
    /**
     * 下发状态
     */
    @ApiParam(value = "下发状态",required = true)
    private Integer status;
    
    /**
     * 传感器型号
     */
    @ApiParam(value = "传感器型号",required = true)
    private String oilWearNumber;
    
    /**
     * 波特率
     */
    @ApiParam(value = "波特率",required = true)
    private String baudRate;

    @ApiParam(hidden = true)
    private String baudRateStr;

    /**
     * 奇偶校验
     */
    @ApiParam(value = "奇偶校验",required = true)
    private String parity;
    @ApiParam(hidden = true)
    private String parityStr;

    /**
     * 补偿使能
     */
    @ApiParam(value = "补偿使能",required = true)
    private Integer inertiaCompEn;
    @ApiParam(hidden = true)
    private String inertiaCompEnStr;

    /**
     * 自动上传时间
     */
    @ApiParam(value = "自动上传时间",required = true)
    private String autoUploadTime;
    @ApiParam(hidden = true)
    private String autoUploadTimeStr;

    /**
     * 输出修正系数K
     */
    @ApiParam(value = "输出修正系数K",required = true)
    private String outputCorrectionK;

    /**
     * 输出修正系数B
     */
    @ApiParam(value = "输出修正系数B",required = true)
    private String outputCorrectionB;

    
    /**
     * 外设ID
     */
    @ApiParam(value = "外设ID",required = true)
    private Integer deviceNumber;
    /**
     * 滤波系数
     */
    @ApiParam(value = "滤波系数",required = true)
    private Integer filterFactor;
    @ApiParam(hidden = true)
    private String filterFactorStr;

    /**
     * 量程
     */
    @ApiParam(value = "量程",required = true)
    private Integer ranges;

    /**
     * 燃料选择
     */
    @ApiParam(value = "燃料选择",required = true)
    private String fuelSelect;

    /**
     * 测量方案
     */
    @ApiParam(value = "测量方案",required = true)
    private Integer meteringSchemes;

}

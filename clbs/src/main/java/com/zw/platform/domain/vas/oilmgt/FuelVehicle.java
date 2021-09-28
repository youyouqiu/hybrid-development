package com.zw.platform.domain.vas.oilmgt;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

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
public class FuelVehicle implements Serializable {
	private static final long serialVersionUID = 1L;
    /**
     * 油耗传感器与车辆关联
     */
    private String id;
    
    /**
     * 车辆id
     */
    private String vId;
    
    /**
     * 下发参数id
     */
    private String paramId;
    
    private String transmissionParamId; // 通讯参数下发id
    
    /**
     * 流量传感器id
     */
    private String oilWearId;
    
    /**
     *  车辆id
     */
    private String vehicleId;
    
    /**
     *  车辆类型
     */
    private String vehicleType;

    private Integer monitorType;
    
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
    
    /**
     * 传感器型号
     */
    private String oilWearNumber;
    
    /**
     * 波特率
     */
    private String baudRate;
    
    private String baudRateStr;

    /**
     * 奇偶校验
     */
    private String parity;
    
    private String parityStr;

    /**
     * 补偿使能
     */
    private Integer inertiaCompEn;
    
    private String inertiaCompEnStr;

    /**
     * 自动上传时间
     */
    private String autoUploadTime;
    
    private String autoUploadTimeStr;

    /**
     * 输出修正系数K
     */
    private String outputCorrectionK;

    /**
     * 输出修正系数B
     */
    private String outputCorrectionB;

    private Integer flag;

    private Date createDataTime;

    private String createDataUsername;

    private Date updateDataTime;

    private String updateDataUsername;
    
    /**
     * 外设ID
     */
    private Integer deviceNumber;
    /**
     * 滤波系数
     */
    private Integer filterFactor;
    
    private String filterFactorStr;

    /**
     * 量程
     */
    private Integer ranges;

    /**
     * 燃料选择
     */
    private String fuelSelect;

    /**
     * 测量方案
     */
    private Integer meteringSchemes;

}

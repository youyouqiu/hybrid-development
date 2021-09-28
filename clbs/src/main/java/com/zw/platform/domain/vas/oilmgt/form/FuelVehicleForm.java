package com.zw.platform.domain.vas.oilmgt.form;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 
 *  流量传感器与车的绑定Form
 * 
 * @author wangying
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class FuelVehicleForm extends BaseFormBean implements Serializable {
	private static final long serialVersionUID = 1L;
    
    /**
     *  车辆id
     */
    private String vehicleId;
    
    /**
     * 车辆id
     */
    private String vId;
    
    /**
     * 下发参数id
     */
    private String paramId;
    
    /**
     * 流量传感器id
     */
    private String oilWearId;
    
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

    /**
     * 奇偶校验
     */
    private String parity;

    /**
     * 补偿使能
     */
    private Integer inertiaCompEn;

    /**
     * 自动上传时间
     */
    private Integer autoUploadTime;

    /**
     * 输出修正系数K
     */
    private String outputCorrectionK;

    /**
     * 输出修正系数B
     */
    private String outputCorrectionB;

}

/**
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved.
 * This software is the confidential and proprietary information of 
 * ZhongWei, Inc. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the 
 * license agreement you entered into with ZhongWei.
 */
package com.zw.platform.domain.vas.oilmassmgt.form;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 
 * TODO  油箱车辆设置form
 * <p>Title: FuelTankForm.java</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @author: wangying
 * @date 2016年10月27日上午9:53:25
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class OilVehicleSettingForm extends BaseFormBean implements Serializable {
	private static final long serialVersionUID = -4658040495054717642L;
	 /**
     *  油箱id
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
     *  传感器型号
     */
    private String sensorType;
    
    /**
     * 自动上传时间
     */
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
     *  加油时间阈值
     */
    private String addOilTimeThreshold;
    
    /**
     * 加油量时间阈值
     */
    private String addOilAmountThreshol;
    
    /**
     *  漏油时间阈值
     */
    private String seepOilTimeThreshold;
    
    /**
     *  漏油油量时间阈值
     */
    private String seepOilAmountThreshol;
    
    /**
     *  标定数组
     */
    private String calibrationSets;
}

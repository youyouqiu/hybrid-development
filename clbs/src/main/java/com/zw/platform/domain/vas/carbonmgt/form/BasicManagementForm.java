package com.zw.platform.domain.vas.carbonmgt.form;

import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 移动源基础信息Form
 * <p>Title: BasicManagementForm.java</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @author: Liubangquan
 * @date 2017年2月23日上午8:56:38
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class BasicManagementForm extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** 车牌号 */
    private String brand = "";
    
    /** 车辆id */
    private String vehicleId = "";
    
    /** 所属企业-名称 */
    private String groupName = "";
    
    /** 所属企业-id */
    private String groupId = "";
    
    /** 分组-名称 */
    private String assignmentName = "";
    
    /** 分组-id */
    private String assignmentId = "";
    
    /** 车辆类型-名称 */
    private String vehicleType = "";
    
    /** 车辆类型-id */
    private String vehicleTypeId = "";
    
    /** 燃油类型 */
    private String fuelType = "";
    
    /** 终端编号 */
    private String deviceNumber = "";
    
    /** 终端id */
    private String deviceId = "";
    
    /** SIM卡号 */
    private String simcardNumber = "";
    
    /** SIM卡id */
    private String simcardId = "";
    
    /** 行驶时间 */
    private String runningTime = "";
    
    /** 行驶里程 */
    private String runningMileage = "";
    
    /** 能耗量 */
    private String energyConsumption = "";
    
    /** 计算基准能耗 */
    private String calculateBaseEnergy = "";
    
    /** 核定基准能耗 */
    private String estimatesBaseEnergy = "";
    
    /** 大修时间 */
    private String overhauledTime = "";
    
    /** 大修间隔 */
    private String overhauledInterval = "";
    
    /** 节油产品安装日期 */
    private String savingProductsInstallTime = "";
    
    /** 怠速阈值 */
    private String idleThreshold = "";
    
    /** 备注 */
    private String comments = "";
    
    /** 车牌颜色 */
    private String plateColorStr = "";
    
    // 车辆基本信息
    private VehicleInfo vehicleInfo;
    
}

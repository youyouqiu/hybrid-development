/**
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved.
 * This software is the confidential and proprietary information of 
 * ZhongWei, Inc. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the 
 * license agreement you entered into with ZhongWei.
 */
package com.zw.platform.domain.vas.carbonmgt.form;

import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 时间能耗统计Form
 * <p>Title: TimeEnergyStatisticsForm.java</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @author: Liubangquan
 * @date 2016年9月20日上午9:43:11
 * @version 1.0
 * 
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TimeEnergyStatisticsForm extends BaseFormBean implements Serializable, Comparable<TimeEnergyStatisticsForm> {
	private static final long serialVersionUID = -4658040495054717642L;
	@ExcelField(title = "车牌号")
	private String brand;
	
	@ExcelField(title = "车型")
	private String vehicleType;
	
	@ExcelField(title = "燃料类型")
	private String fuelType;
	
	@ExcelField(title = "打火时间")
	private String startDate;
	
	@ExcelField(title = "熄火时间")
	private String endDate;
	
	@ExcelField(title = "时长")
	private String duration;
	
	@ExcelField(title = "空调开启时长")
	private String airConditionerDuration;
	
	@ExcelField(title = "总油耗量(L或㎥)")
	private String totalFuelConsumption;
	
	@ExcelField(title = "基准能耗")
	private String referenceEnergyConsumption;
	
	@ExcelField(title = "当期平均能耗")
	private String currentAverageEnergyConsumption;
	
	@ExcelField(title = "能源节约量-燃料")
	private String energySaving_fuel;
	
	@ExcelField(title = "能源节约量-标准煤")
	private String energySaving_standardCoal;
	
	@ExcelField(title = "减少排放量-CO2")
	private String reduceEmissions_CO2;
	
	@ExcelField(title = "减少排放量-SO2")
	private String reduceEmissions_SO2;
	
	@ExcelField(title = "减少排放量-NOX")
	private String reduceEmissions_NOX;
	
	@ExcelField(title = "减少排放量-HCX")
	private String reduceEmissions_HCX;
	
	// 按日期统计
	/**
	 * 节能率
	 */
	private String energySavingRate = "";
	
	// 按年份统计
	/**
	 * 总里程
	 */
	private String mileage = "";
	/**
	 * 车辆id
	 */
	private String vehicleId = "";
	
	/**
	 * 时间
	 */
	private String time = "";
	
	/**
	 * 起始油耗
	 */
	private String startOil = "";
	/**
	 * 结束油耗
	 */
	private String endOil = "";
	
	public int compareTo(TimeEnergyStatisticsForm m) {
        // 只能对一个字段做比较，如果做整个对象的比较就实现不了按指定字段排序了。
        return this.getTime().compareTo(m.getTime());
    }
}

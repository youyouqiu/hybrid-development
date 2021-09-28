package com.zw.platform.domain.vas.carbonmgt;

import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 时间能耗统计导出：按日期统计、按月份统计、按季度统计、按年份统计
 * <p>Title: TimeEnergy1.java</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @author: Liubangquan
 * @date 2016年9月30日下午5:58:41
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TimeEnergy1 extends BaseFormBean implements Serializable{
	private static final long serialVersionUID = 1L;
	@ExcelField(title = "日期")
	private String date;
	
	@ExcelField(title = "车牌号")
	private String brand;
	
	@ExcelField(title = "车辆类型")
	private String vehicleType;
	
	@ExcelField(title = "燃料类型")
	private String fuelType;
	
	@ExcelField(title = "时长")
	private String duration;
	
	@ExcelField(title = "空调开启时长")
	private String airConditionerDuration;
	
	@ExcelField(title = "总油耗量(L或m³)")
	private String totalFuelConsumption;
	
	@ExcelField(title = "基准能耗(L或m³)")
	private String referenceEnergyConsumption;
	
	@ExcelField(title = "当期平均能耗(L或m³)")
	private String currentAverageEnergyConsumption;
	
	@ExcelField(title = "能源节约量-燃料(L或m³)")
	private String energySaving_fuel;
	
	@ExcelField(title = "能源节约量-标准煤(t)")
	private String energySaving_standardCoal;
	
	@ExcelField(title = "节能率(%)")
	private String energySavingRate;
	
	@ExcelField(title = "减少排放量-CO2(t)")
	private String reduceEmissions_CO2;
	
	@ExcelField(title = "减少排放量-SO2(kg)")
	private String reduceEmissions_SO2;
	
	@ExcelField(title = "减少排放量-NOX(kg)")
	private String reduceEmissions_NOX;
	
	@ExcelField(title = "减少排放量-HCX(kg)")
	private String reduceEmissions_HCX;
}

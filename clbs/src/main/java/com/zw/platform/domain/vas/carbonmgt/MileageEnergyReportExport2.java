package com.zw.platform.domain.vas.carbonmgt;

import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 里程能耗报表：按日期统计、按月份统计、按季度统计、按年份统计需要导出的字段
 * <p>Title: MileageEnergyReportExport2.java</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @author: Liubangquan
 * @date 2017年3月27日上午11:40:36
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class MileageEnergyReportExport2 extends BaseFormBean implements Serializable{
	private static final long serialVersionUID = 1L;
	@ExcelField(title = "日期")
	private String date;
	
	@ExcelField(title = "车牌号")
	private String brand;
	
	@ExcelField(title = "车辆类型")
	private String vehicleType;
	
	@ExcelField(title = "燃料类型")
	private String fuelType;
	
	@ExcelField(title = "行驶时长")
	private String duration;
	
	@ExcelField(title = "行驶里程(km)")
	private String mileage;
	
	@ExcelField(title = "平均速度")
	private String averageSpeed;
	
	@ExcelField(title = "空调开启时长")
	private String airConditionerDuration;
	
	@ExcelField(title = "转动时长")
	private String rollingDuration;
	
	@ExcelField(title = "基准能耗(L或m³/百公里)")
	private String baseBenchmark;
	
	@ExcelField(title = "当期平均能耗(L或m³/百公里)")
	private String currentAverageEnergyConsumption;
	
	@ExcelField(title = "基准能耗量(L或m³)")
	private String baseBenchmarkAmount = "";
	
	@ExcelField(title = "当期能耗量(L或m³)")
	private String currentEnergyConsumptionAmount = "";
	
	@ExcelField(title = "能源节约量-燃料(L或m³)")
	private String energySaving_fuel;
	
	@ExcelField(title = "能源节约量-标准煤(t)")
	private String energySaving_standardCoal;
	
	@ExcelField(title = "基准排放量-CO2(t)")
	private String baseEmissions_CO2 = "";
	
	@ExcelField(title = "基准排放量-SO2(kg)")
	private String baseEmissions_SO2 = "";
	
	@ExcelField(title = "基准排放量-NOX(kg)")
	private String baseEmissions_NOX = "";
	
	@ExcelField(title = "基准排放量-HCX(kg)")
	private String baseEmissions_HCX = "";
	
	@ExcelField(title = "当期排放量-CO2(t)")
	private String curEmissions_CO2 = "";
	
	@ExcelField(title = "当期排放量-SO2(kg)")
	private String curEmissions_SO2 = "";
	
	@ExcelField(title = "当期排放量-NOX(kg)")
	private String curEmissions_NOX = "";
	
	@ExcelField(title = "当期排放量-HCX(kg)")
	private String curEmissions_HCX = "";
	
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

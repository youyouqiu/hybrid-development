package com.zw.platform.domain.vas.carbonmgt;

import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class MileageList1 extends BaseFormBean implements Serializable{
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
	
	@ExcelField(title = "起点坐标")
	private String startCoordinate;
	
	@ExcelField(title = "终点坐标")
	private String endCoordinate;
	
	@ExcelField(title = "总里程(km)")
	private String mileage;
	
	@ExcelField(title = "总油耗量(L或m³)")
	private String totalFuelConsumption;
	
	@ExcelField(title = "平均速度")
	private String averageSpeed;
	
	@ExcelField(title = "基准能耗(L或m³/百公里)")
	private String referenceEnergyConsumption;
	
	@ExcelField(title = "当期平均能耗(L或m³/百公里)")
	private String currentAverageEnergyConsumption;
	
	@ExcelField(title = "能源节约量-燃料(L或m³)")
	private String energySaving_fuel;
	
	@ExcelField(title = "能源节约量-标准煤(t)")
	private String energySaving_standardCoal;
	
	@ExcelField(title = "节能率(%)")
	private String energySavingRate;
	
	@ExcelField(title = "减少排放量-CO2(t)")
	private String reduceEmissions_CO2;
	
	@ExcelField(title = "减少排放量-SO2(t)")
	private String reduceEmissions_SO2;
	
	@ExcelField(title = "减少排放量-NOX(kg)")
	private String reduceEmissions_NOX;
	
	@ExcelField(title = "减少排放量-HCX(kg)")
	private String reduceEmissions_HCX;
}

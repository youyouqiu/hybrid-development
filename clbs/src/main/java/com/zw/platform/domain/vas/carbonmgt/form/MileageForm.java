package com.zw.platform.domain.vas.carbonmgt.form;

import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class MileageForm extends BaseFormBean implements Serializable{
	private static final long serialVersionUID = 1L;
	@ExcelField(title = "日期")
	private String date;
	
	@ExcelField(title = "车牌号")
	private String brand;
	
	@ExcelField(title = "车型")
	private String vehicleType;
	
	@ExcelField(title = "燃料类型")
	private String fuelType;
	
	@ExcelField(title = "开始时间")
	private String startDate;
	
	@ExcelField(title = "结束时间")
	private String endDate;
	
	@ExcelField(title = "时长")
	private String duration;
	
	@ExcelField(title = "空调开启时长")
	private String airConditionerDuration;
	
	@ExcelField(title = "起点坐标")
	private String startCoordinate;
	
	@ExcelField(title = "终点坐标")
	private String endCoordinate;
	
	@ExcelField(title = "里程")
	private String mileage;
	
	@ExcelField(title = "总油耗量")
	private String totalFuelConsumption;
	
	@ExcelField(title = "平均速度")
	private String averageSpeed;
	
	@ExcelField(title = "节能率")
	private String energySavingRate;
	
	@ExcelField(title = "基准能耗")
	private String referenceEnergyConsumption;
	
	@ExcelField(title = "当期平均能耗")
	private String currentAverageEnergyConsumption;
	
	@ExcelField(title = "燃料")
	private String energySaving_fuel;
	
	@ExcelField(title = "标准煤")
	private String energySaving_standardCoal;
	
	@ExcelField(title = "减少排放量-CO2")
	private String reduceEmissions_CO2;
	
	@ExcelField(title = "减少排放量-SO2")
	private String reduceEmissions_SO2;
	
	@ExcelField(title = "减少排放量-NOX")
	private String reduceEmissions_NOX;
	
	@ExcelField(title = "减少排放量-HCX")
	private String reduceEmissions_HCX;
    /**
	 * 时间
	 */
	private String time = "";
	
	private String startOil;
	
	private String endOil;
	
	private String vehicleId;
	
    private String queryWay;
	
	private String groupId;
	
	private String startMileage;
	
	private String endMileage;
    
}

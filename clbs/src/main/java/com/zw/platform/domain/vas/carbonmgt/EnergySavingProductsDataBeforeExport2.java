package com.zw.platform.domain.vas.carbonmgt;

import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class EnergySavingProductsDataBeforeExport2 extends BaseFormBean implements Serializable{
	private static final long serialVersionUID = 1L;
	
	@ExcelField(title = "日期")
	private String time = "";
	
	@ExcelField(title = "车牌号")
	private String brand = "";
	
	@ExcelField(title = "所属组织")
	private String groupName = "";
	
	@ExcelField(title = "车辆类型")
	private String vehicleType = "";
	
	@ExcelField(title = "燃料类型")
	private String fuelType = "";
	
	@ExcelField(title = "行驶时长")
	private String duration = "";
	
	@ExcelField(title = "行驶里程（公里）")
	private String mileage = "";

	@ExcelField(title = "平均速度")
	private String averageSpeed = "";
	
	@ExcelField(title = "空调开启时长")
	private String airConditionerDuration = "";
	
	@ExcelField(title = "转动时长")
	private String rollingDuration = "";

	@ExcelField(title = "能耗量（L或m3）")
	private String totalFuelConsumption = "";
	
	@ExcelField(title = "能源价格（元）")
	private String energyPrice = "";
	
	@ExcelField(title = "能耗费用")
	private String energyTotalFee = "";
	
	@ExcelField(title = "百公里能耗")
	private String energy_100 = "";
	
	@ExcelField(title = "CO2排放量（t）")
	private String emissions_CO2 = "";
	
	@ExcelField(title = "SO2排放量（kg）")
	private String emissions_SO2 = "";
	
	@ExcelField(title = "NOX排放量（kg）")
	private String emissions_NOX = "";
	
	@ExcelField(title = "HCX排放量（kg）")
	private String emissions_HCX = "";
	
}

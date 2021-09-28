package com.zw.platform.domain.vas.carbonmgt.form;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class MobileSourceEnergyReportForm extends BaseFormBean implements Serializable{
	private static final long serialVersionUID = 1L;
	// 日期
	private String date = "";
	
	// 车牌号
	private String brand = "";
	
	// 所属组织
	private String groupName = "";
	
	// 车辆类型
	private String vehicleType = "";
	
	// 燃料类型
	private String fuelType = "";
	
	// 启动时间（打火时间）
	private String startDate = "";
	
	// 熄火时间
	private String endDate = "";
	
	// 行驶时长
	private String duration = "";
	
	// 行驶里程（公里）
	private String mileage = "";

	// 平均速度
	private String averageSpeed = "";
	
	// 空调开启时长
	private String airConditionerDuration = "";
	
	// 转动时长
	private String rollingDuration = "";

	// 能耗量（L或m3）
	private String totalFuelConsumption = "";
	
	// 能源价格（元）
	private String energyPrice = "";
	
	// 能耗费用
	private String energyTotalFee = "";
	
	// 百公里能耗
	private String energy_100 = "";
	
	// CO2排放量
	private String emissions_CO2 = "";
	
	// SO2排放量
	private String emissions_SO2 = "";
	
	// NOX排放量
	private String emissions_NOX = "";
	
	// HCX排放量
	private String emissions_HCX = "";
	
    // 统计列表中的日期列
	private String time = "";
	
	// 开始油耗
	private String startOil = "";
	
	// 结束油耗
	private String endOil = "";
	
	// 车辆id
	private String vehicleId = "";
	
	// 查询方式 
    private String queryWay = "";
	
    // 所属组织id
	private String groupId = "";
	
	// 开始里程数
	private String startMileage = "";
	
	// 结束里程数
	private String endMileage = "";
	
	/**
	 *  里程能耗报表需要的字段
	 */
	// 起点坐标
	private String startCoordinate = "";
	
	// 终点坐标
	private String endCoordinate = "";
	
	// 基准能耗
	private String baseBenchmark = "";
	
	// 基准能耗量
	private String baseBenchmarkAmount = "";
	
	// 当期平均能耗
	private String currentAverageEnergyConsumption = "";
	
	// 当期能耗量
	private String currentEnergyConsumptionAmount = "";
	
	// 能源节约量-燃料
	private String energySaving_fuel = "";
	
	// 能源节约量-标准煤
	private String energySaving_standardCoal = "";
	
	// 节能率
	private String energySavingRate = "";
	
	// 减少排放量-CO2
	private String reduceEmissions_CO2 = "";
	
	// 减少排放量-SO2
	private String reduceEmissions_SO2 = "";
	
	// 减少排放量-NOX
	private String reduceEmissions_NOX = "";
	
	// 减少排放量-HCX
	private String reduceEmissions_HCX = "";
	
	// 基准排放量-CO2
	private String baseEmissions_CO2 = "";
	
	// 基准排放量-SO2
	private String baseEmissions_SO2 = "";
	
	// 基准排放量-NOX
	private String baseEmissions_NOX = "";
	
	// 基准排放量-HCX
	private String baseEmissions_HCX = "";
	
	// 当期排放量-CO2
	private String curEmissions_CO2 = "";
	
	// 当期排放量-SO2
	private String curEmissions_SO2 = "";
	
	// 当期排放量-NOX
	private String curEmissions_NOX = "";
	
	// 当期排放量-HCX
	private String curEmissions_HCX = "";
	
	/**
	 * 综合能耗报表需要的字段
	 */
	// 节约能耗
	private String savingEnergy = "";
	
	// 实际排放
	private String actualEmissions = "";
	
	// 基准排放
	private String baseEmissions = "";
	
	// 减排量
	private String reduceEmissionsAmount = "";
	
	// 减排率
	private String reduceEmissionsRate = "";
	
	// 当期节能
	private String energySaving_curPeriod = "";
	
	// 当期节能率
	private String energySavingRate_curPeriod = "";
	
	// 当期节省费用
	private String energySavingFee_curPeriod = "";
	
	// 当期总的减排量CO2
	private String curTotalReduceEmissions_CO2 = "";
    
}

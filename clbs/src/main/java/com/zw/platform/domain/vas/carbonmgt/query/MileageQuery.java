package com.zw.platform.domain.vas.carbonmgt.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class MileageQuery extends BaseQueryBean implements Serializable{
private static final long serialVersionUID = 1L;
	
	/**
	 * 车牌号
	 */
	private String brand;
	/**
	 * 车辆类型
	 */
	private String vehicleType;
	/**
	 * 燃料类型
	 */
	private String fuelType;
	/**
	 * 开始时间
	 */
	private String startDate;
	/**
	 * 结束时间
	 */
	private String endDate;
	/**
	 * 时长
	 */
	private String duration;
	/**
	 * 空调开户时长
	 */
	private String airConditionerDuration;
	/**
	 * 起点坐标
	 */
	private String startCoordinate;
	/**
	 * 终点坐标
	 */
	private String endCoordinate;
	/**
	 * 里程
	 */
	private String mileage;
	/**
	 * 总油耗量
	 */
	private String totalFuelConsumption;
	/**
	 * 平均速度
	 */
	private String averageSpeed;
	/**
	 * 基准能耗
	 */
	private String referenceEnergyConsumption;
	/**
	 * 当期平均能耗
	 */
	private String currentAverageEnergyConsumption;
	/**
	 * 能源节约量-燃料
	 */
	private String energySaving_fuel;
	/**
	 * 能源节约量-标准煤
	 */
	private String energySaving_standardCoal;
	/**
	 * 减少排放量-CO2
	 */
	private String reduceEmissions_CO2;
	/**
	 * 减少排放量-SO2
	 */
	private String reduceEmissions_SO2;
	/**
	 * 减少排放量-NOX
	 */
	private String reduceEmissions_NOX;
	/**
	 * 减少排放量-HCK
	 */
	private String reduceEmissions_HCX;
	// 按日期统计
    /**
     * 节能率
	 */
    private String energySavingRate;
    /**
     * 日期
	 */
    private String date;
    /**
     * 组织
	 */
    private String group;
   
    private String queryWay;
	
	private String groupId;
	
	private String groupName;
	
	private String year;//年
	
	private String month;//月
	
	private String quarter;//季度
}

package com.zw.platform.domain.energy;

import java.io.Serializable;

import com.zw.platform.util.excel.annotation.ExcelField;

import lombok.Data;

/**
 * 能耗对比信息
 * @author hujun
 *
 */
@Data
public class EnergyContrastInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 日期
     */
    @ExcelField(title = "日期")
    private String date;
    /**
     * 车辆id
     */
    private String vehicleId;
    /**
     * 车牌号
     */
    @ExcelField(title = "监控对象")
    private String brand;
    /**
     * 所属组织
     */
    @ExcelField(title = "所属企业")
    private String groupName;
    /**
     * 燃油类型
     */
    private String fuelType;
    /**
     * 行驶时长（行驶过程）
     */
    private Double travelTime = 0.0;
    /**
     * 格式化行驶时长（行驶过程）
     */
    @ExcelField(title = "行驶时长")
    private String formatTravelTime;
    /**
     * 行驶里程（行驶过程）
     */
    @ExcelField(title = "行驶里程（Km）")
    private Double travelMile = 0.0;
    /**
     * 行驶能耗量（行驶过程）
     */
    @ExcelField(title = "行驶能耗量（L）")
    private Double travelFuel = 0.0;
    /**
     * 行驶能耗基准（行驶过程）
     */
    @ExcelField(title = "行驶能耗基准（L/Km）")
    private Double travelBaseFuel = 0.0;   
    /**
     * 空调时长（行驶过程）
     */
    private Double travelAirOpenTime = 0.0;
    /**
     * 格式化空调时长（行驶过程）
     */
    @ExcelField(title = "空调时长")
    private String formatTravelAirOpenTime;
    /**
     * 空调能耗基准（行驶过程）
     */
    @ExcelField(title = "空调能耗基准（L/h）")
    private Double travelAirBaseFuel;    
    /**
     * 基准能耗量（行驶过程）
     */
    @ExcelField(title = "基准能耗量（L）")
    private Double travelEnergyFuel = 0.0;        
    /**
     * 怠速时长（怠速过程）
     */
    private Double idleTime = 0.0; 
    /**
     * 格式化怠速时长（怠速过程）
     */
    @ExcelField(title = "怠速时长")
    private String formatIdleTime;
    /**
     * 怠速能耗量（怠速过程）
     */
    @ExcelField(title = "怠速能耗量（L）")
    private Double idleFuel = 0.0;     
    /**
     * 能耗基准（怠速过程）
     */
    @ExcelField(title = "怠速能耗基准（L/h）")
    private Double idleBaseFuel = 0.0;     
    /**
     * 空调时长（怠速过程）
     */
    private Double idleAirOpenTime = 0.0;
    /**
     * 格式化空调时长（怠速过程）
     */
    @ExcelField(title = "空调时长")
    private String formatIdleAirOpenTime;
    /**
     * 空调能耗基准（怠速过程）
     */
    @ExcelField(title = "空调能耗基准")
    private Double idleAirBaseFuel;  
    /**
     * 基准能耗量（怠速过程）
     */
    @ExcelField(title = "基准能耗量（L）")
    private Double idleEnergyFuel = 0.0;    
    /**
     * 总能耗量
     */
    @ExcelField(title = "总能耗量（L）")
    private Double totalFuel = 0.0;        
    /**
     * 总基准量
     */
    @ExcelField(title = "总基准量（L）")
    private Double totalBase = 0.0;        
    /**
     * 燃料
     */
    @ExcelField(title = "燃料（L）")
    private Double fuel = 0.0;         
    /**
     * 标准煤
     */
    @ExcelField(title = "标准煤（kg）")
    private Double standardCoal = 0.0;     
    /**
     * 减少的CO2排放量
     */
    @ExcelField(title = "CO2（kg）")
    private Double co2Amount = 0.0;        
    /**
     * 减少的SO2排放量
     */
    @ExcelField(title = "SO2（kg）")
    private Double so2Amount;        
    /**
     * 减少的NOX排放量
     */
    @ExcelField(title = "NOX（kg）")
    private Double noxAmount;        
    /**
     * 减少的HCX排放量
     */
    @ExcelField(title = "HCX（kg）")
    private Double hcxAmount;        

}

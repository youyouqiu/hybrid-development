package com.cb.platform.vo;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zhangsq
 * @date 2018/5/14 15:53
 */
@Data
public class VehicleMileDetailVO {
    @ExcelField(title = "车牌号")
    private String vehicleBrandNumber;
    @ExcelField(title = "车牌颜色")
    private String vehicleBrandColor;
    @ExcelField(title = "车辆类型")
    private String vehicleType;
    @ExcelField(title = "道路运输企业")
    private String groupName;
    @ExcelField(title = "行驶时间段")
    private String timeSection;
    @ExcelField(title = "行驶里程数")
    private String gpsMile;

    public VehicleMileDetailVO(String vehicleBrandNumber, String vehicleBrandColor, String vehicleType, String groupName, String timeSection, Double gpsMile) {
        this.vehicleBrandNumber = vehicleBrandNumber;
        this.vehicleBrandColor = vehicleBrandColor;
        this.vehicleType = vehicleType;
        this.groupName = groupName;
        this.timeSection = timeSection;
        this.gpsMile = gpsMile == null ? null : new BigDecimal(gpsMile).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
    }

    public VehicleMileDetailVO() {
    }
}

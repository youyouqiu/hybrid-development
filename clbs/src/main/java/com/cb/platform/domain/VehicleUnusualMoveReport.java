package com.cb.platform.domain;

import com.zw.platform.domain.basicinfo.enums.PlateColor;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;


/**
 * 车辆异动行驶报表
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class VehicleUnusualMoveReport extends VehicleUnusualReportBase {

    @ExcelField(title = "车牌号")
    private String brand;

    @ExcelField(title = "车牌颜色")
    private String color;

    @ExcelField(title = "车辆类型")
    private String vehicleType = "其他车辆";

    @ExcelField(title = "道路运输企业")
    public String groupName;

    @ExcelField(title = "客运车禁行")
    public int passengerVehicleForbid;

    @ExcelField(title = "山区公路禁行")
    public int mountainRoadForbid;

    @ExcelField(title = "合计")
    public int total;

    private String vehicleTypeId;

    public void setColor(String color) {
        if (!StringUtils.isEmpty(color) && StringUtils.isNumeric(color)) {
            this.color = PlateColor.getNameOrBlankByCode(color);
        } else {
            this.color = color;
        }

    }

    /**
     * 计算总数量
     * @return
     */
    public int getTotal() {
        return mountainRoadForbid + passengerVehicleForbid;
    }
}

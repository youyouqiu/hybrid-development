package com.cb.platform.domain;


import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * 车辆异常行驶道路运输企业统计报表
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class VehicleUnusualMoveCompanyReport extends VehicleUnusualReportBase {

    @ExcelField(title = "道路运输企业")
    public String groupName;

    @ExcelField(title = "客运车禁行")
    public int passengerVehicleForbid;

    @ExcelField(title = "山区公路禁行")
    public int mountainRoadForbid;

    @ExcelField(title = "合计")
    public int total;

    /**
     * 计算总数量
     * @return
     */
    public int getTotal() {
        return mountainRoadForbid + passengerVehicleForbid;
    }
}

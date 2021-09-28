package com.zw.platform.domain.reportManagement;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;

import java.util.Date;

/**
 * 车辆运营状态报表实体
 */
@Data
public class VehicleOperationStatusReport {
    /**
     * 车辆id
     */
    private String id;

    /**
     * 车牌号
     */
    @ExcelField(title = "车牌号")
    private String brand;

    /**
     * 车牌颜色
     */
    private Integer plateColor;

    @ExcelField(title = "车牌颜色")
    private String plateColorStr;

    /**
     * 运营状态
     */
    private Integer operatingState;

    @ExcelField(title = "运营状态")
    private String operatingStateStr;

    /**
     * 道路运输证有效期起
     */
    private Date roadTransportValidityStart;

    @ExcelField(title = "发证日期")
    private String roadTransportValidityStartStr;

    /**
     * 道路运输证有效期至
     */
    private Date roadTransportValidity;

    @ExcelField(title = "有效期至")
    private String roadTransportValidityStr;
}

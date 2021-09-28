package com.zw.platform.domain.reportManagement;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;

import java.io.Serializable;

/**
 * 出区划累计时长报表实体
 */
@Data
public class OutAreaTotalTimeReport implements Serializable {
    private static final long serialVersionUID = -3964856839284939487L;

    /**
     * 出区划累计时长统计列表key
     */
    public static final String OUT_AREA_DURATION_STATISTICS_LIST_KEY = "out_area_duration_statistics_list_";

    @ExcelField(title = "车牌号")
    private String plateNumber;

    @ExcelField(title = "所属企业")
    private String groupName;

    @ExcelField(title = "车牌颜色")
    private String plateColor;

    @ExcelField(title = "车辆类型")
    private String vehicleType;

    @ExcelField(title = "出区划时间")
    private String outTime;

    @ExcelField(title = "出区划时长(天)")
    private Long outTotalTime;

    @ExcelField(title = "位置")
    private String address;
}

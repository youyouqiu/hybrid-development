package com.zw.platform.domain.reportManagement;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;


/**
 * 油量里程报表数据实体
 */
@Data
public class OilMassMile {

    private String monitorStrId;

    private byte[] monitorId;

    @ExcelField(title = "监控对象")
    private String monitorName; // 车牌号

    @ExcelField(title = "所属企业")
    private String groupName; // 企业名称

    @ExcelField(title = "开始日期")
    private String startDate; // 开始日期

    @ExcelField(title = "结束日期")
    private String endDate; // 结束日期

    @ExcelField(title = "天数")
    private int days; // 天数

    @ExcelField(title = "用油量")
    private Double oilTank; // 用油量

    @ExcelField(title = "加油量")
    private Double fuelAmount; // 加油量

    @ExcelField(title = "漏油量")
    private Double fuelSpill; // 漏油量

    /**
     * V4.1.2字段修改gps里程为终端里程
     */
    @ExcelField(title = "终端里程")
    private Double gpsMile;

}

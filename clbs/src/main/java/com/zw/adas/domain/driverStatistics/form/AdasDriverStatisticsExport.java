package com.zw.adas.domain.driverStatistics.form;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;

/***
 @Author zhengjc
 @Date 2019/7/10 17:38
 @Description 导出的实体
 @version 1.0
 **/
@Data
public class AdasDriverStatisticsExport {
    /**
     * 司机名称
     */
    @ExcelField(title = "司机名称")
    private String driverName;

    /**
     * 监控对象
     */
    @ExcelField(title = "监控对象")
    private String monitorName;

    /**
     * 所属企业
     */
    @ExcelField(title = "所属企业")
    private String groupName;
    /**
     * 从业资格证号
     */
    @ExcelField(title = "从业资格证号")
    private String cardNumber;
    /**
     * 插卡时间
     */
    @ExcelField(title = "插卡时间")
    private String insertCardTime;
    /**
     * 拔卡时间
     */
    @ExcelField(title = "拔卡时间")
    private String removeCardTime;
    /**
     * 休息次数
     */
    @ExcelField(title = "休息次数")
    private Integer restTimes;
    /**
     * 行驶时长
     */
    @ExcelField(title = "行驶时长")
    private String travelTime;

    /**
     * 行驶里程
     */
    @ExcelField(title = "行驶里程")
    private String travelMileStr;
}

package com.zw.platform.domain.oil;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import org.apache.poi.ss.usermodel.HorizontalAlignment;

import java.io.Serializable;

/**
 * 定时定区域导出Form类
 * @Author: Tianzhangxu
 * @Date: 2019/8/27 16:00
 */
@Data
public class TimeZoneExportForm implements Serializable {

    private static final long serialVersionUID = -3755929375057088346L;

    /**
     * 序号
     */
    @ExcelField(title = "序号")
    private String id;

    /**
     * 监控对象
     */
    @ExcelField(title = "监控对象")
    private String monitorName;

    /**
     * 时间段
     */
    @ExcelField(title = "时段")
    private String timeRange;

    /**
     * 区域名
     */
    @ExcelField(title = "区域")
    private String areaName;

    /**
     * 进区域时间
     */
    @ExcelField(title = "进区域时间")
    private String intoAreaTime;

    /**
     * 进区域次数
     */
    @ExcelField(title = "进区域总次数", mergedRegion = true, align = HorizontalAlignment.CENTER)
    private String intoAreaNumber;

    /**
     * 出区域时间
     */
    @ExcelField(title = "出区域时间")
    private String outAreaTime;

    /**
     * 出区域次数
     */
    @ExcelField(title = "出区域总次数", mergedRegion = true, align = HorizontalAlignment.CENTER)
    private String outAreaNumber;
}

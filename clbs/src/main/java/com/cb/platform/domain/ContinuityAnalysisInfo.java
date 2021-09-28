package com.cb.platform.domain;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;

import java.io.Serializable;

/**
 * @author penghj
 * @version 1.0
 * @date 2019/12/18 15:08
 */
@Data
public class ContinuityAnalysisInfo implements Serializable {
    private static final long serialVersionUID = 6996053861659236223L;

    private String monitorId;

    @ExcelField(title = "监控对象")
    private String monitorName;

    @ExcelField(title = "所属企业")
    private String groupName;

    @ExcelField(title = "分组")
    private String assignmentName;

    @ExcelField(title = "标识颜色")
    private String signColor;

    @ExcelField(title = "对象类型")
    private String objectType;

    /**
     * yyyyMMddHHmmss
     */
    private String startTime;
    @ExcelField(title = "中断开始时间")
    private String breakStartTime;

    @ExcelField(title = "开始时速度")
    private String startSpeed;

    @ExcelField(title = "开始时里程")
    private String startMileage;

    /**
     * yyyyMMddHHmmss
     */
    private String endTime;
    @ExcelField(title = "中断结束时间")
    private String breakEndTime;

    @ExcelField(title = "结束时速度")
    private String endSpeed;

    @ExcelField(title = "结束时里程")
    private String endMileage;

    @ExcelField(title = "中断行驶距离")
    private String distance;

    @ExcelField(title = "中断时长")
    private String durationStr;
    private Long duration;

    @ExcelField(title = "中断纬度")
    private String longitude;

    @ExcelField(title = "中断纬度")
    private String latitude;

    @ExcelField(title = "中断位置")
    private String address;

    private String nextLongitude;

    private String nextLatitude;
}

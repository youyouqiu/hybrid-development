package com.zw.platform.domain.reportManagement;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;


/**
 * 停车信息实体类
 *
 * @author hujun
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ParkingInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @ExcelField(title = "监控对象")
    private String plateNumber;// 车牌号

    @ExcelField(title = "分组")
    private String assignmentName;// 分组

    @ExcelField(title = "从业人员")
    private String professionalsName;// 从业人员

    @ExcelField(title = "电话")
    private String phone;// 停车位置

    @ExcelField(title = "停驶时长")
    private String stopTime;// 停车时长

    @ExcelField(title = "停驶次数")
    private int stopNumber = 0;// 停车次数

    @ExcelField(title = "最后停止位置")
    private String stopLocation;// 停车位置

    private long startTime;// 开始时间

    private long endTime;// 结束时间

    private long stopTimeMs = 0L;// 停车时长（秒）

    private String monitorId;// 监控对象id

    private byte[] monitorIdByte;

    private Long day;

    private String dayFormat;

    /**
     * 怠速里程
     */
    private Double stopMile = 0.0;

    /**
     * 日期
     */
    private String dyaDate;
}
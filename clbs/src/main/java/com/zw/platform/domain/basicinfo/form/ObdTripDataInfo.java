package com.zw.platform.domain.basicinfo.form;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

/**
 * @author penghj
 * @version 1.0
 * @date 2019/2/28 15:50
 */
@Data
public class ObdTripDataInfo implements Serializable {
    private static final long serialVersionUID = 1794177594252860472L;

    /**
     * ID
     */
    private String id = UUID.randomUUID().toString();

    /**
     * 监控对象
     */

    @ExcelField(title = "监控对象")
    private String plateNumber;
    /**
     * 所属企业
     */
    @ExcelField(title = "所属企业")
    private String groupName;
    /**
     * 分组
     */
    @ExcelField(title = "分组")
    private String assignmentName;
    /**
     * 开始时间
     */
    @ExcelField(title = "开始时间")
    private String tripStartTime;
    private Long tripStartTimeL = 0L;
    /**
     * 结束时间
     */
    @ExcelField(title = "结束时间")
    private String tripEndTime;
    /**
     * 行程时长
     */
    @ExcelField(title = "行程时长")
    private String tripDuration;
    /**
     * 行驶时长
     */
    @ExcelField(title = "行驶时长")
    private String drivingDuration;
    /**
     * 行驶里程
     */
    @ExcelField(title = "行驶里程(Km)")
    private String drivingMileage;
    /**
     * 怠速次数
     */
    @ExcelField(title = "怠速次数")
    private Integer idlingNumber;
    /**
     * 怠速时长
     */
    @ExcelField(title = "怠速时长")
    private String idlingDuration;
    /**
     * 总油耗
     */
    @ExcelField(title = "总油耗(L)")
    private String totalOilConsumption;
    /**
     * 行驶油耗
     */
    @ExcelField(title = "行驶油耗(L)")
    private String drivingOilConsumption;
    /**
     * 行驶百公里油耗
     */
    @ExcelField(title = "行驶百公里油耗(L/100km)")
    private String driving100KmOilConsumption;
    /**
     * 怠速油耗
     */
    @ExcelField(title = "怠速油耗(L)")
    private String idlingOilConsumption;
    /**
     * 怠速小时油耗
     */
    @ExcelField(title = "怠速小时油耗(L/h)")
    private String idlingHourOilConsumption;
    /**
     * 急加速次数
     */
    @ExcelField(title = "急加速次数")
    private Integer rapidAccelerationNumber;
    /**
     * 急减速次数
     */
    @ExcelField(title = "急减速次数")
    private Integer rapidDecelerationNumber;
    /**
     * 急转弯次数
     */
    @ExcelField(title = "急转弯次数")
    private Integer sharpTurnNumber;
    /**
     * 刹车次数
     */
    @ExcelField(title = "刹车次数")
    private Integer brakeNumber;
    /**
     * 离合次数
     */
    @ExcelField(title = "离合次数")
    private Integer clutchNumber;
}

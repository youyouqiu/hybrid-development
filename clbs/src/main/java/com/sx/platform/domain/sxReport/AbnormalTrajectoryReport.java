package com.sx.platform.domain.sxReport;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author zhangsq
 * @date 2018/3/14 9:28
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AbnormalTrajectoryReport implements Serializable {

    private static final long serialVersionUID = 1L;

    @ExcelField(title = "监控对象")
    private String plateNumber;        //监控对象

    @ExcelField(title = "所属企业")
    private String groupName;       //所属企业

    @ExcelField(title = "所属分组")
    private String assignmentName;    //所属分组

    @ExcelField(title = "车牌颜色")
    private String plateColor;        //车牌颜色

    @ExcelField(title = "车辆类型")
    private String vehType;         //车辆类型

    @ExcelField(title = "轨迹缺失开始时间")
    private String lostStartTime;

    @ExcelField(title = "轨迹缺失结束时间")
    private String lostEndTime;

    @ExcelField(title = "轨迹缺失次数")
    private Long lostCount;

    @ExcelField(title = "轨迹完整率(%)")
    private String completeRate;

    @ExcelField(title = "轨迹缺失开始位置")
    private String lostStartLocation;

    @ExcelField(title = "轨迹缺失结束位置")
    private String lostEndLocation;

}

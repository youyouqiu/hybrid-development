package com.sx.platform.domain.sxReport;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author zhangsq
 * @date 2018/3/12 14:59
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class LocationQualifiedRateReport implements Serializable {

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

    @ExcelField(title = "不合格定位数据")
    private Long unqualifiedCount = Long.valueOf(0);    //不合格定位数据

    @ExcelField(title = "总定位数据")
    private Long totalCount = Long.valueOf(0);        //总定位数据

    @ExcelField(title = "定位数据合格率(%)")
    private String qualifiedRate;         //定位数据合格率
}

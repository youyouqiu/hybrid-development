package com.sx.platform.domain.sxReport;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author zhangsq
 * @date 2018/3/14 9:28
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ShiftDataReport implements Serializable {

    private static final long serialVersionUID = 1L;

    @ExcelField(title = "监控对象")
    private String plateNumber;        //监控对象

    @ExcelField(title = "所属企业")
    private String groupName;       //所属企业

    @ExcelField(title = "分组")
    private String assignmentName;    //所属分组

    @ExcelField(title = "车牌颜色")
    private String plateColor;        //车牌颜色

    @ExcelField(title = "车辆类型")
    private String vehType;         //车辆类型

    @ExcelField(title = "漂移点数")
    private Integer shiftCount = Integer.valueOf(0);

    @ExcelField(title = "从业人员")
    private String professionalNames;

    @ExcelField(title = "电话")
    private String phone;


}

package com.cb.platform.domain;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

@Data
public class TransportTimesExportEntity implements Serializable {
    private static final long serialVersionUID = 6129770151741136419L;


    @ExcelField(title = "车牌号")
    private String brand;

    @ExcelField(title = "品名")
    private String name;

    @ExcelField(title = "危险品类别")
    private String dangerType;

    private Long count;

    @ExcelField(title = "数量")
    private String countStr;

    @ExcelField(title = "单位")
    private String unit;

    @ExcelField(title = "运输类型")
    private String transportType;

    @ExcelField(title = "运输日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date transportDate;

    @ExcelField(title = "起始地点")
    private String startSite;

    @ExcelField(title = "途径地点")
    private String viaSite;

    @ExcelField(title = "目的地点")
    private String aimSite;

    private String professinoalId;

    @ExcelField(title = "押运员")
    private String professinoal;

    @ExcelField(title = "从业资格证号")
    private String professinoalNumber;
    //电话
    @ExcelField(title = "电话")
    private String phone;
    //备注
    @ExcelField(title = "备注")
    private String remark;

}

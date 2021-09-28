package com.cb.platform.domain;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

@Data
public class TransportTimesImportEntity  implements Serializable {

    private static final long serialVersionUID = -5949461988200651199L;

    @ExcelField(title = "车牌号")
    private String brand;

    @ExcelField(title = "品名")
    private String name;

    @ExcelField(title = "数量")
    private Long count;

    @ExcelField(title = "运输类型")
    private String transportType;

    @ExcelField(title = "运输日期")
    private String transportDate;



    //起始地点
    @ExcelField(title = "起始地点")
    private String startSite;
    //途径地点
    @ExcelField(title = "途径地点")
    private String viaSite;
    //目的地点
    @ExcelField(title = "目的地点")
    private String aimSite;

    private String professinoalId;
    //押运员名称
    @ExcelField(title = "押运员")
    private String professinoal;

    //备注
    @ExcelField(title = "备注")
    private String remark;
}

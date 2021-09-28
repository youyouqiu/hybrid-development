package com.zw.platform.domain.basicinfo.form;

import com.zw.platform.util.excel.annotation.ExcelField;
import com.zw.platform.util.imports.ImportErrorData;
import lombok.Data;

import java.util.Date;

/**
 * @author denghuabing
 * @version V1.0
 * @description: TODO
 * @date 2020/9/11
 **/
@Data
public class ThingInfoImportForm extends ImportErrorData {

    @ExcelField(title = "物品编号", required = true, repeatable = false)
    private String thingNumber; // 物品编号

    @ExcelField(title = "物品名称")
    private String name; // 物品名称

    @ExcelField(title = "物品类别", required = true)
    private String categoryName;

    private String category;

    @ExcelField(title = "物品类型", required = true)
    private String typeName;

    private String type;

    @ExcelField(title = "品牌")
    private String label;

    @ExcelField(title = "型号")
    private String model;

    @ExcelField(title = "主要材料")
    private String material;

    @ExcelField(title = "重量")
    private Integer weight;

    @ExcelField(title = "规格")
    private String spec;

    @ExcelField(title = "制造商")
    private String manufacture;

    @ExcelField(title = "经销商")
    private String dealer;

    @ExcelField(title = "产地")
    private String place;

    private Date productDate;

    @ExcelField(title = "生产日期")
    private String productDateStr;

    @ExcelField(title = "备注")
    private String remark;

    @ExcelField(title = "错误信息", type = 1)
    private String errorMsg;

    private String groupId;

    private String groupName;
}

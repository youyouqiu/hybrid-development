package com.zw.platform.basic.dto.imports;

import com.zw.platform.util.excel.annotation.ExcelField;
import com.zw.platform.util.imports.ImportErrorData;
import lombok.Data;


/**
 * @author denghuabing
 * @version V1.0
 * @date 2020/9/11
 **/
@Data
public class ThingImportDTO extends ImportErrorData {

    @ExcelField(title = "物品编号", required = true, repeatable = false)
    private String thingNumber;

    @ExcelField(title = "物品名称")
    private String name;

    @ExcelField(title = "物品类别", required = true)
    private String categoryName;

    @ExcelField(title = "物品类型", required = true)
    private String typeName;

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

    @ExcelField(title = "生产日期")
    private String productDateStr;

    @ExcelField(title = "备注")
    private String remark;

    @ExcelField(title = "错误信息", type = 1)
    private String errorMsg;
}

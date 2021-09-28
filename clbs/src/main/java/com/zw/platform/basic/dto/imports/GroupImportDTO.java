package com.zw.platform.basic.dto.imports;

import com.zw.platform.util.excel.annotation.ExcelField;
import com.zw.platform.util.imports.ImportErrorData;

import lombok.Data;


/**
 * @author wanxing
 * @Title: 分组导入实体
 * @date 2020/10/3014:55
 */
@Data
public class GroupImportDTO extends ImportErrorData {

    @ExcelField(title = "分组名称", required = true)
    private String name;

    @ExcelField(title = "所属企业", required = true)
    private String orgName;

    @ExcelField(title = "联系人")
    private String contacts;

    /**
     * 电话号码
     */
    @ExcelField(title = "电话号码")
    private String telephone;

    @ExcelField(title = "描述")
    private String description;

    @ExcelField(title = "错误信息", type = 1)
    private String errorMsg;

    private String orgId;
}
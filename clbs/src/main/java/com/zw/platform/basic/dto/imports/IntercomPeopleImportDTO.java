package com.zw.platform.basic.dto.imports;

import com.zw.platform.util.excel.annotation.ExcelField;
import com.zw.platform.util.imports.ImportErrorData;
import lombok.Data;

/**
 * 对讲人员导入
 *
 * @author zhangjuan
 */
@Data
public class IntercomPeopleImportDTO extends ImportErrorData {
    @ExcelField(title = "监控对象", required = true, repeatable = false)
    private String peopleNumber;

    @ExcelField(title = "职位", required = true)
    private String jobName;

    @ExcelField(title = "技能")
    private String skillNames;

    @ExcelField(title = "驾照")
    private String driverTypeNames;

    @ExcelField(title = "资格证")
    private String qualification;

    @ExcelField(title = "血型")
    private String bloodType;

    @ExcelField(title = "身份证", repeatable = false)
    private String identity;

    @ExcelField(title = "民族")
    private String nation;

    private String gender;

    @ExcelField(title = "性别")
    private String genderStr;

    @ExcelField(title = "联系电话")
    private String phone;

    @ExcelField(title = "备注")
    private String remark;

    @ExcelField(title = "错误信息", type = 1)
    private String errorMsg;
}

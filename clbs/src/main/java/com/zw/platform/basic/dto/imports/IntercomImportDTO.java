package com.zw.platform.basic.dto.imports;

import com.zw.platform.util.excel.annotation.ExcelField;
import com.zw.platform.util.imports.ImportErrorData;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class IntercomImportDTO extends ImportErrorData {
    @ExcelField(title = "终端手机号", required = true, repeatable = false)
    private String simCardNumber;

    @ExcelField(title = "原始机型", required = true)
    private String originalModel;

    @ExcelField(title = "设备标识", required = true, repeatable = false)
    private String deviceNumber;

    @ExcelField(title = "设备密码", required = true)
    private String devicePassword;

    @ExcelField(title = "监控对象", required = true, repeatable = false)
    private String monitorName;

    @ExcelField(title = "监控对象类型", required = true)
    private String monitorTypeName;

    @ExcelField(title = "所属组织")
    private String orgName;

    @ExcelField(title = "群组", required = true)
    private String assignments;

    @ExcelField(title = "优先级")
    private Integer priority;

    @ExcelField(title = "错误信息", type = 1)
    private String errorMsg;
    
    @Override
    public String getErrorMsg() {
        return null;
    }

    @Override
    public void setErrorMsg(String errorMsg) {

    }
}

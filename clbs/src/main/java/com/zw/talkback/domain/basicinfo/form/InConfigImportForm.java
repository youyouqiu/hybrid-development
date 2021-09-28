package com.zw.talkback.domain.basicinfo.form;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class InConfigImportForm implements Serializable {
    private static final long serialVersionUID = 1L;

    @ExcelField(title = "终端手机号", required = true, repeatable = false)
    private String simcardNumber;

    @ExcelField(title = "原始机型", required = true)
    private String modelId;

    @ExcelField(title = "设备标识", required = true, repeatable = false)
    private String deviceNumber;

    @ExcelField(title = "设备密码", required = true)
    private String devicePassword;

    @ExcelField(title = "监控对象", required = true, repeatable = false)
    private String monitorName;

    @ExcelField(title = "监控对象类型", required = true)
    private String monitorType;

    @ExcelField(title = "所属组织")
    private String groupName;

    @ExcelField(title = "群组", required = true)
    private String assignments;

    @ExcelField(title = "优先级")
    private Integer priority = 1;

    private  String groupId;
}



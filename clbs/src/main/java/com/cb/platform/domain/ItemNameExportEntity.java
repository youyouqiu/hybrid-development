package com.cb.platform.domain;

import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import org.apache.bval.constraints.NotEmpty;

import java.io.Serializable;

@Data
public class ItemNameExportEntity implements Serializable {
    private static final long serialVersionUID = -217327635976369239L;

    private String id;
    /**
     * 品名
     */
    @ExcelField(title = "品名")
    @NotEmpty(message = "【品名】不能为空！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    private String name;


    @ExcelField(title = "危险货物类型")
    private String dangerType;
    /**
     * 单位：
     * 1：kg
     * 2:L
     */
    @ExcelField(title = "单位")
    private String unit;

    @ExcelField(title = "备注")
    private String remark;
}

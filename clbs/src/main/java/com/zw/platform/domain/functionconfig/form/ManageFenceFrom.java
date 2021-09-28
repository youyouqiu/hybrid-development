package com.zw.platform.domain.functionconfig.form;

import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 电子围栏-总表
 *
 * @author wangjianyu
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ManageFenceFrom extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @ExcelField(title = "type")
    private String type; //关联表名

    @ExcelField(title = "shape")
    private  String shape;

    @ExcelField(title = "preview")
    private String preview;

    private String typeId;

    private Double area;

}

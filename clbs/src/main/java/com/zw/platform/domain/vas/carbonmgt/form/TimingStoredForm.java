package com.zw.platform.domain.vas.carbonmgt.form;

import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * Created by 王健宇 on 2017/2/16.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TimingStoredForm extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;
    @ExcelField(title = "省")
    private String province; // 省

    @ExcelField(title = "油料类型")
    private String oilType; // 油料类型

    @ExcelField(title = "油料价格")
    private String oilPrice; // 油料价格

    @ExcelField(title = "时间")
    private String dayTime; // 时间
}

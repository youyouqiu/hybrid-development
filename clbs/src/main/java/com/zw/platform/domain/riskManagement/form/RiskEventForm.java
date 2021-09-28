package com.zw.platform.domain.riskManagement.form;

import java.io.Serializable;

import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.excel.annotation.ExcelField;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class RiskEventForm extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;
       /*
     * 风险事件
     */
    @ExcelField(title = "风险事件")
    private String riskEvent;
    
    /**
     * 风险类型
     */
    @ExcelField(title = "风险类型")
    private String riskType;
    
    /*
     * 风险描述
     */
    @ExcelField(title = "风险描述")
    private String description;
    
    /*
     * 功能id
     */
    @ExcelField(title = "功能id")
    private int functionId;


    
}

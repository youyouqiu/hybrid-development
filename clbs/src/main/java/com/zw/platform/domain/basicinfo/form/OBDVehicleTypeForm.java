package com.zw.platform.domain.basicinfo.form;

import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Administrator
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OBDVehicleTypeForm extends BaseFormBean {
    private static final long serialVersionUID = 6236538196355096781L;

    @ExcelField(title = "车型分类")
    private String typeStr;

    /**
     * 车型名称或发动机类型
     */
    @ExcelField(title = "车型名称/发动机类型")
    private String name;

    /**
     * 0、乘用车  1、商用车
     */
    private Integer type;

    /**
     * 车型码
     */
    @ExcelField(title = "车型ID")
    private String code;

    @ExcelField(title = "备注")
    private String description;

    /**
     * 1、初始数据  空、自定义数据
     */
    private Integer initial;

    private String vehicleId;
}

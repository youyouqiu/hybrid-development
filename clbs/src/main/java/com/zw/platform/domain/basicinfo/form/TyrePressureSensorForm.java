package com.zw.platform.domain.basicinfo.form;

import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Size;

@Data
@EqualsAndHashCode(callSuper = true)
public class TyrePressureSensorForm extends BaseFormBean {

    private Integer compensate;//补偿使能 1:使能,2:禁用

    private Integer filterFactor;//滤波系数 1:实时,2:平滑,3:平稳

    @ExcelField(title = "传感器型号")
    private String sensorNumber;//传感器型号

    @ExcelField(title = "补偿使能")
    private String compensateName;//补偿使能名称

    @ExcelField(title = "滤波系数")
    private String filterFactorName;//滤波系数名称

    @Size(max = 40, message = "【备注】长度不超过40！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "备注")
    private String remark;//备注

    private Integer sensorType = 7; //胎压传感器 固定7
}

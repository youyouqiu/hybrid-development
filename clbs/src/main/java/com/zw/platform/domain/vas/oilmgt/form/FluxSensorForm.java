package com.zw.platform.domain.vas.oilmgt.form;

import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.bval.constraints.NotEmpty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 
 *  流量传感器Form
 * 
 * @author wangying
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class FluxSensorForm extends BaseFormBean implements Serializable {
	private static final long serialVersionUID = 1L;

    /**
     * 传感器编号
     */
	@NotEmpty(message = "【传感器型号】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
	@Size(max = 25, message = "【传感器型号】长度不超过25！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
	@Pattern(message = "【传感器型号】输入错误，请输入合法字符(中文、-、_、字母、数字、（）、*)！",regexp = "^[A-Za-z0-9_#\\*\\u4e00-\\u9fa5\\-]+$", groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
	@ExcelField(title = "流量传感器型号")
    private String oilWearNumber;

    /**
     * 外设ID
     */
    private Integer deviceNumber = 69; // 默认 0x45

    /**
     * 参数长度
     */
    private String parameterLength;

    /**
     * 奇偶校验
     */
    @NotEmpty(message = "【奇偶校验】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
	@Pattern(message = "【奇偶校验】输入错误，只能输入1,2,3,其中1:奇校验,2:偶校验,3:无校验！",regexp = "^[1-3]{1}$", groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
    @ExcelField(title = "奇偶校验")
    private String parity;

    /**
     * 波特率
     */
    @NotEmpty(message = "【波特率】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
	@Pattern(message = "【波特率】输入错误，只能输入1~7,其中1:2400,2:4800,3:9600,4:19200,5:38400,6:57600,7:115200！",regexp = "^[1-7]{1}$", groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
    @ExcelField(title = "波特率")
    private String baudRate;

    /**
     * 补偿使能
     */
    @NotEmpty(message = "【补偿使能】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
	@Min(value=1,message = "【补偿使能】输入错误，只能输入1,2,其中1:使能,2:禁用！",groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
    @Max(value=2,message = "【补偿使能】输入错误，只能输入1,2,其中1:使能,2:禁用！",groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
    private Integer inertiaCompEn;
    
    @ExcelField(title = "补偿使能")
    private String inertiaCompEnStr;

    /**
     * 滤波系数
     */
    @NotEmpty(message = "【滤波系数】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
    @Min(value=1,message = "【滤波系数】输入错误，只能输入1,2,3,其中1:实时,2:平滑,3:平稳！",groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
    @Max(value=3,message = "【滤波系数】输入错误，只能输入1,2,3,其中1:实时,2:平滑,3:平稳！",groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
    private Integer filterFactor = 2;
    
    @ExcelField(title = "滤波系数")
    private String filterFactorStr;
    @ExcelField(title = "备注")
    private String remark;

    /**
     * 量程
     */
    private Integer ranges = 0;

    /**
     * 燃料选择
     */
    private Integer fuelSelect = 1;

    /**
     * 测量方案
     */
    private Integer meteringSchemes = 1;
}

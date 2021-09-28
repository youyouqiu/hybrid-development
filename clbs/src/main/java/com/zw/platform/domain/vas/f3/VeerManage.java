package com.zw.platform.domain.vas.f3;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.apache.bval.constraints.NotEmpty;

import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.annotation.ExcelField;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class VeerManage {

	private static final long serialVersionUID = 1L;

	@NotEmpty(message = "【传感器型号】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
	@Size(max = 20, message = "【传感器型号】长度不超过20！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
	@Pattern(message = "【传感器型号】输入错误，请输入合法字符(数字、字母、短杠、下划线)！",regexp = "^[\\w\\-]+$", groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
	@ExcelField(title = "传感器型号")
	private String sensorNumber;//传感器型号

	private String baudrateName;//波特率名称

	private String oddEvenCheckName;//奇偶校验名称

	@NotEmpty(message = "【补偿使能】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
	@Min(value=1,message = "【补偿使能】输入错误，只能输入1,2,其中1:使能,2:禁用！",groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
    @Max(value=2,message = "【补偿使能】输入错误，只能输入1,2,其中1:使能,2:禁用！",groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
	@ExcelField(title = "补偿使能")
	private String compensateName;//补偿使能名称

	@Size(max = 40, message = "【备注】长度不超过40！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
	@ExcelField(title = "备注")
	private String remark;//备注

	private Integer baudrate;//波特率

	private Integer oddEvenCheck;//奇偶校验

	private Integer compensate;//补偿使能

	private Integer sensorType;//传感器类别

	private String autotimeName;//自动上传时间名字

	private Integer autoTime;//自动上传时间

	private Integer filterFactor;//滤波系数
}

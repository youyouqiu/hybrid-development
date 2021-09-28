package com.zw.platform.domain.vas.workhourmgt.form;
import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import org.apache.bval.constraints.NotEmpty;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
public class WorkHourSensorForm extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 传感器型号
     */
    @NotEmpty(message = "【传感器型号】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
    @Size(max = 25, message = "【传感器型号】长度不超过25！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "传感器型号")
    private String sensorNumber;

    /**
     * 检测方式(1:电压比较式;2:油耗阈值式;3:油耗波动式)
     */
    @NotEmpty(message = "【检测方式】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
    @Min(value=1,message = "【检测方式】输入错误，只能输入1,2,3其中1:电压比较式,2:油耗阈值式,3:油耗波动式！",
            groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
    @Max(value=3,message = "【检测方式】输入错误，只能输入1,2,3其中1:电压比较式,2:油耗阈值式,3:油耗波动式！",
            groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
    private Integer detectionMode = 1;

    @ExcelField(title = "检测方式")
    private String detectionModeForExport = "";

    /**
     * 滤波系数（1:实时,2:平滑,3:平稳）
     */
    @NotEmpty(message = "【滤波系数】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
    @Min(value=1,message = "【滤波系数】输入错误，只能输入1,2,3,其中1:实时,2:平滑,3:平稳！",
                             groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
    @Max(value=3,message = "【滤波系数】输入错误，只能输入1,2,3,其中1:实时,2:平滑,3:平稳！",
                             groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
    private Integer filterFactor = 2;

    @ExcelField(title = "滤波系数")
    private String filterFactorForExport = "";

    /**
     * 波特率 (其中1:2400,2:4800,3:9600,4:19200,5:38400,6:57600,7:115200)
     */
    @NotEmpty(message = "【波特率】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
    @Min(value=1,message = "【波特率】输入错误，只能输入1-7,其中1:2400,2:4800,3:9600,4:19200,5:38400,6:57600,7:115200！",
            groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
    @Max(value=7,message = "【波特率】输入错误，只能输入1-7,其中1:2400,2:4800,3:9600,4:19200,5:38400,6:57600,7:115200！",
            groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
    private Integer baudRate = 9600;

    @ExcelField(title = "波特率")
    private String baudRateForExport;

    /**
     * 奇偶校验（1：奇校验；2：偶校验；3：无校验）
     */
    @NotEmpty(message = "【奇偶校验】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
    @Min(value=1,message = "【奇偶校验】输入错误，只能输入1,2,3,其中1:奇校验,2:偶校验,3:无校验！",
            groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
    @Max(value=3,message = "【奇偶校验】输入错误，只能输入1,2,3,其中1:奇校验,2:偶校验,3:无校验！",
            groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
    private Integer oddEvenCheck = 3;

    @ExcelField(title = "奇偶校验")
    private String oddEvenCheckForExport = "";

    /**
     * 补偿使能（1:使能,2:禁用）
     */
    @NotEmpty(message = "【补偿使能】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
    @Min(value=1,message = "【补偿使能】输入错误，只能输入1,2,其中1:使能,2:禁用！",groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
    @Max(value=2,message = "【补偿使能】输入错误，只能输入1,2,其中1:使能,2:禁用！",groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
    private Integer compensate = 1;

    @ExcelField(title = "补偿使能")
    private String compensateForExport = "";


    /**
     * 自动上传时间
     */
    private Integer autoTime;

    /**
     * 1温度传感器  2湿度传感器  3正反转传感器 4工时传感器
     */
    private Integer sensorType = 4;

    /**
     * 备注
     */
    @ExcelField(title = "备注")
    private String remark;
}

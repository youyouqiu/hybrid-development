package com.zw.platform.domain.vas.loadmgt.form;

import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.bval.constraints.NotEmpty;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

/***
 @Author gfw
 @Date 2018/9/6 16:41
 @Description 载重传感器 新增实体
 @version 1.0
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class LoadSensorForm extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 传感器型号号
     */
    @NotNull(message = "【传感器型号】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Size(max = 25, message = "【传感器型号】长度不超过25！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Pattern(message = "【传感器型号】输入错误，请输入合法字符(中文、-、_、字母、数字、（）、*)！", regexp = "^[A-Za-z0-9_#\\*\\u4e00-\\u9fa5\\-]+$",
        groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "传感器型号")
    private String sensorNumber;

    /**
     * 默认为载重传感器
     */
    private String sensorType = "6";

    /**
     * 传感器长度
     */
    @Pattern(message = "【传感器长度】输入错误，请输入合法的数字！", regexp = "^(?:[1-9][0-9]*(?:\\.[0-9]+)?|0\\.(?!0+$)[0-9]+)$",
        groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String sensorLength;

    /**
     * 量程
     */
    private String measuringRange;

    /**
     * 上盲区-add by liubq 2016-11-16
     */
    private String upperBlindZone;

    /**
     * 下盲区-add by liubq 2016-11-16
     */
    private String lowerBlindArea;

    /**
     * 滤波系数
     */
    @ExcelField(title = "滤波系数")
    private String filterFactorStr;

    @NotEmpty(message = "【滤波系数】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Min(value = 1, message = "【滤波系数】输入错误，只能输入1,2,3,其中1:实时,2:平滑,3:平稳！",
        groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Max(value = 3, message = "【滤波系数】输入错误，只能输入1,2,3,其中1:实时,2:平滑,3:平稳！",
        groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private Integer filterFactor = 2;

    /**
     * 波特率
     */
    @ExcelField(title = "波特率")
    private String baudRateStr = "";

    /**
     * 波特率 (其中1:2400,2:4800,3:9600,4:19200,5:38400,6:57600,7:115200)
     */
    @NotEmpty(message = "【波特率】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Min(value = 1, message = "【波特率】输入错误，只能输入1-7,其中1:2400,2:4800,3:9600,4:19200,5:38400,6:57600,7:115200！",
        groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Max(value = 7, message = "【波特率】输入错误，只能输入1-7,其中1:2400,2:4800,3:9600,4:19200,5:38400,6:57600,7:115200！",
        groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private Integer baudRate = 9600;

    /**
     * 奇偶效验
     */
    @ExcelField(title = "奇偶效验")
    private String oddEvenCheckStr = "";
    @NotEmpty(message = "【奇偶校验】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Min(value = 1, message = "【奇偶校验】输入错误，只能输入1,2,3,其中1:奇校验,2:偶校验,3:无校验！",
        groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Max(value = 3, message = "【奇偶校验】输入错误，只能输入1,2,3,其中1:奇校验,2:偶校验,3:无校验！",
        groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private Integer oddEvenCheck;

    /**
     * 补偿使能
     */
    @ExcelField(title = "补偿使能")
    private String compensateStr = "";
    @NotEmpty(message = "【补偿使能】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Min(value = 1, message = "【补偿使能】输入错误，只能输入1,2,其中1:使能,2:禁用！",
        groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Max(value = 2, message = "【补偿使能】输入错误，只能输入1,2,其中1:使能,2:禁用！",
        groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private Integer compensate;

    @ExcelField(title = "备注")
    private String remark;

    /**
     * 设备厂商
     */
    //@ExcelField(title = "设备厂商")
    private String manuFacturer;

    /**
     * 启停状态
     */
    //@ExcelField(title = "启停状态")
    private String isStartStr = "";
    private Short isStart = 1;

    /**
     * 出厂时间
     */
    //@ExcelField(title = "出厂时间")
    private String factoryDateStr = "";
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date factoryDate;

    /**
     * 标定数量
     */
    //@ExcelField(title = "标定数量")
    private String calibrationNumber;

    /**
     * 系数K
     */
    //@ExcelField(title = "系数K")
    private String factorK;

    /**
     * 系数B
     */
    //@ExcelField(title = "系数B")
    private String factorB;

    /**
     * 上传间隔
     */
    //@ExcelField(title = "上传间隔")
    private Short uploadInterval;

    /**
     * 描述
     */
    //@ExcelField(title = "描述")
    private String description;

}

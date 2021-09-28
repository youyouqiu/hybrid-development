package com.zw.platform.domain.vas.mileageSensor;

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
 * <p>
 * Title:里程传感器基础信息
 * <p>
 * Copyright: Copyright (c) 2016
 * <p>
 * Company: ZhongWei
 * <p>
 * team: ZhongWeiTeam
 *
 * @version 1.0
 * @author: nixiangqian
 * @date 2017年05月16日 10:36
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class MileageSensor extends BaseFormBean implements Serializable {

    @NotEmpty(message = "【传感器型号】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
    @Size(max = 20, message = "【传感器型号】长度不超过20！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Pattern(message = "【传感器型号】输入错误，请输入合法字符(中文、-、_、字母、数字、（）、*)！",regexp = "^[A-Za-z0-9_.\\(\\)\\（\\）\\*\\u4e00-\\u9fa5\\-]+$", groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
    @ExcelField(title = "轮速传感器型号")
    private String sensorType;//型号

    @NotEmpty(message = "【奇偶校验】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
    @Pattern(message = "【奇偶校验】输入错误，只能输入1,2,3,其中1:奇校验,2:偶校验,3:无校验！",regexp = "^[1-3]{1}$", groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
    private Integer parityCheck;//奇偶校验.

    @ExcelField(title = "奇偶校验")
    private String parityCheckStr;//奇偶校验

    @NotEmpty(message = "【波特率】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
    @Pattern(message = "【波特率】输入错误，只能输入1~7,其中1:2400,2:4800,3:9600,4:19200,5:38400,6:57600,7:115200！",regexp = "^[1-7]{1}$", groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
    private Integer   baudRate;//波特率

    @ExcelField(title = "波特率")
    private String   baudRateStr;//波特率

    @NotEmpty(message = "【补偿使能】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
    @Min(value=1,message = "【补偿使能】输入错误，只能输入1,2,其中1:使能,2:禁用！",groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
    @Max(value=2,message = "【补偿使能】输入错误，只能输入1,2,其中1:使能,2:禁用！",groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
    private Integer compEn;//补偿使能

    @ExcelField(title = "补偿使能")
    private String compEnStr;//补偿使能

    @NotEmpty(message = "【滤波系数】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
    @Min(value=1,message = "【滤波系数】输入错误，只能输入1,2,3,其中1:实时,2:平滑,3:平稳！",groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
    @Max(value=3,message = "【滤波系数】输入错误，只能输入1,2,3,其中1:实时,2:平滑,3:平稳！",groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
    private Integer  filterFactor;//滤波系数
    @ExcelField(title = "滤波系数")
    private String  filterFactorStr;//滤波系数




    private String  remark;//备注

}

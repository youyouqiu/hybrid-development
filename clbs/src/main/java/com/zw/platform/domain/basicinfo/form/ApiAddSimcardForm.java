package com.zw.platform.domain.basicinfo.form;

import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.annotation.ExcelField;
import io.swagger.annotations.ApiParam;
import lombok.Data;
import org.apache.bval.constraints.NotEmpty;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.util.Date;


@Data
public class ApiAddSimcardForm {
    @ApiParam(value = "SIM卡卡号", required = true)
    @ExcelField(title = "SIM卡卡号")
    @NotEmpty(message = "【SIM卡卡号】不能为空！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    private String simcardNumber;

    /**
     * 所属企业名称
     */
    @ExcelField(title = "所属企业", required = true)
    @ApiParam(value = "所属企业")
    private String groupName;

    /**
     * 组织id
     */
    @ApiParam(value = "组织id", required = true)
    @NotEmpty(message = "【所属企业】不能为空！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    private String groupId;

    @Min(value = 0, message = "【启停状态】输入错误，只能输入0,1,其中0:停用,1:启用！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @Max(value = 1, message = "【启停状态】输入错误，只能输入0,1,其中0:停用,1:启用！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @ApiParam(value = "启停状态,0:停用,1:启用", required = true, defaultValue = "1")
    private Integer isStart;

    @ExcelField(title = "ICCID")
    @ApiParam(value = "ICCID")
    private String iccid;

    @ExcelField(title = "IMEI")
    @ApiParam(value = "IMEI")
    private String imei;

    @ExcelField(title = "IMSI")
    @ApiParam(value = "IMSI")
    private String imsi;

    /**
     * 运营商
     */
    @ApiParam(value = "运营商", required = true, defaultValue = "中国移动")
    @Size(max = 50, message = "【运营商】长度不超过50！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @ExcelField(title = "运营商")
    private String operator;

    /**
     * 套餐流量
     */
    @ApiParam(value = "套餐流量")
    @ExcelField(title = "套餐流量")
    private String simFlow;

    @ApiParam(value = "当日流量")
    private String dayRealValue;

    @ApiParam(value = "当月流量")
    private String monthRealValue;

    @ApiParam(value = "流量最后更新时间")
    private String monthTrafficDeadline;

    @ExcelField(title = "修正系数")
    @ApiParam(value = "修正系数")
    private String correctionCoefficient;

    @ExcelField(title = "预警系数")
    @ApiParam(value = "预警系数")
    private String forewarningCoefficient;

    /**
     * 预警流量
     */
    @ApiParam(value = "预警流量")
    private String alertsFlow;

    @ApiParam(value = "流量月结日")
    private String monthlyStatement;

    @ExcelField(title = "小时流量阈值")
    @ApiParam(value = "小时流量阈值")
    private String hourThresholdValue;

    @ExcelField(title = "日流量阈值")
    @ApiParam(value = "日流量阈值")
    private String dayThresholdValue;

    @ExcelField(title = "月流量阈值")
    @ApiParam(value = "月流量阈值")
    private String monthThresholdValue;

    /**
     * 开卡时间
     */
    @ExcelField(title = "激活日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiParam(value = "激活日期，格式：yyyy-MM-dd")
    private Date openCardTime;

    /**
     * 到期时间
     */
    @ExcelField(title = "到期时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiParam(value = "到期时间，格式:yyyy-MM-dd")
    private Date endTime;

    @ExcelField(title = "真实SIM卡号")
    @ApiParam(value = "真实SIM卡号")
    private String realId;

    @ExcelField(title = "备注信息")
    @ApiParam(value = "备注信息")
    private String remark;

}

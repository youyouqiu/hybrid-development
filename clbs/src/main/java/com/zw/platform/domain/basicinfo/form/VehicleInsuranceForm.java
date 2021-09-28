package com.zw.platform.domain.basicinfo.form;

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
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

/**
 * @author zhouzongbo on 2018/5/10 9:30
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class VehicleInsuranceForm extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = -1553708260567984382L;

    /**
     * 车辆id
     */
    @NotEmpty(groups = {ValidGroupAdd.class,ValidGroupUpdate.class},message = "【车辆id】不能为空")
    private String vehicleId;

    /**
     * 保险单号
     */
    @ExcelField(title = "保险单号")
    @Size(min = 1, max = 30,message = "【保险单号】长度范围1~30位",groups = {ValidGroupAdd.class,ValidGroupUpdate.class})
    @NotEmpty(groups = {ValidGroupAdd.class,ValidGroupUpdate.class},message = "【保险单号】不能为空")
    private String insuranceId;

    /**
     * 车牌号
     */
    @ExcelField(title = "车牌号")
    private String brand;

    /**
     * 保险类型
     */
    @ExcelField(title = "保险类型")
    private String insuranceType;

    /**
     * 保险公司
     */
    @ExcelField(title = "保险公司")
    private String company;

    /**
     * 保险开始时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date startTime;
    @ExcelField(title = "保险开始时间")
    private String startTimeStr;

    /**
     * 保险到期时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endTime;
    @ExcelField(title = "保险到期时间")
    private String endTimeStr;

    /**
     * 提前提醒天数
     */
    @ExcelField(title = "提前提醒天数")
    private Short preAlert;

    /**
     * 保险金额
     */
    @ExcelField(title = "保险金额")
    private Integer amountInsured;

    /**
     * 折扣率(%)
     */
    @ExcelField(title = "折扣率(%)")
    private Double discount;
    /**
     * 实际费用
     */
    private Double actualCost;
    @ExcelField(title = "实际费用")
    private String actualCostStr;
    /**
     * 代理人
     */
    @ExcelField(title = "代理人")
    private String agent;

    /**
     * 电话
     */
    @ExcelField(title = "电话")
    private String phone;

    /**
     * 备注
     */
    @ExcelField(title = "备注")
    private String remark;
}

package com.zw.platform.domain.basicinfo.form;

import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.annotation.ExcelField;
import com.zw.platform.util.imports.ImportErrorData;
import lombok.Data;
import org.apache.bval.constraints.NotEmpty;

import java.io.Serializable;
import java.util.Date;

/**
 * @author penghj
 * @version 1.0
 */
@Data
public class SimcardImportForm extends ImportErrorData implements Serializable {
    private static final long serialVersionUID = 2219164705356795231L;
    @ExcelField(title = "ICCID")
    private String iccid;

    @ExcelField(title = "IMEI")
    private String imei;

    @ExcelField(title = "IMSI")
    private String imsi;

    /**
     * sim卡号
     */
    @ExcelField(title = "SIM卡卡号", required = true, repeatable = false)
    private String simcardNumber;

    /**
     * 所属企业名称
     */
    @ExcelField(title = "所属企业", required = true)
    private String groupNameImport;

    /**
     * 启停状态
     */
    @ExcelField(title = "启停状态")
    private String isStarts;
    private Integer isStart;

    /**
     * 运营商
     */
    @ExcelField(title = "运营商")
    private String operator;

    /**
     * 发放地市（1120改动）
     */
    @ExcelField(title = "发放地市")
    private String placementCity;

    @ExcelField(title = "套餐流量")
    private String simFlow;

    @ExcelField(title = "修正系数")
    private String correctionCoefficient;

    @ExcelField(title = "预警系数")
    private String forewarningCoefficient;

    @ExcelField(title = "小时流量阈值")
    private String hourThresholdValue;

    @ExcelField(title = "日流量阈值")
    private String dayThresholdValue;

    @ExcelField(title = "月流量阈值")
    private String monthThresholdValue;

    /**
     * 开卡时间
     */
    @ExcelField(title = "激活日期")
    private String openCardTimeStr;

    private Date openCardTime;

    /**
     * 到期时间
     */
    @ExcelField(title = "到期时间")
    private String endTimeStr;

    private Date endTime;

    /**
     * 组织id
     */
    @NotEmpty(message = "【所属企业】不能为空！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    private String groupId;

    /**
     * 组织
     */
    private String groupName;

    @ExcelField(title = "真实SIM卡号")
    private String realId;

    @ExcelField(title = "备注信息")
    private String remark;

    @ExcelField(title = "错误信息", type = 1)
    private String errorMsg;

    /**
     * 已用流量
     */
    private String useFlow;

    /**
     * 预警流量
     */
    private String alertsFlow;

    /**
     * 伪IP
     */
    private String fakeIP;

    /**
     * 流量月结日
     */
    private String monthlyStatement;

    /**
     * 当日流量
     */
    private String dayRealValue;

    /**
     * 当月流量
     */
    private String monthRealValue;

    /**
     * 流量最后更新时间
     */
    private String monthTrafficDeadline;
}

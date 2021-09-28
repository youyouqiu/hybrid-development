package com.zw.platform.basic.dto.imports;

import com.zw.platform.util.excel.annotation.ExcelField;
import com.zw.platform.util.imports.ImportErrorData;
import lombok.Data;

/**
 * @Author: zjc
 * @Description:sim卡导入类，这里必须按照导入模板顺序添加对应的导入列表
 * @Date: create in 2020/11/9 9:21
 */
@Data
public class SimCardImportDTO extends ImportErrorData {

    /**
     * ICCID
     */
    @ExcelField(title = "ICCID")
    private String iccid;

    /**
     * IMEI
     */
    @ExcelField(title = "IMEI")
    private String imei;

    /**
     * IMSI
     */
    @ExcelField(title = "IMSI")
    private String imsi;

    /**
     * sim卡号
     */
    @ExcelField(title = "终端手机号")
    private String simCardNumber;

    /**
     * 所属企业名称
     */
    @ExcelField(title = "所属企业")
    private String orgName;

    /**
     * 启停状态
     */
    @ExcelField(title = "启停状态")
    private String isStarts;

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

    /**
     * 套餐流量
     */
    @ExcelField(title = "套餐流量(M)")
    private String simFlow;

    /**
     * 修正系数
     */
    @ExcelField(title = "修正系数")
    private String correctionCoefficient;

    /**
     * 预警系数
     */
    @ExcelField(title = "预警系数")
    private String forewarningCoefficient;

    /**
     * 小时流量阈值(M)
     */
    @ExcelField(title = "小时流量阈值(M)")
    private String hourThresholdValue;

    /**
     * 日流量阈值(M)
     */
    @ExcelField(title = "日流量阈值(M)")
    private String dayThresholdValue;

    /**
     * 月流量阈值(M)
     */
    @ExcelField(title = "月流量阈值(M)")
    private String monthThresholdValue;

    /**
     * 开卡时间(激活日期)
     */
    @ExcelField(title = "激活日期")
    private String openCardTime;

    /**
     * 到期时间
     */
    @ExcelField(title = "到期时间")
    private String endTime;
    /**
     * 真实SIM卡号
     */
    @ExcelField(title = "真实SIM卡号")
    private String realId;

    /**
     * 备注信息
     */
    @ExcelField(title = "备注信息")
    private String remark;

    /**
     * 错误信息
     */
    @ExcelField(title = "错误信息", type = 1)
    private String errorMsg;

}

package com.zw.platform.basic.dto.export;

import com.google.common.collect.ImmutableMap;
import com.zw.platform.basic.domain.BaseKvDo;
import com.zw.platform.basic.domain.SimCardListDO;
import com.zw.platform.util.LocalDateUtils;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * @Author: zjc
 * @Description:sim卡导出类，这里必须按照导入模板顺序添加对应的导入列表
 * @Date: create in 2020/11/9 9:21
 */
@Data
public class SimCardExportDTO {

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
     * 当日流量(M)
     */
    @ExcelField(title = "当日流量(M)")
    private String dayRealValue;

    /**
     * 当月流量(M)
     */
    @ExcelField(title = "当月流量(M)")
    private String monthRealValue;

    /**
     * 流量最后更新时间
     */
    @ExcelField(title = "流量最后更新时间")
    private String monthTrafficDeadline;

    /**
     * 月预警流量(M)
     */
    @ExcelField(title = "月预警流量(M)")
    private String alertsFlow;
    /**
     * 流量月结日
     */
    @ExcelField(title = "流量月结日")
    private String monthlyStatement;

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
     * 终端手机号
     */
    @ExcelField(title = "终端号")
    private String deviceNumber;

    @ExcelField(title = "监控对象")
    private String brand;

    @ExcelField(title = "创建日期")
    private String createDataTime;
    @ExcelField(title = "修改日期")
    private String updateDataTime;
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




    private static final Map<Integer, String> isStartMap = ImmutableMap.of(0, "停用", 1, "启用");

    public static SimCardExportDTO build(SimCardListDO data, Map<String, String> userOrgMap,
        Map<String, BaseKvDo<String, String>> monitorIdNameMap) {
        SimCardExportDTO export = new SimCardExportDTO();
        BeanUtils.copyProperties(data, export);
        export.orgName = userOrgMap.get(data.getOrgId());
        export.simCardNumber = data.getSimcardNumber();
        export.isStarts = isStartMap.get(data.getIsStart());
        Date createDataTime = data.getCreateDataTime();
        if (Objects.nonNull(createDataTime)) {
            export.createDataTime = LocalDateUtils.dateFormate(createDataTime);
        }
        Date updateDataTime = data.getUpdateDataTime();
        if (Objects.nonNull(updateDataTime)) {
            export.updateDataTime = LocalDateUtils.dateFormate(updateDataTime);
        }
        Date openCardTime = data.getOpenCardTime();
        if (Objects.nonNull(openCardTime)) {
            export.openCardTime = LocalDateUtils.dateFormate(openCardTime);
        }
        Date endTime = data.getEndTime();
        if (Objects.nonNull(endTime)) {
            export.endTime = LocalDateUtils.dateFormate(endTime);
        }
        BaseKvDo<String, String> kvDo = monitorIdNameMap.get(data.getMonitorId());
        if (kvDo != null) {
            export.brand = kvDo.getFirstVal();
        }
        return export;
    }
}

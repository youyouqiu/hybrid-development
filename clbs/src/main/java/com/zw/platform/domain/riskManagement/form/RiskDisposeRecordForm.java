package com.zw.platform.domain.riskManagement.form;

import com.google.common.collect.Maps;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by PengFeng on 2017/8/23 9:14
 */
@EqualsAndHashCode(callSuper = false)
public class RiskDisposeRecordForm extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 风险类型转换
     */
    private static Map<String, String> riskTypeMap = new HashMap<>();

    /**
     * 风险状态转换
     */
    private static Map<String, String> statusMap = new HashMap<>();

    /**
     * 风控结果转换
     */
    private static Map<String, String> resultMap = new HashMap<>();

    /**
     * 事件类型
     */
    private static Map<String, String> evenTypeMap = Maps.newHashMap();

    /**
     * 风险等级
     */

    private static Map<String, String> riskLevelMap = Maps.newHashMap();

    static {
        riskTypeMap.put("1", "疑似疲劳");
        riskTypeMap.put("2", "注意力分散");
        riskTypeMap.put("3", "违规异常");
        riskTypeMap.put("4", "碰撞危险");
        statusMap.put("1", "待处理");
        statusMap.put("3", "待处理");
        statusMap.put("5", "待处理");
        statusMap.put("2", "待处理");
        statusMap.put("4", "待处理");
        statusMap.put("6", "已处理");
        resultMap.put("1", "接受督导高敏");
        resultMap.put("2", "接受督导低敏");
        resultMap.put("3", "事故发生");

        evenTypeMap.put("6401", "前向碰撞");
        evenTypeMap.put("6402", "车道偏离");
        evenTypeMap.put("64021", "车道左偏离");
        evenTypeMap.put("64022", "车道右偏离");
        evenTypeMap.put("6403", "车距过近");
        evenTypeMap.put("6404", "行人碰撞");
        evenTypeMap.put("6405", "频繁变道");
        // evenTypeMap.put("6407", "障碍物");
        evenTypeMap.put("64081", "急加速");
        evenTypeMap.put("64082", "急减速");
        evenTypeMap.put("64083", "急转弯");
        evenTypeMap.put("6408", "急加/急减/急转弯");
        evenTypeMap.put("6409", "道路识别超限");
        evenTypeMap.put("6410", "道路标示识别");

        evenTypeMap.put("6502", " 接打手持电话");
        evenTypeMap.put("6503", "抽烟");
        //evenTypeMap.put("6504","注意力分散");
        //evenTypeMap.put("6505","异常报警");
        evenTypeMap.put("6506", "闭眼");
        evenTypeMap.put("6507", "打哈欠");
        evenTypeMap.put("6508", "长时间不目视前方");
        evenTypeMap.put("6509", "人证不符");
        evenTypeMap.put("6510", "驾驶员不在驾驶位置");
        evenTypeMap.put("6511", "遮挡");
        evenTypeMap.put("6512", "红外阻断");

        riskLevelMap.put("1", "一般(低)");
        riskLevelMap.put("2", "一般(中)");
        riskLevelMap.put("3", "一般(高)");
        riskLevelMap.put("4", "较重(低)");
        riskLevelMap.put("5", "较重(中)");
        riskLevelMap.put("6", "较重(高)");
        riskLevelMap.put("7", "严重(低)");
        riskLevelMap.put("8", "严重(中)");
        riskLevelMap.put("9", "严重(高)");
        riskLevelMap.put("10", "特重(低)");
        riskLevelMap.put("11", "特重(中)");
        riskLevelMap.put("12", "特重(高)");


    }

    /**
     * 风险编号
     */
    @Getter
    @Setter
    @ExcelField(title = "风险编号", mergedRegion = true)
    private String riskNumber;

    /**
     * 报警类型
     */
    @Getter
    @ExcelField(title = "风险类型", mergedRegion = true)
    private String riskType;

    /**
     * 报警等级
     */
    @Getter
    @ExcelField(title = "风险等级", mergedRegion = true)
    private String riskLevel;

    /**
     * 事件编号
     */
    @Getter
    @Setter
    @ExcelField(title = "事件编号")
    private String eventNumber;

    /**
     * 风险事件
     */
    @Getter
    @ExcelField(title = "报警事件")
    private String riskEvent;

    /**
     * 事件时间
     */
    @Getter
    @Setter
    @ExcelField(title = "事件时间")
    private String eventTime;

    /**
     * 预警时间
     */
    @Getter
    @ExcelField(title = "预警时间", mergedRegion = true)
    private String warTime;

    /**
     * 预警位置
     */
    @Getter
    @Setter
    @ExcelField(title = "预警位置", mergedRegion = true)
    private String address;

    @Getter
    @Setter
    private String formattedAddress;

    /**
     * 监控对象
     */
    @Getter
    @Setter
    @ExcelField(title = "监控对象", mergedRegion = true)
    private String brand;

    /**
     * 驾驶员
     */
    @Getter
    @Setter
    @ExcelField(title = "驾驶员", mergedRegion = true)
    private String driver;

    /**
     * 所属企业
     */
    @Getter
    @Setter
    @ExcelField(title = "所属企业", mergedRegion = true)
    private String groupName;

    /**
     * 状态
     */
    @Getter
    @ExcelField(title = "处理状态", mergedRegion = true)
    private String status;

    /**
     * 处理人姓名
     */
    @Getter
    @Setter
    @ExcelField(title = "处理人", mergedRegion = true)
    private String dealUser;

    /**
     * 归档时间
     */
    @Getter
    @Setter
    @ExcelField(title = "处理时间", mergedRegion = true)
    private String fileTime;

    /**
     * 预警天气
     */
    @Getter
    @Setter
    @ExcelField(title = "天气情况", mergedRegion = true)
    private String weather;

    /**
     * 处理时间
     */
    @Getter
    @Setter
    //  @ExcelField(title = "处理时间", mergedRegion = true)
    private String dealTime;

    /**
     * id
     */
    @Getter
    @Setter
    private String id;

    /**
     * id
     */
    @Getter
    @Setter
    private byte[] idByte;



    /**
     * 所属企业ldapUUID
     */
    @Getter
    @Setter
    private String groupId;

    /**
     * 岗位
     */
    @Getter
    @Setter
    // @ExcelField(title = "岗位", mergedRegion = true)
    private String job;

    /**
     * 回访次数
     */
    @Getter
    @Setter
    // @ExcelField(title = "回访次数", mergedRegion = true)
    private String visitTime;

    /**
     * 风控结果
     */
    @Getter
    // @ExcelField(title = "风控结果", mergedRegion = true)
    private String riskResult;

    /**
     * 经度，提供表格逆地理编码
     */
    @Getter
    @Setter
    private String longtitude;

    /**
     * 纬度，提供表格逆地理编码
     */
    @Getter
    @Setter
    private String latitude;

    @Getter
    @Setter
    private String vehicleId;

    @Getter
    @Setter
    private String mediaName;

    @Getter
    @Setter
    private String mediaUrlNew;

    @Getter
    @Setter
    private String mediaId;

    @Getter
    @Setter
    private String driverIds;

    @Getter
    @Setter
    private String riskId;

    @Getter
    @Setter
    private String riskEventId;

    /**
     * Setter for property 'riskType'.
     *
     * @param riskType Value to set for property 'riskType'.
     */
    public void setRiskType(String riskType) {
        StringBuilder type = new StringBuilder("");

        if (!StringUtil.isNullOrBlank(riskType)) {
            String[] types = riskType.split(",");
            for (String typeN : types) {
                if (StringUtils.isNumeric(types[0])) {
                    type.append(riskTypeMap.get(typeN)).append("+");
                    this.riskType = type.toString().substring(0, type.length() - 1);
                } else {
                    this.riskType = riskType;
                }
            }
        } else {
            this.riskType = riskType;
        }
    }

    /**
     * Setter for property 'status'.
     *
     * @param status Value to set for property 'status'.
     */
    public void setStatus(String status) {
        if (StringUtil.isNullOrBlank(status) || !StringUtils.isNumeric(status)) {
            this.status = status;
        } else {
            this.status = statusMap.get(status);
        }
    }

    /**
     * Setter for property 'riskResult'.
     *
     * @param riskResult Value to set for property 'riskResult'.
     */
    public void setRiskResult(String riskResult) {
        if (StringUtil.isNullOrBlank(riskResult) || !StringUtils.isNumeric(riskResult)) {
            this.riskResult = riskResult;
        } else {
            this.riskResult = resultMap.get(riskResult);
        }
    }

    public RiskDisposeRecordForm() {

    }

    public void setRiskEvent(String riskEvent) {
        if (StringUtil.isNullOrBlank(riskEvent) || !StringUtils.isNumeric(riskEvent)) {
            this.riskEvent = riskEvent;
        } else {
            this.riskEvent = evenTypeMap.get(riskEvent);
        }
    }

    public void setWarTime(String warTime) {
        if (StringUtil.isNullOrBlank(warTime) || !StringUtils.isNumeric(warTime)) {
            this.warTime = warTime;
        } else {
            this.warTime = DateUtil.getDateToString(new Date(Long.parseLong(warTime)), "yyyy-MM-dd HH:mm:ss");
        }
    }

    public void setRiskLevel(String riskLevel) {
        if (StringUtil.isNullOrBlank(riskLevel) || "所有".equals(riskLevel)) {
            this.riskLevel = null;
        } else {
            this.riskLevel = riskLevelMap.get(riskLevel);
        }
    }

}

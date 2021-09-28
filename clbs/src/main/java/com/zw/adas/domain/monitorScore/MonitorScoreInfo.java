package com.zw.adas.domain.monitorScore;

import com.zw.platform.domain.basicinfo.enums.PlateColor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Map;


@Data
public class MonitorScoreInfo {

    /**
     * 监控对象id
     */
    @Getter
    @Setter
    private String vehicleId;

    /**
     * 综合得分
     */
    @Getter
    @Setter
    private Double score;

    /**
     * 综合得分str
     */
    @Getter
    @Setter
    private String scoreStr;

    /**
     * 得分环比
     */
    @Getter
    @Setter
    private String scoreRingRatio;

    /**
     * 得分环比评语
     */
    @Getter
    @Setter
    private String scoreRingRatioStr = "-";

    /**
     * 报警数
     */
    @Getter
    @Setter
    private Integer alarmTotal;

    /**
     * 报警数环比
     */
    @Getter
    @Setter
    private String alarmRingRatio;

    /**
     * 报警数环比评语
     */
    @Getter
    @Setter
    private String alarmRingRatioStr = "-";

    /**
     * 百公里报警数
     */
    @Getter
    @Setter
    private Double hundredsAlarmTotal;

    /**
     * 百公里报警数环比
     */
    @Getter
    @Setter
    private String hundredsAlarmRingRatio;

    /**
     * 行驶里程
     */
    @Getter
    @Setter
    private Double travelMile;

    /**
     * 平均行驶时长
     */
    @Getter
    @Setter
    private String averageTravelTime;

    /**
     * 车牌号
     */
    @Getter
    @Setter
    private String brand;

    /**
     * 车牌颜色
     */
    @Getter

    private Integer plateColor;

    /**
     * 运营类别
     */
    @Getter
    @Setter
    private String purposeCategory;

    /**
     * 运营状态
     */
    @Getter
    private Integer isStart;

    /**
     * 所属企业id
     */
    @Getter
    @Setter
    private String groupId;

    /**
     * 所属企业id
     */
    private String groupName;

    /**
     * 车架号
     */
    @Getter
    @Setter
    private String chassisNumber;

    /**
     * 行驶证发证日期
     */
    @Getter
    @Setter
    private transient Date licenseIssuanceFormDate;

    /**
     * 行驶证有效期
     */
    @Getter
    @Setter
    private transient Date registrationEndFormDate;

    /**
     * 行驶证发证日期
     */
    @Getter
    @Setter
    private String licenseIssuanceDate;

    /**
     * 行驶证有效期
     */
    @Getter
    @Setter
    private String registrationEndDate;

    /**
     * 车辆图片
     */
    @Getter
    @Setter
    private String vehiclePhoto;

    /**
     * 车辆图片路径
     */
    @Getter
    @Setter
    private String vehiclePhotoPath;

    /**
     * 事件报警数
     */
    private transient String eventInfos;

    /**
     * 报警数排行
     */
    @Getter
    @Setter
    private Map<String, Integer> alarmMap;

    /**
     * 车牌颜色 str
     */
    @Getter
    @Setter
    private String plateColorStr;

    /**
     * 运营状态 str
     */
    @Getter
    @Setter
    private String startStatus;

    /**
     * 使用性质
     */
    @Getter
    @Setter
    private String usingNature;

    /**
     * 监控对象图片
     */
    @Getter
    @Setter
    private byte[] img;

    public void setIsStart(Integer isStart) {
        if (isStart == 1) {
            this.startStatus = "启用";
        } else {
            this.startStatus = "停用";
        }
        this.isStart = isStart;
    }

    public void setPlateColor(Integer plateColor) {
        this.plateColorStr = PlateColor.getNameOrBlankByCode(String.valueOf(plateColor));
        this.plateColor = plateColor;
    }

}

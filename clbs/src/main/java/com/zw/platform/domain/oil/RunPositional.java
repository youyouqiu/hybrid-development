package com.zw.platform.domain.oil;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import java.util.Objects;

@Data
public class RunPositional {
    /**
     * 监控对象
     */
    @ExcelField(title = "监控对象")
    private String monitorName;

    /**
     * 所属分组
     */
    @ExcelField(title = "所属分组")
    private String assignmentName;

    @ExcelField(title = "行驶时长")
    private String runTime;

    /**
     * 行驶里程
     */
    @ExcelField(title = "行驶里程")
    private String runMile;
    /**
     * 用油量
     */
    @ExcelField(title = "用油量")
    // 计算用油量 该行驶段内第一个点的油量 + 行驶段内的加油量 – 行驶段内漏油量 – 行驶段内最后一个点油量，（主油箱和副油箱之和）
    private String useOil;

    /**
     * 耗油量
     */
    @ExcelField(title = "耗油量")
    private String consumeOil = "0.00";

    /**
     * 行驶开始时间
     */
    @ExcelField(title = "行驶开始时间")
    private String runStartTime;

    /**
     * 行驶开始位置
     */
    @ExcelField(title = "行驶开始位置")
    private String runStartLocation;

    /**
     * 行驶结束时间
     */
    @ExcelField(title = "行驶结束时间")
    private String runEndTime;

    /**
     * 行驶结束位置
     */
    @ExcelField(title = "行驶结束位置")
    private String runEndLocation;

    /**
     * 终端号
     */
    @ExcelField(title = "终端号")
    private String deviceNumber;

    /**
     * 终端手机号
     */
    @ExcelField(title = "终端手机号")
    private String simcardNumber;

    /**
     * ACC状态
     */
    private String acc;

    /**
     * 定位方式
     */
    private String locationType;

    /**
     * 卫星颗数
     */
    private String satellitesNumber;

    /**
     * gps时间
     */
    private long gpsTime;

    private Double runMileTemp;


    /**
     * 计算用油量临时变量
     */
    private Double useOilTemp;

    public void setAcc(String acc) {
        this.acc = "0".equals(acc) ? "关" : "开";
    }

    public void setLocationType(String locationType) {
        String locateMode = "-";
        if (Objects.nonNull(locationType)) {
            switch (locationType) {
                case "0":
                    locateMode = "卫星+基站定位";
                    break;
                case "1":
                    locateMode = "基站定位";
                    break;
                case "2":
                    locateMode = "卫星定位";
                    break;
                case "3":
                    locateMode = "WIFI+基站定位";
                    break;
                case "4":
                    locateMode = "卫星+WIFI+基站定位";
                    break;
                default:
                    locateMode = "-";
                    break;
            }
        }
        this.locationType = locateMode;
    }
}

package com.zw.platform.domain.oil;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;

import java.util.Objects;


@Data
public class StopPositional {
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

    @ExcelField(title = "停止时长")
    private String stopTime;

    /**
     * 停止开始时间
     */
    @ExcelField(title = "停止开始时间")
    private String stopStartTime;

    /**
     * 停止开始位置
     */
    @ExcelField(title = "停止开始位置")
    private String stopStartLocation;

    /**
     * 停止结束时间
     */
    @ExcelField(title = "停止结束时间")
    private String stopEndTime;

    /**
     * 停止结束位置
     */
    @ExcelField(title = "停止结束位置")
    private String stopEndLocation;

    /**
     * 终端号
     */
    @ExcelField(title = "终端号")
    private String deviceNumber;

    /**
     * SIM卡号
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

package com.zw.platform.domain.oil;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

/**
 * 轨迹回放到处全部位置数据
 * 1.
 * @author create by zhouzongbo 2019-07-12
 */
@Data
public class PositionalForm implements Serializable {

    private static final long serialVersionUID = 3550430582342927927L;

    public static final String STOP_STATE = "2";

    public static final String DRIVING_STATE = "1";

    @ExcelField(title = "监控对象")
    private String plateNumber;

    @ExcelField(title = "定位时间")
    private String timeStr;

    @ExcelField(title = "间隔时间")
    private String intervalTimeStr;

    @ExcelField(title = "所属分组")
    private String assignmentName;

    @ExcelField(title = "终端号")
    private String deviceNumber;

    @ExcelField(title = "终端手机号")
    private String simCard;

    /**
     *  行驶状态 1:行驶; 2:停止;
     */
    @ExcelField(title = "状态")
    private String drivingState;

    @ExcelField(title = "ACC状态")
    private String acc;

    @ExcelField(title = "速度")
    private String speed;

    @ExcelField(title = "行车记录仪速度")
    private String recorderSpeed;

    @ExcelField(title = "方向")
    private String angle;

    @ExcelField(title = "总里程")
    private String gpsMile;

    @ExcelField(title = "定位方式")
    private String locationType;

    @ExcelField(title = "卫星颗数")
    private String satelliteNumber;

    @ExcelField(title = "定位状态")
    private String locationStatus;

    @ExcelField(title = "是否补传")
    private String reissue;

    @ExcelField(title = "经度")
    private String longtitude;

    @ExcelField(title = "纬度")
    private String latitude;
    /**
     * 位置
     */
    @ExcelField(title = "位置")
    private String location = "";

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


    public void setDrivingState(String drivingState) {
        this.drivingState = DRIVING_STATE.equals(drivingState) ? "行驶" : "停止";
    }

    public void setAcc(String acc) {
        this.acc = "0".equals(acc) ? "关" : "开";
    }

    public void setLocationStatus(String locationStatus) {
        this.locationStatus = "0".equals(locationStatus) ? "未定位" : "定位";
    }
}

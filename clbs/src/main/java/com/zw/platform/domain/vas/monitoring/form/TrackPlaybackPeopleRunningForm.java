package com.zw.platform.domain.vas.monitoring.form;

import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * Created by wjy on 2017/9/26.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TrackPlaybackPeopleRunningForm extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @ExcelField(title = "监控对象")
    private String vehicleLicense; // 车牌号

    @ExcelField(title = "定位时间")
    private String time; // 时间

    @ExcelField(title = "间隔时间")
    private String intervalTime; // 间隔时间

    @ExcelField(title = "所属分组")
    private String group; // 分组

    @ExcelField(title = "终端ID")
    private String device; // 终端

    @ExcelField(title = "终端手机号")
    private String SIMcard; // SIM

    @ExcelField(title = "电池电压")
    private String batteryVoltage;

    @ExcelField(title = "信号强度")
    private String signalStrength;

    @ExcelField(title = "速度（km/h）")
    private String speed;

    @ExcelField(title = "海拔")
    private String altitude;

    @ExcelField(title = "方向")
    private String direction;

    @ExcelField(title = "补发")
    private String reissue;

    @ExcelField(title = "经度")
    private String latitude;

    @ExcelField(title = "纬度")
    private String longitude;

    @ExcelField(title = "地理位置")
    private String address;
}

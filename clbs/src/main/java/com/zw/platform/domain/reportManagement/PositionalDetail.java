package com.zw.platform.domain.reportManagement;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;

import java.io.Serializable;

/**
 * 位置详情
 * @author zhouzongbo on 2018/12/12 14:02
 */
@Data
public class PositionalDetail implements Serializable {
    private static final long serialVersionUID = 8327922149614113908L;

    @ExcelField(title = "监控对象")
    private String plateNumber;

    @ExcelField(title = "定位时间")
    private String vtimeStr;

    @ExcelField(title = "ACC")
    private String acc = "";

    private String speed = "0";

    /**
     * 里程传感器速度
     */
    private String mileageSpeed = "0";

    @ExcelField(title = "油量(L)")
    private String  totalOilwearOne = "0";

    @ExcelField(title = "位置")
    private String address;

    private Long  vtime = 0L;
    private String gpsMile;//gps里程
    private String status;
    private String mileageTotal;//里程传感器里程

    private byte[] vehicleId;
    private String monitorId;

    private String longtitude;

    private String latitude;
}

package com.zw.platform.domain.singleVehicle;

import com.zw.platform.domain.infoconfig.form.MonitorInfo;
import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;

@Data
public class SingleLocationInfo implements T808MsgBody {

    /**
     * 定位时间(yyMMddHHmmss)
     */
    private String gpsTime;

    /**
     * 系统接收时间(yyMMddHHmmss)
     */
    private String uploadtime;

    /**
     * 方向（0~359），正北为0.0，顺时针
     */
    private Double direction;

    /**
     * 维度
     */
    private Double latitude;
    /**
     * 经度
     */
    private Double longitude;

    /**
     * 定位状态0：未定位；1：定位
     */
    private Integer locationStatus;

    /**
     * 原始纬度
     */
    private Double originalLatitude;
    /**
     * 原始经度
     */
    private Double originalLongitude;

    private Integer stateInfo;//在线状态

    /**
     * 监控信息
     */
    private MonitorInfo monitorInfo;

    /**
     * 地址
     */
    private String positionDescription;

}

package com.zw.platform.domain.oil;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * Created by wjy on 2017/5/10.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class BdtdPosition implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;

    private String peopleId;

    private String altitude;

    private String batteryVoltage;

    private String signalStrength;

    private String bearing;

    private String latitude;

    private String longitudeHemisphere;//经度半球

    private String locationType;//定位类型

    private String protocolType;//协议类型

    private String satellitesNumber;//卫星数量

    private String userCode;//用户识别码

    private String speed;//速度

    private long vtime;//时间

    private String latitudeHemisphere;//纬度半球

    private String longtitude;

    private String monitorObject;//监控对象

    private String groupName;//分组

    private String imei;//终端编号

    private String SIMCard;//sim卡号

    private String formattedAddress;//地址

    private String plateNumber;// 监控对象

    private String deviceNumber;// 设备编号

    private String vehicleId;
}

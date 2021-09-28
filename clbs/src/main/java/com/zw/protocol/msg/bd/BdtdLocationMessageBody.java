package com.zw.protocol.msg.bd;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/5/2.
 */
@Data
public class BdtdLocationMessageBody implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String altitude;//海拔

    private String batteryVoltage;//电池电压

    private String signalStrength;//信号强度

    private String bearing;//方位

    private String latitude;

    private String longitudeHemisphere;//经度半球

    private String locationType;//定位类型

    private String protocolType;//协议类型

    private String satellitesNumber;//卫星数量

    private String userCode;//用户识别码

    private String speed;//速度

    private String vtime;//时间

    private String latitudeHemisphere;//纬度半球

    private String longitude;

    private String monitorObject;//监控对象

    private String group = "123456";//分组

    private String topic;//终端编号

    private String SIMCard = "123123";//sim卡号
    
    private String peopleName; //人员名称

    private int softwareFance;

    private String vehicleId;

    private String formattedAddress;

    private String alarmName;

    private String pushAlarmSet;

    private Integer stateInfo;

}

/**
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved. This software is the confidential and proprietary information
 * of ZhongWei, Inc. You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with ZhongWei.
 */

package com.zw.protocol.msg;

import com.alibaba.fastjson.JSONArray;
import com.zw.platform.domain.basicinfo.PeopleInfo;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.protocol.msg.t808.T808MsgBody;
import com.zw.ws.entity.t808.location.GpsAttachInfo;
import lombok.Data;

import java.util.List;


/**
 * 通用推送CLBS位置信息
 * @param
 */
@Data
public class LocationInfo implements T808MsgBody {

    private static final long serialVersionUID = 1L;

    private Double latitude;// 维度

    private Double longitude;// 经度

    private Double gpsSpeed;// 速度 0.1km/h

    private Double grapherSpeed;// 记录仪速度0.1KM/h

    private Double gpsOil;// GPS油量数据

    private String gpsTime;// 定位时间(yyMMddHHmmss)

    private String time;

    private String uploadtime;// 系统接收时间(yyMMddHHmmss)

    private Integer batteryElectricity;// 电池电量

    private Integer signalStrength;// 信号强度

    private Long signalState;//信号状态

    private Integer locationType = 2;// 定位类型: 1基站定位 2卫星定位 3混合定位(基站定位+WIFI定位)

    private Integer protocolType;// 协议类型，见协议表(2)

    private Integer position;// 定位状态:0未定位1已定位

    private Integer acc;// ACC状态：1开启 0关闭

    private Integer status;// 车辆状态：1停运 0行驶

    private String alarm;// 报警(超速报警,进区域报警...

    private String globalAlarmSet;//全局报警

    private String pushAlarmSet;//局部报警

    private Integer altitude = 0;// 高程（米）

    private Integer direction;// 方向（0~359），正北为0，顺时针

    private JSONArray ioSignalData;// 90IO检测

    private String todayDistance = "0";// 当天累计行程

    private String distance = "0";// 总里程

    private String todayOilExpand = "0";// 当日油耗

    private String totalOilExpand = "0";// 总油耗

    private VehicleInfo vehicleInfo;// 车辆信息

    private PeopleInfo peopleInfo;// 人员信息

    private String alarmName;

    private String swiftNumber; // 流水号

    private String fenceConfigId;// 绑定id

    private String formattedAddress;// 位置信息

    private JSONArray gpsAttachInfoList = new JSONArray();

    private List<GpsAttachInfo> gpsAttachInfos;

    private String fenceName;// 软围栏名称

    private String fenceType;// 软围栏类型

    private Integer alarmSource = 0;// 0终端报警 1平台报警
    
    private JSONArray temperatureSensor;//温度传感器数据
    
    private JSONArray temphumiditySensor;//湿度传感器数据

    private Integer speedAlarmFlag;//超速报警标识（0：开始超速 1：持续超速 2：结束超速 10：开始夜间超速 11：夜间持续超速 12：夜间超速结束）

    private Integer exceptionMoveFlag;//异动报警标识（0：开始异动 1：持续异动 2：结束异动）

}

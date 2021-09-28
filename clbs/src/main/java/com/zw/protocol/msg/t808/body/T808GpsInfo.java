package com.zw.protocol.msg.t808.body;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zw.protocol.msg.t808.T808MsgBody;
import com.zw.ws.entity.adas.RiskCampaign;
import com.zw.ws.entity.common.MileageSensor;
import lombok.Data;


/**
 * Created by LiaoYuecai on 2016/9/2.
 */
@Data
public class T808GpsInfo implements T808MsgBody {

    // private Long alarm = 0l;//报警
    private Long status = 0L;// 状态

    private double latitude;

    private double longitude;

    private Double altitude = 0.0;// 属性

    private double speed = 0;// 速度

    private Double direction = 0.0;// 描述

    private String time = "";// GPS上传时间

    private JSONArray gpsAttachInfoList = new JSONArray();

    private JSONArray oilMass;// 油量信息

    private JSONArray oilExpend;// 油耗信息

    private JSONArray shakeDates;// 振动传感器信息

    private JSONArray positiveNegative;// 正反转传感器信息

    private JSONArray simCrad;// sim卡信息
    // 载重传感器信息
    private JSONArray loadInfos;

    private MileageSensor mileageSensor;// 里程传感器数据

    private JSONArray temperatureSensor;// 温度传感器数据

    private JSONArray temphumiditySensor;// 湿度传感器数据

    private JSONArray ioSignalData;// 车辆 I/O 输入检测

    private String formattedAddress;

    private JSONArray gpsInfos;

    private Long durationTime;//持续时间

    /**
     * adas
     */
    private RiskCampaign riskCampaign = new RiskCampaign();

    private String uploadtime = "";// 上传时间

    private Integer batteryElectricity;// 电池电量

    private Integer batteryVoltage;// 电池电压

    private Integer signalStrength;// 信号强度

    private String terminalVersion;// 终端版本

    private Integer locationType = 2;// 定位类型 2 卫星定位 1 基站定位 3 WIFI定位

    private JSONArray lbsData;// 基站信息

    private JSONArray wifisData;// wifi信息
    // private Integer longstandAlarm;//超长待机报警

    private String pushAlarmSet;// 局部报警

    private String protocolType = "";// 协议类型

    private String vehicleId;

    private Integer softwareFance = 0;

    private String fenceConfigId;// 围栏绑定表id

    private String fenceType;// 围栏类型

    private String alarmName;// 报警名称

    private Integer stateInfo;//在线状态
    
    // 原始经纬度
    private double originalLatitude;

    private double originalLongitude;

    //原始地址
    private byte [] original;

    /**
     * 胎压数据
     */
    private JSONObject tyreInfos;

    /**
     * 电量检测数据
     */
    private JSONObject electricityCheck;

    /**
     * 工时传感器
     */
    private JSONArray workHourSensor;

    /**
     * 卫星数量
     */
    private Integer satellitesNumber;

}

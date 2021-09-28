package com.zw.protocol.msg.t808.body;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zw.platform.domain.infoconfig.form.MonitorInfo;
import com.zw.platform.util.ConstantUtil;
import com.zw.protocol.msg.t808.T808MsgBody;
import com.zw.ws.entity.common.MileageSensor;
import lombok.Data;
import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.util.List;

/**
 * 通用推送CLBS位置信息
 */
@Data
public class LocationInfo implements T808MsgBody {
    private static final long serialVersionUID = 1L;

    /**
     * 报警流水号
     */
    private String swiftNumber;
    /**
     * 报警 更加不同的协议存在不同的按位处理
     */
    private Long alarm;
    /**
     * 状态 根据不同协议存在不同的按位存储
     */
    private Long status;
    /**
     * 持续时长
     */
    private Long durationTime;
    /**
     * ACC状态：1开启 0关闭
     */
    private Integer acc;

    /**
     * 定位状态0：未定位；1：定位
     */
    private Integer locationStatus;
    /**
     * 运营状态 0：运营状态； 1：停运状态
     */
    private Integer operationStatus;
    /**
     * 油量状态: 0：车辆油路正常； 1：车辆油路断开
     */
    private Integer oilWayStatus;
    /**
     * 电路状态: 0：车辆电路正常； 1：车辆电路断开
     */
    private Integer circuitryStatus;
    /**
     * 车辆加锁状态: 0：车门解锁； 1：车门加锁
     */
    private Integer carDoorLock;
    /**
     * 空压机状态
     */
    private Integer airCompStatus;

    /**
     * 维度
     */
    private Double latitude;
    /**
     * 经度
     */
    private Double longitude;

    /**
     * 原始纬度
     */
    private Double originalLatitude;
    /**
     * 原始经度
     */
    private Double originalLongitude;

    /**
     * 速度 0.1km/h
     */
    private Double gpsSpeed;

    /**
     * 速度取值来源 0:终端; 1:里程传感器;
     */
    private Integer speedValueSource = 0;
    /**
     * 记录仪速度0.1KM/h
     */
    private Double grapherSpeed;
    /**
     * GPS油量数据
     */
    private Double gpsOil;
    /**
     * GPS里程数据
     */
    private Double gpsMileage;
    /**
     * 定位时间(yyMMddHHmmss)
     */
    private String gpsTime;
    /**
     * 系统接收时间(yyMMddHHmmss)
     */
    private String uploadtime;
    /**
     * 电池电量
     */
    private Integer batteryElec;
    /**
     * 电池电压
     */
    private Double batteryVoltage;
    /**
     * 信号强度
     */
    private Integer signalStrength;
    private Long signalState;//信号状态

    /**
     * 终端版本
     */
    private String terminalVersion;

    /**
     * 卫星数量
     */
    private Integer satellitesNumber;

    /**
     * APP2.0.0新增
     * 定位模式
     * 0：卫星+基站定位
     * 1：基站 定位
     * 2：卫星定位
     * 3：WIFI+基站定位
     * 4：卫星+WIFI+基站定位
     */
    private Integer locationPattern;

    /**
     * APP2.0.0新增
     * WIFI定位强度
     */
    private Integer wifiSignalStrength;

    /**
     * APP2.0.0新增
     * 信号类型 1: GPS 2: LBS 3: WIFI 4: 2G 5: 3G 6: 4G 7: 5G
     */
    private Integer signalType;

    /**
     * 用户识别码
     */
    private String userCode;

    /**
     * 信息内容
     */
    private String message;

    /**
     * 定位类型: 2基站定位 1卫星定位 3混合定位(基站定位+WIFI定位)
     */
    private Integer locationType = 1;

    private JSONArray oilMass;// 油量信息

    private JSONArray oilExpend;// 油耗信息

    private JSONArray temperatureSensor;// 温度传感器数据

    private JSONArray temphumiditySensor;// 湿度传感器数据

    private MileageSensor mileageSensor;// 里程传感器数据

    private JSONArray workHourSensor;// 工时传感器

    private JSONArray positiveNegative;// 正反转传感器

    private JSONArray cirIoCheckData;// 外接IO传感器

    /**
     * 载重信息
     */
    private JSONArray loadsInfo;

    /**
     * 协议类型，见协议表(2)
     */
    private String protocolType;

    /**
     * 0终端报警 1平台报警
     */
    private Integer alarmSource = 0;
    /**
     * 高程（米）
     */
    private Double altitude = 0.0;
    /**
     * 方向（0~359），正北为0.0，顺时针
     */
    private Double direction;
    /**
     * 90IO检测
     */
    private JSONArray ioSignalData;

    /**
     * 全局报警
     */
    private String globalAlarmSet;
    /**
     * 局部报警集合
     */
    private String pushAlarmSet;
    /**
     * 监控信息
     */
    private MonitorInfo monitorInfo;

    private Integer stateInfo;//在线状态

    /**
     * 当日里程
     */
    private String dayMileage;

    /**
     * 当日累计里程，由flink计算，每条位置信息都包含此字段
     */
    private String deviceDayMile;

    /**
     * 当日累计里程，由flink计算，每条位置信息都包含此字段
     */
    private String sensorDayMile;

    /**
     * 传感器当日里程
     */
    private String dayMileageSensor;

    /**
     * 当日油耗
     */
    private String dayOilWear;

    /**
     * 地址
     */
    private String positionDescription;

    private String fenceConfigId; // 软围栏id

    private String fenceName;// 软围栏名称

    private String fenceType;// 软围栏类型

    private String alarmName;

    private String formattedAddress;// 位置信息

    /**
     * 总里程
     */
    private String distance;

    private String msgSNAck; // 流水号(0x0201)

    private Integer speedAlarmFlag;//超速报警标识（0：开始超速 1：持续超速 2：结束超速 10：开始夜间超速 11：夜间持续超速 12：夜间超速结束）

    private Integer exceptionMoveFlag;//异动报警标识（0：开始异动 1：持续异动 2：结束异动）

    /**
     * OBD信息
     */
    private JSONObject obd;
    private String obdObjStr;
    private Long earlyAlarmStartTime;
    private String earlyAlarmStartTimeStr;

    /**
     * 车辆附加信息
     */
    private JSONArray gpsAttachInfoList;

    /**
     * 胎压数据
     */
    private JSONObject tyreInfos;

    /**
     * 电量检测数据
     */
    private JSONObject elecData;

    /**
     * 载重传感器信息
     */
    private JSONArray loadInfos;

    /**
     * APP2.0.0新增
     * 基站信息
     */
    private JSONArray lbsData;
    /**
     * APP2.0.0新增
     * wifi信息
     */
    private JSONArray wifisData;

    /**
     * 终端检测信息
     */
    private JSONObject terminalcheck;

    private JSONArray alarmTempList = new JSONArray();//报警温度值

    private JSONArray alarmWetnessList = new JSONArray();//报警湿度值

    /**
     * 主干4.1.1山东报表需要：终端报警类型和时间集合
     */
    private List<String> alarmStartTimeList;

    private Integer roadType;

    private Integer roadLimitSpeed;

    private String roadTypeStr;

    private String speed;

    private String recorderSpeed;

    /**
     * 车架号
     */
    private String frameNumber;

    /**
     * 车架号(终端上传)
     */
    private String frameNumberFromDevice;

    public String getRoadTypeStr() {

        return ConstantUtil.getRoadName(roadType);

    }

    /**
     * 调度报警开始时间
     */
    private Long dispatchAlarmStartTime;

    public long getLastLocationSecond() {
        long lastLocationSecond;
        try {
            lastLocationSecond = DateUtils.parseDate("20" + gpsTime, "yyyyMMddHHmmss").getTime() / 1000;
        } catch (ParseException e) {
            lastLocationSecond = 0;
        }
        return lastLocationSecond;
    }
}

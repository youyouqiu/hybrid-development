package com.zw.platform.util.common;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.zw.app.domain.alarm.AlarmTypePosEnum;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.domain.vas.alram.AlarmType;
import com.zw.platform.util.StringUtil;
import com.zw.protocol.util.ProtocolTypeUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 告警类型获取类
 */
public class AlarmTypeUtil {

    /**
     * io报警
     */
    public static final List<Integer> IO_ALARM = Arrays
        .asList(14100, 14101, 14102, 14103, 14104, 14105, 14106, 14107, 14108, 14109, 14110, 14111, 14112, 14113, 14114,
            14115, 14116, 14117, 14118, 14119, 14120, 14121, 14122, 14123, 14124, 14125, 14126, 14127, 14128, 14129,
            14130, 14131, 14200, 14201, 14202, 14203, 14204, 14205, 14206, 14207, 14208, 14209, 14210, 14211, 14212,
            14213, 14214, 14215, 14216, 14217, 14218, 14219, 14220, 14221, 14222, 14223, 14224, 14225, 14226, 14227,
            14228, 14229, 14230, 14231, 14000, 14001, 14002, 14003, 14004, 141000, 142000);

    public static final List<Integer> DISPATCH_ALARM_LIST = Arrays.asList(152, 153, 154, 155, 156);

    public static final List<String> DISPATCH_ALARM_NAME_LIST =
        Arrays.asList("上班未到岗", "上班离岗", "超时长停留", "任务未到岗", "任务离岗");

    public static final Map<String, String> alarmMap = new LinkedHashMap<>();

    public static final Map<String, String> temperatureAndHumidityBriefAlarmType = new LinkedHashMap<>();

    public static final Map<String, String> instructAlarmMap = new HashMap<>();

    /**
     * 0x0104中 0x0050报警屏蔽解析map
     * 标识位 -> 协议类型("1":808-2013; "11":808-2019) -> alarmParameterId
     */
    public static final Map<Integer, Map<String, String>> deviceAlarmMap = new HashMap<>();

    public static final Map<Integer, Integer> IO_0X90_ALARM = new HashMap<>();

    public static final Map<Integer, Integer> IO_0X91_ALARM = new HashMap<>();

    public static final Map<Integer, Integer> IO_0X92_ALARM = new HashMap<>();

    static {
        alarmMap.put("0", "紧急报警");
        alarmMap.put("1", "超速报警");
        alarmMap.put("2", "疲劳驾驶");
        alarmMap.put("3", "危险预警");
        alarmMap.put("4", "GNSS 模块发生故障");
        alarmMap.put("5", "GNSS 天线未接或被剪断");
        alarmMap.put("6", "GNSS 天线短路");
        alarmMap.put("7", "终端主电源欠压 ");
        alarmMap.put("8", "终端主电源掉电");
        alarmMap.put("9", "终端 LCD 或显示器故障");
        alarmMap.put("10", "TTS 模块故障");
        alarmMap.put("11", "摄像头故障 ");
        alarmMap.put("12", "道路运输证 IC 卡模块故障");
        alarmMap.put("13", "超速预警");
        alarmMap.put("14", "疲劳驾驶预警 ");
        /*alarmMap.put("15", "保留");
        alarmMap.put("16", "保留");
        alarmMap.put("17", "保留");*/
        alarmMap.put("18", "当天累计驾驶超时 ");
        alarmMap.put("19", "超时停车");
        alarmMap.put("20", "进出区域");
        alarmMap.put("21", "进出路线 ");
        alarmMap.put("22", "路段行驶时间不足/过长");
        alarmMap.put("23", "路线偏离报警");
        alarmMap.put("24", "车辆 VSS 故障");
        alarmMap.put("25", "车辆油量异常 ");
        alarmMap.put("26", "车辆被盗(通过车辆防盗器) ");
        alarmMap.put("27", "车辆非法点火");
        alarmMap.put("28", "车辆非法位移");
        alarmMap.put("29", "碰撞预警");
        alarmMap.put("30", "侧翻预警 ");
        alarmMap.put("31", "非法开门报警");

        // 特殊报警
        alarmMap.put("32", "视频信号丢失报警");
        alarmMap.put("33", "视频信号遮挡报警");
        alarmMap.put("34", "存储单元故障报警");
        alarmMap.put("35", "其他视频设备故障报警");
        alarmMap.put("36", "客车超员报警");
        alarmMap.put("37", "异常驾驶行为报警");
        alarmMap.put("38", "特殊报警录像达到存储阈值报警");
        //高温
        temperatureAndHumidityBriefAlarmType.put("6511", "651");
        temperatureAndHumidityBriefAlarmType.put("6521", "651");
        temperatureAndHumidityBriefAlarmType.put("6531", "651");
        temperatureAndHumidityBriefAlarmType.put("6541", "651");
        temperatureAndHumidityBriefAlarmType.put("6551", "651");
        //低温
        temperatureAndHumidityBriefAlarmType.put("6512", "652");
        temperatureAndHumidityBriefAlarmType.put("6522", "652");
        temperatureAndHumidityBriefAlarmType.put("6532", "652");
        temperatureAndHumidityBriefAlarmType.put("6542", "652");
        temperatureAndHumidityBriefAlarmType.put("6552", "652");
        //温度传感器异常
        temperatureAndHumidityBriefAlarmType.put("6513", "653");
        temperatureAndHumidityBriefAlarmType.put("6523", "653");
        temperatureAndHumidityBriefAlarmType.put("6533", "653");
        temperatureAndHumidityBriefAlarmType.put("6543", "653");
        temperatureAndHumidityBriefAlarmType.put("6553", "653");
        //高湿度
        temperatureAndHumidityBriefAlarmType.put("6611", "661");
        temperatureAndHumidityBriefAlarmType.put("6621", "661");
        temperatureAndHumidityBriefAlarmType.put("6631", "661");
        temperatureAndHumidityBriefAlarmType.put("6641", "661");
        temperatureAndHumidityBriefAlarmType.put("6651", "661");
        //低湿度
        temperatureAndHumidityBriefAlarmType.put("6612", "662");
        temperatureAndHumidityBriefAlarmType.put("6622", "662");
        temperatureAndHumidityBriefAlarmType.put("6632", "662");
        temperatureAndHumidityBriefAlarmType.put("6642", "662");
        temperatureAndHumidityBriefAlarmType.put("6652", "662");
        //湿度传感器异常
        temperatureAndHumidityBriefAlarmType.put("6613", "663");
        temperatureAndHumidityBriefAlarmType.put("6623", "663");
        temperatureAndHumidityBriefAlarmType.put("6633", "663");
        temperatureAndHumidityBriefAlarmType.put("6643", "663");
        temperatureAndHumidityBriefAlarmType.put("6653", "663");
        // 加油报警
        temperatureAndHumidityBriefAlarmType.put("6811", "681");
        temperatureAndHumidityBriefAlarmType.put("6821", "681");
        temperatureAndHumidityBriefAlarmType.put("6831", "681");
        temperatureAndHumidityBriefAlarmType.put("6841", "681");
        // 漏油报警
        temperatureAndHumidityBriefAlarmType.put("6812", "682");
        temperatureAndHumidityBriefAlarmType.put("6822", "682");
        temperatureAndHumidityBriefAlarmType.put("6832", "682");
        temperatureAndHumidityBriefAlarmType.put("6842", "682");
        // 载重传感器异常
        temperatureAndHumidityBriefAlarmType.put("7011", "701");
        temperatureAndHumidityBriefAlarmType.put("7021", "701");
        // 工时传感器异常
        temperatureAndHumidityBriefAlarmType.put("13213", "1321");
        temperatureAndHumidityBriefAlarmType.put("13214", "1321");
        // 超载报警
        temperatureAndHumidityBriefAlarmType.put("7012", "702");
        temperatureAndHumidityBriefAlarmType.put("7022", "702");
        // 超速预警
        instructAlarmMap.put("0x005B", "5b9b15ce-bc26-11e6-a4a6-cec0c932ce01");
        // 疲劳驾驶预警
        instructAlarmMap.put("0x005C", "5b9b17c2-bc26-11e6-a4a6-cec0c932ce01");
        // 碰撞预警: 碰撞时间，单位：毫秒
        instructAlarmMap.put("0x005Dtime1", "5b9b195c-bc26-11e6-a4a6-cec0c932ce01");
        // 碰撞预警: 碰撞加速度，单位：0.1g，范围：0-79
        instructAlarmMap.put("0x005Dspeed1", "5b9b208c-bc26-11e6-a4a6-cec0c932ce01");
        // 侧翻预警
        instructAlarmMap.put("0x005E", "5b9b229e-bc26-11e6-a4a6-cec0c932ce01");
        // 车辆非法位移
        instructAlarmMap.put("0x0031", "5b9b3e14-bc26-11e6-a4a6-cec0c932ce01");
        // 超速报警 最高速度，
        instructAlarmMap.put("0x0055", "5b9b465c-bc26-11e6-a4a6-cec0c932ce01");
        // 超速报警: 超速持续时间，单位：秒
        instructAlarmMap.put("0x0056", "5b9b4774-bc26-11e6-a4a6-cec0c932ce01");
        // 疲劳驾驶: 连续驾驶时间门限，单位：秒
        instructAlarmMap.put("0x0057", "5b9b483c-bc26-11e6-a4a6-cec0c932ce01");
        // 疲劳驾驶: 最小休息时间，单位：秒
        instructAlarmMap.put("0x0059", "5b9b4904-bc26-11e6-a4a6-cec0c932ce01");
        // 当天累积驾驶超时
        instructAlarmMap.put("0x0058", "5b9b49cc-bc26-11e6-a4a6-cec0c932ce01");
        // 超时停车: 0x005A
        instructAlarmMap.put("0x005A", "5b9b4a94-bc26-11e6-a4a6-cec0c932ce01");
        // 碰撞侧翻报警(808-2019): 碰撞时间，单位：毫秒
        instructAlarmMap.put("0x005Dtime11", "8b75eedf-7baa-11e9-9de7-000c294a3301");
        // 碰撞侧翻报警: 碰撞加速度，单位：0.1g，范围：0-79
        instructAlarmMap.put("0x005Dspeed11", "8b783fcd-7baa-11e9-9de7-000c294a3301");
        // 违规行驶报警
        instructAlarmMap.put("0x0032", "bcaa98cc-667f-11e9-9cb1-000c297cf508");

        // 0: 紧急报警 : 5b9b4184-bc26-11e6-a4a6-cec0c932ce01
        // 1: 超速报警 : 5b9b465c-bc26-11e6-a4a6-cec0c932ce01,5b9b4774-bc26-11e6-a4a6-cec0c932ce01
        // 2: 疲劳驾驶 : 5b9b483c-bc26-11e6-a4a6-cec0c932ce01,5b9b4904-bc26-11e6-a4a6-cec0c932ce01
        // 3: 危险预警: 5b9b1006-bc26-11e6-a4a6-cec0c932ce01
        // 4: GNSS 模块发生故障: 5b9b247e-bc26-11e6-a4a6-cec0c932ce01
        deviceAlarmMap.put(0,
            ImmutableMap.of("1", "5b9b4184-bc26-11e6-a4a6-cec0c932ce01", "11", "5b9b4184-bc26-11e6-a4a6-cec0c932ce01"));
        deviceAlarmMap.put(1,
            ImmutableMap.of("1", "5b9b465c-bc26-11e6-a4a6-cec0c932ce01,5b9b4774-bc26-11e6-a4a6-cec0c932ce01",
                "11", "5b9b465c-bc26-11e6-a4a6-cec0c932ce01,5b9b4774-bc26-11e6-a4a6-cec0c932ce01"));
        deviceAlarmMap.put(2,
            ImmutableMap.of("1", "5b9b483c-bc26-11e6-a4a6-cec0c932ce01,5b9b4904-bc26-11e6-a4a6-cec0c932ce01",
                "11", "5b9b483c-bc26-11e6-a4a6-cec0c932ce01,5b9b4904-bc26-11e6-a4a6-cec0c932ce01"));
        deviceAlarmMap.put(3,
            ImmutableMap.of("1", "5b9b1006-bc26-11e6-a4a6-cec0c932ce01", "11", "5b9b1006-bc26-11e6-a4a6-cec0c932ce01"));
        deviceAlarmMap.put(4,
            ImmutableMap.of("1", "5b9b247e-bc26-11e6-a4a6-cec0c932ce01", "11", "5b9b247e-bc26-11e6-a4a6-cec0c932ce01"));
        // 5 1：GNSS天线未接或被剪断: 5b9b2636-bc26-11e6-a4a6-cec0c932ce01
        // 6 1：GNSS天线短路: 5b9b279e-bc26-11e6-a4a6-cec0c932ce01
        // 7 1：终端主电源欠压: 5b9b2cc6-bc26-11e6-a4a6-cec0c932ce01
        // 8 1：终端主电源掉电: 5b9b2e24-bc26-11e6-a4a6-cec0c932ce01
        // 9 1：终端LCD或显示器故障: 5b9b2f00-bc26-11e6-a4a6-cec0c932ce01
        deviceAlarmMap.put(5,
            ImmutableMap.of("1", "5b9b2636-bc26-11e6-a4a6-cec0c932ce01", "11", "5b9b2636-bc26-11e6-a4a6-cec0c932ce01"));
        deviceAlarmMap.put(6,
            ImmutableMap.of("1", "5b9b279e-bc26-11e6-a4a6-cec0c932ce01", "11", "5b9b279e-bc26-11e6-a4a6-cec0c932ce01"));
        deviceAlarmMap.put(7,
            ImmutableMap.of("1", "5b9b2cc6-bc26-11e6-a4a6-cec0c932ce01", "11", "5b9b2cc6-bc26-11e6-a4a6-cec0c932ce01"));
        deviceAlarmMap.put(8,
            ImmutableMap.of("1", "5b9b2e24-bc26-11e6-a4a6-cec0c932ce01", "11", "5b9b2e24-bc26-11e6-a4a6-cec0c932ce01"));
        deviceAlarmMap.put(9,
            ImmutableMap.of("1", "5b9b2f00-bc26-11e6-a4a6-cec0c932ce01", "11", "5b9b2f00-bc26-11e6-a4a6-cec0c932ce01"));
        // 10 1：TTS模块故障: 5b9b2fbe-bc26-11e6-a4a6-cec0c932ce01
        // 11 1：摄像头故障: 5b9b3086-bc26-11e6-a4a6-cec0c932ce01
        // 12 1：道路运输证IC卡模块故障: 5b9b314e-bc26-11e6-a4a6-cec0c932ce01
        // 13 1：超速预警: 5b9b15ce-bc26-11e6-a4a6-cec0c932ce01
        deviceAlarmMap.put(10,
            ImmutableMap.of("1", "5b9b2fbe-bc26-11e6-a4a6-cec0c932ce01", "11", "5b9b2fbe-bc26-11e6-a4a6-cec0c932ce01"));
        deviceAlarmMap.put(11,
            ImmutableMap.of("1", "5b9b3086-bc26-11e6-a4a6-cec0c932ce01", "11", "5b9b3086-bc26-11e6-a4a6-cec0c932ce01"));
        deviceAlarmMap.put(12,
            ImmutableMap.of("1", "5b9b314e-bc26-11e6-a4a6-cec0c932ce01", "11", "5b9b314e-bc26-11e6-a4a6-cec0c932ce01"));
        deviceAlarmMap.put(13,
            ImmutableMap.of("1", "5b9b15ce-bc26-11e6-a4a6-cec0c932ce01", "11", "5b9b15ce-bc26-11e6-a4a6-cec0c932ce01"));

        // 14 1：疲劳驾驶预警: 5b9b17c2-bc26-11e6-a4a6-cec0c932ce01
        // 15 1: 违规行驶报警: bcaa98cc-667f-11e9-9cb1-000c297cf508
        // 16 1: 胎压异常报警: bcbcc5a6-667f-11e9-9cb1-000c297cf508
        // 17 1: 右转盲区异常报警: bcc1affe-667f-11e9-9cb1-000c297cf508
        // 18 1：当天累计驾驶超时: 5b9b49cc-bc26-11e6-a4a6-cec0c932ce01
        // 19 1：超时停车: 5b9b4a94-bc26-11e6-a4a6-cec0c932ce01
        // 20 1：进出区域: 5b9b4e36-bc26-11e6-a4a6-cec0c932ce03
        deviceAlarmMap.put(14,
            ImmutableMap.of("1", "5b9b17c2-bc26-11e6-a4a6-cec0c932ce01", "11", "5b9b17c2-bc26-11e6-a4a6-cec0c932ce01"));
        deviceAlarmMap.put(15,
            ImmutableMap.of("1", "bcaa98cc-667f-11e9-9cb1-000c297cf508", "11", "bcaa98cc-667f-11e9-9cb1-000c297cf508"));
        deviceAlarmMap.put(16,
            ImmutableMap.of("1", "bcbcc5a6-667f-11e9-9cb1-000c297cf508", "11", "bcbcc5a6-667f-11e9-9cb1-000c297cf508"));
        deviceAlarmMap.put(17,
            ImmutableMap.of("1", "bcc1affe-667f-11e9-9cb1-000c297cf508", "11", "bcc1affe-667f-11e9-9cb1-000c297cf508"));
        deviceAlarmMap.put(18,
            ImmutableMap.of("1", "5b9b49cc-bc26-11e6-a4a6-cec0c932ce01", "11", "5b9b49cc-bc26-11e6-a4a6-cec0c932ce01"));
        deviceAlarmMap.put(19,
            ImmutableMap.of("1", "5b9b4a94-bc26-11e6-a4a6-cec0c932ce01", "11", "5b9b4a94-bc26-11e6-a4a6-cec0c932ce01"));
        deviceAlarmMap.put(20,
            ImmutableMap.of("1", "5b9b4e36-bc26-11e6-a4a6-cec0c932ce03", "11", "5b9b4e36-bc26-11e6-a4a6-cec0c932ce03"));
        // 21 1：进出路线: 5b9b4fbc-bc26-11e6-a4a6-cec0c932ce03
        // 22 1：路段行驶时间不足/过长: 5b9b507a-bc26-11e6-a4a6-cec0c932ce03
        // 23 1：路线偏离报警: 5b9b53fe-bc26-11e6-a4a6-cec0c932ce01
        // 24 1：车辆VSS故障: 5b9b32d4-bc26-11e6-a4a6-cec0c932ce01
        // 25 1：车辆油量异常: 5b9b3900-bc26-11e6-a4a6-cec0c932ce01
        deviceAlarmMap.put(21,
            ImmutableMap.of("1", "5b9b4fbc-bc26-11e6-a4a6-cec0c932ce03", "11", "5b9b4fbc-bc26-11e6-a4a6-cec0c932ce03"));
        deviceAlarmMap.put(22,
            ImmutableMap.of("1", "5b9b507a-bc26-11e6-a4a6-cec0c932ce03", "11", "5b9b507a-bc26-11e6-a4a6-cec0c932ce03"));
        deviceAlarmMap.put(23,
            ImmutableMap.of("1", "5b9b53fe-bc26-11e6-a4a6-cec0c932ce01", "11", "5b9b53fe-bc26-11e6-a4a6-cec0c932ce01"));
        deviceAlarmMap.put(24,
            ImmutableMap.of("1", "5b9b32d4-bc26-11e6-a4a6-cec0c932ce01", "11", "5b9b32d4-bc26-11e6-a4a6-cec0c932ce01"));
        deviceAlarmMap.put(25,
            ImmutableMap.of("1", "5b9b3900-bc26-11e6-a4a6-cec0c932ce01", "11", "5b9b3900-bc26-11e6-a4a6-cec0c932ce01"));

        // 26 1：车辆被盗(通过车辆防盗器): 5b9b3af4-bc26-11e6-a4a6-cec0c932ce01
        // 27 1：车辆非法点火: 5b9b3ce8-bc26-11e6-a4a6-cec0c932ce01
        // 28 1：车辆非法位移: 5b9b3e14-bc26-11e6-a4a6-cec0c932ce01
        // 29 1：碰撞预警: 808-2013: 5b9b195c-bc26-11e6-a4a6-cec0c932ce01,5b9b208c-bc26-11e6-a4a6-cec0c932ce01
        //                800-2019: 8b75eedf-7baa-11e9-9de7-000c294a3301,8b783fcd-7baa-11e9-9de7-000c294a3301
        // 30 1：侧翻预警: 5b9b229e-bc26-11e6-a4a6-cec0c932ce01
        // 31 1：非法开门报警（终端未设置区域时，不判断非法开门）: 5b9b3f5e-bc26-11e6-a4a6-cec0c932ce01
        deviceAlarmMap.put(26,
            ImmutableMap.of("1", "5b9b3af4-bc26-11e6-a4a6-cec0c932ce01", "11", "5b9b3af4-bc26-11e6-a4a6-cec0c932ce01"));
        deviceAlarmMap.put(27,
            ImmutableMap.of("1", "5b9b3ce8-bc26-11e6-a4a6-cec0c932ce01", "11", "5b9b3ce8-bc26-11e6-a4a6-cec0c932ce01"));
        deviceAlarmMap.put(28,
            ImmutableMap.of("1", "5b9b3e14-bc26-11e6-a4a6-cec0c932ce01", "11", "5b9b3e14-bc26-11e6-a4a6-cec0c932ce01"));
        deviceAlarmMap.put(29,
            ImmutableMap.of("1", "5b9b195c-bc26-11e6-a4a6-cec0c932ce01,5b9b208c-bc26-11e6-a4a6-cec0c932ce01",
                "11", "8b75eedf-7baa-11e9-9de7-000c294a3301,8b783fcd-7baa-11e9-9de7-000c294a3301"));
        deviceAlarmMap.put(30,
            ImmutableMap.of("1", "5b9b229e-bc26-11e6-a4a6-cec0c932ce01", "11", "5b9b229e-bc26-11e6-a4a6-cec0c932ce01"));
        deviceAlarmMap.put(31,
            ImmutableMap.of("1", "5b9b3f5e-bc26-11e6-a4a6-cec0c932ce01", "11", "5b9b3f5e-bc26-11e6-a4a6-cec0c932ce01"));
        // 组装I/O报警
        Integer io90Index = 14000;
        Integer io91Index = 14100;
        Integer io92Index = 14200;
        for (int indexOne = 0; indexOne < 32; indexOne++) {
            if (indexOne < 4) {
                IO_0X90_ALARM.put(indexOne, io90Index + indexOne);
            }
            IO_0X91_ALARM.put(indexOne, io91Index + indexOne);
            IO_0X92_ALARM.put(indexOne, io92Index + indexOne);
        }
    }

    public static String getAlarmName(String alarmNumber) {
        String result;
        StringBuilder alarmNames = new StringBuilder();
        String binaryNumber = new BigInteger(alarmNumber).toString(2);
        int len = binaryNumber.length() - 1;
        for (int i = len; i >= 0; i--) {
            String value = binaryNumber.charAt(len - i) + "";
            if ("1".equals(value)) {
                String alarmType = alarmMap.get(i + "");
                if (alarmType != null) {
                    alarmNames.append(alarmType).append(",");
                }

            }

        }
        if (alarmNames.length() > 0) {
            result = alarmNames.toString().substring(0, alarmNames.length() - 1);
        } else {
            result = alarmNames.toString();
        }
        return result;

    }

    /* T808报警 */
    private static final Map<Integer, String> T_808_ALARM_TYPE = Maps.newHashMap();

    /* 特殊报警 */
    private static final Map<Integer, String> SPECIAL_ALARM_TYPE = Maps.newHashMap();

    private static final String T_808_ALARM_TYPE_STR = "紧急报警,超速报警,疲劳驾驶,危险预警,GNSS模块发生故障,GNSS天线未接或被剪断"
        + ",GNSS天线短路,终端主电源欠压,终端主电源掉电,终端LCD或显示器故障,TTS模块故障,摄像头故障,道路运输证IC卡模块故障"
        + ",超速预警,疲劳驾驶预警,,,,当天累计驾驶超时,超时停车,进出区域报警,进出路线报警,路段行驶时间不足/过长,路线偏离报警,车辆VSS故障"
        + ",车辆油量异常,车辆被盗,车辆非法点火,车辆非法位移,碰撞预警,侧翻预警,非法开门报警";

    private static final String SPECIAL_ALARM_TYPE_STR =
        "视频信号丢失报警,视频信号遮挡报警,存储单元故障报警,其他视频设备故障报警,客车超员报警,异常驾驶行为报警,特殊报警录像达到存储阈值报警,预留,预留,"
            + ",预留,预留,预留,预留,预留,预留,预留,预留,预留,预留,预留,预留,预留,预留,预留,预留,预留,预留,预留,预留,预留,预留";

    static {
        String[] t808AlarmType = T_808_ALARM_TYPE_STR.split(",");
        String[] specialAlarmType = SPECIAL_ALARM_TYPE_STR.split(",");
        for (int i = 0; i < t808AlarmType.length; i++) {
            T_808_ALARM_TYPE.put(i, t808AlarmType[i]);
            SPECIAL_ALARM_TYPE.put(i, specialAlarmType[i]);
        }
    }

    public static String getAlarmName(int num, String type, String fileName) {
        StringBuilder builder = new StringBuilder("");
        Map<Integer, String> tempMap;
        if ("t808Alarm".equals(type)) {
            tempMap = T_808_ALARM_TYPE;
        } else {
            tempMap = SPECIAL_ALARM_TYPE;
        }
        for (int i = 0; i < 32; i++) {
            if ((int) (num & (long) Math.pow(2, i)) > 0) {
                if (!"预留".equals(tempMap.get(i))) {
                    builder.append(tempMap.get(i) + ",");
                }
            }
        }
        String str = builder.toString();
        if (str.contains(",")) {
            str = str.substring(0, builder.toString().lastIndexOf(","));
        }
        if (!StringUtils.isEmpty(fileName)) {
            str = fileName + "," + str;
        }
        return str;
    }

    public static Map<Integer, String> getT808AlarmTypeMap() {
        return T_808_ALARM_TYPE;
    }

    public static Map<Integer, String> getSpecialAlarmTypeMap() {
        return SPECIAL_ALARM_TYPE;
    }

    public static String getAlarmType(Integer alarmCode) {
        return getAlarmType(String.valueOf(alarmCode));
    }

    public static String getAlarmType(String alarmCode) {
        RedisKey redisKey = HistoryRedisKeyEnum.ALARM_TYPE_INFO.of(alarmCode);
        String alarmName = "";
        // 从缓存中获取报警数据字典
        String redisMessage = RedisHelper.getString(redisKey);
        AlarmType alarmType = JSONObject.parseObject(redisMessage, AlarmType.class);
        if (alarmType != null) {
            alarmName = alarmType.getName();
        }
        return alarmName;
    }

    public static Map<Integer, String> getAlarmType(Collection<Integer> alarmCodes) {

        if (CollectionUtils.isEmpty(alarmCodes)) {
            return Collections.emptyMap();
        }
        List<String> alarmCodeList = alarmCodes.stream().map(String::valueOf).collect(Collectors.toList());
        List<RedisKey> redisKeys = HistoryRedisKeyEnum.ALARM_TYPE_INFO.ofs(alarmCodeList);
        List<String> alarmTypes = RedisHelper.batchGetString(redisKeys);
        final int length = Math.min(redisKeys.size(), alarmTypes.size());
        final Map<Integer, String> codeNameMap = new HashMap<>((int) (length / .75 + 1));
        for (int i = 0; i < length; i++) {
            final String alarmTypeJson = alarmTypes.get(i);
            if (StringUtils.isNotEmpty(alarmTypeJson)) {
                JSONObject jsonObject = JSONObject.parseObject(alarmTypeJson);
                final Integer code = jsonObject.getInteger("pos");
                final String name = jsonObject.getString("name");
                codeNameMap.put(code, name);
            }
        }
        return codeNameMap;
    }

    public static Map<String, String> getAlarmTypeString(Collection<String> alarmCodes) {
        if (CollectionUtils.isEmpty(alarmCodes)) {
            return Collections.emptyMap();
        }
        List<String> alarmCodeList = alarmCodes.stream().map(String::valueOf).collect(Collectors.toList());
        List<RedisKey> redisKeys = HistoryRedisKeyEnum.ALARM_TYPE_INFO.ofs(alarmCodeList);
        List<String> alarmTypes = RedisHelper.batchGetString(redisKeys);
        final int length = Math.min(redisKeys.size(), alarmTypes.size());
        final Map<String, String> codeNameMap = new HashMap<>((int) (length / .75 + 1));
        for (int i = 0; i < length; i++) {
            final String alarmTypeJson = alarmTypes.get(i);
            if (StringUtils.isNotEmpty(alarmTypeJson)) {
                JSONObject jsonObject = JSONObject.parseObject(alarmTypeJson);
                final String code = jsonObject.getString("pos");
                final String name = jsonObject.getString("name");
                codeNameMap.put(code, name);
            }
        }
        return codeNameMap;
    }

    /**
     * alarmType的标识开始位,如有类似模块,需要增到该数组
     */
    public static final String[] arrPos =
        new String[] { "651", "652", "653", "661", "662", "663", "14303", "14301", "14300", "14302", "14304", "14305",
            "14306", "681", "682", "701", "1321", "702", "683", "125", "126", "1271", "1272", "130" };

    /**
     * 对应alarmType的标识开始位
     */
    private static final String[] arrName =
        new String[] { "温度传感器高温报警", "温度传感器低温报警", "温度传感器异常报警", "湿度传感器高湿度报警", "湿度传感器低湿度报警", "湿度传感器异常报警", "胎压传感器异常报警",
            "胎压过高报警", "胎压过低报警", "胎温过高报警", "胎压不平衡报警", "慢漏气报警", "胎压传感器电量过低", "加油报警", "漏油报警", "载重传感器异常报警", "工时传感器异常报警",
            "超载报警", "油位传感器异常报警", "视频信号丢失", "视频信号遮挡", "主存储器故障", "灾备存储器故障", "异常驾驶行为" };

    /**
     * 聚合过的特殊报警过滤标识
     */
    private static final List<String> SPECIAL_SENSOR_ALARM =
        Arrays.asList("温度传感器", "湿度传感器", "轮胎", "胎压", "加油报警", "漏油报警", "载重", "工时", "油箱");

    private static final List<String> SPECIAL_VIDEO_ALARM =
        Arrays.asList("视频信号丢失", "视频信号遮挡", "主存储器", "灾备存储器", "异常驾驶行为");

    /**
     * 组装需要聚合的报警类型的标识位和名称
     */
    private static List<AlarmType> getAggregation() {
        List<AlarmType> alarmTypeList = new ArrayList<>();
        int videoIndex = Arrays.asList(arrPos).indexOf("125"); // 视频报警下标
        for (int i = 0; i < arrPos.length; i++) {
            AlarmType alarmType = new AlarmType();
            alarmType.setPos(arrPos[i]);
            alarmType.setName(arrName[i]);
            if (i < videoIndex) {

                alarmType.setType("sensorAlarm");
            } else {
                alarmType.setType("videoAlarm");
            }
            alarmTypeList.add(alarmType);
        }
        return alarmTypeList;
    }

    /**
     * 组装报警名称
     */
    public static List<AlarmType> assemblyAlarmName(List<AlarmType> alarms, String sign,
        Map<String, String> posEventCommonNameMap) {
        List<AlarmType> alarmTypeList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(alarms)) {
            alarmTypeList.addAll(AlarmTypeUtil.getAggregation());
            if ("alarmQuery".equals(sign)) {
                if (posEventCommonNameMap != null) {
                    // 组装报警查询报警类型树
                    alarmQuery(alarms, alarmTypeList, posEventCommonNameMap);
                }
            }
            if ("alarmSetting".equals(sign)) {
                // 组装联动策略报警类型树
                alarmSetting(alarms, alarmTypeList);
            }
        }
        return alarmTypeList;
    }

    private static void alarmQuery(List<AlarmType> alarms, List<AlarmType> alarmTypeList,
        Map<String, String> posEventCommonNameMap) {
        for (AlarmType alarm : alarms) {
            String alarmName = alarm.getName();
            switch (alarm.getType()) {
                case "alert": // 预警
                case "vehicleAlarm": // 车辆报警
                case "faultAlarm": // 故障报警
                case "driverAlarm": // 驾驶员报警
                case "platAlarm": // 平台报警
                case "peopleAlarm": // 北斗天地
                case "peoplePlatAlarm": // 北斗天地平台
                case "asolongAlarm": // ASO
                case "asolongPlatAlarm": // ASO平台
                case "f3longAlarm": // F3超待
                case "f3longPlatAlarm": // F3超待平台
                case "kkslongAlarm": // 康凯斯报警
                    alarmTypeList.add(alarm);
                    break;
                case "highPrecisionAlarm": // F3高精度
                    assemblyHighPrecisionAlarmData(alarm, alarmTypeList);
                    break;
                case "adasAlarm"://主动安全报警
                    assemblyAdasData(alarm, alarmTypeList, posEventCommonNameMap);
                    break;
                case "sensorAlarm": // F3传感器报
                    if (StringUtil.judgeStrIsContainAppointStr(alarmName, SPECIAL_SENSOR_ALARM)) {
                        alarmTypeList.add(alarm);
                    }
                    break;
                case "videoAlarm": // 视频报警
                    if (StringUtil.judgeStrIsContainAppointStr(alarmName, SPECIAL_VIDEO_ALARM)) {
                        alarmTypeList.add(alarm);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private static void assemblyHighPrecisionAlarmData(AlarmType alarm, List<AlarmType> alarmTypeList) {

        boolean flag = false;
        for (AlarmType alarmType : alarmTypeList) {
            String alarmTypeName = alarmType.getName();
            if (StringUtils.isBlank(alarmTypeName)) {
                continue;
            }
            String alarmTypeType = alarmType.getType();
            if (Objects.equals(alarm.getType(), alarmTypeType) && Objects.equals(alarm.getName(), alarmTypeName)) {
                alarmType.setPos(alarm.getPos() + "," + alarmType.getPos());
                flag = true;
            }
        }
        if (!flag) {
            alarmTypeList.add(alarm);
        }
    }

    private static void assemblyAdasData(AlarmType alarm, List<AlarmType> alarmTypeList,
        Map<String, String> posEventCommonNameMap) {
        for (AlarmType alarmType : alarmTypeList) {
            if (alarmType.getName() != null && alarmType.getName().equals(posEventCommonNameMap.get(alarm.getPos()))
                && alarmType.getType().equals("adasAlarm")) {
                //多个协议同一个报警类型时，组装pos
                alarmType.setPos(alarmType.getPos() + "," + alarm.getPos());
                return;
            }
        }
        //主动安全报警
        if ("adasAlarm".equals(alarm.getType()) && posEventCommonNameMap.get(alarm.getPos()) != null) {
            alarm.setName(posEventCommonNameMap.get(alarm.getPos()));
            alarmTypeList.add(alarm);
        }
    }

    private static void alarmSetting(List<AlarmType> alarms, List<AlarmType> alarmTypeList) {
        for (AlarmType alarm : alarms) {
            String alarmName = alarm.getName();
            switch (alarm.getType()) {
                case "alert": // 预警
                case "vehicleAlarm": // 车辆报警
                case "faultAlarm": // 故障报警
                case "driverAlarm": // 驾驶员报警
                case "platAlarm": // 平台报警
                case "ioAlarm": // I/O报警
                    alarmTypeList.add(alarm);
                    break;
                case "sensorAlarm": // F3传感器报警
                    if (StringUtil.judgeStrIsContainAppointStr(alarmName, SPECIAL_SENSOR_ALARM)) {
                        alarmTypeList.add(alarm);
                    }
                    break;
                case "videoAlarm": // 视频报警
                    if (StringUtil.judgeStrIsContainAppointStr(alarmName, SPECIAL_VIDEO_ALARM)) {
                        alarmTypeList.add(alarm);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public static String getProtocolType(String deviceType) {
        return "-1".equals(deviceType) ? "1" : deviceType;
    }

    /**
     * 包含川冀（交通部2013）
     * @param deviceType
     * @return
     */
    public static List<String> getProtocolTypes(String deviceType) {
        List<String> protocolTypes = new ArrayList<>();
        if ("-1".equals(deviceType)) {
            return ProtocolTypeUtil.getDeviceType2013();
        }
        if ("11".equals(deviceType)) {
            return ProtocolTypeUtil.getAll2019Protocol();
        }
        protocolTypes.add(deviceType);
        return protocolTypes;
    }

    /**
     * 获取所有的报警类型
     * @return
     */
    public static List<Integer> getAllAlarmType() {
        List<Integer> alarmPos = new ArrayList<>();
        List<String> redisKeyList = RedisHelper.scanKeys(HistoryRedisKeyEnum.ALARM_TYPE_INFO.of("*"));
        for (String redisKey : redisKeyList) {
            String[] keyData = redisKey.split("_");
            if (keyData.length == 2) {
                alarmPos.add(Integer.parseInt(keyData[0].trim()));
            }
        }
        return alarmPos;
    }

    /**
     * 转换报警类型
     */
    public static List<Integer> typeList(String alarmType) {
        String[] str = alarmType.split(",");
        List<Integer> poss = new ArrayList<>();
        for (String pos : str) {
            poss.addAll(AlarmTypePosEnum.getPosListByType(pos));
        }
        return poss;
    }
}

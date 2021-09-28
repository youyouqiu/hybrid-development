/*
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved. This software
 * is the confidential and proprietary information of ZhongWei, Inc.
 * You shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement you
 * entered into with ZhongWei.
 */

package com.zw.ws.common;

import com.zw.platform.domain.enmu.ProtocolEnum;
import com.zw.platform.util.Translator;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class PublicVariable {

    /**
     * 瞬时报警类型(除了瞬时报警,其他的为持续报警)
     */
    public static final String INSTANTANEOUS_ALARM_TYPE = "0,3,20,21,22,27,28,31";

    /**
     * 油位传感器外设ID
     */
    public static final int OIL_SENSOR_DEVICE_ID = 0x41;

    /**
     * 双油位传感器外设ID
     */
    public static final int OIL_DOUBLE_SENSOR_DEVICE_ID = 0x42;

    /**
     * 车辆状态关键字
     */
    public static final String VEHICLE_STATUS_KEYWORDS = "vehiclestatus";

    /**
     * 排班人员工作状态关键字
     */
    public static final String WORK_STATUS_KEYWORDS = "_work_status";

    /**
     *
     */
    public static final String VEHICLE_INFO_KEYWORDS = "vehicleinfo";

    /**
     * 普通报警默认推送缓存key
     */
    public static final String DEFAULT_PUSH_KEYWORDS = "default_push";

    /**
     * 音视频报警默认推送缓存key
     */
    public static final String DEFAULT_VIDEO_PUSH_KEYWORDS = "defaultVideo_push";

    /**
     * Redis0默认数据库(存实时监控的缓存数据)
     */
    @Deprecated
    public static final int REDIS_DEFAULT_DATABASE = 0;

    /**
     * Redis1数据库(存habase缓存数据)
     */
    @Deprecated
    public static final int REDIS_HBASE_DATABASE = 1;

    /**
     * Redis2数据库(存mysql缓存数据)
     */
    @Deprecated
    public static final int REDIS_MYSQL_DATABASE = 2;

    /**
     * Redis2数据库(存其他缓存数据)
     */
    @Deprecated
    public static final int REDIS_ELSE_DATABASE = 3;

    /**
     * Redis2数据库(存其他缓存数据)
     */
    @Deprecated
    public static final int REDIS_FOUR_DATABASE = 4;

    /**
     * Redis2数据库(存其他缓存数据)
     */
    @Deprecated
    public static final int REDIS_FIVE_DATABASE = 5;

    /**
     * Redis2数据库(存其他缓存数据)
     */
    @Deprecated
    public static final int REDIS_SIX_DATABASE = 6;

    /**
     * Redis2数据库(存其他缓存数据)
     */
    @Deprecated
    public static final int REDIS_SEVEN_DATABASE = 7;

    /**
     * Redis2数据库(存其他缓存数据)
     */
    @Deprecated
    public static final int REDIS_EIGHT_DATABASE = 8;

    /**
     * Redis2数据库(存其他缓存数据)
     */
    @Deprecated
    public static final int REDIS_NINE_DATABASE = 9;

    /**
     * Redis2数据库(存其他缓存数据)
     */
    @Deprecated
    public static final int REDIS_TEN_DATABASE = 10;

    /**
     * Redis2数据库(存其他缓存数据)
     */
    @Deprecated
    public static final int REDIS_ELEVEN_DATABASE = 11;

    /**
     * Redis2数据库(存其他缓存数据)
     */
    @Deprecated
    public static final int REDIS_TWELVE_DATABASE = 12;

    /**
     * Redis2数据库(存其他缓存数据)
     */
    @Deprecated
    public static final int REDIS_THIRTEEN_DATABASE = 13;

    /**
     * Redis2数据库(存其他缓存数据)
     */
    @Deprecated
    public static final int REDIS_FOURTEEN_DATABASE = 14;

    /**
     * Redis2数据库(存其他缓存数据)
     */
    @Deprecated
    public static final int REDIS_FIFTEEN_DATABASE = 15;

    /**
     * Redis缓存失效时间，默认为10分钟
     */
    public static final int REDIS_CACHE_TIMEOUT = 5000;

    /**
     * Redis缓存失效时间，一个小时
     */
    public static final int REDIS_CACHE_TIMEOUT_HOUR = 30000;

    /**
     * Redis缓存失效时间，一天
     */
    public static final int REDIS_CACHE_TIMEOUT_DAY = 86400;

    /**
     * Kafka消费的默认分区(Partion)
     */
    public static final int KAFKA_COMSUME_PARTION = 0;

    /**
     * 终端成功注册
     */
    public static final int DEVICE_REGISTERED_SUCCESS = 0;

    /**
     * 数据库中不存在该终端
     */
    public static final int DEVICE_NOT_EXISTS_IN_DATABASE = 4;

    /**
     * 数据库中不存在该车辆
     */
    public static final int VEHICLE_NOT_EXISTS_IN_DATABASE = 2;

    /**
     * Kafka数据写入分区
     */
    public static final int KAFKA_DATA_WTITE_PARTION = 1;

    /**
     * 订阅车辆状态
     */
    public static final int SUBSCRIBE_VEHICLE_STATUS = 1;

    /**
     * 取消订阅车辆状态
     */
    public static final int UNSUBSCRIBE_VEHICLE_STATUS = 2;

    /**
     * 取消订阅点名位置信息
     */
    public static final int UNSUBSCRIBE_VEHICLE_REAL = 6;

    /**
     * 订阅车辆位置
     */
    public static final int SUBSCRIBE_VEHICLE_LOCATION = 3;

    /**
     * 取消订阅车辆位置
     */
    public static final int UNSUBSCRIBE_VEHICLE_LOCATION = 4;

    /**
     * 订阅车辆报警
     */
    public static final int SUBSCRIBE_VEHICLE_ALARM = 5;

    /**
     * 取消订阅车辆报警
     */
    public static final int UNSUBSCRIBE_VEHICLE_ALARM = 0;

    /**
     * 客户端请求标志
     */
    public static final int REQUEST_SOUCE_CLIENT = 1;

    /**
     * 服务端请求标志
     */
    public static final int REQUEST_SOURCE_SERVER = 2;

    /**
     * 写入数据库Kafka组
     */
    public static final String HBASE_GROUPID = "hbase1";

    /**
     * 里程附加消息ID
     */
    public static final int MILES_ATTACHINFO_ID = 0x01;

    /**
     * 扩展车辆信号状态位，定义见表 T808-2013版
     */
    public static final int EXTENDED_VEHICLE_SIGNAL_STATUS_ID = 0x25;

    /**
     * 扩展车辆信号状态位,空调状态偏移量
     */
    public static final int EXTENDED_VEHICLE_SIGNAL_STATUS_AIR_CONDITION_STATUS_OFFSET = 9;

    /**
     * 扩展车辆信号状态位，卫星颗数偏移量
     */
    public static final int EXTENDED_VEHICLE_SINGLE_STATUS_SATELITE_NUMBER = 0x31;

    /**
     * 外设传感器标定数据
     */
    public static final int CALIBRATION_DATA = 0xF6;

    /**
     * 外设通讯参数设置
     */
    public static final int TRANSMISSION_SETTING_DATA = 0xF5;

    /**
     * 油位传感器外设ID 邮箱1（在油箱中安装，通过测量当前油位高度反映当前油箱中剩余油量）
     */
    public static final int OILLEVEL_SENSOR_ONE_ID = 0x41;

    /**
     * 油位传感器外设ID 邮箱2（在油箱中安装，通过测量当前油位高度反映当前油箱中剩余油量）
     */
    public static final int OILLEVEL_SENSOR_OTWO_ID = 0x42;

    /**
     * 油耗传感器外设ID 油耗1
     */
    public static final int OIL_WEAR_SENSOR_ONE_ID = 0x45;

    /**
     * 震动传感器外设ID 传感器1
     */
    public static final int VIBRATION_SENSOR_ONE_ID = 0X5A;

    /**
     * 邮箱测量方案
     * 油杆指油箱形状：01-长方体；02-圆柱形；03-D 形；04-椭圆形；05-其他
     */
    public static final int RECTANGLE = 1;

    public static final int CYLINDER = 2;

    public static final int DSHAPE = 3;

    public static final int OVAL = 4;

    public static final int OTHERSHAPE = 5;

    public static final String JTB_TYPE = "1";

    public static final String JTB_TYPE_11 = "0";

    public static final String BD_TYPE = "5";

    public static final String GV_TYPE = "2";

    public static final String TH_TYPE = "3";

    public static final String KKS_YX_TYPE = "6";

    public static final String KKS_WX_TYPE = "7";

    public static final String KKS_BSJ_TYPE = "8";

    public static final String ASO_CCDJ_TYPE = "9";//艾赛欧超长待机

    public static final String F3_CCDJ_TYPE = "10";//F3超长待机设备

    public static final int MONITOR_COUNT = 5000; // 监控对象数量

    public static final int IMPORT_THING_FIELDS = 1;

    public static final int EXPORT_VEHICLE_FIELDS = 1;

    /**
     * 插卡司机的车辆关联KEY前缀
     */
    public static final String CARD_NUM_PREFIX = "vc_";

    /**
     * 插卡司机的车辆关联KEY前缀
     */
    public static final String CARD_NUM_PROFESSIONAL_PREFIX = "vp_";

    /**
     *
     */
    public static final String LAST_DRIVER = "last_driver_";

    public static final Translator<String, String> FUNCTION_TYPE = Translator.<String, String>builder()
            .add("1", "简易型车机")
            .add("2", "行车记录仪")
            .add("3", "对讲设备")
            .add("4", "手咪设备")
            .add("5", "超长待机设备")
            .add("6", "定位终端")
            .build();

    private static Map<String, Integer> deviceStartMap = new HashMap<>();

    private static Map<Integer, Integer> deviceChannelMap = new HashMap<>();

    static {

        deviceStartMap.put("停用", 0);
        deviceStartMap.put("启用", 1);

        deviceChannelMap.put(4, 1);
        deviceChannelMap.put(5, 2);
        deviceChannelMap.put(8, 3);
        deviceChannelMap.put(16, 4);
    }

    public static String getDeviceTypeId(String deviceType) {
        return ProtocolEnum.getDeviceTypeByDeviceName(deviceType);
    }

    public static String getFunctionTypeId(String functionType) {
        return FUNCTION_TYPE.p2b(functionType, "");
    }

    public static Integer getDeviceStart(String startStatus) {
        return deviceStartMap.getOrDefault(startStatus, 0);
    }

    public static Integer getDeviceChannel(Integer channelNumber) {
        return deviceChannelMap.getOrDefault(channelNumber, 1);
    }

    public static boolean checkConnectionStatus(String connectionStatus) {
        boolean result = true;
        if (StringUtils.isNotBlank(connectionStatus)) {
            result = !connectionStatus.contains("0");
        }
        return result;

    }

    public static String getTrackPlaybackKey(String userId, String vid, String key) {
        return userId + "&" + vid + key;
    }

    public static final Translator<String, Integer> SEND_STATUS = Translator.of("参数已生效", 0,
        "参数未生效", 1, "参数消息有误", 2, "参数不支持", 3,
        "参数下发中", 4, "终端离线，未下发", 5, Translator.Pair.of("终端处理中", 7),
        Translator.Pair.of("终端接收失败", 8));
}

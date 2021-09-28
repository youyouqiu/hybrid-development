package com.zw.platform.push.handler.device;

import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.util.Translator;
import com.zw.platform.util.common.MonitorUtils;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * 设备公共方法区
 */
public class DeviceHelper {
    public static final Translator<Integer, String> AUDIO_FORMAT = Translator
        .of(0, "ADPCMA", 2, "G726-16K", 3, "G726-24K", 4, "G726-32K", 5, "G726-40K", 6, "G711a",
            Translator.Pair.of(7, "G711u"), Translator.Pair.of(8, "AAC-ADTS"));
    public static final Translator<Integer, String> SAMPLING_RATE =
        Translator.of(0, "8KHZ", 1, "22.05KHZ", 2, "44.1KHZ", 3, "48KHZ");
    public static final Translator<Integer, String> VOCAL_TRACT = Translator.of(0, "单声道", 1, "双声道");

    private DeviceHelper() {
    }

    /**
     * 判断设备是否在线，若在线判断JTBDeviceManager中是否有该设备，没有则加上
     * @param vehicleId    void
     * @param deviceNumber 设备编号
     * @return 流水号
     */
    public static @Nullable Integer getRegisterDevice(String vehicleId, String deviceNumber) {
        if (StringUtils.isBlank(vehicleId)) {
            return null;
        }
        // 设备状态监测,若设备不在缓存中，添加到缓存中，服务端重启后缓存的设备请空
        // 此时无设备注册信息，根据设备心跳重新添加设备状态
        if (!MonitorUtils.isOnLine(vehicleId)) {
            return null;
        }
        return deviceSerialNumber(vehicleId);
    }

    /**
     * 获取监控对象消息流水号，如果监控对象不在线则返回-1
     *
     * @param monitorId 监控对象id
     * @return 流水号 -1 ~ 65535
     * @deprecated 职责不单一，请使用 DeviceHelper.deviceSerialNumber(monitorId) 代替，业务代码根据需要自行判断离线状态
     */
    public static int serialNumber(String monitorId) {
        if (StringUtils.isBlank(monitorId)) {
            return -1;
        }
        // 设备状态监测,若设备不在缓存中，添加到缓存中，服务端重启后缓存的设备请空
        // 此时无设备注册信息，根据设备心跳重新添加设备状态
        if (!MonitorUtils.isOnLine(monitorId)) {
            return -1;
        }
        // redis 获取流水号
        return deviceSerialNumber(monitorId);
    }

    /**
     * 获取参数下发流水号
     * @param vehicleId 设备Id
     * @return 流水号, 0 ~ 65535
     */
    public static int deviceSerialNumber(String vehicleId) {
        if (StringUtils.isBlank(vehicleId)) {
            return 0;
        }
        long serialNumber = RedisHelper.incr(HistoryRedisKeyEnum.VEHICLE_MSGSN.of(vehicleId));
        return (int) (serialNumber & 0xffff);
    }
}

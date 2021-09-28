package com.zw.platform.util;

import com.google.common.collect.ImmutableMap;
import com.zw.platform.basic.constant.MonitorTypeEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.constant.Vehicle;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/***
 @Author lijie
 @Date 2020/4/15 15:57
 @Description 模糊查询工具类
 @version 1.0
 **/
public class FuzzySearchUtil {

    public static final String VEHICLE_TYPE = "!";

    public static final String PEOPLE_TYPE = "@";

    public static final String THING_TYPE = "#";

    public static final String DEVICE_TYPE = "$";

    public static final String SIM_TYPE = "%";

    /**
     * 模糊搜索的缓存分隔符
     */
    public static final String DELIMITER = "&";

    /**
     * 终端模糊搜索前缀
     */
    private static final String DEVICE_PREFIX = "device&";
    /**
     * sim卡模糊搜索前缀
     */
    private static final String SIM_CARD_PREFIX = "simcard&";
    /**
     * 监控对象模糊搜索前缀
     */
    private static final String MONITOR_PREFIX = "vehicle&";

    private static final RedisKey FUZZY_KEY = RedisKeyEnum.FUZZY_MONITOR_DEVICE_SIMCARD.of();
    public static String Thing_TYPE;

    public static String getMonitorType(int type) {
        switch (type) {
            case 0:
                return VEHICLE_TYPE;
            case 1:
                return PEOPLE_TYPE;
            case 2:
                return THING_TYPE;
            default:
                return VEHICLE_TYPE;
        }
    }

    public static String getMonitorType(String type) {
        if (StrUtil.isBlank(type)) {
            return "";
        }
        return getMonitorType(Integer.valueOf(type));
    }

    /**
     * 获取所有监控对象的模糊搜索的key
     * @param monitorName
     * @return
     */
    public static String buildFuzzySearchAllMonitorKey(String monitorName) {
        return String.format("[%s,%s,%s]*%s*", VEHICLE_TYPE, PEOPLE_TYPE, THING_TYPE, monitorName.trim());
    }

    /**
     * 获取所有绑定监控对象的模糊搜索的key
     * @param monitorName
     * @return
     */
    public static String buildFuzzySearchBindMonitorKey(String monitorName) {
        return String.format("[%s,%s,%s]*%s*&*", VEHICLE_TYPE, PEOPLE_TYPE, THING_TYPE, monitorName);
    }

    /**
     * 获取具体类型监控对象的模糊搜索的key
     * @param monitorName
     * @param monitorTypeEnum
     * @return
     */
    public static String buildFuzzySearchAllMonitorKey(String monitorName, MonitorTypeEnum monitorTypeEnum) {
        String monitorType = getMonitorType(monitorTypeEnum.getType());
        return String.format("%s*%s*", monitorType, monitorName);
    }

    /**
     * 获取具体类型所有绑定监控对象的模糊搜索的key
     * @param monitorName
     * @return
     */
    public static String buildFuzzySearchBindMonitorKey(String monitorName, MonitorTypeEnum monitorTypeEnum) {
        String monitorType = getMonitorType(monitorTypeEnum.getType());
        return String.format("%s*%s*&*", monitorType, monitorName);
    }

    /**
     * 根据SIM卡号码搜索监控对象模糊搜索的key
     * 绑定监控对象key样例  @PZ0005&$PZ00005&%13200000005
     * @param keyword 终端手机号关键字
     * @return pattern
     */
    public static String buildFuzzySearchMonitorBySimKey(String keyword) {
        return String.format("*&*&%s*%s*", SIM_TYPE, keyword);
    }

    /**
     * 根据终端号搜索监控对象模糊搜索的key
     * 绑定监控对象key样例  @PZ0005&$PZ00005&%13200000005
     * @param keyword 终端手机号关键字
     * @return pattern
     */
    public static String buildFuzzySearchMonitorByDeviceKey(String keyword) {
        return String.format("*&%s*%s*&*", DEVICE_TYPE, keyword);
    }

    public static Map<String, String> buildDevice(String deviceNumber, String deviceId) {
        return ImmutableMap.of(buildDeviceField(deviceNumber), DEVICE_PREFIX + deviceId);
    }

    public static String buildDeviceField(String deviceNumber) {
        return DEVICE_TYPE + deviceNumber;
    }

    public static Map<String, String> buildSimCard(String simCardNumber, String simCardId) {
        return ImmutableMap.of(buildSimCardField(simCardNumber), SIM_CARD_PREFIX + simCardId);
    }

    public static String buildSimCardField(String simCardNum) {
        return SIM_TYPE + simCardNum;
    }

    public static String buildMonitorField(String monitorType, String name) {
        return getMonitorType(monitorType) + name;
    }

    public static Map<String, String> buildMonitor(String monitorType, String monitorId, String monitorName) {
        return ImmutableMap.of(buildMonitorField(monitorType, monitorName), MONITOR_PREFIX + monitorId);
    }

    public static String buildField(String monitorType, String monitorName, String deviceNumber, String simCardNum) {
        String monitorField = buildMonitorField(monitorType, monitorName);
        return String.format("%s&%s&%s", monitorField, buildDeviceField(deviceNumber), buildSimCardField(simCardNum));
    }

    public static String buildValue(String monitorId, String deviceId, String simCardId) {
        return String.format("vehicle&%s&device&%s&simcard&%s", monitorId, deviceId, simCardId);
    }

    public static Set<String> getFuzzySearchDeviceId(String simpleQueryParam) {
        Set<String> deviceIds = new HashSet<>();
        String pattern = String.format("*%s*", simpleQueryParam);
        List<Map.Entry<String, String>> fuzzyList = RedisHelper.hscan(FUZZY_KEY, pattern);
        for (Map.Entry<String, String> entry : fuzzyList) {
            String key = entry.getKey();
            String value = entry.getValue();
            String[] keys = entry.getKey().split(FuzzySearchUtil.DELIMITER);
            //未包含终端类型的直接过滤掉
            if (!key.contains(DEVICE_TYPE)) {
                continue;
            }
            // 当终端被绑定的时候，排除只根据sim卡搜索出来的结果
            if (keys.length == 3) {
                String monitor = keys[0];
                String device = keys[1];
                //第一位一定是监控对象，第二位一定是终端,监控对象和终端都不匹配的情况
                if (!monitor.contains(simpleQueryParam) && !device.contains(simpleQueryParam)) {
                    continue;
                }
            }
            deviceIds.add(value.split(DEVICE_PREFIX)[1].split(DELIMITER)[0]);
        }
        return deviceIds;
    }

    public static Set<String> getFuzzySearchSimCardId(String simpleQueryParam) {
        Set<String> simCardIds = new HashSet<>();
        String pattern = String.format("*%s*", simpleQueryParam);
        List<Map.Entry<String, String>> fuzzyList = RedisHelper.hscan(FUZZY_KEY, pattern);
        for (Map.Entry<String, String> entry : fuzzyList) {
            String key = entry.getKey();
            if (!key.contains(SIM_TYPE)) {
                continue;
            }
            String value = entry.getValue();
            simCardIds.add(value.split(SIM_CARD_PREFIX)[1]);
        }
        return simCardIds;
    }

    /**
     * 模糊搜索未绑定的对象
     * @param keyword 关键字
     * @param type    终端（$）、sim卡（%）、人、车、物
     * @return 符合条件的终端
     */
    public static Set<String> scanUnbind(String keyword, String type) {
        Set<String> idSet = new HashSet<>();
        String pattern = String.format("%s*%s*", type, keyword);
        List<Map.Entry<String, String>> fuzzyList = RedisHelper.hscan(FUZZY_KEY, pattern);
        for (Map.Entry<String, String> entry : fuzzyList) {
            String[] keys = entry.getKey().split(DELIMITER);
            String[] values = entry.getValue().split(DELIMITER);
            if (keys.length > 1) {
                continue;
            }
            idSet.add(values[1]);
        }
        return idSet;
    }

    /**
     * 按监对象名称扫描某一类型的监控对象
     * @param moType   监控对象类型 为空扫描全部类型的监控对象
     * @param keyword  关键字
     * @param bindType 0 未绑定 1绑定 为空或其他值，查询全部的监控对象
     * @return 监控对象ID集合
     */
    public static Set<String> scanByMonitor(String moType, String keyword, String bindType) {
        Set<String> monitorIds = new HashSet<>();
        String prefix = StringUtils.isNotBlank(moType) ? getMonitorType(moType) : "";
        String pattern = String.format("%s*%s*", prefix, keyword);
        List<Map.Entry<String, String>> fuzzyList = RedisHelper.hscan(FUZZY_KEY, pattern);
        for (Map.Entry<String, String> entry : fuzzyList) {
            String[] keys = entry.getKey().split(DELIMITER);
            String[] values = entry.getValue().split(DELIMITER);
            if (Objects.equals(Vehicle.BindType.HAS_BIND, bindType) && keys.length < 3) {
                continue;
            }
            if (Objects.equals(Vehicle.BindType.UNBIND, bindType) && keys.length > 1) {
                continue;
            }
            String monitorName = keys[0];
            String monitorId = values[1];
            if (monitorName.contains(keyword)) {
                monitorIds.add(monitorId);
            }
        }
        return monitorIds;
    }

    /**
     * 按监控对象名称、终端、sim卡扫描
     * @param keyword 对象名称、终端、sim卡 关键字名称
     * @return 监控对象ID
     */
    public static Set<String> scanBindMonitor(String keyword) {
        Set<String> monitorIds = new HashSet<>();
        String pattern = String.format("*%s*", keyword);
        List<Map.Entry<String, String>> fuzzyList = RedisHelper.hscan(FUZZY_KEY, pattern);
        for (Map.Entry<String, String> entry : fuzzyList) {
            String[] keys = entry.getKey().split(DELIMITER);
            String[] values = entry.getValue().split(DELIMITER);
            if (keys.length < 3) {
                continue;
            }
            monitorIds.add(values[1]);
        }
        return monitorIds;
    }

    /**
     * 扫描监控对象
     * @param redisKey 缓存key  后续可以去掉统一使用改造后的rediskey
     * @param keyword  监控对象名称关键字
     * @param moType   监控对象类型
     * @return 绑定的监控对象ID集合
     */
    public static Set<String> scanBindMonitor(RedisKey redisKey, String keyword, String moType) {
        String startPrefix = getMonitorType(moType);
        String pattern = String.format("%s*%s*&*", startPrefix, keyword);
        List<Map.Entry<String, String>> fuzzyList = RedisHelper.hscan(redisKey, pattern);
        if (CollectionUtils.isEmpty(fuzzyList)) {
            return new HashSet<>();
        }
        Set<String> monitorIds = new HashSet<>();
        for (Map.Entry<String, String> entry : fuzzyList) {
            String[] keys = entry.getKey().split(DELIMITER);
            String[] values = entry.getValue().split(DELIMITER);
            if (keys.length < 3 || values.length < 6) {
                continue;
            }
            if (!keys[0].contains(keyword)) {
                continue;
            }
            monitorIds.add(values[1]);
        }

        return monitorIds;
    }

    /**
     * 根据关键字扫描
     * @param redisKey redisKey
     * @param keyword  keyword
     * @return 符合条件的监控对象ID
     */
    public static Set<String> scan(RedisKey redisKey, String keyword) {
        List<Map.Entry<String, String>> fuzzyList = RedisHelper.hscan(redisKey, "*" + keyword + "*");
        if (CollectionUtils.isEmpty(fuzzyList)) {
            return new HashSet<>();
        }
        Set<String> monitorIds = new HashSet<>();
        for (Map.Entry<String, String> entry : fuzzyList) {
            String[] keys = entry.getKey().split(DELIMITER);
            String[] values = entry.getValue().split(DELIMITER);
            if (keys.length < 3 || values.length < 6) {
                continue;
            }
            monitorIds.add(values[1]);
        }

        return monitorIds;

    }

}

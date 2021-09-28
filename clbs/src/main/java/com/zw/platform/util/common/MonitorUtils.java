package com.zw.platform.util.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.MonitorTypeEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.core.TypeCacheManger;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.VehicleCategoryDTO;
import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.platform.basic.dto.VehicleSubTypeDTO;
import com.zw.platform.basic.dto.VehicleTypeDTO;
import com.zw.platform.util.CommonUtil;
import com.zw.platform.util.FuzzySearchUtil;
import com.zw.protocol.msg.Message;
import com.zw.protocol.msg.t808.T808Message;
import com.zw.protocol.msg.t808.body.LocationInfo;
import com.zw.ws.entity.vehicle.ClientVehicleInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.RandomAccess;
import java.util.Set;
import java.util.function.Function;

/**
 * 监控对象通用工具类
 *
 * @author zjc
 */
public class MonitorUtils {

    private static final RedisKey SEARCH_KEY = RedisKeyEnum.FUZZY_MONITOR_DEVICE_SIMCARD.of();

    /**
     * 通过监控对象的类型返回车辆的英文标识（0->vehicle,1->people,2->thing）
     */
    public static String getEnNameByMonitorType(String monitorType) {
        return MonitorTypeEnum.getEnNameByType(monitorType);
    }

    /**
     * 通过监控对象的类型返回对应的中文名称（0->车，1->人，2->物）
     */
    public static String getMonitorTypeName(String monitorType) {
        return Optional.ofNullable(MonitorTypeEnum.getNameByType(monitorType)).orElse("");
    }

    /**
     * 通过监控对象名称模糊搜索所有的的监控对象id集合（包含绑定和没有绑定）
     */
    public static Set<String> fuzzySearchAllMonitorIds(String monitorName) {
        return fuzzySearchMonitorIds(FuzzySearchUtil.buildFuzzySearchAllMonitorKey(monitorName), monitorName);
    }

    /**
     * 通过监控对象名称模糊搜索所有绑定的的监控对象id集合（只包含绑定，用于报表查询按照监控对象名称进行模糊搜索使用）
     */
    public static Set<String> fuzzySearchBindMonitorIds(String monitorName) {
        return fuzzySearchMonitorIds(FuzzySearchUtil.buildFuzzySearchBindMonitorKey(monitorName), monitorName);
    }

    /**
     * 通过监控对象名称模糊搜索所有具体类型的监控对象id集合（包含绑定和没有绑定）
     */
    public static Set<String> fuzzySearchAllMonitorIds(String monitorName, MonitorTypeEnum monitorTypeEnum) {
        return fuzzySearchMonitorIds(FuzzySearchUtil.buildFuzzySearchAllMonitorKey(monitorName, monitorTypeEnum),
            monitorName);
    }

    /**
     * 通过监控对象名称模糊搜索所有绑定的具体类型监控对象id集合（只包含绑定）
     */
    public static Set<String> fuzzySearchBindMonitorIds(String monitorName, MonitorTypeEnum monitorTypeEnum) {
        return fuzzySearchMonitorIds(FuzzySearchUtil.buildFuzzySearchBindMonitorKey(monitorName, monitorTypeEnum),
            monitorName);
    }

    /**
     * 通过监控对象名称/SIM卡/终端号模糊搜索所有具体类型的监控对象id集合（包含绑定和没有绑定,用于人车物列表分页查询模块）
     */
    public static Set<String> fuzzyFilterAllMonitorIds(String searchParam, MonitorTypeEnum monitorTypeEnum) {
        return fuzzySearchMonitorIds(FuzzySearchUtil.buildFuzzySearchAllMonitorKey(searchParam, monitorTypeEnum));
    }

    /**
     * 通过监控对象名称/SIM卡/终端号模糊搜索所有具体类型的监控对象id集合（包含绑定和没有绑定,用于信息配置列表分页查询模块）
     */
    public static Set<String> fuzzyFilterBindMonitorIds(String searchParam) {
        return fuzzySearchMonitorIds(FuzzySearchUtil.buildFuzzySearchBindMonitorKey(searchParam));
    }

    /**
     * 根据终端手机号模糊搜索监控对象
     * @param simCardNum 终端手机号关键字
     * @return 监控对象ID
     */
    public static Set<String> fuzzySearchBySim(String simCardNum) {
        return fuzzySearchMonitorIds(FuzzySearchUtil.buildFuzzySearchMonitorBySimKey(simCardNum));
    }

    /**
     * 根据终端号模糊搜索监控对象
     * @param deviceNum 终端号关键字
     * @return 监控对象ID
     */
    public static Set<String> fuzzySearchByDevice(String deviceNum) {
        return fuzzySearchMonitorIds(FuzzySearchUtil.buildFuzzySearchMonitorByDeviceKey(deviceNum));
    }

    private static String getMonitorId(String value) {
        return value.split("vehicle")[1].split("&")[1];
    }

    /**
     * @param fuzzySearchKey 模糊搜索的key
     * @param monitorName    监控对象名称(只搜索监控对象的名称，不包含sim卡和终端编号的模糊搜索)
     * @return 监控对象ID集合
     */
    private static Set<String> fuzzySearchMonitorIds(String fuzzySearchKey, String monitorName) {
        Set<String> monitorIdSet = new HashSet<>();
        List<Map.Entry<String, String>> monitoryEntries = RedisHelper.hscan(SEARCH_KEY, fuzzySearchKey);
        for (Map.Entry<String, String> entry : monitoryEntries) {
            String name = getMonitorName(entry);
            if (!name.contains(monitorName)) {
                continue;
            }
            String value = entry.getValue();
            monitorIdSet.add(getMonitorId(value));
        }
        return monitorIdSet;
    }

    /**
     * @param fuzzySearchKey 模糊搜索的key(搜索监控对象的名称，包含sim卡和终端编号，主要用户监控对象的分页列表)
     * @return 监控对象ID集合
     */
    private static Set<String> fuzzySearchMonitorIds(String fuzzySearchKey) {
        Set<String> monitorIdSet = new HashSet<>();
        List<Map.Entry<String, String>> monitoryEntries = RedisHelper.hscan(SEARCH_KEY, fuzzySearchKey);
        for (Map.Entry<String, String> entry : monitoryEntries) {
            String value = entry.getValue();
            monitorIdSet.add(getMonitorId(value));
        }
        return monitorIdSet;
    }

    private static String getMonitorName(Map.Entry<String, String> entry) {
        return entry.getKey().split("&")[0];
    }

    /**
     * 从redis取出绑定信息，并封装成一个BindDTO对象，避免直接操作map出现字段写错的情况
     * @param monitorId 监控对象id
     * @param fields    需要查询的字段
     */
    public static BindDTO getBindDTO(String monitorId, String... fields) {
        Map<String, String> hashMap;
        if (fields != null && fields.length > 0) {
            hashMap = RedisHelper.getHashMap(RedisKeyEnum.MONITOR_INFO.of(monitorId), fields);
        } else {
            hashMap = RedisHelper.hgetAll(RedisKeyEnum.MONITOR_INFO.of(monitorId));
        }
        return mapToClass(hashMap, BindDTO.class);
    }

    /**
     * 获取监控对象的绑定信息
     * @param monitorId 监控对象ID
     * @return 监控对象绑定信息
     */
    public static BindDTO getBindDTO(String monitorId) {
        Map<String, String> hashMap = RedisHelper.getHashMap(RedisKeyEnum.MONITOR_INFO.of(monitorId));
        if (MapUtils.isEmpty(hashMap)) {
            return null;
        }
        return mapToClass(hashMap, BindDTO.class);
    }

    /**
     * 获取监控对象信息-车辆的话会包含车辆的类型类别及车辆其他缓存信息
     * @param monitorId 监控对象ID
     * @return 监控对象信息
     */
    public static VehicleDTO getVehicle(String monitorId) {
        Map<String, String> hashMap = RedisHelper.getHashMap(RedisKeyEnum.MONITOR_INFO.of(monitorId));
        return buildVehicle(hashMap);
    }

    private static VehicleDTO buildVehicle(Map<String, String> vehicleInfoMap) {
        if (MapUtils.isEmpty(vehicleInfoMap)) {
            return null;
        }
        //封装车辆类别和类型相关字段
        TypeCacheManger cacheManger = TypeCacheManger.getInstance();
        VehicleDTO vehicle = mapToClass(vehicleInfoMap, VehicleDTO.class);
        if (StringUtils.isNotBlank(vehicle.getVehicleType())) {
            VehicleTypeDTO vehicleTypeDTO = cacheManger.getVehicleType(vehicle.getVehicleType());
            if (Objects.nonNull(vehicleTypeDTO)) {
                vehicle.setVehicleCategoryId(vehicleTypeDTO.getCategoryId());
                vehicle.setVehicleCategoryName(vehicleTypeDTO.getCategory());
                vehicle.setVehicleTypeName(vehicleTypeDTO.getType());
                vehicle.setCodeNum(vehicleTypeDTO.getCodeNum());
            }
            VehicleCategoryDTO category = cacheManger.getVehicleCategory(vehicle.getVehicleCategoryId());
            if (Objects.nonNull(category)) {
                vehicle.setStandard(category.getStandard());
            }
        }

        if (StringUtils.isBlank(vehicle.getVehicleSubTypeId())) {
            return vehicle;
        }
        //封装车辆子类型信息
        VehicleSubTypeDTO subType = TypeCacheManger.getInstance().getVehicleSubType(vehicle.getVehicleSubTypeId());
        if (Objects.nonNull(subType)) {
            vehicle.setVehicleSubType(subType.getSubType());
        }
        return vehicle;
    }

    private static <T> Map<String, T> getVehicleMap(Function<Map<String, String>, T> mapper,
                                                    Collection<String> monitorIds,
                                                    String... fields) {
        final int batchThreshold = 1000;
        final List<? extends Collection<String>> parts;
        if (monitorIds.size() <= batchThreshold) {
            parts = Collections.singletonList(monitorIds);
        } else if (monitorIds instanceof List && monitorIds instanceof RandomAccess) {
            parts = Lists.partition((List<String>) monitorIds, batchThreshold);
        } else {
            parts = Lists.partition(new ArrayList<>(monitorIds), batchThreshold);
        }
        final Map<String, T> result = Maps.newHashMapWithExpectedSize(monitorIds.size());

        List<String> hkeys;
        if (fields == null || fields.length == 0) {
            hkeys = Collections.emptyList();
        } else {
            final Set<String> fieldSet = new HashSet<>(Arrays.asList(fields));
            fieldSet.add("id");
            hkeys = new ArrayList<>(fieldSet);
        }

        // 查询+组装过程比较耗内存（全字段map对象5kB/车、DTO对象2.5kB/车），因此分批查询和组装，以降低内存峰值，并缩短redis占用
        for (Collection<String> ids : parts) {
            doGetVehicleMap(ids, hkeys, mapper, result);
        }
        return result;
    }

    private static <T> void doGetVehicleMap(Collection<String> ids,
                                            List<String> hkeys,
                                            Function<Map<String, String>, T> mapper,
                                            Map<String, T> result) {
        final List<RedisKey> redisKeys = RedisKeyEnum.MONITOR_INFO.ofs(ids);
        final List<Map<String, String>> monitorList = CollectionUtils.isEmpty(hkeys)
                ? RedisHelper.batchGetHashMap(redisKeys)
                : RedisHelper.batchGetHashMap(redisKeys, hkeys);
        T vehicleDTO;
        for (Map<String, String> monitor : monitorList) {
            vehicleDTO = mapper.apply(monitor);
            // 注：非方法引用场景不建议用Objects.isNull(obj)，直接用null == obj更好，具体参见javadoc
            if (null != vehicleDTO) {
                result.put(monitor.get("id"), vehicleDTO);
            }
        }
    }

    /**
     * 获取监控对象Map --车辆信息是比较全的信息 若是人和物、只有绑定信息和基础信息
     * @param monitorIds 监控对象ID
     * @param fields     为空查询全部
     * @return monitorId-VehicleDTO的映射关系
     */
    public static Map<String, VehicleDTO> getVehicleMap(Collection<String> monitorIds, String... fields) {
        return getVehicleMap(MonitorUtils::buildVehicle, monitorIds, fields);
    }

    /**
     * 从redis取出绑定信息，并封装成一个BindDTO对象，避免直接操作map出现字段写错的情况
     * @param monitorIds 监控对象id集合
     * @param fields     需要查询的字段
     */
    public static Map<String, BindDTO> getBindDTOMap(Collection<String> monitorIds, String... fields) {
        return getVehicleMap(monitor -> mapToClass(monitor, BindDTO.class), monitorIds, fields);
    }

    /**
     * 检查监控对象是否在线
     * @param monitorId 监控对象ID
     * @return true 在线 false 不在线
     */
    public static boolean isOnLine(String monitorId) {
        String statusInfo = RedisHelper.getString(HistoryRedisKeyEnum.MONITOR_STATUS.of(monitorId));
        if (StringUtils.isNotBlank(statusInfo)) {
            ClientVehicleInfo clientVehicleInfo = JSON.parseObject(statusInfo, ClientVehicleInfo.class);
            return !Objects.equals(clientVehicleInfo.getVehicleStatus(), 3);
        }
        return false;
    }

    /**
     * 获取监控对象的位置信息
     * @param monitorIds 监控对象ID集合
     * @return 监控对象ID-监控对象位置消息Map
     */
    public static Map<String, Message> getLocationMap(Collection<String> monitorIds) {
        if (CollectionUtils.isEmpty(monitorIds)) {
            return new HashMap<>(16);
        }
        List<RedisKey> redisKeys = HistoryRedisKeyEnum.MONITOR_LOCATION.ofs(monitorIds);
        List<String> locationStrList = RedisHelper.batchGetString(redisKeys);
        Map<String, Message> result = Maps.newHashMapWithExpectedSize(locationStrList.size());
        for (String location : locationStrList) {
            Message message = JSON.parseObject(location, Message.class);
            if (Objects.isNull(message.getDesc())) {
                continue;
            }
            result.put(message.getDesc().getMonitorId(), message);
        }
        return result;
    }

    /**
     * 获取监控对象的映射关系
     * @param monitorIds 监控对象ID集合
     * @param keyField   作为key的查询字段
     * @param valueField 作为value的查询字段
     * @return keyField-valueField对应值组成的Map
     */
    public static Map<String, String> getKeyValueMap(Collection<String> monitorIds, String keyField,
        String valueField) {
        return RedisHelper.batchGetHashMap(RedisKeyEnum.MONITOR_INFO.ofs(monitorIds), keyField, valueField);
    }

    private static <T> T mapToClass(Map<String, String> map, Class<T> cls) {
        return JSONObject.parseObject(JSONObject.toJSONString(map), cls);
    }

    /**
     * 获取最后一条位置信息
     */
    public static Map<String, LocationInfo> getLastLocationMap(Collection<String> monitorIds) {
        final Map<String, LocationInfo> result = new HashMap<>(CommonUtil.ofMapCapacity(monitorIds.size()));

        List<Message> messages = RedisHelper
            .batchGetStringObj(HistoryRedisKeyEnum.MONITOR_LOCATION.ofs(new HashSet<>(monitorIds)), Message.class);

        if (CollectionUtils.isEmpty(messages)) {
            return result;
        }
        for (Message message : messages) {
            T808Message t808Message = JSON.parseObject(message.getData().toString(), T808Message.class);
            LocationInfo info = JSON.parseObject(t808Message.getMsgBody().toString(), LocationInfo.class);
            result.put(info.getMonitorInfo().getMonitorId(), info);
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.println(FuzzySearchUtil.buildFuzzySearchBindMonitorKey("我"));
    }
}

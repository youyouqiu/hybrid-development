package com.zw.platform.basic.rediscache;

import com.google.common.collect.Maps;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.domain.DeviceDO;
import com.zw.platform.basic.domain.DeviceInfoDo;
import com.zw.platform.basic.domain.DeviceListDO;
import com.zw.platform.basic.dto.DeviceDTO;
import com.zw.platform.util.FuzzySearchUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author: zjc
 * @Description:终端redis缓存操作类
 * @Date: create in 2020/11/4 14:30
 */
public class DeviceRedisCache {
    private static final RedisKey FUZZY_KEY = RedisKeyEnum.FUZZY_MONITOR_DEVICE_SIMCARD.of();
    private static final RedisKey DEVICE_SORT = RedisKeyEnum.DEVICE_SORT_LIST.of();
    private static final String ALL = "*";

    public static void addDeviceCache(DeviceDTO deviceDTO) {
        String orgId = deviceDTO.getOrgId();
        String id = deviceDTO.getId();
        // 维护设备顺序
        RedisHelper.addToListTop(DEVICE_SORT, id);
        //维护企业终端缓存
        RedisHelper.addToSet(RedisKeyEnum.ORG_DEVICE.of(orgId), id);
        //维护企业下未绑定的终端缓存
        RedisHelper.addToSet(RedisKeyEnum.ORG_UNBIND_DEVICE.of(orgId), id);
        //维护模糊搜索缓存
        RedisHelper.addToHash(FUZZY_KEY, FuzzySearchUtil.buildDevice(deviceDTO.getDeviceNumber(), id));
    }

    public static void updateDeviceCache(DeviceDTO deviceDTO, String bindMonitorId, DeviceInfoDo beforeDevice) {
        // 终端所属组织id
        String beforeOrgId = beforeDevice.getOrgId();
        String nowOrgId = deviceDTO.getOrgId();
        String id = deviceDTO.getId();
        //企业变更
        if (!beforeOrgId.equals(nowOrgId)) {
            //在新企业下新增
            RedisHelper.addToSet(RedisKeyEnum.ORG_UNBIND_DEVICE.of(nowOrgId), id);
            RedisHelper.addToSet(RedisKeyEnum.ORG_DEVICE.of(nowOrgId), id);
            //在老企业下删除
            RedisHelper.delSetItem(RedisKeyEnum.ORG_UNBIND_DEVICE.of(beforeOrgId), id);
            RedisHelper.delSetItem(RedisKeyEnum.ORG_DEVICE.of(beforeOrgId), id);
        }

        boolean isBind = Objects.nonNull(bindMonitorId);
        if (isBind) {
            //更新绑定信息缓存
            RedisHelper.addToHash(RedisKeyEnum.MONITOR_INFO.of(bindMonitorId), getUpdateCacheInfo(deviceDTO));
        }

        //维护模糊搜索缓存，终端绑定后不允许修改终端编号，所以不用考虑绑定的情况
        String deviceNum = deviceDTO.getDeviceNumber();
        String beforeDeviceNum = beforeDevice.getDeviceNumber();
        if (!Objects.equals(deviceNum, beforeDeviceNum)) {
            String beforeFiled = FuzzySearchUtil.buildDeviceField(beforeDeviceNum);
            RedisHelper.hdel(FUZZY_KEY, beforeFiled);
            RedisHelper.addToHash(FUZZY_KEY, FuzzySearchUtil.buildDevice(deviceNum, deviceDTO.getId()));
        }
    }

    private static Map<String, String> getUpdateCacheInfo(DeviceDTO deviceDTO) {
        Map<String, String> bindDeviceInfo = Maps.newHashMapWithExpectedSize(7);
        bindDeviceInfo.put("terminalManufacturer", deviceDTO.getTerminalManufacturer());
        bindDeviceInfo.put("terminalType", deviceDTO.getTerminalType());
        bindDeviceInfo.put("terminalTypeId", deviceDTO.getTerminalTypeId());
        bindDeviceInfo.put("functionalType", deviceDTO.getFunctionalType());
        bindDeviceInfo.put("deviceOrgId", deviceDTO.getOrgId());
        bindDeviceInfo.put("deviceNumber", deviceDTO.getDeviceNumber());
        if (StringUtils.isNotBlank(deviceDTO.getManufacturerId())) {
            bindDeviceInfo.put("manufacturerId", deviceDTO.getManufacturerId());
        }
        Integer isVideo = deviceDTO.getIsVideo();
        isVideo = isVideo == null ? 0 : isVideo;
        bindDeviceInfo.put("isVideo", String.valueOf(isVideo));
        return bindDeviceInfo;
    }

    public static void deleteCache(String id, DeviceInfoDo device) {
        //在老企业下删除
        RedisHelper.delSetItem(RedisKeyEnum.ORG_UNBIND_DEVICE.of(device.getOrgId()), device.getId());
        RedisHelper.delSetItem(RedisKeyEnum.ORG_DEVICE.of(device.getOrgId()), device.getId());
        // 维护设备顺序
        RedisHelper.delListItem(DEVICE_SORT, id);
        //删除模糊搜索的缓存
        RedisHelper.hdel(FUZZY_KEY, FuzzySearchUtil.buildDeviceField(device.getDeviceNumber()));
    }

    public static void addImportCache(List<DeviceDO> list) {
        List<String> sortList = new ArrayList<>();
        Map<RedisKey, Collection<String>> orgDeviceSetMap = new HashMap<>();
        Map<RedisKey, Collection<String>> unbindDeviceOrgMapSet = new HashMap<>();
        Map<String, String> fuzzyMap = new HashMap<>();
        RedisKey orgIdDeviceKey;
        RedisKey unbindOrgDeviceKey;
        Collection<String> deviceSet;
        Collection<String> unbindDeviceSet;
        for (DeviceDO deviceDo : list) {
            orgIdDeviceKey = RedisKeyEnum.ORG_DEVICE.of(deviceDo.getOrgId());
            unbindOrgDeviceKey = RedisKeyEnum.ORG_UNBIND_DEVICE.of(deviceDo.getOrgId());
            //初始化相关参数
            sortList.add(deviceDo.getId());
            deviceSet = Optional.ofNullable(orgDeviceSetMap.get(orgIdDeviceKey)).orElse(new HashSet<>());
            unbindDeviceSet =
                Optional.ofNullable(unbindDeviceOrgMapSet.get(unbindOrgDeviceKey)).orElse(new HashSet<>());
            deviceSet.add(deviceDo.getId());
            unbindDeviceSet.add(deviceDo.getId());
            //存储结果
            orgDeviceSetMap.put(orgIdDeviceKey, deviceSet);
            unbindDeviceOrgMapSet.put(unbindOrgDeviceKey, unbindDeviceSet);
            fuzzyMap.putAll(FuzzySearchUtil.buildDevice(deviceDo.getDeviceNumber(), deviceDo.getId()));
        }
        // 维护设备顺序
        RedisHelper.addToListTop(DEVICE_SORT, sortList);
        //维护企业终端缓存

        RedisHelper.batchAddToSet(orgDeviceSetMap);

        //维护企业下未绑定的终端缓存
        RedisHelper.batchAddToSet(unbindDeviceOrgMapSet);
        //维护模糊搜索缓存
        RedisHelper.addToHash(FUZZY_KEY, fuzzyMap);
    }

    public static void deleteDevicesCache(List<DeviceListDO> deviceList) {
        List<String> sortList = new ArrayList<>();
        Map<RedisKey, Collection<String>> orgDeviceSetMap = new HashMap<>();
        Map<RedisKey, Collection<String>> unbindDeviceOrgMapSet = new HashMap<>();
        Set<String> fuzzySet = new HashSet<>();
        RedisKey orgIdDeviceKey;
        RedisKey unbindOrgDeviceKey;
        Collection<String> deviceSet;
        Collection<String> unbindDeviceSet;
        for (DeviceListDO deviceDo : deviceList) {
            orgIdDeviceKey = RedisKeyEnum.ORG_DEVICE.of(deviceDo.getOrgId());
            unbindOrgDeviceKey = RedisKeyEnum.ORG_UNBIND_DEVICE.of(deviceDo.getOrgId());
            //初始化相关参数
            sortList.add(deviceDo.getId());
            deviceSet = Optional.ofNullable(orgDeviceSetMap.get(orgIdDeviceKey)).orElse(new HashSet<>());
            unbindDeviceSet =
                Optional.ofNullable(unbindDeviceOrgMapSet.get(unbindOrgDeviceKey)).orElse(new HashSet<>());
            deviceSet.add(deviceDo.getId());
            unbindDeviceSet.add(deviceDo.getId());
            //存储结果
            orgDeviceSetMap.put(orgIdDeviceKey, deviceSet);
            unbindDeviceOrgMapSet.put(unbindOrgDeviceKey, unbindDeviceSet);
            fuzzySet.add(FuzzySearchUtil.buildDeviceField(deviceDo.getDeviceNumber()));
        }
        // 维护设备顺序
        RedisHelper.delListItem(DEVICE_SORT, sortList);
        //维护企业终端缓存
        RedisHelper.batchDelSet(orgDeviceSetMap);
        //维护企业下未绑定的终端缓存
        RedisHelper.batchDelSet(unbindDeviceOrgMapSet);
        //维护模糊搜索缓存
        RedisHelper.hdel(FUZZY_KEY, fuzzySet);

    }

    public static void clearCache() {
        //删除终端顺序缓存
        RedisHelper.delete(DEVICE_SORT);
        //删除企业与终端关系缓存
        RedisHelper.delByPattern(RedisKeyEnum.ORG_DEVICE.of(ALL));
        //删除企业与未绑定终端关系缓存
        RedisHelper.delByPattern(RedisKeyEnum.ORG_UNBIND_DEVICE.of(ALL));
    }

    /**
     * 初始化终端的redis缓存
     * @param sortIds    终端顺序列表
     * @param deviceList 终端详情列表
     */
    public static void initCache(List<String> sortIds, List<DeviceDTO> deviceList) {
        RedisHelper.addToListTop(DEVICE_SORT, sortIds);

        //未绑定终端模糊搜索的keyValue
        Map<String, String> fuzzyMap = new HashMap<>(deviceList.size());
        //企业与终端的关系缓存Map
        Map<RedisKey, Collection<String>> orgDeviceMap = new HashMap<>(16);
        //企业与未绑定终端关系缓存Map
        Map<RedisKey, Collection<String>> orgUnbindDeviceMap = new HashMap<>(16);
        for (DeviceDTO deviceDTO : deviceList) {
            String deviceId = deviceDTO.getId();
            String orgId = deviceDTO.getOrgId();
            //企业与终端的关系缓存Map
            Collection<String> orgDeviceSet =
                orgDeviceMap.getOrDefault(RedisKeyEnum.ORG_DEVICE.of(orgId), new HashSet<>());
            orgDeviceSet.add(deviceId);
            orgDeviceMap.put(RedisKeyEnum.ORG_DEVICE.of(orgId), orgDeviceSet);
            if (StringUtils.isNotBlank(deviceDTO.getBindId())) {
                continue;
            }
            //未绑定终端模糊搜索的键值对
            fuzzyMap.putAll(FuzzySearchUtil.buildDevice(deviceDTO.getDeviceNumber(), deviceDTO.getId()));
            //企业与未绑定终端的关系缓存Map
            Collection<String> orgUnbindIds =
                orgUnbindDeviceMap.getOrDefault(RedisKeyEnum.ORG_UNBIND_DEVICE.of(orgId), new HashSet<>());
            orgUnbindIds.add(deviceId);
            orgUnbindDeviceMap.put(RedisKeyEnum.ORG_UNBIND_DEVICE.of(orgId), orgUnbindIds);
        }
        //维护模糊搜索缓存
        RedisHelper.addToHash(FUZZY_KEY, fuzzyMap);
        //维护企业与终端的缓存关系
        RedisHelper.batchAddToSet(orgDeviceMap);
        //维护企业与未绑定终端的缓存关系
        RedisHelper.batchAddToSet(orgUnbindDeviceMap);
    }

    /**
     * 获取企业下未绑定的终端ID
     * @param orgIds  企业
     * @param keyword 关键字 可为空
     * @return 符合条件的有序终端ID
     */
    public static List<String> getUnbind(List<String> orgIds, String keyword) {
        if (CollectionUtils.isEmpty(orgIds)) {
            return new ArrayList<>();
        }
        //获取企业下未绑定的终端ID
        List<RedisKey> orgUnbindKey =
            orgIds.stream().map(RedisKeyEnum.ORG_UNBIND_DEVICE::of).collect(Collectors.toList());
        Set<String> ownIds = RedisHelper.batchGetSet(orgUnbindKey);
        if (StringUtils.isNotBlank(keyword) && !ownIds.isEmpty()) {
            Set<String> fuzzyIds = FuzzySearchUtil.scanUnbind(keyword, FuzzySearchUtil.DEVICE_TYPE);
            //两者求交集得到符合条件的终端ID
            ownIds.retainAll(fuzzyIds);
        }
        if (ownIds.isEmpty()) {
            return new ArrayList<>();
        }
        //进行排序
        List<String> sortIds = RedisHelper.getList(DEVICE_SORT);
        List<String> result = new ArrayList<>();
        for (String id : sortIds) {
            if (ownIds.contains(id)) {
                result.add(id);
            }
        }
        return result;
    }
}

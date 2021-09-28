package com.zw.platform.basic.dto;

import com.zw.platform.basic.constant.MonitorTypeEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.domain.netty.DeviceUnbound;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * 信息配置删除操作的中间对象
 *
 * @author zhangjuan
 */
@Getter
@ToString
public class ConfigUpdateDTO {
    @Setter
    private List<BindDTO> configList;
    /**
     * 涉及到监控对象
     */
    private Set<String> monitorIds;

    private Set<String> vehicleIds;

    private Set<String> peopleIds;
    private Set<String> thingIds;

    /**
     * 信息配置绑定ID
     */
    private Set<String> configIds;
    /**
     * 信息配置需要删除的模糊搜索缓存
     */
    private Set<String> delFuzzyField;

    /**
     * 信息配置需要新增的模糊搜索缓存
     */
    private Map<String, String> addFuzzyMap;

    /**
     * 解绑的监控对象信息缓存
     */
    private List<RedisKey> unBindMonitorKeyList;

    /**
     * 添加set类型的未绑定--终端和SIM卡
     */
    private Map<RedisKey, Collection<String>> addOrgUnbindSetMap;


    /**
     * 添加hash类型的未绑定--监控对象
     */
    private Map<RedisKey, Map<String, String>> addOrgUnbindHashMap;

    /**
     * 需要删除的redis
     */
    private List<RedisKey> delRedisKeyList;

    /**
     * 需要删除部分值的redis
     */
    private Map<RedisKey, Collection<String>> delHashRedisMap;

    /**
     * 服务周期ID
     */
    private Set<String> lifecycleSet;

    private List<DeviceUnbound> deviceUnboundList;

    private StringBuilder deleteMsg;

    /**
     * 批量删除完成标识
     */
    private CountDownLatch batchDeleteSuccess;

    private Map<RedisKey, Collection<String>> delSetRedisMap;


    public ConfigUpdateDTO() {
        this.monitorIds = new HashSet<>();
        this.vehicleIds = new HashSet<>();
        this.peopleIds = new HashSet<>();
        this.thingIds = new HashSet<>();
        this.delFuzzyField = new HashSet<>();
        this.addFuzzyMap = new HashMap<>();
        this.unBindMonitorKeyList = new ArrayList<>();
        this.addOrgUnbindSetMap = new HashMap<>();
        this.addOrgUnbindHashMap = new HashMap<>();
        this.delRedisKeyList = new ArrayList<>();
        this.lifecycleSet = new HashSet<>();
        this.deviceUnboundList = new ArrayList<>();
        this.deleteMsg = new StringBuilder();
        this.batchDeleteSuccess = new CountDownLatch(1);
        this.configIds = new HashSet<>();
        this.delHashRedisMap = new HashMap<>();
        this.delSetRedisMap = new HashMap<>();
    }

    public void countDown() {
        this.batchDeleteSuccess.countDown();
    }

    public void addDeviceUnbound(DeviceUnbound deviceUnbound) {
        this.deviceUnboundList.add(deviceUnbound);
    }

    public void addLog(String msg) {
        this.deleteMsg.append(msg);
    }

    public void addMonitor(String monitorId, String moType) {
        this.monitorIds.add(monitorId);
        if (Objects.equals(moType, MonitorTypeEnum.VEHICLE.getType())) {
            this.vehicleIds.add(monitorId);
        } else if (Objects.equals(moType, MonitorTypeEnum.PEOPLE.getType())) {
            this.peopleIds.add(monitorId);
        } else if (Objects.equals(moType, MonitorTypeEnum.THING.getType())) {
            this.peopleIds.add(monitorId);
        }
    }

    public void addConfig(String configId) {
        this.configIds.add(configId);
    }

    public void addDeleteRedis(RedisKey redisKey) {
        this.delRedisKeyList.add(redisKey);
    }

    public void addDelHashRedisMap(RedisKey redisKey, String delValue) {
        Collection<String> delValues = this.delHashRedisMap.get(redisKey);
        if (delValues == null) {
            delValues = new HashSet<>();
        }
        delValues.add(delValue);
        this.delHashRedisMap.put(redisKey, delValues);
    }

    public void addDelSetRedisMap(RedisKey redisKey, String delValue) {
        Collection<String> delValues = this.delSetRedisMap.getOrDefault(redisKey, new HashSet<>());
        delValues.add(delValue);
        this.delSetRedisMap.put(redisKey, delValues);
    }

    public void addUnBindMonitorKey(String monitorId) {
        this.unBindMonitorKeyList.add(RedisKeyEnum.MONITOR_INFO.of(monitorId));
    }

    public void addDelFuzzyField(String filed) {
        this.delFuzzyField.add(filed);
    }

    public void putToAddFuzzyMap(Map<String, String> fuzzyMap) {
        this.addFuzzyMap.putAll(fuzzyMap);
    }

    public void putToOrgUnbindSetMap(RedisKey redisKey, String id) {
        Collection<String> ids = addOrgUnbindSetMap.get(redisKey);
        if (ids == null) {
            ids = new HashSet<>();
        }
        ids.add(id);
        this.addOrgUnbindSetMap.put(redisKey, ids);
    }

    public void putToAddOrgUnbindHashMap(RedisKey redisKey, String monitorId, String value) {
        Map<String, String> fieldMap = this.addOrgUnbindHashMap.get(redisKey);
        if (fieldMap == null) {
            fieldMap = new HashMap<>(16);
        }
        fieldMap.put(monitorId, value);
        this.addOrgUnbindHashMap.put(redisKey, fieldMap);
    }

    public void addLifecycle(String lifecycleId) {
        this.lifecycleSet.add(lifecycleId);
    }
}

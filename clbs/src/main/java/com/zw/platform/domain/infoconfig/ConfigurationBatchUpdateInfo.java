package com.zw.platform.domain.infoconfig;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.zw.platform.domain.infoconfig.form.AssignmentVehicleForm;
import com.zw.platform.domain.netty.DeviceUnbound;
import com.zw.ws.common.MessageEncapsulationHelper;
import com.zw.ws.entity.vehicle.ClientVehicleInfo;
import lombok.Data;
import redis.clients.jedis.Jedis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * 信息配置批量解绑实体类
 * @author zhouzongbo on 2019/6/17 11:47
 */
@Data
public class ConfigurationBatchUpdateInfo implements Serializable {

    private static final long serialVersionUID = 6696023104177477270L;
    /**
     * 批量删除完成标识
     */
    private CountDownLatch batchDeleteSuccess;
    /**
     * IP地址
     */
    private String ip;
    /**
     * 监控对象ID
     */
    private Set<String> monitorIds;
    private Set<String> deviceIds;
    private Set<String> simCardIds;
    /**
     * 联动策略/音视频 需要删除音视频和报警联动策略的监控对象
     */
    private Set<String> needRemoveMonitors;

    /**
     * 信息配置表ID
     */
    private List<String> configIdList;

    /**
     * 分组和监控对象绑定关系
     */
    private List<AssignmentVehicleForm> deleteAssignmentMonitorList;

    /**
     * 监控对象状态信息
     */
    private List<ClientVehicleInfo> clientVehicleList;

    /**
     * 监控对象状态信息消息实体{@link MessageEncapsulationHelper} webSocketMessageEncapsulation()
     */
    private List<String> webSocketMessageList;

    /**
     * 绑定关系表: key: 监控对象ID, value: 绑定关系
     */
    private Map<String, ConfigList> configLists;
    /**
     * 服务周期集合
     */
    private Set<String> lifecycleSet;

    /**
     * Redis10分区需要删除的数据
     */
    private Map<String, List<String>> redisTenDelKey;
    /**
     * Redis10分区需要添加的数据
     */
    private Map<String, Map<String, String>> redisTenAddKey;
    private Map<String, JSONObject> monitorMap;
    private Set<DeviceUnbound> deviceSendSet;
    private Jedis redisZero;

    /**
     * 涉及分区: 0/5/7//912/13
     */
    private Map<Integer, List<String>> redisDelMap;

    public ConfigurationBatchUpdateInfo(Map<String, ConfigList> configLists, Jedis redisZero) {
        this.batchDeleteSuccess = new CountDownLatch(2);
        this.configLists = configLists;
        this.redisZero = redisZero;
        this.deviceIds = new HashSet<>();
        this.simCardIds = new HashSet<>();
        this.monitorIds = new HashSet<>();
        this.webSocketMessageList = new ArrayList<>();
        this.deviceSendSet = new HashSet<>();
        this.redisDelMap = new HashMap<>(32);
        this.needRemoveMonitors = new HashSet<>();
        this.configIdList = new ArrayList<>();
        this.lifecycleSet = new HashSet<>();
    }

    public void countDown() {
        this.batchDeleteSuccess.countDown();
    }

    /**
     * 缓存需要删除的key
     * @param redisKey  redisKey
     * @param partition 分区
     */
    public void putToRedisDelMap(String redisKey, Integer partition) {
        this.redisDelMap.computeIfAbsent(partition, x -> Lists.newArrayList()).add(redisKey);
    }

    public void addDeviceId(String deviceId) {
        this.deviceIds.add(deviceId);
    }

    public void addSimCardId(String simCardId) {
        this.simCardIds.add(simCardId);
    }

    public void addMonitorId(String monitorId) {
        this.monitorIds.add(monitorId);
    }

    public void addWebSocketMessageList(String message) {
        this.webSocketMessageList.add(message);
    }

    public void addDeviceSendSet(DeviceUnbound deviceUnbound) {
        this.deviceSendSet.add(deviceUnbound);
    }

    public void addNeedRemoveMonitors(String monitorId) {
        this.needRemoveMonitors.add(monitorId);
    }

    public void addConfigId(String configId) {
        this.configIdList.add(configId);
    }

    public void addLifecycleSet(String lifecycleId) {
        this.lifecycleSet.add(lifecycleId);
    }
}

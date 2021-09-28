package com.zw.platform.service.sensor;

import com.zw.platform.domain.param.RemoteUpgradeTask;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

/**
 * @author zhouzongbo on 2019/1/17 11:11
 */
public class RemoteUpgradeInstance {

    private static RemoteUpgradeInstance remoteUpgradeInstance;

    /**
     * 最大同时升级的终端数
     */
    private static final int MAX_SEMAPHORE_NUMBER = 5;
    /**
     * 远程升级任务task
     */
    private static Map<String, RemoteUpgradeTask> remoteUpgradeTaskMap;
    /**
     * 当前用户终端升级个数
     */
    private static Map<String, Set<String>> userUpgrades;

    /**
     * 用户同时升级的终端数量
     */
    private static Map<String, Semaphore> userSemaphore;

    private RemoteUpgradeInstance() {
        remoteUpgradeTaskMap = new ConcurrentHashMap<>(16);
        userUpgrades = new ConcurrentHashMap<>();
        userSemaphore = new ConcurrentHashMap<>();
    }

    public static RemoteUpgradeInstance getInstance() {
        if (remoteUpgradeInstance == null) {
            remoteUpgradeInstance = new RemoteUpgradeInstance();
        }
        return remoteUpgradeInstance;
    }

    public RemoteUpgradeTask getRemoteUpgradeTask(String deviceId) {
        return remoteUpgradeTaskMap.get(deviceId);
    }

    public void putRemoteUpgradeTask(String deviceId, RemoteUpgradeTask remoteUpgradeTask) {
        remoteUpgradeTaskMap.put(deviceId, remoteUpgradeTask);
    }

    public void removeAll(List<String> deviceIdList) {
        if (MapUtils.isNotEmpty(remoteUpgradeTaskMap)) {
            for (String deviceId : deviceIdList) {
                remoteUpgradeTaskMap.remove(deviceId);
            }
        }
    }

    /**
     * 移除单个任务
     * @param deviceId deviceId
     */
    public void removeUpgradeTask(String deviceId) {
        remoteUpgradeTaskMap.remove(deviceId);
    }

    public boolean isContains(String deviceId) {
        return remoteUpgradeTaskMap.containsKey(deviceId);
    }

    public void putUserUpgrade(String user, String deviceId) {
        userUpgrades.computeIfAbsent(user, k -> new HashSet<>()).add(deviceId);
    }

    /**
     * 当前用户正在升级终端个数
     * @param userId userId
     * @return
     */
    public Set<String> getUserUpgrades(String userId) {
        Set<String> userUpgradeSet = userUpgrades.get(userId);
        if (CollectionUtils.isNotEmpty(userUpgradeSet)) {
            return userUpgradeSet;
        }
        return new HashSet<>();
    }

    /**
     * 正在升级总终端数
     * @return int
     */
    public Set<String> getTotalUpgrades() {
        Set<String> totalUpgrades = new HashSet<>();
        Collection<Set<String>> values = userUpgrades.values();
        if (CollectionUtils.isNotEmpty(values)) {
            for (Set<String> value : values) {
                totalUpgrades.addAll(value);
            }
        }
        return totalUpgrades;
    }

    public void remove(String userId, String deviceId) {
        Set<String> userUpgradeSet = getUserUpgrades(userId);
        if (CollectionUtils.isNotEmpty(userUpgradeSet)) {
            userUpgradeSet.remove(deviceId);
            userUpgrades.putIfAbsent(userId, userUpgradeSet);
        }
    }

    /**
     * 获取用户的
     * @param userId userId
     * @return Semaphore
     */
    public synchronized Semaphore getUserSemaphore(String userId) {
        if (userSemaphore.containsKey(userId)) {
            // Semaphore semaphore = ;
            // if (semaphore.availablePermits() > MAX_SEMAPHORE_NUMBER) {
            //     // 释放多余的信号量, 保证一个用户最多拥有5个信号量s
            //     semaphore.release(semaphore.availablePermits() - MAX_SEMAPHORE_NUMBER);
            // }
            return userSemaphore.get(userId);
        } else {
            Semaphore semaphore = new Semaphore(MAX_SEMAPHORE_NUMBER, true);
            userSemaphore.put(userId, semaphore);
            return semaphore;
        }
    }
}

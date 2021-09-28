package com.zw.platform.push.common;

import com.zw.platform.commons.HashSetValueMap;
import com.zw.platform.util.ConstantUtil;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import com.zw.ws.entity.OilSupplementRequestData;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

//订阅关系维护，与页面session关联，缺点是当session数量增多，内存消耗也会增多
//后续可考虑直接维护用户与监控对象的映射关系，再维护session与用户的映射关系
public enum WsSessionManager {
    //
    INSTANCE;

    //用户会话关系 sessionId <-> 用户
    private final Map<String, String> sessionUsers;

    //位置信息订阅关系 sessionId <-> 终端id
    private final HashSetValueMap<String, String> position;

    //状态信息订阅关系 sessionId <-> 监控对象id
    private final HashSetValueMap<String, String> status;

    /**
     * 驾驶员身份库订阅关系
     * 下发指令订阅：vehicleId_下发流水号 <-> sessionId
     * 查询指令订阅：vehicleId <-> sessionId（多个id，逗号隔开）
     */
    private final Map<String, String> driverDiscern;

    /**
     * 平台巡检
     */
    private final HashMap<String, String> inspection;

    /**
     * 油补 补发数据状态
     * 油补协议没有流水号,应答就找不到对于的关系
     * 所以一个车走完一个流程才能再次补发,没有走完点击补发直接返回失败状态
     * 车牌号 -> OilSupplementSendNumber(sessionId和发送次数)
     */
    private final Map<String, OilSupplementRequestData> oilSupplementMap;

    WsSessionManager() {
        sessionUsers = new ConcurrentHashMap<>();
        position = new HashSetValueMap<>();
        status = new HashSetValueMap<>();
        driverDiscern = new HashMap<>();
        inspection = new HashMap<>();
        oilSupplementMap = new ConcurrentHashMap<>();
    }

    private Set<String> addSubscription(String session, Set<String> objectIds) {
        position.putAll(session, objectIds);
        //不管是否已经存在订阅，都将订阅关系发送给F3
        return objectIds.stream()
            .map(id -> ConstantUtil.PREFIX_POSITION + id)
            .collect(Collectors.toSet());
    }

    public Set<String> removeSubscription(String session, Set<String> objectIds) {
        Set<String> removals = new HashSet<>(objectIds.size());
        for (String objectId : objectIds) {
            //移除指定session与该对象的订阅关系,如果没有其它session有该对象的订阅关系,则取消对该对象的订阅
            if (position.remove(session, objectId)) {
                removals.add(ConstantUtil.PREFIX_POSITION + objectId);
            }
        }
        return removals;
    }

    public Set<String> removeSubscription(String sessionId) {
        //移除session对应的订阅关系，并临时保存这些订阅关系
        final Set<String> objectIds = position.removeKey(sessionId);
        return objectIds.stream()
            .map(id -> ConstantUtil.PREFIX_POSITION + id)
            .collect(Collectors.toSet());
    }

    public String getSessionUser(String sessionId) {
        return sessionUsers.getOrDefault(sessionId, "");
    }

    public void addSessionUser(String sessionId, String userName) {
        sessionUsers.put(sessionId, userName);
    }

    //region 位置数据相关订阅关系
    public Set<String> getPositionSessions(String deviceId) {
        return position.getKeys(deviceId);
    }

    public void addPositions(String sessionId, Set<String> deviceIds) {
        final Set<String> addList = addSubscription(sessionId, deviceIds);
        if (CollectionUtils.isNotEmpty(addList)) {
            WebSubscribeManager.getInstance().sendMsgToAll(addList, ConstantUtil.WEB_SUBSCRIPTION_ADD);
        }
    }

    public void removePositions(String sessionId, Set<String> deviceIds) {
        final Set<String> removals = removeSubscription(sessionId, deviceIds);
        if (CollectionUtils.isNotEmpty(removals)) {
            WebSubscribeManager.getInstance().sendMsgToAll(removals, ConstantUtil.WEB_SUBSCRIPTION_REMOVE);
        }
    }

    public void removePositions(String sessionId) {
        //延迟执行100ms，保证当session关闭和添加订阅几乎同时发生时，remove在add之后执行
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        final Set<String> removals = removeSubscription(sessionId);
        if (!removals.isEmpty()) {
            WebSubscribeManager.getInstance().sendMsgToAll(removals, ConstantUtil.WEB_SUBSCRIPTION_REMOVE);
        }
    }

    public Set<String> getAllPositions() {
        return position.values();
    }
    //endregion

    //region 状态数据相关订阅关系
    public Set<String> getStatusSessions(String monitorId) {
        return status.getKeys(monitorId);
    }

    public void removeStatusSessions(String monitorId) {
        status.removeValue(monitorId);
    }

    public void addStatuses(String sessionId, Set<String> monitorIds) {
        status.putAll(sessionId, monitorIds);
    }

    public void removeStatuses(String sessionId, Set<String> monitorIds) {
        status.removeAll(sessionId, monitorIds);
    }

    public Set<String> getAllStatuses() {
        return status.keySet();
    }
    //endregion

    //驾驶员识别相关订阅
    public String getDriverDiscernSession(String idAndMsgSn) {
        return driverDiscern.get(idAndMsgSn);
    }

    public void addDriverDiscern(String sessionId, String idAndMsgSns) {
        driverDiscern.put(idAndMsgSns, sessionId);
    }

    public void removeDriverDiscernSession(String idAndMsgSns) {
        driverDiscern.remove(idAndMsgSns);
    }

    //平台巡检
    public String getInspectionSession(String idAndMsgSn) {
        return inspection.get(idAndMsgSn);
    }

    public void addInspection(String idAndMsgSns, String sessionId) {
        inspection.put(idAndMsgSns, sessionId);
    }

    public Set<String> getInspectionValues() {
        return  new HashSet<>(inspection.values());
    }

    public void removeInspection(String idAndMsgSns) {
        inspection.remove(idAndMsgSns);
    }

    public void removeInspectionBySessionId(String sessionId) {
        Set<String> delKeys = new HashSet<>();
        for (Map.Entry<String, String> entry : inspection.entrySet()) {
            if (entry.getValue().equals(sessionId)) {
                delKeys.add(entry.getKey());
            }
        }
        for (String delKey : delKeys) {
            inspection.remove(delKey);
        }
    }

    public synchronized String removeBySession(String sessionId) {
        removePositions(sessionId);

        //状态订阅不用发送到F3
        status.removeKey(sessionId);

        //其它订阅关系都移除后，移除用户与session间的映射
        return sessionUsers.remove(sessionId);
    }

    public Boolean isAlreadyExistOilSupplementRequestData(String monitorName) {
        return oilSupplementMap.containsKey(monitorName);
    }

    public void addOilSupplementRequestData(String monitorName, OilSupplementRequestData oilSupplementRequestData) {
        oilSupplementMap.put(monitorName, oilSupplementRequestData);
    }

    public void removeOilSupplementRequestData(String monitorName) {
        oilSupplementMap.remove(monitorName);
    }

    public OilSupplementRequestData getOilSupplementRequestData(String monitorName) {
        if (StringUtils.isBlank(monitorName)) {
            return null;
        }
        return oilSupplementMap.get(monitorName);
    }
}

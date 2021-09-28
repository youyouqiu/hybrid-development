package com.zw.adas.push.common;

import com.zw.platform.commons.HashSetValueMap;

import java.util.Set;

public enum RiskSessionManager {
    INSTANCE;

    private final HashSetValueMap<String, String> riskMap;
    private final HashSetValueMap<String, String> riskReminder;

    RiskSessionManager() {
        riskMap = new HashSetValueMap<>();
        riskReminder = new HashSetValueMap<>();
    }

    public void subscribeRisk(String sessionId, Set<String> monitorIds) {
        riskMap.putAll(sessionId, monitorIds);
    }

    public void unsubscribeRisk(String sessionId) {
        riskMap.removeKey(sessionId);
    }

    public Set<String> getRiskSubscribers(String monitorId) {
        return riskMap.getKeys(monitorId);
    }

    public void subscribeReminders(String sessionId, Set<String> monitorIds) {
        riskReminder.putAll(sessionId, monitorIds);
    }

    public Set<String> getReminders(String monitorId) {
        return riskReminder.getKeys(monitorId);
    }

    public Set<String> removeReminders(String sessionId) {
        return riskReminder.removeKey(sessionId);
    }
}

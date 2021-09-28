package com.zw.app.service.webMaster.manager;

import java.util.Map;

/**
 * @author penghj
 * @version 1.0
 * @date 2019/2/19 14:29
 */
public interface AppObdManagerService {

    /**
     * 判断监控对象是否绑定obd传感器
     * @param monitorId
     * @return
     * @throws Exception
     */
    Map<String, Object> findIsBandObdSensor(String monitorId) throws Exception;
}

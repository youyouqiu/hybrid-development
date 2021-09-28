package com.zw.adas.service.defineSetting;

import java.util.Map;


public interface AdasActiveSafetyService {
    void sendParamSet(String vehicleIds, String ipAddress)
            throws Exception;

    Map<String, Object> sendAdasParamter(String vehicleId, String ipAddress)
            throws Exception;

    Map<String, Object> sendDsmParamter(String vehicleIds, String ipAddress)
            throws Exception;
}

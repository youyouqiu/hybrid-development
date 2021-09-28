package com.zw.app.service.webMaster.manager.impl;

import com.zw.app.annotation.AppMethodVersion;
import com.zw.app.annotation.AppServerVersion;
import com.zw.app.controller.AppVersionConstant;
import com.zw.app.service.webMaster.manager.AppObdManagerService;
import com.zw.platform.service.obdManager.OBDManagerSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author penghj
 * @version 1.0
 * @date 2019/2/19 14:30
 */
@Service
@AppServerVersion
public class AppObdManagerServiceImpl implements AppObdManagerService {

    @Autowired
    private OBDManagerSettingService obdManagerSettingService;

    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_THREE,
        url = "/clbs/app/manager/obdManager/findIsBandObdSensor")
    public Map<String, Object> findIsBandObdSensor(String monitorId) throws Exception {
        Map<String, Object> resultMap = new HashMap<>(16);
        boolean isBandObdSensor = obdManagerSettingService.findIsBandObdSensor(monitorId);
        resultMap.put("isBandObdSensor", isBandObdSensor);
        return resultMap;
    }
}

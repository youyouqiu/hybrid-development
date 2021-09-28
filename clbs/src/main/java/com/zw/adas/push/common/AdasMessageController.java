package com.zw.adas.push.common;

import com.zw.platform.util.privilege.UserPrivilegeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.util.Set;

@Controller
public class AdasMessageController {
    @Autowired
    private UserPrivilegeUtil userPrivilegeUtil;

    /**
     * 订阅主动安全的报警
     */
    @MessageMapping("/risk/security/subscribeRisk")
    public void subscribeRisk(@Header("simpSessionId") String sessionId) {
        final Set<String> vehicles = userPrivilegeUtil.getCurrentUserVehicles();
        RiskSessionManager.INSTANCE.subscribeRisk(sessionId, vehicles);
    }

    /**
     * 取消订阅主动安全的报警
     */
    @MessageMapping("/risk/security/unsubscribeRisk")
    public void unsubscribeRisk(@Header("simpSessionId") String sessionId) {
        RiskSessionManager.INSTANCE.unsubscribeRisk(sessionId);
    }

    /**
     * 订阅平台设置报警提醒
     */
    @MessageMapping("/risk/security/subVehicleUser")
    public void subVehicleUser(@Header("simpSessionId") String sessionId) {
        final Set<String> vehicles = userPrivilegeUtil.getCurrentUserVehicles();
        RiskSessionManager.INSTANCE.subscribeReminders(sessionId, vehicles);
    }
}

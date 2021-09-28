package com.zw.platform.controller.app;

import com.zw.platform.commons.Auth;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author Chen Feng
 * @version 1.0 2018/8/8
 */
@Controller
@RequestMapping("/m/app/group")
public class GroupAlarmTypeController {
    private static final String GROUP_ALARM_TYPE_CONFIG_PAGE = "modules/intercomplatform/app/groupAlarmType";

    @Auth
    @RequestMapping(value = "/alarmType/page", method = RequestMethod.GET)
    public String configPage() {
        return GROUP_ALARM_TYPE_CONFIG_PAGE;
    }
}

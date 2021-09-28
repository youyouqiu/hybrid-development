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
@RequestMapping("/m/app")
public class AppConfigController {
    private static final String APP_CONFIG_PAGE = "modules/intercomplatform/app/appSetting";

//    @Auth
//    @RequestMapping(value = "/personalized/page", method = RequestMethod.GET)
//    public String configPage() {
//        return APP_CONFIG_PAGE;
//    }
}

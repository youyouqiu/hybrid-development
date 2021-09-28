package com.zw.platform.controller.monitoring;

import com.zw.platform.commons.Auth;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/v/multiWindowmonitoring")
public class MultiWindowMonitoringController {

    private static final String MULTI_PAGE = "vas/monitoring/multiWindowMonitoring";

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public ModelAndView multi() {
        ModelAndView mv = new ModelAndView(MULTI_PAGE);
        return mv;
    }
}

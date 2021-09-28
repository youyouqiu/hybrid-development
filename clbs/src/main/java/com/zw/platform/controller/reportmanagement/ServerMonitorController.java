package com.zw.platform.controller.reportmanagement;

import com.zw.platform.commons.Auth;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


@Controller
@RequestMapping("/r/reportManagement/serverMonitor")
public class ServerMonitorController {
    private static final String LIST_PAGE = "modules/reportManagement/serverMonitor/serverMonitorList";

    @Auth
    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    public String getListPage() {
        return LIST_PAGE;
    }
}

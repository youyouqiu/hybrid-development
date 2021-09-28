package com.cb.platform.contorller;

import com.zw.platform.commons.Auth;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *
 * @Author zhangqiang
 * @Date 2020/6/9 10:12
 */
@Controller
@RequestMapping("/cb/cbReportManagement/overSpeed")
public class OverSpeedController {


    private static final String LIST_PAGE = "/modules/cbReportManagement/overSpeed/list";

    @Auth
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String list() {
        return LIST_PAGE;
    }
}

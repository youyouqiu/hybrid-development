package com.zw.platform.controller.monitoring;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * 前端打印controller
 * @author zhouzongbo on 2019/5/13 16:11
 */
@Controller
@RequestMapping("/m/web/print/")
public class WebPrintController {

    private static final String PAGE_LIST = "test/webPrintList";

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ModelAndView initPrintList() {
        return new ModelAndView(PAGE_LIST);
    }
}

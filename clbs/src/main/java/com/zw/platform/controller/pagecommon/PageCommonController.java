package com.zw.platform.controller.pagecommon;

import com.zw.platform.commons.Auth;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @Author: zjc
 * @Description:页面跳转通用controller，避免每次跳转都要在类中写一个页面跳转的方法
 * @Date: create in 2020/11/10 9:38
 */
@Controller
@RequestMapping("/page")
public class PageCommonController {

    /**
     * 通用页面跳转地址
     * @param listPage
     * @return
     */
    @Auth
    @RequestMapping(method = RequestMethod.GET)
    public String listPage(String listPage) {
        return listPage;
    }

    /**
     * 通用页面跳转地址,不需要通过auth做按钮控制的，需要在调整sql菜单
     * @param listPage
     * @return
     */

    @RequestMapping("/noAuth")
    public String listNoAuthPage(String listPage) {
        return listPage;
    }

}

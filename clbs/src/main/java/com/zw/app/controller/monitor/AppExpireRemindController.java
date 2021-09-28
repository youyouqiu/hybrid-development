package com.zw.app.controller.monitor;

import com.zw.app.entity.monitor.AppExpireRemindDetailQueryEntity;
import com.zw.app.entity.monitor.AppExpireRemindQueryEntity;
import com.zw.app.service.monitor.AppExpireRemindService;
import com.zw.app.util.AppVersionUtil;
import com.zw.app.util.common.AppResultBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/***
 @Author zhengjc
 @Date 2019/11/21 17:55
 @Description app到期提醒
 @version 1.0
 **/
@Controller
@RequestMapping("/app/expireRemind")
public class AppExpireRemindController {

    @Autowired
    private AppExpireRemindService appExpireRemindService;

    @RequestMapping(value = { "/getExpireRemindInfos" }, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean getExpireRemindInfos(HttpServletRequest request, @Validated AppExpireRemindQueryEntity entity,
        BindingResult result) {
        return AppVersionUtil.getResultData(request, entity, result, appExpireRemindService);
    }

    @RequestMapping(value = { "/getExpireRemindInfoDetails" }, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean getExpireRemindInfoDetails(HttpServletRequest request,
        @Validated AppExpireRemindDetailQueryEntity entity, BindingResult result) {
        return AppVersionUtil.getResultData(request, entity, result, appExpireRemindService);
    }
}

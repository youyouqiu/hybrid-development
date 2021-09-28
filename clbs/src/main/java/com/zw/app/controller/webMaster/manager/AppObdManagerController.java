package com.zw.app.controller.webMaster.manager;

import com.zw.app.entity.methodParameter.JudgeIsBingObdSensorEntity;
import com.zw.app.service.webMaster.manager.AppObdManagerService;
import com.zw.app.util.AppVersionUtil;
import com.zw.app.util.common.AppResultBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * App OBD模块
 * @author penghj
 * @version 1.0
 * @date 2019/2/19 13:53
 */
@Controller
@RequestMapping("/app/manager/obdManager")
public class AppObdManagerController {

    @Value("${sys.error.msg}")
    private String sysError;

    @Autowired
    private AppObdManagerService appObdManagerService;

    @RequestMapping(value = "/findIsBandObdSensor", method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean findIsBandObdSensor(HttpServletRequest request,
        @Validated JudgeIsBingObdSensorEntity queryEntity, BindingResult result) {
        return AppVersionUtil.getResultData(request, queryEntity, result, appObdManagerService);
    }
}

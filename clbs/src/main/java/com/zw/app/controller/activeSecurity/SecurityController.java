package com.zw.app.controller.activeSecurity;

import com.zw.app.entity.BaseEntity;
import com.zw.app.entity.methodParameter.DayRiskDetailEntity;
import com.zw.app.entity.methodParameter.DayRiskEntity;
import com.zw.app.entity.methodParameter.DealRiskEntity;
import com.zw.app.entity.methodParameter.QueryEventsEntity;
import com.zw.app.entity.methodParameter.QueryMediaEntity;
import com.zw.app.entity.methodParameter.QueryRiskEntity;
import com.zw.app.service.activeSecurity.ActiveSecurityService;
import com.zw.app.util.AppVersionUtil;
import com.zw.app.util.common.AppResultBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;


@Controller
@RequestMapping("/app/risk/security")
public class SecurityController {
    private static Logger logger = LogManager.getLogger(SecurityController.class);

    @Autowired
    private ActiveSecurityService activeSecurityService;

    @Value("${sys.error.msg}")
    private String sysError;

    @RequestMapping(value = {"/getRiskList"}, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean getRiskList(HttpServletRequest request, @Validated QueryRiskEntity qr,
                                     BindingResult result) {
        return AppVersionUtil.getResultData(request, qr, result, activeSecurityService);
    }

    @RequestMapping(value = {"/getRiskEventByRiskId"}, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean getRiskEventByRiskId(HttpServletRequest request, @Validated QueryEventsEntity qe,
                                              BindingResult result) {
        return AppVersionUtil.getResultData(request, qe, result, activeSecurityService);
    }

    @RequestMapping(value = {"/dealInfo"}, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean dealInfo(HttpServletRequest request, @Validated BaseEntity base,
                                  BindingResult result) {
        return AppVersionUtil.getResultData(request, base, result, activeSecurityService);
    }

    @RequestMapping(value = {"/getMediaInfo"}, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean getMediaInfo(HttpServletRequest request, @Validated QueryMediaEntity qm,
                                      BindingResult result) {
        return AppVersionUtil.getResultData(request, qm, result, activeSecurityService);
    }

    @RequestMapping(value = {"/dealRisk"}, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean dealRisk(HttpServletRequest request, @Validated DealRiskEntity dr,
                                  BindingResult result) {
        return AppVersionUtil.getResultData(request, dr, result, activeSecurityService);
    }



    @RequestMapping(value = {"/getDayRiskNum"}, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean getDayRiskNum(HttpServletRequest request, @Validated DayRiskEntity dre,
                                       BindingResult result) {
        return AppVersionUtil.getResultData(request, dre, result, activeSecurityService);
    }

    @RequestMapping(value = {"/getDayRiskDetail"}, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean getDayRiskDetail(HttpServletRequest request, @Validated DayRiskDetailEntity drde,
                                          BindingResult result) {
        return AppVersionUtil.getResultData(request, drde, result, activeSecurityService);
    }

}

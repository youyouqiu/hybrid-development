package com.zw.app.controller.appOCR;

import com.zw.app.entity.appOCR.BindOcrProfessionalsEntity;
import com.zw.app.entity.appOCR.QueryOcrEntity;
import com.zw.app.entity.appOCR.QueryOcrProfessionalsEntity;
import com.zw.app.service.appOCR.AppOcrProfessionalsService;
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

@Controller
@RequestMapping("/app/ocr/professionals")
public class AppOcrProfessionalsController {

    @Autowired
    private AppOcrProfessionalsService appOcrProfessionalsService;

    @Value("${sys.error.msg}")
    private String errorMsg;

    @RequestMapping(value = "/getList", method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean getProfessionalsList(HttpServletRequest request, @Validated QueryOcrEntity query,
        BindingResult result) {
        return AppVersionUtil.getResultData(request, query, result, appOcrProfessionalsService);
    }

    @RequestMapping(value = "/getInfo", method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean getProfessionalsInfo(HttpServletRequest request, @Validated QueryOcrEntity query,
        BindingResult result) {
        return AppVersionUtil.getResultData(request, query, result, appOcrProfessionalsService);
    }

    @RequestMapping(value = "/saveInfo", method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean saveProfessionalsInfo(HttpServletRequest request, @Validated QueryOcrProfessionalsEntity query,
        BindingResult result) {
        return AppVersionUtil.getResultData(request, query, result, appOcrProfessionalsService);
    }

    @RequestMapping(value = "/bind", method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean bindProfessionalsInfo(HttpServletRequest request, @Validated BindOcrProfessionalsEntity query,
        BindingResult result) {
        return AppVersionUtil.getResultData(request, query, result, appOcrProfessionalsService);
    }
}

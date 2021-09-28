package com.zw.platform.controller.generalCargoReport;

import com.zw.platform.commons.Auth;
import com.zw.platform.domain.generalCargoReport.ViolationRecordQuery;
import com.zw.platform.service.generalCargoReport.ViolationRecordService;
import com.zw.platform.util.common.JsonResultBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/***
 @Author zhengjc
 @Date 2019/9/2 18:34
 @Description 违章处置报表
 @version 1.0
 **/
@Controller
@RequestMapping("/s/cargo/violationRecord/")
public class ViolationRecordController {
    private static final Logger logger = LogManager.getLogger(ViolationRecordController.class);

    private static final String LIST_PAGE = "modules/sdReportManagement/trafficViolationsReport";

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Autowired
    private ViolationRecordService violationRecordService;

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    /**
     * 违章记录表
     */
    @RequestMapping(value = "/getViolationRecords", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getViolationRecords(ViolationRecordQuery query) {
        try {
            return violationRecordService.getViolationRecords(query);
        } catch (Exception e) {
            logger.error("查询违章记录表异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 违章记录表-离线导出
     */
    @RequestMapping(value = "/exportViolationRecords", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean exportViolationRecords(ViolationRecordQuery query) {
        return violationRecordService.exportViolationRecords(query);
    }
}

package com.zw.platform.controller.reportmanagement;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.BigDataReport.OnlineReport;
import com.zw.platform.service.reportManagement.OnlineReportService;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.excel.ExportExcelUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Created by fanlu on 2017/3/30.
 */
@Controller
@RequestMapping("/m/reportManagement/onlineReport")
public class OnlineReportController {

    private static final String LIST_PAGE = "modules/reportManagement/onlineReport";

    private static Logger log = LogManager.getLogger(OnlineReportController.class);

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Autowired
    private OnlineReportService onlineReportService;

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    @RequestMapping(value = { "/online" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean findOnline(String vehicleList, String startTime, String endTime) {
        try {
            if (StringUtils.isNotBlank(vehicleList) && StringUtils.isNotBlank(startTime) && StringUtils
                .isNotBlank(endTime)) {
                List<OnlineReport> or = onlineReportService.findOnlineList(vehicleList, startTime, endTime);
                JSONObject objJson = new JSONObject(); // 传入JSONObject
                objJson.put("online", or);
                return new JsonResultBean(objJson); // 返回给页面
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("上线率统计页面查询监控对象上线率信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @RequestMapping(value = { "/onlineByF3Pass" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean onlineByF3Pass(String vehicleList, String startTime, String endTime) {
        try {
            if (StringUtils.isNotBlank(vehicleList) && StringUtils.isNotBlank(startTime) && StringUtils
                .isNotBlank(endTime)) {
                List<OnlineReport> onlineReports = onlineReportService.onlineByF3Pass(vehicleList, startTime, endTime);
                JSONObject object = new JSONObject();
                object.put("online", onlineReports);
                return new JsonResultBean(object);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("上线率统计页面查询监控对象上线率信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @RequestMapping(value = { "/onlineExport" }, method = RequestMethod.POST)
    @ResponseBody
    public Boolean findOnlineExport(String vehicleList, String startTime, String endTime, HttpServletResponse res) {
        try {
            onlineReportService.findOnlineList(vehicleList, startTime, endTime);
            return true;
        } catch (Exception e) {
            log.error("上线率统计页面导出数据(post)异常", e);
        }
        return false;
    }

    /**
     * 导出(生成excel文件)
     * @param res
     */
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void export(HttpServletResponse res) {
        try {
            ExportExcelUtil.setResponseHead(res, "车辆上线率报表");
            onlineReportService.export(null, 1, res);
        } catch (Exception e) {
            log.error("上线率统计页面导出数据异常(get)", e);
        }
    }
}

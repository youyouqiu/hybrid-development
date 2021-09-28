package com.zw.lkyw.controller.sendMessageReport;

import com.zw.lkyw.domain.sendMessageReport.DetailQuery;
import com.zw.lkyw.service.sendMessageReport.SendMessageReportSevice;
import com.zw.platform.commons.Auth;
import com.zw.platform.controller.core.RoleController;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.excel.ExportExcelUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 下发消息统计
 * @author denghuabing on 2019/12/23 10:44
 */
@RequestMapping("/lkyw/sendMessageReport")
@Controller
public class SendMessageReportController {

    private Logger log = LogManager.getLogger(RoleController.class);

    private static final String LIST_PAGE = "vas/lkyw/sendMessageReport/list";

    @Value("${sys.error.msg}")
    private String errorMsg;

    @Autowired
    private SendMessageReportSevice sendMessageReportSevice;

    @Auth
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String getListPage() {
        return LIST_PAGE;
    }

    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getList(String vehicleIds, String startTime, String endTime) {
        try {
            return sendMessageReportSevice.getList(vehicleIds, startTime, endTime);
        } catch (Exception e) {
            log.error("获取下发消息数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, errorMsg);
        }
    }

    @RequestMapping(value = "/export", method = RequestMethod.POST)
    public void export(String vehicleIds, String startTime, String endTime, HttpServletResponse response) {
        try {
            sendMessageReportSevice.export(vehicleIds, startTime, endTime, response);
        } catch (Exception e) {
            log.error("导出下发消息报表异常", e);
        }
    }

    @RequestMapping(value = "/detail", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getDetail(DetailQuery query, HttpServletResponse response) {
        try {
            return sendMessageReportSevice.getDetail(query, response);
        } catch (Exception e) {
            log.error("获取下发消息详情异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, errorMsg);
        }
    }

    @RequestMapping(value = "/exportDetail", method = RequestMethod.GET)
    public void exportDetail(DetailQuery query, HttpServletResponse response) {
        ExportExcelUtil.setResponseHead(response, "下发消息详情");
        try {
            sendMessageReportSevice.exportDetail(query, response);
        } catch (IOException e) {
            log.error("导出下发详情异常", e);
        }
    }
}

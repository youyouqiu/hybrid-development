package com.zw.platform.controller.reportmanagement;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.reportManagement.DrivingRecordInfo;
import com.zw.platform.service.reportManagement.DrivingRecordReportService;
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
import java.util.List;


/**
 * 行驶记录仪报表controller
 */
@Controller
@RequestMapping("/m/reportManagement/drivingRecord")
public class DrivingRecordReportController {
    private static final Logger logger = LogManager.getLogger(DrivingRecordReportController.class);

    private static final String LIST_PAGE = "/modules/reportManagement/runRecorderReport";

    @Value("${sys.error.msg}")
    private String syError;

    @Autowired
    private DrivingRecordReportService drivingRecordReportService;

    /**
     * 获取行驶记录仪报表页面
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @Auth
    public String getPage() {
        return LIST_PAGE;
    }

    /**
     * 获取行驶记录仪报表页面数据
     */
    @RequestMapping(value = "/getData", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getDrivingRecordCollection(String monitorId, String queryStartTime, String queryEndTime) {
        try {
            List<DrivingRecordInfo> result =
                drivingRecordReportService.getDrivingRecordCollection(monitorId, queryStartTime, queryEndTime);
            JSONObject msg = new JSONObject();
            msg.put("result", result);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            logger.error("获取行驶记录仪报表数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, syError);
        }
    }

    /**
     * 导出行驶记录仪报表列表数据
     */
    @RequestMapping(value = "/exportData", method = RequestMethod.GET)
    public void exportDrivingRecordData(HttpServletResponse response, String fuzzyParam) {
        try {
            ExportExcelUtil.setResponseHead(response, "行驶记录仪报表");
            drivingRecordReportService.exportDrivingRecord(response, fuzzyParam);
        } catch (Exception e) {
            logger.error("导出行驶记录仪报表数据异常", e);
        }

    }
}

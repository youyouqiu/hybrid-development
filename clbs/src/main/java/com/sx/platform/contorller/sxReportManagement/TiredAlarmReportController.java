package com.sx.platform.contorller.sxReportManagement;

import com.sx.platform.service.sxReportManagement.TiredAlarmReportService;
import com.zw.platform.commons.Auth;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.excel.ExportExcelUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;


@Controller
@RequestMapping("/sx/sxReportManagement/tiredAlarmReport")
public class TiredAlarmReportController {
    private static final String LIST_PAGE = "modules/sxReportManagement/tiredAlarmReport";

    private static Logger logger = LogManager.getLogger(TiredAlarmReportController.class);

    @Autowired
    private TiredAlarmReportService tiredAlarmReportService;

    @Auth
    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    @RequestMapping(value = {"/list"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getList(String vehicleList, String startTime, String endTime) {
        try {
            return tiredAlarmReportService.getListFromPaas(vehicleList, startTime, endTime);
        } catch (Exception e) {
            logger.error("查询疲劳报警明细", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }


    /**
     * 导出(生成excel文件)
     * @param res
     */
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void export(HttpServletResponse res) {
        try {
            ExportExcelUtil.setResponseHead(res, "疲劳驾驶报警明细报表");
            tiredAlarmReportService.export(null, 1, res);
        } catch (Exception e) {
            logger.error("疲劳驾驶报警明细报表页面导出数据异常(get)", e);
        }
    }


}

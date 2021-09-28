package com.sx.platform.contorller.sxReportManagement;

import com.sx.platform.service.sxReportManagement.SxSpeedAlarmReportService;
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
@RequestMapping("/sx/sxReportManagement/sxSpeedAlarmReport")
public class SxSpeedAlarmReportController {
    private static final String LIST_PAGE = "modules/sxReportManagement/sxSpeedAlarmReport";

    private static Logger logger = LogManager.getLogger(SxSpeedAlarmReportController.class);

    @Autowired
    private SxSpeedAlarmReportService sxSpeedAlarmReportService;

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getList(String vehicleList, String speedType, String startTime, String endTime) {
        try {
            return sxSpeedAlarmReportService.getListFromPaas(vehicleList, speedType, startTime, endTime);
        } catch (Exception e) {
            logger.error("查询超速报警明细失败", e);
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
            ExportExcelUtil.setResponseHead(res, "超速报警明细报表");
            sxSpeedAlarmReportService
                .export(null, 1, res);
        } catch (Exception e) {
            logger.error("超速报警明细报表页面导出数据异常(get)", e);
        }
    }

}

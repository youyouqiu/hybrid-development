package com.sx.platform.contorller.sxReportManagement;

import com.sx.platform.service.sxReportManagement.BeforeDawnReportService;
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
@RequestMapping("/sx/sxReportManagement/beforeDawnReport")
public class BeforeDawnReportController {
    private static final String LIST_PAGE = "modules/sxReportManagement/beforeDawnReport";

    private static Logger logger = LogManager.getLogger(BeforeDawnReportController.class);

    @Autowired
    private BeforeDawnReportService beforeDawnReportService;

    @Auth
    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    @RequestMapping(value = {"/list"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getList(String vehicleList, String startTime, String endTime) {
        try {
            return beforeDawnReportService.getListFromPaas(vehicleList, startTime, endTime);
        } catch (Exception e) {
            logger.error("查询凌晨2-5点报警失败", e);
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
            ExportExcelUtil.setResponseHead(res, "凌晨2点-5点运行报表");
            beforeDawnReportService.export(null, 1, res);
        } catch (Exception e) {
            logger.error("凌晨2点-5点运行报表导出数据异常(get)", e);
        }
    }
}

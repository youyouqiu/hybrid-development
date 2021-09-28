package com.sx.platform.contorller.sxReportManagement;

import com.sx.platform.service.sxReportManagement.LocationQualifiedRateReportService;
import com.zw.platform.commons.Auth;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.excel.ExportExcelUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;

/**
 * @author zhangsq
 * @date 2018/3/12 10:40
 */
@RestController
@RequestMapping("/sx/sxReportManagement/locationQualifiedRateReport")
public class LocationQualifiedRateReportController {

    private static final String LIST_PAGE = "modules/sxReportManagement/locationQualifiedRateReport";

    private static Logger logger = LogManager.getLogger(LocationQualifiedRateReportController.class);
    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Autowired
    private LocationQualifiedRateReportService locationQualifiedRateReportService;

    @Auth
    @RequestMapping(value = "list", method = RequestMethod.GET)
    public ModelAndView listPage() {
        return new ModelAndView(LIST_PAGE);
    }

    @RequestMapping(value = "list", method = RequestMethod.POST)
    public JsonResultBean list(String band, String startTime, String endTime) {
        try {
            return locationQualifiedRateReportService.getLocationQualifiedRateFromPaas(band, startTime, endTime);
        } catch (Exception e) {
            logger.error("获取定位数据合格率异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 导出(生成excel文件)
     *
     * @param res
     */
    @RequestMapping(value = "/export")
    public void export2(HttpServletResponse res) {
        try {
            ExportExcelUtil.setResponseHead(res, "定位数据合格率");
            locationQualifiedRateReportService.export(null, 1, res);
        } catch (Exception e) {
            logger.error(" 导出定位数据合格率报表异常", e);
        }
    }

}

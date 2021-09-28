package com.zw.platform.controller.generalCargoReport;

import com.github.pagehelper.Page;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.generalCargoReport.CargoMonthReportInfo;
import com.zw.platform.domain.generalCargoReport.CargoSearchQuery;
import com.zw.platform.service.generalCargoReport.MonthReportCargoService;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.excel.TemplateExportExcel;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 山东报表月表
 */
@Controller
@RequestMapping("/s/cargo/monthReport")
public class MonthReportCargoController {

    @Autowired
    MonthReportCargoService monthReportCargoService;

    @Autowired
    TemplateExportExcel templateExportExcel;

    @Value("${sys.error.msg}")
    private String sysError;

    private static Logger log = LogManager.getLogger(MonthReportCargoController.class);

    private static final String LIST_PAGE = "modules/sdReportManagement/monthData";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    /**
     * 获取山东月报表
     * @author lijie
     * @date 2019/9/3 10:40
     */
    @Auth
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @AvoidRepeatSubmitToken(setToken = true)
    public ModelAndView spotCheckList() {
        try {
            ModelAndView mav = new ModelAndView(LIST_PAGE);
            return mav;
        } catch (Exception e) {
            log.error("获取山东月报表界面弹出异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 查询普货抽查表数据
     * @author lijie
     * @date 2018/9/2 10:59
     */
    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean searchMonthData(CargoSearchQuery cargoSearchQuery) {
        try {
            if (StringUtils.isNotBlank(cargoSearchQuery.getTime()) && StringUtils
                .isNotBlank(cargoSearchQuery.getGroupIds()) && cargoSearchQuery.getLength() != 0
                && cargoSearchQuery.getStart() >= 0) {
                Page<CargoMonthReportInfo> feedBacks = monthReportCargoService.searchMonthData(cargoSearchQuery);
                return new PageGridBean(cargoSearchQuery, feedBacks, true);
            } else {
                return new PageGridBean(false);
            }
        } catch (Exception e) {
            log.error("查询普货抽查表数据异常", e);
            return new PageGridBean(false);
        }
    }

    /**
     * 导出(生成excel文件)
     * @param res res
     */
    @RequestMapping(value = "/export", method = RequestMethod.POST)
    public void export(HttpServletResponse res, CargoSearchQuery cargoSearchQuery) {
        try {
            List<CargoMonthReportInfo> cargoMonthReportInfos =
                monthReportCargoService.exportSearchMonthData(cargoSearchQuery);
            Map<String, Object> data = new HashMap<>();
            data.put("cargoMonthReportInfos", cargoMonthReportInfos);
            data.put("time", cargoSearchQuery.getTime().substring(5, 7));
            String fileName = "道路运输车辆动态监控数据" + cargoSearchQuery.getTime().substring(5, 7) + "月份月表";
            templateExportExcel.templateExportExcel("/file/cargoReport/道路运输车辆动态监控数据月报表模板.xls", res, data, fileName);
        } catch (Exception e) {
            log.error("导出普货月报表异常", e);
        }
    }

}

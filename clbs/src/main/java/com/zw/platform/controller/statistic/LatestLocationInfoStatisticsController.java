package com.zw.platform.controller.statistic;

import com.zw.platform.commons.Auth;
import com.zw.platform.service.statistic.LatestLocationInfoStatisticsService;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.excel.ExportExcelUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * @author penghj
 * @version 1.0
 * @date 2018/12/25 9:23
 */
@Controller
@RequestMapping("/v/statistic/latestLocationInfoStatistics")
public class LatestLocationInfoStatisticsController {
    private static final Logger log = LogManager.getLogger(LatestLocationInfoStatisticsController.class);

    private static final String LIST_PAGE = "vas/statistic/latestLocationInfoStatistics/latestLocation";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Resource
    private LatestLocationInfoStatisticsService latestLocationInfoStatisticsService;

    /**
     * 获得最新位置信息页面
     */
    @Auth
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ModelAndView getListPage() {
        try {
            return new ModelAndView(LIST_PAGE);
        } catch (Exception e) {
            log.error("获取最新位置信息报表页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 获得列表数据
     */
    @ResponseBody
    @RequestMapping(value = "/listByPass", method = RequestMethod.POST)
    public JsonResultBean listByPass(String vehicleIds, String queryTime) {
        try {
            return latestLocationInfoStatisticsService.getLatestLocationInfoByF3Pass(vehicleIds, queryTime);
        } catch (Exception e) {
            log.error("查询最新位置信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 导出
     * @param simpleQueryParam 监控对象名称
     */
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void export(HttpServletResponse response, String simpleQueryParam) {
        try {
            ExportExcelUtil.setResponseHead(response, "最新位置信息报表");
            latestLocationInfoStatisticsService.exportLatestLocationInfo(response, simpleQueryParam);
        } catch (Exception e) {
            log.error("导出车辆信息异常", e);
        }
    }
}

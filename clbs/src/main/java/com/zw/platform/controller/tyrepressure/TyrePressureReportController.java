package com.zw.platform.controller.tyrepressure;

import com.alibaba.fastjson.JSON;
import com.zw.platform.domain.statistic.StatisticQuery;
import com.zw.platform.domain.vas.workhourmgt.SensorVehicleInfo;
import com.zw.platform.service.sensor.TyrePressureReportService;
import com.zw.platform.service.workhourmgt.WorkHourStatisticsService;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * 胎压报表
 * create by denghuabing 2019.2.26
 */
@Controller
@RequestMapping("/v/statistic/tirePressureReport")
public class TyrePressureReportController {

    private Logger logger = LogManager.getLogger(TyrePressureReportController.class);

    private static final String LIST_PAGE = "vas/statistic/tirePressureReport/list";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Autowired
    private WorkHourStatisticsService workHourStatisticsService;

    @Autowired
    private TyrePressureReportService tyrePressureReportService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ModelAndView getListPage() {
        try {
            ModelAndView mav = new ModelAndView(LIST_PAGE);
            List<SensorVehicleInfo> vehicleList =
                workHourStatisticsService.getBindVehicle(SensorVehicleInfo.SENSOR_TYPE_TYRE_PRESSURE);
            mav.addObject("vehicleList", JSON.toJSONString(vehicleList));
            return mav;
        } catch (Exception e) {
            logger.error("胎压报表页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 获取总数据 存入redis
     * @param query
     * @return
     */
    @RequestMapping(value = "/getTotalInfo", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getTotalInfo(StatisticQuery query) {
        try {
            if (query != null) {
                return tyrePressureReportService.getTotalInfo(query);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("胎压报表获取车辆数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 获取图表信息
     * @param query
     * @return
     */
    @RequestMapping(value = "/getChartInfo", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getChartInfo(StatisticQuery query) {
        try {
            if (query != null) {
                return tyrePressureReportService.getChartInfo(query);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            logger.error("胎压报表获取图表信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 获取表单信息
     * @param query
     * @return
     */
    @RequestMapping(value = "/getFormInfo", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getFormInfo(StatisticQuery query) {
        try {
            if (query != null) {
                return tyrePressureReportService.getFormInfo(query);
            }
            return new PageGridBean(PageGridBean.FAULT);
        } catch (Exception e) {
            logger.error("胎压报表获取表单信息异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }
}

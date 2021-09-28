package com.zw.platform.controller.statistic;

import com.alibaba.fastjson.JSON;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.statistic.StatisticQuery;
import com.zw.platform.domain.vas.workhourmgt.SensorVehicleInfo;
import com.zw.platform.service.statistic.LoadManagementStatisticService;
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

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Objects;

/**
 * 载重报表
 * @author zhouzongbo on 2018/9/10 15:25
 */
@Controller
@RequestMapping("/v/statistic/loadManagementStatistic")
public class LoadManagementStatisticController {

    private static Logger log = LogManager.getLogger(LoadManagementStatisticController.class);

    private static final String LIST_PAGE = "vas/statistic/loadManagementStatistic/list";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Autowired
    private LoadManagementStatisticService loadManagementStatisticService;

    @Autowired
    private WorkHourStatisticsService workHourStatisticsService;

    /**
     * 载重统计列表页
     * @return ModelAndView
     */
    @Auth
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ModelAndView list() {
        try {
            ModelAndView mav = new ModelAndView(LIST_PAGE);
            List<SensorVehicleInfo> vehicleList =
                workHourStatisticsService.getBindVehicle(SensorVehicleInfo.SENSOR_TYPE_LOAD);
            mav.addObject("vehicleList", JSON.toJSONString(vehicleList));
            return mav;
        } catch (Exception e) {
            log.error("获取载重统计列表页异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 获得载重图表内容
     * @param query query
     * @return JsonResultBean
     */
    @ResponseBody
    @RequestMapping(value = "/getLoadChartInfo", method = RequestMethod.POST)
    public JsonResultBean getLoadChartInfo(StatisticQuery query) {
        try {
            if (Objects.nonNull(query)) {
                return loadManagementStatisticService.getLoadChartInfo(query);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("获得载重图表内容异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 获得载重总数据表格内容
     * @param query query status载重状态 01: 空载； 02: 满载； 03: 超载； 04: 装载； 05: 卸载；06: 轻载；07: 重载； null:所有状态
     * @return PageGridBean
     */
    @ResponseBody
    @RequestMapping(value = "/getTotalLoadInfoList", method = RequestMethod.POST)
    public PageGridBean getTotalDataFormInfo(final StatisticQuery query) {
        try {
            if (Objects.nonNull(query)) {
                return loadManagementStatisticService.getTotalLoadInfoList(query);
            }
            return new PageGridBean(PageGridBean.FAULT);
        } catch (Exception e) {
            log.error("获得载重总数据表格内容异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

    /**
     * 导出载重数据表格内容
     * @param sensorSequence sensorSequence ---> 0: 载重1; 1: 载重2
     * @param status         载重状态 01: 空载； 02: 满载； 03: 超载； 04: 装载； 05: 卸载；06: 轻载；07: 重载； null:所有状态
     */
    @ResponseBody
    @RequestMapping(value = "/export", method = RequestMethod.POST)
    public void export(HttpServletResponse response, Integer sensorSequence, Integer status) {
        try {
            if (Objects.nonNull(sensorSequence)) {
                loadManagementStatisticService.export(response, sensorSequence, status);
            }
        } catch (Exception e) {
            log.error("导出载重数据内容异常", e);
        }
    }

}

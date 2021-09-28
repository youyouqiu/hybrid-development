package com.zw.platform.controller.workhourmgt;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.vas.workhourmgt.SensorVehicleInfo;
import com.zw.platform.domain.vas.workhourmgt.query.WorkHourQuery;
import com.zw.platform.service.workhourmgt.WorkHourStatisticsService;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author penghj
 */
@Controller
@RequestMapping("/v/workhourmgt/workHourStatistics")
public class WorkHourStatisticsController {
    private static Logger log = LogManager.getLogger(WorkHourStatisticsController.class);

    private static final String LIST_PAGE = "vas/workhourmgt/workHourStatistics/list";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Resource
    private WorkHourStatisticsService workHourStatisticsService;

    /**
     * 工时统计列表页
     */
    @Auth
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ModelAndView list() {
        try {
            ModelAndView mav = new ModelAndView(LIST_PAGE);
            List<SensorVehicleInfo> vehicleList = workHourStatisticsService.getBindVehicle(SensorVehicleInfo.SENSOR_TYPE_WORK_HOUR);
            mav.addObject("vehicleList", JSON.toJSONString(vehicleList));
            return mav;
        } catch (Exception e) {
            log.error("获取工时统计列表页异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 获得图表内容
     */
    @ResponseBody
    @RequestMapping(value = "/getChartInfo", method = RequestMethod.POST)
    public JsonResultBean getChartInfo(WorkHourQuery query) {
        try {
            JSONObject result = workHourStatisticsService.getChartInfo(query,false);
            return new JsonResultBean(result);
        } catch (Exception e) {
            log.error("获得图表内容异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 获得总数据表格内容
     */
    @ResponseBody
    @RequestMapping(value = "/getTotalDataFormInfo", method = RequestMethod.POST)
    public PageGridBean getTotalDataFormInfo(final WorkHourQuery query) {
        try {
            return workHourStatisticsService.getTotalDataFormInfo(query);
        } catch (Exception e) {
            log.error("获得总数据表格内容异常", e);
            return new PageGridBean(PageGridBean.FAULT);
        }
    }

}

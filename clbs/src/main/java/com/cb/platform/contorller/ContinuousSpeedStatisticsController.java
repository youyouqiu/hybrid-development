package com.cb.platform.contorller;

import com.cb.platform.dto.report.sichuan.ContinuousSpeedStatisticsQuery;
import com.cb.platform.service.ContinuousSpeedStatisticsService;
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

/**
 * 持续超速统计
 * @author hujun
 * @Date 创建时间：2018年4月27日 上午9:33:25
 */
@Controller
@RequestMapping("/cb/cbReportManagement/continuousSpeed")
public class ContinuousSpeedStatisticsController {
    private static final Logger logger = LogManager.getLogger(ContinuousSpeedStatisticsController.class);

    private static final String LIST_PAGE = "/modules/cbReportManagement/continuousSpeed";

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Autowired
    private ContinuousSpeedStatisticsService continuousSpeedStatisticsService;

    /**
     * 持续超速统计报表界面
     */
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String speedStatistics() {
        return LIST_PAGE;
    }

    /**
     * 持续超速统计-持续超速道路运输企业统计表
     */
    @RequestMapping(value = "/getOrgReport", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getContinuousSpeedOrgReport(ContinuousSpeedStatisticsQuery query) {
        try {
            return continuousSpeedStatisticsService.getContinuousSpeedOrgReport(query);
        } catch (Exception e) {
            logger.error("持续超速道路运输企业统计表查询异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 持续超速统计-持续超速道路运输企业统表-离线导出
     */
    @RequestMapping(value = "/exportOrgReport", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean exportContinuousSpeedOrgReport(ContinuousSpeedStatisticsQuery query) {
        return continuousSpeedStatisticsService.exportContinuousSpeedOrgReport(query);
    }

    /**
     * 持续超速统计-持续超速车辆统计表
     */
    @RequestMapping(value = "/getVehicleReport", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getContinuousSpeedVehicleReport(ContinuousSpeedStatisticsQuery query) {
        try {
            return continuousSpeedStatisticsService.getContinuousSpeedVehicleReport(query);
        } catch (Exception e) {
            logger.error("持续超速车辆统计表查询异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 持续超速统计-持续超速车辆统计表-离线导出
     */
    @RequestMapping(value = "/exportVehicleReport", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean exportContinuousSpeedVehicleReport(ContinuousSpeedStatisticsQuery query) {
        return continuousSpeedStatisticsService.exportContinuousSpeedVehicleReport(query);
    }

    /**
     * 持续超速统计-持续超速车辆明细表
     */
    @RequestMapping(value = "/getVehicleDetailReport", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getContinuousSpeedVehicleDetailReport(ContinuousSpeedStatisticsQuery query) {
        try {
            return continuousSpeedStatisticsService.getContinuousSpeedVehicleDetailReport(query);
        } catch (Exception e) {
            logger.error("持续超速车辆明细表查询异常", e);
            return new PageGridBean(PageGridBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 持续超速统计-持续超速车辆明细表-离线导出
     */
    @RequestMapping(value = "/exportVehicleDetailReport", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean exportContinuousSpeedVehicleDetailReport(ContinuousSpeedStatisticsQuery query) {
        return continuousSpeedStatisticsService.exportContinuousSpeedVehicleDetailReport(query);
    }

}

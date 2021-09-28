package com.cb.platform.contorller;

import com.cb.platform.dto.MonitorOffRouteDetailPageQuery;
import com.cb.platform.dto.MonitorOffRoutePageQuery;
import com.cb.platform.dto.MonitorOffRouteQuery;
import com.cb.platform.dto.OrgOffRoutePageQuery;
import com.cb.platform.dto.OrgOffRouteQuery;
import com.cb.platform.service.OffRouteService;
import com.zw.platform.commons.Auth;
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
 * 路线偏离报警统计
 * @author Zhang Yanhui
 * @since 2021/3/19 11:08
 */

@Controller
@RequestMapping("/cb/cbReportManagement/offRoute")
public class OffRouteController {
    private static final Logger log = LogManager.getLogger(OffRouteController.class);

    private static final String LIST_PAGE = "/modules/cbReportManagement/offRoute/list";

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Autowired
    private OffRouteService offRouteService;

    @Auth
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String list() {
        return LIST_PAGE;
    }

    @ResponseBody
    @RequestMapping(value = "/getOrgDataList", method = RequestMethod.POST)
    public PageGridBean getDataList(OrgOffRoutePageQuery query) {
        try {
            return offRouteService.getDataList(query);
        } catch (Exception e) {
            log.error("获取企业路线偏离统计数据列表异常", e);
        }
        return new PageGridBean(PageGridBean.FAULT, sysErrorMsg);
    }

    @ResponseBody
    @RequestMapping(value = "/exportOrgDataList", method = RequestMethod.POST)
    public JsonResultBean exportOrgDataList(OrgOffRoutePageQuery query) {
        return offRouteService.exportOrgDataList(query);
    }

    @ResponseBody
    @RequestMapping(value = "/getOrgChartStatisticsData", method = RequestMethod.POST)
    public JsonResultBean getChartStatisticsData(OrgOffRouteQuery query) {
        try {
            return offRouteService.getChartStatisticsData(query);
        } catch (Exception e) {
            log.error("获取企业图形统计数据异常", e);
        }
        return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
    }

    @ResponseBody
    @RequestMapping(value = "/getOrgDetailBasicInfoAndTrend", method = RequestMethod.POST)
    public JsonResultBean getOrgDetailBasicInfoAndTrend(OrgOffRouteQuery query) {
        try {
            return offRouteService.getOrgDetailBasicInfoAndTrend(query);
        } catch (Exception e) {
            log.error("获取企业路线偏离报警明细基本信息和报警趋势异常", e);
        }
        return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
    }

    @ResponseBody
    @RequestMapping(value = "/getOrgDetailMonitorList", method = RequestMethod.POST)
    public PageGridBean getOrgDetailMonitorList(OrgOffRoutePageQuery query) {
        try {
            return offRouteService.getOrgDetailMonitorList(query);
        } catch (Exception e) {
            log.error("获取企业路线偏离报警明细监控对象数据列表异常", e);
        }
        return new PageGridBean(JsonResultBean.FAULT, sysErrorMsg);
    }

    @ResponseBody
    @RequestMapping(value = "/exportOrgDetailMonitorList", method = RequestMethod.POST)
    public JsonResultBean exportOrgDetailMonitorList(OrgOffRoutePageQuery query) {
        return offRouteService.exportOrgDetailMonitorList(query);
    }

    @ResponseBody
    @RequestMapping(value = "/getMonitorDataList", method = RequestMethod.POST)
    public PageGridBean getMonitorDataList(MonitorOffRoutePageQuery query) {
        try {
            return offRouteService.getMonitorDataList(query);
        } catch (Exception e) {
            log.error("获取车辆路线偏离统计数据列表异常", e);
        }
        return new PageGridBean(PageGridBean.FAULT, sysErrorMsg);
    }

    @ResponseBody
    @RequestMapping(value = "/exportMonitorDataList", method = RequestMethod.POST)
    public JsonResultBean exportMonitorDataList(MonitorOffRoutePageQuery query) {
        return offRouteService.exportMonitorDataList(query);
    }

    @ResponseBody
    @RequestMapping(value = "/getMonitorChartStatisticsData", method = RequestMethod.POST)
    public JsonResultBean getMonitorChartStatisticsData(MonitorOffRouteQuery query) {
        try {
            return offRouteService.getMonitorChartStatisticsData(query);
        } catch (Exception e) {
            log.error("获取车辆图形统计数据异常", e);
        }
        return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
    }

    @ResponseBody
    @RequestMapping(value = "/getMonitorDetailList", method = RequestMethod.POST)
    public PageGridBean getMonitorDetailList(MonitorOffRouteDetailPageQuery query) {
        try {
            return offRouteService.getMonitorDetailList(query);
        } catch (Exception e) {
            log.error("获取车辆路线偏离报警明细列表异常", e);
        }
        return new PageGridBean(JsonResultBean.FAULT, sysErrorMsg);
    }

    @ResponseBody
    @RequestMapping(value = "/exportMonitorDetailList", method = RequestMethod.POST)
    public JsonResultBean exportMonitorDetailList(MonitorOffRouteDetailPageQuery query) {
        return offRouteService.exportMonitorDetailList(query);
    }
}

package com.zw.platform.controller.reportmanagement;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.commons.Auth;
import com.zw.platform.service.reportManagement.MileageReportService;
import com.zw.platform.util.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
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
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author fanlu
 * @date 2017/3/30
 */
@Controller
@RequestMapping("/m/reportManagement/mileageReport")
public class MileageReportController {

    private static final String LIST_PAGE = "modules/reportManagement/mileageReport";

    private static final Logger log = LogManager.getLogger(MileageReportController.class);

    @Autowired
    private MileageReportService mileageReportService;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    /**
     * 页面
     */
    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public ModelAndView listPage() {
        ModelAndView modelAndView = new ModelAndView(LIST_PAGE);
        modelAndView.addObject("viewNameOne", "位置报表");
        modelAndView.addObject("viewNameTwo", "行驶报表");
        return modelAndView;
    }

    /**
     * 页面2
     * @author tianzhangxu
     * 报表中有2个地方都有行驶报表，为了区分接口，以便前端进行高亮处理
     */
    @Auth
    @RequestMapping(value = { "/twoList" }, method = RequestMethod.GET)
    public ModelAndView listPageTwo() {
        ModelAndView modelAndView = new ModelAndView(LIST_PAGE);
        modelAndView.addObject("viewNameOne", "部标监管报表");
        modelAndView.addObject("viewNameTwo", "行驶里程报表");
        return modelAndView;
    }

    /**
     * 行驶里程查询
     */
    @RequestMapping(value = { "/showdata" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean showData(String allVid, String startTime, String endTime) {
        try {
            if (StringUtils.isBlank(allVid) || StringUtils.isBlank(startTime) || StringUtils.isBlank(endTime)) {
                return new JsonResultBean(JsonResultBean.FAULT, "页面参数错误");
            }
            LocalDateTime startDateTime = DateUtil.YMD_HMS.ofDateTime(startTime).orElse(null);
            LocalDateTime endDateTime = DateUtil.YMD_HMS.ofDateTime(endTime).orElse(null);
            if (startDateTime == null || endDateTime == null) {
                return new JsonResultBean(JsonResultBean.FAULT, "页面参数错误");
            }
            JSONObject msg = new JSONObject();
            long startTimeL = startDateTime.toEpochSecond(ZoneOffset.of("+8"));
            long endTimeL = endDateTime.toEpochSecond(ZoneOffset.of("+8"));
            List<String> moIds = Arrays.stream(allVid.split(",")).distinct().collect(Collectors.toList());
            msg.put("mileageReports", mileageReportService.findMileageById(moIds, startTimeL, endTimeL));
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("行驶里程统计页面获取行驶里程数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 获取分组里程统计数据
     */
    @RequestMapping(value = { "/getAssignMileageData" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getAssignMileageData(String assignNames) {
        try {
            if (StringUtils.isNotBlank(assignNames)) {
                JSONObject result = mileageReportService.getAssignMileageData(assignNames);
                return new JsonResultBean(result);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT, "页面参数错误");
            }
        } catch (Exception e) {
            log.error("获取分组里程统计数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 获得行驶里程统计
     * @param monitorIds 监控对象id 逗号分隔
     * @param startTime  开始日期(yyyy-MM-dd)
     * @param endTime    结束日期(yyyy-MM-dd)
     */
    @RequestMapping(value = "/getDrivingMileageStatistics", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getDrivingMileageStatistics(String monitorIds, String startTime, String endTime) {
        try {
            return mileageReportService.getDrivingMileageStatistics(monitorIds, startTime, endTime, false);
        } catch (Exception e) {
            log.error("行驶里程统计查询异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 行驶里程明细
     * @param monitorId 监控对象id
     * @param startTime 开始日期(yyyy-MM-dd)
     * @param endTime   结束日期(yyyy-MM-dd)
     */
    @RequestMapping(value = "/getDrivingMileageDetails", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getDrivingMileageDetails(String monitorId, String startTime, String endTime) {
        try {
            return mileageReportService.getDrivingMileageDetails(monitorId, startTime, endTime);
        } catch (Exception e) {
            log.error("行驶里程明细查询异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 行驶里程位置明细
     * @param monitorId 监控对象id
     * @param startTime 开始时间
     * @param endTime   结束时间
     */
    @RequestMapping(value = "/getDrivingMileageLocationDetails", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getDrivingMileageLocationDetails(String monitorId, String startTime, String endTime,
        Integer reissue) {
        try {
            return mileageReportService.getDrivingMileageLocationDetails(monitorId, startTime, endTime, reissue);
        } catch (Exception e) {
            log.error("行驶里程明细查询异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 部标监管报表-行驶里程报表 导出
     * @param exportType 2:行驶统计;3:行驶明细;4:位置明细;
     */
    @RequestMapping(value = "/exportDrivingMileage", method = RequestMethod.POST)
    @ResponseBody
    public void exportDrivingMileage(HttpServletResponse response, String monitorId, String startTime, String endTime,
        Integer exportType, String queryParam, Integer reissue) {
        try {
            mileageReportService.exportDrivingMileage(response, monitorId, startTime, endTime, exportType, queryParam,
                reissue);
        } catch (Exception e) {
            log.error("行驶里程统计页面导出数据异常", e);
        }
    }
}

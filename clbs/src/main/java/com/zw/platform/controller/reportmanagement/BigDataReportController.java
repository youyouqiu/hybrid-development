package com.zw.platform.controller.reportmanagement;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.commons.Auth;
import com.zw.platform.controller.monitoring.TrackPlaybackController;
import com.zw.platform.service.basicinfo.VehicleService;
import com.zw.platform.service.reportManagement.BigDataReportService;
import com.zw.platform.util.common.JsonResultBean;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Administrator
 * @date 2017/2/28
 */
@Controller
@RequestMapping("/m/reportManagement/bigDataReport")
public class BigDataReportController {
    private static final Logger logger = LogManager.getLogger(TrackPlaybackController.class);

    @Autowired
    VehicleService vehicleService;

    @Autowired
    private BigDataReportService bigDataReportService;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    private static final String LIST_PAGE = "modules/reportManagement/bigDataReport";

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    /**
     * 大数据报表,里程对比图,点击柱形查询单车的里程统计数据
     * @param vehicleId 车辆id
     * @param brand     车牌号
     * @param groupId   组织id
     */
    @RequestMapping(value = { "/getMileCompareData" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getMileCompareData(@RequestParam("vehicleId") final String vehicleId,
        @RequestParam("brand") final String brand, @RequestParam("groupId") final String groupId) {
        try {
            if (StringUtils.isBlank(vehicleId) || StringUtils.isBlank(brand) || StringUtils.isBlank(groupId)) {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
            return bigDataReportService.getMouthMileDataByVehicleId(vehicleId, brand, groupId);
        } catch (Exception e) {
            logger.error("大数据报表,里程对比图,点击柱形查询单车的里程统计数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @RequestMapping(value = { "/getBigDataReportData" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getBigDataReportData(@RequestParam("groupId") final String groupId) {
        try {
            if (StringUtils.isBlank(groupId)) {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
            JSONObject msg = bigDataReportService.queryBigDataValue(groupId);
            if (msg == null) {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
            return new JsonResultBean(msg);
        } catch (Exception e) {
            logger.error("查询大数据报表(月报表)异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 大数据报表,里程对比图,点击柱形查询单车的里程统计数据
     * @param vehicleId 车辆id
     * @param groupId   组织id
     */
    @RequestMapping(value = { "/getMileData" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getMileData(@RequestParam("vehicleId") final String vehicleId,
        @RequestParam("groupId") final String groupId) {
        try {
            if (StringUtils.isBlank(vehicleId) || StringUtils.isBlank(groupId)) {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
            JSONObject result = bigDataReportService.queryMonitorMile(vehicleId, groupId);
            return new JsonResultBean(result);
        } catch (Exception e) {
            logger.error("大数据报表,里程对比图,点击柱形查询单车的里程统计数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

}

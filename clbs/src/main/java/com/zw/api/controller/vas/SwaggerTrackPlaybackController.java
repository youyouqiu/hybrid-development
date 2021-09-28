package com.zw.api.controller.vas;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zw.api.config.ResponseUntil;
import com.zw.platform.commons.Auth;
import com.zw.platform.service.basicinfo.VehicleService;
import com.zw.platform.service.monitoring.impl.HistoryServiceImpl;
import com.zw.platform.service.monitoring.impl.RealTimeServiceImpl;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.JsonResultBean;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/swagger/v/track")
@Api(tags = { "历史轨迹" }, description = "历史轨迹相关api接口")
public class SwaggerTrackPlaybackController {
    /**
     * 保存历史轨迹为围栏新增页面
     */
    private static final Logger log = LogManager.getLogger(SwaggerTrackPlaybackController.class);

    /**
     * 日期转换格式
     */
    private static final String DATE_FORMAT1 = "yyyy-MM-dd HH:mm:ss";
    private static final String DATE_FORMAT2 = "yyyy-MM-dd";

    @Autowired
    RealTimeServiceImpl realTime;

    @Autowired
    HistoryServiceImpl historyService;

    @Autowired
    private VehicleService vehicleService;

    @Auth

    @ApiOperation(value = "根据条件查询历史轨迹数据", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "vehicleId", value = "车辆id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "startTime", value = "开始时间    格式： yyyy-MM-dd HH:m：ss", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "endTime", value = "结束时间    格式： yyyy-MM-dd HH:m：ss", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "sensorFlag", value = "是否绑定里程传感器标识 0:没有 1:有", required = true, paramType = "query",
            dataType = "string") })
    @SuppressWarnings("null")
    @RequestMapping(value = { "/getHistoryData" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getTodayFirstData(String vehicleId, String startTime, String endTime, HttpServletRequest req,
        HttpServletResponse response, Integer sensorFlag) {
        try {
            ResponseUntil.setResponseHeader(response); // 解决跨域问题
            if (vehicleService.findVehicleById(vehicleId) == null) { // 车辆是否存在
                return new JsonResultBean(JsonResultBean.FAULT, "车辆不存在！");
            }
            try {
                DateUtils.parseDate(startTime, DATE_FORMAT1);
                DateUtils.parseDate(endTime, DATE_FORMAT1);
            } catch (Exception e) {
                return new JsonResultBean(JsonResultBean.FAULT, "时间格式错误！");
            }
            // 根据车辆id查询车辆全部历史轨迹
            String ip = new GetIpAddr().getIpAddr(req);// 获得访问ip
            JsonResultBean jrb = historyService.getHistoryVehicle(vehicleId, startTime, endTime, sensorFlag);
            historyService.addlog(vehicleId, ip);
            return jrb;
        } catch (Exception e) {
            log.error("查询历史轨迹数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    @ApiOperation(value = "根据条件查询历史轨迹数据", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "vehicleId", value = "车辆id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "startTime", value = "开始时间    格式： yyyy-MM-dd HH:m：ss", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "endTime", value = "结束时间    格式： yyyy-MM-dd HH:m：ss", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "type", value = "监控对象类型(vehicle:车; people:人)", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "sensorFlag", value = "是否绑定里程传感器标识 0:没有 1:有", required = true, paramType = "query",
            dataType = "string") })
    @SuppressWarnings("null")
    @RequestMapping(value = { "/getHistoryDataForBDTD" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getTodayFirstDataForBDTD(String vehicleId, String startTime, String endTime, String type,
        HttpServletRequest req, HttpServletResponse response, Integer sensorFlag) {
        try {
            if (vehicleId == null || "".equals(vehicleId) || startTime == null || "".equals(startTime)
                || endTime == null || "".equals(endTime) || type == null || "".equals(type)) {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
            ResponseUntil.setResponseHeader(response); // 解决跨域问题
            // 根据车辆id查询车辆全部历史轨迹
            String ip = new GetIpAddr().getIpAddr(req);// 获得访问ip
            JsonResultBean jrb = historyService.getHistoryPeople(vehicleId, startTime, endTime, sensorFlag);
            historyService.addlog(vehicleId, ip);
            return jrb;
        } catch (Exception e) {
            log.error("查询历史轨迹数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }

    }

    @ApiOperation(value = "查询当月里程数据", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "vehicleId", value = "车辆id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "nowMonth", value = "当前月   格式： yyyy-mm-dd(必须是yyyy-mm-01)", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "nextMonth", value = "下月间   格式： yyyy-mm-dd(必须是yyyy-mm-01)", required = true,
            paramType = "query", dataType = "string") })
    @RequestMapping(value = { "/getActiveDate" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getActiveDate(String vehicleId, String nowMonth, String nextMonth,
        HttpServletResponse response) {
        try {
            ResponseUntil.setResponseHeader(response); // 解决跨域问题
            // 校验数据
            if (vehicleService.findVehicleById(vehicleId) == null) { // 车辆是否存在
                return new JsonResultBean(JsonResultBean.FAULT, "车辆不存在！");
            }
            try {
                DateUtils.parseDate(nowMonth, DATE_FORMAT2);
                DateUtils.parseDate(nextMonth, DATE_FORMAT2);
                // 判断是否为当月第一天
                String nowDate = nowMonth.substring(8);
                String nextDate = nextMonth.substring(8);
                int nowMon = Integer.parseInt(nowMonth.substring(5, 7));
                int nextMon = Integer.parseInt(nextMonth.substring(5, 7));
                if (!"01".equals(nowDate) || !"01".equals(nextDate)) {
                    return new JsonResultBean(JsonResultBean.FAULT, "月份格式错误，必须为月份第一天！");
                }
                if (nextMon - nowMon != 1) {
                    return new JsonResultBean(JsonResultBean.FAULT, "参数必须是前一月和下一月！");
                }
            } catch (Exception e) {
                return new JsonResultBean(JsonResultBean.FAULT, "月份格式错误！");
            }
            JSONObject msg = new JSONObject();
            JSONArray dates = new JSONArray();
            JSONArray dailyMiles = new JSONArray();
            nowMonth = nowMonth + " 00:00:00";
            nextMonth = nextMonth + " 00:00:00";
            List<Map<String, String>> activeDate =
                historyService.getDailyMileByDate(vehicleId, Converter.convertToUnixTimeStamp(nowMonth),
                    Converter.convertToUnixTimeStamp(nextMonth));
            if (CollectionUtils.isEmpty(activeDate)) {
                return new JsonResultBean(false);
            }
            for (Map<String, String> date : activeDate) {
                dates.add(date.get("ATIME"));
                dailyMiles.add(date.get("MILE"));
            }
            msg.put("date", dates);
            msg.put("dailyMile", dailyMiles);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("查询当月里程数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    @ApiOperation(value = "查询当月里程数据(北斗天地)", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "vehicleId", value = "车辆id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "nowMonth", value = "当前月   格式： yyyy-mm-dd(必须是yyyy-mm-01)", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "nextMonth", value = "下月间   格式： yyyy-mm-dd(必须是yyyy-mm-01)", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "type", value = "监控对象类型(vehicle:车; people:人)", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = { "/getActiveDateForBDTD" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getActiveDateForBDTD(String vehicleId, String nowMonth, String nextMonth, String type,
        HttpServletRequest req, HttpServletResponse response) {
        try {
            ResponseUntil.setResponseHeader(response); // 解决跨域问题
            JSONObject msg = new JSONObject();
            JSONArray dates = new JSONArray();
            JSONArray dailyMiles = new JSONArray();
            nowMonth = nowMonth + " 00:00:00";
            nextMonth = nextMonth + " 00:00:00";
            // 根据车辆 id查询指定月份返回每日里程
            List<Map<String, String>> activeDate = null;
            if (type.equals("vehicle")) {
                activeDate = historyService.getDailyMileByDate(vehicleId, Converter.convertToUnixTimeStamp(nowMonth),
                    Converter.convertToUnixTimeStamp(nextMonth));
                for (Map<String, String> date : activeDate) {
                    dates.add(date.get("ATIME"));
                    dailyMiles.add(date.get("MILE"));
                }
                msg.put("date", dates);
                msg.put("dailyMile", dailyMiles);
            }
            if (activeDate != null && activeDate.size() > 0) {
                return new JsonResultBean(msg);
            }
            return new JsonResultBean(false);
        } catch (Exception e) {
            log.error("查询当月里程数据(北斗天地)异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

}

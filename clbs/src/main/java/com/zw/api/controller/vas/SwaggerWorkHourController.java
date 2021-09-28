package com.zw.api.controller.vas;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.vas.workhourmgt.VibrationSensorBind;
import com.zw.platform.service.workhourmgt.WorkingHoursService;
import com.zw.platform.util.common.JsonResultBean;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/swagger/v/workhourmgt/vbStatistic")
@Api(tags = { "工时统计" }, description = "工时统计相关api")
public class SwaggerWorkHourController {
    private static Logger log = LogManager.getLogger(SwaggerWorkHourController.class);

    @Autowired
    private WorkingHoursService workingHoursService;

    @ApiOperation(value = "获取工时统计列表", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "page", value = "页数", required = true, paramType = "query", dataType = "Long",
            defaultValue = "1"),
        @ApiImplicitParam(name = "limit", value = "每页显示条数", required = true, paramType = "query", dataType = "Long",
            defaultValue = "20"),
        @ApiImplicitParam(name = "startTime", value = "开始时间(如2017-02-14 00:00:00)", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "endTime", value = "结束时间(如2017-02-14 23:00:00)", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "type", value = "查找类型(0/1)", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "band", value = "车辆id", required = true, paramType = "query", dataType = "string") })
    @Auth
    @RequestMapping(value = { "/getWorkHours" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getWorkHours(String type, String band, String startTime, String endTime) {
        try {
            JSONObject message = workingHoursService.getWorkHours(type, band, startTime, endTime);
            return new JsonResultBean(JsonResultBean.SUCCESS, message.toJSONString());
        } catch (Exception e) {
            log.error("获取工时统计列表异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    @ApiOperation(value = "获取振动传感器(报警频率阈值,工作频率阈值,怠速频率阈值)", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @Auth
    @RequestMapping(value = { "/getThresholds" }, method = RequestMethod.POST)
    @ResponseBody
    public VibrationSensorBind getThresholds(String vehicleId) {
        try {
            return workingHoursService.getThresholds(vehicleId);
        } catch (Exception e) {
            log.error("获取振动传感器异常", e);
            return null;
        }
    }
}

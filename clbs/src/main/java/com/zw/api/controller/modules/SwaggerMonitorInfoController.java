package com.zw.api.controller.modules;

import com.zw.api.domain.DriverInfo;
import com.zw.api.domain.LocationInfo;
import com.zw.api.domain.MonitorInfo;
import com.zw.api.domain.ResultBean;
import com.zw.api.domain.VehicleInfo;
import com.zw.api.service.MonitorInfoService;
import com.zw.platform.util.common.BusinessException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("/swagger/m/monitoring/monitor")
@Api(tags = { "监控对象信息" }, description = "监控对象相关API接口")
@Log4j2
public class SwaggerMonitorInfoController {
    @Autowired
    private MonitorInfoService monitorInfoService;

    @ApiOperation(value = "获取监控对象信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "name", value = "监控对象名称", paramType = "query", dataType = "string", required = true)
    @ApiResponse(code = 200,
        message = "true 成功, 其它为错误, 返回格式:{success: true, obj: {}, msg: ''},"
            + " obj中的属性参照下方的Model",
        response = MonitorInfo.class)
    @RequestMapping(value = "/", method = RequestMethod.GET)
    @ResponseBody
    public ResultBean<MonitorInfo> monitorInfo(final String name) {
        Objects.requireNonNull(name, "监控对象名称不能为空");

        MonitorInfo info = monitorInfoService.findMonitorByName(name);
        if (info == null) {
            return new ResultBean<>(false, "未找到监控对象");
        }
        return new ResultBean<>(true, "", info);
    }


    @ApiOperation(value = "新增监控对象", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "name", value = "车牌号", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "plateColor",
            value = "车牌颜色，1:蓝色 2:黄色 3:黑色 4:白色 5:绿色 9:其它 90:农蓝 91:农黄 92:农绿 93:黄绿色 94:渐变绿色", required = true,
            paramType = "query", dataType = "Integer"),
        @ApiImplicitParam(name = "simNo", value = "终端手机号", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "deviceNo", value = "终端号", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "protocol",
            value = "终端协议类型，1:JTT808-2013 11:JTT808-2019 12: 川标 13:冀标 14:桂标 15:苏标 16:浙标 17:吉标 18:陕标"
                + " 19:赣标 20:沪标 24:京标 25:黑标",
            required = true, paramType = "query", dataType = "Integer"),
        @ApiImplicitParam(name = "orgCode", value = "组织机构代码", required = true, paramType = "query",
            dataType = "string") })
    @ApiResponse(code = 200,
        message = "true 成功, 其它为错误, 返回格式:{success: true, msg: ''},",
        response = MonitorInfo.class)
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public ResultBean<Object> addVehicle(@Validated VehicleInfo vehicleInfo) throws BusinessException {
        boolean res = monitorInfoService.addVehicle(vehicleInfo);
        return new ResultBean<>(res, "");
    }


    @ApiOperation(value = "获取监控对象最新的位置信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "name", value = "监控对象名称,用逗号隔开,最多20个监控对象", paramType = "query",
        dataType = "string", required = true)
    @ApiResponse(code = 200,
        message = "true 成功, 其它为错误, 返回格式:{success: true, obj: {}, msg: ''},"
            + " obj中的属性参照下方的Model",
        response = LocationInfo.class)
    @RequestMapping(value = "/location/latest", method = RequestMethod.GET)
    @ResponseBody
    public ResultBean<List<LocationInfo>> latestLocation(final String name) {
        Objects.requireNonNull(name, "监控对象名称不能为空");

        List<LocationInfo> info = monitorInfoService.fetchLatestLocation(name);
        if (info == null) {
            return new ResultBean<>(false, "未找到监控对象最新的位置信息");
        }
        return new ResultBean<>(true, "", info);
    }

    @ApiOperation(value = "查询历史GPS数据", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "name", value = "监控对象名称", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "startTime", value = "开始时间    格式： yyyy-MM-dd HH:mm:ss", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "endTime", value = "结束时间    格式： yyyy-MM-dd HH:mm:ss", required = true,
            paramType = "query", dataType = "string") })
    @ApiResponse(code = 200,
        message = "true 成功, 其它为错误, 返回格式:{success: true, obj: {}, msg: ''},"
            + " obj中的属性参照下方的Model",
        response = LocationInfo.class)
    @RequestMapping(value = { "/location/history" }, method = RequestMethod.GET)
    @ResponseBody
    public ResultBean<List<LocationInfo>> historyLocations(String name, String startTime, String endTime) {
        Objects.requireNonNull(name, "监控对象名称不能为空");
        Objects.requireNonNull(startTime, "开始时间不能为空");
        Objects.requireNonNull(endTime, "结束时间不能为空");

        List<LocationInfo> locations = monitorInfoService.queryHistoryLocations(name, startTime, endTime);
        return new ResultBean<>(true, "", locations);
    }

    @ApiOperation(value = "查询驾驶员插卡信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "name", value = "监控对象名称",
        required = true, paramType = "query", dataType = "string")
    @ApiResponse(code = 200,
        message = "true 成功, 其它为错误, 返回格式:{success: true, obj: {}, msg: ''}, obj中的属性参照下方的Model",
        response = LocationInfo.class)
    @RequestMapping(value = { "/driver" }, method = RequestMethod.GET)
    @ResponseBody
    public ResultBean<DriverInfo> driverInfo(String name) {
        Objects.requireNonNull(name, "监控对象名称不能为空");

        DriverInfo driverInfo = monitorInfoService.getDriver(name);
        if (driverInfo == null) {
            return new ResultBean<>(false, "未找到驾驶员信息");
        }
        return new ResultBean<>(true, "", driverInfo);
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResultBean<?> handleException(Exception e) {
        return new ResultBean<>(false, e.getMessage());
    }
}

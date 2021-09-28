package com.zw.api.controller.vas;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.commons.Auth;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.service.basicinfo.impl.VehicleServiceImpl;
import com.zw.platform.service.monitoring.impl.RealTimeServiceImpl;
import com.zw.platform.util.common.AddressUtil;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.JsonResultBean;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;


@Controller
@RequestMapping("/swagger/v/monitoring")
@Api(tags = {"实时监控"}, description = "实时监控相关api接口")
public class SwaggerRealTimeMonitoring {
    private static Logger log = LogManager.getLogger(SwaggerRealTimeMonitoring.class);

    @Autowired
    private RealTimeServiceImpl realTime;

    @Autowired
    private VehicleServiceImpl vehicleService;

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @Auth
    @ApiOperation(value = "保存报警方式自定义设置到redis中", notes = "包括声音闪烁", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @RequestMapping(value = {"/alarmSettingSave"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean alarmSettingSave(
            @ApiParam("是否有闪烁的报警类型,有为true，否则false(数组多个值回车输入)") @RequestParam List<String> flicker,
            @ApiParam("是否有声音的报警类型,有为true，否则false(数组多个值回车输入)") @RequestParam List<String> sound) {
        String userId = SystemHelper.getCurrentUserId();
        RedisKey flickerKey = HistoryRedisKeyEnum.ALARM_SETTING_FLICKER.of(userId);
        RedisKey soundKey = HistoryRedisKeyEnum.ALARM_SETTING_SOUND.of(userId);
        RedisHelper.delete(Arrays.asList(flickerKey, soundKey));
        RedisHelper.addToList(flickerKey, flicker);
        RedisHelper.addToList(soundKey, sound);
        return new JsonResultBean(true);
    }

    @ApiOperation(value = "根据用户获取报警参数自定义设置", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @RequestMapping(value = {"/getAlarmSetting"}, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getAlarmSetting()
        throws BusinessException,
        ParseException {
        JSONObject msg = new JSONObject();
        String userId = SystemHelper.getCurrentUserId();
        RedisKey flickerKey = HistoryRedisKeyEnum.ALARM_SETTING_FLICKER.of(userId);
        RedisKey soundKey = HistoryRedisKeyEnum.ALARM_SETTING_SOUND.of(userId);
        List<String> flicker = RedisHelper.getList(flickerKey);
        List<String> sound = RedisHelper.getList(soundKey);
        msg.put("flicker", flicker);
        msg.put("sound", sound);
        return new JsonResultBean(msg);
    }

    @ApiOperation(value = "", notes = "根据经纬度查询详细地址", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    // @ApiImplicitParam(name = "lnglatXYs", value = "实时位置的经纬度，0代表经度，1代表纬度", required = true, paramType =
    // "query",dataType = "string")
    // 逆地址查询
    @RequestMapping(value = {"/getAddress"}, method = RequestMethod.POST)
    @ResponseBody
    @Deprecated
    public JsonResultBean getAddress(@ApiParam("实时位置的经纬度，0代表经度，1代表纬度(数组多个值回车输入)") @RequestParam String[] lnglatXYs)
        throws BusinessException {
        if (!lnglatXYs.getClass().isArray()) { // 不是数组型
            return new JsonResultBean(JsonResultBean.FAULT, "参数不是数组型！");
        }
        try {
            String address;
            if (lnglatXYs.length != 2) {
                address = "AddressNull";
            } else {
                address = AddressUtil.inverseAddress(lnglatXYs[0], lnglatXYs[1]).getFormattedAddress();
                if (address == null) {
                    address = "AddressNull";
                }
            }
            return new JsonResultBean(address);
        } catch (Exception e) {
            return new JsonResultBean(JsonResultBean.FAULT, "参数值错误！");
        }

    }

    // 逆地址查询
    @ApiOperation(value = "保存实时位置的逆地址", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @ApiImplicitParam(name = "addressNew", value = "逆地址，例：  {'longitude':'116.521','latitude':'40.006'"
                                                   + ",'adcode':'110105','building':'','buildingType':'','city':''"
                                                   + ",'cityCode':'010','district':'朝阳区','neighborhood':''"
                                                   + ",'neighborhoodType':'','province':'北京市','street':'南皋路'"
                                                   + ",'streetNumber':'125号','township':'崔各庄镇','crosses':''"
                                                   + ",'pois':'','formattedAddress':'北京市朝阳区崔各庄镇南皋闸管理站'}",
        required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = {"/setAddress"}, method = RequestMethod.POST)
    @ResponseBody
    public String setAddress(String addressNew) {
        return "";
    }

}

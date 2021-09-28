package com.zw.api.controller.modules;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.zw.api.config.ResponseUntil;
import com.zw.api.domain.AlarmInfo;
import com.zw.api.domain.ResultBean;
import com.zw.api.service.SwaggerAlarmService;
import com.zw.platform.domain.oil.AlarmHandle;
import com.zw.platform.domain.vas.alram.query.AlarmSearchQuery;
import com.zw.platform.service.alarm.AlarmSearchService;
import com.zw.platform.util.common.JsonResultBean;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/swagger/a/search")
@Api(tags = { "报警查询" }, description = "报警查询相关api")
public class SwaggerAlarmSearchController {
    private static final Logger logger = LogManager.getLogger(SwaggerAlarmSearchController.class);

    @Autowired
    private AlarmSearchService alarmSearchService;

    @Autowired
    private SwaggerAlarmService alarmService;

    /**
     * 日期转换格式
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @ApiOperation(value = "获取报警信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "read", description = "可读") })
    })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "monitorName", value = "监控对象名称,用逗号隔开,最多50个监控对象", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "startTime", value = "开始时间(格式yyyy-mm-dd hh:mm:ss 如2017-02-13 00:00:00)",
            required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "stopTime", value = "结束时间(格式yyyy-mm-dd hh:mm:ss 如2017-02-13 00:00:00)",
            required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "type", value = "报警类型(-1:所有报警类型 0:紧急报警 1:超速报警 2:疲劳驾驶 3:危险预警"
            + " 13:超速预警 14:疲劳驾驶预警 15:违规行驶报警 16:胎压预警 17:右转盲区异常报警 18:当天累积驾驶超时 19:超时停车,"
            + " 其它值可参照808协议报警类型说明)", paramType = "query", dataType = "int", defaultValue = "-1")
    })
    @RequestMapping(value = "/", method = RequestMethod.GET)
    @ResponseBody
    public ResultBean<List<AlarmInfo>> findMonitorAlarms(
        @RequestParam String monitorName,
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime stopTime,
        @RequestParam(defaultValue = "-1") int type
    ) {
        if (monitorName == null) {
            return new ResultBean<>(false, "监控对象名称不能为空");
        }

        List<AlarmInfo> alarms = alarmService.findAlarms(monitorName, startTime, stopTime, type);
        return new ResultBean<>(true, "", alarms);
    }

    /**
     * 分页查询,String[] vehicleList
     */
    @ApiOperation(value = "获取报警信息列表", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "page", value = "页数", required = true, paramType = "query", dataType = "Long",
            defaultValue = "1"),
        @ApiImplicitParam(name = "limit", value = "每页显示条数", required = true, paramType = "query", dataType = "Long",
            defaultValue = "20"),
        @ApiImplicitParam(name = "alarmType", value = "报警类型(默认-1查询全部,可选类型0~31,具体描述可参照808协议报警类型说明)", required = true,
            paramType = "query", dataType = "string", defaultValue = "-1"),
        @ApiImplicitParam(name = "status", value = "处理状态(-1:全部;0:未处理;1:已处理)", required = true, paramType = "query",
            dataType = "string", defaultValue = "-1"),
        @ApiImplicitParam(name = "alarmStartTime", value = "报警开始时间(格式yyyy-mm-dd hh:mm:ss 如2017-02-13 00:00:00)",
            required = true, paramType = "query", dataType = "Long"),
        @ApiImplicitParam(name = "alarmEndTime", value = "报警结束时间(格式yyyy-mm-dd hh:mm:ss 如2017-02-13 00:00:00)",
            required = true, paramType = "query", dataType = "Long"),
        @ApiImplicitParam(name = "vehicleList", value = "车牌号列表，每个车牌号后加逗号(如渝A88888,渝S99993,)", required = true,
            paramType = "query", dataType = "string") })
    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getListPage(String alarmType, int status, String alarmStartTime, String alarmEndTime,
        String vehicleList) {
        JSONObject msg = new JSONObject();
        AlarmSearchQuery query = new AlarmSearchQuery();
        try {
            if (StringUtils.isNotBlank(alarmEndTime)) {
                query.setAlarmEndTime(DateUtils.parseDate(alarmEndTime, DATE_FORMAT).getTime() / 1000);
            }
            if (StringUtils.isNotBlank(alarmStartTime)) {
                query.setAlarmStartTime(DateUtils.parseDate(alarmStartTime, DATE_FORMAT).getTime() / 1000);
            }
        } catch (ParseException e) {
            return new JsonResultBean(JsonResultBean.FAULT, "请输入正确的日期格式yyyy-mm-dd hh:mm:ss！");
        }
        if (!Pattern.matches("^-1$", String.valueOf(status)) && !Pattern
            .matches("^[01]{1}$", String.valueOf(status))) { // 校验所属企业是否在数据库存在
            return new JsonResultBean(JsonResultBean.FAULT, "处理状态只能为-1，0，1！");
        }
        query.setStatus(status);
        if (!Pattern.matches("^-1$", String.valueOf(alarmType)) // 校验所属企业是否在数据库存在
            && !Pattern.matches("^(0|[1-2][0-9]|3[01])$", String.valueOf(alarmType))) {
            return new JsonResultBean(JsonResultBean.FAULT, "报警类型默认-1查询全部,可选类型0~31,具体描述可参照808协议报警类型说明！");
        }
        query.setType(alarmType);
        List<AlarmHandle> result = new ArrayList<>();
        try {
            final List<String> vehicleIds = Lists.newArrayList(vehicleList.split(","));
            result = alarmSearchService.getAlarmHandle(vehicleIds, query);
        } catch (Exception e) {
            logger.error("getAlarmHandle异常" + e);
        }
        msg.put("msg", result);
        return new JsonResultBean(msg);
    }

    @ApiOperation(value = "获取报警信息列表", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "vid", value = "车id", required = true, paramType = "query", dataType = "string",
            defaultValue = "-1"), })
    @RequestMapping(value = { "/alarmList" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getAlarmList(String vid, HttpServletResponse response) {
        ResponseUntil.setResponseHeader(response); // 解决跨域问题
        JSONObject msg = new JSONObject();
        List<AlarmHandle> result = new ArrayList<>();
        try {
            result = alarmSearchService.getAlarmList(vid);
        } catch (Exception e) {
            logger.error("getAlarmHandle异常" + e);
        }
        msg.put("msg", result);
        return new JsonResultBean(msg);
    }

}

package com.zw.api2.controller.workhour;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.vas.workhourmgt.SensorVehicleInfo;
import com.zw.platform.domain.vas.workhourmgt.query.WorkHourQuery;
import com.zw.platform.service.workhourmgt.WorkHourStatisticsService;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import java.util.List;

@Controller
@RequestMapping("api/v/workhourmgt/workHourStatistics")
@Api(tags = { "工时统计报表_dev" }, description = "工时统计报表相关api")
public class ApiWorkHourStatisticsController {
    private static Logger log = LogManager.getLogger(ApiWorkHourStatisticsController.class);

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Resource
    private WorkHourStatisticsService workHourStatisticsService;

    /**
     * 工时统计列表页
     */
    @ApiOperation(value = "工时统计列表页", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @Auth
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean list() {
        try {
            JSONObject data = new JSONObject();
            List<SensorVehicleInfo> vehicleList =
                workHourStatisticsService.getBindVehicle(SensorVehicleInfo.SENSOR_TYPE_WORK_HOUR);
            data.put("vehicleList", JSON.toJSONString(vehicleList));
            return new JsonResultBean(data);
        } catch (Exception e) {
            log.error("获取工时统计列表页异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 获得图表内容
     */
    @ApiIgnore
    @ResponseBody
    @RequestMapping(value = "/getChartInfo", method = RequestMethod.POST)
    public JsonResultBean getChartInfo(WorkHourQuery query) {
        try {
            JSONObject result = workHourStatisticsService.getChartInfo(query, false);
            return new JsonResultBean(result);
        } catch (Exception e) {
            log.error("获得图表内容异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 获得总数据表格内容
     */
    @ApiOperation(value = "获取车辆工时统计数据", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "page", value = "页数（若输入页数大于最大页数，则返回第一页的数据）", required = true, paramType = "query",
            dataType = "long", defaultValue = "1"),
        @ApiImplicitParam(name = "limit", value = "每页显示条数", required = true, paramType = "query", dataType = "long",
            defaultValue = "20"),
        @ApiImplicitParam(name = "vehicleId", value = "车辆id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "startTimeStr", value = "开始时间（YYYY-MM-DD HH:MM:SS）", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "endTimeStr", value = "截止时间（YYYY-MM-DD HH:MM:SS）", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "sensorSequence", value = "发动机型号 0: 1#发动机  1: 2#发动机", required = true,
            paramType = "query", dataType = "Integer") })
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

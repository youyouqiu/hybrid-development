package com.zw.app.controller.monitor;

import com.alibaba.fastjson.JSONObject;
import com.zw.app.domain.monitor.BasicLocationInfo;
import com.zw.app.domain.monitor.MonitorEntity;
import com.zw.app.domain.monitor.MonitorSensorEntity;
import com.zw.app.entity.AppFuzzyVehicle;
import com.zw.app.entity.methodParameter.QueryLocationEntity;
import com.zw.app.entity.methodParameter.SetLocationEntity;
import com.zw.app.entity.monitor.MonitorRunAndStopDataQueryEntity;
import com.zw.app.service.monitor.MonitorManagementService;
import com.zw.app.util.AppVersionUtil;
import com.zw.app.util.common.AppResultBean;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.realTimeVideo.AudioVideoRransmitForm;
import com.zw.platform.service.monitoring.HistoryService;
import com.zw.platform.service.realTimeVideo.VideoService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.JsonResultBean;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * 监控对象管理
 * @author hujun
 * @date 2018/8/20 14:41
 */
@Controller
@RequestMapping("/app/monitor")
@Api(tags = { "App监控对象管理" }, description = "App监控对象管理相关api接口")
public class MonitorManagementController {

    private static final Logger log = LogManager.getLogger(MonitorManagementController.class);

    @Autowired
    MonitorManagementService monitorManagementService;

    @Autowired
    VideoService videoService;

    @Autowired
    HistoryService historyService;

    @Value("${sys.error.msg}")
    private String sysError;


    /**
     * 获取当前用户权限分组数据监控对象统计数据
     */
    @Auth
    @ApiOperation(value = "获取当前用户权限分组数据监控对象统计数据", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "type", value = "状态类型 0：全部，1：在线，2：离线", required = true,
        paramType = "query", dataType = "int")
    @RequestMapping(value = { "/assignmentByUser" }, method = RequestMethod.GET)
    @ResponseBody
    public AppResultBean getAssignmentByUser(Integer type) {
        try {
            JSONObject result = monitorManagementService.getAssignmentByUser(type);
            if (result == null) {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
            return new AppResultBean(result);
        } catch (Exception e) {
            log.error("获取当前用户权限分组数据异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 获取当前用户监控对象列表
     */
    @ApiOperation(value = "获取当前用户监控对象列表", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "assignmentId", value = "分组id", paramType = "query",
            required = true, dataType = "string"),
        @ApiImplicitParam(name = "type", value = "状态类型 0：全部，1：在线，2：离线", required = true,
            paramType = "query", dataType = "int") })
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    @ResponseBody
    public AppResultBean getMonitorList(String assignmentId, Integer type, Integer page, Integer pageSize) {
        try {
            JSONObject result = monitorManagementService.getMonitorList(assignmentId, type, page, pageSize);
            if (result == null) {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
            return new AppResultBean(result);
        } catch (Exception e) {
            log.error("获取当前用户监控对象列表异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 模糊搜索当前用户监控对象列表
     */
    @ApiOperation(value = "模糊搜索当前用户监控对象列表", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "fuzzyParam", value = "模糊搜索参数", required = true,
            paramType = "query", dataType = "string") })
    @RequestMapping(value = { "/fuzzyList" }, method = RequestMethod.GET)
    @ResponseBody
    public AppResultBean getFuzzyMonitorList(HttpServletRequest request,
        @Validated AppFuzzyVehicle appFuzzyVehicle, BindingResult bindingResult) {
        try {
            if (bindingResult.getAllErrors().size() != 0) {
                return new AppResultBean(AppResultBean.PARAM_ERROR,
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
            }
            String requestURI = request.getRequestURI();
            String meth = AppVersionUtil.dealVersionName(requestURI, appFuzzyVehicle.getVersion());
            Method method = monitorManagementService.getClass()
                .getMethod(meth, String.class);
            JSONObject result = (JSONObject) method
                .invoke(monitorManagementService, appFuzzyVehicle.getFuzzyParam());
            //JSONObject result = monitorManagementService.getFuzzyMonitorList(fuzzyParam);
            if (result == null) {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
            return new AppResultBean(result);
        } catch (Exception e) {
            log.error("模糊搜索当前用户监控对象列表异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 模糊搜索当前用户监控对象列表
     */
    @ApiOperation(value = "模糊搜索当前用户监控对象列表", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "vehicleIds", value = "车辆ids", required = true,
            paramType = "query", dataType = "string") })
    @RequestMapping(value = { "/fuzzyDetailList" }, method = RequestMethod.GET)
    @ResponseBody
    public AppResultBean getFuzzyMonitorDetailList(HttpServletRequest request,
        @Validated AppFuzzyVehicle appFuzzyVehicle, BindingResult bindingResult) {
        try {
            if (bindingResult.getAllErrors().size() != 0) {
                return new AppResultBean(AppResultBean.PARAM_ERROR,
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
            }
            String requestURI = request.getRequestURI();
            String meth = AppVersionUtil.dealVersionName(requestURI, appFuzzyVehicle.getVersion());
            Method method = monitorManagementService.getClass()
                .getMethod(meth, String.class);
            JSONObject result = (JSONObject) method
                .invoke(monitorManagementService, appFuzzyVehicle.getVehicleIds());
            //JSONObject result = monitorManagementService.getFuzzyMonitorList(fuzzyParam);
            if (result == null) {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
            return new AppResultBean(result);
        } catch (Exception e) {
            log.error("模糊搜索当前用户监控对象列表异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 获取当前用户监控对象ids(按照用户权限分组顺序排序)
     */
    @ApiOperation(value = "获取当前用户监控对象ids", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/monitorIds" }, method = RequestMethod.GET)
    @ResponseBody
    public AppResultBean getMonitorIds(String id, String favoritesIds) {
        try {
            JSONObject result = monitorManagementService.getMonitorIds(id, favoritesIds);
            return new AppResultBean(result);
        } catch (Exception e) {
            log.error("获取当前用户监控对象ids异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 获取监控对象基础位置信息
     */
    @ApiOperation(value = "获取监控对象基础位置信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "id", value = "监控对象id", required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = { "/basicLocationInfo/{id}" }, method = RequestMethod.GET)
    @ResponseBody
    public AppResultBean getBasicLocationInfo(@PathVariable("id") String id) {
        try {
            BasicLocationInfo basicLocationInfo = monitorManagementService.getBasicLocationInfo(id);
            if (basicLocationInfo == null) {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
            return new AppResultBean(basicLocationInfo);
        } catch (Exception e) {
            log.error("获取监控对象基础位置信息异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 获取监控对象基础位置信息
     * @return APP2.0.0新增
     */
    @RequestMapping(value = "/getBasicLocationInfoByMonitorId", method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean getBasicLocationInfoByMonitorId(HttpServletRequest request,
        @Validated QueryLocationEntity queryEntity, BindingResult result) {
        return AppVersionUtil.getResultData(request, queryEntity, result, monitorManagementService);
    }

    /**
     * 获取监控对象详细位置信息
     */
    @ApiOperation(value = "获取监控对象详细位置信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "id", value = "监控对象id", required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = { "/detailLocationInfo/{id}" }, method = RequestMethod.GET)
    @ResponseBody
    public AppResultBean getDetailLocationInfo(@PathVariable("id") String id, @Validated MonitorEntity monitorEntity,
        BindingResult result, HttpServletRequest request) {
        try {
            monitorEntity.setId(id);
            if (result.getAllErrors().size() != 0) {
                return new AppResultBean(AppResultBean.PARAM_ERROR, result.getAllErrors().get(0).getDefaultMessage());
            }
            String requestURI = request.getRequestURI();
            String meth = AppVersionUtil.dealVersionName(requestURI, monitorEntity.getVersion());
            Method method = monitorManagementService.getClass().getMethod(meth, String.class, Integer.class);
            AppResultBean re = (AppResultBean) method
                .invoke(monitorManagementService, monitorEntity.getId(), monitorEntity.getVersion());
            if (re != null) {
                return re;
            } else {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("获取监控对象详细位置信息异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 获取监控对象详细位置信息
     */
    @RequestMapping(value = { "/setDetailLocationInfo" }, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean setDetailLocationInfo(@Validated SetLocationEntity entity,
        BindingResult result, HttpServletRequest request) {
        try {
            if (result.getAllErrors().size() != 0) {
                return new AppResultBean(AppResultBean.PARAM_ERROR, result.getAllErrors().get(0).getDefaultMessage());
            }
            String requestURI = request.getRequestURI();
            String meth = AppVersionUtil.dealVersionName(requestURI, entity.getVersion());
            Method method = monitorManagementService.getClass().getMethod(meth, String.class, Integer.class);
            AppResultBean re = (AppResultBean) method
                .invoke(monitorManagementService, entity.getLocationInfo(), entity.getVersion());
            if (re != null) {
                return re;
            } else {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("获取监控对象详细位置信息异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }



    /**
     * 获取监控对象信息
     */
    @ApiOperation(value = "获取监控对象信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "id", value = "监控对象id", required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = "/getMonitorInfo/{id}", method = RequestMethod.GET)
    @ResponseBody
    public AppResultBean getMonitorInfo(@PathVariable("id") String id) {
        try {
            JSONObject result = monitorManagementService.getMonitorInfo(id);
            if (result == null) {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
            return new AppResultBean(result);
        } catch (Exception e) {
            log.error("获取监控对象信息异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 获取监控对象位置历史数据
     */
    @ApiOperation(value = "获取监控对象位置历史数据", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "监控对象id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "startTime", value = "开始时间", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "endTime", value = "结束时间", required = true,
            paramType = "query", dataType = "string") })
    @RequestMapping(value = "/{id}/history/location", method = RequestMethod.GET)
    @ResponseBody
    public AppResultBean getHistoryLocation(@PathVariable String id, String startTime, String endTime) {
        try {
            JSONObject result = monitorManagementService.getHistoryLocation(id, startTime, endTime);
            if (result == null) {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
            return new AppResultBean(result);
        } catch (Exception e) {
            log.error("获取监控对象位置历史数据异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 获取监控对象湿度数据
     */
    @RequestMapping(value = "/{id}/history/humidity", method = RequestMethod.GET)
    @ResponseBody
    public AppResultBean getHumidityData(@PathVariable String id, String startTime, String endTime) {
        try {
            if (StringUtils.isNotBlank(id) && StringUtils.isNotBlank(startTime) && StringUtils.isNotBlank(endTime)) {
                JSONObject msg = monitorManagementService.getHumidityInfo(id, startTime, endTime);
                if (msg != null) {
                    return new AppResultBean(msg);
                }
            }
            return new AppResultBean(AppResultBean.PARAM_ERROR);
        } catch (Exception e) {
            log.error("获取监控对象湿度历史数据异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 获取监控对象外设列表
     */
    @RequestMapping(value = "/{id}/attached", method = RequestMethod.GET)
    @ResponseBody
    public AppResultBean getAttachedInfo(@PathVariable String id) {
        try {
            if (StringUtils.isNotBlank(id)) {
                JSONObject msg = monitorManagementService.getMonitorAttached(id);
                if (msg != null) {
                    return new AppResultBean(msg);
                }
            }
            return new AppResultBean(AppResultBean.PARAM_ERROR);
        } catch (Exception e) {
            log.error("获取监控对象外设列表");
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 获取监控对象正反转历史数据
     */
    @RequestMapping(value = "/{id}/history/motor", method = RequestMethod.GET)
    @ResponseBody
    public AppResultBean getPositiveInversionInfo(@PathVariable String id, String startTime, String endTime) {
        try {
            if (StringUtils.isNotBlank(id) && StringUtils.isNotBlank(startTime) && StringUtils.isNotBlank(endTime)) {
                JSONObject msg = monitorManagementService.getMonitorWinchInfo(id, startTime, endTime);
                return new AppResultBean(msg);
            }
            return new AppResultBean(AppResultBean.PARAM_ERROR);
        } catch (Exception e) {
            log.error("获取监控对象正反转历史数据异常");
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 获取监控对象开关历史数据
     */
    @RequestMapping(value = "/{id}/history/switch", method = RequestMethod.GET)
    @ResponseBody
    public AppResultBean getSwitchInfo(@PathVariable String id, String startTime, String endTime) {
        try {
            if (StringUtils.isNotBlank(id) && StringUtils.isNotBlank(startTime) && StringUtils.isNotBlank(endTime)) {
                JSONObject msg = monitorManagementService.getSwitchInfo(id, startTime, endTime);
                if (msg != null) {
                    return new AppResultBean(msg);
                }
            }
            return new AppResultBean(AppResultBean.PARAM_ERROR);
        } catch (Exception e) {
            log.error("获取监控对象开关历史数据异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 获取监控对象里程速度历史数据
     */
    @ApiOperation(value = "获取监控对象里程速度历史数据", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "监控对象id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "startTime", value = "开始时间", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "endTime", value = "结束时间", required = true,
            paramType = "query", dataType = "string") })
    @RequestMapping(value = "/{id}/history/mileage", method = RequestMethod.GET)
    @ResponseBody
    public AppResultBean getMileageHistoryData(@PathVariable String id, String startTime, String endTime) {
        try {
            JSONObject result = monitorManagementService.getMileageHistoryData(id, startTime, endTime);
            if (result == null) {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
            return new AppResultBean(result);
        } catch (Exception e) {
            log.error("获取里程速度历史数据异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 获取停止数据历史数据
     */
    @ApiOperation(value = "获取停止数据历史数据", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "监控对象id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "startTime", value = "开始时间", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "endTime", value = "结束时间", required = true,
            paramType = "query", dataType = "string") })
    @RequestMapping(value = "/{id}/history/stop", method = RequestMethod.GET)
    @ResponseBody
    public AppResultBean getStopHistoryData(@PathVariable String id, HttpServletRequest request,
        @Validated MonitorRunAndStopDataQueryEntity queryEntity, BindingResult result) {
        queryEntity.setMonitorId(id);
        return AppVersionUtil.getResultData(request, queryEntity, result, monitorManagementService);
    }

    /**
     * 获取监控对象油量历史数据
     */
    @ApiOperation(value = "获取监控对象油量历史数据", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "监控对象id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "startTime", value = "开始时间", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "endTime", value = "结束时间", required = true,
            paramType = "query", dataType = "string") })
    @RequestMapping(value = "/{id}/history/oilMass", method = RequestMethod.GET)
    @ResponseBody
    public AppResultBean getOilHistoryData(@PathVariable String id, String startTime, String endTime) {
        try {
            JSONObject result = monitorManagementService.getOilHistoryData(id, startTime, endTime);
            if (result == null) {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
            return new AppResultBean(result);
        } catch (Exception e) {
            log.error("获取监控对象油量历史数据异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 获取监控对象油耗历史数据
     */
    @ApiOperation(value = "获取监控对象油耗历史数据", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "监控对象id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "startTime", value = "开始时间", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "endTime", value = "结束时间", required = true,
            paramType = "query", dataType = "string") })
    @RequestMapping(value = "/{id}/history/oilConsume", method = RequestMethod.GET)
    @ResponseBody
    public AppResultBean getOilConsumeHistoryData(@PathVariable String id, String startTime, String endTime) {
        try {
            JSONObject result = monitorManagementService.getOilConsumeHistoryData(id, startTime, endTime);
            if (result == null) {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
            return new AppResultBean(result);
        } catch (Exception e) {
            log.error("获取监控对象油耗历史数据异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 获取监控对象温度历史数据
     */
    @ApiOperation(value = "获取监控对象温度历史数据", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "监控对象id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "startTime", value = "开始时间", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "endTime", value = "结束时间", required = true,
            paramType = "query", dataType = "string") })
    @RequestMapping(value = "/{id}/history/temperature", method = RequestMethod.GET)
    @ResponseBody
    public AppResultBean getTemperatureHistoryData(@PathVariable String id, String startTime, String endTime) {
        try {
            JSONObject result = monitorManagementService.getTemperatureHistoryData(id, startTime, endTime);
            if (result == null) {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
            return new AppResultBean(result);
        } catch (Exception e) {
            log.error("获取监控对象温度历史数据异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 获取监控对象工时历史数据
     */
    @RequestMapping(value = "/{id}/history/workHour", method = RequestMethod.GET)
    @ResponseBody
    public AppResultBean getWorkHour(@PathVariable String id, String startTime, String endTime) {
        try {
            if (StringUtils.isNotBlank(id) && StringUtils.isNotBlank(startTime) && StringUtils.isNotBlank(endTime)) {
                JSONObject msg = monitorManagementService.getWorkHoursHistoryData(id, startTime, endTime);
                if (msg != null) {
                    return new AppResultBean(msg);
                }
            }
            return new AppResultBean(AppResultBean.PARAM_ERROR);
        } catch (Exception e) {
            log.error("获取监控对象历史数据异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 获取监控对象设置通道数据
     */
    @ApiOperation(value = "获取监控对象设置通道数据", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "id", value = "监控对象id", required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = "/channelData/{id}", method = RequestMethod.GET)
    @ResponseBody
    public AppResultBean getChannelData(@PathVariable("id") String id) {
        try {
            JSONObject result = monitorManagementService.getChannelData(id);
            if (result == null) {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
            return new AppResultBean(result);
        } catch (Exception e) {
            log.error("获取监控对象设置通道数据异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 视频指令下发0x9102
     */
    @RequestMapping(value = { "/sendVideoParam" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean sendVideoControl(@ModelAttribute("form") final AudioVideoRransmitForm form,
        HttpServletRequest request) {
        try {
            if (form != null) {
                form.setEquipmentType("APP");
                videoService.sendRealTimeControl(form, new GetIpAddr().getIpAddr(request));
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("实时视频,视频窗口右键音视频实时传输控制指令下发异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 获取监控对象指定时间段里程统计数据
     */
    @ApiOperation(value = "获取监控对象指定时间段里程统计数据", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "监控对象id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "startDate", value = "开始日期", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "endDate", value = "结束日期", required = true,
            paramType = "query", dataType = "string") })
    @RequestMapping(value = "/{id}/history/mileDayStatistics", method = RequestMethod.GET)
    @ResponseBody
    public AppResultBean getMileDayStatistics(@PathVariable String id, String startDate, String endDate) {
        try {
            JSONObject result = monitorManagementService.getMileDayStatistics(id, startDate, endDate);
            if (result == null) {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
            return new AppResultBean(result);
        } catch (Exception e) {
            log.error("获取监控对象指定时间段里程统计数据异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 判断监控对象是否在线及是否为808协议
     */
    @ApiOperation(value = "判断监控对象是否在线及是否为808协议", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "id", value = "监控对象id", required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = "/{id}/checkMonitorOnline", method = RequestMethod.GET)
    @ResponseBody
    public AppResultBean checkMonitorOnline(@PathVariable String id) {
        try {
            Integer flag = monitorManagementService.checkMonitorOnline(id);
            if (flag == null) {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
            return new AppResultBean(flag);
        } catch (Exception e) {
            log.error("判断监控对象是否在线异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 监控对象权限校验(判断该监控对象是否解绑以及当前用户是否有该监控对象权限)
     */
    @ApiOperation(value = "监控对象权限校验", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "id", value = "监控对象id", required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = "/{id}/checkMonitorAuth", method = RequestMethod.GET)
    @ResponseBody
    public AppResultBean checkMonitorAuth(@PathVariable String id) {
        try {
            Integer flag = monitorManagementService.checkMonitorAuth(id);
            if (flag == null) {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
            return new AppResultBean(flag);
        } catch (Exception e) {
            log.error("监控对象权限校验异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 轨迹回放图表-载重数据
     */
    @RequestMapping(value = "/loadWeight", method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean getLoadWeight(HttpServletRequest request, @Validated MonitorSensorEntity monitorSensorEntity,
        BindingResult result) {
        try {
            if (result.getAllErrors().size() != 0) {
                return new AppResultBean(AppResultBean.SERVER_ERROR, result.getAllErrors().get(0).getDefaultMessage());
            }
            String requestURI = request.getRequestURI();
            String meth = AppVersionUtil.dealVersionName(requestURI, monitorSensorEntity.getVersion());
            Method method = monitorManagementService.getClass()
                .getMethod(meth, String.class, String.class, String.class, Integer.class);
            return (AppResultBean) method
                .invoke(monitorManagementService, monitorSensorEntity.getMonitorId(),
                    monitorSensorEntity.getStartTime(), monitorSensorEntity.getEndTime(),
                    monitorSensorEntity.getSensorFlag());
        } catch (Exception e) {
            log.error("app历史轨迹图表-载重数据查询异常", e);
            return new AppResultBean(AppResultBean.PARAM_ERROR, sysError);
        }
    }

    /**
     * 轨迹回放图表-胎压数据
     */
    @RequestMapping(value = "/tirePressureData", method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean getTirePressureData(HttpServletRequest request,
        @Validated MonitorSensorEntity monitorSensorEntity, BindingResult result) {
        try {
            if (result.getAllErrors().size() != 0) {
                return new AppResultBean(AppResultBean.PARAM_ERROR, result.getAllErrors().get(0).getDefaultMessage());
            }
            String requestURI = request.getRequestURI();
            String meth = AppVersionUtil.dealVersionName(requestURI, monitorSensorEntity.getVersion());
            Method method = monitorManagementService.getClass()
                .getMethod(meth, String.class, String.class, String.class, Integer.class);
            return (AppResultBean) method
                .invoke(monitorManagementService, monitorSensorEntity.getMonitorId(),
                    monitorSensorEntity.getStartTime(), monitorSensorEntity.getEndTime(),
                    monitorSensorEntity.getSensorFlag());
        } catch (Exception e) {
            log.error("app历史轨迹图表-胎压数据查询异常", e);
            return new AppResultBean(AppResultBean.PARAM_ERROR, sysError);
        }
    }

}

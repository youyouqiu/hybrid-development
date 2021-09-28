package com.zw.api2.controller.workhour;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.api2.swaggerEntity.SwaggerWirelessUpdateParam;
import com.zw.api2.swaggerEntity.SwaggerWorkHourSettingForm;
import com.zw.api2.swaggerEntity.SwaggerWorkHourSettingInfo;
import com.zw.platform.domain.basicinfo.Personnel;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.param.WirelessUpdateParam;
import com.zw.platform.domain.vas.workhourmgt.WorkHourSettingInfo;
import com.zw.platform.domain.vas.workhourmgt.form.WorkHourSettingForm;
import com.zw.platform.domain.vas.workhourmgt.query.WorkHourSettingQuery;
import com.zw.platform.service.basicinfo.VehicleService;
import com.zw.platform.service.oilVehicleSetting.F3OilVehicleSettingService;
import com.zw.platform.service.workhourmgt.WorkHourSettingService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
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
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 工时设置controller
 * @author zhouzongbo on 2019/1/28 16:21
 */
@Controller
@RequestMapping("/api/v/workhourmgt/workhoursetting")
@Api(tags = { "工时管理设置_dev" }, description = "工时管理设置相关api")
public class ApiWorkHourSettingController {
    private static Logger log = LogManager.getLogger(ApiWorkHourSettingController.class);

    @Autowired
    private WorkHourSettingService workHourSettingService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private F3OilVehicleSettingService f3OilVehicleSettingService;

    @Value("${up.error}")
    private String upError;

    @Value("${up.error.workHour.type}")
    private String upErrorWorkHourType;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Value("${vehicle.bind.workHour}")
    private String hasWorkHour;

    @Value("${device.info.null}")
    private String deviceInfoNull;

    /**
     * 分页
     */
    @ApiOperation(value = "工时管理设置列表", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "page", value = "页数（若输入页数大于最大页数，则返回第一页的数据）", required = true, paramType = "query",
            dataType = "long", defaultValue = "1"),
        @ApiImplicitParam(name = "limit", value = "每页显示条数", required = true, paramType = "query", dataType = "long",
            defaultValue = "20"),
        @ApiImplicitParam(name = "protocol", value = "协议类型", required = true, paramType = "query", dataType = "string",
            defaultValue = "1"),
        @ApiImplicitParam(name = "simpleQueryParam", value = "按照监控对象，传感器型号进行模糊搜索", required = false,
            paramType = "query", dataType = "string"), })
    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getListPage(final WorkHourSettingQuery query) {
        try {
            if (query != null) {
                Page<WorkHourSettingInfo> result = workHourSettingService.findWorkHourSettingList(query);
                return new PageGridBean(result, true);
            }
            return null;
        } catch (Exception e) {
            log.error("分页查询工时设置列表异常", e);
            return new PageGridBean(false);
        }
    }

    /**
     * 设置页面
     * @return ModelAndView
     */
    @ApiOperation(value = "设置工时管理", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/getWorkHourSettingBindPage_{id}_{monitorType}", method = RequestMethod.GET)
    @AvoidRepeatSubmitToken(setToken = true)
    @ResponseBody
    public JsonResultBean getWorkHourSettingPage(
        @ApiParam(value = "车辆id", required = true) @PathVariable final String id,
        @ApiParam(value = "监控对象类型", required = true) @PathVariable final Integer monitorType) {
        try {
            JSONObject data = new JSONObject();
            // 车辆信息
            buildCommonResultData(id, data, monitorType);
            return new JsonResultBean(data);
        } catch (Exception e) {
            log.error("获取工时设置页面异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 设置
     * @param bindingResult bindingResult
     * @return JsonResultBean
     */
    @ApiOperation(value = "工时管理设置", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/addWorkHourSetting", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean addWorkHourSetting(
        @Validated({ ValidGroupAdd.class }) @ModelAttribute("swaggerForm") final SwaggerWorkHourSettingForm swaggerForm,
        final BindingResult bindingResult) {
        try {
            WorkHourSettingForm form = new WorkHourSettingForm();
            BeanUtils.copyProperties(swaggerForm, form);
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT,
                    SpringBindingResultWrapper.warpErrors(bindingResult));
            }
            WorkHourSettingInfo info = workHourSettingService.findVehicleWorkHourSettingByVid(form.getVehicleId());
            if (Objects.nonNull(info) && StringUtils.isNotBlank(info.getId())) {
                return new JsonResultBean(JsonResultBean.FAULT, hasWorkHour);
            }
            String ipAddress = new GetIpAddr().getIpAddr(request);
            return workHourSettingService.addWorkHourSetting(form, ipAddress);
        } catch (Exception e) {
            log.error("设置工时异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 根据车辆Id查询车与工时的绑定信息
     * @param vehicleId vehicleId
     */
    @ApiOperation(value = "根据车辆Id查询车与工时的绑定信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "vehicleId", value = "车辆id", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/getWorkHourBindInfo", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean referenceBrandSet(String vehicleId) {
        try {
            if (StringUtils.isNotBlank(vehicleId)) {
                WorkHourSettingInfo workHourSetting = workHourSettingService.findVehicleWorkHourSettingByVid(vehicleId);
                return new JsonResultBean(workHourSetting);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("根据车辆Id查询车与工时的绑定信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }

    }

    /**
     * 修改页面
     * @param vehicleId vehicleId
     * @return ModelAndView
     */
    @ApiOperation(value = "修改工时管理", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/getWorkHourSettingEditPage_{vehicleId}_{type}_{monitorType}.gsp",
        method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean getWorkHourSettingEditPage(
        @ApiParam(value = "车辆id", required = true) @PathVariable final String vehicleId,
        @ApiParam(value = "传感器序号: 0:发动机1; 1:发动机2", required = true) @PathVariable final String type,
        @ApiParam(value = "监控对象类型(1:车,2:物,3:人)", required = true) @PathVariable final Integer monitorType,
        HttpServletResponse response) {
        try {
            JSONObject data = new JSONObject();
            buildCommonResultData(vehicleId, data, monitorType);
            // 编辑数据
            WorkHourSettingInfo vehicleWorkHourSetting =
                workHourSettingService.findVehicleWorkHourSettingByVid(vehicleId);
            if (Objects.nonNull(vehicleWorkHourSetting)) {
                data.put("vehicleWorkHourSetting", vehicleWorkHourSetting);
                data.put("type", type);
                return new JsonResultBean(data);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT, "该条数据已解除绑定");
            }
        } catch (Exception e) {
            log.error("获取工时修改页面异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 车辆信息和参考对象信息
     */
    private void buildCommonResultData(String id, JSONObject data, Integer monitorType) throws Exception {

        // 参考对象
        JSONObject monitor = new JSONObject();
        if (monitorType == 1) {
            // 车辆信息
            VehicleInfo vehicleInfo = vehicleService.findVehicleById(id);
            monitor.put("id", vehicleInfo.getId());
            monitor.put("brand", vehicleInfo.getBrand());
        }
        if (monitorType == 3) {
            // 人员信息
            // Personnel vehicleInfo = personnelService.findPeopleById(id);
            Personnel vehicleInfo = null;
            monitor.put("id", vehicleInfo.getId());
            monitor.put("brand", vehicleInfo.getPeopleNumber());
        }
        if (monitorType == 2) {
            // 物品信息
            // ThingInfo vehicleInfo = thingInfoService.get(id);
            // monitor.put("id", vehicleInfo.getId());
            // monitor.put("brand", vehicleInfo.getThingNumber());
        }
        List<WorkHourSettingInfo> vehicleList = workHourSettingService.findReferenceVehicle();
        data.put("vehicleInfo", monitor);
        data.put("monitorType", monitorType);
        data.put("vehicleList", JSON.toJSONString(vehicleList));
    }

    /**
     * 修改
     * @param sform         sform
     * @param bindingResult bindingResult
     * @return JsonResultBean
     */
    @ApiOperation(value = "工时管理修改", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/updateWorkHourSetting", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateWorkHourSetting(
        @Validated({ ValidGroupUpdate.class }) @ModelAttribute("sform") final SwaggerWorkHourSettingForm sform,
        final BindingResult bindingResult) {
        try {
            WorkHourSettingForm form = new WorkHourSettingForm();
            BeanUtils.copyProperties(sform, form);
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT,
                    SpringBindingResultWrapper.warpErrors(bindingResult));
            }

            String ipAddress = new GetIpAddr().getIpAddr(request);
            return workHourSettingService.updateWorkHourSetting(form, ipAddress);
        } catch (Exception e) {
            log.error("修改工时异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 解绑
     * @param id id 发动机
     * @return JsonResultBean
     */
    @ApiOperation(value = "工时管理解绑车辆", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/deleteWorkHourSettingBind_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteWorkHourSettingBind(@ApiParam(value = "工时管理配置id") @PathVariable final String id) {
        try {
            if (StringUtils.isNotBlank(id)) {
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return workHourSettingService.deleteWorkHourSettingBind(id, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("解绑工时异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 批量解绑
     * @param ids ids
     * @return JsonResultBean
     */
    @ApiOperation(value = "工时管理批量解绑车辆", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "ids", value = "工时管理配置ids（id以','隔开）", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/deleteMoreWorkHourSettingBind", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMoreWorkHourSettingBind(final String ids) {
        try {
            if (StringUtils.isNotBlank(ids)) {
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return workHourSettingService.deleteMoreWorkHourSettingBind(ids, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("批量解绑工时异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 详情
     * @param vehicleId vehicleId
     * @return ModelAndView
     */
    @ApiOperation(value = "获取工时详情页面", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/getWorkHourSettingDetailPage_{vehicleId}_{type}.gsp", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean getWorkHourSettingDetail(
        @ApiParam(value = "车辆id", required = true) @PathVariable final String vehicleId,
        @ApiParam(value = "传感器序号: 0:发动机1; 1:发动机2", required = true) @PathVariable final String type) {
        try {
            JSONObject data = new JSONObject();
            WorkHourSettingInfo vehicleWorkHourSetting =
                workHourSettingService.findVehicleWorkHourSettingByVid(vehicleId);
            vehicleWorkHourSetting.setSensorSequenceType(type);
            data.put("vehicleWorkHourSetting", vehicleWorkHourSetting);
            return new JsonResultBean(data);
        } catch (Exception e) {
            log.error("获取工时详情页面异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 读取基本信息(getF3Param)
     * @param id sensorVehicleId
     * @return ModelAndView
     */
    @ApiOperation(value = "获取工时传感器基本信息页面", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/basicInfo_{id}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean basicInfo(@ApiParam(value = "工时设置id", required = true) @PathVariable("id") final String id) {
        try {
            JSONObject data = new JSONObject();
            // 根据id查询车绑定的发动机
            getWorkHourBindData(id, data);
            return new JsonResultBean(data);
        } catch (Exception e) {
            log.error("基本信息弹出页面异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 根据传感器车辆绑定表id查询数据
     */
    private void getWorkHourBindData(String id, JSONObject data) {
        WorkHourSettingInfo workHourSettingInfo = workHourSettingService.getSensorVehicleByBindId(id);
        workHourSettingInfo.setSensorPeripheralID("8" + workHourSettingInfo.getSensorSequence());
        data.put("result", workHourSettingInfo);
    }

    /**
     * 常规参数(getF3Param)
     * @param id id
     * @return ModelAndView
     */
    @ApiOperation(value = "获取该监控对象工时设置的页面", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/general_{id}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean generalPage(
        @ApiParam(value = "工时设置id", required = true) @PathVariable("id") final String id) {
        try {
            JSONObject data = new JSONObject();
            WorkHourSettingInfo workHourSettingInfo = workHourSettingService.getSensorVehicleByBindId(id);
            workHourSettingInfo.setSensorPeripheralID("8" + workHourSettingInfo.getSensorSequence());
            Integer baudRateCalculateTimeScope = workHourSettingInfo.getBaudRateCalculateTimeScope();
            if (Objects.nonNull(baudRateCalculateTimeScope)) {
                switch (baudRateCalculateTimeScope) {
                    case 1:
                        workHourSettingInfo.setBaudRateCalculateTimeScope(10);
                        break;
                    case 2:
                        workHourSettingInfo.setBaudRateCalculateTimeScope(15);
                        break;
                    case 3:
                        workHourSettingInfo.setBaudRateCalculateTimeScope(20);
                        break;
                    case 4:
                        workHourSettingInfo.setBaudRateCalculateTimeScope(30);
                        break;
                    case 5:
                        workHourSettingInfo.setBaudRateCalculateTimeScope(60);
                        break;
                    default:
                        break;
                }
            }
            data.put("result", workHourSettingInfo);
            return new JsonResultBean(data);
        } catch (Exception e) {
            log.error("常规参数弹出页面异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 远程升级
     * @param id id
     * @return ModelAndView
     */
    @ApiOperation(value = "获取该监控对象工时传感器远程升级页面", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/upgrade_{id}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean upgradePage(
        @ApiParam(value = "工时设置id", required = true) @PathVariable("id") final String id) {
        try {
            JSONObject data = new JSONObject();
            getWorkHourBindData(id, data);
            return new JsonResultBean(data);
        } catch (Exception e) {
            log.error("远程升级弹出页面异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 私有参数（校验车辆是否在线: v/oilmassmgt/oilcalibration/checkVehicleOnlineStatus）
     * @param id id
     * @return ModelAndView
     */
    @ApiOperation(value = "获取该监控对象工时传感器私有参数设置页面", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/parameters_{id}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean parametersPage(
        @ApiParam(value = "工时设置id", required = true) @PathVariable("id") final String id) {
        try {
            JSONObject data = new JSONObject();
            getWorkHourBindData(id, data);
            return new JsonResultBean(data);
        } catch (Exception e) {
            log.error("私有参数弹出页面异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 通讯参数
     * @param id id
     * @return 通讯参数
     */
    @ApiOperation(value = "获取该监控对象工时传感器通讯参数页面", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/newsletter_{id}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean newsletterPage(
        @ApiParam(value = "工时设置id", required = true) @PathVariable("id") final String id) {
        try {
            JSONObject data = new JSONObject();
            getWorkHourBindData(id, data);
            return new JsonResultBean(data);
        } catch (Exception e) {
            log.error("通讯参数弹出页面异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 工时基准修正
     * @param id id
     * @return ModelAndView
     */
    @ApiIgnore
    @RequestMapping(value = { "/getWorkHourBaseValue_{id}.gsp" }, method = RequestMethod.GET)
    public JsonResultBean getWorkHourBaseValue(@PathVariable("id") final String id) {
        try {
            JSONObject data = new JSONObject();
            getWorkHourBindData(id, data);
            return new JsonResultBean(data);
        } catch (Exception e) {
            log.error("工时基准修正弹出页面异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 根据id下发参数
     * sendParam: [{
     *   "sensorVehicleId":"eaae4ea2-81e2-43f6-a579-1a5a3e59ceb4",
     *   "paramId":"",
     *   "vehicleId":"c0cd2974-b2c0-405c-ab47-282317820f59"
     * }]
     * @return JsonResultBean
     */
    @ApiOperation(value = "工时管理根据id下发参数", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "sensorVehicleId", value = "传感器id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "paramId", value = "下发的参数id", paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "vehicleId", value = "车辆id", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/sendWorkHourSetting", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean sendWorkHourSetting(String sensorVehicleId, String paramId, String vehicleId) {
        try {
            List<JSONObject> paramList = new ArrayList<>();
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("sensorVehicleId", sensorVehicleId);
            paramList.add(jsonObject1);
            JSONObject jsonObject2 = new JSONObject();
            jsonObject2.put("paramId", paramId);
            paramList.add(jsonObject2);
            JSONObject jsonObject3 = new JSONObject();
            jsonObject3.put("vehicleId", vehicleId);
            paramList.add(jsonObject3);
            if (CollectionUtils.isNotEmpty(paramList)) {
                String ip = new GetIpAddr().getIpAddr(request);
                // 工时下发
                workHourSettingService.sendWorkHourSetting(paramList, ip);
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("下发参数异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 获取F3传感器数据
     * @param vid         vid
     * @param commandType 基本信息: 0xF8; 通讯参数: 0xF5; 常规参数: 0xF4;
     * @param sensorID    工时外设ID : 0x80(发动机1)(16进制:128) 0x81(发动机2：129):
     * @return JsonResultBean
     */
    @ApiOperation(value = "获取F3传感器数据", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "vid", value = "车辆id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "commandType", value = "基本信息: 0xF8; 通讯参数: 0xF5; 常规参数: 0xF4;", required = true,
            paramType = "query", dataType = "Integer"),
        @ApiImplicitParam(name = "sensorID", value = "工时外设ID : 0x80(发动机1)(16进制:128) 0x81(发动机2：129)", required = true,
            paramType = "query", dataType = "Integer") })
    @RequestMapping(value = "/getF3Param", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getF3Param(String vid, Integer commandType, Integer sensorID) {
        try {
            return f3OilVehicleSettingService
                .sendF3SensorParam(vid, Integer.toHexString(sensorID), Integer.toHexString(commandType));
        } catch (Exception e) {
            log.error("获取F3传感器数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 获取F3传感器平台设置常规参数
     * @param id id
     * @return JsonResultBean
     */
    @ApiOperation(value = "获取F3传感器平台设置常规参数", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "传感器与车辆配置id", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/getSensorSetting", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getSensorSetting(String id) {
        try {
            WorkHourSettingInfo workHourSettingInfo = workHourSettingService.getSensorVehicleByBindId(id);
            JSONObject msg = new JSONObject();
            msg.put("setting", workHourSettingInfo);
            if (workHourSettingInfo != null) {
                return new JsonResultBean(JsonResultBean.SUCCESS, msg.toJSONString());
            }
            return new JsonResultBean(JsonResultBean.FAULT, deviceInfoNull);
        } catch (Exception e) {
            log.error("获取F3传感器数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 获取F3传感器私有数据(下发调用)
     * @param vid        vid
     * @param sensorID   工时外设ID : 0x80(发动机1)(16进制:128) 0x81(发动机2: 129):
     * @param commandStr 下发内容
     * @return JsonResultBean
     */
    @ApiOperation(value = "获取F3传感器私有参数(下发调用)", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "vid", value = "车辆id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "commandStr", value = "下发内容", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "sensorID", value = "工时外设ID : 0x80(发动机1)(16进制:128) 0x81(发动机2：129)", required = true,
            paramType = "query", dataType = "Integer") })
    @RequestMapping(value = "/getF3PrivateParam", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getF3PrivateParam(String vid, Integer sensorID, String commandStr) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            return f3OilVehicleSettingService
                .sendF3SensorPrivateParam(vid, Integer.toHexString(sensorID), commandStr, ipAddress, "3");
        } catch (Exception e) {
            log.error("获取F3传感器私有数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 工时传感器常规参数修正下发
     * @param swaggerSetting swaggerSetting
     * @return JsonResultBean
     */
    @ApiOperation(value = "工时传感器常规参数修正下发", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "deal_type", value = "以什么为准 pt:平台; report:传感器", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/updateSensorSetting", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateSensorSetting(
        @ModelAttribute("swaggerSetting") final SwaggerWorkHourSettingInfo swaggerSetting) {
        try {
            WorkHourSettingInfo setting = new WorkHourSettingInfo();
            BeanUtils.copyProperties(swaggerSetting, setting);
            // pt:平台; report:以传感器为准
            String dealType = request.getParameter("deal_type");
            String ipAddress = new GetIpAddr().getIpAddr(request);
            return f3OilVehicleSettingService.updateWorkHourSetting(setting, dealType, ipAddress, 0);
        } catch (Exception e) {
            log.error("传感器常规参数修正下发异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 外设软件升级
     */
    @ApiOperation(value = "传感器远程升级", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "commandType", value = "命令类型0x80: 128; 0x81:129", required = false,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "vehicleId", value = "车辆id", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = { "/saveWirelessUpdate" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean save(
        @ModelAttribute("swaggerWirelessParam") final SwaggerWirelessUpdateParam swaggerWirelessParam, String vehicleId,
        final BindingResult bindingResult) {
        // 数据校验
        if (bindingResult.hasErrors()) {
            return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
        }
        try {
            WirelessUpdateParam wirelessParam = new WirelessUpdateParam();
            wirelessParam.setWAddress(swaggerWirelessParam.getAddress());
            wirelessParam.setWTcpPort(swaggerWirelessParam.getTcpPort());
            wirelessParam.setWDailPwd((swaggerWirelessParam.getDailPwd()));
            wirelessParam.setWDailUserName(swaggerWirelessParam.getDailUserName());
            wirelessParam.setFirmwareVersion(swaggerWirelessParam.getFirmwareVersion());
            int commandType;
            try {
                String commandTypeStr = request.getParameter("commandType");
                if (StringUtil.isNull(commandTypeStr)) {
                    return new JsonResultBean(JsonResultBean.FAULT, upErrorWorkHourType);
                }
                commandType = Integer.parseInt(commandTypeStr);
            } catch (Exception ex) {
                return new JsonResultBean(JsonResultBean.FAULT, upErrorWorkHourType);

            }
            String ipAddress = new GetIpAddr().getIpAddr(request);// 获得访问ip
            return f3OilVehicleSettingService.updateWirelessUpdate(wirelessParam, vehicleId, commandType, ipAddress, 1);
        } catch (Exception e) {
            log.error("外设软件升级：" + e.getMessage(), e);
            return new JsonResultBean(JsonResultBean.FAULT, upError);
        }
    }

    /**
     * 工时基值修正下发
     * @param setting setting
     * @return JsonResultBean
     * @deprecated
     */
    @ApiIgnore
    @RequestMapping(value = "/updateWorkHourBaseValue", method = RequestMethod.POST)
    @ResponseBody
    @Deprecated
    public JsonResultBean updateWorkHourBaseValue(WorkHourSettingInfo setting) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            return f3OilVehicleSettingService.updateWorkHourSetting(setting, "", ipAddress, 1);
        } catch (Exception e) {
            log.error("工时基值修正下发异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }
}


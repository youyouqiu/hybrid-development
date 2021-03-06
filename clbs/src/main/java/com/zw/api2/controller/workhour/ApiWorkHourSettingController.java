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
 * ????????????controller
 * @author zhouzongbo on 2019/1/28 16:21
 */
@Controller
@RequestMapping("/api/v/workhourmgt/workhoursetting")
@Api(tags = { "??????????????????_dev" }, description = "????????????????????????api")
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
     * ??????
     */
    @ApiOperation(value = "????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "page", value = "???????????????????????????????????????????????????????????????????????????", required = true, paramType = "query",
            dataType = "long", defaultValue = "1"),
        @ApiImplicitParam(name = "limit", value = "??????????????????", required = true, paramType = "query", dataType = "long",
            defaultValue = "20"),
        @ApiImplicitParam(name = "protocol", value = "????????????", required = true, paramType = "query", dataType = "string",
            defaultValue = "1"),
        @ApiImplicitParam(name = "simpleQueryParam", value = "??????????????????????????????????????????????????????", required = false,
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
            log.error("????????????????????????????????????", e);
            return new PageGridBean(false);
        }
    }

    /**
     * ????????????
     * @return ModelAndView
     */
    @ApiOperation(value = "??????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/getWorkHourSettingBindPage_{id}_{monitorType}", method = RequestMethod.GET)
    @AvoidRepeatSubmitToken(setToken = true)
    @ResponseBody
    public JsonResultBean getWorkHourSettingPage(
        @ApiParam(value = "??????id", required = true) @PathVariable final String id,
        @ApiParam(value = "??????????????????", required = true) @PathVariable final Integer monitorType) {
        try {
            JSONObject data = new JSONObject();
            // ????????????
            buildCommonResultData(id, data, monitorType);
            return new JsonResultBean(data);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * ??????
     * @param bindingResult bindingResult
     * @return JsonResultBean
     */
    @ApiOperation(value = "??????????????????", authorizations = {
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
            log.error("??????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * ????????????Id?????????????????????????????????
     * @param vehicleId vehicleId
     */
    @ApiOperation(value = "????????????Id?????????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "vehicleId", value = "??????id", required = true, paramType = "query",
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
            log.error("????????????Id???????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }

    }

    /**
     * ????????????
     * @param vehicleId vehicleId
     * @return ModelAndView
     */
    @ApiOperation(value = "??????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/getWorkHourSettingEditPage_{vehicleId}_{type}_{monitorType}.gsp",
        method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean getWorkHourSettingEditPage(
        @ApiParam(value = "??????id", required = true) @PathVariable final String vehicleId,
        @ApiParam(value = "???????????????: 0:?????????1; 1:?????????2", required = true) @PathVariable final String type,
        @ApiParam(value = "??????????????????(1:???,2:???,3:???)", required = true) @PathVariable final Integer monitorType,
        HttpServletResponse response) {
        try {
            JSONObject data = new JSONObject();
            buildCommonResultData(vehicleId, data, monitorType);
            // ????????????
            WorkHourSettingInfo vehicleWorkHourSetting =
                workHourSettingService.findVehicleWorkHourSettingByVid(vehicleId);
            if (Objects.nonNull(vehicleWorkHourSetting)) {
                data.put("vehicleWorkHourSetting", vehicleWorkHourSetting);
                data.put("type", type);
                return new JsonResultBean(data);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT, "???????????????????????????");
            }
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * ?????????????????????????????????
     */
    private void buildCommonResultData(String id, JSONObject data, Integer monitorType) throws Exception {

        // ????????????
        JSONObject monitor = new JSONObject();
        if (monitorType == 1) {
            // ????????????
            VehicleInfo vehicleInfo = vehicleService.findVehicleById(id);
            monitor.put("id", vehicleInfo.getId());
            monitor.put("brand", vehicleInfo.getBrand());
        }
        if (monitorType == 3) {
            // ????????????
            // Personnel vehicleInfo = personnelService.findPeopleById(id);
            Personnel vehicleInfo = null;
            monitor.put("id", vehicleInfo.getId());
            monitor.put("brand", vehicleInfo.getPeopleNumber());
        }
        if (monitorType == 2) {
            // ????????????
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
     * ??????
     * @param sform         sform
     * @param bindingResult bindingResult
     * @return JsonResultBean
     */
    @ApiOperation(value = "??????????????????", authorizations = {
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
            log.error("??????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * ??????
     * @param id id ?????????
     * @return JsonResultBean
     */
    @ApiOperation(value = "????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/deleteWorkHourSettingBind_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteWorkHourSettingBind(@ApiParam(value = "??????????????????id") @PathVariable final String id) {
        try {
            if (StringUtils.isNotBlank(id)) {
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return workHourSettingService.deleteWorkHourSettingBind(id, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("??????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * ????????????
     * @param ids ids
     * @return JsonResultBean
     */
    @ApiOperation(value = "??????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "ids", value = "??????????????????ids???id???','?????????", required = true, paramType = "query",
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
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * ??????
     * @param vehicleId vehicleId
     * @return ModelAndView
     */
    @ApiOperation(value = "????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/getWorkHourSettingDetailPage_{vehicleId}_{type}.gsp", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean getWorkHourSettingDetail(
        @ApiParam(value = "??????id", required = true) @PathVariable final String vehicleId,
        @ApiParam(value = "???????????????: 0:?????????1; 1:?????????2", required = true) @PathVariable final String type) {
        try {
            JSONObject data = new JSONObject();
            WorkHourSettingInfo vehicleWorkHourSetting =
                workHourSettingService.findVehicleWorkHourSettingByVid(vehicleId);
            vehicleWorkHourSetting.setSensorSequenceType(type);
            data.put("vehicleWorkHourSetting", vehicleWorkHourSetting);
            return new JsonResultBean(data);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * ??????????????????(getF3Param)
     * @param id sensorVehicleId
     * @return ModelAndView
     */
    @ApiOperation(value = "???????????????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/basicInfo_{id}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean basicInfo(@ApiParam(value = "????????????id", required = true) @PathVariable("id") final String id) {
        try {
            JSONObject data = new JSONObject();
            // ??????id???????????????????????????
            getWorkHourBindData(id, data);
            return new JsonResultBean(data);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * ??????????????????????????????id????????????
     */
    private void getWorkHourBindData(String id, JSONObject data) {
        WorkHourSettingInfo workHourSettingInfo = workHourSettingService.getSensorVehicleByBindId(id);
        workHourSettingInfo.setSensorPeripheralID("8" + workHourSettingInfo.getSensorSequence());
        data.put("result", workHourSettingInfo);
    }

    /**
     * ????????????(getF3Param)
     * @param id id
     * @return ModelAndView
     */
    @ApiOperation(value = "??????????????????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/general_{id}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean generalPage(
        @ApiParam(value = "????????????id", required = true) @PathVariable("id") final String id) {
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
            log.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * ????????????
     * @param id id
     * @return ModelAndView
     */
    @ApiOperation(value = "??????????????????????????????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/upgrade_{id}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean upgradePage(
        @ApiParam(value = "????????????id", required = true) @PathVariable("id") final String id) {
        try {
            JSONObject data = new JSONObject();
            getWorkHourBindData(id, data);
            return new JsonResultBean(data);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * ???????????????????????????????????????: v/oilmassmgt/oilcalibration/checkVehicleOnlineStatus???
     * @param id id
     * @return ModelAndView
     */
    @ApiOperation(value = "????????????????????????????????????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/parameters_{id}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean parametersPage(
        @ApiParam(value = "????????????id", required = true) @PathVariable("id") final String id) {
        try {
            JSONObject data = new JSONObject();
            getWorkHourBindData(id, data);
            return new JsonResultBean(data);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * ????????????
     * @param id id
     * @return ????????????
     */
    @ApiOperation(value = "??????????????????????????????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/newsletter_{id}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean newsletterPage(
        @ApiParam(value = "????????????id", required = true) @PathVariable("id") final String id) {
        try {
            JSONObject data = new JSONObject();
            getWorkHourBindData(id, data);
            return new JsonResultBean(data);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * ??????????????????
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
            log.error("????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * ??????id????????????
     * sendParam: [{
     *   "sensorVehicleId":"eaae4ea2-81e2-43f6-a579-1a5a3e59ceb4",
     *   "paramId":"",
     *   "vehicleId":"c0cd2974-b2c0-405c-ab47-282317820f59"
     * }]
     * @return JsonResultBean
     */
    @ApiOperation(value = "??????????????????id????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "sensorVehicleId", value = "?????????id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "paramId", value = "???????????????id", paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "vehicleId", value = "??????id", required = true, paramType = "query",
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
                // ????????????
                workHourSettingService.sendWorkHourSetting(paramList, ip);
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("??????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????F3???????????????
     * @param vid         vid
     * @param commandType ????????????: 0xF8; ????????????: 0xF5; ????????????: 0xF4;
     * @param sensorID    ????????????ID : 0x80(?????????1)(16??????:128) 0x81(?????????2???129):
     * @return JsonResultBean
     */
    @ApiOperation(value = "??????F3???????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "vid", value = "??????id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "commandType", value = "????????????: 0xF8; ????????????: 0xF5; ????????????: 0xF4;", required = true,
            paramType = "query", dataType = "Integer"),
        @ApiImplicitParam(name = "sensorID", value = "????????????ID : 0x80(?????????1)(16??????:128) 0x81(?????????2???129)", required = true,
            paramType = "query", dataType = "Integer") })
    @RequestMapping(value = "/getF3Param", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getF3Param(String vid, Integer commandType, Integer sensorID) {
        try {
            return f3OilVehicleSettingService
                .sendF3SensorParam(vid, Integer.toHexString(sensorID), Integer.toHexString(commandType));
        } catch (Exception e) {
            log.error("??????F3?????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????F3?????????????????????????????????
     * @param id id
     * @return JsonResultBean
     */
    @ApiOperation(value = "??????F3?????????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "????????????????????????id", required = true, paramType = "query",
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
            log.error("??????F3?????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????F3?????????????????????(????????????)
     * @param vid        vid
     * @param sensorID   ????????????ID : 0x80(?????????1)(16??????:128) 0x81(?????????2: 129):
     * @param commandStr ????????????
     * @return JsonResultBean
     */
    @ApiOperation(value = "??????F3?????????????????????(????????????)", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "vid", value = "??????id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "commandStr", value = "????????????", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "sensorID", value = "????????????ID : 0x80(?????????1)(16??????:128) 0x81(?????????2???129)", required = true,
            paramType = "query", dataType = "Integer") })
    @RequestMapping(value = "/getF3PrivateParam", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getF3PrivateParam(String vid, Integer sensorID, String commandStr) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            return f3OilVehicleSettingService
                .sendF3SensorPrivateParam(vid, Integer.toHexString(sensorID), commandStr, ipAddress, "3");
        } catch (Exception e) {
            log.error("??????F3???????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ???????????????????????????????????????
     * @param swaggerSetting swaggerSetting
     * @return JsonResultBean
     */
    @ApiOperation(value = "???????????????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "deal_type", value = "??????????????? pt:??????; report:?????????", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/updateSensorSetting", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateSensorSetting(
        @ModelAttribute("swaggerSetting") final SwaggerWorkHourSettingInfo swaggerSetting) {
        try {
            WorkHourSettingInfo setting = new WorkHourSettingInfo();
            BeanUtils.copyProperties(swaggerSetting, setting);
            // pt:??????; report:??????????????????
            String dealType = request.getParameter("deal_type");
            String ipAddress = new GetIpAddr().getIpAddr(request);
            return f3OilVehicleSettingService.updateWorkHourSetting(setting, dealType, ipAddress, 0);
        } catch (Exception e) {
            log.error("???????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????????????????
     */
    @ApiOperation(value = "?????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "commandType", value = "????????????0x80: 128; 0x81:129", required = false,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "vehicleId", value = "??????id", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = { "/saveWirelessUpdate" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean save(
        @ModelAttribute("swaggerWirelessParam") final SwaggerWirelessUpdateParam swaggerWirelessParam, String vehicleId,
        final BindingResult bindingResult) {
        // ????????????
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
            String ipAddress = new GetIpAddr().getIpAddr(request);// ????????????ip
            return f3OilVehicleSettingService.updateWirelessUpdate(wirelessParam, vehicleId, commandType, ipAddress, 1);
        } catch (Exception e) {
            log.error("?????????????????????" + e.getMessage(), e);
            return new JsonResultBean(JsonResultBean.FAULT, upError);
        }
    }

    /**
     * ????????????????????????
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
            log.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }
}


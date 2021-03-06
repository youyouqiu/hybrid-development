package com.zw.api2.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.api2.swaggerEntity.SwaggerFuelVehicleForm;
import com.zw.api2.swaggerEntity.SwaggerSimpleFluxSensorQuery;
import com.zw.api2.swaggerEntity.SwaggerWirelessUpdateParam;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.param.WirelessUpdateParam;
import com.zw.platform.domain.vas.oilmgt.FluxSensor;
import com.zw.platform.domain.vas.oilmgt.FuelVehicle;
import com.zw.platform.domain.vas.oilmgt.form.FluxSensorBindForm;
import com.zw.platform.domain.vas.oilmgt.query.FluxSensorBindQuery;
import com.zw.platform.service.alarm.AlarmSettingService;
import com.zw.platform.service.basicinfo.VehicleService;
import com.zw.platform.service.oilVehicleSetting.F3OilVehicleSettingService;
import com.zw.platform.service.oilmgt.FluxSensorBindService;
import com.zw.platform.service.oilmgt.FluxSensorService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.SpringBindingResultWrapper;
import com.zw.platform.util.common.ValidGroupUpdate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * <p>Title: ?????????????????????Controller</p> <p>Copyright: Copyright (c) 2016</p> <p>Company: ZhongWei</p> <p>team:
 * ZhongWeiTeam</p>
 * @version 1.0
 * @author: wangying
 * @date 2016???9???18?????????5:07:13
 */
@RestController
@RequestMapping("/api/v/oilmgt/fluxsensorbind")
@Api(tags = { "???????????????????????????????????????" }, description = "????????????????????????api")
public class ApiFluxSensorBindController {
    private static Logger log = LogManager.getLogger(ApiFluxSensorBindController.class);

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Value("${bound.seccess}")
    private String boundSeccess;

    @Value("${bound.fail}")
    private String boundFail;

    @Value("${edit.success}")
    private String editSeccess;

    @Value("${edit.fail}")
    private String editFail;

    @Value("${up.error}")
    private String upError;

    @Value("${up.error.terminal.off.line}")
    private String upErrorTerminalOffLine;

    @Value("${terminal.off.line}")
    private String terminalOffLine;

    @Value("${device.info.null}")
    private String deviceInfoNull;

    @Value("${vehicle.bound.oilwear}")
    private String vehicleBoundOilwear;

    @Value("${up.error.fluxsensor.type}")
    private String upErrorFluxsensorType;

    @Value("${data.relieve.bound}")
    private String dataRelieveBound;

    @Autowired
    private FluxSensorBindService fluxSensorBindService;

    @Autowired
    private FluxSensorService fluxSensorService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private F3OilVehicleSettingService f3OilVehicleSettingService;

    @Autowired
    private AlarmSettingService alarmSettingService;

    @Autowired
    private HttpServletRequest request;

    private static final String LIST_PAGE = "vas/oilmgt/fluxsensorbind/list";

    private static final String BIND_PAGE = "vas/oilmgt/fluxsensorbind/bind";

    private static final String EDIT_PAGE = "vas/oilmgt/fluxsensorbind/edit";

    private static final String DETAIL_PAGE = "vas/oilmgt/fluxsensorbind/detail";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    private static final String UPGRADE_PAGE = "vas/oilmgt/fluxsensorbind/upgrade";

    private static final String BASICINFO_PAGE = "vas/oilmgt/fluxsensorbind/basicInfo"; // ????????????

    private static final String GENERAL_PAGE = "vas/oilmgt/fluxsensorbind/general"; // ????????????

    private static final String NEWSLETTER_PAGE = "vas/oilmgt/fluxsensorbind/newsletter"; // ????????????

    private static final String PARAMETERS_PAGE = "vas/oilmgt/fluxsensorbind/parameters"; // ????????????

    @ApiIgnore
    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() throws BusinessException {
        return LIST_PAGE;
    }

    @ApiOperation(value = "??????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getListPage(@ModelAttribute(
        "swaggerSimpleFluxSensorQuery") final SwaggerSimpleFluxSensorQuery swaggerSimpleFluxSensorQuery) {
        FluxSensorBindQuery query = new FluxSensorBindQuery();
        BeanUtils.copyProperties(swaggerSimpleFluxSensorQuery, query);
        try {
            if (query != null) {
                Page<FuelVehicle> result = (Page<FuelVehicle>) fluxSensorBindService.findFluxSensorBind(query);
                return new PageGridBean(result, true);
            }
            return null;
        } catch (Exception e) {
            log.error("?????????????????????findFluxSensorBind?????????", e);
            return new PageGridBean(false);
        }
    }

    @ApiOperation(value = "????????????id?????????????????????????????????", notes = "???????????????????????????????????????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/bind_{id}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean bindPage(@PathVariable("id") @ApiParam("??????????????????id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(BIND_PAGE);
            // ?????????
            VehicleInfo vehicle = alarmSettingService.findPeopleOrVehicleOrThingById(id);
            // ?????????????????????
            List<FuelVehicle> vehicleList = fluxSensorBindService.findReferenceVehicle();
            // ?????????????????????
            List<FluxSensor> fluxSensorList = fluxSensorService.findFluxSensorByPage(null, false);
            mav.addObject("vehicle", vehicle);
            mav.addObject("fluxSensorList", JSON.toJSONString(fluxSensorList));
            mav.addObject("vehicleList", JSON.toJSONString(vehicleList));
            return new JsonResultBean(mav.getModel());
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(sysErrorMsg);
        }
    }

    /**
     * @param form
     * @param bindingResult
     * @return JsonResultBean
     * @throws BusinessException
     * @throws @author           wangying
     * @Title: ??????
     */
    @ApiOperation(value = "?????????????????????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "oilWearId", value = "???????????????id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "vehicleId", value = "??????id", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/bind.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean bind(
        @Validated({ ValidGroupUpdate.class }) @ModelAttribute("form") final FluxSensorBindForm form,
        final BindingResult bindingResult) {
        try {
            if (form != null) {
                // ????????????
                if (bindingResult.hasErrors()) {
                    return new JsonResultBean(JsonResultBean.FAULT,
                        SpringBindingResultWrapper.warpErrors(bindingResult));
                } else {
                    String ipAddress = new GetIpAddr().getIpAddr(request); // ????????????IP??????
                    // ???????????????
                    return fluxSensorBindService.addFluxSensorBind(form, ipAddress);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * @param id
     * @return ModelAndView
     * @throws BusinessException
     * @throws IOException
     * @throws @author           wangying
     * @Title: ??????
     */
    @ApiOperation(value = "????????????id????????????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/edit_{id}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean editPage(@PathVariable("id") @ApiParam("??????????????????id") final String id,
        HttpServletResponse response) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            // ?????????????????????
            List<FuelVehicle> vehicleList = fluxSensorBindService.findReferenceVehicle();
            // ?????????????????????
            List<FluxSensor> fluxSensorList = fluxSensorService.findFluxSensorByPage(null, false);
            // ????????????id??????????????????????????????
            FuelVehicle sensor = fluxSensorBindService.findFuelVehicleById(id);
            if (sensor != null) {
                mav.addObject("fluxSensorList", JSON.toJSONString(fluxSensorList));
                mav.addObject("vehicleList", JSON.toJSONString(vehicleList));
                mav.addObject("result", sensor);
                return new JsonResultBean(mav.getModel());
            } else {
                response.setContentType("text/htmlcharset=utf-8");
                PrintWriter out = response.getWriter();
                out.println("<script language='javascript'>");
                out.println("$('#commonWin').modal('hide');");
                out.println("layer.msg('??????????????????????????????');");
                out.println("myTable.refresh();");
                out.println("</script>");
                return null;
            }
        } catch (Exception e) {
            log.error("????????????????????????????????????", e);
            return new JsonResultBean(sysErrorMsg);
        }
    }

    /**
     * @param form
     * @param bindingResult
     * @return JsonResultBean
     * @throws BusinessException
     * @throws IOException
     * @throws @author           wangying
     * @Title: ??????
     */
    @ApiOperation(value = "?????????????????????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "oilWearId", value = "???????????????id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "vehicleId", value = "??????id", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean edit(
        @Validated({ ValidGroupUpdate.class }) @ModelAttribute("form") final FluxSensorBindForm form,
        final BindingResult bindingResult) {
        try {
            if (form != null) {
                // ????????????
                if (bindingResult.hasErrors()) {
                    return new JsonResultBean(JsonResultBean.FAULT,
                        SpringBindingResultWrapper.warpErrors(bindingResult));
                } else {
                    if (fluxSensorBindService.findFuelVehicleById(form.getId()) == null) {
                        return new JsonResultBean(JsonResultBean.FAULT, dataRelieveBound);
                    }
                    String ipAddress = new GetIpAddr().getIpAddr(request); // ??????????????????
                    // ???????????????
                    return fluxSensorBindService.updateFluxSensorBind(form, ipAddress);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????id?????? ??????
     */
    @ApiOperation(value = "????????????id?????????????????????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id) {
        try {
            if (id != null && !"".equals(id)) {
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return fluxSensorBindService.deleteFluxSensorBind(id, ipAddress);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("???????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ????????????
     */
    @ApiOperation(value = "????????????ids???????????????????????????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "deltems", value = "????????????id??????String(???????????????)", required = true, paramType = "query",
        dataType = "string")
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore() {
        try {
            String items = request.getParameter("deltems");
            if (items != null && !"".equals(items)) {
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return fluxSensorBindService.deleteFluxSensorBind(items, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("???????????? ??????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @ApiOperation(value = "????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/detail_{id}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean detailPage(@PathVariable("id") @ApiParam("??????????????????id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(DETAIL_PAGE);
            // ?????????
            VehicleInfo vehicle = alarmSettingService.findPeopleOrVehicleOrThingById(id);
            FuelVehicle fuelVehicle = fluxSensorBindService.findFuelVehicleByVid(id);
            mav.addObject("vehicle", vehicle);
            mav.addObject("result", fuelVehicle);
            return new JsonResultBean(mav.getModel());
        } catch (Exception e) {
            log.error("????????????????????????????????????", e);
            return new JsonResultBean(sysErrorMsg);
        }
    }

    /**
     * ??????id????????????
     */
    @ApiOperation(value = "????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = { @ApiImplicitParam(name = "sendParam", value = "???????????????", required = true) })
    @RequestMapping(value = "/sendFuel", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean sendFuel(String sendParam) {
        try {
            if (sendParam != null && !"".equals(sendParam)) {
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return fluxSensorBindService.sendFuel(sendParam, ipAddress);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * @return ????????????
     * @throws BusinessException
     * @author Axh
     */
    @ApiOperation(value = "????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/upgrade_{id}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean upgradePage(@PathVariable("id") @ApiParam("??????????????????id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(UPGRADE_PAGE);
            FuelVehicle fuelVehicle = fluxSensorBindService.findFuelVehicleById(id);
            mav.addObject("result", fuelVehicle);
            return new JsonResultBean(mav.getModel());
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(sysErrorMsg);
        }
    }

    /**
     * ????????????
     * @author LiFudong
     */

    @ApiOperation(value = "????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = { @ApiImplicitParam(name = "vehicleId", value = "??????id", required = true) })
    @RequestMapping(value = { "/saveWirelessUpdate" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean save(
        @ModelAttribute("swaggerWirelessUpdateParam") SwaggerWirelessUpdateParam swaggerWirelessUpdateParam,
        String vehicleId, final BindingResult bindingResult) {
        WirelessUpdateParam wirelessParam = new WirelessUpdateParam();
        BeanUtils.copyProperties(swaggerWirelessUpdateParam, wirelessParam);
        try {
            // ????????????
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
            }
            Integer commandType = 131;
            String commandTypeStr = request.getParameter("commandType");
            if (commandTypeStr != null) {
                if (StringUtil.isNull(commandTypeStr)) {
                    return new JsonResultBean(JsonResultBean.FAULT, upErrorFluxsensorType);
                }
                commandType = Integer.parseInt(commandTypeStr);
                String ip = new GetIpAddr().getIpAddr(request);// ????????????ip
                return fluxSensorBindService.updateWirelessUpdate(wirelessParam, vehicleId, commandType, ip);
            }
            return new JsonResultBean(JsonResultBean.FAULT, upError);
        } catch (Exception e) {
            log.error("?????????????????????" + e.getMessage(), e);
            return new JsonResultBean(JsonResultBean.FAULT, upError);
        }
    }

    /**
     * ??????????????????
     * @return ????????????
     * @author LiFudong
     */
    @ApiOperation(value = "????????????id??????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/basicInfo_{id}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean basicInfo(@PathVariable("id") @ApiParam("??????????????????id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(BASICINFO_PAGE);
            // ????????????id??????????????????????????????
            FuelVehicle fuelVehicle = fluxSensorBindService.findFuelVehicleById(id);
            mav.addObject("result", fuelVehicle);
            return new JsonResultBean(mav.getModel());
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(sysErrorMsg);
        }
    }

    /**
     * ??????????????????
     * @return ????????????
     * @throws BusinessException
     * @author LiFudong
     */
    @ApiOperation(value = "????????????id??????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/general_{id}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean generalPage(@PathVariable("id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(GENERAL_PAGE);
            // ????????????id??????????????????????????????
            FuelVehicle fuelVehicle = fluxSensorBindService.findFuelVehicleById(id);
            mav.addObject("result", fuelVehicle);
            return new JsonResultBean(mav.getModel());
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(sysErrorMsg);
        }
    }

    /**
     * ??????????????????
     * @return ????????????
     * @author LiFudong
     */
    @ApiOperation(value = "??????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/newsletter_{id}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean newsletterPage(@PathVariable("id") @ApiParam("??????????????????id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(NEWSLETTER_PAGE);
            // ????????????id??????????????????????????????
            FuelVehicle fuelVehicle = fluxSensorBindService.findFuelVehicleById(id);
            mav.addObject("result", fuelVehicle);
            return new JsonResultBean(mav.getModel());
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(sysErrorMsg);
        }
    }

    /**
     * ??????????????????
     * @return ????????????
     * @author LiFudong
     */
    @ApiOperation(value = "??????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/parameters_{id}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean parametersPage(@PathVariable("id") @ApiParam("??????????????????id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(PARAMETERS_PAGE);
            // ????????????id??????????????????????????????
            FuelVehicle fuelVehicle = fluxSensorBindService.findFuelVehicleById(id);
            mav.addObject("result", fuelVehicle);
            return new JsonResultBean(mav.getModel());
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(sysErrorMsg);
        }
    }

    /**
     * ??????F3?????????????????????????????????
     * @param id
     * @return
     * @throws BusinessException
     */
    @ApiOperation(value = "??????F3?????????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "?????????????????????id", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/getSensorSetting", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getSensorSetting(String id) {
        try {
            FuelVehicle fuelVehicle = fluxSensorBindService.findFuelVehicleById(id);
            JSONObject msg = new JSONObject();
            msg.put("setting", fuelVehicle);
            if (fuelVehicle != null) {
                return new JsonResultBean(JsonResultBean.SUCCESS, msg.toJSONString());
            }
            return new JsonResultBean(JsonResultBean.FAULT, deviceInfoNull);
        } catch (Exception e) {
            log.error("??????F3?????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????F3???????????????
     * @param vid
     * @param commandType
     * @param sensorID
     * @return
     * @throws BusinessException
     */
    @ApiOperation(value = "??????F3???????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = { @ApiImplicitParam(name = "vid", value = "??????id", required = true),
        @ApiImplicitParam(name = "commandType", value = "????????????", required = true),
        @ApiImplicitParam(name = "sensorID", value = "?????????id", required = true) })
    @RequestMapping(value = "/getF3Param", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getF3Param(String vid, Integer commandType, Integer sensorID) {
        try {
            return f3OilVehicleSettingService.sendF3SensorParam(vid, Integer.toHexString(sensorID),
                Integer.toHexString(commandType));
        } catch (Exception e) {
            log.error("??????F3?????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????F3?????????????????????
     * @param vid
     * @param commandStr
     * @param sensorID
     * @return
     * @throws BusinessException
     */
    @ApiOperation(value = "??????F3?????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = { @ApiImplicitParam(name = "vid", value = "??????id", required = true),
        @ApiImplicitParam(name = "sensorID", value = "?????????id", required = true),
        @ApiImplicitParam(name = "commandStr", value = "?????????", required = true) })
    @RequestMapping(value = "/getF3PrivateParam", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getF3PrivateParam(String vid, Integer sensorID, String commandStr) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request); // ????????????IP??????
            return f3OilVehicleSettingService.sendF3SensorPrivateParam(vid, Integer.toHexString(sensorID), commandStr,
                ipAddress, "2");
        } catch (Exception e) {
            log.error("??????F3???????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ?????????????????????????????????
     * @param swaggerFuelVehicleForm
     * @return
     * @throws BusinessException
     */
    @ApiOperation(value = "?????????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/updateSensorSetting", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateSensorSetting(
        @ModelAttribute("swaggerFuelVehicleForm") SwaggerFuelVehicleForm swaggerFuelVehicleForm) {
        FuelVehicle setting = new FuelVehicle();
        BeanUtils.copyProperties(swaggerFuelVehicleForm, setting);
        try {
            String dealType = request.getParameter("deal_type");
            String ipAddress = new GetIpAddr().getIpAddr(request);
            return f3OilVehicleSettingService.updateFuelSetting(setting, dealType, ipAddress);
        } catch (Exception e) {
            log.error("???????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

}

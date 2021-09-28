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
 * <p>Title: 流量传感器绑定Controller</p> <p>Copyright: Copyright (c) 2016</p> <p>Company: ZhongWei</p> <p>team:
 * ZhongWeiTeam</p>
 * @version 1.0
 * @author: wangying
 * @date 2016年9月18日下午5:07:13
 */
@RestController
@RequestMapping("/api/v/oilmgt/fluxsensorbind")
@Api(tags = { "油耗车辆设置（即油耗管理）" }, description = "油耗车辆设置相关api")
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

    private static final String BASICINFO_PAGE = "vas/oilmgt/fluxsensorbind/basicInfo"; // 基本信息

    private static final String GENERAL_PAGE = "vas/oilmgt/fluxsensorbind/general"; // 常规参数

    private static final String NEWSLETTER_PAGE = "vas/oilmgt/fluxsensorbind/newsletter"; // 通讯参数

    private static final String PARAMETERS_PAGE = "vas/oilmgt/fluxsensorbind/parameters"; // 私有参数

    @ApiIgnore
    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() throws BusinessException {
        return LIST_PAGE;
    }

    @ApiOperation(value = "获取油耗车辆设置列表", authorizations = {
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
            log.error("分页查询分组（findFluxSensorBind）异常", e);
            return new PageGridBean(false);
        }
    }

    @ApiOperation(value = "根据车辆id查询油耗车辆绑定选项值", notes = "返回车辆实体，参考车辆集合，流量传感器集合", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/bind_{id}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean bindPage(@PathVariable("id") @ApiParam("油耗车辆设置id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(BIND_PAGE);
            // 查询车
            VehicleInfo vehicle = alarmSettingService.findPeopleOrVehicleOrThingById(id);
            // 查询已绑定的车
            List<FuelVehicle> vehicleList = fluxSensorBindService.findReferenceVehicle();
            // 查询流量传感器
            List<FluxSensor> fluxSensorList = fluxSensorService.findFluxSensorByPage(null, false);
            mav.addObject("vehicle", vehicle);
            mav.addObject("fluxSensorList", JSON.toJSONString(fluxSensorList));
            mav.addObject("vehicleList", JSON.toJSONString(vehicleList));
            return new JsonResultBean(mav.getModel());
        } catch (Exception e) {
            log.error("绑定界面弹出异常", e);
            return new JsonResultBean(sysErrorMsg);
        }
    }

    /**
     * @param form
     * @param bindingResult
     * @return JsonResultBean
     * @throws BusinessException
     * @throws @author           wangying
     * @Title: 绑定
     */
    @ApiOperation(value = "保存车辆与流量传感器的绑定关系", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "oilWearId", value = "流量传感器id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "vehicleId", value = "车辆id", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/bind.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean bind(
        @Validated({ ValidGroupUpdate.class }) @ModelAttribute("form") final FluxSensorBindForm form,
        final BindingResult bindingResult) {
        try {
            if (form != null) {
                // 数据校验
                if (bindingResult.hasErrors()) {
                    return new JsonResultBean(JsonResultBean.FAULT,
                        SpringBindingResultWrapper.warpErrors(bindingResult));
                } else {
                    String ipAddress = new GetIpAddr().getIpAddr(request); // 客户端的IP地址
                    // 新增绑定表
                    return fluxSensorBindService.addFluxSensorBind(form, ipAddress);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("设置油耗参数异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * @param id
     * @return ModelAndView
     * @throws BusinessException
     * @throws IOException
     * @throws @author           wangying
     * @Title: 修改
     */
    @ApiOperation(value = "根据车辆id查询车辆与流量的绑定详情", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/edit_{id}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean editPage(@PathVariable("id") @ApiParam("油耗车辆设置id") final String id,
        HttpServletResponse response) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            // 查询已绑定的车
            List<FuelVehicle> vehicleList = fluxSensorBindService.findReferenceVehicle();
            // 查询流量传感器
            List<FluxSensor> fluxSensorList = fluxSensorService.findFluxSensorByPage(null, false);
            // 根据车辆id查询车与传感器的绑定
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
                out.println("layer.msg('该条数据已解除绑定！');");
                out.println("myTable.refresh();");
                out.println("</script>");
                return null;
            }
        } catch (Exception e) {
            log.error("修改油耗参数弹出页面异常", e);
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
     * @Title: 修改
     */
    @ApiOperation(value = "修改车辆与流量传感器的绑定关系", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "oilWearId", value = "流量传感器id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "vehicleId", value = "车辆id", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean edit(
        @Validated({ ValidGroupUpdate.class }) @ModelAttribute("form") final FluxSensorBindForm form,
        final BindingResult bindingResult) {
        try {
            if (form != null) {
                // 数据校验
                if (bindingResult.hasErrors()) {
                    return new JsonResultBean(JsonResultBean.FAULT,
                        SpringBindingResultWrapper.warpErrors(bindingResult));
                } else {
                    if (fluxSensorBindService.findFuelVehicleById(form.getId()) == null) {
                        return new JsonResultBean(JsonResultBean.FAULT, dataRelieveBound);
                    }
                    String ipAddress = new GetIpAddr().getIpAddr(request); // 修改分组日志
                    // 新增绑定表
                    return fluxSensorBindService.updateFluxSensorBind(form, ipAddress);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("修改油耗参数异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 根据id删除 终端
     */
    @ApiOperation(value = "根据绑定id删除车辆与流量传感器的绑定关系", authorizations = {
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
            log.error("解绑流量传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 批量删除
     */
    @ApiOperation(value = "根据绑定ids批量删除车辆与流量传感器的绑定关系", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "deltems", value = "批量删除id集合String(用逗号隔开)", required = true, paramType = "query",
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
            log.error("油耗车辆 批量解绑异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @ApiOperation(value = "油耗车辆设置详情", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/detail_{id}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean detailPage(@PathVariable("id") @ApiParam("油耗车辆设置id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(DETAIL_PAGE);
            // 查询车
            VehicleInfo vehicle = alarmSettingService.findPeopleOrVehicleOrThingById(id);
            FuelVehicle fuelVehicle = fluxSensorBindService.findFuelVehicleByVid(id);
            mav.addObject("vehicle", vehicle);
            mav.addObject("result", fuelVehicle);
            return new JsonResultBean(mav.getModel());
        } catch (Exception e) {
            log.error("油耗配置详情界面弹出异常", e);
            return new JsonResultBean(sysErrorMsg);
        }
    }

    /**
     * 根据id下发围栏
     */
    @ApiOperation(value = "油耗车辆设置下发", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = { @ApiImplicitParam(name = "sendParam", value = "下发的参数", required = true) })
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
            log.error("下发油耗参数异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * @return 远程升级
     * @throws BusinessException
     * @author Axh
     */
    @ApiOperation(value = "远程升级", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/upgrade_{id}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean upgradePage(@PathVariable("id") @ApiParam("油耗车辆设置id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(UPGRADE_PAGE);
            FuelVehicle fuelVehicle = fluxSensorBindService.findFuelVehicleById(id);
            mav.addObject("result", fuelVehicle);
            return new JsonResultBean(mav.getModel());
        } catch (Exception e) {
            log.error("远程升级弹出页面异常", e);
            return new JsonResultBean(sysErrorMsg);
        }
    }

    /**
     * 远程升级
     * @author LiFudong
     */

    @ApiOperation(value = "远程升级", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = { @ApiImplicitParam(name = "vehicleId", value = "车辆id", required = true) })
    @RequestMapping(value = { "/saveWirelessUpdate" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean save(
        @ModelAttribute("swaggerWirelessUpdateParam") SwaggerWirelessUpdateParam swaggerWirelessUpdateParam,
        String vehicleId, final BindingResult bindingResult) {
        WirelessUpdateParam wirelessParam = new WirelessUpdateParam();
        BeanUtils.copyProperties(swaggerWirelessUpdateParam, wirelessParam);
        try {
            // 数据校验
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
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                return fluxSensorBindService.updateWirelessUpdate(wirelessParam, vehicleId, commandType, ip);
            }
            return new JsonResultBean(JsonResultBean.FAULT, upError);
        } catch (Exception e) {
            log.error("外设软件升级：" + e.getMessage(), e);
            return new JsonResultBean(JsonResultBean.FAULT, upError);
        }
    }

    /**
     * 读取基本参数
     * @return 基本参数
     * @author LiFudong
     */
    @ApiOperation(value = "根据终端id查询终端详情", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/basicInfo_{id}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean basicInfo(@PathVariable("id") @ApiParam("油耗车辆设置id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(BASICINFO_PAGE);
            // 根据车辆id查询车与传感器的绑定
            FuelVehicle fuelVehicle = fluxSensorBindService.findFuelVehicleById(id);
            mav.addObject("result", fuelVehicle);
            return new JsonResultBean(mav.getModel());
        } catch (Exception e) {
            log.error("基本信息弹出页面异常", e);
            return new JsonResultBean(sysErrorMsg);
        }
    }

    /**
     * 读取常规参数
     * @return 常规参数
     * @throws BusinessException
     * @author LiFudong
     */
    @ApiOperation(value = "根据终端id查询终端详情", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/general_{id}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean generalPage(@PathVariable("id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(GENERAL_PAGE);
            // 根据车辆id查询车与传感器的绑定
            FuelVehicle fuelVehicle = fluxSensorBindService.findFuelVehicleById(id);
            mav.addObject("result", fuelVehicle);
            return new JsonResultBean(mav.getModel());
        } catch (Exception e) {
            log.error("常规参数弹出页面异常", e);
            return new JsonResultBean(sysErrorMsg);
        }
    }

    /**
     * 读取通讯参数
     * @return 通讯参数
     * @author LiFudong
     */
    @ApiOperation(value = "读取通讯参数", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/newsletter_{id}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean newsletterPage(@PathVariable("id") @ApiParam("油耗车辆设置id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(NEWSLETTER_PAGE);
            // 根据车辆id查询车与传感器的绑定
            FuelVehicle fuelVehicle = fluxSensorBindService.findFuelVehicleById(id);
            mav.addObject("result", fuelVehicle);
            return new JsonResultBean(mav.getModel());
        } catch (Exception e) {
            log.error("通讯参数弹出页面异常", e);
            return new JsonResultBean(sysErrorMsg);
        }
    }

    /**
     * 读取私有参数
     * @return 私有参数
     * @author LiFudong
     */
    @ApiOperation(value = "读取私有参数", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/parameters_{id}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean parametersPage(@PathVariable("id") @ApiParam("油量车辆设置id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(PARAMETERS_PAGE);
            // 根据车辆id查询车与传感器的绑定
            FuelVehicle fuelVehicle = fluxSensorBindService.findFuelVehicleById(id);
            mav.addObject("result", fuelVehicle);
            return new JsonResultBean(mav.getModel());
        } catch (Exception e) {
            log.error("私有参数弹出页面异常", e);
            return new JsonResultBean(sysErrorMsg);
        }
    }

    /**
     * 获取F3传感器平台设置常规参数
     * @param id
     * @return
     * @throws BusinessException
     */
    @ApiOperation(value = "获取F3传感器平台设置常规参数", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "传感器平台设置id", required = true, paramType = "query",
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
            log.error("获取F3传感器数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 获取F3传感器数据
     * @param vid
     * @param commandType
     * @param sensorID
     * @return
     * @throws BusinessException
     */
    @ApiOperation(value = "获取F3传感器数据", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = { @ApiImplicitParam(name = "vid", value = "车辆id", required = true),
        @ApiImplicitParam(name = "commandType", value = "指令类型", required = true),
        @ApiImplicitParam(name = "sensorID", value = "传感器id", required = true) })
    @RequestMapping(value = "/getF3Param", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getF3Param(String vid, Integer commandType, Integer sensorID) {
        try {
            return f3OilVehicleSettingService.sendF3SensorParam(vid, Integer.toHexString(sensorID),
                Integer.toHexString(commandType));
        } catch (Exception e) {
            log.error("获取F3传感器数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 获取F3传感器私有数据
     * @param vid
     * @param commandStr
     * @param sensorID
     * @return
     * @throws BusinessException
     */
    @ApiOperation(value = "获取F3传感器私有数据", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = { @ApiImplicitParam(name = "vid", value = "车辆id", required = true),
        @ApiImplicitParam(name = "sensorID", value = "传感器id", required = true),
        @ApiImplicitParam(name = "commandStr", value = "指令串", required = true) })
    @RequestMapping(value = "/getF3PrivateParam", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getF3PrivateParam(String vid, Integer sensorID, String commandStr) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request); // 客户端的IP地址
            return f3OilVehicleSettingService.sendF3SensorPrivateParam(vid, Integer.toHexString(sensorID), commandStr,
                ipAddress, "2");
        } catch (Exception e) {
            log.error("获取F3传感器私有数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 传感器常规参数修正下发
     * @param swaggerFuelVehicleForm
     * @return
     * @throws BusinessException
     */
    @ApiOperation(value = "传感器常规参数修正下发", authorizations = {
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
            log.error("传感器常规参数修正下发异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

}

package com.zw.api2.controller.calibration;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.api2.swaggerEntity.SwaggerBindOilVehicleSetting;
import com.zw.api2.swaggerEntity.SwaggerDemOilVehicleSetting;
import com.zw.api2.swaggerEntity.SwaggerEditOilVehicleSetting;
import com.zw.api2.swaggerEntity.SwaggerOilVehicleSetting;
import com.zw.api2.swaggerEntity.SwaggerPageParamQuery;
import com.zw.api2.swaggerEntity.SwaggerSaveWirelessParam;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.RodSensor;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.param.WirelessUpdateParam;
import com.zw.platform.domain.vas.oilmassmgt.DoubleOilVehicleSetting;
import com.zw.platform.domain.vas.oilmassmgt.FuelTank;
import com.zw.platform.domain.vas.oilmassmgt.OilVehicleSetting;
import com.zw.platform.domain.vas.oilmassmgt.form.OilCalibrationForm;
import com.zw.platform.domain.vas.oilmassmgt.query.OilVehicleSettingQuery;
import com.zw.platform.service.alarm.AlarmSettingService;
import com.zw.platform.service.oilVehicleSetting.F3OilVehicleSettingService;
import com.zw.platform.service.oilmassmgt.FuelTankManageService;
import com.zw.platform.service.oilmassmgt.OilVehicleSettingService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.Converter;
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
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * ??????????????????Controller <p> Title: OilVehicleSettingController.java </p> <p> Copyright: Copyright (c) 2016 </p> <p> Company:
 * ZhongWei </p> <p> team: ZhongWeiTeam </p>
 * @version 1.0
 * @author: Liubangquan
 * @date 2016???10???24?????????4:26:13
 */
@Controller
@RequestMapping("api/v/oilmassmgt/oilvehiclesetting")
@Api(tags = { "??????????????????_dev" }, description = "????????????????????????api??????")
public class ApiOilVehicleSettingController {
    private static Logger log = LogManager.getLogger(ApiOilVehicleSettingController.class);

    private static DecimalFormat decimalFormat = new DecimalFormat("#.#"); // ??????????????????

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Value("${vehicle.bound.oilsensor}")
    private String vehicleBoundOilsensor;

    @Value("${data.relieve.bound}")
    private String dataRelieveBound;

    @Value("${set.relieve.bound}")
    private String setRelieveBound;

    @Value("${terminal.off.line}")
    private String terminalOffLine;

    @Value("${device.info.null}")
    private String deviceInfoNull;

    @Value("${up.error}")
    private String upError;

    @Value("${up.error.oil.type}")
    private String upErrorOilType;

    @Autowired
    private OilVehicleSettingService oilVehicleSettingService;

    @Autowired
    private F3OilVehicleSettingService f3OilVehicleSettingService;

    @Autowired
    private FuelTankManageService fuelTankManageService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private AlarmSettingService alarmSettingService;

    private static final String EDIT_PAGE = "vas/oilmassmgt/oilvehiclesetting/edit";

    private static final String DETAIL_PAGE = "vas/oilmassmgt/oilvehiclesetting/detail";

    private static final String BASICINFO_PAGE = "vas/oilmassmgt/oilvehiclesetting/basicInfo"; // ????????????

    private static final String GENERAL_PAGE = "vas/oilmassmgt/oilvehiclesetting/general"; // ????????????

    private static final String NEWSLETTER_PAGE = "vas/oilmassmgt/oilvehiclesetting/newsletter"; // ????????????

    private static final String CALIBRATION_PAGE = "vas/oilmassmgt/oilvehiclesetting/calibration"; // ????????????

    private static final String PARAMETERS_PAGE = "vas/oilmassmgt/oilvehiclesetting/parameters"; // ????????????

    private static final String UPGRADE_PAGE = "vas/oilmassmgt/oilvehiclesetting/upgrade"; // ????????????

    /**
     * ????????????
     */
    @ApiOperation(value = "????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/upgrade_{id}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean upgradePage(
        @ApiParam(name = "id", value = "????????????????????????id", required = true) @PathVariable("id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(UPGRADE_PAGE);
            OilVehicleSetting oilVehicleSetting = oilVehicleSettingService.findOilBoxVehicleByBindId(id);
            if (oilVehicleSetting != null) {
                oilVehicleSetting.setOilBoxType("4" + oilVehicleSetting.getOilBoxType());
                mav.addObject("result", oilVehicleSetting);
                return new JsonResultBean(mav.getModel());
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * @return ????????????
     * @author angbike
     */
    @ApiOperation(value = "????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/parameters_{id}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean parametersPage(
        @ApiParam(name = "id", value = "????????????????????????id", required = true) @PathVariable("id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(PARAMETERS_PAGE);
            OilVehicleSetting oilVehicleSetting = oilVehicleSettingService.findOilBoxVehicleByBindId(id);
            if (oilVehicleSetting != null) {
                oilVehicleSetting.setOilBoxType("4" + oilVehicleSetting.getOilBoxType());
                mav.addObject("result", oilVehicleSetting);
                return new JsonResultBean(mav.getModel());
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * @return ????????????
     * @author angbike
     */
    @ApiOperation(value = "????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/calibration_{id}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean calibrationPage(
        @ApiParam(name = "id", value = "????????????????????????id", required = true) @PathVariable("id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(CALIBRATION_PAGE);
            // ??????id???????????????????????????
            OilVehicleSetting oilSetting = oilVehicleSettingService.findOilBoxVehicleByBindId(id);
            if (null != oilSetting) {
                oilSetting.setOilBoxType("4" + oilSetting.getOilBoxType());
                VehicleInfo vehicle = alarmSettingService.findPeopleOrVehicleOrThingById(oilSetting.getVId());
                oilSetting.setBrand(vehicle.getBrand());
                mav.addObject("result", oilSetting);
                return new JsonResultBean(mav.getModel());
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * @return ????????????
     * @author angbike
     */
    @ApiOperation(value = "????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/newsletter_{id}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean newsletterPage(
        @ApiParam(name = "id", value = "????????????????????????id", required = true) @PathVariable("id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(NEWSLETTER_PAGE);
            // ??????id???????????????
            OilVehicleSetting oilVehicleSetting = oilVehicleSettingService.findOilBoxVehicleByBindId(id);
            if (oilVehicleSetting != null) {
                oilVehicleSetting.setOilBoxType("4" + oilVehicleSetting.getOilBoxType());
                mav.addObject("result", oilVehicleSetting);
                return new JsonResultBean(mav.getModel());
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * @return ????????????
     * @author angbike
     */
    @ApiOperation(value = "????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/general_{id}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean generalPage(
        @ApiParam(name = "id", value = "????????????????????????id", required = true) @PathVariable("id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(GENERAL_PAGE);
            // ??????id???????????????
            OilVehicleSetting oilVehicleSetting = oilVehicleSettingService.findOilBoxVehicleByBindId(id);
            if (oilVehicleSetting != null) {
                oilVehicleSetting.setOilBoxType("4" + oilVehicleSetting.getOilBoxType());
                mav.addObject("result", oilVehicleSetting);
                return new JsonResultBean(mav.getModel());
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * @return ????????????
     * @author angbike
     */
    @ApiOperation(value = "????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/basicInfo_{id}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean basicInfo(
        @ApiParam(name = "id", value = "????????????????????????id", required = true) @PathVariable("id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(BASICINFO_PAGE);
            // ??????id???????????????????????????
            OilVehicleSetting oilSetting = oilVehicleSettingService.findOilBoxVehicleByBindId(id);
            if (oilSetting != null) {
                oilSetting.setOilBoxType("4" + oilSetting.getOilBoxType());
                mav.addObject("result", oilSetting);
                return new JsonResultBean(mav.getModel());
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    @ApiOperation(value = "????????????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "simpleQueryParam", value = "???????????????,????????????20", paramType = "query", dataType = "string")
    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getListPage(@ModelAttribute("query") SwaggerPageParamQuery query) {
        try {
            final OilVehicleSettingQuery query1 = new OilVehicleSettingQuery();
            BeanUtils.copyProperties(query, query1);
            Page<OilVehicleSetting> result = oilVehicleSettingService.findOilVehicleList(query1);
            return new PageGridBean(result, true);
        } catch (Exception e) {
            log.error("?????????????????????findOilVehicleList?????????", e);
            return new PageGridBean(false);
        }

    }

    /**
     * ??????
     * @author wangying
     */
    @ApiOperation(value = "????????????id?????????????????????????????????", notes = "?????????????????????????????????????????????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/bind_{id}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean bindPage(
        @ApiParam(name = "id", value = "????????????????????????id", required = true) @PathVariable("id") final String id) {
        try {
            JSONObject data = new JSONObject();
            // ?????????
            VehicleInfo vehicle = alarmSettingService.findPeopleOrVehicleOrThingById(id);
            // ??????????????????
            List<DoubleOilVehicleSetting> vehicleList = oilVehicleSettingService.findReferenceVehicle();
            // ????????????
            List<FuelTank> fuelTankList = oilVehicleSettingService.findFuelTankList();
            // ????????????
            List<RodSensor> rodSensorList = oilVehicleSettingService.findRodSensorList();
            data.put("vehicle", vehicle);
            data.put("fuelTankList", JSON.toJSONString(fuelTankList));
            data.put("rodSensorList", JSON.toJSONString(rodSensorList));
            data.put("vehicleList", JSON.toJSONString(vehicleList));
            data.put("id", UUID.randomUUID().toString());
            data.put("id2", UUID.randomUUID().toString());
            return new JsonResultBean(data);
        } catch (Exception e) {
            log.error("?????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * ??????????????????
     * @author wangying
     */

    @ApiOperation(value = "??????????????????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "??????1???????????????id,??????uuid", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "oilBoxId", value = "??????1id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "vehicleId", value = "??????id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "sensorType", value = "??????1id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "id2", value = "??????2???????????????id(???????????????2??????,???????????????2??????)", paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "oilBoxId2", value = "??????2id(???????????????2??????,???????????????2??????)", paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "sensorType2", value = "??????2id(???????????????2??????,???????????????2??????)", paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "calibrationSets", value = "??????1????????????", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "automaticUploadTime", value = "??????1??????????????????(01:??????,02:10s,03:20s,04:30s)",
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "outputCorrectionCoefficientK", value = "??????1??????????????????K,????????????????????????[1,200]",
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "outputCorrectionCoefficientB", value = "??????1??????????????????B,????????????????????????[0,200]",
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "addOilTimeThreshold", value = "??????1???????????????????????????????????????1???120?????????", paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "addOilAmountThreshol", value = "??????1?????????????????????????????????1???60?????????", paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "seepOilTimeThreshold", value = "??????1???????????????????????????????????????1???120?????????", paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "seepOilAmountThreshol", value = "??????1?????????????????????????????????1???60?????????", paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "calibrationSets2", value = "??????2????????????(???????????????2??????,???????????????2??????)", paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "automaticUploadTime2", value = "??????2??????????????????(01:??????,02:10s,03:20s,04:30s)",
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "outputCorrectionCoefficientK2", value = "??????2??????????????????K,????????????????????????[1,200]",
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "outputCorrectionCoefficientB2", value = "??????2??????????????????B,????????????????????????[0,200]",
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "addOilTimeThreshold2", value = "??????2???????????????????????????,?????????1???120?????????", paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "addOilAmountThreshol2", value = "??????2?????????????????????????????????1???60?????????", paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "seepOilTimeThreshold2", value = "??????2???????????????????????????????????????1???120?????????", paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "seepOilAmountThreshol2", value = "??????2?????????????????????????????????1???60?????????", paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/bind.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean bind(
        @Validated({ ValidGroupUpdate.class }) @ModelAttribute("form") final SwaggerBindOilVehicleSetting bean,
        final BindingResult bindingResult) {
        try {
            DoubleOilVehicleSetting bean1 = new DoubleOilVehicleSetting();
            BeanUtils.copyProperties(bean, bean1);
            // ????????????
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
            } else {
                // ????????????id ??????????????????????????????????????????
                if (StringUtils.isNotBlank(
                    oilVehicleSettingService.findOilVehicleSettingByVid(bean1.getVehicleId()).getId())) {
                    return new JsonResultBean(JsonResultBean.FAULT, vehicleBoundOilsensor);
                }
                String ipAddress = new GetIpAddr().getIpAddr(request); // ??????????????????IP??????
                // ???????????????
                return oilVehicleSettingService.addFuelTankBind(bean1, ipAddress);
            }
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @ApiOperation(value = "????????????id??????????????????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/edit_{vId}_{type}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean editPage(@PathVariable("vId") final String vehicleId, @PathVariable("type") String type,
        HttpServletResponse response) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            // ?????????????????????
            List<DoubleOilVehicleSetting> vehicleList = oilVehicleSettingService.findReferenceVehicle();
            // ????????????
            List<FuelTank> fuelTankList = oilVehicleSettingService.findFuelTankList();
            // ????????????id??????????????????????????????
            DoubleOilVehicleSetting oilSetting = oilVehicleSettingService.findOilVehicleSettingByVid(vehicleId);
            // ????????????
            List<RodSensor> rodSensorList = oilVehicleSettingService.findRodSensorList();
            if (StringUtils.isBlank(oilSetting.getId2())) { // ?????????
                oilSetting.setNewId2(UUID.randomUUID().toString());
            }
            // ???????????????????????????id????????????????????????
            getOilCalibrationList(oilSetting);
            if (StringUtils.isNotBlank(oilSetting.getId())) {
                mav.addObject("fuelTankList", JSON.toJSONString(fuelTankList));
                mav.addObject("vehicleList", JSON.toJSONString(vehicleList));
                mav.addObject("rodSensorList", JSON.toJSONString(rodSensorList));
                mav.addObject("result", oilSetting);
                mav.addObject("oilBoxType", type);
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
            log.error("??????????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * ??????????????????
     * @author Liubangquan
     */
    private void getOilCalibrationList(DoubleOilVehicleSetting oilSetting) {
        try {
            if (StringUtils.isNotBlank(oilSetting.getOilBoxId())) { // ??????1????????????
                // ????????????????????????
                List<OilCalibrationForm> list = fuelTankManageService.getOilCalibrationList(oilSetting.getId());
                if (null != list && !list.isEmpty()) {
                    for (OilCalibrationForm ocf : list) {
                        ocf.setOilLevelHeight(decimalFormat.format(Converter.toDouble(ocf.getOilLevelHeight())));
                        ocf.setOilValue(decimalFormat.format(Converter.toDouble(ocf.getOilValue())));
                    }
                }
                if (null != list && !list.isEmpty()) {
                    StringBuilder oilLevelHeights = new StringBuilder();
                    StringBuilder oilValues = new StringBuilder();
                    for (OilCalibrationForm of : list) {
                        oilLevelHeights.append(of.getOilLevelHeight()).append(",");
                        oilValues.append(of.getOilValue()).append(",");
                    }
                    if (oilLevelHeights.length() > 0) {
                        oilLevelHeights = new StringBuilder(oilLevelHeights.substring(0, oilLevelHeights.length() - 1));
                    }
                    if (oilValues.length() > 0) {
                        oilValues = new StringBuilder(oilValues.substring(0, oilValues.length() - 1));
                    }
                    oilSetting.setOilLevelHeights(oilLevelHeights.toString());
                    oilSetting.setOilValues(oilValues.toString());
                }
            }
            if (StringUtils.isNotBlank(oilSetting.getOilBoxId2())) { // ??????2????????????
                // ????????????????????????
                List<OilCalibrationForm> list = fuelTankManageService.getOilCalibrationList(oilSetting.getId2());
                if (null != list && !list.isEmpty()) {
                    for (OilCalibrationForm ocf : list) {
                        ocf.setOilLevelHeight(decimalFormat.format(Converter.toDouble(ocf.getOilLevelHeight())));
                        ocf.setOilValue(decimalFormat.format(Converter.toDouble(ocf.getOilValue())));
                    }
                }
                if (null != list && !list.isEmpty()) {
                    StringBuilder oilLevelHeights = new StringBuilder();
                    StringBuilder oilValues = new StringBuilder();
                    for (OilCalibrationForm of : list) {
                        oilLevelHeights.append(of.getOilLevelHeight()).append(",");
                        oilValues.append(of.getOilValue()).append(",");
                    }
                    if (oilLevelHeights.length() > 0) {
                        oilLevelHeights = new StringBuilder(oilLevelHeights.substring(0, oilLevelHeights.length() - 1));
                    }
                    if (oilValues.length() > 0) {
                        oilValues = new StringBuilder(oilValues.substring(0, oilValues.length() - 1));
                    }
                    oilSetting.setOilLevelHeights2(oilLevelHeights.toString());
                    oilSetting.setOilValues2(oilValues.toString());
                }
            }
        } catch (Exception e) {
            log.error("????????????????????????", e);
        }

    }

    /**
     * ????????????????????????
     * @author wangying
     */
    @ApiOperation(value = "???????????????????????????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/edit.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean edit(
        @Validated({ ValidGroupUpdate.class }) @ModelAttribute("form") final SwaggerEditOilVehicleSetting bean,
        final BindingResult bindingResult) {
        try {
            DoubleOilVehicleSetting bean1 = new DoubleOilVehicleSetting();
            BeanUtils.copyProperties(bean, bean1);
            // ????????????
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
            } else {
                if (oilVehicleSettingService.findOilBoxVehicleByBindId(bean1.getId()) == null) {
                    return new JsonResultBean(JsonResultBean.FAULT, dataRelieveBound);
                }
                String ipAddress = new GetIpAddr().getIpAddr(request);// ????????????ip
                // ????????????
                return oilVehicleSettingService.updateOilVehicleSetting(bean1, ipAddress);
            }
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????id????????????
     */
    @ApiOperation(value = "????????????id??????????????????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(
        @ApiParam(name = "id", value = "????????????????????????id", required = true) @PathVariable("id") final String id) {
        try {
            if (!"".equals(id)) {
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return oilVehicleSettingService.deleteFuelTankBindById(id, ipAddress);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ????????????
     */
    @ApiOperation(value = "????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ApiImplicitParam(name = "deltems", value = "????????????????????????ids,???????????????", required = true, paramType = "query",
        dataType = "string")
    @ResponseBody
    public JsonResultBean deleteMore() {
        try {
            String items = request.getParameter("deltems");
            if (!"".equals(items)) {
                String ipAddress = new GetIpAddr().getIpAddr(request); // ????????????ip
                return oilVehicleSettingService.deleteFuelTankBindById(items, ipAddress);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }

        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @ApiOperation(value = "????????????id??????????????????????????????????????????", notes = "????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/detail_{id}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean detailPage(
        @ApiParam(name = "id", value = "????????????????????????id", required = true) @PathVariable("id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(DETAIL_PAGE);
            // ????????????id???????????????????????????
            DoubleOilVehicleSetting oilSetting = oilVehicleSettingService.findOilVehicleSettingByVid(id);
            if (null != oilSetting) {
                VehicleInfo vehicle = alarmSettingService.findPeopleOrVehicleOrThingById(id);
                oilSetting.setBrand(vehicle.getBrand());
            }
            // ???????????????????????????id????????????????????????
            getOilCalibrationList(oilSetting);
            mav.addObject("result", oilSetting);
            return new JsonResultBean(mav.getModel());
        } catch (Exception e) {
            log.error("??????????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * ??????F3???????????????
     */
    @ApiOperation(value = "??????F3???????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "vid", value = "??????id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "commandType", value = "????????????", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "sensorID", value = "??????Id", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/getF3Param", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getF3Param(String vid, Integer commandType, Integer sensorID) {
        try {
            if (vid != null && !vid.isEmpty() && commandType != null && sensorID != null) {
                String ipAddress = new GetIpAddr().getIpAddr(request);// ????????????ip
                return f3OilVehicleSettingService.sendF3SensorParam(vid, Integer.toHexString(sensorID),
                    Integer.toHexString(commandType));
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("??????F3?????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????F3?????????????????????
     */
    @ApiOperation(value = "??????F3?????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "vid", value = "??????id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "commandStr", value = "????????????", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "sensorID", value = "??????Id", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/getF3PrivateParam", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getF3PrivateParam(String vid, Integer sensorID, String commandStr) {
        try {
            if (!"".equals(vid) && !"".equals(commandStr) && sensorID != 0) {
                String ip = new GetIpAddr().getIpAddr(request);// ????????????ip
                return f3OilVehicleSettingService.sendF3SensorPrivateParam(vid, Integer.toHexString(sensorID),
                    commandStr, ip, "1");
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("??????F3???????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ????????????????????????
     */
    @ApiOperation(value = "????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "??????id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "sensorID", value = "??????Id", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/getOilCalibrationList", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getOilCalibrationList(String id, Integer sensorID) {
        try {
            DoubleOilVehicleSetting oilSetting = oilVehicleSettingService.findOilVehicleSettingByVid(id);
            // ???????????????????????????id????????????????????????
            getOilCalibrationList(oilSetting);
            JSONObject msg = new JSONObject();
            List<Map<String, String>> list = new ArrayList<>();
            if (sensorID == 65) {
                for (int i = 0; i < oilSetting.getOilLevelHeights().split(",").length; i++) {
                    Map<String, String> map = new HashMap<>();
                    map.put("oilLevelHeight", String.valueOf(oilSetting.getOilLevelHeights().split(",")[i]));
                    map.put("oilValue", String.valueOf(oilSetting.getOilValues().split(",")[i]));
                    list.add(map);
                }
            } else {
                if (oilSetting.getOilValues2() != null && oilSetting.getOilValues2().split(",").length > 1) {
                    list = new ArrayList<>();
                    for (int i = 0; i < oilSetting.getOilLevelHeights2().split(",").length; i++) {
                        Map<String, String> map = new HashMap<>();
                        map.put("oilLevelHeight", String.valueOf(oilSetting.getOilLevelHeights2().split(",")[i]));
                        map.put("oilValue", String.valueOf(oilSetting.getOilValues2().split(",")[i]));
                        list.add(map);
                    }
                }
            }

            msg.put("settingList", list);
            msg.put("setting", oilSetting);
            return new JsonResultBean(JsonResultBean.SUCCESS, msg.toJSONString());
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }

    }

    /**
     * ???????????????????????????
     */
    @ApiOperation(value = "???????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "????????????????????????id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "queryType", value = "????????????????????????4", defaultValue = "4", paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/getSensorSetting", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getSensorSetting(String id, String queryType) {
        try {
            OilVehicleSetting oilVehicleSetting = oilVehicleSettingService.findOilBoxVehicleByBindId(id);
            JSONObject msg = new JSONObject();
            msg.put("setting", oilVehicleSetting);
            if (oilVehicleSetting != null) {
                return new JsonResultBean(JsonResultBean.SUCCESS, msg.toJSONString());
            }
            return new JsonResultBean(JsonResultBean.FAULT, deviceInfoNull);
        } catch (Exception e) {
            log.error("??????F3?????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????????????????
     * @author FanLu
     */
    @ApiOperation(value = "??????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "vehicleId", value = "??????id", required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = { "/saveWirelessUpdate" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean save(@ModelAttribute("wirelessParam") SwaggerSaveWirelessParam wirelessParam,
        String vehicleId, final BindingResult bindingResult) {

        WirelessUpdateParam wirelessParam1 = new WirelessUpdateParam();
        BeanUtils.copyProperties(wirelessParam, wirelessParam1);
        // ????????????
        if (bindingResult.hasErrors()) {
            return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
        }
        try {
            Integer commandType = 13141;
            try {
                String commandTypeStr = request.getParameter("commandType");
                if (StringUtil.isNull(commandTypeStr)) {
                    return new JsonResultBean(JsonResultBean.FAULT, upErrorOilType);
                }
                commandType = Integer.parseInt(commandTypeStr);
            } catch (Exception ex) {
                return new JsonResultBean(JsonResultBean.FAULT, upErrorOilType);

            }
            String ipAddress = new GetIpAddr().getIpAddr(request);// ????????????ip
            return f3OilVehicleSettingService.updateWirelessUpdate(wirelessParam1, vehicleId, commandType, ipAddress,
                0);
        } catch (Exception e) {
            log.error("?????????????????????" + e.getMessage(), e);
            return new JsonResultBean(JsonResultBean.FAULT, upError);
        }
    }

    /**
     * ?????????????????????????????????
     */
    @ApiOperation(value = "?????????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "deal_type", value = "????????????????????????id", required = true, paramType = "query",
        dataType = "string")
    @RequestMapping(value = "/updateSensorSetting", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateSensorSetting(
        @Validated({ ValidGroupUpdate.class }) @ModelAttribute("setting") SwaggerOilVehicleSetting setting) {
        OilVehicleSetting setting1 = new OilVehicleSetting();
        BeanUtils.copyProperties(setting, setting1);
        try {
            String dealType = request.getParameter("deal_type");
            if (!"".equals(dealType)) {
                String ipAddress = new GetIpAddr().getIpAddr(request); // ????????????IP??????
                return f3OilVehicleSettingService.updateRoutineSetting(setting1, dealType, ipAddress);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("???????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ?????????????????????????????????
     */
    @ApiOperation(value = "?????????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "deal_type", value = "????????????????????????id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "oilLevelHeight", value = "?????????????????????????????????", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "oilValue", value = "??????????????????????????????", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/updateDemarcateSetting", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateDemarcateSetting(@ModelAttribute("setting") SwaggerDemOilVehicleSetting setting) {
        try {
            OilVehicleSetting setting1 = new OilVehicleSetting();
            BeanUtils.copyProperties(setting, setting1);
            String[] oilLevelHeight = request.getParameterValues("oilLevelHeight");
            String[] oilValue = request.getParameterValues("oilValue");
            String type = request.getParameter("deal_type");
            List<OilCalibrationForm> list = new ArrayList<>();
            if (type.equals("report")) { // ???????????????
                for (int i = 0; i < oilLevelHeight.length; i++) {
                    OilCalibrationForm form = new OilCalibrationForm();
                    form.setOilBoxVehicleId(setting1.getId());
                    form.setOilLevelHeight(oilLevelHeight[i]);
                    form.setOilValue(oilValue[i]);
                    form.setCreateDataUsername(SystemHelper.getCurrentUsername());
                    list.add(form);
                }
            }
            String ip = new GetIpAddr().getIpAddr(request);// ????????????ip
            return f3OilVehicleSettingService.updateDemarcateSetting(list, setting1, ip);
        } catch (Exception e) {
            log.error("???????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????????????????(??????????????????)
     */
    @ApiOperation(value = "????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "sendParam",
        value = "????????????json???????????????[" + "{'oilVehicleId':'??????????????????????????????id','vehicleId':'??????id',"
            + "'settingParamId':'??????????????????id','calibrationParamId':'????????????id',"
            + "'transmissionParamId':'??????????????????id'},{}...]  "
            + "??????[{'oilVehicleId':'66263370-c772-4b99-8fc2-a63e0c36a875',"
            + "'settingParamId':'','calibrationParamId':'','transmissionParamId':"
            + "'','vehicleId':'b233ed24-722c-4b00-836a-f90ab6d7235f'}]", required = true, paramType = "query",
        dataType = "string")
    @RequestMapping(value = "/sendOil", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean sendOil(String sendParam) {
        try {
            ArrayList<JSONObject> paramList = (ArrayList<JSONObject>) JSON.parseArray(sendParam, JSONObject.class);
            if (paramList != null && !paramList.isEmpty()) {
                String ip = new GetIpAddr().getIpAddr(request);// ????????????ip
                oilVehicleSettingService.sendOil(paramList, ip);
                return new JsonResultBean(JsonResultBean.SUCCESS);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ????????????Id?????????????????????????????????
     * @param vehicleId ?????????????????????id
     */
    @ApiOperation(value = "????????????Id?????????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "vehicleId", value = "??????id", required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = "/getBindInfo", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean referenceBrandSet(String vehicleId) {
        try {
            if (!vehicleId.isEmpty()) {
                JSONObject msg = new JSONObject();
                // ????????????id???????????????????????????
                DoubleOilVehicleSetting oilSetting = oilVehicleSettingService.findOilVehicleSettingByVid(vehicleId);
                msg.put("oilSetting", oilSetting);
                return new JsonResultBean(msg);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("????????????Id???????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }

    }

}

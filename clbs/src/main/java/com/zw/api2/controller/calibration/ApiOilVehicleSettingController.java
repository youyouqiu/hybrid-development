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
 * 油量车辆设置Controller <p> Title: OilVehicleSettingController.java </p> <p> Copyright: Copyright (c) 2016 </p> <p> Company:
 * ZhongWei </p> <p> team: ZhongWeiTeam </p>
 * @version 1.0
 * @author: Liubangquan
 * @date 2016年10月24日下午4:26:13
 */
@Controller
@RequestMapping("api/v/oilmassmgt/oilvehiclesetting")
@Api(tags = { "油量车辆设置_dev" }, description = "油量车辆设置相关api接口")
public class ApiOilVehicleSettingController {
    private static Logger log = LogManager.getLogger(ApiOilVehicleSettingController.class);

    private static DecimalFormat decimalFormat = new DecimalFormat("#.#"); // 保留一位小数

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

    private static final String BASICINFO_PAGE = "vas/oilmassmgt/oilvehiclesetting/basicInfo"; // 基本信息

    private static final String GENERAL_PAGE = "vas/oilmassmgt/oilvehiclesetting/general"; // 常规参数

    private static final String NEWSLETTER_PAGE = "vas/oilmassmgt/oilvehiclesetting/newsletter"; // 通讯参数

    private static final String CALIBRATION_PAGE = "vas/oilmassmgt/oilvehiclesetting/calibration"; // 标定数据

    private static final String PARAMETERS_PAGE = "vas/oilmassmgt/oilvehiclesetting/parameters"; // 私有参数

    private static final String UPGRADE_PAGE = "vas/oilmassmgt/oilvehiclesetting/upgrade"; // 远程升级

    /**
     * 远程升级
     */
    @ApiOperation(value = "远程升级", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/upgrade_{id}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean upgradePage(
        @ApiParam(name = "id", value = "车辆与油箱的绑定id", required = true) @PathVariable("id") final String id) {
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
            log.error("远程升级弹出页面异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * @return 私有参数
     * @author angbike
     */
    @ApiOperation(value = "私有参数", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/parameters_{id}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean parametersPage(
        @ApiParam(name = "id", value = "车辆与油箱的绑定id", required = true) @PathVariable("id") final String id) {
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
            log.error("私有参数弹出页面异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * @return 标定数据
     * @author angbike
     */
    @ApiOperation(value = "标定数据", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/calibration_{id}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean calibrationPage(
        @ApiParam(name = "id", value = "车辆与油箱的绑定id", required = true) @PathVariable("id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(CALIBRATION_PAGE);
            // 根据id查询车与油箱的绑定
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
            log.error("标定数据弹出页面异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * @return 通讯参数
     * @author angbike
     */
    @ApiOperation(value = "通讯参数", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/newsletter_{id}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean newsletterPage(
        @ApiParam(name = "id", value = "车辆与油箱的绑定id", required = true) @PathVariable("id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(NEWSLETTER_PAGE);
            // 根据id查询车绑定
            OilVehicleSetting oilVehicleSetting = oilVehicleSettingService.findOilBoxVehicleByBindId(id);
            if (oilVehicleSetting != null) {
                oilVehicleSetting.setOilBoxType("4" + oilVehicleSetting.getOilBoxType());
                mav.addObject("result", oilVehicleSetting);
                return new JsonResultBean(mav.getModel());
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("通讯参数弹出页面异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * @return 常规参数
     * @author angbike
     */
    @ApiOperation(value = "常规参数", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/general_{id}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean generalPage(
        @ApiParam(name = "id", value = "车辆与油箱的绑定id", required = true) @PathVariable("id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(GENERAL_PAGE);
            // 根据id查询车绑定
            OilVehicleSetting oilVehicleSetting = oilVehicleSettingService.findOilBoxVehicleByBindId(id);
            if (oilVehicleSetting != null) {
                oilVehicleSetting.setOilBoxType("4" + oilVehicleSetting.getOilBoxType());
                mav.addObject("result", oilVehicleSetting);
                return new JsonResultBean(mav.getModel());
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("常规参数弹出页面异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * @return 基本信息
     * @author angbike
     */
    @ApiOperation(value = "基本信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/basicInfo_{id}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean basicInfo(
        @ApiParam(name = "id", value = "车辆与油箱的绑定id", required = true) @PathVariable("id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(BASICINFO_PAGE);
            // 根据id查询车与油箱的绑定
            OilVehicleSetting oilSetting = oilVehicleSettingService.findOilBoxVehicleByBindId(id);
            if (oilSetting != null) {
                oilSetting.setOilBoxType("4" + oilSetting.getOilBoxType());
                mav.addObject("result", oilSetting);
                return new JsonResultBean(mav.getModel());
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("基本信息弹出页面异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    @ApiOperation(value = "分页查询油量车辆设置列表", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "simpleQueryParam", value = "模糊搜索值,长度小于20", paramType = "query", dataType = "string")
    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getListPage(@ModelAttribute("query") SwaggerPageParamQuery query) {
        try {
            final OilVehicleSettingQuery query1 = new OilVehicleSettingQuery();
            BeanUtils.copyProperties(query, query1);
            Page<OilVehicleSetting> result = oilVehicleSettingService.findOilVehicleList(query1);
            return new PageGridBean(result, true);
        } catch (Exception e) {
            log.error("分页查询分组（findOilVehicleList）异常", e);
            return new PageGridBean(false);
        }

    }

    /**
     * 设置
     * @author wangying
     */
    @ApiOperation(value = "根据车辆id查询油量车辆绑定选项值", notes = "返回车辆实体，参考车辆集合，邮箱集合，油杆集合", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/bind_{id}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean bindPage(
        @ApiParam(name = "id", value = "车辆与油箱的绑定id", required = true) @PathVariable("id") final String id) {
        try {
            JSONObject data = new JSONObject();
            // 查询车
            VehicleInfo vehicle = alarmSettingService.findPeopleOrVehicleOrThingById(id);
            // 查询参考车辆
            List<DoubleOilVehicleSetting> vehicleList = oilVehicleSettingService.findReferenceVehicle();
            // 查询油箱
            List<FuelTank> fuelTankList = oilVehicleSettingService.findFuelTankList();
            // 查询油杆
            List<RodSensor> rodSensorList = oilVehicleSettingService.findRodSensorList();
            data.put("vehicle", vehicle);
            data.put("fuelTankList", JSON.toJSONString(fuelTankList));
            data.put("rodSensorList", JSON.toJSONString(rodSensorList));
            data.put("vehicleList", JSON.toJSONString(vehicleList));
            data.put("id", UUID.randomUUID().toString());
            data.put("id2", UUID.randomUUID().toString());
            return new JsonResultBean(data);
        } catch (Exception e) {
            log.error("绑定传感器弹出页面异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 油箱车辆绑定
     * @author wangying
     */

    @ApiOperation(value = "保存车辆与邮箱油杆的绑定关系", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "邮箱1与车辆绑定id,生成uuid", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "oilBoxId", value = "邮箱1id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "vehicleId", value = "车辆id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "sensorType", value = "油杆1id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "id2", value = "邮箱2与车辆绑定id(未绑定邮箱2不填,若绑定邮箱2必填)", paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "oilBoxId2", value = "邮箱2id(未绑定邮箱2不填,若绑定邮箱2必填)", paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "sensorType2", value = "油杆2id(未绑定邮箱2不填,若绑定邮箱2必填)", paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "calibrationSets", value = "油箱1标定组数", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "automaticUploadTime", value = "油箱1自动上传时间(01:被动,02:10s,03:20s,04:30s)",
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "outputCorrectionCoefficientK", value = "油箱1输出修正系数K,必须为整数，范围[1,200]",
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "outputCorrectionCoefficientB", value = "油箱1输出修正系数B,必须为整数，范围[0,200]",
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "addOilTimeThreshold", value = "油箱1加油时间阈值（秒），必须是1到120的整数", paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "addOilAmountThreshol", value = "油箱1加油量时间阈值，必须为1到60的整数", paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "seepOilTimeThreshold", value = "油箱1漏油时间阈值（秒），必须是1到120的整数", paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "seepOilAmountThreshol", value = "油箱1漏油量时间阈值，必须为1到60的整数", paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "calibrationSets2", value = "邮箱2标定组数(未绑定邮箱2不填,若绑定邮箱2必填)", paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "automaticUploadTime2", value = "油箱2自动上传时间(01:被动,02:10s,03:20s,04:30s)",
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "outputCorrectionCoefficientK2", value = "油箱2输出修正系数K,必须为整数，范围[1,200]",
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "outputCorrectionCoefficientB2", value = "油箱2输出修正系数B,必须为整数，范围[0,200]",
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "addOilTimeThreshold2", value = "油箱2加油时间阈值（秒）,必须是1到120的整数", paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "addOilAmountThreshol2", value = "油箱2加油量时间阈值，必须为1到60的整数", paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "seepOilTimeThreshold2", value = "油箱2漏油时间阈值（秒），必须是1到120的整数", paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "seepOilAmountThreshol2", value = "油箱2漏油量时间阈值，必须为1到60的整数", paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/bind.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean bind(
        @Validated({ ValidGroupUpdate.class }) @ModelAttribute("form") final SwaggerBindOilVehicleSetting bean,
        final BindingResult bindingResult) {
        try {
            DoubleOilVehicleSetting bean1 = new DoubleOilVehicleSetting();
            BeanUtils.copyProperties(bean, bean1);
            // 数据校验
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
            } else {
                // 根据车辆id 删除绑定关系（避免同时操作）
                if (StringUtils.isNotBlank(
                    oilVehicleSettingService.findOilVehicleSettingByVid(bean1.getVehicleId()).getId())) {
                    return new JsonResultBean(JsonResultBean.FAULT, vehicleBoundOilsensor);
                }
                String ipAddress = new GetIpAddr().getIpAddr(request); // 获取客户端的IP地址
                // 新增绑定表
                return oilVehicleSettingService.addFuelTankBind(bean1, ipAddress);
            }
        } catch (Exception e) {
            log.error("油箱车辆绑定异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @ApiOperation(value = "根据车辆id查询车辆与邮箱油杆的绑定详情", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/edit_{vId}_{type}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean editPage(@PathVariable("vId") final String vehicleId, @PathVariable("type") String type,
        HttpServletResponse response) {
        try {
            ModelAndView mav = new ModelAndView(EDIT_PAGE);
            // 查询已绑定的车
            List<DoubleOilVehicleSetting> vehicleList = oilVehicleSettingService.findReferenceVehicle();
            // 查询油箱
            List<FuelTank> fuelTankList = oilVehicleSettingService.findFuelTankList();
            // 根据车辆id查询车与传感器的绑定
            DoubleOilVehicleSetting oilSetting = oilVehicleSettingService.findOilVehicleSettingByVid(vehicleId);
            // 查询油杆
            List<RodSensor> rodSensorList = oilVehicleSettingService.findRodSensorList();
            if (StringUtils.isBlank(oilSetting.getId2())) { // 双油箱
                oilSetting.setNewId2(UUID.randomUUID().toString());
            }
            // 根据油量车辆设置表id读取油箱标定数据
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
                out.println("layer.msg('该条数据已解除绑定！');");
                out.println("myTable.refresh();");
                out.println("</script>");
                return null;
            }
        } catch (Exception e) {
            log.error("修改油量车辆设置弹出页面异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 查询标定数据
     * @author Liubangquan
     */
    private void getOilCalibrationList(DoubleOilVehicleSetting oilSetting) {
        try {
            if (StringUtils.isNotBlank(oilSetting.getOilBoxId())) { // 油箱1标定数据
                // 读取油箱标定数据
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
            if (StringUtils.isNotBlank(oilSetting.getOilBoxId2())) { // 油箱2标定数据
                // 读取油箱标定数据
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
            log.error("查询标定数据异常", e);
        }

    }

    /**
     * 修改油箱车辆设置
     * @author wangying
     */
    @ApiOperation(value = "保存修改的车辆与邮箱油杆的绑定详情", authorizations = {
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
            // 数据校验
            if (bindingResult.hasErrors()) {
                return new JsonResultBean(JsonResultBean.FAULT, SpringBindingResultWrapper.warpErrors(bindingResult));
            } else {
                if (oilVehicleSettingService.findOilBoxVehicleByBindId(bean1.getId()) == null) {
                    return new JsonResultBean(JsonResultBean.FAULT, dataRelieveBound);
                }
                String ipAddress = new GetIpAddr().getIpAddr(request);// 获得访问ip
                // 修改绑定
                return oilVehicleSettingService.updateOilVehicleSetting(bean1, ipAddress);
            }
        } catch (Exception e) {
            log.error("修改油箱车辆设置异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 根据id删除绑定
     */
    @ApiOperation(value = "根据绑定id删除车辆与邮箱油杆的绑定关系", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(
        @ApiParam(name = "id", value = "车辆与油箱的绑定id", required = true) @PathVariable("id") final String id) {
        try {
            if (!"".equals(id)) {
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return oilVehicleSettingService.deleteFuelTankBindById(id, ipAddress);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("删除绑定设置异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 批量删除
     */
    @ApiOperation(value = "批量删除", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ApiImplicitParam(name = "deltems", value = "车辆与油箱的绑定ids,以逗号分割", required = true, paramType = "query",
        dataType = "string")
    @ResponseBody
    public JsonResultBean deleteMore() {
        try {
            String items = request.getParameter("deltems");
            if (!"".equals(items)) {
                String ipAddress = new GetIpAddr().getIpAddr(request); // 获得访问ip
                return oilVehicleSettingService.deleteFuelTankBindById(items, ipAddress);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }

        } catch (Exception e) {
            log.error("批量删除绑定设置异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @ApiOperation(value = "根据绑定id查询车辆与邮箱油杆的绑定详情", notes = "用于详情", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = { "/detail_{id}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean detailPage(
        @ApiParam(name = "id", value = "车辆与油箱的绑定id", required = true) @PathVariable("id") final String id) {
        try {
            ModelAndView mav = new ModelAndView(DETAIL_PAGE);
            // 根据车辆id查询车与油箱的绑定
            DoubleOilVehicleSetting oilSetting = oilVehicleSettingService.findOilVehicleSettingByVid(id);
            if (null != oilSetting) {
                VehicleInfo vehicle = alarmSettingService.findPeopleOrVehicleOrThingById(id);
                oilSetting.setBrand(vehicle.getBrand());
            }
            // 根据油量车辆设置表id读取油箱标定数据
            getOilCalibrationList(oilSetting);
            mav.addObject("result", oilSetting);
            return new JsonResultBean(mav.getModel());
        } catch (Exception e) {
            log.error("删除油量车辆设置弹出页面异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 获取F3传感器数据
     */
    @ApiOperation(value = "获取F3传感器数据", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "vid", value = "车辆id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "commandType", value = "参数类型", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "sensorID", value = "外设Id", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/getF3Param", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getF3Param(String vid, Integer commandType, Integer sensorID) {
        try {
            if (vid != null && !vid.isEmpty() && commandType != null && sensorID != null) {
                String ipAddress = new GetIpAddr().getIpAddr(request);// 获得访问ip
                return f3OilVehicleSettingService.sendF3SensorParam(vid, Integer.toHexString(sensorID),
                    Integer.toHexString(commandType));
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("获取F3传感器数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 获取F3传感器私有数据
     */
    @ApiOperation(value = "获取F3传感器私有数据", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "vid", value = "车辆id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "commandStr", value = "参数类型", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "sensorID", value = "外设Id", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/getF3PrivateParam", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getF3PrivateParam(String vid, Integer sensorID, String commandStr) {
        try {
            if (!"".equals(vid) && !"".equals(commandStr) && sensorID != 0) {
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                return f3OilVehicleSettingService.sendF3SensorPrivateParam(vid, Integer.toHexString(sensorID),
                    commandStr, ip, "1");
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("获取F3传感器私有数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 获取邮箱标定数据
     */
    @ApiOperation(value = "获取邮箱标定数据", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "车辆id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "sensorID", value = "外设Id", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/getOilCalibrationList", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getOilCalibrationList(String id, Integer sensorID) {
        try {
            DoubleOilVehicleSetting oilSetting = oilVehicleSettingService.findOilVehicleSettingByVid(id);
            // 根据油量车辆设置表id读取油箱标定数据
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
            log.error("获取邮箱标定数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }

    }

    /**
     * 获取传感器常规参数
     */
    @ApiOperation(value = "获取传感器常规参数", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "车辆与油箱的绑定id", required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "queryType", value = "参数类型，默认值4", defaultValue = "4", paramType = "query",
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
            log.error("获取F3传感器数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 外设软件升级
     * @author FanLu
     */
    @ApiOperation(value = "外设软件升级", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "vehicleId", value = "车辆id", required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = { "/saveWirelessUpdate" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean save(@ModelAttribute("wirelessParam") SwaggerSaveWirelessParam wirelessParam,
        String vehicleId, final BindingResult bindingResult) {

        WirelessUpdateParam wirelessParam1 = new WirelessUpdateParam();
        BeanUtils.copyProperties(wirelessParam, wirelessParam1);
        // 数据校验
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
            String ipAddress = new GetIpAddr().getIpAddr(request);// 获得访问ip
            return f3OilVehicleSettingService.updateWirelessUpdate(wirelessParam1, vehicleId, commandType, ipAddress,
                0);
        } catch (Exception e) {
            log.error("外设软件升级：" + e.getMessage(), e);
            return new JsonResultBean(JsonResultBean.FAULT, upError);
        }
    }

    /**
     * 传感器常规参数修正下发
     */
    @ApiOperation(value = "传感器常规参数修正下发", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "deal_type", value = "车辆与油箱的绑定id", required = true, paramType = "query",
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
                String ipAddress = new GetIpAddr().getIpAddr(request); // 客户端的IP地址
                return f3OilVehicleSettingService.updateRoutineSetting(setting1, dealType, ipAddress);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("传感器常规参数修正下发异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 传感器标定参数修正下发
     */
    @ApiOperation(value = "传感器标定参数修正下发", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "deal_type", value = "车辆与油箱的绑定id", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "oilLevelHeight", value = "液位高度，多个数组格式", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "oilValue", value = "油量值，多个数组格式", required = true, paramType = "query",
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
            if (type.equals("report")) { // 以上报为准
                for (int i = 0; i < oilLevelHeight.length; i++) {
                    OilCalibrationForm form = new OilCalibrationForm();
                    form.setOilBoxVehicleId(setting1.getId());
                    form.setOilLevelHeight(oilLevelHeight[i]);
                    form.setOilValue(oilValue[i]);
                    form.setCreateDataUsername(SystemHelper.getCurrentUsername());
                    list.add(form);
                }
            }
            String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
            return f3OilVehicleSettingService.updateDemarcateSetting(list, setting1, ip);
        } catch (Exception e) {
            log.error("传感器标定参数修正下发异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 下发油箱参数(包括批量下发)
     */
    @ApiOperation(value = "油量车辆设置下发", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "sendParam",
        value = "下发参数json串，格式：[" + "{'oilVehicleId':'车辆与邮箱油杆的绑定id','vehicleId':'车辆id',"
            + "'settingParamId':'邮箱设置下发id','calibrationParamId':'标定下发id',"
            + "'transmissionParamId':'通讯参数下发id'},{}...]  "
            + "例：[{'oilVehicleId':'66263370-c772-4b99-8fc2-a63e0c36a875',"
            + "'settingParamId':'','calibrationParamId':'','transmissionParamId':"
            + "'','vehicleId':'b233ed24-722c-4b00-836a-f90ab6d7235f'}]", required = true, paramType = "query",
        dataType = "string")
    @RequestMapping(value = "/sendOil", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean sendOil(String sendParam) {
        try {
            ArrayList<JSONObject> paramList = (ArrayList<JSONObject>) JSON.parseArray(sendParam, JSONObject.class);
            if (paramList != null && !paramList.isEmpty()) {
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                oilVehicleSettingService.sendOil(paramList, ip);
                return new JsonResultBean(JsonResultBean.SUCCESS);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("下发油箱参数异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 根据车辆Id查询车与油箱的绑定信息
     * @param vehicleId 设置的监控对象id
     */
    @ApiOperation(value = "根据车辆Id查询车与油箱的绑定信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "vehicleId", value = "车辆id", required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = "/getBindInfo", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean referenceBrandSet(String vehicleId) {
        try {
            if (!vehicleId.isEmpty()) {
                JSONObject msg = new JSONObject();
                // 根据车辆id查询车与油箱的绑定
                DoubleOilVehicleSetting oilSetting = oilVehicleSettingService.findOilVehicleSettingByVid(vehicleId);
                msg.put("oilSetting", oilSetting);
                return new JsonResultBean(msg);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("根据车辆Id查询车与油箱的绑定信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }

    }

}

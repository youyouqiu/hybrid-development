package com.zw.api2.controller.calibration;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zw.platform.commons.Auth;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.vas.oilmassmgt.DoubleOilVehicleSetting;
import com.zw.platform.domain.vas.oilmassmgt.OilVehicleSetting;
import com.zw.platform.domain.vas.oilmassmgt.form.LastOilCalibrationForm;
import com.zw.platform.domain.vas.oilmassmgt.form.OilCalibrationForm;
import com.zw.platform.service.oilmassmgt.OilCalibrationService;
import com.zw.platform.service.oilmassmgt.OilVehicleSettingService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MonitorUtils;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * 油量标定Controller <p>Title: OilCalibrationController.java</p> <p>Copyright: Copyright (c) 2016</p> <p>Company:
 * ZhongWei</p> <p>team: ZhongWeiTeam</p>
 *
 * @version 1.0
 * @author: Liubangquan
 * @date 2016年12月13日下午1:55:31
 */
@Controller
@RequestMapping("api/v/oilmassmgt/oilcalibration")
@Api(tags = {"油量标定_dev"}, description = "油量标定相关api接口")
public class ApiOilCalibrationController {
    private static Logger log = LogManager.getLogger(ApiOilCalibrationController.class);

    @Autowired
    private OilCalibrationService oilCalibrationService;

    @Autowired
    private OilVehicleSettingService oilVehicleSettingService;

    @Autowired
    private LogSearchService logSearchService;

    @Resource
    private HttpServletRequest request;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    /**
     * 进入油箱管理列表页面
     */
    @ApiOperation(value = "获得进入油箱管理列表页面数据", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @Auth
    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean listPage() {
        try {
            JSONObject data = new JSONObject();
            List<OilVehicleSetting> vehicleList = oilCalibrationService.getVehicleList();
            String vehicleListJsonStr = JSON.toJSONString(vehicleList);
            data.put("vehicleList", JSON.parseArray(vehicleListJsonStr));
            return new JsonResultBean(data);
        } catch (Exception e) {
            log.error("进入油量标定页面异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 根据车辆id获取油箱标定数据
     */
    @ApiOperation(value = "根据车辆id获取油箱标定数据", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "vehicleId", value = "车辆id",
            required = true, paramType = "query", dataType = "string")})
    @RequestMapping(value = "/getOilCalibration", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getOilCalibration(String vehicleId) {
        try {
            JSONObject msg = new JSONObject();
            Map<String, List<OilCalibrationForm>> map = oilCalibrationService.getOilCalibrationByVid(vehicleId);
            if (null != map && map.size() > 0) {
                msg.put("oilCalibration1", map.get("1")); // 油箱1标定数据
                msg.put("oilCalibration2", map.get("2")); // 油箱2标定数据
            } else {
                msg.put("oilCalibration1", null); // 油箱1标定数据
                msg.put("oilCalibration2", null); // 油箱2标定数据
            }
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("获取油箱标定数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 油量标定：获取最新一次的油量数据
     */
    @ApiOperation(value = "油量标定：获取最新一次的油量数据", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "vehicleId", value = "车辆id",
            required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "curBox", value = "当前油箱",
            required = false, paramType = "query", dataType = "string")})
    @RequestMapping(value = "/getLatestOilData", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getLatestOilData(String vehicleId, String curBox) {
        try {
            JSONObject msg = new JSONObject();
            // 查询车辆位置信息，同时返回下发指令的msgSN，用于对接设备返回的信息
            String msgSN = oilCalibrationService.getLatestPositional(vehicleId);
            msg.put("msgSN", msgSN);
            String type = request.getParameter("type");
            if (type == null || type.equals("")) {
                String[] vehicle = logSearchService.findCarMsg(vehicleId);
                if (vehicle != null) {
                    String brand = vehicle[0];
                    String plateColor = vehicle[1];
                    String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                    String logMsg = "监控对象：" + brand + " 车辆点名";
                    logSearchService.addLog(ip, logMsg, "3", "MONITORING", brand, plateColor);
                }
            }
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("获取最新一次油量数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 保存修正标定数据
     */
    @ApiOperation(value = "修正并下发标定数据", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "vehicleId", value = "车辆id", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "oilBoxVehicleIds", value = "油量车辆设置表id（油箱1）",
            required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "oilBoxVehicleIds2", value = "油量车辆设置表id（油箱2，如果有油箱2，必填）",
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "oilLevelHeights", value = "油箱1液位高度集合（中间用逗号分隔）",
            required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "oilLevelHeights2", value = "油箱2液位高度集合（中间用逗号分隔，如果有油箱2，必填）",
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "oilValues", value = "油箱1油量值集合（中间用逗号分隔）",
            required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "oilValues2", value = "油箱2油量值集合（中间用逗号分隔，如果有油箱2，必填）",
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "settingParamId", value = "下发油箱id（之前下发过才有此id）",
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "calibrationParamId", value = "下发标定id（之前下发过才有此id）",
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "transmissionParamId", value = "下发通讯参数设置id（之前下发过才有此id）",
            paramType = "query", dataType = "string")})
    @RequestMapping(value = "/saveOilCalibration", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean saveOilCalibration(String vehicleId, String oilBoxVehicleIds, String oilBoxVehicleIds2,
        String oilLevelHeights, String oilLevelHeights2, String oilValues,
        String oilValues2, String settingParamId, String calibrationParamId,
        String transmissionParamId) {
        try {
            boolean flag = false;
            if (StringUtils.isNotBlank(oilBoxVehicleIds) && StringUtils.isNotBlank(oilLevelHeights)
                && StringUtils.isNotBlank(oilValues)) {
                flag = oilCalibrationService.updateOilCalibration(vehicleId, oilBoxVehicleIds, oilBoxVehicleIds2,
                    oilLevelHeights, oilLevelHeights2, oilValues, oilValues2);
            }
            JSONObject msg = new JSONObject();
            String msgSN = "";
            if (flag) {
                // 标定重新下发
                ArrayList<JSONObject> paramList = new ArrayList<>();
                if (!Converter.toBlank(oilBoxVehicleIds).equals("")) { // 油箱1
                    JSONObject oilBox = new JSONObject();
                    oilBox.put("oilVehicleId", oilBoxVehicleIds);
                    oilBox.put("vehicleId", vehicleId);
                    oilBox.put("reCalibrationFlag", "1");
                    if (!Converter.toBlank(settingParamId).equals("")) {
                        oilBox.put("settingParamId", settingParamId);
                    }
                    if (!Converter.toBlank(calibrationParamId).equals("")) {
                        oilBox.put("calibrationParamId", calibrationParamId);
                    }
                    if (!Converter.toBlank(transmissionParamId).equals("")) {
                        oilBox.put("transmissionParamId", transmissionParamId);
                    }
                    paramList.add(oilBox);
                }
                if (!Converter.toBlank(oilBoxVehicleIds2).equals("")) { // 油箱2
                    JSONObject oilBox2 = new JSONObject();
                    oilBox2.put("oilVehicleId", oilBoxVehicleIds2);
                    oilBox2.put("vehicleId", vehicleId);
                    oilBox2.put("reCalibrationFlag", "2");
                    if (!Converter.toBlank(settingParamId).equals("")) {
                        oilBox2.put("settingParamId", settingParamId);
                    }
                    if (!Converter.toBlank(calibrationParamId).equals("")) {
                        oilBox2.put("calibrationParamId", calibrationParamId);
                    }
                    if (!Converter.toBlank(transmissionParamId).equals("")) {
                        oilBox2.put("transmissionParamId", transmissionParamId);
                    }
                    paramList.add(oilBox2);
                }
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                msgSN = oilVehicleSettingService.sendOil(paramList, ip);
                msg.put("msgSN", msgSN);
            } else {
                msg.put("msg", "标定数据修正失败");
            }
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("保存修正标定数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 保存车辆最后一次标定的时间
     */
    @ApiOperation(value = "保存车辆最后一次标定的时间", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "vehicleId", value = "车辆id", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "oilBoxVehicleIds", value = "油量车辆设置表id（油箱1）",
            required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "oilBoxVehicleIds2", value = "油量车辆设置表id（油箱2，如果有油箱2，必填）", paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "tank1Last", value = "油箱1最后一次标定时间（如果油箱1标定过）", paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "tank2Last", value = "油箱2最后一次标定时间（如果油箱2标定过）", paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/saveLastOilCalibration", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean saveLastOilCalibration(String vehicleId, String oilBoxVehicleIds, String oilBoxVehicleIds2,
        String tank1Last, String tank2Last) {
        try {
            if (!Converter.toBlank(oilBoxVehicleIds).equals("") || !Converter.toBlank(oilBoxVehicleIds2).equals("")) {
                oilCalibrationService.deleteLastCalibration(vehicleId);
            }
            if (!Converter.toBlank(oilBoxVehicleIds).equals("")) { // 油箱1
                LastOilCalibrationForm form = new LastOilCalibrationForm();
                form.setVehicleId(vehicleId);
                form.setOilBoxType("1");
                form.setLastCalibrationTime(tank1Last);
                form.setCreateDataUsername(SystemHelper.getCurrentUsername());
                oilCalibrationService.saveLastCalibration(form);
            }
            if (!Converter.toBlank(oilBoxVehicleIds2).equals("")) { // 油箱1
                LastOilCalibrationForm form = new LastOilCalibrationForm();
                form.setVehicleId(vehicleId);
                form.setOilBoxType("2");
                form.setLastCalibrationTime(tank2Last);
                form.setCreateDataUsername(SystemHelper.getCurrentUsername());
                oilCalibrationService.saveLastCalibration(form);
            }
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("保存车辆最后一次标定的时间异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 获取车辆最后一次标定的时间
     */
    @ApiOperation(value = "获取车辆最后一次标定的时间", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "vehicleId", value = "车辆id", required = true,
            paramType = "query", dataType = "string")})
    @RequestMapping(value = "/getLastCalibration", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getLastCalibration(String vehicleId) {
        try {
            JSONObject msg = new JSONObject();
            msg.put("tank1Time", "");
            msg.put("tank2Time", "");
            List<LastOilCalibrationForm> list = oilCalibrationService.getLastCalibration(vehicleId);
            if (null != list && !list.isEmpty()) {
                for (LastOilCalibrationForm form : list) {
                    if (Converter.toBlank(form.getOilBoxType()).equals("1")) { // 油箱1
                        msg.put("tank1Time", form.getLastCalibrationTime());
                    } else if (Converter.toBlank(form.getOilBoxType()).equals("2")) { // 油箱2
                        msg.put("tank2Time", form.getLastCalibrationTime());
                    }
                }
            }
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("获取车辆最后一次标定的时间异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 获取标定状态
     */
    @ApiOperation(value = "获取标定状态", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "vehicleId", value = "车辆id", required = true,
            paramType = "query", dataType = "string")})
    @RequestMapping(value = "/checkCalibrationStatus", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean checkCalibrationStatus(String vehicleId) {
        try {
            if (vehicleId != null) {
                JSONObject msg = new JSONObject();
                String calibrationStatus = oilCalibrationService.getCalibrationStatusByVid(vehicleId);
                msg.put("updateDataTime", "");
                if (Converter.toBlank(calibrationStatus).equals("0")) {
                    // 标定状态为空闲状态，可以对其进行标定，同时重置标定状态为占用状态，以防其他用户可以对当前车辆进行标定操作
                    oilCalibrationService.updateCalibrationStatusByVid(vehicleId, "1");
                } else {
                    // 标定状态为占用状态，获取其更新时间，用于页面判断并还原异常标定状态的数据
                    String updateDataTime = oilCalibrationService.getCalibrationUpdateTimeByVid(vehicleId);
                    if (!Converter.toBlank(updateDataTime).equals("")) {
                        msg.put("updateDataTime",
                            Converter.toString(Converter.toDate(updateDataTime), "yyyy-MM-dd HH:mm:ss"));
                    }
                }
                msg.put("calibrationStatus", calibrationStatus);
                return new JsonResultBean(msg);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("获取标定状态异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 更新车辆标定状态
     */
    @ApiOperation(value = "更新车辆标定状态", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "vehicleId", value = "车辆id", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "calibrationStatus", value = "标定状态标识（0-空闲状态，可以标定；1-占用状态，不能标定）",
            required = true, paramType = "query", dataType = "string")})
    @RequestMapping(value = "/updateCalibrationStatus", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateCalibrationStatus(String vehicleId, String calibrationStatus) {
        try {
            if (vehicleId != null && !"".equals(vehicleId) && calibrationStatus != null
                && !"".equals(calibrationStatus)) {
                boolean success = oilCalibrationService.updateCalibrationStatusByVid(vehicleId, calibrationStatus);
                if (success) {
                    return new JsonResultBean(JsonResultBean.SUCCESS);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("更新车辆标定状态异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 判断车辆是否绑定油箱或者传感器
     */
    @ApiOperation(value = "判断车辆是否绑定油箱或者传感器", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "vehicleId", value = "车辆id", required = true,
            paramType = "query", dataType = "string")})
    @RequestMapping(value = "/checkIsBondOilBox", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean checkIsBondOilBox(String vehicleId) {
        try {
            boolean isBond = oilCalibrationService.findIsBondOilBox(vehicleId);
            if (isBond) {
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("判断车辆绑定油箱或者传感器异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 校验车辆的在线状态
     */
    @ApiOperation(value = "校验车辆的在线状态", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "vehicleId", value = "车辆id", required = true,
            paramType = "query", dataType = "string")})
    @RequestMapping(value = "/checkVehicleOnlineStatus", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean checkVehicleOnlineStatus(String vehicleId) {
        try {
            return new JsonResultBean(MonitorUtils.isOnLine(vehicleId));
        } catch (Exception e) {
            log.error("校验车辆的在线状态异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 根据监控对象id查询监控对象与油箱绑定的信息
     */
    @ApiOperation(value = "根据监控对象id查询监控对象与油箱绑定的信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "vehicleId", value = "车辆id", required = true,
            paramType = "query", dataType = "string")})
    @RequestMapping(value = "/checkBoxBondInfo", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean checkIsBondOilBoxInfo(String vehicleId) {
        try {
            if (!"".equals(vehicleId)) {
                JSONObject msg = new JSONObject();
                // 根据车辆id查询车与油箱的绑定信息
                DoubleOilVehicleSetting oilSetting = oilVehicleSettingService.findOilVehicleSettingByVid(vehicleId);
                if (oilSetting != null) {
                    msg.put("oilSetting", oilSetting);
                    return new JsonResultBean(msg);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("根据车辆id查询监控对象与油箱绑定信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }

    }

    /**
     * 下发修正标定数据
     */
    @ApiOperation(value = "下发修正标定数据", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "vehicleId", value = "车辆id", required = true,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "oilBoxVehicleIds", value = "油量车辆设置表id（油箱1）",
            required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "oilBoxVehicleIds2", value = "油量车辆设置表id（油箱2，如果有油箱2，必填）",
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "settingParamId", value = "下发油箱id（之前下发过才有此id）",
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "calibrationParamId", value = "下发标定id（之前下发过才有此id）",
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "transmissionParamId", value = "下发通讯参数设置id（之前下发过才有此id）",
            paramType = "query", dataType = "string")})
    @RequestMapping(value = "/sendOilCalibration", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean sendOilCalibration(String vehicleId, String oilBoxVehicleIds, String oilBoxVehicleIds2,
        String settingParamId, String calibrationParamId,
        String transmissionParamId) {
        try {
            JSONObject msg = new JSONObject();
            // 标定重新下发
            ArrayList<JSONObject> paramList = new ArrayList<>();
            if (!Converter.toBlank(oilBoxVehicleIds).equals("")) { // 油箱1
                JSONObject oilBox = new JSONObject();
                oilBox.put("oilVehicleId", oilBoxVehicleIds);
                oilBox.put("vehicleId", vehicleId);
                oilBox.put("reCalibrationFlag", "1");
                if (!Converter.toBlank(settingParamId).equals("")) {
                    oilBox.put("settingParamId", settingParamId);
                }
                if (!Converter.toBlank(calibrationParamId).equals("")) {
                    oilBox.put("calibrationParamId", calibrationParamId);
                }
                if (!Converter.toBlank(transmissionParamId).equals("")) {
                    oilBox.put("transmissionParamId", transmissionParamId);
                }
                paramList.add(oilBox);
            }
            if (!Converter.toBlank(oilBoxVehicleIds2).equals("")) { // 油箱2
                JSONObject oilBox2 = new JSONObject();
                oilBox2.put("oilVehicleId", oilBoxVehicleIds2);
                oilBox2.put("vehicleId", vehicleId);
                oilBox2.put("reCalibrationFlag", "2");
                if (!Converter.toBlank(settingParamId).equals("")) {
                    oilBox2.put("settingParamId", settingParamId);
                }
                if (!Converter.toBlank(calibrationParamId).equals("")) {
                    oilBox2.put("calibrationParamId", calibrationParamId);
                }
                if (!Converter.toBlank(transmissionParamId).equals("")) {
                    oilBox2.put("transmissionParamId", transmissionParamId);
                }
                paramList.add(oilBox2);
            }
            String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
            String msgSN = oilVehicleSettingService.sendOil(paramList, ip);
            msg.put("msgSN", msgSN);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("下发修正油量标定数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }

    }

}

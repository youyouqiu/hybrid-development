package com.zw.api.controller.vas;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zw.platform.commons.Auth;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.vas.oilmassmgt.OilVehicleSetting;
import com.zw.platform.domain.vas.oilmassmgt.form.LastOilCalibrationForm;
import com.zw.platform.domain.vas.oilmassmgt.form.OilCalibrationForm;
import com.zw.platform.service.oilmassmgt.OilCalibrationService;
import com.zw.platform.service.oilmassmgt.OilVehicleSettingService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.BusinessException;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * 油量标定Controller <p>Title: OilCalibrationController.java</p> <p>Copyright: Copyright (c) 2016</p> <p>Company:
 * ZhongWei</p> <p>team: ZhongWeiTeam</p>
 * @author: Liubangquan
 * @date 2016年12月13日下午1:55:31
 * @version 1.0
 */
@RestController
@RequestMapping("/swagger/v/oilmassmgt/oilcalibration")
@Api(tags = {"油量标定"}, description = "油量标定相关api接口")
public class SwaggerOilCalibrationController {
    private static Logger log = LogManager.getLogger(SwaggerOilCalibrationController.class);

    @Autowired
    private OilCalibrationService oilCalibrationService;

    @Autowired
    private OilVehicleSettingService oilVehicleSettingService;

    @Autowired
    private HttpServletRequest request;

    /**
     * 油量标定界面初始化
     * @Title: listPage
     * @return
     * @throws BusinessException
     * @return ModelAndView
     * @throws @author
     *             Liubangquan
     */
    @ApiOperation(value = "油量标定界面初始化-车辆下拉列表", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @Auth
    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    public JsonResultBean listPage() {
        try {
            List<OilVehicleSetting> vehicleList = oilCalibrationService.getVehicleList();
            String vehicleListJsonStr = JSON.toJSONString(vehicleList);
            return new JsonResultBean(JSON.parseArray(vehicleListJsonStr));
        } catch (Exception e) {
            log.error("油量标定界面初始化-车辆下拉列表异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 根据车辆id获取油箱标定数据
     * @Title: getOilCalibration
     * @return
     * @throws BusinessException
     * @return JsonResultBean
     * @throws @author
     *             Liubangquan
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
            log.error("根据车辆id获取油箱标定数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 油量标定：获取最新一次的油量数据
     * @Title: getLatestOilData
     * @param vehicleId
     * @return
     * @throws BusinessException
     * @return JsonResultBean
     * @throws @author
     *             Liubangquan
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
            // RedisHelper.setString(msgSN, ,PublicVariable.REDIS_ELSE_DATABASE);
            msg.put("msgSN", msgSN);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("获取最新一次的油量数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 保存修正标定数据
     * @Title: saveOilCalibration
     * @param vehicleId
     * @param oilLevelHeights
     * @param oilValues
     * @return
     * @throws BusinessException
     * @return JsonResultBean
     * @throws @author
     *             Liubangquan
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
			required = false, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "oilLevelHeights", value = "油箱1液位高度集合（中间用逗号分隔）",
			required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "oilLevelHeights2", value = "油箱2液位高度集合（中间用逗号分隔，如果有油箱2，必填）",
			required = false, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "oilValues", value = "油箱1油量值集合（中间用逗号分隔）",
			required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "oilValues2", value = "油箱2油量值集合（中间用逗号分隔，如果有油箱2，必填）",
			required = false, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "settingParamId", value = "下发油箱id（之前下发过才有此id）",
			required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "calibrationParamId", value = "下发标定id（之前下发过才有此id）",
			required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "transmissionParamId", value = "下发通讯参数设置id（之前下发过才有此id）",
			required = true, paramType = "query", dataType = "string")})
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
            flag = true;
            flag = true;
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
                msg.put("msgSN", msgSN); // msg.put("msg", "标定数据修正成功");
            } else {
                msg.put("msg", "标定数据修正失败");
            }
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("修正并下发标定数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 保存车辆最后一次标定的时间
     * @Title: saveLastOilCalibration
     * @param vehicleId
     * @param oilBoxVehicleIds
     * @param oilBoxVehicleIds2
     * @return
     * @throws BusinessException
     * @return JsonResultBean
     * @throws @author
     *             Liubangquan
     */
    @ApiOperation(value = "保存车辆最后一次标定的时间", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "vehicleId", value = "车辆id", required = true,
			paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "oilBoxVehicleIds", value = "油量车辆设置表id（油箱1）",
			required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "oilBoxVehicleIds2", value = "油量车辆设置表id（油箱2，如果有油箱2，必填）",
			required = false, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "tank1Last", value = "油箱1最后一次标定时间（如果油箱1标定过）",
			required = false, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "tank2Last", value = "油箱2最后一次标定时间（如果油箱2标定过）",
			required = false, paramType = "query", dataType = "string")})
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
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 获取车辆最后一次标定的时间
     * @Title: getLastCalibration
     * @param vehicleId
     * @return
     * @throws BusinessException
     * @return JsonResultBean
     * @throws @author
     *             Liubangquan
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
            if (null != list && list.size() > 0) {
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
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 获取标定状态
     * @Title: checkCalibrationStatus
     * @param vehicleId
     * @return
     * @throws BusinessException
     * @return JsonResultBean
     * @throws @author
     *             Liubangquan
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
        } catch (Exception e) {
            log.error("获取标定状态异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 更新车辆标定状态
     * @Title: updateCalibrationStatus
     * @param vehicleId
     * @param calibrationStatus
     * @return
     * @throws BusinessException
     * @return JsonResultBean
     * @throws @author
     *             Liubangquan
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
            boolean success = oilCalibrationService.updateCalibrationStatusByVid(vehicleId, calibrationStatus);
            if (success) {
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("更新车辆标定状态异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 判断车辆是否绑定油箱或者传感器
     * @Title: checkIsBondOilBox
     * @param vehicleId
     * @return
     * @throws BusinessException
     * @return JsonResultBean
     * @throws @author
     *             Liubangquan
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
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 校验车辆的在线状态
     * @Title: checkVehicleOnlineStatus
     * @param vehicleId
     * @return
     * @throws BusinessException
     * @return JsonResultBean
     * @throws @author
     *             Liubangquan
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
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

}

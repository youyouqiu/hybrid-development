package com.zw.platform.controller.alarm;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.vas.alram.AlarmLinkageDTO;
import com.zw.platform.domain.vas.alram.AlarmType;
import com.zw.platform.domain.vas.alram.form.AlarmParameterSettingForm;
import com.zw.platform.domain.vas.alram.query.AlarmSettingQuery;
import com.zw.platform.service.alarm.AlarmSettingService;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.VehicleUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Administrator
 */
@Controller
@RequestMapping("/a/alarmSetting")
@Api(tags = { "报警参数设置_dev" }, description = "报警参数设置相关api接口")
public class AlarmSettingController {
    private static Logger log = LogManager.getLogger(AlarmSettingController.class);

    @Autowired
    private AlarmSettingService alarmSettingService;

    @Autowired
    private HttpServletRequest request;

    private static final String LIST_PAGE = "vas/alarm/alarmSetting/list";

    private static final String EDIT_PAGE = "vas/alarm/alarmSetting/setting";

    private static final String EDIT_F3LS_PAGE = "vas/alarm/alarmSetting/settingF3Ls";

    private static final String EDIT_ASOLS_PAGE = "vas/alarm/alarmSetting/settingAsoLs";

    private static final String EDIT_BD_PAGE = "vas/alarm/alarmSetting/settingBd";

    private static final String LINKAGE_PAGE = "vas/alarm/alarmSetting/linkage";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    @ApiIgnore
    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    /**
     * 分页查询报警参数设置列表
     */
    @Auth
    @ApiOperation(value = "分页查询报警参数设置列表", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = { @ApiImplicitParam(name = "page", value = "页码", required = true),
        @ApiImplicitParam(name = "limit", value = "页容量", required = true),
        @ApiImplicitParam(name = "simpleQueryParam", value = "模糊搜索值,长度小于20"),
        @ApiImplicitParam(name = "groupId", value = "企业id"),
        @ApiImplicitParam(name = "assignmentId", value = "分组id") })
    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getListPage(final AlarmSettingQuery query) {
        try {
            if (query != null) {
                return new PageGridBean(alarmSettingService.findAlarmSetting(query), true);
            }
            return null;
        } catch (Exception e) {
            log.error("分页查询分组（findAlarmSetting）异常", e);
            return new PageGridBean(false);
        }
    }

    /**
     * 参考对象下拉列表
     */
    @RequestMapping(value = "/referentList/{deviceType}", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean getReferentList(@PathVariable("deviceType") String deviceType) {
        try {
            return new JsonResultBean(alarmSettingService.getReferentList(deviceType));
        } catch (Exception e) {
            log.error("获取参考对象下拉列表异常!", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 获取报警参数设置详情
     */
    @RequestMapping(value = "/getAlarmParameterSettingDetails/{moId}", method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean getAlarmParameterSettingDetails(@PathVariable("moId") String moId) {
        try {
            return new JsonResultBean(alarmSettingService.getAlarmParameterSettingDetails(moId));
        } catch (Exception e) {
            log.error("获取报警参数设置详情异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 保存报警参数的设置
     */
    @RequestMapping(value = "/saveAlarmParameterSetting", method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean saveAlarmParameterSetting(String moIds, String alarmParameterSettingJsonStr) {
        try {
            if (StringUtils.isBlank(moIds) || StringUtils.isBlank(alarmParameterSettingJsonStr)) {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
            return alarmSettingService.saveAlarmParameterSetting(moIds, alarmParameterSettingJsonStr);
        } catch (Exception e) {
            log.error("保存报警参数的设置异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 报警设置页面
     */
    @RequestMapping(value = { "/setting_{id}.gsp" }, method = RequestMethod.GET)
    @AvoidRepeatSubmitToken(setToken = true)
    public ModelAndView editPage(@PathVariable("id") final String id, String deviceType) {
        ModelAndView mav;
        try {
            switch (deviceType) {
                case "10":
                    mav = new ModelAndView(EDIT_F3LS_PAGE);
                    // mav = alarmSettingService.findF3Object(id, mav);
                    break;
                case "9":
                    mav = new ModelAndView(EDIT_ASOLS_PAGE);
                    // mav = alarmSettingService.findAsoObject(id, mav);
                    break;
                case "5":
                    mav = new ModelAndView(EDIT_BD_PAGE);
                    // mav = alarmSettingService.findBdObject(id, mav);
                    break;
                default:
                    mav = new ModelAndView(EDIT_PAGE);
                    // mav = alarmSettingService.find808Object(id, mav, deviceType);
                    break;
            }
            // 查询参考车牌下拉列表 新增了单独的查询接口
            // List<AlarmSetting> referVehicleList = alarmSettingService.findReferVehicleByDeviceType(deviceType);
            // 根据id查询车辆、人、物品信息
            VehicleInfo vehicle = alarmSettingService.findPeopleOrVehicleOrThingById(id);
            mav.addObject("vehicle", vehicle);
            // mav.addObject("referVehicleList", JSON.parseArray(JSON.toJSONString(referVehicleList)));
            return mav;
        } catch (Exception e) {
            log.error("报警设置弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 恢复默认
     * @param deviceType 协议类型
     */
    @RequestMapping(value = "/resetDefaultAlarm", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean resetDefaultAlarm(String deviceType) {
        try {
            if (StringUtils.isNotEmpty(deviceType)) {
                return alarmSettingService.resetDefaultAlarm(deviceType);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("报警参数设置恢复默认异常!", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }



    /**
     * 恢复默认（高精度）
     */
    @RequestMapping(value = "/resetDefaultHighPrecisionAlarm", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean resetDefaultHighPrecisionAlarm(String deviceType) {
        try {
            if (StringUtils.isNotEmpty(deviceType)) {
                return alarmSettingService.resetDefaultHighPrecisionAlarm(deviceType);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("报警参数设置恢复高精度默认异常!", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    @RequestMapping(value = "/getDeviceAlarmParam", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getDeviceAlarmParam(String monitorId, String deviceType) {
        try {
            if (StringUtils.isNotEmpty(monitorId)) {
                return alarmSettingService.sendDeviceAlarmParam(monitorId, deviceType);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("读取终端报警参数异常!", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 批量设置报警页面
     */
    @RequestMapping(value = { "/settingmore.gsp" }, method = RequestMethod.GET)
    @AvoidRepeatSubmitToken(setToken = true)
    public ModelAndView settingMorePage(String vehicleIds, String deviceType, HttpServletResponse response) {
        try {
            if (StringUtils.isBlank(vehicleIds)) {
                response.setContentType("text/htmlcharset=utf-8");
                PrintWriter out = response.getWriter();
                out.println("<script language='javascript'>");
                out.println("$('#commonWin').modal('hide');");
                out.println("layer.msg('请至少选择一条数据！');");
                out.println("</script>");
                return null;
            } else {
                Set<String> moIds = Arrays.stream(vehicleIds.split(",")).collect(Collectors.toSet());
                String brands = VehicleUtil.batchGetBindInfosByRedis(moIds, Lists.newArrayList("name")).values()
                    .stream()
                    .map(BindDTO::getName)
                    .collect(Collectors.joining(","));
                VehicleInfo vehicle = new VehicleInfo();
                vehicle.setId(vehicleIds);
                vehicle.setBrand(brands);
                ModelAndView mav;
                switch (deviceType) {
                    case "10":
                        mav = new ModelAndView(EDIT_F3LS_PAGE);
                        // mav = alarmSettingService.findF3Object(null, mav);
                        break;
                    case "9":
                        mav = new ModelAndView(EDIT_ASOLS_PAGE);
                        // mav = alarmSettingService.findAsoObject(null, mav);
                        break;
                    case "5":
                        mav = new ModelAndView(EDIT_BD_PAGE);
                        // mav = alarmSettingService.findBdObject(null, mav);
                        break;

                    default:
                        mav = new ModelAndView(EDIT_PAGE);
                        // mav = alarmSettingService.find808Object(null, mav, deviceType);
                        break;
                }
                // 查询参考车牌下拉列表
                // List<AlarmSetting> referVehicleList = alarmSettingService.findReferVehicleByDeviceType(deviceType);
                mav.addObject("vehicle", vehicle);
                // mav.addObject("referVehicleList", JSON.parseArray(JSON.toJSONString(referVehicleList)));
                return mav;
            }
        } catch (Exception e) {
            log.error("批量报警设置弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 保存报警参数的设置
     */
    @ApiOperation(value = "保存报警参数的设置", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "车辆ids(用逗号隔开)", required = true),
        @ApiImplicitParam(name = "checkedParams", value = "所选报警参数的json串，AlarmParameterSettingForm的实体json串。例："
            + "[{'alarmParameterId':'5b9b1006-bc26-11e6-a4a6-cec0c932ce01',"
            + "'vehicleId':'cae21196-cb66-4256-88a6-7cdfb23e2c78'},"
            + "{'alarmParameterId':'5b9b15ce-bc26-11e6-a4a6-cec0c932ce01',"
            + "'parameterValue':'5','vehicleId':'cae21196-cb66-4256-88a6-7cdfb23e2c78'}]",
            required = true) })
    @RequestMapping(value = "/setting.gsp", method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean updateResourcesByRole(@RequestParam("id") final String vehicleIds,
        @RequestParam("checkedParams") final String checkedParams,
        @RequestParam("deviceType") final String deviceType) {
        try {
            if (StringUtils.isEmpty(vehicleIds) || StringUtils.isEmpty(checkedParams)
                || StringUtils.isEmpty(deviceType)) {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
            log.info("报警参数批量设置：{},时间：{}", Thread.currentThread().getName(), LocalDateTime.now());
            return alarmSettingService.updateAlarmParameterByBatch(vehicleIds, checkedParams, deviceType);
        } catch (Exception e) {
            log.error("批量修改报警参数设置异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 读取高精度
     */
    @RequestMapping(value = "/sendParameter", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean sendParameter(String vehicleId, String paramIds) {
        try {
            if (StringUtils.isNotBlank(vehicleId) && StringUtils.isNotBlank(paramIds)) {
                return alarmSettingService.sendParameter(vehicleId, paramIds);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("读取高精度设置异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 根据id删除报警参数设置
     */
    @ApiOperation(value = "根据车辆id删除该车辆的所有报警参数设置", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id) {
        try {
            if (StringUtils.isNotBlank(id)) {
                return alarmSettingService.deleteByVehicleIds(Collections.singletonList(id));
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("删除报警参数设置异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 批量删除
     */
    @ApiOperation(value = "根据车辆ids删除对应车辆的所有报警参数设置（用逗号隔开）", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "deltems", value = "车辆ids(用逗号隔开)")
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore() {
        try {
            String items = request.getParameter("deltems");
            if (StringUtils.isNotBlank(items)) {
                return alarmSettingService
                    .deleteByVehicleIds(Arrays.stream(items.split(",")).collect(Collectors.toList()));
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("批量删除报警参数设置异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 根据id下发报警参数设置
     */
    @ApiOperation(value = "报警参数下发", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "sendParam",
        value = "下发参数json串，格式：[{'alarmVehicleId':'车辆与报警参数绑定id','vehicleId':'车辆id',"
        + "'alarmTypeId':'报警类型id','paramId':'下发id'},{}...]。  例：[{'alarmVehicleId':'null',"
        + "'paramId':'02fc3f69-b14e-4761-ae0f-c9c82c7e3b75',"
        + "'vehicleId':'cae21196-cb66-4256-88a6-7cdfb23e2c78','alarmTypeId':'null'}]",
        required = true)
    @RequestMapping(value = "/sendAlarm", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean sendAlarm(String sendParam) {
        try {
            if (StringUtils.isEmpty(sendParam)) {
                return new JsonResultBean(JsonResultBean.FAULT, "下发失败 ！");
            }
            log.info("报警参数下发：{},时间：{}", Thread.currentThread().getName(), LocalDateTime.now());
            ArrayList<JSONObject> paramList = JSON.parseObject(sendParam, ArrayList.class);
            // 下发报警参数设置
            return alarmSettingService.sendAlarm(paramList);
        } catch (Exception e) {
            log.error("下发报警参数设置异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "下发失败！");
        }
    }

    /**
     * 根据车辆id查询参考车牌报警参数
     */
    @ApiOperation(value = "根据车辆id查询参考车牌报警参数", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/getAlarmParameter_{vehicleId}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getAlarmParameter(@PathVariable("vehicleId") String vehicleId) {
        try {
            List<AlarmParameterSettingForm> parameterList = alarmSettingService.findParameterByVehicleId(vehicleId);
            if (CollectionUtils.isNotEmpty(parameterList)) {
                return new JsonResultBean(parameterList);
            }
            return new JsonResultBean(new JSONArray());
        } catch (Exception e) {
            log.error("根据车id查询报警参数设置异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 获取联动策略页面
     */
    @ApiIgnore
    @RequestMapping(value = { "/linkage_{vehicleId}" }, method = RequestMethod.GET)
    public ModelAndView linkagePage(@PathVariable("vehicleId") final String vehicleId, String deviceType) {
        ModelAndView mav = new ModelAndView(LINKAGE_PAGE);
        try {
            final List<String> monitorIdList = Arrays.stream(vehicleId.split(",")).collect(Collectors.toList());
            Map<String, BindDTO> bindInfoMap = VehicleUtil.batchGetBindInfosByRedis(monitorIdList);
            //查询所需的报警类型
            final Integer deviceTypeVal = Integer.valueOf(deviceType);
            List<AlarmType> alarmTypeList = alarmSettingService.getAlarmType(monitorIdList, deviceTypeVal);
            List<AlarmLinkageDTO> settingList = new ArrayList<>();
            if (monitorIdList.size() == 1) {
                settingList = alarmSettingService.getLinkageSettingList(vehicleId);
            }
            List<VehicleInfo> referVehicleList =
                alarmSettingService.findReferPhotoVehicles(monitorIdList, deviceTypeVal);
            mav.addObject("linkageFlag", monitorIdList.size() == 1 ? 0 : 1);
            mav.addObject("brands",
                bindInfoMap.values().stream().map(BindDTO::getName).collect(Collectors.joining(",")));
            mav.addObject("vehicleIds", String.join(",", bindInfoMap.keySet()));
            mav.addObject("deviceType", deviceType);
            mav.addObject("alarmTypeList", JSON.toJSONString(alarmTypeList));
            mav.addObject("settingList", JSON.toJSONString(settingList));
            mav.addObject("referVehicleList", JSON.toJSONString(referVehicleList));
            return mav;
        } catch (Exception e) {
            log.error("联动策略弹出页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 参考车牌查询联动参考设置参数
     */
    @ApiOperation(value = "参考车牌查询联动参考设置参数", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "vehicleId", value = "车辆id", required = true)
    @RequestMapping(value = "/referSetting", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getReferSetting(String vehicleId) {
        try {
            if (vehicleId != null) {
                List<AlarmLinkageDTO> referSettingList = alarmSettingService.getLinkageSettingList(vehicleId);
                return new JsonResultBean(referSettingList);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("根据车id查询联动参考设置异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 保存联动参考设置参数
     */
    @ApiOperation(value = "保存联动参考设置参数", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "linkageParam", value = "下发参数json串，格式：[{'photo':'拍照参数','recording':'录像',"
            + "'videoFlag':'实时视频','msg':'下发短信','pos':'预警类型'}]。  "
            + "例：[{'photo':null,'recording':null,'videoFlag':0,"
            + "'msg':{'msgContent':,'marks':3,4,5},'pos':14,'msgFlg':0}]", required = true),
        @ApiImplicitParam(name = "vehicleId", value = "车辆id", required = true) })
    @RequestMapping(value = "/savePhotoSetting", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean saveLinkageSetting(String linkageParam, String vehicleId) {
        try {
            if (StringUtils.isNotBlank(linkageParam) && StringUtils.isNotBlank(vehicleId)) {
                return alarmSettingService.saveLinkageSetting(linkageParam, vehicleId);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("保存联动参考设置异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 保存联动参考设置参数
     */
    @ApiOperation(value = "保存联动参考设置参数", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "linkageParam", value = "下发参数json串，格式：[{'photo':'拍照参数','recording':'录像',"
            + "'videoFlag':'实时视频','msg':'下发短信','pos':'预警类型'}]。  "
            + "例：[{'photo':null,'recording':null,'videoFlag':0,"
            + "'msg':{'msgContent':,'marks':3,4,5},'pos':14,'msgFlg':0}]",
            required = true),
        @ApiImplicitParam(name = "vehicleId", value = "车辆id（多个批量时以,隔开）", required = true) })
    @RequestMapping(value = "/saveMorePhotoSetting", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean saveMoreLinkageSetting(String linkageParam, String vehicleId) {
        try {
            if (StringUtils.isNotBlank(linkageParam) && StringUtils.isNotBlank(vehicleId)) {
                return alarmSettingService.saveLinkageSetting(linkageParam, vehicleId);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("批量保存联动参考设置异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 更新redis报警类型的信息
     */
    @ApiOperation(value = "更新redis报警类型的信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/updateAlarmType", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateRedisAlarmTypeInfo() {
        try {
            return alarmSettingService.updateAlarmType();
        } catch (Exception e) {
            log.error("重新存入redis报警类型信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 获取IO报警状态
     */
    @ApiOperation(value = "获取IO报警状态", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = { @ApiImplicitParam(name = "vehicleId", value = "车辆id", required = true),
        @ApiImplicitParam(name = "alarmTypeId", value = "报警类型id", required = true),
        @ApiImplicitParam(name = "value", value = "高低电压", required = true) })
    @ResponseBody
    @RequestMapping(value = "/getIOAlarmStateTxt", method = { RequestMethod.GET })
    public JsonResultBean getIoAlarmStateTxt(String vehicleId, String alarmTypeId, String value) {
        try {
            return alarmSettingService.getIOAlarmStateTxt(vehicleId, alarmTypeId, value);
        } catch (Exception e) {
            log.error("重新存入redis报警类型信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 初始化超速报警限速值和路网超速限速值冲突

     */
    @RequestMapping(value = "/initRoadAlarmSpeedLimit", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean initRoadAlarmSpeedLimit() {
        try {
            return new JsonResultBean(alarmSettingService.deleteRoadAlarmSpeedLimit());
        } catch (Exception e) {
            log.error("初始化超速报警限速值和路网超速限速值冲突信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }
}

package com.zw.api2.controller.alarm;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.api2.swaggerEntity.SwaggerAlarmSettingQuery;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.basicinfo.DeviceInfo;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.vas.alram.AlarmLinkageDTO;
import com.zw.platform.domain.vas.alram.AlarmSetting;
import com.zw.platform.domain.vas.alram.AlarmType;
import com.zw.platform.domain.vas.alram.form.AlarmParameterSettingForm;
import com.zw.platform.domain.vas.alram.query.AlarmSettingQuery;
import com.zw.platform.service.alarm.AlarmSettingService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @version 1.0
 * @Author gfw
 * @Date 2018/12/11 13:47
 * @Description 报警参数设置相关API
 */
@Controller
@RequestMapping("api/a/alarmSetting")
@Api(tags = { "报警参数设置_dev" }, description = "报警参数设置相关api接口")
public class ApiAlarmSettingController {
    private static Logger log = LogManager.getLogger(ApiAlarmSettingController.class);

    @Autowired
    private AlarmSettingService alarmSettingService;

    @Autowired
    private HttpServletRequest request;

    private static final String EDIT_PAGE = "vas/alarm/alarmSetting/setting";

    private static final String EDIT_F3LS_PAGE = "vas/alarm/alarmSetting/settingF3Ls";

    private static final String EDIT_ASOLS_PAGE = "vas/alarm/alarmSetting/settingAsoLs";

    private static final String EDIT_BD_PAGE = "vas/alarm/alarmSetting/settingBd";

    private static final String LINKAGE_PAGE = "vas/alarm/alarmSetting/linkage";

    /**
     * 分页查询报警参数设置列表
     */
    @Auth
    @ApiOperation(value = "分页查询报警参数设置列表", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "page", value = "页数（若输入页数大于最大页数，则返回第一页的数据）", required = true,
            paramType = "query", dataType = "long", defaultValue = "1"),
        @ApiImplicitParam(name = "limit", value = "每页显示条数", required = true, paramType = "query",
            dataType = "long", defaultValue = "20"),
        @ApiImplicitParam(name = "simpleQueryParam", value = "模糊搜索值,长度小于20", required = false,
            paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "groupId", value = "所属组织", required = false, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "assignmentId", value = "所属分组", required = false, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "deviceType", value = "设备类型", required = true, paramType = "query",
            dataType = "string", defaultValue = "-1") })
    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getListPage(@ModelAttribute("query") SwaggerAlarmSettingQuery swaggerAlarmSettingQuery) {
        Page<AlarmSetting> result = null;
        try {
            AlarmSettingQuery query = new AlarmSettingQuery();
            BeanUtils.copyProperties(swaggerAlarmSettingQuery, query);
            result = alarmSettingService.findAlarmSetting(query);

            return new PageGridBean(result, true);
        } catch (Exception e) {
            log.error("分页查询分组（findAlarmSetting）异常", e);
            PageGridBean pageGridBean = new PageGridBean(false);
            pageGridBean.setMessage(e.getMessage());
            return pageGridBean;
        } finally {
            if (result != null) {
                result.close();
            }
        }
    }

    /**
     * @param id
     * @return
     * @throws BusinessException ModelAndView
     * @Description:设置报警
     * @exception:
     * @author: wangying
     * @time:2016年12月7日 下午2:38:26
     */
    @ApiOperation(value = "根据车辆id查询报警参数设置", notes = "返回报警参数集合，该车辆已选报警参数的selected的值为true", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(
        value = { @ApiImplicitParam(name = "id", value = "车辆id", paramType = "query", dataType = "string"),
            @ApiImplicitParam(name = "deviceType", value = "设备类型", paramType = "query", dataType = "string"),
            @ApiImplicitParam(name = "type", value = "设备类型", paramType = "query", dataType = "string") })
    @ResponseBody
    @RequestMapping(value = { "/setting_{id}.gsp" }, method = RequestMethod.GET)
    public JsonResultBean editPage(@PathVariable("id") final String id, String deviceType,
        @RequestParam(value = "type", required = false) Integer type) {
        ModelAndView mav = null;
        try {
            switch (deviceType) {
                case "10":
                    mav = new ModelAndView(EDIT_F3LS_PAGE);
                    mav = alarmSettingService.findF3Object(id, mav);
                    break;
                case "9":
                    mav = new ModelAndView(EDIT_ASOLS_PAGE);
                    mav = alarmSettingService.findAsoObject(id, mav);
                    break;
                case "5":
                    mav = new ModelAndView(EDIT_BD_PAGE);
                    mav = alarmSettingService.findBdObject(id, mav);
                    break;

                default:
                    mav = new ModelAndView(EDIT_PAGE);
                    mav = alarmSettingService.find808Object(id, mav, deviceType);
                    break;
            }
            // 查询参考车牌下拉列表
            List<AlarmSetting> referVehicleList = alarmSettingService.findReferVehicleByDeviceType(deviceType);
            // 根据id查询车辆、人、物品信息
            VehicleInfo vehicle = alarmSettingService.findPeopleOrVehicleOrThingById(id);
            mav.addObject("vehicle", vehicle);
            mav.addObject("referVehicleList", JSON.parseArray(JSON.toJSONString(referVehicleList)));
            return new JsonResultBean(mav.getModel());
        } catch (Exception e) {
            log.error("报警设置弹出页面异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * @param id
     * @param deviceType
     * @return
     * @throws BusinessException ModelAndView
     * @throws IOException
     * @Description:批量设置报警
     * @exception:
     * @author: wangying
     * @time:2016年12月7日 下午2:38:26
     */
    @ApiOperation(value = "根据车辆id集合(用逗号隔开)查询报警参数设置", notes = "返回所有报警参数集合，用于批量设置报警参数", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(
        value = { @ApiImplicitParam(name = "deviceType", value = "设备类型", paramType = "query", dataType = "string") })
    @ResponseBody
    @RequestMapping(value = { "/settingmore_{id}.gsp" }, method = RequestMethod.GET)
    public JsonResultBean settingMorePage(@ApiParam(value = "车辆id集合") @PathVariable("id") final String id,
        String deviceType, HttpServletResponse response) {
        try {
            final String items = id;
            if (StringUtils.isBlank(items)) {
                response.setContentType("text/htmlcharset=utf-8");
                PrintWriter out = response.getWriter();
                out.println("<script language='javascript'>");
                out.println("$('#commonWin').modal('hide');");
                out.println("layer.msg('请选择一条数据！');");
                out.println("</script>");
                return null;
            } else {
                String[] item = items.split(",");
                StringBuilder brands = new StringBuilder();
                if (item.length > 0) {
                    for (String its : item) {
                        VehicleInfo v = alarmSettingService.findPeopleOrVehicleOrThingById(its);
                        if (v != null) {
                            brands.append(v.getBrand()).append(",");
                        }
                    }
                }
                VehicleInfo vehicle = new VehicleInfo();
                vehicle.setId(items);
                vehicle.setBrand(brands.substring(0, brands.toString().lastIndexOf(',')));
                ModelAndView mav;
                switch (deviceType) {
                    case "10":
                        mav = new ModelAndView(EDIT_F3LS_PAGE);
                        mav = alarmSettingService.findF3Object(null, mav);
                        break;
                    case "9":
                        mav = new ModelAndView(EDIT_ASOLS_PAGE);
                        mav = alarmSettingService.findAsoObject(null, mav);
                        break;
                    case "5":
                        mav = new ModelAndView(EDIT_BD_PAGE);
                        mav = alarmSettingService.findBdObject(null, mav);
                        break;

                    default:
                        mav = new ModelAndView(EDIT_PAGE);
                        mav = alarmSettingService.find808Object(null, mav, deviceType);
                        break;
                }
                // 查询参考车牌下拉列表
                List<AlarmSetting> referVehicleList = alarmSettingService.findReferVehicleByDeviceType(deviceType);
                mav.addObject("vehicle", vehicle);
                mav.addObject("referVehicleList", JSON.parseArray(JSON.toJSONString(referVehicleList)));
                return new JsonResultBean(mav.getModel());
            }
        } catch (Exception e) {
            log.error("批量报警设置弹出页面异常", e);
            return new JsonResultBean("报警设置异常");
        }
    }

    /**
     * 保存报警参数的设置
     * @param vehicleIds
     * @param checkedParams
     * @param deviceType
     * @return
     */
    @ApiOperation(value = "保存报警参数的设置", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "deviceType", value = "设备类型", dataType = "string", paramType = "query",
            required = true),
        @ApiImplicitParam(name = "id", value = "车辆ids(用逗号隔开)", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "checkedParams", value = "所选报警参数的json串，AlarmParameterSettingForm的实体json串。例："
            + "[{'alarmParameterId':'5b9b1006-bc26-11e6-a4a6-cec0c932ce01',"
            + "'vehicleId':'cae21196-cb66-4256-88a6-7cdfb23e2c78'},"
            + "{'alarmParameterId':'5b9b15ce-bc26-11e6-a4a6-cec0c932ce01',"
            + "'parameterValue':'5','vehicleId':'cae21196-cb66-4256-88a6-7cdfb23e2c78'}]", required = true,
            paramType = "query", dataType = "string") })
    @RequestMapping(value = "/setting.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateResourcesByRole(@RequestParam("id") final String vehicleIds,
        @RequestParam("checkedParams") final String checkedParams,
        @RequestParam("deviceType") final String deviceType) {
        try {
            if (vehicleIds != null && !vehicleIds.isEmpty() && checkedParams != null && !checkedParams.isEmpty()
                && deviceType != null && !deviceType.isEmpty()) {
                String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
                return alarmSettingService.updateAlarmParameterByBatch(vehicleIds, checkedParams, deviceType);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("批量修改报警参数设置异常", e);
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
    public JsonResultBean delete(@ApiParam(value = "id") @PathVariable("id") final String id) {
        try {
            if (!StringUtils.isEmpty(id)) {
                List<String> vehicleIds = new ArrayList<>();
                vehicleIds.add(id);
                return alarmSettingService.deleteByVehicleIds(vehicleIds);
            }
            return new JsonResultBean(JsonResultBean.FAULT, "车辆id不能为空");
        } catch (Exception e) {
            log.error("删除报警参数设置异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
        }
    }

    /**
     * 批量删除
     * @return result
     */
    @ApiOperation(value = "根据车辆ids删除对应车辆的所有报警参数设置（用逗号隔开）", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "deltems", value = "车辆ids(用逗号隔开)", required = false, paramType = "query",
        dataType = "string")
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore() {
        try {
            String items = request.getParameter("deltems");
            if (items != null && !items.isEmpty()) {
                String[] item = items.split(",");
                // 获取访问客户端的IP
                List<String> ids = Arrays.asList(item);
                return alarmSettingService.deleteByVehicleIds(ids);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("批量删除报警参数设置异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
        }
    }

    /**
     * 根据id下发报警参数设置
     * @param sendParam sendParam
     */
    @ApiOperation(value = "报警参数下发", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "sendParam", value = "下发参数json串，格式：[{'alarmVehicleId':'车辆与报警参数绑定id',"
        + "'vehicleId':'车辆id',"
        + "'alarmTypeId':'报警类型id','paramId':'下发id'},{}...]。  例：[{'alarmVehicleId':'null',"
        + "'paramId':'02fc3f69-b14e-4761-ae0f-c9c82c7e3b75',"
        + "'vehicleId':'cae21196-cb66-4256-88a6-7cdfb23e2c78','alarmTypeId':'null'}]", required = true,
        paramType = "query", dataType = "string")
    @RequestMapping(value = "/sendAlarm", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean sendAlarm(String sendParam) {
        try {
            if (sendParam != null && !sendParam.isEmpty()) {
                ArrayList<JSONObject> paramList = JSON.parseObject(sendParam, ArrayList.class);
                String ip = new GetIpAddr().getIpAddr(request);
                // 下发报警参数设置
                return alarmSettingService.sendAlarm(paramList);
            }
            return new JsonResultBean(JsonResultBean.FAULT, "下发失败 ！");
        } catch (Exception e) {
            log.error("下发报警参数设置异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "下发失败！");
        }
    }

    /**
     * getAlarmParameter
     * @param vehicleId 车Id
     * @return result
     */
    @ApiOperation(value = "根据车辆id查询参考车牌报警参数", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/getAlarmParameter_{vehicleId}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getAlarmParameter(@ApiParam(value = "车辆id") @PathVariable("vehicleId") String vehicleId) {
        try {
            List<AlarmParameterSettingForm> parameterList = alarmSettingService.findParameterByVehicleId(vehicleId);
            return new JsonResultBean(parameterList);
        } catch (Exception e) {
            log.error("根据车id查询报警参数设置异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 获取联动策略页面
     * @param vehicleId
     * @return
     */
    @ApiOperation(value = "获取联动策略", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "vehicleId", value = "车辆id", dataType = "string", paramType = "query"),
        @ApiImplicitParam(name = "deviceType", value = "设备类型（-1:JT/T808-2013;11:JT/T808-2019等）",
            dataType = "string", paramType = "query")})
    @ResponseBody
    @RequestMapping(value = { "/linkage_{vehicleId}" }, method = RequestMethod.GET)
    public JsonResultBean linkagePage(@PathVariable("vehicleId") final String vehicleId, String deviceType) {
        ModelAndView mav = new ModelAndView(LINKAGE_PAGE);
        try {
            String [] vids = vehicleId.split(",");
            //查询所需的报警类型
            List<AlarmType> alarmTypeList =
                alarmSettingService.getAlarmType(Arrays.asList(vids), DeviceInfo.DEVICE_TYPE_2013);
            List<AlarmLinkageDTO> settingList = alarmSettingService.getLinkageSettingList(vehicleId);
            List<VehicleInfo> referVehicleList =
                alarmSettingService.findReferPhotoVehicles(Arrays.asList(vids), Integer.valueOf(deviceType));
            VehicleInfo vehicle = alarmSettingService.findPeopleOrVehicleOrThingById(vehicleId);
            mav.addObject("vehicle", vehicle);
            mav.addObject("alarmTypeList", JSON.parseArray(JSON.toJSONString(alarmTypeList)));
            mav.addObject("settingList", JSON.parseArray(JSON.toJSONString(settingList)));
            mav.addObject("referVehicleList", JSON.parseArray(JSON.toJSONString(referVehicleList)));
            return new JsonResultBean(mav.getModel());
        } catch (Exception e) {
            log.error("获取联动策略", e);
            return new JsonResultBean(JsonResultBean.FAULT, "获取联动策略出错");
        }
    }

    /**
     * 参考车牌查询联动参考设置参数
     * @param vehicleId
     * @return
     */
    @ApiOperation(value = "参考车牌查询联动参考设置参数", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "vehicleId", value = "车辆id", required = true, paramType = "query", dataType = "string")
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
     * @param linkageParam
     * @param vehicleId
     * @return
     */
    @ApiOperation(value = "保存联动参考设置参数", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "linkageParam", value = "下发参数json串，格式：[{'photo':'拍照参数','recording':'录像',"
            + "'videoFlag':'实时视频','msg':'下发短信','pos':'预警类型'}]。  "
            + "例：[{'photo':null,'recording':null,'videoFlag':0,"
            + "'msg':{'msgContent':,'marks':3,4,5},'pos':14,'msgFlg':0}]", required = true, paramType = "query",
            dataType = "string"),
        @ApiImplicitParam(name = "vehicleId", value = "车辆id", required = true, paramType = "query",
            dataType = "string") })
    @RequestMapping(value = "/savePhotoSetting", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean saveLinkageSetting(String linkageParam, String vehicleId) {
        try {
            if (linkageParam != null && !linkageParam.isEmpty() && vehicleId != null && !vehicleId.isEmpty()) {
                // 获取访问客户端的IP
                String ipAddress = new GetIpAddr().getIpAddr(request);
                alarmSettingService.saveLinkageSetting(linkageParam, vehicleId);
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("保存联动参考设置异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 更新redis报警类型的信息
     */
    @ApiIgnore
    @ApiOperation(value = "更新redis报警类型的信息", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/updateAlarmType", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateReidsAlarmTypeInfo() {
        try {
            return alarmSettingService.updateAlarmType();
        } catch (Exception e) {
            log.error("重新存入redis报警类型信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 获取IO报警状态
     *
     * 项目功能 在主干上未发现调用痕迹
     */
    @ApiIgnore
    @ApiOperation(value = "获取IO报警状态", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "vehicleId", value = "车辆id",
            required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "alarmTypeId", value = "报警类型id",
            required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "value", value = "高低电压 高电压:1 低电压:2",
            required = true, paramType = "query", dataType = "string") })
    @ResponseBody
    @RequestMapping(value = "/getIOAlarmStateTxt", method = { RequestMethod.GET })
    public JsonResultBean getIOAlarmStateTxt(String vehicleId, String alarmTypeId, String value) {
        try {
            return alarmSettingService.getIOAlarmStateTxt(vehicleId, alarmTypeId, value);
        } catch (Exception e) {
            log.error("获取IO报警状态异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }
}

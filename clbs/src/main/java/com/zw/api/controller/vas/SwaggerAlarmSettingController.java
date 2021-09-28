package com.zw.api.controller.vas;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.vas.alram.AlarmSetting;
import com.zw.platform.domain.vas.alram.form.AlarmParameterSettingForm;
import com.zw.platform.domain.vas.alram.query.AlarmSettingQuery;
import com.zw.platform.service.alarm.AlarmSettingService;
import com.zw.platform.service.basicinfo.VehicleService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Controller
@RequestMapping("/swagger/v/alarmSetting")
@Api(tags = {"报警参数设置"}, description = "报警参数设置相关api接口")
public class SwaggerAlarmSettingController {
    @Autowired
    private AlarmSettingService alarmSettingService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private HttpServletRequest request;

    @Auth

    @ApiOperation(value = "分页查询报警参数设置列表", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "page", value = "页数（若输入页数大于最大页数，则返回第一页的数据）",
			required = true, paramType = "query", dataType = "long", defaultValue = "1"),
        @ApiImplicitParam(name = "limit", value = "每页显示条数",
			required = true, paramType = "query", dataType = "long", defaultValue = "20"),
        @ApiImplicitParam(name = "simpleQueryParam", value = "模糊搜索值,长度小于20",
			paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "groupId", value = "所属组织",
			paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "assignmentId", value = "所属分组",
			paramType = "query", dataType = "string")})
    @RequestMapping(value = {"/list"}, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getListPage(final AlarmSettingQuery query) {
        if (query != null) {
            Page<AlarmSetting> result = null;
            // 校验传入字段
            try {
                if (query.getPage() == null || query.getLimit() == null) { // page和limit不能为空
                    return new PageGridBean(PageGridBean.FAULT);
                }
                if (StringUtils.isNotBlank(query.getSimpleQueryParam())
					&& query.getSimpleQueryParam().length() > 20) { // 模糊搜索长度小于20
                    return new PageGridBean(PageGridBean.FAULT);
                }

                result = alarmSettingService.findAlarmSetting(query);

                return new PageGridBean(result, true);
            } catch (Exception e) {
                return null;
            } finally {
                if (result != null) {
                    result.close();
                }
            }
        }
        return null;
    }

    /**
     * 设置报警
     */
    @ApiOperation(value = "根据车辆id查询报警参数设置", notes = "返回报警参数集合，该车辆已选报警参数的selected的值为true", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @RequestMapping(value = {"/setting_{id}.gsp"}, method = RequestMethod.GET)
    public JsonResultBean editPage(@PathVariable("id") final String id) throws Exception {
        if (vehicleService.findVehicleById(id) == null) { // 校验车辆是否存在
            return new JsonResultBean(JsonResultBean.FAULT, "车辆不存在！");
        }
        JSONObject objJson = new JSONObject();
        List<AlarmSetting> alertList = new ArrayList<>(); // 预警型
        List<AlarmSetting> driverAlarmList = new ArrayList<>(); // 驾驶员引起报警
        List<AlarmSetting> vehicleAlarmList = new ArrayList<>(); // 车辆报警
        List<AlarmSetting> faultAlarmList = new ArrayList<>(); // 故障报警
        List<AlarmSetting> otherAlarmList = new ArrayList<>(); // 其他报警

        List<AlarmSetting> selectList = alarmSettingService.findByVehicleId(id);
        List<AlarmSetting> allList = alarmSettingService.findAllAlarmParameter();
        // 查询参考车牌下拉列表
        List<AlarmSetting> referVehicleList = alarmSettingService.findVehicleAlarmSetting();
        // 根据id查询车辆
        VehicleInfo vehicle = vehicleService.findVehicleById(id);
        // 重组报警参数值
        if (allList != null && !allList.isEmpty()) {
            for (AlarmSetting alarm : allList) {
                boolean selectFlag = false;
                if (selectList != null && !selectList.isEmpty()) {
                    for (AlarmSetting selectAlarm : selectList) {
                        if (selectAlarm.getAlarmParameterId().equals(alarm.getId())) {
                            alarm.setSelected(true);
                            alarm.setParameterValue(selectAlarm.getParameterValue());
                            selectFlag = true;
                            break;
                        }
                    }
                }
                if (!selectFlag) {
                    alarm.setSelected(false);
                    alarm.setParameterValue(alarm.getDefaultValue() != null ? alarm.getDefaultValue().toString() : "");
                }
                switch (alarm.getType()) {
                    case "alart":
                        alertList.add(alarm);
                        break;
                    case "driverAlarm":
                        driverAlarmList.add(alarm);
                        break;
                    case "vehicleAlarm":
                        vehicleAlarmList.add(alarm);
                        break;
                    case "faultAlarm":
                        faultAlarmList.add(alarm);
                        break;
                    case "otherAlarm":
                        otherAlarmList.add(alarm);
                        break;
                    default:
                        break;
                }
            }
        }
        String allListJsonStr = JSON.toJSONString(allList);
        String alertListJsonStr = JSON.toJSONString(alertList);
        String driverAlarmListJsonStr = JSON.toJSONString(driverAlarmList);
        String vehicleAlarmListJsonStr = JSON.toJSONString(vehicleAlarmList);
        String faultAlarmListJsonStr = JSON.toJSONString(faultAlarmList);
        String otherAlarmListJsonStr = JSON.toJSONString(otherAlarmList);
        String referVehicleListJsonStr = JSON.toJSONString(referVehicleList);

        objJson.put("allList", JSON.parseArray(allListJsonStr));
        objJson.put("alertList", JSON.parseArray(alertListJsonStr));
        objJson.put("driverAlarmList", JSON.parseArray(driverAlarmListJsonStr));
        objJson.put("vehicleAlarmList", JSON.parseArray(vehicleAlarmListJsonStr));
        objJson.put("faultAlarmList", JSON.parseArray(faultAlarmListJsonStr));
        objJson.put("otherAlarmList", JSON.parseArray(otherAlarmListJsonStr));
        objJson.put("vehicle", vehicle);
        objJson.put("referVehicleList", JSON.parseArray(referVehicleListJsonStr));
        return new JsonResultBean(objJson);
    }

    /**
     * 批量设置报警
     */
    @ApiOperation(value = "根据车辆id集合(用逗号隔开)查询报警参数设置", notes = "返回所有报警参数集合，用于批量设置报警参数", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @RequestMapping(value = {"/settingmore_{id}.gsp"}, method = RequestMethod.GET)
    public JsonResultBean settingMorePage(@PathVariable("id") final String items) throws Exception {
        if (StringUtils.isBlank(items)) {
            return new JsonResultBean(JsonResultBean.FAULT, "请选择一条数据");
        }
        JSONObject objJson = new JSONObject();
        List<AlarmSetting> alertList = new ArrayList<>(); // 预警型
        List<AlarmSetting> driverAlarmList = new ArrayList<>(); // 驾驶员引起报警
        List<AlarmSetting> vehicleAlarmList = new ArrayList<>(); // 车辆报警
        List<AlarmSetting> faultAlarmList = new ArrayList<>(); // 故障报警
        List<AlarmSetting> otherAlarmList = new ArrayList<>(); // 其他报警

        List<AlarmSetting> allList = alarmSettingService.findAllAlarmParameter();
        // 查询参考车牌下拉列表
        List<AlarmSetting> referVehicleList = alarmSettingService.findVehicleAlarmSetting();
        String[] item = items.split(",");
        StringBuilder sb = new StringBuilder();
        for (String id : item) {
            VehicleInfo v = vehicleService.findVehicleById(id);
            if (v != null) {
                sb.append(v.getBrand()).append(",");
            } else {
                return new JsonResultBean(JsonResultBean.FAULT, "包含不存在的车辆！");
            }
        }
        String brands = sb.toString();
        VehicleInfo vehicle = new VehicleInfo();
        vehicle.setId(items);
        vehicle.setBrand(brands.substring(0, brands.lastIndexOf(',')));
        // 重组报警参数值
        if (allList != null && !allList.isEmpty()) {
            for (AlarmSetting alarm : allList) {
                alarm.setSelected(false);
                alarm.setParameterValue(alarm.getDefaultValue() != null ? alarm.getDefaultValue().toString() : "");
                switch (alarm.getType()) {
                    case "alart":
                        alertList.add(alarm);
                        break;
                    case "driverAlarm":
                        driverAlarmList.add(alarm);
                        break;
                    case "vehicleAlarm":
                        vehicleAlarmList.add(alarm);
                        break;
                    case "faultAlarm":
                        faultAlarmList.add(alarm);
                        break;
                    case "otherAlarm":
                        otherAlarmList.add(alarm);
                        break;
                    default:
                        break;
                }
            }
        }
        String allListJsonStr = JSON.toJSONString(allList);
        String alertListJsonStr = JSON.toJSONString(alertList);
        String driverAlarmListJsonStr = JSON.toJSONString(driverAlarmList);
        String vehicleAlarmListJsonStr = JSON.toJSONString(vehicleAlarmList);
        String faultAlarmListJsonStr = JSON.toJSONString(faultAlarmList);
        String otherAlarmListJsonStr = JSON.toJSONString(otherAlarmList);
        String referVehicleListJsonStr = JSON.toJSONString(referVehicleList);

        objJson.put("allList", JSON.parseArray(allListJsonStr));
        objJson.put("alertList", JSON.parseArray(alertListJsonStr));
        objJson.put("driverAlarmList", JSON.parseArray(driverAlarmListJsonStr));
        objJson.put("vehicleAlarmList", JSON.parseArray(vehicleAlarmListJsonStr));
        objJson.put("faultAlarmList", JSON.parseArray(faultAlarmListJsonStr));
        objJson.put("otherAlarmList", JSON.parseArray(otherAlarmListJsonStr));
        objJson.put("vehicle", vehicle);
        objJson.put("referVehicleList", JSON.parseArray(referVehicleListJsonStr));
        return new JsonResultBean(objJson);
    }

    @ApiOperation(value = "保存报警参数的设置", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "id", value = "车辆ids(用逗号隔开)",
			required = true, paramType = "query", dataType = "string"),
        @ApiImplicitParam(name = "checkedParams",
			value = "所选报警参数的json串，AlarmParameterSettingForm的实体json串。例："
					+ "[{'alarmParameterId':'5b9b1006-bc26-11e6-a4a6-cec0c932ce01',"
					+ "'vehicleId':'cae21196-cb66-4256-88a6-7cdfb23e2c78'},"
					+ "{'alarmParameterId':'5b9b15ce-bc26-11e6-a4a6-cec0c932ce01',"
					+ "'parameterValue':'5','vehicleId':'cae21196-cb66-4256-88a6-7cdfb23e2c78'}]",
			required = true, paramType = "query", dataType = "string")})
    @RequestMapping(value = "/setting.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean updateResourcesByRole(@RequestParam("id") final String vehicleIds,
        @RequestParam("checkedParams") final String checkedParams,
        @RequestParam("deviceType") final String deviceType) {
        try {
            // 获取访问客户端的IP
            String ipAddress = new GetIpAddr().getIpAddr(request);
            return alarmSettingService.updateAlarmParameterByBatch(vehicleIds, checkedParams, deviceType);
        } catch (Exception e) {
            return new JsonResultBean(JsonResultBean.FAULT, "参数（checkedParams）格式错误！");
        }
    }

    /**
     * 根据id删除报警参数设置
     */
    @ApiOperation(value = "根据车辆id删除该车辆的所有报警参数设置", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id) throws Exception {
        List<String> vehicleIds = new ArrayList<>();
        if (vehicleService.findVehicleById(id) == null) { // 判断车辆是否存在
            return new JsonResultBean(JsonResultBean.FAULT, "车辆不存在！");
        }
        vehicleIds.add(id);
        // 获取访问客户端的IP地址
        return alarmSettingService.deleteByVehicleIds(vehicleIds);
    }

    /**
     * 批量删除
     */
    @ApiOperation(value = "根据车辆ids删除对应车辆的所有报警参数设置（用逗号隔开）", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "deltems", value = "车辆ids(用逗号隔开)", paramType = "query", dataType = "string")
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore() throws Exception {
        String items = request.getParameter("deltems");
        String[] item = items.split(",");
        // 获取客户端的IP地址
        if (item.length > 0) {
            List<String> ids = Arrays.asList(item);
            for (String id : ids) {
                if (vehicleService.findVehicleById(id) == null) { // 判断车辆是否存在
                    return new JsonResultBean(JsonResultBean.FAULT, "包含不存在的车辆！");
                }
            }
            alarmSettingService.deleteByVehicleIds(ids);
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 根据id下发报警参数设置
     */
    @ApiOperation(value = "报警参数下发", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @ApiImplicitParam(name = "sendParam",
		value = "下发参数json串，格式：[{'alarmVehicleId':'车辆与报警参数绑定id','vehicleId':'车辆id',"
				+ "'alarmTypeId':'报警类型id','paramId':'下发id'},{}...]。  例：[{'alarmVehicleId':'null',"
				+ "'paramId':'02fc3f69-b14e-4761-ae0f-c9c82c7e3b75',"
				+ "'vehicleId':'cae21196-cb66-4256-88a6-7cdfb23e2c78','alarmTypeId':'null'}]",
		required = true, paramType = "query", dataType = "string")
    @RequestMapping(value = "/sendAlarm", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean sendAlarm(String sendParam) {
        try {
            if (sendParam != null && !sendParam.isEmpty()) {
                ArrayList<JSONObject> paramList = JSON.parseObject(sendParam, ArrayList.class);
                String ipAddress = new GetIpAddr().getIpAddr(request);
                // 下发报警参数设置
                return alarmSettingService.sendAlarm(paramList);
            }
            return new JsonResultBean(JsonResultBean.FAULT, "下发失败 ！");
        } catch (Exception e) {
            return new JsonResultBean(JsonResultBean.FAULT, "下发参数格式不正确！");
        }

    }

    @ApiOperation(value = "根据车辆id查询参考车牌报警参数", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des")})})
    @RequestMapping(value = "/getAlarmParameter_{vehicleId}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getAlarmParameter(@PathVariable("vehicleId") String vehicleId) {
        try {
            List<AlarmParameterSettingForm> parameterList = alarmSettingService.findParameterByVehicleId(vehicleId);
            return new JsonResultBean(parameterList);
        } catch (Exception e) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

}

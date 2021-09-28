package com.zw.api.controller.modules;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.github.pagehelper.Page;
import com.zw.platform.basic.dto.query.MonitorTreeReq;
import com.zw.platform.basic.service.MonitorTreeService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.functionconfig.form.FenceConfigForm;
import com.zw.platform.domain.functionconfig.query.FenceConfigQuery;
import com.zw.platform.service.basicinfo.VehicleService;
import com.zw.platform.service.functionconfig.CircleService;
import com.zw.platform.service.functionconfig.FenceConfigService;
import com.zw.platform.service.functionconfig.FenceService;
import com.zw.platform.service.functionconfig.LineService;
import com.zw.platform.service.functionconfig.MarkService;
import com.zw.platform.service.functionconfig.PolygonService;
import com.zw.platform.service.functionconfig.RectangleService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p> Title:电子围栏Controller </p> <p> Copyright: Copyright (c) 2016 </p> <p> Company: ZhongWei </p> <p> team:
 * ZhongWeiTeam </p>
 * @author wangying
 * @version 1.0
 * @since 2016年8月4日下午6:40:28
 */
@Controller
@RequestMapping("/swagger/m/bindfence")
@Api(tags = { "围栏管理" }, description = "围栏绑定相关api接口")
public class SwaggerBindFenceController {
    private static final Logger log = LogManager.getLogger(SwaggerBindFenceController.class);

    @Autowired
    VehicleService vehicleService;

    @Autowired
    UserService userService;

    @Autowired
    FenceService fenceService;

    @Autowired
    FenceConfigService fenceConfigService;

    @Autowired
    MarkService markService;

    @Autowired
    LineService lineService;

    @Autowired
    RectangleService rectangleService;

    @Autowired
    PolygonService polygonService;

    @Autowired
    CircleService circleService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private MonitorTreeService monitorTreeService;

    // 前端需要显示的列
    private static final String COLUMN_STR = "id,type,name,vehicle_id,brand,dirStatus,alarm_in,"
        + "alarm_out,alarm_start_time,alarm_end_time,alarm_start_date,"
        + "alarm_end_date,speed,over_speed_last_time,paramId,fenceId";

    @Auth

    @ApiOperation(value = "分页查询围栏与车辆绑定列表", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "page", value = "页数（若输入页数大于最大页数，则返回第一页的数据）", required = true, paramType = "query",
            dataType = "long", defaultValue = "1"),
        @ApiImplicitParam(name = "limit", value = "每页显示条数", required = true, paramType = "query", dataType = "long",
            defaultValue = "20"),
        @ApiImplicitParam(name = "simpleQueryParam", value = "模糊搜索值,长度小于20", paramType = "query",
            dataType = "string") })
    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getListPage(final FenceConfigQuery query) throws BusinessException {
        try {
            if (query == null) {
                return new PageGridBean(PageGridBean.FAULT);
            }
            // 校验传入字段
            if (query.getPage() == null || query.getLimit() == null) { // page和limit不能为空
                return new PageGridBean(PageGridBean.FAULT);
            }
            if (StringUtils.isNotBlank(query.getSimpleQueryParam())
                && query.getSimpleQueryParam().length() > 20) { // 模糊搜索长度小于20
                return new PageGridBean(PageGridBean.FAULT);
            }
            // 根据用户名获取用户id
            // 获取用户权限下的监控对象集合
            Set<String> vehicleIds = userService.getCurrentUserMonitorIds();
            if (vehicleIds != null && vehicleIds.size() > 0) {
                query.setVehicleIds(new ArrayList<>(vehicleIds));
            }
            Page<Map<String, Object>> result = fenceConfigService.findFenceConfigByPage(query);
            List<Map<String, Object>> dataMap = result.getResult();
            if (dataMap != null && dataMap.size() > 0) {
                // 遍历所有列名，若没有值，默认设置为""
                String[] column = COLUMN_STR.split(",");
                for (Map<String, Object> map : dataMap) {
                    for (String keyStr : column) {
                        if (!map.containsKey(keyStr)) {
                            map.put(keyStr, "");
                        }
                    }
                }
            }
            return new PageGridBean(query, result, true);
        } catch (Exception e) {
            log.error("分页查询围栏与车辆绑定信息异常", e);
            return new PageGridBean(false);
        }
    }

    @ApiOperation(value = "获取车辆树结构", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/vehicelTree", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getVehicleTree() {
        try {
            MonitorTreeReq monitorTreeReq = new MonitorTreeReq();
            monitorTreeReq.setMonitorType("vehicle");
            monitorTreeReq.setType("single");
            JSONArray result = monitorTreeService.getMonitorTreeFuzzy(monitorTreeReq);
            return new JsonResultBean("", result);
        } catch (Exception e) {
            log.error("获取车辆树结构异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
        }
    }

    @ApiOperation(value = "获取围栏树结构", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "type", value = "围栏树结构类型，multiple： 多选树结构     single: 单选树结构 ", required = true,
        paramType = "query", dataType = "string")
    @RequestMapping(value = "/fenceTree", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getFenceTree(String type) {
        // 校验传入参数
        if (!"single".equals(type) && !"multiple".equals(type)) {
            return new JsonResultBean(JsonResultBean.FAULT, "围栏树结构类型值错误！");
        }
        String result = fenceConfigService.getFenceTree(type);
        return new JsonResultBean("", result);
    }

    /**
     * 查询围栏详情
     * @author wangying
     */
    @ApiOperation(value = "根据围栏节点查询围栏详细信息（点的经纬度）", notes = "在地图上显示围栏", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "fenceNodes", value = "围栏树节点的json串，格式：[{'pId':'围栏类型','id':'围栏id'},{},...]。"
        + "例：[{'pId':'zw_m_circle','id':'8ed11a17-bd71-4e1a-a0e7-75ee1cac9de7'}]。"
        + "(围栏类型：zw_m_marker：标注；zw_m_line：线；zw_m_rectangle：矩形；zw_m_circle：圆形；zw_m_polygon：多边形)", required = true,
        paramType = "query", dataType = "string")
    @RequestMapping(value = { "/getFenceDetail" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getFenceDetail(String fenceNodes) {
        try {
            List<JSONObject> nodeList = JSON.parseArray(fenceNodes, JSONObject.class);
            if (nodeList == null || nodeList.isEmpty()) {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
            JSONArray dataArr = nodeList.stream().filter(Objects::nonNull)
                .map(obj -> fenceService.getFenceDetail(obj.getString("pId"), obj.getString("id")))
                .collect(Collectors.toCollection(JSONArray::new));
            return new JsonResultBean(dataArr);
        } catch (Exception e) {
            return new JsonResultBean(JsonResultBean.FAULT, "围栏树节点格式错误");
        }
    }

    /**
     * 保存绑定详情
     * @return JsonResultBean
     * @author wangying
     */
    @ApiOperation(value = "保存车辆与围栏的绑定", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "data",
        value = "车辆与围栏绑定的Json串，格式 ：[{'alarmIn':'进围栏报警，0：不报警；1：报警','alarmOut':'出围栏报警，0：不报警；1：报警','"
            + "alarmStartTime':'开始日期，yyyy-mm-dd','alarmEndTime':'结束日期，yyyy-mm-dd',"
            + "'alarmStartDate':'开始时间  yyyy-mm-dd hh:MM:ss','alarmEndDate':'开始时间  yyyy"
            + "-mm-dd hh:MM:ss','speed':'限速（km/h）','overSpeedLastTime':'超速持续时间（s）',"
            + "'fenceId':'围栏id','vehicleId':'车辆id'},{},...]， 例： [{'alarmIn':'1','alarmOut':'1',"
            + "'alarmStartTime':'2017-02-07','alarmEndTime':'2017-02-08',"
            + "'alarmStartDate':'2016-01-01 05:07:08','alarmEndDate':'2016-01-01 13:20:20',"
            + "'speed':'78','overSpeedLastTime':'10'," + "'fenceId':'f4f8fb9b-910b-48fc-8c98-b0a8b39190e9',"
            + "'vehicleId':'cae21196-cb66-4256-88a6-7cdfb23e2c78'}]", required = true, paramType = "query",
        dataType = "string")
    @RequestMapping(value = { "/saveBindFence" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean saveBindFence(String data) {
        JSONObject msg = new JSONObject();
        msg.put("flag", 2); // 0：失败 1： 通过 2：校验失败
        boolean checkFlag = true;
        try { // 校验传入参数格式
            List<FenceConfigForm> list = JSON.parseObject(data, new TypeReference<ArrayList<FenceConfigForm>>() {
            });
            if (list != null && list.size() != 0) {
                StringBuilder message = new StringBuilder();
                // 校验同一个围栏和同一个车辆是否已经绑定过
                for (FenceConfigForm config : list) {
                    String vehicleId = config.getVehicleId();
                    String fenceId = config.getFenceId();
                    // 校验
                    if (vehicleService.findVehicleById(vehicleId) == null) {
                        return new JsonResultBean(JsonResultBean.FAULT, "包含不存在的车辆！");
                    }
                    if (fenceService.findFenceInfoById(fenceId) == null) {
                        return new JsonResultBean(JsonResultBean.FAULT, "包含不存在的围栏！");
                    }
                    Map<String, Object> fenceConfig = fenceConfigService.findByVIdAndFId(vehicleId, fenceId);
                    if (fenceConfig != null) { // 已绑定
                        checkFlag = false;
                        message.append("车辆\"").append(fenceConfig.get("brand")).append("\"与围栏\"")
                            .append(fenceConfig.get("name")).append("\"已存在绑定关系，不能重复绑定！<br/>");
                    }
                }
                if (!checkFlag) {
                    msg.put("errMsg", message);
                    return new JsonResultBean(msg);
                } else {
                    fenceConfigService.addFenceConfigByBatch(list);
                    msg.put("flag", 1);
                    msg.put("errMsg", "保存成功！");
                    return new JsonResultBean(msg);
                }
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            return new JsonResultBean(JsonResultBean.FAULT, "参数格式不正确！");
        }

    }

    /**
     * 根据id下发围栏
     */
    @ApiOperation(value = "车辆与围栏绑定下发", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "sendParam",
        value = "下发参数json串，格式：[{'fenceConfigId':'车辆与围栏的绑定id'," + "'vehicleId':'车辆id','paramId':'围栏绑定下发id'},{}...]。  "
            + "例：{'fenceConfigId':'b7d75f0a-068f-4977-87f3-589defa8047c',"
            + "'paramId':'','vehicleId':'cae21196-cb66-4256-88a6-7cdfb23e2c78'}]", required = true, paramType = "query",
        dataType = "string")
    @RequestMapping(value = "/sendFence", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean sendFence(String sendParam) {
        List<Map<String, Object>> mapList = new ArrayList<>();
        try { // 校验参数格式
            List<JSONObject> paramList = JSON.parseArray(sendParam, JSONObject.class);
            if (paramList != null && paramList.size() > 0) {
                for (JSONObject obj : paramList) {
                    Map<String, Object> map = new HashMap<>();
                    // 校验车辆
                    if (vehicleService.findVehicleById((String) obj.get("vehicleId")) == null) {
                        return new JsonResultBean(JsonResultBean.FAULT, "包含不存在的车辆！");
                    }
                    // 校验绑定关系
                    if (fenceConfigService.queryFenceConfigById((String) obj.get("fenceConfigId")) == null) {
                        return new JsonResultBean(JsonResultBean.FAULT, "包含不存在的绑定关系！");
                    }
                    map.put("bindId", obj.get("fenceConfigId"));
                    map.put("vehicleId", obj.get("vehicleId"));
                    // map.put("fenceConfig", form);
                    map.put("sendType", "1"); // 1:围栏绑定 2：解除绑定
                    if (obj.get("paramId") != null && !"".equals(obj.get("paramId"))) {
                        map.put("paramId", obj.get("paramId"));
                    }
                    mapList.add(map);
                }
                // 电子围栏绑定下发设备
                fenceConfigService.sendFenceByType(mapList);
                return new JsonResultBean(JsonResultBean.SUCCESS);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            return new JsonResultBean(JsonResultBean.FAULT, "下发参数格式错误！");
        }
    }

    /**
     * 根据id删除 车辆与围栏的绑定
     */
    @ApiOperation(value = "根据绑定id删除车辆与围栏的绑定关系", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/delete_{id}.gsp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete(@PathVariable("id") final String id) {
        try {
            FenceConfigForm form = new FenceConfigForm();
            form.setId(id);
            form.setFlag(0);
            // 下发解绑
            List<String> ids = new ArrayList<>();
            ids.add(id);
            // 校验绑定关系是否存在
            List<Map<String, Object>> configs = fenceConfigService.findFenceConfigByIds(ids);
            fenceConfigService.sendUnbindFence(configs);
            String ipAddress = new GetIpAddr().getIpAddr(request);
            // 数据库删除绑定
            return fenceConfigService.unbindFence(id, ipAddress);
        } catch (Exception e) {
            log.error("删除车辆与围栏的绑定关系异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    /**
     * 批量删除
     */
    @ApiOperation(value = "根据绑定id集合批量删除车辆与围栏的绑定关系", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "deltems", value = "绑定id集合串(用逗号隔开)", required = true, paramType = "query",
        dataType = "string")
    @RequestMapping(value = "/deletemore", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean deleteMore(@RequestParam("deltems") String items) {
        try {
            if (items != null && !items.isEmpty()) {
                String[] item = items.split(",");
                List<String> ids = Arrays.asList(item);
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return fenceConfigService.unbindFenceByBatch(ids, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("保存围栏绑定信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    @ApiOperation(value = "获取分组车辆树结构", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @RequestMapping(value = "/assignvehicleTreeForApp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getAssignVehicleTreeForApp() {
        try {
            JSONArray jsonResult = vehicleService.assignVehicleTreeForApp();
            return new JsonResultBean(jsonResult);
        } catch (Exception e) {
            log.error("获取分组车辆树结构异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    @ApiOperation(value = "获取分组车辆树结构(包括人和车)", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "page", value = "页数（若输入页数大于最大页数，则返回第一页的数据）", required = true, paramType = "query",
            dataType = "long", defaultValue = "1"),
        @ApiImplicitParam(name = "limit", value = "每页显示条数", required = true, paramType = "query", dataType = "long",
            defaultValue = "15") })
    @RequestMapping(value = "/monitorTreeForApp", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getMonitorTree(int limit, int page) throws BusinessException {
        try {
            JSONArray jsonResult = vehicleService.monitorTreeFoApp(limit, page);
            return new JsonResultBean(jsonResult);
        } catch (Exception e) {
            log.error("获取分组车辆树结构异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    @ApiOperation(value = "获取当前用户拥有权限的所有车（绑定的）", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2,
            scopes = { @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "queryParam", value = "监控对象名称", paramType = "query", dataType = "string")
    @RequestMapping(value = "/monitorForUser", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getMonitorForUser(String queryParam) {
        try {
            List<VehicleInfo> list = vehicleService.findMonitorByName(queryParam);
            return new JsonResultBean(list);
        } catch (Exception e) {
            log.error("获取分组车辆树结构异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

}

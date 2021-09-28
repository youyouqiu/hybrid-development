package com.zw.platform.controller.alarm;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.GroupDTO;
import com.zw.platform.basic.dto.query.MonitorTreeReq;
import com.zw.platform.basic.service.MonitorService;
import com.zw.platform.basic.service.MonitorTreeService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.enmu.ProtocolEnum;
import com.zw.platform.domain.multimedia.HandleMultiAlarms;
import com.zw.platform.domain.vas.alram.AlarmType;
import com.zw.platform.domain.vas.switching.SwitchType;
import com.zw.platform.dto.alarm.AlarmPageReq;
import com.zw.platform.service.alarm.AlarmSearchService;
import com.zw.platform.service.basicinfo.AssignmentService;
import com.zw.platform.service.monitoring.RealTimeRiskService;
import com.zw.platform.service.offlineExport.OfflineExportService;
import com.zw.platform.service.switching.SwitchTypeService;
import com.zw.platform.util.DateUtil;
import com.zw.platform.util.common.AlarmTypeUtil;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.platform.util.common.ZipUtil;
import com.zw.talkback.common.ControllerTemplate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import jodd.util.StringUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Administrator
 */
@Controller
@RequestMapping("/a/search")
@Api(tags = { "报警查询_dev" })
public class AlarmSearchController {
    private static final String LIST_PAGE = "vas/alarm/alarmSearch/alarmSearch";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    private static final Logger logger = LogManager.getLogger(AlarmSearchController.class);

    @Autowired
    private AlarmSearchService alarmSearchService;

    @Autowired
    private RealTimeRiskService realTimeRiskService;

    @Autowired
    private MonitorTreeService monitorTreeService;

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private UserService userService;

    @Autowired
    private MonitorService monitorService;

    @Resource
    private SwitchTypeService switchTypeService;

    @Autowired
    private OfflineExportService exportService;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    /**
     * 日期转换格式
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 将车辆id存入session中
     */
    @ApiOperation(value = "将车辆id存入session中", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = { @ApiImplicitParam(name = "vehicleId", value = "车辆id"),
        @ApiImplicitParam(name = "session", value = "http的session") })
    @RequestMapping(value = { "/addSession" }, method = RequestMethod.POST)
    @ResponseBody
    public boolean addSession(String vehicleId, HttpSession session) {
        if (StringUtils.isNotBlank(vehicleId)) {
            try {
                // 获取前端页面vid传入session,
                session.setAttribute("vehicleIdAlram", vehicleId);
                return true;
            } catch (Exception e) {
                logger.error("addSession方法异常", e);
            }
        }
        return false;
    }

    /**
     * 实时监控双击报警信息跳转到报警查询页面
     */
    @ApiOperation(value = "ModelAndView_实时监控双击报警信息跳转到报警查询页面", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = { @ApiImplicitParam(name = "avid", value = "报警车辆id"),
        @ApiImplicitParam(name = "atype", value = "2为点击全局报警,0为实时监控点击进入此页面"),
        @ApiImplicitParam(name = "atime", value = "最早报警开始时间"),
        @ApiImplicitParam(name = "session", value = "http的session") })
    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public ModelAndView listPage(String avid, String atype, String atime, String alarmTypeArr) {
        try {
            // atyp:2为点击全局报警,0为实时监控点击进入此页面
            ModelAndView mv = new ModelAndView(LIST_PAGE);
            // 查询所需的报警类型
            List<AlarmType> alarms = alarmSearchService.getAlarmType();
            List<SwitchType> ioSwitchType = switchTypeService.getIoSwitchType();
            JSONObject alarmTypeName = installIoAlarmTypeTree(ioSwitchType);
            mv.addObject("alarmTypeName", alarmTypeName);
            // 实时监控、视频, 跳转报警,根据传递的alarmType进行报警类型展示
            if (StringUtils.isNotEmpty(alarmTypeArr)) {
                setAlarmTypeCheckedStatus(alarms, alarmTypeArr);
            }

            mv.addObject("type", JSON.toJSONString(alarms));
            if (StringUtil.isNotBlank(atype)) {
                if ("2".equals(atype)) {
                    Set<String> moIds = RedisHelper.getSet(HistoryRedisKeyEnum.UNHANDLED_VEHICLE.of());
                    atime = DateUtil.YMD_HMS.format(LocalDate.now().atStartOfDay()).orElseThrow(RuntimeException::new);
                    if (CollectionUtils.isNotEmpty(moIds)) {
                        // 全局报警跳转查询最大只支持5000个监控对象
                        avid = moIds.stream().limit(5000).collect(Collectors.joining(","));
                    } else {
                        return mv;
                    }
                }
                // 如果是实时监控、实时视频跳转, 则查询开始时间为atime(最早报警开始时间)
                // 通过vid查询车辆树组织ID
                String assignIds = assignmentService.getAssignsByMonitorId(avid);
                mv.addObject("assignIds", assignIds);
                mv.addObject("atype", atype);
                mv.addObject("avid", avid);
                mv.addObject("atime", atime);
            }
            return mv;
        } catch (Exception e) {
            logger.error("实时监控双击报警信息跳转到报警查询页面异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    private void setAlarmTypeCheckedStatus(List<AlarmType> alarms, String alarmTypeArr) {
        String[] arrPos = AlarmTypeUtil.arrPos;
        String[] queryAlarmTypes = alarmTypeArr.split(",");
        Set<String> alarmTypeSet = exchangeAlarmType(arrPos, queryAlarmTypes);

        for (AlarmType alarm : alarms) {
            if (!alarmTypeSet.contains(alarm.getPos())) {
                alarm.setChecked(false);
            }
        }
    }

    private Set<String> exchangeAlarmType(String[] arrPos, String[] queryAlarmTypes) {
        Set<String> alarmTypeSet = new HashSet<>();

        boolean isAccessFlag;
        for (String alarmType : queryAlarmTypes) {
            isAccessFlag = false;
            if (AlarmTypeUtil.temperatureAndHumidityBriefAlarmType.containsKey(alarmType)) {
                alarmTypeSet.add(AlarmTypeUtil.temperatureAndHumidityBriefAlarmType.get(alarmType));
                isAccessFlag = true;
            } else {
                for (String pos : arrPos) {
                    if (alarmType.startsWith(pos)) {
                        alarmTypeSet.add(pos);
                        isAccessFlag = true;
                        break;
                    }
                }
            }
            if (!isAccessFlag) {
                alarmTypeSet.add(alarmType);
            }
        }
        return alarmTypeSet;
    }

    public void setAlarmType(String alarmType, Set<String> alarmTypeSet, String[] arrPos, int index) {
        if (alarmType.startsWith(arrPos[index])) {
            alarmTypeSet.add(arrPos[index]);
        }
    }

    /**
     * 分页查询品牌
     */
    @ResponseBody
    @RequestMapping(value = "/alarmPageList", method = RequestMethod.POST)
    public PageGridBean alarmPageList(final AlarmPageReq alarmPageReq) {
        try {
            return alarmSearchService.alarmPageList(alarmPageReq);
        } catch (Exception e) {
            logger.error("分页查询报警异常", e);
            String message = e instanceof BusinessException ? ((BusinessException) e).getDetailMsg() : sysErrorMsg;
            return new PageGridBean(PageGridBean.FAULT, message);
        }
    }

    @RequestMapping(value = "/exportAlarmList", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean exportAlarmList(final AlarmPageReq alarmPageReq) {

        String alarmTypes = alarmPageReq.getAlarmTypes();
        String startTime = alarmPageReq.getAlarmStartTime();
        String endTime = alarmPageReq.getAlarmEndTime();
        if (StringUtils.isBlank(alarmTypes) || StringUtils.isBlank(startTime) || StringUtils.isBlank(endTime)) {
            return new JsonResultBean(JsonResultBean.FAULT, "参数错误!");
        }
        // 传感器报警类型转换
        alarmPageReq.setAlarmTypes(StringUtils.join(AlarmTypeUtil.typeList(alarmTypes), ","));

        return ControllerTemplate
            .addExportOffline(exportService, alarmPageReq.getAlarmRecordOffLineExport(), "导出报警列表异常");
    }

    /**
     * 修改单条状态
     */
    @ApiIgnore
    @RequestMapping(value = { "/findEndTime" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean findEndTime(String vehicleId, String type, String startTime) {
        try {
            String[] alarm = type.split(",");
            if (alarm.length != 1) {
                return new JsonResultBean(JsonResultBean.SUCCESS, "0");
            }
            long time = DateUtils.parseDate(startTime, DATE_FORMAT).getTime();
            String endTime = alarmSearchService.getLatestAlarmHandle(vehicleId, Integer.parseInt(alarm[0]), time);
            if ("0".equals(endTime)) {
                return new JsonResultBean(JsonResultBean.SUCCESS);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            logger.error("修改单条状态异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

    @ApiOperation(value = "报警处理页面弹出", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "vid", value = "车辆id")
    @RequestMapping(value = { "/alarmDeal" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean alarmDeal(String vid) {
        try {
            JSONObject msg = new JSONObject();
            String sim = "";
            String device = "";
            String deviceType = "";
            String assignmentName = "";
            BindDTO bindDTO = VehicleUtil.getBindInfoByRedis(vid);
            if (bindDTO != null) {
                sim = bindDTO.getSimCardNumber();
                device = bindDTO.getDeviceNumber();
                deviceType = bindDTO.getDeviceType();
                List<GroupDTO> currentUserGroupList = userService.getCurrentUserGroupList();
                Map<String, String> userGroupIdAndNameMap =
                    currentUserGroupList.stream().collect(Collectors.toMap(GroupDTO::getId, GroupDTO::getName));
                String groupIds = bindDTO.getGroupId();
                assignmentName = Arrays.stream(groupIds.split(","))
                    .map(userGroupIdAndNameMap::get)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.joining(","));
            }
            msg.put("sim", sim);
            msg.put("device", device);
            msg.put("type", deviceType);
            msg.put("assignmentName", assignmentName);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            logger.error("报警处理异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }

    }

    /**
     * 根据组织id或分组id统计其下所有监控对象数量
     * @param id   组织id或是分组id
     * @param type group/assignment 组织/分组
     */
    @ApiOperation(value = "根据组织id或分组id统计其下所有监控对象数量", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = { @ApiImplicitParam(name = "id", value = "组织id或是分组id"),
        @ApiImplicitParam(name = "type", value = "group/assignment 组织/分组") })
    @RequestMapping(value = { "/getMonitorNum" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getMonitorNum(String id, String type) {
        try {
            if (Objects.equals(type, "assignment")) {
                type = "group";
            } else if (Objects.equals(type, "group")) {
                type = "org";
            }
            Set<String> moIds = monitorService.getMonitorByGroupOrOrgDn(id, type, null);
            return new JsonResultBean(moIds.size());
        } catch (Exception e) {
            logger.error("根据组织id或分组id统计其下所有监控对象数量异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "根据组织id或分组id统计其下所有监控对象数量异常");
        }
    }

    /**
     * 模糊搜索，重组树结构
     */
    @ApiOperation(value = "模糊搜索，重组树结构", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = { @ApiImplicitParam(name = "type", value = "组织id或是分组id", required = true),
        @ApiImplicitParam(name = "queryParam", value = "查询条件", required = true),
        @ApiImplicitParam(name = "deviceType", value = "设备类型"),
        @ApiImplicitParam(name = "webType", value = "当前所属界面", defaultValue = "1", required = true) })
    @RequestMapping(value = "/monitorTreeFuzzy", method = RequestMethod.POST)
    @ResponseBody
    public String getVehicleTreeFuzzy(String type, String queryParam, String deviceType, Integer webType,
        String queryType) {
        try {
            MonitorTreeReq query = new MonitorTreeReq();
            query.setQueryType(queryType);
            query.setType(type);
            query.setKeyword(queryParam);
            if (StringUtils.isNotBlank(deviceType)) {
                query.setDeviceTypes(Collections.singletonList(deviceType));
            }
            query.setWebType(webType);
            query.setNeedCarousel(false);
            query.setMonitorType("monitor");
            query.setChecked(false);
            query.setNeedAccStatus(false);
            query.setNeedQuitPeople(false);
            String result = monitorTreeService.getMonitorTreeFuzzy(query).toJSONString();
            // 压缩数据
            result = ZipUtil.compress(result);
            return result;
        } catch (Exception e) {
            logger.error("模糊搜索车辆树信息异常", e);
            return null;
        }
    }

    /**
     * 添加监控对象在线状态和acc状态
     */
    @RequestMapping(value = "/new/monitorTreeFuzzy", method = RequestMethod.POST)
    @ResponseBody
    public String getVehicleTreeFuzzyNew(String type, String queryParam, String deviceType, Integer webType,
        String queryType) {
        try {
            MonitorTreeReq query = new MonitorTreeReq();
            query.setQueryType(queryType);
            query.setType(type);
            query.setKeyword(queryParam);

            if (Objects.equals("1", deviceType)) {
                query.setDeviceTypes(Arrays.asList(ProtocolEnum.REALTIME_VIDEO_DEVICE_TYPE));
            } else if (StringUtils.isNotBlank(deviceType) && !Objects.equals("1", deviceType)) {
                query.setDeviceTypes(Collections.singletonList(deviceType));
            }
            query.setWebType(webType);
            query.setNeedCarousel(false);
            query.setMonitorType("monitor");
            query.setChecked(false);
            query.setNeedAccStatus(true);
            query.setNeedQuitPeople(false);
            String result = monitorTreeService.getMonitorTreeFuzzy(query).toJSONString();
            // 压缩数据
            result = ZipUtil.compress(result);
            return result;
        } catch (Exception e) {
            logger.error("模糊搜索车辆树信息异常", e);
            return null;
        }
    }

    @RequestMapping(value = "/reportFuzzySearch", method = RequestMethod.POST)
    @ResponseBody
    public String reportFuzzySearch(String type, String queryParam, String queryType,
        @RequestParam(value = "treeType", required = false) String treeType) {
        try {
            String result = monitorTreeService.reportFuzzySearch(type, queryParam, queryType, treeType).toJSONString();
            // 压缩数据
            result = ZipUtil.compress(result);
            return result;
        } catch (Exception e) {
            logger.error("报表模糊查询异常", e);
            return null;
        }
    }

    /**
     * 模糊搜索，返回监控对象数量
     * @param queryParam 查询条件
     * @param deviceType 查询类型
     */
    @ApiOperation(value = "模糊搜索，返回监控对象数量", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = { @ApiImplicitParam(name = "queryParam", value = "查询条件", required = true),
        @ApiImplicitParam(name = "deviceType", value = "设备类型"),
        @ApiImplicitParam(name = "webType", value = "当前所属界面", defaultValue = "1", required = true) })
    @RequestMapping(value = "/monitorTreeFuzzyCount", method = RequestMethod.POST)
    @ResponseBody
    public int getMonitorTreeFuzzyCount(String queryParam, String queryType, String deviceType) {
        try {
            if (StringUtils.isBlank(queryType)) {
                queryType = "name";
            }
            MonitorTreeReq treeReq = new MonitorTreeReq();
            treeReq.setQueryType(queryType);
            treeReq.setKeyword(queryParam);
            treeReq.setMonitorType("monitor");
            if (StringUtils.isNotBlank(deviceType)) {
                treeReq.setDeviceTypes(treeReq.getDeviceTypes("monitor", deviceType, null, false));
            }
            return monitorTreeService.getMonitorTreeFuzzyCount(treeReq);
        } catch (Exception e) {
            logger.error("模糊搜索车辆树信息异常", e);
            return 0;
        }
    }

    /**
     * 组装报警类型名称树
     */
    private JSONObject installIoAlarmTypeTree(List<SwitchType> ioSwitchType) {
        JSONObject alarmTypeName = new JSONObject();
        JSONArray tree = new JSONArray();
        if (CollectionUtils.isNotEmpty(ioSwitchType)) {
            for (SwitchType switchType : ioSwitchType) {
                String identify = switchType.getIdentify();
                JSONObject parentNode = new JSONObject();
                parentNode.put("isParent", true);
                parentNode.put("name", switchType.getName());
                parentNode.put("id", identify);
                parentNode.put("pId", -1);
                parentNode.put("isCondition", false);
                JSONObject childNodeOne = new JSONObject();
                childNodeOne.put("isParent", false);
                childNodeOne.put("name", switchType.getStateOne());
                childNodeOne.put("id", identify + "1");
                childNodeOne.put("pId", identify);
                childNodeOne.put("isCondition", true);
                JSONObject childNodeTwo = new JSONObject();
                childNodeTwo.put("isParent", false);
                childNodeTwo.put("name", switchType.getStateTwo());
                childNodeTwo.put("id", identify + "2");
                childNodeTwo.put("pId", identify);
                childNodeTwo.put("isCondition", true);
                JSONObject abnormalIo = new JSONObject();
                abnormalIo.put("isParent", false);
                abnormalIo.put("name", switchType.getName() + "异常");
                abnormalIo.put("id", identify + "3");
                abnormalIo.put("pId", identify);
                abnormalIo.put("isCondition", true);
                tree.add(parentNode);
                tree.add(childNodeOne);
                tree.add(childNodeTwo);
                tree.add(abnormalIo);
            }
        }
        for (int i = 90; i <= 92; i++) {
            JSONObject sensor = new JSONObject();
            sensor.put("isParent", false);
            sensor.put("name", i == 90 ? "终端I/O异常" : (i == 91 ? "I/O采集1异常" : "I/O采集2异常"));
            sensor.put("id", "0x" + i);
            sensor.put("pId", -1);
            sensor.put("isCondition", true);
            tree.add(sensor);
        }
        alarmTypeName.put("tree", tree);
        return alarmTypeName;
    }

    /**
     * 批量报警处理
     */
    @RequestMapping(value = "/batch/handleAlarm", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean batchHandleAlarm(HandleMultiAlarms handleMultiAlarms) {
        try {
            handleMultiAlarms
                .setRecords(JSON.parseArray(handleMultiAlarms.getRecordsStr(), HandleMultiAlarms.Record.class));
            realTimeRiskService.batchHandleAlarm(handleMultiAlarms);
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            logger.error("批量报警处理异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "系统响应异常，请稍后再试或联系管理员！");
        }
    }

}

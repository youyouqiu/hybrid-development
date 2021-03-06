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
@Api(tags = { "????????????_dev" })
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
     * ??????????????????
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * ?????????id??????session???
     */
    @ApiOperation(value = "?????????id??????session???", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = { @ApiImplicitParam(name = "vehicleId", value = "??????id"),
        @ApiImplicitParam(name = "session", value = "http???session") })
    @RequestMapping(value = { "/addSession" }, method = RequestMethod.POST)
    @ResponseBody
    public boolean addSession(String vehicleId, HttpSession session) {
        if (StringUtils.isNotBlank(vehicleId)) {
            try {
                // ??????????????????vid??????session,
                session.setAttribute("vehicleIdAlram", vehicleId);
                return true;
            } catch (Exception e) {
                logger.error("addSession????????????", e);
            }
        }
        return false;
    }

    /**
     * ?????????????????????????????????????????????????????????
     */
    @ApiOperation(value = "ModelAndView_?????????????????????????????????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = { @ApiImplicitParam(name = "avid", value = "????????????id"),
        @ApiImplicitParam(name = "atype", value = "2?????????????????????,0????????????????????????????????????"),
        @ApiImplicitParam(name = "atime", value = "????????????????????????"),
        @ApiImplicitParam(name = "session", value = "http???session") })
    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public ModelAndView listPage(String avid, String atype, String atime, String alarmTypeArr) {
        try {
            // atyp:2?????????????????????,0????????????????????????????????????
            ModelAndView mv = new ModelAndView(LIST_PAGE);
            // ???????????????????????????
            List<AlarmType> alarms = alarmSearchService.getAlarmType();
            List<SwitchType> ioSwitchType = switchTypeService.getIoSwitchType();
            JSONObject alarmTypeName = installIoAlarmTypeTree(ioSwitchType);
            mv.addObject("alarmTypeName", alarmTypeName);
            // ?????????????????????, ????????????,???????????????alarmType????????????????????????
            if (StringUtils.isNotEmpty(alarmTypeArr)) {
                setAlarmTypeCheckedStatus(alarms, alarmTypeArr);
            }

            mv.addObject("type", JSON.toJSONString(alarms));
            if (StringUtil.isNotBlank(atype)) {
                if ("2".equals(atype)) {
                    Set<String> moIds = RedisHelper.getSet(HistoryRedisKeyEnum.UNHANDLED_VEHICLE.of());
                    atime = DateUtil.YMD_HMS.format(LocalDate.now().atStartOfDay()).orElseThrow(RuntimeException::new);
                    if (CollectionUtils.isNotEmpty(moIds)) {
                        // ???????????????????????????????????????5000???????????????
                        avid = moIds.stream().limit(5000).collect(Collectors.joining(","));
                    } else {
                        return mv;
                    }
                }
                // ??????????????????????????????????????????, ????????????????????????atime(????????????????????????)
                // ??????vid?????????????????????ID
                String assignIds = assignmentService.getAssignsByMonitorId(avid);
                mv.addObject("assignIds", assignIds);
                mv.addObject("atype", atype);
                mv.addObject("avid", avid);
                mv.addObject("atime", atime);
            }
            return mv;
        } catch (Exception e) {
            logger.error("???????????????????????????????????????????????????????????????", e);
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
     * ??????????????????
     */
    @ResponseBody
    @RequestMapping(value = "/alarmPageList", method = RequestMethod.POST)
    public PageGridBean alarmPageList(final AlarmPageReq alarmPageReq) {
        try {
            return alarmSearchService.alarmPageList(alarmPageReq);
        } catch (Exception e) {
            logger.error("????????????????????????", e);
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
            return new JsonResultBean(JsonResultBean.FAULT, "????????????!");
        }
        // ???????????????????????????
        alarmPageReq.setAlarmTypes(StringUtils.join(AlarmTypeUtil.typeList(alarmTypes), ","));

        return ControllerTemplate
            .addExportOffline(exportService, alarmPageReq.getAlarmRecordOffLineExport(), "????????????????????????");
    }

    /**
     * ??????????????????
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
            logger.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????????????????");
        }
    }

    @ApiOperation(value = "????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParam(name = "vid", value = "??????id")
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
            logger.error("??????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????????????????");
        }

    }

    /**
     * ????????????id?????????id????????????????????????????????????
     * @param id   ??????id????????????id
     * @param type group/assignment ??????/??????
     */
    @ApiOperation(value = "????????????id?????????id????????????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = { @ApiImplicitParam(name = "id", value = "??????id????????????id"),
        @ApiImplicitParam(name = "type", value = "group/assignment ??????/??????") })
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
            logger.error("????????????id?????????id??????????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "????????????id?????????id??????????????????????????????????????????");
        }
    }

    /**
     * ??????????????????????????????
     */
    @ApiOperation(value = "??????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = { @ApiImplicitParam(name = "type", value = "??????id????????????id", required = true),
        @ApiImplicitParam(name = "queryParam", value = "????????????", required = true),
        @ApiImplicitParam(name = "deviceType", value = "????????????"),
        @ApiImplicitParam(name = "webType", value = "??????????????????", defaultValue = "1", required = true) })
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
            // ????????????
            result = ZipUtil.compress(result);
            return result;
        } catch (Exception e) {
            logger.error("?????????????????????????????????", e);
            return null;
        }
    }

    /**
     * ?????????????????????????????????acc??????
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
            // ????????????
            result = ZipUtil.compress(result);
            return result;
        } catch (Exception e) {
            logger.error("?????????????????????????????????", e);
            return null;
        }
    }

    @RequestMapping(value = "/reportFuzzySearch", method = RequestMethod.POST)
    @ResponseBody
    public String reportFuzzySearch(String type, String queryParam, String queryType,
        @RequestParam(value = "treeType", required = false) String treeType) {
        try {
            String result = monitorTreeService.reportFuzzySearch(type, queryParam, queryType, treeType).toJSONString();
            // ????????????
            result = ZipUtil.compress(result);
            return result;
        } catch (Exception e) {
            logger.error("????????????????????????", e);
            return null;
        }
    }

    /**
     * ???????????????????????????????????????
     * @param queryParam ????????????
     * @param deviceType ????????????
     */
    @ApiOperation(value = "???????????????????????????????????????", authorizations = {
        @Authorization(value = com.zw.api.config.SwaggerConfig.securitySchemaOAuth2, scopes = {
            @AuthorizationScope(scope = "global", description = "des") }) })
    @ApiImplicitParams(value = { @ApiImplicitParam(name = "queryParam", value = "????????????", required = true),
        @ApiImplicitParam(name = "deviceType", value = "????????????"),
        @ApiImplicitParam(name = "webType", value = "??????????????????", defaultValue = "1", required = true) })
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
            logger.error("?????????????????????????????????", e);
            return 0;
        }
    }

    /**
     * ???????????????????????????
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
                abnormalIo.put("name", switchType.getName() + "??????");
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
            sensor.put("name", i == 90 ? "??????I/O??????" : (i == 91 ? "I/O??????1??????" : "I/O??????2??????"));
            sensor.put("id", "0x" + i);
            sensor.put("pId", -1);
            sensor.put("isCondition", true);
            tree.add(sensor);
        }
        alarmTypeName.put("tree", tree);
        return alarmTypeName;
    }

    /**
     * ??????????????????
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
            logger.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????????????????");
        }
    }

}

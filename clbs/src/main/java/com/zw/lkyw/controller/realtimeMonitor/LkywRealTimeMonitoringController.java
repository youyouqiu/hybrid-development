package com.zw.lkyw.controller.realtimeMonitor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zw.adas.utils.controller.AdasControllerTemplate;
import com.zw.lkyw.domain.LocationForLkyw;
import com.zw.lkyw.domain.SendMsgBasicInfo;
import com.zw.lkyw.domain.SendMsgDetail;
import com.zw.lkyw.domain.SendMsgMonitorInfo;
import com.zw.lkyw.service.realTimeMonitoring.LkywRealTimeMonitoringService;
import com.zw.lkyw.utils.sendMsgCache.SendMsgCache;
import com.zw.platform.basic.dto.query.MonitorTreeReq;
import com.zw.platform.basic.service.F3MessageService;
import com.zw.platform.basic.service.MonitorTreeService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.Auth;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.enmu.ProtocolEnum;
import com.zw.platform.service.realTimeVideo.RealTimeVideoService;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.ZipUtil;
import com.zw.platform.util.spring.InitData;
import com.zw.ws.entity.vehicle.VehiclePositionalInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/lkyw/v/monitoring")
public class LkywRealTimeMonitoringController {

    /**
     * log日志记录
     */
    private static Logger log = LogManager.getLogger(LkywRealTimeMonitoringController.class);

    private static final String INDEX_PAGE = "vas/lkyw/monitoring/realTimeMonitoring";

    private static final String SEND_TEXT_BY_BATCH_PAGE = "vas/lkyw/monitoring/batchSendTxtByRealTimeMonitoring";
    @Autowired
    private LkywRealTimeMonitoringService lkywRealTimeMonitoringService;
    @Autowired
    private SendMsgCache sendMsgCache;

    @Autowired
    private RealTimeVideoService realTimeVideoService;

    @Autowired
    private InitData initData;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Autowired
    private F3MessageService f3MessageService;

    @Autowired
    private UserService userService;

    @Autowired
    private MonitorTreeService monitorTreeService;

    @Value("${video.findlog.flag:true}")
    private boolean logFindFlag;

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public ModelAndView index(String id) {
        ModelAndView mv = new ModelAndView(INDEX_PAGE);
        mv.addObject("logFlag", logFindFlag);
        mv.addObject("jumpId", id);
        return mv;
    }

    /**
     * 获取实时监控-全部车辆列表信息
     * @param monitorIds 可视范围车辆id，逗号隔开（此参数为空时查询当前用户权限下所有监控对象状态信息）
     * @param userName   当前用户名
     */
    @RequestMapping(value = "/monitors", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getMonitorsLocation(String monitorIds, String userName) {
        try {
            Set<String> monitorIdSet;
            if (StringUtils.isBlank(monitorIds)) {
                monitorIdSet = userService.getMonitorIdsByUser(userName);
            } else {
                monitorIdSet = new HashSet<>();
                Collections.addAll(monitorIdSet, monitorIds.split(","));
            }
            // 此处通过调用新方法，实现中间结果（DTO列表）GC不可达，从而允许释放内存
            final String resultStr = this.getLkywCacheLocationStr(monitorIdSet);
            return new JsonResultBean(ZipUtil.compress(resultStr));
        } catch (Exception e) {
            e.printStackTrace();
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 单独抽离是因为本方法可能耗费大量内存，序列化并返回后使大对象立即可被回收
     */
    private String getLkywCacheLocationStr(Collection<String> monitorIds) {
        return JSON.toJSONString(f3MessageService.getLkywCacheLocation(monitorIds, false));
    }

    /**
     * 获取监控对象气泡信息
     * @param monitorId
     * @return
     */
    @RequestMapping(value = "/getMonitorBubble", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getMonitorBubble(String monitorId) {
        try {
            List<LocationForLkyw> locationForLkyws =
                f3MessageService.getLkywCacheLocation(Collections.singletonList(monitorId), true);
            LocationForLkyw result = locationForLkyws.isEmpty() ? null : locationForLkyws.get(0);
            return new JsonResultBean(result);
        } catch (Exception e) {
            e.printStackTrace();
            return new JsonResultBean(JsonResultBean.FAULT);
        }

    }

    /**
     * 获取地图打点位置信息
     * @param monitorIds 可视范围内监控对象ID
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/getVehiclePositional", method = RequestMethod.POST)
    public JsonResultBean getVehiclePositional(String monitorIds) {
        try {
            if (StringUtils.isNotBlank(monitorIds)) {
                initData.updateVehiclePositional(monitorIds);
            }
            final List<VehiclePositionalInfo> vehiclePosInfos =
                userService.getCurrentUserMonitorIds().stream().map(InitData.vehiclePositionalInfo::get)
                    .filter(Objects::nonNull).collect(Collectors.toList());
            return new JsonResultBean(vehiclePosInfos);
        } catch (Exception e) {
            log.error("获取车辆位置信息失败", e);
            return new JsonResultBean(JsonResultBean.FAULT, "获取车辆位置信息失败");
        }
    }

    @RequestMapping(value = { "/getUerReportMenu" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getUerReportMenu() {
        return AdasControllerTemplate
            .getResultBean(() -> lkywRealTimeMonitoringService.getUerReportMenu(), "查询用户两客一危报表菜单异常！");
    }

    @RequestMapping(value = { "/putMsg" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean putMsg(String monitorId, String monitorName, String groupName, String msgContent,
        Integer isUrgent, String playType, Integer sendType) {
        Long sendTime = System.currentTimeMillis();
        String sendUserName = SystemHelper.getCurrentUsername();
        return AdasControllerTemplate.getResultBean(() -> {
            SendMsgMonitorInfo monitorInfo = SendMsgMonitorInfo.getInstance(monitorId, monitorName, groupName);
            monitorInfo.assembelVehicleInfo(2, "其他车辆");
            SendMsgBasicInfo sendMsgBasicInfo =
                SendMsgBasicInfo.getInstance(msgContent, isUrgent, playType, sendType, sendUserName, sendTime);
            SendMsgDetail sendMsg = SendMsgDetail.getSendMsg(monitorInfo, sendMsgBasicInfo);
            sendMsgCache.putMsgCache(sendMsg);

        }, "查询用户两客一危报表菜单异常！");
    }

    @RequestMapping(value = { "/putStoreMsg" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean putStoreMsg(String monitorId, String monitorName, String groupName, String msgContent,
        Integer isUrgent, String playType, Integer sendType) {
        Long sendTime = System.currentTimeMillis();
        String sendUserName = SystemHelper.getCurrentUsername();
        return AdasControllerTemplate.getResultBean(() -> {
            SendMsgMonitorInfo monitorInfo = SendMsgMonitorInfo.getInstance(monitorId, monitorName, groupName);
            monitorInfo.assembelVehicleInfo(2, "其他车辆");
            SendMsgBasicInfo sendMsgBasicInfo =
                SendMsgBasicInfo.getInstance(msgContent, isUrgent, playType, sendType, sendUserName, sendTime);
            sendMsgBasicInfo.assembleSendResult(1, "终端离线");
            SendMsgDetail sendMsg = SendMsgDetail.getSendMsg(monitorInfo, sendMsgBasicInfo);
            sendMsgCache.putStoreCache(sendMsg);

        }, "查询用户两客一危报表菜单异常！");
    }

    @RequestMapping(value = { "/getSendMsgCacheInfo" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getSendMsgCacheInfo() {
        return AdasControllerTemplate.getResultBean(() -> sendMsgCache.getSendCacheInfo(), "查询用户两客一危报表菜单异常！");
    }

    /**
     * 批量下发文本页面(实时监控页面)
     */
    @RequestMapping(value = { "/getRealTimeMonitoringSendTextByBatchPage_{deviceType}" }, method = RequestMethod.GET)
    public ModelAndView getSendTextByBatchPage(@PathVariable("deviceType") String deviceType) {
        ModelAndView modelAndView = new ModelAndView(SEND_TEXT_BY_BATCH_PAGE);
        modelAndView.addObject("deviceType", deviceType);
        return modelAndView;
    }

    /**
     * 获得下发文本信息状态列表
     */
    @RequestMapping(value = { "/getSendTextStatusList" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getSendTextStatusList(String vehicleIds) {
        try {
            return realTimeVideoService.getSendTextStatusList(vehicleIds);
        } catch (Exception e) {
            log.error("获得下发文本信息状态列表异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 查询报警记录
     * @param latestTime         列表中最新的时间
     * @param latestAlarmDataStr 列表中报警时间为最新时间的报警数据(监控对象id|报警类型|报警时间(毫秒),监控对象id|报警类型|报警时间(毫秒))
     * @param oldestTime         列表中最老的时间
     * @param oldestAlarmDataStr 列表中报警时间为最老时间的报警数据(监控对象id|报警类型|报警时间(毫秒),监控对象id|报警类型|报警时间(毫秒))
     * @param alarmTypeStr       报警类型逗号分隔(不传就是查询所有需要展示的报警类型)
     * @param mark               页签标识
     */
    @RequestMapping(value = "/getTodayAlarmRecord", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getTodayAlarmRecord(String latestTime, String latestAlarmDataStr, String oldestTime,
        String oldestAlarmDataStr, String alarmTypeStr, String mark) {
        try {
            return lkywRealTimeMonitoringService
                .getTodayAlarmRecord(latestTime, latestAlarmDataStr, oldestTime, oldestAlarmDataStr, alarmTypeStr,
                    mark);
        } catch (Exception e) {
            log.error("查询报警记录异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 查询当天报警次数
     */
    @RequestMapping(value = "/getTodayAlarmQuantity", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getTodayAlarmQuantity() {
        try {
            return lkywRealTimeMonitoringService.getTodayAlarmQuantity();
        } catch (Exception e) {
            log.error("查询当天报警次数异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 进入页面初始化需要查询报警的监控对象id
     */
    @RequestMapping(value = "/initNeedQueryAlarmMonitorIds", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean initNeedQueryAlarmMonitorIds() {
        try {
            lkywRealTimeMonitoringService.initNeedQueryAlarmMonitorIds();
            return new JsonResultBean();
        } catch (Exception e) {
            log.error("初始化需要查询报警的监控对象id异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 根据组织id查询车辆（组装成树节点的格式）
     * @param groupId 企业ID
     * @return 分组-分组下的监控对象树集合Map
     */
    @RequestMapping(value = "/putMonitorByGroup", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean putMonitorByGroup(String groupId, boolean isChecked, String monitorType, String deviceType,
        Integer status) {
        try {
            MonitorTreeReq treeReq = new MonitorTreeReq();
            treeReq.setChecked(isChecked);
            treeReq.setMonitorType(monitorType);
            if (StringUtils.isNotBlank(deviceType) && Objects.equals(monitorType, "vehicle")) {
                List<String> deviceTypes;
                if (Objects.equals(deviceType, String.valueOf(ProtocolEnum.ONE))) {
                    deviceTypes = Arrays.asList(ProtocolEnum.PROTOCOL_TYPE_808_2011_2013_STR);
                } else {
                    deviceTypes = Arrays.asList(ProtocolEnum.PROTOCOL_TYPE_808_2019_STR);
                }
                treeReq.setDeviceTypes(deviceTypes);
            }
            treeReq.setStatus(status);
            JSONArray treeNodes = monitorTreeService.getByOrgDn(groupId, treeReq);
            return new JsonResultBean(getGroupMonitorTreeMap(treeNodes));
        } catch (Exception e) {
            log.error("获取车辆树信息异常", e);
            return null;
        }
    }

    private Map<String, JSONArray> getGroupMonitorTreeMap(JSONArray monitorTreeNodes) {
        Map<String, JSONArray> result = new HashMap<>(16);
        for (Object item : monitorTreeNodes) {
            JSONObject treeNode = (JSONObject) item;
            String assignmentId = treeNode.getString("pId");
            JSONArray groupMonitors = result.getOrDefault(treeNode.getString("pId"), new JSONArray());
            groupMonitors.add(treeNode);
            result.put(assignmentId, groupMonitors);
        }
        return result;
    }
}

package com.zw.platform.controller.monitoring;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.sx.platform.service.sxReportManagement.OffLineReportService;
import com.zw.adas.service.riskdisposerecord.AdasRiskService;
import com.zw.adas.utils.elasticsearch.AdasElasticSearchUtil;
import com.zw.lkyw.utils.sendMsgCache.SendMsgCache;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.Auth;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.form.ProfessionalsForm;
import com.zw.platform.domain.customenum.RecordCollectionEnum;
import com.zw.platform.domain.functionconfig.form.LineForm;
import com.zw.platform.domain.functionconfig.form.PolygonForm;
import com.zw.platform.domain.infoconfig.form.MonitorInfo;
import com.zw.platform.domain.multimedia.HandleAlarms;
import com.zw.platform.domain.multimedia.form.OrderForm;
import com.zw.platform.domain.netty.ServerParamList;
import com.zw.platform.domain.vas.alram.AlarmSetting;
import com.zw.platform.domain.vas.monitoring.form.T808_0x8202;
import com.zw.platform.push.factory.AlarmFactory;
import com.zw.platform.repository.vas.AlarmSettingDao;
import com.zw.platform.service.alarm.AlarmSearchService;
import com.zw.platform.service.alarm.AlarmSettingService;
import com.zw.platform.service.functionconfig.FenceConfigService;
import com.zw.platform.service.functionconfig.ManageFenceService;
import com.zw.platform.service.monitoring.OrderService;
import com.zw.platform.service.monitoring.RealTimeRiskService;
import com.zw.platform.service.monitoring.impl.RealTimeServiceImpl;
import com.zw.platform.service.multimedia.MultimediaService;
import com.zw.platform.service.oil.PositionalService;
import com.zw.platform.service.reportManagement.DrivingRecordReportService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.service.reportManagement.impl.LogSearchServiceImpl;
import com.zw.platform.service.sendTxt.SendTxtService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.Customer;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MonitorUtils;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.platform.util.privilege.UserPrivilegeUtil;
import com.zw.protocol.msg.Message;
import com.zw.protocol.msg.t808.T808Message;
import com.zw.protocol.msg.t808.body.LocationInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/v/monitoring")
public class RealTimeMonitoring {
    private static Logger log = LogManager.getLogger(RealTimeMonitoring.class);

    @Autowired
    RealTimeServiceImpl realTime;

    @Autowired
    MultimediaService multiService;

    @Autowired
    SendTxtService sendService;

    @Autowired
    UserService userService;

    @Autowired
    ManageFenceService manageFenceService;

    @Autowired
    AlarmSettingService alarmService;

    @Autowired
    AlarmSearchService alarmSearchService;

    @Autowired
    LogSearchServiceImpl logSearchServiceImpl;

    @Autowired
    LogSearchService logSearchService;

    @Autowired
    OrderService orderService;

    @Autowired
    AlarmSettingService alarmSettingService;

    @Autowired
    AlarmSettingDao alarmSettingDao;

    @Autowired
    FenceConfigService fenceConfigService;

    @Autowired
    OffLineReportService offLineReportService;

    @Autowired
    AlarmFactory alarmFactory;

    @Autowired
    private ServerParamList serverParamList;

    @Resource
    private HttpServletRequest request;

    @Autowired
    private RealTimeRiskService realTimeRiskService;

    @Autowired
    private DrivingRecordReportService drivingRecordReportService;

    @Autowired
    private AdasRiskService adasRiskService;

    @Autowired
    private AdasElasticSearchUtil adasElasticSearchUtil;

    @Autowired
    private SendMsgCache sendMsgCache;

    @Autowired
    private PositionalService positionalService;

    @Autowired
    private UserPrivilegeUtil userPrivilegeUtil;

    private static final String DATE_FORMAT2 = "yyyy-MM-dd HH:mm:ss";

    private static final String INDEX_PAGE = "vas/monitoring/realTimeMonitoring";

    private static final String IMPORT_PAGE = "vas/monitoring/import";

    private static final String SEND_TEXT_BY_BATCH_PAGE = "vas/monitoring/batchSendTxtByRealTimeMonitoring";

    @Value("${video.host}")
    private String videoHost;

    @Value("${video.port}")
    private String videoPort;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Value("${polygon.name.exist}")
    private String polygonNameExist;

    @Value("${video.findlog.flag:true}")
    private boolean logFindFlag;

    @Auth
    @RequestMapping(value = { "/realTimeMonitoring" }, method = RequestMethod.GET)
    public ModelAndView index(String id) {
        ModelAndView mv = new ModelAndView(INDEX_PAGE);
        mv.addObject("logFlag", logFindFlag);
        mv.addObject("jumpId", id);
        mv.addObject("videoHost", videoHost);
        mv.addObject("videoPort", videoPort);
        return mv;
    }

    @RequestMapping(value = { "/getAddress" }, method = RequestMethod.POST)
    @ResponseBody
    public String getAddress(String[] lnglatXYs) {

        if (lnglatXYs == null) {
            return "未定位";
        }
        if (lnglatXYs[1].equals("0.0") || lnglatXYs[1].equals("0") || lnglatXYs[1].equals("") || lnglatXYs[0]
            .equals("0.0") || lnglatXYs[0].equals("0") || lnglatXYs[0].equals("")) {
            return "未定位";
        }
        try {
            if (lnglatXYs[1] != null && lnglatXYs[0] != null && !lnglatXYs[1].equals("0.0") && !lnglatXYs[1].equals("0")
                && !lnglatXYs[1].equals("") && !lnglatXYs[0].equals("0") && !lnglatXYs[0].equals("0.0") && !lnglatXYs[0]
                .equals("") && lnglatXYs[1].length() >= 6 && lnglatXYs[0].length() >= 7) {
                String longitude = lnglatXYs[0].substring(0, 7);
                String latitude = lnglatXYs[1].substring(0, 6);
                return positionalService.getAddress(longitude, latitude);
            }
        } catch (Exception e) {
            log.error("获取单个逆地址编码异常", e);
        }
        return "未定位";
    }

    @RequestMapping(value = { "/setAddress" }, method = RequestMethod.POST)
    @ResponseBody
    @Deprecated
    public String setAddress(String addressNew) {
        return "";
    }

    @RequestMapping(value = { "/getAlarmParam" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getAlarmParam(String vehicleId, String alarm) {
        try {
            List<AlarmSetting> alarmSettings = alarmService.findAlarmSetting(vehicleId, alarm);
            return new JsonResultBean(alarmSettings);
        } catch (Exception e) {
            log.error("查询报警设置异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @RequestMapping(value = { "/photo" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean photo(@ModelAttribute("form") OrderForm form, String vid) {
        try {
            if (Objects.nonNull(form)) {
                // 拍照下发
                Customer c = new Customer();
                form.setSerialNumber(Integer.valueOf(c.getCustomerID()));
                if (form.getOrderType() == 1) {
                    if (MonitorUtils.isOnLine(vid)) {
                        orderService.takePhoto(form);
                    } else {
                        return new JsonResultBean(JsonResultBean.FAULT);
                    }
                }
                realTimeRiskService.sendHandleAlarmsAndPhoto(form);
            }
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("拍照下发异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @RequestMapping(value = { "/sendTxt" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean sendTxt(@ModelAttribute("form") OrderForm form, String vid) {
        try {
            // 短信下发
            Customer c = new Customer();
            form.setSerialNumber(Integer.valueOf(c.getCustomerID()));
            if (form.getOrderType() == 5) {
                if (MonitorUtils.isOnLine(vid)) {
                    orderService.sendTxt(form);
                } else {
                    sendMsgCache.putStoreCache(orderService.getSendMsgDetail(form));
                    return new JsonResultBean(JsonResultBean.FAULT);
                }
            }
            realTimeRiskService.sendHandleAlarmsAndPhoto(form);
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("短信下发异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @RequestMapping(value = { "/regionalQuery" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean regionalQuery() {
        try {
            Set<String> assignVehicle = userService.getCurrentUserMonitorIds();
            Map<String, List<String>> listAddressMap = new HashMap<>(16);
            Map<String, Message> locationMap = MonitorUtils.getLocationMap(assignVehicle);
            Set<String> allMoIds = new HashSet<>();
            for (Map.Entry<String, Message> entry : locationMap.entrySet()) {
                Message message = entry.getValue();
                if (Objects.isNull(message.getDesc().getMsgID())) {
                    continue;
                }
                T808Message t808Message = JSON.parseObject(message.getData().toString(), T808Message.class);
                LocationInfo locationInfo =
                    JSON.parseObject(t808Message.getMsgBody().toString(), LocationInfo.class);
                MonitorInfo monitorInfo = locationInfo.getMonitorInfo();
                if (Objects.isNull(monitorInfo)) {
                    continue;
                }
                String monitorId = monitorInfo.getMonitorId();
                allMoIds.add(monitorId);
                List<String> list = new ArrayList<>();
                list.add(monitorId);
                list.add(String.valueOf(locationInfo.getLatitude()));
                list.add(String.valueOf(locationInfo.getLongitude()));
                list.add(monitorInfo.getAssignmentName());
                list.add(monitorInfo.getMonitorName());
                String monitorType =
                    MonitorUtils.getEnNameByMonitorType(String.valueOf(monitorInfo.getMonitorType()));
                list.add(monitorType);
                listAddressMap.put(monitorId, list);
            }
            Map<String, BindDTO> bindInfoMap =
                VehicleUtil.batchGetBindInfosByRedis(allMoIds, Lists.newArrayList("groupId", "groupName"));
            Map<String, String> userGroupIdAndNameMap = userService.getCurrentGroupIdAndGroupName();
            for (Map.Entry<String, BindDTO> entry : bindInfoMap.entrySet()) {
                String monitorId = entry.getKey();
                BindDTO bindInfo = entry.getValue();
                if (StringUtils.isBlank(monitorId) || bindInfo == null) {
                    continue;
                }
                List<String> listAddress = listAddressMap.get(monitorId);
                if (CollectionUtils.isEmpty(listAddress)) {
                    continue;
                }
                String groupIds = bindInfo.getGroupId();
                String groupNames = Arrays.stream(groupIds.split(","))
                    .map(userGroupIdAndNameMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(","));
                listAddress.set(3, groupNames);
            }
            return new JsonResultBean(listAddressMap.values());
        } catch (Exception e) {
            log.error("区域查询异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 处理报警
     * @param handleAlarms
     * @return
     */
    @RequestMapping(value = { "/handleAlarm" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean handleAlarm(HandleAlarms handleAlarms) {
        try {
            if (handleAlarms != null) {
                if ("监听".equals(handleAlarms.getHandleType())) {
                    if (!MonitorUtils.isOnLine(handleAlarms.getVehicleId())) {
                        return new JsonResultBean(JsonResultBean.FAULT);
                    }

                }
                realTimeRiskService.saveCommonHandleAlarms(handleAlarms);

                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("报警状态存储异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @RequestMapping(value = { "/parametersTrace" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean parametersTrace(String[] parameters) {
        try {
            JSONObject msg = new JSONObject();
            T808_0x8202 ptf = new T808_0x8202();
            ptf.setInterval(Integer.valueOf(parameters[1]));
            ptf.setValidity(Integer.valueOf(parameters[2]));
            String msgSN = realTime.getParametersTrace(parameters[0], ptf);
            String[] vcehicle = logSearchServiceImpl.findCarMsg(parameters[0]);
            String brand = vcehicle[0];
            String plateColor = vcehicle[1];
            msg.put("msgSN", msgSN);
            String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
            String logMsg = "监控对象：" + brand + " 车辆跟踪";
            logSearchServiceImpl.addLog(ip, logMsg, "2", "MONITORING", brand, plateColor);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("车辆跟踪操作异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    public int getAlarmType(String alarmType) {
        if (alarmType.contains("出区域") || alarmType.contains("进区域")) {
            return 2012;
        }
        if (alarmType.contains("不足") || alarmType.contains("过长")) {
            return 22;
        }
        if (alarmType.contains("超速报警")) {
            return 1;
        }
        switch (alarmType) {
            case "紧急报警":
                return 0;
            case "超速报警":
                return 1;
            case "疲劳驾驶":
                return 2;
            case "危险预警":
                return 3;
            case "GNSS模块发生故障":
                return 4;
            case "GNSS天线未接或被剪断":
                return 5;
            case "GNSS天线短路":
                return 6;
            case "终端主电源欠压":
                return 7;
            case "终端主电源掉电":
                return 8;
            case "终端LCD或显示器故障":
                return 9;
            case "TTS模块故障":
                return 10;
            case "摄像头故障":
                return 11;
            case "道路运输证IC卡模块故障":
                return 12;
            case "超速预警":
                return 13;
            case "疲劳驾驶预警":
                return 14;
            case "当天累计驾驶超时":
                return 18;
            case "超时停车":
                return 19;
            case "进出区域":
                return 20;
            case "进出路线":
                return 21;
            case "路段行驶时间不足/过长":
                return 22;
            case "路线偏离报警":
                return 23;
            case "车辆VSS故障":
                return 24;
            case "车辆油量异常":
                return 25;
            case "车辆被盗":
                return 26;
            case "车辆非法点火":
                return 27;
            case "车辆非法位移":
                return 28;
            case "碰撞预警":
                return 29;
            case "侧翻预警":
                return 30;
            case "非法开门报警":
                return 31;
            case "SOS报警":
                return 32;
            default:
                return 1000;
        }
    }

    /**
     * 获取车牌
     * @param vehicleId
     * @return
     * @throws BusinessException
     * @throws ParseException
     */
    @RequestMapping(value = { "/getBrandParameter" }, method = RequestMethod.POST)
    @ResponseBody
    public String getBrandParameter(String vehicleId) {
        try {
            String brand = "";
            BindDTO bindDTO = MonitorUtils.getBindDTO(vehicleId, "name");
            if (bindDTO != null) {
                brand = bindDTO.getName();
            }
            return brand;
        } catch (Exception e) {
            log.error("获取车牌异常", e);
            return null;
        }
    }

    @RequestMapping(value = { "/orderMsg" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean orderMsg(@ModelAttribute("form") OrderForm form) throws Exception {
        try {
            if (!MonitorUtils.isOnLine(form.getVid())) {
                return new JsonResultBean(false, "终端设备离线");
            }
            boolean re = false;
            Customer c = new Customer();
            form.setSerialNumber(Integer.valueOf(c.getCustomerID()));
            Map<String, Object> relt = new HashMap<String, Object>();
            String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
            String[] vehicle = logSearchService.findCarMsg(form.getVid());
            String brand = vehicle[0];// 车牌号

            StringBuilder message = new StringBuilder();
            message.append("监控对象 : ").append(brand);
            int sign = 0;
            Integer orderType = form.getOrderType();
            switch (orderType) {
                case 1: // 拍照
                    re = orderService.takePhoto(form);
                    message.append(" 车辆拍照");
                    break;
                case 2: // 录像
                    re = orderService.getVideo(form);
                    message.append(" 车辆录像");
                    break;
                case 30:// 定时汇报
                    re = orderService.regularReports(form);
                    message.append(" 车辆定时回报");
                    break;
                case 31:// 定距汇报
                    re = orderService.regularReports(form);
                    message.append(" 车辆定距回报");
                    break;
                case 32:// 定时定距
                    re = orderService.regularReports(form);
                    message.append(" 车辆定时定距回报");
                    break;
                case 40:// 电话回拨
                    re = orderService.telBack(form);
                    message.append(" 电话回拨");
                    break;
                case 41:// 监听
                    re = orderService.telBack(form);
                    message.append(" 车辆监听");
                    break;
                case 5:// 文本信息
                    re = orderService.sendTxt(form);
                    message.append(" 文本信息下发");
                    break;
                case 6:// 提问下发
                    re = orderService.sendQuestion(form);
                    message.append(" 提问下发");
                    break;
                case 7:// 终端控制
                    re = orderService.terminalControl(form);
                    message.append(" 终端复位");
                    break;
                case 8:// 车辆控制
                    re = orderService.vehicleControl(form);
                    if (form.getSign().equals(1)) {
                        message.append(" 车辆加锁");
                    } else {
                        message.append(" 车辆解锁");
                    }
                    break;
                case 9:// 设置超速
                    re = orderService.updateSpeedMax(form);
                    message.append(" 设置超速");
                    break;
                case 10:// 行驶记录数据采集
                    re = orderService.recordCollection(form);
                    // 行驶记录仪采集表增增加采集记录
                    drivingRecordReportService
                        .addDrivingRecordInfo(form.getVid(), form.getCommandSign(), form.getSerialNumber());
                    String signContent = RecordCollectionEnum.getSignContentBy(String.valueOf(form.getCommandSign()));
                    message.append(" ").append(signContent);
                    break;
                case 11:// 多媒体检索
                    re = orderService.multimediaRetrieval(form);
                    message.append(" 多媒体检索");
                    break;
                case 12:// 多媒体上传
                    re = orderService.multimediaUpload(form);
                    message.append(" 多媒体上传");
                    break;
                case 13:// 录音上传
                    re = orderService.record(form);
                    message.append(" 录音上传");
                    break;
                case 14:// 数据下行透传
                    if (StringUtils.isEmpty(form.getParam())) {
                        return new JsonResultBean(false, "原始命令类型不能为空");
                    }
                    if (StringUtils.isEmpty(form.getData())) {
                        return new JsonResultBean(false, "原始命令内容不能为空");
                    }
                    re = orderService.originalOrder(form);
                    message.append(" 发送原始命令");
                    break;
                case 15:// 查询终端参数
                    re = orderService.terminalParameters(form);
                    message.append(" 查询终端参数");
                    break;
                case 16:// 信息服务
                    re = orderService.informationService(form);
                    message.append(" 信息服务");
                    break;
                case 17:// 行驶记录参数下传
                    re = orderService.recordSend(form);
                    message.append(" 行驶记录参数下传");
                    break;
                case 18:
                    re = orderService.updateTerminalPlate(form, ip);
                    message.append(" 修改终端车牌号为：").append(form.getBrand());
                    break;
                case 19:
                    re = orderService.sendOBDParam(form, ip);
                    message.append(" 设置OBD车型信息");
                    break;
                case 42: // 断油电功能
                    re = orderService.oilElectric(form);
                    message.append(" 油电控制");
                    break;
                case 49:
                    re = orderService.sendLindCheck(form.getVid());
                    message.append("链路检测");
                    break;
                case 50:
                    re = orderService.sendFenceQuery(form, ip);
                    message.append("围栏查询");
                    break;
                case 51:
                    re = orderService.sendDriverActiveReport(form, ip);
                    message.append("驾驶员主动上报");
                    break;
                default:
                    sign = 1;
                    break;
            }
            relt.put("type", re);
            if (sign == 0) {
                relt.put("serialNumber", form.getSerialNumber());
                logSearchService.addLog(ip, message.toString(), "3", "MONITORING", brand, vehicle[1]);
            }
            return new JsonResultBean(relt);
        } catch (Exception e) {
            log.error("下发指令异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 查看历史轨迹日志
     * @param vehicleId
     * @throws BusinessException
     */
    @RequestMapping(value = { "/trackPlayBackLog" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean trackPlayBackLog(String vehicleId, String type) {
        try {
            String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
            // 点击车的日志
            String groupName = "";
            String monitoring = "";
            String message = "";
            String plateColor = "";
            String brand = "";
            BindDTO bindDTO = MonitorUtils.getBindDTO(vehicleId);
            if (bindDTO != null) {
                brand = bindDTO.getName();
                plateColor = bindDTO.getPlateColor() == null ? null : String.valueOf(bindDTO.getPlateColor());
                groupName = bindDTO.getOrgName();
                monitoring = brand + "@(" + groupName + ")";
                message = "监控对象:(" + monitoring + ")查看历史轨迹记录";
                logSearchService.addLog(ip, message, "3", "MONITORING", brand, plateColor);
            }
            // 记录查看轨迹的日志
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("查看历史轨迹日志异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 根据车辆id查询超速参数的参数值
     * @param vehicleId
     * @return
     * @throws Exception
     */
    @RequestMapping(value = { "/findSpeedParameter" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean findSpeedParameter(String vehicleId) {
        try {
            List<AlarmSetting> list = alarmSearchService.findSpeedParameter(vehicleId);
            return new JsonResultBean(list);
        } catch (Exception e) {
            log.error("查询超速参数的参数值异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
         * @return String
     * @throws BusinessException
     * @throws @author           yangyi
     * @Title: 导入
     */
    @RequestMapping(value = { "/import" }, method = RequestMethod.GET)
    public ModelAndView importPage(@RequestParam("pid") final String pid) throws BusinessException {
        ModelAndView mav = new ModelAndView(IMPORT_PAGE);
        if (!pid.equals("")) {
            mav.addObject("pid", pid);
        }
        return mav;
    }

    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    public final JsonResultBean importType(@RequestParam(value = "file", required = false) MultipartFile file,
        HttpServletRequest request) {
        try {
            String name = request.getParameter("name");
            String type = request.getParameter("type");
            String excursion = request.getParameter("excursion");
            List<LineForm> listLine = new ArrayList<LineForm>();
            List<PolygonForm> listPolygon = new ArrayList<PolygonForm>();
            if (type.equals("zw_m_line")) {
                listLine = manageFenceService.findLineByName(name);
            } else if (type.equals("zw_m_polygon")) {
                listPolygon = manageFenceService.findPolygonByName(name);
            }
            if (listLine.size() != 0 || listPolygon.size() != 0) {
                return new JsonResultBean(JsonResultBean.FAULT, polygonNameExist);
            } else {
                Map resultMap = manageFenceService.addCoordinates(file, name, type, excursion);
                String msg = "导入结果：" + "<br/>" + resultMap.get("resultInfo") + "<br/>" + resultMap.get("errorMsg");
                return new JsonResultBean(true, msg);
            }
        } catch (Exception e) {
            log.error("导入信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 获取当前系统时间
     * @return time
     * @throws BusinessException
     */
    @RequestMapping(value = { "/getTime" }, method = RequestMethod.POST)
    @ResponseBody
    public String getTime() throws BusinessException {
        Date date = new Date();
        String time = DateFormatUtils.format(date, DATE_FORMAT2);
        return time;
    }

    /**
     * 通过车辆ID获取终端类型
     * @return time
     * @throws BusinessException
     */
    @RequestMapping(value = { "/getDeviceTypeByVid" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getDeviceTypeByVid(String vehicleId) {
        JSONObject result = new JSONObject();
        result.put("warningType", false);
        try {
            BindDTO bindDTO = MonitorUtils.getBindDTO(vehicleId, "deviceType");
            if (Objects.nonNull(bindDTO)) {
                String deviceType = bindDTO.getDeviceType();
                result.put("deviceType", deviceType);
                if ("10".equals(deviceType) || "9".equals(deviceType) || "5".equals(deviceType)) {
                    result.put("warningType", true);
                }
            }
        } catch (Exception e) {
            log.error("获取终端类型异常", e);
        }
        return new JsonResultBean(result);

    }

    /**
     * 根据围栏下发id和监控对象id从缓存中取电子围栏信息
     */
    @RequestMapping(value = { "/getFenceInfo" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean findFenceInfo(String vehicleId, int sendDownId) {
        try {
            JSONObject msg = null;
            if (vehicleId != null && !"".equals(vehicleId) && sendDownId != 0) {
                msg = realTime.getFenceInfoBySendId(sendDownId, vehicleId);
            }
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("根据围栏下发id和监控对象id从缓存中取围栏信息", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 查询host
     * @return
     */
    @RequestMapping(value = { "/getHost" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getHost() {
        try {
            JSONObject obj = new JSONObject();
            String host = serverParamList.getAccessServerAddress();
            obj.put("host", host);
            return new JsonResultBean(obj);
        } catch (Exception e) {
            log.error("获取host失败", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 查询OBD车型信息
     * @return
     */
    @RequestMapping(value = "/findOBD", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean findOBD() {
        try {
            return orderService.findOBD();
        } catch (Exception e) {
            log.error("查询OBD车型信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 检测用户在视频监控中是否有处理报警的权限
     * @return
     */
    @RequestMapping(value = "/checkAlarmRole", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean checkAlarmRole() {
        HttpSession session = SystemHelper.getSession();
        if (session.getAttribute("permissions") != null) {
            List<String> menuUrls = (List<String>) (session.getAttribute("permissions"));
            //报警页面地址
            if (menuUrls.contains("/a/search/list")) {
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    // 获取发生风险的车的从业人员
    @RequestMapping(value = { "/getRiskProfessionalsInfo" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getRiskProfessionalsInfo(String vehicleId) {
        try {
            if (vehicleId != null) {
                List<ProfessionalsForm> professionalsForms = realTimeRiskService.getRiskProfessionalsInfo(vehicleId);
                if (professionalsForms != null) {
                    return new JsonResultBean(professionalsForms);
                }
            }
            return new JsonResultBean(false);
        } catch (Exception e) {
            log.error("获取从业人员信息失败!", e);
            return new JsonResultBean(false);
        }
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

    @RequestMapping(value = { "/getRiskSize" }, method = RequestMethod.POST)
    @ResponseBody
    public long getRiskSize() {
        try {
            return adasElasticSearchUtil.getRiskSizeByVehicleIds(userPrivilegeUtil.getCurrentUserVehicles());
        } catch (Exception e) {
            log.error("获取当前用户未处理主动安全风险数异常", e);
            return 0;
        }
    }

    @RequestMapping(value = { "/setTreeShow" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean setTreeShow(int aliasesFlag, int showTreeCountFlag) {
        try {
            return realTimeRiskService.setTreeShow(aliasesFlag, showTreeCountFlag);
        } catch (Exception e) {
            log.error("设置实时监控树显示设置异常！", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

}

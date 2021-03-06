package com.zw.platform.controller.connectionparamsset_809;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zw.platform.basic.domain.VehicleDO;
import com.zw.platform.basic.repository.NewVehicleDao;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.connectionparamsset_809.PlantParam;
import com.zw.platform.domain.connectionparamsset_809.PlantParamQuery;
import com.zw.platform.domain.connectionparamsset_809.T809AlarmSetting;
import com.zw.platform.domain.connectionparamsset_809.T809PlantFormCheck;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.netty.ServerParamList;
import com.zw.platform.domain.vas.alram.AlarmType;
import com.zw.platform.service.connectionparamsset_809.ConnectionParamsSetService;
import com.zw.platform.service.core.UserService;
import com.zw.platform.service.protocol.ProtocolUtilService;
import com.zw.platform.service.reportManagement.SuperPlatformMsgService;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.MsgUtil;
import com.zw.platform.util.common.AvoidRepeatSubmitToken;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.protocol.msg.Message;
import com.zw.protocol.msg.t809.body.AgingPwdUpData;
import com.zw.protocol.msg.t809.body.FromLinkLogout;
import com.zw.protocol.msg.t809.body.MainLogout;
import com.zw.protocol.msg.t809.body.MainVehicleInfo;
import com.zw.protocol.msg.t809.body.module.ExchangeVehicleReq;
import com.zw.protocol.msg.t809.body.module.ExtendPlatformMsgInfo;
import com.zw.protocol.msg.t809.body.module.MainModule;
import com.zw.protocol.msg.t809.body.module.PlatformAlarmInfo;
import com.zw.protocol.msg.t809.body.module.PlatformExchangeInformation;
import com.zw.protocol.msg.t809.body.module.PlatformMsgAckInfo;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * ??????????????????
 * @author Tdz
 **/
@Controller
@RequestMapping("/m/connectionparamsset")
public class ConnectionParamsSetController {

    private static final Logger log = LogManager.getLogger(ConnectionParamsSetController.class);

    private static final String LIST_PAGE = "modules/connectionparamsset/list";

    private static final String ADD_PAGE = "modules/connectionparamsset/add";

    private static final String EDIT_PAGE = "modules/connectionparamsset/edit";

    private static final String DETAIL_PAGE = "modules/connectionparamsset/detail";

    private static final String ERROR_PAGE = "html/errors/error_exception";

    /**
     * ??????????????????
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Autowired
    private ServerParamList serverParamList;

    @Autowired
    private ConnectionParamsSetService connectionParamsSetService;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Autowired
    private NewVehicleDao newVehicleDao;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private SuperPlatformMsgService superPlatformMsgService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProtocolUtilService protocolUtilService;

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean list(PlantParamQuery plantParamQuery) {
        try {
            List<PlantParam> plantParam = connectionParamsSetService.get809ConnectionParamsSet(plantParamQuery);
            return new JsonResultBean(plantParam);
        } catch (Exception e) {
            log.error("809????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @RequestMapping(value = { "/protocolList" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean findProtocolListByType(Integer type) {
        try {
            return new JsonResultBean(protocolUtilService.findProtocolListByType(type));
        } catch (Exception e) {
            log.error("??????808/809??????????????????map??????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????????????????????????????
     * @return JsonResultBean j
     */
    @RequestMapping(value = { "/getActiveSafetyProtocol" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean getActiveSafetyProtocol() {
        try {
            return new JsonResultBean(protocolUtilService.findActiveSafetyProtocolList());
        } catch (Exception e) {
            log.error("????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????????????????????????????
     * @param centerId ???????????????
     * @param id       ????????????id
     */
    @RequestMapping(value = { "/check809CenterIdUnique" }, method = RequestMethod.POST)
    @ResponseBody
    public Boolean check809CenterIdUnique(Integer centerId, String id) {
        try {
            return connectionParamsSetService.check809CenterIdUnique(centerId, id);
        } catch (Exception e) {
            log.error("????????????????????????????????????", e);
            return JsonResultBean.FAULT;
        }
    }

    /**
     * ?????????????????????
     * @param centerId ???????????????
     * @param ip ????????????id
     * @param ipBranch ?????????IP
     * @param id ?????????IP
     * @return JsonResultBean
     */
    @RequestMapping(value = { "/check809Unique" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean check809Unique(Integer centerId, String ip, String ipBranch, String id) {
        try {
            return connectionParamsSetService.check809Unique(centerId, ip, ipBranch, id);
        } catch (Exception e) {
            log.error("???????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ???????????????????????????
     * @param platformName ????????????
     */
    @RequestMapping(value = { "/checkPlatformNameUnique" }, method = RequestMethod.POST)
    @ResponseBody
    public Boolean checkPlatformNameUnique(String platformName, String id) {
        try {
            return connectionParamsSetService.checkPlatformNameUnique(platformName, id);
        } catch (Exception e) {
            log.error("?????????????????????????????????", e);
            return JsonResultBean.FAULT;
        }
    }

    /**
     * ??????809????????????????????????
     * @author hujun
     */
    @Auth
    @RequestMapping(value = { "/add" }, method = RequestMethod.GET)
    public String addPage() {
        return ADD_PAGE;
    }

    /**
     * ??????809????????????
     * @author hujun
     */
    @RequestMapping(value = { "/add" }, method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean add809PlantParam(final HttpServletRequest request, PlantParam plantParam) {
        try {
            // ????????????ip
            String ipAddress = new GetIpAddr().getIpAddr(request);
            boolean flag = connectionParamsSetService.save809ConnectionParamsSet(plantParam, ipAddress);
            if (flag) {
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        } catch (Exception e) {
            log.error("??????809??????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * ??????809??????????????????
     * @author hujun
     */
    @Auth
    @RequestMapping(value = { "/edit_{id}" }, method = RequestMethod.GET)
    public ModelAndView editPage(PlantParamQuery plantParamQuery) {
        try {
            return getPage(plantParamQuery, EDIT_PAGE);
        } catch (Exception e) {
            log.error("809????????????????????????????????????", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * ??????809????????????
     * @author hujun
     */
    @RequestMapping(value = { "/save" }, method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean savePage(final PlantParam param, final HttpServletRequest request) {
        try {
            // ????????????ip
            String ipAddress = new GetIpAddr().getIpAddr(request);
            boolean flag = connectionParamsSetService.checkedPlatFormCanOperate(param.getId());
            if (flag) { //??????????????????????????????????????????
                flag = connectionParamsSetService.update809ConnectionParamsSet(param, ipAddress);
                if (flag) {
                    return new JsonResultBean(JsonResultBean.SUCCESS);
                }
                return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT, "??????????????????????????????????????????????????????");
            }
        } catch (Exception e) {
            log.error("??????809??????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 809????????????????????????
     * @author hujun
     */
    @Auth
    @RequestMapping(value = { "/detail_{id}" }, method = RequestMethod.GET)
    public ModelAndView detailPage(PlantParamQuery plantParamQuery) {
        try {
            return getPage(plantParamQuery, DETAIL_PAGE);
        } catch (Exception e) {
            log.error("809????????????????????????????????????", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * ??????809????????????
     * @author hujun
     */
    @RequestMapping(value = { "/delete" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete809PlantParam(HttpServletRequest request, String id) {
        try {
            // ????????????ip
            String ipAddress = new GetIpAddr().getIpAddr(request);
            boolean flag = connectionParamsSetService.checkedPlatFormCanOperate(id);
            if (flag) {
                return connectionParamsSetService.delete809ConnectionParamsSet(id, ipAddress);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT, "??????????????????????????????????????????????????????");
            }
        } catch (Exception e) {
            log.error("??????809??????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @RequestMapping(value = { "/handle" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean handle(int serverCommand, String platformId) {
        try {
            if (StringUtils.isEmpty(platformId)) {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
            switch (serverCommand) {
                case 0: //????????????
                    close(getParam(platformId));
                    break;
                case 1: //????????????
                    connect(getParam(platformId));
                    break;
                case 2: //???????????????
                    login(getParam(platformId));
                    break;
                case 4: //???????????????
                    mainLogout(platformId);
                    break;
                case 5: //???????????????
                    fromLinkLogout(platformId);
                    break;
                case 6: //??????????????????
                    agingPwdUpData(platformId);
                    break;
                //????????????
                case 7:
                    closeDataFilter(getParam(platformId));
                    break;
                //????????????
                case 8:
                    openDataFilter(getParam(platformId));
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            log.error("809??????????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * ??????????????????????????????
     */
    @RequestMapping(value = { "/vehicleHandle" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean vehicleHandle(PlatformExchangeInformation info) {
        try {
            switch (info.getServerCommand()) {
                // ??????????????????????????????
                case 1:
                    upExgMsgStaticInfo(info, ConstantUtil.T809_UP_EXG_MSG_APPLY_FOR_MONITOR_STARTUP);
                    break;
                // ??????????????????????????????
                case 2:
                    upVehicleInfoTo809(null, 0, ConstantUtil.T809_UP_EXG_MSG_APPLY_FOR_MONITOR_END, info);
                    break;
                // ????????????????????????
                case 3:
                    upExgMsgStaticInfo(info, ConstantUtil.T809_UP_EXG_MSG_APPLY_HISGNSSDATA_REQ);
                    break;
                // ????????????????????????
                case 8:
                    upVehicleInfoTo809(null, 5, ConstantUtil.T809_UP_EXG_MSG_HISTORY_LOCATION, info);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            log.error("809??????????????????????????????????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    @RequestMapping(value = { "/platformMsgAck" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean platformMsgAck(PlatformMsgAckInfo platformMsgAckInfo, HttpServletRequest request) {
        try {
            if (StringUtils.isEmpty(platformMsgAckInfo.getServerIp())) { //????????????ip??????????????????????????????
                return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
            }
            // ????????????ip
            String ipAddress = new GetIpAddr().getIpAddr(request);
            // ?????????????????????????????????????????????
            connectionParamsSetService.sendPlatformMsgAck(platformMsgAckInfo, ipAddress);
        } catch (Exception e) {
            log.error("809??????????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    @RequestMapping(value = { "/platformAlarmAck" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean platformAlarmAck(PlatformAlarmInfo info, HttpServletRequest request) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            JSONObject msg = new JSONObject();
            superPlatformMsgService.updatePastData(); // ?????????????????????????????????
            Integer result = connectionParamsSetService.sendFormAlarmAck(info, ipAddress);
            msg.put("handleStatus", result);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("??????809????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @RequestMapping(value = { "/extendPlatformAlarmAck" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean extendAlarmHandleAck(ExtendPlatformMsgInfo info, HttpServletRequest request) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            JSONObject msg = new JSONObject();
            superPlatformMsgService.updatePastData(); // ?????????????????????????????????
            Integer result = connectionParamsSetService.sendExtendHandleAck(info, ipAddress);
            msg.put("handleStatus", result);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("????????????809????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @RequestMapping(value = { "/platformGangAck" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean platformGangAck(PlatformMsgAckInfo info, HttpServletRequest request) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            JSONObject msg = new JSONObject();
            superPlatformMsgService.updatePastData(); // ?????????????????????????????????
            Integer result = connectionParamsSetService.sendPlatformGangAck(info, ipAddress);
            msg.put("handleStatus", result);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    private ModelAndView getPage(PlantParamQuery plantParamQuery, String editPage) throws Exception {
        ModelAndView mav = new ModelAndView(editPage);
        if (!StringUtils.isEmpty(plantParamQuery.getId())) {
            PlantParam param = connectionParamsSetService.get809ConnectionParamsForEdit(plantParamQuery);
            if (param == null) {
                return new ModelAndView(ERROR_PAGE);
            }
            mav.addObject("result", param);
        }
        return mav;
    }

    /**
     * ??????????????????????????????
     */
    private void upExgMsgStaticInfo(PlatformExchangeInformation info, int t809Id) throws Exception {
        /* ???????????????????????????????????? */
        ExchangeVehicleReq req = new ExchangeVehicleReq();
        Date startTime = DateUtils.parseDate(info.getStartTime(), DATE_FORMAT);
        Date endTime = DateUtils.parseDate(info.getEndTime(), DATE_FORMAT);
        req.setStartTime(startTime.getTime() / 1000);
        req.setEndTime(endTime.getTime() / 1000);
        /* ???????????? */
        upVehicleInfoTo809(req, 16, t809Id, info);
    }

    /**
     * ??????????????????????????????????????????????????????
     */
    private void upVehicleInfoTo809(MainModule mainModule, Integer len, Integer t809Id,
        PlatformExchangeInformation peInfo) {
        /* ?????????????????????????????? */
        MainVehicleInfo info = getVehicleInfo(mainModule, len, t809Id, peInfo.getBrand());
        if (Objects.isNull(info)) {
            return;
        }
        /* ??????809????????????message */
        int id = ConstantUtil.T809_UP_EXG_MSG;
        Message message = MsgUtil.getMsg(id, MsgUtil.getT809Message(id, peInfo.getIp(), peInfo.getCenterId(), info))
            .assembleDesc809(peInfo.getPlatFormId());
        /* ???????????? */
        WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(message);
    }

    /**
     * ???????????????
     */
    private void mainLogout(String platformId) {
        MainLogout logout = new MainLogout();
        PlantParam param = getParam(platformId);
        logout.setUserID(Integer.parseInt(param.getUserName()));
        logout.setPassword(param.getPassword());
        /* ??????809????????????message */
        Message message = MsgUtil.getMsg(ConstantUtil.T809_UP_DISCONNECT_REQ,
            MsgUtil.getT809Message(ConstantUtil.T809_UP_DISCONNECT_REQ, param.getIp(), param.getCenterId(), logout))
            .assembleDesc809(param.getId());
        WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(message);
    }

    /**
     * ???????????????
     */
    private void fromLinkLogout(String platformId) {
        FromLinkLogout logout = new FromLinkLogout();
        PlantParam param = getParam(platformId);
        /* ??????809????????????message */
        Message message = MsgUtil.getMsg(ConstantUtil.T809_UP_CLOSELINK_INFORM,
            MsgUtil.getT809Message(ConstantUtil.T809_UP_CLOSELINK_INFORM, param.getIp(), param.getCenterId(), logout))
            .assembleDesc809(param.getId());
        WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(message);
    }

    /**
     * ????????????
     */
    private void agingPwdUpData(String platformId) {
        PlantParamQuery plantParamQuery = new PlantParamQuery();
        plantParamQuery.setId(platformId);
        AgingPwdUpData agingPwdUpData = new AgingPwdUpData();
        PlantParam param = connectionParamsSetService.get809ConnectionParamsSet(plantParamQuery).get(0);
        /* ???????????????????????? */
        agingPwdUpData.setAuthorizeCode1(param.getAuthorizeCode1());
        agingPwdUpData.setAuthorizeCode2(param.getAuthorizeCode2());
        agingPwdUpData.setDataType(ConstantUtil.T809_UP_AUTHORIZE_MSG_STARTUP);
        agingPwdUpData.setPlatformId(param.getPlatformId());
        /* ??????809????????????message */
        Message message = MsgUtil.getMsg(ConstantUtil.T809_UP_AUTHORIZE_MSG, MsgUtil
            .getT809Message(ConstantUtil.T809_UP_AUTHORIZE_MSG, param.getIp(), param.getCenterId(), agingPwdUpData))
            .assembleDesc809(param.getId());
        WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(message);
    }

    private MainVehicleInfo getVehicleInfo(MainModule mainModule, Integer len, Integer id, String brand) {
        VehicleDO vehicleDO = newVehicleDao.findByBrandAndPlateColor(brand, null);
        if (Objects.isNull(vehicleDO)) {
            return null;
        }
        Integer color = vehicleDO.getPlateColor();
        if (color == null) {
            color = 2;
        }
        MainVehicleInfo info = new MainVehicleInfo();
        info.setVehicleNo(brand);
        info.setVehicleColor(color);
        info.setDataType(id);
        info.setDataLength(len);
        if (mainModule != null) {
            info.setData(MsgUtil.objToJson(mainModule));
        }
        return info;
    }

    /**
     * ????????????
     */
    private void connect(PlantParam param) {
        /* ??????809????????????message */
        Message message = MsgUtil.getMsg(ConstantUtil.WEB_809_CHECK_START,
            MsgUtil.getT809Message(ConstantUtil.WEB_809_CHECK_START, param.getIp(), param.getCenterId(), param))
            .assembleDesc809(param.getId());
        WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(message);
    }

    /**
     * ????????????
     */
    private void close(PlantParam plantParam) {
        /* ??????809????????????message */
        Message message = MsgUtil.getMsg(ConstantUtil.WEB_809_CHECK_END,
            MsgUtil.getT809Message(ConstantUtil.WEB_809_CHECK_END, plantParam.getIp(), plantParam.getCenterId(), null))
            .assembleDesc809(plantParam.getId());
        WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(message);
        connectionParamsSetService.cancelRemindByGroupId(plantParam.getGroupId());
    }

    /**
     * ????????????
     */
    private void openDataFilter(PlantParam param) {
        // ??????????????????0:?????? 1:??????
        int dataFilterStatus = 1;
        Message message =
            MsgUtil.getMsg(ConstantUtil.WEB_809_INVALID_FILTER, dataFilterStatus).assembleDesc809(param.getId());
        WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(message);
    }

    /**
     * ????????????
     */
    private void closeDataFilter(PlantParam param) {
        // ??????????????????0:?????? 1:??????
        int dataFilterStatus = 0;
        Message message =
            MsgUtil.getMsg(ConstantUtil.WEB_809_INVALID_FILTER, dataFilterStatus).assembleDesc809(param.getId());
        WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(message);
    }

    /**
     * ???????????????
     */
    private void login(PlantParam plantParam) {
        /* ??????809????????????message */
        Message message = MsgUtil.getMsg(ConstantUtil.T809_UP_CONNECT_REQ, MsgUtil
            .getT809Message(ConstantUtil.T809_UP_CONNECT_REQ, plantParam.getIp(), plantParam.getCenterId(), null))
            .assembleDesc809(plantParam.getId());
        WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(message);
    }

    /**
     * ??????????????????????????????
     */
    private PlantParam getParam(String platformId) {
        PlantParamQuery plantParamQuery = new PlantParamQuery();
        plantParamQuery.setId(platformId);
        List<PlantParam> plantParam = connectionParamsSetService.get809ConnectionParamsSet(plantParamQuery);
        return plantParam.get(0);
    }

    /**
     * ??????????????????????????????
     * @param platFormName ????????????
     * @param pid          ??????id
     * @author hujun
     */
    @RequestMapping(value = { "/check809PlatFormSole" }, method = RequestMethod.POST)
    @ResponseBody
    public boolean check809PlatFormSole(String platFormName, String pid) {
        try {
            return connectionParamsSetService.check809PlatFormSole(platFormName, pid);
        } catch (Exception e) {
            log.error("????????????????????????????????????", e);
            return false;
        }
    }

    /**
     * ?????????????????????????????????????????????
     * @param protocolType ????????????
     * @param pid          ??????id
     * @author hujun
     */
    @RequestMapping(value = { "/check809ProtocolType" }, method = RequestMethod.POST)
    @ResponseBody
    public boolean check809ProtocolType(String protocolType, String pid) {
        try {
            return connectionParamsSetService.check809ProtocolType(protocolType, pid);
        } catch (Exception e) {
            log.error("???????????????????????????????????????????????????", e);
            return false;
        }
    }

    /**
     * ???????????????ip????????????ip????????????
     * @param param ??????????????????
     * @author hujun
     */
    @RequestMapping(value = { "/check809Ip" }, method = RequestMethod.POST)
    @ResponseBody
    public boolean check809Ip(T809PlantFormCheck param) {
        try {
            return connectionParamsSetService.check809Ip(param);
        } catch (Exception e) {
            log.error("???????????????ip????????????ip??????????????????", e);
            return false;
        }
    }

    /**
     * ????????????????????????????????????????????????ip???groupId???centerId???????????????
     * @param param ??????????????????
     */
    @RequestMapping(value = { "/check809DateSole" }, method = RequestMethod.POST)
    @ResponseBody
    public boolean check809DateSole(T809PlantFormCheck param) {
        try {
            return connectionParamsSetService.check809DateSole(param);
        } catch (Exception e) {
            log.error("????????????????????????????????????", e);
            return false;
        }
    }

    /**
     * ??????????????????
     */
    @RequestMapping(value = { "/findPlantParamById" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean findPlantParamById(String id) {
        try {
            if (!StringUtils.isEmpty(id)) {
                PlantParamQuery plantParamQuery = new PlantParamQuery();
                plantParamQuery.setId(id);
                PlantParam plantParam = connectionParamsSetService.get809ConnectionParamsForEdit(plantParamQuery);
                if (plantParam != null) {
                    return new JsonResultBean(plantParam);
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * ????????????
     */
    @RequestMapping(value = "/setAlarm", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean setAlarm(T809AlarmSetting t809AlarmSetting) {
        try {
            if (t809AlarmSetting != null && t809AlarmSetting.getProtocolType() != null && StringUtils
                .isNotEmpty(t809AlarmSetting.getSettingId())) {
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return connectionParamsSetService.add809AlarmMapping(t809AlarmSetting, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("809????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "809????????????????????????");
        }
    }

    /**
     * ??????809??????id?????????????????????809????????????
     */
    @RequestMapping(value = "/get809AlarmMapping", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean get809AlarmMapping(T809AlarmSetting t809AlarmSetting) {
        try {
            if (t809AlarmSetting != null && StringUtils.isNotEmpty(t809AlarmSetting.getSettingId())
                && t809AlarmSetting.getProtocolType() != null) {
                Map<String, String> result = connectionParamsSetService.get809AlarmMapping(t809AlarmSetting);
                return new JsonResultBean(result);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("??????809??????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT, "??????809??????????????????");
        }
    }

    /**
     * ??????808????????????
     */
    @RequestMapping(value = "/getAlarmType", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getAlarmType(Integer protocolType) {
        try {
            List<AlarmType> alarms = connectionParamsSetService.getAlarmType(protocolType);// ???????????????????????????
            return new JsonResultBean(JSON.toJSONString(alarms));
        } catch (Exception e) {
            log.error("??????808??????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * ????????????id???????????????????????????????????????
     */
    @RequestMapping(value = "/checkGroup", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean checkGroupIsItLegal(String groupId) {
        try {
            if (StringUtils.isNotBlank(groupId)) {
                OrganizationLdap organizationLdap = userService.getOrgByUuid(groupId);
                if (organizationLdap != null) {
                    String license = organizationLdap.getLicense();
                    if (StringUtils.isNotBlank(license)) {
                        return new JsonResultBean(JsonResultBean.SUCCESS);
                    }
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("????????????id?????????????????????????????????????????????", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }
}

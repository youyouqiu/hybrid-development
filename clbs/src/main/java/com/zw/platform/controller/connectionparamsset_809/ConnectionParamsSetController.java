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
 * 连接参数设置
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
     * 日期转换格式
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
            log.error("809连接参数查询异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @RequestMapping(value = { "/protocolList" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean findProtocolListByType(Integer type) {
        try {
            return new JsonResultBean(protocolUtilService.findProtocolListByType(type));
        } catch (Exception e) {
            log.error("根据808/809类型获取协议map异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 获取主动安全协议类型
     * @return JsonResultBean j
     */
    @RequestMapping(value = { "/getActiveSafetyProtocol" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean getActiveSafetyProtocol() {
        try {
            return new JsonResultBean(protocolUtilService.findActiveSafetyProtocolList());
        } catch (Exception e) {
            log.error("获取主动安全协议类型异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 验证平台接入码为一性
     * @param centerId 平台接入码
     * @param id       平台设置id
     */
    @RequestMapping(value = { "/check809CenterIdUnique" }, method = RequestMethod.POST)
    @ResponseBody
    public Boolean check809CenterIdUnique(Integer centerId, String id) {
        try {
            return connectionParamsSetService.check809CenterIdUnique(centerId, id);
        } catch (Exception e) {
            log.error("验证平台接入码为一性异常", e);
            return JsonResultBean.FAULT;
        }
    }

    /**
     * 验证平台唯一性
     * @param centerId 平台接入码
     * @param ip 平台设置id
     * @param ipBranch 从链路IP
     * @param id 主链路IP
     * @return JsonResultBean
     */
    @RequestMapping(value = { "/check809Unique" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean check809Unique(Integer centerId, String ip, String ipBranch, String id) {
        try {
            return connectionParamsSetService.check809Unique(centerId, ip, ipBranch, id);
        } catch (Exception e) {
            log.error("验证平台唯一性异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 验证平台名称为一性
     * @param platformName 平台名称
     */
    @RequestMapping(value = { "/checkPlatformNameUnique" }, method = RequestMethod.POST)
    @ResponseBody
    public Boolean checkPlatformNameUnique(String platformName, String id) {
        try {
            return connectionParamsSetService.checkPlatformNameUnique(platformName, id);
        } catch (Exception e) {
            log.error("验证平台名称唯一性异常", e);
            return JsonResultBean.FAULT;
        }
    }

    /**
     * 新增809连接参数弹出界面
     * @author hujun
     */
    @Auth
    @RequestMapping(value = { "/add" }, method = RequestMethod.GET)
    public String addPage() {
        return ADD_PAGE;
    }

    /**
     * 新增809连接参数
     * @author hujun
     */
    @RequestMapping(value = { "/add" }, method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean add809PlantParam(final HttpServletRequest request, PlantParam plantParam) {
        try {
            // 获得访问ip
            String ipAddress = new GetIpAddr().getIpAddr(request);
            boolean flag = connectionParamsSetService.save809ConnectionParamsSet(plantParam, ipAddress);
            if (flag) {
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        } catch (Exception e) {
            log.error("保存809连接参数异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 修改809参数弹出界面
     * @author hujun
     */
    @Auth
    @RequestMapping(value = { "/edit_{id}" }, method = RequestMethod.GET)
    public ModelAndView editPage(PlantParamQuery plantParamQuery) {
        try {
            return getPage(plantParamQuery, EDIT_PAGE);
        } catch (Exception e) {
            log.error("809修改界面连接参数查询异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 修改809连接参数
     * @author hujun
     */
    @RequestMapping(value = { "/save" }, method = RequestMethod.POST)
    @ResponseBody
    @AvoidRepeatSubmitToken(removeToken = true)
    public JsonResultBean savePage(final PlantParam param, final HttpServletRequest request) {
        try {
            // 获得访问ip
            String ipAddress = new GetIpAddr().getIpAddr(request);
            boolean flag = connectionParamsSetService.checkedPlatFormCanOperate(param.getId());
            if (flag) { //如果平台不允许操作则返回提示
                flag = connectionParamsSetService.update809ConnectionParamsSet(param, ipAddress);
                if (flag) {
                    return new JsonResultBean(JsonResultBean.SUCCESS);
                }
                return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT, "该平台已启动，请先关闭所有服务再试！");
            }
        } catch (Exception e) {
            log.error("修改809连接参数异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 809连接参数详情界面
     * @author hujun
     */
    @Auth
    @RequestMapping(value = { "/detail_{id}" }, method = RequestMethod.GET)
    public ModelAndView detailPage(PlantParamQuery plantParamQuery) {
        try {
            return getPage(plantParamQuery, DETAIL_PAGE);
        } catch (Exception e) {
            log.error("809详情界面连接参数查询异常", e);
            return new ModelAndView(ERROR_PAGE);
        }
    }

    /**
     * 删除809连接参数
     * @author hujun
     */
    @RequestMapping(value = { "/delete" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean delete809PlantParam(HttpServletRequest request, String id) {
        try {
            // 获得访问ip
            String ipAddress = new GetIpAddr().getIpAddr(request);
            boolean flag = connectionParamsSetService.checkedPlatFormCanOperate(id);
            if (flag) {
                return connectionParamsSetService.delete809ConnectionParamsSet(id, ipAddress);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT, "该平台已启动，请先关闭所有服务再试！");
            }
        } catch (Exception e) {
            log.error("删除809连接参数异常", e);
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
                case 0: //关闭服务
                    close(getParam(platformId));
                    break;
                case 1: //开启服务
                    connect(getParam(platformId));
                    break;
                case 2: //主链路连接
                    login(getParam(platformId));
                    break;
                case 4: //主链路注销
                    mainLogout(platformId);
                    break;
                case 5: //从链路断开
                    fromLinkLogout(platformId);
                    break;
                case 6: //发送时效口令
                    agingPwdUpData(platformId);
                    break;
                //关闭过滤
                case 7:
                    closeDataFilter(getParam(platformId));
                    break;
                //开启过滤
                case 8:
                    openDataFilter(getParam(platformId));
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            log.error("809连接参数设置页面连接操作异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 申请交换指定车辆信息
     */
    @RequestMapping(value = { "/vehicleHandle" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean vehicleHandle(PlatformExchangeInformation info) {
        try {
            switch (info.getServerCommand()) {
                // 申请交换指定车辆信息
                case 1:
                    upExgMsgStaticInfo(info, ConstantUtil.T809_UP_EXG_MSG_APPLY_FOR_MONITOR_STARTUP);
                    break;
                // 取消交换指定车辆信息
                case 2:
                    upVehicleInfoTo809(null, 0, ConstantUtil.T809_UP_EXG_MSG_APPLY_FOR_MONITOR_END, info);
                    break;
                // 补发车辆定位信息
                case 3:
                    upExgMsgStaticInfo(info, ConstantUtil.T809_UP_EXG_MSG_APPLY_HISGNSSDATA_REQ);
                    break;
                // 补发终端位置信息
                case 8:
                    upVehicleInfoTo809(null, 5, ConstantUtil.T809_UP_EXG_MSG_HISTORY_LOCATION, info);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            log.error("809连接参数设置页面申请交换指定车辆定位信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    @RequestMapping(value = { "/platformMsgAck" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean platformMsgAck(PlatformMsgAckInfo platformMsgAckInfo, HttpServletRequest request) {
        try {
            if (StringUtils.isEmpty(platformMsgAckInfo.getServerIp())) { //若服务器ip地址为空，则返回失败
                return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
            }
            // 获得访问ip
            String ipAddress = new GetIpAddr().getIpAddr(request);
            // 处理平台查岗、平台间报文并下发
            connectionParamsSetService.sendPlatformMsgAck(platformMsgAckInfo, ipAddress);
        } catch (Exception e) {
            log.error("809连接参数设置页面设置应答异常", e);
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
            superPlatformMsgService.updatePastData(); // 更新上级消息处理表状态
            Integer result = connectionParamsSetService.sendFormAlarmAck(info, ipAddress);
            msg.put("handleStatus", result);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("标准809报警督办处理异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @RequestMapping(value = { "/extendPlatformAlarmAck" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean extendAlarmHandleAck(ExtendPlatformMsgInfo info, HttpServletRequest request) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            JSONObject msg = new JSONObject();
            superPlatformMsgService.updatePastData(); // 更新上级消息处理表状态
            Integer result = connectionParamsSetService.sendExtendHandleAck(info, ipAddress);
            msg.put("handleStatus", result);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("西藏扩展809督办消息处理异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    @RequestMapping(value = { "/platformGangAck" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean platformGangAck(PlatformMsgAckInfo info, HttpServletRequest request) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            JSONObject msg = new JSONObject();
            superPlatformMsgService.updatePastData(); // 更新上级消息处理表状态
            Integer result = connectionParamsSetService.sendPlatformGangAck(info, ipAddress);
            msg.put("handleStatus", result);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("查岗消息处理异常", e);
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
     * 申请交换指定车辆信息
     */
    private void upExgMsgStaticInfo(PlatformExchangeInformation info, int t809Id) throws Exception {
        /* 组装车辆交换信息时间参数 */
        ExchangeVehicleReq req = new ExchangeVehicleReq();
        Date startTime = DateUtils.parseDate(info.getStartTime(), DATE_FORMAT);
        Date endTime = DateUtils.parseDate(info.getEndTime(), DATE_FORMAT);
        req.setStartTime(startTime.getTime() / 1000);
        req.setEndTime(endTime.getTime() / 1000);
        /* 下发信息 */
        upVehicleInfoTo809(req, 16, t809Id, info);
    }

    /**
     * 申请交换指定车辆定位信息统一下发方法
     */
    private void upVehicleInfoTo809(MainModule mainModule, Integer len, Integer t809Id,
        PlatformExchangeInformation peInfo) {
        /* 组装车辆交换信息实体 */
        MainVehicleInfo info = getVehicleInfo(mainModule, len, t809Id, peInfo.getBrand());
        if (Objects.isNull(info)) {
            return;
        }
        /* 组装809消息下发message */
        int id = ConstantUtil.T809_UP_EXG_MSG;
        Message message = MsgUtil.getMsg(id, MsgUtil.getT809Message(id, peInfo.getIp(), peInfo.getCenterId(), info))
            .assembleDesc809(peInfo.getPlatFormId());
        /* 信息下发 */
        WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(message);
    }

    /**
     * 主链路注销
     */
    private void mainLogout(String platformId) {
        MainLogout logout = new MainLogout();
        PlantParam param = getParam(platformId);
        logout.setUserID(Integer.parseInt(param.getUserName()));
        logout.setPassword(param.getPassword());
        /* 组装809消息下发message */
        Message message = MsgUtil.getMsg(ConstantUtil.T809_UP_DISCONNECT_REQ,
            MsgUtil.getT809Message(ConstantUtil.T809_UP_DISCONNECT_REQ, param.getIp(), param.getCenterId(), logout))
            .assembleDesc809(param.getId());
        WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(message);
    }

    /**
     * 从链路断开
     */
    private void fromLinkLogout(String platformId) {
        FromLinkLogout logout = new FromLinkLogout();
        PlantParam param = getParam(platformId);
        /* 组装809消息下发message */
        Message message = MsgUtil.getMsg(ConstantUtil.T809_UP_CLOSELINK_INFORM,
            MsgUtil.getT809Message(ConstantUtil.T809_UP_CLOSELINK_INFORM, param.getIp(), param.getCenterId(), logout))
            .assembleDesc809(param.getId());
        WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(message);
    }

    /**
     * 时效口令
     */
    private void agingPwdUpData(String platformId) {
        PlantParamQuery plantParamQuery = new PlantParamQuery();
        plantParamQuery.setId(platformId);
        AgingPwdUpData agingPwdUpData = new AgingPwdUpData();
        PlantParam param = connectionParamsSetService.get809ConnectionParamsSet(plantParamQuery).get(0);
        /* 组装时效口令参数 */
        agingPwdUpData.setAuthorizeCode1(param.getAuthorizeCode1());
        agingPwdUpData.setAuthorizeCode2(param.getAuthorizeCode2());
        agingPwdUpData.setDataType(ConstantUtil.T809_UP_AUTHORIZE_MSG_STARTUP);
        agingPwdUpData.setPlatformId(param.getPlatformId());
        /* 组装809消息下发message */
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
     * 开启服务
     */
    private void connect(PlantParam param) {
        /* 组装809消息下发message */
        Message message = MsgUtil.getMsg(ConstantUtil.WEB_809_CHECK_START,
            MsgUtil.getT809Message(ConstantUtil.WEB_809_CHECK_START, param.getIp(), param.getCenterId(), param))
            .assembleDesc809(param.getId());
        WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(message);
    }

    /**
     * 关闭服务
     */
    private void close(PlantParam plantParam) {
        /* 组装809消息下发message */
        Message message = MsgUtil.getMsg(ConstantUtil.WEB_809_CHECK_END,
            MsgUtil.getT809Message(ConstantUtil.WEB_809_CHECK_END, plantParam.getIp(), plantParam.getCenterId(), null))
            .assembleDesc809(plantParam.getId());
        WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(message);
        connectionParamsSetService.cancelRemindByGroupId(plantParam.getGroupId());
    }

    /**
     * 开启过滤
     */
    private void openDataFilter(PlantParam param) {
        // 是否开启过滤0:关闭 1:开启
        int dataFilterStatus = 1;
        Message message =
            MsgUtil.getMsg(ConstantUtil.WEB_809_INVALID_FILTER, dataFilterStatus).assembleDesc809(param.getId());
        WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(message);
    }

    /**
     * 关闭过滤
     */
    private void closeDataFilter(PlantParam param) {
        // 是否开启过滤0:关闭 1:开启
        int dataFilterStatus = 0;
        Message message =
            MsgUtil.getMsg(ConstantUtil.WEB_809_INVALID_FILTER, dataFilterStatus).assembleDesc809(param.getId());
        WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(message);
    }

    /**
     * 主链路连接
     */
    private void login(PlantParam plantParam) {
        /* 组装809消息下发message */
        Message message = MsgUtil.getMsg(ConstantUtil.T809_UP_CONNECT_REQ, MsgUtil
            .getT809Message(ConstantUtil.T809_UP_CONNECT_REQ, plantParam.getIp(), plantParam.getCenterId(), null))
            .assembleDesc809(plantParam.getId());
        WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(message);
    }

    /**
     * 获取指定平台设置参数
     */
    private PlantParam getParam(String platformId) {
        PlantParamQuery plantParamQuery = new PlantParamQuery();
        plantParamQuery.setId(platformId);
        List<PlantParam> plantParam = connectionParamsSetService.get809ConnectionParamsSet(plantParamQuery);
        return plantParam.get(0);
    }

    /**
     * 校验平台名称是否唯一
     * @param platFormName 平台名称
     * @param pid          平台id
     * @author hujun
     */
    @RequestMapping(value = { "/check809PlatFormSole" }, method = RequestMethod.POST)
    @ResponseBody
    public boolean check809PlatFormSole(String platFormName, String pid) {
        try {
            return connectionParamsSetService.check809PlatFormSole(platFormName, pid);
        } catch (Exception e) {
            log.error("校验平台名称是否唯一异常", e);
            return false;
        }
    }

    /**
     * 校验协议类型下是否已经存在平台
     * @param protocolType 协议类型
     * @param pid          平台id
     * @author hujun
     */
    @RequestMapping(value = { "/check809ProtocolType" }, method = RequestMethod.POST)
    @ResponseBody
    public boolean check809ProtocolType(String protocolType, String pid) {
        try {
            return connectionParamsSetService.check809ProtocolType(protocolType, pid);
        } catch (Exception e) {
            log.error("校验协议类型下是否已经存在平台异常", e);
            return false;
        }
    }

    /**
     * 校验主链路ip和从链路ip有无重复
     * @param param 校验参数实体
     * @author hujun
     */
    @RequestMapping(value = { "/check809Ip" }, method = RequestMethod.POST)
    @ResponseBody
    public boolean check809Ip(T809PlantFormCheck param) {
        try {
            return connectionParamsSetService.check809Ip(param);
        } catch (Exception e) {
            log.error("校验主链路ip和从链路ip有无重复异常", e);
            return false;
        }
    }

    /**
     * 校验平台数据是否唯一（相同主链路ip下groupId及centerId不能重复）
     * @param param 校验参数实体
     */
    @RequestMapping(value = { "/check809DateSole" }, method = RequestMethod.POST)
    @ResponseBody
    public boolean check809DateSole(T809PlantFormCheck param) {
        try {
            return connectionParamsSetService.check809DateSole(param);
        } catch (Exception e) {
            log.error("校验平台数据是否唯一异常", e);
            return false;
        }
    }

    /**
     * 获取平台信息
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
            log.error("获取平台信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 报警设置
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
            log.error("809转发报警设置异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "809转发报警设置异常");
        }
    }

    /**
     * 根据809设置id和协议类型查询809报警映射
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
            log.error("查询809报警映射异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "查询809报警映射异常");
        }
    }

    /**
     * 获取808报警类型
     */
    @RequestMapping(value = "/getAlarmType", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getAlarmType(Integer protocolType) {
        try {
            List<AlarmType> alarms = connectionParamsSetService.getAlarmType(protocolType);// 查询所需的报警类型
            return new JsonResultBean(JSON.toJSONString(alarms));
        } catch (Exception e) {
            log.error("获取808报警类型异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 根据组织id检查组织是否有经营许可证号
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
            log.error("根据组织id检查组织是否有经营许可证号异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }
}

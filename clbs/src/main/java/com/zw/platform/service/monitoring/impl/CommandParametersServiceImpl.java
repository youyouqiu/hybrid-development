package com.zw.platform.service.monitoring.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.tobato.fastdfs.domain.conn.FdfsWebServer;
import com.google.common.collect.Lists;
import com.zw.adas.utils.FastDFSClient;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.GroupDTO;
import com.zw.platform.basic.repository.GroupDao;
import com.zw.platform.basic.repository.NewConfigDao;
import com.zw.platform.basic.service.IpAddressService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.core.SendParam;
import com.zw.platform.domain.param.CameraParam;
import com.zw.platform.domain.param.CommunicationParam;
import com.zw.platform.domain.param.DeviceConnectServerParam;
import com.zw.platform.domain.param.DeviceParam;
import com.zw.platform.domain.param.EventSetParam;
import com.zw.platform.domain.param.GNSSParam;
import com.zw.platform.domain.param.InformationParam;
import com.zw.platform.domain.param.PhoneBookParam;
import com.zw.platform.domain.param.PhoneParam;
import com.zw.platform.domain.param.PositionParam;
import com.zw.platform.domain.param.SerialPortParam;
import com.zw.platform.domain.param.SingleMediaSearchUp;
import com.zw.platform.domain.param.StationParam;
import com.zw.platform.domain.param.WirelessUpdateParam;
import com.zw.platform.domain.param.form.CommandParametersForm;
import com.zw.platform.domain.sendTxt.DeviceCommand;
import com.zw.platform.domain.systems.DeviceUpgrade;
import com.zw.platform.domain.systems.Directive;
import com.zw.platform.domain.systems.form.DirectiveForm;
import com.zw.platform.domain.taskjob.TaskJobForm;
import com.zw.platform.domain.vas.monitoring.MonitorCommandBindForm;
import com.zw.platform.domain.vas.monitoring.query.CommandParametersQuery;
import com.zw.platform.push.cache.ParamSendingCache;
import com.zw.platform.push.cache.SendModule;
import com.zw.platform.push.cache.SendTarget;
import com.zw.platform.push.controller.SubscibeInfo;
import com.zw.platform.push.controller.SubscibeInfoCache;
import com.zw.platform.push.handler.device.DeviceHelper;
import com.zw.platform.repository.modules.CommandParametersDao;
import com.zw.platform.repository.modules.ParameterDao;
import com.zw.platform.repository.vas.DeviceUpgradeDao;
import com.zw.platform.repository.vas.RealTimeCommandDao;
import com.zw.platform.service.core.F3SendStatusProcessService;
import com.zw.platform.service.monitoring.CommandParametersService;
import com.zw.platform.service.monitoring.RealTimeCommandService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.service.sendTxt.SendTxtService;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.MsgUtil;
import com.zw.platform.util.QuartzManager;
import com.zw.platform.util.SendHelper;
import com.zw.platform.util.StrUtil;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.RedisQueryUtil;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.protocol.msg.t808.T808Message;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import com.zw.protocol.util.ProtocolTypeUtil;
import com.zw.ws.entity.t808.simcard.T808Msg8106;
import com.zw.ws.impl.WsElectronicDefenceService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


/**
 * @author Administrator
 */
@Service
public class CommandParametersServiceImpl implements CommandParametersService, IpAddressService {

    public static final Logger logger = LogManager.getLogger(CommandParametersServiceImpl.class);

    @Autowired
    private LogSearchService logSearchService;

    @Autowired
    private CommandParametersDao commandParametersDao;

    @Autowired
    private ParameterDao parameterDao;

    @Autowired
    private RealTimeCommandService commandService;

    @Autowired
    private RealTimeCommandDao realTimeCommandDao;

    @Autowired
    private SendTxtService sendTxtService;

    @Autowired
    private SendHelper sendHelper;

    @Autowired
    private F3SendStatusProcessService f3SendStatusProcessService;

    @Autowired
    private WsElectronicDefenceService wsElectronicDefenceService;

    @Autowired
    private DeviceUpgradeDao deviceUpgradeDao;

    @Autowired
    private FastDFSClient fastDFSClient;

    @Autowired
    private FdfsWebServer fdfsWebServer;

    @Autowired
    private QuartzManager quartzManager;

    @Autowired
    private ParamSendingCache paramSendingCache;

    @Autowired
    private UserService userService;

    @Autowired
    private NewConfigDao newConfigDao;

    @Autowired
    private GroupDao groupDao;

    private static final String job_name = "upgradeTaskJob";

    private static final String bean_class = "com.zw.platform.task.UpgradeTaskJob";

    @Override
    public Page<CommandParametersForm> getList(CommandParametersQuery query) throws Exception {
        // 0：监控对象 1：企业 2：分组
        String queryType = query.getQueryType();
        String simpleQueryParam = query.getSimpleQueryParam();
        boolean queryParamIsNotBlank = StringUtils.isNotBlank(simpleQueryParam);
        // 获取权限下的车辆
        String currentUsername = SystemHelper.getCurrentUsername();
        Set<String> groupSet = RedisHelper.getSet(RedisKeyEnum.USER_GROUP.of(currentUsername));
        if (CollectionUtils.isEmpty(groupSet)) {
            return new Page<>();
        }
        if (Objects.equals(queryType, "2") && queryParamIsNotBlank) {
            simpleQueryParam = StringUtil.mysqlLikeWildcardTranslation(simpleQueryParam);
            Set<String> filterAssignmentIds = groupDao.findAssignmentIdByFuzzyAssignmentName(simpleQueryParam);
            // 交集
            groupSet.retainAll(filterAssignmentIds);
        }
        List<RedisKey> groupMonitorKeys = RedisKeyEnum.GROUP_MONITOR.ofs(groupSet);
        Set<String> groupMonitorIds = RedisHelper.batchGetSet(groupMonitorKeys);
        if (CollectionUtils.isEmpty(groupMonitorIds)) {
            return new Page<>();
        }
        Integer deviceType = query.getDeviceType();
        String deviceTypeStr = deviceType == null ? null : deviceType.toString();
        List<String> deviceTypes = ProtocolTypeUtil.getProtocolTypes(deviceTypeStr);
        // 协议类型下的车辆id
        Set<String> moIdsByProtocolTypes = newConfigDao.getMoIdByDeviceTypes(deviceTypes);
        // 交集
        groupMonitorIds.retainAll(moIdsByProtocolTypes);
        if (CollectionUtils.isEmpty(groupMonitorIds)) {
            return new Page<>();
        }
        if (Objects.equals(queryType, "0") && queryParamIsNotBlank) {
            simpleQueryParam = StringUtil.mysqlLikeWildcardTranslation(simpleQueryParam);
            Set<String> filterMoIds = newConfigDao.getMoIdsByFuzzyMoName(simpleQueryParam);
            // 交集
            groupMonitorIds.retainAll(filterMoIds);
        } else if (Objects.equals(queryType, "1") && queryParamIsNotBlank) {
            List<String> filterOrgIds = userService.fuzzSearchUserOrgIdsByOrgName(simpleQueryParam);
            Set<String> filterMoIds =
                CollectionUtils.isEmpty(filterOrgIds) ? new HashSet<>() : newConfigDao.getMoIdsByOrgIds(filterOrgIds);
            // 交集
            groupMonitorIds.retainAll(filterMoIds);
        }
        if (CollectionUtils.isEmpty(groupMonitorIds)) {
            return new Page<>();
        }
        //排序
        List<String> sortVehicleList = VehicleUtil.sortVehicles(groupMonitorIds);
        int total = sortVehicleList.size();
        // 当前页
        int curPage = query.getPage().intValue();
        // 每页条数
        int pageSize = query.getLimit().intValue();
        // 遍历开始条数
        int start = (curPage - 1) * pageSize;
        // 遍历结束条数
        int end = pageSize > (total - start) ? total : (pageSize * curPage);
        List<String> queryList = sortVehicleList.subList(start, end);
        List<CommandParametersForm> resultList = new LinkedList<>();
        // 从缓存重截取 开始 结束页
        if (CollectionUtils.isNotEmpty(queryList)) {
            // 根据车辆id查询
            resultList = commandParametersDao.findByVehicleIds(queryList, query.getCommandType());
            // 排序
            VehicleUtil.sort(resultList, sortVehicleList);
        }
        //赋值
        setStatus(resultList, query.getCommandType());
        return RedisQueryUtil.getListToPage(resultList, query, total);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void findInfo(ModelAndView mav, String vid, String commandType) throws Exception {
        Object object = getParamInfo(vid, commandType);
        if (Objects.nonNull(object) && "21".equals(commandType)) {
            List<InformationParam> informationParamList = (List<InformationParam>) object;
            List<Integer> infoIdList =
                informationParamList.stream().filter(informationParam -> Objects.nonNull(informationParam.getInfoId()))
                    .map(InformationParam::getInfoId).collect(Collectors.toList());

            mav.addObject("infoIdList", infoIdList);
        }
        mav.addObject("result", object);
    }

    /**
     * 参考对象
     */
    @Override
    public List<MonitorCommandBindForm> findReferVehicle(String commandType, String vid, Integer deviceType) {
        String deviceTypeStr = deviceType == null ? null : deviceType.toString();
        // 获取权限内协议类型下的车辆的车辆
        Set<String> validVehicleIdSet =
            new HashSet<>(userService.getValidVehicleId(null, null, deviceTypeStr, null, null, false));
        List<MonitorCommandBindForm> monitorCommandBindForms = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(validVehicleIdSet)) {
            List<MonitorCommandBindForm> list;
            if (StringUtils.isEmpty(vid)) {
                list = realTimeCommandDao.findReferVehicle(commandType, null, "");
            } else {
                // 修改排除自己
                list = realTimeCommandDao.findReferVehicle(commandType, null, vid);
            }
            if (CollectionUtils.isNotEmpty(list)) {
                // 过滤
                monitorCommandBindForms = list.stream()
                    .filter(monitorCommandBindForm -> validVehicleIdSet.contains(monitorCommandBindForm.getVid()))
                    .collect(Collectors.toList());
            }
        }
        return monitorCommandBindForms;
    }

    @Override
    public JsonResultBean delete(String id, String vid, String commandType) throws Exception {
        MonitorCommandBindForm commandBind = realTimeCommandDao.getCommandBindInfoById(id);
        boolean flag = realTimeCommandDao.deleteCommandBind(commandBind);
        if (flag) {
            parameterDao.deleteByVechicleidParameterName(vid, id, commandType);
            delCommandTypeParam(commandBind.getParamId(), Integer.valueOf(commandType));
            String name = getCommandName(commandType);
            BindDTO bindDTO = VehicleUtil.getBindInfoByRedis(vid);
            if (bindDTO != null) {
                String brand = bindDTO.getName();
                Integer color = bindDTO.getPlateColor();
                String message = "监控对象【" + brand + "】删除指令参数【" + name + "】";
                logSearchService
                    .addLog(getIpAddress(), message, "3", "", brand, color == null ? null : color.toString());
            }
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public JsonResultBean sendParam(String vid, String commandType, String videoTactics) {
        BindDTO bindDTO = VehicleUtil.getBindInfoByRedis(vid);
        if (bindDTO == null) {
            return new JsonResultBean(JsonResultBean.FAULT, "监控对象不存在");
        }
        String name = bindDTO.getName();
        String deviceType = bindDTO.getDeviceType();
        String deviceNumber = bindDTO.getDeviceNumber();
        String deviceId = bindDTO.getDeviceId();
        String mobile = bindDTO.getSimCardNumber();
        // 序列号
        Integer msgSno = DeviceHelper.getRegisterDevice(vid, deviceNumber);
        String paramType = "F3-commandParam-" + commandType;
        String paramId = "";
        // 设备已经注册
        if (msgSno != null) {
            // 已下发
            int status = 4;
            // 下发参数
            // 先更新下发状态再下发: 避免车辆第一次下发不存在paramId的情况
            paramId = sendHelper.updateParameterStatus(paramId, msgSno, status, vid, paramType, vid + paramType);
            SendParam sendParam = new SendParam();
            sendParam.setMsgSNACK(msgSno);
            sendParam.setParamId(paramId);
            sendParam.setVehicleId(vid);
            f3SendStatusProcessService.updateSendParam(sendParam, 1);
            if ("23".equals(commandType)) {
                //查询终端属性   下发8107  订阅0107
                sendTxtService.devicePropertyQuery(deviceId, mobile, msgSno, deviceType);
            } else {
                T808Msg8106 t8080x8106 = assemble8106Param(commandType, videoTactics, deviceType);
                SubscibeInfo info =
                    new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, msgSno, ConstantUtil.T808_PARAM_ACK);
                SubscibeInfoCache.getInstance().putTable(info);
                T808Message message =
                    MsgUtil.get808Message(mobile, ConstantUtil.T808_QUERY_PARAMS, msgSno, t8080x8106, deviceType);
                WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_QUERY_PARAMS, deviceId);
            }
        } else {
            // 设备未注册
            int status = 5;
            msgSno = 0;
            // 工时绑定下发
            sendHelper.updateParameterStatus(paramId, msgSno, status, vid, paramType, vid + paramType);
        }
        if (msgSno != 0) {
            String module = "读取" + getCommandName(commandType);
            String logMsg = "监控对象：" + name + "( @" + bindDTO.getOrgName() + ")" + module;
            Integer plateColorInt = bindDTO.getPlateColor();
            logSearchService
                .addLog(getIpAddress(), logMsg, "3", "", name, plateColorInt == null ? "" : plateColorInt.toString());
            String username = SystemHelper.getCurrentUsername();
            JSONObject json = new JSONObject();
            json.put("msgId", msgSno);
            json.put("userName", username);
            return new JsonResultBean(JsonResultBean.SUCCESS, json.toJSONString());
        }
        return new JsonResultBean(JsonResultBean.FAULT, "终端离线");
    }

    /**
     * 组装0x8106参数
     */
    private T808Msg8106 assemble8106Param(String commandType, String videoTactics, String deviceType) {
        //查询 RS232串口参数 GNSS等  下发8106 订阅0104
        List<Integer> paramIds = new ArrayList<>();
        String param;
        T808Msg8106 t8080x8106 = new T808Msg8106();
        switch (commandType) {
            //通讯参数
            case "11":
                param = "0010";
                paramIds.add(Integer.parseInt(param, 16));
                param = "0011";
                paramIds.add(Integer.parseInt(param, 16));
                param = "0012";
                paramIds.add(Integer.parseInt(param, 16));
                param = "0013";
                paramIds.add(Integer.parseInt(param, 16));
                param = "0014";
                paramIds.add(Integer.parseInt(param, 16));
                param = "0015";
                paramIds.add(Integer.parseInt(param, 16));
                param = "0016";
                paramIds.add(Integer.parseInt(param, 16));
                param = "0017";
                paramIds.add(Integer.parseInt(param, 16));
                // 808-2019版协议从服务器相关字段
                if ("11".equals(deviceType)) {
                    // 从服务器APN
                    param = "0023";
                    paramIds.add(Integer.parseInt(param, 16));
                    // 从服务器无线通信拨号用户名
                    param = "0024";
                    paramIds.add(Integer.parseInt(param, 16));
                    // 从服务器无线通信拨号密码
                    param = "0025";
                    paramIds.add(Integer.parseInt(param, 16));
                    // 从服务器地址
                    param = "0026";
                    paramIds.add(Integer.parseInt(param, 16));
                    t8080x8106.setParamSum(12);
                } else {
                    param = "0018";
                    paramIds.add(Integer.parseInt(param, 16));
                    param = "0019";
                    paramIds.add(Integer.parseInt(param, 16));
                    t8080x8106.setParamSum(10);
                }
                break;
            //终端参数
            case "12":
                param = "0001";
                paramIds.add(Integer.parseInt(param, 16));
                param = "0002";
                paramIds.add(Integer.parseInt(param, 16));
                param = "0003";
                paramIds.add(Integer.parseInt(param, 16));
                param = "0004";
                paramIds.add(Integer.parseInt(param, 16));
                param = "0005";
                paramIds.add(Integer.parseInt(param, 16));
                param = "0006";
                paramIds.add(Integer.parseInt(param, 16));
                param = "0007";
                paramIds.add(Integer.parseInt(param, 16));
                param = "0030";
                paramIds.add(Integer.parseInt(param, 16));
                param = "0031";
                paramIds.add(Integer.parseInt(param, 16));
                t8080x8106.setParamSum(9);
                break;
            //位置汇报参数
            case "14":
                param = "0020";
                paramIds.add(Integer.parseInt(param, 16));
                param = "0021";
                paramIds.add(Integer.parseInt(param, 16));
                param = "0022";
                paramIds.add(Integer.parseInt(param, 16));
                param = "0027";
                paramIds.add(Integer.parseInt(param, 16));
                param = "0028";
                paramIds.add(Integer.parseInt(param, 16));
                param = "0029";
                paramIds.add(Integer.parseInt(param, 16));
                param = "002C";
                paramIds.add(Integer.parseInt(param, 16));
                param = "002D";
                paramIds.add(Integer.parseInt(param, 16));
                param = "002E";
                paramIds.add(Integer.parseInt(param, 16));
                param = "002F";
                paramIds.add(Integer.parseInt(param, 16));
                t8080x8106.setParamSum(10);
                break;
            //电话参数
            case "16":
                param = "0041";
                paramIds.add(Integer.parseInt(param, 16));
                param = "0042";
                paramIds.add(Integer.parseInt(param, 16));
                param = "0043";
                paramIds.add(Integer.parseInt(param, 16));
                param = "0044";
                paramIds.add(Integer.parseInt(param, 16));
                param = "0045";
                paramIds.add(Integer.parseInt(param, 16));
                param = "0046";
                paramIds.add(Integer.parseInt(param, 16));
                param = "0047";
                paramIds.add(Integer.parseInt(param, 16));
                param = "0048";
                paramIds.add(Integer.parseInt(param, 16));
                param = "0049";
                paramIds.add(Integer.parseInt(param, 16));
                param = "0040";
                paramIds.add(Integer.parseInt(param, 16));
                t8080x8106.setParamSum(10);
                break;
            //视频拍照参数
            case "17":
                param = "0070";
                paramIds.add(Integer.parseInt(param, 16));
                param = "0071";
                paramIds.add(Integer.parseInt(param, 16));
                param = "0072";
                paramIds.add(Integer.parseInt(param, 16));
                param = "0073";
                paramIds.add(Integer.parseInt(param, 16));
                param = "0074";
                paramIds.add(Integer.parseInt(param, 16));
                if ("0".equals(videoTactics)) {
                    //定时
                    param = "0064";
                    paramIds.add(Integer.parseInt(param, 16));
                    t8080x8106.setParamSum(6);
                } else if ("1".equals(videoTactics)) {
                    //定距
                    param = "0065";
                    paramIds.add(Integer.parseInt(param, 16));
                    t8080x8106.setParamSum(6);
                } else {
                    //定时定距
                    param = "0064";
                    paramIds.add(Integer.parseInt(param, 16));
                    param = "0065";
                    paramIds.add(Integer.parseInt(param, 16));
                    t8080x8106.setParamSum(7);
                }
                break;
            //GNSS参数
            case "18":
                param = "0090";
                paramIds.add(Integer.parseInt(param, 16));
                param = "0091";
                paramIds.add(Integer.parseInt(param, 16));
                param = "0092";
                paramIds.add(Integer.parseInt(param, 16));
                param = "0093";
                paramIds.add(Integer.parseInt(param, 16));
                param = "0094";
                paramIds.add(Integer.parseInt(param, 16));
                t8080x8106.setParamSum(5);
                break;
            //RS232串口参数
            case "24":
                param = "F901";
                paramIds.add(Integer.parseInt(param, 16));
                t8080x8106.setParamSum(1);
                break;
            //RS485串口参数
            case "25":
                param = "F902";
                paramIds.add(Integer.parseInt(param, 16));
                t8080x8106.setParamSum(1);
                break;
            //CAN串口参数
            case "26":
                param = "F903";
                paramIds.add(Integer.parseInt(param, 16));
                t8080x8106.setParamSum(1);
                break;
            //GNSS及无线网络状态查询
            case "27":
                param = "F904";
                paramIds.add(Integer.parseInt(param, 16));
                t8080x8106.setParamSum(1);
                break;
            //视频通道状态查询
            case "28":
                param = "F905";
                paramIds.add(Integer.parseInt(param, 16));
                t8080x8106.setParamSum(1);
                break;
            //录像存储状态查询
            case "29":
                param = "F906";
                paramIds.add(Integer.parseInt(param, 16));
                t8080x8106.setParamSum(1);
                break;
            default:
                break;
        }
        t8080x8106.setParamIds(paramIds);
        return t8080x8106;
    }

    @Override
    @SuppressWarnings("unchecked")
    public JsonResultBean getReferenceInfo(String vid, String commandType) throws Exception {
        JSONObject msg = new JSONObject();
        Object paramInfo = getParamInfo(vid, commandType);
        if (Objects.nonNull(paramInfo) && "21".equals(commandType)) {
            List<InformationParam> informationParamList = (List<InformationParam>) paramInfo;
            List<Integer> infoIdList =
                informationParamList.stream().filter(informationParam -> Objects.nonNull(informationParam.getInfoId()))
                    .map(InformationParam::getInfoId).collect(Collectors.toList());

            msg.put("infoIdList", infoIdList);
        }
        msg.put("result", paramInfo);
        return new JsonResultBean(msg);
    }

    private Object getParamInfo(String vid, String commandType) throws Exception {
        Object object = null;
        switch (commandType) {
            //通讯参数
            case "11":
                object = commandService.getCommunicationParam(vid, commandType);
                break;
            //终端参数
            case "12":
                object = commandService.getDeviceParam(vid, commandType);
                break;
            // 无线升级-终端
            case "131":
                object = commandService.getWirelessUpdateParam(vid, commandType);
                break;
            //控制终端连接指定服务器
            case "132":
                object = commandService.getDeviceConnectServerParam(vid, commandType);
                break;
            //位置汇报参数
            case "14":
                object = commandService.getPositionParam(vid, commandType);
                break;
            //电话参数
            case "16":
                object = commandService.getPhoneParam(vid, commandType);
                break;
            //视频拍照参数
            case "17":
                object = commandService.getCameraParam(vid, commandType);
                break;
            //GNSS参数
            case "18":
                object = commandService.getGNSSParam(vid, commandType);
                break;
            //事件设置
            case "19":
                MonitorCommandBindForm eventBind = commandService.findBind(vid, commandType);
                if (eventBind != null) {
                    object = commandService.findEventParam(Arrays.asList(eventBind.getParamId().split(",")));
                }
                break;
            //电话本设置
            case "20":
                MonitorCommandBindForm phoneBind = commandService.findBind(vid, commandType);
                if (phoneBind != null) {
                    object = commandService.findPhoneBookParam(Arrays.asList(phoneBind.getParamId().split(",")));
                }
                break;
            //信息点播菜单
            case "21":
                MonitorCommandBindForm infoBind = commandService.findBind(vid, commandType);
                if (infoBind != null) {
                    object = commandService.findInformationParam(Arrays.asList(infoBind.getParamId().split(",")));
                }
                break;
            //基站参数设置
            case "22":
                StationParam stationParam = commandService.getStationParam(vid, commandType);
                if (stationParam.getLocationTime() != null) {
                    String[] locationTimes = stationParam.getLocationTime().split(";");
                    stationParam.setLocationTimes(locationTimes);
                }
                object = stationParam;
                break;
            //RS232串口参数
            case "24":
            //RS485串口参数
            case "25":
            //CAN总线参数
            case "26":
                MonitorCommandBindForm form = commandService.findBind(vid, commandType);
                if (form != null) {
                    List<SerialPortParam> result =
                        commandService.getSerialPortParam(Arrays.asList(form.getParamId().split(",")));
                    //按序号排序
                    object = result.stream().sorted(Comparator.comparing(SerialPortParam::getSerialPortNumber))
                        .collect(Collectors.toList());
                }
                break;
            default:
                break;
        }
        return object;
    }

    @Override
    public String getCommandName(String commandType) {
        String name = "";
        if (!StringUtils.isEmpty(commandType)) {
            switch (commandType) {
                case "11":
                    name = "通讯参数";
                    break;
                case "12":
                    name = "终端参数";
                    break;
                case "131":
                    name = "无线升级";
                    break;
                case "132":
                    name = "控制终端连接指定服务器";
                    break;
                case "133":
                    name = "终端关机";
                    break;
                case "134":
                    name = "终端复位";
                    break;
                case "135":
                    name = "恢复出厂设置";
                    break;
                case "136":
                    name = "关闭数据通信";
                    break;
                case "137":
                    name = "关闭所有无线通信";
                    break;
                case "14":
                    name = "位置汇报参数";
                    break;
                case "15":
                    name = "终端属性查询";
                    break;
                case "16":
                    name = "电话参数";
                    break;
                case "17":
                    name = "视频拍照参数";
                    break;
                case "18":
                    name = "GNSS参数";
                    break;
                case "19":
                    name = "事件设置";
                    break;
                case "20":
                    name = "电话本设置";
                    break;
                case "21":
                    name = "信息点播菜单";
                    break;
                case "22":
                    name = "基站参数设置";
                    break;
                case "23":
                    name = "终端信息";
                    break;
                case "24":
                    name = "RS232串口参数";
                    break;
                case "25":
                    name = "RS485串口参数";
                    break;
                case "26":
                    name = "CAN总线参数";
                    break;
                case "27":
                    name = "GNSS及无线网络状态查询";
                    break;
                case "28":
                    name = "视频通道状态查询";
                    break;
                case "29":
                    name = "录像存储状态查询";
                    break;
                default:
                    break;
            }
        }
        return name;
    }

    @Override
    public JsonResultBean sendParamByCommandType(String monitorIds, Integer commandType, String upgradeType) {
        if (StringUtils.isBlank(monitorIds) || commandType == null) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        String ip = getIpAddress();
        List<String> monitorIdList = Arrays.asList(monitorIds.split(","));
        Map<String, BindDTO> monitorMap = VehicleUtil.batchGetBindInfosByRedis(monitorIdList);
        List<MonitorCommandBindForm> monitorParamIdList = new ArrayList<>();
        Map<String, List<String>> monitorCommandTypeParamMap = new HashMap<>();
        if (hasParams(commandType)) {
            monitorParamIdList = realTimeCommandDao.getMonitorParamId(monitorIdList, commandType, upgradeType);
            // 获得监控对象对应的指令类型参数
            monitorCommandTypeParamMap = getMonitorCommandTypeParamMap(monitorParamIdList, commandType);
        } else {
            for (String id : monitorIdList) {
                MonitorCommandBindForm form = new MonitorCommandBindForm();
                form.setVid(id);
                form.setId(id + "_" + commandType);
                monitorParamIdList.add(form);
            }
        }
        if (monitorCommandTypeParamMap.size() == 0 && commandType.equals(138)) {
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        // 获得下发状态集合
        Map<String, DirectiveForm> sendStatusMap = getSendStatus(monitorIdList, String.valueOf(commandType));
        for (MonitorCommandBindForm form : monitorParamIdList) {
            String monitorId = form.getVid();
            String paramId = form.getId();
            BindDTO bindDTO = monitorMap.get(monitorId);
            if (bindDTO != null) {
                String directiveId =
                    sendStatusMap.containsKey(monitorId) ? sendStatusMap.get(monitorId).getId() : null;
                List<String> paramJsonList = monitorCommandTypeParamMap.get(monitorId);
                //通过指令类型下发参数和更新下发状态
                sendParamByCommandTypeAndUpdateSendStatus(commandType, monitorId, paramId, bindDTO, directiveId,
                    paramJsonList, ip);
            }
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 指令是否存在参数
     */
    private boolean hasParams(Integer commandType) {
        return commandType != 133 && commandType != 134 && commandType != 135 && commandType != 136
            && commandType != 137;
    }

    public Integer getCommandType(Integer commandType) {
        switch (commandType) {
            case 311:
            case 312:
            case 313:
            case 314:
                commandType = 31;
                break;
            default:
                break;
        }
        return commandType;
    }

    @Override
    public JsonResultBean saveParamByCommandType(MultipartFile file, String monitorIds, String paramJsonStr,
        Integer commandType) throws IOException {
        if (StrUtil.isNotBlank(monitorIds) && commandType != null && StringUtils.isNotBlank(paramJsonStr)) {
            List<String> monitorIdList = Arrays.asList(monitorIds.split(","));
            JSONArray jsonArr = JSONArray.parseArray(paramJsonStr);
            JSONObject paramJsonObj = jsonArr.getJSONObject(0);
            String upgradeType = paramJsonObj.getString("upgradeType");
            List<MonitorCommandBindForm> monitorParamIdList =
                realTimeCommandDao.getMonitorParamId(monitorIdList, commandType, upgradeType);
            List<String> paramIdList = getParamIdList(monitorParamIdList);
            List<MonitorCommandBindForm> monitorParamInfoList = new ArrayList<>();
            List<String> paramJsonList = new ArrayList<>();
            String userName = SystemHelper.getCurrentUsername();
            // 信息ID重复校验
            List<String> repetitionInfoIdList = new ArrayList<>();
            for (String monitorId : monitorIdList) {
                MonitorCommandBindForm form = new MonitorCommandBindForm();
                form.setCommandType(commandType);
                form.setCreateDataUsername(userName);
                form.setVid(monitorId);
                form.setUpgradeType(upgradeType);
                if (StrUtil.isNotBlank(paramJsonStr)) {
                    repetitionInfoIdList.clear();
                    StringBuilder paramId = new StringBuilder();
                    for (int i = 0, len = jsonArr.size(); i < len; i++) {
                        String id = UUID.randomUUID().toString();
                        paramJsonObj = jsonArr.getJSONObject(i);
                        paramJsonObj.put("id", id);
                        paramJsonObj.put("flag", 1);
                        paramJsonObj.put("createDataUsername", userName);
                        paramJsonObj.put("vehicleId", monitorId);
                        if (commandType == 22) {
                            paramJsonObj.remove("vehicleNumber");
                        }
                        if (commandType == 21) {
                            // 信息菜单点播,才增加重复判断
                            String infoId = paramJsonObj.getString("infoId");
                            if (repetitionInfoIdList.contains(infoId)) {
                                return new JsonResultBean(JsonResultBean.FAULT, String.format("信息ID[%s]重复", infoId));
                            } else {
                                repetitionInfoIdList.add(infoId);
                            }
                        }
                        paramJsonList.add(paramJsonObj.toJSONString());
                        paramId.append(id).append(",");
                    }
                    form.setParamId(jodd.util.StringUtil.cutSuffix(paramId.toString(), ","));
                } else {
                    String paramId = resolveCommandType(commandType);
                    if (paramId == null) {
                        return new JsonResultBean(JsonResultBean.FAULT);
                    }
                    form.setParamId(paramId);
                }
                monitorParamInfoList.add(form);
            }
            //保存指令类型参数
            saveCommandTypeParam(file, commandType, paramIdList, paramJsonList);
            // 新增关联表数据
            realTimeCommandDao.addCommandBind(monitorParamInfoList);
            if (CollectionUtils.isNotEmpty(monitorParamIdList)) {
                // 删除原有关联表数据
                realTimeCommandDao.delCommandBindByIds(
                    monitorParamIdList.stream().map(MonitorCommandBindForm::getId).collect(Collectors.toList()));
            }
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    private String resolveCommandType(Integer commandType) {
        switch (commandType) {
            case 133:
                return "终端关机";
            case 134:
                return "终端复位";
            case 135:
                return "恢复出厂设置";
            case 136:
                return "关闭数据通信";
            case 137:
                return "关闭所有无线通信";
            case 311:
                return "删除所有圆形区域";
            case 312:
                return "删除所有矩形区域";
            case 313:
                return "删除所有多边形区域";
            case 314:
                return "删除所有路线区域";
            default:
                return null;
        }
    }

    /**
     * 保存指令类型参数
     */
    private void saveCommandTypeParam(MultipartFile file, Integer commandType, List<String> paramIdList,
        List<String> paramJsonList) throws IOException {
        switch (commandType) {
            // 通讯参数
            case 11:
                List<CommunicationParam> commParamList =
                    paramJsonList.stream().map(str -> JSONObject.parseObject(str, CommunicationParam.class))
                        .collect(Collectors.toList());
                // 新增新的指令参数设置
                realTimeCommandDao.addCommunicationParamByBatch(commParamList);
                if (CollectionUtils.isNotEmpty(paramIdList)) {
                    // 删除原有的参数设置
                    realTimeCommandDao.delCommunicationParamByIds(paramIdList);
                }
                break;
            // 终端参数
            case 12:
                List<DeviceParam> deviceParamList =
                    paramJsonList.stream().map(str -> JSONObject.parseObject(str, DeviceParam.class))
                        .collect(Collectors.toList());
                realTimeCommandDao.addDeviceParamByBatch(deviceParamList);
                if (CollectionUtils.isNotEmpty(paramIdList)) {
                    realTimeCommandDao.delDeviceParamByIds(paramIdList);
                }
                break;
            // 无线升级
            case 131:
                List<WirelessUpdateParam> wirelessUpdateParamList =
                    paramJsonList.stream().map(str -> JSONObject.parseObject(str, WirelessUpdateParam.class))
                        .collect(Collectors.toList());
                realTimeCommandDao.addWirelessUpdateParamByBatch(wirelessUpdateParamList);
                if (CollectionUtils.isNotEmpty(paramIdList)) {
                    realTimeCommandDao.delWirelessUpdateParamByIds(paramIdList);
                }
                break;
            // 控制终端连接指定服务器
            case 132:
                List<DeviceConnectServerParam> deviceConnectServerParamList =
                    paramJsonList.stream().map(str -> JSONObject.parseObject(str, DeviceConnectServerParam.class))
                        .collect(Collectors.toList());
                realTimeCommandDao.addDeviceConnectServerParamByBatch(deviceConnectServerParamList);
                if (CollectionUtils.isNotEmpty(paramIdList)) {
                    realTimeCommandDao.delDeviceConnectServerParamByIds(paramIdList);
                }
                break;
            // 终端关机
            case 133:
                // 终端复位
            case 134:
                // 恢复出厂设置
            case 135:
                // 关闭数据通信
            case 136:
                // 关闭所有无线通信
            case 137:
                break;
            // 位置汇报参数
            case 14:
                List<PositionParam> positionParamList =
                    paramJsonList.stream().map(str -> JSONObject.parseObject(str, PositionParam.class))
                        .collect(Collectors.toList());
                realTimeCommandDao.addPositionParamByBatch(positionParamList);
                if (CollectionUtils.isNotEmpty(paramIdList)) {
                    realTimeCommandDao.delPositionParamByIds(paramIdList);
                }
                break;
            // 电话参数
            case 16:
                List<PhoneParam> phoneParamList =
                    paramJsonList.stream().map(str -> JSONObject.parseObject(str, PhoneParam.class))
                        .collect(Collectors.toList());
                realTimeCommandDao.addPhoneParamByBatch(phoneParamList);
                if (CollectionUtils.isNotEmpty(paramIdList)) {
                    realTimeCommandDao.delPhoneParamByIds(paramIdList);
                }
                break;
            // 视频拍照参数
            case 17:
                List<CameraParam> cameraParamList =
                    paramJsonList.stream().map(str -> JSONObject.parseObject(str, CameraParam.class))
                        .collect(Collectors.toList());
                realTimeCommandDao.addCameraParamByBatch(cameraParamList);
                if (CollectionUtils.isNotEmpty(paramIdList)) {
                    realTimeCommandDao.delCameraParamByIds(paramIdList);
                }
                break;
            // GNSS参数
            case 18:
                List<GNSSParam> gnssParams =
                    paramJsonList.stream().map(str -> JSONObject.parseObject(str, GNSSParam.class))
                        .collect(Collectors.toList());
                realTimeCommandDao.addGNSSParamByBatch(gnssParams);
                if (CollectionUtils.isNotEmpty(paramIdList)) {
                    realTimeCommandDao.delGNSSParamByIds(paramIdList);
                }
                break;
            // 事件设置
            case 19:
                List<EventSetParam> eventSetParamList =
                    paramJsonList.stream().map(str -> JSONObject.parseObject(str, EventSetParam.class))
                        .collect(Collectors.toList());
                realTimeCommandDao.addEventSetParamByBatch(eventSetParamList);
                if (CollectionUtils.isNotEmpty(paramIdList)) {
                    realTimeCommandDao.delEventSetParamByIds(paramIdList);
                }
                break;
            // 电话本设置
            case 20:
                List<PhoneBookParam> phoneBookParamList =
                    paramJsonList.stream().map(str -> JSONObject.parseObject(str, PhoneBookParam.class))
                        .collect(Collectors.toList());
                realTimeCommandDao.addPhoneBookParamByBatch(phoneBookParamList);
                if (CollectionUtils.isNotEmpty(paramIdList)) {
                    realTimeCommandDao.delPhoneBookParamByIds(paramIdList);
                }
                break;
            // 信息点播菜单
            case 21:
                List<InformationParam> informationParamList =
                    paramJsonList.stream().map(str -> JSONObject.parseObject(str, InformationParam.class))
                        .collect(Collectors.toList());
                realTimeCommandDao.addInformationParamByBatch(informationParamList);
                if (CollectionUtils.isNotEmpty(paramIdList)) {
                    realTimeCommandDao.delInformationParamByIds(paramIdList);
                }
                break;
            // 基站参数设置
            case 22:
                List<StationParam> stationParamList =
                    paramJsonList.stream().map(str -> JSONObject.parseObject(str, StationParam.class))
                        .collect(Collectors.toList());
                for (StationParam stationParam : stationParamList) {
                    String locationTime = stationParam.getLocationTime();
                    if (StringUtils.isNotBlank(locationTime)) {
                        String[] split = locationTime.split(";");
                        stationParam.setLocationNumber(split.length);
                        continue;
                    }
                    stationParam.setLocationNumber(0);
                }
                realTimeCommandDao.addStationParamByBatch(stationParamList);
                if (CollectionUtils.isNotEmpty(paramIdList)) {
                    realTimeCommandDao.delStationParamByIds(paramIdList);
                }
                break;
            // RS232串口参数
            case 24:
                // RS485串口参数
            case 25:
                // CAN总线参数
            case 26:
                List<SerialPortParam> serialPortParamList =
                    paramJsonList.stream().map(str -> JSONObject.parseObject(str, SerialPortParam.class))
                        .collect(Collectors.toList());
                realTimeCommandDao.addSerialPortParamByBatch(serialPortParamList);
                if (CollectionUtils.isNotEmpty(paramIdList)) {
                    realTimeCommandDao.delSerialPortParamByIds(paramIdList);
                }
                break;
            //下发升级包
            case 138:
                List<DeviceUpgrade> deviceUpgradeList = Lists.newLinkedList();
                DeviceUpgrade deviceUpgrade;
                String fileId = null;
                String userName = SystemHelper.getCurrentUsername();
                for (int i = 0; i < paramJsonList.size(); i++) {
                    deviceUpgrade = JSONObject.parseObject(paramJsonList.get(i), DeviceUpgrade.class);
                    if (i == 0) {
                        if (StringUtils.isEmpty(deviceUpgrade.getUpgradeFileId()) && file == null) {
                            return;
                        }
                        if (file != null) {
                            //代表新增
                            String url = fastDFSClient.uploadFile(file).split(fdfsWebServer.getWebServerUrl())[1];
                            fileId = UUID.randomUUID().toString();
                            deviceUpgrade.setUpgradeFileId(fileId);
                            deviceUpgrade.setUploadTime(new Date());
                            deviceUpgrade.setUrl(url);
                            deviceUpgrade.setFileName(file.getOriginalFilename());
                            deviceUpgradeDao.addDeviceUpgradeFile(deviceUpgrade);
                        }
                    }
                    if (StringUtils.isEmpty(deviceUpgrade.getUpgradeFileId())) {
                        deviceUpgrade.setUpgradeFileId(fileId);
                    }

                    //判断是否是定时升级
                    if (deviceUpgrade.getUpgradeStrategyFlag() == 1 && deviceUpgrade.getScheduleUpgradeTime() != null) {
                        timingUpgrade(deviceUpgrade);
                    }
                    deviceUpgrade.setCreateDataUsername(userName);
                    deviceUpgradeList.add(deviceUpgrade);
                }
                deviceUpgradeDao.addDeviceUpgradeByBatch(deviceUpgradeList);
                if (CollectionUtils.isNotEmpty(paramIdList)) {
                    //删除以前的定时任务
                    deleteUpgradeTask(paramIdList.get(0));
                    deviceUpgradeDao.delDeviceUpgradeByIds(paramIdList);
                }
                break;
            default:
                break;
        }
    }

    private void deleteUpgradeTask(String id) {
        TaskJobForm taskJobForm = deviceUpgradeDao.getTaskJobById(id);
        if (taskJobForm != null && taskJobForm.getJobGroup() != null && taskJobForm.getJobName() != null) {
            try {
                quartzManager.deleteJob(taskJobForm);
                deviceUpgradeDao.updateBeforeVehicleUpgrade(taskJobForm.getId());
            } catch (Exception e) {
                logger.error("终端升级删除定时任务异常！", e);
            }
        }
    }

    private void timingUpgrade(DeviceUpgrade deviceUpgrade) {
        String corn = getDateData(
            Objects.requireNonNull(DateUtil.getDateToString(deviceUpgrade.getScheduleUpgradeTime(), null)));
        deviceUpgrade.setCronExpression(corn);
        //定时任务名保证唯一性
        deviceUpgrade.setJobName(job_name + "_" + deviceUpgrade.getVehicleId() + "_" + DateUtil
            .getDateToString(deviceUpgrade.getScheduleUpgradeTime(), DateUtil.DATE_FORMAT) + "_" + deviceUpgrade
            .getUpgradeType());
        deviceUpgrade.setBeanClass(bean_class);
        deviceUpgrade.setDescription("下发升级包定任务");
        //1代表需要运行的 0 代表已经跑过的,2 代表已经删除的
        deviceUpgrade.setJobStatus("1");
        Map<String, Object> map = new HashMap<>();
        map.put("id", deviceUpgrade.getId());
        map.put("vehicleId", deviceUpgrade.getVehicleId());
        map.put("upgradeType", deviceUpgrade.getUpgradeType());
        quartzManager.addJob(deviceUpgrade, map);
    }

    private String getDateData(String time) {
        String[] data = new String[6];
        String day = time.substring(0, 10);
        String sec = time.substring(11, 19);
        String[] d = day.split("-");
        String[] m = sec.split(":");
        System.arraycopy(d, 0, data, 0, d.length);
        System.arraycopy(m, 0, data, 3, m.length);
        return data[5] + " " + data[4] + " " + data[3] + " " + data[2] + " " + data[1] + " ? " + data[0];
    }

    /**
     * 删除指令类型参数
     */
    private void delCommandTypeParam(String paramId, Integer commandType) {
        if (StrUtil.isNotBlank(paramId)) {
            List<String> paramIdList = Arrays.asList(paramId.split(","));
            switch (commandType) {
                // 通讯参数
                case 11:
                    realTimeCommandDao.delCommunicationParamByIds(paramIdList);
                    break;
                // 终端参数
                case 12:
                    realTimeCommandDao.delDeviceParamByIds(paramIdList);
                    break;
                // 无线升级
                case 131:
                    realTimeCommandDao.delWirelessUpdateParamByIds(paramIdList);
                    break;
                // 控制终端连接指定服务器
                case 132:
                    realTimeCommandDao.delDeviceConnectServerParamByIds(paramIdList);
                    break;
                // 终端关机
                case 133:
                    // 终端复位
                case 134:
                    // 恢复出厂设置
                case 135:
                    // 关闭数据通信
                case 136:
                    // 关闭所有无线通信
                case 137:
                    break;
                // 位置汇报参数
                case 14:
                    realTimeCommandDao.delPositionParamByIds(paramIdList);
                    break;
                // 电话参数
                case 16:
                    realTimeCommandDao.delPhoneParamByIds(paramIdList);
                    break;
                // 视频拍照参数
                case 17:
                    realTimeCommandDao.delCameraParamByIds(paramIdList);
                    break;
                // GNSS参数
                case 18:
                    realTimeCommandDao.delGNSSParamByIds(paramIdList);
                    break;
                // 事件设置
                case 19:
                    realTimeCommandDao.delEventSetParamByIds(paramIdList);
                    break;
                // 电话本设置
                case 20:
                    realTimeCommandDao.delPhoneBookParamByIds(paramIdList);
                    break;
                // 信息点播菜单
                case 21:
                    realTimeCommandDao.delInformationParamByIds(paramIdList);
                    break;
                // 基站参数设置
                case 22:
                    realTimeCommandDao.delStationParamByIds(paramIdList);
                    break;
                // RS232串口参数
                case 24:
                    // RS485串口参数
                case 25:
                    // CAN总线参数
                case 26:
                    realTimeCommandDao.delSerialPortParamByIds(paramIdList);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 通过指令类型下发参数和更新下发状态
     * @param commandType   指令类型
     * @param monitorId     监控对象id
     * @param paramId       参数id
     * @param bindDTO    监控对象信息
     * @param directiveId   下发指令表id
     * @param paramJsonList 参数对象
     */
    private void sendParamByCommandTypeAndUpdateSendStatus(Integer commandType, String monitorId, String paramId,
        BindDTO bindDTO, String directiveId, List<String> paramJsonList, String ip) {
        String deviceId = bindDTO.getDeviceId();
        String deviceNumber = bindDTO.getDeviceNumber();
        String deviceType = bindDTO.getDeviceType();
        Integer msgSno = DeviceHelper.getRegisterDevice(monitorId, deviceNumber);
        String mobile = bindDTO.getSimCardNumber();
        String paramJsonStr = CollectionUtils.isNotEmpty(paramJsonList) ? paramJsonList.get(0) : "";
        SendTarget sendTarget = SendTarget.getInstance(SendModule.DIRECTIVE_PARAMETER, String.valueOf(commandType));
        switch (commandType) {
            // 通讯参数
            case 11:
                if (StrUtil.isNotBlank(paramJsonStr)) {
                    CommunicationParam communicationParam =
                        JSONObject.parseObject(paramJsonStr, CommunicationParam.class);
                    //在线
                    if (msgSno != null) {
                        sendTxtService.setCommunicationParam(deviceId, mobile, communicationParam, msgSno, deviceType,
                            sendTarget);
                    }
                }
                break;
            // 终端参数
            case 12:
                if (StrUtil.isNotBlank(paramJsonStr)) {
                    DeviceParam deviceParam = JSONObject.parseObject(paramJsonStr, DeviceParam.class);
                    if (msgSno != null) {
                        sendTxtService.setDeviceParam(deviceId, mobile, deviceParam, msgSno, deviceType, sendTarget);
                    }
                }
                break;
            // 无线升级
            case 131:
                if (StrUtil.isNotBlank(paramJsonStr)) {
                    WirelessUpdateParam wwParam = JSONObject.parseObject(paramJsonStr, WirelessUpdateParam.class);
                    DeviceCommand wwDeviceCommand = new DeviceCommand();
                    wwDeviceCommand.setCw(1);
                    wwDeviceCommand.setParam(getWirelessUpdateParamString(wwParam));
                    if (msgSno != null) {
                        sendTxtService.deviceCommand(mobile, wwDeviceCommand, msgSno, deviceId, deviceType, sendTarget);
                    }
                }
                break;
            // 控制终端连接指定服务器
            case 132:
                if (StrUtil.isNotBlank(paramJsonStr)) {
                    DeviceConnectServerParam ccParam =
                        JSONObject.parseObject(paramJsonStr, DeviceConnectServerParam.class);
                    DeviceCommand ccDeviceCommand = new DeviceCommand();
                    ccDeviceCommand.setCw(2);
                    ccDeviceCommand.setParam(getDeviceConnectServerParamString(ccParam));
                    if (msgSno != null) {
                        sendTxtService.deviceCommand(mobile, ccDeviceCommand, msgSno, deviceId, deviceType, sendTarget);
                    }
                }
                break;
            //终端关机
            case 133:
                //终端复位
            case 134:
                //恢复出厂设置
            case 135:
                //关闭数据通信
            case 136:
                //关闭所有无线通信
            case 137:
                DeviceCommand deviceCommand = new DeviceCommand();
                deviceCommand.setCw(commandType - 130);
                if (msgSno != null) {
                    sendTxtService.deviceCommand(mobile, deviceCommand, msgSno, deviceId, deviceType, sendTarget);
                }
                break;
            // 位置汇报参数
            case 14:
                if (StrUtil.isNotBlank(paramJsonStr)) {
                    PositionParam positionParam = JSONObject.parseObject(paramJsonStr, PositionParam.class);
                    if (msgSno != null) {
                        sendTxtService
                            .setPositionParam(deviceId, mobile, positionParam, msgSno, deviceType, sendTarget);
                    }
                }
                break;
            // 电话参数
            case 16:
                if (StrUtil.isNotBlank(paramJsonStr)) {
                    PhoneParam phoneParam = JSONObject.parseObject(paramJsonStr, PhoneParam.class);
                    if (msgSno != null) {
                        sendTxtService.setPhoneParam(deviceId, mobile, phoneParam, msgSno, deviceType, sendTarget);
                    }
                }
                break;
            // 视频拍照参数
            case 17:
                if (StrUtil.isNotBlank(paramJsonStr)) {
                    CameraParam cameraParam = JSONObject.parseObject(paramJsonStr, CameraParam.class);
                    if (msgSno != null) {
                        sendTxtService.setCameraParam(deviceId, mobile, cameraParam, msgSno, deviceType, sendTarget);
                    }
                }
                break;
            // GNSS参数
            case 18:
                if (StrUtil.isNotBlank(paramJsonStr)) {
                    GNSSParam gnssParam = JSONObject.parseObject(paramJsonStr, GNSSParam.class);
                    if (msgSno != null) {
                        sendTxtService.setGNSSParam(deviceId, mobile, gnssParam, msgSno, deviceType, sendTarget);
                    }
                }
                break;
            // 事件设置
            case 19:
                if (StrUtil.isNotBlank(paramJsonStr)) {
                    List<EventSetParam> event =
                        paramJsonList.stream().map(str -> JSONObject.parseObject(str, EventSetParam.class))
                            .collect(Collectors.toList());
                    if (msgSno != null) {
                        sendTxtService.setEvent(deviceId, mobile, event, msgSno, deviceType, sendTarget);
                    }
                }
                break;
            // 电话本设置
            case 20:
                if (StrUtil.isNotBlank(paramJsonStr)) {
                    List<PhoneBookParam> phone =
                        paramJsonList.stream().map(str -> JSONObject.parseObject(str, PhoneBookParam.class))
                            .collect(Collectors.toList());
                    if (msgSno != null) {
                        sendTxtService.setPhoneBook(deviceId, mobile, phone, msgSno, deviceType, sendTarget);
                    }
                }
                break;
            // 信息点播菜单
            case 21:
                if (StrUtil.isNotBlank(paramJsonStr)) {
                    List<InformationParam> info =
                        paramJsonList.stream().map(str -> JSONObject.parseObject(str, InformationParam.class))
                            .collect(Collectors.toList());
                    if (msgSno != null) {
                        sendTxtService.setInformationDemand(deviceId, mobile, info, msgSno, deviceType, sendTarget);
                    }
                }
                break;
            // 基站参数设置
            case 22:
                if (StrUtil.isNotBlank(paramJsonStr)) {
                    StationParam stationParam = JSONObject.parseObject(paramJsonStr, StationParam.class);
                    if (msgSno != null) {
                        sendTxtService.setStationParam(deviceId, mobile, stationParam, msgSno, deviceType, sendTarget);
                    }
                }
                break;
            // RS232串口参数
            // RS485串口参数
            case 25:
            case 24:
                // CAN总线参数
            case 26:
                if (StrUtil.isNotBlank(paramJsonStr)) {
                    List<SerialPortParam> serialPortParam =
                        paramJsonList.stream().map(str -> JSONObject.parseObject(str, SerialPortParam.class))
                            .collect(Collectors.toList());
                    if (msgSno != null) {
                        Integer id = commandType == 24 ? 0xF901 : (commandType == 25 ? 0xF902 : 0xF903);
                        sendTxtService
                            .setSerialPortParam(deviceId, mobile, serialPortParam, msgSno, id, deviceType, sendTarget);
                    }
                }
                break;
            //指令参数批量升级终端
            case 138:
                if (StrUtil.isNotBlank(paramJsonStr)) {
                    // list的size为1
                    List<DeviceUpgrade> list =
                        paramJsonList.stream().map(str -> JSONObject.parseObject(str, DeviceUpgrade.class))
                            .collect(Collectors.toList());
                    //判断终端是否离线并获取流水号
                    if (msgSno != null) {
                        sendTxtService.setDeviceUpgrade(monitorId, deviceId, mobile, list, msgSno, deviceType);
                        DeviceUpgrade deviceUpgrade = list.get(0);
                        String createDataUsername = deviceUpgrade.getCreateDataUsername();
                        String fileName = deviceUpgrade.getFileName();
                        String brand = bindDTO.getName();
                        logSearchService
                            .addLog(ip, "用户" + createDataUsername + "使用终端升级文件：" + fileName + "对" + brand + "进行远程升级",
                                "3", "", "下发终端升级文件");
                    }
                }
                break;
            default:
                break;
        }
        //在线
        if (msgSno != null) {
            directiveId = sendHelper
                .updateParameterStatus(directiveId, msgSno, 4, monitorId, String.valueOf(commandType), paramId);
            SendParam sendParam = new SendParam();
            sendParam.setMsgSNACK(msgSno);
            sendParam.setParamId(directiveId);
            sendParam.setVehicleId(monitorId);
            f3SendStatusProcessService.updateSendParam(sendParam, 2);
            //离线
        } else {
            sendHelper.updateParameterStatus(directiveId, 0, 5, monitorId, String.valueOf(commandType), paramId);
        }
    }

    private void deleteAllFences(String deviceNumber, Integer msgSno, String mobile, String deviceId, int msgId) {
        List<Integer> emptyIds = Collections.emptyList();
        VehicleInfo vehicle = new VehicleInfo();
        vehicle.setDeviceId(deviceId);
        vehicle.setSimcardNumber(mobile);
        vehicle.setDeviceNumber(deviceNumber);
        wsElectronicDefenceService.deleteDefenseInfo(msgId, vehicle, emptyIds, msgSno);
    }

    private String getWirelessUpdateParamString(WirelessUpdateParam wirelessUpdateParam) {
        StringBuilder str = new StringBuilder();
        String url = wirelessUpdateParam.getUrl();
        if (StringUtils.isNotBlank(url)) {
            str.append(url);
        }
        str.append(";");
        String wdailName = wirelessUpdateParam.getWDailName();
        if (StringUtils.isNotBlank(wdailName)) {
            str.append(wdailName);
        }
        str.append(";");
        String wdailUserName = wirelessUpdateParam.getWDailUserName();
        if (StringUtils.isNotBlank(wdailUserName)) {
            str.append(wdailUserName);
        }
        str.append(";");
        String wdailPwd = wirelessUpdateParam.getWDailPwd();
        if (StringUtils.isNotBlank(wdailPwd)) {
            str.append(wdailPwd);
        }
        str.append(";");
        String waddress = wirelessUpdateParam.getWAddress();
        if (StringUtils.isNotBlank(waddress)) {
            str.append(waddress);
        }
        str.append(";");
        Integer wtcpPort = wirelessUpdateParam.getWTcpPort();
        if (wtcpPort != null) {
            str.append(wtcpPort);
        }
        str.append(";");
        Integer wudpPort = wirelessUpdateParam.getWUdpPort();
        if (wudpPort != null) {
            str.append(wudpPort);
        }
        str.append(";");
        String manufactorId = wirelessUpdateParam.getManufactorId();
        if (StringUtils.isNotBlank(manufactorId)) {
            str.append(manufactorId);
        }
        str.append(";");
        String hardwareVersion = wirelessUpdateParam.getHardwareVersion();
        if (StringUtils.isNotBlank(hardwareVersion)) {
            str.append(hardwareVersion);
        }
        str.append(";");
        String firmwareVersion = wirelessUpdateParam.getFirmwareVersion();
        if (StringUtils.isNotBlank(firmwareVersion)) {
            str.append(firmwareVersion);
        }
        str.append(";");
        Integer wtimeLimit = wirelessUpdateParam.getWTimeLimit();
        if (wtimeLimit != null) {
            str.append(wtimeLimit);
        }
        str.append(";");
        return str.toString();
    }

    private String getDeviceConnectServerParamString(DeviceConnectServerParam ccParam) {
        StringBuilder str = new StringBuilder();
        Integer caccessControl = ccParam.getAccessControl();
        if (caccessControl != null) {
            str.append(caccessControl);
        }
        str.append(";");
        String authCode = ccParam.getAuthCode();
        str.append(authCode);
        str.append(";");
        String cdailName = ccParam.getDailName();
        if (StringUtils.isNotBlank(cdailName)) {
            str.append(cdailName);
        }
        str.append(";");
        String cdailUserName = ccParam.getDailUserName();
        if (StringUtils.isNotBlank(cdailUserName)) {
            str.append(cdailUserName);
        }
        str.append(";");
        String cdailPwd = ccParam.getDailPwd();
        if (StringUtils.isNotBlank(cdailPwd)) {
            str.append(cdailPwd);
        }
        str.append(";");
        String caddress = ccParam.getAddress();
        if (StringUtils.isNotBlank(caddress)) {
            str.append(caddress);
        }
        str.append(";");
        Integer ctcpPort = ccParam.getTcpPort();
        if (ctcpPort != null) {
            str.append(ctcpPort);
        }
        str.append(";");
        Integer cudpPort = ccParam.getUdpPort();
        if (cudpPort != null) {
            str.append(cudpPort);
        }
        str.append(";");
        Integer ctimeLimit = ccParam.getTimeLimit();
        if (ctimeLimit != null) {
            str.append(ctimeLimit);
        }
        str.append(";");
        return str.toString();
    }

    /**
     * 获得下发状态集合
     */
    private Map<String, DirectiveForm> getSendStatus(List<String> monitorIdList, String paramType) {
        Map<String, DirectiveForm> monitorSendStatusMap = new HashMap<>(16);
        List<DirectiveForm> sendStatusList = parameterDao.getSendStatusList(monitorIdList, paramType);
        if (CollectionUtils.isNotEmpty(sendStatusList)) {
            monitorSendStatusMap = sendStatusList.stream()
                .collect(Collectors.toMap(DirectiveForm::getMonitorObjectId, form -> form, (o, p) -> o));
        }
        return monitorSendStatusMap;
    }

    /**
     * 获得监控对象对应的指令类型参数
     */
    private Map<String, List<String>> getMonitorCommandTypeParamMap(List<MonitorCommandBindForm> monitorParamIdList,
        Integer commandType) {
        Map<String, List<String>> monitorCommandTypeParamMap = new HashMap<>(16);
        List<String> paramIdList = getParamIdList(monitorParamIdList);
        if (CollectionUtils.isNotEmpty(paramIdList)) {
            List paramList = null;
            switch (commandType) {
                //通讯参数
                case 11:
                    paramList = realTimeCommandDao.findCommunicationParamByParamIds(paramIdList);
                    break;
                // 终端参数
                case 12:
                    paramList = realTimeCommandDao.findDeviceParamByParamIds(paramIdList);
                    break;
                // 无线升级
                case 131:
                    paramList = realTimeCommandDao.findWirelessUpgradeParamByParamIds(paramIdList);
                    break;
                // 控制终端连接指定服务器
                case 132:
                    paramList = realTimeCommandDao.findDeviceConnectServerParamByParamIds(paramIdList);
                    break;
                // 位置汇报参数
                case 14:
                    paramList = realTimeCommandDao.findPositionParamByParamIds(paramIdList);
                    break;
                // 电话参数
                case 16:
                    paramList = realTimeCommandDao.findPhoneParamByParamIds(paramIdList);
                    break;
                // 视频拍照参数
                case 17:
                    paramList = realTimeCommandDao.findCameraParamByParamIds(paramIdList);
                    break;
                // GNSS参数
                case 18:
                    paramList = realTimeCommandDao.findGNSSParamByParamIds(paramIdList);
                    break;
                // 事件设置
                case 19:
                    paramList = realTimeCommandDao.findEventParamByParamIds(paramIdList);
                    break;
                // 电话本设置
                case 20:
                    paramList = realTimeCommandDao.findPhoneBookParamByParamIds(paramIdList);
                    break;
                // 信息点播菜单
                case 21:
                    paramList = realTimeCommandDao.findInformationParamByParamIds(paramIdList);
                    break;
                // 基站参数设置
                case 22:
                    paramList = realTimeCommandDao.findStationParamByParamIds(paramIdList);
                    break;
                // RS232串口参数
                case 24:
                    //RS485串口参数;
                case 25:
                    //CAN总线参数
                case 26:
                    paramList = realTimeCommandDao.findSerialPortParamByParamIds(paramIdList);
                    break;
                //批量升级包
                case 138:
                    paramList = realTimeCommandDao.findDeviceUpgradeDaoByIds(paramIdList);
                    break;
                default:
                    break;
            }
            if (CollectionUtils.isNotEmpty(paramList)) {
                for (Object info : paramList) {
                    String jsonStr = JSON.toJSONString(info);
                    String vid = JSON.parseObject(jsonStr).getString("vid");
                    List<String> paramStrList;
                    if (monitorCommandTypeParamMap.containsKey(vid)) {
                        paramStrList = monitorCommandTypeParamMap.get(vid);
                    } else {
                        paramStrList = new ArrayList<>();
                    }
                    paramStrList.add(jsonStr);
                    monitorCommandTypeParamMap.put(vid, paramStrList);
                }
            }
        }
        return monitorCommandTypeParamMap;
    }

    /**
     * 获得关联表中的参数id
     */
    private List<String> getParamIdList(List<MonitorCommandBindForm> monitorParamIdList) {
        List<String> paramIdList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(monitorParamIdList)) {
            for (MonitorCommandBindForm info : monitorParamIdList) {
                String paramId = info.getParamId();
                paramIdList.addAll(Arrays.asList(paramId.split(",")));
            }
        }
        return paramIdList;
    }

    private void setStatus(List<CommandParametersForm> resultList, String commandType) {
        if (CollectionUtils.isEmpty(resultList)) {
            return;
        }
        List<GroupDTO> currentUserGroupList = userService.getCurrentUserGroupList();
        Map<String, String> userGroupIdAndNameMap =
            currentUserGroupList.stream().collect(Collectors.toMap(GroupDTO::getId, GroupDTO::getName));
        Set<String> moIds = resultList.stream().map(CommandParametersForm::getVehicleId).collect(Collectors.toSet());
        Map<String, BindDTO> monitorMap = VehicleUtil.batchGetBindInfosByRedis(moIds);
        for (CommandParametersForm parameter : resultList) {
            String vehicleId = parameter.getVehicleId();
            String parameterId = parameter.getId();
            BindDTO bindDTO = monitorMap.get(vehicleId);
            if (bindDTO != null) {
                parameter.setGroupName(bindDTO.getOrgName());
                String groupIds = bindDTO.getGroupId();
                String groupNames = Arrays.stream(groupIds.split(","))
                    .map(userGroupIdAndNameMap::get)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.joining(","));
                parameter.setAssignmentName(groupNames);
            }
            // 下发参数状态
            if (StringUtils.isNotBlank(vehicleId) && StringUtils.isNotBlank(parameterId)) {
                String parameterName = parameterId;
                if ("30".equals(commandType) || "31".equals(commandType) || "133".equals(commandType) || "134"
                    .equals(commandType) || "135".equals(commandType) || "136".equals(commandType) || "137"
                    .equals(commandType)) {
                    parameterName = vehicleId + "_" + commandType;
                }
                List<Directive> directiveList =
                    parameterDao.findParameterByType(vehicleId, parameterName, commandType);
                Directive firstDirective = null;
                if (CollectionUtils.isNotEmpty(directiveList)) {
                    firstDirective = directiveList.get(0);
                }
                if (firstDirective != null) {
                    parameter.setStatus(firstDirective.getStatus());
                    parameter.setParamId(firstDirective.getId());
                }
            }
        }
    }

    @Override
    public Integer sendOneMediaSearchUpMsg(String monitorId, Integer mediaId, Integer deleteSign) {
        if (StringUtils.isBlank(monitorId) || mediaId == null || deleteSign == null) {
            return null;
        }
        BindDTO bindDTO = VehicleUtil.getBindInfoByRedis(monitorId);
        if (bindDTO == null) {
            return null;
        }
        SingleMediaSearchUp msgBody = new SingleMediaSearchUp();
        msgBody.setId(mediaId);
        msgBody.setDeleteSign(deleteSign);
        String deviceNumber = bindDTO.getDeviceNumber();
        String deviceId = bindDTO.getDeviceId();
        String deviceType = bindDTO.getDeviceType();
        String simCardNumber = bindDTO.getSimCardNumber();
        // 流水号
        Integer msgSno = DeviceHelper.getRegisterDevice(monitorId, deviceNumber);
        String commType = "30";
        String paramType = monitorId + "_" + commType;
        String paramId = sendHelper.getLastSendParamID(monitorId, paramType, commType);
        if (msgSno != null) {
            // 订阅推送消息
            SubscibeInfo info =
                new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, msgSno, ConstantUtil.T808_DEVICE_GE_ACK);
            SubscibeInfoCache.getInstance().putTable(info);
            paramSendingCache.put(SystemHelper.getCurrentUsername(), msgSno, simCardNumber,
                SendTarget.getInstance(SendModule.DIRECTIVE_PARAMETER));
            T808Message message =
                MsgUtil.get808Message(simCardNumber, ConstantUtil.T808_ONE_MULTIMEDIA_UP, msgSno, msgBody, deviceType);
            WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_ONE_MULTIMEDIA_UP, deviceId);
            sendHelper.updateParameterStatus(paramId, msgSno, 4, monitorId, commType, paramType);
        } else { // 设备离线
            sendHelper.updateParameterStatus(paramId, 0, 5, monitorId, commType, paramType);
        }
        return msgSno;
    }

    @Override
    public JsonResultBean sendDeleteDeviceFence(String monitorIds, Integer commandType, Integer deviceFence) {
        List<String> monitorIdList = Arrays.asList(monitorIds.split(","));
        List<BindDTO> bindDTOList = new ArrayList<>(VehicleUtil.batchGetBindInfosByRedis(monitorIdList).values());
        if (CollectionUtils.isEmpty(bindDTOList)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        for (BindDTO bindDTO : bindDTOList) {
            String monitorId = bindDTO.getId();
            String deviceNumber = bindDTO.getDeviceNumber();
            String deviceId = bindDTO.getDeviceId();
            String simCardNumber = bindDTO.getSimCardNumber();

            // 流水号
            Integer msgSno = DeviceHelper.getRegisterDevice(monitorId, deviceNumber);
            Integer commandTypeVal = getCommandType(commandType);
            String paramType = monitorId + "_" + commandTypeVal;
            String paramId = sendHelper.getLastSendParamID(monitorId, paramType, String.valueOf(commandTypeVal));

            if (msgSno != null) {
                switch (deviceFence) {
                    case 311:
                        deleteAllFences(deviceNumber, msgSno, simCardNumber, deviceId, 0x8601);
                        break;
                    case 312:
                        deleteAllFences(deviceNumber, msgSno, simCardNumber, deviceId, 0x8603);
                        break;
                    case 313:
                        deleteAllFences(deviceNumber, msgSno, simCardNumber, deviceId, 0x8605);
                        break;
                    case 314:
                        deleteAllFences(deviceNumber, msgSno, simCardNumber, deviceId, 0x8607);
                        break;
                    default:
                        break;
                }
                sendHelper
                    .updateParameterStatus(paramId, msgSno, 4, monitorId, String.valueOf(commandTypeVal), paramType);
            } else { // 设备离线
                sendHelper.updateParameterStatus(paramId, 0, 5, monitorId, String.valueOf(commandTypeVal), paramType);
            }
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }
}

package com.zw.platform.service.alarm.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.zw.app.domain.alarm.AlarmSetTypeAndAlarmTypeMappingRelationEnum;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.domain.MonitorDeviceTypeDO;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.event.ConfigUnBindEvent;
import com.zw.platform.basic.event.ConfigUpdateEvent;
import com.zw.platform.basic.repository.NewConfigDao;
import com.zw.platform.basic.service.IpAddressService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.commons.ParallelWorker;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.core.SendParam;
import com.zw.platform.domain.enmu.ProtocolEnum;
import com.zw.platform.domain.systems.Directive;
import com.zw.platform.domain.systems.form.DirectiveForm;
import com.zw.platform.domain.vas.alram.AlarmLinkageDTO;
import com.zw.platform.domain.vas.alram.AlarmLinkageParam;
import com.zw.platform.domain.vas.alram.AlarmParameter;
import com.zw.platform.domain.vas.alram.AlarmParameterDetailsDTO;
import com.zw.platform.domain.vas.alram.AlarmParameterSetting;
import com.zw.platform.domain.vas.alram.AlarmParameterSettingDTO;
import com.zw.platform.domain.vas.alram.AlarmSetting;
import com.zw.platform.domain.vas.alram.AlarmSettingReferentDTO;
import com.zw.platform.domain.vas.alram.AlarmType;
import com.zw.platform.domain.vas.alram.IoAlarmSettingDTO;
import com.zw.platform.domain.vas.alram.IoVehicleConfigInfo;
import com.zw.platform.domain.vas.alram.MsgParamDTO;
import com.zw.platform.domain.vas.alram.Msgparam;
import com.zw.platform.domain.vas.alram.OutputControl;
import com.zw.platform.domain.vas.alram.OutputControlDTO;
import com.zw.platform.domain.vas.alram.OutputControlSend;
import com.zw.platform.domain.vas.alram.OutputControlSendInfo;
import com.zw.platform.domain.vas.alram.PhotoDTO;
import com.zw.platform.domain.vas.alram.PhotoParam;
import com.zw.platform.domain.vas.alram.SpecialAlarmDO;
import com.zw.platform.domain.vas.alram.form.AlarmParameterSettingForm;
import com.zw.platform.domain.vas.alram.query.AlarmSettingQuery;
import com.zw.platform.domain.vas.switching.IoVehicleConfig;
import com.zw.platform.domain.vas.switching.SwitchType;
import com.zw.platform.event.ConfigUnbindVehicleEvent;
import com.zw.platform.manager.dto.LinkageParamDTO;
import com.zw.platform.manager.url.AlarmLinkageUrlEnum;
import com.zw.platform.push.controller.SubscibeInfo;
import com.zw.platform.push.controller.SubscibeInfoCache;
import com.zw.platform.push.controller.UserCache;
import com.zw.platform.push.handler.device.DeviceHelper;
import com.zw.platform.push.mqserver.ZMQFencePub;
import com.zw.platform.repository.modules.ParameterDao;
import com.zw.platform.repository.realTimeVideo.RecordingSettingDao;
import com.zw.platform.repository.vas.AlarmSearchDao;
import com.zw.platform.repository.vas.AlarmSettingDao;
import com.zw.platform.repository.vas.IoVehicleConfigDao;
import com.zw.platform.repository.vas.SwitchTypeDao;
import com.zw.platform.service.alarm.AlarmSearchService;
import com.zw.platform.service.alarm.AlarmSettingService;
import com.zw.platform.service.core.F3SendStatusProcessService;
import com.zw.platform.service.reportManagement.impl.LogSearchServiceImpl;
import com.zw.platform.service.sendTxt.SendTxtService;
import com.zw.platform.util.AssembleUtil;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.MsgUtil;
import com.zw.platform.util.SendHelper;
import com.zw.platform.util.StrUtil;
import com.zw.platform.util.common.AlarmParameterUtil;
import com.zw.platform.util.common.AlarmTypeUtil;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MethodLog;
import com.zw.platform.util.common.RedisQueryUtil;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.protocol.msg.t808.T808Message;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import com.zw.ws.entity.t808.parameter.ParamItem;
import com.zw.ws.entity.t808.simcard.T808Msg8106;
import com.zw.ws.impl.WsAlarmService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinTask;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.zw.platform.domain.vas.alram.AlarmLinkageDTO.ALARM_HANDLE_LINKAGE_NON_CHECK;
import static com.zw.platform.domain.vas.alram.AlarmLinkageDTO.HANDLE_TYPE_MSG;
import static com.zw.platform.domain.vas.alram.AlarmLinkageDTO.HANDLE_TYPE_PHOTO;
import static com.zw.platform.domain.vas.alram.MsgParamDTO.MESSAGE_TYPE_ONE_NOTIFICATION;
import static com.zw.platform.domain.vas.alram.MsgParamDTO.MESSAGE_TYPE_TWO_NOTIFICATION;
import static com.zw.platform.domain.vas.alram.MsgParamDTO.TEXT_TYPE_NOTIFICATION;

@Service
public class AlarmSettingServiceImpl implements AlarmSettingService, IpAddressService {
    private static final Logger log = LogManager.getLogger(AlarmSettingServiceImpl.class);

    @Autowired
    private AlarmSettingDao alarmSettingDao;

    @Autowired
    private AlarmSearchDao alarmSearchDao;

    @Autowired
    private UserService userService;

    @Autowired
    private ParameterDao parameterDao;

    @Autowired
    private WsAlarmService wsAlarmService;

    @Autowired
    private LogSearchServiceImpl logSearchServiceImpl;

    @Autowired
    private RecordingSettingDao recordingSettingDao;

    @Autowired
    private SwitchTypeDao switchTypeDao;

    @Autowired
    private IoVehicleConfigDao ioVehicleConfigDao;

    @Autowired
    private NewConfigDao newConfigDao;

    @Autowired
    private F3SendStatusProcessService f3SendStatusProcessService;

    @Autowired
    private SendTxtService sendTxtService;

    @Autowired
    private SendHelper sendHelper;

    @Autowired
    private AlarmSearchService alarmSearchService;

    @Value("${terminal.off.line}")
    private String terminalOffLine;

    private static final Set<String> IO_NAME = Sets.newHashSet("终端I/O异常", "I/O采集1异常", "I/O采集2异常");

    /**
     * 路网限速pos
     */
    private static final String ROAD_NETWORK_POS = "164";
    /**
     * 超速报警pos
     */
    private static final String SPEED_LIMIT_POS = "1";

    @Override
    public Page<AlarmSetting> findAlarmSetting(AlarmSettingQuery query) {
        String orgId = query.getGroupId();
        String assignmentId = query.getAssignmentId();
        String deviceType = query.getDeviceType();
        String simpleQueryParam = query.getSimpleQueryParam();
        // 根据条件获取有效车id
        List<String> vehicleIds =
            userService.getValidVehicleId(orgId, assignmentId, deviceType, simpleQueryParam, null, true);
        int listSize = vehicleIds.size();
        // 当前页
        int curPage = query.getPage().intValue();
        // 每页条数
        int pageSize = query.getLimit().intValue();
        // 遍历开始条数
        int lst = (curPage - 1) * pageSize;
        // 遍历条数
        int ps = pageSize > (listSize - lst) ? listSize : (pageSize * curPage);
        List<String> pageVehicleIdList = vehicleIds.subList(lst, ps);
        Map<String, BindDTO> bindInfoMap = VehicleUtil.batchGetBindInfosByRedis(pageVehicleIdList);
        // 设置对象类型名称
        userService.setObjectTypeName(bindInfoMap.values());

        // 查询当前分页中的车辆是否设置了报警参数
        Set<String> bindAlarmSettingList = CollectionUtils.isEmpty(pageVehicleIdList) ? Collections.emptySet() :
            alarmSettingDao.findByVehicleIds(pageVehicleIdList);

        Map<String, List<Directive>> monitorDirectiveListMap =
            CollectionUtils.isEmpty(pageVehicleIdList) ? new HashMap<>(4) :
                parameterDao.findDirectiveByMoIdAndType(pageVehicleIdList, "6").stream()
                    .collect(Collectors.groupingBy(Directive::getMonitorObjectId));

        List<AlarmSetting> resultList = new ArrayList<>();
        for (String moId : pageVehicleIdList) {
            BindDTO bindDTO = bindInfoMap.get(moId);
            AlarmSetting alarmSetting = new AlarmSetting();
            alarmSetting.setVehicleType(bindDTO.getObjectTypeName());
            alarmSetting.setVehicleId(moId);
            alarmSetting.setVId(moId);
            alarmSetting.setBrand(bindDTO.getName());
            alarmSetting.setDeviceType(bindDTO.getDeviceType());
            alarmSetting.setGroups(bindDTO.getOrgName());

            List<Directive> directives = monitorDirectiveListMap.get(moId);
            if (CollectionUtils.isNotEmpty(directives)) {
                directives.stream()
                    .filter(obj -> Objects.equals(obj.getParameterName(), moId + "_alarmSettingType"))
                    .max(Comparator.comparing(Directive::getCreateDataTime))
                    .ifPresent(obj -> {
                        alarmSetting.setParamId(obj.getId());
                        alarmSetting.setStatus(obj.getStatus());
                    });
            }
            // 是否可以下发
            if (bindAlarmSettingList.contains(moId)) {
                alarmSetting.setSettingUp(true);
            }
            resultList.add(alarmSetting);
        }
        return RedisQueryUtil.getListToPage(resultList, query, listSize);
    }

    @Override
    public List<AlarmSetting> findVehicleAlarmSetting() {
        List<AlarmSetting> list = new ArrayList<>();
        String uuid = userService.getCurrentUserUuid();
        // 获取当前用户所属组织及下级组织
        List<String> orgList = userService.getCurrentUserOrgIds();
        if (StringUtils.isNotBlank(uuid) && !orgList.isEmpty()) {
            list = alarmSettingDao.findVehicleAlarmSetting(uuid, orgList);
            // 处理result，将groupId对应的groupName给result相应的值赋上
            setGroupNameByGroupId(list);
        }
        return list;
    }

    public void setGroupNameByGroupId(List<AlarmSetting> result) {
        if (CollectionUtils.isEmpty(result)) {
            return;
        }
        Set<String> moIds = result.stream().map(AlarmSetting::getVehicleId).collect(Collectors.toSet());
        Map<String, BindDTO> bindInfoMap = VehicleUtil.batchGetBindInfosByRedis(moIds);
        Map<String, List<AlarmSetting>> monitorAlarmSettingListMap = bindInfoMap.isEmpty() ? new HashMap<>(4) :
            alarmSettingDao.findByMoIds(bindInfoMap.keySet()).stream()
                .collect(Collectors.groupingBy(AlarmSetting::getVehicleId));

        Map<String, List<Directive>> monitorDirectiveListMap =
            bindInfoMap.isEmpty() ? new HashMap<>(4) :
                parameterDao.findDirectiveByMoIdAndType(bindInfoMap.keySet(), "6").stream()
                    .collect(Collectors.groupingBy(Directive::getMonitorObjectId));

        for (AlarmSetting parameter : result) {
            String vehicleId = parameter.getVehicleId();
            BindDTO bindDTO = bindInfoMap.get(vehicleId);
            parameter.setGroups(bindDTO.getOrgName());

            if (StringUtils.isBlank(vehicleId)) {
                continue;
            }
            // 下发状态
            List<AlarmSetting> settingList = monitorAlarmSettingListMap.get(vehicleId);
            if (CollectionUtils.isEmpty(settingList)) {
                continue;
            }
            StringBuilder parameterName = new StringBuilder();
            for (AlarmSetting setting : settingList) {
                // 不下发
                if ("0".equals(setting.getSendFlag())) {
                    continue;
                }
                parameterName.append(setting.getId()).append(",");
            }
            if (StringUtils.isBlank(parameterName.toString())) {
                continue;
            }
            parameterName = new StringBuilder(parameterName.substring(0, (parameterName.length() - 1)));
            List<Directive> directiveList = monitorDirectiveListMap.get(vehicleId);
            Directive param = null;
            if (CollectionUtils.isNotEmpty(directiveList)) {
                String finalParameterName = parameterName.toString();
                param = directiveList
                    .stream()
                    .filter(obj -> Objects.equals(obj.getParameterName(), finalParameterName))
                    .max(Comparator.comparing(Directive::getCreateDataTime))
                    .orElse(null);
            }
            if (param != null) {
                parameter.setParamId(param.getId());
                parameter.setStatus(param.getStatus());
            }
        }
    }

    @Override
    public JsonResultBean deleteByVehicleIds(List<String> vehicleIds) {
        boolean result = alarmSettingDao.deleteByVehicleIds(vehicleIds);
        if (!result) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        StringBuilder message = new StringBuilder();
        String brand = "";
        String plateColor = "";
        //需要下发消息的集合
        List<JSONObject> needSendAddMsgList = new ArrayList<>();
        List<String> needSendRemoveMsgList = new ArrayList<>();
        Set<String> moIdSet = new HashSet<>(vehicleIds);
        Map<String, BindDTO> bindInfoMap = VehicleUtil.batchGetBindInfosByRedis(moIdSet);
        // 删除车辆报警设置缓存
        deleteVehicleAlarmCaches(moIdSet, bindInfoMap, needSendAddMsgList, needSendRemoveMsgList);
        List<String> parameterNames = new ArrayList<>();
        for (String vehicleId : moIdSet) {
            parameterNames.add(vehicleId + "_alarmSettingType");
            BindDTO bindDTO = bindInfoMap.get(vehicleId);
            if (bindDTO == null) {
                continue;
            }
            brand = bindDTO.getName();
            Integer plateColorInt = bindDTO.getPlateColor();
            if (plateColorInt != null) {
                plateColor = plateColorInt.toString();
            }
            String endString = " ) 恢复报警参数设置 ";
            message.append("监控对象 : ").append(brand).append(" ( @").append(bindDTO.getOrgName()).append(endString);
        }
        if (CollectionUtils.isNotEmpty(needSendAddMsgList)) {
            needSendAddMsgList.forEach(jsonObject ->
                WebSubscribeManager.getInstance().sendMsgToAll(jsonObject, ConstantUtil.WEB_ALARM_ADD));
        }
        if (CollectionUtils.isNotEmpty(needSendRemoveMsgList)) {
            needSendRemoveMsgList.forEach(str ->
                WebSubscribeManager.getInstance().sendMsgToAll(str, ConstantUtil.WEB_ALARM_REMOVE));
        }
        // 删除指令
        if (CollectionUtils.isNotEmpty(parameterNames)) {
            // 解决锁问题，按主键更新/删除
            final List<String> ids = parameterDao.listIdByParameterName(parameterNames);
            if (ids != null && ids.size() != 0) {
                parameterDao.deleteByIds(ids);
            }
        }
        String ipAddress = getIpAddress();
        if (vehicleIds.size() == 1) {
            logSearchServiceImpl.addLog(ipAddress, message.toString(), "3", "", brand, plateColor);
        } else {
            // 记录日志
            logSearchServiceImpl.addLog(ipAddress, message.toString(), "3", "batch", "批量恢复默认报警参数设置");
        }
        // 通知storm更新数据库数据
        ZMQFencePub.pubChangeFence("9");
        // 通知storm更新缓存数据
        ZMQFencePub.pubChangeFence("12");
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    @EventListener
    public void listenConfigUpdate(ConfigUpdateEvent updateEvent) {
        BindDTO curBind = updateEvent.getCurBindDTO();
        BindDTO oldBind = updateEvent.getOldBindDTO();
        //终端或监控对象进行了改变
        if (!Objects.equals(curBind.getDeviceId(), oldBind.getDeviceId()) || !Objects
            .equals(curBind.getId(), oldBind.getId())) {
            deleteByVehicleIds(Collections.singletonList(oldBind.getId()));
        }
    }


    private void deleteVehicleAlarmCaches(Set<String> vehicleIds, Map<String, BindDTO> bindInfoMap,
        List<JSONObject> needSendAddMsgList, List<String> needSendRemoveMsgList) {
        List<RedisKey> needDelRedisKeyList = new ArrayList<>();
        Map<RedisKey, String> needAddKeyValueMap = new HashMap<>(16);
        //所有的监控对象视频报警参数集合
        Map<String, List<AlarmParameterSettingForm>> monitorVideoAlarmSettingMap =
            recordingSettingDao.getVideoAlarmsByVehicleIds(new ArrayList<>(vehicleIds)).stream()
                .collect(Collectors.groupingBy(AlarmParameterSettingForm::getVehicleId));
        JSONObject defaultType = WebSubscribeManager.getInstance().getDefaultType(2);
        for (String vehicleId : vehicleIds) {
            BindDTO bindDTO = bindInfoMap.get(vehicleId);
            if (bindDTO == null) {
                continue;
            }
            String deviceId = bindDTO.getDeviceId();
            RedisKey monitorDeviceAlarmPushRedisKey = HistoryRedisKeyEnum.ALARM_PUSH_SET_DEVICE_ID.of(deviceId);
            String key3 = monitorDeviceAlarmPushRedisKey.get();
            needDelRedisKeyList.add(monitorDeviceAlarmPushRedisKey);
            needSendRemoveMsgList.add(key3);

            RedisKey monitorIdAlarmPushRedisKey = HistoryRedisKeyEnum.ALARM_PUSH_SET_MONITOR_ID.of(vehicleId);
            String key4 = monitorIdAlarmPushRedisKey.get();
            needDelRedisKeyList.add(monitorIdAlarmPushRedisKey);
            needSendRemoveMsgList.add(key4);

            needDelRedisKeyList.add(HistoryRedisKeyEnum.MONITOR_ALARM_PARAM.of(vehicleId, "76"));
            needDelRedisKeyList.add(HistoryRedisKeyEnum.MONITOR_ALARM_PARAM.of(vehicleId, "77"));
            needDelRedisKeyList.add(HistoryRedisKeyEnum.MONITOR_ALARM_PARAM.of(vehicleId, "82"));
            needDelRedisKeyList.add(HistoryRedisKeyEnum.MONITOR_ALARM_PARAM.of(vehicleId, "150"));
            needDelRedisKeyList.add(HistoryRedisKeyEnum.MONITOR_ALARM_PARAM.of(vehicleId, "151"));
            needDelRedisKeyList.add(HistoryRedisKeyEnum.MONITOR_ALARM_PARAM.of(vehicleId, "209"));

            needDelRedisKeyList.add(HistoryRedisKeyEnum.MONITOR_IO_ALARM_SETTING.of(vehicleId));
            // 查询是否设置808视频报警
            List<AlarmParameterSettingForm> videoAlarmParams = monitorVideoAlarmSettingMap.get(vehicleId);
            if (CollectionUtils.isEmpty(videoAlarmParams)) {
                continue;
            }
            // 推送设置存缓存
            WebSubscribeManager.getInstance()
                .pushAlarmSetAndSaveAlarmSetValue(vehicleId, deviceId, videoAlarmParams, defaultType,
                    needSendAddMsgList, needAddKeyValueMap, false);
        }
        RedisHelper.delete(needDelRedisKeyList);
        RedisHelper.setStringMap(needAddKeyValueMap);
    }

    @Override
    public List<AlarmSetting> findByVehicleId(String vehicleId) {
        if (StringUtils.isNotBlank(vehicleId)) {
            return alarmSettingDao.findByVehicleId(vehicleId);
        }
        return null;
    }

    @Override
    public List<AlarmSetting> findById(String vehicleId) throws Exception {
        if (StringUtils.isNotBlank(vehicleId)) {
            return alarmSettingDao.findById(vehicleId);
        }
        return null;
    }

    @Override
    public List<AlarmSetting> findAllAlarmParameter() {
        return alarmSettingDao.findAllAlarmParameter();
    }

    @Override
    public JsonResultBean sendAlarm(ArrayList<JSONObject> paramList) {
        StringBuilder msg = new StringBuilder();
        String vehicleId = "";
        List<String> vehicleIds = new ArrayList<>();
        for (JSONObject jsonObject : paramList) {
            vehicleIds.add(jsonObject.getString("vehicleId"));
        }
        if (CollectionUtils.isEmpty(vehicleIds)) {
            return new JsonResultBean(JsonResultBean.FAULT, "车辆id为空！");
        }
        Map<String, List<AlarmSetting>> vehicleAlarmSettingMap = alarmSettingDao.findAlarmSettingByBatch(vehicleIds)
            .stream()
            .collect(Collectors.groupingBy(AlarmSetting::getVehicleId));
        Map<String, BindDTO> bindInfoMap = VehicleUtil.batchGetBindInfosByRedis(vehicleIds);
        if (MapUtils.isEmpty(bindInfoMap)) {
            return new JsonResultBean(JsonResultBean.FAULT, "车辆未绑定！");
        }
        List<AlarmSetting> alarmSettingList;
        String paramId;
        for (JSONObject obj : paramList) {
            String finalVehicleId = obj.getString("vehicleId");
            vehicleId = finalVehicleId;
            // 判断当前车辆是否绑定报警参数设置
            alarmSettingList = vehicleAlarmSettingMap.get(vehicleId);
            if (CollectionUtils.isEmpty(alarmSettingList)) {
                continue;
            }
            paramId = obj.getString("paramId");
            // 参数类型
            String paramType = "6";
            String parameterName = finalVehicleId + "_alarmSettingType";
            BindDTO bindDTO = bindInfoMap.get(vehicleId);
            if (bindDTO == null) {
                continue;
            }
            // 报警参数设置下发 TODO 做成批量查询、批量更新
            sendAlarmSetting(bindDTO, paramId, vehicleId, paramType, parameterName, alarmSettingList);
            msg.append("监控对象 : ").append(bindDTO.getName()).append(" ( @").append(bindDTO.getOrgName())
                .append(" ) 报警参数下发 <br/>");
        }
        String ipAddress = getIpAddress();
        if (!msg.toString().isEmpty()) {
            if (paramList.size() == 1) {
                String[] vehicle = logSearchServiceImpl.findCarMsg(vehicleId);
                logSearchServiceImpl.addLog(ipAddress, msg.toString(), "2", "", vehicle[0], vehicle[1]);
            } else {
                logSearchServiceImpl.addLog(ipAddress, msg.toString(), "2", "batch", "监控对象报警参数批量下发");
            }
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    /**
     * 报警参数设置下发
     * @param bindDTO          绑定信息
     * @param paramId          下发id
     * @param vehicleId        车id
     * @param paramType        参数类型
     * @param parameterName    报警设置id
     * @param alarmSettingList void
     * @author wangying
     * @since 2016年12月8日 上午11:19:13
     */
    private void sendAlarmSetting(BindDTO bindDTO, String paramId, String vehicleId, String paramType,
        String parameterName, List<AlarmSetting> alarmSettingList) {
        String deviceNumber = bindDTO.getDeviceNumber();
        // 序列号
        Integer msgSN = DeviceHelper.getRegisterDevice(vehicleId, deviceNumber);
        if (msgSN != null) { // 设备已经注册
            // 下发参数
            wsAlarmService.alarmSettingCompose(alarmSettingList, msgSN, bindDTO);
            int status = 4; // 已下发
            // 油箱绑定下发
            paramId = updateParameterStatus(paramId, msgSN, status, vehicleId, paramType, parameterName);
            SendParam sendParam = new SendParam();
            sendParam.setMsgSNACK(msgSN);
            sendParam.setParamId(paramId);
            sendParam.setVehicleId(vehicleId);
            f3SendStatusProcessService.updateSendParam(sendParam, 1);
        } else { // 设备未注册
            int status = 5; // 设备为注册
            msgSN = 0;
            // 油箱绑定下发
            updateParameterStatus(paramId, msgSN, status, vehicleId, paramType, parameterName);
        }
    }

    /**
     * 修改参数下发表数据
     * @param paramId       参数下发id
     * @param msgSN         流水号
     * @param vehicleId     车辆id
     * @param paramType     参数下发类型
     * @param parameterName 绑定id
     * @author wangying
     */
    public String updateParameterStatus(String paramId, int msgSN, int status, String vehicleId, String paramType,
        String parameterName) {
        // int status = 4; // 已下发
        if (StrUtil.isNotBlank(paramId)) {
            List<String> paramIds = new ArrayList<>();
            paramIds.add(paramId);
            // 重新下发 ，修改流水号
            // 1 : 下发未回应
            parameterDao.updateMsgSNAndNameById(paramIds, msgSN, status, parameterName, 1);
        } else {
            DirectiveForm form = new DirectiveForm();
            form.setDownTime(new Date());
            form.setMonitorObjectId(vehicleId);
            form.setStatus(status);
            form.setParameterType(paramType);
            form.setParameterName(parameterName);
            form.setSwiftNumber(msgSN);
            form.setReplyCode(1);
            // 批量新增
            parameterDao.addDirective(form);
            paramId = form.getId();
        }
        return paramId;
    }

    @Override
    public JsonResultBean updateAlarmParameterByBatch(String ids, String checkedParams, String deviceType) {
        // 处理入参
        List<AlarmParameterSettingForm> list = JSON.parseArray(checkedParams, AlarmParameterSettingForm.class);
        final Set<String> vehicleIds = Arrays.stream(ids.split(",")).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(list) || CollectionUtils.isEmpty(vehicleIds)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        roadNetWorkBuild(list);

        // 过滤出io报警
        final Map<Boolean, List<AlarmParameterSettingForm>> ioAlarmSettings = list.stream().filter(
            info -> StringUtils.isNotEmpty(info.getPos()) && AlarmTypeUtil.IO_ALARM
                .contains(Integer.valueOf(info.getPos().trim())))
            .collect(Collectors.partitioningBy(info -> StringUtils.isBlank(info.getParameterValue())));
        // 过滤掉未设置的io报警
        list.removeAll(ioAlarmSettings.get(Boolean.TRUE));

        // 查询当前设置
        final List<String> types = Arrays
            .asList("sensorAlarm", "alert", "faultAlarm", "otherAlarm", "f3longAlarm", "vehicleAlarm", "driverAlarm",
                "platAlarm", "asolongAlarm", "f3longPlatAlarm", "asolongPlatAlarm", "peoplePlatAlarm", "peopleAlarm",
                "adasAlarm", "ioAlarm", "highPrecisionAlarm");
        final Map<String, Object> params = ImmutableMap.of("vehicleIds", vehicleIds, "types", types);
        // vehicleId -> alarmParameterId ->【有序】AlarmParameterSetting()
        final Map<String, Map<String, AlarmParameterSetting>> currentVehicleSettings =
            alarmSettingDao.findSettingsByVehicleIds(params).stream().collect(Collectors
                .groupingBy(AlarmParameterSetting::getVehicleId, Collectors
                    .toMap(AlarmParameterSetting::getAlarmParameterId, Function.identity(), (o, p) -> o,
                        LinkedHashMap::new)));

        // 参数新增、删除列表
        final List<AlarmParameterSetting> settingsForAdd = new ArrayList<>();
        final List<String> settingIdsForRemove = new ArrayList<>();
        // 需要修改下发状态的条件集合
        final List<Map<String, String>> needUpdateStatusConditions = new ArrayList<>();
        List<String> parameterNames = new ArrayList<>();

        for (String vehicleId : vehicleIds) {
            final Map<String, AlarmParameterSetting> currentVehicleSetting =
                currentVehicleSettings.getOrDefault(vehicleId, Collections.emptyMap());
            // 对比得到新增、删除列表
            this.calcChangeList(list, ioAlarmSettings.get(Boolean.TRUE), settingsForAdd, settingIdsForRemove, vehicleId,
                currentVehicleSetting);
            // 需要修改下发状态的条件
            this.calcUpdateStatusConditions(needUpdateStatusConditions, vehicleId, currentVehicleSetting);
            parameterNames.add(vehicleId + "_alarmSettingType");
        }

        // 前置耗时操作
        final ForkJoinTask<?> insertTask =
            CollectionUtils.isEmpty(settingsForAdd) ? null : this.fastInsertSettings(settingsForAdd);
        //需要下发消息的集合
        List<JSONObject> needSendAddMsgList = new ArrayList<>();
        Map<String, BindDTO> bindInfoMap = VehicleUtil.batchGetBindInfosByRedis(vehicleIds);
        //所有的监控对象视频报警参数集合
        final Map<String, List<AlarmParameterSettingForm>> videoAlarmSettings =
            recordingSettingDao.getVideoAlarmsByVehicleIds(vehicleIds).stream()
                .collect(Collectors.groupingBy(AlarmParameterSettingForm::getVehicleId));
        final JSONObject defaultType0 = WebSubscribeManager.getInstance().getDefaultType(0);
        final JSONObject defaultType1 = WebSubscribeManager.getInstance().getDefaultType(1);
        StringBuilder logMsg = new StringBuilder();
        Map<RedisKey, String> needAddKeyValueMap = new HashMap<>(16);
        List<RedisKey> needDelRedisKeyList = new ArrayList<>();
        for (String vehicleId : vehicleIds) {
            BindDTO bindDTO = bindInfoMap.get(vehicleId);
            if (bindDTO == null) {
                continue;
            }
            // 808视频报警
            List<AlarmParameterSettingForm> videoAlarmParams = videoAlarmSettings.get(vehicleId);
            JSONObject defaultType = defaultType1;
            List<AlarmParameterSettingForm> alarmSettingList = new ArrayList<>(list);
            if (CollectionUtils.isNotEmpty(videoAlarmParams)) {
                alarmSettingList.addAll(videoAlarmParams);
                defaultType = defaultType0;
            }
            String deviceId = bindDTO.getDeviceId();
            //推送报警设置缓存和保存报警设置值
            WebSubscribeManager.getInstance()
                .pushAlarmSetAndSaveAlarmSetValue(vehicleId, deviceId, alarmSettingList, defaultType,
                    needSendAddMsgList, needAddKeyValueMap, true);
            // 保存io报警参数设置
            this.saveIoAlarmSettingToRedisByBatch(vehicleId, ioAlarmSettings.get(Boolean.FALSE), needAddKeyValueMap,
                needDelRedisKeyList);
            logMsg.append("监控对象 : ").append(bindDTO.getName()).append(" ( @").append(bindDTO.getOrgName())
                .append(" ) 报警参数设置 ");
        }
        RedisHelper.delete(needDelRedisKeyList);
        RedisHelper.setStringMap(needAddKeyValueMap);
        needSendAddMsgList.forEach(
            jsonObject -> WebSubscribeManager.getInstance().sendMsgToAll(jsonObject, ConstantUtil.WEB_ALARM_ADD));
        if (CollectionUtils.isNotEmpty(settingIdsForRemove)) {
            alarmSettingDao.deleteAlarmSettingByBatch(settingIdsForRemove);
        }
        if (CollectionUtils.isNotEmpty(needUpdateStatusConditions)) {
            parameterDao.updateStatusByBatch(6, needUpdateStatusConditions);
        }
        if (CollectionUtils.isNotEmpty(parameterNames)) {
            // 解决锁问题，按主键更新/删除
            final List<String> idList = parameterDao.listIdByParameterName(parameterNames);
            if (CollectionUtils.isNotEmpty(idList)) {
                parameterDao.deleteByIds(idList);
            }
        }
        String ipAddress = getIpAddress();
        // 记录日志
        if (logMsg.length() > 0) {
            if (vehicleIds.size() > 1) {
                logSearchServiceImpl.addLog(ipAddress, logMsg.toString(), "3", "batch", "批量设置监控对象报警参数");
            } else {
                BindDTO bindDTO = new ArrayList<>(bindInfoMap.values()).get(0);
                String brand = bindDTO.getName();
                Integer plateColorInt = bindDTO.getPlateColor();
                logSearchServiceImpl.addLog(ipAddress, logMsg.toString(), "3", "", brand,
                    plateColorInt == null ? "" : plateColorInt.toString());
            }
            // 通知storm更新数据库数据
            ZMQFencePub.pubChangeFence("9");
            // 通知storm更新缓存数据
            ZMQFencePub.pubChangeFence("12");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        // 等待耗时任务结束
        if (null != insertTask) {
            try {
                insertTask.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }


    private void roadNetWorkBuild(List<AlarmParameterSettingForm> list) {
        Map<String, AlarmParameterSettingForm> map = new HashMap<>();
        for (AlarmParameterSettingForm parameterSettingForm : list) {
            String pos = parameterSettingForm.getPos();
            if (map.size() > 2) {
                break;
            }
            if (ROAD_NETWORK_POS.equals(pos) || SPEED_LIMIT_POS.equals(pos)) {
                map.put(pos, parameterSettingForm);
            }
        }
        Optional.ofNullable(map.get(ROAD_NETWORK_POS)).ifPresent(o ->
            Optional.ofNullable(map.get(SPEED_LIMIT_POS)).ifPresent(p ->
                o.setParameterValue(p.getParameterValue())));
    }

    /**
     * 数据多并行新增 or 数据少直接新增
     * @param settingsForAdd 待新增数据
     * @return 并行时：线程池、任务；非并行时：null
     */
    private ForkJoinTask<?> fastInsertSettings(List<AlarmParameterSetting> settingsForAdd) {
        settingsForAdd.sort(Comparator.comparing(AlarmParameterSetting::getId));
        // 超过{bestBatchSize}条insert时，并行处理
        final int bestBatchSize = 1000;
        final int totalInsertSize = settingsForAdd.size();
        if (totalInsertSize <= bestBatchSize) {
            alarmSettingDao.addAlarmSettingByBatch(settingsForAdd);
            return null;
        }
        return ParallelWorker.invokeAsync(settingsForAdd, 1000,
            settings -> alarmSettingDao.addAlarmSettingByBatch(settings));
    }

    /**
     * 需要修改下发状态的条件
     */
    private void calcUpdateStatusConditions(List<Map<String, String>> needUpdateStatusConditions, String vehicleId,
        Map<String, AlarmParameterSetting> currentVehicleSetting) {
        Collection<AlarmParameterSetting> alarmParameters = currentVehicleSetting.values();
        if (CollectionUtils.isNotEmpty(alarmParameters)) {
            StringBuilder builder = new StringBuilder();
            alarmParameters.stream().filter(o -> !"0".equals(o.getSendFlag()))
                .forEach(o -> builder.append(o.getId()).append(","));
            if (builder.length() > 0) {
                builder.deleteCharAt(builder.length() - 1);
            }
            Map<String, String> condition = new HashMap<>(3);
            condition.put("monitorObjectId", vehicleId);
            condition.put("parameterName", builder.toString());
            condition.put("parameterType", "6");
            needUpdateStatusConditions.add(condition);
        }
    }

    /**
     * 对比得到新增、删除列表
     */
    private void calcChangeList(List<AlarmParameterSettingForm> list, List<AlarmParameterSettingForm> ioAlarmSettings,
        List<AlarmParameterSetting> settingsForAdd, List<String> settingIdsForRemove, String vehicleId,
        Map<String, AlarmParameterSetting> currentVehicleSetting) {
        for (AlarmParameterSettingForm newSetting : list) {
            String alarmParameterId = newSetting.getAlarmParameterId();
            String parameterValue = newSetting.getParameterValue();
            Integer alarmPush = newSetting.getAlarmPush();
            AlarmParameterSetting currentSetting = currentVehicleSetting.get(alarmParameterId);
            final boolean exist = null != currentSetting;
            final boolean different = null == currentSetting
                || !Objects.equals(currentSetting.getAlarmPush(), alarmPush)
                || !Objects.equals(currentSetting.getParameterValue(), parameterValue);
            if (different) {
                settingsForAdd.add(AlarmParameterSetting.of(vehicleId, alarmParameterId, parameterValue, alarmPush));
            }
            if (different && exist) {
                settingIdsForRemove.add(currentSetting.getId());
            }
        }
        // 删除此次不设置但原本存在的IO报警（特殊处理）
        ioAlarmSettings.forEach(o -> Optional.ofNullable(currentVehicleSetting.get(o.getAlarmParameterId()))
            .ifPresent(p -> settingIdsForRemove.add(p.getId())));
    }

    /**
     * 保存io报警参数设置
     * @param vehicleId           监控对象id
     * @param ioAlarmSettings      io报警参数设置
     * @param needAddKeyValueMap    redis需要新增的
     * @param needDelRedisKeyList   redis需要删除的
     */
    private void saveIoAlarmSettingToRedisByBatch(String vehicleId, List<AlarmParameterSettingForm> ioAlarmSettings,
        Map<RedisKey, String> needAddKeyValueMap, List<RedisKey> needDelRedisKeyList) {
        RedisKey ioAlarmSettingRedisKey = HistoryRedisKeyEnum.MONITOR_IO_ALARM_SETTING.of(vehicleId);
        needDelRedisKeyList.add(ioAlarmSettingRedisKey);
        if (CollectionUtils.isEmpty(ioAlarmSettings)) {
            return;
        }
        JSONObject jsonObject = new JSONObject();
        for (AlarmParameterSettingForm info : ioAlarmSettings) {
            JSONObject jsonInfo = new JSONObject();
            //报警推送（0、无 1、局部 2、全局）
            jsonInfo.put("alarmPush", info.getAlarmPush());
            //1:高电平为报警 2:低电平为报警
            String value = info.getParameterValue().trim();
            //转换成和协议一样 0:高电平为报警 1:低电平为报警
            Integer parameterValue = null;
            if ("1".equals(value)) {
                parameterValue = 0;
            } else if ("2".equals(value)) {
                parameterValue = 1;
            }
            jsonInfo.put("parameterValue", parameterValue);
            jsonObject.put(info.getPos().trim(), jsonInfo);
        }
        needAddKeyValueMap.put(ioAlarmSettingRedisKey, jsonObject.toJSONString());
    }

    @Override
    public List<AlarmParameterSettingForm> findParameterByVehicleId(String vehicleId) {
        return alarmSettingDao.findParameterByVehicleId(vehicleId);
    }

    @Override
    public boolean deleteAlarmSettingByVid(String vehicleId, Integer type) {
        if (StringUtils.isNotEmpty(vehicleId)) {
            if (Objects.isNull(type) || ConfigUnbindVehicleEvent.TYPE_SINGLE == type) {
                return alarmSettingDao.deleteAlarmSettingByVehicleId(vehicleId);
            } else {
                List<String> monitorIds = Arrays.asList(vehicleId.split(","));
                return alarmSettingDao.deleteBatchAlarmSettingByVehicleId(monitorIds);
            }
        }
        return false;
    }

    @EventListener
    public void updateVehicleUnbound(ConfigUnBindEvent event) {
        List<String> monitorIds = event.getUnbindList().stream().map(BindDTO::getId).collect(Collectors.toList());
        alarmSettingDao.deleteBatchAlarmSettingByVehicleId(monitorIds);
    }

    @MethodLog(name = "查询报警参数设置", description = "查询报警参数设置--------")
    @Override
    public List<AlarmSetting> findAlarmSetting(String vehicleId, String alarm) {
        List<String> alarmTypes = Arrays.asList(alarm.split(","));
        for (int i = 0; i < alarmTypes.size(); i++) {
            if (alarmTypes.get(i).contains("区域")) {
                alarmTypes.set(i, "进出区域");
            }
        }
        return alarmSettingDao.findAlarmByVidAndType(vehicleId, alarmTypes);
    }

    @Override
    public List<AlarmSetting> findReferVehicleByDeviceType(String deviceType) {
        if (StringUtils.isBlank(deviceType)) {
            return new ArrayList<>();
        }
        // 分组权限的绑定的监控对象id
        Set<String> monitorIds = userService.getCurrentUserMonitorIds();
        if (CollectionUtils.isEmpty(monitorIds)) {
            return new ArrayList<>();
        }
        //若协议类型为-1则为部标协议0,1
        if ("-1".equals(deviceType)) {
            deviceType = StringUtils.join(ProtocolEnum.PROTOCOL_TYPE_808_2011_2013_STR, ",");
        }
        if ("11".equals(deviceType)) {
            deviceType = StringUtils.join(ProtocolEnum.PROTOCOL_TYPE_808_2019_STR, ",");
        }
        // 获取设置了报警参数的车辆id
        Set<String> alarmSettingVehicleIds =
            RedisHelper.scanKeys(HistoryRedisKeyEnum.ALARM_PUSH_SET_MONITOR_ID.of("*")).stream()
                .map(str -> str.replaceFirst("4_", "")).collect(Collectors.toSet());

        // 与设置了报警参数的车辆id 取交集 -> 权限下设置了参数的车辆id
        monitorIds.retainAll(alarmSettingVehicleIds);

        List<String> deviceTypes = Arrays.stream(deviceType.split(",")).collect(Collectors.toList());
        Map<String, Set<String>> deviceTypeAndMoIdsMap = newConfigDao.getMonitorByDeviceTypes(deviceTypes).stream()
            .collect(Collectors.groupingBy(MonitorDeviceTypeDO::getDeviceType,
                Collectors.mapping(MonitorDeviceTypeDO::getMonitorId, Collectors.toSet())));

        Set<String> fullConditionMoIds = new HashSet<>();
        for (String dt : deviceTypes) {
            // 协议类型下的车辆
            Set<String> deviceTypeMoIds = deviceTypeAndMoIdsMap.get(dt);
            if (CollectionUtils.isEmpty(deviceTypeMoIds)) {
                continue;
            }
            // 与权限下设置了参数的车辆取交集 -> 权限内协议类型下设置了参数的车辆id
            deviceTypeMoIds.retainAll(monitorIds);
            if (CollectionUtils.isEmpty(deviceTypeMoIds)) {
                continue;
            }
            fullConditionMoIds.addAll(deviceTypeMoIds);
        }
        Map<String, BindDTO> bindInfoMap = VehicleUtil.batchGetBindInfosByRedis(fullConditionMoIds);
        return fullConditionMoIds
            .stream()
            .map(moId -> {
                BindDTO bindDTO = bindInfoMap.get(moId);
                return new AlarmSetting(moId, bindDTO.getName());
            }).collect(Collectors.toList());
    }

    @Override
    public List<AlarmSettingReferentDTO> getReferentList(String deviceType) {
        List<String> deviceTypeList = new ArrayList<>();
        // -1:交通部JT/T808-2013; 11:交通部JT/T808-2019; 5:BDTD-SM; 9:ASO; 10:F3超长待机;
        if ("-1".equals(deviceType)) {
            Collections.addAll(deviceTypeList, ProtocolEnum.PROTOCOL_TYPE_808_2011_2013_STR);
        } else if ("11".equals(deviceType)) {
            Collections.addAll(deviceTypeList, ProtocolEnum.PROTOCOL_TYPE_808_2019_STR);
        } else {
            deviceTypeList.add(deviceType);
        }
        // 终端类型下的车辆
        Set<String> deviceTypeMoIds =
            newConfigDao.getMonitorByDeviceTypes(deviceTypeList).stream().map(MonitorDeviceTypeDO::getMonitorId)
                .filter(StringUtils::isNotBlank).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(deviceTypeMoIds)) {
            return new ArrayList<>();
        }
        // 权限的绑定的监控对象id
        Set<String> currentUserMonitorIds = userService.getCurrentUserMonitorIds();
        if (CollectionUtils.isEmpty(currentUserMonitorIds)) {
            return new ArrayList<>();
        }
        // 与终端类型下的车辆id 取交集 -> 权限内终端类型下的车辆id
        currentUserMonitorIds.retainAll(deviceTypeMoIds);
        if (CollectionUtils.isEmpty(currentUserMonitorIds)) {
            return new ArrayList<>();
        }
        // 获取设置了报警参数的车辆id
        Set<String> alreadySettingMoIds =
            RedisHelper.scanKeys(HistoryRedisKeyEnum.ALARM_PUSH_SET_MONITOR_ID.of("*")).stream()
                .map(str -> str.replaceFirst("4_", "")).collect(Collectors.toSet());
        // 与设置了报警参数的车辆id 取交集 -> 权限内终端类型下设置了参数的车辆id
        currentUserMonitorIds.retainAll(alreadySettingMoIds);
        if (CollectionUtils.isEmpty(currentUserMonitorIds)) {
            return new ArrayList<>();
        }
        Map<String, BindDTO> bindInfoMap =
            VehicleUtil.batchGetBindInfosByRedis(currentUserMonitorIds, Lists.newArrayList("name"));
        return bindInfoMap.values().stream().filter(Objects::nonNull)
            .map(obj -> new AlarmSettingReferentDTO(obj.getId(), obj.getName())).collect(Collectors.toList());
    }

    @Override
    public AlarmParameterDetailsDTO getAlarmParameterSettingDetails(String moId) {
        List<AlarmParameterSettingForm> alarmParameterSettingFormList = alarmSettingDao.findParameterByVehicleId(moId);
        Map<String, List<AlarmParameterSettingForm>> alarmParameterSettingMap =
            alarmParameterSettingFormList.stream().collect(Collectors.groupingBy(AlarmParameterSettingForm::getType));
        AlarmParameterDetailsDTO alarmParameterDetailsDTO =
            AlarmParameterUtil.assemblePageDisplayData(alarmParameterSettingMap);
        // 交通部JT/T808 -> io报警
        List<AlarmParameterSettingForm> ioParameterSettingList = alarmSettingDao.findIoParameterByVehicleId(moId);
        assembleIoAlarmSetting(moId, alarmParameterDetailsDTO, ioParameterSettingList);
        return alarmParameterDetailsDTO;
    }

    @Override
    public JsonResultBean saveAlarmParameterSetting(String moId, String alarmParameterSettingJsonStr) {
        List<String> moIds = Arrays.stream(moId.split(",")).distinct().collect(Collectors.toList());
        List<AlarmParameterSettingForm> list = analysisAlarmSetting(moIds, alarmParameterSettingJsonStr);
        if (CollectionUtils.isEmpty(list)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        roadNetWorkBuild(list);

        // 过滤出io报警
        final Map<Boolean, List<AlarmParameterSettingForm>> ioAlarmSettings = list.stream().filter(
            info -> StringUtils.isNotEmpty(info.getPos()) && AlarmTypeUtil.IO_ALARM
                .contains(Integer.valueOf(info.getPos().trim())))
            .collect(Collectors.partitioningBy(info -> StringUtils.isBlank(info.getParameterValue())));
        // 过滤掉未设置的io报警
        list.removeAll(ioAlarmSettings.get(Boolean.TRUE));

        // 查询当前设置
        final List<String> types = Arrays
            .asList("sensorAlarm", "alert", "faultAlarm", "otherAlarm", "f3longAlarm", "vehicleAlarm", "driverAlarm",
                "platAlarm", "asolongAlarm", "f3longPlatAlarm", "asolongPlatAlarm", "peoplePlatAlarm", "peopleAlarm",
                "adasAlarm", "ioAlarm", "highPrecisionAlarm");
        final Map<String, Object> params = ImmutableMap.of("vehicleIds", moIds, "types", types);
        // vehicleId -> alarmParameterId ->【有序】AlarmParameterSetting()
        final Map<String, Map<String, AlarmParameterSetting>> currentVehicleSettings =
            alarmSettingDao.findSettingsByVehicleIds(params).stream().collect(Collectors
                .groupingBy(AlarmParameterSetting::getVehicleId, Collectors
                    .toMap(AlarmParameterSetting::getAlarmParameterId, Function.identity(), (o, p) -> o,
                        LinkedHashMap::new)));

        // 参数新增、删除列表
        final List<AlarmParameterSetting> settingsForAdd = new ArrayList<>();
        final List<String> settingIdsForRemove = new ArrayList<>();
        // 需要修改下发状态的条件集合
        final List<Map<String, String>> needUpdateStatusConditions = new ArrayList<>();
        List<String> parameterNames = new ArrayList<>();

        for (String vehicleId : moIds) {
            final Map<String, AlarmParameterSetting> currentVehicleSetting =
                currentVehicleSettings.getOrDefault(vehicleId, Collections.emptyMap());
            // 对比得到新增、删除列表
            this.calcChangeList(list, ioAlarmSettings.get(Boolean.TRUE), settingsForAdd, settingIdsForRemove, vehicleId,
                currentVehicleSetting);
            // 需要修改下发状态的条件
            this.calcUpdateStatusConditions(needUpdateStatusConditions, vehicleId, currentVehicleSetting);
            parameterNames.add(vehicleId + "_alarmSettingType");
        }

        // 前置耗时操作
        final ForkJoinTask<?> insertTask =
            CollectionUtils.isEmpty(settingsForAdd) ? null : this.fastInsertSettings(settingsForAdd);
        //需要下发消息的集合
        List<JSONObject> needSendAddMsgList = new ArrayList<>();
        Map<String, BindDTO> bindInfoMap = VehicleUtil.batchGetBindInfosByRedis(moIds);
        //所有的监控对象视频报警参数集合
        final Map<String, List<AlarmParameterSettingForm>> videoAlarmSettings =
            recordingSettingDao.getVideoAlarmsByVehicleIds(moIds).stream()
                .collect(Collectors.groupingBy(AlarmParameterSettingForm::getVehicleId));
        final JSONObject defaultType0 = WebSubscribeManager.getInstance().getDefaultType(0);
        final JSONObject defaultType1 = WebSubscribeManager.getInstance().getDefaultType(1);
        StringBuilder logMsg = new StringBuilder();
        Map<RedisKey, String> needAddKeyValueMap = new HashMap<>(16);
        List<RedisKey> needDelRedisKeyList = new ArrayList<>();
        for (String vehicleId : moIds) {
            BindDTO bindDTO = bindInfoMap.get(vehicleId);
            if (bindDTO == null) {
                continue;
            }
            // 808视频报警
            List<AlarmParameterSettingForm> videoAlarmParams = videoAlarmSettings.get(vehicleId);
            JSONObject defaultType = defaultType1;
            List<AlarmParameterSettingForm> alarmSettingList = new ArrayList<>(list);
            if (CollectionUtils.isNotEmpty(videoAlarmParams)) {
                alarmSettingList.addAll(videoAlarmParams);
                defaultType = defaultType0;
            }
            String deviceId = bindDTO.getDeviceId();
            //推送报警设置缓存和保存报警设置值
            WebSubscribeManager.getInstance()
                .pushAlarmSetAndSaveAlarmSetValue(vehicleId, deviceId, alarmSettingList, defaultType,
                    needSendAddMsgList, needAddKeyValueMap, true);
            // 保存io报警参数设置
            this.saveIoAlarmSettingToRedisByBatch(vehicleId, ioAlarmSettings.get(Boolean.FALSE), needAddKeyValueMap,
                needDelRedisKeyList);
            logMsg.append("监控对象 : ").append(bindDTO.getName()).append(" ( @").append(bindDTO.getOrgName())
                .append(" ) 报警参数设置 ");
        }
        RedisHelper.delete(needDelRedisKeyList);
        RedisHelper.setStringMap(needAddKeyValueMap);
        needSendAddMsgList.forEach(
            jsonObject -> WebSubscribeManager.getInstance().sendMsgToAll(jsonObject, ConstantUtil.WEB_ALARM_ADD));
        if (CollectionUtils.isNotEmpty(settingIdsForRemove)) {
            alarmSettingDao.deleteAlarmSettingByBatch(settingIdsForRemove);
        }
        if (CollectionUtils.isNotEmpty(needUpdateStatusConditions)) {
            parameterDao.updateStatusByBatch(6, needUpdateStatusConditions);
        }
        if (CollectionUtils.isNotEmpty(parameterNames)) {
            // 解决锁问题，按主键更新/删除
            final List<String> idList = parameterDao.listIdByParameterName(parameterNames);
            if (CollectionUtils.isNotEmpty(idList)) {
                parameterDao.deleteByIds(idList);
            }
        }
        String ipAddress = getIpAddress();
        // 记录日志
        if (logMsg.length() > 0) {
            if (moIds.size() > 1) {
                logSearchServiceImpl.addLog(ipAddress, logMsg.toString(), "3", "batch", "批量设置监控对象报警参数");
            } else {
                BindDTO bindDTO = new ArrayList<>(bindInfoMap.values()).get(0);
                String brand = bindDTO.getName();
                Integer plateColorInt = bindDTO.getPlateColor();
                logSearchServiceImpl.addLog(ipAddress, logMsg.toString(), "3", "", brand,
                    plateColorInt == null ? "" : plateColorInt.toString());
            }
            // 通知storm更新数据库数据
            ZMQFencePub.pubChangeFence("9");
            // 通知storm更新缓存数据
            ZMQFencePub.pubChangeFence("12");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        // 等待耗时任务结束
        if (null != insertTask) {
            try {
                insertTask.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    private List<AlarmParameterSettingForm> analysisAlarmSetting(List<String> moIds, String settingJsonStr) {
        List<AlarmParameterSettingForm> alarmParameterSettingFormList = new ArrayList<>();
        AlarmParameterDetailsDTO alarmParameterSetting =
            JSON.parseObject(settingJsonStr, AlarmParameterDetailsDTO.class);
        final List<String> types = Arrays
            .asList("sensorAlarm", "alert", "faultAlarm", "f3longAlarm", "vehicleAlarm", "driverAlarm", "platAlarm",
                "asolongAlarm", "f3longPlatAlarm", "asolongPlatAlarm", "peoplePlatAlarm", "peopleAlarm",
                "highPrecisionAlarm");
        List<AlarmParameter> alarmParameterList = alarmSettingDao.getAlarmTypeParameterByTypes(types);
        Map<String, List<AlarmParameter>> alarmParameterMap =
            alarmParameterList.stream().collect(Collectors.groupingBy(AlarmParameter::getAlarmType));
        alarmParameterSettingFormList
            .addAll(analysisAlarmSetting(alarmParameterMap, alarmParameterSetting.getAlertList()));
        alarmParameterSettingFormList
            .addAll(analysisAlarmSetting(alarmParameterMap, alarmParameterSetting.getDriverAlarmList()));
        alarmParameterSettingFormList
            .addAll(analysisAlarmSetting(alarmParameterMap, alarmParameterSetting.getVehicleAlarmList()));
        alarmParameterSettingFormList
            .addAll(analysisAlarmSetting(alarmParameterMap, alarmParameterSetting.getFaultAlarmList()));
        alarmParameterSettingFormList
            .addAll(analysisAlarmSetting(alarmParameterMap, alarmParameterSetting.getHighPrecisionAlarmList()));
        alarmParameterSettingFormList
            .addAll(analysisAlarmSetting(alarmParameterMap, alarmParameterSetting.getSensorAlarmList()));
        alarmParameterSettingFormList
            .addAll(analysisAlarmSetting(alarmParameterMap, alarmParameterSetting.getPlatAlarmList()));
        alarmParameterSettingFormList
            .addAll(analysisAlarmSetting(alarmParameterMap, alarmParameterSetting.getPeopleAlarmList()));
        alarmParameterSettingFormList
            .addAll(analysisAlarmSetting(alarmParameterMap, alarmParameterSetting.getPeoplePlatAlarmList()));
        alarmParameterSettingFormList
            .addAll(analysisAlarmSetting(alarmParameterMap, alarmParameterSetting.getAsolongAlarmList()));
        alarmParameterSettingFormList
            .addAll(analysisAlarmSetting(alarmParameterMap, alarmParameterSetting.getAsolongPlatAlarmList()));
        alarmParameterSettingFormList
            .addAll(analysisAlarmSetting(alarmParameterMap, alarmParameterSetting.getF3longAlarmList()));
        alarmParameterSettingFormList
            .addAll(analysisAlarmSetting(alarmParameterMap, alarmParameterSetting.getF3longPlatAlarmList()));
        // 批量设置不能设置io报警参数
        if (moIds.size() > 1) {
            return alarmParameterSettingFormList;
        }
        List<AlarmParameter> ioAlarmParameterList = alarmSettingDao.getIoAlarmTypeParameterByMoId(moIds.get(0));
        Map<String, List<AlarmParameter>> oiAlarmParameterMap =
            ioAlarmParameterList.stream().collect(Collectors.groupingBy(AlarmParameter::getAlarmType));
        List<IoAlarmSettingDTO> deviceIoAlarmList = alarmParameterSetting.getDeviceIoAlarmList();
        alarmParameterSettingFormList.addAll(analysisIoAlarmSetting(oiAlarmParameterMap, deviceIoAlarmList));
        List<IoAlarmSettingDTO> ioCollectionOneAlarmList = alarmParameterSetting.getIoCollectionOneAlarmList();
        alarmParameterSettingFormList.addAll(analysisIoAlarmSetting(oiAlarmParameterMap, ioCollectionOneAlarmList));
        List<IoAlarmSettingDTO> ioCollectionTwoAlarmList = alarmParameterSetting.getIoCollectionTwoAlarmList();
        alarmParameterSettingFormList.addAll(analysisIoAlarmSetting(oiAlarmParameterMap, ioCollectionTwoAlarmList));
        return alarmParameterSettingFormList;
    }

    private List<AlarmParameterSettingForm> analysisIoAlarmSetting(Map<String, List<AlarmParameter>> alarmParameterMap,
        List<IoAlarmSettingDTO> ioAlarmSettingList) {
        if (CollectionUtils.isEmpty(ioAlarmSettingList)) {
            return new ArrayList<>();
        }
        return analysisAlarmSetting(alarmParameterMap,
            ioAlarmSettingList.stream().map(AlarmParameterSettingDTO::new).collect(Collectors.toList()));
    }

    private List<AlarmParameterSettingForm> analysisAlarmSetting(Map<String, List<AlarmParameter>> alarmParameterMap,
        List<AlarmParameterSettingDTO> alarmParameterSettings) {
        List<AlarmParameterSettingForm> alarmParameterSettingFormList = new ArrayList<>();
        if (CollectionUtils.isEmpty(alarmParameterSettings)) {
            return alarmParameterSettingFormList;
        }
        for (AlarmParameterSettingDTO alarmParameterSettingDTO : alarmParameterSettings) {
            String alarmSettingType = alarmParameterSettingDTO.getAlarmSettingType();
            Integer alarmPush = alarmParameterSettingDTO.getAlarmPush();
            JSONObject parameterValueJsonObj = alarmParameterSettingDTO.getParameterValue();
            Set<String> alarmTypeSet =
                AlarmSetTypeAndAlarmTypeMappingRelationEnum.getAlarmTypeSetByAlarmSettingType(alarmSettingType);
            for (String alarmType : alarmTypeSet) {
                List<AlarmParameter> alarmParameters = alarmParameterMap.get(alarmType);
                if (CollectionUtils.isEmpty(alarmParameters)) {
                    continue;
                }
                for (AlarmParameter alarmParameter : alarmParameters) {
                    String alarmParameterId = alarmParameter.getId();
                    if (StringUtils.isBlank(alarmParameterId)) {
                        continue;
                    }
                    String paramCode = alarmParameter.getParamCode();
                    AlarmParameterSettingForm alarmParameterSettingForm = new AlarmParameterSettingForm();
                    alarmParameterSettingForm.setAlarmParameterId(alarmParameterId);
                    alarmParameterSettingForm.setAlarmPush(alarmPush);
                    alarmParameterSettingForm.setParameterValue(
                        parameterValueJsonObj == null || StringUtils.isBlank(paramCode) ? null :
                            parameterValueJsonObj.getString(paramCode));
                    alarmParameterSettingForm.setPos(alarmType);
                    alarmParameterSettingForm.setParamCode(paramCode);
                    alarmParameterSettingFormList.add(alarmParameterSettingForm);
                }
            }
        }
        return alarmParameterSettingFormList;
    }

    /**
     * 组装报警参数设置
     */
    private void assembleIoAlarmSetting(String moId, AlarmParameterDetailsDTO alarmParameterDetailsDTO,
        List<AlarmParameterSettingForm> ioAlarmSettings) {
        List<IoVehicleConfigInfo> ioVehicleConfigInfoList = ioVehicleConfigDao.findIoConfigBy(moId);
        if (CollectionUtils.isEmpty(ioVehicleConfigInfoList)) {
            return;
        }
        Map<Integer, IoVehicleConfigInfo> ioVehicleConfigInfoMap = ioVehicleConfigInfoList
            .stream()
            .peek(IoVehicleConfigInfo::assemblePos)
            .filter(obj -> obj.getPos() != null)
            .collect(Collectors.toMap(IoVehicleConfigInfo::getPos, Function.identity(), (v1, v2) -> v1));
        // 终端io
        List<IoAlarmSettingDTO> deviceIoAlarmList = new ArrayList<>();
        //io采集1
        List<IoAlarmSettingDTO> ioCollectionOneAlarmList = new ArrayList<>();
        // oi采集2
        List<IoAlarmSettingDTO> ioCollectionTwoAlarmList = new ArrayList<>();
        for (AlarmParameterSettingForm alarmParameterSettingForm : ioAlarmSettings) {
            String pos = alarmParameterSettingForm.getPos();
            IoAlarmSettingDTO ioAlarmSettingDTO = new IoAlarmSettingDTO();
            if (pos.startsWith("140")) {
                deviceIoAlarmList.add(ioAlarmSettingDTO);
            } else if (pos.startsWith("141")) {
                ioCollectionOneAlarmList.add(ioAlarmSettingDTO);
            } else if (pos.startsWith("142")) {
                ioCollectionTwoAlarmList.add(ioAlarmSettingDTO);
            } else {
                continue;
            }
            ioAlarmSettingDTO.setAlarmSettingType(pos);
            ioAlarmSettingDTO.setAlarmSettingName(alarmParameterSettingForm.getName());
            IoVehicleConfigInfo ioVehicleConfigInfo = ioVehicleConfigInfoMap.get(Integer.valueOf(pos));
            ioAlarmSettingDTO.setStateOne(ioVehicleConfigInfo.getStateOne());
            ioAlarmSettingDTO.setStateTwo(ioVehicleConfigInfo.getStateTwo());
            ioAlarmSettingDTO.setHighSignalType(ioVehicleConfigInfo.getHighSignalType());
            ioAlarmSettingDTO.setLowSignalType(ioVehicleConfigInfo.getLowSignalType());
            Integer alarmPush = alarmParameterSettingForm.getAlarmPush();
            ioAlarmSettingDTO.setAlarmPush(alarmPush == null ? 1 : alarmPush);
            String parameterValue = alarmParameterSettingForm.getParameterValue();
            if (StringUtils.isBlank(parameterValue)) {
                continue;
            }
            JSONObject parameterValueJsonObj = new JSONObject();
            parameterValueJsonObj.put(alarmParameterSettingForm.getParamCode(), parameterValue);
            ioAlarmSettingDTO.setParameterValue(parameterValueJsonObj);
        }
        alarmParameterDetailsDTO.setDeviceIoAlarmList(
            deviceIoAlarmList.stream().sorted(Comparator.comparing(IoAlarmSettingDTO::getAlarmSettingType))
                .collect(Collectors.toList()));
        alarmParameterDetailsDTO.setIoCollectionOneAlarmList(
            ioCollectionOneAlarmList.stream().sorted(Comparator.comparing(IoAlarmSettingDTO::getAlarmSettingType))
                .collect(Collectors.toList()));
        alarmParameterDetailsDTO.setIoCollectionTwoAlarmList(
            ioCollectionTwoAlarmList.stream().sorted(Comparator.comparing(IoAlarmSettingDTO::getAlarmSettingType))
                .collect(Collectors.toList()));
    }

    /**
     * 设置参数
     * @param alarm     alarm
     * @param selectMap 已设置参数
     */
    private void settingParam(AlarmSetting alarm, Map<String, AlarmSetting> selectMap) {
        if (selectMap.containsKey(alarm.getId())) {
            AlarmSetting selectAlarm = selectMap.get(alarm.getId());
            alarm.setSelected(true);
            alarm.setAlarmPush(selectAlarm.getAlarmPush());
            alarm.setParameterValue(selectAlarm.getParameterValue() != null ? selectAlarm.getParameterValue() : "");
        } else {
            alarm.setSelected(false);
            alarm.setAlarmPush(1);
            alarm.setParameterValue("");
        }
    }

    @Override
    public ModelAndView find808Object(String id, ModelAndView mav, String deviceType) {
        // 查询所有参数设置
        List<AlarmSetting> allList =
            alarmSettingDao.findAllAlarmParameterByProtocolType(AlarmTypeUtil.getProtocolType(deviceType));
        Map<String, AlarmSetting> selectMap = new HashMap<>();
        if (StringUtils.isNotBlank(id)) {
            List<AlarmSetting> ioAlarmParamList = alarmSettingDao.findAlarmParameterByIoMonitorId(id);
            if (CollectionUtils.isNotEmpty(ioAlarmParamList)) {
                allList.addAll(ioAlarmParamList);
            }
            // 查询该车以前设置的参数
            selectMap = alarmSettingDao.findMapById(id);
        }

        Map<String, List<AlarmSetting>> alarmSetMap = getAlarmSettingMap(allList);

        List<AlarmSetting> deviceIos = new ArrayList<>();
        List<AlarmSetting> collectionOneIos = new ArrayList<>();
        List<AlarmSetting> collectionTwoIos = new ArrayList<>();
        // 重组报警参数值
        rebuildAlarmParameter(id, allList, selectMap, alarmSetMap, deviceIos, collectionOneIos, collectionTwoIos);

        for (Map.Entry<String, List<AlarmSetting>> entry : alarmSetMap.entrySet()) {
            mav.addObject(entry.getKey(), JSON.parseArray(JSON.toJSONString(entry.getValue())));
        }
        return mav;
    }

    private Map<String, List<AlarmSetting>> getAlarmSettingMap(List<AlarmSetting> allList) {
        Map<String, List<AlarmSetting>> alarmSetMap = new HashMap<>();
        alarmSetMap.put("allList", allList);
        // 预警型
        alarmSetMap.put("alertList", new ArrayList<>());
        // 驾驶员报警
        alarmSetMap.put("driverAlarmList", new ArrayList<>());
        // 车辆报警
        alarmSetMap.put("vehicleAlarmList", new ArrayList<>());
        // 故障报警
        alarmSetMap.put("faultAlarmList", new ArrayList<>());
        // F3传感器报警
        alarmSetMap.put("sensorAlarmList", new ArrayList<>());
        // 平台报警
        alarmSetMap.put("platAlarmList", new ArrayList<>());
        // 驾驶员进出区域报警
        alarmSetMap.put("driverareaAlarmList", new ArrayList<>());
        // 驾驶员进出线路报警
        alarmSetMap.put("driverlineAlarmList", new ArrayList<>());
        // 驾驶员路段行驶时间报警
        alarmSetMap.put("drivertimeAlarmList", new ArrayList<>());
        // 温度传感器报警
        alarmSetMap.put("tempAlarmList", new ArrayList<>());
        // 湿度传感器报警
        alarmSetMap.put("humAlarmList", new ArrayList<>());
        // 油量传感器报警
        alarmSetMap.put("oilAlarmList", new ArrayList<>());
        // IO报警
        alarmSetMap.put("ioAlarmList", new ArrayList<>());
        // 区域报警
        alarmSetMap.put("areaAlarmList", new ArrayList<>());
        // 线路报警
        alarmSetMap.put("lineAlarmList", new ArrayList<>());
        // 关键点报警
        alarmSetMap.put("pointAlarmList", new ArrayList<>());
        // 工时报警
        alarmSetMap.put("workHourAlarmList", new ArrayList<>());
        // 反转报警
        alarmSetMap.put("veerAlarmList", new ArrayList<>());
        // 胎压报警
        alarmSetMap.put("tirePressureAlarmList", new ArrayList<>());
        //终端IO报警
        alarmSetMap.put("deviceIos", new ArrayList<>());
        //IO采集1
        alarmSetMap.put("collectionOneIos", new ArrayList<>());
        //IO采集2
        alarmSetMap.put("collectionTwoIos", new ArrayList<>());
        // 载重报警(异常、超载)
        alarmSetMap.put("loadAlarmList", new ArrayList<>());
        //F3高精度报警
        alarmSetMap.put("highPrecisionAlarmList", new ArrayList<>());
        //F3高精度报警 -> 设备电量报警
        alarmSetMap.put("devicePowerAlarmList", new ArrayList<>());
        return alarmSetMap;
    }

    /**
     * 重新组装报警参数
     * @param id               车辆ID
     * @param allList          返回list
     * @param selectMap        监控对象已设置的报警参数集合
     * @param alarmSetMap      报警类型集合
     * @param deviceIos        终端ID集合
     * @param collectionOneIos 采集板一
     * @param collectionTwoIos 采集板er
     */
    private void rebuildAlarmParameter(String id, List<AlarmSetting> allList, Map<String, AlarmSetting> selectMap,
        Map<String, List<AlarmSetting>> alarmSetMap, List<AlarmSetting> deviceIos, List<AlarmSetting> collectionOneIos,
        List<AlarmSetting> collectionTwoIos) {
        if (CollectionUtils.isEmpty(allList)) {
            return;
        }
        for (AlarmSetting alarm : allList) {
            String pos = alarm.getPos();
            // 设置参数
            settingParam(alarm, selectMap);
            // 报警分类
            switch (alarm.getType()) {
                case "alert":
                    // 预警
                    if (!alarm.isSelected()) {
                        alarm.setAlarmPush(0);
                    }
                    alarmSetMap.get("alertList").add(alarm);
                    break;
                case "driverAlarm":
                    // 驾驶员报警
                    getDriverAlarm(alarmSetMap, alarm);
                    break;
                case "vehicleAlarm":
                    // 车辆报警
                    if (!"27".equals(pos) && !alarm.isSelected()) {
                        alarm.setAlarmPush(0);
                    }
                    alarmSetMap.get("vehicleAlarmList").add(alarm);
                    break;
                case "faultAlarm":
                    // 故障报警
                    getFaultAlarm(alarmSetMap, alarm);
                    break;
                case "platAlarm":
                    // 平台报警
                    getPlatAlarm(alarmSetMap, alarm);
                    break;
                case "sensorAlarm":
                    // F3传感器报警
                    getSensorAlarm(alarmSetMap, alarm);
                    break;
                case "ioAlarm":
                    getIoAlarm(id, deviceIos, collectionOneIos, collectionTwoIos, alarm);
                    break;
                case "highPrecisionAlarm":
                    int alarmTypeInt = Integer.parseInt(pos);
                    if (alarmTypeInt >= 18810 && alarmTypeInt <= 18815) {
                        alarmSetMap.get("devicePowerAlarmList").add(alarm);
                    } else {
                        alarmSetMap.get("highPrecisionAlarmList").add(alarm);
                    }
                    break;
                default:
                    break;
            }
        }
        sortedIoAlarms(deviceIos, collectionOneIos, collectionTwoIos, alarmSetMap);
    }

    private void getFaultAlarm(Map<String, List<AlarmSetting>> alarmSetMap, AlarmSetting alarm) {
        if (!alarm.isSelected()) {
            alarm.setAlarmPush(0);
        }
        if (("16".equals(alarm.getPos()) || "17".equals(alarm.getPos())) && !alarm.isSelected()) {
            alarm.setAlarmPush(1);
        }
        alarmSetMap.get("faultAlarmList").add(alarm);
    }

    private void getIoAlarm(String id, List<AlarmSetting> deviceIos, List<AlarmSetting> collectionOneIos,
        List<AlarmSetting> collectionTwoIos, AlarmSetting alarm) {
        List<IoVehicleConfigInfo> ioVehicleConfigInfoList = ioVehicleConfigDao.findIoConfigBy(id);
        Map<Integer, List<IoVehicleConfigInfo>> groupIoVehicleConfig =
            ioVehicleConfigInfoList.stream().collect(Collectors.groupingBy(IoVehicleConfigInfo::getIoType));
        if (alarm.getName().contains("I/O") && alarm.getPos().contains("140") && !"14004".equals(alarm.getPos())) {
            List<IoVehicleConfigInfo> ioConfigs = groupIoVehicleConfig.get(1);
            setState(ioConfigs, alarm, deviceIos);
        } else if (alarm.getName().contains("I/O") && alarm.getPos().contains("141") && !"141000"
            .equals(alarm.getPos())) {
            List<IoVehicleConfigInfo> ioConfigs = groupIoVehicleConfig.get(2);
            setState(ioConfigs, alarm, collectionOneIos);
        } else if (alarm.getName().contains("I/O") && alarm.getPos().contains("142") && !"142000"
            .equals(alarm.getPos())) {
            List<IoVehicleConfigInfo> ioConfigs = groupIoVehicleConfig.get(3);
            setState(ioConfigs, alarm, collectionTwoIos);
        }
    }

    private void getSensorAlarm(Map<String, List<AlarmSetting>> alarmSetMap, AlarmSetting alarm) {
        if (alarm.getName().contains("温度传感器")) {
            alarmSetMap.get("tempAlarmList").add(alarm);
        } else if (alarm.getName().contains("湿度传感器")) {
            alarmSetMap.get("humAlarmList").add(alarm);
        } else if (alarm.getName().contains("油箱")) {
            alarmSetMap.get("oilAlarmList").add(alarm);
        } else if (alarm.getName().contains("门磁")) {
            alarmSetMap.get("ioAlarmList").add(alarm);
        } else if (alarm.getName().contains("工时传感器异常报警")) {
            alarmSetMap.get("workHourAlarmList").add(alarm);
        } else if (alarm.getName().contains("反转")) {
            alarmSetMap.get("veerAlarmList").add(alarm);
        } else if (alarm.getName().contains("载重传感器") || alarm.getName().contains("载重超载报警")) {
            alarmSetMap.get("loadAlarmList").add(alarm);
        } else if (alarm.getName().startsWith("轮胎") || "胎压传感器异常报警".equals(alarm.getName())) {
            alarmSetMap.get("tirePressureAlarmList").add(alarm);
        } else {
            alarmSetMap.get("sensorAlarmList").add(alarm);
        }
    }

    private void getPlatAlarm(Map<String, List<AlarmSetting>> alarmSetMap, AlarmSetting alarm) {
        if (alarm.getName().contains("区域")) {
            alarmSetMap.get("areaAlarmList").add(alarm);
        } else if (alarm.getName().contains("线路")) {
            alarmSetMap.get("lineAlarmList").add(alarm);
        } else if (alarm.getName().contains("关键点")) {
            if (!alarm.isSelected()) {
                alarm.setAlarmPush(1);
            }
            alarmSetMap.get("pointAlarmList").add(alarm);
        } else {
            if (("78".equals(alarm.getPos()) || "82".equals(alarm.getPos())) && !alarm.isSelected()) {
                alarm.setAlarmPush(0);
            }
            if (Objects.equals("209", alarm.getPos()) && !alarm.isSelected()) {
                alarm.setAlarmPush(-1);
            }
            alarmSetMap.get("platAlarmList").add(alarm);
        }
    }

    private void getDriverAlarm(Map<String, List<AlarmSetting>> alarmSetMap, AlarmSetting alarm) {
        if (alarm.getName().contains("区域")) {
            alarmSetMap.get("driverareaAlarmList").add(alarm);
        } else if (alarm.getName().contains("线路")) {
            alarmSetMap.get("driverlineAlarmList").add(alarm);
        } else if (alarm.getName().contains("路段行驶时间")) {
            if (!alarm.isSelected()) {
                alarm.setAlarmPush(0);
            }
            alarmSetMap.get("drivertimeAlarmList").add(alarm);
        } else {
            if (("18".equals(alarm.getPos()) || "19".equals(alarm.getPos())) && !alarm.isSelected()) {
                alarm.setAlarmPush(0);
            }
            alarmSetMap.get("driverAlarmList").add(alarm);
        }
    }

    private void setState(List<IoVehicleConfigInfo> ioConfigs, AlarmSetting alarm, List<AlarmSetting> ios) {
        String pos = alarm.getPos().substring(alarm.getPos().length() - 2);
        if (pos.startsWith("0")) {
            pos = pos.substring(1);
        }
        for (IoVehicleConfigInfo io : ioConfigs) {
            if ((io.getIoSite() + "").equals(pos)) {
                alarm.setStateOne(io.getStateOne());
                alarm.setStateTwo(io.getStateTwo());
                alarm.setHighSignalType(io.getHighSignalType());
                alarm.setLowSignalType(io.getLowSignalType());
                ios.add(alarm);
            }
        }
    }

    private void sortedIoAlarms(List<AlarmSetting> deviceIos, List<AlarmSetting> collectionOneIos,
        List<AlarmSetting> collectionTwoIos, Map<String, List<AlarmSetting>> alarmSetMap) {
        if (CollectionUtils.isNotEmpty(deviceIos)) {
            deviceIos = deviceIos.stream()
                .sorted(Comparator.comparingInt((AlarmSetting alarm) -> Integer.parseInt(alarm.getPos())))
                .collect(Collectors.toList());
            alarmSetMap.get("deviceIos").addAll(deviceIos);
        }
        if (CollectionUtils.isNotEmpty(collectionOneIos)) {
            collectionOneIos = collectionOneIos.stream()
                .sorted(Comparator.comparingInt((AlarmSetting alarm) -> Integer.parseInt(alarm.getPos())))
                .collect(Collectors.toList());
            alarmSetMap.get("collectionOneIos").addAll(collectionOneIos);
        }
        if (CollectionUtils.isNotEmpty(collectionTwoIos)) {
            collectionTwoIos = collectionTwoIos.stream()
                .sorted(Comparator.comparingInt((AlarmSetting alarm) -> Integer.parseInt(alarm.getPos())))
                .collect(Collectors.toList());
            alarmSetMap.get("collectionTwoIos").addAll(collectionTwoIos);
        }
    }

    /**
     * 重组北斗天地报警参数值
     */
    @Override
    public ModelAndView findBdObject(String id, ModelAndView mav) {
        // 查询所有参数设置
        List<AlarmSetting> allList = alarmSettingDao.findAllAlarmParameter();
        Map<String, AlarmSetting> selectMap = null;
        if (StringUtils.isNotBlank(id)) {
            selectMap = alarmSettingDao.findMapById(id); // 查询该车设置的参数
        }
        Map<String, List<AlarmSetting>> alarmSetMap = new HashMap<>();
        alarmSetMap.put("allList", allList);
        // 北斗报警
        alarmSetMap.put("peopleAlarmList", new ArrayList<>());
        // 北斗平台报警
        alarmSetMap.put("peoplePlatAlarmList", new ArrayList<>());
        // 人员区域报警
        alarmSetMap.put("peopleAreaAlarmList", new ArrayList<>());
        // 人员线路报警
        alarmSetMap.put("peopleLineAlarmList", new ArrayList<>());

        // 重组报警参数值
        if (CollectionUtils.isNotEmpty(allList) && selectMap != null) {
            for (AlarmSetting alarm : allList) {
                String type = alarm.getType();
                // 设置参数
                settingParam(alarm, selectMap);
                if ("peopleAlarm".equals(type)) {
                    alarmSetMap.get("peopleAlarmList").add(alarm);
                } else if ("peoplePlatAlarm".equals(type)) {
                    if (alarm.getName().contains("区域")) {
                        alarmSetMap.get("peopleAreaAlarmList").add(alarm);
                    } else if (alarm.getName().contains("线路")) {
                        alarmSetMap.get("peopleLineAlarmList").add(alarm);
                    } else {
                        alarmSetMap.get("peoplePlatAlarmList").add(alarm);
                    }
                }
            }
        }
        for (Map.Entry<String, List<AlarmSetting>> entry : alarmSetMap.entrySet()) {
            mav.addObject(entry.getKey(), JSON.parseArray(JSON.toJSONString(entry.getValue())));
        }
        return mav;
    }

    /**
     * 重组艾塞欧报警参数值
     */
    @Override
    public ModelAndView findAsoObject(String id, ModelAndView mav) {
        // 查询所有参数设置
        List<AlarmSetting> allList = alarmSettingDao.findAllAlarmParameter();
        Map<String, AlarmSetting> selectMap = null;
        if (StringUtils.isNotBlank(id)) {
            // 查询该车设置的参数
            selectMap = alarmSettingDao.findMapById(id);
        }
        Map<String, List<AlarmSetting>> alarmSetMap = new HashMap<>();
        alarmSetMap.put("allList", allList);
        // 艾塞欧超长待机报警
        alarmSetMap.put("asolongAlarmList", new ArrayList<>());
        // 艾塞欧超待平台报警
        alarmSetMap.put("asolongPlatAlarmList", new ArrayList<>());
        // 拆机报警
        alarmSetMap.put("unpackAlarmList", new ArrayList<>());
        // 重组报警参数值
        if (CollectionUtils.isNotEmpty(allList) & selectMap != null) {
            for (AlarmSetting alarm : allList) {
                String type = alarm.getType();
                // 设置参数
                settingParam(alarm, selectMap);
                if (Objects.equals(type, "asolongAlarm")) {
                    if (alarm.getName().contains("拆机")) {
                        alarmSetMap.get("unpackAlarmList").add(alarm);
                    } else {
                        alarmSetMap.get("asolongAlarmList").add(alarm);
                    }
                } else if (Objects.equals(type, "asolongPlatAlarm")) {
                    alarmSetMap.get("asolongPlatAlarmList").add(alarm);
                }
            }
        }
        for (Map.Entry<String, List<AlarmSetting>> entry : alarmSetMap.entrySet()) {
            mav.addObject(entry.getKey(), JSON.parseArray(JSON.toJSONString(entry.getValue())));
        }
        return mav;
    }

    /**
     * 重组F3报警参数值
     */
    @Override
    public ModelAndView findF3Object(String id, ModelAndView mav) {
        // 查询所有参数设置
        List<AlarmSetting> allList = alarmSettingDao.findAllAlarmParameter();
        Map<String, AlarmSetting> selectMap = null;
        if (StringUtils.isNotBlank(id)) {
            // 查询该车设置的参数
            selectMap = alarmSettingDao.findMapById(id);
        }
        Map<String, List<AlarmSetting>> alarmSetMap = new HashMap<>();
        alarmSetMap.put("allList", allList);
        // F3超长待机报警
        alarmSetMap.put("f3longAlarmList", new ArrayList<>());
        // F3超待平台报警
        alarmSetMap.put("f3longPlatAlarmList", new ArrayList<>());
        // 重组报警参数值
        if (CollectionUtils.isNotEmpty(allList) & selectMap != null) {
            for (AlarmSetting alarm : allList) {
                // 设置参数
                settingParam(alarm, selectMap);
                if ("f3longAlarm".equals(alarm.getType())) {
                    alarmSetMap.get("f3longAlarmList").add(alarm);
                } else if ("f3longPlatAlarm".equals(alarm.getType())) {
                    alarmSetMap.get("f3longPlatAlarmList").add(alarm);
                }
            }
        }
        for (Map.Entry<String, List<AlarmSetting>> entry : alarmSetMap.entrySet()) {
            mav.addObject(entry.getKey(), JSON.parseArray(JSON.toJSONString(entry.getValue())));
        }
        return mav;
    }

    @Override
    public VehicleInfo findPeopleById(String id) {
        return alarmSettingDao.findPeopleById(id);
    }

    @Override
    public VehicleInfo findPeopleOrVehicleOrThingById(String id) {
        return alarmSettingDao.findPeopleOrVehicleOrThingById(id);
    }

    /**
     * 获取支持联动策略的报警类型
     */
    @Override
    public List<AlarmType> getAlarmType(Collection<String> vehicleIds, int deviceType) {
        List<Integer> types;
        if (deviceType == -1) {
            types = new ArrayList<>(Arrays.asList(ProtocolEnum.PROTOCOL_TYPE_808_2013));
        } else {
            types = new ArrayList<>();
            types.add(deviceType);
        }
        types.add(0);
        List<AlarmType> alarms = alarmSearchDao.getAlarmTypeByProtocolType(types);
        final Map<String, AlarmType> alarmMap = AssembleUtil.collectionToMap(alarms, AlarmType::getPos);
        //获得车辆io绑定的io功能检测类型(嘉隆项目需要过滤io)
        List<IoVehicleConfigInfo> functionIdBingIoSites = switchTypeDao.getFuntcionIdBingIoSite(vehicleIds, null);
        for (IoVehicleConfigInfo info : functionIdBingIoSites) {
            Integer pos = null;
            if (info.getIoType() == 1) {
                pos = 14000;
            } else if (info.getIoType() == 2) {
                pos = 14100;
            } else if (info.getIoType() == 3) {
                pos = 14200;
            }
            if (pos != null) {
                pos = pos + info.getIoSite();
                info.setPos(pos);
                Optional.ofNullable(alarmMap.get(String.valueOf(pos)))
                        .ifPresent(alarm -> alarm.setName(info.getName()));
            }
        }
        List<AlarmType> alarmFilters = alarms.stream()
            .filter(info -> filterAlarmType(info.getName()))
            .collect(Collectors.toList());
        return AlarmTypeUtil.assemblyAlarmName(alarmFilters, "alarmSetting", null);
    }

    /**
     * 过滤出"终端I/O异常", "I/O采集1异常", "I/O采集2异常" 等几个报警
     * @param name 报警名称
     * @return boolean
     */
    private boolean filterAlarmType(String name) {
        if (StringUtils.isBlank(name)) {
            return false;
        }

        final String ioAlarmPrefix = "I/O";
        if (name.contains(ioAlarmPrefix)) {
            return IO_NAME.contains(name);
        }

        return true;
    }

    /**
     * 获取联动策略设置
     */
    @Override
    public List<AlarmLinkageDTO> getLinkageSettingList(String vehicleId) {
        List<AlarmLinkageParam> settingList = alarmSettingDao.findLinkageSettingList(vehicleId);
        Set<String> photoSettingIds = new HashSet<>(settingList.size());
        Set<String> recordingIds = new HashSet<>(settingList.size());
        Set<String> msgSettingIds = new HashSet<>(settingList.size());
        Set<String> outputControlIds = new HashSet<>(settingList.size());

        for (AlarmLinkageParam setting : settingList) {
            photoSettingIds.add(setting.getPhotoId());
            recordingIds.add(setting.getRecordingId());
            msgSettingIds.add(setting.getMsgId());
            outputControlIds.add(setting.getOutputControlId());
        }

        final Map<String, PhotoDTO> photoMap = AssembleUtil.convertToMap(photoSettingIds,
                alarmSettingDao::listPhotoSetting, PhotoDTO::getId);
        final Map<String, PhotoDTO> recordingMap = AssembleUtil.convertToMap(recordingIds,
                alarmSettingDao::listPhotoSetting, PhotoDTO::getId);
        final Map<String, MsgParamDTO> msgMap = AssembleUtil.convertToMap(msgSettingIds,
                alarmSettingDao::listMsgSetting, MsgParamDTO::getId);
        final Map<String, OutputControlDTO> outputControlMap = AssembleUtil.convertToMap(outputControlIds,
                alarmSettingDao::listOutputControlSetting, OutputControlDTO::getId);
        List<AlarmLinkageDTO> result = new ArrayList<>();
        for (AlarmLinkageParam setting : settingList) {
            AlarmLinkageDTO linkageDTO = new AlarmLinkageDTO();
            linkageDTO.setAlarmHandleLinkageCheck(setting.getAlarmHandleLinkageCheck());
            linkageDTO.setAlarmHandleType(setting.getAlarmHandleType());
            linkageDTO.setAlarmHandleResult(setting.getAlarmHandleResult());
            linkageDTO.setHandleUsername(setting.getHandleUsername());
            linkageDTO.setPhoto(photoMap.get(setting.getPhotoId()));
            linkageDTO.setRecording(recordingMap.get(setting.getRecordingId()));
            linkageDTO.setMsg(msgMap.get(setting.getMsgId()));
            linkageDTO.setOutputControl(outputControlMap.get(setting.getOutputControlId()));
            linkageDTO.setPos(setting.getAlarmTypeId());
            linkageDTO.setVideoFlag(setting.getVideoFlag());
            linkageDTO.setUploadAudioResourcesFlag(setting.getUploadAudioResourcesFlag());
            result.add(linkageDTO);
        }
        return result;
    }

    /**
     * 获取联动策略参考车辆
     */
    @Override
    public List<VehicleInfo> findReferPhotoVehicles(List<String> vehicleIds, Integer deviceType) {
        List<VehicleInfo> result = new ArrayList<>();
        if (deviceType == null) {
            return result;
        }
        List<Integer> deviceTypes = ProtocolEnum.getProtocolTypes(deviceType);
        String uuid = userService.getCurrentUserUuid();
        List<VehicleInfo> list = alarmSettingDao.findAlarmLinkageReferenceVehicles(uuid, vehicleIds, deviceTypes);
        if (CollectionUtils.isEmpty(list)) {
            return result;
        }
        List<String> queryVehicleIds = list.stream().map(VehicleInfo::getId).collect(Collectors.toList());
        return VehicleUtil.batchGetBindInfosByRedis(queryVehicleIds, Lists.newArrayList("name")).values()
            .stream()
            .map(obj -> {
                VehicleInfo info = new VehicleInfo();
                info.setId(obj.getId());
                info.setBrand(obj.getName());
                return info;
            }).collect(Collectors.toList());
    }

    /**
     * 添加联动策略设置
     */
    @Override
    public JsonResultBean saveLinkageSetting(String linkageParam, String monitorIds) {
        if (StringUtils.isBlank(monitorIds) || StringUtils.isBlank(linkageParam)) {
            return new JsonResultBean(JsonResultBean.FAULT, "智能联动设置数据不能为空!");
        }

        List<AlarmLinkageDTO> linkageParamList = JSON.parseArray(linkageParam, AlarmLinkageDTO.class);
        // 删除该车以前的设置
        final Set<String> monitorIdSet = Arrays.stream(monitorIds.split(",")).collect(Collectors.toSet());
        final Map<String, Set<String>> deleteMonitorAlarmTypeCache = deleteBatchLinkageSetting(monitorIdSet);
        final Map<String, Set<String>> addMonitorAlarmTypeCache = new HashMap<>(16);

        // 批量添加新设置
        final JsonResultBean errorMsg = this.addLinkageParam(linkageParamList, monitorIdSet, addMonitorAlarmTypeCache);
        if (errorMsg != null) {
            return errorMsg;
        }
        // 同步数据给报警联动项目
        LinkageParamDTO linkageParamReq = new LinkageParamDTO(deleteMonitorAlarmTypeCache, addMonitorAlarmTypeCache);
        Map<String, String> requestParam = new HashMap<>(16);
        requestParam.put("linkageParamReq", JSON.toJSONString(linkageParamReq));
        HttpClientUtil.send(AlarmLinkageUrlEnum.ADD_ALARM_LINKAGE, requestParam);
        // 记录日志
        this.addLinkageLog(getIpAddress(), monitorIdSet, deleteMonitorAlarmTypeCache, addMonitorAlarmTypeCache);
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    private JsonResultBean addLinkageParam(List<AlarmLinkageDTO> linkageParamList,
                                            Set<String> monitorIdSet,
                                            Map<String, Set<String>> addMonitorAlarmTypeCache) {
        final int len = monitorIdSet.size();
        // key: alarm:监控对象ID -> key:协议类型 value:存储的值
        Map<RedisKey, Map<String, String>> linkageCache = new HashMap<>(len);
        List<Msgparam> msgList = new ArrayList<>(len);
        List<PhotoParam> photoAndRecodingList = new ArrayList<>(len);
        List<OutputControl> outputControlList = new ArrayList<>(len);
        List<SpecialAlarmDO> specialAlarmList = new ArrayList<>(len);
        // 用户所属企业
        OrganizationLdap organizationLdap = userService.getCurrentUserOrg();
        String orgName = Objects.isNull(organizationLdap) ? StringUtils.EMPTY : organizationLdap.getName();
        for (String monitorId : monitorIdSet) {
            for (AlarmLinkageDTO req : linkageParamList) {
                final String errorMsg = this.validAlarmHandle(req);
                if (StringUtils.isNotEmpty(errorMsg)) {
                    return new JsonResultBean(JsonResultBean.FAULT, errorMsg);
                }
                SpecialAlarmDO specialAlarm = this.buildSpecialAlarmDO(req, monitorId);
                this.buildMsg(msgList, req, specialAlarm);
                this.buildPhotoList(photoAndRecodingList, req, specialAlarm);
                this.buildVideoList(photoAndRecodingList, req, specialAlarm);
                this.buildOutputControlList(outputControlList, req, specialAlarm, monitorId);
                specialAlarmList.add(specialAlarm);
                // 设置缓存
                req.setHandleUserOrgName(orgName);

                RedisKey cacheKey = RedisKeyEnum.ALARM_LINKAGE.of(monitorId);
                linkageCache.computeIfAbsent(cacheKey, k -> new HashMap<>(16))
                            .put(req.getPos(), JSON.toJSONString(req));
                addMonitorAlarmTypeCache.computeIfAbsent(monitorId, k -> new HashSet<>()).add(req.getPos());
            }
        }

        alarmSettingDao.addSpecialAlarmDOByBatch(specialAlarmList);
        if (CollectionUtils.isNotEmpty(msgList)) {
            alarmSettingDao.addMsgSettingByBatch(msgList);
        }

        if (CollectionUtils.isNotEmpty(photoAndRecodingList)) {
            alarmSettingDao.addPhotoSettingByBatch(photoAndRecodingList);
        }

        if (CollectionUtils.isNotEmpty(outputControlList)) {
            alarmSettingDao.addOutputControlSettingByBatch(outputControlList);
        }
        // 删除redis缓存
        RedisHelper.delete(new ArrayList<>(linkageCache.keySet()));
        // 添加缓存
        RedisHelper.batchAddToHash(linkageCache);
        return null;
    }

    private void addLinkageLog(String ipAddress,
                               Set<String> monitorIdSet,
                               Map<String, Set<String>> deleteMonitorAlarmTypeCache,
                               Map<String, Set<String>> addMonitorAlarmTypeCache) {
        if (monitorIdSet.isEmpty()) {
            return;
        }
        final Map<String, BindDTO> bindInfoMap = VehicleUtil.batchGetBindInfosByRedis(monitorIdSet);
        if (bindInfoMap.isEmpty()) {
            return;
        }
        final BindDTO anyone = bindInfoMap.values().iterator().next();
        final String deviceType = anyone.getDeviceType();
        final int paramType;
        if (Arrays.asList(ProtocolEnum.PROTOCOL_TYPE_808_2011_2013_STR).contains(deviceType)) {
            paramType = -1;
        } else if (Arrays.asList(ProtocolEnum.PROTOCOL_TYPE_808_2019_STR).contains(deviceType)) {
            paramType = Integer.parseInt(ProtocolEnum.T808_2019.getDeviceType());
        } else {
            paramType = Integer.parseInt(deviceType);
        }
        final List<AlarmType> alarmType = this.getAlarmType(monitorIdSet, paramType);
        final Map<String, String> alarmTypeMap =
                alarmType.stream().collect(Collectors.toMap(AlarmType::getPos, AlarmType::getName, (o, p) -> o));
        StringBuilder message = new StringBuilder();
        for (String monitorId : monitorIdSet) {
            BindDTO bindDTO = bindInfoMap.get(monitorId);
            final Set<String> deleted = deleteMonitorAlarmTypeCache.getOrDefault(monitorId, Collections.emptySet());
            final Set<String> added = addMonitorAlarmTypeCache.getOrDefault(monitorId, Collections.emptySet());
            message.append(bindDTO.getName()).append("：<br/>报警由").append(deleted.size()).append("个");
            if (!deleted.isEmpty()) {
                message.append("(");
                deleted.forEach(id -> message.append(alarmTypeMap.get(id)).append("、"));
                message.deleteCharAt(message.length() - 1);
                message.append(")");
            }
            message.append("修改为").append(added.size()).append("个");
            if (!added.isEmpty()) {
                message.append("(");
                added.forEach(id -> message.append(alarmTypeMap.get(id)).append("、"));
                message.deleteCharAt(message.length() - 1);
                message.append(")");
            }
            message.append("<br/>");
        }
        if (monitorIdSet.size() > 1) {
            logSearchServiceImpl.addLog(ipAddress, message.toString(), "3", "batch", "批量设置报警联动策略");
        } else {
            final String operation = "监控对象：" + anyone.getName() + " 设置报警联动策略";
            logSearchServiceImpl.addLog(ipAddress, message.toString(), "3", "more", operation);
        }
    }

    /**
     * 组装短信数据
     * @param msgList msgList
     * @param req req
     * @param specialAlarm specialAlarm
     */
    private void buildMsg(List<Msgparam> msgList, AlarmLinkageDTO req, SpecialAlarmDO specialAlarm) {
        final MsgParamDTO msgReq = req.getMsg();
        if (msgReq == null) {
            return;
        }
        final Integer textType = msgReq.getTextType();
        final Integer messageTypeOne = msgReq.getMessageTypeOne();
        final Integer messageTypeTwo = msgReq.getMessageTypeTwo();
        msgReq.setTextType(Objects.isNull(textType) ? TEXT_TYPE_NOTIFICATION : textType);
        msgReq.setMessageTypeOne(Objects.isNull(messageTypeOne) ? MESSAGE_TYPE_ONE_NOTIFICATION : messageTypeOne);
        msgReq.setMessageTypeTwo(Objects.isNull(messageTypeTwo) ? MESSAGE_TYPE_TWO_NOTIFICATION : messageTypeTwo);
        final Msgparam msgparam = Msgparam.of(msgReq);
        msgparam.setCreateDataUsername(SystemHelper.getCurrentUsername());

        msgList.add(msgparam);
        specialAlarm.setMsgId(msgparam.getId());
    }

    public static OutputControlSendInfo getOutputControlSendInfo(Collection<OutputControl> outputControls,
                                                                 boolean isClose) {
        OutputControlSendInfo outputControlSendInfo = new OutputControlSendInfo();
        List<OutputControlSend> outputControlSends = new ArrayList<>();
        for (OutputControl outputControl: outputControls) {
            OutputControlSend outputControlSend = new OutputControlSend();
            outputControlSend.setType(0xF3);
            outputControlSend.setSensorId(outputControl.getPeripheralId());
            outputControlSend.setSign(1);
            outputControlSend.setControlType(outputControl.getControlSubtype());
            outputControlSend.setControlIo(outputControl.getOutletSet() + 1);
            outputControlSend.setControlTime(outputControl.getControlTime() == null
                    ? 0xFFFF
                    : outputControl.getControlTime());
            if (isClose) {
                outputControlSend.setControlStauts(0);
                outputControlSend.setControlTime(0xFFFF);
            } else {
                if (outputControlSend.getControlType() != 3) {
                    Integer controlStatus = outputControl.getControlStatus();
                    outputControlSend.setControlStauts(controlStatus == null ? 0 : controlStatus);
                } else {
                    outputControlSend.setControlStauts(
                        (int) ((outputControl.getAnalogOutputRatio() == null ? 0 : outputControl.getAnalogOutputRatio())
                            * 10));
                }
            }
            outputControlSends.add(outputControlSend);
        }
        outputControlSendInfo.setInfoList(outputControlSends);
        outputControlSendInfo.setNum(outputControlSends.size());
        return outputControlSendInfo;
    }

    @Override
    public JsonResultBean updateAlarmType() {
        // 获取到全部的报警数据字典
        List<AlarmType> alarmTypes = alarmSearchDao.getAlarmType(null);
        if (CollectionUtils.isEmpty(alarmTypes)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        Map<RedisKey, String> needAddKeyValueMap = new HashMap<>(alarmTypes.size());
        for (AlarmType alarmType : alarmTypes) {
            String pos = alarmType.getPos();
            String type = alarmType.getType();
            String name = alarmType.getName();
            if (type.contains("platAlarm") || type.contains("PlatAlarm")) {
                name = name + "(平台)";
                alarmType.setName(name);
            }
            needAddKeyValueMap.put(HistoryRedisKeyEnum.ALARM_TYPE_INFO.of(pos), JSON.toJSONString(alarmType));
        }
        RedisHelper.setStringMap(needAddKeyValueMap);
        ZMQFencePub.pubChangeFence("11");
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    @Override
    public JsonResultBean getIOAlarmStateTxt(String vehicleId, String alarmTypeId, String value) {
        AlarmType alarmType = alarmSearchDao.getAlarmTypeById(alarmTypeId);
        if (alarmType == null) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        String name = alarmType.getName();
        String pos = alarmType.getPos();
        Integer ioType = null;
        if (name.contains("I/O") && pos.contains("140")) {
            ioType = 1;
        } else if (name.contains("I/O") && pos.contains("141")) {
            ioType = 2;
        } else if (name.contains("I/O") && pos.contains("142")) {
            ioType = 3;
        }
        if (ioType == null) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        Integer ioSite = Integer.parseInt(name.substring(3));
        IoVehicleConfig switchType = ioVehicleConfigDao.findByIoTypeAndSite(ioSite, ioType, vehicleId);
        if (switchType == null) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        String txt = "";
        SwitchType type = switchTypeDao.findByid(switchType.getFunctionId());
        if ("1".equals(value)) {
            Integer high = switchType.getHighSignalType();
            if (high != null) {
                if (high == 1) {
                    txt = type.getStateOne();
                } else {
                    txt = type.getStateTwo();
                }
            } else {
                return new JsonResultBean(JsonResultBean.FAULT, "IO未设置高低电平对应状态");
            }
        } else if ("2".equals(value)) {
            Integer low = switchType.getLowSignalType();
            if (low != null) {
                if (low == 1) {
                    txt = type.getStateOne();
                } else {
                    txt = type.getStateTwo();
                }
            } else {
                return new JsonResultBean(JsonResultBean.FAULT, "IO未设置高低电平对应状态");
            }
        }
        return new JsonResultBean(txt);
    }

    @Override
    public JsonResultBean resetDefaultAlarm(String deviceType) {
        // 查询当前设置
        final List<String> types = Arrays
            .asList("sensorAlarm", "alert", "faultAlarm", "f3longAlarm", "vehicleAlarm", "driverAlarm", "platAlarm",
                "asolongAlarm", "f3longPlatAlarm", "asolongPlatAlarm", "peoplePlatAlarm", "peopleAlarm", "adasAlarm",
                "highPrecisionAlarm");
        List<AlarmParameterSettingForm> defaultAlarmParameterSettings =
            alarmSettingDao.getDefaultAlarmParameterSetting(AlarmTypeUtil.getProtocolType(deviceType), types);
        if (CollectionUtils.isEmpty(defaultAlarmParameterSettings)) {
            return new JsonResultBean(Lists.newArrayList());
        }
        // 设置报警默认推送方式
        for (AlarmParameterSettingForm alarm : defaultAlarmParameterSettings) {
            alarm.setAlarmPush(1);
            setAlarmPushDefault(alarm);
        }
        Map<String, List<AlarmParameterSettingForm>> defaultAlarmParameterSettingMap =
            defaultAlarmParameterSettings.stream().collect(Collectors.groupingBy(AlarmParameterSettingForm::getType));
        return new JsonResultBean(AlarmParameterUtil.assemblePageDisplayData(defaultAlarmParameterSettingMap));
    }

    @Override
    public JsonResultBean resetDefaultHighPrecisionAlarm(String deviceType) {
        // 查询所有参数设置
        List<AlarmSetting> alarmSettingList =
            alarmSettingDao.findHighPrecisionAlarmByProtocolType(AlarmTypeUtil.getProtocolType(deviceType));
        if (CollectionUtils.isEmpty(alarmSettingList)) {
            return new JsonResultBean(Lists.newArrayList());
        }
        for (AlarmSetting alarm : alarmSettingList) {
            alarm.setAlarmPush(1);
        }
        // 设置报警默认推送方式
        return new JsonResultBean(alarmSettingList);
    }

    private void setAlarmPushDefault(AlarmParameterSettingForm alarm) {
        String pos = alarm.getPos();
        String name = alarm.getName();
        switch (alarm.getType()) {
            case "alert":
                // 预警
                alarm.setAlarmPush(0);
                break;
            case "driverAlarm":
                // 驾驶员报警
                if (name.contains("路段行驶时间")) {
                    alarm.setAlarmPush(0);
                } else {
                    if ("18".equals(pos) || "19".equals(pos)) {
                        alarm.setAlarmPush(0);
                    }
                }
                break;
            case "vehicleAlarm":
                // 车辆报警
                if (!"27".equals(pos)) {
                    alarm.setAlarmPush(0);
                }
                break;
            case "faultAlarm":
                // 故障报警
                if ("16".equals(pos) || "17".equals(pos)) {
                    alarm.setAlarmPush(1);
                } else {
                    alarm.setAlarmPush(0);
                }
                break;
            case "platAlarm":
                // 平台报警
                if (name.contains("关键点")) {
                    alarm.setAlarmPush(1);
                } else {
                    if ("78".equals(pos) || "82".equals(pos)) {
                        alarm.setAlarmPush(0);
                    }
                    if ("77".equals(pos) && "param2".equals(alarm.getParamCode())) {
                        alarm.setParameterValue("02:00 -- 05:00");
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public JsonResultBean sendDeviceAlarmParam(String monitorId, String deviceType) {
        BindDTO bindDTO = VehicleUtil.getBindInfoByRedis(monitorId);
        if (bindDTO == null) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        String deviceNumber = bindDTO.getDeviceNumber();
        Integer msgSno = DeviceHelper.getRegisterDevice(monitorId, deviceNumber);
        String deviceId = bindDTO.getDeviceId();
        String simCardNumber = bindDTO.getSimCardNumber();
        String paramType = "0x8104-AlarmSettingData";
        String paramId = sendHelper.getLastSendParamID(monitorId, monitorId + paramType, paramType);
        if (Objects.nonNull(msgSno)) {
            // 设备已注册, 订阅回应的user
            String username = SystemHelper.getCurrentUsername();
            UserCache.put(Converter.toBlank(msgSno), username);
            // 下发参数
            sendHelper.updateParameterStatus(paramId, msgSno, 4, monitorId, paramType, monitorId + paramType);
            SendParam sendParam = new SendParam();
            sendParam.setMsgSNACK(msgSno);
            sendParam.setParamId(paramId);
            sendParam.setVehicleId(monitorId);
            sendParam.setMsgId("9999");
            f3SendStatusProcessService.updateSendParam(sendParam, 1);
            // 获取所有的终端参数
            sendTxtService.terminalParameters(deviceId, simCardNumber, msgSno, bindDTO.getDeviceType());
        } else {
            msgSno = 0;
            sendHelper.updateParameterStatus(paramId, msgSno, 5, monitorId, paramType, monitorId + paramType);
        }
        if (msgSno == 0) {
            return new JsonResultBean(JsonResultBean.FAULT, terminalOffLine);
        }
        String brand = bindDTO.getName();
        Integer plateColorInt = bindDTO.getPlateColor();
        String plateColor = plateColorInt == null ? "" : plateColorInt.toString();
        String orgName = bindDTO.getOrgName();
        String logMsg = "监控对象：" + brand + "( @" + orgName + ")" + " 获取F3-外设常规参数数据";
        String ipAddr = getIpAddress();
        logSearchServiceImpl.addLog(ipAddr, logMsg, "3", "", brand, plateColor);
        String username = SystemHelper.getCurrentUsername();
        JSONObject json = new JSONObject();
        json.put("msgId", String.valueOf(msgSno));
        json.put("userName", username);
        return new JsonResultBean(JsonResultBean.SUCCESS, JSON.toJSONString(json));
    }

    @Override
    public Map<String, Set<String>> deleteBatchLinkageSetting(Set<String> needRemoveMonitors) {
        List<AlarmLinkageParam> oldSettingList = alarmSettingDao.findLinkageSettingListByMonitorIds(needRemoveMonitors);
        if (CollectionUtils.isEmpty(oldSettingList)) {
            return new HashMap<>(4);
        }
        Map<String, Set<String>> monitorAlarmTypeCache = new HashMap<>(16);
        List<String> linkageSettingIdList = new ArrayList<>(oldSettingList.size());
        List<String> photoAndRecordingSettingIdList = new ArrayList<>(oldSettingList.size());
        List<String> outControlSettingIdList = new ArrayList<>(oldSettingList.size());
        List<String> msgSettingIdList = new ArrayList<>(oldSettingList.size());
        // 组装设置了联动策略的数据->拍照和录像、下发短信、输出控制ID
        oldSettingList.forEach(p -> {
            linkageSettingIdList.add(p.getId());
            String photoId = p.getPhotoId();
            if (StringUtils.isNotEmpty(photoId)) {
                photoAndRecordingSettingIdList.add(photoId);
            }
            String recordingId = p.getRecordingId();
            if (StringUtils.isNotEmpty(recordingId)) {
                photoAndRecordingSettingIdList.add(recordingId);
            }
            if (StringUtils.isNotEmpty(p.getOutputControlId())) {
                outControlSettingIdList.add(p.getOutputControlId());
            }
            if (StringUtils.isNotEmpty(p.getMsgId())) {
                msgSettingIdList.add(p.getMsgId());
            }
            monitorAlarmTypeCache.computeIfAbsent(p.getVehicleId(), k -> new HashSet<>()).add(p.getAlarmTypeId());
        });
        alarmSettingDao.deleteLinkageSettingByBatch(linkageSettingIdList);
        if (CollectionUtils.isNotEmpty(photoAndRecordingSettingIdList)) {
            alarmSettingDao.deletePhotoSettingByBatch(photoAndRecordingSettingIdList);
        }

        if (CollectionUtils.isNotEmpty(outControlSettingIdList)) {
            alarmSettingDao.deleteOutputControlSettingByBatch(outControlSettingIdList);
        }

        if (CollectionUtils.isNotEmpty(msgSettingIdList)) {
            alarmSettingDao.deleteMsgSettingByBatch(msgSettingIdList);
        }
        return monitorAlarmTypeCache;
    }

    @Override
    public JsonResultBean sendParameter(String vehicleId, String paramIdStr) {
        BindDTO bindDTO = VehicleUtil.getBindInfoByRedis(vehicleId);
        if (bindDTO == null) {
            return new JsonResultBean(JsonResultBean.FAULT, "监控对象不存在");
        }
        String deviceNumber = bindDTO.getDeviceNumber();
        Integer msgSno = DeviceHelper.getRegisterDevice(vehicleId, deviceNumber);
        String deviceId = bindDTO.getDeviceId();
        String simCardNumber = bindDTO.getSimCardNumber();
        String paramType = "F3-highPrecisionAlarm";
        String paramName = vehicleId + "_" + paramType;
        String paramId = getLastSendParamID(vehicleId, paramName, paramType);
        String username = SystemHelper.getCurrentUsername();
        if (msgSno != null) {
            UserCache.put(Converter.toBlank(msgSno), username);
            sendHelper.updateParameterStatus(paramId, msgSno, 4, vehicleId, paramType, paramName);
            SubscibeInfo info =
                new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, msgSno, ConstantUtil.T808_PARAM_ACK);
            SubscibeInfoCache.getInstance().putTable(info);
            List<ParamItem> paramItems = Arrays.stream(paramIdStr.split(","))
                .map(id -> {
                    ParamItem item = new ParamItem();
                    item.setParamId(Integer.parseInt(id.replaceAll("0x", ""), 16));
                    return item;
                })
                .collect(Collectors.toList());
            sendParam(simCardNumber, paramItems, msgSno, deviceId, bindDTO.getDeviceType());
            // 设备未注册
        } else {
            msgSno = 0;
            sendHelper.updateParameterStatus(paramId, msgSno, 5, vehicleId, paramType, paramName);
        }
        // 如果终端流水号为0,则终端离线
        if ("0".equals(String.valueOf(msgSno))) {
            return new JsonResultBean(JsonResultBean.FAULT, "终端离线");
        }
        JSONObject json = new JSONObject();
        json.put("msgId", String.valueOf(msgSno));
        json.put("userName", username);
        return new JsonResultBean(JsonResultBean.SUCCESS, JSON.toJSONString(json));
    }

    private String getLastSendParamID(String vehicleId, String parameterName, String type) {
        List<Directive> paramlist = parameterDao.findParameterByType(vehicleId, parameterName, type);
        Directive param;
        if (paramlist != null && !paramlist.isEmpty()) {
            param = paramlist.get(0);
            return param.getId();
        }
        return "";
    }

    public void sendParam(String mobile, List<ParamItem> paramItems, Integer msgSN, String deviceId,
        String deviceType) {
        T808Msg8106 t8080x8106 = new T808Msg8106();
        t8080x8106.setParamSum(paramItems.size());
        t8080x8106.setParamItems(paramItems);
        T808Message message =
            MsgUtil.get808Message(mobile, ConstantUtil.T808_QUERY_PARAMS, msgSN, t8080x8106, deviceType);
        WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_QUERY_PARAMS, deviceId);
    }

    @Override
    public boolean deleteRoadAlarmSpeedLimit() {
        return alarmSettingDao.deleteRoadAlarmSpeedLimit();
    }

    private SpecialAlarmDO buildSpecialAlarmDO(AlarmLinkageDTO req, String vehicleId) {
        SpecialAlarmDO specialAlarm = new SpecialAlarmDO();
        specialAlarm.setVehicleId(vehicleId);
        specialAlarm.setAlarmTypeId(req.getPos());
        specialAlarm.setVideoFlag(req.getVideoFlag());
        specialAlarm.setUploadAudioResourcesFlag(req.getUploadAudioResourcesFlag());
        specialAlarm.setAlarmHandleType(req.getAlarmHandleType());
        specialAlarm.setAlarmHandleResult(req.getAlarmHandleResult());
        final Integer alarmHandleLinkageCheck = req.getAlarmHandleLinkageCheck();
        if (Objects.isNull(alarmHandleLinkageCheck)) {
            specialAlarm.setAlarmHandleLinkageCheck(ALARM_HANDLE_LINKAGE_NON_CHECK);
            specialAlarm.setHandleUsername(StringUtils.EMPTY);
        } else {
            specialAlarm.setHandleUsername(req.getHandleUsername());
            specialAlarm.setAlarmHandleLinkageCheck(alarmHandleLinkageCheck);
        }
        specialAlarm.setCreateDataUsername(SystemHelper.getCurrentUsername());
        return specialAlarm;
    }

    /**
     * 组装拍照
     * @param photoAndRecodingList photoAndRecodingList
     * @param req req
     * @param specialAlarm specialAlarm
     */
    private void buildPhotoList(List<PhotoParam> photoAndRecodingList,
                                AlarmLinkageDTO req,
                                SpecialAlarmDO specialAlarm) {
        final PhotoDTO photoReq = req.getPhoto();
        if (photoReq == null) {
            return;
        }
        final PhotoParam photoParam = PhotoParam.of(photoReq);
        photoAndRecodingList.add(photoParam);
        specialAlarm.setPhotoId(photoParam.getId());
    }

    /**
     * 组装录像
     * @param photoAndRecodingList photoAndRecodingList
     * @param req req
     * @param specialAlarm specialAlarm
     */
    private void buildVideoList(List<PhotoParam> photoAndRecodingList,
                                AlarmLinkageDTO req,
                                SpecialAlarmDO specialAlarm) {
        final PhotoDTO recording = req.getRecording();
        if (recording == null) {
            return;
        }
        final PhotoParam photoParam = PhotoParam.of(recording);
        photoParam.setCommand(0xFFFF);
        recording.setCommand(0xFFFF);
        photoAndRecodingList.add(photoParam);
        specialAlarm.setRecordingId(photoParam.getId());
    }

    /**
     * 组装输出控制
     * @param outputControlList outputControlList
     * @param req req
     * @param specialAlarm specialAlarm
     * @param monitorId monitorId
     */
    private void buildOutputControlList(List<OutputControl> outputControlList, AlarmLinkageDTO req,
            SpecialAlarmDO specialAlarm, String monitorId) {
        final OutputControlDTO outputControlReq = req.getOutputControl();
        if (outputControlReq == null) {
            return;
        }
        final OutputControl outputControl = OutputControl.of(outputControlReq, monitorId, req.getDeviceType());
        outputControlList.add(outputControl);
        specialAlarm.setOutputControlId(outputControl.getId());
    }

    private String validAlarmHandle(AlarmLinkageDTO req) {
        final Integer alarmHandleLinkage = req.getAlarmHandleLinkageCheck();
        if (alarmHandleLinkage == null || alarmHandleLinkage == ALARM_HANDLE_LINKAGE_NON_CHECK) {
            return null;
        }

        final Integer alarmHandleResult = req.getAlarmHandleResult();
        if (alarmHandleResult == null) {
            return "处理结果不能为空!";
        }

        // 勾选了报警联动处理
        final String handleUsername = req.getHandleUsername();
        if (StringUtils.isBlank(handleUsername)) {
            return "报警处理联动-处理人不能为空!";
        }

        final Integer alarmHandleType = req.getAlarmHandleType();

        if (alarmHandleType == HANDLE_TYPE_MSG) {
            // 处理方式为"下发短信", 则下发短信的内容不能为空
            final MsgParamDTO msg = req.getMsg();
            if (Objects.isNull(msg)) {
                return "保存失败，请完成下发短信的设置！";
            }

            final String marks = msg.getMarks();
            if (StringUtils.isBlank(marks) && !marks.contains(Msgparam.MARK_DEVICE_VOICE)) {
                return "保存失败，下发短信时TSS读播为必选项！";
            }
        } else if (alarmHandleType == HANDLE_TYPE_PHOTO) {
            // 处理方式为拍照, 则拍照的内容不能为空
            final PhotoDTO photo = req.getPhoto();
            if (Objects.isNull(photo)) {
                return "保存失败，请完成拍照的设置！";
            }

            if (StringUtils.isEmpty(photo.getWayId())) {
                return "保存失败，拍照时通道ID为必选项！";
            }
        }
        return null;
    }

    @Override
    public List<AlarmParameter> getAlarmParameterByAlarmParameterIds(Collection<String> alarmParameterIds) {
        if (CollectionUtils.isEmpty(alarmParameterIds)) {
            new ArrayList<>();
        }
        return alarmSettingDao.getAlarmParameterByAlarmParameterIds(alarmParameterIds);
    }
}

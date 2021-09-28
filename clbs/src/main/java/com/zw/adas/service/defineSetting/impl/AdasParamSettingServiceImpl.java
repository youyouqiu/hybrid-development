package com.zw.adas.service.defineSetting.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.google.common.collect.Sets;
import com.zw.adas.domain.define.enumcontant.AdasSettingLengthEnum;
import com.zw.adas.domain.define.setting.AdasAlarmParamSetting;
import com.zw.adas.domain.define.setting.AdasCommonParamSetting;
import com.zw.adas.domain.define.setting.AdasJingParamSetting;
import com.zw.adas.domain.define.setting.AdasPlatformParamSetting;
import com.zw.adas.domain.define.setting.AdasSettingListDo;
import com.zw.adas.domain.define.setting.dto.AdasParamRequestDTO;
import com.zw.adas.domain.define.setting.query.AdasParamSettingForm;
import com.zw.adas.domain.define.setting.query.AdasRiskParamQuery;
import com.zw.adas.push.cache.AdasSubcibeTable;
import com.zw.adas.repository.mysql.riskdisposerecord.AdasAlarmJingParamSettingDao;
import com.zw.adas.repository.mysql.riskdisposerecord.AdasAlarmParamSettingDao;
import com.zw.adas.repository.mysql.riskdisposerecord.AdasCommonParamSettingDao;
import com.zw.adas.service.defineSetting.AdasParamSettingService;
import com.zw.adas.service.defineSetting.AdasSendTxtService;
import com.zw.adas.utils.AdasDirectiveStatusOutTimeUtil;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.MonitorTypeEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.domain.BaseKvDo;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.GroupMonitorDTO;
import com.zw.platform.basic.repository.NewVehicleDao;
import com.zw.platform.basic.service.GroupMonitorService;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.basic.util.common.MonitorHelper;
import com.zw.platform.commons.ParallelWorker;
import com.zw.platform.commons.RedisException;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.systems.Directive;
import com.zw.platform.domain.systems.form.DirectiveForm;
import com.zw.platform.push.controller.SubscibeInfo;
import com.zw.platform.push.controller.SubscibeInfoCache;
import com.zw.platform.push.controller.UserCache;
import com.zw.platform.push.handler.device.DeviceHelper;
import com.zw.platform.push.mqserver.ZMQFencePub;
import com.zw.platform.repository.modules.ParameterDao;
import com.zw.platform.service.redis.RedisVehicleService;
import com.zw.platform.service.reportManagement.impl.LogSearchServiceImpl;
import com.zw.platform.util.CommonUtil;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.MsgUtil;
import com.zw.platform.util.SendHelper;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.RedisQueryUtil;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.platform.util.spring.InitData;
import com.zw.protocol.msg.t808.T808Message;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import com.zw.protocol.util.ProtocolTypeUtil;
import com.zw.ws.entity.adas.paramSetting.ChuanBlindArea;
import com.zw.ws.entity.adas.paramSetting.ChuanDriverAssistance;
import com.zw.ws.entity.adas.paramSetting.ChuanDriverSurvey;
import com.zw.ws.entity.adas.paramSetting.ChuanIntenseDriving;
import com.zw.ws.entity.adas.paramSetting.ChuanTirePressure;
import com.zw.ws.entity.adas.paramSetting.GanBlindArea;
import com.zw.ws.entity.adas.paramSetting.GanDriverAssistance;
import com.zw.ws.entity.adas.paramSetting.GanDriverSurvey;
import com.zw.ws.entity.adas.paramSetting.GuiBlindArea;
import com.zw.ws.entity.adas.paramSetting.GuiDriverAssistance;
import com.zw.ws.entity.adas.paramSetting.GuiDriverSurvey;
import com.zw.ws.entity.adas.paramSetting.GuiIntenseDriving;
import com.zw.ws.entity.adas.paramSetting.GuiTirePressure;
import com.zw.ws.entity.adas.paramSetting.HeiDriverDrivingBehavior;
import com.zw.ws.entity.adas.paramSetting.HeiDriverIdentification;
import com.zw.ws.entity.adas.paramSetting.HeiEquipmentFailureMonitoring;
import com.zw.ws.entity.adas.paramSetting.HeiVehicleOperationMonitoring;
import com.zw.ws.entity.adas.paramSetting.HuBlindArea;
import com.zw.ws.entity.adas.paramSetting.HuDriverAssistance;
import com.zw.ws.entity.adas.paramSetting.HuDriverSurvey;
import com.zw.ws.entity.adas.paramSetting.HuOvercrowding;
import com.zw.ws.entity.adas.paramSetting.JiDriverAssistance;
import com.zw.ws.entity.adas.paramSetting.JiDriverSurvey;
import com.zw.ws.entity.adas.paramSetting.JiLinBlindArea;
import com.zw.ws.entity.adas.paramSetting.JiLinDriverAssistance;
import com.zw.ws.entity.adas.paramSetting.JiLinDriverSurvey;
import com.zw.ws.entity.adas.paramSetting.JingAdasSendInfo;
import com.zw.ws.entity.adas.paramSetting.LuBlindArea;
import com.zw.ws.entity.adas.paramSetting.LuDriverAssistance;
import com.zw.ws.entity.adas.paramSetting.LuDriverComparison;
import com.zw.ws.entity.adas.paramSetting.LuDriverSurvey;
import com.zw.ws.entity.adas.paramSetting.LuTirePressure;
import com.zw.ws.entity.adas.paramSetting.ShanBlindArea;
import com.zw.ws.entity.adas.paramSetting.ShanDriverAssistance;
import com.zw.ws.entity.adas.paramSetting.ShanDriverSurvey;
import com.zw.ws.entity.adas.paramSetting.SuBlindArea;
import com.zw.ws.entity.adas.paramSetting.SuDriverAssistance;
import com.zw.ws.entity.adas.paramSetting.SuDriverSurvey;
import com.zw.ws.entity.adas.paramSetting.SuTirePressure;
import com.zw.ws.entity.adas.paramSetting.XiangDriverAssistance;
import com.zw.ws.entity.adas.paramSetting.XiangDriverComparison;
import com.zw.ws.entity.adas.paramSetting.XiangDriverSurvey;
import com.zw.ws.entity.adas.paramSetting.XiangOvercrowding;
import com.zw.ws.entity.adas.paramSetting.YueBlindArea;
import com.zw.ws.entity.adas.paramSetting.YueDriverAssistance;
import com.zw.ws.entity.adas.paramSetting.YueDriverSurvey;
import com.zw.ws.entity.adas.paramSetting.YueTirePressure;
import com.zw.ws.entity.adas.paramSetting.ZheBlindArea;
import com.zw.ws.entity.adas.paramSetting.ZheDriverAssistance;
import com.zw.ws.entity.adas.paramSetting.ZheDriverSurvey;
import com.zw.ws.entity.adas.paramSetting.ZhongBlindArea;
import com.zw.ws.entity.adas.paramSetting.ZhongDriverAssistance;
import com.zw.ws.entity.adas.paramSetting.ZhongDriverSurvey;
import com.zw.ws.entity.adas.paramSetting.ZhongTirePressure;
import com.zw.ws.entity.t808.oil.T808_0x8900;
import com.zw.ws.entity.t808.parameter.ParamItem;
import com.zw.ws.entity.t808.simcard.T808Msg8106;
import jodd.util.StringUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * adas参数实现
 * 2019/6/10 11:01
 *
 * @author gfw
 * @version 1.0
 **/
@Service
public class AdasParamSettingServiceImpl implements AdasParamSettingService {

    /**
     * 在线
     */
    private static final int ON_LINE = 1;
    /**
     * 离线
     */
    private static final int OFF_LINE = 3;
    /**
     * 推送标示
     */
    private static final int IS_PUSH = 1;

    private static final Map<Integer, Integer[]> jingParamMap = new HashMap<>();
    private static final Logger log = LogManager.getLogger(AdasParamSettingServiceImpl.class);
    private static final List<String> parameterNames = new ArrayList<>();

    static {
        parameterNames.add("64_ADAS");
        parameterNames.add("65_ADAS");
        parameterNames.add("70_ADAS");
        parameterNames.add("66_ADAS");
        parameterNames.add("67_ADAS");
        parameterNames.add("51_ADAS");
        parameterNames.add("52_ADAS");
        //驾驶员比对参数设置
        parameterNames.add("233_ADAS");
        // 51 代表京标驾驶行为报警参数设置查询 52 代表京标车辆运行监测报警参数设置查询  53 代表京标终端参数查询
        jingParamMap.put(51, new Integer[] { 0xF511, 0xF512, 0xF513, 0xF514, 0xF515, 0xF516, 0xF517 });
        jingParamMap.put(52, new Integer[] { 0xF521, 0xF522, 0xF523, 0xF524, 0xF525, 0xF526, 0xF527, 0xF528, 0xF529 });
        jingParamMap.put(53, new Integer[] { 0xFF00, 0xFF01, 0xFF02 });
    }

    @Autowired
    AdasAlarmParamSettingDao alarmParamDao;

    @Autowired
    private MonitorHelper monitorHelper;

    @Autowired
    AdasCommonParamSettingDao commonParamDao;
    @Autowired
    AdasAlarmJingParamSettingDao jingParamSettingDao;
    @Autowired
    private AdasSubcibeTable adasSubcibeTable;

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;
    /**
     * 指令下发协议标志位
     */
    private final String parameterType = "F3_ADAS_";
    @Autowired
    private LogSearchServiceImpl logSearchServiceImpl;
    @Autowired
    private NewVehicleDao newVehicleDao;
    @Autowired
    private GroupMonitorService groupMonitorService;
    @Autowired
    private UserService userService;
    @Autowired
    private OrganizationService organizationService;
    @Autowired
    private AdasSendTxtService adasSendTxtService;
    private RedisVehicleService redisVehicleService;
    @Autowired
    private SendHelper sendHelper;
    @Autowired
    private ParameterDao parameterDao;

    @Autowired
    public void setRedisVehicleService(RedisVehicleService redisVehicleService) {
        this.redisVehicleService = redisVehicleService;
    }

    /**
     * adas参数设置 批量/单个
     */

    private boolean insertIntoParamSetting(AdasParamRequestDTO requestDTO,
        LinkedBlockingQueue<Map<String, String>> queue) {
        List<String> vehicleIds = requestDTO.getVehicleIdList();
        List<AdasParamSettingForm> adasParamSettingForms = requestDTO.getAlarmParamSettingList();
        List<AdasPlatformParamSetting> platformParamSettings = requestDTO.getPlatformParamSettingList();
        boolean sendFlag = requestDTO.isSendFlag();

        //需要删除的页签
        Set<Integer> paramTypeSet = new HashSet<>();
        if (platformParamSettings != null && platformParamSettings.size() > 0) {
            deletePlatformParams(vehicleIds);
        }
        if (vehicleIds != null && vehicleIds.size() != 0) {
            List<AdasAlarmParamSetting> alarmList = new ArrayList<>();
            List<AdasCommonParamSetting> paramList = new ArrayList<>();
            List<AdasPlatformParamSetting> platformParamList = new ArrayList<>();
            List<String> lst = new ArrayList<>();
            String type = "";
            for (String vehicleId : vehicleIds) {
                for (AdasParamSettingForm paramSettingForm : adasParamSettingForms) {
                    // 通用参数
                    AdasCommonParamSetting commonParam = paramSettingForm.getCommonParamSetting();
                    paramTypeSet.add(commonParam.getParamType());
                    type = "F3_ADAS_" + commonParam.getProtocolType();
                    // 报警参数
                    for (AdasAlarmParamSetting alarmParamSetting : paramSettingForm.getAdasAlarmParamSettings()) {
                        alarmParamSetting.setId(UUID.randomUUID().toString());
                        alarmParamSetting.setVehicleId(vehicleId);
                        alarmParamSetting.setParamType(commonParam.getParamType());
                        AdasAlarmParamSetting alarmSettings = new AdasAlarmParamSetting();
                        BeanUtils.copyProperties(alarmParamSetting, alarmSettings);
                        alarmList.add(alarmSettings);
                    }
                    commonParam.setId(UUID.randomUUID().toString());
                    commonParam.setVehicleId(vehicleId);
                    // 处理定距单位换算
                    //dealCommonUnit(commonParam);
                    AdasCommonParamSetting common = new AdasCommonParamSetting();
                    BeanUtils.copyProperties(commonParam, common);
                    paramList.add(common);
                }
                //平台参数设置
                List<AdasPlatformParamSetting> pushList = new ArrayList<>();
                Map<String, AdasPlatformParamSetting> settingMap = new HashMap<>();
                for (AdasPlatformParamSetting platformParamSetting : platformParamSettings) {
                    platformParamSetting.setVehicleId(vehicleId);
                    AdasPlatformParamSetting platform = new AdasPlatformParamSetting();
                    BeanUtils.copyProperties(platformParamSetting, platform);
                    platformParamList.add(platform);
                    if (IS_PUSH == platform.getAlarmSwitch()) {
                        pushList.add(platform);
                        settingMap.put(platform.getRiskFunctionId(), platform);
                    }
                }
                if (!settingMap.isEmpty()) {
                    InitData.platformParamMap.put(vehicleId, settingMap);
                }
                String key = getRiskVehicleKey(vehicleId);
                if (!adasParamSettingForms.isEmpty()) {
                    // redis 缓存
                    RedisHelper.setString(HistoryRedisKeyEnum.ADAS_PARAM_VEHICLE.of(vehicleId),
                        JSON.toJSONString(adasParamSettingForms));
                    // 新增redis
                    Map<String, String> emap = new HashMap<>(8);
                    emap.put("msg", "exist");
                    RedisHelper.addToHash(HistoryRedisKeyEnum.ADAS_RISK_AUTO_VEHICLE.of(vehicleId), emap);
                }
                // zmq通知
                String adasSetting =
                    getAdasSetting(key, pushList, adasParamSettingForms.size() != 0, platformParamSettings);
                lst.add(adasSetting);
            }
            // zmq 发送消息
            sendMessage(lst);
            if (paramTypeSet.size() > 0) {
                commonParamDao.deleteCommonByParamType(vehicleIds, paramTypeSet);
                alarmParamDao.deleteAdasByParamType(vehicleIds, paramTypeSet);
            }
            boolean flag = true;
            if (!adasParamSettingForms.isEmpty()) {
                boolean flag1 = alarmParamDao.insertAlarmParamBatch(alarmList);
                // 新增通用表
                boolean flag2 = commonParamDao.insertCommonParamBatch(paramList);
                flag = flag1 && flag2;
                // 是否参数下发
                if (sendFlag) {
                    deleteSendDirectBeforeSend(requestDTO);
                    updateDirectiveStatus(vehicleIds, adasParamSettingForms, type, queue);
                }
            }
            if (!platformParamList.isEmpty()) {
                boolean flag3 = alarmParamDao.insertPlatformParams(platformParamList);
                flag = flag3 && flag;
            }
            addSettingLog(vehicleIds, getIpAddress(), "1", "新增参数设置");
            // 结果集
            return flag;

        }
        return true;
    }

    @Override
    public boolean addAndSendParam(AdasParamRequestDTO requestDTO) throws BusinessException {
        //检查车辆id是否为空
        requestDTO.checkVehIdIsEmpty();
        //初始化平台相关参数
        requestDTO.init();
        LinkedBlockingQueue<Map<String, String>> queue = new LinkedBlockingQueue<>();
        //参数保存
        boolean flag = insertIntoParamSetting(requestDTO, queue);
        if (flag && requestDTO.canSendParam()) {

            processingThreads(queue, requestDTO.getProtocolType());
        }
        return false;
    }

    private void deleteSendDirectBeforeSend(AdasParamRequestDTO requestDTO) {
        //本页签下发在下发之前需要删除以前下发状态数据
        List<AdasParamSettingForm> alarmParamSettingList = requestDTO.getAlarmParamSettingList();
        if (alarmParamSettingList.size() == 1) {
            AdasParamSettingForm data = alarmParamSettingList.get(0);
            AdasCommonParamSetting commonParamSetting = data.getCommonParamSetting();
            String vid = commonParamSetting.getVehicleId();
            String parameterName = commonParamSetting.getParamType() + "_ADAS";
            String parameterType = "F3_ADAS_" + commonParamSetting.getProtocolType();
            List<Directive> directives = parameterDao.findParameterByType(vid, parameterName, parameterType);
            List<String> ids = directives.stream().map(Directive::getId).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(ids)) {
                return;
            }
            parameterDao.deleteByIds(ids);
        }
    }

    private void updateDirectiveStatus(List<String> vehicleIds, List<AdasParamSettingForm> adasParamSettingForms,
        String type, LinkedBlockingQueue<Map<String, String>> paramStatusQueue) {
        List<DirectiveForm> directiveForms = new ArrayList<>();
        Set<String> directiveParamIds = new HashSet<>();
        Map<String, Map<String, String>> map = getDirectiveMap(vehicleIds, type);
        Set<String> allOnLine = getAllOnlineVids();
        for (String vehicleId : vehicleIds) {
            for (AdasParamSettingForm paramSettingForm : adasParamSettingForms) {
                String parameterType = "F3_ADAS_" + paramSettingForm.getCommonParamSetting().getProtocolType();
                String parameterName = paramSettingForm.getCommonParamSetting().getParamType() + "_ADAS";
                int msgnSn = allOnLine.contains(vehicleId) ? -1 : 0;
                Integer status = msgnSn == 0 ? 5 : 4;
                DirectiveForm directiveForm =
                    sendHelper.generateDirective(vehicleId, status, parameterType, msgnSn, parameterName, 1, null);
                if (map.get(vehicleId) != null && map.get(vehicleId).get(parameterName) != null) {
                    directiveForm.setId(map.get(vehicleId).get(parameterName));
                }
                directiveParamIds.add(directiveForm.getId());
                directiveForms.add(directiveForm);
            }
            Map<String, String> vehicleMap = new HashMap<>();
            vehicleMap.put(vehicleId, SystemHelper.getCurrentUsername());
            offerParamStatus(vehicleMap, paramStatusQueue);
        }
        alarmParamDao.updateDirectiveStatus(directiveForms);
        maintainOutTimeCache(directiveParamIds);
    }

    private Map<String, Map<String, String>> getDirectiveMap(List<String> vehicleIds, String type) {
        Map<String, Map<String, String>> map = new HashMap<>();
        List<Directive> directives = sendHelper.findParameterByVehicleIds(vehicleIds, parameterNames, type);
        for (Directive directive : directives) {
            Map<String, String> paramMap = new HashMap<>();
            if (map.get(directive.getMonitorObjectId()) != null) {
                paramMap = map.get(directive.getMonitorObjectId());
            }
            paramMap.put(directive.getParameterName(), directive.getId());
            map.put(directive.getMonitorObjectId(), paramMap);
        }
        return map;
    }

    private void updateDirStatus(Map<String, List<AdasParamSettingForm>> map, String type,
        LinkedBlockingQueue<Map<String, String>> paramStatusQueue) {
        List<DirectiveForm> directiveForms = new ArrayList<>();
        Map<String, Map<String, String>> directiveMap = getDirectiveMap(new ArrayList<>(map.keySet()), type);
        Set<String> allOnLine = getAllOnlineVids();
        Integer protocol = null;
        for (Map.Entry<String, List<AdasParamSettingForm>> entry : map.entrySet()) {
            for (AdasParamSettingForm paramSettingForm : entry.getValue()) {
                if (protocol == null) {
                    protocol = paramSettingForm.getCommonParamSetting().getProtocolType();
                }
                String parameterType = "F3_ADAS_" + paramSettingForm.getCommonParamSetting().getProtocolType();
                String parameterName = paramSettingForm.getCommonParamSetting().getParamType() + "_ADAS";
                int msgnSn = allOnLine.contains(entry.getKey()) ? -1 : 0;
                Integer status = msgnSn == 0 ? 5 : 4;
                DirectiveForm directiveForm =
                    sendHelper.generateDirective(entry.getKey(), status, parameterType, msgnSn, parameterName, 1, null);
                if (directiveMap.get(entry.getKey()) != null
                    && directiveMap.get(entry.getKey()).get(parameterName) != null) {
                    directiveForm.setId(directiveMap.get(entry.getKey()).get(parameterName));
                }
                directiveForms.add(directiveForm);
            }
            Map<String, String> vehicleMap = new HashMap<>();
            vehicleMap.put(entry.getKey(), SystemHelper.getCurrentUsername());
            offerParamStatus(vehicleMap, paramStatusQueue);
        }
        alarmParamDao.updateDirectiveStatus(directiveForms);
    }

    @Override
    public void sendParam(String vehicleId, List<AdasParamSettingForm> adasParamSettingForms, String userName) {
        for (AdasParamSettingForm paramSettingForm : adasParamSettingForms) {
            switch (paramSettingForm.getCommonParamSetting().getParamType()) {
                //驾驶员身份识别参数下发（黑标）
                case 38:
                    send38Param(paramSettingForm, vehicleId, userName);
                    break;
                //车辆运行监测参数下发（黑标）
                case 39:
                    send39Param(paramSettingForm, vehicleId, userName);
                    break;
                //驾驶员驾驶行为参数下发（黑标）
                case 40:
                    send40Param(paramSettingForm, vehicleId, userName);
                    break;
                //设备失效监测参数下发（黑标）
                case 41:
                    send41Param(paramSettingForm, vehicleId, userName);
                    break;
                //前向参数下发
                case 64:
                    send64param(paramSettingForm, vehicleId, userName);
                    break;
                //驾驶员行为异常参数下发
                case 65:
                    send65param(paramSettingForm, vehicleId, userName);
                    break;
                //胎压状态参数设置
                case 66:
                    send66param(paramSettingForm, vehicleId, userName);
                    break;
                //盲区监测参数设置
                case 67:
                    send67param(paramSettingForm, vehicleId, userName);
                    break;
                //不按规定上下课超员
                case 68:
                    send68param(paramSettingForm, vehicleId, userName);
                    break;
                //激烈驾驶
                case 70:
                    send70param(paramSettingForm, vehicleId, userName);
                    break;
                case 0xE9:
                    sendE9param(paramSettingForm, vehicleId, userName);
                    break;
                default:
                    break;
            }
        }
    }

    private void sendE9param(AdasParamSettingForm paramSettingForm, String vehicleId, String userName) {
        Object driverComparison = null;
        Integer protocolType = paramSettingForm.getCommonParamSetting().getProtocolType();
        switch (String.valueOf(protocolType)) {
            //鲁标
            case ProtocolTypeUtil.LU_PROTOCOL_808_2019:
                driverComparison = new LuDriverComparison(paramSettingForm);
                break;
            //湘标
            case ProtocolTypeUtil.XIANG_PROTOCOL_808_2013:
                driverComparison = new XiangDriverComparison(paramSettingForm);
                break;
            default:
                log.info("驾驶员对比设置未成功下发,protocolType:" + protocolType + ",未匹配到对应的协议类型.");
                break;
        }
        if (driverComparison != null) {
            //下发异常驾驶员行为
            send(0xF0E9, driverComparison, vehicleId, String.valueOf(protocolType), 0xE9, userName);
        }
    }

    /**
     * 设备失效监测参数下发
     */
    private void send41Param(AdasParamSettingForm paramSettingForm, String vehicleId, String userName) {
        HeiEquipmentFailureMonitoring sendParam = new HeiEquipmentFailureMonitoring(paramSettingForm);
        Integer protocolType = paramSettingForm.getCommonParamSetting().getProtocolType();
        Integer parameterId = 0xE141;
        send(parameterId, sendParam, vehicleId, String.valueOf(protocolType), 41, userName);
    }

    /**
     * 驾驶员驾驶行为参数下发
     */
    private void send40Param(AdasParamSettingForm paramSettingForm, String vehicleId, String userName) {
        HeiDriverDrivingBehavior sendParam = new HeiDriverDrivingBehavior(paramSettingForm);
        Integer protocolType = paramSettingForm.getCommonParamSetting().getProtocolType();
        Integer parameterId = 0xE140;
        send(parameterId, sendParam, vehicleId, String.valueOf(protocolType), 40, userName);
    }

    /**
     * 车辆运行监测参数下发
     */
    private void send39Param(AdasParamSettingForm paramSettingForm, String vehicleId, String userName) {
        HeiVehicleOperationMonitoring sendParam = new HeiVehicleOperationMonitoring(paramSettingForm);
        Integer protocolType = paramSettingForm.getCommonParamSetting().getProtocolType();
        Integer parameterId = 0xE139;
        send(parameterId, sendParam, vehicleId, String.valueOf(protocolType), 39, userName);
    }

    /**
     * 驾驶员身份识别参数下发
     */
    private void send38Param(AdasParamSettingForm paramSettingForm, String vehicleId, String userName) {
        HeiDriverIdentification sendParam = new HeiDriverIdentification(paramSettingForm);
        Integer protocolType = paramSettingForm.getCommonParamSetting().getProtocolType();
        Integer parameterId = 0xE138;
        send(parameterId, sendParam, vehicleId, String.valueOf(protocolType), 38, userName);
    }

    private void send68param(AdasParamSettingForm paramSettingForm, String vehicleId, String userName) {
        Object overcrowding = null;
        Integer protocolType = paramSettingForm.getCommonParamSetting().getProtocolType();
        switch (String.valueOf(protocolType)) {
            //川标
            case ProtocolTypeUtil.SI_CHUAN_PROTOCOL_808_2013:
                break;
            //冀标
            case ProtocolTypeUtil.JI_PROTOCOL_808_2013:
                break;
            //桂标
            case ProtocolTypeUtil.GUANG_XI_PROTOCOL_808_2013:
                break;
            //苏标
            case ProtocolTypeUtil.JIANG_SU_PROTOCOL_808_2013:
                break;
            //浙标
            case ProtocolTypeUtil.ZHE_JIANG_PROTOCOL_808_2013:
                break;
            //吉标
            case ProtocolTypeUtil.JI_LIN_PROTOCOL_808_2013:
                break;
            //陕标
            case ProtocolTypeUtil.SHAN_XI_PROTOCOL_808_2013:
                break;
            //赣标(江西)
            case ProtocolTypeUtil.JIANG_XI_PROTOCOL_808_2013:
                break;
            //沪标(上海)
            case ProtocolTypeUtil.SHANG_HAI_PROTOCOL_808_2019:
                overcrowding = new HuOvercrowding(paramSettingForm);
                break;
            //湘标
            case ProtocolTypeUtil.XIANG_PROTOCOL_808_2013:
                overcrowding = new XiangOvercrowding(paramSettingForm);
                break;
            default:
                log.info("不按规定上下课设置未成功下发,protocolType:" + protocolType + ",未匹配到对应的协议类型.");
                break;
        }
        if (overcrowding != null) {
            send(0xF368, overcrowding, vehicleId, String.valueOf(protocolType), 68, userName);
        }
    }

    private void send64param(AdasParamSettingForm paramSettingForm, String vehicleId, String userName) {
        Object driverAssistance = null;
        Integer protocolType = paramSettingForm.getCommonParamSetting().getProtocolType();
        int parameterId = 0xF364;
        switch (String.valueOf(protocolType)) {
            //川标
            case ProtocolTypeUtil.SI_CHUAN_PROTOCOL_808_2013:
                driverAssistance = new ChuanDriverAssistance(paramSettingForm);
                break;
            //冀标
            case ProtocolTypeUtil.JI_PROTOCOL_808_2013:
                driverAssistance = new JiDriverAssistance(paramSettingForm);
                break;
            //桂标
            case ProtocolTypeUtil.GUANG_XI_PROTOCOL_808_2013:
                driverAssistance = new GuiDriverAssistance(paramSettingForm);
                break;
            //苏标
            case ProtocolTypeUtil.JIANG_SU_PROTOCOL_808_2013:
                driverAssistance = new SuDriverAssistance(paramSettingForm);
                break;
            //浙标
            case ProtocolTypeUtil.ZHE_JIANG_PROTOCOL_808_2013:
                driverAssistance = new ZheDriverAssistance(paramSettingForm);
                break;
            //吉标
            case ProtocolTypeUtil.JI_LIN_PROTOCOL_808_2013:
                driverAssistance = new JiLinDriverAssistance(paramSettingForm);
                break;
            //陕标
            case ProtocolTypeUtil.SHAN_XI_PROTOCOL_808_2013:
                driverAssistance = new ShanDriverAssistance(paramSettingForm);
                break;
            //赣标(江西)
            case ProtocolTypeUtil.JIANG_XI_PROTOCOL_808_2013:
                driverAssistance = new GanDriverAssistance(paramSettingForm);
                break;
            //沪标
            case ProtocolTypeUtil.SHANG_HAI_PROTOCOL_808_2019:
                driverAssistance = new HuDriverAssistance(paramSettingForm);
                break;
            //中位标准2019
            case ProtocolTypeUtil.ZW_PROTOCOL_808_2019:
                driverAssistance = new ZhongDriverAssistance(paramSettingForm);
                parameterId = 0xF0E1;
                break;
            //鲁标2019
            case ProtocolTypeUtil.LU_PROTOCOL_808_2019:
                driverAssistance = new LuDriverAssistance(paramSettingForm);
                break;
            //湘标2013
            case ProtocolTypeUtil.XIANG_PROTOCOL_808_2013:
                driverAssistance = new XiangDriverAssistance(paramSettingForm);
                break;
            //粤标2013
            case ProtocolTypeUtil.YUE_PROTOCOL_808_2019:
                driverAssistance = new YueDriverAssistance(paramSettingForm);
                break;
            default:
                log.info("前向参数设置未成功下发,protocolType:" + protocolType + ",未匹配到对应的协议类型.");
                break;
        }
        if (driverAssistance != null) {
            send(parameterId, driverAssistance, vehicleId, String.valueOf(protocolType), 64, userName);
        }

    }

    private void send65param(AdasParamSettingForm paramSettingForm, String vehicleId, String userName) {
        Object driverSurvey = null;
        Integer protocolType = paramSettingForm.getCommonParamSetting().getProtocolType();
        int parameterId = 0xF365;
        switch (String.valueOf(protocolType)) {
            //川标
            case ProtocolTypeUtil.SI_CHUAN_PROTOCOL_808_2013:
                driverSurvey = new ChuanDriverSurvey(paramSettingForm);
                break;
            //冀标
            case ProtocolTypeUtil.JI_PROTOCOL_808_2013:
                driverSurvey = new JiDriverSurvey(paramSettingForm);
                break;
            //桂标
            case ProtocolTypeUtil.GUANG_XI_PROTOCOL_808_2013:
                driverSurvey = new GuiDriverSurvey(paramSettingForm);
                break;
            //苏标
            case ProtocolTypeUtil.JIANG_SU_PROTOCOL_808_2013:
                driverSurvey = new SuDriverSurvey(paramSettingForm);
                break;
            //浙标
            case ProtocolTypeUtil.ZHE_JIANG_PROTOCOL_808_2013:
                driverSurvey = new ZheDriverSurvey(paramSettingForm);
                break;
            //吉标
            case ProtocolTypeUtil.JI_LIN_PROTOCOL_808_2013:
                driverSurvey = new JiLinDriverSurvey(paramSettingForm);
                break;
            //陕标
            case ProtocolTypeUtil.SHAN_XI_PROTOCOL_808_2013:
                driverSurvey = new ShanDriverSurvey(paramSettingForm);
                break;
            //赣标(江西)
            case ProtocolTypeUtil.JIANG_XI_PROTOCOL_808_2013:
                driverSurvey = new GanDriverSurvey(paramSettingForm);
                break;
            //沪标
            case ProtocolTypeUtil.SHANG_HAI_PROTOCOL_808_2019:
                driverSurvey = new HuDriverSurvey(paramSettingForm);
                break;
            //中位标准2019
            case ProtocolTypeUtil.ZW_PROTOCOL_808_2019:
                driverSurvey = new ZhongDriverSurvey(paramSettingForm);
                parameterId = 0XF0E2;
                break;
            //鲁标2019
            case ProtocolTypeUtil.LU_PROTOCOL_808_2019:
                driverSurvey = new LuDriverSurvey(paramSettingForm);
                break;
            //湘标2011
            case ProtocolTypeUtil.XIANG_PROTOCOL_808_2013:
                driverSurvey = new XiangDriverSurvey(paramSettingForm);
                break;
            //粤标2013
            case ProtocolTypeUtil.YUE_PROTOCOL_808_2019:
                driverSurvey = new YueDriverSurvey(paramSettingForm);
                break;
            default:
                log.info("驾驶员行为参数设置未成功下发,protocolType:" + protocolType + ",未匹配到对应的协议类型.");
                break;
        }
        if (driverSurvey != null) {
            //下发异常驾驶员行为
            send(parameterId, driverSurvey, vehicleId, String.valueOf(protocolType), 65, userName);
        }

    }

    private void send66param(AdasParamSettingForm paramSettingForm, String vehicleId, String userName) {
        Object tirePressure = null;
        Integer protocolType = paramSettingForm.getCommonParamSetting().getProtocolType();
        if (paramSettingForm.getAdasAlarmParamSettings().size() == 0) {
            return;
        }
        //获取胎压型号
        String id = paramSettingForm.getAdasAlarmParamSettings().get(0).getTyreNumber();
        String name = alarmParamDao.findTireModelById(id);
        int parameterId = 0XF366;
        switch (String.valueOf(protocolType)) {
            //川标
            case ProtocolTypeUtil.SI_CHUAN_PROTOCOL_808_2013:
                tirePressure = new ChuanTirePressure(paramSettingForm, name);
                break;
            //冀标
            case ProtocolTypeUtil.JI_PROTOCOL_808_2013:
                break;
            //桂标
            case ProtocolTypeUtil.GUANG_XI_PROTOCOL_808_2013:
                tirePressure = new GuiTirePressure(paramSettingForm, name);
                break;
            //苏标
            case ProtocolTypeUtil.JIANG_SU_PROTOCOL_808_2013:
                tirePressure = new SuTirePressure(paramSettingForm, name);
                break;
            //浙标
            case ProtocolTypeUtil.ZHE_JIANG_PROTOCOL_808_2013:
                break;
            //吉标
            case ProtocolTypeUtil.JI_LIN_PROTOCOL_808_2013:
                break;
            //陕标
            case ProtocolTypeUtil.SHAN_XI_PROTOCOL_808_2013:
                break;
            //赣标(江西)
            case ProtocolTypeUtil.JIANG_XI_PROTOCOL_808_2013:
                //tirePressure = new GanTirePressure(paramSettingForm, name);
                break;
            //沪标
            case ProtocolTypeUtil.SHANG_HAI_PROTOCOL_808_2019:
                break;
            //中位标准2019
            case ProtocolTypeUtil.ZW_PROTOCOL_808_2019:
                tirePressure = new ZhongTirePressure(paramSettingForm);
                parameterId = 0XF0E3;
                break;
            //鲁标2019
            case ProtocolTypeUtil.LU_PROTOCOL_808_2019:
                tirePressure = new LuTirePressure(paramSettingForm, name);
                break;
            //粤标2013
            case ProtocolTypeUtil.YUE_PROTOCOL_808_2019:
                tirePressure = new YueTirePressure(paramSettingForm, name);
                break;
            default:
                log.info("胎压监测设置未成功下发,protocolType:" + protocolType + ",未匹配到对应的协议类型.");
                break;
        }
        if (tirePressure != null) {
            //下发胎压参数设置
            send(parameterId, tirePressure, vehicleId, String.valueOf(protocolType), 66, userName);
        }

    }

    private void send67param(AdasParamSettingForm paramSettingForm, String vehicleId, String userName) {
        Object blindArea = null;
        Integer protocolType = paramSettingForm.getCommonParamSetting().getProtocolType();
        int parameterId = 0XF367;
        switch (String.valueOf(protocolType)) {
            //川标
            case ProtocolTypeUtil.SI_CHUAN_PROTOCOL_808_2013:
                blindArea = new ChuanBlindArea(paramSettingForm);
                break;
            //冀标
            case ProtocolTypeUtil.JI_PROTOCOL_808_2013:
                break;
            //桂标
            case ProtocolTypeUtil.GUANG_XI_PROTOCOL_808_2013:
                blindArea = new GuiBlindArea(paramSettingForm);
                break;
            //苏标
            case ProtocolTypeUtil.JIANG_SU_PROTOCOL_808_2013:
                blindArea = new SuBlindArea(paramSettingForm);
                break;
            //浙标
            case ProtocolTypeUtil.ZHE_JIANG_PROTOCOL_808_2013:
                parameterId = 0XF366;
                blindArea = new ZheBlindArea(paramSettingForm);
                break;
            //吉标
            case ProtocolTypeUtil.JI_LIN_PROTOCOL_808_2013:
                blindArea = new JiLinBlindArea(paramSettingForm);
                break;
            //陕标
            case ProtocolTypeUtil.SHAN_XI_PROTOCOL_808_2013:
                blindArea = new ShanBlindArea(paramSettingForm);
                break;
            //赣标(江西)
            case ProtocolTypeUtil.JIANG_XI_PROTOCOL_808_2013:
                blindArea = new GanBlindArea(paramSettingForm);
                break;
            //沪标
            case ProtocolTypeUtil.SHANG_HAI_PROTOCOL_808_2019:
                blindArea = new HuBlindArea(paramSettingForm);
                break;
            //中位标准2019
            case ProtocolTypeUtil.ZW_PROTOCOL_808_2019:
                blindArea = new ZhongBlindArea(paramSettingForm);
                parameterId = 0XF0E4;
                break;
            //鲁标
            case ProtocolTypeUtil.LU_PROTOCOL_808_2019:
                blindArea = new LuBlindArea(paramSettingForm);
                break;

            //粤标
            case ProtocolTypeUtil.YUE_PROTOCOL_808_2019:
                blindArea = new YueBlindArea(paramSettingForm);
                break;
            default:
                log.info("盲区监测设置未成功下发,protocolType:" + protocolType + ",未匹配到对应的协议类型.");
                break;
        }
        if (blindArea != null) {
            //下发盲区参数设置
            send(parameterId, blindArea, vehicleId, String.valueOf(protocolType), 67, userName);
        }

    }

    private void send70param(AdasParamSettingForm paramSettingForm, String vehicleId, String userName) {
        Object intenseDriving = null;
        Integer protocolType = paramSettingForm.getCommonParamSetting().getProtocolType();
        switch (String.valueOf(protocolType)) {
            //川标
            case ProtocolTypeUtil.SI_CHUAN_PROTOCOL_808_2013:
                intenseDriving = new ChuanIntenseDriving(paramSettingForm);
                break;
            //冀标
            case ProtocolTypeUtil.JI_PROTOCOL_808_2013:
                break;
            //桂标
            case ProtocolTypeUtil.GUANG_XI_PROTOCOL_808_2013:
                intenseDriving = new GuiIntenseDriving(paramSettingForm);
                break;
            //苏标
            case ProtocolTypeUtil.JIANG_SU_PROTOCOL_808_2013:
                break;
            //浙标
            case ProtocolTypeUtil.ZHE_JIANG_PROTOCOL_808_2013:
                break;
            //吉标
            case ProtocolTypeUtil.JI_LIN_PROTOCOL_808_2013:
                break;
            //陕标
            case ProtocolTypeUtil.SHAN_XI_PROTOCOL_808_2013:
                break;
            //赣标(江西)
            case ProtocolTypeUtil.JIANG_XI_PROTOCOL_808_2013:
                break;
            //沪标(上海)
            case ProtocolTypeUtil.SHANG_HAI_PROTOCOL_808_2019:
                break;
            default:
                log.info("激烈驾驶设置未成功下发,protocolType:" + protocolType + ",未匹配到对应的协议类型.");
                break;
        }
        if (intenseDriving != null) {
            //下发异常驾驶员行为
            send(0xF370, intenseDriving, vehicleId, String.valueOf(protocolType), 70, userName);
        }
    }

    private void send(Integer parameterId, Object paramVal, String vid, String protocolType, Integer protocol,
        String userName) {
        List<ParamItem> driveParams = getSendParams(paramVal, parameterId, protocolType);
        try {
            adasSendTxtService
                .sendF3SetParam(vid, protocol + "_ADAS", driveParams, "F3_ADAS_" + protocolType, false, userName);
        } catch (Exception e) {
            log.error("参数下发异常！", e);
        }
    }

    private List<ParamItem> getSendParams(Object paramVal, Integer parameterId, String protocolType) {
        List<ParamItem> params = new ArrayList<>();
        ParamItem paramItem = new ParamItem();
        paramItem.setParamValue(paramVal);
        paramItem.setParamId(parameterId);
        paramItem.setParamLength(AdasSettingLengthEnum.getParamIdLength(parameterId, protocolType));
        params.add(paramItem);
        return params;
    }

    /**
     * 条件查询参数设置列表
     */
    @Override
    public Page<AdasSettingListDo> selectParamByCondition(AdasRiskParamQuery adasRiskParamQuery) throws Exception {
        // redis 协议+组织+分组+监控对象
        Page<AdasSettingListDo> page = new Page<>();
        // 车是具有绑定关系的
        Map<String, Object> map = new HashMap<>(200);
        map.put("query", adasRiskParamQuery.getSimpleQueryParam());
        map.put("assignmentId", adasRiskParamQuery.getAssignmentId());
        map.put("groupId", adasRiskParamQuery.getGroupId());
        // 协议类型 12:川标 / 13:冀标  15:苏标 14:桂标 20:沪标 21:中位标准 24：京标
        Integer protocol = adasRiskParamQuery.getProtocol();
        List<String> vehicleList = redisVehicleService.getUserVehicles(map, null, protocol);
        if (vehicleList == null) {
            throw new RedisException(">=======redis 缓存出错了===========<");
        }
        if (vehicleList.size() == 0) {
            return page;
        }
        
        /*
         * 监控对象类型
         */
        Integer monitorType = 0;
        Set<String> moIds = newVehicleDao.findAllMidsBytype(monitorType);
        // 去重 (用户所有的对应协议的监控对象和信息配置中的车辆监控对象去重)
        vehicleList = new ArrayList<>(Sets.intersection(new LinkedHashSet<>(vehicleList), moIds));
        //获得当前所有在线的监控对象
        Set<String> allOnlineVids = getAllOnlineVids();
        // redis 状态信息 在线/离线
        Map<String, Integer> onlineStatusMap = new HashMap<>(200);
        for (String vid : vehicleList) {
            if (allOnlineVids.contains(vid)) {
                onlineStatusMap.put(vid, ON_LINE);
                continue;
            }
            onlineStatusMap.put(vid, OFF_LINE);
        }
        if (adasRiskParamQuery.getStatusInfo() == null) {
            adasRiskParamQuery.setStatusInfo(0);
        }
        // 查询在线的车辆ids
        if (adasRiskParamQuery.getStatusInfo() == 1) {
            Sets.SetView<String> intersection = Sets.intersection(allOnlineVids, new HashSet<>(vehicleList));
            vehicleList = new ArrayList<>(intersection);
        }
        // 查询离线的车辆ids
        if (adasRiskParamQuery.getStatusInfo() == 2) {
            vehicleList.removeAll(allOnlineVids);
        }
        // 查询所有所有的车辆ids
        // mysql 终端类别+下发状态+分页
        if (adasRiskParamQuery.getTerminalCategory() != null && adasRiskParamQuery.getSendStatus() != null) {
            // 筛选对应状态的车id
            String pname = "";
            List<String> pnames = new ArrayList<>();
            switch (adasRiskParamQuery.getTerminalCategory()) {
                case 0:
                    pnames.add(64 + "_ADAS");//前向
                    pnames.add(65 + "_ADAS");//驾驶员行为监测
                    pnames.add(70 + "_ADAS");//激烈驾驶
                    pnames.add(66 + "_ADAS");//胎压
                    pnames.add(67 + "_ADAS");//盲区
                    pnames.add(68 + "_ADAS");//不按规定上下客
                    pnames.add(51 + "_ADAS");//驾驶员行为监测报警
                    pnames.add(52 + "_ADAS");//车辆运行监测报警
                    pnames.add(38 + "_ADAS");//驾驶员身份识别参数(黑标)
                    pnames.add(39 + "_ADAS");//车辆运行监测(黑标)
                    pnames.add(40 + "_ADAS");//驾驶员驾驶行为(黑标)
                    pnames.add(41 + "_ADAS");//设备失效监测(黑标)
                    pnames.add(0XE9 + "_ADAS");//驾驶员对比(湘标和鲁标)
                    break;
                case 1:
                    pname = 64 + "_ADAS";
                    break;
                case 2:
                    pname = 65 + "_ADAS";
                    break;
                case 3:
                    pname = 70 + "_ADAS";
                    break;
                case 4:
                    pname = 67 + "_ADAS";
                    break;
                case 5:
                    pname = 66 + "_ADAS";
                    break;
                case 6:
                    pname = 68 + "_ADAS";
                    break;
                case 7:
                    pname = 51 + "_ADAS";
                    break;
                case 8:
                    pname = 52 + "_ADAS";
                    break;
                case 9:
                    pname = 38 + "_ADAS";
                    break;
                case 10:
                    pname = 39 + "_ADAS";
                    break;
                case 11:
                    pname = 40 + "_ADAS";
                    break;
                case 12:
                    pname = 41 + "_ADAS";
                    break;
                case 13:
                    pname = 0xE9 + "_ADAS";
                    break;
                default:
                    break;
            }
            String ptype = parameterType + protocol;
            List<String> strings;
            if (adasRiskParamQuery.getTerminalCategory() != 0) {
                strings = commonParamDao.selectDirect(pname, adasRiskParamQuery.getSendStatus(), ptype);
            } else {
                strings = commonParamDao.selectDirectByList(pnames, adasRiskParamQuery.getSendStatus(), ptype);
            }
            if (adasRiskParamQuery.getSendStatus() != -1) {
                Sets.SetView<String> intersection =
                    Sets.intersection(new HashSet<>(vehicleList), new HashSet<>(strings));
                vehicleList = new ArrayList<>(intersection);
            } else {
                HashSet<String> str1 = new HashSet<>(vehicleList);
                str1.removeAll(new HashSet<>(strings));
                vehicleList = new ArrayList<>(str1);
            }
        }
        if (vehicleList.size() == 0) {
            return page;
        }
        int listSize = vehicleList.size();
        int curPage = adasRiskParamQuery.getPage().intValue();// 当前页
        int pageSize = adasRiskParamQuery.getLimit().intValue(); // 每页条数
        int lst = (curPage - 1) * pageSize;// 遍历开始条数
        int ps = pageSize > (listSize - lst) ? listSize : (pageSize * curPage);// 遍历条数
        List<String> list = new ArrayList<>();
        for (int i = 0; i < vehicleList.size(); i++) {
            if (i >= lst && i < ps) {
                list.add(vehicleList.get(i));
            }
        }

        // 处理list
        Map<String, Object> paramMap = new HashMap<>(16);
        paramMap.put("protocol", adasRiskParamQuery.getProtocol());
        paramMap.put("terminalCategory", adasRiskParamQuery.getTerminalCategory());
        List<AdasSettingListDo> settingList = new ArrayList<>();
        if (list.size() > 0) {
            paramMap.put("list", list);
            if (ProtocolTypeUtil.JING_PROTOCOL_808_2019.equals(String.valueOf(protocol))) {
                //京标协议与其他协议差距较大 参数设置新增了一张表
                settingList = jingParamSettingDao.selectJingParamList(paramMap);
            } else {
                settingList = commonParamDao.selectParamList(paramMap);
            }

        }
        Map<String, BaseKvDo<String, Integer>> sendStatusMap =
            commonParamDao.selectDirectStatus(list, parameterType + protocol);
        for (AdasSettingListDo setting : settingList) {
            String vid = setting.getVehicleId();
            //设置车辆在线状态
            setting.setOnLineStatus(onlineStatusMap.get(vid));
            //设置下发状态
            setting.initSendStatus(sendStatusMap);

        }

        //按照车的绑定时间进行排序
        VehicleUtil.sort(settingList, list);
        page = RedisQueryUtil.getListToPage(settingList, adasRiskParamQuery, listSize);

        // 分组名称
        setAssignName(page);
        return page;
    }

    private Set<String> getAllOnlineVids() {
        Set<String> keys = new HashSet<>(RedisHelper.scanKeys(HistoryRedisKeyEnum.FUZZY_VEHICLE_STATUS.of()));
        Set<String> allOnlineVids = new HashSet<>();
        for (String vid : keys) {
            allOnlineVids.add(vid.substring(0, vid.indexOf("-vehiclestatus")));
        }
        return allOnlineVids;
    }

    /**
     * 根据车id查询已经进行参数设置信息
     * @param protocol 协议类型
     */
    @Override
    public List<Map<String, Object>> findReferVehicle(Integer protocol) {
        List<Map<String, Object>> list = new ArrayList<>();
        String userId = SystemHelper.getCurrentUser().getId().toString();
        String uuid = userService.getUserUuidByDn(userId);
        // 获取当前用户所属组织及下级组织
        List<String> orgList = organizationService.getOrgUuidsByUser(userId);
        if (StringUtils.isNotBlank(uuid) && !orgList.isEmpty()) {
            String tableName = "zw_m_adas_common_param_setting";
            if (ProtocolTypeUtil.JING_PROTOCOL_808_2019.equals(String.valueOf(protocol))) {
                //京标参数定义表
                tableName = "zw_m_adas_jing_alarm_param_setting";
            }
            list = commonParamDao.findReferVehicle(tableName, uuid, protocol, orgList);
        }
        return list;
    }

    /**
     * 获取当前监控对象的参数设置信息
     */
    @Override
    public List<AdasParamSettingForm> findParamByVehicleId(String vid) {
        List<AdasParamSettingForm> list = new ArrayList<>();
        List<AdasCommonParamSetting> commonParam = commonParamDao.selectByVehicleId(vid);
        for (AdasCommonParamSetting adasCommon : commonParam) {
            AdasParamSettingForm adasParam = new AdasParamSettingForm();

            adasParam.setCommonParamSetting(adasCommon);
            List<AdasAlarmParamSetting> adasAlarm = alarmParamDao.selectByVehicleId(vid, adasCommon.getParamType());
            //66代表胎压
            if (adasCommon.getParamType() == 66) {
                String id = adasAlarm.get(0).getTyreNumber();
                String name = alarmParamDao.findTireModelById(id);
                if (name != null) {
                    adasAlarm.get(0).setTyreNumberName(name);
                } else {
                    adasAlarm.get(0).setTyreNumberName("900R20");
                }

            }
            adasParam.setAdasAlarmParamSettings(adasAlarm);
            list.add(adasParam);
        }
        return list;
    }

    /**
     * 根据车辆id和参数进行对应修改
     */
    @Override
    public void updateParamByVehicleId(AdasParamRequestDTO requestDTO) throws BusinessException {
        requestDTO.checkVehIdIsEmpty();
        requestDTO.init();
        String vehicleId = requestDTO.getVehicleIds();
        List<AdasParamSettingForm> adasParamSettingForms = requestDTO.getAlarmParamSettingList();
        List<AdasPlatformParamSetting> platformParamSettings = requestDTO.getPlatformParamSettingList();
        boolean sendFlag = requestDTO.isSendFlag();
        String key = getRiskVehicleKey(vehicleId);
        List<String> lst = new ArrayList<>();
        if (!adasParamSettingForms.isEmpty()) {
            for (AdasParamSettingForm adasParamSettingForm : adasParamSettingForms) {
                AdasCommonParamSetting common = adasParamSettingForm.getCommonParamSetting();
                String flag = commonParamDao.findadasParam(common);
                // 处理定距单位换算
                //dealCommonUnit(common);
                List<AdasAlarmParamSetting> adasAlarmParamSettings = adasParamSettingForm.getAdasAlarmParamSettings();
                if (flag != null) {
                    Integer paramType = common.getParamType();
                    // 盲区,胎压,激烈驾驶无公共参数
                    if (paramType != 70 && paramType != 66 && paramType != 67 && paramType != 233) {
                        commonParamDao.updateCommonParamById(common, vehicleId);
                    }
                    for (AdasAlarmParamSetting adasAlarmParam : adasAlarmParamSettings) {
                        alarmParamDao.updateAlarmParamById(adasAlarmParam, vehicleId);
                    }
                } else {
                    List<AdasCommonParamSetting> list = new ArrayList<>();
                    list.add(common);
                    common.setId(UUID.randomUUID().toString());
                    common.setFlag(1);
                    commonParamDao.insertCommonParamBatch(list);
                    for (AdasAlarmParamSetting adasAlarmParamSetting : adasAlarmParamSettings) {
                        adasAlarmParamSetting.setId(UUID.randomUUID().toString());
                        adasAlarmParamSetting.setFlag(1);
                    }
                    alarmParamDao.insertAlarmParamBatch(adasAlarmParamSettings);
                }
            }
            Set<AdasParamSettingForm> redisSetSettingSet = new HashSet<>();
            for (AdasParamSettingForm form : adasParamSettingForms) {
                AdasParamSettingForm paramSetting = new AdasParamSettingForm();
                BeanUtils.copyProperties(form, paramSetting);
                redisSetSettingSet.add(paramSetting);
            }
            String beforeSetting = RedisHelper.getString(HistoryRedisKeyEnum.ADAS_PARAM_VEHICLE.of(vehicleId));
            if (StringUtil.isNotEmpty(beforeSetting)) {
                List<AdasParamSettingForm> beforeSettingList =
                    JSON.parseArray(beforeSetting, AdasParamSettingForm.class);
                redisSetSettingSet.addAll(beforeSettingList);
            }
            RedisHelper
                .setString(HistoryRedisKeyEnum.ADAS_PARAM_VEHICLE.of(vehicleId), JSON.toJSONString(redisSetSettingSet));
            Map<String, String> emap = new HashMap<>(8);
            emap.put("msg", "exist");
            RedisHelper.addToHash(HistoryRedisKeyEnum.ADAS_RISK_AUTO_VEHICLE.of(vehicleId), emap);
            // 是否参数下发
            if (sendFlag) {
                sendParam(vehicleId, adasParamSettingForms, SystemHelper.getCurrentUsername());
            }
        }
        List<AdasPlatformParamSetting> pushList = new ArrayList<>();
        if (!platformParamSettings.isEmpty()) {
            List<String> vehicleIds = new ArrayList<>();
            vehicleIds.add(vehicleId);
            //先删除平台参数设置,维护提醒内存
            deletePlatformParams(vehicleIds);
            //存储平台参数设置，维护提醒内存
            pushList = addPlatformParams(platformParamSettings, vehicleId);
        }
        // zmq通知
        String adasSetting = getAdasSetting(key, pushList, adasParamSettingForms.size() != 0, platformParamSettings);
        lst.add(adasSetting);
        // zmq 发送消息
        sendMessage(lst);
    }

    private void deletePlatformParams(List<String> vehicleIds) {
        alarmParamDao.deletePlatformParamByVehicleId(vehicleIds);
        for (String key : vehicleIds) {
            InitData.platformParamMap.remove(key);
            InitData.automaticVehicleMap.remove(key);
            RedisHelper.delByPattern(HistoryRedisKeyEnum.FUZZY_ADAS_PLATFORM_REMIND.of(key));
        }
    }

    private List<AdasPlatformParamSetting> addPlatformParams(List<AdasPlatformParamSetting> platformParamSettings,
        String vehicleId) {
        alarmParamDao.insertPlatformParams(platformParamSettings);
        Map<String, AdasPlatformParamSetting> settingMap = new HashMap<>();
        List<AdasPlatformParamSetting> pushList = new ArrayList<>();
        Set<String> automaticSet = new HashSet<>();
        for (AdasPlatformParamSetting setting : platformParamSettings) {

            if (IS_PUSH == setting.getAlarmSwitch()) {
                pushList.add(setting);
                settingMap.put(setting.getRiskFunctionId(), setting);
            }
            automaticDealAndGetSetBuild(automaticSet, setting);
        }
        if (!settingMap.isEmpty()) {
            InitData.platformParamMap.put(vehicleId, settingMap);
        }
        if (!automaticSet.isEmpty()) {
            InitData.automaticVehicleMap.put(vehicleId, automaticSet);
        }
        return pushList;
    }

    /**
     * 删除参数设置
     */
    @Override
    public void deleteRiskVehicleIds(List<String> vehicleIds, String ipAddress, String sign) throws Exception {
        if (CollectionUtils.isNotEmpty(vehicleIds)) {
            //获取当前协议类型
            Integer vieProtocol = getVieProtocol(vehicleIds.get(0));
            if (vieProtocol == -1) {
                throw new Exception("协议类型有误");
            }
            if (ProtocolTypeUtil.JING_PROTOCOL_808_2019.equals(String.valueOf(vieProtocol))) {
                jingParamSettingDao.deleteParamSettingByVehicleIds(vehicleIds);
            } else {
                commonParamDao.deleteCommonByBatch(vehicleIds);
                alarmParamDao.deleteCommonByBatch(vehicleIds);
            }
            deletePlatformParams(vehicleIds);
            // 删除下发表状态
            String protocol = parameterType + vieProtocol;
            commonParamDao.updateDirectiveByVidAndProtocol(protocol, vehicleIds);
            List<String> vids = new ArrayList<>();
            List<RedisKey> deleteKeys = new ArrayList<>();
            for (String vid : vehicleIds) {
                RedisKey autoKey = HistoryRedisKeyEnum.ADAS_RISK_AUTO_VEHICLE.of(vid);
                deleteKeys.add(autoKey);
                deleteKeys.add(HistoryRedisKeyEnum.ADAS_PARAM_VEHICLE.of(vid));
                String key = getDeleteRiskVehicleKey(autoKey.get());
                vids.add(key);
            }
            RedisHelper.delete(deleteKeys);
            addSettingLog(vehicleIds, ipAddress, sign, "恢复参数设置");
            System.err.println(vids);
            sendMessage(vids);
        }
    }

    private String getDeleteRiskVehicleKey(String key) {
        Map<String, String> emap = new HashMap<>(8);
        emap.put("msg", "exist");
        return "#del#" + key + "#" + JSON.toJSONString(emap);
    }

    /**
     * 参数
     * @param vehIds    车辆id的集合
     * @param ipAddress ip地址
     */
    @Override
    public void sendParamSet(List<String> vehIds, String ipAddress,
        LinkedBlockingQueue<Map<String, String>> paramStatusQueue, Integer protocol) {
        String type = "";
        Map<String, List<AdasParamSettingForm>> listHashMap = new HashMap<>();
        if (vehIds.size() == 0) {
            return;
        }
        String defaultParams = RedisHelper.getString(HistoryRedisKeyEnum.ADAS_PARAM_DEFAULT_PROTOCOL.of(protocol));
        Map<String, RedisKey> redisKeyMap = new HashMap<>();
        for (String vid : vehIds) {
            redisKeyMap.put(vid, HistoryRedisKeyEnum.ADAS_PARAM_VEHICLE.of(vid));
        }

        Map<String, String> redisKeyStringMap = RedisHelper.batchGetStringMap(redisKeyMap);

        for (Map.Entry<String, String> entry : redisKeyStringMap.entrySet()) {
            JSONArray array;
            if (entry.getValue() == null) {
                array = JSON.parseArray(defaultParams);
            } else {
                array = JSON.parseArray(entry.getValue());
            }
            List<AdasParamSettingForm> adasParamSettingForms = new ArrayList<>();
            for (Object o : array) {
                AdasParamSettingForm adasParamSettingForm =
                    JSON.parseObject(JSON.toJSONString(o), AdasParamSettingForm.class);
                type = "F3_ADAS_" + adasParamSettingForm.getCommonParamSetting().getProtocolType();
                adasParamSettingForms.add(adasParamSettingForm);
            }
            listHashMap.put(entry.getKey(), adasParamSettingForms);
        }
        updateDirStatus(listHashMap, type, paramStatusQueue);
        addSettingLog(vehIds, ipAddress, "1", "主动安全参数下发");
    }

    /**
     * 生成redis 需要的key
     */
    private String getRiskVehicleKey(String vehicleId) {
        return "RISK_" + vehicleId + "_" + "AUTO";
    }

    /**
     * 生成zmq需要的数据
     */
    private String getAdasSetting(String key, List<AdasPlatformParamSetting> pushList, boolean hasSetting,
        List<AdasPlatformParamSetting> platformParamSettings) {
        Map<String, Object> emap = new HashMap<>(8);
        if (hasSetting) {
            emap.put("msg", "exist");
        }
        if (!pushList.isEmpty()) {
            emap.put("platform", JSON.toJSONString(pushList));
        }
        if (!platformParamSettings.isEmpty() && pushList.isEmpty()) {
            emap.put("platform", "clean");
        }
        return "#set#" + key + "#" + JSON.toJSONString(emap);
    }

    /**
     * Zmq 消息发送
     */
    private void sendMessage(List<String> lst) {
        if (lst.size() > 0) {
            ZMQFencePub.pubAdasRiskParam(JSON.toJSONString(lst));
        }
    }

    /**
     * 设置分组名称
     */
    public void setAssignName(Page<AdasSettingListDo> page) {

        Set<String> vehicleIds = Sets.newHashSet();
        for (AdasSettingListDo adasSettingListDo : page) {
            vehicleIds.add(adasSettingListDo.getVehicleId());
        }
        List<GroupMonitorDTO> list = groupMonitorService.getByMonitorIds(vehicleIds);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        Map<String, List<String>> vehicleIdGroupNameMap = new HashMap<>(CommonUtil.ofMapCapacity(list.size()));
        for (GroupMonitorDTO monitorDTO : list) {
            vehicleIdGroupNameMap.computeIfAbsent(monitorDTO.getMonitorId(), o -> new ArrayList<>())
                .add(monitorDTO.getGroupName());
        }
        for (AdasSettingListDo data : page) {
            // 获取车辆的分组名称
            List<String> groupNames = vehicleIdGroupNameMap.get(data.getVehicleId());
            if (groupNames == null) {
                continue;
            }
            data.setGroupName(StringUtils.join(groupNames, ","));
        }

    }

    private void addSettingLog(List<String> vehicleIds, String ipAddress, String sign, String message) {
        // 1表示需要记录日志
        if ("1".equals(sign)) {
            if (vehicleIds.size() == 1) {
                String[] vehicle = logSearchServiceImpl.findCarMsg(vehicleIds.get(0));
                logSearchServiceImpl.addLog(ipAddress, message, "3", "", vehicle[0], vehicle[1]);
            } else {
                logSearchServiceImpl.addLog(ipAddress, message, "3", "batch", "批量参数设置");
            }
        }
    }

    /**
     * 获取车辆所属的协议
     */
    private Integer getVieProtocol(String vid) {
        Integer protocol = commonParamDao.selectProtocolByVid(vid);
        if (protocol == null) {
            protocol = jingParamSettingDao.selectProtocolByVid(vid);
        }
        return protocol != null ? protocol : -1;
    }

    /**
     * 川冀标处理8900 （读取外设基本信息和外设状态）
     */
    @Override
    public JsonResultBean sendF3PInfo(String vehicleId, String sensorID, String commandType, String ipAddress) {
        List<Integer> sensorList = new ArrayList<>();
        sensorList.add(Integer.parseInt(sensorID, 16));
        String parameterName = commandType + "_" + sensorID + "_ADAS";
        String paramType = "0x8900_ADAS_" + getVieProtocol(vehicleId);
        // 获取最后一次下发的编号
        String paramId = sendHelper.getLastSendParamID(vehicleId, parameterName, paramType);
        // 获取车辆及设备信息
        BindDTO bindDTO = monitorHelper.getBindDTO(vehicleId, MonitorTypeEnum.VEHICLE);
        String deviceId = bindDTO.getDeviceId();
        String simCardNumber = bindDTO.getSimCardNumber();
        String deviceNumber = bindDTO.getDeviceNumber();
        // 序列号
        Integer msgSN = DeviceHelper.getRegisterDevice(vehicleId, deviceNumber);
        //在线且设备已注册
        if (msgSN != null) {
            // 下发参数
            sendHelper.updateParameterStatus(paramId, msgSN, 4, vehicleId, paramType, parameterName);
            //组装下发
            T808_0x8900<Integer> t8080x8900 = new T808_0x8900<>();
            t8080x8900.setType(Integer.parseInt(commandType, 16));
            t8080x8900.setSum(1);
            t8080x8900.setSensorDatas(sensorList);
            // 先订阅再下发
            T808Message message = MsgUtil
                .get808Message(simCardNumber, ConstantUtil.T808_PENETRATE_DOWN, msgSN, t8080x8900,
                    bindDTO.getDeviceType());
            WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_PENETRATE_DOWN, deviceId);
        } else {
            msgSN = 0;// 绑定下发
            sendHelper.updateParameterStatus(paramId, msgSN, 5, vehicleId, paramType, parameterName);
        }
        if ("0".equals(String.valueOf(msgSN))) { // 如果终端流水号为0,则终端离线
            return new JsonResultBean(JsonResultBean.FAULT, "终端离线");
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 处理川冀标8103线程
     */
    @Override
    public void processingThreads(LinkedBlockingQueue<Map<String, String>> paramStatusQueue, Integer protocol) {
        taskExecutor.execute(() -> processing(paramStatusQueue, protocol));
    }

    private void processing(LinkedBlockingQueue<Map<String, String>> paramStatusQueue, Integer protocol) {
        Map<String, String> vehicleMap;
        while (!Thread.currentThread().isInterrupted()) {
            try {
                vehicleMap = paramStatusQueue.remove();
                String vehicleId = vehicleMap.entrySet().iterator().next().getKey();
                String userName = vehicleMap.get(vehicleId);
                AdasDirectiveStatusOutTimeUtil.directiveStatusOutTimeCache.getIfPresent(userName);
                String value = RedisHelper.getString(HistoryRedisKeyEnum.ADAS_PARAM_VEHICLE.of(vehicleId));
                if (value == null) {
                    value = RedisHelper.getString(HistoryRedisKeyEnum.ADAS_PARAM_DEFAULT_PROTOCOL.of(protocol));
                }
                JSONArray array = JSON.parseArray(value);
                List<AdasParamSettingForm> adasParamSettingForms = new ArrayList<>();
                for (Object o : array) {
                    AdasParamSettingForm adasParamSettingForm =
                        JSON.parseObject(JSON.toJSONString(o), AdasParamSettingForm.class);
                    adasParamSettingForms.add(adasParamSettingForm);
                }
                sendParam(vehicleId, adasParamSettingForms, userName);
            } catch (NoSuchElementException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("异步处理川冀标下发异常", e);
            }
        }

    }

    public void offerParamStatus(Map<String, String> vehicleMap,
        LinkedBlockingQueue<Map<String, String>> paramStatusQueue) {
        if (offerMessage(vehicleMap, paramStatusQueue)) {
            return;
        }
        log.error("位置消息队列已满");
    }

    private <T> boolean offerMessage(T object, LinkedBlockingQueue<T> queue) {
        try {
            boolean res = queue.offer(object, 10, TimeUnit.MILLISECONDS);
            if (!res) {
                // 如果队列已满，则移除头元素，更新尾元素
                queue.poll();
                queue.put(object);
            }
            return res;
        } catch (InterruptedException e) {
            log.info("线程中断");
        }
        return false;
    }

    @Override
    public Map<String, Integer> getStatus(String vehicleId, Integer protocolType, String paramTypes) {
        Map<String, Integer> map = new HashMap<>();
        String[] paramType = paramTypes.split(",");
        for (String s : paramType) {
            String parameterType = "F3_ADAS_" + protocolType;
            String parameterName = s + "_ADAS";
            Integer status = parameterDao.getSendAdasStatus(vehicleId, parameterType, parameterName);
            map.put(s, status);
        }
        return map;
    }

    @Override
    public List<AdasPlatformParamSetting> findPlatformParamByVehicleId(String vehicleId) {
        return alarmParamDao.findPlatformSetting(vehicleId);
    }

    @Override
    public List<Map<String, String>> findAllTireModel() {
        return alarmParamDao.findAllTireModel();
    }

    @Override
    public void updateDirectiveStatus(Set<String> directiveIdSet) {
        alarmParamDao.updateDirectiveStatusByIdSet(directiveIdSet);
    }

    @Override
    public Set<String> findLogicChannelsByVehicleId(List<String> vehicleIds) {
        Map<String, Set<String>> vehicleIdChannelMap = new HashMap<>();
        List<Map<String, String>> mapList = alarmParamDao.findLogicChannelsByVehicleIds(vehicleIds);
        for (Map<String, String> map : mapList) {
            Set<String> channels =
                Optional.ofNullable(vehicleIdChannelMap.get(map.get("vehicleId"))).orElse(new HashSet<>());
            channels.add(map.get("channel"));
            vehicleIdChannelMap.put(map.get("vehicleId"), channels);
        }
        Set<String> result = vehicleIdChannelMap.get(vehicleIds.get(0));
        for (Map.Entry<String, Set<String>> entry : vehicleIdChannelMap.entrySet()) {
            if (result == null || result.isEmpty()) {
                break;
            }
            result = Sets.intersection(entry.getValue(), result);
        }
        return result != null ? result : new HashSet<>();
    }

    @Override
    public void maintenanceRemoteUpgradeCache(String vehicleId) {
        Set<String> userNameSet = adasSubcibeTable.getRemoteUpgradeCache().getIfPresent(vehicleId);
        userNameSet = userNameSet != null ? userNameSet : new HashSet<>();
        userNameSet.add(SystemHelper.getCurrentUsername());
        adasSubcibeTable.getRemoteUpgradeCache().put(vehicleId, userNameSet);
    }

    //***********************京标分割线**********************************************
    @Override
    public List<AdasJingParamSetting> findJingParamByVehicleId(String vehicleId) {
        return jingParamSettingDao.findJingParamByVehicleId(vehicleId);
    }

    @SuppressWarnings("checkstyle:WhitespaceAround")
    @Override
    public Map<String, Map<Integer, List<ParamItem>>> insertJingParamSetting(List<String> vehicleIds,
        List<AdasJingParamSetting> adasJingParamSettingList, List<AdasPlatformParamSetting> platformParamSettings,
        boolean sendFlag, String ipAddress) {
        //1.组装要存储的参数设置
        Set<Integer> paramTypeSet = new HashSet<>(); //需要删除的页签
        List<AdasJingParamSetting> insertParams = new ArrayList<>();//主动安全参数设置
        List<AdasPlatformParamSetting> platformParamList = new ArrayList<>();//平台参数设置
        List<String> lst = new ArrayList<>();//zmq推送信息
        Map<String, Map<Integer, List<ParamItem>>> sendParamMap = new HashMap<>();//组装下发8103
        Map<String, Set<String>> automaticMap = new HashMap<>(); //自动处理 自动获取map
        if (platformParamSettings != null && platformParamSettings.size() > 0) {
            deletePlatformParams(vehicleIds);
        }
        for (String vehicleId : vehicleIds) {
            for (AdasJingParamSetting jingParamSetting : adasJingParamSettingList) {
                AdasJingParamSetting insertParam = new AdasJingParamSetting();
                jingParamSetting.setVehicleId(vehicleId);
                jingParamSetting.setId(UUID.randomUUID().toString());
                BeanUtils.copyProperties(jingParamSetting, insertParam);
                insertParams.add(insertParam);
                paramTypeSet.add(jingParamSetting.getParamType());
                if (sendFlag) {
                    Map<Integer, List<ParamItem>> paramItemMap = new HashMap<>();
                    if (sendParamMap.get(vehicleId) != null) {
                        paramItemMap = sendParamMap.get(vehicleId);
                    }
                    List<ParamItem> paramItemList = new ArrayList<>();
                    if (paramItemMap.get(jingParamSetting.getParamType()) != null) {
                        paramItemList = paramItemMap.get(jingParamSetting.getParamType());
                    }
                    JingAdasSendInfo paramVal = buildJingSendInfo(jingParamSetting);
                    ParamItem paramItem = new ParamItem();
                    paramItem.setParamValue(paramVal);
                    paramItem.setParamId(Integer.parseInt(jingParamSetting.getParameterId().replaceAll("0x", ""), 16));
                    paramItem.setParamLength(9);
                    paramItemList.add(paramItem);
                    paramItemMap.put(jingParamSetting.getParamType(), paramItemList);
                    sendParamMap.put(vehicleId, paramItemMap);
                }
            }
            //平台参数设置
            List<AdasPlatformParamSetting> pushList = new ArrayList<>();
            //自动处理 自动获取
            Set<String> automaticSet = new HashSet<>();
            Map<String, AdasPlatformParamSetting> settingMap = new HashMap<>();
            for (AdasPlatformParamSetting platformParamSetting : platformParamSettings) {
                platformParamSetting.setVehicleId(vehicleId);
                AdasPlatformParamSetting platform = new AdasPlatformParamSetting();
                BeanUtils.copyProperties(platformParamSetting, platform);
                platformParamList.add(platform);
                if (IS_PUSH == platform.getAlarmSwitch()) {
                    pushList.add(platform);
                    settingMap.put(platform.getRiskFunctionId(), platform);
                }
                automaticDealAndGetSetBuild(automaticSet, platformParamSetting);
            }
            if (automaticSet.size() != 0) {
                automaticMap.put(vehicleId, automaticSet);
            }
            if (!settingMap.isEmpty()) {
                InitData.platformParamMap.put(vehicleId, settingMap);
            }
            String key = getRiskVehicleKey(vehicleId);
            Set<AdasJingParamSetting> adasJingParamSettingSet = new HashSet<>(adasJingParamSettingList);
            if (!adasJingParamSettingSet.isEmpty()) {
                String beforeSetting = RedisHelper.getString(HistoryRedisKeyEnum.ADAS_PARAM_VEHICLE.of(vehicleId));
                if (StringUtil.isNotEmpty(beforeSetting)) {
                    List<AdasJingParamSetting> beforeSettingList =
                        JSON.parseArray(beforeSetting, AdasJingParamSetting.class);
                    adasJingParamSettingSet.addAll(beforeSettingList);
                }
                // redis 缓存
                RedisHelper.setString(HistoryRedisKeyEnum.ADAS_PARAM_VEHICLE.of(vehicleId),
                    JSON.toJSONString(adasJingParamSettingSet));
                // 新增redis
                Map<String, String> emap = new HashMap<>(8);
                emap.put("msg", "exist");
                RedisHelper.addToHash(HistoryRedisKeyEnum.ADAS_RISK_AUTO_VEHICLE.of(vehicleId), emap);
            }
            // zmq通知
            String adasSetting =
                getAdasSetting(key, pushList, adasJingParamSettingList.size() != 0, platformParamSettings);
            lst.add(adasSetting);
        }
        // zmq 发送消息
        sendMessage(lst);
        //2.删除原有的参数设置
        if (!paramTypeSet.isEmpty()) {
            jingParamSettingDao.deleteParamSetting(vehicleIds, paramTypeSet);
        }
        //3.异步存储参数设置和平台参数设置(根据参数设置的多少来存储,1000条单线程批量插入，1000条多线程批量插入)
        final int bestBatchSize = 1000;
        final int totalInsertSize = insertParams.size();
        if (!insertParams.isEmpty()) {
            if (totalInsertSize <= bestBatchSize) {
                jingParamSettingDao.addJingAlarmSettingByBatch(insertParams);
            } else {
                ParallelWorker.invoke(insertParams, bestBatchSize,
                    settings -> jingParamSettingDao.addJingAlarmSettingByBatch(settings));
            }
        }
        if (!platformParamList.isEmpty()) {
            if (automaticMap.size() != 0) {
                InitData.automaticVehicleMap.putAll(automaticMap);//自动处理 自动获取
            }
            final int platformInsertSize = platformParamList.size();
            if (platformInsertSize <= bestBatchSize) {
                alarmParamDao.insertPlatformParams(platformParamList);
            } else {
                ParallelWorker
                    .invoke(platformParamList, bestBatchSize, settings -> alarmParamDao.insertPlatformParams(settings));
            }
        }
        if (sendFlag && !sendParamMap.isEmpty()) {
            Set<String> directiveParamIds = updateDirectiveStatusOfJing(vehicleIds, sendParamMap);
            maintainOutTimeCache(directiveParamIds);
        }
        addSettingLog(vehicleIds, ipAddress, "1", "新增参数设置");
        return sendParamMap;
    }

    private void automaticDealAndGetSetBuild(Set<String> automaticSet, AdasPlatformParamSetting platformParamSetting) {
        if (platformParamSetting.getAutomaticDealOne() != null && platformParamSetting.getAutomaticDealOne() == 1) {
            automaticSet.add(platformParamSetting.getRiskFunctionId() + "_deal_1");
        }
        if (platformParamSetting.getAutomaticDealTwo() != null && platformParamSetting.getAutomaticDealTwo() == 1) {
            automaticSet.add(platformParamSetting.getRiskFunctionId() + "_deal_2");
        }
        if (platformParamSetting.getAutomaticDealThree() != null && platformParamSetting.getAutomaticDealThree() == 1) {
            automaticSet.add(platformParamSetting.getRiskFunctionId() + "_deal_3");
        }
        if (platformParamSetting.getAutomaticGetOne() != null && platformParamSetting.getAutomaticGetOne() == 0) {
            automaticSet.add(platformParamSetting.getRiskFunctionId() + "_get_1");
        }
        if (platformParamSetting.getAutomaticGetTwo() != null && platformParamSetting.getAutomaticGetTwo() == 0) {
            automaticSet.add(platformParamSetting.getRiskFunctionId() + "_get_2");
        }
        if (platformParamSetting.getAutomaticGetThree() != null && platformParamSetting.getAutomaticGetThree() == 0) {
            automaticSet.add(platformParamSetting.getRiskFunctionId() + "_get_3");
        }
    }

    private JingAdasSendInfo buildJingSendInfo(AdasJingParamSetting paramSetting) {
        JingAdasSendInfo sendInfo = new JingAdasSendInfo();
        sendInfo.setLevel(Integer.parseInt(paramSetting.getAlarmLevel()));
        sendInfo.setAlarmVolume(Integer.parseInt(paramSetting.getAlarmVolume()));
        sendInfo.setVoiceBroadcast(paramSetting.getSpeech());
        sendInfo.setVideoTime(paramSetting.getAlarmVideoDuration());
        sendInfo.setVideoResolution(Integer.parseInt(paramSetting.getVideoResolution().replaceAll("0x", ""), 16));
        sendInfo.setCameraNum(paramSetting.getPhotographNumber());
        sendInfo.setCameraResolution(Integer.parseInt(paramSetting.getCameraResolution().replaceAll("0x", ""), 16));
        sendInfo.setCameraTime(paramSetting.getPhotographTime());
        sendInfo.setSpeedThreshold(paramSetting.getSpeedLimit());
        sendInfo.setDurationThreshold(paramSetting.getDurationThreshold());
        return sendInfo;
    }

    @Override
    public void sendJingParamSet(List<String> vehIds, String ipAddress) {
        //组装下发的参数
        Map<String, Map<Integer, List<ParamItem>>> sendParamMap = new HashMap<>();//组装下发8103
        if (vehIds.size() == 0) {
            return;
        }
        //默认参数设置  预防没有设置参数的车辆
        String defaultParams = RedisHelper
            .getString(HistoryRedisKeyEnum.ADAS_PARAM_DEFAULT_PROTOCOL.of(ProtocolTypeUtil.JING_PROTOCOL_808_2019));
        Map<String, RedisKey> redisKeyMap = new HashMap<>();
        for (String vid : vehIds) {
            redisKeyMap.put(vid, HistoryRedisKeyEnum.ADAS_PARAM_VEHICLE.of(vid));
        }
        Map<String, String> batchGetStringMap = RedisHelper.batchGetStringMap(redisKeyMap);
        for (Map.Entry<String, String> entry : batchGetStringMap.entrySet()) {
            JSONArray array =
                entry.getValue() == null ? JSON.parseArray(defaultParams) : JSON.parseArray(entry.getValue());
            for (Object o : array) {
                AdasJingParamSetting adasParamSettingForm =
                    JSON.parseObject(JSON.toJSONString(o), AdasJingParamSetting.class);
                Map<Integer, List<ParamItem>> paramItemMap = new HashMap<>();
                if (sendParamMap.get(entry.getKey()) != null) {
                    paramItemMap = sendParamMap.get(entry.getKey());
                }
                List<ParamItem> paramItemList = new ArrayList<>();
                if (paramItemMap.get(adasParamSettingForm.getParamType()) != null) {
                    paramItemList = paramItemMap.get(adasParamSettingForm.getParamType());
                }
                JingAdasSendInfo paramVal = buildJingSendInfo(adasParamSettingForm);
                ParamItem paramItem = new ParamItem();
                paramItem.setParamValue(paramVal);
                paramItem.setParamId(Integer.parseInt(adasParamSettingForm.getParameterId().replaceAll("0x", ""), 16));
                paramItem.setParamLength(9);
                paramItemList.add(paramItem);
                paramItemMap.put(adasParamSettingForm.getParamType(), paramItemList);
                sendParamMap.put(entry.getKey(), paramItemMap);
            }
        }
        //下发8103
        Set<String> directiveParamIds = updateDirectiveStatusOfJing(vehIds, sendParamMap);
        maintainOutTimeCache(directiveParamIds);
        sendParamsOfJing(vehIds, sendParamMap, SystemHelper.getCurrentUsername());
        addSettingLog(vehIds, ipAddress, "1", "主动安全参数下发");
    }

    private void sendParamsOfJing(List<String> vehIds, Map<String, Map<Integer, List<ParamItem>>> sendParamMap,
        String currentUsername) {
        for (String vehicleId : vehIds) {
            Map<Integer, List<ParamItem>> sendF3Map = sendParamMap.get(vehicleId);
            for (Map.Entry<Integer, List<ParamItem>> entry : sendF3Map.entrySet()) {
                try {
                    adasSendTxtService
                        .sendF3SetParam(vehicleId, entry.getKey() + "_ADAS", entry.getValue(), "F3_ADAS_24", false,
                            currentUsername);
                } catch (Exception e) {
                    log.error("参数下发异常！", e);
                }
            }
        }
    }

    @Override
    public JsonResultBean sendPInfo(String vid, Integer type) {
        BindDTO bindDTO = monitorHelper.getBindDTO(vid, MonitorTypeEnum.VEHICLE);
        String deviceNumber = bindDTO.getDeviceNumber();
        Integer msgSN = DeviceHelper.getRegisterDevice(vid, deviceNumber);
        String deviceId = bindDTO.getDeviceId();
        String simCardNumber = bindDTO.getSimCardNumber();
        String paramType = "F3-8106-" + type;
        String paramId = this.getLastSendParamID(vid, vid + paramType, paramType);
        if (msgSN != null) {
            // 订阅回应的user
            String username = SystemHelper.getCurrentUsername();
            UserCache.put(String.valueOf(msgSN), username);
            // 下发参数
            sendHelper.updateParameterStatus(paramId, msgSN, 4, vid, paramType, vid + paramType);
            SubscibeInfo info =
                new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, msgSN, ConstantUtil.T808_PARAM_ACK);
            SubscibeInfoCache.getInstance().putTable(info);
            sendJingParam(simCardNumber, msgSN, deviceId, bindDTO, type);
        } else { // 设备未注册
            msgSN = 0;
            sendHelper.updateParameterStatus(paramId, msgSN, 5, vid, paramType, vid + paramType);
        }
        if ("0".equals(String.valueOf(msgSN))) { // 如果终端流水号为0,则终端离线
            return new JsonResultBean(JsonResultBean.FAULT, "终端离线");
        }
        String username = SystemHelper.getCurrentUsername();
        JSONObject json = new JSONObject();
        json.put("msgId", String.valueOf(msgSN));
        json.put("userName", username);

        return new JsonResultBean(json);
    }

    private String getLastSendParamID(String vehicleId, String paramid, String type) {
        List<Directive> paramlist = parameterDao.findParameterByType(vehicleId, paramid, type); // 6:报警
        Directive param;
        if (paramlist != null && !paramlist.isEmpty()) {
            param = paramlist.get(0);
            return param.getId();
        }
        return "";
    }

    public void sendJingParam(String mobile, Integer msgSN, String deviceId, BindDTO bindDTO, Integer type) {
        // 51 代表京标驾驶行为报警参数设置查询 52 代表京标车辆运行监测报警参数设置查询  53 代表京标终端参数查询
        Integer[] params = jingParamMap.get(type);
        if (params != null && params.length > 0) {
            List<Integer> paramIDs = Arrays.asList(params);
            T808Msg8106 t8080x8106 = new T808Msg8106();
            t8080x8106.setParamSum(params.length);
            t8080x8106.setParamIds(paramIDs);
            T808Message message = MsgUtil
                .get808Message(mobile, ConstantUtil.T808_QUERY_PARAMS, msgSN, t8080x8106, bindDTO.getDeviceType());
            WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_QUERY_PARAMS, deviceId);
            return;
        }
        log.info("京标协议查询的" + type + "类型没有对应的参数id集合");
    }

    @Override
    public void sendJing8103(List<String> vehicleIds, Map<String, Map<Integer, List<ParamItem>>> sendParamMap) {
        if (sendParamMap.size() != 0) {
            String userName = SystemHelper.getCurrentUsername();
            taskExecutor.execute(() -> sendParamsOfJing(vehicleIds, sendParamMap, userName));
        }
    }

    private Set<String> updateDirectiveStatusOfJing(List<String> vehicleIds,
        Map<String, Map<Integer, List<ParamItem>>> sendParamMap) {
        List<DirectiveForm> directiveForms = new ArrayList<>();
        Set<String> directiveParamIds = new HashSet<>();
        String parameterType = "F3_ADAS_" + ProtocolTypeUtil.JING_PROTOCOL_808_2019;
        Map<String, Map<String, String>> map = getDirectiveMap(vehicleIds, parameterType);
        Set<String> allOnLine = getAllOnlineVids();
        for (String vehicleId : vehicleIds) {
            for (Map.Entry<Integer, List<ParamItem>> entry : sendParamMap.get(vehicleId).entrySet()) {
                String parameterName = entry.getKey() + "_ADAS";
                int msgnSn = allOnLine.contains(vehicleId) ? -1 : 0;
                Integer status = msgnSn == 0 ? 5 : 4;
                DirectiveForm directiveForm =
                    sendHelper.generateDirective(vehicleId, status, parameterType, msgnSn, parameterName, 1, null);
                if (map.get(vehicleId) != null && map.get(vehicleId).get(parameterName) != null) {
                    directiveForm.setId(map.get(vehicleId).get(parameterName));
                }
                directiveParamIds.add(directiveForm.getId());
                directiveForms.add(directiveForm);
            }
        }
        alarmParamDao.updateDirectiveStatus(directiveForms);
        return directiveParamIds;
    }

    private void maintainOutTimeCache(Set<String> directiveParamIds) {
        Set<String> ids =
            AdasDirectiveStatusOutTimeUtil.directiveStatusOutTimeCache.getIfPresent(SystemHelper.getCurrentUsername());
        if (ids != null) {
            ids.addAll(directiveParamIds);
        } else {
            ids = directiveParamIds;
        }
        AdasDirectiveStatusOutTimeUtil.directiveStatusOutTimeCache.put(SystemHelper.getCurrentUsername(), ids);
    }

}

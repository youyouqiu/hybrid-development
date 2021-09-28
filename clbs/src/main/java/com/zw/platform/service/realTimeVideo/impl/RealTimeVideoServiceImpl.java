package com.zw.platform.service.realTimeVideo.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cb.platform.domain.VehicleScheduler;
import com.cb.platform.repository.mysqlDao.VehicleScheduleDao;
import com.zw.app.domain.monitor.SwitchInfo;
import com.zw.lkyw.domain.SendMsgBasicInfo;
import com.zw.lkyw.domain.SendMsgDetail;
import com.zw.lkyw.domain.SendMsgMonitorInfo;
import com.zw.lkyw.utils.sendMsgCache.SendMsgCache;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.constant.Vehicle;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.TerminalTypeInfo;
import com.zw.platform.domain.core.SendParam;
import com.zw.platform.domain.sendTxt.SendTextParam;
import com.zw.platform.domain.sendTxt.SendTxt;
import com.zw.platform.domain.systems.form.DirectiveForm;
import com.zw.platform.dto.video.DeviceVideoParamDto;
import com.zw.platform.push.handler.device.DeviceHelper;
import com.zw.platform.repository.modules.ParameterDao;
import com.zw.platform.repository.modules.TerminalTypeDao;
import com.zw.platform.repository.vas.IoVehicleConfigDao;
import com.zw.platform.service.core.F3SendStatusProcessService;
import com.zw.platform.service.realTimeVideo.RealTimeVideoService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.service.sendTxt.SendTxtService;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.ConvertUtil;
import com.zw.platform.util.SendHelper;
import com.zw.platform.util.StrUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MapUtil;
import com.zw.platform.util.common.MonitorUtils;
import com.zw.protocol.msg.Message;
import com.zw.protocol.msg.t808.T808Message;
import com.zw.protocol.msg.t808.body.LocationInfo;
import com.zw.protocol.util.ProtocolTypeUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Chen Feng
 * @version 1.0 2018/12/12
 */
@Log4j2
@Service
public class RealTimeVideoServiceImpl implements RealTimeVideoService {
    @Autowired
    private SendHelper sendHelper;

    @Autowired
    private ParameterDao parameterDao;

    @Autowired
    VehicleScheduleDao vehicleScheduleDao;

    @Autowired
    private SendTxtService sendTxtService;

    @Autowired
    private F3SendStatusProcessService f3SendStatusProcessService;

    @Autowired
    private LogSearchService logSearchService;

    @Autowired
    private IoVehicleConfigDao ioVehicleConfigDao;

    @Autowired
    private SendMsgCache sendMsgCache;

    @Autowired
    private TerminalTypeDao terminalTypeDao;

    @Autowired
    private UserService userService;

    @Override
    public JsonResultBean sendTextByBatch(SendTxt sendTxt, List<String> vehicleIdList, String ipAddress) {
        List<DirectiveForm> directiveList = new ArrayList<>();
        // 用于批量下发文本信息
        String paramType = "F3-8300-sendTextByBatch";
        //用于组建缓存，存放HBase  终端离线  下发失败的信息 可以直接存储HBase
        Map<String, SendMsgDetail> failureSendMsgDetails = new HashMap<>(vehicleIdList.size());
        //下发成功等待返回结果待组装最终下发结果的 数据。
        Map<String, SendMsgDetail> waitResultMsgDetails = new HashMap<>(vehicleIdList.size());
        StringBuilder message = new StringBuilder();
        //先获取所有监控对象的信息，用于后期组装数据。
        Map<String, SendMsgMonitorInfo> monitorInfoMap = assblemSendMsgMonitorInfo(vehicleIdList);
        for (String monitorId : vehicleIdList) {
            directiveList.add(
                getDirectiveFormOrNew(paramType, monitorId, failureSendMsgDetails, waitResultMsgDetails, sendTxt,
                    monitorInfoMap, message));
        }
        //直接存储下发文本信息失败的
        sendMsgCache.putStoreCache(failureSendMsgDetails);
        //想缓存中放入需要等待返回结果的。
        sendMsgCache.putMsgCache(waitResultMsgDetails);
        addFilterAddDirective(directiveList);
        //过滤下发种状态的指令实体
        List<DirectiveForm> needSendText = getSendingDirectives(directiveList);
        dealAndSendText(vehicleIdList, ipAddress, sendTxt, needSendText, message);
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    @Override
    public JsonResultBean sendTtsByBatch(String sendTextContent, String vehicleIds, String ipAddress) {
        if (StrUtil.areNotBlank(sendTextContent, vehicleIds)) {
            List<String> vehicleIdList = Arrays.asList(vehicleIds.split(","));
            if (CollectionUtils.isNotEmpty(vehicleIdList)) {
                BindDTO bindDTO = MonitorUtils.getBindDTO(vehicleIdList.get(0));
                String deviceType = Objects.isNull(bindDTO) ? null : bindDTO.getDeviceType();
                SendTxt sendTxt;
                if (ProtocolTypeUtil.checkDeviceType2019(deviceType)) {
                    SendTextParam sendTextParam = new SendTextParam();
                    sendTextParam.setTerminalTtsPlay(1);
                    sendTextParam.setSendTextContent(sendTextContent);
                    sendTxt = SendTxt.getSendTxt2019(sendTextParam);
                } else {
                    sendTxt = SendTxt.getSendTxt2013(sendTextContent, "4");
                }
                return sendTextByBatch(sendTxt, vehicleIdList, ipAddress);
            }
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    private List<DirectiveForm> getSendingDirectives(List<DirectiveForm> directiveList) {
        return directiveList.stream().filter(directiveForm -> directiveForm.getStatus() == 4)
            .collect(Collectors.toList());
    }

    /**
     * 组装将存储HBase下发信息基础信息的实体类
     * @param sendTxt 发送信息的实体类的数据
     * @return SendMsgBasicInfo
     */
    @Override
    public SendMsgBasicInfo getBasicInfo(SendTxt sendTxt, Integer swiftNumber) {
        SendMsgBasicInfo basicInfo;
        //下发消息的标志 二进制，从右向左数 1位：是否紧急 3位：终端显示器显示 4位： TTS读播 5： 广告屏显示
        int sign = sendTxt.getSign();
        //播放方式 按照存储HBase的接口解析 0：TTS读播 1：终端显示器显示 2：广告屏显示 并以逗号隔开。
        List<String> playTypes = new ArrayList<>();
        // 终端显示器显示，第3位，播放方式"1"
        if (((sign >> 2) & 1) == 1) {
            playTypes.add("1");
        }
        // TTS读播，第4位，播放方式"0"
        if (((sign >> 3) & 1) == 1) {
            playTypes.add("0");
        }
        // 广告屏显示，第5位，播放方式"2"
        if (((sign >> 4) & 1) == 1) {
            playTypes.add("2");
        }
        final String playType = String.join(",", playTypes);
        //得到基础信息对象
        basicInfo = SendMsgBasicInfo
            .getInstance(sendTxt.getTxt(), swiftNumber, playType, 1, SystemHelper.getCurrentUserRealname(),
                System.currentTimeMillis());
        return basicInfo;
    }

    private void dealAndSendText(List<String> vehicleIdList, String ipAddress, SendTxt sendTxt,
        List<DirectiveForm> needSendText, StringBuilder message) {
        if (CollectionUtils.isNotEmpty(needSendText)) {
            List<VehicleScheduler> needAddVehicleScheduler = new ArrayList<>();
            String userName = SystemHelper.getCurrentUsername();
            //从Redis中查询config_list数据
            Map<String, BindDTO> monitorConfigInfoMap = MonitorUtils.getBindDTOMap(vehicleIdList);
            for (DirectiveForm info : needSendText) {
                String monitorId = info.getMonitorObjectId();
                BindDTO bindDTO = monitorConfigInfoMap.get(monitorId);
                if (Objects.isNull(bindDTO) || !Objects.equals(bindDTO.getBindType(), Vehicle.BindType.HAS_BIND)) {
                    continue;
                }
                boolean simCardNumberIsNotBlank = StringUtils.isNotBlank(bindDTO.getSimCardNumber());
                if (simCardNumberIsNotBlank) {
                    needAddVehicleScheduler
                        .add(getScheduleInfo(monitorId, sendTxt.getTxt(), bindDTO.getOrgId(), userName));
                }
                SendParam sendParam = getSendParam(info);
                f3SendStatusProcessService.updateSendParam(sendParam, 2);
                //下发短信
                sendTxtService.sendTextAndSubscribeAnswer(bindDTO, sendTxt, info.getSwiftNumber());
                final String title = "监控对象 : " + bindDTO.getName() + " 文本信息下发";
                final String msg = SendTxt.convertTxtToLogMsg(bindDTO.getDeviceType(), sendTxt);
                logSearchService.addLog(ipAddress, msg, "3", "more", title);
                message.append("监控对象 : ").append(bindDTO.getName()).append(" 文本信息下发").append(" <br/>");
            }
            //保存车辆调度信息
            saveVehicleSchedules(needAddVehicleScheduler);
            String msg = message.toString();
            if (StringUtils.isNotBlank(msg)) {
                logSearchService.addLog(ipAddress, msg, "3", "batch", "批量文本信息下发");
            }
        }
    }

    private void saveVehicleSchedules(List<VehicleScheduler> needAddVehicleScheduler) {
        if (CollectionUtils.isNotEmpty(needAddVehicleScheduler)) {
            //批量新增车辆调度信息
            vehicleScheduleDao.addByBatch(needAddVehicleScheduler);
        }
    }

    private SendParam getSendParam(DirectiveForm info) {
        SendParam sendParam = new SendParam();
        sendParam.setMsgSNACK(info.getSwiftNumber());
        sendParam.setParamId(info.getId());
        sendParam.setVehicleId(info.getMonitorObjectId());
        return sendParam;
    }

    private void addFilterAddDirective(List<DirectiveForm> directiveList) {
        List<DirectiveForm> needAddDirective =
            directiveList.stream().filter(directiveForm -> directiveForm.getUpdateOrAdd() == 2)
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(needAddDirective)) {
            parameterDao.addDirectiveByBatch(needAddDirective);
        }
    }

    /**
     * 从mysql的到指令数据或者新增一个，同时组装 需要存放HBase的map集合。
     * @param paramType             指令类型
     * @param monitorId             监控对象id
     * @param failureSendMsgDetails 下发文本信息失败的map
     * @param waitResultMsgDetails  等待下发文本信息结果的map
     * @param sendTxt               文本信息相关内容
     * @param monitorInfoMap        监控对象信息的Map
     * @return 指令数据。
     */
    private DirectiveForm getDirectiveFormOrNew(String paramType, String monitorId,
        Map<String, SendMsgDetail> failureSendMsgDetails, Map<String, SendMsgDetail> waitResultMsgDetails,
        SendTxt sendTxt, Map<String, SendMsgMonitorInfo> monitorInfoMap, StringBuilder message) {
        //得到流水号
        Integer swiftNumber = DeviceHelper.getRegisterDevice(monitorId, "");
        DirectiveForm directive = parameterDao.findDirective(monitorId, paramType);
        //获取下发文本信息的基本信息
        SendMsgBasicInfo basicInfo = getBasicInfo(sendTxt, swiftNumber);
        SendMsgDetail detail;
        if (directive == null) {
            directive = sendHelper
                .generateDirective(monitorId, null, paramType, swiftNumber == null ? 0 : swiftNumber, null, 1, null);
        }

        if (swiftNumber != null) {
            directive.setStatus(4);
            //得到存储HBase下发消息的详细信息实体类
            detail = SendMsgDetail.getSendMsg(monitorInfoMap.get(monitorId), basicInfo);
            //存放等待返回结果的缓存
            waitResultMsgDetails.put(detail.getKey(), detail);
        } else {
            //终端离线，未下发
            message.append("终端离线监控对象 : ").append(monitorInfoMap.get(monitorId).getMonitorName()).append(" 文本信息下发")
                .append(" <br/>");
            //组装下发消息信息的实体
            basicInfo.assembleSendResult(1, "终端离线");
            //得到存储HBase下发消息的详细信息实体类
            detail = SendMsgDetail.getSendMsg(monitorInfoMap.get(monitorId), basicInfo);
            failureSendMsgDetails.put(detail.getStoreKey(), detail);
            directive.setStatus(5);
        }
        directive.setSwiftNumber(swiftNumber == null ? 0 : swiftNumber);
        directive.setDirectiveName("0x8300");
        if (directive.getUpdateOrAdd() == 1) {
            directive.setDownTime(new Date());
            parameterDao.updateDirectiveById(directive);
        }
        return directive;
    }

    /**
     * 组装下发文本信息 监控对象信息
     * @param vehicleIdList 车辆Id 集合
     * @return map
     */
    @Override
    public Map<String, SendMsgMonitorInfo> assblemSendMsgMonitorInfo(List<String> vehicleIdList) {
        Map<String, SendMsgMonitorInfo> monitorMap = new HashMap<>(vehicleIdList.size() * 3 / 2);
        List<Map<String, String>> monitorInfoMapList =
            RedisHelper.batchGetHashMap(RedisKeyEnum.MONITOR_INFO.ofs(vehicleIdList));
        Map<String, VehicleDTO> bindInfos =
            monitorInfoMapList.stream().filter(Objects::nonNull).map(map -> MapUtil.mapToObj(map, VehicleDTO.class))
                .collect(Collectors.toMap(VehicleDTO::getId, Function.identity()));
        for (VehicleDTO bind : bindInfos.values()) {
            SendMsgMonitorInfo monitorInfo;
            if (!Objects.equals(bind.getBindType(), Vehicle.BindType.HAS_BIND)) {
                continue;
            }
            String id = bind.getId();
            String groupName = bind.getOrgName();
            String monitorName = bind.getName();
            String objectType;
            Integer signColor;
            monitorInfo = SendMsgMonitorInfo.getInstance(id, monitorName, groupName);
            //监控对象类型是车辆时  获取车辆类型和颜色
            if ("0".equals(bind.getMonitorType())) {
                objectType = bind.getVehicleType();
                signColor = bind.getPlateColor();
                monitorInfo.assembelVehicleInfo(signColor, objectType);
            }
            monitorMap.put(id, monitorInfo);
        }
        return monitorMap;
    }

    @Override
    public JsonResultBean getAudioAndVideoParameters(String monitorId) {
        BindDTO configInfo = MonitorUtils.getBindDTO(monitorId);
        if (configInfo == null) {
            return new JsonResultBean(JsonResultBean.FAULT, "没有找到监控对象绑定信息");
        }
        String terminalType = configInfo.getTerminalType();
        String terminalManufacturer = configInfo.getTerminalManufacturer();
        TerminalTypeInfo terminalTypeInfo = terminalTypeDao.getTerminalTypeInfoBy(terminalType, terminalManufacturer);
        if (terminalTypeInfo == null) {
            return new JsonResultBean(JsonResultBean.FAULT, "没有找到监控对象音视频参数");
        }
        Integer supportVideoFlag = terminalTypeInfo.getSupportVideoFlag();
        DeviceVideoParamDto deviceVideoParamDto = new DeviceVideoParamDto();
        deviceVideoParamDto.setUserUuid(userService.getCurrentUserUuid());
        deviceVideoParamDto.setMonitorId(monitorId);
        deviceVideoParamDto.setSimcardId(configInfo.getSimCardId());
        deviceVideoParamDto.setSimcardNumber(configInfo.getSimCardNumber());
        deviceVideoParamDto.setDeviceId(configInfo.getDeviceId());
        deviceVideoParamDto.setDeviceNumber(configInfo.getDeviceNumber());
        deviceVideoParamDto.setSupportVideoFlag(supportVideoFlag);
        deviceVideoParamDto.setDeviceType(configInfo.getDeviceType());
        if (Objects.equals(supportVideoFlag, 1)) {
            Integer audioFormat = terminalTypeInfo.getAudioFormat();
            deviceVideoParamDto.setAudioFormatStr(DeviceHelper.AUDIO_FORMAT.b2p(audioFormat));
            Integer storageAudioFormat = terminalTypeInfo.getStorageAudioFormat();
            deviceVideoParamDto.setStorageAudioFormatStr(DeviceHelper.AUDIO_FORMAT.b2p(storageAudioFormat));
            Integer samplingRate = terminalTypeInfo.getSamplingRate();
            deviceVideoParamDto.setSamplingRateStr(DeviceHelper.SAMPLING_RATE.b2p(samplingRate));
            Integer storageSamplingRate = terminalTypeInfo.getStorageSamplingRate();
            deviceVideoParamDto.setStorageSamplingRateStr(DeviceHelper.SAMPLING_RATE.b2p(storageSamplingRate));
            Integer vocalTract = terminalTypeInfo.getVocalTract();
            deviceVideoParamDto.setVocalTractStr(DeviceHelper.VOCAL_TRACT.b2p(vocalTract));
            Integer storageVocalTract = terminalTypeInfo.getStorageVocalTract();
            deviceVideoParamDto.setStorageVocalTractStr(DeviceHelper.VOCAL_TRACT.b2p(storageVocalTract));
        }
        return new JsonResultBean(deviceVideoParamDto);
    }

    @Override
    public JsonResultBean getSendTextStatusList(String vehicleIds) throws Exception {
        List<DirectiveForm> result = new ArrayList<>();
        if (StringUtils.isNotBlank(vehicleIds)) {
            List<String> monitorIdList = Arrays.asList(vehicleIds.split(","));
            Map<String, BindDTO> bindInfos = MonitorUtils.getBindDTOMap(monitorIdList);
            List<DirectiveForm> sendTextStatusList =
                parameterDao.getSendStatusList(monitorIdList, "F3-8300-sendTextByBatch");
            if (CollectionUtils.isNotEmpty(sendTextStatusList)) {
                List<DirectiveForm> filterList = sendTextStatusList.stream().filter(
                    directiveForm -> installData(directiveForm, bindInfos.get(directiveForm.getMonitorObjectId())))
                    .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(filterList)) {
                    result.addAll(filterList);
                }
            }
            return new JsonResultBean(result);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public JsonResultBean getIoSignalInfo(String monitorId, String type) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (StringUtils.isNotBlank(monitorId) && StringUtils.isNotBlank(type)) {
            String monitorLocation = RedisHelper.getString(HistoryRedisKeyEnum.MONITOR_LOCATION.of(monitorId));
            if (StringUtils.isNotBlank(monitorLocation)) {
                Message message = JSON.parseObject(monitorLocation, Message.class);
                T808Message t808Message = JSON.parseObject(message.getData().toString(), T808Message.class);
                LocationInfo info = JSON.parseObject(t808Message.getMsgBody().toString(), LocationInfo.class);
                //监控对象绑定的io信息
                List<SwitchInfo> monitorBindIoInfo = ioVehicleConfigDao.getBindIoInfoByVehicleId(monitorId);
                if (ConstantUtil.TERMINAL_IO.equals(type)) {
                    JSONArray ioSignalData = info.getIoSignalData();
                    installTerminalIoInfo(result, monitorBindIoInfo, ioSignalData);
                    return new JsonResultBean(result);
                }
                if (ConstantUtil.SENSOR_IO.equals(type)) {
                    JSONArray cirIoCheckData = info.getCirIoCheckData();
                    installSensorIoInfo(result, monitorBindIoInfo, cirIoCheckData);
                    return new JsonResultBean(result);
                }
                if (ConstantUtil.ALL_IO.equals(type)) {
                    installTerminalIoInfo(result, monitorBindIoInfo, info.getIoSignalData());
                    installSensorIoInfo(result, monitorBindIoInfo, info.getCirIoCheckData());
                    return new JsonResultBean(result);
                }
            }
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    /**
     * 组装传感器io信息
     */
    private void installSensorIoInfo(List<Map<String, Object>> result, List<SwitchInfo> monitorBindIoInfo,
        JSONArray cirIoCheckData) {
        if (cirIoCheckData != null && cirIoCheckData.size() > 0) {
            int lenK = CollectionUtils.isNotEmpty(monitorBindIoInfo) ? monitorBindIoInfo.size() : 0;
            for (int i = 0, len = cirIoCheckData.size(); i < len; i++) {
                Map<String, Object> ioInfo = new HashMap<>(16);
                List<Map<String, Object>> ioStatusInfoList = new ArrayList<>();
                JSONObject sensorIoInfo = cirIoCheckData.getJSONObject(i);
                Integer id = sensorIoInfo.getInteger("id");
                if (id != 0x91 && id != 0x92) {
                    continue;
                }
                Integer ioType = id == 0x91 ? 2 : 3;
                ioInfo.put("id", id);
                //传感器是否异常：0正常 1异常
                Integer unusual = sensorIoInfo.getInteger("unusual");
                //传感器状态
                ioInfo.put("sensorStatus", unusual);
                if (unusual == 0) {
                    //IO总数
                    Integer ioCount = sensorIoInfo.getInteger("ioCount");
                    //io位报警
                    JSONArray statusList = sensorIoInfo.getJSONArray("statusList");
                    //0:表示所有io位正常 1:表示io位有正常或者异常
                    Integer abnormalType = sensorIoInfo.getInteger("abnormal");
                    JSONArray abnormalList = abnormalType == 0 ? null : sensorIoInfo.getJSONArray("abnormalList");
                    for (int j = 0; j < ioCount; j++) {
                        Map<String, Object> ioStatusInfo = new HashMap<>(16);
                        int ioIndex = j / 32;
                        //状态集合
                        Integer ioStatus = statusList.getJSONObject(ioIndex).getInteger("ioStatus");
                        //异常集合
                        Integer ioAbnormal =
                            abnormalList == null ? null : abnormalList.getJSONObject(ioIndex).getInteger("ioAbnormal");
                        //0:正常;  1:异常
                        int nowIoAbnormalStatus = ioAbnormal == null ? 0 :
                            ConvertUtil.binaryIntegerWithOne(ioAbnormal, ioIndex >= 1 ? j - (32 * ioIndex) : j);
                        //状态(0:高电平; 1:低电平)
                        int state =
                            ConvertUtil.binaryIntegerWithOne(ioStatus, ioIndex >= 1 ? j - (32 * ioIndex) : j);
                        int k = 0;
                        for (; k < lenK; k++) {
                            SwitchInfo switchInfo = monitorBindIoInfo.get(k);
                            if (ioType.equals(switchInfo.getIoType()) && j == switchInfo.getIoSite()) {
                                // io名称
                                ioStatusInfo.put("ioName", switchInfo.getName());
                                if (nowIoAbnormalStatus == 1) {
                                    // io状态名称
                                    ioStatusInfo.put("ioStatusName", switchInfo.getName() + "异常");
                                    break;
                                }
                                //电平状态
                                String signalType =
                                    state == 0 ? switchInfo.getHighSignalType() : switchInfo.getLowSignalType();
                                //io状态str
                                String ioStateStr =
                                    "1".endsWith(signalType) ? switchInfo.getStateOne() : switchInfo.getStateTwo();
                                // io状态名称
                                ioStatusInfo.put("ioStatusName", ioStateStr);
                                break;
                            }
                        }
                        // 如果没有对应io没有绑定跳过
                        if (k == lenK) {
                            // io名称
                            ioStatusInfo.put("ioName", "I/O " + j);
                            // io状态名称
                            ioStatusInfo
                                .put("ioStatusName", nowIoAbnormalStatus == 1 ? "异常" : (state == 0 ? "高电平" : "低电平"));
                        }
                        ioStatusInfo.put("ioSite", j);
                        ioStatusInfoList.add(ioStatusInfo);
                    }
                    ioInfo.put("ioData", ioStatusInfoList);
                }
                result.add(ioInfo);
            }
        }
    }

    /**
     * 组装终端io信息
     */
    private void installTerminalIoInfo(List<Map<String, Object>> result, List<SwitchInfo> monitorBindIoInfo,
        JSONArray ioSignalData) {
        if (ioSignalData != null && ioSignalData.size() > 0) {
            int lenK = CollectionUtils.isNotEmpty(monitorBindIoInfo) ? monitorBindIoInfo.size() : 0;
            for (int i = 0, len = ioSignalData.size(); i < len; i++) {
                Map<String, Object> ioInfo = new HashMap<>(16);
                List<Map<String, Object>> ioStatusInfoList = new ArrayList<>();
                JSONObject terminalIoInfo = ioSignalData.getJSONObject(i);
                Integer id = terminalIoInfo.getInteger("id");
                if (id != 0x90) {
                    continue;
                }
                ioInfo.put("id", id);
                //传感器是否异常：0正常 1异常
                Integer unusual = terminalIoInfo.getInteger("unusual");
                //传感器状态
                ioInfo.put("sensorStatus", unusual);
                if (unusual == 0) {
                    for (int j = 0; j <= 3; j++) {
                        Map<String, Object> ioStatusInfo = new HashMap<>(16);
                        //状态(0:高电平; 1:低电平 2:无接口)
                        Integer state = terminalIoInfo.getInteger("signal" + j);
                        int k = 0;
                        for (; k < lenK; k++) {
                            SwitchInfo switchInfo = monitorBindIoInfo.get(k);
                            if (switchInfo.getIoType() == 1 && j == switchInfo.getIoSite()) {
                                // io名称
                                ioStatusInfo.put("ioName", switchInfo.getName());
                                if (state == 2) {
                                    ioStatusInfo.put("ioStatusName", "无接口");
                                    break;
                                }
                                //电平状态
                                String signalType =
                                    state == 0 ? switchInfo.getHighSignalType() : switchInfo.getLowSignalType();
                                //io状态str
                                String ioStateStr =
                                    "1".endsWith(signalType) ? switchInfo.getStateOne() : switchInfo.getStateTwo();
                                // io状态名称
                                ioStatusInfo.put("ioStatusName", ioStateStr);
                                break;
                            }
                        }
                        // 如果没有对应io没有绑定跳过
                        if (k == lenK) {
                            // io名称
                            ioStatusInfo.put("ioName", "I/O " + j);
                            // io状态名称
                            ioStatusInfo.put("ioStatusName", state == 0 ? "高电平" : (state == 1 ? "低电平" : "无接口"));
                        }
                        ioStatusInfo.put("ioSite", j);
                        ioStatusInfoList.add(ioStatusInfo);
                    }
                    ioInfo.put("ioData", ioStatusInfoList);
                }
                result.add(ioInfo);
            }
        }
    }

    /**
     * 新增车辆调度信息
     */
    private VehicleScheduler getScheduleInfo(String monitorId, String sendTextContent, String groupId,
        String userName) {
        VehicleScheduler vehicleScheduler = new VehicleScheduler();
        vehicleScheduler.setVehicleId(monitorId);
        if (!StringUtils.isEmpty(sendTextContent) && (sendTextContent.contains("zw") || sendTextContent
            .contains("ZW"))) {
            sendTextContent = "";
        }
        vehicleScheduler.setContent(sendTextContent);
        vehicleScheduler.setGroupId(groupId);
        vehicleScheduler.setSendDate(new Date());
        vehicleScheduler.setSendTime(new Date());
        vehicleScheduler.setSendUsername(userName);
        vehicleScheduler.setCreateDataUsername(userName);
        return vehicleScheduler;
    }

    /**
     * 组装数据
     */
    private boolean installData(DirectiveForm directiveForm, BindDTO bindInfo) {
        if (bindInfo != null) {
            directiveForm.setPlateNumber(bindInfo.getName());
            return true;
        }
        return false;
    }
}

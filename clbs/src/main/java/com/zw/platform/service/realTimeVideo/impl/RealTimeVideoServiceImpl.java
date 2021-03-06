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
        // ??????????????????????????????
        String paramType = "F3-8300-sendTextByBatch";
        //???????????????????????????HBase  ????????????  ????????????????????? ??????????????????HBase
        Map<String, SendMsgDetail> failureSendMsgDetails = new HashMap<>(vehicleIdList.size());
        //???????????????????????????????????????????????????????????? ?????????
        Map<String, SendMsgDetail> waitResultMsgDetails = new HashMap<>(vehicleIdList.size());
        StringBuilder message = new StringBuilder();
        //??????????????????????????????????????????????????????????????????
        Map<String, SendMsgMonitorInfo> monitorInfoMap = assblemSendMsgMonitorInfo(vehicleIdList);
        for (String monitorId : vehicleIdList) {
            directiveList.add(
                getDirectiveFormOrNew(paramType, monitorId, failureSendMsgDetails, waitResultMsgDetails, sendTxt,
                    monitorInfoMap, message));
        }
        //???????????????????????????????????????
        sendMsgCache.putStoreCache(failureSendMsgDetails);
        //????????????????????????????????????????????????
        sendMsgCache.putMsgCache(waitResultMsgDetails);
        addFilterAddDirective(directiveList);
        //????????????????????????????????????
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
     * ???????????????HBase????????????????????????????????????
     * @param sendTxt ?????????????????????????????????
     * @return SendMsgBasicInfo
     */
    @Override
    public SendMsgBasicInfo getBasicInfo(SendTxt sendTxt, Integer swiftNumber) {
        SendMsgBasicInfo basicInfo;
        //????????????????????? ??????????????????????????? 1?????????????????? 3??????????????????????????? 4?????? TTS?????? 5??? ???????????????
        int sign = sendTxt.getSign();
        //???????????? ????????????HBase??????????????? 0???TTS?????? 1???????????????????????? 2?????????????????? ?????????????????????
        List<String> playTypes = new ArrayList<>();
        // ???????????????????????????3??????????????????"1"
        if (((sign >> 2) & 1) == 1) {
            playTypes.add("1");
        }
        // TTS????????????4??????????????????"0"
        if (((sign >> 3) & 1) == 1) {
            playTypes.add("0");
        }
        // ?????????????????????5??????????????????"2"
        if (((sign >> 4) & 1) == 1) {
            playTypes.add("2");
        }
        final String playType = String.join(",", playTypes);
        //????????????????????????
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
            //???Redis?????????config_list??????
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
                //????????????
                sendTxtService.sendTextAndSubscribeAnswer(bindDTO, sendTxt, info.getSwiftNumber());
                final String title = "???????????? : " + bindDTO.getName() + " ??????????????????";
                final String msg = SendTxt.convertTxtToLogMsg(bindDTO.getDeviceType(), sendTxt);
                logSearchService.addLog(ipAddress, msg, "3", "more", title);
                message.append("???????????? : ").append(bindDTO.getName()).append(" ??????????????????").append(" <br/>");
            }
            //????????????????????????
            saveVehicleSchedules(needAddVehicleScheduler);
            String msg = message.toString();
            if (StringUtils.isNotBlank(msg)) {
                logSearchService.addLog(ipAddress, msg, "3", "batch", "????????????????????????");
            }
        }
    }

    private void saveVehicleSchedules(List<VehicleScheduler> needAddVehicleScheduler) {
        if (CollectionUtils.isNotEmpty(needAddVehicleScheduler)) {
            //??????????????????????????????
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
     * ???mysql??????????????????????????????????????????????????? ????????????HBase???map?????????
     * @param paramType             ????????????
     * @param monitorId             ????????????id
     * @param failureSendMsgDetails ???????????????????????????map
     * @param waitResultMsgDetails  ?????????????????????????????????map
     * @param sendTxt               ????????????????????????
     * @param monitorInfoMap        ?????????????????????Map
     * @return ???????????????
     */
    private DirectiveForm getDirectiveFormOrNew(String paramType, String monitorId,
        Map<String, SendMsgDetail> failureSendMsgDetails, Map<String, SendMsgDetail> waitResultMsgDetails,
        SendTxt sendTxt, Map<String, SendMsgMonitorInfo> monitorInfoMap, StringBuilder message) {
        //???????????????
        Integer swiftNumber = DeviceHelper.getRegisterDevice(monitorId, "");
        DirectiveForm directive = parameterDao.findDirective(monitorId, paramType);
        //???????????????????????????????????????
        SendMsgBasicInfo basicInfo = getBasicInfo(sendTxt, swiftNumber);
        SendMsgDetail detail;
        if (directive == null) {
            directive = sendHelper
                .generateDirective(monitorId, null, paramType, swiftNumber == null ? 0 : swiftNumber, null, 1, null);
        }

        if (swiftNumber != null) {
            directive.setStatus(4);
            //????????????HBase????????????????????????????????????
            detail = SendMsgDetail.getSendMsg(monitorInfoMap.get(monitorId), basicInfo);
            //?????????????????????????????????
            waitResultMsgDetails.put(detail.getKey(), detail);
        } else {
            //????????????????????????
            message.append("???????????????????????? : ").append(monitorInfoMap.get(monitorId).getMonitorName()).append(" ??????????????????")
                .append(" <br/>");
            //?????????????????????????????????
            basicInfo.assembleSendResult(1, "????????????");
            //????????????HBase????????????????????????????????????
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
     * ???????????????????????? ??????????????????
     * @param vehicleIdList ??????Id ??????
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
            //??????????????????????????????  ???????????????????????????
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
            return new JsonResultBean(JsonResultBean.FAULT, "????????????????????????????????????");
        }
        String terminalType = configInfo.getTerminalType();
        String terminalManufacturer = configInfo.getTerminalManufacturer();
        TerminalTypeInfo terminalTypeInfo = terminalTypeDao.getTerminalTypeInfoBy(terminalType, terminalManufacturer);
        if (terminalTypeInfo == null) {
            return new JsonResultBean(JsonResultBean.FAULT, "???????????????????????????????????????");
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
                //?????????????????????io??????
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
     * ???????????????io??????
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
                //????????????????????????0?????? 1??????
                Integer unusual = sensorIoInfo.getInteger("unusual");
                //???????????????
                ioInfo.put("sensorStatus", unusual);
                if (unusual == 0) {
                    //IO??????
                    Integer ioCount = sensorIoInfo.getInteger("ioCount");
                    //io?????????
                    JSONArray statusList = sensorIoInfo.getJSONArray("statusList");
                    //0:????????????io????????? 1:??????io????????????????????????
                    Integer abnormalType = sensorIoInfo.getInteger("abnormal");
                    JSONArray abnormalList = abnormalType == 0 ? null : sensorIoInfo.getJSONArray("abnormalList");
                    for (int j = 0; j < ioCount; j++) {
                        Map<String, Object> ioStatusInfo = new HashMap<>(16);
                        int ioIndex = j / 32;
                        //????????????
                        Integer ioStatus = statusList.getJSONObject(ioIndex).getInteger("ioStatus");
                        //????????????
                        Integer ioAbnormal =
                            abnormalList == null ? null : abnormalList.getJSONObject(ioIndex).getInteger("ioAbnormal");
                        //0:??????;  1:??????
                        int nowIoAbnormalStatus = ioAbnormal == null ? 0 :
                            ConvertUtil.binaryIntegerWithOne(ioAbnormal, ioIndex >= 1 ? j - (32 * ioIndex) : j);
                        //??????(0:?????????; 1:?????????)
                        int state =
                            ConvertUtil.binaryIntegerWithOne(ioStatus, ioIndex >= 1 ? j - (32 * ioIndex) : j);
                        int k = 0;
                        for (; k < lenK; k++) {
                            SwitchInfo switchInfo = monitorBindIoInfo.get(k);
                            if (ioType.equals(switchInfo.getIoType()) && j == switchInfo.getIoSite()) {
                                // io??????
                                ioStatusInfo.put("ioName", switchInfo.getName());
                                if (nowIoAbnormalStatus == 1) {
                                    // io????????????
                                    ioStatusInfo.put("ioStatusName", switchInfo.getName() + "??????");
                                    break;
                                }
                                //????????????
                                String signalType =
                                    state == 0 ? switchInfo.getHighSignalType() : switchInfo.getLowSignalType();
                                //io??????str
                                String ioStateStr =
                                    "1".endsWith(signalType) ? switchInfo.getStateOne() : switchInfo.getStateTwo();
                                // io????????????
                                ioStatusInfo.put("ioStatusName", ioStateStr);
                                break;
                            }
                        }
                        // ??????????????????io??????????????????
                        if (k == lenK) {
                            // io??????
                            ioStatusInfo.put("ioName", "I/O " + j);
                            // io????????????
                            ioStatusInfo
                                .put("ioStatusName", nowIoAbnormalStatus == 1 ? "??????" : (state == 0 ? "?????????" : "?????????"));
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
     * ????????????io??????
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
                //????????????????????????0?????? 1??????
                Integer unusual = terminalIoInfo.getInteger("unusual");
                //???????????????
                ioInfo.put("sensorStatus", unusual);
                if (unusual == 0) {
                    for (int j = 0; j <= 3; j++) {
                        Map<String, Object> ioStatusInfo = new HashMap<>(16);
                        //??????(0:?????????; 1:????????? 2:?????????)
                        Integer state = terminalIoInfo.getInteger("signal" + j);
                        int k = 0;
                        for (; k < lenK; k++) {
                            SwitchInfo switchInfo = monitorBindIoInfo.get(k);
                            if (switchInfo.getIoType() == 1 && j == switchInfo.getIoSite()) {
                                // io??????
                                ioStatusInfo.put("ioName", switchInfo.getName());
                                if (state == 2) {
                                    ioStatusInfo.put("ioStatusName", "?????????");
                                    break;
                                }
                                //????????????
                                String signalType =
                                    state == 0 ? switchInfo.getHighSignalType() : switchInfo.getLowSignalType();
                                //io??????str
                                String ioStateStr =
                                    "1".endsWith(signalType) ? switchInfo.getStateOne() : switchInfo.getStateTwo();
                                // io????????????
                                ioStatusInfo.put("ioStatusName", ioStateStr);
                                break;
                            }
                        }
                        // ??????????????????io??????????????????
                        if (k == lenK) {
                            // io??????
                            ioStatusInfo.put("ioName", "I/O " + j);
                            // io????????????
                            ioStatusInfo.put("ioStatusName", state == 0 ? "?????????" : (state == 1 ? "?????????" : "?????????"));
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
     * ????????????????????????
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
     * ????????????
     */
    private boolean installData(DirectiveForm directiveForm, BindDTO bindInfo) {
        if (bindInfo != null) {
            directiveForm.setPlateNumber(bindInfo.getName());
            return true;
        }
        return false;
    }
}

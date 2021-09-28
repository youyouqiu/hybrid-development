package com.zw.platform.service.realTimeVideo.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.google.common.collect.Lists;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.event.ConfigUnBindEvent;
import com.zw.platform.basic.service.IpAddressService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.basic.util.DbUtils;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.TerminalTypeInfo;
import com.zw.platform.domain.core.SendParam;
import com.zw.platform.domain.realTimeVideo.RecordingSetting;
import com.zw.platform.domain.realTimeVideo.VideoChannelSetting;
import com.zw.platform.domain.realTimeVideo.VideoSetting;
import com.zw.platform.domain.realTimeVideo.VideoSleepSetting;
import com.zw.platform.domain.systems.Directive;
import com.zw.platform.domain.systems.form.DirectiveForm;
import com.zw.platform.domain.vas.alram.form.AlarmParameterSettingForm;
import com.zw.platform.domain.vas.alram.query.AlarmSettingQuery;
import com.zw.platform.push.handler.device.DeviceHelper;
import com.zw.platform.push.mqserver.ZMQFencePub;
import com.zw.platform.repository.modules.ParameterDao;
import com.zw.platform.repository.modules.TerminalTypeDao;
import com.zw.platform.repository.realTimeVideo.RecordingSettingDao;
import com.zw.platform.repository.realTimeVideo.VideoChannelSettingDao;
import com.zw.platform.repository.realTimeVideo.VideoSettingDao;
import com.zw.platform.repository.realTimeVideo.VideoSleepSettingDao;
import com.zw.platform.repository.vas.AlarmSettingDao;
import com.zw.platform.service.core.F3SendStatusProcessService;
import com.zw.platform.service.realTimeVideo.VideoParamSettingService;
import com.zw.platform.service.realTimeVideo.VideoSleepService;
import com.zw.platform.service.reportManagement.impl.LogSearchServiceImpl;
import com.zw.platform.util.CommonUtil;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.RedisQueryUtil;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.platform.vo.realTimeVideo.AlarmParam;
import com.zw.platform.vo.realTimeVideo.VideoParamVo;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import com.zw.protocol.util.ProtocolTypeUtil;
import com.zw.ws.impl.WsVideoService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author 作者 E-mail:yangya
 * @version 创建时间：2017年12月26日 上午11:34:28 类说明:
 */
@Service
public class VideoParamSettingServiceImpl implements VideoParamSettingService, IpAddressService {

    @Autowired
    private UserService userService;

    @Autowired
    private VideoSettingDao videoSettingMapper;

    @Autowired
    private VideoChannelSettingDao videoChannelSettingDao;

    @Autowired
    private RecordingSettingDao recordingSettingDao;

    @Autowired
    private VideoSleepSettingDao videoSleepSettingDao;

    @Autowired
    private WsVideoService wsVideoService;

    @Autowired
    private ParameterDao parameterDao;

    @Autowired
    private AlarmSettingDao alarmSettingDao;

    @Autowired
    private LogSearchServiceImpl logSearchServiceImpl;

    @Autowired
    private VideoSleepService videoSleepService;

    @Autowired
    private F3SendStatusProcessService f3SendStatusProcessService;

    @Autowired
    private TerminalTypeDao terminalTypeDao;

    @Override
    public Page<Map<String, Object>> findVideoSetting(AlarmSettingQuery query) {
        String groupId = query.getGroupId();
        String assignmentId = query.getAssignmentId();
        String deviceType = query.getDeviceType();
        String simpleQueryParam = query.getSimpleQueryParam();
        //  获取用户权限内的绑定的并且是808协议的监控对象
        List<String> vehicleIds =
            userService.getValidVehicleId(groupId, assignmentId, deviceType, simpleQueryParam, null, true);
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
        return RedisQueryUtil.getListToPage(assemblePageResultList(pageVehicleIdList), query, listSize);
    }

    /**
     * 组装分页返回结果集合
     */
    private List<Map<String, Object>> assemblePageResultList(List<String> pageVehicleIdList) {
        Map<String, BindDTO> bindInfoMap = VehicleUtil.batchGetBindInfosByRedis(pageVehicleIdList);
        // 设置对象类型名称
        userService.setObjectTypeName(bindInfoMap.values());

        List<VideoSetting> settingIds = videoSettingMapper.findIdsAndIsSettingByVehicleIds(pageVehicleIdList);
        Map<String, List<VideoSetting>> videoSettingMap =
            settingIds.stream().collect(Collectors.groupingBy(VideoSetting::getVideoSettingVid));
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (String moId : pageVehicleIdList) {
            Map<String, Object> resultMap = new HashMap<>(16);
            BindDTO bindDTO = bindInfoMap.get(moId);
            resultMap.put("brand", bindDTO.getName());
            resultMap.put("groupName", bindDTO.getOrgName());
            resultMap.put("vehicleType", bindDTO.getObjectTypeName());
            resultMap.put("deviceType", bindDTO.getDeviceType());
            resultMap.put("id", moId);
            resultMap.put("monitorType", bindDTO.getMonitorType());
            List<VideoSetting> videoSettingList = videoSettingMap.get(moId);
            if (CollectionUtils.isNotEmpty(videoSettingList)) {
                VideoSetting videoSetting = videoSettingList.get(0);
                resultMap.put("videoSettingVid", videoSetting.getVideoSettingVid());
                resultMap.put("videoChannelVid", videoSetting.getVideoChannelVid());
                resultMap.put("videoSleepVid", videoSetting.getVideoSleepVid());
                resultMap.put("videoRecordingVid", videoSetting.getVideoRecordingVid());
            } else {
                resultMap.put("videoSettingVid", null);
                resultMap.put("videoChannelVid", null);
                resultMap.put("videoSleepVid", null);
                resultMap.put("videoRecordingVid", null);
            }
            // 查询下发设置
            List<Directive> paramList = parameterDao.findParameterByType(moId, null, "8103-75-76-77-79-7A");
            Directive param = null;
            if (CollectionUtils.isNotEmpty(paramList)) {
                param = paramList.get(0);
            }
            if (param != null) {
                resultMap.put("paramId", param.getId());
                resultMap.put("status", param.getStatus());
            }
            resultList.add(resultMap);
        }
        return resultList;
    }

    @Override
    public List<JSONObject> getAllReferVehicle(String vehicleId, String deviceType) {
        deviceType = ProtocolTypeUtil.getAll2019Protocol().contains(deviceType) ? "11" : "-1";
        // 查询用户权限下的808协议的监控对象
        Set<String> validVehicleIds =
            new HashSet<>(userService.getValidVehicleId(null, null, deviceType, null, null, true));
        final Set<String> beenSetVehicleIds = videoSettingMapper.getVehicleIdBySettingVideo();
        // 仅保留设置过的且有效的，而且排除入参
        validVehicleIds.retainAll(beenSetVehicleIds);
        validVehicleIds.remove(vehicleId);
        return VehicleUtil.batchGetBindInfosByRedis(validVehicleIds, Lists.newArrayList("name")).values().stream()
            .map(obj -> {
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("id", obj.getId());
                jsonObj.put("brand", obj.getName());
                return jsonObj;
            }).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getVideoParam(String vehicleId) {
        return getVideoParams(vehicleId);
    }

    /**
     * 获取参数设置值
     */
    public Map<String, Object> getVideoParams(String vehicleId) {
        Map<String, Object> videoParams = new HashMap<>(16);
        // 查询视频参数设置
        List<VideoSetting> videoSettings = videoSettingMapper.getVideoParamByVehicleId(vehicleId);
        // 查询通道号设置
        List<VideoChannelSetting> videoChannels = videoChannelSettingDao.getVideoChannelByVehicleId(vehicleId);
        // 查询视频报警设置
        RecordingSetting videoRecording = recordingSettingDao.getVedioRecordingSettingByVehicleId(vehicleId);
        // 查询报警参数设置
        List<AlarmParameterSettingForm> videoAlarms = getVideoAlarms(vehicleId);
        // 查询休眠唤醒设置
        VideoSleepSetting videoSleep = videoSleepSettingDao.getVideoSleepByVehicleId(vehicleId);
        videoParams.put("videoSettings", videoSettings);
        videoParams.put("videoChannels", videoChannels);
        videoParams.put("vedioAlarms", videoAlarms);
        videoParams.put("vedioRecording", videoRecording);
        videoParams.put("videoSleep", videoSleep);
        return videoParams;
    }

    /**
     * 获取视频报警类型
     */
    public List<AlarmParameterSettingForm> getVideoAlarms(String vehicleId) {
        List<AlarmParameterSettingForm> videoAlarmList = recordingSettingDao.getVedioAlarmsByVid(vehicleId);
        Map<String, AlarmParameterSettingForm> videoAlarmMap = new HashMap<>(videoAlarmList.size());
        videoAlarmList.forEach(videoAlarm -> {
            String name;
            String pos;
            if (videoAlarm.getName().contains("存储器")) {
                name = "存储器故障";
                pos = "127";
            } else if (videoAlarm.getName().contains("异常驾驶行为")) {
                name = "异常驾驶行为";
                pos = "130";
            } else if (videoAlarm.getName().contains("视频信号丢失")) {
                name = "视频信号丢失";
                pos = "125";
            } else if (videoAlarm.getName().contains("视频信号遮挡")) {
                name = "视频信号遮挡";
                pos = "126";
            } else {
                name = videoAlarm.getName();
                pos = videoAlarm.getPos();
            }
            videoAlarm.setName(name);
            videoAlarm.setPos(pos);
            if (!videoAlarmMap.containsKey(name)) {
                videoAlarmMap.put(name, videoAlarm);
            }
        });
        return new ArrayList<>(videoAlarmMap.values());
    }

    @Override
    public void saveAllParam(VideoParamVo videoParam) {
        String vehicleId = videoParam.getVehicleId();
        String ipAddress = getIpAddress();
        String currentUsername = SystemHelper.getCurrentUsername();
        BindDTO bindDTO = VehicleUtil.getBindInfoByRedis(vehicleId);
        // 添加修改音视频参数
        String videoSettingJsonStr = videoParam.getVideoSetting();
        saveVideoSettingAndRecordLog(bindDTO, ipAddress, currentUsername, videoSettingJsonStr);
        // 添加修改音视频通道
        String videoChannelSettingsJsonStr = videoParam.getVideoChannelSettings();
        saveVideoChannelSettingAndRecordLog(bindDTO, ipAddress, currentUsername, videoChannelSettingsJsonStr);
        // 添加修改音视频报警参数
        String alarmParamsJsonStr = videoParam.getAlarmParams();
        saveVideoAlarmParamSettingAndRecordLog(bindDTO, ipAddress, currentUsername, alarmParamsJsonStr);
        // 添加修改音视频录像参数
        String recordingSettingJsonStr = videoParam.getRecordingSetting();
        saveVideoRecordingSetting(bindDTO, currentUsername, recordingSettingJsonStr);
        // 添加音视频休眠参数
        String videoSleepSettingJsonStr = videoParam.getVideoSleepSetting();
        saveVideoSleepSettingAndRecordLog(bindDTO, ipAddress, currentUsername, videoSleepSettingJsonStr);

        // 删除下发表设置
        parameterDao.deleteByVechicleidType(vehicleId, "8103-75-76-77-79-7A");
        ZMQFencePub.pubChangeFence("20");
    }

    private void saveVideoSleepSettingAndRecordLog(BindDTO bindDTO, String ipAddress, String currentUsername,
        String videoSleepSettingJsonStr) {
        if (StringUtils.isBlank(videoSleepSettingJsonStr)) {
            return;
        }
        String vehicleId = bindDTO.getId();
        VideoSleepSetting videoSleepSetting = JSON.parseObject(videoSleepSettingJsonStr, VideoSleepSetting.class);
        videoSleepSetting.setVehicleId(vehicleId);
        if (null == videoSleepSettingDao.getVideoSleepByVehicleId(vehicleId)) {
            videoSleepSetting.setCreateDataUsername(currentUsername);
            videoSleepSettingDao.saveVideoSleep(videoSleepSetting);
        } else {
            videoSleepSetting.setUpdateDataUsername(currentUsername);
            videoSleepSettingDao.updateVideoSleep(videoSleepSetting);
        }
        addVideoSettingLog(bindDTO, "休眠唤醒", ipAddress);
    }

    private void saveVideoRecordingSetting(BindDTO bindDTO, String currentUsername, String recordingSettingJsonStr) {
        if (StringUtils.isBlank(recordingSettingJsonStr)) {
            return;
        }
        String vehicleId = bindDTO.getId();
        RecordingSetting recordingSetting = JSON.parseObject(recordingSettingJsonStr, RecordingSetting.class);
        recordingSetting.setVehicleId(vehicleId);
        RecordingSetting videoRecordingByVehicleId = recordingSettingDao.getVedioRecordingSettingByVehicleId(vehicleId);
        // 如果数据库没有数据就是添加，有数据就是修改
        if (null == videoRecordingByVehicleId) {
            recordingSetting.setCreateDataUsername(currentUsername);
            recordingSettingDao.insertVedioRecordingParamSettings(recordingSetting);
        } else {
            recordingSetting.setUpdateDataUsername(currentUsername);
            recordingSettingDao.updateVedioRecordingById(recordingSetting);
        }
    }

    private void saveVideoAlarmParamSettingAndRecordLog(BindDTO bindDTO, String ipAddress, String currentUsername,
        String alarmParamsJsonStr) {
        if (StringUtils.isBlank(alarmParamsJsonStr)) {
            return;
        }
        List<AlarmParam> alarmParams = JSON.parseArray(alarmParamsJsonStr, AlarmParam.class);
        if (CollectionUtils.isEmpty(alarmParams)) {
            return;
        }
        String vehicleId = bindDTO.getId();
        Set<String> posSet = alarmParams.stream().map(AlarmParam::getPos).collect(Collectors.toSet());
        List<AlarmParameterSettingForm> alarmParametersList = recordingSettingDao.getAlarmParametersByPosList(posSet);
        List<AlarmParameterSettingForm> alarmParamSettings = new ArrayList<>();
        // 向storm发送的消息
        JSONObject stormParam = new JSONObject();
        alarmParams.forEach(alarmParam -> {
            // 根据报警类型和报警标识pos查出alarmType的集合，如插入视频信号丢失时需要插入32条数据（通道1——32视频信号丢失）
            String pos = alarmParam.getPos();
            List<AlarmParameterSettingForm> alarmParameters =
                alarmParametersList.stream().filter(obj -> obj.getPos().startsWith(pos)).collect(Collectors.toList());
            // 客车超员参数需向storm发送消息
            if ("129".equals(pos)) {
                stormParam.put("passengercarover", alarmParam.getParamValue());
            }
            alarmParameters.forEach(alarmParamSetting -> {
                alarmParamSetting.setVehicleId(vehicleId);
                alarmParamSetting.setParameterValue(alarmParam.getParamValue());
                alarmParamSetting.setAlarmPush(alarmParam.getAlarmPush());
                alarmParamSetting.setIgnore(alarmParam.getIgnore());
                alarmParamSetting.setCreateDataUsername(currentUsername);
                alarmParamSettings.add(alarmParamSetting);
            });

        });
        // 删除车下的所有视频报警参数类型然后再添加
        recordingSettingDao.deleteVedioAlarmParamByVehicleId(vehicleId);
        // 批量添加报警参数设置
        boolean flag = recordingSettingDao.addVedioAlarmParams(alarmParamSettings);
        addVideoSettingLog(bindDTO, "报警参数", ipAddress);
        if (!flag) {
            return;
        }
        // 查询非视频报警
        List<AlarmParameterSettingForm> otherAlarmParams = alarmSettingDao.findSettingByVid(vehicleId);
        int defaultType = 2;
        if (CollectionUtils.isNotEmpty(otherAlarmParams)) {
            alarmParamSettings.addAll(otherAlarmParams);
            defaultType = 0;
        }
        Map<String, String> monitorInfoMap = RedisHelper.hgetAll(RedisKeyEnum.MONITOR_INFO.of(bindDTO));
        if (monitorInfoMap == null) {
            return;
        }
        String deviceId = bindDTO.getDeviceId();
        // 推送设置存缓存
        WebSubscribeManager.getInstance().subAlarmSetting(vehicleId, deviceId, alarmParamSettings, defaultType);
        // 向storm推送消息
        stormParam.put("deviceId", deviceId);
        ZMQFencePub.pubChangeParam(stormParam.toJSONString());
        // 通知storm更新数据库数据
        ZMQFencePub.pubChangeFence("9");
        // 通知storm更新redis13分区缓存数据
        ZMQFencePub.pubChangeFence("12");
    }

    private void saveVideoChannelSettingAndRecordLog(BindDTO bindDTO, String ipAddress, String currentUsername,
        String videoChannelSettingsJsonStr) {
        if (StringUtils.isBlank(videoChannelSettingsJsonStr)) {
            return;
        }
        String vehicleId = bindDTO.getId();
        List<VideoChannelSetting> videoChannelSettings =
            JSON.parseArray(videoChannelSettingsJsonStr, VideoChannelSetting.class);
        // 删除历史设置的音视频通道
        videoChannelSettingDao.delete(vehicleId);
        videoChannelSettings.forEach(videoChannel -> {
            Boolean panoramic = videoChannel.getPanoramic();
            if (panoramic == null) {
                videoChannel.setPanoramic(false);
            }
            videoChannel.setVehicleId(vehicleId);
            videoChannel.setCreateDataUsername(currentUsername);
        });
        if (CollectionUtils.isNotEmpty(videoChannelSettings)) {
            videoChannelSettingDao.addVideoChannels(videoChannelSettings);
            addVideoSettingLog(bindDTO, "音视频通道", ipAddress);
        }
    }

    private void saveVideoSettingAndRecordLog(BindDTO bindDTO, String ipAddress, String currentUsername,
        String videoSettingJsonStr) {
        if (StringUtils.isBlank(videoSettingJsonStr)) {
            return;
        }
        String vehicleId = bindDTO.getId();
        List<VideoSetting> videoSettings = JSON.parseArray(videoSettingJsonStr, VideoSetting.class);
        // 如果videoSettings大于1，说明选择了参考车牌，删除以前的设置
        if (videoSettings.size() > 1) {
            videoSettingMapper.delete(vehicleId);
        }
        for (VideoSetting videoSetting : videoSettings) {
            videoSetting.setVehicleId(vehicleId);
            saveVideoSetting(videoSetting, currentUsername);
            addVideoSettingLog(bindDTO, "音视频参数", ipAddress);
        }
    }

    /**
     * 保存视频参数
     */
    private void saveVideoSetting(VideoSetting videoSetting, String currentUsername) {
        String vehicleId = videoSetting.getVehicleId();
        // 如果逻辑通道号为全部就删除其他的数据只添加一条
        if (videoSetting.getAllChannel() == 1) {
            videoSettingMapper.delete(vehicleId);
        } else {
            videoSettingMapper.deleteVideoParam(vehicleId, videoSetting.getLogicChannel());
            // 逻辑通道号为全部时logicChannel为0
            videoSettingMapper.deleteVideoParam(vehicleId, 0);
        }
        // 保存视频参数
        videoSetting.setCreateDataUsername(currentUsername);
        videoSettingMapper.saveVideoParam(videoSetting);
    }

    private void addVideoSettingLog(BindDTO bindDTO, String settingModule, String ipAddress) {
        String brand = bindDTO.getName();
        String msg = "监控对象(" + brand + ")_设置视频参数(" + settingModule + ")";
        Integer plateColorInt = bindDTO.getPlateColor();
        logSearchServiceImpl
            .addLog(ipAddress, msg, "3", "", brand, plateColorInt == null ? "" : plateColorInt.toString());
    }

    @Override
    public void sendVideoSetting(String vehicleIds) {
        Set<String> moIds = Arrays.stream(vehicleIds.split(",")).collect(Collectors.toSet());
        Map<String, BindDTO> bindInfoMap = VehicleUtil.batchGetBindInfosByRedis(moIds);
        StringBuilder message = new StringBuilder();
        String brand = "";
        String plateColor = "";
        for (String vid : moIds) {
            BindDTO bindDTO = bindInfoMap.get(vid);
            if (bindDTO == null) {
                continue;
            }
            // 音视频参数设置
            String paramType = "8103-75-76-77-79-7A";
            // 获取参数设置值
            Map<String, Object> videoParams = getVideoParams(vid);
            String deviceNumber = bindDTO.getDeviceNumber();
            String simCardNumber = bindDTO.getSimCardNumber();
            String deviceId = bindDTO.getDeviceId();
            String deviceType = bindDTO.getDeviceType();
            // 序列号
            Integer msgSno = DeviceHelper.getRegisterDevice(vid, deviceNumber);
            // 设备已经注册
            if (msgSno != null) {
                // 音视频参数设置下发
                wsVideoService.sendVideoSetting(videoParams, msgSno, simCardNumber, deviceId, deviceType);
                // 已下发
                int status = 4;
                // 绑定下发
                String paramId = setParameterStatus(msgSno, status, vid, paramType);
                SendParam sendParam = new SendParam();
                sendParam.setMsgSNACK(msgSno);
                sendParam.setParamId(paramId);
                sendParam.setVehicleId(vid);
                f3SendStatusProcessService.updateSendParam(sendParam, 1);
                // 设备未注册
            } else {
                int status = 5;
                msgSno = 0;
                // 绑定下发
                setParameterStatus(msgSno, status, vid, paramType);
            }
            brand = bindDTO.getName();
            Integer plateColorInt = bindDTO.getPlateColor();
            plateColor = plateColorInt == null ? "" : plateColorInt.toString();
            message.append("监控对象 : ").append(brand).append(" ( @").append(bindDTO.getOrgName())
                .append(" ) 音视频参数设置下发 <br/>");
        }
        String ipAddress = getIpAddress();
        if (moIds.size() == 1) {
            logSearchServiceImpl.addLog(ipAddress, message.toString(), "3", "", brand, plateColor);
        } else {
            // 记录日志
            logSearchServiceImpl.addLog(ipAddress, message.toString(), "3", "batch", "音视频参数设置下发");
        }
    }

    /**
     * 下发音视频参数数据
     */
    private String setParameterStatus(int msgSno, int status, String vehicleId, String paramType) {
        List<Directive> params = parameterDao.findParameterByType(vehicleId, null, paramType);
        if (params != null && !params.isEmpty()) {
            List<String> paramIds = new ArrayList<>();
            for (Directive param : params) {
                paramIds.add(param.getId());
            }
            // 修改流水号和状态
            // 1 : 下发未回应
            parameterDao.updateMsgSNAndNameById(paramIds, msgSno, status, null, 1);
            return paramIds.get(0);
        } else {
            // 新增下发参数数据
            DirectiveForm form = new DirectiveForm();
            form.setDownTime(new Date());
            form.setMonitorObjectId(vehicleId);
            form.setStatus(status);
            form.setParameterType(paramType);
            form.setSwiftNumber(msgSno);
            form.setReplyCode(1);
            // 新增
            parameterDao.addDirective(form);
            return form.getId();
        }
    }

    /**
     * 删除车辆报警推送设置缓存
     */
    private void deleteVehicleAlarmCaches(String vehicleId, String deviceId) {
        RedisKey alarmPushSetDeviceId = HistoryRedisKeyEnum.ALARM_PUSH_SET_DEVICE_ID.of(deviceId);
        if (RedisHelper.isContainsKey(alarmPushSetDeviceId)) {
            RedisHelper.delete(alarmPushSetDeviceId);
            WebSubscribeManager.getInstance().sendMsgToAll(alarmPushSetDeviceId.get(), ConstantUtil.WEB_ALARM_REMOVE);
        }
        RedisKey alarmPushSetMonitorId = HistoryRedisKeyEnum.ALARM_PUSH_SET_MONITOR_ID.of(vehicleId);
        if (RedisHelper.isContainsKey(alarmPushSetMonitorId)) {
            RedisHelper.delete(alarmPushSetMonitorId);
        }
        // 查询808非视频报警
        List<AlarmParameterSettingForm> otherAlarmParams = alarmSettingDao.findSettingByVid(vehicleId);
        if (CollectionUtils.isNotEmpty(otherAlarmParams)) {
            // 推送设置存缓存
            WebSubscribeManager.getInstance().subAlarmSetting(vehicleId, deviceId, otherAlarmParams, 1);
        }
    }

    @Override
    public void delete(String vehicleId) throws Exception {
        StringBuilder message = deleteParam(vehicleId);
        BindDTO bindDTO = VehicleUtil.getBindInfoByRedis(vehicleId);
        String brand = "";
        String color = "";
        if (bindDTO != null) {
            brand = bindDTO.getName();
            Integer plateColorInt = bindDTO.getPlateColor();
            color = plateColorInt == null ? "" : plateColorInt.toString();
        }
        if (message != null) {
            ZMQFencePub.pubChangeFence("20");
            ZMQFencePub.pubChangeFence("9");
            ZMQFencePub.pubChangeFence("12");
            logSearchServiceImpl.addLog(getIpAddress(), message.toString(), "3", "", brand, color);
        }
    }

    @Override
    public void deleteMore(String vehicleIds) {
        StringBuilder message = deleteParam(vehicleIds);
        if (message != null) {
            ZMQFencePub.pubChangeFence("20");
            logSearchServiceImpl.addLog(getIpAddress(), message.toString(), "3", "batch", "批量恢复默认视频参数设置");
        }
    }

    private StringBuilder deleteParam(String ids) {
        Set<String> moIds = Arrays.stream(ids.split(",")).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(moIds)) {
            return null;
        }
        Map<String, BindDTO> bindInfoMap = VehicleUtil.batchGetBindInfosByRedis(moIds);
        StringBuilder message = new StringBuilder();
        for (String vehicleId : moIds) {
            BindDTO bindDTO = bindInfoMap.get(vehicleId);
            // 删除音视频参数设置
            videoSettingMapper.delete(vehicleId);
            // 删除音视频通道
            videoChannelSettingDao.delete(vehicleId);
            // 删除音视频录像参数
            recordingSettingDao.deleteByVehicleId(vehicleId);
            // 删除音视频休眠唤醒设置
            videoSleepSettingDao.delete(vehicleId);
            // 删除音视频报警参数
            recordingSettingDao.deleteVedioAlarmParamByVehicleId(vehicleId);
            // 删除车辆报警推送设置缓存
            deleteVehicleAlarmCaches(vehicleId, bindDTO.getDeviceId());
            // 删除下发表设置
            parameterDao.deleteByVechicleidType(vehicleId, "8103-75-76-77-79-7A");
            message.append("监控对象 : ").append(bindDTO.getName()).append(" ( @").append(bindDTO.getOrgName())
                .append(" ) 恢复默认视频参数设置 <br/>");
        }
        return message;
    }

    /**
     * 信息配置解绑: 批量删除音视频参数
     * @param monitorMap 监控对象缓存信息
     */
    public void deleteBatchParam(Set<String> monitorIds, Map<String, JSONObject> monitorMap) {
        // 删除音视频参数设置
        videoSettingMapper.deleteBatch(monitorIds);
        // 删除音视频通道
        videoChannelSettingDao.deleteBatch(monitorIds);
        // 删除音视频录像参数
        recordingSettingDao.deleteByMonitorIds(monitorIds);
        // 删除音视频休眠唤醒设置
        videoSleepSettingDao.deleteBatch(monitorIds);
        // 删除车辆报警推送设置缓存
        RedisHelper.batchDelete(HistoryRedisKeyEnum.ALARM_PUSH_SET_MONITOR_ID.ofs(monitorIds));
        List<RedisKey> alarmPushSetDeviceIdRedisKeys = monitorIds.stream()
            .map(moId -> HistoryRedisKeyEnum.ALARM_PUSH_SET_DEVICE_ID.of(monitorMap.get(moId).getString("deviceId")))
            .collect(Collectors.toList());
        RedisHelper.batchDelete(alarmPushSetDeviceIdRedisKeys);
        alarmPushSetDeviceIdRedisKeys.forEach(
            redisKey -> WebSubscribeManager.getInstance().sendMsgToAll(redisKey.get(), ConstantUtil.WEB_ALARM_REMOVE));
    }

    @EventListener
    public void updateVehicleUnbound(ConfigUnBindEvent unBindEvent) {
        if (Objects.equals("update", unBindEvent.getOperation())) {
            return;
        }
        Set<String> monitorIds = new HashSet<>();
        Map<String, JSONObject> monitorMap =
            new HashMap<>(CommonUtil.ofMapCapacity(unBindEvent.getUnbindList().size()));
        JSONObject jsonObject;
        for (BindDTO bindDTO : unBindEvent.getUnbindList()) {
            monitorIds.add(bindDTO.getId());
            jsonObject = new JSONObject();
            jsonObject.put("deviceId", bindDTO.getDeviceId());
            monitorMap.put(bindDTO.getId(), jsonObject);
        }
        deleteBatchParam(monitorIds, monitorMap);
    }

    @Override
    public JsonResultBean getVideoParam(String vehicleId, Integer logicChannel) {
        return new JsonResultBean(videoSettingMapper.getVideoParamByVehicleIdAndLogicChannel(vehicleId, logicChannel));
    }

    /**
     * 保存单个通道设置
     */
    @Override
    public void saveVideoParam(VideoSetting videoSetting) {
        saveVideoSetting(videoSetting, SystemHelper.getCurrentUsername());
        BindDTO bindDTO = VehicleUtil.getBindInfoByRedis(videoSetting.getVehicleId());
        addVideoSettingLog(bindDTO, "音视频参数", getIpAddress());
    }

    @Override
    public JsonResultBean getVideoSleep(String vehicleId) throws Exception {
        return videoSleepService.getVideoSleep(vehicleId);
    }

    @Override
    public void addVideoChannelParam(String terminalTypeId, String monitorId) {
        if (StringUtils.isBlank(terminalTypeId) || StringUtils.isBlank(monitorId)) {
            return;
        }
        /* 1.根据终端类型id查询设置通道ids */
        TerminalTypeInfo info = terminalTypeDao.getTerminalTypeInfo(terminalTypeId);
        if (info == null) {
            return;
        }
        String channels = info.getDeviceChannelId();
        /* 2.若该设备类型有设置通道则根据通道ids查找设置通道参数 */
        if (StringUtils.isBlank(channels)) {
            return;
        }
        List<String> channelsList = Arrays.asList(channels.split(","));
        List<VideoChannelSetting> videoChannelSettings = terminalTypeDao.getTerminalTypeChannelInfo(channelsList);
        /* 3.设置监控对象音视频通道信息 */
        // 删除历史设置的音视频通道
        videoChannelSettingDao.delete(monitorId);
        String currentUsername = SystemHelper.getCurrentUsername();
        // 添加修改音视频通道
        videoChannelSettings.forEach(videoChannel -> {
            Boolean panoramic = videoChannel.getPanoramic();
            if (panoramic == null) {
                videoChannel.setPanoramic(false);
            }
            videoChannel.setVehicleId(monitorId);
            videoChannel.setCreateDataUsername(currentUsername);
        });
        if (CollectionUtils.isNotEmpty(videoChannelSettings)) {
            videoChannelSettingDao.addVideoChannels(videoChannelSettings);
        }
    }

    @Override
    public void addBatchVideoChannelParam(Map<String, String> vehicleBindChannelNum) {
        if (MapUtils.isNotEmpty(vehicleBindChannelNum)) {
            // 缓存getDeviceChannelId
            Map<String, List<VideoChannelSetting>> deviceChannelMap = new HashMap<>(32);
            // 1. 获取终端型号ID
            Set<String> terminalTypeIds = new HashSet<>(vehicleBindChannelNum.values());
            // 监控对象ID
            Set<String> monitorIds = new HashSet<>(vehicleBindChannelNum.keySet());
            // 2. 查询出终端型号数据
            List<TerminalTypeInfo> terminalTypeInfoList = terminalTypeDao.findTerminalTypeInfo(terminalTypeIds);
            if (CollectionUtils.isNotEmpty(terminalTypeInfoList)) {
                // 3.根据id进行分组
                Map<String, List<TerminalTypeInfo>> terminalTypeInfoMap =
                    terminalTypeInfoList.stream().collect(Collectors.groupingBy(TerminalTypeInfo::getId));
                // 4.批量删除历史设置的音视频通道
                videoChannelSettingDao.deleteMoreByMonitorIds(monitorIds);
                // 5.遍历数据
                List<VideoChannelSetting> videoChannelSettingList = new ArrayList<>();

                String currentUsername = SystemHelper.getCurrentUsername();
                VideoChannelSetting newVideoChannelSetting;
                for (Map.Entry<String, String> map : vehicleBindChannelNum.entrySet()) {
                    String monitorId = map.getKey();
                    String terminalId = map.getValue();
                    List<TerminalTypeInfo> terminalTypeInfo = terminalTypeInfoMap.get(terminalId);
                    if (CollectionUtils.isEmpty(terminalTypeInfo)) {
                        continue;
                    }

                    String deviceChannelId = terminalTypeInfo.get(0).getDeviceChannelId();
                    if (StringUtils.isEmpty(deviceChannelId)) {
                        continue;
                    }

                    // 先从内存中查询数据, 如果不存在, 则去数据库中查询, 然后维护到内存中
                    List<VideoChannelSetting> videoChannelSettings = deviceChannelMap.get(deviceChannelId);
                    if (CollectionUtils.isEmpty(videoChannelSettings)) {
                        List<String> channelsList = Arrays.asList(deviceChannelId.split(","));
                        videoChannelSettings = terminalTypeDao.getTerminalTypeChannelInfo(channelsList);
                        if (CollectionUtils.isEmpty(videoChannelSettings)) {
                            continue;
                        }
                        deviceChannelMap.put(deviceChannelId, videoChannelSettings);
                    }

                    // 缓存查询出来的数据
                    // 7.添加修改音视频通道
                    for (VideoChannelSetting videoChannel : videoChannelSettings) {
                        if (null == videoChannel.getPanoramic()) {
                            videoChannel.setPanoramic(Boolean.FALSE);
                        }
                        newVideoChannelSetting = new VideoChannelSetting();
                        BeanUtils.copyProperties(videoChannel, newVideoChannelSetting);
                        newVideoChannelSetting.setId(UUID.randomUUID().toString());
                        newVideoChannelSetting.setVehicleId(monitorId);
                        newVideoChannelSetting.setCreateDataUsername(currentUsername);
                        videoChannelSettingList.add(newVideoChannelSetting);
                    }

                }
                if (CollectionUtils.isNotEmpty(videoChannelSettingList)) {
                    DbUtils.partitionUpdate(videoChannelSettingList, videoChannelSettingDao::addVideoChannels);
                    ZMQFencePub.pubChangeFence("20");
                }
            }
        }
    }
}

package com.zw.ws.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.enmu.ProtocolEnum;
import com.zw.platform.domain.realTimeVideo.RecordingSetting;
import com.zw.platform.domain.realTimeVideo.VideoChannelSetting;
import com.zw.platform.domain.realTimeVideo.VideoSetting;
import com.zw.platform.domain.realTimeVideo.VideoSleepSetting;
import com.zw.platform.domain.vas.alram.form.AlarmParameterSettingForm;
import com.zw.platform.push.controller.SubscibeInfo;
import com.zw.platform.push.controller.SubscibeInfoCache;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.MsgUtil;
import com.zw.protocol.msg.t808.T808Message;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import com.zw.ws.entity.t808.parameter.ParamItem;
import com.zw.ws.entity.t808.parameter.T808_0x8103;
import com.zw.ws.entity.t808.video.ChannelParam;
import com.zw.ws.entity.t808.video.ChannelSetting;
import com.zw.ws.entity.t808.video.ImageParam;
import com.zw.ws.entity.t808.video.SpecialAlarmParam;
import com.zw.ws.entity.t808.video.VideoOverallParam;
import com.zw.ws.entity.t808.video.VideoPartParam;
import com.zw.ws.entity.t808.video.VideoPartSetting;
import com.zw.ws.entity.t808.video.WakeUpParam;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by lifudong. 音视频参数下发
 */
@Component
public class WsVideoService {
    private static final Logger logger = LogManager.getLogger(WsVideoService.class);

    /**
     * 参数ID
     */
    private static final int VIDEOALL_SETTING_ID = 0x0075; // 音视频全部通道参数设置

    private static final int VIDEOPART_SETTING_ID = 0x0077; // 音视频单独通道参数设置

    private static final int CHANNEL_SETTING_ID = 0x0076; // 音视频通道列表设置

    private static final int RECORD_WARNING_ID = 0x0079; // 录像报警

    private static final int IGNORE_WARNING_ID = 0x007A; // 报警屏蔽设置

    private static final int IMAGE_WARNING_ID = 0x007B; // 图像分析报警参数设置

    private static final int WAKEUP_SETTING_ID = 0x007C; // 休眠唤醒模式设置

    /**
     * 下发音视频参数
     */
    public void sendVideoSetting(Map<String, Object> videoParams, Integer msgSno, String simCardNumber,
        String deviceId, String deviceType) {
        if (MapUtils.isEmpty(videoParams)) {
            return;
        }
        T808_0x8103 parameter = new T808_0x8103();
        List<ParamItem> paramItemList = new ArrayList<>();
        int count = 0;
        //音视频参数设置
        count = setMediaParams(videoParams, paramItemList, count);
        //音视频通道列表设置
        if (ProtocolEnum.T808_2011_1078.getDeviceType().equals(deviceType)) {
            //1078报批稿需要单独处理
            count = setMediaChannels1078(videoParams, paramItemList, count);
        } else {
            count = setMediaChannels(videoParams, paramItemList, count);
        }
        //报警参数设置
        count = setMediaAlarms(videoParams, paramItemList, count);
        //特殊报警录像参数设置
        count = setMediaRecordings(videoParams, paramItemList, count);
        //终端休眠唤醒模式设置
        count = setMediaSleeps(videoParams, paramItemList, count);
        try {
            parameter.setParametersCount(count);
            parameter.setPackageSum(count);
            parameter.setParamItems(paramItemList);
            // 订阅推送消息
            SubscibeInfo info = new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, msgSno,
                ConstantUtil.T808_DEVICE_GE_ACK);
            SubscibeInfoCache.getInstance().putTable(info);

            T808Message message =
                MsgUtil.get808Message(simCardNumber, ConstantUtil.T808_SET_PARAM, msgSno, parameter, deviceType);
            WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_SET_PARAM, deviceId);
        } catch (Exception e) {
            logger.error("WsVideoService类异常" + e);
        }
    }

    private int setMediaSleeps(Map<String, Object> videoParams, List<ParamItem> paramItemList, int count) {
        Object videoSleepObj = videoParams.get("videoSleep");
        if (videoSleepObj == null) {
            return count;
        }
        VideoSleepSetting videoSleep = (VideoSleepSetting) videoSleepObj;
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> videoSleepMap = mapper.convertValue(videoSleep, Map.class);
        String key;
        Object value;
        for (Map.Entry<String, Object> entry : videoSleepMap.entrySet()) {
            key = entry.getKey();
            value = entry.getValue();
            if ((key.contains("wakeupTime") && key.length() == 11) || key.contains("wakeupClose")) {
                if (value == null || "".equals(value)) {
                    entry.setValue("0000");
                } else {
                    String str = String.valueOf(value);
                    str = str.replace(":", "");
                    entry.setValue(str);
                }
            }
        }
        WakeUpParam wakeUpParam  = new WakeUpParam();
        try {
            org.apache.commons.beanutils.BeanUtils.populate(wakeUpParam, videoSleepMap);
        } catch (Exception e) {
            logger.error("视频参数设置参数下发时实体复制异常!", e);
        }
        // bit2
        String wakeupMode = videoSleep.getWakeupHandSign().toString();
        // bit1
        wakeupMode += videoSleep.getWakeupTimeSign().toString();
        // bit0
        wakeupMode += videoSleep.getWakeupConditionSign().toString();
        wakeUpParam.setWakeupMode(Integer.parseInt(wakeupMode, 2));
        //单独视频通道参数设置
        ParamItem paramItem = new ParamItem();
        paramItem.setParamId(WAKEUP_SETTING_ID);
        paramItem.setParamLength(20);
        paramItem.setParamValue(wakeUpParam);
        paramItemList.add(paramItem);
        count++;
        return count;
    }

    private int setMediaRecordings(Map<String, Object> videoParams, List<ParamItem> paramItemList, int count) {
        Object videoRecordingObj = videoParams.get("vedioRecording");
        if (videoRecordingObj == null) {
            return count;
        }
        RecordingSetting videoRecording = (RecordingSetting) videoRecordingObj;
        SpecialAlarmParam specialAlarmParam = new SpecialAlarmParam();
        BeanUtils.copyProperties(videoRecording, specialAlarmParam);
        ParamItem specialParamItem = new ParamItem();
        specialParamItem.setParamId(RECORD_WARNING_ID);
        specialParamItem.setParamLength(3);
        specialParamItem.setParamValue(specialAlarmParam);
        paramItemList.add(specialParamItem);
        count++;
        return count;
    }

    private int setMediaAlarms(Map<String, Object> videoParams, List<ParamItem> paramItemList, int count) {
        Object videoAlarmObj = videoParams.get("vedioAlarms");
        if (videoAlarmObj == null) {
            return count;
        }
        List<AlarmParameterSettingForm> videoAlarms =
            JSON.parseArray(JSON.toJSONString(videoAlarmObj), AlarmParameterSettingForm.class);
        if (CollectionUtils.isEmpty(videoAlarms)) {
            return count;
        }
        String[] str = new String[32];
        ImageParam imageParam = new ImageParam();
        for (AlarmParameterSettingForm videoAlarm : videoAlarms) {
            if ("视频信号丢失".equals(videoAlarm.getName())) {
                // bit0
                str[6] = String.valueOf(videoAlarm.getAlarmPush() == -1 ? 1 : 0); 
            } else if ("视频信号遮挡".equals(videoAlarm.getName())) {
                // bit1
                str[5] = String.valueOf(videoAlarm.getAlarmPush() == -1 ? 1 : 0); 
            } else if ("存储器故障".equals(videoAlarm.getName())) {
                // bit2
                str[4] = String.valueOf(videoAlarm.getAlarmPush() == -1 ? 1 : 0); 
            } else if ("其他视频设备故障".equals(videoAlarm.getName())) {
                // bit3
                str[3] = String.valueOf(videoAlarm.getAlarmPush() == -1 ? 1 : 0); 
            } else if ("客车超员".equals(videoAlarm.getName())) {
                // bit4
                str[2] = String.valueOf(videoAlarm.getAlarmPush() == -1 ? 1 : 0); 
                // int value = 20; // 默认值
                Integer value = null;
                if (StringUtils.isNotBlank(videoAlarm.getParameterValue())) {
                    value = Integer.valueOf(videoAlarm.getParameterValue());
                }
                imageParam.setLoadPeople(value);
            } else if ("异常驾驶行为".equals(videoAlarm.getName())) {
                // bit5
                str[1] = String.valueOf(videoAlarm.getAlarmPush() == -1 ? 1 : 0); 
                // int value = 5; // 默认值
                Integer value = null;
                if (StringUtils.isNotBlank(videoAlarm.getParameterValue())) {
                    value = Integer.valueOf(videoAlarm.getParameterValue());
                }
                imageParam.setTiredThreshold(value);
            } else if ("报警存储超限".equals(videoAlarm.getName())) {
                // bit6
                str[0] = String.valueOf(videoAlarm.getAlarmPush() == -1 ? 1 : 0); 
            }
        }
        String binaryString = StringUtils.join(str);
        //屏蔽设置
        ParamItem ignoreParamItem = new ParamItem();
        ignoreParamItem.setParamId(IGNORE_WARNING_ID);
        ignoreParamItem.setParamLength(4);
        ignoreParamItem.setParamValue(Integer.parseInt(binaryString, 2));
        paramItemList.add(ignoreParamItem);
        //图像分析报警参数设置
        ParamItem imageParamItem = new ParamItem();
        imageParamItem.setParamId(IMAGE_WARNING_ID);
        imageParamItem.setParamLength(2);
        imageParamItem.setParamValue(imageParam);
        paramItemList.add(imageParamItem);
        count += 2;
        return count;
    }

    private int setMediaChannels(Map<String, Object> videoParams, List<ParamItem> paramItemList, int count) {
        Object videoChannelsObj = videoParams.get("videoChannels");
        if (videoChannelsObj == null) {
            return count;
        }
        List<VideoChannelSetting> videoChannels =
            JSON.parseArray(JSON.toJSONString(videoChannelsObj), VideoChannelSetting.class);
        if (CollectionUtils.isEmpty(videoChannels)) {
            return count;
        }
        ChannelParam channelParam = new ChannelParam();
        JSONArray contrasts = new JSONArray();
        int audioSum = 0;
        int videoSum = 0;
        int avSum = 0;
        for (VideoChannelSetting videoChannel : videoChannels) {
            if (videoChannel.getChannelType() == 0) {
                avSum += 1;
            } else if (videoChannel.getChannelType() == 1) {
                audioSum += 1;
            } else if (videoChannel.getChannelType() == 2) {
                videoSum += 1;
            }
            ChannelSetting channelSetting = new ChannelSetting();
            BeanUtils.copyProperties(videoChannel, channelSetting);
            contrasts.add(channelSetting);
        }
        channelParam.setAudioSum(audioSum);
        channelParam.setVideoSum(videoSum);
        channelParam.setAudioVideoSum(avSum);
        channelParam.setContrasts(contrasts);
        //通道参数设置
        ParamItem paramItem = new ParamItem();
        paramItem.setParamId(CHANNEL_SETTING_ID);
        paramItem.setParamLength(3 + 4 * (audioSum + videoSum + avSum));
        paramItem.setParamValue(channelParam);
        paramItemList.add(paramItem);
        count++;
        return count;
    }

    /**
     * 1078报批稿 0x0076
     */
    private int setMediaChannels1078(Map<String, Object> videoParams, List<ParamItem> paramItemList, int count) {
        Object videoChannelsObj = videoParams.get("videoChannels");
        if (videoChannelsObj == null) {
            return count;
        }
        List<VideoChannelSetting> videoChannels =
            JSON.parseArray(JSON.toJSONString(videoChannelsObj), VideoChannelSetting.class);
        if (CollectionUtils.isEmpty(videoChannels)) {
            return count;
        }
        ChannelParam channelParam = new ChannelParam();
        JSONArray contrasts = new JSONArray();
        int audioSum = 0;
        int videoSum = 0;
        for (VideoChannelSetting videoChannel : videoChannels) {
            if (videoChannel.getChannelType() == 1) {
                audioSum += 1;
            } else if (videoChannel.getChannelType() == 2) {
                videoSum += 1;
            }
            ChannelSetting channelSetting = new ChannelSetting();
            BeanUtils.copyProperties(videoChannel, channelSetting);
            contrasts.add(channelSetting);
        }
        channelParam.setAudioSum(audioSum);
        channelParam.setVideoSum(videoSum);
        channelParam.setContrasts(contrasts);
        //通道参数设置
        ParamItem paramItem = new ParamItem();
        paramItem.setParamId(CHANNEL_SETTING_ID);
        paramItem.setParamLength(2 + 3 * (audioSum + videoSum));
        paramItem.setParamValue(channelParam);
        paramItemList.add(paramItem);
        count++;
        return count;
    }

    private int setMediaParams(Map<String, Object> videoParams, List<ParamItem> paramItemList, int count) {
        Object videoSettingsObj = videoParams.get("videoSettings");
        if (videoSettingsObj == null) {
            return count;
        }
        List<VideoSetting> videoSettings =
            JSONArray.parseArray(JSON.toJSONString(videoSettingsObj), VideoSetting.class);
        if (CollectionUtils.isEmpty(videoSettings)) {
            return count;
        }
        if (videoSettings.size() == 1 && videoSettings.get(0).getAllChannel() == 1) {
            VideoSetting videoSetting = videoSettings.get(0);
            VideoOverallParam videoParam = new VideoOverallParam();
            videoParam.setRealCodeType(videoSetting.getRealCodeSchema());
            videoParam.setSaveCodeType(videoSetting.getSaveCodeSchema());
            BeanUtils.copyProperties(videoSetting, videoParam);
            //终端音视频参数设置
            ParamItem paramItem = new ParamItem();
            paramItem.setParamId(VIDEOALL_SETTING_ID);
            paramItem.setParamLength(21);
            paramItem.setParamValue(videoParam);
            paramItemList.add(paramItem);
        } else {
            VideoPartParam videoParam = new VideoPartParam();
            JSONArray settings = new JSONArray();
            for (VideoSetting videoSetting : videoSettings) {
                VideoPartSetting videoPartSetting = new VideoPartSetting();
                BeanUtils.copyProperties(videoSetting, videoPartSetting);
                settings.add(videoPartSetting);
            }
            videoParam.setWaySum(videoSettings.size());
            videoParam.setVideoSettings(settings);
            //单独视频通道参数设置
            ParamItem paramItem = new ParamItem();
            paramItem.setParamId(VIDEOPART_SETTING_ID);
            paramItem.setParamLength(1 + 21 * videoSettings.size());
            paramItem.setParamValue(videoParam);
            paramItemList.add(paramItem);
        }
        count++;
        return count;
    }
}

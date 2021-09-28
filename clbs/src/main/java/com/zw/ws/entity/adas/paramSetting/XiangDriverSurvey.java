package com.zw.ws.entity.adas.paramSetting;

import com.zw.adas.domain.define.setting.AdasAlarmParamSetting;
import com.zw.adas.domain.define.setting.query.AdasParamSettingForm;
import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description:主动安全参数(湘标) (驾驶员状态监测参数设置)
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class XiangDriverSurvey extends PublicParameters implements T808MsgBody {

    /**
     * 报警使能
     */
    private Long alarmEnable = 0L;

    /**
     * 事件使能
     */
    private Long eventEnable = 3L;

    /**
     * 吸烟报警判断时间间隔
     */
    private Integer smokingDecideTime = 0xFFFF;

    /**
     * 接打手持电话报警判断时间间隔
     */
    private Integer pickUpDecideTime = 0xFFFF;

    /**
     * 预留
     */
    private byte[] keep1 = new byte[4];

    /**
     * 疲劳驾驶报警分级速度阈值
     */
    private Integer fatigueSpeed = 0xff;

    /**
     * 疲劳驾驶报警前后视频录制时间
     */
    private Integer fatigueVideoTime = 0xff;

    /**
     * 疲劳驾驶报警拍照张数
     */
    private Integer fatigueCameraNum = 0xff;

    /**
     * 疲劳驾驶报警拍照间隔时间
     */
    private Integer fatigueCameraTime = 0xff;

    /**
     * 接打电话报警前后视频录制时间
     */
    private Integer pickUpVideoTime = 0xff;

    /**
     * 接打电话报警拍驾驶员完整面部特征照片张数
     */
    private Integer pickUpCameraNum = 0xff;

    /**
     * 接打电话报警拍驾驶员完整面部特征照片间隔时间
     */
    private Integer pickUpCameraTime = 0xff;

    /**
     * 抽烟报警前后视频录制时间
     */
    private Integer smokingVideoTime = 0xff;

    /**
     * 抽烟报警拍驾驶员完整面部特征照片张数
     */
    private Integer smokingCameraNum = 0xff;

    /**
     * 抽烟报警拍驾驶员完整面部特征照片间隔时间
     */
    private Integer smokingCameraTime = 0xff;

    /**
     * （川） 长时间不目视前方
     * <p>
     * （冀）分神驾驶
     * <p>
     * 前后视频录制时间
     */
    private Integer attentionVideoTime = 0xff;

    /**
     * （川） 长时间不目视前方
     * <p>
     * （冀）分神驾驶
     * <p>
     * 报警拍照张数
     */
    private Integer attentionCameraNum = 0xff;

    /**
     * （川） 长时间不目视前方
     * <p>
     * （冀）分神驾驶
     * <p>
     * 报警拍照间隔时间
     */
    private Integer attentionCameraTime = 0xff;

    /**
     * （川）未检出到驾驶员
     * <p>
     * （冀）驾驶行为异常
     * <p>
     * 视频录制时间
     */
    private Integer driveDeedVideoTime = 0xff;

    /**
     * （川）未检出到驾驶员
     * <p>
     * （冀）驾驶行为异常
     * <p>
     * 抓拍照片张数
     */
    private Integer driveDeedCameraNum = 0xff;

    /**
     * （川）未检出到驾驶员
     * <p>
     * （冀）驾驶行为异常
     * <p>
     * 拍照间隔
     */
    private Integer driveDeedCameraTime = 0xff;

    /**
     * 驾驶员身份识别触发
     */
    private Integer driveDeedType = 0xff;

    /**
     * 预留
     */
    private byte[] keep2 = new byte[14];
    /**
     * 未系安全带驾驶报警前后视频录制时间
     */
    private Integer safetyBeltVideoTime = 0xff;
    /**
     * 未系安全带驾驶报警拍照张数
     */
    private Integer safetyBeltCameraNum = 0xff;
    /**
     * 未系安全带驾驶报警拍照时间间隔
     */
    private Integer safetyBeltCameraTime = 0xff;
    /**
     * 生理疲劳联动上传主码流视频通道
     */
    private Integer fatigueStreamChannelNum = 0;
    /**
     * 生理疲劳联动上传子码流视频通道
     */
    private Integer fatigueSubStreamChannelNum = 0;
    /**
     * 生理疲劳联动抓拍通道
     */
    private Integer fatigueCaptureChannelNum = 0;
    /**
     * 拨打电话联动上传主码流视频通道
     */
    private Integer pickUpStreamChannelNum = 0;
    /**
     * 拨打电话联动上传子码流视频通道
     */
    private Integer pickUpSubStreamChannelNum = 0;
    /**
     * 拨打电话联动抓拍通道
     */
    private Integer pickUpCaptureChannelNum = 0;
    /**
     * 抽烟联动上传主码流视频通道
     */
    private Integer smokingStreamChannelNum = 0;
    /**
     * 抽烟联动上传子码流视频通道
     */
    private Integer smokingSubStreamChannelNum = 0;
    /**
     * 抽烟联动抓拍通道
     */
    private Integer smokingCaptureChannelNum = 0;
    /**
     * 不目视前方联动上传主码流视频通道
     */
    private Integer attentionStreamChannelNum = 0;
    /**
     * 不目视前方联动上传子码流视频通道
     */
    private Integer attentionSubStreamChannelNum = 0;
    /**
     * 不目视前方联动抓拍通道
     */
    private Integer attentionCaptureChannelNum = 0;
    /**
     * 摄像头偏离驾驶位联动上传主码流视频通道
     */
    private Integer driveStreamChannelNum = 0;
    /**
     * 摄像头偏离驾驶位联动上传子码流视频通道
     */
    private Integer driveSubStreamChannelNum = 0;
    /**
     * 摄像头偏离驾驶位联动抓拍通道
     */
    private Integer driveCaptureChannelNum = 0;
    /**
     * 玩手机联动上传主码流视频通道
     */
    private Integer playPhoneStreamChannelNum = 0;
    /**
     * 玩手机联动上传子码流视频通道
     */
    private Integer playPhoneSubStreamChannelNum = 0;
    /**
     * 玩手机联动抓拍通道
     */
    private Integer playPhoneCaptureChannelNum = 0;
    /**
     * 未系安全带联动上传主码流视频通道
     */
    private Integer safetyBeltStreamChannelNum = 0;
    /**
     * 未系安全带联动上传子码流视频通道
     */
    private Integer safetyBeltSubStreamChannelNum = 0;
    /**
     * 未系安全带联动抓拍通道
     */
    private Integer safetyBeltCaptureChannelNum = 0;
    /**
     * 报警使能顺序
     */

    private static Map<String, Integer> alarmEnableMap = new HashMap<>();
    /**
     * 报警参数
     */
    private static Map<String, List<String>> alarmParamMap = new HashMap();
    /**
     * 事件使能
     */
    private static Map<String, Integer> eventEnableMap = new HashMap();

    static {

        //报警使能顺序维护
        alarmEnableMap.put("276501", 0);
        alarmEnableMap.put("276502", 2);
        alarmEnableMap.put("276503", 4);
        alarmEnableMap.put("276504", 6);
        alarmEnableMap.put("276505", 8);
        alarmEnableMap.put("276506", 16);
        alarmEnableMap.put("276507", 18);
        alarmEnableMap.put("276512", 24);
        alarmEnableMap.put("276510", 25);
        //报警参数字段顺序维护
        alarmParamMap.put("276501", Arrays
            .asList("fatigueStreamChannelNum", "fatigueSubStreamChannelNum", "fatigueCaptureChannelNum",
                "fatigueCameraTime", "fatigueCameraNum", "fatigueVideoTime"));
        alarmParamMap.put("276502", Arrays
            .asList("pickUpStreamChannelNum", "pickUpSubStreamChannelNum", "pickUpCaptureChannelNum",
                "pickUpCameraTime", "pickUpCameraNum", "pickUpVideoTime", "pickUpDecideTime"));
        alarmParamMap.put("276503", Arrays
            .asList("smokingStreamChannelNum", "smokingSubStreamChannelNum", "smokingCaptureChannelNum",
                "smokingCameraTime", "smokingCameraNum", "smokingVideoTime", "smokingDecideTime"));

        alarmParamMap.put("276504", Arrays
            .asList("attentionStreamChannelNum", "attentionSubStreamChannelNum", "attentionCaptureChannelNum",
                "attentionCameraTime", "attentionCameraNum", "attentionVideoTime"));

        alarmParamMap.put("276505", Arrays
            .asList("driveStreamChannelNum", "driveSubStreamChannelNum", "driveCaptureChannelNum",
                "driveDeedCameraTime", "driveDeedCameraNum", "driveDeedVideoTime"));
        alarmParamMap.put("276506",
            Arrays.asList("playPhoneStreamChannelNum", "playPhoneSubStreamChannelNum", "playPhoneCaptureChannelNum"));
        alarmParamMap.put("276507", Arrays
            .asList("safetyBeltStreamChannelNum", "safetyBeltSubStreamChannelNum", "safetyBeltCaptureChannelNum",
                "safetyBeltCameraTime", "safetyBeltCameraNum", "safetyBeltVideoTime"));
        //驾驶员更换事件（附带驾驶员身份识别触发）
        alarmParamMap.put("276509", Arrays.asList("driveDeedType"));
        //事件使能顺序维护
        eventEnableMap.put("276509", 0);
        eventEnableMap.put("276508", 1);
    }

    public XiangDriverSurvey(AdasParamSettingForm paramSettingForm) {
        super(paramSettingForm.getCommonParamSetting());
        for (AdasAlarmParamSetting paramSetting : paramSettingForm.getAdasAlarmParamSettings()) {
            String functionId = paramSetting.getRiskFunctionId().toString();
            //组装报警使能
            handelAlarmEnable(paramSetting, functionId);
            //组装事件使能
            handleEventEnable(paramSetting, functionId);
            //组装报警事件参数设置
            handelAlarmParam(paramSetting, functionId);

        }
    }

    private void handleEventEnable(AdasAlarmParamSetting paramSetting, String key) {
        Integer position = eventEnableMap.get(key);
        if (position == null) {
            return;
        }

        if (isDriverChange(key)) {
            eventEnable = calBinaryData(eventEnable, paramSetting.getDriverChangeEnable(), position);
            return;
        }
        if (isInitiativePicture(key)) {
            eventEnable = calBinaryData(eventEnable, paramSetting.getInitiativePictureEnable(), position);
            return;
        }
    }

    private boolean isInitiativePicture(String key) {
        return key.equals("276508");
    }

    private boolean isDriverChange(String key) {
        return key.equals("276509");
    }

    private void handelAlarmParam(AdasAlarmParamSetting paramSetting, String key) {

        List<String> fields = alarmParamMap.get(key);
        if (fields == null) {
            return;
        }
        //如果是驾驶员身份识别，直接返回
        if (isDriverIdentify(fields)) {
            setValIfPresent(fields.get(0), paramSetting.getTouchStatus());
            return;
        }
        //必须严格按照顺序来，不能错乱
        setValIfPresent(fields.get(0), getFinalChannelVal(paramSetting.getPrimaryChannel()));
        setValIfPresent(fields.get(1), getFinalChannelVal(paramSetting.getSubcodeChannel()));
        setValIfPresent(fields.get(2), getFinalChannelVal(paramSetting.getCaptureChannel()));
        //如果是玩手机，直接返回
        if (isPlayPhone(fields)) {
            return;
        }
        setValIfPresent(fields.get(3), paramSetting.getPhotographTime());

        setValIfPresent(fields.get(4), paramSetting.getPhotographNumber());
        setValIfPresent(fields.get(5), paramSetting.getVideoRecordingTime());
        //抽烟和接打电话多一个判断时间间隔
        if (isPhoneAndSmoke(fields)) {
            setValIfPresent(fields.get(6), paramSetting.getTimeSlotThreshold());
        }
    }

    private boolean isPhoneAndSmoke(List<String> fields) {
        return fields.size() == 7;
    }

    private boolean isPlayPhone(List<String> fields) {
        return fields.size() == 3;
    }

    private boolean isDriverIdentify(List<String> fields) {
        return fields.size() == 1;
    }

    private void handelAlarmEnable(AdasAlarmParamSetting paramSetting, String key) {
        Integer position = alarmEnableMap.get(key);
        if (position == null) {
            return;
        }
        alarmEnable = calBinaryData(alarmEnable, paramSetting.getAlarmEnable(), position);
    }
}

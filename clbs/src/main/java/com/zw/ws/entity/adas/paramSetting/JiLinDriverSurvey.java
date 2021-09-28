package com.zw.ws.entity.adas.paramSetting;

import com.zw.adas.domain.define.setting.AdasAlarmParamSetting;
import com.zw.adas.domain.define.setting.query.AdasParamSettingForm;
import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.Map;


/**
 * @Description:主动安全参数(吉标) (驾驶员行为参数设置)
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class JiLinDriverSurvey extends PublicParameters implements T808MsgBody {

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
    private byte[] keep1 = new byte[3];

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
     * 接打电话报警分级速度阈值
     */
    private Integer pickUpSpeed = 0xff;

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
     * 抽烟报警分级车速阈值
     */
    private Integer smokingSpeed = 0xff;

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
     * 长时间不目视前方
     * <p>
     * 报警分 级车速阈值
     */
    private Integer attentionSpeed = 0xff;

    /**
     * 长时间不目视前方
     * <p>
     * <p>
     * 前后视频录制时间
     */
    private Integer attentionVideoTime = 0xff;

    /**
     * 长时间不目视前方
     * <p>
     * <p>
     * 报警拍照张数
     */
    private Integer attentionCameraNum = 0xff;

    /**
     * 长时间不目视前方
     * <p>
     * 报警拍照间隔时间
     */
    private Integer attentionCameraTime = 0xff;

    /**
     * 驾驶员不在驾驶位置
     * 分级速度阈值
     */
    private Integer driveDeedSpeed = 0xff;

    /**
     * 驾驶员不在驾驶位置
     * 视频录制时间
     */
    private Integer driveDeedVideoTime = 0xff;

    /**
     * 驾驶员不在驾驶位置
     * 抓拍照片张数
     */
    private Integer driveDeedCameraNum = 0xff;

    /**
     * 驾驶员不在驾驶位置
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
    private byte[] keep2 = new byte[13];

    /**
     * 未系安全带驾驶报警分级车速阈值
     */
    private Integer safetyBeltSpeed = 0xff;

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
     * 双手离开驾驶报警分级车速阈值
     */
    private Integer steeringSpeed = 0xff;

    /**
     * 双手离开驾驶报警前后视频录制时间
     */
    private Integer steeringtVideoTime = 0xff;

    /**
     * 双手开离开驾驶报警拍照张数
     */
    private Integer steeringCameraNum = 0xff;

    /**
     * 双手离开驾驶报警拍照时间间隔
     */
    private Integer steeringCameraTime = 0xff;

    /**
     * 疲劳驾驶联动上传主码流视频通道
     */
    private Integer fatiguePrimaryChannel = 0;

    /**
     * 疲劳驾驶联动上传子码流视频通道
     */
    private Integer fatigueSubcodeChannel = 0;

    /**
     * 疲劳驾驶联动上传抓拍通道
     */
    private Integer fatiguePhoto = 0;

    /**
     * 接打电话联动上传主码流视频通道
     */
    private Integer pickUpPrimaryChannel = 0;

    /**
     * 接打电话联动上传子码流视频通道
     */
    private Integer pickUpSubcodeChannel = 0;

    /**
     * 接打电话联动上传抓拍通道
     */
    private Integer pickUpPhoto = 0;

    /**
     * 抽烟联动上传主码流视频通道
     */
    private Integer smokingPrimaryChannel = 0;

    /**
     * 抽烟联动上传子码流视频通道
     */
    private Integer smokingSubcodeChannel = 0;

    /**
     * 抽烟联动上传抓拍通道
     */
    private Integer smokingPhoto = 0;

    /**
     * 注意力分散联动上传主码流视频通道
     */
    private Integer attentionPrimaryChannel = 0;

    /**
     * 注意力分散联动上传子码流视频通道
     */
    private Integer attentionSubcodeChannel = 0;

    /**
     * 注意力分散联动上传抓拍通道
     */
    private Integer attentionPhoto = 0;

    /**
     * 驾驶员不在驾驶位联动上传主码流视频通道
     */
    private Integer driveDeedPrimaryChannel = 0;

    /**
     * 驾驶员不在驾驶位联动上传子码流视频通道
     */
    private Integer driveDeedSubcodeChannel = 0;

    /**
     * 驾驶员不在驾驶位联动上传抓拍通道
     */
    private Integer driveDeedPhoto = 0;

    /**
     * 预留
     */
    private byte[] keep3 = new byte[6];

    /**
     * 未系安全带联动上传主码流视频通道
     */
    private Integer safetyBeltPrimaryChannel = 0;

    /**
     * 未系安全带联动上传子码流视频通道
     */
    private Integer safetyBeltSubcodeChannel = 0;

    /**
     * 未系安全带联动上传抓拍通道
     */
    private Integer safetyBeltPhoto = 0;

    /**
     * 双手离把联动上传主码流视频通道
     */
    private Integer steeringPrimaryChannel = 0;

    /**
     * 双手离把联动上传子码流视频通道
     */
    private Integer steeringSubcodeChannel = 0;

    /**
     * 双手离把联动上传抓拍通道
     */
    private Integer steeringPhoto = 0;

    /**
     * 预留
     */
    private byte[] keep4 = new byte[10];

    /**
     * 川冀标驾驶员行为报警使能顺序
     */
    private static Map<String, Object> surveyAlarmEnableMap = new HashMap();

    /**
     * 川冀标驾驶员行为报警事件参数设置参数交互字段
     */
    private static Map<String, Object> surveyAlarmParamMap = new HashMap();

    /**
     * 176513 疲劳
     * 176502 接打手持电话
     * 176503 抽烟
     * 176504 长时间不目视前方
     * 176505 驾驶员不在驾驶位置报警
     * 176506 未系安全带
     * 176517 双手离开方向盘
     * 176509 设备遮挡失效报警
     * 176508 红外阻断墨镜失效报警
     */
    static {
        //报警使能顺序维护
        String[][] alarmEnableOrder =
            {{"176513", "0,1"}, {"176502", "2,3"}, {"176503", "4,5"}, {"176504", "6,7"}, {"176505", "8,9"},
                {"176506", "18,19"}, {"176517", "20,21"}, {"176509", "24"}, {"176508", "25"}};
        for (String[] ints : alarmEnableOrder) {
            surveyAlarmEnableMap.put(ints[0], ints[1]);
        }
        //报警事件参数设置参数交互字段维护
        String[][] assistAlarmParamOrder =
            {{"176513", "fatigueSpeed,fatigueVideoTime,fatigueCameraNum,fatigueCameraTime,"
                  + "fatiguePrimaryChannel,fatigueSubcodeChannel,fatiguePhoto"},
                {"176502", "pickUpSpeed,pickUpVideoTime,pickUpCameraNum,pickUpCameraTime,"
                    + "pickUpPrimaryChannel,pickUpSubcodeChannel,pickUpPhoto"},
                {"176503", "smokingSpeed,smokingVideoTime,smokingCameraNum,smokingCameraTime,"
                    + "smokingPrimaryChannel,smokingSubcodeChannel,smokingPhoto"},
                {"176504", "attentionSpeed,attentionVideoTime,attentionCameraNum,attentionCameraTime,"
                    + "attentionPrimaryChannel,attentionSubcodeChannel,attentionPhoto"},
                {"176505", "driveDeedSpeed,driveDeedVideoTime,driveDeedCameraNum,driveDeedCameraTime,"
                    + "driveDeedPrimaryChannel,driveDeedSubcodeChannel,driveDeedPhoto"},
                {"176506", "safetyBeltSpeed,safetyBeltVideoTime,safetyBeltCameraNum,safetyBeltCameraTime,"
                    + "safetyBeltPrimaryChannel,safetyBeltSubcodeChannel,safetyBeltPhoto"},
                {"176517", "steeringSpeed,steeringtVideoTime,steeringCameraNum,steeringCameraTime,"
                    + "steeringPrimaryChannel,steeringSubcodeChannel,steeringPhoto"}
            };
        for (String[] ints : assistAlarmParamOrder) {
            surveyAlarmParamMap.put(ints[0], ints[1]);
        }
    }

    public JiLinDriverSurvey(AdasParamSettingForm paramSettingForm) {
        super(paramSettingForm.getCommonParamSetting());
        for (AdasAlarmParamSetting paramSetting : paramSettingForm.getAdasAlarmParamSettings()) {
            String functionId = paramSetting.getRiskFunctionId().toString();
            //组装报警使能
            handelAlarmEnable(paramSetting, functionId);
            //组装报警事件参数设置
            handelAlarmParam(paramSetting, functionId);
        }
    }

    private void handelAlarmParam(AdasAlarmParamSetting paramSetting, String key) {
        //176509 设备遮挡失效报警 176508 红外阻断墨镜失效报警  无事件参数设置
        if ("176509".equals(key) || "176508".equals(key)) {
            return;
        }
        //驾驶员身份识别
        if ("176510".equals(key)) {
            setValIfPresent("driveDeedType", paramSetting.getTouchStatus());
            return;
        }
        String[] params = surveyAlarmParamMap.get(key).toString().split(",");
        //分级速度阀值
        setValIfPresent(params[0], paramSetting.getAlarmLevelSpeedThreshold());
        //报警录制时间
        setValIfPresent(params[1], paramSetting.getVideoRecordingTime());
        //报警拍照张数
        setValIfPresent(params[2], paramSetting.getPhotographNumber());
        //报警拍照间隔
        setValIfPresent(params[3], paramSetting.getPhotographTime());
        //主码流
        setValIfPresent(params[4], paramSetting.getPrimaryChannel());
        //子码流
        setValIfPresent(params[5], paramSetting.getSubcodeChannel());
        //抓拍通道
        setValIfPresent(params[6], paramSetting.getCaptureChannel());
        if ("176503".equals(key)) {
            setValIfPresent("smokingDecideTime", paramSetting.getTimeSlotThreshold());
        }
        if ("176502".equals(key)) {
            setValIfPresent("pickUpDecideTime", paramSetting.getTimeSlotThreshold());
        }
    }

    private void handelAlarmEnable(AdasAlarmParamSetting paramSetting, String key) {
        if (surveyAlarmEnableMap.get(key) != null) {
            //176509 设备遮挡失效报警  176508 红外阻断墨镜失效报警
            if ("176509".equals(key) || "176508".equals(key)) {
                alarmEnable = calBinaryData(alarmEnable, paramSetting.getRoadSignEnable(),
                    Integer.parseInt(surveyAlarmEnableMap.get(key).toString()));
                return;
            }
            String[] order = surveyAlarmEnableMap.get(key).toString().split(",");
            alarmEnable = calBinaryData(alarmEnable, paramSetting.getOneLevelAlarmEnable(), Integer.parseInt(order[0]));
            alarmEnable = calBinaryData(alarmEnable, paramSetting.getTwoLevelAlarmEnable(), Integer.parseInt(order[1]));
        }
    }
}

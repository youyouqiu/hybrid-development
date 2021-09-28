package com.zw.ws.entity.adas.paramSetting;

import com.zw.adas.domain.define.setting.AdasAlarmParamSetting;
import com.zw.adas.domain.define.setting.query.AdasParamSettingForm;
import com.zw.protocol.msg.t808.T808MsgBody;
import com.zw.ws.entity.adas.EnableClassOrder;
import com.zw.ws.entity.adas.EnableOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

/**
 * 216502 接打电话
 * 216503 抽烟
 * 216501 闭眼
 * 216504 打哈欠
 * 216508 长时间不目视前方报警
 * 216510 遮挡报警
 * 216514 红外阻断
 * 216515 人证不符
 * 216516 驾驶员不在驾驶位置
 * 216517 外设状态异常
 * 216518 双手同时脱离方向盘报警
 * 216519 辅助多媒体
 * @Description:主动安全参数(沪标) (驾驶员行为参数设置)
 */
@Data
@EqualsAndHashCode(callSuper = true)
@EnableClassOrder
@Component
public class ZhongDriverSurvey extends ZhongPublicParameters implements T808MsgBody {

    /*  接打电话报警  */
    /**
     * 接打电话
     */
    @EnableOrder(enableIndex = 0, auxiliaryEnableIndex = 16, functionId = "216502",
        value = "pickUpSpeed,pickUpVideoTime,pickUpCameraNum,pickUpCameraTime")
    private static transient String PICK_UP = "216502";
    /**
     * 抽烟
     */
    @EnableOrder(enableIndex = 1, auxiliaryEnableIndex = 17, functionId = "216503",
        value = "smokingSpeed,smokingVideoTime,smokingCameraNum,smokingCameraTime")
    private static transient String SMOKES = "216503";
    /**
     * 闭眼
     */
    @EnableOrder(enableIndex = 2, auxiliaryEnableIndex = 18, functionId = "216501",
        value = "closeEyesSpeed,closeEyesVideoTime,closeEyesCameraNum,closeEyesCameraTime")
    private static transient String EYES_CLOSED = "216501";
    /**
     * 打哈欠
     */
    @EnableOrder(enableIndex = 3, auxiliaryEnableIndex = 19, functionId = "216504",
        value = "yawnSpeed,yawnVideoTime,yawnCameraNum,yawnCameraTime")
    private static transient String YAWN = "216504";
    /**
     * 长时间不目视前方报警
     */
    @EnableOrder(enableIndex = 4, auxiliaryEnableIndex = 20, functionId = "216508",
        value = "postureSpeed,postureVideoTime,postureCameraNum,postureCameraTime")
    private static transient String LONG_TIME_VISUAL = "216508";

    /*  抽烟报警  */
    /**
     * 遮挡报警
     */
    @EnableOrder(enableIndex = 5, auxiliaryEnableIndex = 21, functionId = "216510",
        value = "shutterSpeed,shutterVideoTime,shutterCameraNum,shutterCameraTime")
    private static transient String OCCLUSION_ALARM = "216510";

    /**
     * 红外阻断
     */
    @EnableOrder(enableIndex = 6, auxiliaryEnableIndex = 22, functionId = "216514",
        value = "shutterSpeed,shutterVideoTime,shutterCameraNum,shutterCameraTime")
    private static transient String INFRARED_BLOCKING = "216515";
    /**
     * 人证不符
     */
    @EnableOrder(enableIndex = 6, auxiliaryEnableIndex = 22, functionId = "216515",
        value = "identTime,identVideoTime,identCameraNum,identCameraTime")
    private static transient String WITNESSES_DO_NOT_MATCH = "216515";
    /**
     * 驾驶员不在驾驶位置
     */
    @EnableOrder(functionId = "216516", value = "null,checkIdentVideoTime,checkIdentCameraNum,checkIdentCameraTime")
    private static transient String DRIVER_MISS = "216516";
    /**
     * 双手同时脱离方向盘报警
     */
    @EnableOrder(enableIndex = 7, auxiliaryEnableIndex = 23, functionId = "216518",
        value = "null,checkIdentVideoTime,checkIdentCameraNum,checkIdentCameraTime")
    private static transient String OFF_THE_STEERING_WHEEL = "216518";
    /**
     * 外设状态异常
     */
    private static transient String PERIPHERAL_STATUS = "216517";

    /*  闭眼驾驶报警  */
    /**
     * 辅助多媒体
     */
    private static transient String AUXILIARY_MULTIMEDIA = "216519";
    /**
     * 接打电话报警-分级速度阈值
     */
    private Integer pickUpSpeed = 0xff;
    /**
     * 接打电话报警-前后视频录制时间
     */
    private Integer pickUpVideoTime = 0xff;
    /**
     * 接打电话报警-拍照片张数
     */
    private Integer pickUpCameraNum = 0xff;

    /*  打哈欠报警  */
    /**
     * 接打电话报警-拍照片间隔时间
     */
    private Integer pickUpCameraTime = 0xff;
    /**
     * 接打电话报警-判断时间间隔
     */
    private Integer pickUpTime = 0xff;
    /**
     * 抽烟报警-判断时间间隔
     */
    private Integer smokingTime = 0xff;
    /**
     * 抽烟报警-分级车速阈值
     */
    private Integer smokingSpeed = 0xff;

    /*  注意力分散  */
    /**
     * 抽烟报警-前后视频录制时间
     */
    private Integer smokingVideoTime = 0xff;
    /**
     * 抽烟报警-拍照片张数
     */
    private Integer smokingCameraNum = 0xff;
    /**
     * 抽烟报警-拍照片间隔时间
     */
    private Integer smokingCameraTime = 0xff;
    /**
     * 闭眼驾驶报警-分级速度阈值
     */
    private Integer closeEyesSpeed = 0xff;

    /*  遮挡报警  */
    /**
     * 闭眼驾驶报警-前后视频录制时间
     */
    private Integer closeEyesVideoTime = 0xff;
    /**
     * 闭眼驾驶报警-抓拍照片张数
     */
    private Integer closeEyesCameraNum = 0xff;
    /**
     * 闭眼驾驶报警-拍照间隔
     */
    private Integer closeEyesCameraTime = 0xff;
    /**
     * 打哈欠报警-分级速度阈值
     */
    private Integer yawnSpeed = 0xff;

    /*  人证不符事件  */
    /**
     * 打哈欠报警-前后视频录制时间
     */
    private Integer yawnVideoTime = 0xff;
    /**
     * 打哈欠报警-抓拍照片张数
     */
    private Integer yawnCameraNum = 0xff;
    /**
     * 打哈欠报警-拍照间隔
     */
    private Integer yawnCameraTime = 0xff;
    /**
     * 注意力分散-分级速度阈值
     */
    private Integer postureSpeed = 0xff;

    /*  未检测到驾驶员事件  */
    /**
     * 注意力分散-前后视频录制时间
     */
    private Integer postureVideoTime = 0xff;
    /**
     * 注意力分散-抓拍照片张数
     */
    private Integer postureCameraNum = 0xff;
    /**
     * 注意力分散-拍照间隔
     */
    private Integer postureCameraTime = 0xff;
    /**
     * 遮挡报警-分级速度阈值
     */
    private Integer shutterSpeed = 0xff;
    /**
     * 遮挡报警-视频录制时间
     */
    private Integer shutterVideoTime = 0xff;
    /**
     * 遮挡报警-抓拍照片张数
     */
    private Integer shutterCameraNum = 0xff;
    /**
     * 遮挡报警-拍照间隔
     */
    private Integer shutterCameraTime = 0xff;
    /**
     * 人证不符事件-定时检测驾驶员人证不符的间隔时间
     */
    private Integer identTime = 0xff;
    /**
     * 人证不符事件-前后视频录制时间
     */
    private Integer identVideoTime = 0xff;
    /**
     * 人证不符事件-抓拍照片张数
     */
    private Integer identCameraNum = 0xff;
    /**
     * 人证不符事件-拍照间隔
     */
    private Integer identCameraTime = 0xff;
    /**
     * 未检测到驾驶员事件-前后视频录制时间
     */
    private Integer checkIdentVideoTime = 0xff;
    /**
     * 未检测到驾驶员事件-抓拍照片张数
     */
    private Integer checkIdentCameraNum = 0xff;
    /**
     * 未检测到驾驶员事件-拍照间隔
     */
    private Integer checkIdentCameraTime = 0xff;
    /**
     * 双手离把驾驶报警分级车速阈值
     */
    private Integer steeringSpeed = 0xff;
    /**
     * 双手离把驾驶报警前后视频录制时间
     */
    private Integer steeringVideoTime = 0xff;
    /**
     * 双手离把驾驶报警拍照张数
     */
    private Integer steeringCameraNum = 0xff;
    /**
     * 双手离把驾驶报警拍照时间间隔
     */
    private Integer steeringCameraTime = 0xff;
    /**
     * 保留字段
     */
    private byte[] reserve = new byte[2];
    /**
     * 辅助信息使能
     */
    private Integer eventEnable = 0;
    /**
     * 使能参数-一级报警使能
     */
    private Integer oneLevelAlarmEnable = 0;

    /**
     * 使能参数-二级报警使能
     */
    private Integer twoLevelAlarmEnable = 0;
    /**
     * 报警语音使能-一级报警语音使能
     */
    private Integer oneLevelVoiceEnable = 0;
    /**
     * 报警语音使能-二级报警语音使能
     */
    private Integer twoLevelVoiceEnable = 0;
    /**
     * 报警/辅助信息语音使能-事件报警语音使能
     */
    private Integer eventVoiceEnable = 0;

    /**
     * 辅助多媒体信息-辅助多媒体策略
     */
    private Integer multimediaStrategy = 0;

    public ZhongDriverSurvey() {
    }

    public ZhongDriverSurvey(AdasParamSettingForm paramSettingForm) {
        super(paramSettingForm.getCommonParamSetting());
        for (AdasAlarmParamSetting paramSetting : paramSettingForm.getAdasAlarmParamSettings()) {
            String functionId = paramSetting.getRiskFunctionId().toString();
            handelEnable(paramSetting, functionId);
            handelAlarmParam(paramSetting, functionId);
        }
    }

    private void handelEnable(AdasAlarmParamSetting paramSetting, String functionId) {
        Integer index = ZhongWeiParamSettingUtil.enableOrderMap.get(functionId);
        if (index != null) {
            oneLevelAlarmEnable = calBinaryData(oneLevelAlarmEnable, paramSetting.getOneLevelAlarmEnable(), index);
            oneLevelVoiceEnable = calBinaryData(oneLevelVoiceEnable, paramSetting.getOneLevelVoiceReminder(), index);
            multimediaStrategy =
                calBinaryData(multimediaStrategy, paramSetting.getOneLevelAuxiliaryMultimedia(), index);
            twoLevelAlarmEnable = calBinaryData(twoLevelAlarmEnable, paramSetting.getTwoLevelAlarmEnable(), index);
            twoLevelVoiceEnable = calBinaryData(twoLevelVoiceEnable, paramSetting.getTwoLevelVoiceReminder(), index);
            multimediaStrategy = calBinaryData(multimediaStrategy, paramSetting.getTwoLevelAuxiliaryMultimedia(),
                ZhongWeiParamSettingUtil.auxiliaryEnableOrderMap.get(functionId));

        }
        if (WITNESSES_DO_NOT_MATCH.equals(functionId)) {
            eventEnable = calBinaryData(eventEnable, paramSetting.getAuxiliaryEnable(), 1);
            eventVoiceEnable = calBinaryData(eventVoiceEnable, paramSetting.getVoiceReminderEnable(), 1);
        }
        if (DRIVER_MISS.equals(functionId)) {
            eventEnable = calBinaryData(eventEnable, paramSetting.getAuxiliaryEnable(), 2);
            eventVoiceEnable = calBinaryData(eventVoiceEnable, paramSetting.getVoiceReminderEnable(), 2);
        }
        if (PERIPHERAL_STATUS.equals(functionId)) {
            eventEnable = calBinaryData(eventEnable, paramSetting.getAuxiliaryEnable(), 3);
            eventVoiceEnable = calBinaryData(eventVoiceEnable, paramSetting.getVoiceReminderEnable(), 3);
        }

    }

    private void handelAlarmParam(AdasAlarmParamSetting paramSetting, String functionId) {
        String paramsString = ZhongWeiParamSettingUtil.assistAlarmParamMap.get(functionId);
        if (paramsString != null) {
            String[] params = ZhongWeiParamSettingUtil.assistAlarmParamMap.get(functionId).split(",");
            //分级速度阀值
            setValIfPresent(params[0], paramSetting.getAlarmLevelSpeedThreshold());
            if (WITNESSES_DO_NOT_MATCH.equals(functionId)) {
                //定时检测间隔
                setValIfPresent(params[0], paramSetting.getTimeSlotThreshold());
            }
            //报警录制时间
            setValIfPresent(params[1], paramSetting.getVideoRecordingTime());
            //报警拍照张数
            setValIfPresent(params[2], paramSetting.getPhotographNumber());
            //报警拍照间隔
            setValIfPresent(params[3], paramSetting.getPhotographTime());
            if (SMOKES.equals(functionId)) {
                setValIfPresent("smokingTime", paramSetting.getTimeSlotThreshold());
            }
            if (PICK_UP.equals(functionId)) {
                setValIfPresent("pickUpTime", paramSetting.getTimeSlotThreshold());
            }
        }
    }

}

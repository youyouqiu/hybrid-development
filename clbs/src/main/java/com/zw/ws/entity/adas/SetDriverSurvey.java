package com.zw.ws.entity.adas;

import com.zw.platform.domain.riskManagement.form.RiskEventVehicleConfigForm;
import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * @Description: @Author:nixiangqian @Date:Create in 2018/7/11 13:47 ASM
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SetDriverSurvey extends SetDrive implements T808MsgBody {

    /**
     * 接打电话报警-判断时间间隔(秒)
     */
    private Integer pickUpTime = 120;

    /**
     * 接打电话报警-分级速度阈值(km/h)
     */
    private Integer pickUpSpeed = 50;

    /**
     * 接打电话报警-前后视频录制时间(秒)
     */
    private Integer pickUpVideoTime = 10;

    /**
     * 接打电话报警-拍照片张数
     */
    private Integer pickUpCameraNum = 3;

    /**
     * 接打电话报警-拍照片间隔时间(100ms)
     */
    private Integer pickUpCameraTime = 5;

    /**
     * 抽烟报警-判断时间间隔(秒)
     */
    private Integer smokingTime = 180;

    /**
     * 抽烟报警-分级车速阈值(KM/H)
     */
    private Integer smokingSpeed = 50;

    /**
     * 抽烟报警-前后视频录制时间(秒)
     */
    private Integer smokingVideoTime = 10;

    /**
     * 抽烟报警-拍照片张数
     */
    private Integer smokingCameraNum = 3;

    /**
     * 抽烟报警-拍照片间隔时间(100ms )
     */
    private Integer smokingCameraTime = 5;

    /**
     * 闭眼驾驶报警-分级速度阈值(位 km/h)
     */
    private Integer closeEyesSpeed = 50;

    /**
     * 闭眼驾驶报警-前后视频录制时间(秒)
     */
    private Integer closeEyesVideoTime = 10;

    /**
     * 闭眼驾驶报警-抓拍照片张数
     */
    private Integer closeEyesCameraNum = 3;

    /**
     * 闭眼驾驶报警-拍照间隔(100ms)
     */
    private Integer closeEyesCameraTime = 5;

    /**
     * 打哈欠报警-分级速度阈值(km/h)
     */
    private Integer yawnSpeed = 50;

    /**
     * 打哈欠报警-前后视频录制时间(秒)
     */
    private Integer yawnVideoTime = 10;

    /**
     * 打哈欠报警-抓拍照片张数
     */
    private Integer yawnCameraNum = 3;

    /**
     * 打哈欠报警-拍照间隔(100ms)
     */
    private Integer yawnCameraTime = 5;

    /**
     * 长时间不目视前方报警-分级速度阈值(km/h)
     */
    private Integer postureSpeed = 50;

    /**
     * 长时间不目视前方报警-前后视频录制时间(秒)
     */
    private Integer postureVideoTime = 10;

    /**
     * 长时间不目视前方报警-抓拍照片张数
     */
    private Integer postureCameraNum = 3;

    /**
     * 长时间不目视前方报警-拍照间隔(100ms )
     */
    private Integer postureCameraTime = 5;

    /**
     * 人证不符事件-定时检测驾驶员人证不符的间隔时间(分钟)
     */
    private Integer identTime = 10;

    /**
     * 人证不符事件-前后视频录制时间(秒)
     */
    private Integer identVideoTime = 10;

    /**
     * 人证不符事件-抓拍照片张数
     */
    private Integer identCameraNum = 3;

    /**
     * 人证不符事件-拍照间隔(100ms)
     */
    private Integer identCameraTime = 5;

    /**
     * 驾驶员不在驾驶位置事件-前后视频录制时间(秒)
     */
    private Integer checkIdentVideoTime = 10;

    /**
     * 驾驶员不在驾驶位置事件-抓拍照片张数
     */
    private Integer checkIdentCameraNum = 3;

    /**
     * 驾驶员不在驾驶位置事件-拍照间隔(100ms)
     */
    private Integer checkIdentCameraTime = 5;

    /**
     * 遮挡报警-分级速度阈值(红外阻断)
     */
    private Integer shutterSpeed = 50;

    /**
     * 遮挡报警-视频录制时间(红外阻断)
     */
    private Integer shutterVideoTime = 0;

    /**
     * 遮挡报警-抓拍照片张数(红外阻断)
     */
    private Integer shutterCameraNum = 3;

    /**
     * 遮挡报警-拍照间隔(红外阻断)
     */
    private Integer shutterCameraTime = 5;

    /**
     * 保留字段
     */
    private byte[] reserve = new byte[2];

    public void init(RiskEventVehicleConfigForm revConfig) {

        handleEventEnable(revConfig);
        handleAlarmAndVoiceEnable(revConfig);
        handleEventVoiceEnable(revConfig);

        //事件参数设置
        String eventId = revConfig.getRiskId();
        if (AdasEvent.PICKUP.eq(
            eventId)) {
            //接打电话
            setValIfPresent("pickUpTime", revConfig.getTimeInterval().intValue());
            setValIfPresent("pickUpSpeed", revConfig.getHighSpeed());
            setValIfPresent("pickUpVideoTime", revConfig.getVideoRecordingTime());
            setValIfPresent("pickUpCameraNum", revConfig.getPhotographNumber());
            setValIfPresent("pickUpCameraTime", parseIntFromFloat(revConfig.getPhotographTime(), 10));
            //这里取接打电话作为DSM该类型的代表对数据进行DSM相关参数的初始化
            initBase(revConfig);
        } else if (AdasEvent.SMOKING.eq(eventId)) {
            //抽烟报警
            setValIfPresent("smokingTime", revConfig.getTimeInterval().intValue());
            setValIfPresent("smokingSpeed", revConfig.getHighSpeed());
            setValIfPresent("smokingVideoTime", revConfig.getVideoRecordingTime());
            setValIfPresent("smokingCameraNum", revConfig.getPhotographNumber());
            setValIfPresent("smokingCameraTime", parseIntFromFloat(revConfig.getPhotographTime(), 10));
        } else if (AdasEvent.CLOS_EEYES.eq(eventId)) {
            //闭眼
            setValIfPresent("closeEyesSpeed", revConfig.getHighSpeed());
            setValIfPresent("closeEyesVideoTime", revConfig.getVideoRecordingTime());
            setValIfPresent("closeEyesCameraNum", revConfig.getPhotographNumber());
            setValIfPresent("closeEyesCameraTime", parseIntFromFloat(revConfig.getPhotographTime(), 10));
        } else if (AdasEvent.YAWN.eq(eventId)) {
            //打哈欠
            setValIfPresent("yawnSpeed", revConfig.getHighSpeed());
            setValIfPresent("yawnVideoTime", revConfig.getVideoRecordingTime());
            setValIfPresent("yawnCameraNum", revConfig.getPhotographNumber());
            setValIfPresent("yawnCameraTime", parseIntFromFloat(revConfig.getPhotographTime(), 10));
        } else if (AdasEvent.POSTURE.eq(eventId)) {
            //长时间不目视前方
            setValIfPresent("postureSpeed", revConfig.getHighSpeed());
            setValIfPresent("postureVideoTime", revConfig.getVideoRecordingTime());
            setValIfPresent("postureCameraNum", revConfig.getPhotographNumber());
            setValIfPresent("postureCameraTime", parseIntFromFloat(revConfig.getPhotographTime(), 10));
        } else if (AdasEvent.IDENT.eq(eventId)) {
            //人证不符
            setValIfPresent("identTime", revConfig.getTimeInterval().intValue());
            setValIfPresent("identVideoTime", revConfig.getVideoRecordingTime());
            setValIfPresent("identCameraNum", revConfig.getPhotographNumber());
            setValIfPresent("identCameraTime", parseIntFromFloat(revConfig.getPhotographTime(), 10));
        } else if (AdasEvent.CHECK_IDENT.eq(eventId)) {
            //驾驶员不在驾驶位置
            setValIfPresent("checkIdentVideoTime", revConfig.getVideoRecordingTime());
            setValIfPresent("checkIdentCameraNum", revConfig.getPhotographNumber());
            setValIfPresent("checkIdentCameraTime", parseIntFromFloat(revConfig.getPhotographTime(), 10));
        } else if (AdasEvent.KEEP_OUT.eq(eventId) || AdasEvent.INFRARED_BLOCKING.eq(eventId)) {
            setValIfPresent("shutterSpeed", revConfig.getHighSpeed());
            setValIfPresent("shutterVideoTime", revConfig.getVideoRecordingTime());
            setValIfPresent("shutterCameraNum", revConfig.getPhotographNumber());
            setValIfPresent("shutterCameraTime", parseIntFromFloat(revConfig.getPhotographTime(), 10));
        }

    }

    private void handleEventEnable(RiskEventVehicleConfigForm revConfig) {
        String eventId = revConfig.getRiskId();
        //人证不符事件
        if (AdasEvent.IDENT.eq(eventId)) {
            eventEnable = calBinaryData(eventEnable, revConfig.getCheckSwitch(), 1);
            //人证不符属于dsm中的一种，所以取它的主动抓拍事件使能即可代表dsm
            eventEnable = calBinaryData(eventEnable, revConfig.getInitiativeCaptureAlarmEnable(), 0);
        }
        //驾驶员不在驾驶位置
        if (AdasEvent.CHECK_IDENT.eq(eventId)) {
            eventEnable = calBinaryData(eventEnable, revConfig.getCheckSwitch(), 2);
        }

    }

    private void handleAlarmAndVoiceEnable(RiskEventVehicleConfigForm revConfig) {
        String eventId = revConfig.getRiskId();
        if (AdasEvent.PICKUP.eq(
            eventId)) {
            //接打电话
            calAlarmAndVoiceEnable(revConfig, 0);
        } else if (AdasEvent.SMOKING.eq(eventId)) {
            //抽烟
            calAlarmAndVoiceEnable(revConfig, 1);
        } else if (AdasEvent.CLOS_EEYES.eq(eventId)) {
            //闭眼
            calAlarmAndVoiceEnable(revConfig, 2);
        } else if (AdasEvent.YAWN.eq(eventId)) {
            //打哈欠
            calAlarmAndVoiceEnable(revConfig, 3);
        } else if (AdasEvent.POSTURE.eq(eventId)) {
            //长时间不目视前方
            calAlarmAndVoiceEnable(revConfig, 4);
        } else if (AdasEvent.KEEP_OUT.eq(eventId)) {
            //遮挡
            calAlarmAndVoiceEnable(revConfig, 5);
        } else if (AdasEvent.INFRARED_BLOCKING.eq(eventId)) {
            //红外阻断
            calAlarmAndVoiceEnable(revConfig, 6);
        }

    }

    private void handleEventVoiceEnable(RiskEventVehicleConfigForm revConfig) {

        String eventId = revConfig.getRiskId();
        if (AdasEvent.IDENT.eq(eventId)) {
            //人证不符事件语音提示使能
            eventVoiceEnable = calBinaryData(eventVoiceEnable, revConfig.getVoiceEnable(), 1);
            //人证不符属于dsm的一种，所以去它的主动抓拍事件语音使能即可代表dsm
            eventVoiceEnable = calBinaryData(eventVoiceEnable, revConfig.getInitiativeCaptureVoiceEnable(), 0);
        } else if (AdasEvent.CHECK_IDENT.eq(eventId)) {
            //驾驶员不在驾驶位置事件语音提示
            eventVoiceEnable = calBinaryData(eventVoiceEnable, revConfig.getVoiceEnable(), 2);
        }

    }

}

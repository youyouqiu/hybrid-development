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
 * @Description:主动安全参数(中位标准) (前向参数设置)
 */
@Data
@EqualsAndHashCode(callSuper = false)
@EnableClassOrder
@Component
public class ZhongDriverAssistance extends ZhongPublicParameters implements T808MsgBody {

    /**
     * 中位标准2019 functionId 定义
     *
     *
     * 道路标识识别：2164091
     * 外设状态：216406
     * 前向碰撞：216401
     * 车道左偏离：2164021
     * 车道右偏离：2164022
     * 车距过近：216403
     * 行人碰撞：216404
     * 频繁变道：216405
     * 道路标识超限报警：2164092
     * 急加速：2164081
     * 急减速：2164082
     * 急转弯：2164083
     * 辅助多媒体：216410
     */

    /**
     * 道路标识识别
     */
    @EnableOrder(functionId = "2164091", value = "roadMarkingEventVideoTime,roadMarkingCameraNum,roadMarkingCameraTime")
    private  static transient  String ROAD_SIGN_RECOGNITION = "2164091";
    /**
     * 外设状态
     */
    private static transient  String PERIPHERAL_STATUS = "216406";

    /**
     * 前向碰撞
     */
    @EnableOrder(enableIndex = 0, auxiliaryEnableIndex = 16, functionId = "216401", value =
        "vehicleCollisionTime,vehicleCollisionSpeed,vehicleCollisionVideoTime,"
            + "vehicleCollisionCameraNum,vehicleCollisionCameraTime")
    private  static transient  String FORWARD_COLLISION = "216401";
    /**
     * 车道左偏离
     */
    @EnableOrder(enableIndex = 1, auxiliaryEnableIndex = 17, functionId = "2164021",
        value = "null,deviateSpeed,deviateVideoTime,deviateCameraNum,deviateCameraTime")
    private  static transient String LEFT_LANE_DEPARTURE = "2164021";
    /**
     * 车道右偏离
     */
    @EnableOrder(enableIndex = 2, auxiliaryEnableIndex = 18, functionId = "2164022",
        value = "null,deviateSpeed,deviateVideoTime,deviateCameraNum,deviateCameraTime")
    private  static transient String RIGHT_LANE_DEPARTURE = "2164022";
    /**
     * 车距过近
     */
    @EnableOrder(enableIndex = 3, auxiliaryEnableIndex = 19, functionId = "216403",
        value = "distanceMail,distanceSpeed,distanceVideoTime,distanceCameraNum,distanceCameraTime")
    private  static transient String DISTANCE_TOO_CLOSE = "216403";
    /**
     * 行人碰撞
     */
    @EnableOrder(enableIndex = 4, auxiliaryEnableIndex = 20, functionId = "216404",
        value = "pedestrianCollisionTime,pedestrianCollisionSpeed,pedestrianCollisionVideoTime,"
            + "pedestrianCollisionCameraNum,pedestrianCollisionCameraTime")
    private  static transient String PEDESTRIAN_COLLISIONS = "216404";
    /**
     * 频繁变道
     */
    @EnableOrder(enableIndex = 5, auxiliaryEnableIndex = 21, functionId = "216405",
        value = "null,laneChangeSpeed,laneChangeVideoTime,laneChangeCameraNum,"
            + "laneChangeCameraTime,laneChangeNum,laneChangeTime")
    private  static transient String FREQUENTLY_CHANGE_LANES = "216405";
    /**
     * 道路标识超限报警
     */
    @EnableOrder(enableIndex = 6, functionId = "2164092",
        value = "speedLimitVideoTime,speedLimitCameraNum,speedLimitCameraTime")
    private  static transient String ROAD_SIGNS_OVER_LIMIT_ALARM = "2164092";

    /**
     * 急加速
     */
    @EnableOrder(enableIndex = 7, auxiliaryEnableIndex = 23, functionId = "2164081",
        value = "null,quickSpeed,quickVideoTime,quickCameraNum,quickCameraTime")
    private  static transient String URGENT_TO_ACCELERATE = "2164081";
    /**
     * 急减速
     */
    @EnableOrder(enableIndex = 8, auxiliaryEnableIndex = 24, functionId = "2164082",
        value = "null,quickSpeed,quickVideoTime,quickCameraNum,quickCameraTime")
    private  static transient String SHARP_SLOWDOWN = "2164082";
    /**
     * 急转弯
     */
    @EnableOrder(enableIndex = 9, auxiliaryEnableIndex = 25, functionId = "2164083",
        value = "null,quickSpeed,quickVideoTime,quickCameraNum,quickCameraTime")
    private  static transient String A_SHARP_TURN = "2164083";

    /**
     * 辅助多媒体
     */
    private  static transient String AUXILIARY_MULTIMEDIA = "216410";

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
     * 前车碰撞预警-时间阈值
     */
    private Integer vehicleCollisionTime = 0xff;

    /**
     * 前车碰撞预警-分级速度阈值
     */
    private Integer vehicleCollisionSpeed = 0xff;
    /**
     * 前车碰撞预警-前后视频录制时间
     */
    private Integer vehicleCollisionVideoTime = 0xff;
    /**
     * 前车碰撞预警-拍照片张数
     */
    private Integer vehicleCollisionCameraNum = 0xff;
    /**
     * 前车碰撞预警-拍照间隔
     */
    private Integer vehicleCollisionCameraTime = 0xff;

    /**
     * 车道偏离预警-分级速度阈值
     */
    private Integer deviateSpeed = 0xff;
    /**
     * 车道偏离预警-前后视频录制时间
     */
    private Integer deviateVideoTime = 0xff;
    /**
     * 车道偏离预警-拍照片张数
     */
    private Integer deviateCameraNum = 0xff;
    /**
     * 车道偏离预警-拍照间隔
     */
    private Integer deviateCameraTime = 0xff;
    /**
     * 车距过近预警-距离阈值
     */
    private Integer distanceMail = 0xff;

    /**
     * 车距过近预警-拍照间隔
     */
    private Integer distanceSpeed = 0xff;
    /**
     * 车距过近预警-前后视频录制时间
     */
    private Integer distanceVideoTime = 0xff;
    /**
     * 车距过近预警-拍照张数
     */
    private Integer distanceCameraNum = 0xff;
    /**
     * 车距过近预警-拍照间隔
     */
    private Integer distanceCameraTime = 0xff;
    /**
     * 行人碰撞预警-时间阈值
     */
    private Integer pedestrianCollisionTime = 0xff;

    /**
     * 行人碰撞预警-分级速度阈值
     */
    private Integer pedestrianCollisionSpeed = 0xff;
    /**
     * 行人碰撞预警-前后视频录制时间
     */
    private Integer pedestrianCollisionVideoTime = 0xff;
    /**
     * 行人碰撞预警-拍照片张数
     */
    private Integer pedestrianCollisionCameraNum = 0xff;
    /**
     * 行人碰撞预警-拍照间隔
     */
    private Integer pedestrianCollisionCameraTime = 0xff;
    /**
     * 频繁变道预警-判断时间段
     */
    private Integer laneChangeTime = 0xff;
    /**
     * 频繁变道预警-判断次数
     */
    private Integer laneChangeNum = 0xff;

    /**
     * 频繁变道预警-分级速度阈值
     */
    private Integer laneChangeSpeed = 0xff;
    /**
     * 频繁变道预警-前后视频录制时间
     */
    private Integer laneChangeVideoTime = 0xff;
    /**
     * 频繁变道预警-拍照片张数
     */
    private Integer laneChangeCameraNum = 0xff;
    /**
     * 频繁变道预警-拍照间隔
     */
    private Integer laneChangeCameraTime = 0xff;

    /**
     * 保留项
     */
    private byte[] reservedItem1 = new byte[5];

    /**
     * 急加/急减/急转弯报警-分级速度阈值
     */
    private Integer quickSpeed = 0xff;
    /**
     * 急加/急减/急转弯报警-前后视频录制时间
     */
    private Integer quickVideoTime = 0xff;
    /**
     * 急加/急减/急转弯报警-拍照片张数
     */
    private Integer quickCameraNum = 0xff;

    /**
     * 急加/急减/急转弯报警-拍照间隔
     */
    private Integer quickCameraTime = 0xff;
    /**
     * 道路标志超限识别-前后视频录制时间(秒)
     */
    private Integer roadMarkingEventVideoTime = 0xff;
    /**
     * 道路标志超限识别-拍照张数
     */
    private Integer roadMarkingCameraNum = 0xff;
    /**
     * 道路标志超限识别-拍照间隔(100ms)
     */
    private Integer roadMarkingCameraTime = 0xff;
    /**
     * 道路标志超限报警-拍照张数
     */
    private Integer speedLimitCameraNum = 0xff;
    /**
     * 道路标志超限报警-前后视频录制时间
     */
    private Integer speedLimitVideoTime = 0xff;
    /**
     * 道路标志超限报警-拍照间隔(100ms)
     */
    private Integer speedLimitCameraTime = 0xff;
    /**
     * 辅助多媒体信息-辅助多媒体策略
     */
    private Integer multimediaStrategy = 0;

    /**
     * 保留项
     */
    private byte[] reservedItem = new byte[2];

    public ZhongDriverAssistance() {
    }

    public ZhongDriverAssistance(AdasParamSettingForm paramSettingForm) {
        super(paramSettingForm.getCommonParamSetting());
        for (AdasAlarmParamSetting paramSetting : paramSettingForm.getAdasAlarmParamSettings()) {
            String functionId = paramSetting.getRiskFunctionId().toString();
            // 组装使能
            handelEnable(paramSetting, functionId);
            //组装报警事件参数设置
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
            if (!ROAD_SIGNS_OVER_LIMIT_ALARM.equals(functionId)) {
                twoLevelAlarmEnable = calBinaryData(twoLevelAlarmEnable, paramSetting.getTwoLevelAlarmEnable(), index);
                twoLevelVoiceEnable =
                    calBinaryData(twoLevelVoiceEnable, paramSetting.getTwoLevelVoiceReminder(), index);
                multimediaStrategy = calBinaryData(multimediaStrategy, paramSetting.getTwoLevelAuxiliaryMultimedia(),
                    ZhongWeiParamSettingUtil.auxiliaryEnableOrderMap.get(functionId));
            }
        }
        if (ROAD_SIGN_RECOGNITION.equals(functionId)) {
            eventEnable = calBinaryData(eventEnable, paramSetting.getRoadSignRecognition(), 0);
            eventVoiceEnable = calBinaryData(eventVoiceEnable, paramSetting.getVoiceReminderEnable(), 0);
        }
        if (PERIPHERAL_STATUS.equals(functionId)) {
            eventEnable = calBinaryData(eventEnable, paramSetting.getAuxiliaryEnable(), 2);
            eventVoiceEnable = calBinaryData(eventVoiceEnable, paramSetting.getVoiceReminderEnable(), 2);
        }

    }

    private void handelAlarmParam(AdasAlarmParamSetting paramSetting, String functionId) {
        if (PERIPHERAL_STATUS.equals(functionId)) {
            return;
        }
        String[] params = ZhongWeiParamSettingUtil.assistAlarmParamMap.get(functionId).split(",");
        if (ROAD_SIGN_RECOGNITION.equals(functionId) || ROAD_SIGNS_OVER_LIMIT_ALARM.equals(functionId)) {
            //前后视频录制时间
            setValIfPresent(params[0], paramSetting.getVideoRecordingTime());
            //拍照张数
            setValIfPresent(params[1], paramSetting.getPhotographNumber());
            //拍照间隔
            setValIfPresent(params[2], paramSetting.getPhotographTime());
            return;
        }
        //时距阈值
        setValIfPresent(params[0], paramSetting.getTimeDistanceThreshold());
        //分级速度阈值
        setValIfPresent(params[1], paramSetting.getAlarmLevelSpeedThreshold());
        //报警录制时间
        setValIfPresent(params[2], paramSetting.getVideoRecordingTime());
        //报警拍照张数
        setValIfPresent(params[3], paramSetting.getPhotographNumber());
        //报警拍照间隔
        setValIfPresent(params[4], paramSetting.getPhotographTime());
        if (FREQUENTLY_CHANGE_LANES.equals(functionId)) {
            //频繁变道报警判断次数
            setValIfPresent(params[5], paramSetting.getFrequencyThreshold());
            //频繁变道报警判断时间段
            setValIfPresent(params[6], paramSetting.getTimeSlotThreshold());
        }
    }

}

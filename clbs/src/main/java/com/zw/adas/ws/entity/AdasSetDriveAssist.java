package com.zw.adas.ws.entity;

import com.zw.adas.domain.riskManagement.form.AdasRiskEventVehicleConfigForm;
import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Description:主动安全辅助系统参数
 * @Author:nixiangqian
 * @Date:Create in 2018/7/11 11:57 ADAS
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AdasSetDriveAssist extends AdasSetDrive implements T808MsgBody {

    /**
     * 驾驶员不在驾驶位置预警-时间阈值(100ms)
     */
    private Integer vehicleCollisionTime = 27;

    /**
     * 驾驶员不在驾驶位置预警-分级速度阈值(km/h)
     */
    private Integer vehicleCollisionSpeed = 50;

    /**
     * 驾驶员不在驾驶位置预警-前后视频录制时间(秒)
     */
    private Integer vehicleCollisionVideoTime = 10;

    /**
     * 驾驶员不在驾驶位置预警-拍照片张数
     */
    private Integer vehicleCollisionCameraNum = 3;

    /**
     * 驾驶员不在驾驶位置预警-拍照间隔(100ms )
     */
    private Integer vehicleCollisionCameraTime = 5;

    /**
     * 车道偏离预警-分级速度阈值(km/h)
     */
    private Integer deviateSpeed = 50;

    /**
     * 车道偏离预警-前后视频录制时间(秒)
     */
    private Integer deviateVideoTime = 10;

    /**
     * 车道偏离预警-拍照片张数
     */
    private Integer deviateCameraNum = 3;

    /**
     * 车道偏离预警-拍照间隔(100ms)
     */
    private Integer deviateCameraTime = 5;

    /**
     * 车距过近碰撞报警-距离阈值(m)
     */
    private Integer distanceMail = 30;

    /**
     * 车距碰撞预警-分级速度阈值(km/h)
     */
    private Integer distanceSpeed = 50;

    /**
     * 车距碰撞预警-前后视频录制时间(秒)
     */
    private Integer distanceVideoTime = 10;

    /**
     * 车距碰撞预警-拍照张数
     */
    private Integer distanceCameraNum = 3;

    /**
     * 车距碰撞预警-拍照间隔(100ms)
     */
    private Integer distanceCameraTime = 5;

    /**
     * 行人碰撞预警-时间阈值(100ms)
     */
    private Integer pedestrianCollisionTime = 30;

    /**
     * 行人碰撞预警-分级速度阈值(km/h)
     */
    private Integer pedestrianCollisionSpeed = 50;

    /**
     * 行人碰撞预警-前后视频录制时间(秒)
     */
    private Integer pedestrianCollisionVideoTime = 10;

    /**
     * 行人碰撞预警-拍照片张数
     */
    private Integer pedestrianCollisionCameraNum = 3;

    /**
     * 行人碰撞预警-拍照间隔(100ms)
     */
    private Integer pedestrianCollisionCameraTime = 5;

    /**
     * 频繁变道预警-判断时间段(秒)
     */
    private Integer laneChangeTime = 60;

    /**
     * 频繁变道预警-判断次数
     */
    private Integer laneChangeNum = 5;

    /**
     * 频繁变道预警-分级速度阈值(km/h)
     */
    private Integer laneChangeSpeed = 50;

    /**
     * 频繁变道预警-前后视频录制时间(秒)
     */
    private Integer laneChangeVideoTime = 10;

    /**
     * 频繁变道预警-拍照片张数
     */
    private Integer laneChangeCameraNum = 3;

    /**
     * 频繁变道预警-拍照间隔(100ms)
     */
    private Integer laneChangeCameraTime = 5;

    // /**
    //  * 障碍物预警-时间阈值(100ms)
    //  */
    // private Integer obstacleDistance = 30;
    //
    // /**
    //  * 障碍物预警-分级速度阈值(km/h)
    //  */
    // private Integer obstacleSpeed = 50;
    //
    // /**
    //  * 障碍物预警-前后视频录制时间(秒)
    //  */
    // private Integer obstacleVideoTime = 10;
    //
    // /**
    //  * 障碍物预警-拍照张数
    //  */
    // private Integer obstacleCameraNum = 3;
    //
    // /**
    //  * 障碍物预警-拍照间隔时间(100ms )
    //  */
    // private Integer obstacleCameraTime = 5;

    /**
     * 保留项（原障碍物）
     */
    private byte[] reservedItem1 = new byte[]{(byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255};
    /**
     * 道路标志识别超限-前后视频录制时间(秒)
     */
    private Integer speedLimitVideoTime = 10;

    /**
     * 道路标志识别超限-拍照张数
     */
    private Integer speedLimitCameraNum = 3;

    /**
     * 道路标志识别超限-拍照间隔(位 100ms)
     */
    private Integer speedLimitCameraTime = 5;

    /**
     * 急加/急减/急转弯报警-分级速度阈值(m/h)
     */
    private Integer quickSpeed = 50;

    /**
     * 急加/急减/急转弯报警-前后视频录制时间(秒)
     */
    private Integer quickVideoTime = 10;

    /**
     * 急加/急减/急转弯报警-拍照片张数
     */
    private Integer quickCameraNum = 3;

    /**
     * 急加/急减/急转弯报警-拍照间隔(100ms)
     */
    private Integer quickCameraTime = 5;

    /**
     * 道路标志识别-前后视频录制时间(秒)
     */
    private Integer roadMarkingEventVideoTime = 5;

    /**
     * 道路标志识别-拍照张数
     */
    private Integer roadMarkingCameraNum = 1;

    /**
     * 道路标志识别-拍照间隔(100ms)
     */
    private Integer roadMarkingCameraTime = 5;

    /**
     * 保留项
     */
    private byte[] reservedItem = new byte[2];

    public void init(AdasRiskEventVehicleConfigForm revConfig) {
        handleEventEnable(revConfig);
        handleAlarmAndVoiceEnable(revConfig);
        handleEventVoiceEnable(revConfig);

        //事件参数设置
        String eventId = revConfig.getRiskId();
        if (AdasEvent.VEHICLE_COLLISION.eq(
                eventId)) {
            //驾驶员不在驾驶位置(数据库存的是秒，协议是100毫秒，需要乘以10)
            setValIfPresent("vehicleCollisionTime", getMultiRatioData(revConfig.getTimeInterval(), 10));
            setValIfPresent("vehicleCollisionSpeed", revConfig.getHighSpeed());
            setValIfPresent("vehicleCollisionVideoTime", revConfig.getVideoRecordingTime());
            setValIfPresent("vehicleCollisionCameraNum", revConfig.getPhotographNumber());
            //数据库存的是秒，协议是100毫秒，需要乘以10
            setValIfPresent("vehicleCollisionCameraTime", parseIntFromFloat(revConfig.getPhotographTime(), 10));
            //这里取驾驶员不在驾驶位置作为ADAS该类型的代表对数据进行DSM相关参数的初始化
            initBase(revConfig);
        } else if (AdasEvent.DEVIATE.eq(eventId)) {
            //车道偏离
            setValIfPresent("deviateSpeed", revConfig.getHighSpeed());
            setValIfPresent("deviateVideoTime", revConfig.getVideoRecordingTime());
            setValIfPresent("deviateCameraNum", revConfig.getPhotographNumber());
            setValIfPresent("deviateCameraTime", parseIntFromFloat(revConfig.getPhotographTime(), 10));
        } else if (AdasEvent.DISTANCE.eq(eventId)) {
            //车距过近
            setValIfPresent("distanceMail", revConfig.getTimeInterval().intValue());
            setValIfPresent("distanceSpeed", revConfig.getHighSpeed());
            setValIfPresent("distanceVideoTime", revConfig.getVideoRecordingTime());
            setValIfPresent("distanceCameraNum", revConfig.getPhotographNumber());
            setValIfPresent("distanceCameraTime", parseIntFromFloat(revConfig.getPhotographTime(), 10));
        } else if (AdasEvent.PEDESTRIAN_COLLISION.eq(eventId)) {
            //行人碰撞(数据库存的是秒，协议是100毫秒，需要乘以10)
            setValIfPresent("pedestrianCollisionTime", getMultiRatioData(revConfig.getTimeInterval().intValue(), 10));
            setValIfPresent("pedestrianCollisionSpeed", revConfig.getHighSpeed());
            setValIfPresent("pedestrianCollisionVideoTime", revConfig.getVideoRecordingTime());
            setValIfPresent("pedestrianCollisionCameraNum", revConfig.getPhotographNumber());
            setValIfPresent("pedestrianCollisionCameraTime", parseIntFromFloat(revConfig.getPhotographTime(), 10));
        } else if (AdasEvent.LANE_CHANGE.eq(eventId)) {
            //频繁变道
            setValIfPresent("laneChangeTime", revConfig.getTimeInterval().intValue());
            setValIfPresent("laneChangeSpeed", revConfig.getHighSpeed());
            setValIfPresent("laneChangeVideoTime", revConfig.getVideoRecordingTime());
            setValIfPresent("laneChangeCameraNum", revConfig.getPhotographNumber());
            setValIfPresent("laneChangeCameraTime", parseIntFromFloat(revConfig.getPhotographTime(), 10));
        } else if (AdasEvent.SPEED_LIMIT.eq(eventId)) {
            //道路标识超限
            setValIfPresent("speedLimitVideoTime", revConfig.getVideoRecordingTime());
            setValIfPresent("speedLimitCameraNum", revConfig.getPhotographNumber());
            setValIfPresent("speedLimitCameraTime", parseIntFromFloat(revConfig.getPhotographTime(), 10));
            // } else if (AdasEvent.OBSTACLE.eq(eventId)) {
            //     //障碍物(数据库存的是秒，协议是100毫秒，需要乘以10)
            //     setValIfPresent("obstacleDistance", getMultiRatioData(revConfig.getTimeInterval().intValue(), 10));
            //     setValIfPresent("obstacleSpeed", revConfig.getHighSpeed());
            //     setValIfPresent("obstacleVideoTime", revConfig.getVideoRecordingTime());
            //     setValIfPresent("obstacleCameraNum", revConfig.getPhotographNumber());
            //     setValIfPresent("obstacleCameraTime", parseIntFromFloat(revConfig.getPhotographTime(), 10));
        } else if (AdasEvent.QUICK.eq(eventId)) {
            //急加/急减/急转弯
            setValIfPresent("quickSpeed", revConfig.getHighSpeed());
            setValIfPresent("quickVideoTime", revConfig.getVideoRecordingTime());
            setValIfPresent("quickCameraNum", revConfig.getPhotographNumber());
            setValIfPresent("quickCameraTime", parseIntFromFloat(revConfig.getPhotographTime(), 10));
        } else if (AdasEvent.ROAD_MARKING.eq(eventId)) {
            //道路标识识别
            setValIfPresent("roadMarkingEventVideoTime", revConfig.getVideoRecordingTime());
            setValIfPresent("roadMarkingCameraNum", revConfig.getPhotographNumber());
            setValIfPresent("roadMarkingCameraTime", parseIntFromFloat(revConfig.getPhotographTime(), 10));
        }

    }

    private void handleAlarmAndVoiceEnable(AdasRiskEventVehicleConfigForm revConfig) {
        String eventId = revConfig.getRiskId();
        if (AdasEvent.VEHICLE_COLLISION.eq(
                eventId)) {
            //驾驶员不在驾驶位置
            calAlarmAndVoiceEnable(revConfig, 0);
        } else if (AdasEvent.DEVIATE.eq(eventId)) {
            //车道左偏离
            calAlarmAndVoiceEnable(revConfig, 1);
            //车道右偏离
            calAlarmAndVoiceEnable(revConfig, 2);
        } else if (AdasEvent.DISTANCE.eq(eventId)) {
            //车距过近
            calAlarmAndVoiceEnable(revConfig, 3);
        } else if (AdasEvent.PEDESTRIAN_COLLISION.eq(eventId)) {
            //行人碰撞
            calAlarmAndVoiceEnable(revConfig, 4);
        } else if (AdasEvent.LANE_CHANGE.eq(eventId)) {
            //频繁变道
            calAlarmAndVoiceEnable(revConfig, 5);
        } else if (AdasEvent.SPEED_LIMIT.eq(eventId)) {
            //道路标识超限
            calAlarmAndVoiceEnable(revConfig, 6);
            // } else if (AdasEvent.OBSTACLE.eq(eventId)) {
            //     //障碍物
            //     calAlarmAndVoiceEnable(revConfig, 7);
        } else if (AdasEvent.QUICK.eq(eventId)) {
            //急加/急减/急转弯
            calAlarmAndVoiceEnable(revConfig, 7);
            calAlarmAndVoiceEnable(revConfig, 8);
            calAlarmAndVoiceEnable(revConfig, 9);
        }
    }

    private void handleEventEnable(AdasRiskEventVehicleConfigForm revConfig) {
        if (AdasEvent.ROAD_MARKING.eq(revConfig.getRiskId())) {
            //道路标识事件使能
            eventEnable = calBinaryData(eventEnable, revConfig.getRoadMarkAlarmEnable(), 0);
            //道路标识属于adas的一种，所以取它主动抓拍使能即可代表adas
            eventEnable = calBinaryData(eventEnable, revConfig.getInitiativeCaptureAlarmEnable(), 1);
        }

    }

    private void handleEventVoiceEnable(AdasRiskEventVehicleConfigForm revConfig) {
        if (AdasEvent.ROAD_MARKING.eq(revConfig.getRiskId())) {
            //道路标识识别事件语音使能
            eventVoiceEnable = calBinaryData(eventVoiceEnable, revConfig.getVoiceEnable(), 0);
            //道路标识属于adas的一种，所以取它主动抓语音拍使能即可代表adas
            eventVoiceEnable = calBinaryData(eventVoiceEnable, revConfig.getInitiativeCaptureVoiceEnable(), 1);
        }

    }


}

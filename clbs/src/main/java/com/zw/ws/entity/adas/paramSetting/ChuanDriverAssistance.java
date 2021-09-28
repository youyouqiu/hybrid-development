package com.zw.ws.entity.adas.paramSetting;

import com.zw.adas.domain.define.setting.AdasAlarmParamSetting;
import com.zw.adas.domain.define.setting.query.AdasParamSettingForm;
import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.Map;


/**
 * @Description:主动安全参数(川标) (前向参数设置)
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ChuanDriverAssistance extends PublicParameters implements T808MsgBody {
    /**
     * 报警使能
     */
    private Long alarmEnable = 0L;

    /**
     * 事件使能
     */
    private Long eventEnable = 2L;

    /**
     * 保留项
     */
    private byte[] keep1 = new byte[1];

    /**
     * 障碍物预警-时间阈值(100ms)
     */
    private Integer obstacleDistance = 0xff;

    /**
     * 障碍物预警-分级速度阈值(km/h)
     */
    private Integer obstacleSpeed = 0xff;

    /**
     * 障碍物预警-前后视频录制时间(秒)
     */
    private Integer obstacleVideoTime = 0xff;

    /**
     * 障碍物预警-拍照张数
     */
    private Integer obstacleCameraNum = 0xff;

    /**
     * 障碍物预警-拍照间隔时间(100ms )
     */
    private Integer obstacleCameraTime = 0xff;

    /**
     * 频繁变道预警-判断时间段(秒)
     */
    private Integer laneChangeTime = 0xff;

    /**
     * 频繁变道预警-判断次数
     */
    private Integer laneChangeNum = 0xff;

    /**
     * 频繁变道预警-分级速度阈值(km/h)
     */
    private Integer laneChangeSpeed = 0xff;

    /**
     * 频繁变道预警-前后视频录制时间(秒)
     */
    private Integer laneChangeVideoTime = 0xff;

    /**
     * 频繁变道预警-拍照片张数
     */
    private Integer laneChangeCameraNum = 0xff;

    /**
     * 频繁变道预警-拍照间隔(100ms)
     */
    private Integer laneChangeCameraTime = 0xff;

    /**
     * 车道偏离预警-分级速度阈值(km/h)
     */
    private Integer deviateSpeed = 0xff;

    /**
     * 车道偏离预警-前后视频录制时间(秒)
     */
    private Integer deviateVideoTime = 0xff;

    /**
     * 车道偏离预警-拍照片张数
     */
    private Integer deviateCameraNum = 0xff;

    /**
     * 车道偏离预警-拍照间隔(100ms)
     */
    private Integer deviateCameraTime = 0xff;

    /**
     * 驾驶员不在驾驶位置预警-时间阈值(100ms)
     */
    private Integer vehicleCollisionTime = 0xff;

    /**
     * 驾驶员不在驾驶位置预警-分级速度阈值(km/h)
     */
    private Integer vehicleCollisionSpeed = 0xff;

    /**
     * 驾驶员不在驾驶位置预警-前后视频录制时间(秒)
     */
    private Integer vehicleCollisionVideoTime = 0xff;

    /**
     * 驾驶员不在驾驶位置预警-拍照片张数
     */
    private Integer vehicleCollisionCameraNum = 0xff;

    /**
     * 驾驶员不在驾驶位置预警-拍照间隔(100ms )
     */
    private Integer vehicleCollisionCameraTime = 0xff;

    /**
     * 行人碰撞预警-时间阈值(100ms)
     */
    private Integer pedestrianCollisionTime = 0xff;

    /**
     * 行人碰撞预警-分级速度阈值(km/h)
     */
    private Integer pedestrianCollisionSpeed = 0xff;

    /**
     * 行人碰撞预警-前后视频录制时间(秒)
     */
    private Integer pedestrianCollisionVideoTime = 0xff;

    /**
     * 行人碰撞预警-拍照片张数
     */
    private Integer pedestrianCollisionCameraNum = 0xff;

    /**
     * 行人碰撞预警-拍照间隔(100ms)
     */
    private Integer pedestrianCollisionCameraTime = 0xff;

    /**
     * 车距过近碰撞报警-距离阈值(m)
     */
    private Integer distanceMail = 0xff;

    /**
     * 车距碰撞预警-分级速度阈值(km/h)
     */
    private Integer distanceSpeed = 0xff;

    /**
     * 车距碰撞预警-前后视频录制时间(秒)
     */
    private Integer distanceVideoTime = 0xff;

    /**
     * 车距碰撞预警-拍照张数
     */
    private Integer distanceCameraNum = 0xff;

    /**
     * 车距碰撞预警-拍照间隔(100ms)
     */
    private Integer distanceCameraTime = 0xff;

    /**
     * 道路标志识别超限-拍照张数
     */
    private Integer speedLimitCameraNum = 0xff;

    /**
     * 道路标志识别超限-拍照间隔(位 100ms)
     */
    private Integer speedLimitCameraTime = 0xff;

    /**
     * 保留项
     */
    private byte[] keep2 = new byte[4];

    /**
     * 川冀标前向使能顺序
     */
    private static Map<String, Object> assistAlarmEnableMap = new HashMap();

    /**
     * 川冀标前向事件参数交互字段维护
     */
    private static Map<String, Object> assistAlarmParamMap = new HashMap();

    /**
     * 川标使能顺序
     * 障碍物 126407
     * 频繁变道 126405
     *车道偏离 126402
     *前向碰撞 126401
     *行人碰撞 126404
     *车距过近 126403
     *道路标识超限 126409
     */
    static {
        //报警使能顺序维护
        String[][] alarmEnableOrder =
            {{"126407", "0,1"}, {"126405", "2,3"}, {"126402", "4,5"}, {"126401", "6,7"}, {"126404", "8,9"},
                {"126403", "10,11"}, {"126409", "16"}};
        for (String[] ints : alarmEnableOrder) {
            assistAlarmEnableMap.put(ints[0], ints[1]);
        }
        //报警事件参数设置参数交互字段维护
        String[][] assistAlarmParamOrder =
            {{"126407", "obstacleDistance,obstacleSpeed,obstacleVideoTime,obstacleCameraNum,obstacleCameraTime"},
                {"126405", "laneChangeTime,laneChangeSpeed,laneChangeVideoTime,laneChangeCameraNum,"
                    + "laneChangeCameraTime,laneChangeNum"},
                {"126402", "null,deviateSpeed,deviateVideoTime,deviateCameraNum,deviateCameraTime"},
                {"126401", "vehicleCollisionTime,vehicleCollisionSpeed,vehicleCollisionVideoTime,"
                    + "vehicleCollisionCameraNum,vehicleCollisionCameraTime"},
                {"126404", "pedestrianCollisionTime,pedestrianCollisionSpeed,pedestrianCollisionVideoTime,"
                    + "pedestrianCollisionCameraNum,pedestrianCollisionCameraTime"},
                {"126403",
                    "distanceMail,distanceSpeed,distanceVideoTime,distanceCameraNum,distanceCameraTime"},
                {"126409", "speedLimitCameraNum,speedLimitCameraTime"}
            };
        for (String[] ints : assistAlarmParamOrder) {
            assistAlarmParamMap.put(ints[0], ints[1]);
        }
    }

    public ChuanDriverAssistance(AdasParamSettingForm paramSettingForm) {
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

    private void handelAlarmParam(AdasAlarmParamSetting paramSetting, String key) {
        String[] params = assistAlarmParamMap.get(key).toString().split(",");
        //道路标识
        if ("126409".equals(key)) {
            //报警拍照张数
            setValIfPresent(params[0],
                paramSetting.getPhotographNumber() != null ? paramSetting.getPhotographNumber() : 0xff);
            //报警拍照间隔
            setValIfPresent(params[1],
                paramSetting.getPhotographTime() != null ? paramSetting.getPhotographTime() : 0xff);
            return;
        }
        //126402 车道偏离,126405 频繁变道
        if (!"126402".equals(key) || !"126405".equals(key)) {
            //时距阈值
            setValIfPresent(params[0], paramSetting.getTimeDistanceThreshold());
        }
        //分级速度阈值
        setValIfPresent(params[1], paramSetting.getAlarmLevelSpeedThreshold());
        //报警录制时间
        setValIfPresent(params[2], paramSetting.getVideoRecordingTime());
        //报警拍照张数
        setValIfPresent(params[3], paramSetting.getPhotographNumber());
        //报警拍照间隔
        setValIfPresent(params[4], paramSetting.getPhotographTime());
        if ("126405".equals(key)) {
            //频繁变道报警判断时间段
            setValIfPresent(params[0], paramSetting.getTimeSlotThreshold());
            //频繁变道报警判断次数
            setValIfPresent(params[5], paramSetting.getFrequencyThreshold());
        }
    }

    private void handleEventEnable(AdasAlarmParamSetting paramSetting, String key) {
        //道路标识超限
        if ("126409".equals(key)) {
            eventEnable = calBinaryData(eventEnable, paramSetting.getRoadSignRecognition(), 0);
        }

    }

    private void handelAlarmEnable(AdasAlarmParamSetting paramSetting, String key) {
        //道路标识超限
        if ("126409".equals(key)) {
            alarmEnable = calBinaryData(alarmEnable, paramSetting.getRoadSignEnable(),
                Integer.parseInt(assistAlarmEnableMap.get(key).toString()));
            return;
        }
        String[] order = assistAlarmEnableMap.get(key).toString().split(",");
        alarmEnable = calBinaryData(alarmEnable, paramSetting.getOneLevelAlarmEnable(), Integer.parseInt(order[0]));
        alarmEnable = calBinaryData(alarmEnable, paramSetting.getTwoLevelAlarmEnable(), Integer.parseInt(order[1]));
    }

}

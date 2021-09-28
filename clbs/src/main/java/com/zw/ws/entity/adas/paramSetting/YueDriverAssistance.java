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
 * @Description:主动安全参数(粤标) (高级驾驶辅助)
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class YueDriverAssistance extends PublicParameters implements T808MsgBody {
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
     * 前向碰撞报警-时间阈值(100ms)
     */
    private Integer vehicleCollisionTime = 0xff;

    /**
     * 前向碰撞报警-分级速度阈值(km/h)
     */
    private Integer vehicleCollisionSpeed = 0xff;

    /**
     * 前向碰撞报警-前后视频录制时间(秒)
     */
    private Integer vehicleCollisionVideoTime = 0xff;

    /**
     * 前向碰撞报警-拍照片张数
     */
    private Integer vehicleCollisionCameraNum = 0xff;

    /**
     * 前向碰撞报警-拍照间隔(100ms )
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
     * 实线变道报警分级速度
     */
    private Integer fullTimeSpeed = 0xff;

    /**
     * 实线变道前后视频录制时间
     */
    private Integer fullTimeVideoTime = 0xff;

    /**
     * 实线变道报警拍照张数
     */
    private Integer fullTimeCameraNum = 0xff;

    /**
     * 实线变道报警拍照间隔
     */
    private Integer fullTimeCameraTime = 0xff;

    /**
     * 车厢过道行人检测报警分级速度阈值
     */
    private Integer pedestrianInspectSpeed = 0xff;

    /**
     * 车厢过道行人检测报警前后视频录制时间
     */
    private Integer pedestrianInspectVideoTime = 0xff;
    /**
     * 车厢过道行人检测报警拍照张数
     */
    private Integer pedestrianInspectCameraNum = 0xff;

    /**
     * 车厢过道行人检测报警拍照间隔
     */
    private Integer pedestrianInspectCameraTime = 0xff;
    /**
     * 报警使能顺序
     */

    private static Map<String, List<Integer>> alarmEnableMap = new HashMap<>();
    /**
     * 报警参数
     */

    private static Map<String, List<String>> alarmParamMap = new HashMap<>();
    /**
     * 事件使能  车厢过道行人监测
     */
    private static Map<String, List<Integer>> eventEnableMap = new HashMap<>();

    static {
        //报警使能顺序维护
        alarmEnableMap.put("286407", Arrays.asList(0, 1));
        alarmEnableMap.put("286405", Arrays.asList(2, 3));
        alarmEnableMap.put("286402", Arrays.asList(4, 5));
        alarmEnableMap.put("286401", Arrays.asList(6, 7));
        alarmEnableMap.put("286404", Arrays.asList(8, 9));
        alarmEnableMap.put("286403", Arrays.asList(10, 11));
        alarmEnableMap.put("286406", Arrays.asList(16));
        //新增后期
        alarmEnableMap.put("286410", Arrays.asList(17, 18));
        alarmEnableMap.put("286411", Arrays.asList(19));
        //报警参数字段顺序维护
        alarmParamMap.put("286407", Arrays
            .asList("obstacleCameraTime", "obstacleCameraNum", "obstacleVideoTime", "obstacleSpeed",
                "obstacleDistance"));
        alarmParamMap.put("286405", Arrays
            .asList("laneChangeCameraTime", "laneChangeCameraNum", "laneChangeVideoTime", "laneChangeSpeed",
                "laneChangeTime", "laneChangeNum"));
        alarmParamMap
            .put("286402", Arrays.asList("deviateCameraTime", "deviateCameraNum", "deviateVideoTime", "deviateSpeed"));

        alarmParamMap.put("286401", Arrays
            .asList("vehicleCollisionCameraTime", "vehicleCollisionCameraNum", "vehicleCollisionVideoTime",
                "vehicleCollisionSpeed", "vehicleCollisionTime"));
        alarmParamMap.put("286404", Arrays
            .asList("pedestrianCollisionCameraTime", "pedestrianCollisionCameraNum", "pedestrianCollisionVideoTime",
                "pedestrianCollisionSpeed", "pedestrianCollisionTime"));
        alarmParamMap.put("286403", Arrays
            .asList("distanceCameraTime", "distanceCameraNum", "distanceVideoTime", "distanceSpeed", "distanceMail"));
        alarmParamMap.put("286406", Arrays.asList("speedLimitCameraTime", "speedLimitCameraNum"));

        alarmParamMap.put("286410",

            Arrays.asList("fullTimeCameraTime", "fullTimeCameraNum", "fullTimeVideoTime", "fullTimeSpeed"));
        alarmParamMap.put("286411", Arrays
            .asList("pedestrianInspectCameraTime", "pedestrianInspectCameraNum", "pedestrianInspectVideoTime",
                "pedestrianInspectSpeed"));

        //事件使能
        eventEnableMap.put("286406", Arrays.asList(0, 1));
    }

    public YueDriverAssistance(AdasParamSettingForm paramSettingForm) {
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
        List<String> fields = alarmParamMap.get(key);
        //必须严格按照顺序来，不能错乱
        setValIfPresent(fields.get(0), paramSetting.getPhotographTime());
        setValIfPresent(fields.get(1), paramSetting.getPhotographNumber());
        //是否是道路标志识别
        if (isRoadSignRecognition(fields.size())) {
            return;
        }
        setValIfPresent(fields.get(2), paramSetting.getVideoRecordingTime());
        setValIfPresent(fields.get(3), paramSetting.getAlarmLevelSpeedThreshold());
        //是否是车道偏离
        if (isDeviate(fields)) {
            return;
        }
        //是否是频繁变道
        if (isLaneChange(fields)) {
            setValIfPresent(fields.get(4), paramSetting.getTimeSlotThreshold());
            setValIfPresent(fields.get(5), paramSetting.getFrequencyThreshold());
            return;
        }
        setValIfPresent(fields.get(4), paramSetting.getTimeDistanceThreshold());
    }

    private boolean isDeviate(List<String> fields) {
        return fields.size() == 4;
    }

    private boolean isLaneChange(List<String> fields) {
        return fields.size() == 6;
    }

    private boolean isRoadSignRecognition(int size) {
        return size == 2;
    }

    private void handleEventEnable(AdasAlarmParamSetting paramSetting, String key) {
        List<Integer> positions = eventEnableMap.get(key);
        if (positions != null) {
            eventEnable = calBinaryData(eventEnable, paramSetting.getRoadSignRecognition(), positions.get(0));
            eventEnable = calBinaryData(eventEnable, paramSetting.getInitiativePictureEnable(), positions.get(1));
        }

    }

    private void handelAlarmEnable(AdasAlarmParamSetting paramSetting, String key) {
        List<Integer> position = alarmEnableMap.get(key);
        if (position == null) {
            return;
        }
        //是否是道路标识超限和车厢过道行人检测报警（没有一级二级）
        boolean isRoadSign = key.equals("286406");

        if (isRoadSign) {
            alarmEnable = calBinaryData(alarmEnable, paramSetting.getRoadSignEnable(), position.get(0));
            return;
        }
        boolean isPedestrianInspect = key.equals("286411");
        if (isPedestrianInspect) {
            alarmEnable = calBinaryData(alarmEnable, paramSetting.getPedestrianInspect(), position.get(0));
            return;
        }

        alarmEnable = calBinaryData(alarmEnable, paramSetting.getOneLevelAlarmEnable(), position.get(0));
        alarmEnable = calBinaryData(alarmEnable, paramSetting.getTwoLevelAlarmEnable(), position.get(1));
    }

}

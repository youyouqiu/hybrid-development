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
 * @Description:主动安全参数(湘标) (高级驾驶辅助参数设置)
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class XiangDriverAssistance extends PublicParameters implements T808MsgBody {
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
    private byte[] keep1 = new byte[13];

    /**
     * 障碍物预警-时间阈值(100ms)
     */
    private Integer obstacleDistance = 0xff;

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
     * 前车碰撞预警-时间阈值(100ms)
     */
    private Integer vehicleCollisionTime = 0xff;

    /**
     * 前车碰撞预警-前后视频录制时间(秒)
     */
    private Integer vehicleCollisionVideoTime = 0xff;

    /**
     * 前车碰撞预警-拍照片张数
     */
    private Integer vehicleCollisionCameraNum = 0xff;

    /**
     * 前车碰撞预警-拍照间隔(100ms )
     */
    private Integer vehicleCollisionCameraTime = 0xff;
    /**
     * 行人碰撞预警分级速度阈值
     */
    private Integer pedestrianCollisionSpeed = 0xff;

    /**
     * 行人碰撞预警-时间阈值(100ms)
     */
    private Integer pedestrianCollisionTime = 0xff;

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
     * 保留项
     */
    private byte[] keep2 = new byte[20];

    /**
     * 车道偏离联动上传主码流视频同
     */
    private Integer deviateStreamChannelNum = 0;

    /**
     * 车道偏离联动上传子码流视频通道
     */
    private Integer deviateSubStreamChannelNum = 0;

    /**
     * 车道偏离联动上传抓拍通道
     */
    private Integer deviateCaptureChannelNum = 0;

    /**
     * 高级驾驶辅助碰撞联动上传主码流视频通道
     */
    private Integer vehicleStreamChannelNum = 0;

    /**
     * 高级驾驶辅助碰撞联动上传子码流视频通道
     */
    private Integer vehicleSubStreamChannelNum = 0;

    /**
     * 高级驾驶辅助碰撞联动上传抓拍通道
     */
    private Integer vehicleCaptureChannelNum = 0;

    /**
     * 行人碰撞联动上传主码流视频同
     */
    private Integer pedestrianStreamChannelNum = 0;

    /**
     * 行人碰撞联动上传子码流视频通道
     */
    private Integer pedestrianSubStreamChannelNum = 0;

    /**
     * 行人碰撞联动上传抓拍通道
     */
    private Integer pedestrianCaptureChannelNum = 0;

    /**
     * 车距过近联动上传主码流视频同
     */
    private Integer distanceStreamChannelNum = 0;

    /**
     * 车距过近联动上传子码流视频通道
     */
    private Integer distanceSubStreamChannelNum = 0;

    /**
     * 车距过近联动上传抓拍通道
     */
    private Integer distanceCaptureChannelNum = 0;

    /**
     * 保留项
     */
    private byte[] keep3 = new byte[16];

    /**
     * 吉标高级驾驶辅助事件参数交互字段维护
     */
    private static Map<String, Object> assistAlarmParamMap = new HashMap();

    /**
     * 报警使能顺序
     */

    private static Map<String, Integer> alarmEnableMap = new HashMap<>();
    /**
     * 报警参数
     */

    private static Map<String, List<String>> alarmParamMap = new HashMap<>();
    /**
     * 事件使能
     */
    private static Map<String, List<Integer>> eventEnableMap = new HashMap<>();

    static {

        //报警使能顺序维护
        alarmEnableMap.put("-276402", 4);
        alarmEnableMap.put("-276401", 6);
        alarmEnableMap.put("-276404", 8);
        alarmEnableMap.put("-276403", 10);
        //报警参数字段顺序维护
        alarmParamMap.put("-276402", Arrays
            .asList("deviateCameraTime", "deviateCameraNum", "deviateVideoTime", "deviateStreamChannelNum",
                "deviateSubStreamChannelNum", "deviateCaptureChannelNum"));
        alarmParamMap.put("-276401", Arrays
            .asList("vehicleCollisionCameraTime", "vehicleCollisionCameraNum", "vehicleCollisionVideoTime",
                "vehicleStreamChannelNum", "vehicleSubStreamChannelNum", "vehicleCaptureChannelNum",
                "vehicleCollisionTime"));
        alarmParamMap.put("-276404", Arrays
            .asList("pedestrianCollisionCameraTime", "pedestrianCollisionCameraNum", "pedestrianCollisionVideoTime",
                "pedestrianStreamChannelNum", "pedestrianSubStreamChannelNum", "pedestrianCaptureChannelNum",
                "pedestrianCollisionTime", "pedestrianCollisionSpeed"));

        alarmParamMap.put("-276403", Arrays
            .asList("distanceCameraTime", "distanceCameraNum", "distanceVideoTime", "distanceStreamChannelNum",
                "distanceSubStreamChannelNum", "distanceCaptureChannelNum", "distanceMail"));

        alarmParamMap.put("276405", Arrays.asList("speedLimitCameraNum"));
        //事件使能
        eventEnableMap.put("276405", Arrays.asList(0, 1));

    }

    public XiangDriverAssistance(AdasParamSettingForm paramSettingForm) {
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
        //是否是道路标志识别
        if (isRoadSignRecognition(fields.size())) {
            setValIfPresent(fields.get(0), paramSetting.getPhotographNumber());
            return;
        }
        //必须严格按照顺序来，不能错乱
        setValIfPresent(fields.get(0), paramSetting.getPhotographTime());
        setValIfPresent(fields.get(1), paramSetting.getPhotographNumber());

        setValIfPresent(fields.get(2), paramSetting.getVideoRecordingTime());
        setValIfPresent(fields.get(3), getFinalChannelVal(paramSetting.getPrimaryChannel()));
        setValIfPresent(fields.get(4), getFinalChannelVal(paramSetting.getSubcodeChannel()));
        setValIfPresent(fields.get(5), getFinalChannelVal(paramSetting.getCaptureChannel()));

        //是否是前向碰撞
        if (isVehicleCollision(fields)) {
            setValIfPresent(fields.get(6), paramSetting.getTimeDistanceThreshold());
            return;
        }

        //是否是行人碰撞
        if (isPedestrianCollision(fields)) {
            setValIfPresent(fields.get(6), paramSetting.getTimeDistanceThreshold());
            setValIfPresent(fields.get(7), parseIntData(paramSetting.getSpeedThreshold()));
            return;
        }

    }

    private boolean isVehicleCollision(List<String> fields) {
        return fields.size() == 7;
    }

    private boolean isPedestrianCollision(List<String> fields) {
        return fields.size() == 8;
    }

    private boolean isRoadSignRecognition(int size) {
        return size == 1;
    }

    private void handleEventEnable(AdasAlarmParamSetting paramSetting, String key) {
        List<Integer> positions = eventEnableMap.get(key);
        if (positions != null) {
            eventEnable = calBinaryData(eventEnable, paramSetting.getRoadSignRecognition(), positions.get(0));
            eventEnable = calBinaryData(eventEnable, paramSetting.getInitiativePictureEnable(), positions.get(1));
        }

    }

    private void handelAlarmEnable(AdasAlarmParamSetting paramSetting, String key) {
        Integer position = alarmEnableMap.get(key);
        if (position == null) {
            return;
        }
        alarmEnable = calBinaryData(alarmEnable, paramSetting.getAlarmEnable(), position);
    }

}

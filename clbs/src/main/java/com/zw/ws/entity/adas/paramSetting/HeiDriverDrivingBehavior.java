package com.zw.ws.entity.adas.paramSetting;

import com.zw.adas.domain.define.setting.AdasAlarmParamSetting;
import com.zw.adas.domain.define.setting.query.AdasParamSettingForm;
import lombok.Data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: zjc
 * @Description:驾驶员驾驶行为参数下发参数实体（黑标）
 * @Date: create in 2020/11/24 14:23
 */
@Data
public class HeiDriverDrivingBehavior extends PublicParameters {
    /**
     * 抽烟报警判断时间间隔
     */
    private Integer smokingTime = 0xFF;
    /**
     * 手持接打电话报警判断时间间隔
     */
    private Integer pickUpTime = 0xFF;
    /**
     * 疲劳驾驶状态报警前后视频录制时间
     */
    private Integer fatigueVideoTime = 0xFF;
    /**
     * 疲劳驾驶报警拍照张数
     */
    private Integer fatigueCameraNum = 0xFF;
    /**
     * 疲劳驾驶报警拍照间隔时间
     */
    private Integer fatigueCameraTime = 0xFF;
    /**
     * 手持接打电话报警前后视频录制时间
     */
    private Integer pickUpVideoTime = 0xFF;
    /**
     * 手持接打电话报警拍照张数
     */
    private Integer pickUpCameraNum = 0xFF;
    /**
     * 手持接打电话报警拍照间隔时间
     */
    private Integer pickUpCameraTime = 0xFF;
    /**
     * 长时间不目视前方报警前后视频录制时间
     */
    private Integer longTimeLookVideoTime = 0xFF;
    /**
     * 长时间不目视前方报警拍照张数
     */
    private Integer longTimeLookCameraNum = 0xFF;
    /**
     * 长时间不目视前方报警拍照间隔时间
     */
    private Integer longTimeLookCameraTime = 0xFF;
    /**
     * 驾驶员不在驾驶位置报警前后视频录制时间
     */
    private Integer driverNotPositionVideoTime = 0xFF;
    /**
     * 驾驶员不在驾驶位置报警拍照张数
     */
    private Integer driverNotPositionCameraNum = 0xFF;
    /**
     * 驾驶员不在驾驶位置报警拍照间隔时间
     */
    private Integer driverNotPositionCameraTime = 0xFF;
    /**
     * 抽烟报警前后视频录制时间
     */
    private Integer smokingVideoTime = 0xFF;
    /**
     * 抽烟报警拍照张数
     */
    private Integer smokingCameraNum = 0xFF;
    /**
     * 抽烟报警拍照间隔时间
     */
    private Integer smokingCameraTime = 0xFF;
    /**
     * 保留字段
     */
    private byte[] keep1 = new byte[10];

    private static Map<String, List<String>> alarmParamMap = new HashMap<>();

    static {
        //报警类型设置那几个字段
        alarmParamMap.put("254001", Arrays.asList("fatigueVideoTime", "fatigueCameraTime", "fatigueCameraNum"));
        alarmParamMap
            .put("254002", Arrays.asList("pickUpVideoTime", "pickUpCameraTime", "pickUpCameraNum", "pickUpTime"));
        alarmParamMap
            .put("254003", Arrays.asList("longTimeLookVideoTime", "longTimeLookCameraTime", "longTimeLookCameraNum"));
        alarmParamMap
            .put("254004", Arrays.asList("smokingVideoTime", "smokingCameraTime", "smokingCameraNum", "smokingTime"));
        alarmParamMap.put("254005",
            Arrays.asList("driverNotPositionVideoTime", "driverNotPositionCameraTime", "driverNotPositionCameraNum"));
    }

    public HeiDriverDrivingBehavior(AdasParamSettingForm paramSettingForm) {
        super(paramSettingForm.getCommonParamSetting());
        for (AdasAlarmParamSetting paramSetting : paramSettingForm.getAdasAlarmParamSettings()) {
            String functionId = paramSetting.getRiskFunctionId().toString();
            //组装报警事件参数设置
            handelAlarmParam(paramSetting, functionId);
        }
    }

    private void handelAlarmParam(AdasAlarmParamSetting paramSetting, String functionId) {
        List<String> fields = alarmParamMap.get(functionId);
        //这里需要严格控制顺序，不能错乱
        setValIfPresent(fields.get(0), paramSetting.getVideoRecordingTime());
        setValIfPresent(fields.get(1), paramSetting.getPhotographTime());
        setValIfPresent(fields.get(2), paramSetting.getPhotographNumber());

        //手持接打电话和抽烟（多出一个触发报警时间间隔）
        if (isPhoneAndSmoke(fields.size())) {
            setValIfPresent(fields.get(3), paramSetting.getTimeSlotThreshold());
        }

    }

    /**
     * 是否是抽烟和接打电话
     * @param size
     * @return
     */
    private boolean isPhoneAndSmoke(int size) {
        return size == 4;
    }
}

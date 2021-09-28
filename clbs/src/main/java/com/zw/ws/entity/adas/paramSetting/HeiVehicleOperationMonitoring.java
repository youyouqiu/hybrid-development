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
 * @Description: 车辆运行监测参数下发实体（黑标）
 * @Date: create in 2020/11/24 11:30
 */
@Data
public class HeiVehicleOperationMonitoring extends PublicParameters {
    /**
     * 前向碰撞预警、报警区分速度阈值
     */
    private Integer collisionSpeed = 0xFF;
    /**
     * 前向碰撞预警时间阈值
     */
    private Integer collisionWarnTime = 0xFF;
    /**
     * 前向碰撞报警时间阈值
     */
    private Integer collisionAlarmTime = 0xFF;
    /**
     * 前向碰撞报警前后视频录制时间
     */
    private Integer collisionVideoTime = 0xFF;
    /**
     * 前向碰撞报警拍照张数
     */
    private Integer collisionCameraNum = 0xFF;
    /**
     * 前向碰撞报警拍照间隔
     */
    private Integer collisionCameraTime = 0xFF;
    /**
     * 车道偏离预、报警判断速度阈值
     */
    private Integer deviateSpeed = 0xFF;
    /**
     * 车道偏离报警前后视频录制时间
     */
    private Integer deviateVideoTime = 0xFF;
    /**
     * 车道偏离报警拍照张数
     */
    private Integer deviateCameraNum = 0xFF;
    /**
     * 车道偏离报警拍照间隔
     */
    private Integer deviateCameraTime = 0xFF;
    /**
     * 超速报警前后视频录制时间
     */
    private Integer speedingVideoTime = 0xFF;
    /**
     * 超速报警拍照张数
     */
    private Integer speedingCameraNum = 0xFF;
    /**
     * 超速报警拍照间隔
     */
    private Integer speedingCameraTime = 0xFF;
    /**
     * 路线偏离报警前后视频录制时间
     */
    private Integer deviationVideoTime = 0xFF;
    /**
     * 路线偏离报警拍照张数
     */
    private Integer deviationCameraNum = 0xFF;
    /**
     * 路线偏离报警拍照间隔
     */
    private Integer deviationCameraTime = 0xFF;
    /**
     * 禁行路段/区域报警前后视频录制时间
     */
    private Integer banTravelVideoTime = 0xFF;
    /**
     * 禁行路段/区域报警拍照张数
     */
    private Integer banTravelCameraNum = 0xFF;
    /**
     * 禁行路段/区域报警拍照间隔
     */
    private Integer banTravelCameraTime = 0xFF;
    /**
     * 保留字段
     */
    private byte[] keep1 = new byte[10];

    private static Map<String, List<String>> alarmParamMap = new HashMap<>();

    static {
        //报警类型设置那几个字段
        alarmParamMap.put("253901", Arrays
            .asList("collisionVideoTime", "collisionCameraTime", "collisionCameraNum", "collisionSpeed",
                "collisionAlarmTime", "collisionWarnTime"));
        alarmParamMap
            .put("253902", Arrays.asList("deviateVideoTime", "deviateCameraTime", "deviateCameraNum", "deviateSpeed"));
        alarmParamMap.put("253903", Arrays.asList("speedingVideoTime", "speedingCameraTime", "speedingCameraNum"));
        alarmParamMap.put("253904", Arrays.asList("deviationVideoTime", "deviationCameraTime", "deviationCameraNum"));
        alarmParamMap.put("253905", Arrays.asList("banTravelVideoTime", "banTravelCameraTime", "banTravelCameraNum"));

    }

    public HeiVehicleOperationMonitoring(AdasParamSettingForm paramSettingForm) {
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

        //车道偏离预、报警（多出1个速度阈值）
        if (isDeviate(fields.size())) {
            setValIfPresent(fields.get(3), parseIntData(paramSetting.getSpeedThreshold()));
        }
        //前向碰撞预警、报警(多出了三个（速度阈值报警时间间隔和预警时间间隔）)
        if (isCollision(fields.size())) {
            setValIfPresent(fields.get(3), parseIntData(paramSetting.getSpeedThreshold()));
            setValIfPresent(fields.get(4), paramSetting.getTimeThreshold());
            setValIfPresent(fields.get(5), paramSetting.getWarningTimeThreshold());
        }
    }

    /**
     * 是否是车道路，偏离
     * @param size
     * @return
     */
    private boolean isDeviate(int size) {
        return size == 4;
    }

    /**
     * 是否是前向碰撞
     * @param size
     * @return
     */
    private boolean isCollision(int size) {
        return size == 6;
    }
}

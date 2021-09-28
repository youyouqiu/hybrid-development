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
 * @Description:设备失效监测参数下发实体（黑标）
 * @Date: create in 2020/11/24 14:51
 */
@Data
public class HeiEquipmentFailureMonitoring extends PublicParameters {
    /**
     * 遮挡失效报警判断时间间隔
     */
    private Integer occlusionTime = 0xFF;
    /**
     * 红外阻断型墨镜失效报警判断时间间隔
     */
    private Integer infraRedBlockTime = 0xFF;
    /**
     * 遮挡失效报警前后视频录制时间
     */
    private Integer occlusionVideoTime = 0xFF;
    /**
     * 遮挡失效报警拍照张数
     */
    private Integer occlusionCameraNum = 0xFF;
    /**
     * 遮挡失效报警拍照间隔时间
     */
    private Integer occlusionCameraTime = 0xFF;
    /**
     * 红外阻断型墨镜失效报警前后视频录制时间
     */
    private Integer infraRedBlockVideoTime = 0xFF;
    /**
     * 红外阻断型墨镜失效报警拍照张数
     */
    private Integer infraRedBlockCameraNum = 0xFF;
    /**
     * 红外阻断型墨镜失效报警拍照间隔时间
     */
    private Integer infraRedBlockCameraTime = 0xFF;

    /**
     * 主存储器异常报警前后视频录制时间
     */
    private Integer mainMemErrVideoTime = 0xFF;
    /**
     * 主存储器异常报警拍照张数
     */
    private Integer mainMemErrCameraNum = 0xFF;
    /**
     * 主存储器异常报警拍照间隔时间
     */
    private Integer mainMemErrCameraTime = 0xFF;
    /**
     * 备用存储器异常报警前后视频录制时间
     */
    private Integer spareMemErrVideoTime = 0xFF;
    /**
     * 备用存储器异常报警拍照张数
     */
    private Integer spareMemErrCameraNum = 0xFF;
    /**
     * 备用存储器异常报警拍照间隔时间
     */
    private Integer spareMemErrCameraTime = 0xFF;
    /**
     * 卫星信号异常前后视频录制时间
     */
    private Integer gpsSignalVideoTime = 0xFF;
    /**
     * 卫星信号异常拍照张数
     */
    private Integer gpsSignalCameraNum = 0xFF;
    /**
     * 卫星信号异常拍照间隔时间
     */
    private Integer gpsSignalCameraTime = 0xFF;
    /**
     * 通信信号异常前后视频录制时间
     */
    private Integer signalVideoTime = 0xFF;
    /**
     * 通信信号异常拍照张数
     */
    private Integer signalCameraNum = 0xFF;
    /**
     * 通信信号异常拍照间隔时间
     */
    private Integer signalCameraTime = 0xFF;
    /**
     * 备用电池欠压前后视频录制时间
     */
    private Integer batteryVoltageVideoTime = 0xFF;
    /**
     * 备用电池欠压常拍照张数
     */
    private Integer batteryVoltageCameraNum = 0xFF;
    /**
     * 备用电池欠压拍照间隔时间
     */
    private Integer batteryVoltageCameraTime = 0xFF;
    /**
     * 备用电池失效前后视频录制时间
     */
    private Integer batteryInvalidVideoTime = 0xFF;
    /**
     * 备用电池失效常拍照张数
     */
    private Integer batteryInvalidCameraNum = 0xFF;
    /**
     * 备用电池失效拍照间隔时间
     */
    private Integer batteryInvalidCameraTime = 0xFF;
    /**
     * IC卡从业资格证模块故障前后视频录制时间
     */
    private Integer icCardErrVideoTime = 0xFF;
    /**
     * IC卡从业资格证模块故障拍照张数
     */
    private Integer icCardErrCameraNum = 0xFF;
    /**
     * IC卡从业资格证模块故障拍照间隔时间
     */
    private Integer icCardErrCameraTime = 0xFF;

    /**
     * 保留字段
     */
    private byte[] keep1 = new byte[10];
    private static Map<String, List<String>> alarmParamMap = new HashMap<>();

    static {
        //报警类型设置那几个字段
        alarmParamMap.put("254101",
            Arrays.asList("occlusionVideoTime", "occlusionCameraTime", "occlusionCameraNum", "occlusionTime"));
        alarmParamMap.put("254102", Arrays
            .asList("infraRedBlockVideoTime", "infraRedBlockCameraTime", "infraRedBlockCameraNum",
                "infraRedBlockTime"));
        alarmParamMap
            .put("254103", Arrays.asList("mainMemErrVideoTime", "mainMemErrCameraTime", "mainMemErrCameraNum"));
        alarmParamMap
            .put("254104", Arrays.asList("spareMemErrVideoTime", "spareMemErrCameraTime", "spareMemErrCameraNum"));
        alarmParamMap.put("254105", Arrays.asList("gpsSignalVideoTime", "gpsSignalCameraTime", "gpsSignalCameraNum"));
        alarmParamMap.put("254106", Arrays.asList("signalVideoTime", "signalCameraTime", "signalCameraNum"));
        alarmParamMap.put("254107",
            Arrays.asList("batteryVoltageVideoTime", "batteryVoltageCameraTime", "batteryVoltageCameraNum"));
        alarmParamMap.put("254108",
            Arrays.asList("batteryInvalidVideoTime", "batteryInvalidCameraTime", "batteryInvalidCameraNum"));
        alarmParamMap.put("254109", Arrays.asList("icCardErrVideoTime", "icCardErrCameraTime", "icCardErrCameraNum"));
    }

    public HeiEquipmentFailureMonitoring(AdasParamSettingForm paramSettingForm) {
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

        //是否是遮挡和红外阻断（多出一个触发报警时间间隔）
        if (isOcclusionAndInfraRedBlock(fields.size())) {
            setValIfPresent(fields.get(3), paramSetting.getTimeSlotThreshold());
        }

    }

    /**
     * 是否是遮挡和红外阻断
     * @param size
     * @return
     */
    private boolean isOcclusionAndInfraRedBlock(int size) {
        return size == 4;
    }
}

package com.zw.ws.entity.adas.paramSetting;

import com.zw.adas.domain.define.setting.AdasAlarmParamSetting;
import com.zw.adas.domain.define.setting.query.AdasParamSettingForm;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class HuOvercrowding extends PublicParameters {
    /**
     * 超员报警使能
     */
    private Long alarmEnable = 0L;
    /**
     * 拍照分辨率
     */
    private Integer cameraResolution = 0xff;
    /**
     * 保留位
     */
    private Byte[] keep = new Byte[15];
    /**
     * 超员报警分级速度阈值
     */
    private Integer overManSpeed = 0xff;
    /**
     * 超员报警前后视频录制时间
     */
    private Integer overManVideoTime = 0xff;
    /**
     * 超员报警拍照张数
     */
    private Integer overManCameraNum = 0xff;
    /**
     * 超员报警拍照间隔时间
     */
    private Integer overManCameraTime = 0xff;
    /**
     * 违规上下客报警分级速度阈值
     */
    private Integer illegalSpeed = 0xff;
    /**
     * 违规上下客报警前后视频录制时间
     */
    private Integer illegalVideoTime = 0xff;
    /**
     * 违规上下客报警拍照张数
     */
    private Integer illegalCameraNum = 0xff;
    /**
     * 违规上下客报警拍照间隔时间
     */
    private Integer illegalCameraTime = 0xff;
    /**
     * 超员联动上传主码流视频通道
     */
    private Integer overManPrimaryChannel = 0;
    /**
     * 超员联动上传子码流视频通道
     */
    private Integer overManSubcodeChannel = 0;
    /**
     * 超员联动上传抓拍通道
     */
    private Integer overManPhoto = 0;
    /**
     * 违规联动上传主码流视频通道
     */
    private Integer illegalPrimaryChannel = 0;
    /**
     * 违规联动上传子码流视频通道
     */
    private Integer illegalSubcodeChannel = 0;
    /**
     * 违规联动上传抓拍通道
     */
    private Integer illegalPhoto = 0;

    /**
     * 沪标不按规定上下客或超员报警使能顺序
     */
    private static Map<String, Object> surveyAlarmEnableMap = new HashMap();

    /**
     * 沪标不按规定上下客或超员报警参数设置参数交互字段
     */
    private static Map<String, Object> surveyAlarmParamMap = new HashMap();

    /**
     * 206801 超员报警
     * 206802 不按规定上下客报警危险说明
     */
    static {
        //报警使能顺序维护
        String[][] alarmEnableOrder = { { "206801", "0" }, { "206802", "1" } };
        for (String[] ints : alarmEnableOrder) {
            surveyAlarmEnableMap.put(ints[0], ints[1]);
        }
        //报警事件参数设置参数交互字段维护
        String[][] assistAlarmParamOrder = {
            { "206801", "overManSpeed,overManVideoTime,overManCameraNum,overManCameraTime,"
                    + "overManPrimaryChannel,overManSubcodeChannel,overManPhoto" },
            { "206802", "illegalSpeed,illegalVideoTime,illegalCameraNum,illegalCameraTime,"
                    + "illegalPrimaryChannel,illegalSubcodeChannel,illegalPhoto" } };
        for (String[] ints : assistAlarmParamOrder) {
            surveyAlarmParamMap.put(ints[0], ints[1]);
        }
    }

    public HuOvercrowding(AdasParamSettingForm paramSettingForm) {
        this.cameraResolution = parseIntData(paramSettingForm.getCommonParamSetting().getCameraResolution());
        for (AdasAlarmParamSetting paramSetting : paramSettingForm.getAdasAlarmParamSettings()) {
            String functionId = paramSetting.getRiskFunctionId().toString();
            //组装报警使能
            handelAlarmEnable(paramSetting, functionId);
            //组装报警事件参数设置
            handelAlarmParam(paramSetting, functionId);
        }
    }

    private void handelAlarmEnable(AdasAlarmParamSetting paramSetting, String key) {
        if (surveyAlarmEnableMap.get(key) != null) {
            alarmEnable = calBinaryData(alarmEnable, paramSetting.getOneLevelAlarmEnable(),
                Integer.parseInt(surveyAlarmEnableMap.get(key).toString()));
        }
    }

    private void handelAlarmParam(AdasAlarmParamSetting paramSetting, String key) {
        String[] params = surveyAlarmParamMap.get(key).toString().split(",");
        //分级速度阀值
        setValIfPresent(params[0], paramSetting.getAlarmLevelSpeedThreshold());
        //报警录制时间
        setValIfPresent(params[1], paramSetting.getVideoRecordingTime());
        //报警拍照张数
        setValIfPresent(params[2], paramSetting.getPhotographNumber());
        //报警拍照间隔
        setValIfPresent(params[3], paramSetting.getPhotographTime());
        //主码流
        setValIfPresent(params[4], paramSetting.getPrimaryChannel());
        //子码流
        setValIfPresent(params[5], paramSetting.getSubcodeChannel());
        //抓拍通道
        setValIfPresent(params[6], paramSetting.getCaptureChannel());
    }
}

package com.zw.ws.entity.adas.paramSetting;

import com.zw.adas.domain.define.setting.AdasAlarmParamSetting;
import com.zw.adas.domain.define.setting.query.AdasParamSettingForm;
import lombok.Data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 湘标车辆检测
 */
@Data
public class XiangOvercrowding extends PublicParameters {
    /**
     * 超员报警使能
     */
    private Long alarmEnable = 0L;

    /**
     * 保留项 16位
     */
    private byte[] keep = new byte[16];

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
     * 保留位
     */
    private byte[] keep2 = new byte[4];
    /**
     * 超员联动上传主码流视频通
     */
    private Integer overManStreamChannelNum = 0;

    /**
     * 超员联动上传子码流视频通道
     */
    private Integer overManSubStreamChannelNum = 0;

    /**
     * 超员联动上传抓拍通道
     */
    private Integer overManCaptureChannelNum = 0;
    /**
     * 保留位
     */
    private byte[] keep3 = new byte[16];
    /**
     * 报警使能顺序
     */

    private static Map<String, Integer> alarmEnableMap = new HashMap<>();
    /**
     * 报警参数
     */

    private static Map<String, List<String>> alarmParamMap = new HashMap<>();

    static {
        //报警使能顺序维护
        alarmEnableMap.put("276801", 0);
        //报警参数字段顺序维护
        alarmParamMap.put("276801", Arrays
            .asList("overManCameraTime", "overManCameraNum", "overManVideoTime", "overManStreamChannelNum",
                "overManSubStreamChannelNum", "overManCaptureChannelNum"));

    }

    public XiangOvercrowding(AdasParamSettingForm paramSettingForm) {
        setValIfPresent("cameraResolution",
            parseIntData(paramSettingForm.getCommonParamSetting().getCameraResolution()));
        for (AdasAlarmParamSetting paramSetting : paramSettingForm.getAdasAlarmParamSettings()) {
            String functionId = paramSetting.getRiskFunctionId().toString();
            //组装报警使能
            handelAlarmEnable(paramSetting, functionId);
            //组装报警事件参数设置
            handelAlarmParam(paramSetting, functionId);
        }
    }

    private void handelAlarmEnable(AdasAlarmParamSetting paramSetting, String key) {
        Integer position = alarmEnableMap.get(key);

        if (position != null) {
            alarmEnable = calBinaryData(alarmEnable, paramSetting.getAlarmEnable(), position);
        }
    }

    private void handelAlarmParam(AdasAlarmParamSetting paramSetting, String key) {
        List<String> fields = alarmParamMap.get(key);

        setValIfPresent(fields.get(0), paramSetting.getPhotographTime());
        setValIfPresent(fields.get(1), paramSetting.getPhotographNumber());
        setValIfPresent(fields.get(2), paramSetting.getVideoRecordingTime());
        setValIfPresent(fields.get(3), getFinalChannelVal(paramSetting.getPrimaryChannel()));
        setValIfPresent(fields.get(4), getFinalChannelVal(paramSetting.getSubcodeChannel()));
        setValIfPresent(fields.get(5), getFinalChannelVal(paramSetting.getCaptureChannel()));

    }
}

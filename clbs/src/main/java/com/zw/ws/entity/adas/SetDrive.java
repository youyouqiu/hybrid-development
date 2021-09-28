package com.zw.ws.entity.adas;

import com.zw.platform.domain.riskManagement.form.RiskEventVehicleConfigForm;
import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;

/**
 * @Description:主动安全辅助系统参数
 * @Author:nixiangqian
 * @Date:Create in 2018/7/11 11:57
 */
@Data
public class SetDrive implements T808MsgBody, AdasParamCommonMethod {

    /**
     * 报警判断速度阈值(km/h)
     */
    protected Integer speedThreshold = 30;

    /**
     * 报警提示音量
     */
    protected Integer alarmVolume = 6;

    /**
     * 灵敏度
     */
    protected Integer sensitivity = 2;

    /**
     * 拍照分辨率
     */
    protected Integer cameraResolution = 0x01;

    /**
     * 视频录制分辨率
     */
    protected Integer videoResolution = 0x01;

    /**
     * 使能参数-事件使能
     */
    protected Integer eventEnable = 0x0000;

    /**
     * 使能参数-一级报警使能
     */
    protected Integer oneLevelAlarmEnable = 0x0000;

    /**
     * 使能参数-二级报警使能
     */
    protected Integer twoLevelAlarmEnable = 0x0000;

    /**
     * 报警语音使能-一级报警语音使能
     */
    protected Integer oneLevelVoiceEnable = 0x0000;

    /**
     * 报警语音使能-二级报警语音使能
     */
    protected Integer twoLevelVoiceEnable = 0x0000;

    /**
     * 报警语音使能-事件报警语音使能
     */
    protected Integer eventVoiceEnable = 0x0000;

    /**
     * 主动拍照事件-拍照策略
     */
    protected Integer cameraStrategy = 0x00;

    /**
     * 主动拍照事件-定时拍照时间间隔(分钟))
     */
    protected Integer timingCamera = 30;

    /**
     * 主动拍照事件-定距拍照距离间隔(km)
     */
    protected Integer fixedCamera = 5;

    /**
     * 主动拍照事件-拍照张数
     */
    protected Integer cameraNum = 1;

    /**
     * 主动拍照事件-拍照时间间隔)(100ms)
     */
    protected Integer cameraTime = 5;

    protected void initBase(RiskEventVehicleConfigForm revConfig) {
        //DSM、ADAS共同属性统一设置
        handleBaseSetting(revConfig);
        //主动拍照
        handleInitiativePhoto(revConfig);

    }

    private void handleBaseSetting(RiskEventVehicleConfigForm revConfig) {
        //低速阈值速度阈值对应低速（该低速阈值的设定包括疲劳驾驶、分心驾驶、异常报警、碰撞危险4类风险报警信息，统一设定）
        setValIfPresent("speedThreshold", revConfig.getLowSpeed());
        //提示音量
        setValIfPresent("alarmVolume", revConfig.getAlarmVolume());
        //灵敏度
        setValIfPresent("sensitivity", revConfig.getSensitivity());
        //拍照分辨率
        setValIfPresent("cameraResolution", parseIntData(revConfig.getCameraResolution()));
        //视频录制分辨率
        setValIfPresent("videoResolution", parseIntData(revConfig.getVideoResolution()));
    }


    protected Integer parseIntFromFloat(Float data, Integer ratio) {
        if (data != null) {
            Float result = data * ratio;
            return result.intValue();
        }
        return null;
    }

    protected Integer getMultiRatioData(Integer data, Integer ratio) {
        if (data != null) {
            return data * ratio;
        }
        return null;
    }

    protected Integer getMultiRatioData(Double data, Integer ratio) {
        Double result;
        if (data != null) {
            result = data * ratio;
            return result.intValue();
        }
        return null;
    }

    private void handleInitiativePhoto(RiskEventVehicleConfigForm revConfig) {
        handleCmeraStrategy(revConfig);
        //定时拍照
        setValIfPresent("timingCamera", revConfig.getTimingPhotoInterval());
        //定距拍照
        setValIfPresent("fixedCamera", revConfig.getDistancePhotoInterval());
        //拍照张数
        setValIfPresent("cameraNum", revConfig.getTimingPhoto());
        //拍照间隔
        setValIfPresent("cameraTime", parseIntFromFloat(revConfig.getDsmAdasTimeInterval(), 10));
    }

    protected void calAlarmAndVoiceEnable(RiskEventVehicleConfigForm revConfig, Integer pointer) {
        //预警使能
        oneLevelAlarmEnable = calBinaryData(oneLevelAlarmEnable, revConfig.getOneLevelAlarmEnable(), pointer);
        twoLevelAlarmEnable = calBinaryData(twoLevelAlarmEnable, revConfig.getTwoLevelAlarmEnable(), pointer);
        //语音使能
        oneLevelVoiceEnable = calBinaryData(oneLevelVoiceEnable, revConfig.getOneLevelVoiceEnable(), pointer);
        twoLevelVoiceEnable = calBinaryData(twoLevelVoiceEnable, revConfig.getTwoLevelVoiceEnable(), pointer);
    }

    protected void handleCmeraStrategy(RiskEventVehicleConfigForm revConfig) {
        cameraStrategy = calBinaryData(cameraStrategy, revConfig.getTimingCapture(), 0);
        cameraStrategy = calBinaryData(cameraStrategy, revConfig.getDistanceCapture(), 1);
    }


}

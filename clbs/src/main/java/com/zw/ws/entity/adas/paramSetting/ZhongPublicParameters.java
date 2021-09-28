package com.zw.ws.entity.adas.paramSetting;

import com.zw.adas.domain.define.setting.AdasCommonParamSetting;
import com.zw.platform.util.StrUtil;
import com.zw.protocol.msg.t808.T808MsgBody;
import com.zw.ws.entity.adas.AdasParamCommonMethod;
import lombok.Data;

/**
 * @Description:中位标准2019主动安全参数（8103公共参数）
 */
@Data
public class ZhongPublicParameters implements T808MsgBody, AdasParamCommonMethod {

    /**
     * 报警判断速度阈值
     */
    private Integer speedThreshold = 0xff;

    /**
     * 报警提示音量
     */
    private Integer alarmVolume = 0xff;

    /**
     * 灵敏度
     */
    private Integer sensitivity = 0xff;

    /**
     * 拍照分辨率
     */
    private Integer cameraResolution = 0x01;

    /**
     * 视频录制分辨率
     */
    private Integer videoResolution = 0x01;

    /**
     * 辅助多媒体信息-前后视频录制时间
     */
    private Integer multimediaVideoTime = 0xff;
    /**
     * 辅助多媒体信息-1#执行摄像头通道号
     */
    private Integer multimediaChannelNum1 = 0xff;
    /**
     * 辅助多媒体信息-2#执行摄像头通道号
     */
    private Integer multimediaChannelNum2 = 0xff;
    /**
     * 辅助多媒体信息-3#执行摄像头通道号
     */
    private Integer multimediaChannelNum3 = 0xff;

    public ZhongPublicParameters(AdasCommonParamSetting commonParam) {
        //中位标准前向、驾驶员行为共同属性统一设置
        handleCommonParamSetting(commonParam);
    }

    public ZhongPublicParameters() {
    }

    private void handleCommonParamSetting(AdasCommonParamSetting commonParam) {
        //速度阈值
        setValIfPresent("speedThreshold", commonParam.getSpeedLimit());
        //提示音量
        setValIfPresent("alarmVolume", Integer.parseInt(commonParam.getAlarmVolume()));
        //灵敏度
        setValIfPresent("sensitivity", commonParam.getSensitivity());
        //拍照分辨率
        setValIfPresent("cameraResolution", parseIntData(commonParam.getCameraResolution()));
        //视频录制分辨率
        setValIfPresent("videoResolution", parseIntData(commonParam.getVideoResolution()));
        //辅助多媒体信息-前后视频录制时间
        setValIfPresent("multimediaVideoTime", commonParam.getRecordingTime());
        //辅助多媒体信息-1#执行摄像头通道号
        setValIfPresent("multimediaChannelNum1", getHexVal(commonParam.getChannelOne()));
        //辅助多媒体信息-2#执行摄像头通道号
        setValIfPresent("multimediaChannelNum2", getHexVal(commonParam.getChannelTwo()));
        //辅助多媒体信息-3#执行摄像头通道号
        setValIfPresent("multimediaChannelNum3", getHexVal(commonParam.getChannelThree()));

    }

    private Integer getHexVal(String hexVal) {
        if (StrUtil.isBlank(hexVal)) {
            return null;
        }
        return Integer.parseInt(hexVal.replace("0x", ""), 16);
    }

}

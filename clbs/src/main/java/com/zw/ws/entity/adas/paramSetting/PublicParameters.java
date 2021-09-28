package com.zw.ws.entity.adas.paramSetting;

import com.zw.adas.domain.define.setting.AdasCommonParamSetting;
import com.zw.protocol.msg.t808.T808MsgBody;
import com.zw.ws.entity.adas.AdasParamCommonMethod;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @Description:主动安全参数（8103公共参数）
 */
@Data
public class PublicParameters implements T808MsgBody, AdasParamCommonMethod {

    /**
     * 拍照分辨率
     */
    protected Integer cameraResolution = 0xff;

    /**
     * 视频录制分辨率
     */
    protected Integer videoResolution = 0xff;

    /**
     * 报警提示音量
     */
    protected Integer alarmVolume = 0xff;

    /**
     * 主动拍照事件-拍照策略
     */
    protected Integer cameraStrategy = 0xff;

    /**
     * 主动拍照事件-拍照张数
     */
    protected Integer cameraNum = 0xff;

    /**
     * 主动拍照事件-拍照时间间隔)(100ms)
     */
    protected Integer cameraTime = 0xff;

    /**
     * 主动拍照事件-定时拍照时间间隔(分钟))
     */
    protected Integer timingCamera = 0xFFFF;

    /**
     * 主动拍照事件-定距拍照距离间隔(m)
     */
    protected Integer fixedCamera = 0xFFFF;

    /**
     * 报警判断速度阈值(km/h)
     */
    protected Integer speedThreshold = 0xff;

    /**
     * 动态比对间隔(黑标新增)
     */
    private Integer comparisonInterval = 0xff;

    public PublicParameters(AdasCommonParamSetting commonParam) {
        //前向、驾驶员行为，激烈驾驶共同属性统一设置
        handleCommonParamSetting(commonParam);
    }

    public PublicParameters() {
    }

    private void handleCommonParamSetting(AdasCommonParamSetting commonParam) {
        //拍照分辨率
        setValIfPresent("cameraResolution", parseIntData(commonParam.getCameraResolution()));
        //视频录制分辨率
        setValIfPresent("videoResolution", parseIntData(commonParam.getVideoResolution()));
        //提示音量
        setValIfPresent("alarmVolume", parseIntData(commonParam.getAlarmVolume()));
        //拍照策略
        setValIfPresent("cameraStrategy", parseIntData(commonParam.getTouchStatus()));
        //拍照张数
        setValIfPresent("cameraNum", commonParam.getPhotographNumber());
        //拍照时间间隔
        setValIfPresent("cameraTime", commonParam.getPhotographTime());
        //定时拍照时间间隔
        setValIfPresent("timingCamera", parseIntData(commonParam.getTimingPhotoInterval()));
        //定距拍照距离间隔
        setDistanceInterval(commonParam.getDistancePhotoInterval());
        //速度阈值
        setValIfPresent("speedThreshold", commonParam.getSpeedLimit());

        //动态对比时间间隔
        setValIfPresent("comparisonInterval", commonParam.getDynamicContrastInterval());

    }

    /**
     * 定距拍照距离间隔
     * @param fixedCamera
     */
    private void setDistanceInterval(String fixedCamera) {
        if (!isNotBlankVal(fixedCamera)) {
            return;
        }
        BigDecimal bigDecimal = new BigDecimal(fixedCamera);
        BigDecimal big = bigDecimal.multiply(new BigDecimal("1000")).setScale(0);
        setValIfPresent("fixedCamera", big.intValue());

    }

}

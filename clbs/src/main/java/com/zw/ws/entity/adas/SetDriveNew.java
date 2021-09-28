package com.zw.ws.entity.adas;

import com.zw.adas.domain.define.setting.AdasCommonParamSetting;
import com.zw.platform.util.Reflections;
import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;


/**
 * 主动安全参数（8103公共参数）
 */
@Data
public class SetDriveNew implements T808MsgBody {

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

    SetDriveNew(AdasCommonParamSetting commonParam) {
        //前向、驾驶员行为，激烈驾驶共同属性统一设置
        handleCommonParamSetting(commonParam);
    }

    private void handleCommonParamSetting(AdasCommonParamSetting commonParam) {
        //拍照分辨率
        setValIfPresent("cameraResolution", parseIntData(commonParam.getCameraResolution()));
        //视频录制分辨率
        setValIfPresent("videoResolution", parseIntData(commonParam.getVideoResolution()));
        //提示音量
        setValIfPresent("alarmVolume", Integer.parseInt(commonParam.getAlarmVolume()));
        //拍照策略
        setValIfPresent("cameraStrategy", parseIntData(commonParam.getTouchStatus()));
        //拍照张数
        setValIfPresent("cameraNum",
            commonParam.getPhotographNumber() != null ? commonParam.getPhotographNumber() : 0xff);
        //拍照时间间隔
        setValIfPresent("cameraTime", commonParam.getPhotographTime() != null ? commonParam.getPhotographTime() : 0xff);
        //定时拍照时间间隔
        setValIfPresent("timingCamera",
            StringUtils.isNotEmpty(commonParam.getTimingPhotoInterval())
                ? Integer.parseInt(commonParam.getTimingPhotoInterval()) :
                0xFFFF);
        String fixedCamera = commonParam.getDistancePhotoInterval();
        if (fixedCamera != null && !fixedCamera.equals("-1")) {
            BigDecimal bigDecimal = new BigDecimal(fixedCamera);
            BigDecimal big = bigDecimal.multiply(new BigDecimal("1000")).setScale(0, BigDecimal.ROUND_HALF_UP);
            commonParam.setDistancePhotoInterval(big.toString());
        }
        //定距拍照距离间隔
        setValIfPresent("fixedCamera",
            StringUtils.isNotEmpty(commonParam.getDistancePhotoInterval()) && !fixedCamera.equals("-1")
                ? Integer.parseInt(commonParam.getDistancePhotoInterval()) :
                0xFFFF);
        //速度阈值
        setValIfPresent("speedThreshold", commonParam.getSpeedLimit());
    }

    private Integer parseIntData(String data) {
        try {
            return data == null ? null : Integer.parseInt(data.split("0x")[1]);
        } catch (Exception e) {
            return Integer.parseInt(data);
        }

    }

    public void setValIfPresent(String fieldName, Integer value) {
        if (value != -1) {
            Reflections.setValIfPresent(fieldName, value, this);
        }
    }

    protected Long calBinaryData(Long originData, Integer pointerData, Integer pointer) {
        if (pointerData != null) {
            originData |= (pointerData << pointer);
        }
        return originData;

    }
}

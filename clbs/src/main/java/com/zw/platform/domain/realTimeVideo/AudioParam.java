package com.zw.platform.domain.realTimeVideo;

import lombok.Data;


/**
 * 音频参数实体
 */
@Data
public class AudioParam {

    private String vehicleId; // 监控对象Id
    
    private String deviceId; // 终端id

    private Integer audioCode; // 音频编码方式

    private Integer videoCode;  // 视频编码方式

    private Integer audioSampling; // 音频采样率

    private Integer audioSamplingBit; // 音频采样位数

    private Integer audioFpsLen; // 音频帧长度

}

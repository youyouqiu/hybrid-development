package com.zw.platform.dto.video;

import lombok.Data;

import java.io.Serializable;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/11/11 17:33
 */
@Data
public class DeviceVideoParamDto implements Serializable {
    private static final long serialVersionUID = 5736787616407809201L;

    /**
     * 用户id
     */
    private String userUuid;
    /**
     * 监控对象id
     */
    private String monitorId;
    /**
     * 终端手机id
     */
    private String simcardId;
    /**
     * 终端手机卡号
     */
    private String simcardNumber;
    /**
     * 终端id
     */
    private String deviceId;
    /**
     * 终端编号
     */
    private String deviceNumber;
    /**
     * 实时流音频格式
     */
    private String audioFormatStr;
    /**
     * 实时流采样率
     */
    private String samplingRateStr;
    /**
     * 实时流声道数
     */
    private String vocalTractStr;
    /**
     * 存储流音频格式
     */
    private String storageAudioFormatStr;
    /**
     * 存储流采样率
     */
    private String storageSamplingRateStr;
    /**
     * 存储流声道数
     */
    private String storageVocalTractStr;
    /**
     * 是否支持视频 0:否; 1:是;
     */
    private Integer supportVideoFlag;

    /**
     * 终端类型
     */
    private String deviceType;
}

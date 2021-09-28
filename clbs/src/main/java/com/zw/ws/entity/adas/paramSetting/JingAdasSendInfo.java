package com.zw.ws.entity.adas.paramSetting;

import lombok.Data;

/**
 * @Description: 8103 京标下发实体
 * @Author zhangqiang
 * @Date 2020/6/11 11:23
 */
@Data
public class JingAdasSendInfo {
    /**
     * 超报警级别
     */
    private Integer level;
    /**
     * 报警提示音量
     */
    private Integer alarmVolume;
    /**
     * 是否语音播报
     */
    private Integer voiceBroadcast;
    /**
     * 报警视频时长
     */
    private Integer videoTime;
    /**
     * 报警视频分辨率
     */
    private Integer videoResolution;
    /**
     * 报警照片张数
     */
    private Integer cameraNum;
    /**
     * 照片分辨率
     */
    private Integer cameraResolution;
    /**
     * 照片时间间隔
     */
    private Integer cameraTime;
    /**
     * 报警判断速度阀值
     */
    private Integer speedThreshold;
    /**
     * 报警判断持续时长阀值
     */
    private Integer durationThreshold;
}

package com.zw.platform.domain.realTimeVideo;

import lombok.Data;

/**
 * @author penghj
 * @version 1.0
 * @date 2021/2/23 15:09
 */
@Data
public class VideoPlayResultDTO {
    /**
     * 车辆id
     */
    private String vehicleId;

    /**
     * 终端通道号
     */
    private Integer channelNumber;

    /**
     * 播放状态：0：成功 1：失败
     */
    private Integer playStatus;

    /**
     * 失败原因 1:终端离线; 2:视频请求超时; 3:终端网络不稳定及其他;
     */
    private Integer failReason;
}

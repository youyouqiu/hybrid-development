package com.zw.platform.domain.realTimeVideo;

import lombok.Data;


/**
 * 音频参数实体
 */
@Data
public class DiskInfo {


    private Integer videoPlayTime; // 视频播放缺省时间（s）

    private Integer videoStopTime;  // 视频空闲断开时间（s）

    private Integer memoryRate; // 存储容量预警阈值（%）

    private Integer memory; // 存储容量状态（%）

    private Integer memoryType; // 存储空间满后处理类型：0：空间满后自动覆盖； 1：空间满后停止录制
    
}

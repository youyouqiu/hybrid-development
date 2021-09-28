package com.zw.platform.domain.realTimeVideo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/6/22 17:51
 */
@Data
@NoArgsConstructor
public class HistoryPlayControl {
    /**
     * 车id
     */
    private String vehicleIds;

    /**
     * 通道号
     */
    private String channelNums;

    /**
     * 回放控制
     * 0：开始回放
     * 1：暂停回放
     * 2：结束回放
     * 3：快进回放
     * 4：关键帧快退回放
     * 5：拖地回放
     * 6：关键帧播放
     */
    private Integer remote;

    /**
     * 快进快退
     * 回放控制为3或4是有效，否则为0
     * 0：无效
     * 1：1倍
     * 2：2倍
     * 3：4倍
     * 4：8倍
     * 5：16倍
     */
    private Integer forwardOrRewind;

    /**
     * 拖动回放时间点（YYMMDDHHmmss）回放控制为5时有效，否则全为0
     */
    private String dragPlaybackTime;

    /**
     * 回放控制为2时有效,前端需要使用
     * 'TIMEOUT': TODO 前端定义的
     * keyframes: 关键帧播放停止
     * resourceChange: 资源切换停止(1个通道号的视频是多段,播放下一段需要先发送停止)
     * drag: 视频拖动停止(视频拖动播放的逻辑是,先发送停止,然后下发9201从拖动结束的地方开始播放视频)
     */
    private String closeType;
}

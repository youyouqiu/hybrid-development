package com.zw.platform.domain.realTimeVideo;

import com.zw.protocol.msg.t808.T808MsgBody;

import lombok.Data;

/**
 * 实时音视频传输控制0x9102
 * @author hujun
 * @version 创建时间：2018年1月2日 下午5:53:04
 */
@Data
public class VideoControl implements T808MsgBody {
    private Integer channelNum;//逻辑通道号
    private Integer control;//控制指令（0：关闭音视频传输指令，1：切换码流，2: 暂停该通道所有流的发送，3：恢复暂停前流的发送，与暂停前的流类型一致，4：关闭双向对讲）
    private Integer closeVideoType;//关闭音视频类型（0:关闭该通道有关音视频数据，1:只关闭该通道有关的音频，保留该通道有关视频，2:只关闭该通道有关的视频，保留该通道的有关音频）
    private Integer changeStreamType;//切换码流类型（0主码流 ，1子码流）
}

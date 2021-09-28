package com.zw.platform.domain.realTimeVideo;

import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = false)
public class VideoPlaybackControl  implements T808MsgBody {
    private static final long serialVersionUID = 1L;

    /**
     通道号
     */
    private Integer channelNum;

    /**
     回放控制
     0：开始回放
     1：暂停回放
     2：结束回放
     3：快进回放
     4：关键帧快退回放
     5：拖地回放
     6：关键帧播放
     */
    private Integer remote;

    /**
     快进快退
     回放控制为3或4是有效，否则为0
     0：无效
     1：1倍
     2：2倍
     3：4倍
     4：8倍
     5：16倍
     */
    private Integer forwardOrRewind;

    /**
     拖动回放时间点（YYMMDDHHmmss）回放控制为5时有效，否则全为0
     */
    private String dragPlaybackTime;
}

package com.zw.platform.domain.realTimeVideo;


import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = false)
public class ResourceListSend implements T808MsgBody {

    /* 通道号 */
    private Integer channelNum;

    /* 视频开始时间 */
    private String startTime;

    /* 视频结束时间 */
    private String endTime;

    /* 报警类型 */
    private long alarm;

    /* 音视频资源类型 0:音视频，1：音频，2：视频，3：音频或视频 */
    private Integer videoType;

    /* 码流 0:所有码流，1：主码流，2，子码流 */
    private Integer streamType;

    /* 存储类型 0：所有存储器，1：主存储器，2：灾备存储器 */
    private Integer storageType;

}

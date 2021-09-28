package com.zw.platform.domain.realTimeVideo;


import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;


/**
 * 云台消息体
 */
@Data
public class CloudTerrace implements T808MsgBody {
    private Integer channelNum;// 逻辑通道号

    private Integer control;// 控制指令
    
    private Integer speed;
}

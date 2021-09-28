package com.zw.platform.domain.netty;

import com.zw.protocol.msg.t808.T808MsgBody;

import lombok.Data;


/**
 * 丢包率下发 @author  Tdz
 * @create 2018-01-18 14:28
 **/
@Data
public class LossRateSend implements T808MsgBody{
    private Integer channelNum;// 通道号

    private Integer packetLoss;// 丢包率
}

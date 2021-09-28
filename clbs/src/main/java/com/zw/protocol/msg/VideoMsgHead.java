package com.zw.protocol.msg;


import lombok.Data;


/**
 * Created by LiaoYuecai on 2017/6/19.
 */
@Data
public class VideoMsgHead {
    protected Integer msgID = 0;// 消息ID

    protected String mobile;// 手机号码

    protected Integer subNumber = 0;// 流水号

    protected Integer channelNum = 0;// 通道号
}


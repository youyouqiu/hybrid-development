package com.zw.protocol.msg;


import lombok.Data;


/**
 * Created by LiaoYuecai on 2017/6/19.
 */
@Data
public class RtpMessage {
    private VideoMsgHead msgHead;

    private Object msgBody;
}

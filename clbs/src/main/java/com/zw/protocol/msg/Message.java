package com.zw.protocol.msg;

import lombok.Data;

/**
 * Created by LiaoYuecai on 2017/6/19.
 */
@Data
public class Message implements MsgBean {
    private MsgDesc desc;
    private Object data;

    /**
     * 组装809下发参数Desc参数值
     */
    public Message assembleDesc809(String t809PlatId) {
        desc.setT809PlatId(t809PlatId);
        desc.setMessageType(2);
        return this;
    }
}

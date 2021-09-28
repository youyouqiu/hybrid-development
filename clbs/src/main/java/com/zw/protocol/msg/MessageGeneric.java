package com.zw.protocol.msg;

import lombok.Data;

@Data
public class MessageGeneric<T> implements MsgBean {
    private MsgDesc desc;
    private T data;
}

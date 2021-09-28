package com.zw.protocol.msg.t808;

import com.zw.protocol.msg.MsgBean;
import lombok.Data;

@Data
public class T808MessageGeneric<E> implements MsgBean {
    private T808MsgHead msgHead;
    private E msgBody;
}

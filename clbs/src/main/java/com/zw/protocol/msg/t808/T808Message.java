package com.zw.protocol.msg.t808;

import com.zw.protocol.msg.MsgBean;
import lombok.Data;

/**
 * Created by LiaoYuecai on 2017/6/19.
 */
@Data
public class T808Message implements MsgBean {
    private T808MsgHead msgHead;
    private Object msgBody;
}

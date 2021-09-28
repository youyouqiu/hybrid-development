package com.zw.protocol.msg.t809;

import com.zw.protocol.msg.MsgBean;
import lombok.Data;

/**
 * Created by LiaoYuecai on 2017/6/19.
 */
@Data
public class T809Message implements MsgBean {
    private T809MsgHead msgHead;
    private Object msgBody;
}

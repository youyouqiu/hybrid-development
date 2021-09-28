package com.zw.protocol.msg.t808;

import com.zw.protocol.msg.MsgBean;
import lombok.Data;

/**
 * Created by wjy on 2017/8/14.
 */
@Data
public class T808MsgBody513 implements MsgBean {
    private String msgSNAck;
    private Object gpsInfo;
}

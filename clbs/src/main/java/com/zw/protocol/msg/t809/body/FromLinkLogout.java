package com.zw.protocol.msg.t809.body;

import com.zw.protocol.msg.t809.T809MsgBody;

import lombok.Data;


/**
 * Created by LiaoYuecai on 2017/2/10.
 */
@Data
public class FromLinkLogout implements T809MsgBody {
    private int reasonCode=0x01;
}

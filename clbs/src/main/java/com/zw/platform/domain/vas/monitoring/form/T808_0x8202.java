package com.zw.platform.domain.vas.monitoring.form;

import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/4/11.
 */
@Data
public class T808_0x8202 implements T808MsgBody {
   // private static final long serialVersionUID = 1L;
    private int interval; // 回传时间间隔(秒)
    private int validity; //位置追踪有效时间(秒)
}

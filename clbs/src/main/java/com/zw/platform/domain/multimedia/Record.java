package com.zw.platform.domain.multimedia;

import com.zw.protocol.msg.t808.T808MsgBody;

import lombok.Data;

/**
 * Created by LiaoYuecai on 2017/4/1.
 */
@Data
public class Record implements T808MsgBody{
    private Integer command;//录音命令
    private Integer time;//录音时间
    private Integer saveSign;//保存标志
    private Integer frequency;//音频采样率
}

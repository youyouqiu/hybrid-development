package com.zw.platform.domain.multimedia;

import com.zw.protocol.msg.t808.T808MsgBody;

import lombok.Data;

/**
 * 多媒体检索
 *
 * @author  Tdz
 * @create 2017-04-24 9:52
 **/
@Data
public class MultimediaRetrieval implements T808MsgBody{
    private Integer type;
    private Integer wayID;
    private Integer eventCode;
    private String startTime;
    private String endTime;
}

package com.zw.platform.domain.sendTxt;

import java.util.List;

import com.zw.protocol.msg.t808.T808MsgBody;

import lombok.Data;

/**
 * 提问下发
 *
 * @author  Tdz
 * @create 2017-04-21 13:46
 **/
@Data
public class SendQuestion  implements T808MsgBody
{
    private Integer sign;
    private Integer regRet;
    private String question;
    private List<Answer> answers;
}

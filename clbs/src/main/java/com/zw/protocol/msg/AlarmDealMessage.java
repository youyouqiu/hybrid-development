package com.zw.protocol.msg;

import com.zw.protocol.msg.t809.T809Message;
import lombok.Data;

import java.util.List;

/***
 @Author lijie
 @Date 2020/8/6 10:34
 @Description 报警处理上报消息
 @version 1.0
 **/
@Data
public class AlarmDealMessage {

    //上级平台id
    private String t809PlatId;

    private List<T809Message> t809MessageList;

}

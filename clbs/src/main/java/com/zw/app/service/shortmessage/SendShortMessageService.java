package com.zw.app.service.shortmessage;

import com.zw.app.util.common.AppResultBean;

/***
 @Author gfw
 @Date 2018/12/10 16:53
 @Description 短信发送接口
 @APPVersion V1.2.0
 @version 1.0
 **/
public interface SendShortMessageService {
    /**
     * 短信发送
     * @param telephone
     * @return
     */
    AppResultBean sendMessage(String telephone);
}

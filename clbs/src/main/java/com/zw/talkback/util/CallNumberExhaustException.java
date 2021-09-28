package com.zw.talkback.util;

/***
 @Author zhengjc
 @Date 2019/8/2 11:16
 @Description 组呼和个呼号码耗尽异常
 @version 1.0
 **/
public class CallNumberExhaustException extends Exception {

    public CallNumberExhaustException(String message) {
        super(message);
    }

}

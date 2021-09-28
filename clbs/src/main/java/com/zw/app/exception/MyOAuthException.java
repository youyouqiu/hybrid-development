package com.zw.app.exception;

import org.springframework.security.core.AuthenticationException;
/***
 @Author gfw
 @Date 2018/12/7 9:56
 @Description 自定义异常
 @version 1.0
 **/
public class MyOAuthException extends AuthenticationException {
    public MyOAuthException(String msg, Throwable t) {
        super(msg, t);
    }

    public MyOAuthException(String msg) {
        super(msg);
    }
}

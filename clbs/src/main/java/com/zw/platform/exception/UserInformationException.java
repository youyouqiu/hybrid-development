package com.zw.platform.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * 用户信息异常
 * @author hujun
 * @date 2018/10/24 10:07
 */
public class UserInformationException extends AuthenticationException {

    /**
     * Constructs a <code>BadCredentialsException</code> with the specified message.
     *
     * @param msg the detail message
     */
    public UserInformationException(String msg) {
        super(msg);
    }

    /**
     * Constructs a <code>BadCredentialsException</code> with the specified message and
     * root cause.
     *
     * @param msg the detail message
     * @param t root cause
     */
    public UserInformationException(String msg, Throwable t) {
        super(msg, t);
    }

}

package com.zw.platform.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * 用户不允许登录App异常
 * @author hujun
 * @date 2018/10/24 17:09
 */
public class AccessDeniedAppException extends AuthenticationException {

    /**
     * Constructs a <code>BadCredentialsException</code> with the specified message.
     *
     * @param msg the detail message
     */
    public AccessDeniedAppException(String msg) {
        super(msg);
    }

    /**
     * Constructs a <code>BadCredentialsException</code> with the specified message and
     * root cause.
     *
     * @param msg the detail message
     * @param t root cause
     */
    public AccessDeniedAppException(String msg, Throwable t) {
        super(msg, t);
    }

}

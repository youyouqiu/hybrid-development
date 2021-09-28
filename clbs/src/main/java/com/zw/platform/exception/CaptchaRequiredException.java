package com.zw.platform.exception;

import org.springframework.security.core.AuthenticationException;

public class CaptchaRequiredException extends AuthenticationException {
    private static final long serialVersionUID = 1L;

    public CaptchaRequiredException(String msg) {
        super(msg);
    }
}
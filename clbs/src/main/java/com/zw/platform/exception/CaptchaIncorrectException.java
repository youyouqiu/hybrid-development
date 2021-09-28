package com.zw.platform.exception;

import org.springframework.security.core.AuthenticationException;

public class CaptchaIncorrectException extends AuthenticationException {
    private static final long serialVersionUID = 1L;

    public CaptchaIncorrectException(String msg) {
        super(msg);
    }
}
package com.zw.platform.commons.filter;

import com.zw.platform.exception.CaptchaIncorrectException;
import com.zw.platform.exception.CaptchaRequiredException;
import com.zw.platform.util.AccountLocker;
import com.zw.platform.util.LocalizedUtils;
import com.zw.platform.util.common.CaptchaUtil;
import com.zw.platform.util.spring.LoginFailureHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

/**
 * 登录前检查：验证码检验和账号锁定状态检查
 */
public class PreAuthFilter extends OncePerRequestFilter {

    private static final String CAPTCHA_PARAMETER = "captchaCode";
    private static final String USERNAME_PARAMETER = "username";

    private final String authPath;

    @Value("${module.loginValidate}")
    private boolean loginValidate;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private AccountLocker accountLocker;

    @Autowired
    private LoginFailureHandler loginFailureHandler;

    public PreAuthFilter(String authPath) {
        this.authPath = authPath;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        final String requestURI = request.getRequestURI();
        if (Objects.equals(authPath, requestURI)) {
            try {
                checkAccountLock(request);
                if (loginValidate) {
                    checkValidateCode(request);
                }
            } catch (AuthenticationException e) {
                loginFailureHandler.onAuthenticationFailure(request, response, e);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private void checkAccountLock(HttpServletRequest request) {
        String username = obtainRequestParam(request, USERNAME_PARAMETER);
        long currentTime = System.currentTimeMillis();
        if (accountLocker.isLocked(username, currentTime)) {
            long lockTimeLeft = accountLocker.lockTimeLeft(username, currentTime);
            throw new LockedException(LocalizedUtils.message(messageSource, "login.fail.lock", lockTimeLeft));
        }
    }

    /**
     * 校验验证码
     */
    private void checkValidateCode(final HttpServletRequest request) {
        String inputCode = this.obtainRequestParam(request, CAPTCHA_PARAMETER).toLowerCase();
        if (!CaptchaUtil.checkCaptcha2(request.getSession(), inputCode)) {
            throw new CaptchaIncorrectException(LocalizedUtils.message(messageSource, "validate.code.error"));
        }
    }

    /**
     * 获取request里的请求参数
     */
    private String obtainRequestParam(final HttpServletRequest request, String paramName) {
        Object obj = request.getParameter(paramName);
        if (obj == null) {
            throw new CaptchaRequiredException(LocalizedUtils.message(messageSource, "validate.code.error"));
        }
        return obj.toString();
    }
}
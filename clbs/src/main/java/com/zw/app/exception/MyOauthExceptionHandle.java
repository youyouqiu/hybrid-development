package com.zw.app.exception;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/***
 @Author gfw
 @Date 2018/12/7 9:57
 @Description 自定义异常处理类
 @version 1.0
 **/
public class MyOauthExceptionHandle implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        String message = authException.getMessage();
        response.getWriter().print(message);
        response.getWriter().close();
    }
}

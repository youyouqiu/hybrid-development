package com.zw.platform.util.spring;

import com.alibaba.fastjson.JSONObject;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * 添加登录校验失败处理类
 * Created by PengFeng on 2017/11/21  16:44
 */
public class LoginFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");

        JSONObject json = new JSONObject();
        json.put("loginFailure", true);
        json.put("failureType", "error");
        json.put("msg", exception.getMessage());
        response.getWriter().write(json.toJSONString());
    }
}

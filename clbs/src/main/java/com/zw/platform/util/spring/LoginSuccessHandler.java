package com.zw.platform.util.spring;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.util.AccountLocker;
import com.zw.talkback.service.dispatch.MonitoringDispatchService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 添加登录权限验证成功处理
 * Created by PengFeng on 2017/11/22  10:04
 */
public class LoginSuccessHandler implements AuthenticationSuccessHandler {
    private static final Logger log = LogManager.getLogger(LoginSuccessHandler.class);

    @Autowired
    private AccountLocker accountLocker;

    @Autowired
    MonitoringDispatchService monitoringDispatchService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        JSONObject json = new JSONObject();
        json.put("loginFailure", false);
        json.put("failureType", "");
        json.put("msg", "");
        response.getWriter().write(json.toJSONString());

        String username = request.getParameter("username");
        clearUserDispatchLoginInformation(username);
        accountLocker.reset(username);
    }

    /**
     * 清除用户调度登录信息
     * @param userName 用户名称
     */
    private void clearUserDispatchLoginInformation(String userName) {
        try {
            monitoringDispatchService.dispatchLoginOut(userName);
        } catch (Exception e) {
            log.error("清除用户{}调度登录信息异常", userName, e);
        }
    }
}

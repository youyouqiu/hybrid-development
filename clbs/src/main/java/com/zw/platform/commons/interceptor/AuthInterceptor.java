package com.zw.platform.commons.interceptor;

import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.commons.Auth;
import com.zw.platform.commons.SystemHelper;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AuthInterceptor extends HandlerInterceptorAdapter {
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
        ModelAndView modelAndView) throws Exception {
        if (modelAndView == null) {
            return;
        }
        if (handler instanceof HandlerMethod) {
            //判断用户是否已经登录
            if (SystemHelper.getCurrentUsername() != null) {
                // 检查用户是否已被停用
                final RedisKey stateKey = HistoryRedisKeyEnum.USER_STATE.of(SystemHelper.getCurrentUsername());
                String state = RedisHelper.getString(stateKey);
                if ("0".equals(state)) {
                    modelAndView.addObject("errorMsg", "您登录的用户已被停用！请联系管理员延期时间");
                    response.sendRedirect("/clbs/login?type=stop");
                    return;
                }
            }
            Auth auth = ((HandlerMethod) handler).getMethod().getAnnotation(Auth.class);
            if (auth != null) { // 有权限控制的就要检查
                modelAndView.addObject("hasRole", SystemHelper.checkPermissionEditable());
            }
        }
    }
}

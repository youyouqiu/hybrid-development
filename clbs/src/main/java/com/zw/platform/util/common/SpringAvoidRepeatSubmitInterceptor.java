package com.zw.platform.util.common;

import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.util.HttpServletRequestUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * 表单重复提交的功能
 * @author Administrator
 */
public final class SpringAvoidRepeatSubmitInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private HttpServletRequestUtil httpUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws Exception {
        httpUtils.setRequest(request);
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            AvoidRepeatSubmitToken annotation = method.getAnnotation(AvoidRepeatSubmitToken.class);
            if (annotation == null) {
                return true;
            }
            boolean needRemoveSession = annotation.removeToken();
            if (!needRemoveSession) {
                return true;
            }
            String resubmitToken = request.getParameter("resubmitToken");
            if (StringUtils.isBlank(resubmitToken)) {
                response.getWriter().print("{\"success\":\"false\",\"msg\":\"token not found\"}");
                return false;
            }
            resubmitToken = RedisHelper.getString(RedisKeyEnum.FORM_REPEAT_SUBMIT_HASH_CODE.of(resubmitToken));
            if (StringUtils.isBlank(resubmitToken)) {
                RedisHelper.setString(RedisKeyEnum.FORM_REPEAT_SUBMIT_HASH_CODE.of(resubmitToken), resubmitToken, 30);
                return true;
            }
            response.getWriter().print("{\"success\":\"false\",\"msg\":\"表单重复提交\"}");
            return false;
        }
        return super.preHandle(request, response, handler);
    }

}
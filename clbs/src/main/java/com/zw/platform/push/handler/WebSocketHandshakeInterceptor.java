/*
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved. This software is the confidential and proprietary information
 * of ZhongWei, Inc. You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with ZhongWei.
 */

package com.zw.platform.push.handler;

import com.zw.platform.util.GetIpAddr;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * @author Administrator
 * @version 1.0
 */
@Service
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
        Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletServerHttpRequest = (ServletServerHttpRequest) request;
            HttpServletRequest httpServletRequest = servletServerHttpRequest.getServletRequest();
            HttpSession session = httpServletRequest.getSession(false);
            if (session != null) {
                String sessionId = session.getId();
                // 使用sessionId区分WebSocketHandler，以便定向发送消息
                String sessionIdAttribute = (String) session.getAttribute("sessionId");
                if (sessionIdAttribute == null) {
                    sessionIdAttribute = sessionId;
                }
                Object userName = session.getAttribute("userName");
                attributes.put("sessionId", sessionIdAttribute);
                attributes.put("userName", userName == null ? "admin" : userName); //视频回放H5页面无登录信息，默认使用admin用户
                attributes.put("ip", new GetIpAddr().getIpAddr(httpServletRequest));
            }
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
        Exception exception) {

    }
}

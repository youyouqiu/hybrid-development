package com.zw.platform.push.controller;

import com.alibaba.fastjson.JSON;
import com.zw.platform.domain.realTimeVideo.ResourceListBean;
import com.zw.platform.service.realTimeVideo.VideoService;
import com.zw.platform.util.common.JsonResultBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

/**
 * @author Administrator
 */
@Controller
public class VideoMessageController {

    @Autowired
    private VideoService videoService;

    /**
     * 视频回放
     * 接收终端资源当月视频资源订阅消息
     */
    @MessageMapping("/video/history/month")
    @SendToUser(value = "/topic/video/history/month", broadcast = false)
    public JsonResultBean historyMonth(String message, @Header("simpSessionId") String sessionId,
        SimpMessageHeaderAccessor headerAccessor) {
        ResourceListBean resourceListBean = JSON.parseObject(message, ResourceListBean.class);
        return videoService.sendGetHistoryMonthInstruct(resourceListBean, sessionId, getUsername(headerAccessor),
            getIpAddress(headerAccessor));
    }

    /**
     * 视频回放
     * 接收终端资源单天视频资源订阅消息
     */
    @MessageMapping("/video/history/day")
    @SendToUser(value = "/topic/video/history/day", broadcast = false)
    public JsonResultBean historyDay(String message, @Header("simpSessionId") String sessionId,
        SimpMessageHeaderAccessor headerAccessor) {
        ResourceListBean resourceListBean = JSON.parseObject(message, ResourceListBean.class);
        return videoService.sendGetHistoryDayInstruct(resourceListBean, sessionId, getUsername(headerAccessor),
            getIpAddress(headerAccessor));
    }

    private String getIpAddress(SimpMessageHeaderAccessor headerAccessor) {
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        Object ipObj = sessionAttributes.get("ip");
        return ipObj == null ? null : ipObj.toString();
    }

    private String getUsername(SimpMessageHeaderAccessor headerAccessor) {
        Principal user = headerAccessor.getUser();
        return user == null ? null : user.getName();
    }
}

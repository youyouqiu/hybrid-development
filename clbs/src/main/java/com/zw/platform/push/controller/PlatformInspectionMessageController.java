package com.zw.platform.push.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zw.platform.dto.platformInspection.PlatformInspectionParamDTO;
import com.zw.platform.push.common.WsSessionManager;
import com.zw.platform.service.platformInspection.PlatformInspectionService;
import com.zw.platform.service.platformInspection.impl.PlatformInspectionServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author lijie
 */
@Controller
public class PlatformInspectionMessageController {


    @Autowired
    PlatformInspectionService platformInspectionService;



    /**
     * 平台巡检下发
     * @param requestContent
     * @param sessionId
     */
    @MessageMapping("/inspection")
    public void inspection(SimpMessageHeaderAccessor headerAccessor, String requestContent,
        @Header("simpSessionId") String sessionId) {

        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        String ip = sessionAttributes.get("ip").toString();
        List<PlatformInspectionParamDTO> platformInspectionParams = JSON.parseArray(requestContent,
            PlatformInspectionParamDTO.class);
        List<PlatformInspectionParamDTO> platformInspectionParamDTOList = new ArrayList<>();
        for (PlatformInspectionParamDTO platformInspectionParam : platformInspectionParams) {
            if (platformInspectionParam.getInspectionType().equals(PlatformInspectionServiceImpl.IDENTIFY_INSPECTION)) {
                platformInspectionService.sendDriverIdentify(platformInspectionParam, sessionId, ip);
            } else {
                platformInspectionParamDTOList.add(platformInspectionParam);
            }
        }

        //下发9710巡检
        platformInspectionService.sendPlatformInspection(platformInspectionParamDTOList, sessionId);
    }


    @MessageMapping("/unsubscribe/inspection")
    public void unsubscribeInspection(SimpMessageHeaderAccessor headerAccessor, String requestContent,
        @Header("simpSessionId") String sessionId) {
        JSONObject jsonObject = JSON.parseObject(requestContent);
        String time = jsonObject.getString("time");
        WsSessionManager.INSTANCE.removeInspectionBySessionId(sessionId + "_" + time);
    }

}

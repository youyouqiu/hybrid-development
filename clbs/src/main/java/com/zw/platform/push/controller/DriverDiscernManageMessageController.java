package com.zw.platform.push.controller;

import com.alibaba.fastjson.JSON;
import com.zw.platform.domain.basicinfo.driverDiscernManage.DriverDiscernManageIssueParam;
import com.zw.platform.service.driverDiscernManage.DriverDiscernManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * @author tianzhangxu
 */
@Controller
public class DriverDiscernManageMessageController {

    @Autowired
    private DriverDiscernManageService manageService;

    /**
     * 批量下发查询指令
     * @param requestContent requestContent
     */
    @MessageMapping("/query/batch")
    public void queryBatch(String requestContent, @Header("simpSessionId") String sessionId) {
        List<String> vehicleIds = JSON.parseArray(requestContent, String.class);
        manageService.sendQueryBatch(vehicleIds, sessionId);
    }

    /**
     * 批量下发指令下发
     * @param requestContent requestContent
     */
    @MessageMapping("/issue/batch")
    public void issueBatch(String requestContent, @Header("simpSessionId") String sessionId) {
        DriverDiscernManageIssueParam param = JSON.parseObject(requestContent, DriverDiscernManageIssueParam.class);
        manageService.sendIssueBatch(param, sessionId);
    }
}

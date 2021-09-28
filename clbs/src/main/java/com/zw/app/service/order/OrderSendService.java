package com.zw.app.service.order;

import com.alibaba.fastjson.JSONObject;

import javax.servlet.http.HttpServletRequest;


public interface OrderSendService {

    /**
     * 检查服务器通信是否正常
     * @return
     * @throws Exception
     */
    String checkServerUnobstructed() throws Exception;

    /**
     * 下发8201 位置信息查询指令
     * @param monitorId
     * @return
     */
    JSONObject send0x8201(String monitorId,HttpServletRequest request) throws Exception;
}

/*
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved. This software is the confidential and proprietary information
 * of ZhongWei, Inc. You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with ZhongWei.
 */

package com.zw.ws.common;

import com.alibaba.fastjson.JSON;
import com.zw.platform.util.common.Converter;
import com.zw.ws.entity.common.WebSocketResponse;
import com.zw.ws.entity.t808.location.RecievedLocationMessageDescription;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * <p>
 * Title: MessageEncapsulationHelper.java
 * </p>
 * <p>
 * Copyright: Copyright (c) 2016
 * </p>
 * <p>
 * Company: ZhongWei
 * </p>
 * <p>
 * team: ZhongWeiTeam
 * </p>
 * @version 1.0
 * @author: Jiangxiaoqiang
 * @date 2016年8月16日下午2:41:39
 */
@Component
public class MessageEncapsulationHelper {
    /**
     * webSocketMessageEncapsulation
     * @return String
     * @author Jiangxiaoqiang
     */
    public static <E> String webSocketMessageEncapsulation(E data, int messageType) {
        WebSocketResponse<E> webSocketResponse = new WebSocketResponse<E>();
        RecievedLocationMessageDescription kafkaRecievedLocationMessageDescription =
            new RecievedLocationMessageDescription();
        kafkaRecievedLocationMessageDescription.setMsgID(messageType);
        kafkaRecievedLocationMessageDescription.setSysTime(Converter.toString(new Date()));
        webSocketResponse.setDesc(kafkaRecievedLocationMessageDescription);
        webSocketResponse.setData(data);
        return JSON.toJSONString(webSocketResponse);
    }
}

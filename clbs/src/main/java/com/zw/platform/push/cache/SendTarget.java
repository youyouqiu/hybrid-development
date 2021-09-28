package com.zw.platform.push.cache;

import lombok.Data;

/***
 @Author zhengjc
 @Date 2019/12/13 10:44
 @Description 参数设置订阅下发的目的地
 @version 1.0
 **/
@Data
public class SendTarget {
    private SendModule sendModule;
    private String subModule;

    public static SendTarget getInstance(SendModule sendModule, String subModule) {
        SendTarget sendTarget = new SendTarget();
        sendTarget.sendModule = sendModule;
        sendTarget.subModule = subModule;
        return sendTarget;
    }

    public static SendTarget getInstance(SendModule sendModule) {
        SendTarget sendTarget = new SendTarget();
        sendTarget.sendModule = sendModule;
        return sendTarget;
    }

    public String getTargetUrl() {
        return sendModule.toString().toLowerCase();
    }

}

package com.zw.lkyw.domain;

import lombok.Data;

/***
 @Author zhengjc
 @Date 2020/1/2 11:16
 @Description 下发短信监控对象信息
 @version 1.0
 **/
@Data
public class SendMsgMonitorInfo {
    /**
     * 监控对象id
     */
    private String monitorId;

    /**
     * 监控对象名称
     */
    private String monitorName;

    /**
     * 标识颜色
     */
    private Integer signColor;

    /**
     * 对象类型
     */
    private String objectType;
    /**
     * 所属企业
     */
    private String groupName;

    public static SendMsgMonitorInfo getInstance(String monitorId, String monitorName, String groupName) {
        SendMsgMonitorInfo sendMsgMonitorInfo = new SendMsgMonitorInfo();
        sendMsgMonitorInfo.monitorId = monitorId;
        sendMsgMonitorInfo.monitorName = monitorName;
        sendMsgMonitorInfo.groupName = groupName;
        return sendMsgMonitorInfo;
    }

    public void assembelVehicleInfo(Integer signColor, String objectType) {
        this.signColor = signColor;
        this.objectType = objectType;
    }

}



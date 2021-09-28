package com.zw.platform.push.controller;

import lombok.Data;

/**
 * <p>
 * Title:
 * <p>
 * Copyright: Copyright (c) 2016
 * <p>
 * Company: ZhongWei
 * <p>
 * team: ZhongWeiTeam
 *
 * @version 1.0
 * @author: nixiangqian
 * @date 2017年08月01日 16:23
 */
@Data
public class SubscibeInfo {

    private String userName;// 用户号

    /**
     * websocket sessionId
     */
    private String sessionId;

    private String deviceid;// 设备号

    private int msgSn;// 发送消息编号

    private int respMsgId;// 返回消息编号

    private Integer count = 0;// 需返回总次数

    private Integer type = 0;// 1 需要接收0x0001和0x0900

    private String adasRiskInfos;//adas风险推送下发9208时下发风险信息

    /**
     * 下发状态记录id
     */
    private String directiveId;

    public SubscibeInfo(){}

    public SubscibeInfo(String userName, String deviceid, Integer msgSn, int respMsgId) {
        this.userName = userName;
        this.deviceid = deviceid;
        this.msgSn = (msgSn == null ? 0 : msgSn);
        this.respMsgId = respMsgId;
    }

    public SubscibeInfo(String userName, String sessionId, String deviceid, Integer msgSn, int respMsgId) {
        this.userName = userName;
        this.sessionId = sessionId;
        this.deviceid = deviceid;
        this.msgSn = (msgSn == null ? 0 : msgSn);
        this.respMsgId = respMsgId;
    }

    public SubscibeInfo(String sessionId, String deviceId, int msgSn, int respMsgId, String directiveId) {
        this.sessionId = sessionId;
        this.deviceid = deviceId;
        this.msgSn = msgSn;
        this.respMsgId = respMsgId;
        this.directiveId = directiveId;
    }

    public SubscibeInfo(String userName, String deviceid, Integer msgSn, int respMsgId, Integer type) {
        this.userName = userName;
        this.deviceid = deviceid;
        this.msgSn = (msgSn == null ? 0 : msgSn);
        this.respMsgId = respMsgId;
        this.type = type;
    }

    public SubscibeInfo(String deviceid, Integer msgSn, int respMsgId) {
        this.deviceid = deviceid;
        this.msgSn = (msgSn == null ? 0 : msgSn);
        this.respMsgId = respMsgId;
    }

    public SubscibeInfo(String deviceid, int respMsgId) {
        this.deviceid = deviceid;
        this.respMsgId = respMsgId;
    }

    public SubscibeInfo(String userName, String deviceid, Integer msgSn, int respMsgId, String adasRiskInfos) {
        this.userName = userName;
        this.deviceid = deviceid;
        this.msgSn = (msgSn == null ? 0 : msgSn);
        this.respMsgId = respMsgId;
        this.adasRiskInfos = adasRiskInfos;
    }

}

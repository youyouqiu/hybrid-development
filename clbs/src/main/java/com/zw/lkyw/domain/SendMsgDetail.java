package com.zw.lkyw.domain;

import com.zw.platform.util.common.UuidUtils;
import lombok.Data;

/**
 * 下发消息明细参数
 * @author Administrator
 */
@Data
public class SendMsgDetail {

    private byte[] monitorIdHbase;
    /**
     * 流水号
     */
    private Integer serialNumber;
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

    /**
     * 消息内容
     */
    private String msgContent;

    /**
     * 消息类型
     */
    private Integer msgId;

    /**
     * 是否紧急
     */
    private Integer isUrgent;

    /**
     * 播放方式
     */
    private String playType;

    /**
     * 下发方式
     */
    private Integer sendType;

    /**
     * 下发人
     */
    private String sendUserName;

    /**
     * 下发状态
     */
    private Integer sendStatus;

    /**
     * 下发时间
     */
    private Long sendTime;

    /**
     * 失败原因
     */
    private String failureReason;

    private SendMsgDetail(SendMsgMonitorInfo monitorInfo, SendMsgBasicInfo basicInfo) {
        assembleMonitorInfo(monitorInfo);
        assembleMsgInfo(basicInfo);
    }

    private void assembleMsgInfo(SendMsgBasicInfo basicInfo) {
        this.msgContent = basicInfo.getMsgContent();
        this.msgId = basicInfo.getMsgId();
        this.playType = basicInfo.getPlayType();
        this.sendType = basicInfo.getSendType();
        this.sendUserName = basicInfo.getSendUserName();
        this.sendStatus = basicInfo.getSendStatus();
        this.sendTime = basicInfo.getSendTime();
        this.failureReason = basicInfo.getFailureReason();
        this.serialNumber = basicInfo.getSerialNumber();
    }

    private void assembleMonitorInfo(SendMsgMonitorInfo monitorInfo) {
        this.monitorId = monitorInfo.getMonitorId();
        this.monitorIdHbase = UuidUtils.getBytesFromStr(monitorId);
        this.monitorName = monitorInfo.getMonitorName();
        this.signColor = monitorInfo.getSignColor();
        this.objectType = monitorInfo.getObjectType();
        this.groupName = monitorInfo.getGroupName();
    }

    public static SendMsgDetail getSendMsg(SendMsgMonitorInfo monitorInfo, SendMsgBasicInfo basicInfo) {
        return new SendMsgDetail(monitorInfo, basicInfo);
    }

    public static String getKey(String monitorId, Integer serialNumber) {
        return monitorId + "_" + serialNumber;
    }

    public String getKey() {
        return getKey(monitorId, serialNumber);
    }

    public String getStoreKey() {
        return monitorId + "_" + sendTime;
    }

}

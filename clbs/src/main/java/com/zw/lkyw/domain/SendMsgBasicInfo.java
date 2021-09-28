package com.zw.lkyw.domain;

import com.zw.platform.util.ConstantUtil;
import lombok.Data;

/***
 @Author zhengjc
 @Date 2020/1/2 11:14
 @Description 下发短信基础信息
 @version 1.0
 **/
@Data
public class SendMsgBasicInfo {
    /**
     * 流水号
     */
    private Integer serialNumber;
    /**
     * 消息内容
     */
    private String msgContent;

    /**
     * 消息类型
     */
    private Integer msgId = ConstantUtil.T808_SEND_TXT;

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

    public static SendMsgBasicInfo getInstance(String msgContent, Integer serialNumber, String playType,
        Integer sendType, String sendUserName, Long sendTime) {
        SendMsgBasicInfo sendMsgBasicInfo = new SendMsgBasicInfo();
        sendMsgBasicInfo.msgContent = msgContent;
        sendMsgBasicInfo.serialNumber = serialNumber;
        sendMsgBasicInfo.playType = playType;
        sendMsgBasicInfo.sendType = sendType;
        sendMsgBasicInfo.sendUserName = sendUserName;
        sendMsgBasicInfo.sendTime = sendTime;
        return sendMsgBasicInfo;
    }

    public void assembleSendResult(Integer sendStatus, String failureReason) {
        this.sendStatus = sendStatus;
        this.failureReason = failureReason;
    }
}

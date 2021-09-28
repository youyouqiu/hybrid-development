package com.zw.platform.domain.sendTxt;

import lombok.Data;


/***
 @Author zhengjc
 @Date 2019/5/24 9:57
 @Description 文本下发2019实体
 @version 1.0
 **/
@Data
public class SendTextParam {

    /**
     * 下发的车辆id
     */
    private String vehicleIds;

    /**
     * 紧急按钮由复选框 内容1:服务;2:紧急;3:通知(协议0字节0-1位)
     */

    private int messageTypeOne;

    /**
     * 终端显示（协议0字节2位）
     */
    private int terminalDisplay;

    /**
     * 终端tts读播（协议0字节3位）
     */
    private int terminalTtsPlay;

    /**
     * 0: 中心导航信息; 1: CAN故障码信息（协议0字节5位）
     */
    private int messageTypeTwo;

    /**
     * 下发的文本类型 1：通知 2：服务（协议起始字节1）
     */
    private int textType;

    /**
     * 下发的文本内容（协议起始字节2）
     */
    private String sendTextContent;

    public int getSignData() {
        terminalDisplay = terminalDisplay << 2;
        terminalTtsPlay = terminalTtsPlay << 3;
        messageTypeTwo = messageTypeTwo << 5;
        return messageTypeOne + terminalDisplay + terminalTtsPlay + messageTypeTwo;

    }

}

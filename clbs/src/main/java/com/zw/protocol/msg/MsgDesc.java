package com.zw.protocol.msg;

import com.zw.platform.util.ConstantUtil;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

/**
 * Created by LiaoYuecai on 2017/6/21.
 */
@Data
public class MsgDesc implements MsgBean {
    private String uuid = UUID.randomUUID().toString();

    private String sysTime = ConstantUtil.DATE_FORMAT.format(new Date());

    private String deviceNumber;
    private String protocol;
    private Integer msgID;
    private String monitorId;
    private String monitorName;
    private String deviceId;
    private Integer monitorType;

    //    private String msgSNAck;

    private String type = "0";

    /**
     * 消息类型 1：普通协议 2：809协议
     */
    protected Integer messageType = 0;

    private String deviceNo;

    private String vId;

    private String vNo;

    private String dId;

    private String msgSNAck;

    /**
     * 809转发配置
     */
    private String t809PlatId;// 转发平台id

    /**
     * 对应报警附件目录请求消息源子业务类型标识
     */
    private Integer sourceDataType;
    /**
     * 对应报警附件目录请求请求消息源报文序列号
     */
    private Integer sourceMsgSn;

}

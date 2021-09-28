package com.zw.protocol.msg.t809;

import com.zw.protocol.msg.MsgBean;
import lombok.Data;


/**
 * Created by LiaoYuecai on 2017/2/10.
 */
@Data
public class T809MsgHead implements MsgBean {

    private Integer msgLength = 0;//数据长度(包括头标识、数据头、数据体和尾标识)

    private Integer msgSn = 0;//报文序列号a

    private Integer msgID = 0;//业务数据类型

    private Integer msgGNSSCenterId = 0;//下级平台接入码，上级平台给下级平台分配唯一标识码。

    //协议版本好标识，上下级平台之间采用的标准协议版编号；长度为3 个字节来表示，0x01 0x02 0x0F 标识的版本号是v1.2.15，以此类推。
    private byte[] versionFlag = new byte[3];

    //报文加密标识位b: 0 表示报文不加密，1 表示报文加密。
    private Integer encryptFlag = 0;

    private String serverIp;//上级平台IP

    private Long encryptKey = 0L;//数据加密的密匙，长度为4 个字节。

    private Long time; // 发送消息时的系统UTC时间(809-2019版协议新增字段)

    private String groupId;

    private String handleId; // 上级平台处理表id

    private Integer protocolType; // 协议类型,用于前端根据不同的协议展示不同的内容(100: 35658-809)

    public Integer getMsgLength() {
        return msgLength;
    }

    public void setMsgLength(Integer msgLength) {
        this.msgLength = msgLength;
    }

}

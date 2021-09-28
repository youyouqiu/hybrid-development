package com.zw.protocol.msg.t809.body.module;

import lombok.Data;

import java.io.Serializable;


/**
 * 平台查岗、下发平台间报文数据接收实体
 *
 * @author hujun
 * @date 2018/6/6 14:50
 */
@Data
public class PlatformMsgAckInfo implements Serializable {
    private Integer infoId;

    private Integer msgDataType; // 子业务类型

    private String answer;

    private Integer objectType;

    private String objectId;

    private String serverIp;// 上级平台ip

    private Integer msgGNSSCenterId;// 接入码

    private String groupId; // 企业uuid

    private String gangId; // 上级平台消息处理表id

    private Integer msgSn; // 报文序列号

    private Integer msgID; // 业务数据类型

    private String platFormId; // 转发平台IP
}

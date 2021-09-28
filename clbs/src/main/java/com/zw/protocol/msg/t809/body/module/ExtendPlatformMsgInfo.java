package com.zw.protocol.msg.t809.body.module;

import lombok.Data;


/**
 * 西藏扩展809督办处理实体
 */
@Data
public class ExtendPlatformMsgInfo {
    private Integer infoId; // 信息id
    private Integer result; // 处理结果
    private String msgId; // 上级平台消息处理表id
    private String serverIp;// 上级平台ip
    private Integer msgGNSSCenterId;// 接入码
}

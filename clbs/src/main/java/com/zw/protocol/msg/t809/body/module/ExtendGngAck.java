package com.zw.protocol.msg.t809.body.module;

import lombok.Data;


/**
 * 扩展809查岗应答实体
 */
@Data
public class ExtendGngAck {
    private Integer infoLength; // 信息长度
    private Integer infoId; // 信息id
    private String infoContent; // 信息内容
    private String dutyman; // 查岗响应用户名
}

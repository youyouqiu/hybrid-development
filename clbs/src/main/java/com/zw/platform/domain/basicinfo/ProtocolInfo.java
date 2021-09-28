package com.zw.platform.domain.basicinfo;

import lombok.Data;

/**
 * @author denghuabing
 * @version V1.0
 * @description: 协议数据实体
 * @date 2020/6/17
 **/
@Data
public class ProtocolInfo {

    /**
     * 协议类型
     */
    private String type;

    /**
     * 协议名称
     */
    private String protocolName;

    /**
     * 协议编码
     */
    private String protocolCode;
}

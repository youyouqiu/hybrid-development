package com.zw.protocol.msg.t808;

import java.io.Serializable;

/**
 * 协议消息的公共部分(区分协议类型)
 * @author zhouzongbo on 2019/5/21 11:42
 */
public abstract class AbstractCommonProtocolInfo implements Serializable {

    private static final long serialVersionUID = -2751227700087534220L;

    /**
     * JT/T808-2013
     */
    public static final int PROTOCOL_TYPE_808_2013 = 0;

    /**
     * JT/T808-2019
     */
    public static final int PROTOCOL_TYPE_808_2019 = 1;

    public transient Integer protocolType = PROTOCOL_TYPE_808_2013;

    public Integer getProtocolType() {
        return protocolType;
    }

    public void setProtocolType(Integer protocolType) {
        this.protocolType = protocolType;
    }
}

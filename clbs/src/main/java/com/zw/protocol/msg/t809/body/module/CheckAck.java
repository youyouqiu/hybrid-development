package com.zw.protocol.msg.t809.body.module;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by LiaoYuecai on 2017/2/14.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CheckAck extends ExchangeDetail {
    private Integer infoId;

    private Integer infoLength;

    private String infoContent;

    private Integer objectType;

    private String objectId;

    /**
     * 应答人
     */
    private String responder;

    /**
     * 应答人电话
     */
    private String responderTel;

    private Integer sourceDataType;

    private Integer sourceMsgSn;
    /**
     * 巡检应答人 IP 地址
     */
    private String responderIpAddress;
    /**
     * 下级平台日志
     */
    private String responderLogs;
}

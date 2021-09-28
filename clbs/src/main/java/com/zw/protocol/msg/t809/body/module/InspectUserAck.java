package com.zw.protocol.msg.t809.body.module;

import com.zw.protocol.msg.t809.T809MsgBody;
import lombok.Data;

/**
 * @author wanxing
 * @Title: 巡检人员应答实体
 * @date 2020/12/3117:11
 */
@Data
public class InspectUserAck implements T809MsgBody {

    /**
     * 巡检对象类型
     */
    private Integer objectType;
    /**
     * 巡检对象ID
     */
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
     * 社会保险号
     */
    private String socialSecurityNumber;

    /**
     * 身份证号
     */
    private String idCardNumber;

    /**
     * 图片直接数组
     */
    private byte[] responderPhoto;
}

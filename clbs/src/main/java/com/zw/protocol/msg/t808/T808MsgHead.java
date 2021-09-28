package com.zw.protocol.msg.t808;

import com.zw.protocol.msg.MsgBean;
import lombok.Data;

/**
 * Created by LiaoYuecai on 2017/6/19.
 */
@Data
public class T808MsgHead implements MsgBean {
    public static final int PROTOCOL_TYPE_2013 = 0;

    public static final int PROTOCOL_TYPE_2019 = 1;

    protected Integer msgID = 0;//消息ID
    protected Integer bodySize = 0;//消息体长度
    protected Integer encrypt = 0;//数据加密方式
    protected Integer subPackage = 0;//分包
    protected String mobile;//手机号码
    protected Integer msgSN = 0;//流水号
    protected Integer packageSum = 0;//消息总包数
    protected Integer packageNO = 0;//包序号
    /**
     * 协议版本: 每次关键修改递增, 初始化版本为1(0: 808-2013版本; 1: 808-2019版)
     */
    private Integer type = 0;
    /**
     * 协议类型
     */
    private String deviceType;
}

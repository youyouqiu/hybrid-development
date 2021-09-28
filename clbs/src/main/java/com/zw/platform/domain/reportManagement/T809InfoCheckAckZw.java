package com.zw.platform.domain.reportManagement;

import com.zw.platform.util.StrUtil;
import com.zw.protocol.msg.MsgDesc;
import lombok.Data;

import java.io.Serializable;

@Data
public class T809InfoCheckAckZw implements Serializable {
    /**
     * 对应报警信息核查请求消息源子业务类型标识
     */
    private Integer sourceDataType;

    /**
     * 对应报警信息核查请求消息源报文序列号
     */
    private Integer sourceMsgSn;

    /**
     * 发起报警平台唯一编码，由平台所在地行政区域代码和平台编号组成
     */
    private String platformId;

    /**
     * 报警类型
     */
    private Integer warnType;

    /**
     * 报警时间，UTC时间格式
     */
    private Long warnTime;

    /**
     * 事件开始时间，用UTC时间表示
     */
    private Long startTime;

    /**
     * 事件结束时间，用UTC时间表示
     */
    private Long endTime;

    /**
     * 线路ID（JT/T 808-2019中0x8606规定的报文中的线路ID）
     */
    private Integer drvLineId;

    /**
     * 上报报警信息长度
     */
    private Integer infoLength;

    /**
     * 上报报警信息内容
     */
    private String infoContent;

    /**
     * 初始化报警信息核查请求消息源子业务类型标识和对应报警信息核查请求消息源报文序列号
     * @param desc
     */
    public void initSourceInfo(MsgDesc desc) {
        sourceDataType = desc.getMsgID();
        sourceMsgSn = desc.getMsgSNAck() == null ? 0 : Integer.valueOf(desc.getMsgSNAck());
    }

    public Integer getDataLength() {

        return 62 + infoLength;
    }

    /**
     * 初始化相关长度字段
     */
    public void initLength() {
        infoLength = StrUtil.getLen(infoContent);
    }

}

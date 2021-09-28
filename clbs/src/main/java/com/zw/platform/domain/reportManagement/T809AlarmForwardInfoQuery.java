package com.zw.platform.domain.reportManagement;

import lombok.Data;

import java.io.Serializable;

/**
 * 809报警转发信息查询实体
 */
@Data
public class T809AlarmForwardInfoQuery implements Serializable {
    /**
     * 报警信息消息或车辆定位信息报文序列号(流水号)
     */
    private Integer msgSn;

    /**
     * 业务数据类型(0x1402 0x1403)
     */
    private Integer msgId;

    /**
     * 转发平台id
     */
    private byte[] plateFormId;

    /**
     * 转发平台id
     */
    private String plateFormIdStr;

    /**
     * 查询开始时间
     */
    private Long queryStartTime;

    /**
     * 查询结束时间
     */
    private Long queryEndTime;
}

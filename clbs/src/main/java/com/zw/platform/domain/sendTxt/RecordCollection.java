package com.zw.platform.domain.sendTxt;


import com.zw.protocol.msg.t808.T808MsgBody;

import lombok.Data;


/**
 * 记录采集 @author  Tdz
 * @create 2017-04-24 9:35
 **/
@Data
public class RecordCollection implements T808MsgBody {
    private Integer sign;
    /**
     * 行驶记录数据采集 命令字
     */
    private Integer cw;

    /**
     * 数据块A
     */
    private RecordCollectionDataA data;

    private String startTime;

    private String endTime;

    /**
     * 最大单位数据块个数N(高字节0~255)
     */
    private int maxSum = 1;
}

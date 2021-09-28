package com.zw.lkyw.domain.sendMessageReport;

import lombok.Data;

/**
 * @author denghuabing on 2020/1/2 15:00
 */
@Data
public class DetailQuery {

    private String monitorId;

    /**
     * 开始时间 20191201000000(yyyyMMddHHmmss)
     */
    private String startTime;

    /**
     * 结束时间 20191201000000(yyyyMMddHHmmss)
     */
    private String endTime;

    /**
     * 消息内容
     */
    private String msgContent;

    /**
     * 下发方式  0:系统下发 1:人工下发
     */
    private String sendType;

    /**
     * 下发状态 0:下发成功 1: 下发失败
     */
    private String sendStatus;

}

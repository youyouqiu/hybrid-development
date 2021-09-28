package com.zw.lkyw.domain;

import lombok.Data;

/**
 * 视频巡检实体
 * @author denghuabing on 2019/12/27 14:17
 */
@Data
public class VideoInspectionData {

    /**
     * 	终端离线
     */
    public static final Integer OFF_LINE_MSG = 1;

    /**
     * 	视频请求超时
     */
    public static final Integer OVER_TIME_MSG = 2;

    /**
     * 	终端网络不稳定及其他
     */
    public static final Integer ERROR_MSG = 3;

    /**
     * 巡检开始时间   20191231000000(yyyyMMddHHmmss)
     */
    private String startTime;

    private String startTimeStr;

    private String monitorId;

    private String monitorName;
    //车辆类型
    private String objectType;

    private String plateColor;

    private String groupName;

    private String groupId;

    /**
     * 通道号
     */
    private Integer channelNum;

    /**
     * 状态：0：成功 1：失败
     */
    private Integer status;

    /**
     * 失败原因
     */
    private Integer failReason;
}

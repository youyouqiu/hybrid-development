package com.cb.platform.domain;

import lombok.Data;


/**
 * 道路运输企业查询时间内抽查信息统计实体(一个监控对象一天内对应同一个操作发生一次或多次均记为一次)
 */
@Data
public class VehicleSpotCheckCont {
    /**
     * 抽查定位信息次数
     */
    private Integer spotCheckPositionNumber;

    /**
     * 抽查历史轨迹次数
     */
    private Integer spotCheckHistoricalTrackNumber;

    /**
     * 查看视频次数
     */
    private Integer spotCheckVideoNumber;

    /**
     * 违章处理次数
     */
    private Integer violationHandingNumber;
}

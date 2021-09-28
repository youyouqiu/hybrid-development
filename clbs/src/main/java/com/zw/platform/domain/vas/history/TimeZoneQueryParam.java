package com.zw.platform.domain.vas.history;

import lombok.Data;

/**
 * 定时定区域查询条件类
 * @author Created by zhouzongbo on 2019/5/5.
 */
@Data
public class TimeZoneQueryParam {
    /**
     * 开始时间
     */
    private Long startTimeOne;
    /**
     * 结束时间
     */
    private Long endTimeOne;

    /**
     * 开始时间2
     */
    private Long startTimeTwo;
    /**
     * 结束时间2
     */
    private Long endTimeTwo;

    /**
     * 区域1的经纬度
     */
    private String areaOne;

    /**
     * 区域2的经纬度
     */
    private String areaTwo;

    /**
     * 区域集合
     */
    private String areaListStr;

    /**
     * 监控对象ID集合
     */
    private String monitorIds;

}

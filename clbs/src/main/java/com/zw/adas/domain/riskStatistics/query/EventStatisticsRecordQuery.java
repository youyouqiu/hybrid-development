package com.zw.adas.domain.riskStatistics.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @Description:主动安全统计报表列表传参实体
 * @Author zjc
 * @Date 2020/6/18 10:08
 */
@Data
public class EventStatisticsRecordQuery extends BaseQueryBean {
    /**
     * 车辆id,多个按照逗号隔开
     */
    private String vehicleIds;

    /**
     * 开始时间
     */
    private long startTime;
    /**
     * 结束时间
     */
    private long endTime;

    private Set<String> vehicleIdSet;

    public void init() {

        vehicleIdSet = new HashSet<>(Arrays.asList(vehicleIds.split(",")));
    }

}

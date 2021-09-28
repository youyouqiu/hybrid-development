package com.zw.adas.domain.riskStatistics.query;

import lombok.Data;

/**
 * @Description:主动安全统计报详情传参实体
 * @Author zhangqiang
 * @Date 2020/6/18 10:08
 */
@Data
public class RiskStatisticsRecordQuery {
    /**
     * 车辆id
     */
    private String vehicleId;
    /**
     * 报警类型公共field
     */
    private String commonField;
    /**
     * 开始时间
     */
    private String startTime;
    /**
     * 结束时间
     */
    private String endTime;
    /**
     * 每页条树（默认20条）
     */
    private int limit = 20;
    /**
     * es查询的searchAfter（用于上一页下一页操作）
     */
    private Object[] searchAfter;

}

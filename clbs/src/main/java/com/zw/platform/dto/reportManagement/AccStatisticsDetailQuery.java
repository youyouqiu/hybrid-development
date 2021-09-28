package com.zw.platform.dto.reportManagement;

import com.zw.platform.basic.dto.query.BasePageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ACC统计报表详情入参实体
 * @author tianzhangxu
 * @version 1.0
 * @date 2021/5/21 11:55
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AccStatisticsDetailQuery extends BasePageQuery {

    private static final long serialVersionUID = -2077723997698831334L;

    /**
     * 监控对象id,多个用逗号隔开
     */
    private String monitorId;

    /**
     * 开始时间 yyyyMMddHHmmss (开始日期的0点0分0秒)
     */
    private String startTime;

    /**
     * 结束时间 yyyyMMddHHmmss (结束日期的23时59分59秒)
     */
    private String endTime;
}

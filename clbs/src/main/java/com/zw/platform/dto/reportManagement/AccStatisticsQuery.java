package com.zw.platform.dto.reportManagement;

import com.zw.platform.basic.dto.query.BasePageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ACC统计报表列表入参实体
 * @author tianzhangxu
 * @version 1.0
 * @date 2021/5/21 11:55
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AccStatisticsQuery extends BasePageQuery {

    private static final long serialVersionUID = -1350711279608564287L;

    /**
     * 监控对象id,多个用逗号隔开
     */
    private String monitorIds;

    /**
     * 开始时间 yyyyMMdd
     */
    private String startDate;

    /**
     * 结束时间 yyyyMMdd
     */
    private String endDate;
}

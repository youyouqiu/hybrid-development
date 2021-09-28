package com.zw.platform.dto.reportManagement;

import com.zw.platform.basic.dto.query.BasePageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 加漏油详情查询入参实体
 * @author tianzhangxu
 * @version 1.0
 * @date 2021/4/6 18:05
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class OilAmountAndSpillQuery extends BasePageQuery {

    private static final long serialVersionUID = -7157496316120859807L;

    /**
     * 监控对象id
     */
    private String vehicleId;

    /**
     * 开始时间 yyyyMMddHHmmss(开始日期的0点0分0秒)
     */
    private String startTime;

    /**
     * 结束时间 yyyyMMddHHmmss(结束日期的23时59分59秒)
     */
    private String endTime;

    /**
     * 查询类型
     * 0: 加油量数据
     * 1：漏油量数据
     */
    private Integer type;
}

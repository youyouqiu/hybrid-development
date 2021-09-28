package com.zw.platform.domain.BigDataReport;

import lombok.Data;

/**
 * 大数据查询日期
 * @author hujun
 * @date 2018/9/27 17:02
 */
@Data
public class BigDataQueryDate {
    private static final long serialVersionUID = 1L;

    private long startTime;// 开始时间

    private long endTime;// 结束时间

    private String month;// 查询年月
}

package com.zw.platform.domain.vas.workhourmgt.query;

import lombok.Data;

/**
 * Created by LiaoYuecai on 2016/9/28.
 * Hbase数据源
 */
@Data
public class WorkHourDataSource {

    private String team;//车队
    private String brand;//车牌号
    private String longtitude;//经度
    private String latitude;//纬度
    private String rate;//频率
    private String duration;//持续时间
    private String status;//发动机状态
    private long vtime;//时间
    private String position;//位置

}

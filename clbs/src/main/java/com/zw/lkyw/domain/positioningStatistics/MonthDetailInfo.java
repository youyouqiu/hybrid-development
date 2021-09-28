package com.zw.lkyw.domain.positioningStatistics;

import lombok.Data;

@Data
public class MonthDetailInfo {
    /**
     * 无效数
     */
    private int invalidNum;
    /**
     * 定位数
     */
    private int totalNum;
    /**
     * 日期 时间戳,单位:秒
     */
    private long day;
    /**
     * 有效率
     */
    private double ratio;

    /**
     * 有效率(百分比)
     */
    private String ratioStr = "--";

    /**
     * 排序（定位是第几天的数据）
     */
    private int index;

    public MonthDetailInfo(int index) {
        this.index = index;
    }

    public MonthDetailInfo() {
    }
}

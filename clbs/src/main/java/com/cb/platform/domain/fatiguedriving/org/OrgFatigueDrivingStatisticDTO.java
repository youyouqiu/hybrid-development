package com.cb.platform.domain.fatiguedriving.org;

import lombok.Data;

/**
 * 企业疲劳-折线图详情
 * @author Administrator
 */
@Data
public class OrgFatigueDrivingStatisticDTO {
    /**
     * 疲劳车辆数
     */
    private Integer monitorNum;

    /**
     * 日期(格式 yyyyMMdd)
     */
    private Long day;

    /**
     * 累计疲劳次数
     */
    private Integer accumulatedNum;

    /**
     * 日间疲劳次数
     */
    private Integer dayNum;

    /**
     * 夜间疲劳次数
     */
    private Integer nightNum;

    /**
     * 总计疲劳次数
     */
    private Integer totalNum;

    /**
     * 同比率
     */
    private Double yearRate;

    /**
     * 环比率
     */
    private Double ringRatio;

    public static OrgFatigueDrivingStatisticDTO getInstance(int day) {
        OrgFatigueDrivingStatisticDTO data = new OrgFatigueDrivingStatisticDTO();
        data.monitorNum = 100;
        data.day = 20200501L + day;
        data.accumulatedNum = 30;
        data.dayNum = 30;
        data.nightNum = 40;
        data.totalNum = 100;
        data.yearRate = 30.3;
        data.ringRatio = 40.3;
        return data;
    }
}

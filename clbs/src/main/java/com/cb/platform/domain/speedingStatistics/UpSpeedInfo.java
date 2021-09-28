package com.cb.platform.domain.speedingStatistics;

import lombok.Data;

import java.util.List;

/**
 *
 * @Author zhangqiang
 * @Date 2020/5/18 10:36
 */

@Data
public class UpSpeedInfo {
    /**
     * 超速百分之20-百分之50
     */
    private int upSpeed20;
    /**
     * 超速百分之50以上；
     */
    private int upSpeed50;
    /**
     * 超速百分之20以下
     */
    private int upSpeed;

    /**
     * 每日数据
     */
    List<UpSpeedDayInfo> dayInfoList;

}

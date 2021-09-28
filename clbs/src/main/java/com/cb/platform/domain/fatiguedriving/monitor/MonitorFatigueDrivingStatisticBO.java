package com.cb.platform.domain.fatiguedriving.monitor;

import lombok.Data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 监控对象超速统计信息BO
 * @author Administrator
 */
@Data
public class MonitorFatigueDrivingStatisticBO {
    /**
     * 日间疲劳次数
     */
    private Integer totalDayNum;

    /**
     * 夜间疲劳次数
     */
    private Integer totalNightNum;

    /**
     * 累计疲劳次数
     */
    private Integer totalAccumulatedNum;

    /**
     * 统计信息
     */
    private List<MonitorFatigueDrivingStatisticDTO> statisticInfo;

    public static MonitorFatigueDrivingStatisticBO getInstance() {
        MonitorFatigueDrivingStatisticBO data = new MonitorFatigueDrivingStatisticBO();
        List<MonitorFatigueDrivingStatisticDTO> statisticInfo = new ArrayList<>();

        data.totalDayNum = 30;
        data.totalNightNum = 40;
        data.totalAccumulatedNum = 30;
        for (int i = 0; i < 30; i++) {
            statisticInfo.add(MonitorFatigueDrivingStatisticDTO.getInstance(i));
        }
        statisticInfo.sort(Comparator.comparing(MonitorFatigueDrivingStatisticDTO::getDay));
        data.statisticInfo = statisticInfo;
        return data;
    }
}

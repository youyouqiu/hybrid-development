package com.cb.platform.domain.fatiguedriving.org;

import lombok.Data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 企业疲劳图形数据-折线图和圆形图
 * @author Administrator
 */
@Data
public class OrgFatigueDrivingStatisticBO {

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
    private List<OrgFatigueDrivingStatisticDTO> statisticInfo;

    public static OrgFatigueDrivingStatisticBO getInstance() {
        OrgFatigueDrivingStatisticBO data = new OrgFatigueDrivingStatisticBO();
        List<OrgFatigueDrivingStatisticDTO> statisticInfo = new ArrayList<>();

        data.totalDayNum = 30;
        data.totalNightNum = 40;
        data.totalAccumulatedNum = 30;
        for (int i = 0; i < 30; i++) {
            statisticInfo.add(OrgFatigueDrivingStatisticDTO.getInstance(i));
        }
        statisticInfo.sort(Comparator.comparing(OrgFatigueDrivingStatisticDTO::getDay));
        data.statisticInfo = statisticInfo;
        return data;
    }

}

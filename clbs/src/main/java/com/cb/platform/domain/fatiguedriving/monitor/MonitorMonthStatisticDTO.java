package com.cb.platform.domain.fatiguedriving.monitor;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 单监控对象疲劳排名
 * @author Administrator
 */
@Data
public class MonitorMonthStatisticDTO {
    /**
     * 监控对象名称
     */
    private String monitorName;

    /**
     * 所属企业
     */
    private String groupName;

    /**
     * 车牌颜色
     */
    private String plateColor;

    /**
     * 对象类型
     */
    private String objectType;

    /**
     * 日间疲劳次数
     */
    private Integer dayNum;

    /**
     * 夜间疲劳次数
     */
    private Integer nightNum;

    /**
     * 累计疲劳次数
     */
    private Integer accumulatedNum;

    /**
     * 排名
     */
    private Integer rank;

    private static MonitorMonthStatisticDTO getInstance() {
        MonitorMonthStatisticDTO data = new MonitorMonthStatisticDTO();
        data.monitorName = "闽C3232";
        data.groupName = "中位科技";
        data.plateColor = "红色";
        data.objectType = "客车";
        data.dayNum = 30;
        data.nightNum = 30;
        data.accumulatedNum = 40;
        data.rank = 20;
        return data;
    }

    public static List<MonitorMonthStatisticDTO> getList(long length) {
        List<MonitorMonthStatisticDTO> list = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            list.add(getInstance());
        }
        return list;
    }
}

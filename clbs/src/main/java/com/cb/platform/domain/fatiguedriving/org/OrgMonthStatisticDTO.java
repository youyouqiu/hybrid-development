package com.cb.platform.domain.fatiguedriving.org;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 单企业疲劳排名
 * @author Administrator
 */
@Data
public class OrgMonthStatisticDTO {
    /**
     * 企业名称
     */
    private String organizationName;

    /**
     * 疲劳车辆数
     */
    private Integer monitorNum;

    /**
     * 疲劳次数
     */
    private Integer totalNum;

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

    private static OrgMonthStatisticDTO getInstance() {
        OrgMonthStatisticDTO data = new OrgMonthStatisticDTO();
        data.organizationName = "中位科技";
        data.monitorNum = 100;
        data.totalNum = 100;
        data.dayNum = 30;
        data.nightNum = 30;
        data.accumulatedNum = 40;
        data.rank = 20;
        return data;
    }

    public static List<OrgMonthStatisticDTO> getList(long length) {
        List<OrgMonthStatisticDTO> list = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            list.add(getInstance());
        }
        return list;
    }
}

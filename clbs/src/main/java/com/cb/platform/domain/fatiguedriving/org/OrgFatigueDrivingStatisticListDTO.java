package com.cb.platform.domain.fatiguedriving.org;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 企业疲劳列表数据
 * @author Administrator
 */
@Data
public class OrgFatigueDrivingStatisticListDTO {
    /**
     * 企业id
     */
    private String organizationId;

    /**
     * 企业名称
     */
    private String organizationName;

    /**
     * 企业疲劳监控对象数
     */
    private Integer monitorNum;

    /**
     * 累计疲劳数
     */
    private Integer accumulatedNum;

    /**
     * 日间疲劳数
     */
    private Integer dayNum;

    /**
     * 夜间疲劳数
     */
    private Integer nightNum;

    /**
     * 合计
     */
    private Integer totalNum;

    /**
     * 日期(格式:yyyyMM)
     */
    private String day;

    private static OrgFatigueDrivingStatisticListDTO getInstance() {
        OrgFatigueDrivingStatisticListDTO data = new OrgFatigueDrivingStatisticListDTO();
        data.organizationId = "01218fca-4e97-1039-9581-ad4b9fb0a647";
        data.organizationName = "中位科技111";
        data.monitorNum = 23;
        data.accumulatedNum = 30;
        data.dayNum = 40;
        data.nightNum = 30;
        data.totalNum = 100;
        data.day = "20200528";
        return data;
    }

    public static List<OrgFatigueDrivingStatisticListDTO> getList(long length) {
        List<OrgFatigueDrivingStatisticListDTO> list = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            list.add(getInstance());
        }
        return list;
    }
}

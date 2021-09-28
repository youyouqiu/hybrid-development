package com.cb.platform.domain.fatiguedriving.org;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 企业监控对象疲劳详情列表
 * @author Administrator
 */
@Data
public class OrgMonitorFatigueDrivingListDTO {
    /**
     * 监控对象名称
     */
    private String monitorName;

    /**
     * 车牌颜色
     */
    private String plateColor;

    /**
     * 对象类型
     */
    private String objectType;

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

    public static OrgMonitorFatigueDrivingListDTO getInstance() {
        OrgMonitorFatigueDrivingListDTO data = new OrgMonitorFatigueDrivingListDTO();
        data.monitorName = "闽A33232";
        data.objectType = "客车";
        data.plateColor = "红色";
        data.accumulatedNum = 30;
        data.dayNum = 40;
        data.nightNum = 30;
        data.totalNum = 100;
        return data;
    }

    public static List<OrgMonitorFatigueDrivingListDTO> getList(long length) {
        List<OrgMonitorFatigueDrivingListDTO> list = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            list.add(getInstance());
        }
        return list;
    }
}

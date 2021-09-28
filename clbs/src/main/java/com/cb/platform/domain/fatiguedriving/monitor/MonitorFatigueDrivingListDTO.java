package com.cb.platform.domain.fatiguedriving.monitor;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 监控对象疲劳统计列表
 * @author Administrator
 */
@Data
public class MonitorFatigueDrivingListDTO {
    /**
     * 监控对象id
     */
    private String monitorId;

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

    public static MonitorFatigueDrivingListDTO getInstance() {
        MonitorFatigueDrivingListDTO data = new MonitorFatigueDrivingListDTO();
        data.monitorId = UUID.randomUUID().toString();
        data.monitorName = "闽C33232";
        data.groupName = "中位科技111";
        data.plateColor = "彩色";
        data.objectType = "客车";
        data.accumulatedNum = 30;
        data.nightNum = 40;
        data.dayNum = 30;
        data.totalNum = 100;
        return data;
    }

    public static List<MonitorFatigueDrivingListDTO> getList(long length) {
        List<MonitorFatigueDrivingListDTO> list = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            list.add(getInstance());
        }
        return list;
    }
}

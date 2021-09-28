package com.zw.platform.domain.oil;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;


@Data
@EqualsAndHashCode(callSuper = false)
public class PositionalQuery extends BaseQueryBean implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int EXPORT_LOCATION = 1;

    /**
     * 显示基站定位(导出标识)
     */
    public static final int EXPORT_STATION = 1;

    private String vehicleId;

    private String startTime;

    private String endTime;

    private Long startTimeLong;

    private Long endTimeLong;

    /**
     * 是否导出位置: 0: 不导出; 1: 导出
     */
    private Integer isExportLocation = 0;

    /**
     * .0: 导出全部数据; 1: 导出行驶数据
     */
    private Integer flag = 0;

    /**
     * 0: 全部; 1: OBD数据; 2: 报警数据
     */
    public Integer tab = 0;

    /**
     * .0: 不显示基站定位; 1: 显示基站定位
     */
    private Integer isStationEnabled = 0;

    /**
     * 导出列表标识: 全部数据: TRACKPLAY_DATA; OBD数据: TRACKPLAY_OBD_LIST; 报警数据: TRACKPLAY_ALARM;停止数据:TRACKPLAY_STOP
     * ;行驶段数据：TRACKPLAY_RUN
     */
    private String mark;

    public String getTitle() {
        String title;
        switch (tab) {
            case 1:
                title = "OBD数据";
                break;
            case 2:
                title = "报警数据";
                break;
            case 3:
                title = "停止段数据";
                break;
            case 4:
                title = "行驶段数据";
                break;
            default:
                title = (flag == 0 ? "全部数据" : "行驶数据");
                break;
        }
        return title;
    }

    private List<String> customColumnList;
}

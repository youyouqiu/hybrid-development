package com.cb.platform.domain.speedingStatistics.quey;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;

/**
 * @Description: 超速统计分页查询参数
 * @Author zhangqiang
 * @Date 2020/5/18 11:17
 */
@Data
public class UpSpeedInfoVehicleQuery extends BaseQueryBean {
    /**
     * 车辆id
     */
    private String vehicleIds;
    /**
     * 查询时间
     */
    private String time;
    /**
     * 模糊查询参数
     */
    private String fuzzyParameter;

}

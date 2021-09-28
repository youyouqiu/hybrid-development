package com.zw.platform.service.workhourmgt;


import com.alibaba.fastjson.JSONObject;
import com.zw.platform.domain.vas.workhourmgt.SensorVehicleInfo;
import com.zw.platform.domain.vas.workhourmgt.query.WorkHourQuery;
import com.zw.platform.util.common.PageGridBean;

import java.util.List;

/**
 * @author ponghj
 */
public interface WorkHourStatisticsService {


    /**
     * 获得总数据表格内容
     * @param query
     * @return
     * @throws Exception
     */
    PageGridBean getTotalDataFormInfo(WorkHourQuery query) throws Exception;

    /**
     * 获得图表内容
     * @param query
     * @param isApp 判断是否是App调用改接口
     * @return
     * @throws Exception
     */
    JSONObject getChartInfo(WorkHourQuery query, boolean isApp) throws Exception;

    /**
     * 获得传感器绑定的车辆(公用)
     * @param sensorType 1: 温度传感器;2: 湿度传感器;3: 正反转传感器;4: 工时传感器;5：液位传感器；6：载重传感器
     * @return list
     * @throws Exception Exception
     */
    List<SensorVehicleInfo> getBindVehicle(Integer sensorType);
}

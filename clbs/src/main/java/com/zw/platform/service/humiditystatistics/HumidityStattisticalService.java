package com.zw.platform.service.humiditystatistics;

import com.zw.platform.domain.vas.f3.HumidityStatisics;

import java.util.List;

/**
 * 湿度统计dao
 * Created by Administrator on 2017/7/17.
 */
public interface HumidityStattisticalService {
    /**
     * 根据车辆id和时间查找车辆的湿度等信息
     */
    List<HumidityStatisics> findHumidityByVehicleId(String startTime, String endTime, String vehicleId)
        throws Exception;
}

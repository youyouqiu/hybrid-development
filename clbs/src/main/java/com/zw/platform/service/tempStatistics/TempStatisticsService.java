package com.zw.platform.service.tempStatistics;

import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.domain.vas.f3.TempStatistics;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 温度传感器统计
 * Created by Administrator on 2017/7/12.
 */
public interface TempStatisticsService {
    /**
     * 根据车辆id和时间查询温度传感器信息
     */
    public List<TempStatistics> findVehicleDataByBrand(String starTime, String endTime, String vehicleBrand)
        throws Exception;

    /**
     * 导出温度报表
     * @param response
     * @param type
     * @param redisKey
     * @param fileName
     * @throws IOException
     */
    void exportTempStatisticsList(HttpServletResponse response, int type, RedisKey redisKey, String fileName)
            throws IOException;
}

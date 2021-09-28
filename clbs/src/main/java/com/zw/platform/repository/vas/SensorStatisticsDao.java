package com.zw.platform.repository.vas;

import com.zw.platform.domain.vas.f3.HumidityStatisics;
import com.zw.platform.domain.vas.f3.TempStatistics;
import com.zw.platform.domain.vas.f3.WinchStatistics;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 温度/湿度/传感器管理统计
 * @author Administrator
 *
 */
public interface SensorStatisticsDao {
	/**
	 * 根据时间和车辆id查询温度传感器数据
	 * @param vehicleId
	 * @param startTime
	 * @param endTime
	 */
 	List<TempStatistics> findVehicleDataByVehicleId(@Param("startTimes") long startTimes, @Param("endTimes") long endTimes, @Param("vehicleId") String vehicleId);

	/**
	 * 根据时间和车辆id查询湿度传感器数据
	 */
	List<HumidityStatisics> findWetnessDataByVehicleId(@Param("startTimes") long startTimes, @Param("endTimes") long endTimes, @Param("vehicleId") String vehicleId);

	/**
	 * 根据时间和车辆id查询正反转传感器数据
	 *
	 */
    List<WinchStatistics> findWinchDataByVehicleId(@Param("vehicleId") String vehicleId, @Param("startTime") long startTime, @Param("endTime") long endTime);
}

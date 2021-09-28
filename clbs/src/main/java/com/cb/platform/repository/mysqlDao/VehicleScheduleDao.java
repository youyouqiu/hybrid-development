package com.cb.platform.repository.mysqlDao;

import com.cb.platform.domain.VehicleScheduler;
import java.util.List;
import org.apache.ibatis.annotations.Param;


public interface VehicleScheduleDao {

    /**
     * 新增车辆调度信息
     * @param vehicleScheduler
     * @return
     */
    boolean add(VehicleScheduler vehicleScheduler);

    /**
     * 批量新增车辆调度信息
     * @param vehicleSchedulerList
     */
    void addByBatch(List<VehicleScheduler> vehicleSchedulerList);

    /**
     * 车辆调度信息道路运输企业统计月报表查询接口
     * @param list
     * @param sendDate
     * @return
     * @throws Exception
     */
    List<VehicleScheduler> getEnterpriseList(@Param("list") List<String> list,@Param("vehiclelist") List<String> vehiclelist, @Param("sendDate") String sendDate);

    /**
     * 车辆调度信息统计月报表查询接口
     * @param list
     * @param sendDate
     * @return
     */
    List<VehicleScheduler> getVehicleList(@Param("list") List<String> list, @Param("sendDate") String sendDate);

    /**
     *  车辆调度信息明细表查询接口
     * @param list
     * @param startTime
     * @param endTime
     * @return
     */
    List<VehicleScheduler> getDetailList(@Param("list") List<String> list, @Param("startTime") String startTime,
                                         @Param("endTime") String endTime);

}

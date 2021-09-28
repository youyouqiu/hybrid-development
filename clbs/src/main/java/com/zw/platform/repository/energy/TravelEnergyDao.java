package com.zw.platform.repository.energy;


import com.zw.platform.domain.energy.Energy;
import com.zw.platform.domain.energy.TravelEnergy;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;


public interface TravelEnergyDao {

    List<TravelEnergy> find(@Param("vehicleId") String vehicleId);

    Energy getEnergy(@Param("vehicleId") String vehicleId);

    boolean delete(@Param("id") String id);

    boolean add(@Param("param") TravelEnergy travelEnergy);

    boolean update(@Param("param") Energy energy);

    boolean insert(@Param("param") Energy energy);

    /**
     * 根据车辆id查询行驶基准最大结束时间
     * @param vehicleId
     * @return
     */
    public Date findBaseTimeByVehicleId(@Param("vehicleId") String vehicleId);
}

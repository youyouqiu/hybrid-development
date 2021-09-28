package com.zw.platform.repository.energy;


import com.zw.platform.domain.energy.IdleEnergy;
import com.zw.platform.domain.energy.IdleStandard;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;


public interface IdleEnergyDao {

    /**
     * 根据车辆id查询全部基准
     * @param vehicleId
     * @return
     */
    public List<IdleStandard> findIdleStandards(@Param("vehicleId") String vehicleId);

    /**
     * 根据车辆id查询怠速基准汇总数据
     */
    public IdleEnergy findIdleEnergyByVehicleId(@Param("vehicleId") String vehicleId);

    /**
     * 根据基准id删除怠速基准
     * @param id
     * @return
     */
    public boolean deleteIdleStandards(@Param("id") String id);

    /**
     * 根据车辆id存入怠速汇总数据
     * @param idleEnergy
     * @return
     */
    public boolean upDateIdleCollectData(IdleEnergy idleEnergy);

    /**
     * 根据车辆id添加怠速基准
     * @param idleStandard
     * @return
     */
    public boolean addIdleStandards(IdleStandard idleStandard);

    /**
     * 根据车辆id查询基准最大结束时间
     * @param vehicleId
     * @return
     */
    public Date findBaseTimeByVehicleId(@Param("vehicleId") String vehicleId);
}

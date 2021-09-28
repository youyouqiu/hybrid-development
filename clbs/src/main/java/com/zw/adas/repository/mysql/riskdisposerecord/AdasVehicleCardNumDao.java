package com.zw.adas.repository.mysql.riskdisposerecord;

import com.zw.adas.domain.driverStatistics.VehicleIcHistoryDO;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 维护车辆 和从业资格证号关系
 *
 * @author XK
 */
public interface AdasVehicleCardNumDao {

    Set<String> findAllBindIcCardVehicleId();

    int insert(VehicleIcHistoryDO entity);

    Set<String> listUniqueIdentificationNumber(@Param("vehicleIds") Collection<String> vehicleIds);

    List<Map<String, String>> listOldData(@Param("skipId") String skipId, @Param("batchSize") int batchSize);

    int batchInsert(List<VehicleIcHistoryDO> list);
}

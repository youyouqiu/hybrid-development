package com.zw.platform.repository.vas;

import com.zw.platform.domain.vas.oilmgt.FuelVehicle;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by Tdz on 2016/9/21.
 */
public interface OilStatisticalDao {
//    List<Positional> getOilInfo();
    List<FuelVehicle> getVehiceInfo(@Param("userId") String userId,@Param("groupId") String groupId);
}

package com.cb.platform.repository.mysqlDao;

import com.cb.platform.domain.VehicleTravelForm;
import com.cb.platform.domain.VehicleTravelQuery;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;


public interface VehicleTravelDao {
    void addVehicleTravel(VehicleTravelForm vehicleTravelForm);

    void deleteVehicleTravelById(String id);

    void updateVehicleTravel(VehicleTravelForm form);

    VehicleTravelForm findVehicleTravelById(String id);

    List<VehicleTravelForm> searchVehicleTravels(@Param("query") VehicleTravelQuery query);

    List<String> isRepeateTravelId(@Param("id") String id, @Param("travelId") String travelId);

    void addVehicleTravelByBatch(@Param("vehicleTravelForms") List<VehicleTravelForm> vehicleTravelForms);

    String findLogTravelIdsByIds(@Param("ids") List<String> ids);

    String findLogTravelIdsByVehicleIds(@Param("vehicleIds") List<String> vehicleIds);

    void deleteVehicleTravelByIds(@Param("ids") String ids);

    void deleteVehicleTravelByVehicleIds(@Param("vehicleIds") List<String> vehicleIds);
}

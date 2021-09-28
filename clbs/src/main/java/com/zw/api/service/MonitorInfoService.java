package com.zw.api.service;

import com.zw.api.domain.DriverInfo;
import com.zw.api.domain.LocationInfo;
import com.zw.api.domain.MonitorInfo;
import com.zw.api.domain.VehicleInfo;
import com.zw.platform.util.common.BusinessException;

import java.util.List;
import java.util.Set;

public interface MonitorInfoService {
    MonitorInfo findMonitorByName(String name);

    boolean addVehicle(VehicleInfo vehicleInfo) throws BusinessException;

    List<LocationInfo> fetchLatestLocation(String name);

    List<LocationInfo> queryHistoryLocations(String name, String startTime, String endTime);

    Set<String> getMonitorIdByName(List<String> name);

    DriverInfo getDriver(String name);
}

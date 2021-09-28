package com.zw.api.repository.mysql;

import com.zw.api.domain.DriverInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

public interface MonitorInfoDao {
    Set<String> getIdByName(@Param("name") List<String> name);

    DriverInfo getDriverInfo(@Param("name") String name, @Param("identity") String identity);
}

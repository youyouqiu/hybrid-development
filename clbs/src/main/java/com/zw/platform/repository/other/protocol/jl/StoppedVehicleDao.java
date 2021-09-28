package com.zw.platform.repository.other.protocol.jl;

import com.zw.platform.domain.other.protocol.jl.dto.StoppedVehicleRecordDto;
import com.zw.platform.domain.other.protocol.jl.dto.StoppedVehicleRecordQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface StoppedVehicleDao {

    List<StoppedVehicleRecordDto> recordPage(@Param("query") StoppedVehicleRecordQuery query);

    boolean addRecode(@Param("list") List<StoppedVehicleRecordDto> list);
}

package com.zw.platform.service.other.protocol.jl;

import com.zw.platform.domain.other.protocol.jl.query.QueryAloneVehicleInfo;
import com.zw.platform.util.common.JsonResultBean;

public interface QueryVehicleInfoService {

    JsonResultBean query(QueryAloneVehicleInfo info) throws Exception;
}

package com.zw.platform.service.other.protocol.jl;

import com.alibaba.fastjson.JSONObject;
import com.cb.platform.util.page.PassCloudResultBean;
import com.github.pagehelper.Page;
import com.zw.platform.domain.other.protocol.jl.dto.StoppedVehicleQuery;
import com.zw.platform.domain.other.protocol.jl.dto.StoppedVehicleRecordDto;
import com.zw.platform.domain.other.protocol.jl.dto.StoppedVehicleRecordQuery;
import com.zw.platform.util.common.JsonResultBean;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 停运车辆
 * @author create by zhouzongbo
 */
public interface StoppedVehicleService {

    PassCloudResultBean page(StoppedVehicleQuery query);

    JsonResultBean upload(List<StoppedVehicleRecordDto> list) throws Exception;

    Page<StoppedVehicleRecordDto> recordPage(StoppedVehicleRecordQuery query);

    void exportStoppedRecord(StoppedVehicleRecordQuery query, HttpServletResponse response) throws IOException;

    List<JSONObject> getStoppedPlatformInfo();
}

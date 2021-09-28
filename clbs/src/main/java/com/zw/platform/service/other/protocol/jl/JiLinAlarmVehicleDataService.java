package com.zw.platform.service.other.protocol.jl;

import com.github.pagehelper.Page;
import com.zw.platform.domain.other.protocol.jl.dto.AlarmVehicleDTO;
import com.zw.platform.domain.other.protocol.jl.query.AlarmVehiclePageReq;
import com.zw.platform.domain.other.protocol.jl.query.AlarmVehicleReq;
import com.zw.platform.domain.other.protocol.jl.query.SingleAlarmVehicleReq;
import com.zw.platform.util.common.JsonResultBean;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/6/12 14:14
 */
public interface JiLinAlarmVehicleDataService {

    /**
     * 单个/统一设置-报警车辆上传
     * @param vehicleReq vehicleReq
     * @return JsonResultBean
     * @throws Exception Exception
     */
    JsonResultBean insertAlarmUpload(AlarmVehicleReq vehicleReq) throws Exception;

    /**
     * 批量(分别设置)报警车辆上传
     * @param vehicleReqList vehicleReqList
     * @return JsonResultBean
     * @throws Exception Exception
     */
    JsonResultBean insertBatchAlarmUpload(List<SingleAlarmVehicleReq> vehicleReqList) throws Exception;

    /**
     * 报警车辆上传记录列表
     * @param alarmVehiclePageReq req
     * @return Page<AlarmVehicleDTO>
     */
    Page<AlarmVehicleDTO> listAlarmVehicle(AlarmVehiclePageReq alarmVehiclePageReq);

    /**
     * 导出报警车辆上传记录
     * @param httpServletResponse httpServletResponse
     * @param alarmVehiclePageReq req
     * @throws Exception Exception
     */
    void exportAlarmList(HttpServletResponse httpServletResponse, AlarmVehiclePageReq alarmVehiclePageReq)
        throws Exception;
}

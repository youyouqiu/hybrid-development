package com.zw.platform.repository.other.protocol.jl;

import com.zw.platform.domain.other.protocol.jl.dto.AlarmVehicleDO;
import com.zw.platform.domain.other.protocol.jl.query.AlarmVehiclePageReq;

import java.util.List;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/6/15 14:28
 */
public interface AlarmVehicleDao {

    /**
     * 批量新增报警上传记录
     * @param uploadRecords 记录
     * @return boolean
     */
    boolean insertBatchAlarmUploadRecord(List<AlarmVehicleDO> uploadRecords);

    /**
     * 分页查询
     * @param alarmVehiclePageReq req
     * @return List<AlarmVehicleDO>
     */
    List<AlarmVehicleDO> listAlarmVehicleUploadRecord(AlarmVehiclePageReq alarmVehiclePageReq);
}

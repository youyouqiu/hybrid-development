package com.zw.platform.repository.other.protocol.jl;

import com.zw.platform.domain.other.protocol.jl.dto.ViolateVehicleDO;
import com.zw.platform.domain.other.protocol.jl.query.ViolateVehiclePageReq;

import java.util.List;

/**
 * 违规车辆 dao
 * @author create by zhouzongbo on 2020/6/12.
 */
public interface ViolateVehicleDao {

    /**
     * 批量插入车辆上传记录列表
     * @param violateVehicleList list
     * @return boolean
     */
    boolean insertBatchViolateUpload(List<ViolateVehicleDO> violateVehicleList);

    /**
     * 分页查询
     * @param violateVehiclePageReq req
     * @return list
     */
    List<ViolateVehicleDO> listViolateVehicle(ViolateVehiclePageReq violateVehiclePageReq);
}
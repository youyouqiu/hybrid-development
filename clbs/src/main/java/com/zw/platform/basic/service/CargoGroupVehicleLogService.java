package com.zw.platform.basic.service;

import com.zw.platform.basic.dto.VehicleDTO;

import java.util.List;
import java.util.Map;

/**
 * 记录货运车辆的增删改成记录
 *
 * @author zhangjuan
 */
public interface CargoGroupVehicleLogService {
    /**
     * 新增记录
     *
     * @param vehicleDTO 车辆信息
     */
    void add(VehicleDTO vehicleDTO);

    /**
     * 删除货运记录
     *
     * @param vehicleList 车辆列表
     */
    void delete(List<VehicleDTO> vehicleList);

    /**
     * 更新货运记录
     *
     * @param curVehicle 当前车辆信息
     * @param oldVehicle 历史车辆信息
     */
    void update(VehicleDTO curVehicle, VehicleDTO oldVehicle);


    /**
     * 批量更新货运报表记录
     *
     * @param curVehicleList 当前车辆信息列表
     * @param oldVehicleMap  历史车辆map
     */
    void updateBatch(List<VehicleDTO> curVehicleList, Map<String, VehicleDTO> oldVehicleMap);
}

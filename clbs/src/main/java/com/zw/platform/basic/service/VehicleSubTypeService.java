package com.zw.platform.basic.service;

import com.github.pagehelper.Page;
import com.zw.platform.basic.dto.VehicleSubTypeDTO;
import com.zw.platform.domain.basicinfo.query.VehicleSubTypeQuery;
import com.zw.platform.util.common.BusinessException;

import java.util.Collection;
import java.util.List;

/**
 * 车辆子类型管理接口
 */
public interface VehicleSubTypeService {
    /**
     * 添加车辆子类型
     * @param subTypeDTO 车辆子类型实体
     * @return 是否添加成功
     * @throws BusinessException 异常
     */
    boolean add(VehicleSubTypeDTO subTypeDTO) throws BusinessException;

    /**
     * 判断辆子类型是否已经存在
     * @param id          辆子类型ID 可为空
     * @param vehicleType 车辆
     * @param name        辆子类型名称
     * @return true 已经存在 false 不存在
     */
    boolean isExistSubType(String id, String vehicleType, String name);

    /**
     * 更新辆子类型
     * @param subTypeDTO subTypeDTO
     * @return 是否更新成功
     * @throws BusinessException 异常
     */
    boolean update(VehicleSubTypeDTO subTypeDTO) throws BusinessException;

    /**
     * 删除
     * @param id id
     * @return 是否删除成功
     * @throws BusinessException 异常
     */
    boolean delete(String id) throws BusinessException;

    /**
     * 批量删除
     * @param ids ids
     * @return 删除的车辆类别名称
     */
    String deleteBatch(Collection<String> ids);

    /**
     * 根据ID获取详情
     * @param id id
     * @return 详情
     */
    VehicleSubTypeDTO getById(String id);

    /**
     * 根据关键字查询车辆子类型
     * @param keyword keyword
     * @return 车辆类别列表
     */
    List<VehicleSubTypeDTO> getAllByKeyword(String keyword);

    /**
     * 检查紫雷水是否绑定车辆
     * @param id id
     * @return true 绑定 false 未绑定
     */
    boolean checkVehicleSubTypeIsBinding(String id);

    /**
     * 获取子类型分页查询
     * @param subTypeQuery 查询条件
     * @return 分页查询结果
     */
    Page<VehicleSubTypeDTO> getByPage(VehicleSubTypeQuery subTypeQuery);

    /**
     * 根据车辆类型获取子类型
     * @param vehicleTypeId 车辆类型ID
     * @return 车辆子类型
     */
    List<VehicleSubTypeDTO> getByType(String vehicleTypeId);

}

package com.zw.platform.basic.repository;

import com.zw.platform.basic.domain.VehicleCategoryDO;
import com.zw.platform.basic.dto.VehicleCategoryDTO;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * zw_m_vehicle_category
 * @author zhangjuan
 * @date 2020/10/16
 */
public interface NewVehicleCategoryDao {
    /**
     * 根据名称获取车辆类别
     * @param name 车辆类别名称
     * @return 车辆类别
     */
    VehicleCategoryDO getByName(@Param("name") String name);

    /**
     * 根据车辆类型id查询类别信息
     * @param vehicleTypeId 辆类型id
     * @return VehicleCategoryDO
     */
    VehicleCategoryDO getByVehicleTypeId(String vehicleTypeId);

    /**
     * 查询车辆类别
     * @param category 车辆类别
     * @return VehicleCategoryDO
     */
    VehicleCategoryDO findByCategory(String category);

    /**
     * 获取所有车辆类别
     * @return VehicleCategoryDO
     */
    List<VehicleCategoryDO> getAll();

    /**
     * 插入车辆类别
     * @param vehicleCategoryDO 车辆类别数据库实体
     * @return 是否插入成功
     */
    boolean insert(VehicleCategoryDO vehicleCategoryDO);

    /**
     * 更新车辆类别
     * @param vehicleCategoryDO 车辆类别
     * @return 是否更新成功
     */
    boolean update(VehicleCategoryDO vehicleCategoryDO);

    /**
     * 获取详情
     * @param id id
     * @return 详情
     */
    VehicleCategoryDTO getById(@Param("id") String id);

    /**
     * 根据关键字查询
     * @param keyword 关键字
     * @return 类别列表
     */
    List<VehicleCategoryDTO> getByKeyword(String keyword);

    /**
     * 根据ID获取类别列表
     * @param ids ids
     * @return 类别列表
     */
    List<VehicleCategoryDTO> getByIds(@Param("ids") Collection<String> ids);

    /**
     * 批量删除
     * @param ids ids
     * @return 删除条数
     */
    int deleteBatch(@Param("ids") Collection<String> ids);

    /**
     * 删除
     * @param id id
     * @return boolean
     */
    boolean delete(String id);

    /**
     * 根据图标ID获取车辆类别
     * @param iconId 图标id
     * @return 车辆类别列表
     */
    List<VehicleCategoryDTO> getByIcon(@Param("iconId") String iconId);

    /**
     * 根据类别标准查询
     * @param standard 类别标准
     * @return VehicleCategoryDO
     */
    List<VehicleCategoryDO> findByStandard(Integer standard);

}

package com.zw.platform.basic.repository;

import com.github.pagehelper.Page;
import com.zw.platform.basic.domain.VehicleTypeDO;
import com.zw.platform.basic.dto.VehicleSubTypeDTO;
import com.zw.platform.domain.basicinfo.query.VehicleSubTypeQuery;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * zw_m_vehicle_type -- 车辆子类专用
 * @author zhangjuan
 * @date 2020/10/19
 */
public interface NewVehicleSubTypeDao {
    /**
     * 获取某个类型下车辆子类型对应的类型详情
     * @param vehicleType 车辆类型
     * @param name        子类型名称
     * @return 详情
     */
    VehicleSubTypeDTO getByName(@Param("vehicleType") String vehicleType, @Param("name") String name);

    /**
     * 根据子类型名查询子类型
     * @param vehicleType    类型名
     * @param vehicleSubType 子类型名
     * @return VehicleSubTypeDTO
     */
    VehicleSubTypeDTO getByVehicleTypeAndSubType(@Param("vehicleType") String vehicleType,
        @Param("vehicleSubType") String vehicleSubType);

    /**
     * 根据车辆子类型查询子类型
     * @param vehicleSubType 车辆子类型
     * @return VehicleSubTypeDTO
     */
    VehicleSubTypeDTO findByVehicleSubType(String vehicleSubType);

    /**
     * 根据车辆id查询子类别中的图标
     * @param vehicleId vehicleId
     * @return VehicleSubTypeDTO
     */
    VehicleSubTypeDTO findByVehicleId(String vehicleId);

    /**
     * 根据ID获取车辆子类型
     * @param id id
     * @return 子类型
     */
    VehicleSubTypeDTO getById(@Param("id") String id);

    /**
     * 根据ID集合获取车辆子类型
     * @param ids ID集合
     * @return 子类型列表
     */
    List<VehicleSubTypeDTO> getByIds(@Param("ids") Collection<String> ids);

    /**
     * 根据关键字搜索车辆子类型
     * @param keyword keyword
     * @return 子类型列表
     */
    List<VehicleSubTypeDTO> getByKeyword(String keyword);

    /**
     * 添加
     * @param vehicleTypeDO 车辆子类型
     * @return 是否操作成功
     */
    boolean insert(VehicleTypeDO vehicleTypeDO);

    /**
     * 修改
     * @param vehicleTypeDO 车辆子类型
     * @return 是否操作成功
     */
    boolean update(VehicleTypeDO vehicleTypeDO);

    /**
     * 删除车辆子类型
     * @param ids 车辆子类型ID集合
     * @return 条数
     */
    int deleteByBatch(@Param("ids") Collection<String> ids);

    /**
     * 删除车辆子类型
     * @param id 辆子类型ID
     * @return boolean
     */
    boolean delete(String id);

    /**
     * 获取车辆绑定的车辆子类型ID
     * @param ids 车辆子类型ID集合
     * @return 有车辆绑定的车辆子类型ID集合
     */
    Set<String> getVehicleBindTypeList(@Param("ids") Collection<String> ids);

    /**
     * 分页查询
     * @param subTypeQuery 分页查询条件
     * @return 子类型列表
     */
    Page<VehicleSubTypeDTO> getByPage(VehicleSubTypeQuery subTypeQuery);

    /**
     * 根据车辆类型获取子类型
     * @param vehicleTypeId 车辆类型ID
     * @return 车辆子类型
     */
    List<VehicleSubTypeDTO> getByType(@Param("vehicleTypeId") String vehicleTypeId);

    /**
     * 校验车辆子类型是否绑定了车辆类型
     * @param id 车辆子类型id
     * @return boolean
     */
    boolean checkVehicleSubTypeIsBinding(String id);
}

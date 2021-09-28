package com.zw.platform.basic.repository;

import com.github.pagehelper.Page;
import com.zw.platform.basic.domain.VehicleTypeDO;
import com.zw.platform.basic.dto.VehicleTypeDTO;
import com.zw.platform.basic.dto.query.VehicleTypePageQuery;
import com.zw.platform.domain.basicinfo.VehicleType;
import com.zw.platform.domain.basicinfo.form.VehicleSubTypeForm;
import com.zw.platform.domain.basicinfo.query.VehicleTypeQuery;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * zw_m_vehicle_type -- 处理车辆的类型
 * @author zhangjuan
 */
public interface NewVehicleTypeDao {

    /**
     * 分页查询
     * @param query query
     * @return VehicleTypeDO
     */
    Page<VehicleTypeDO> findByPage(VehicleTypeQuery query);

    /**
     * 获取所有车辆类型id和车辆类型信息
     * @return List<Map < String, String>>
     */
    List<Map<String, String>> getTypeMaps();

    /**
     * 通过车辆id查询车辆类型
     * @param vehicleId 车辆id
     * @return VehicleTypeDO
     */
    VehicleTypeDO getByVehicleId(String vehicleId);

    /**
     * 查询车辆类型信息
     * @param vehicleType 车辆类型
     * @return VehicleTypeDO
     */
    VehicleTypeDO getByVehicleType(String vehicleType);

    /**
     * 查询车辆类型信息
     * @param id          id
     * @param vehicleType vehicleType
     * @return VehicleTypeDO
     */
    VehicleTypeDO getByIdAndVehicleType(@Param("id") String id, @Param("vehicleType") String vehicleType);

    /**
     * 根据车辆类型id查询类别信息
     * @param typeId 车辆类型id
     * @return
     */
    VehicleType getVehicleCategoryInfo(String typeId);

    /**
     * 获取某个类别下车辆类型对应的类型详情
     * @param category 类别
     * @param name     类型名称
     * @return 详情
     */
    VehicleTypeDTO getByName(@Param("category") String category, @Param("name") String name);

    /**
     * 添加
     * @param vehicleTypeDO 车辆类型
     * @return 是否操作成功
     */
    boolean insert(VehicleTypeDO vehicleTypeDO);

    /**
     * 修改
     * @param vehicleTypeDO 车辆类型
     * @return 是否操作成功
     */
    boolean update(VehicleTypeDO vehicleTypeDO);

    /**
     * 根据ID获取车型
     * @param id id
     * @return 车型详情
     */
    VehicleTypeDTO getById(@Param("id") String id);

    /**
     * 根据ID集合获取车型
     * @param ids 车型ID集合
     * @return 车型列表
     */
    List<VehicleTypeDTO> getByIds(@Param("ids") Collection<String> ids);

    /**
     * 删除车型
     * @param ids 车型ID集合
     * @return 条数
     */
    int deleteByBatch(@Param("ids") Collection<String> ids);

    /**
     * 删除车辆类型
     * @param id 车辆类型id
     * @return boolean
     */
    boolean delete(String id);

    /**
     * 根据关键字查询车型列表
     * @param keyword keyword
     * @return 车型列表
     */
    List<VehicleTypeDTO> getByKeyword(@Param("keyword") String keyword);

    /**
     * 分页查询
     * @param pageQuery 分页条件
     * @return 分页
     */
    Page<VehicleTypeDTO> getByPage(VehicleTypePageQuery pageQuery);

    /**
     * 根据车辆类别获取车辆类型列表
     * @param categoryId 车辆类别ID
     * @return 车辆类型列表
     */
    List<VehicleTypeDTO> getByCategoryId(@Param("categoryId") String categoryId);

    /**
     * 根据类别id集合查询类型
     * @param categoryIds 车辆类别ID
     * @return List<VehicleType>
     */
    List<VehicleTypeDO> findByCategoryIds(List<String> categoryIds);

    /**
     * 获取车辆绑定的车辆类型ID
     * @param ids 车型Id集合
     * @return 有车辆绑定的车型ID集合
     */
    Set<String> getVehicleBindTypeList(@Param("ids") Collection<String> ids);

    /**
     * 批量添加
     * @param vehicleTypes vehicleTypes
     * @return 添加数量
     */
    int addBatch(@Param("vehicleTypeList") List<VehicleTypeDO> vehicleTypes);

    /**
     * 新增车辆类型
     * @param vehicleTypeDO vehicleTypeDO
     * @return int
     */
    boolean add(VehicleTypeDO vehicleTypeDO);

    /**
     * 根据标准类型获取车辆类型
     * @param standard 车辆类别标准
     * @return 车辆类型列表
     */
    List<VehicleTypeDTO> getByStandard(@Param("standard") Integer standard);

    /**
     * 查询是否绑定了车辆
     * @param id 车辆类型id
     * @return boolean
     */
    boolean getIsBandVehicle(String id);

    /**
     * 根据车辆类别和车辆类型查找关联信息
     * @param vehicleType 车辆类型
     * @param category    车辆类别
     * @return VehicleTypeDO
     */
    VehicleTypeDO findByVehicleTypeAndCategory(@Param("vehicleType") String vehicleType,
        @Param("category") String category);

    /**
     * 类型是否绑定了子类型
     * @param id pid
     * @return boolean
     */
    boolean checkTypeIsBindingSubType(String id);

    /**
     * 查询车辆子类型
     * @param subTypeId  subTypeId
     * @return List<VehicleTypeDO>
     */
    List<VehicleSubTypeForm> findBySubType(String subTypeId);

    /**
     * 根据类别标准查询
     * @param standard standard
     * @return list
     */
    List<VehicleTypeDO> findByStandard(Integer standard);
}

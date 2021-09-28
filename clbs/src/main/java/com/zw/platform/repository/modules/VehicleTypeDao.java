package com.zw.platform.repository.modules;

import com.github.pagehelper.Page;
import com.zw.platform.domain.basicinfo.VehicleType;
import com.zw.platform.domain.basicinfo.form.VehicleSubTypeForm;
import com.zw.platform.domain.basicinfo.form.VehicleTypeForm;
import com.zw.platform.domain.basicinfo.query.VehicleTypeQuery;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 *
 */
@Deprecated
public interface VehicleTypeDao {

    Page<VehicleType> find(VehicleTypeQuery query);

    boolean add(VehicleTypeForm form);

    boolean delete(String id);

    VehicleType get(final String id);

    /**
     * 通过id查询车辆类型
     * @param ids id
     * @return List<VehicleType>
     */
    List<VehicleType> getByIds(@Param("ids") Collection<String> ids);

    /**
     * 通过车辆id查询车辆类型
     * @param vehicleId 车辆id
     * @return VehicleType
     */
    VehicleType getByVehicleId(String vehicleId);

    int update(final VehicleTypeForm form);

    boolean addVehicleTypeByBatch(List<VehicleTypeForm> importList);

    VehicleType findByVehicleType(String vehicleType);

    VehicleType isExist(@Param("id") String id, @Param("vehicleType") String vehicleType);

    VehicleType findByVehicleTypeTwo(@Param("vehicleType") String vehicleType);

    List<String> findByVehicleGenerateTemplate();

    String findByVehicleTypeId(final String vehicleType);

    String findByVehicleTypet(final String ctype);

    List<VehicleType> findVehicleType(String id);

    boolean getIsBand(String id);

    /**
     * 根据车辆类别和车辆类型查找关联信息
     *
     * @param category    车辆类别
     * @param vehicleType 车辆类型
     * @return
     */
    VehicleType findVehicleTypeId(@Param("category") String category, @Param("vehicleType") String vehicleType);

    /**
     * 查找所有车辆类型关联信息(参数为null表示查所有)
     *
     * @return
     */
    List<VehicleType> findALLVehicleType(@Param("typeId") String typeId);

    /**
     * 判断车辆类别是否存在(返回车辆类别id)
     *
     * @param category
     * @return
     */
    String findCategory(String category);

    /**
     * 类型是否绑定了子类型
     *
     * @param id pid
     * @return boolean
     */
    boolean checkTypeIsBindingSubType(String id);

    /**
     * 查询类型下的所有子类型
     *
     * @param id pid
     * @return list
     */
    List<VehicleSubTypeForm> findTypeIsBindingSubType(String id);

    /**
     * 查询类别标准是2(工程机械)的所有类型
     *
     * @return list
     * @param standardInt
     */
    List<VehicleType> findVehicleTypes(Integer standardInt);

    /**
     * 根据类别id集合查询类型
     *
     * @return list
     */
    List<VehicleType> findVehicleTypesByCategoryIds(List<String> categoryIds);

    /**
     * 查询车辆类别为危险品运输车的车型
     *
     * @return
     */
    List<String> selectBycategory(@Param("id") String id);

    /**
     * 通过id 获取车辆类型
     *
     * @param vehicleTypes
     * @return
     */
    List<Map<String, String>> queryVehicleTypeByIds(@Param("ids") List<String> vehicleTypes);

    VehicleType getVehicleTypeAndCateGory(@Param("vehicleType") String vehicleType);

    List<Map<String, String>> getTypeMaps();

    /**
     * 根据车辆类型id查询类别信息
     */
    VehicleType getVehicleCategoryInfo(String typeId);

    List<Map<String, String>> getVehTypes();

    /**
     * 通过车辆类型获取车辆类型id
     * @param vehicleType 车辆类型
     * @return id
     */
    List<String> getVehicleTypeId(@Param("vehicleType") String vehicleType);

    Integer getServiceCycle(String typeId);

}

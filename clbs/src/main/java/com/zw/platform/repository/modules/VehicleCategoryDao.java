package com.zw.platform.repository.modules;

import com.github.pagehelper.Page;
import com.zw.platform.domain.basicinfo.VehicleType;
import com.zw.platform.domain.basicinfo.form.VehicleTypeForm;
import com.zw.platform.domain.basicinfo.query.VehicleTypeQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Deprecated
public interface VehicleCategoryDao {
    Page<VehicleType> find(VehicleTypeQuery query);//查询车辆类别

    boolean add(VehicleTypeForm form);//新增车辆类别

    boolean delete(String id);//删除车辆类别

    VehicleType get(final String id);//获取单个车辆类别信息

    boolean update(final VehicleTypeForm form);//修改车辆类别

    VehicleType findByVehicleType(String vehicleCategory);//通过车辆类别，查询类别信息 保证唯一性

    List<String> findCategory();//查询所有车辆类别信息

    List<String> findVtypeById(String id);//通过车辆类别ID 查询车辆类别下的所以车辆类型

    List<VehicleType> findCategoryByico(String id);//通过图标ID查询该图标绑定的车辆类别

    boolean updateVtype(String id); //通过车辆类型ID修改车辆类别

    /**
     * 重复验证
     * @param id id
     * @return VehicleType
     */
    VehicleType getByStandard(String id);

    /**
     * 通过车辆类别ID 查询车辆类别下的所以车辆类型
     * @param id 类别id
     * @return this
     */
    List<VehicleType> findVehicleTypeByCategoryId(String id);

    /**
     * 根据子类型查询类别中的standard
     * @param id 子类型id
     * @return
     */
    VehicleType getStandardBySubTypeId(String id);

    /**
     * 根据子类型查询类别中的standard
     * @param id 类型id
     * @return
     */
    VehicleType getStandardByVehicleTypeId(String id);

    /**
     * 查询车辆类别列表
     * @return list
     * @author zhouzongbo
     */
    List<VehicleType> findVehicleCategoryList();

    /**
     * 根据类别标准查询类别
     * @return list
     * @author zhouzongbo
     */
    List<VehicleType> findVehicleCategorysByStandard(Integer standard);

    String getBusId();

    /**
     * 通过车辆类别id查询该车的标准是否为货运
     * @param id
     * @return
     */
    int countStandardById(@Param("id") String id);
}

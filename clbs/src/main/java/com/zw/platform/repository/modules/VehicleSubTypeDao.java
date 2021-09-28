package com.zw.platform.repository.modules;

import com.github.pagehelper.Page;
import com.zw.platform.domain.basicinfo.VehicleSubTypeInfo;
import com.zw.platform.domain.basicinfo.form.VehicleSubTypeForm;
import com.zw.platform.domain.basicinfo.query.VehicleSubTypeQuery;
import org.apache.ibatis.annotations.Param;

/**
 * 子类型dao
 * @author zhouzongbo on 2018/4/17 9:34
 */
@Deprecated
public interface VehicleSubTypeDao {
    /**
     * 子类型
     * @param form this
     * @return boolean
     */
    boolean addSubType(VehicleSubTypeForm form);

    /**
     * 根据子类型名查询子类型
     * @param vehicleType 类型名
     * @param vehicleSubType 子类型名
     * @return VehicleSubTypeInfo
     */
    VehicleSubTypeInfo getSubTypeBy(@Param("vehicleType") String vehicleType,
        @Param("vehicleSubType") String vehicleSubType);

    /**
     * 根据子类型id查询
     * @param id 子类型id
     * @return this
     */
    VehicleSubTypeInfo getVehicleSubTypeById(String id);

    /**
     * 分页查询子类型列表
     * @param query query
     * @return Page<VehicleSubTypeInfo>
     */
    Page<VehicleSubTypeInfo> findVehicleSubTypePage(VehicleSubTypeQuery query);

    /**
     * 修改子类型
     * @param form this
     * @return boolean
     */
    boolean updateSubType(VehicleSubTypeForm form);

    /**
     * 校验车辆子类型是否绑定了车辆类型
     * @param id 车辆子类型id
     * @return boolean
     */
    boolean checkVehicleSubTypeIsBinding(String id);

    /**
     * 删除车辆子类型
     * @param id 车辆id
     * @return boolean
     */
    boolean deleteSubType(String id);

    /**
     * 根据车辆子类型查询子类型
     * @param vehicleSubType 子类型名
     * @return VehicleSubTypeInfo
     */
    VehicleSubTypeInfo getSubTypeBySubName(String vehicleSubType);

    /**
     * 根据车辆id查询子类别中的图标
     * @param vehicleId vehicleId
     * @return this
     */
    VehicleSubTypeInfo getSubTypeByVehicleId(String vehicleId);
}

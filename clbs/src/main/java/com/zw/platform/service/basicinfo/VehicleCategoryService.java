package com.zw.platform.service.basicinfo;

import com.github.pagehelper.Page;
import com.zw.platform.basic.domain.VehicleCategoryDO;
import com.zw.platform.basic.domain.VehicleTypeDO;
import com.zw.platform.basic.dto.VehicleCategoryDTO;
import com.zw.platform.domain.basicinfo.VehicleType;
import com.zw.platform.domain.basicinfo.form.VehicleTypeForm;
import com.zw.platform.domain.basicinfo.query.VehicleTypeQuery;

import java.util.List;


@Deprecated
public interface VehicleCategoryService {
    /**
     * 新增
     */
    boolean add(VehicleTypeForm form, String ipAddress) throws Exception;

    /**
     * 分页查询 User
     */
    Page<VehicleType> findByPage(VehicleTypeQuery query) throws Exception;

    /**
     * 根据id删除一个 VehicleType
     */
    boolean delete(final String id, String ipAddress) throws Exception;

    /**
     * 批量删除车辆类别
     * @param ids
     * @param ipAddress
     * @return
     * @throws Exception
     */
    String deleteBatch(String ids, String ipAddress) throws Exception;

    /**
     * 通过id得到一个 VehicleType
     */
    VehicleType get(final String id) throws Exception;

    /**
     * 修改 User
     */
    boolean update(final VehicleTypeForm form, String ipAddress) throws Exception;

    /**
     * 通过车辆类别查询类别信息
     * @param vehicleCategory
     * @return
     * @throws Exception
     */
    VehicleCategoryDO findByVehicleType(String vehicleCategory) throws Exception;

    VehicleCategoryDTO getByStandard(String id) throws Exception;

    /**
     * 通过车辆类别ID 查询车辆类别下的所以车辆类型
     * @param id 类别id
     * @return this
     */
    List<VehicleTypeDO> findVehicleTypeByCategoryId(String id);

    /**
     * 查询绑定了车辆类型的类别
     * @author zhouzongbo
     * @return list
     */
    List<VehicleCategoryDO> findVehicleCategoryList();
}

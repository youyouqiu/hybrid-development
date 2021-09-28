package com.zw.platform.service.basicinfo;

import com.github.pagehelper.Page;
import com.zw.platform.basic.dto.VehicleSubTypeDTO;
import com.zw.platform.domain.basicinfo.VehicleSubTypeInfo;
import com.zw.platform.domain.basicinfo.form.VehicleSubTypeForm;
import com.zw.platform.domain.basicinfo.query.VehicleSubTypeQuery;
import com.zw.platform.util.common.JsonResultBean;

import javax.servlet.http.HttpServletResponse;

/**
 * 子类型(service)
 * @author zhouzongbo on 2018/4/17 9:32
 */
@Deprecated
public interface VehicleSubTypeService {

    /**
     * 新增子类型
     * @param form this
     * @param ip this
     * @return boolean
     * @throws Exception ex
     */
    boolean addSubType(VehicleSubTypeForm form, String ip) throws Exception;

    /**
     * 根据子类型名查询子类型
     * @param vehicleType 类型名
     * @param vehicleSubType 子类型名
     * @return VehicleSubTypeInfo
     */
    VehicleSubTypeDTO getSubTypeBy(String vehicleType, String vehicleSubType);

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
    Page<VehicleSubTypeDTO> findVehicleSubTypePage(VehicleSubTypeQuery query);

    /**
     * 修改子类型
     * @param form this
     * @param ip 请求ip
     * @return boolean
     */
    boolean updateSubType(VehicleSubTypeForm form, String ip) throws Exception;

    /**
     * 校验车辆子类型是否绑定了车辆类型
     * @param id 车辆子类型id
     * @return boolean
     */
    boolean checkVehicleSubTypeIsBinding(String id);

    /**
     * 删除子类型
     * @param id 子类型id
     * @param ip this ip
     * @param mark 0:单个删除;1:批量删除
     * @return JsonResultBean
     * @throws Exception this
     */
    JsonResultBean deleteSubType(String id, String ip, int mark) throws Exception;

    /**
     * 导出子类型
     * @param title this title
     * @param type this type
     * @param response this response
     */
    void exportSubType(String title, int type, HttpServletResponse response);

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

package com.zw.platform.service.basicinfo;

import com.github.pagehelper.Page;
import com.zw.platform.basic.domain.VehicleTypeDO;
import com.zw.platform.basic.dto.VehicleTypeDTO;
import com.zw.platform.domain.basicinfo.form.VehicleSubTypeForm;
import com.zw.platform.domain.basicinfo.form.VehicleTypeForm;
import com.zw.platform.domain.basicinfo.query.VehicleTypeQuery;
import com.zw.platform.util.common.JsonResultBean;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;


/**
 * Modification by Wjy on 2016/8/31.
 */
@Deprecated
public interface VehicleTypeService {
    /**
     * 新增
     */
    void add(VehicleTypeForm form, String ipAddress) throws Exception;

    /**
     * 分页查询 User
     * @return
     */
    Page<VehicleTypeDO> findByPage(VehicleTypeQuery query) throws Exception;

    /**
     * 根据id删除一个 VehicleType
     */
    JsonResultBean delete(final String id, String ipAddress) throws Exception;

    /**
     * 通过id得到一个 VehicleType
     * @return
     */
    VehicleTypeDTO get(final String id) throws Exception;

    /**
     * 修改 User
     */
    JsonResultBean update(final VehicleTypeForm form, String ipAddress) throws Exception;

    /**
     * 导出
     */
    boolean exportVehicleType(String title, int type, HttpServletResponse response) throws Exception;

    /**
     * 导入
     */
    Map importVehicleType(MultipartFile file, String ipAddress) throws Exception;

    /**
     * 生成导入模板
     * @param response
     * @return
     */
    boolean generateTemplate(HttpServletResponse response) throws Exception;

    VehicleTypeDO findByVehicleType(final String vehicleType) throws Exception;

    VehicleTypeDO findByVehicleType(String id, String vehicleType) throws Exception;

    /**
     * 根据车辆类别和车辆类型查找关联信息
     * @param category
     *            车辆类别
     * @param vehicleType
     *            车辆类型
     * @return
     */
    VehicleTypeDO findVehicleTypeId(String category, String vehicleType) throws Exception;

    String findByVehicleTypet(final String id) throws Exception;

    List<VehicleTypeDO> findVehicleType(String vehicleCategory) throws Exception;// 通过车辆类别ID查询车辆类型

    /**
     * 车型是否已被绑定
     * @param id
     * @return boolean
     * @author:Fan Lu
     */
    boolean getIsBand(String id) throws Exception;

    /**
     * 类型是否绑定了子类型
     * @param id pid
     * @return boolean
     */
    boolean checkTypeIsBindingSubType(String id);

    /**
     * 查询车辆类型下的车辆子类型
     * @param id 类型id
     * @return list
     */
    List<VehicleSubTypeForm> findTypeIsBindingSubType(String id);

    /**
     * 查询类别标准是2(工程机械)的所有类型
     * @return list
     * @param standardInt
     */
    List<VehicleTypeDO> findVehicleTypes(Integer standardInt);

}

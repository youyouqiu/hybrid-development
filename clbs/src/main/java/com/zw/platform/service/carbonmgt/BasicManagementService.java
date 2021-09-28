package com.zw.platform.service.carbonmgt;

import com.github.pagehelper.Page;
import com.zw.platform.domain.vas.carbonmgt.FuelType;
import com.zw.platform.domain.vas.carbonmgt.TimingStored;
import com.zw.platform.domain.vas.carbonmgt.form.BasicManagementForm;
import com.zw.platform.domain.vas.carbonmgt.form.FuelTypeForm;
import com.zw.platform.domain.vas.carbonmgt.query.BasicManagementQuery;
import com.zw.platform.domain.vas.carbonmgt.query.FuelTypeQuery;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.JsonResultBean;

import java.util.List;


/**
 * Created by 王健宇 on 2017/2/17.
 */

public interface BasicManagementService {
    List<TimingStored> oilPricesQuery(String timeStart, String timeEnd, String district, String oiltype)
        throws Exception;

    /**
     * 移动源基础信息列表查询
     * @Title: find
     * @param query
     * @return
     * @throws BusinessException
     * @return List<BasicManagementForm>
     * @throws @author
     *             Liubangquan
     */
    Page<BasicManagementForm> find(BasicManagementQuery query) throws Exception;

    /**
     * 添加移动源基础信息
     * @Title: addMobileSourceBaseInfo
     * @param form
     * @throws BusinessException
     * @return boolean
     * @throws @author
     *             Liubangquan
     */
    public boolean addMobileSourceBaseInfo(BasicManagementForm form) throws Exception;

    /**
     * 根据车辆id查询移动源基础信息表详情
     * @Title: getMobileSourceBaseinfoByVid
     * @param vehicleId
     * @return
     * @throws BusinessException
     * @return BasicManagementForm
     * @throws @author
     *             Liubangquan
     */
    public BasicManagementForm getMobileSourceBaseinfoByVid(String vehicleId) throws Exception;

    /**
     * 根据车辆id修改移动源基础信息
     * @Title: editMobileSourceBaseinfo
     * @param form
     * @return
     * @throws BusinessException
     * @return boolean
     * @throws @author
     *             Liubangquan
     */
    public boolean editMobileSourceBaseinfo(BasicManagementForm form) throws Exception;

    /**
     * 根据车牌颜色编号获取车牌颜色名称
     * @Title: getPlateColorByPlateColorId
     * @param plateColor
     * @return
     * @throws BusinessException
     * @return String
     * @throws @author
     *             Liubangquan
     */
    public String getPlateColorByPlateColorId(String plateColor) throws Exception;

    /**
     * 删除移动源基础信息
     * @Title: deleteMobileSourceBaseinfo
     * @param vehicleId
     * @return
     * @throws BusinessException
     * @return boolean
     * @throws @author
     *             Liubangquan
     */
    public boolean deleteMobileSourceBaseinfo(String vehicleId) throws Exception;

    /**
     * 分页查询燃料类型列表
     * @param query
     * @return
     */
    Page<FuelTypeQuery> findFuelTypeByPage(FuelTypeQuery query) throws Exception;

    /**
     * 新增燃料类型
     * @param fuelType
     * @return
     */
    boolean addFuelType(FuelTypeForm fuelType, String ipAddress) throws Exception;

    /**
     * 根据燃料类型查询燃料类型实体
     * @param fuelType
     * @return
     */
    List<FuelType> findFuelType(String fuelType) throws Exception;

    FuelType get(String id);

    /**
     * 修改燃料类型
     * @param form
     * @return
     */
    JsonResultBean updateFuelType(FuelTypeForm form, String ipAddress) throws Exception;

    /**
     * 根据id删除燃料类型
     * @param id
     * @return
     */
    boolean deleteFuelType(String id, String ipAddress) throws Exception;

    /**
     * 批量删除燃料类型
     * @param ids
     * @return
     */
    boolean deleteFuelTypeMuch(List<String> ids, String ipAddress) throws Exception;
}

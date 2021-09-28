package com.zw.platform.repository.vas;

import com.github.pagehelper.Page;
import com.zw.platform.domain.vas.carbonmgt.FuelType;
import com.zw.platform.domain.vas.carbonmgt.TimingStored;
import com.zw.platform.domain.vas.carbonmgt.form.BasicManagementForm;
import com.zw.platform.domain.vas.carbonmgt.form.FuelTypeForm;
import com.zw.platform.domain.vas.carbonmgt.query.BasicManagementQuery;
import com.zw.platform.domain.vas.carbonmgt.query.FuelTypeQuery;
import com.zw.platform.util.common.BusinessException;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 移动源基础信息Dao
 * Created by 王健宇 on 2017/2/17.
 */
public interface BasicManagementDao {
    List<TimingStored> oilPricesQuery(@Param("timeStart") String timeStart, @Param("timeEnd") String timeEnd,
        @Param("district") String district, @Param("oiltype") String oiltype);

    /**
     * 查询移动源基础信息列表：绑定了终端、sim卡的非工程车辆
     * @param query     查询条件
     * @param userId    用户id
     * @param groupList 组织列表
     * @return List<BasicManagementForm>
     * @throws
     * @Title: find
     * @author Liubangquan
     */
    public List<BasicManagementForm> find(@Param("param") BasicManagementQuery query, @Param("userId") String userId,
        @Param("groupList") List<String> groupList);

    /**
     * 设置移动源基础信息
     * @param form
     * @return boolean
     * @throws BusinessException
     * @throws
     * @Title: addMobileSourceBaseInfo
     * @author Liubangquan
     */
    public boolean addMobileSourceBaseInfo(BasicManagementForm form) throws BusinessException;

    /**
     * 根据车辆id获取移动源基础信息表详情
     * @param vehicleId
     * @return BasicManagementForm
     * @throws BusinessException
     * @throws
     * @Title: getBaseInfoById
     * @author Liubangquan
     */
    public BasicManagementForm getBaseInfoByVehicleId(String vehicleId) throws BusinessException;

    /**
     * 修改移动源基础信息
     * @param form
     * @return boolean
     * @throws BusinessException
     * @throws
     * @Title: editMobileSourceBaseinfo
     * @author Liubangquan
     */
    public boolean editMobileSourceBaseinfo(BasicManagementForm form) throws BusinessException;

    /**
     * 删除移动源基础信息
     * @param vehicleId
     * @return boolean
     * @throws BusinessException
     * @throws
     * @Title: deleteMobileSourceBaseinfo
     * @author Liubangquan
     */
    public boolean deleteMobileSourceBaseinfo(String vehicleId) throws BusinessException;

    /**
     * 查询燃料类型信息
     * @param query
     * @return
     * @author tangshunyu
     */
    Page<FuelTypeQuery> findFuelTypeList(FuelTypeQuery query);

    boolean addFuelType(FuelTypeForm fuelType);

    List<FuelType> findFuelType(String fuelType);

    /**
     * 根据ID查询燃料类型
     * @param id
     * @return
     * @author tangshunyu
     */
    FuelType get(String id);

    /**
     * 修改燃料类型
     * @param form
     * @return
     */
    boolean updateFuelType(FuelTypeForm form);

    /**
     * 根据Id删除燃料类型
     * @param id
     * @return
     */
    boolean deleteFuel(String id);

    boolean deleteFuelTypeMuch(List<String> ids);

    /**
     * 根据燃料名称查询燃料id
     * @param fuelType
     * @return
     */
    String findFuelTypeIdByName(String fuelType);
}

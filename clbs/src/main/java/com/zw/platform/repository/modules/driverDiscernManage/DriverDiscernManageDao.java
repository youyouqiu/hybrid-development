package com.zw.platform.repository.modules.driverDiscernManage;

import com.zw.platform.domain.basicinfo.driverDiscernManage.DeviceDriverInfo;
import com.zw.platform.domain.basicinfo.driverDiscernManage.DriverDiscernManageInfo;
import com.zw.platform.domain.basicinfo.query.DriverDiscernManageQuery;
import com.zw.platform.dto.driverMiscern.VehicleDeviceDriverDo;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * @Description: 驾驶员身份识别DAO
 * @Author Tianzhangxu
 * @Date 2020/9/27 10:55
 */
public interface DriverDiscernManageDao {



    /**
     * 根据车辆ID集合查询驾驶员身份识别管理信息
     */
    List<DriverDiscernManageInfo> findDriverDiscernManageInfo(DriverDiscernManageQuery query);

    /**
     * 根据车辆ID查询对应驾驶员信息
     * @param vehicleId vehicleId
     * @return List<DeviceDriverInfo>
     */
    List<DeviceDriverInfo> findDriverDetail(String vehicleId);

    /**
     * 新增驾驶员身份识别管理信息
     * @param info info
     * @return boolean
     */
    boolean insert(DriverDiscernManageInfo info);

    /**
     *  根据车辆ID查询驾驶员识别管理信息
     * @param vehicleId vehicleId
     * @return DriverDiscernManageInfo
     */
    DriverDiscernManageInfo findByVid(String vehicleId);

    /**
     * 更新驾驶员身份识别管理信息
     * @param info info
     * @return boolean
     */
    boolean update(DriverDiscernManageInfo info);

    /**
     * 根据车辆ID删除其与终端驾驶员关联关系
     * @param vehicleId vehicleId
     * @return boolean
     */
    boolean deleteDriverAndPro(String vehicleId);

    /**
     * 添加车辆与终端驾驶员关联关系
     * @param vehicleDeviceDriverDos vehicleDeviceDriverDos
     * @return boolean
     */
    boolean insertDriverAndPro(
        @Param("vehicleDeviceDriverDos") Collection<VehicleDeviceDriverDo> vehicleDeviceDriverDos);

}

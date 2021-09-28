package com.zw.platform.repository.vas;

import com.zw.platform.domain.systems.DeviceUpgrade;
import com.zw.platform.domain.taskjob.TaskJobForm;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/***
 @Author zhengjc
 @Date 2019/9/30 17:04
 @Description 下发升级包dao
 @version 1.0
 **/
public interface DeviceUpgradeDao {

    void addDeviceUpgradeByBatch(@Param("list") List<DeviceUpgrade> list);

    boolean deleteDeviceUpgradeById(@Param("list") List<String> ids);

    void addDeviceUpgradeFile(DeviceUpgrade deviceUpgrade);

    void updateDeviceUpgradeFile(DeviceUpgrade deviceUpgrade);

    void deleteDeviceUpgradeFile(@Param("upgradeFileId") String upgradeFileId);

    List<DeviceUpgrade> queryList();

    DeviceUpgrade queryDeviceUpgradeById(@Param("upgradeFileId") String upgradeFileId);

    void delDeviceUpgradeByIds(@Param("list") List<String> paramIdList);

    List<DeviceUpgrade> getBeforeVehicleUpgrade(@Param("vehicleId") String vehicleId,
        @Param("upgradeType") String upgradeType);

    void updateBeforeVehicleUpgrade(@Param("id") String id);

    TaskJobForm getTaskJobById(@Param("id") String id);

    List<TaskJobForm> getTaskJobs();

    void updateUpgradeStrategy(@Param("id") String id);
}

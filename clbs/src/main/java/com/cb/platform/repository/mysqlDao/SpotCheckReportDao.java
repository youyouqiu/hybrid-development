package com.cb.platform.repository.mysqlDao;

import com.cb.platform.domain.VehicleSpotCheckInfo;
import com.zw.platform.domain.vas.alram.AlarmSetting;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author penghj
 * @version 1.0
 * @date 2019/2/21 10:53
 */
public interface SpotCheckReportDao {

    /**
     * 查询车辆抽查明细信息
     * @param vehicleIdList
     * @param startTime
     * @param endTime
     * @return
     */
    List<VehicleSpotCheckInfo> getVehicleSpotCheckDetailList(@Param("vehicleIdList") List<String> vehicleIdList,
        @Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 查找车辆设置的限速
     * @param vid
     * @return
     */
    AlarmSetting findSpeedLimitByVid(String vid);

    /**
     * 查询抽查明细 通过用户id
     * @param userNameList
     * @param startTime
     * @param endTime
     * @return
     */
    List<VehicleSpotCheckInfo> getSpotCheckDetailListByUserIds(@Param("userNameList") List<String> userNameList,
        @Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 新增车辆抽查数据
     * @param vehicleSpotCheckInfo vehicleSpotCheckInfo
     */
    void addVehicleSpotCheckInfo(VehicleSpotCheckInfo vehicleSpotCheckInfo);

    /**
     * 批量新增车辆抽查数据
     * @param vehicleSpotCheckInfos
     */
    void addVehicleSpotCheckInfoByBatch(List<VehicleSpotCheckInfo> vehicleSpotCheckInfos);
}

package com.zw.platform.repository.vas;

import com.github.pagehelper.Page;
import com.zw.platform.domain.vas.mileageSensor.MileageSensorConfig;
import com.zw.platform.domain.vas.mileageSensor.MileageSensorConfigQuery;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * Title:里程传感器配置Dao
 * <p>
 * Copyright: Copyright (c) 2016
 * <p>
 * Company: ZhongWei
 * <p>
 * team: ZhongWeiTeam
 * @version 1.0
 * @author: nixiangqian
 * @date 2017年05月16日 11:21
 */
public interface MileageSensorConfigDao {

    /**
     * 获取参考设置车辆
     * @return
     */
    List<MileageSensorConfig> findVehicleSensorSet(@Param("userId") String userId,
        @Param("groupList") List<String> groupList, @Param("reportDeviceTypes") List<String> reportDeviceTypes);

    /**
     * 新增MileageSensorConfig
     * @param mileageSensorConfig
     */
    boolean addMileageSensorConfig(MileageSensorConfig mileageSensorConfig);

    /**
     * 修改 MileageSensorConfig
     * @param mileageSensorConfig
     */
    boolean updateMileageSensorConfig(MileageSensorConfig mileageSensorConfig);

    /**
     * 批量删除MileageSensorConfig
     * @param vehicleIds
     */
    boolean deleteBatchMileageSensorConfig(@Param("vehicleIds") List<String> vehicleIds);

    /**
     * 根据vehicleId查询MMileageSensorConfig
     * @param vehicleId
     * @return
     */
    MileageSensorConfig findByVehicleId(@Param("vehicleId") String vehicleId);

    List<MileageSensorConfig> findByVehicleIdBatch(@Param("ids") Collection<String> ids);



    /**
     * 根据查询条件查询信息
     * @param query
     * @return
     */
    Page<MileageSensorConfig> findByQuery(@Param("param") MileageSensorConfigQuery query,
        @Param("userId") String userId, @Param("groupList") List<String> groupList);

    List<MileageSensorConfig> findByQueryRedis(List<String> necessaryList);

    List<MileageSensorConfig> findVehicleSensorSetByProtocols(@Param("userId") String userId,
        @Param("groupList") List<String> groupList, @Param("protocols") List<Integer> protocols);
}

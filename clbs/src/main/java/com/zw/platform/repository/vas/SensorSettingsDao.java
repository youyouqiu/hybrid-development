package com.zw.platform.repository.vas;

import com.github.pagehelper.Page;
import com.zw.platform.domain.vas.f3.TransdusermonitorSet;
import com.zw.platform.domain.vas.mileageSensor.MileageSensorConfigQuery;
import com.zw.platform.domain.vas.workhourmgt.SensorSettingInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface SensorSettingsDao {
    /**
     * 根据传感器类别查询监测设置
     */
    Page<TransdusermonitorSet> findByQuery(@Param("param") MileageSensorConfigQuery query,
        @Param("userId") String userId, @Param("sensorType") int sensorType,
        @Param("groupList") List<String> groupList);

    Long findByQuery_COUNT(@Param("param") MileageSensorConfigQuery query, @Param("userId") String userId,
        @Param("sensorType") int sensorType, @Param("groupList") List<String> groupList);

    /**
     * 增加监测设置
     */
    boolean addTransdusermonitorSet(TransdusermonitorSet transdusermonitorSet);

    /**
     * 根据id查询监测设置
     */
    TransdusermonitorSet findTransdusermonitorSetById(String id);

    /**
     * 根据ID修改 传感器设置
     */
    boolean updateSensorVehicle(TransdusermonitorSet transdusermonitorSet);

    /**
     * 根据id删除传感器设置，解绑关系
     */
    boolean deleteSensorVehicle(String id);

    /**
     * 根据车辆id删除对应传感器类型的所有绑定关系 sensorType=-1 则为删除所有与该车辆绑定的传感器
     */
    void deleteAllBind(@Param("vid") String vid, @Param("sensorType") int sensorType);

    /**
     * 根据传感器类别查询已绑定传感器车辆的车牌号
     */
    List<TransdusermonitorSet> findVehicleByType(@Param("sensorType") int transduserType,
        @Param("userId") String userId, @Param("groupList") List<String> groupList,
        @Param("protocols") List<Integer> protocols);

    /**
     * 参考车牌
     */
    List<TransdusermonitorSet> consultVehicle(@Param("sensorType") int transduserType, @Param("userId") String userId,
        @Param("groupList") List<String> groupList, @Param("protocols") List<Integer> protocols);

    /**
     * 根据车辆id与传感器型号查询该车辆绑定的传感器类型和信息
     */
    List<TransdusermonitorSet> findByVehicleId(@Param("sensorType") int sensorType,
        @Param("vehicleId") String vehicleId);

    /**
     * @param sensorType
     * @return
     * @Description:查询车辆绑定的传感器
     * @author wanxing
     */
    List<Map<String, String>> findBindingMonitor(String sensorType);

    List<Map<String, String>> findBindingMonitorByType(String sensorType);

    Page<TransdusermonitorSet> findByQueryRedis(@Param("list") List<String> vehicles,
        @Param("sensorList") List<String> sensors, @Param("sensorType") String sensorType);

    /**
     * 通过传感器的id或者型号查询绑定车的id的集合
     * @param map
     * @return
     * @Description:
     * @author wanxing
     */
    List<TransdusermonitorSet> findVehicleIdBySensorIdOrsensorType(Map<String, Object> map);

    /**
     * 根据传感器类型查询传感器
     * @param detectionMode
     * @param sensorType
     * @return list
     */
    List<SensorSettingInfo> findSensorInfoBySensorType(@Param("detectionMode") String detectionMode,
        @Param("sensorType") String sensorType);

    void deleteBatchBindByMonitorIds(@Param("monitorIds") List<String> monitorIds);

    /**
     * 根据传感器类型查询监控对象绑定传感器数量
     * @param monitorId
     * @param sensorType
     * @return
     */
    List<SensorSettingInfo> getMonitorBandSensorInfoBySensorType(@Param("monitorId") String monitorId,
        @Param("sensorType") Integer sensorType);

    /**
     * 根据传感器类型查询监控对象集合绑定传感器数量
     * @param monitorIdList
     * @param sensorType
     * @return
     */
    List<SensorSettingInfo> getMonitorListBandSensorInfoBySensorType(@Param("monitorIdList") List<String> monitorIdList,
        @Param("sensorType") Integer sensorType);

    /**
     * 查询监控对象绑定的油耗传感器数量
     * @param monitorId
     * @return
     */
    List<Integer> getMonitorBandOilExpendSensorNo(@Param("monitorId") String monitorId);

    List<TransdusermonitorSet> findVehicleReference(@Param("sensorType") int sensorType, @Param("userId") String userId,
        @Param("groupList") List<String> groupList, @Param("reportDeviceTypes") List<String> protocols);
}

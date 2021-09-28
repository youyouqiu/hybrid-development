package com.zw.platform.repository.vas;

import com.zw.platform.domain.vas.f3.SensorPolling;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @version 1.0
 * @author: nixiangqian
 * @date 2017年05月09日 14:22
 */
public interface SensorPollingDao {

    /**
     * 根据监控对象获取轮询设置
     * @param vehicleId 监控对象id
     * @return List<SensorPolling>
     */
    List<SensorPolling> findByVehicleId(@Param("vehicleId") String vehicleId);

    /**
     * 根据监控对象批量获取轮询设置
     * @param vehicleIds 监控对象id
     * @return List<SensorPolling>
     */
    List<SensorPolling> findByVehicleIds(@Param("vehicleIds") Collection<String> vehicleIds);

    /**
     * 根据配置获取轮询设置
     * @param configid
     * @return
     */
    List<SensorPolling> findByConfigid(@Param("configid") String configid);

    /**
     * 根据主键ID查询外设信息
     * @param id
     * @return
     */
    SensorPolling findByid(@Param("id") String id);

    /**
     * 新增SensorPolling
     * @param sensorPolling
     */
    void addSensorPolling(final SensorPolling sensorPolling);

    /**
     * 批量新增
     * @param sensorPollings
     * @return
     */
    boolean addByBatch(final List<SensorPolling> sensorPollings);

    /**
     * 删除
     * @param id
     */
    void delete(@Param("id") String id);

    /**
     * 批量删除
     * @param ids ids
     */
    void batchDeleteByIds(@Param("ids") Collection<String> ids);

    /**
     * 根据配置编号删除轮询信息
     * @param configId
     */
    void deleteByConfigId(@Param("configId") String configId);

    List<String> findConfigId();

    List<Map<String, String>> findAllSensorPoll();

    /**
     * 判断空调状态是否下发，0为下发，
     * @param vehicleId
     * @return
     */
    String findStatus(@Param("vehicleId") String vehicleId);

    /**
     * 根据传感器id查询设置了相应轮询传感器的监控对象
     * @param sensorType
     * @return
     */
    List<String> getSensorPollMonitorBySensorType(String sensorType);

    /**
     * 根据车id和传感器id查询设置了相应轮询传感器的监控对象
     */
    List<String> getBindSensorMonitorBySensorId(String sensorId);

    /**
     * 根据车id查询设置了相应轮询传感器的监控对象
     */
    List<String> getAllBindSensorMonitor();

    void deleteBatchByConfigId(@Param("sensorConfigIdList") List<String> sensorConfigIdList);

    /**
     * 根据监控对象id获取外设传感器id列表
     */
    List<String> getSensorPollingListByMonitorId(String monitorId);

    /**
     * 获得权限下轮询了对应传感器的监控对象id
     * @param sensorIdList
     * @param ownMonitorIdList
     * @return
     */
    Set<String> getPollMonitorIdListBySensorIdAndOwnMonitor(@Param("sensorIdList") List<String> sensorIdList,
        @Param("ownMonitorIdList") Set<String> ownMonitorIdList);
}

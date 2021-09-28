package com.zw.platform.repository.vas;

import com.zw.platform.domain.param.RemoteUpgradeSensorBasicInfo;
import com.zw.platform.domain.vas.sensorUpgrade.SensorBind;
import com.zw.platform.domain.vas.sensorUpgrade.SensorType;
import com.zw.platform.domain.vas.sensorUpgrade.SensorUpgrade;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 传感器升级dao
 */
public interface SensorUpgradeDao {
    /**
     * 获取传感器类型列表
     */
    List<SensorType> getAllSensorList();

    /**
     * 根据监控对象id查询监控对象外设升级状态和日期
     */
    List<SensorUpgrade> getMonitorSensorUpgradeStatus(SensorBind sensorBind);

    /**
     * 获取油位传感器基础信息
     * @param monitorId
     * @param sequenceNumber
     * @return
     */
    RemoteUpgradeSensorBasicInfo getOilSensorBasicInfo(@Param("monitorId") String monitorId,
        @Param("sequenceNumber") Integer sequenceNumber);

    /**
     * 获取流量传感器基础信息（油耗）
     * @param monitorId
     * @param sequenceNumber
     * @return
     */
    RemoteUpgradeSensorBasicInfo getFluxSensorBasicInfo(@Param("monitorId") String monitorId,
        @Param("sequenceNumber") String sequenceNumber);

    /**
     * 获取通用传感器基础信息（温度、湿度、正反转、液位）
     * @param monitorId
     * @param sequenceNumber
     * @return
     */
    RemoteUpgradeSensorBasicInfo getGeneralSensorBasicInfo(@Param("monitorId") String monitorId,
        @Param("sequenceNumber") String sequenceNumber);

    /**
     * 获取流量传感器基础信息（油耗）
     * @param monitorId
     * @param sequenceNumber
     * @return
     */
    RemoteUpgradeSensorBasicInfo getMileSensorBasicInfo(@Param("monitorId") String monitorId,
        @Param("sequenceNumber") String sequenceNumber);

    /**
     * 新增传感器数据
     * @param sensorUpgrade sensorUpgrade
     * @return
     */
    Boolean addSensorUpgrade(SensorUpgrade sensorUpgrade);

    /**
     * 查询传感器状态信息
     * @param monitorId monitorId
     * @param sensorId sensorId
     * @return
     */
    SensorUpgrade getSensorUpgradeBy(@Param("monitorId") String monitorId, @Param("sensorId") String sensorId);

    void updateSensorUpgrade(SensorUpgrade sensorUpgrade);
}

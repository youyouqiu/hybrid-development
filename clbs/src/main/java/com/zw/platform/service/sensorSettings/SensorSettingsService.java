package com.zw.platform.service.sensorSettings;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.domain.vas.workhourmgt.SensorSettingInfo;
import com.zw.platform.util.common.JsonResultBean;
import org.apache.ibatis.annotations.Param;

import com.github.pagehelper.Page;
import com.zw.platform.domain.vas.f3.TransdusermonitorSet;
import com.zw.platform.domain.vas.mileageSensor.MileageSensorConfigQuery;

import java.util.ArrayList;
import java.util.List;

public interface SensorSettingsService {
    /**
     * 根据传感器类别查询监测设置
     * @param sensorType
     * @return
     */
    Page<TransdusermonitorSet> findTransduserByType(MileageSensorConfigQuery query, int sensorType) throws Exception;

    /**
     * 根据传感器类别查询监测设置
     * @param sensorType
     * @return
     */
    TransdusermonitorSet findWorkHourSettingByVid(String vehicleId, int sensorType) throws Exception;

    /**
     * 增加传感器监测设置
     */
    JsonResultBean addTransdusermonitorSet(String[] sensorList, int sensorType, String ipAddress) throws Exception;

    /**
     * 批量增加传感器监测设置
     */
    boolean addBatchTransdusermonitorSet(List<TransdusermonitorSet> transdusermonitotSets) throws Exception;

    /**
     * 根据监测id设置查询监测设置
     */
    TransdusermonitorSet findTransdusermonitorSetById(String id) throws Exception;

    /**
     * 根据ID修改 传感器设置
     */
    JsonResultBean updateSensorVehicle(TransdusermonitorSet transdusermonitorSet, String ipAddress) throws Exception;

    /**
     * 根据id删除传感器设置，解绑关系
     */
    JsonResultBean deleteSensorVehicle(List<String> ids, String ipAddress) throws Exception;

    /**
     * 批量解除绑定
     */
    void deleteBatchSensor(List<String> ids) throws Exception;

    /**
     * 根据车辆id删除对应传感器类型的所有绑定关系
     */
    void deleteAllBind(String vid, int sensorType, Integer type);

    /**
     * 根据传感器类别查询已经绑定的车的车牌 sensorType=-1 则为删除所有与该车辆绑定的传感器
     */
    List<TransdusermonitorSet> findVehicleBrandByType(int transduserType, List<Integer> protocols) throws Exception;

    /**
     * 参考车牌
     */
    List<TransdusermonitorSet> consultVehicle(int transduserType, List<Integer> protocols) throws Exception;

    /**
     * 根据车辆id与传感器型号查询该车辆绑定的传感器类型和信息
     */
    List<TransdusermonitorSet> findByVehicleId(@Param("sensorType") int sensorType,
        @Param("vehicleId") String vehicleId) throws Exception;

    /**
     * 下发温度参数
     * @param paramList
     */
    JsonResultBean sendSetDeviceParam(ArrayList<JSONObject> paramList, int sensorType, String ipAddress, Integer flag)
        throws Exception;

    /**
     * 修改温度传感器,先删除,后添加,为了控制事务
     * @param vehicleId
     * @param type
     * @param sensorList
     * @param ipAddress
     */
    public boolean updateSensorSetting(String vehicleId, int type, String[] sensorList, String ipAddress)
        throws Exception;

    /**
     * 获取传感器
     * @param detectionMode
     * @param sensorType    sensorType
     * @return list
     */
    List<SensorSettingInfo> findSensorInfo(String detectionMode, String sensorType);

    List<TransdusermonitorSet> findVehicleReference(int i);
}

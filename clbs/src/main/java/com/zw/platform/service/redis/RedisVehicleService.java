package com.zw.platform.service.redis;

import com.zw.platform.domain.vas.f3.TransdusermonitorSet;
import com.zw.platform.domain.vas.oilmassmgt.OilVehicleSetting;
import com.zw.platform.util.common.RedisSensorQuery;

import java.util.List;
import java.util.Map;

public interface RedisVehicleService {

    /**
     * 获取用户拥有的车
     * @param map         存放分组，和组织，模糊查询条件
     * @param monitorType 传感器类型,模糊查询条件
     * @param protocol    车绑定的设备协议类型
     * @return 车辆列表
     * @author wanxing
     */
    List<String> getUserVehicles(Map<String, Object> map, String monitorType, Integer protocol)
        throws InterruptedException;

    /**
     * 根据监控类型获取用户能监控的且已绑定设备的车辆id列表
     * @param query 查询条件
     * @param type  类型
     * @return 车辆id列表
     * @throws InterruptedException
     */
    List<String> getVehicleByType(RedisSensorQuery query, String type) throws InterruptedException;

    /**
     * 根据传感器类型添加车辆绑定关系
     * @param sensorType 传感器类型
     * @param vid        车辆id
     * @param sid        传感器车辆绑定关系id
     * @param number     传感器型号
     */
    void addVehicleSensorBind(int sensorType, String vid, String sid, String number);

    /**
     * 删除车辆和传感器绑定关系
     * @param sensorType 传感器类型
     * @param vid        车辆id
     * @param sid        传感器车辆绑定关系id
     * @param number     传感器型号
     */
    void delVehicleSensorBind(int sensorType, String vid, String sid, String number);

    /**
     * 删除车辆和油箱绑定关系
     * @param id     车辆id
     * @param sid    油箱车辆绑定关系id
     * @param number 油箱型号
     */
    void delVehicleTankBind(String vid, String sid, String number);

    /**
     * 删除车辆油箱和油位传感器绑定关系
     * @param vid
     * @param sid
     * @param number
     */
    void delVehicleOilSensorBind(String vid, String sid, String number);

    /**
     * 根据传感器类型删除车辆所有的传感器绑定关系
     * @param vid        车辆id
     * @param sensorType 传感器类型
     */
    void delAllSensorBind(String vid, int sensorType);

    /**
     * 删除redis中车和所有传感器的缓存
     */
    void delAllSensorBindByVehicleId(String vid);

    void updateVehicleSensorBind(TransdusermonitorSet set);

    /**
     * 修改邮箱型号后,维护车和邮箱的缓存,用于模糊匹配
     */
    void updateVehicleOilBoxCache(String oilName, List<OilVehicleSetting> list);

    /**
     * 根据监控类型获取已绑定设备的车辆id列表
     * @param type
     * @return
     * @throws Exception
     */
    Map<String, String> getVehicleBindByType(String type) throws Exception;

    /**
     * 获取绑定油位传感器的监控对象
     * @return
     * @throws Exception
     */
    List<String> getOilSensorVehicle(RedisSensorQuery query) throws InterruptedException;

    /**
     * 删除工时
     * @param vehicleId    workHourSettingInfo
     * @param id
     * @param sensorNumber sensorNumber
     */
    void delVehicleWorkHourBind(String vehicleId, String id, String sensorNumber);

    /**
     * 删除载重
     * @param vehicleId
     * @param id
     * @param sensorNumber
     */
    void delLoadBind(String vehicleId, String id, String sensorNumber);

    /**
     * 删除OBD
     * @param vehicleId    workHourSettingInfo
     * @param id
     * @param sensorNumber sensorNumber
     */
    void delOBDSettingBind(String vehicleId, String id, String sensorNumber);

    void delAllSensorBindByMonitorIds(List<String> monitorIds);
}

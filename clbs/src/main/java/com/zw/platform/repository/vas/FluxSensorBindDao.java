package com.zw.platform.repository.vas;

import com.github.pagehelper.Page;
import com.zw.platform.domain.vas.oilmgt.FluxSensor;
import com.zw.platform.domain.vas.oilmgt.FuelVehicle;
import com.zw.platform.domain.vas.oilmgt.form.FluxSensorBindForm;
import com.zw.platform.domain.vas.oilmgt.query.FluxSensorBindQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>Title: 流量传感器与车的绑定Dao</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @version 1.0
 * @author: wangying
 * @date 2016年9月19日上午9:16:03
 */
public interface FluxSensorBindDao {

    /**
     * 查询流量传感器和车的绑定
     */
    Page<FuelVehicle> findVehicleSensor(@Param("param") FluxSensorBindQuery query, @Param("userId") String userId,
        @Param("groupList") List<String> groupList);

    List<FuelVehicle> findVehicleSensorRedis(@Param("param") FluxSensorBindQuery query,
        @Param("list") List<String> list);

    /**
         * @param userId     userId
     * @param groupList groupList
     * @param reportDeviceTypes reportDeviceTypes
     * @return List<FuelVehicle>
     * @author wangying
     */
    List<FuelVehicle> findFuelVehicle(@Param("userId") String userId, @Param("groupList") List<String> groupList,
        @Param("reportDeviceTypes") List<String> reportDeviceTypes);

    /**
         * @param userId     userId
     * @param groupList groupList
     * @param reportDeviceTypes reportDeviceTypes
     * @return List<FuelVehicle>
     * @author wangying
     */
    Set<String> findFuelVehicleIds(@Param("userId") String userId, @Param("groupList") List<String> groupList,
        @Param("reportDeviceTypes") List<String> reportDeviceTypes);

    /**
         * @param vehicleId
     * @return FuelVehicle
     * @throws
     * @Title: 根据id查询车辆查询油耗车辆设置
     * @author wangying
     */
    FuelVehicle findFuelVehicleById(@Param("id") String id);

    /**
         * @param vehicleId
     * @return FuelVehicle
     * @throws
     * @Title: 根据车辆id查询车辆查询油耗车辆设置
     * @author wangying
     */
    FuelVehicle findFuelVehicleByVid(@Param("vehicleId") String vehicleId);

    /**
         * @param form
     * @return boolean
     * @throws
     * @Title: 新增车与传感器绑定
     * @author wangying
     */
    boolean addFluxSensorBind(FluxSensorBindForm form);

    /**
         * @param form
     * @return boolean
     * @throws
     * @Title: 修改
     * @author wangying
     */
    boolean updateFluxSensorBind(FluxSensorBindForm form);

    /**
         * @param id
     * @return boolean
     * @throws
     * @Title: 根据id删除
     * @author wangying
     */
    boolean deleteFluxSensorBind(String id);

    /**
         * @param bindId
     * @return FluxSensor
     * @throws
     * @Title: 根据绑定id
     * @author wangying
     */
    FluxSensor findFluxSensorByBindId(String bindId);

    /**
         * @param id
     * @return boolean
     * @throws
     * @Title: 根据车辆id删除
     * @author wangying
     */
    int deleteFluxSensorByVid(String vehicleId);

    /**
     * @return
     * @Description:获取车辆绑定的传感器的
     * @author wanxing
     */
    public List<Map<String, String>> findBindingOilMonitor();

    /**
     * @param vehicles
     * @return
     * @Description:redis 获取十条数据
     * @author wanxing
     */
    List<FuelVehicle> findVehicleSensorRedis(List<String> list);

    /**
     * 通过传感器的id查询，维护缓存
     * @param sensorId
     * @return
     * @Description:
     * @author wanxing
     */
    List<String> findBySensorId(String sensorId);

    List<FuelVehicle> findFuelVehicleByProtocols(@Param("userId") String userId,
        @Param("groupList") List<String> groupList, @Param("protocols") List<Integer> protocols);
}

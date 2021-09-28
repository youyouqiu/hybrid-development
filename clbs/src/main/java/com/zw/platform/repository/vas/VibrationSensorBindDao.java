package com.zw.platform.repository.vas;

import com.github.pagehelper.Page;
import com.zw.platform.domain.vas.workhourmgt.VibrationSensorBind;
import com.zw.platform.domain.vas.workhourmgt.form.VibrationSensorBindForm;
import com.zw.platform.domain.vas.workhourmgt.query.VibrationSensorBindQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * <p>Title: 振动传感器与车的绑定Dao</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: ZhongWei</p>
 * <p>team: ZhongWeiTeam</p>
 * @version 1.0
 * @author: wangying
 * @date 2016年9月19日上午9:16:03
 */
public interface VibrationSensorBindDao {
    /**
     * 查询振动传感器和车的绑定（管理员）
     */
    Page<VibrationSensorBind> findVehicleSensor(@Param("param") VibrationSensorBindQuery query,
        @Param("userId") String userId, @Param("groupList") List<String> groupList);

    /**
         * @param vehicleId
     * @return List<FuelVehicle>
     * @throws
     * @Title: 根据车辆查询工时车辆设置(管理员)
     * @author wangying
     */
    List<VibrationSensorBind> findWorkHourVehicle(@Param("userId") String userId,
        @Param("groupList") List<String> groupList);

    /**
         * @param vehicleId
     * @return FuelVehicle
     * @throws
     * @Title: 根据id查询车辆查询工时车辆设置
     * @author wangying
     */
    VibrationSensorBind findWorkHourVehicleById(@Param("id") String id);

    /**
         * @param vehicleId
     * @return FuelVehicle
     * @throws
     * @Title: 根据车辆id查询车辆查询工时车辆设置
     * @author wangying
     */
    VibrationSensorBind findWorkHourVehicleByVid(@Param("vehicleId") String vehicleId);

    VibrationSensorBind getThresholds(@Param("vehicleId") String vehicleId);

    /**
         * @param form
     * @return boolean
     * @throws
     * @Title: 新增车与传感器绑定
     * @author wangying
     */
    boolean addWorkHourSensorBind(VibrationSensorBindForm form);

    /**
         * @param form
     * @return boolean
     * @throws
     * @Title: 修改
     * @author wangying
     */
    boolean updateWorkHourSensorBind(VibrationSensorBindForm form);

    /**
         * @param id
     * @return boolean
     * @throws
     * @Title: 根据id删除
     * @author wangying
     */
    boolean deleteWorkHourSensorBindById(String id);

    /**
         * @param id
     * @return boolean
     * @throws
     * @Title: 根据传感器id删除
     * @author wangying
     */
    boolean deleteWorkHourSensorBindBySid(@Param("sensorId") String sensorId);

    /**
         * @param id
     * @return boolean
     * @throws
     * @Title: 根据车辆id删除
     * @author wangying
     */
    boolean deleteWorkHourSensorBindByVid(@Param("vehicleId") String vehicleId);

    List<Map<String, String>> findBindingMonitor();

    List<VibrationSensorBind> findVehicleSensorRedis(List<String> list);

    /**
     * 根据振动传感器的id获取绑定的车的id
     * @param id
     * @return
     * @Description:
     * @author wanxing
     */
    List<String> findWorkHourVehicleBySensorId(String id);

    boolean deleteBatchWorkHourSensorBindByVid(@Param("monitorIds") List<String> monitorIds);

    /**
     *
     *TODO
     * @Title: 根据绑定id
     * @param bindId
     * @return
     * @return FluxSensor
     * @throws
     * @author wangying
     */
    //	FluxSensor findWorkHourSensorByBindId(String bindId);
}

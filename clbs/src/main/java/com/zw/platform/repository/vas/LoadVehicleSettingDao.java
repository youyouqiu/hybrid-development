package com.zw.platform.repository.vas;

import com.github.pagehelper.Page;
import com.zw.platform.domain.vas.loadmgt.LoadVehicleSettingInfo;
import com.zw.platform.domain.vas.loadmgt.ZwMSensorInfo;
import com.zw.platform.domain.vas.loadmgt.form.LoadVehicleSettingSensorForm;
import com.zw.platform.domain.vas.loadmgt.query.LoadVehicleSettingQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/***
 @Author gfw
 @Date 2018/9/10 11:00
 @Description 载重车辆设置Dao层
 @version 1.0
 **/
public interface LoadVehicleSettingDao {
    /**
     * 查询载重设置列表
     * @param query
     * @param userId
     * @param groupList
     * @return
     */
    Page<LoadVehicleSettingInfo> findLoadVehicleList(@Param("query") LoadVehicleSettingQuery query,
        @Param("userId") String userId, @Param("groupList") List<String> groupList);

    /**
     * 根据车辆Id列表查询传感器车辆信息
     * @param vehicleIds
     * @return
     */
    List<LoadVehicleSettingInfo> findVehicleByIds(@Param("vehicleIds") List<String> vehicleIds,
        @Param("engineIds") List<String> engineIds);

    /**
     * 根据传感器类型查询传感器
     * @param sensorType
     * @return
     */
    List<ZwMSensorInfo> findSensor(@Param("sensorType") String sensorType);

    /**
     * 查询参考对象
     * @param userId
     * @param groupList
     * @param type
     * @return
     */
    List<LoadVehicleSettingInfo> findLoadVehicle(@Param("userId") String userId,
        @Param("groupList") List<String> groupList, @Param("type") String type);

    /**
     * 根据车辆id获取已经绑定车辆的传感器信息
     * @param vehicleId
     * @return
     */
    List<LoadVehicleSettingInfo> findVehicleLoadSettingByVid(String vehicleId);

    /**
     * 根据vehicleId 获取已绑定人的传感器信息
     * @param vehicleId
     * @return
     */
    List<LoadVehicleSettingInfo> findPeopleLoadSettingByVid(String vehicleId);

    /**
     * 根据vehicleId 获取已绑定物的传感器信息
     * @param vehicleId
     * @return
     */
    List<LoadVehicleSettingInfo> findThingLoadSettingByVid(String vehicleId);

    /**
     * 将传感器和车辆进行绑定
     * @param form
     * @return
     */
    boolean addLoadSetting(LoadVehicleSettingSensorForm form);

    /**
     * 根据监控对象id获取已绑定的传感器
     * @param vehicleId
     * @return
     */
    List<LoadVehicleSettingInfo> findLoadSettingByMonitorVid(String vehicleId);

    /**
     * 更新传感器信息
     * @param form
     * @return
     */
    boolean updateLoadSetting(LoadVehicleSettingSensorForm form);

    /**
     * 删除数据 逻辑删除
     * @param id
     * @return
     */
    boolean deleteLoadSetting(String id);

    /**
     * 根据传感器车辆绑定表id查询数据
     * @param id
     * @return
     */
    LoadVehicleSettingInfo findSensorVehicleByBindId(String id);

    /**
     * 更新传感器序号为0
     * @param twoId
     * @return
     */
    boolean updateLoadSettingByID(String twoId);

    List<LoadVehicleSettingInfo> findLoadVehicleByProtocols(@Param("userId") String userId,
        @Param("groupList") List<String> groupList, @Param("protocols") List<Integer> protocols);
}

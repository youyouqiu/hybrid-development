package com.zw.platform.repository.vas;

import com.github.pagehelper.Page;
import com.zw.platform.domain.vas.workhourmgt.SensorVehicleInfo;
import com.zw.platform.domain.vas.workhourmgt.WorkHourSettingInfo;
import com.zw.platform.domain.vas.workhourmgt.form.WorkHourSensorForm;
import com.zw.platform.domain.vas.workhourmgt.form.WorkHourSettingForm;
import com.zw.platform.domain.vas.workhourmgt.query.WorkHourQuery;
import com.zw.platform.domain.vas.workhourmgt.query.WorkHourSettingQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 工时设置dao
 * @author zhouzongbo on 2018/5/28 16:27
 */
public interface WorkHourSettingDao {

    /**
     * 根据vehicleId查询工时设置
     * @param vehicleIds vehicleIds
     * @return list
     */
    List<WorkHourSettingInfo> findVehicleWorkHourSettingRedis(List<String> vehicleIds);

    /**
     * 查询工时设置列表
     * @param query     query
     * @param userId    获取用户的uuid
     * @param groupList 组织id
     * @return page
     */
    Page<WorkHourSettingInfo> findVehicleWorkHourSetting(@Param("query") WorkHourSettingQuery query,
        @Param("userId") String userId, @Param("groupList") List<String> groupList);

    /**
     * 查询参考对象
     * @param userId    userId
     * @param groupList groupList
     * @return list
     */
    List<WorkHourSettingInfo> findWorkHourSettingVehicle(@Param("userId") String userId,
        @Param("groupList") List<String> groupList, @Param("type") String type);

    /**
     * 新增
     * @param form form
     * @return boolean
     */
    boolean addWorkHourSetting(WorkHourSettingForm form);

    List<WorkHourSettingInfo> findWorkHourSettingByMonitorVid(String vehicleId);

    List<WorkHourSettingInfo> findVehicleWorkHourSettingByVid(String vehicleId);

    List<WorkHourSettingInfo> findThingWorkHourSettingByVid(String vehicleId);

    List<WorkHourSettingInfo> findPeopleWorkHourSettingByVid(String vehicleId);

    /**
     * 修改
     * @param form form
     * @return boolean
     */
    boolean updateWorkHourSetting(WorkHourSettingForm form);

    /**
     * 修改数据
     * @param id id
     * @return boolean
     */
    boolean deleteWorkHourSetting(String id);

    /**
     * 解绑数据
     * @param id id
     * @return boolean
     */
    boolean deleteWorkHourSettingById(String id);

    /**
     * 修改传感器序号
     * @param id id
     * @return boolean
     */
    boolean updateSensorVehicleSensorSequence(String id);

    /**
     * 根据传感器车辆绑定表id查询数据
     * @param id id
     * @return WorkHourSettingInfo
     */
    WorkHourSettingInfo getSensorVehicleByBindId(String id);

    WorkHourSettingInfo getSensorVehicleById(String id);

    /**
     * 获得绑定工时传感器的车辆
     * @param userId
     * @param groupList
     * @param sensorType
     * @param reportDeviceTypes
     * @return
     */
    List<SensorVehicleInfo> getBindVehicle(@Param("userId") String userId, @Param("groupList") List<String> groupList,
        @Param("sensorType") Integer sensorType, @Param("reportDeviceTypes") List<String> reportDeviceTypes);

    boolean updateWorkHourSensorSomeField(WorkHourSensorForm form);

    /**
     * @param vehicleIds
     * @param engineIds
     * @return
     */
    List<WorkHourSettingInfo> findEngineVehicleByIds(@Param("vehicleIds") List<String> vehicleIds,
        @Param("engineIds") List<String> engineIds);

    /**
     * 通过车辆id和传感器序号获取SensorVehicle信息
     * @param query
     * @return
     */
    WorkHourSettingInfo findSensorVehicleByVehicleIdAndSensorSequence(WorkHourQuery query);

    List<WorkHourSettingInfo> getWorkHourSettingByVehicleId(String vehicleId);

    boolean updateWorkSettingBind(WorkHourSettingForm form);

    List<WorkHourSettingInfo> findWorkHourSettingVehicleByProtoclos(@Param("userId") String userId,
        @Param("groupList") List<String> groupList, @Param("protocols") List<Integer> protocols);
}

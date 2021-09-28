package com.zw.platform.repository.vas;


import com.github.pagehelper.Page;
import com.zw.platform.domain.basicinfo.query.SensorConfigQuery;
import com.zw.platform.domain.systems.Directive;
import com.zw.platform.domain.vas.f3.SensorConfig;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;


/**
 * <p> Title:轮询配置Dao <p> Copyright: Copyright (c) 2016 <p> Company: ZhongWei <p> team: ZhongWeiTeam
 * @version 1.0
 * @author: nixiangqian
 * @date 2017年05月09日 14:48
 */
public interface SensorConfigDao {

    /**
     * 查询分页数据
     * @param query
     * @return
     */
    Page<SensorConfig> findByPage(@Param("param") SensorConfigQuery query, @Param("userId") String userId,
                                  @Param("groupList") List<String> groupList);

    /**
     * 获取所有的vehicle
     * @Description:
     * @author wanxing
     * @return
     */
    List<SensorConfig> findAllVehicle(List<String> list);

    // 多余 ，后面删除
    // List<SensorConfig> finadAllVehicle(SensorConfigQuery query);
    /**
     * 根据id查询监控轮询配置信息
     * @param id
     * @return
     */
    SensorConfig findById(@Param("id") String id);

    /**
     * 根据vehicleId查询监控轮询配置信息
     * @param vehicleId
     * @return
     */
    SensorConfig findByVehicleId(@Param("vehicleId") String vehicleId);

    /**
     * 根据vehicleId批量查询监控轮询配置信息
     * @param vehicleIds 监控对象id
     * @return List<SensorConfig>
     */
    List<SensorConfig> findByVehicleIds(@Param("vehicleIds") Collection<String> vehicleIds);

    /**
     * 查询轮询参数设置了的车辆
     * @Title: findVehicleAlarmSetting
     * @return
     * @return List<AlarmSetting>
     * @throws @author
     *             Liubangquan
     */
    List<SensorConfig> findVehicleSensorSetting(@Param("userId") String userId,
        @Param("groupList") List<String> groupList, @Param("protocols") List<Integer> protocols);

    /**
     * 新增配置信息数据
     * @param sensorConfig
     */
    void add(final SensorConfig sensorConfig);

    /**
     * 修改配置信息数据
     * @param sensorConfig
     */
    void updateSensorConfig(final SensorConfig sensorConfig);

    /**
     * 根据vechileId删除监控轮询配置信息
     * @param vechileId
     */
    void deleteByVechileId(@Param("vechileId") String vechileId);

    /**
     * 根据ids批量删除
     * @param ids
     */
    void deleteBatch(final List<String> ids);

    /**
     * 根据vechileIds批量删除
     * @param vechileIds
     */
    boolean deleteBatchByVechileId(@Param("vechileIds") List<String> vechileIds);


    @Select("select * from zw_m_directive where monitor_object_id = #{vehicleId} and swift_number = #{swiftNumber} "
        + "and reply_code = 0 order by create_data_username desc limit 1")
    @ResultType(Directive.class)
    Directive getDirectiveStatus(@Param("vehicleId")String vehicleId, @Param("swiftNumber")Integer swiftNumber);

    List<String> findSensorConfigIdByVehicleId(@Param("monitorIds") List<String> monitorIds);
}

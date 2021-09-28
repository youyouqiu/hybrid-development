package com.zw.platform.repository.vas;

import com.github.pagehelper.Page;
import com.zw.app.domain.monitor.SwitchInfo;
import com.zw.platform.domain.basicinfo.query.SensorConfigQuery;
import com.zw.platform.domain.vas.alram.AlarmParameter;
import com.zw.platform.domain.vas.alram.AlarmType;
import com.zw.platform.domain.vas.alram.IoVehicleConfigInfo;
import com.zw.platform.domain.vas.switching.IoVehicleConfig;
import com.zw.platform.domain.vas.switching.SwitchingSignal;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * @author zhangsq
 * @date 2018/6/28 9:54
 */
public interface IoVehicleConfigDao {

    /**
     * 新增
     *
     * @param ioVehicleConfig
     */
    boolean add(IoVehicleConfig ioVehicleConfig);

    /**
     * 新增
     *
     * @param ioVehicleConfigs
     */
    boolean addBatch(@Param("ioVehicleConfigs") List<IoVehicleConfig> ioVehicleConfigs);


    /**
     * 修改
     *
     * @param ioVehicleConfig
     */
    boolean updateIoConfig(IoVehicleConfig ioVehicleConfig);

    /**
     * @param vehicles
     * @return
     * @Description: 加了缓存以后 查询数据专用方法
     * @author wanxing
     */
    public Page<SwitchingSignal> findByPageRedis(List<String> vehicles);

    /**
     * 根据查询条件查询信息
     *
     * @param query
     * @return
     */
    public Page<SwitchingSignal> findByPage(@Param("param") SensorConfigQuery query, @Param("userId") String userId,
                                            @Param("groupList") List<String> groupList);

    public Boolean deleteByVehicleIds(@Param("vehicleIds") List<String> ids);

    public Boolean deleteByVehicleId(String vehicleId);

    List<Map> getVehicleBindIos(@Param("vehicleId") String vehicleId, @Param("ioType") Integer ioType);

    @Select("select id from zw_m_alarm_type where name = #{name} and pos = #{pos} and flag = 1")
    @ResultType(String.class)
    String getAlarmTypeByPosAndName(@Param("name") String name, @Param("pos") String pos);

    @Select("select id from zw_m_alarm_type where pos = #{pos} and flag = 1")
    @ResultType(String.class)
    String getAlarmTypeByPos(@Param("pos") String pos);

    boolean addAlarmParameterBatch(@Param("alarmParameters") List<AlarmParameter> alarmParameters);

    @Select("select * from zw_m_io_vehicle_config where id = #{id} and flag = 1")
    @ResultType(IoVehicleConfig.class)
    IoVehicleConfig findById(@Param("id") String id);

    /**
     * 根据id列表获取数据
     *
     * @param ids
     * @return
     */
    List<IoVehicleConfig> findByIds(@Param("ids") List<String> ids, @Param("vehicleId") String vehicleId);

    /**
     * 根据报警类型Id和io监控对象ID获取报警参数ID
     * @param alarmTypeId 报警类型id
     * @param ioMonitorId 监控对象id
     * @return List<String>
     */
    List<String> findParaIdByAlarmTypeIdAndIoMonitorId(@Param("alarmTypeId") String alarmTypeId,
        @Param("ioMonitorId") String ioMonitorId);


    boolean delAlarmParaByIds(@Param("ids") List<String> ids);

    boolean delAlarmParaSettingByIds(@Param("ids") List<String> ids, @Param("vehicleId") String vehicleId);

    boolean delVehicleConfigByIds(@Param("ids") List<String> ids, @Param("vehicleId") String vehicleId);

    /**
     * 删除液位异常报警设置
     * @param vehicleId
     * @return
     */
    boolean delLevelAlarm(@Param("vehicleId") String vehicleId, @Param("levelAlarms") List<AlarmType> levelAlarms);

    List<AlarmType> getLevelAlarm(@Param("tankerNo") String tankerNo);

    IoVehicleConfig findByIoTypeAndSite(@Param("ioSite") Integer ioSite, @Param("IoType") Integer ioType,
        @Param("vehicleId") String vehicleId);

    /**
     * 根据车辆id获取io报警参数Id列表
     * @param vehicleId
     * @return
     */
    List<String> findAlarmParameterIdsByVehicleId(@Param("ioMonitorId") String vehicleId);

    @Update("UPDATE zw_m_alarm_parameter set flag=0 WHERE io_monitor_id = #{vehicleId} and flag = 1")
    boolean delAlarmParameterByVehicleId(@Param("vehicleId") String vehicleId);

    List<IoVehicleConfigInfo> findIoConfigBy(@Param("vehicleId") String vehicleId);

    List<SwitchInfo> getBindIoInfoByVehicleId(@Param("vehicleId") String vehicleId);

    List<String> findIoConfigByVehicleId(String vehicleId);
}

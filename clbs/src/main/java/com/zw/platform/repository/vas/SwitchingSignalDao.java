package com.zw.platform.repository.vas;

import com.github.pagehelper.Page;
import com.zw.app.domain.monitor.IoSensorConfigInfo;
import com.zw.platform.domain.basicinfo.query.SensorConfigQuery;
import com.zw.platform.domain.vas.alram.IoVehicleConfigInfo;
import com.zw.platform.domain.vas.switching.SwitchSignalStatisticsInfo;
import com.zw.platform.domain.vas.switching.SwitchingSignal;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p> Title:开关信号管理Dao <p> Copyright: Copyright (c) 2016 <p> Company: ZhongWei <p> team: ZhongWeiTeam
 * @version 1.0
 * @author: nixiangqian
 * @date 2017年06月21日 14:25
 */
public interface SwitchingSignalDao {

    /**
     * 根据查询条件查询信息
     * @param query
     * @return
     */
    public Page<SwitchingSignal> findByPage(@Param("param") SensorConfigQuery query, @Param("userId") String userId,
        @Param("groupList") List<String> groupList);

    /**
     * @return
     */
    public List<SwitchingSignal> findVehicleSensorSetting(@Param("userId") String userId,
        @Param("groupList") List<String> groupList, @Param("protocols") List<Integer> protocols);

    /**
     * 根据车辆编号查询信息
     * @param vehicleId
     * @return
     */
    public SwitchingSignal findByVehicleId(@Param("vehicleId") String vehicleId);

    /**
     * 根据编号查询信息
     * @param id
     * @return
     */
    public SwitchingSignal findById(@Param("id") String id);

    /**
     * 新增开关管理
     * @param signal
     * @return
     */
    public Boolean addSwitchingSignal(SwitchingSignal signal);

    /**
     * 修改开关管理
     * @param signal
     * @return
     */
    public Boolean updateSwitchingSignal(SwitchingSignal signal);

    /**
     * 根据Id删除配置信息
     * @param id
     * @return
     */
    public Boolean deleteById(String id);

    /**
     * 根据id批量删除
     * @param ids
     * @return
     */
    public Boolean deleteBatchByIds(@Param("ids") List<String> ids);

    /**
     * @param vehicles
     * @return
     * @Description: 加了缓存以后 查询数据专用方法
     * @author wanxing
     */
    public Page<SwitchingSignal> findByPageRedis(List<String> vehicles);

    public Boolean deleteByVehicleId(String vehicleId);

    /**
     * 获取车辆绑定IO传感器所有高低电频信息
     * @param monitor
     * @return
     */
    List<IoSensorConfigInfo> getIoSensorConfigInfo(@Param("monitor") String monitor);

    /**
     * 获得绑定了开关信号的车辆
     * @param userId
     * @param groupList
     * @return
     */
    List<SwitchSignalStatisticsInfo> getBindSwitchSignalVehicle(@Param("userId") String userId,
        @Param("groupList") List<String> groupList);

    /**
     * 获得车辆功能id绑定的io位置
     * @param vehicleId
     * @return
     */
    List<IoVehicleConfigInfo> getFunctionIdBingIoSite(@Param("vehicleId") String vehicleId);

    Boolean deleteBatchByMonitorIds(List<String> monitorIds);
}

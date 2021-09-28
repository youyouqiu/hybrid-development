package com.zw.platform.service.switching;

import com.github.pagehelper.Page;
import com.zw.platform.domain.basicinfo.query.SensorConfigQuery;
import com.zw.platform.domain.vas.switching.SwitchingSignal;
import com.zw.platform.util.common.JsonResultBean;

import java.util.List;

/**
 * <p> Title:开关信号管理Service <p> Copyright: Copyright (c) 2016 <p> Company: ZhongWei <p> team: ZhongWeiTeam
 * @version 1.0
 * @author: nixiangqian
 * @date 2017年06月21日 13:59
 */
public interface SwitchingSignalService {

    /**
     * 根据查询条件查询信息
     * @param query
     * @return
     */
    public Page<SwitchingSignal> findByPage(SensorConfigQuery query) throws Exception;

    /**
     * 根据查询条件查询信息
     * @param
     * @param protocols
     * @return
     */
    public List<SwitchingSignal> findVehicleSensorSetting(List<Integer> protocols) throws Exception;

    /**
     * 根据车辆编号查询信息
     * @param vehicleId
     * @return
     */
    public SwitchingSignal findByVehicleId(String vehicleId) throws Exception;

    /**
     * 根据车辆编号查询信息
     * @param vehicleId
     * @return
     */
    public Integer[] findAirStatus(String vehicleId) throws Exception;

    /**
     * 根据编号查询信息
     * @param id
     * @return
     */
    public SwitchingSignal findById(String id) throws Exception;

    /**
     * 新增开关管理
     * @param signal
     * @return
     */
    public JsonResultBean addSwitchingSignal(SwitchingSignal signal, String ipAddress) throws Exception;

    /**
     * 修改开关管理
     * @param signal
     * @return
     */
    public Boolean updateSwitchingSignal(SwitchingSignal signal, String ipAddress) throws Exception;

    /**
     * 根据Id删除配置信息
     * @param id
     * @return
     */
    public Boolean deleteById(String id, String ipAddress) throws Exception;

    /**
     * 根据id批量删除
     * @param ids
     * @return
     */
    public JsonResultBean deleteBatchByIds(List<String> ids, String ipAddress) throws Exception;

    /**
     * 根据车辆编号下发8201获取状态位信息
     * @param vehicleId
     * @return
     */
    public JsonResultBean sendPosition(String vehicleId, String ipAddress) throws Exception;

    /**
     * 根据vehcileid删除监测功能类型的绑定
     */
    public Boolean deleteConfigByVehicleId(String vehicleId, Integer type);

}

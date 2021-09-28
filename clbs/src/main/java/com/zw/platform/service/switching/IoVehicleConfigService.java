package com.zw.platform.service.switching;

import com.github.pagehelper.Page;
import com.zw.platform.domain.basicinfo.query.SensorConfigQuery;
import com.zw.platform.domain.vas.switching.IoVehicleConfig;
import com.zw.platform.domain.vas.switching.SwitchingSignal;
import com.zw.platform.util.common.JsonResultBean;

import java.util.List;
import java.util.Map;

/**
 * @author zhangsq
 * @date 2018/6/28 10:18
 */
public interface IoVehicleConfigService {


    /**
     * 批量绑定io信号位
     *
     * @param ioVehicleConfigs
     * @param ip
     * @return
     */
    JsonResultBean addIoConfigs(List<IoVehicleConfig> ioVehicleConfigs, String ip) throws Exception;


    Page<SwitchingSignal> findByPage(SensorConfigQuery query) throws Exception;

    Boolean deleteById(String vehicleId, String ipAddress) throws Exception;

    JsonResultBean deleteBatchByIds(List<String> ids, String ipAddress) throws Exception;

    /**
     * 根据车辆id和io类型获取io绑定列表
     *
     * @param vehicleId
     * @param ioType
     * @return
     */
    List<Map> getVehicleBindIos(String vehicleId, Integer ioType);

    /**
     * 修改io参数设置
     *
     * @param ioVehicleConfigs 
     * @param delIds
     * @param ip
     * @param vehicleId
     * @return
     * @throws Exception
     */
    JsonResultBean updateIoConfigs(List<IoVehicleConfig> ioVehicleConfigs, String delIds, String ip, String vehicleId) throws Exception;
}

package com.zw.platform.service.sensor;


import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.domain.basicinfo.query.SensorConfigQuery;
import com.zw.platform.domain.systems.Directive;
import com.zw.platform.domain.vas.f3.SensorConfig;
import com.zw.platform.util.common.JsonResultBean;

import java.util.ArrayList;
import java.util.List;


/**
 * @version 1.0
 * @author: nixiangqian
 * @date 2017年05月09日 14:39
 */
public interface SensorConfigService {

    /**
     * 查询分页数据
     * @param query
     * @return
     */
    Page<SensorConfig> findByPage(SensorConfigQuery query) throws Exception;

    /**
     * 获取单个车的下发状态
     * @param vehicleId
     * @return
     */
    SensorConfig getSendWebSocket(String vehicleId);

    /**
     * 根据id查询监控轮询配置信息
     * @param id
     * @return
     */
    SensorConfig findById(String id) throws Exception;

    /**
     * 根据vehicleId查询监控轮询配置信息
     * @param vehicleId
     * @return
     */
    SensorConfig findByVehicleId(String vehicleId);

    /**
     * 新增配置信息
     * @param sensorConfig
     */
    void addSensorConfig(SensorConfig sensorConfig, String vehicleId);

    /**
     * 查询轮询参数设置了的车辆
     */
    List<SensorConfig> findVehicleSensorSetting(List<Integer> protocols);

    /**
     * 修改配置信息数据
     * @param sensorConfig
     */
    void updateSensorConfig(SensorConfig sensorConfig, String vehicleId);

    /**
     * 根据vehicleId删除监控轮询配置信息
     * @param vehicleId
     */
    JsonResultBean deleteByVehicleId(String vehicleId);

    /**
     * 根据vehicleIds批量删除
     * @param vehicleIds
     */
    JsonResultBean deleteBatchByVehicleId(List<String> vehicleIds);

    /**
     * 下发参数
     * @param paramList
     * @throws Exception
     */
    JsonResultBean sendParam(ArrayList<JSONObject> paramList);

    /**
     * 清除轮询
     * @param paramList
     * @return
     */
    JsonResultBean sendClearPolling(ArrayList<JSONObject> paramList);

    /**
     * 获取下发指令
     * @param vehicleId
     * @param swiftNumber
     * @return
     */
    Directive getDirectiveStatus(String vehicleId, Integer swiftNumber);
}

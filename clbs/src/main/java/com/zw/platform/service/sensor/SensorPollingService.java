package com.zw.platform.service.sensor;

import com.zw.platform.domain.vas.f3.SensorPolling;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * Title:轮询数据Service
 * <p>
 * Copyright: Copyright (c) 2016
 * <p>
 * Company: ZhongWei
 * <p>
 * team: ZhongWeiTeam
 *
 * @version 1.0
 * @author: nixiangqian
 * @date 2017年05月09日 14:12
 */
public interface SensorPollingService {

    /**
     * 根据监控对象获取轮询设置
     * @param vehicleId
     * @return
     */
    List<SensorPolling> findByVehicleId(String vehicleId);

    /**
     * 车和外设轮训的关系
     * @return
     */
    Map<String, String> findAllSensorPoll();

    /**
     * 根据配置获取轮询设置
     * @param configid
     * @return
     */
    List<SensorPolling> findByConfigid(String configid);

    /**
     * 根据主键ID查询外设信息
     * @param id
     * @return
     */
    SensorPolling findByid(String id);

    /**
     * 新增SensorPolling
     * @param sensorPolling
     */
    void addSensorPolling(SensorPolling sensorPolling);


    /**
     * 批量新增
     *
     * @param sensorPollings
     * @return
     */
    boolean addByBatch(List<SensorPolling> sensorPollings);

    /**
     * 根据车辆编号删除轮询信息
     * @param configId
     */
    void deleteByConfigId(String configId);

    /**
     * 获取Io状态，0下发生效，1，下发失败，null未下发
     * @param vehicleId
     * @return
     * @throws Exception
     */
    String findStatus(String vehicleId)throws Exception;


}

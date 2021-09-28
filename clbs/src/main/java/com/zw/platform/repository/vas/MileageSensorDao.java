package com.zw.platform.repository.vas;

import com.github.pagehelper.Page;
import com.zw.platform.domain.vas.mileageSensor.MileageSensor;
import com.zw.platform.domain.vas.mileageSensor.MileageSensorQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * Title:里程传感器基础信息Dao
 * <p>
 * Copyright: Copyright (c) 2016
 * <p>
 * Company: ZhongWei
 * <p>
 * team: ZhongWeiTeam
 *
 * @version 1.0
 * @author: nixiangqian
 * @date 2017年05月16日 11:10
 */
public interface MileageSensorDao {
    /**
     * 新增MileageSensor
     * @param mileageSensor
     */
    boolean addMileageSensor(MileageSensor mileageSensor);

    /**
     * 检查是否有配置,返回有配置的传感器型号
     * @param id
     * @return
     */
    String checkConfig(@Param("id")String id);

    /**
     * 修改 MileageSensor
     * @param mileageSensor
     */
    boolean updateMileageSensor(MileageSensor mileageSensor);

    /**
     * 批量添加MileageSensor
     * @param mileageSensors
     */
    boolean addBatchMileageSensors(List<MileageSensor> mileageSensors);

    /**
     * 批量删除MileageSensor
     * @param mileageSensorids
     */
    boolean deleteBatchMileageSensor(@Param("mileageSensorids")List<String> mileageSensorids);

    /**
     * 根据ID查询MileageSensor
     * @param id
     * @return
     */
    MileageSensor findById(@Param("id")String  id);

    /**
     * 根据sensorType查询MileageSensor
     * @param sensorType
     * @return
     */
    MileageSensor findBySensorType(@Param("sensorType")String  sensorType);

    /**
     * 根据查询条件查询信息
     * @param query
     * @return
     */
    Page<MileageSensor> findByQuery(MileageSensorQuery query);

    /**
     * 根据所有可用
     * @return
     */
    List<MileageSensor> findAll();

    /**
     * 查询所有已经绑定车的传感器
     * @return
     */
    List<Map<String,Object>> findBindingMonitor(Map<String,Object> map);

}

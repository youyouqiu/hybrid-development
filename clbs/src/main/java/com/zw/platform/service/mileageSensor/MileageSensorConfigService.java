package com.zw.platform.service.mileageSensor;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.domain.vas.mileageSensor.MileageSensorConfig;
import com.zw.platform.domain.vas.mileageSensor.MileageSensorConfigQuery;
import com.zw.platform.util.common.JsonResultBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Title:里程传感器配置Service
 * <p>
 * Copyright: Copyright (c) 2016
 * <p>
 * Company: ZhongWei
 * <p>
 * team: ZhongWeiTeam
 * @version 1.0
 * @author: nixiangqian
 * @date 2017年05月16日 11:16
 */
public interface MileageSensorConfigService {

    /**
     * 新增MileageSensorConfig
     * @param mileageSensorConfig
     */
    JsonResultBean addMileageSensorConfig(MileageSensorConfig mileageSensorConfig, String ipAddress) throws Exception;

    /**
     * 修改 MileageSensorConfig
     * @param mileageSensorConfig
     * @param isClearSendStatus   true 清除原来下发状态 false 不清除原来的下发状态
     */
    JsonResultBean updateMileageSensorConfig(MileageSensorConfig mileageSensorConfig, boolean isClearSendStatus,
        String ipAddress) throws Exception;

    /**
     * 获取参考设置车辆
     * @return
     */
    List<MileageSensorConfig> findVehicleSensorSet() throws Exception;

    /**
     * 批量删除MileageSensorConfig
     * @param vehicleIds
     */
    JsonResultBean deleteBatchMileageSensorConfig(List<String> vehicleIds, String ipAddress) throws Exception;

    /**
     * 更新标定状态及时间
     * @param mileageSensorConfig
     */
    Boolean updateNominalStatus(MileageSensorConfig mileageSensorConfig) throws Exception;

    /**
     * 根据vehicleId查询MMileageSensorConfig
     * @param vehicleId
     * @return
     */
    MileageSensorConfig findByVehicleId(String vehicleId) throws Exception;

    /**
     * 根据vehicleId查询MMileageSensorConfig
     * @param vehicleId
     * @param isAllow   true 获取下发信息 false
     * @return
     */
    MileageSensorConfig findByVehicleId(String vehicleId, boolean isAllow) throws Exception;

    /**
     * 根据查询条件查询信息
     * @param query
     * @return
     */
    Page<MileageSensorConfig> findByQuery(MileageSensorConfigQuery query) throws Exception;

    /**
     * 下发参数
     * @param map
     */
    JSONObject sendParam(Map<String, Object> map) throws Exception;

    /**
     * 下发参数
     * @param paramList
     */
    JsonResultBean sendParam(ArrayList<JSONObject> paramList, String ipAddress) throws Exception;

    List<MileageSensorConfig> findVehicleSensorSetByProtocols(List<Integer> protocols);
}

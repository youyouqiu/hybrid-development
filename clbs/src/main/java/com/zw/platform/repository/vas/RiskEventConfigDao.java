package com.zw.platform.repository.vas;


import com.zw.platform.domain.riskManagement.form.RiskEventVehicleConfigForm;
import com.zw.platform.domain.riskManagement.form.RiskEventVehicleForm;
import com.zw.platform.domain.riskManagement.query.RiskEventConfigQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


/**
 * 风险事件定义 Created by fanlu on 2017/8/22.
 */
public interface RiskEventConfigDao {
    /**
     * 查询风险定义绑定列表
     * @param query
     * @param userId
     * @param groupList
     * @return
     */

    List<Map<String, Object>> findRiskVehicleList(@Param("param") RiskEventConfigQuery query,
                                                  @Param("userId") String userId,
                                                  @Param("groupList") List<String> groupList);

    boolean addRiskVehicle(RiskEventVehicleForm eventForm);

    boolean addRiskVehicleConfig(List<RiskEventVehicleConfigForm> configForm);

    /**
     * 根据车id查询风险事件的设置
     * @param vehicleId
     * @return
     */
    public List<RiskEventVehicleConfigForm> findRiskSettingByVid(String vehicleId);

    /**
     * 查询参考车辆的设置详情
     * @return
     */
    public List<RiskEventVehicleConfigForm> findReferVehicle(@Param("userId") String userId,
                                                             @Param("groupList") List<String> groupList);

    /**
     * 批量删除风险设置
     * @param vehicleIds
     */
    public void deleteRiskVehicleByBatch(@Param("vehicleIds") List<String> vehicleIds);

    /**
     * 批量删除风险事件设置
     * @param vehicleIds
     */
    public void deleteRiskVehicleConfigByBatch(@Param("vehicleIds") List<String> vehicleIds);

    /**
     * 批量新增风险设置
     * @param list
     */
    public void addRiskVehicleByBatch(List<RiskEventVehicleForm> list);

    List<String> findAllRiskConfig();

    /**
     * 查询所有车辆的风险设置
     * @return
     */
    public List<RiskEventVehicleConfigForm> findRiskAllSetting();

    public List<Map<String, Object>> findRiskVehicleListRedis(List<String> vehicleList);

    /**
     * 根据id或 vehicleId 或 risk_id 查询
     * @param map
     * @return
     */
    public RiskEventVehicleConfigForm findRiskEventConfigByMap(Map<String,Object> map);

    List<RiskEventVehicleConfigForm> findDsmRiskSettingByVid(String vehicleId);

    List<RiskEventVehicleConfigForm> findAdasRiskSettingByVid(String vehicleId);

    void deleteDsmRiskVehicleConfig(String vehicleId);

    void deleteDsmRiskVehicleBind(String vehicleId);

    void deleteAdasRiskVehicleConfig(String vehicleId);

    int getRiskEventVehicleConfigFormByVehicleId(String vehicleId);
}

package com.zw.adas.repository.mysql.riskdisposerecord;


import com.zw.adas.domain.riskManagement.form.AdasRiskEventVehicleConfigForm;
import com.zw.adas.domain.riskManagement.form.AdasRiskEventVehicleForm;
import com.zw.adas.domain.riskManagement.query.AdasRiskEventConfigQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


/**
 * 风险事件定义 Created by fanlu on 2017/8/22.
 */
public interface AdasRiskEventConfigDao {
    /**
     * 查询风险定义绑定列表
     *
     * @param query
     * @param userId
     * @param groupList
     * @return
     */

    List<Map<String, Object>> findRiskVehicleList(@Param("param") AdasRiskEventConfigQuery query,
                                                  @Param("userId") String userId,
                                                  @Param("groupList") List<String> groupList);

    boolean addRiskVehicle(AdasRiskEventVehicleForm eventForm);

    boolean addRiskVehicleConfig(List<AdasRiskEventVehicleConfigForm> configForm);

    /**
     * 根据车id查询风险事件的设置
     *
     * @param vehicleId
     * @return
     */
    public List<AdasRiskEventVehicleConfigForm> findRiskSettingByVid(String vehicleId);

    /**
     * 查询参考车辆的设置详情
     *
     * @return
     */
    public List<AdasRiskEventVehicleConfigForm> findReferVehicle(@Param("userId") String userId,
                                                                 @Param("groupList") List<String> groupList);

    /**
     * 批量删除风险设置
     *
     * @param vehicleIds
     */
    public void deleteRiskVehicleByBatch(@Param("vehicleIds") List<String> vehicleIds);

    /**
     * 批量删除风险事件设置
     *
     * @param vehicleIds
     */
    public void deleteRiskVehicleConfigByBatch(@Param("vehicleIds") List<String> vehicleIds);



    void deleteAdasCommonParameterByBatch(@Param("vehicleIds") List<String> vehicleIds);

    void deleteAdasAlarmParameterByBatch(@Param("vehicleIds") List<String> vehicleIds);

    /**
     * 批量新增风险设置
     *
     * @param list
     */
    public void addRiskVehicleByBatch(List<AdasRiskEventVehicleForm> list);

    List<String> findAllRiskConfig();

    /**
     * 查询所有车辆的风险设置
     *
     * @return
     */
    public List<AdasRiskEventVehicleConfigForm> findRiskAllSetting();

    public List<Map<String, Object>> findRiskVehicleListRedis(List<String> vehicleList);

    /**
     * 根据id或 vehicleId 或 risk_id 查询
     *
     * @param map
     * @return
     */
    public AdasRiskEventVehicleConfigForm findRiskEventConfigByMap(Map<String, Object> map);

    List<AdasRiskEventVehicleConfigForm> findDsmRiskSettingByVid(String vehicleId);

    List<AdasRiskEventVehicleConfigForm> findAdasRiskSettingByVid(String vehicleId);

    void deleteDsmRiskVehicleConfig(String vehicleId);

    void deleteDsmRiskVehicleBind(String vehicleId);

    void deleteAdasRiskVehicleConfig(String vehicleId);

    int getRiskEventVehicleConfigFormByVehicleId(String vehicleId);
}

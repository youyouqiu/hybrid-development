package com.zw.adas.service.defineSetting;

import com.zw.adas.domain.riskManagement.form.AdasRiskEventVehicleConfigForm;
import com.zw.adas.domain.riskManagement.query.AdasRiskEventConfigQuery;
import com.zw.platform.util.common.JsonResultBean;

import java.util.List;
import java.util.Map;

/**
 * Created by fanlu on 2017/8/22.
 */
public interface AdasRiskEventConfigService {

    /**
     * 分页查询
     */
    List<Map<String, Object>> findRiskVehicleList(final AdasRiskEventConfigQuery query);

    /**
     * 根据车id查询风险事件的设置
     */
    List<AdasRiskEventVehicleConfigForm> findRiskSettingByVid(String vehicleId);

    /**
     * 查询参考车辆
     */
    List<AdasRiskEventVehicleConfigForm> findReferVehicle();

    /**
     * 批量修改风险定义设置
     */
    void updateRiskSettingByBatch(List<String> vehicleIds, List<AdasRiskEventVehicleConfigForm> list,
        String ipAddress) throws Exception;

    /**
     * 根据车辆id删除风险绑定
     * @param sign 标识符(0 表示不需要记录操作日志 || 1 表示需要记录操作日志)
     */
    void deleteRiskSettingByVehicleIds(List<String> vehicleIds, String ipAddress, String sign);

    /**
     * 参数下发
     * @param vehicleIds 车辆编号
     * @return 最后一辆车下发消息编号
     */
    String sendParamSet(List<String> vehicleIds, String ipAddress) throws Exception;

    JsonResultBean sendPInfo(String brand, String sensorID, String commandType, String ipAddress);

    List<AdasRiskEventVehicleConfigForm> findDsmRiskSettingByVid(String vehicleId);

    List<AdasRiskEventVehicleConfigForm> findAdasRiskSettingByVid(String vehicleId);

    void updateADRiskSetting(String vehicleId, List<AdasRiskEventVehicleConfigForm> dbSetting, String ipAddress,
        boolean flag);
}

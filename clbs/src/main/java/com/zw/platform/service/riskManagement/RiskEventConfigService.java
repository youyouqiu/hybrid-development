package com.zw.platform.service.riskManagement;

import com.zw.platform.basic.event.ConfigUnBindEvent;
import com.zw.platform.domain.riskManagement.form.RiskEventVehicleConfigForm;

import java.util.List;
import java.util.Map;


/**
 * Created by fanlu on 2017/8/22.
 */
public interface RiskEventConfigService {

    /**
     * 根据车辆id删除风险绑定
     * @param unBindEvent
     * @param sign 标识符(0 表示不需要记录操作日志 || 1 表示需要记录操作日志)
     */
    void deleteRiskSettingByVehicleIds(List<String> vehicleIds, ConfigUnBindEvent unBindEvent, String sign);

    /**
     * 查询所有车辆的风险设置
     */
    List<RiskEventVehicleConfigForm> findAllRiskSetting();

    RiskEventVehicleConfigForm findRiskEventConfigByMap(Map<String, Object> map);
}

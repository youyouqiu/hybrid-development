package com.zw.adas.repository.mysql.riskdisposerecord;

import com.zw.adas.domain.define.setting.AdasJingParamSetting;
import com.zw.adas.domain.define.setting.AdasPlatformParamSetting;
import com.zw.adas.domain.define.setting.AdasSettingListDo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @Author zhangqiang
 * @Date 2020/6/10 14:19
 */
public interface AdasAlarmJingParamSettingDao {

    /**
     * 查询参数设置内容
     * @param vehicleId
     * @return
     */
    List<AdasJingParamSetting> findJingParamByVehicleId(String vehicleId);

    /**
     * 京标参数设置列表查询
     * @param paramMap
     * @return
     */
    List<AdasSettingListDo> selectJingParamList(@Param("params") Map<String, Object> paramMap);

    /**
     * 根据车辆ids删除参数设置
     * @param vehicleIds
     */
    void deleteParamSetting(@Param("vehicleIds") List<String> vehicleIds,
        @Param("paramTypeSet") Set<Integer> paramTypeSet);

    void addJingAlarmSettingByBatch(@Param("settings") List<AdasJingParamSetting> settings);

    void deleteParamSettingByVehicleIds(@Param("vehicleIds") List<String> vehicleIds);

    List<AdasPlatformParamSetting> findJingUnAutomaticInfo();

    Integer selectProtocolByVid(String vid);
}

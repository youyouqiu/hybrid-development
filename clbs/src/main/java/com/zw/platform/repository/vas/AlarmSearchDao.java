package com.zw.platform.repository.vas;

import com.zw.platform.domain.vas.alram.AlarmParameter;
import com.zw.platform.domain.vas.alram.AlarmSetting;
import com.zw.platform.domain.vas.alram.AlarmType;
import com.zw.platform.domain.vas.alram.query.AlarmConfig;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface AlarmSearchDao {
    public List<AlarmType> getAlarmType(@Param("type") String type);

    /**
     * 获得调度报警类型
     * @return List<AlarmType>
     */
    List<AlarmType> getDispatchAlarmType();

    public AlarmType getAlarmName(@Param("pos") String pos);

    List<AlarmSetting> findSpeedParameter(String vehicleId);

    List<AlarmConfig> getAlarmConfig(String vehicleId);

    List<AlarmType> getAlarm808();

    List<AlarmParameter> findAlarmParametByName(String alarmName);

    List<AlarmParameter> findAlarmParameterByNameAndType(@Param("alarmName") String alarmName,
        @Param("type") String type);

    AlarmType getAlarmTypeById(@Param("alarmTypeId") String alarmTypeId);

    List<AlarmType> findAllAlarmType();

    List<AlarmType> getAlarmTypeByProtocolType(@Param("protocolTypes") List<Integer> protocolTypes);

    List<Map<String, Object>> getAdasEventMap();

    AlarmType getRoadSpeed();

    List<Map<String, String>> findPosEventCommonNameMap();

    /**
     * 查询所有报警信息，暂存内存中，方便后续使用
     * @return
     */
    List<String> getAllAlarmTypes();
}

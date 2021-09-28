package com.zw.platform.util.common;

import com.alibaba.fastjson.JSONObject;
import com.zw.app.domain.alarm.AlarmSetTypeAndAlarmTypeMappingRelationEnum;
import com.zw.platform.domain.vas.alram.AlarmParameterDetailsDTO;
import com.zw.platform.domain.vas.alram.AlarmParameterSettingDTO;
import com.zw.platform.domain.vas.alram.form.AlarmParameterSettingForm;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author penghj
 * @version 1.0
 * @date 2021/5/6 15:16
 */
public class AlarmParameterUtil {

    public static AlarmParameterDetailsDTO assemblePageDisplayData(
        Map<String, List<AlarmParameterSettingForm>> alarmParameterSettingMap) {
        AlarmParameterDetailsDTO alarmParameterDetailsDTO = new AlarmParameterDetailsDTO();
        // 交通部JT/T808 -> 预警
        List<AlarmParameterSettingForm> alertAlarmSettings = alarmParameterSettingMap.get("alert");
        alarmParameterDetailsDTO.setAlertList(assembleBasicAlarmSetting(alertAlarmSettings));
        // 交通部JT/T808 -> 驾驶员引起报警
        List<AlarmParameterSettingForm> driverAlarmSettings = alarmParameterSettingMap.get("driverAlarm");
        alarmParameterDetailsDTO.setDriverAlarmList(assembleBasicAlarmSetting(driverAlarmSettings));
        // 交通部JT/T808 -> 车辆报警
        List<AlarmParameterSettingForm> vehicleAlarmSettings = alarmParameterSettingMap.get("vehicleAlarm");
        alarmParameterDetailsDTO.setVehicleAlarmList(assembleBasicAlarmSetting(vehicleAlarmSettings));
        // 交通部JT/T808 -> 故障报警
        List<AlarmParameterSettingForm> faultAlarmSettings = alarmParameterSettingMap.get("faultAlarm");
        alarmParameterDetailsDTO.setFaultAlarmList(assembleBasicAlarmSetting(faultAlarmSettings));
        // 交通部JT/T808 -> F3高精度报警
        List<AlarmParameterSettingForm> highPrecisionAlarmSettings = alarmParameterSettingMap.get("highPrecisionAlarm");
        alarmParameterDetailsDTO.setHighPrecisionAlarmList(assembleBasicAlarmSetting(highPrecisionAlarmSettings));
        // 交通部JT/T808 -> F3传感器报警
        List<AlarmParameterSettingForm> sensorAlarmSettings = alarmParameterSettingMap.get("sensorAlarm");
        alarmParameterDetailsDTO.setSensorAlarmList(assembleBasicAlarmSetting(sensorAlarmSettings));
        // 交通部JT/T808 -> 平台报警
        List<AlarmParameterSettingForm> platAlarmSettings = alarmParameterSettingMap.get("platAlarm");
        alarmParameterDetailsDTO.setPlatAlarmList(assembleBasicAlarmSetting(platAlarmSettings));
        //  BDTD-SM -> BDTD-SM
        List<AlarmParameterSettingForm> peopleAlarmSettings = alarmParameterSettingMap.get("peopleAlarm");
        alarmParameterDetailsDTO.setPeopleAlarmList(assembleBasicAlarmSetting(peopleAlarmSettings));
        // BDTD-SM -> 平台报警
        List<AlarmParameterSettingForm> peoplePlatAlarmSettings = alarmParameterSettingMap.get("peoplePlatAlarm");
        alarmParameterDetailsDTO.setPeoplePlatAlarmList(assembleBasicAlarmSetting(peoplePlatAlarmSettings));
        // ASO -> ASO
        List<AlarmParameterSettingForm> asolongAlarmSettings = alarmParameterSettingMap.get("asolongAlarm");
        alarmParameterDetailsDTO.setAsolongAlarmList(assembleBasicAlarmSetting(asolongAlarmSettings));
        // ASO -> 平台报警
        List<AlarmParameterSettingForm> asolongPlatAlarmSettings = alarmParameterSettingMap.get("asolongPlatAlarm");
        alarmParameterDetailsDTO.setAsolongPlatAlarmList(assembleBasicAlarmSetting(asolongPlatAlarmSettings));
        // F3超长待机 -> F3超长待机
        List<AlarmParameterSettingForm> f3longAlarmSettings = alarmParameterSettingMap.get("f3longAlarm");
        alarmParameterDetailsDTO.setF3longAlarmList(assembleBasicAlarmSetting(f3longAlarmSettings));
        // F3超长待机 -> 平台报警
        List<AlarmParameterSettingForm> f3longPlatAlarmSettings = alarmParameterSettingMap.get("f3longPlatAlarm");
        alarmParameterDetailsDTO.setF3longPlatAlarmList(assembleBasicAlarmSetting(f3longPlatAlarmSettings));
        return alarmParameterDetailsDTO;
    }

    private static List<AlarmParameterSettingDTO> assembleBasicAlarmSetting(
        List<AlarmParameterSettingForm> alarmSettings) {
        if (CollectionUtils.isEmpty(alarmSettings)) {
            return null;
        }
        // 重新组装过后返回给前端的报警参数设置
        Map<String, AlarmParameterSettingDTO> resultAlarmSettMap = new HashMap<>(16);
        // 数据库中查询出来的报警参数设置
        Map<String, List<AlarmParameterSettingForm>> alarmParamSettingMap =
            alarmSettings.stream().collect(Collectors.groupingBy(AlarmParameterSettingForm::getPos));
        for (Map.Entry<String, List<AlarmParameterSettingForm>> entry : alarmParamSettingMap.entrySet()) {
            String pos = entry.getKey();
            String alarmSettingType = AlarmSetTypeAndAlarmTypeMappingRelationEnum.getAlarmSettingTypeByAlarmType(pos);
            if (resultAlarmSettMap.containsKey(alarmSettingType)) {
                continue;
            }
            List<AlarmParameterSettingForm> alarmParamSettings = entry.getValue();
            AlarmParameterSettingForm first = alarmParamSettings.get(0);
            AlarmParameterSettingDTO alarmParameterSettingDTO = new AlarmParameterSettingDTO();
            alarmParameterSettingDTO.setAlarmSettingType(alarmSettingType);
            String alarmSettingName = AlarmSetTypeAndAlarmTypeMappingRelationEnum
                .getAlarmSettingNameByAlarmSettingType(alarmSettingType, first.getName());
            alarmParameterSettingDTO.setAlarmSettingName(alarmSettingName);
            alarmParameterSettingDTO.setAlarmPush(first.getAlarmPush());
            JSONObject parameterValueJsonObj = new JSONObject();
            for (AlarmParameterSettingForm alarmSetting : alarmParamSettings) {
                String parameterValue = alarmSetting.getParameterValue();
                if (StringUtils.isBlank(parameterValue)) {
                    continue;
                }
                parameterValueJsonObj.put(alarmSetting.getParamCode(), parameterValue);
            }
            if (!parameterValueJsonObj.isEmpty()) {
                alarmParameterSettingDTO.setParameterValue(parameterValueJsonObj);
            }
            resultAlarmSettMap.put(alarmSettingType, alarmParameterSettingDTO);
        }
        return new ArrayList<>(resultAlarmSettMap.values());
    }

}

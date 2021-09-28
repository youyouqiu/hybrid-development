package com.zw.platform.domain.vas.alram;

import lombok.Data;

import java.util.List;

/**
 * @author penghj
 * @version 1.0
 * @date 2021/4/29 10:36
 */
@Data
public class AlarmParameterDetailsDTO {
    /**
     * 交通部JT/T808 -> 预警
     */
    private List<AlarmParameterSettingDTO> alertList;
    /**
     * 交通部JT/T808 -> 驾驶员引起报警
     */
    private List<AlarmParameterSettingDTO> driverAlarmList;
    /**
     * 交通部JT/T808 -> 车辆报警
     */
    private List<AlarmParameterSettingDTO> vehicleAlarmList;
    /**
     * 交通部JT/T808 -> 故障报警
     */
    private List<AlarmParameterSettingDTO> faultAlarmList;
    /**
     * 交通部JT/T808 -> 终端io
     */
    private List<IoAlarmSettingDTO> deviceIoAlarmList;
    /**
     * 交通部JT/T808 -> io采集1
     */
    private List<IoAlarmSettingDTO> ioCollectionOneAlarmList;
    /**
     * 交通部JT/T808 -> oi采集2
     */
    private List<IoAlarmSettingDTO> ioCollectionTwoAlarmList;
    /**
     * 交通部JT/T808 -> F3高精度报警
     */
    private List<AlarmParameterSettingDTO> highPrecisionAlarmList;
    /**
     * 交通部JT/T808 -> F3传感器报警
     */
    private List<AlarmParameterSettingDTO> sensorAlarmList;
    /**
     * 交通部JT/T808 -> 平台报警
     */
    private List<AlarmParameterSettingDTO> platAlarmList;

    /**
     * BDTD-SM -> BDTD-SM
     */
    private List<AlarmParameterSettingDTO> peopleAlarmList;
    /**
     * BDTD-SM -> 平台报警
     */
    private List<AlarmParameterSettingDTO> peoplePlatAlarmList;

    /**
     * ASO -> ASO
     */
    private List<AlarmParameterSettingDTO> asolongAlarmList;
    /**
     * ASO -> 平台报警
     */
    private List<AlarmParameterSettingDTO> asolongPlatAlarmList;

    /**
     * F3超长待机 -> F3超长待机
     */
    private List<AlarmParameterSettingDTO> f3longAlarmList;
    /**
     * F3超长待机 -> 平台报警
     */
    private List<AlarmParameterSettingDTO> f3longPlatAlarmList;

}

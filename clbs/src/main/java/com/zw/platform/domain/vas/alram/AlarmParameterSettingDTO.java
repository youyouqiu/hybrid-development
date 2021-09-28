package com.zw.platform.domain.vas.alram;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author penghj
 * @version 1.0
 * @date 2021/4/29 10:26
 */
@Data
@NoArgsConstructor
public class AlarmParameterSettingDTO {
    /**
     * 报警设置类型
     */
    private String alarmSettingType;
    /**
     * 报警设置名称
     */
    private String alarmSettingName;
    /**
     * 报警推送方式
     */
    private Integer alarmPush;
    /**
     * 参数值 {"param1":"1","param2":"2"}
     */
    private JSONObject parameterValue;

    public AlarmParameterSettingDTO(IoAlarmSettingDTO ioAlarmSettingDTO) {
        this.alarmSettingType = ioAlarmSettingDTO.getAlarmSettingType();
        this.alarmSettingName = ioAlarmSettingDTO.getAlarmSettingName();
        this.alarmPush = ioAlarmSettingDTO.getAlarmPush();
        this.parameterValue = ioAlarmSettingDTO.getParameterValue();
    }
}

package com.zw.platform.domain.vas.alram;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

/**
 * @author penghj
 * @version 1.0
 * @date 2021/4/30 16:17
 */
@Data
public class IoAlarmSettingDTO {
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
     * 状态1
     */
    private String stateOne;
    /**
     * 状态2
     */
    private String stateTwo;
    /**
     * 高电平对应的状态
     */
    private Integer highSignalType;
    /**
     * 低电平对应的状态
     */
    private Integer lowSignalType;
    /**
     * 参数值 {"param1":"1","param2":"2"}
     */
    private JSONObject parameterValue;
}

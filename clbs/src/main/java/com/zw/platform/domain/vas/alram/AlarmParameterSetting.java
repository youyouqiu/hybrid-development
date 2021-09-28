package com.zw.platform.domain.vas.alram;

import com.zw.platform.commons.SystemHelper;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * 报警参数值
 *
 * @author Zhang Yanhui
 * @since 2019/10/21 17:12
 */

@Data
public class AlarmParameterSetting implements Serializable {

    private static final long serialVersionUID = -5966813560488164057L;

    private String id;

    private String vehicleId;

    private String alarmParameterId;

    private String parameterValue;

    private Integer alarmPush;

    private Integer ignore;

    private Integer flag;

    private Date createDataTime;

    private String createDataUsername;

    private Date updateDataTime;

    private String updateDataUsername;

    /**
     *  是否可下发
     */
    private String sendFlag;

    public static AlarmParameterSetting of(String vehicleId, String alarmParameterId, String parameterValue,
        Integer alarmPush) {
        AlarmParameterSetting setting = new AlarmParameterSetting();
        setting.setId(UUID.randomUUID().toString());
        setting.setVehicleId(vehicleId);
        setting.setAlarmParameterId(alarmParameterId);
        setting.setParameterValue(parameterValue);
        setting.setAlarmPush(alarmPush);
        setting.setIgnore(0);
        setting.setFlag(1);
        setting.setCreateDataTime(new Date());
        setting.setCreateDataUsername(SystemHelper.getCurrentUsername());
        return setting;
    }
}

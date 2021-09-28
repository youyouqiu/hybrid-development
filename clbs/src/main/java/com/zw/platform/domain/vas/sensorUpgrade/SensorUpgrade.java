package com.zw.platform.domain.vas.sensorUpgrade;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;

import java.util.Date;


/**
 * 传感器升级实体类
 */
@Data
public class SensorUpgrade extends BaseFormBean {
    private String vehicleId;

    private String sensorId; // 传感器id

    private Date sensorUpgradeDate; // 最近升级日期

    private Integer sensorUpgradeStatus; // 升级状态

    private String deviceType = "1";
}

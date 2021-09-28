package com.zw.platform.domain.vas.alram;

import lombok.Data;


/**
 * 报警参数信息
 * @author Administrator
 */
@Data
public class AlarmParameter {

    private String id;

    private String paramCode;

    private String alarmTypeId;

    private String alarmType;

    private String alarmTypeName;

    private String type;

    private String defaultValue;

    private String ioMonitorId;

    private String vehicleId;

    /**
     * 监控对象类型
     */
    private String monitorType;

    /**
     *  是否可下发
     */
    private String sendFlag;
}

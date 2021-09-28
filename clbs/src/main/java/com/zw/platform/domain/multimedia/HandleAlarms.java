package com.zw.platform.domain.multimedia;

import lombok.Data;

@Data
public class HandleAlarms {

    /**
     * 主键ID
     */
    private String id;

    /**
     * 监控对象id
     */
    private String vehicleId;

    /**
     * 车牌号
     */
    private String plateNumber;

    /**
     * 报警标识字符串
     */
    private String alarm;

    /**
     * 报警类型
     */
    private String handleType;

    /**
     * 报警开始时间
     */
    private String startTime;

    /**
     * 报警开始时间 yyyyMMddHHmmssSSS
     */
    private String alarmStartTimeStr;

    /**
     * 报警结束时间
     */
    private String endTime;

    /**
     * 终端号
     */
    private String device;

    /**
     * SIM卡号
     */
    private String simcard;

    private Integer sno;

    private Integer webType;

    /**
     * 备注
     */
    private String remark;

    /**
     * 报警类型描述
     */
    private String description;

    /**
     * 下发监听：电话号码
     */
    private String monitorPhone;

    /**
     * 川冀标主动安全报警event_id
     */
    private String riskEventId;

    /**
     * 川冀标主动安全报警risk_id
     */
    private String riskId;

    /**
     * 主干4.1.1新增报表处理短信内容
     */
    private String dealOfMsg;

    /**
     * 判断是否是联动策略自动处理
     */
    private Integer isAutoDeal = 0;

    /**
     * 判断是否是只处理主动安全报警（实时监控处理adas）
     */
    private Integer isAdas = 0;
}

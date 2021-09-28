package com.zw.platform.push.cache;

/**
 * 下发参数之后通用应答的模块名称
 * @author zhengjc
 * @since 2019/12/3 9:59
 * @version 1.0
 **/
public enum SendModule {
    ACTIVE_SECURITY("主动安全参数设置"),
    ALARM_PARAMETER_SETTING("报警参数设置"),
    DIRECTIVE_PARAMETER("监控管理指令参数"),
    ELECTRONIC_FENCE("实时监控电子围栏"),
    FORWARD_AND_BACKWARD("正反转管理设置"),
    FUEL_CONSUMPTION("油耗管理设置"),
    HUMIDITY_MONITORING("湿度监测设置"),
    LOAD("载重管理设置"), OIL("油量管理设置"),
    MILEAGE_MONITORING("里程监测设置"),
    OBD("OBD管理设置"),
    PERIPHERALS_POLLING("传感器配置外设轮询"),
    TEMPERATURE_MONITORING("温度监测设置"),
    TIME("工时管理设置"),
    TIRE_PRESSURE_MONITORING("胎压监测设置"),
    SEND_TXT("发送文本信息");
    String name;

    SendModule(String name) {
        this.name = name;
    }
}
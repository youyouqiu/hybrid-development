package com.zw.adas.constant;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 设备故障类型
 *
 * @author zhangjuan
 */
public enum FaultTypeEnum {
    /**
     * 设备故障类型
     */
    MAIN_MEMORY_EXCEPTION(0, 0x00, "主存储器异常"),
    SPARE_MEMORY_EXCEPTION(1, 0x01, "备用存储器异常"),
    SATELLITE_SIGNAL_EXCEPTION(2, 0x02, "卫星信号异常"),
    COMMUNICATION_SIGNAL_EXCEPTION(3, 0x03, "通信信号异常"),
    SPARE_BATTERY_UNDER_VOLTAGE(4, 0x04, "备用电池欠压"),
    SPARE_BATTERY_IS_INVALID(5, 0x05, "备用电池失效"),
    IC_QUALIFICATION_CERTIFICATE_MODULE_ERROR(6, 0x06, "IC卡从业资格证模块故障");
    private static Map<Integer, FaultTypeEnum> codeNameMap = new HashMap<>();

    static {
        for (FaultTypeEnum faultTypeEnum : FaultTypeEnum.values()) {
            codeNameMap.put(faultTypeEnum.getCode(), faultTypeEnum);
        }
    }

    @Getter
    private Integer code;

    @Getter
    private Integer hexCode;

    @Getter
    private String name;

    FaultTypeEnum(Integer code, Integer hexCode, String name) {
        this.code = code;
        this.name = name;
        this.hexCode = hexCode;
    }

    public static String getNameByCode(Integer code) {
        FaultTypeEnum faultType = codeNameMap.get(code);
        return faultType == null ? null : faultType.getName();
    }

    public static Integer getHexCodeByCode(Integer code) {
        FaultTypeEnum faultType = codeNameMap.get(code);
        return faultType == null ? null : faultType.getHexCode();
    }
}

package com.zw.platform.basic.constant;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 监控对象枚举类型
 * @author zhangjuan
 */
public enum MonitorTypeEnum {
    /**
     * 监控对象类型：0：车 1：人 2：物
     */
    VEHICLE("0", "车", "vehicleSkin", "vehicle"),
    PEOPLE("1", "人", "peopleSkin", "people"),
    THING("2", "物", "thingSkin",  "thing");
    private static Map<String, MonitorTypeEnum> typeNameMap = new HashMap<>(16);
    private static Map<String, MonitorTypeEnum> nameTypeMap = new HashMap<>(16);
    private static Map<String, MonitorTypeEnum> enNameTypeMap = new HashMap<>(16);

    MonitorTypeEnum(String type, String typeName, String iconSkin, String enName) {
        this.type = type;
        this.typeName = typeName;
        this.iconSkin = iconSkin;
        this.enName = enName;
    }

    @Getter
    private String type;

    @Getter
    private String typeName;

    @Getter
    private String iconSkin;

    @Getter
    private String enName;

    static {
        MonitorTypeEnum[] values = MonitorTypeEnum.values();
        for (MonitorTypeEnum monitorType : values) {
            typeNameMap.put(monitorType.getType(), monitorType);
            nameTypeMap.put(monitorType.getTypeName(), monitorType);
            enNameTypeMap.put(monitorType.getEnName(), monitorType);
        }
    }

    public static String getTypeByName(String name) {
        MonitorTypeEnum monitorTypeEnum = nameTypeMap.get(name);
        return monitorTypeEnum == null ? null : monitorTypeEnum.getType();
    }

    public static String getNameByType(String type) {
        MonitorTypeEnum monitorTypeEnum = typeNameMap.get(type);
        return monitorTypeEnum == null ? null : monitorTypeEnum.getTypeName();
    }

    public static String getEnNameByType(String type) {
        MonitorTypeEnum monitorTypeEnum = typeNameMap.get(type);
        return monitorTypeEnum == null ? null : monitorTypeEnum.getEnName();
    }

    public static String getTypeByEnName(String enName) {
        MonitorTypeEnum monitorTypeEnum = enNameTypeMap.get(enName);
        return monitorTypeEnum == null ? null : monitorTypeEnum.getType();
    }

    public static MonitorTypeEnum getByType(String type) {
        return typeNameMap.get(type);
    }

}

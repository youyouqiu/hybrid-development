package com.zw.adas.domain.enums;

public enum AdasRiskEventEnum {
    CRASH("crash", "碰撞危险"),
    EXCEPTION("exception", "违规异常"),
    DISTRACTION("distraction", "注意力分散"),
    TIRED("tired", "疑似疲劳"),
    ACUTE("acute", "激烈驾驶");

    private String eventType;

    private String name;

    AdasRiskEventEnum(String eventType, String name) {
        this.eventType = eventType;
        this.name = name;
    }

    public static String getTypeByName(String name) {
        if (name != null) {
            for (AdasRiskEventEnum event : AdasRiskEventEnum.values()) {
                if (event.getName().equals(name)) {
                    return event.getEventType();
                }
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public String getEventType() {
        return eventType;
    }
}

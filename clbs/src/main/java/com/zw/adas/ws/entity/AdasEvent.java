package com.zw.adas.ws.entity;

public enum AdasEvent {

    VEHICLE_COLLISION("6401", "驾驶员不在驾驶位置"),
    DEVIATE("6402", "车道偏离"),
    DISTANCE("6403", "车距过近"),
    PEDESTRIAN_COLLISION("6404", "行人碰撞"),
    LANE_CHANGE("6405", "频繁变道"),
    // OBSTACLE("6407", "障碍物"),
    QUICK("6408", "急加/急减/急转弯"),
    SPEED_LIMIT("6409", "道路标识超限"),
    ROAD_MARKING("6410", "道路标识识别"),
    PICKUP("6502", "接打电话"),
    SMOKING("6503", "抽烟"),
    CLOS_EEYES("6506", "闭眼"),
    YAWN("6507", "打哈欠"),
    POSTURE("6508", "长时间不目视前方"),
    IDENT("6509", "人证不符"),
    CHECK_IDENT("6510", "未检测到驾驶员"),
    KEEP_OUT("6511", "遮挡报警"),
    INFRARED_BLOCKING("6512", "红外阻断报警");


    private String eventId;

    private String name;

    AdasEvent(String eventId, String name) {
        this.name = name;
        this.eventId = eventId;
    }

    @Override
    public String toString() {
        return this.eventId;
    }

    public boolean eq(String eventId) {
        return this.eventId.equals(eventId);

    }

}

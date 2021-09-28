package com.zw.adas.domain.leardboard;

public enum EVENT {

    SMOKE(6503, "抽烟", "distraction"), PHONE(6502, "接打手持电话", "distraction"),

    LEFT_OFFSET(64021, "车道左偏离", "crash"), RIGHT_OFFSET(64022, "车道右偏离", "crash"),

    CHANGE_LANES(6405, "频繁变道", "crash"), EYE_CLOSE(6506, "闭眼", "tired"),

    YAWNING(6507, "打哈欠", "tired"), DISTANCE(6403, "车距过近", "crash"),

    PEDESTRIAN_COLLISIONS(6404, "行人碰撞", "crash"), ACCELERATE(64081, "急加速", "crash"),

    SLOW_DOWN(64082, "急减速", "crash"), TURN(64083, "急转弯", "crash"),

    OBSTACLES(6407, "障碍物", "crash"), ABNORMAL_POSTURE(6508, "长时间不目视前方", "abnormal"),

    INCONFORMITY_CERTIFICATE(6509, "人证不符", "abnormal"), NO_DRIVER_DETECTED(6510, "驾驶员不在驾驶位置", "abnormal"),

    VEHICLE_CRASH(6401, "前向碰撞", "crash"), ROAD_MARK_TRANSFINITE(6409, "道路标识超限", "crash"),

    KEEP_OUT(6511, "遮挡", "abnormal"), INFRARED_BLOCKING(6512, "红外阻断", "abnormal");

    private int eventCode;

    private String eventName;

    private String eventType;

    EVENT(int eventCode, String eventName, String eventType) {
        this.eventCode = eventCode;
        this.eventName = eventName;
        this.eventType = eventType;
    }

    public int getEventCode() {
        return this.eventCode;
    }

    public String getEventName() {
        return this.eventName;
    }

    public String getEventType() {
        return this.eventType;
    }

    @Override
    public String toString() {
        return eventCode + "";
    }

}

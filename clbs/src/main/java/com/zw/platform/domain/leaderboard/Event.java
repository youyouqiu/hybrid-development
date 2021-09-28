package com.zw.platform.domain.leaderboard;

/**
 * @author Administrator
 */

public enum Event {

    SMOKE(6503, "抽烟"), PHONE(6502, "接打手持电话"),

    LEFT_OFFSET(64021, "车道左偏离"), RIGHT_OFFSET(64022, "车道右偏离"),

    CHANGE_LANES(6405, "频繁变道"), EYE_CLOSE(6506, "闭眼"),

    YAWNING(6507, "打哈欠"), DISTANCE(6403, "车距过近"),

    PEDESTRIAN_COLLISIONS(6404, "行人碰撞"), ACCELERATE(64081, "急加速"),

    SLOW_DOWN(64082, "急减速"), TURN(64083, "急转弯"),

    ABNORMAL_POSTURE(6508, "长时间不目视前方"),

    INCONFORMITY_CERTIFICATE(6509, "人证不符"), NO_DRIVER_DETECTED(6510, "驾驶员不在驾驶位置"),

    VEHICLE_CRASH(6401, "前向碰撞"), ROAD_MARK_TRANSFINITE(6409, "道路标识超限"),
    KEEP_OUT(6511, "遮挡"), INFRARED_BLOCKING(6512, "红外阻断");

    private int eventCode;

    private String eventName;

    Event(int eventCode, String eventName) {
        this.eventCode = eventCode;
        this.eventName = eventName;

    }

    public int getEventCode() {
        return this.eventCode;
    }

    public String getEventName() {
        return this.eventName;
    }

    @Override
    public String toString() {
        return eventCode + "";
    }

}

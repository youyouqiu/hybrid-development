package com.zw.adas.domain.enums;

import com.zw.adas.domain.common.AdasRiskType;

/***
 @Author zhengjc
 @Date 2019/7/9 15:31
 @Description 风险事件排行专有枚举
 @version 1.0


 TIRED("疑似疲劳", "1"), DISTRACTION("注意力分散", "2"), EXCEPTION("违规异常", "3"), CRASH("碰撞危险", "4"), CLUSTER("组合风险",
 "5"), IntenseDriving("激烈驾驶", "6"), BLANK("", "");
 **/
public enum AdasEventRankEn {
    VEHICLE_CRASH("前向碰撞", AdasRiskType.CRASH),

    LEFT_OFFSET("车道左偏离报警", AdasRiskType.CRASH),

    RIGHT_OFFSET("车道右偏离报警", AdasRiskType.CRASH),

    DISTANCE("车距过近", AdasRiskType.CRASH),

    PEDESTRIAN_COLLISIONS("行人碰撞", AdasRiskType.CRASH),

    CHANGE_LANES("频繁变道", AdasRiskType.CRASH),

    OBSTACLES("障碍物", AdasRiskType.CRASH),

    ACCELERATE("急加速", AdasRiskType.CRASH),

    SLOW_DOWN("急减速", AdasRiskType.CRASH),

    TURN("急转弯", AdasRiskType.CRASH),

    ROAD_MARK_TRANSFINITE("道路标识超限", AdasRiskType.CRASH),

    PHONE("接打手持电话", AdasRiskType.DISTRACTION),

    SMOKE("抽烟", AdasRiskType.DISTRACTION),

    EYE_CLOSE("闭眼", AdasRiskType.TIRED),

    YAWNING("打哈欠", AdasRiskType.TIRED),

    ABNORMAL_POSTURE("长时间不目视前方", AdasRiskType.TIRED),

    DISTRACTED_DRIVING("分神驾驶", AdasRiskType.DISTRACTION),

    IN_CONFORMITY_CERTIFICATE("人证不符", AdasRiskType.EXCEPTION),

    NO_DRIVER_DETECTED("驾驶员不在驾驶位置", AdasRiskType.EXCEPTION),

    KEEP_OUT("遮挡", AdasRiskType.EXCEPTION),

    INFRARED_BLOCKING("红外阻断", AdasRiskType.EXCEPTION),

    FATIGUE_DRIVING("疲劳驾驶", AdasRiskType.TIRED),

    DRIVER_BEHAVIOR_MONITOR_FAILURE("驾驶员行为监测功能失效", AdasRiskType.EXCEPTION),

    DRIVER_EXCEPTION("驾驶员异常", AdasRiskType.EXCEPTION),

    OFF_WHEEL("双手同时脱离方向盘", AdasRiskType.EXCEPTION),

    NOT_WEARING_SEAT_BELT("未系安全带报警", AdasRiskType.EXCEPTION),

    ASSIST_FAILURE("驾驶辅助功能失效", AdasRiskType.EXCEPTION),

    IDLE_SPEED("怠速", AdasRiskType.IntenseDriving),

    ABNORMAL_FLAME_OUT("异常熄火", AdasRiskType.IntenseDriving), NEUTRAL_TAXIING("空挡滑行", AdasRiskType.IntenseDriving),

    ENGINE_OVERDRIVE("发动机超转", AdasRiskType.IntenseDriving), TIMEOUT_DRIVING("超时驾驶", AdasRiskType.BLANK), QUICK_CROSSING(
        "路口快速通过", AdasRiskType.CRASH), HIGH_TIRE_PRESSURE("胎压过高", AdasRiskType.CRASH), LOW_TIRE_PRESSURE("胎压过低",
        AdasRiskType.CRASH), HIGH_TIRE_TEMPERATURE("胎温过高", AdasRiskType.CRASH), SENSOR_ANOMALY("传感器异常",
        AdasRiskType.CRASH), IMBALANCE_TIRE_PRESSURE("胎压不平衡", AdasRiskType.CRASH), SLOW_LEAK("慢漏气",
        AdasRiskType.CRASH), LOW_BATTERY("电池电量低", AdasRiskType.CRASH), CLOSE_BEHIND("后方接近",
        AdasRiskType.CRASH), LEFT_REAR_APPROACH("左侧后方接近", AdasRiskType.CRASH), RIGHT_REAR_APPROACH("右侧后方接近",
        AdasRiskType.CRASH), LEFT_BLIND_ALERT("左侧盲区预警", AdasRiskType.CRASH), RIGHT_BLIND_ALERT("右侧盲区预警",
        AdasRiskType.CRASH);

    AdasEventRankEn(String eventName) {
        this.eventName = eventName;

    }

    AdasEventRankEn(String eventName, AdasRiskType riskTYpe) {
        this.eventName = eventName;
        this.riskType = riskTYpe;

    }

    private String eventName;

    private AdasRiskType riskType;

    public String getEventName() {
        return this.eventName;
    }

    public String getRiskType() {
        return this.riskType.getRiskType();
    }

    public static void main(String[] args) {
        System.out.println(AdasEventRankEn.values().length);
    }

}

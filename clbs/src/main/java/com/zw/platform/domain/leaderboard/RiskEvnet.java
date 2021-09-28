package com.zw.platform.domain.leaderboard;

import lombok.Data;


@Data
public class RiskEvnet {
    int riskTotal;//风险总数
    //事件
    int eventPhone;//接打电话

    String phoneRingRatio;

    int eventException;//异常报警

    String exceptionRingRatio;

    int eventVehicleCrash;//车辆碰撞危险

    String crashRingRatio;

    int eventVehicleDistance;//车距过近

    String distanceRingRatio;

    int eventDistraction;//分心报警

    String distractionRingRatio;

    int eventVehicleOffset;//车道偏移

    String offsetRingRatio;

    int eventYawning;//打哈欠

    String yawningRingRatio;

    int eventEyeClose;//闭眼

    String eyeCloseRingRatio;

    int eventSmoke;//抽烟

    String smokeRingRatio;

    int eventBowHead;//低头

    String bowHeadRingRatio;

    int eventObstacles;//障碍物

    String obstaclesRingRatio;

    int eventHitPeople;//行人碰撞

    String hitPeopleRingRatio;

    int eventFrequentOffset;//频繁变道

    String frequentOffsetRingRatio;

    //808预警事件
    int alarmOverspeed;//808超速预警

    String overspeedRingRatio;

    int alarmOvertime;//808超速预警

    String overtimeRingRatio;

    int alarmCrash;//808碰撞预警

    String alarmCrashRingRatio;

    //风险
    int rtypeDistraction;//分心驾驶

    String rtypeDistractionRingRatio;

    int rtypeException;//异常报警

    String rtypeExceptionRingRatio;

    int rtypeCrash;//碰撞危险

    String rtypeCrashRingRatio;

    int rtypeTired;//疲劳驾驶

    String rtypeTiredRingRatio;

    int rtypeCluster;//集束风险

    String rtypeClusterRingRatio;

    //风险等级
    int specialSerious;//特重

    String specialSeriousRingRatio;

    int serious;//严重

    String seriousRingRatio;

    int heavier;//较重

    String heavierRingRatio;

    int general;//一般

    String generalRingRatio;

}

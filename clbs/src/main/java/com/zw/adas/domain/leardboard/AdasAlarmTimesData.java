package com.zw.adas.domain.leardboard;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class AdasAlarmTimesData {

    private String time;//当前时刻

    private String timeStr;//当前完整时间

    private int smoke;//风险事件抽烟

    private int phone;//风险时间接打手持电话

    private int leftOffset;//风险事件车道左偏移

    private int rightOffset;//风险事件车道右偏移

    private int changeLanes;//风险事件频繁变道

    private int eyeClose;//风险事件闭眼

    private int yawning;//风险事件打哈欠

    private int distance;//风险事件车距过近

    private int accelerate;//风险事件急加速

    private int slowDown;//风险事件急减速

    private int turn;//风险事件急转弯

    private int obstacles;//风险事件障碍物

    private int abnormalPosture;//风险事件长时间不目视前方

    private int inConformityCertificate;//风险事件人证不符

    private int noDriverDetected;//风险事件驾驶员不在驾驶位置

    private int vehicleCrash;//前向碰撞

    private int roadMarkTransfinite;//道路标识超限

    private int keepOut;//遮挡

    private int infraredBlocking;//红外阻挡

    /**
     * 道路标识识别
     */
    private int roadIdentification;
    /**
     * 疲劳驾驶
     */
    private int fatigueDriving;
    /**
     * 驾驶员行为监测功能失效
     */
    private int driverBehaviorMonitorFailure;
    /**
     * 驾驶员变更
     */
    private int driverChange;
    /**
     * 双手同时脱离方向盘
     */
    private int offWheel;
    /**
     * 未系安全带报警
     */
    private int notWearingSeatBelt;
    /**
     * 驾驶辅助功能失效
     */
    private int assistFailure;
    /**
     * 怠速
     */
    private int idleSpeed;
    /**
     * 异常熄火
     */
    private int abnormalFlameOut;
    /**
     * 空挡滑行
     */
    private int neutralTaxiing;
    /**
     * 发动机超转
     */
    private int engineOverdrive;
    /**
     * 分神驾驶
     */
    private int distractedDriving;
    /**
     * 超时驾驶
     */
    private int timeoutDriving;
    /**
     * 路口快速通过
     */
    private int quickCrossing;
    /**
     * 胎压过高
     */
    private int highTirePressure;
    /**
     * 胎压过低
     */
    private int lowTirePressure;
    /**
     * 胎温过高
     */
    private int highTireTemperature;
    /**
     * 传感器异常
     */
    private int sensorAnomaly;
    /**
     * 胎压不平衡
     */
    private int imbalanceTirePressure;
    /**
     * 慢漏气
     */
    private int slowLeak;
    /**
     * 电池电量低
     */
    private int lowBattery;
    /**
     * 后方接近
     */
    private int closeBehind;
    /**
     * 左侧后方接近
     */
    private int leftRearApproach;
    /**
     * 右侧后方接近
     */
    private int rightRearApproach;
    /**
     * 左侧盲区预警
     */
    private int leftBlindAlert;
    /**
     * 右侧盲区预警
     */
    private int rightBlindAlert;
    /**
     * 驾驶员异常
     */
    private int driverException;
    /**
     * 行人碰撞
     */
    private int pedestrianCollisions;
    /**
     * 沪标报警(按规定上下客报警)
     */
    private int abormalLoad;

    /**
     * 沪标报警(超员报警)
     */
    private int overMan;
    /**
     * 外设状态异常报警（4.2.3中位标准新增）
     */
    private int peripheralStateException;
    /**
     * 路网超速（4.2.3中位标准新增）
     */
    private int networkSpeed;
    /**
     * 京标车道偏离报警
     */
    private int vehicleOffset;
    /**
     * 京标盲区监测报警
     */
    private int blindSpotMonitoring;


    /**
     * 黑标(4.3.5)标盲区超速报警
     */
    private int overSpeed;
    /**
     * 黑标(4.3.5)路线偏离报警
     */
    private int lineOffset;
    /**
     * 黑标(4.3.5)禁行报警
     */
    private int forbid;
    /**
     * 黑标(4.3.5)设备异常
     */
    private int equipmentAbnormal;
    /**
     * 黑标(4.3.5)车道左偏离预警
     */
    private int leftOffsetWarning;
    /**
     * 黑标(4.3.5)车道右偏离预警
     */
    private int rightOffsetWarning;
    /**
     * 黑标(4.3.6)鲁湘粤新增其他
     */
    private int other;

    /**
     * 实线变道 (4.3.6)鲁湘粤补充
     */
    private int solidLineChange;

    private Map<String, Integer> orgRiskList = new HashMap<>();

}

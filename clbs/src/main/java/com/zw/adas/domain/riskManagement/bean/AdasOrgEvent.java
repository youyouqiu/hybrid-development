package com.zw.adas.domain.riskManagement.bean;

import com.zw.platform.util.Reflections;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 功能描述:
 * @author zhengjc
 * @date 2019/7/9
 * @time 15:01
 */
@Data
public class AdasOrgEvent implements Serializable {
    /**
     * （时间）
     */
    private long time;

    /**
     * 驾驶员不在驾驶位置
     */
    private int vehicleCrash;

    /**
     * 风险事件车道左偏移
     */
    private int leftOffset;
    /**
     * 风险事件车道右偏移
     */
    private int rightOffset;
    /**
     * 风险事件车距过近
     */
    private int distance;
    /**
     * 风险事件行人碰撞
     */
    private int pedestrianCollisions;

    /**
     * 风险事件频繁变道
     */
    private int changeLanes;
    /**
     * 风险事件障碍物
     */
    private int obstacles;
    /**
     * 风险事件急加速
     */
    private int accelerate;
    /**
     * 风险事件急减速
     */
    private int slowDown;

    /**
     * 风险事件急转弯
     */
    private int turn;

    /**
     * 道路标识超限
     */
    private int roadMarkTransfinite;

    /**
     * 风险时间接打电话
     */
    private int phone;

    /**
     * 风险事件抽烟
     */
    private int smoke;

    /**
     * 风险事件闭眼
     */
    private int eyeClose;

    /**
     * 风险事件打哈欠
     */
    private int yawning;

    /**
     * 风险事件长时间不目视前方
     */
    private int abnormalPosture;

    /**
     * 分神驾驶
     */
    private int distractedDriving;

    /**
     * 风险事件人证不符
     */
    private int inConformityCertificate;

    /**
     * 风险事件驾驶员不在驾驶位置
     */
    private int noDriverDetected;

    /**
     * 红外遮挡
     */
    private int keepOut;

    /**
     * 红外阻断
     */
    private int infraredBlocking;

    /**
     * 疲劳驾驶
     */
    private int fatigueDriving;

    /**
     * 驾驶员行为监测功能失效
     */
    private int driverBehaviorMonitorFailure;

    /**
     * 驾驶员异常
     */
    private int driverException;

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
     * 超时驾驶（4.1.2新增）
     */
    private int timeoutDriving;

    /**
     * 路口快速通过（4.1.2新增）
     */
    private int quickCrossing;

    /**
     * 胎压过高（4.1.2新增）
     */
    private int highTirePressure;

    /**
     * 胎压过低（4.1.2新增）
     */
    private int lowTirePressure;
    /**
     * 胎温过高（4.1.2新增）
     */
    private int highTireTemperature;
    /**
     * 传感器异常（4.1.2新增）
     */
    private int sensorAnomaly;
    /**
     * 胎压不平衡（4.1.2新增）
     */
    private int imbalanceTirePressure;
    /**
     * 慢漏气（4.1.2新增）
     */
    private int slowLeak;
    /**
     * 电池电量低（4.1.2新增）
     */
    private int lowBattery;
    /**
     * 后方接近（4.1.2新增）
     */
    private int closeBehind;
    /**
     * 左侧后方接近（4.1.2新增）
     */
    private int leftRearApproach;
    /**
     * 右侧后方接近（4.1.2新增）
     */
    private int rightRearApproach;
    /**
     * 左侧盲区预警（4.1.2新增）
     */
    private int leftBlindAlert;
    /**
     * 右侧盲区预警（4.1.2新增）
     */
    private int rightBlindAlert;
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
     * (4.3.6)鲁湘粤新增其他
     */
    private int other;

    /**
     * 实线变道 (4.3.6)鲁湘粤补充
     */
    private int solidLineChange;

    public List<Map<String, String>> getOrgEventList(Map<String, String> allCommonEvent) {

        List<Map<String, String>> result = new ArrayList<>();
        for (Map.Entry<String, String> entry : allCommonEvent.entrySet()) {
            Map<String, String> data = new HashMap<>();
            data.put("name", entry.getKey());
            data.put("value", Reflections.getFieldValue(this, entry.getValue()).toString());
            result.add(data);
        }
        return result;
    }

}

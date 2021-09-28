package com.zw.adas.domain.riskStatistics.bean;

import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.domain.basicinfo.enums.PlateColor;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;

@Data
public class AdasStatisticsListBean {

    private String vehicleId;
    /**
     * 监控对象
     */
    @ExcelField(title = "监控对象")
    private String brand;

    /**
     * 车辆颜色
     */
    @ExcelField(title = "车牌颜色")
    private String plateColor;

    /**
     * 所属企业
     */
    @ExcelField(title = "所属企业")
    private String orgName;
    /**
     * 疲劳驾驶
     */
    @ExcelField(title = "疲劳驾驶")
    private int fatigueDriving;

    /**
     * 接打手持电话
     */
    @ExcelField(title = "接打手持电话")
    private int phone;

    /**
     * 闭眼
     */
    @ExcelField(title = "闭眼")
    private int eyeClose;

    /**
     * 打哈欠
     */
    @ExcelField(title = "打哈欠")
    private int yawning;

    /**
     * 抽烟
     */
    @ExcelField(title = "抽烟")
    private int smoke;

    /**
     * 频繁变道
     */
    @ExcelField(title = "频繁变道")
    private int changeLanes;

    /**
     * 车道偏离（京标）
     */
    @ExcelField(title = "车道偏离")
    private int vehicleOffset;
    /**
     * 车道左偏离
     */
    @ExcelField(title = "车道左偏离")
    private int leftOffset;
    /**
     * 车道右偏离
     */
    @ExcelField(title = "车道右偏离")
    private int rightOffset;

    /**
     * 前向碰撞
     */
    @ExcelField(title = "前向碰撞")
    private int vehicleCrash;

    /**
     * 车距过近
     */
    @ExcelField(title = "车距过近")
    private int distance;

    /**
     * 行人碰撞
     */
    @ExcelField(title = "行人碰撞")
    private int pedestrianCollisions;

    /**
     * 分神驾驶
     */
    @ExcelField(title = "分神驾驶")
    private int distractedDriving;

    /**
     * 驾驶员异常
     */
    @ExcelField(title = "驾驶员异常")
    private int driverException;

    /**
     * 长时间不目视前方
     */
    @ExcelField(title = "长时间不目视前方")
    private int abnormalPosture;

    /**
     * 障碍物
     */
    @ExcelField(title = "障碍物")
    private int obstacles;

    /**
     * 路网超速（4.2.3中位标准新增）
     */
    @ExcelField(title = "路网超速")
    private int networkSpeed;

    /**
     * 道路标识超限
     */
    @ExcelField(title = "道路标识超限")
    private int roadMarkTransfinite;
    /**
     * 人证不符
     */
    @ExcelField(title = "人证不符")
    private int inConformityCertificate;

    /**
     * 超时驾驶（4.1.2新增）
     */
    @ExcelField(title = "超时驾驶")
    private int timeoutDriving;

    /**
     * 沪标报警(不按规定上下客)
     */
    @ExcelField(title = "沪标报警")
    private int abormalLoad;

    /**
     * 沪标报警(超员)
     */
    @ExcelField(title = "沪标报警")
    private int overMan;

    /**
     * 红外阻断
     */
    @ExcelField(title = "红外阻断")
    private int infraredBlocking;

    /**
     * 遮挡
     */
    @ExcelField(title = "遮挡")
    private int keepOut;

    /**
     * 驾驶员不在驾驶位置
     */
    @ExcelField(title = "驾驶员不在驾驶位置")
    private int noDriverDetected;

    /**
     * 双手同时脱离方向盘
     */
    @ExcelField(title = "双手同时脱离方向盘")
    private int offWheel;

    /**
     * 急转弯
     */
    @ExcelField(title = "急转弯")
    private int turn;

    /**
     * 急加速
     */
    @ExcelField(title = "急加速")
    private int accelerate;
    /**
     * 急减速
     */
    @ExcelField(title = "急减速")
    private int slowDown;

    /**
     * 路口快速通过（4.1.2新增）
     */
    @ExcelField(title = "路口快速通过")
    private int quickCrossing;

    /**
     * 未系安全带
     */
    @ExcelField(title = "未系安全带")
    private int notWearingSeatBelt;
    /**
     * 左侧盲区预警（4.1.2新增）
     */
    @ExcelField(title = "左侧盲区预警")
    private int leftBlindAlert;
    /**
     * 右侧盲区预警（4.1.2新增）
     */
    @ExcelField(title = "右侧盲区预警")
    private int rightBlindAlert;

    /**
     * 左侧后方接近（4.1.2新增）
     */
    @ExcelField(title = "左侧后方接近")
    private int leftRearApproach;
    /**
     * 右侧后方接近（4.1.2新增）
     */
    @ExcelField(title = "右侧后方接近")
    private int rightRearApproach;
    /**
     * 后方接近（4.1.2新增）
     */
    @ExcelField(title = "后方接近")
    private int closeBehind;

    /**
     * 胎压不平衡（4.1.2新增）
     */
    @ExcelField(title = "胎压不平衡")
    private int imbalanceTirePressure;

    /**
     * 慢漏气（4.1.2新增）
     */
    @ExcelField(title = "慢漏气")
    private int slowLeak;

    /**
     * 胎压过高（4.1.2新增）
     */
    @ExcelField(title = "胎压过高")
    private int highTirePressure;

    /**
     * 胎压过低（4.1.2新增）
     */
    @ExcelField(title = "胎压过低")
    private int lowTirePressure;
    /**
     * 胎温过高（4.1.2新增）
     */
    @ExcelField(title = "胎温过高")
    private int highTireTemperature;

    /**
     * 空挡滑行
     */
    @ExcelField(title = "空挡滑行")
    private int neutralTaxiing;

    /**
     * 发动机超转
     */
    @ExcelField(title = "发动机超转")
    private int engineOverdrive;

    /**
     * 怠速
     */
    @ExcelField(title = "怠速")
    private int idleSpeed;

    /**
     * 异常熄火
     */
    @ExcelField(title = "异常熄火")
    private int abnormalFlameOut;

    /**
     * 电池电量低报警（4.1.2新增）
     */
    @ExcelField(title = "电池电量低报警")
    private int lowBattery;
    /**
     * 传感器异常（4.1.2新增）
     */
    @ExcelField(title = "传感器异常")
    private int sensorAnomaly;

    /**
     * 驾驶员辅助功能失效
     */
    @ExcelField(title = "驾驶员辅助功能失效")
    private int assistFailure;

    /**
     * 驾驶员行为监测功能失效
     */
    @ExcelField(title = "驾驶员行为监测功能失效")
    private int driverBehaviorMonitorFailure;

    /**
     * 外设状态异常报警（4.2.3中位标准新增）
     */
    @ExcelField(title = "外设状态异常报警")
    private int peripheralStateException;
    /**
     * 京标盲区监测报警
     */
    @ExcelField(title = "盲区监测")
    private int blindSpotMonitoring;
    /**
     * 黑标(4.3.5)超速报警
     */
    @ExcelField(title = "超速报警")
    private int overSpeed;
    /**
     * 黑标(4.3.5)路线偏离报警
     */
    @ExcelField(title = "路线偏离报警")
    private int lineOffset;
    /**
     * 黑标(4.3.5)禁行报警
     */
    @ExcelField(title = "禁行报警")
    private int forbid;
    /**
     * 黑标(4.3.5)设备异常
     */
    @ExcelField(title = "设备异常")
    private int equipmentAbnormal;
    /**
     * 黑标(4.3.5)车道左偏离预警
     */
    @ExcelField(title = "车道左偏离预警")
    private int leftOffsetWarning;
    /**
     * 黑标(4.3.5)车道右偏离预警
     */
    @ExcelField(title = "车道右偏离预警")
    private int rightOffsetWarning;

    /**
     * (4.3.6)鲁湘粤新增其他
     */
    @ExcelField(title = "其他")
    private int other;

    /**
     * 合计
     */
    @ExcelField(title = "合计")
    private long total;

    public void initData(BindDTO bindDTO) {
        brand = bindDTO.getName();
        orgName = bindDTO.getOrgName();
        plateColor = PlateColor.getNameOrBlankByCode(bindDTO.getPlateColor());
    }
}

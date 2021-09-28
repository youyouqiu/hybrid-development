package com.zw.platform.domain.statistic;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * @author zhouzongbo on 2019/1/3 15:11
 */
@Data
public class TravelBaseInfo implements Serializable {
    private static final long serialVersionUID = 4534210029394043907L;

    /**
     * 行驶
     */
    public static final int STATUS_TRAVEL = 0;
    /**
     * 停止
     */
    public static final int STATUS_STOP = 1;

    /**
     * 行驶转换为停止次数
     */
    public static final int TRAVEL_CHANGE_STOP_MAX_TIMES = 5;

    /**
     * 停止转换为行驶次数
     */
    public static final int STOP_CHANGE_TRAVEL_TIMES = 3;

    /**
     * 行驶与停止速度区分
     */
    public static final double DISTINGUISH_SPEED = 5.0;

    public static final boolean IS_APP_SEARCH = false;

    public TravelBaseInfo() {
    }

    public TravelBaseInfo(Double speed, Boolean isTravelStatus, Integer timeDifference, Boolean flogKey,
        Long lastVTime) {
        this.speed = speed;
        this.preTime = lastVTime * 1000;
        this.lastVTime = lastVTime * 1000;
        this.milliSecondDifference = timeDifference;
        this.flogKey = flogKey;
        this.isTravelStatus = isTravelStatus;
        // 设置初始状态
        initStatus();
    }

    private void initStatus() {
        if (this.isTravelStatus) {
            // 计算行驶报表:
            // 1.第一条位置数据速度小于5km,则第一条位置数据状态为"停止".
            // 2.第一条位置数据速度大于5km,则第一条位置数据状态为"行驶".
            // 3. 假设 0: 行驶, 1: 停止. 模拟行驶状态: 0 0 1 1 1 1 1 ,第一条位置数据为行驶状态,
            // 但是连续行驶次数小于3次, 通过firstPositionStatus = true, 计算开始位置为行驶状态，但是连续行驶次数小于3次的数据（仅计算一次）
            if (checkIsStopFlag(this.speed)) {
                this.status = STATUS_STOP;
                this.firstPositionStatus = false;
            } else {
                this.status = STATUS_TRAVEL;
                this.firstPositionStatus = true;
            }
        } else {
            // 1.计算停止报表: 第一条位置数据速度小于5km,则第一条位置数据为停止
            // 2. 假设 0: 行驶, 1: 停止. 模拟行驶状态: 1 1 0 0 0 0 1,第一条位置数据为“停止”状态,
            // 但是连续“停止”次数小于5次, 通过firstPositionStatus = true, 计算开始位置为“停止”状态，但是连续行驶次数小于5次的数据（仅计算一次）
            if (checkIsStopFlag(this.speed)) {
                this.status = STATUS_STOP;
                this.firstPositionStatus = true;
            } else {
                this.status = STATUS_TRAVEL;
                this.firstPositionStatus = false;
            }
        }
    }

    /**
     * 速度
     */
    private Double speed;

    /**
     * 连续"行驶"or"停止"的位置条数
     */
    private Integer countPosition = 0;
    /**
     * 开始时间
     */
    private Long firstVTime = 0L;
    /**
     * 结束时间
     */
    private Long lastVTime = 0L;
    /**
     * 最新一条位置数据里程
     */
    private Double lastGpsMile = 0.0;
    /**
     * 上一条位置数据的行驶里程
     */
    private Double preTravelMile = 0.0;

    /**
     * 主油箱最大油耗
     */
    private Double minTotalOilWearOne = 0.0;
    /**
     * 主油箱最小油耗
     */
    private Double maxTotalOilWearOne = 0.0;
    /**
     * 连续的位置都是行驶状态
     */
    private Integer travelCountTimes = 0;
    /**
     * 连续的位置都是停止状态
     */
    private Integer stopCountTimes = 0;
    /**
     * 上一个状态: 0: 行驶; 1: 停止
     */
    private Integer status;

    /**
     * 第一条位置数据状态
     * 行驶报表: true: 行驶, false: 停止
     * 停止报表: true: 停止, false: 行驶
     */
    private Boolean firstPositionStatus;

    private String endLocation;

    /**
     * 上一条位置的数据的时间
     * 1.如果两条位置的时间差大于5分钟, 结束当前行驶状态, 则使用前一个状态的最后一个条位置数据时间 - 前一个状态的第一条位置数据时间
     */
    private Long preTime;

    /**
     * 两点间的时间差
     */
    private Boolean twoPointTimeDifferenceFlag;

    /**
     * 两点间的时间间隔 5分钟
     */
    private Integer milliSecondDifference = 5 * 60 * 1000;

    /**
     * 上一条位置数据 gpsMile or 里程传感器mile
     */
    // private Double preGpsMile;

    private Boolean flogKey;

    /**
     * true: 行驶统计报表; false: 停止统计报表
     */
    private Boolean isTravelStatus;

    /**
     * 重置
     * @param info info
     * @return
     */
    public static void initTravelBaseInfo(TravelBaseInfo info) {
        info.setCountPosition(0);
        info.setFirstVTime(0L);
        info.setLastVTime(0L);
        info.setLastGpsMile(0.0);
        info.setMinTotalOilWearOne(0.0);
        info.setMaxTotalOilWearOne(0.0);
        info.setTravelCountTimes(0);
        info.setStopCountTimes(0);
    }

    /**
     * 速度小于5: true: 停止; false: 行驶
     * @return
     */
    private boolean checkIsStopFlag() {
        return this.speed <= DISTINGUISH_SPEED;
    }

    public static boolean checkIsTravelFlag(Double speed) {
        return speed > DISTINGUISH_SPEED;
    }

    public static boolean checkIsStopFlag(Double speed) {
        return speed <= DISTINGUISH_SPEED;
    }

    /**
     * 计算两点之间的时间差; 如果大于5分钟, 则结束当前状态, 重新开始计算状态
     * @param currentTime 后一条位置数据的gpsTime
     */
    public void setTimeAndMileDifferenceFlag(Long currentTime, Double lastGpsMile) {
        this.twoPointTimeDifferenceFlag = (currentTime - this.preTime > milliSecondDifference);
        // 更新前一个点的时间
        if (!this.twoPointTimeDifferenceFlag) {
            this.lastVTime = currentTime;
            // gpsMile可能为空
            if (Objects.nonNull(lastGpsMile)) {
                this.lastGpsMile = new BigDecimal(lastGpsMile).setScale(1, RoundingMode.HALF_UP).doubleValue();
            }
        }
    }

    /**
     * 获取某个状态段的行驶时长
     * @return 行驶时长
     */
    public Long getTravelTimeByTwoPointTimeDifferenceFlag() {
        // if (twoPointTimeDifferenceFlag) {
        //     // 如果两个点的时间差大于5分钟, 则用当前状态段的最后一个点 - 当前状态段的第一个点的时间
        //     return this.lastVTime - this.firstVTime;
        // } else {
        // 如果两个点的时间差小于等于5分钟, 则用当前状态段的最后一个点 - 当前状态段的第一个点的时间
        return this.lastVTime - this.firstVTime;
        // }
    }
}
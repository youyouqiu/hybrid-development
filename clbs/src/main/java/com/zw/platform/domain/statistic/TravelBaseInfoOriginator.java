package com.zw.platform.domain.statistic;


/**
 * 备忘录: 状态存储
 *
 * @author zhouzongbo on 2019/1/4 9:55
 */
public class TravelBaseInfoOriginator {

    private TravelBaseInfo travelBaseInfo;

    /**
     * 创建备忘录对象
     * 由于计算上一个状态的时间和里程 = 这个状态的第一条位置数据 - 上一个状态的第一条位置数据
     * (所以travelBaseInfo中的countPosition - 1 等于某个状态实际连续行驶or停止次数)
     * @return TravelBaseInfoCaretaker
     */
    public TravelBaseInfo createTravelBaseInfo() {
        TravelBaseInfo travelBaseInfo = new TravelBaseInfo();
        travelBaseInfo.setCountPosition(this.travelBaseInfo.getCountPosition() - 1);
        travelBaseInfo.setFirstVTime(this.travelBaseInfo.getFirstVTime());
        travelBaseInfo.setLastVTime(this.travelBaseInfo.getLastVTime());
        travelBaseInfo.setLastGpsMile(this.travelBaseInfo.getLastGpsMile());
        travelBaseInfo.setPreTravelMile(this.travelBaseInfo.getPreTravelMile());
        travelBaseInfo.setMinTotalOilWearOne(this.travelBaseInfo.getMinTotalOilWearOne());
        travelBaseInfo.setMaxTotalOilWearOne(this.travelBaseInfo.getMaxTotalOilWearOne());
        travelBaseInfo.setTravelCountTimes(this.travelBaseInfo.getTravelCountTimes());
        travelBaseInfo.setStopCountTimes(this.travelBaseInfo.getStopCountTimes());
        travelBaseInfo.setStatus(this.travelBaseInfo.getStatus());
        travelBaseInfo.setSpeed(this.travelBaseInfo.getSpeed());
        travelBaseInfo.setFirstPositionStatus(this.travelBaseInfo.getFirstPositionStatus());
        travelBaseInfo.setEndLocation(this.travelBaseInfo.getEndLocation());
        return travelBaseInfo;
    }

    /**
     * 恢复之前存储的对象
     */
    public void restoreTravelBaseInfo(TravelBaseInfo travelBaseInfo) {
        this.travelBaseInfo = travelBaseInfo;
    }

    public void setTravelBaseInfo(TravelBaseInfo travelBaseInfo) {
        this.travelBaseInfo = travelBaseInfo;
    }

    public TravelBaseInfo getTravelBaseInfo() {
        return travelBaseInfo;
    }
}

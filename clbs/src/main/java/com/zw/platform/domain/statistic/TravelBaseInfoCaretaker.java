package com.zw.platform.domain.statistic;

/**
 * 备忘录: 状态存储
 * @author zhouzongbo on 2019/1/4 9:55
 */
public class TravelBaseInfoCaretaker {

    private TravelBaseInfo travelBaseInfo;

    public TravelBaseInfo retrieveTravelBaseInfo() {
        return this.travelBaseInfo;
    }

    public void saveTravelBaseInfo(TravelBaseInfo travelBaseInfo){
        this.travelBaseInfo = travelBaseInfo;
    }
}

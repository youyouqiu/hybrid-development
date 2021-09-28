package com.zw.adas.domain.driverStatistics.show;

import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.PrecisionUtils;
import lombok.Data;

import java.util.List;

/***
 @Author zhengjc
 @Date 2019/7/13 17:04
 @Description ic卡司机实时监控页面动态信息实体
 @version 1.0
 **/
@Data
public class AdasDriverInfoShow {

    /**
     * 车辆id
     */
    private String vehicleId;
    /**
     * 司机名称
     */

    private String driverName;

    /**
     * 从业资格证号
     */
    private String cardNumber;

    /**
     * 插卡时间
     */
    private String insertCardTime;

    /**
     * 拔卡时间
     */
    private String removeCardTime;

    /**
     * 单次行驶时长
     */
    private String todayLastTravelTime;

    /**
     * 单次行驶里程
     */
    private double todayLastTravelMile;

    private String todayLastTravelMileStr;
    /**
     * 当天行驶时长
     */
    private String todayTravelTime;

    /**
     * 当天行驶里程
     */
    private double todayTravelMile;
    private String todayTravelMileStr;

    public static AdasDriverInfoShow assembleData(List<AdasDriverStatisticsShow> driverStatistics, String vehicleId) {
        AdasDriverInfoShow adasDriverInfoShow = new AdasDriverInfoShow();
        AdasDriverStatisticsShow adss = null;
        long todayTravelTime = 0;
        double todayTravelMile = 0.0;
        for (AdasDriverStatisticsShow driverStatistic : driverStatistics) {
            //查找该车最后一条插拔卡记录信息
            if (adss == null && driverStatistic.getMonitorId().equals(vehicleId)) {
                adss = driverStatistic;
            }
            todayTravelMile += driverStatistic.getTodayTravelMile();
            todayTravelTime += driverStatistic.getTodayTravelTime();
        }
        initThisTimeData(adasDriverInfoShow, adss);
        adasDriverInfoShow.todayTravelMile = todayTravelMile;
        adasDriverInfoShow.todayTravelMileStr = PrecisionUtils.roundByScale(todayTravelMile, 1);
        adasDriverInfoShow.todayTravelTime = DateUtil.formatTime(todayTravelTime);
        return adasDriverInfoShow;
    }

    private static void initThisTimeData(AdasDriverInfoShow adasDriverInfoShow, AdasDriverStatisticsShow adss) {
        adasDriverInfoShow.driverName = adss.getDriverName();
        adasDriverInfoShow.cardNumber = adss.getCardNumber();
        adasDriverInfoShow.insertCardTime = adss.getInsertCardTime();
        adasDriverInfoShow.removeCardTime = adss.getRemoveCardTime();
        adasDriverInfoShow.todayLastTravelMile = adss.getTodayLastTravelMile();
        adasDriverInfoShow.todayLastTravelMileStr = PrecisionUtils.roundByScale(adss.getTodayLastTravelMile(), 1);
        adasDriverInfoShow.todayLastTravelTime = DateUtil.formatTime(adss.getTodayLastTravelTime());
    }

}

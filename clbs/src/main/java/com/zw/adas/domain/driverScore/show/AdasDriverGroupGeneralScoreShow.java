package com.zw.adas.domain.driverScore.show;

import lombok.Data;

/***
 @Author zhengjc
 @Date 2019/10/14 19:50
 @Description 企业司机综合评分信息
 @version 1.0
 **/
@Data
public class AdasDriverGroupGeneralScoreShow {

    /**
     * 企业得分
     */
    private Double score;

    /**
     * 得分环比
     */
    private Double scoreRingRatio;
    /**
     * 司机总数
     */
    private int totalDriver;

    /**
     * 不良行为次数
     */
    private int badBehavior;

    /**
     * 不良行为次数环比
     */
    private Double badBehaviorRingRatio;

    /**
     * 最大的得分范围(0代表0-20 2地表21-40 4代表41-60 6代表61-80 8代表81-100)
     */
    private Integer maxScoreRange;

    /**
     * 日均行驶时长
     */
    private String dayOfDriverTime;

    /**
     * 百公里不良行为次数
     */
    private Double hundredMileBadBehavior;

    /**
     * 百公里不良行为占比
     */
    private Double hundredMileBadBehaviorRingRatio;

    /**
     * 平均速度
     */
    private Double averageSpeed;

    /**
     * 清醒度
     */
    private Double lucidity;

    /**
     * 警惕性
     */
    private Double vigilance;
    /**
     * 专注度
     */
    private Double focus;

    /**
     * 自觉性
     */
    private Double consciousness;
    /**
     * 平稳性
     */
    private Double stationarity;

    public static AdasDriverGroupGeneralScoreShow getInstanceShow() {
        AdasDriverGroupGeneralScoreShow show = new AdasDriverGroupGeneralScoreShow();
        show.score = 88.0;
        show.scoreRingRatio = 20.0;
        show.totalDriver = 100;
        show.badBehavior = 10;
        show.badBehaviorRingRatio = 20.0;
        show.maxScoreRange = 2;
        show.dayOfDriverTime = "3小时50分52秒";
        show.hundredMileBadBehavior = 20.0;
        show.hundredMileBadBehaviorRingRatio = 30.0;
        show.averageSpeed = 30.20;
        show.lucidity = 20.0;
        show.vigilance = 20.0;
        show.focus = 20.0;
        show.consciousness = 20.0;
        show.stationarity = 20.0;
        return show;
    }
}

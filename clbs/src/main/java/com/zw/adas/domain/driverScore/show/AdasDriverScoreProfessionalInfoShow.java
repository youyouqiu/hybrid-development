package com.zw.adas.domain.driverScore.show;

import com.zw.adas.domain.driverStatistics.show.AdasProfessionalShow;
import lombok.Data;

import java.util.List;
import java.util.Map;

/***
 @Author zhengjc
 @Date 2019/10/15 10:50
 @Description 司机从业人员弹出框
 @version 1.0
 **/
@Data
public class AdasDriverScoreProfessionalInfoShow {
    /**
     * (综合得分)
     */
    private Double score;

    /**
     * (得分环比)
     */
    private Double scoreRingRatio;

    /**
     * (不良行为次数)
     */
    private int badBehavior;

    /**
     * (不良行为次数环比)
     */
    private Double badBehaviorRingRatio;

    /**
     * (百公里不良行为次)
     */
    private Double hundredMileBadBehavior;

    /**
     * (百公里不良行为次数环比)
     */
    private Double hundredMileBadBehaviorRingRatio;

    /**
     * (行驶里程)
     */
    private Double driverMile;

    /**
     * （日均行驶时长）
     */
    private String dayOfDriverTime;

    /**
     * （清醒度0）
     */
    private Double lucidity;

    /**
     * （警惕性1）
     */
    private Double vigilance;

    /**
     * （专注度2）
     */
    private Double focus;

    /**
     * (自觉性3)
     */
    private Double consciousness;

    /**
     * （平稳性4）
     */
    private Double stationarity;

    /**
     * 返回的各个报警事件的报警数
     */
    private List<Map<String, String>> eventInfos;

    private transient String eventInfoStr;

    private String groupId;

    private String cardNumberName;

    /**
     * 得分范围
     */
    private transient Integer scoreRange;
    /**
     * 插卡司机基础信息
     */
    private AdasProfessionalShow adasProfessionalShow;

    /**
     * 驾驶员企业id
     */
    private String driverGroupId;

}

package com.zw.adas.domain.driverScore.show;

import com.zw.adas.domain.driverStatistics.show.AdasProfessionalShow;
import com.zw.adas.utils.AdasCommonHelper;
import com.zw.platform.util.common.PrecisionUtils;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
 @Author zhengjc
 @Date 2019/10/15 10:50
 @Description 司机从业人员弹出框
 @version 1.0
 **/
@Data
public class AdasDriverScoreProfessionalInfoExport {
    /**
     * (综合得分)
     */
    private String score;

    /**
     * (得分环比)
     */
    private String scoreRingRatio;

    /**
     * (不良行为次数)
     */
    private String badBehavior;

    /**
     * (百公里不良行为次)
     */
    private String hundredMileBadBehavior;

    /**
     * (不良行为次数环比)
     */
    private String badBehaviorRingRatio;

    /**
     * (百公里不良行为次数环比)
     */
    private String hundredMileBadBehaviorRingRatio;

    /**
     * (行驶里程)
     */
    private String driverMile;

    /**
     * （日均行驶时长）
     */
    private String dayOfDriverTime;

    /**
     * （清醒度）
     */
    private String lucidity;

    /**
     * （警惕性）
     */
    private String vigilance;

    /**
     * （专注度）
     */
    private Double focus;

    /**
     * (自觉性)
     */
    private String consciousness;

    /**
     * （平稳性）
     */
    private String stationarity;

    /**
     * 返回的各个报警事件的报警数
     */
    private List<Map<String, String>> eventInfos;

    private transient String eventInfoStr;

    /**
     * 插卡司机综合得分和基础信息
     */
    private AdasProfessionalShow professional;

    /**
     * 导出的事件详情表
     */
    private List<AdasDriverScoreEventShow> eventShows = new ArrayList<>();

    /**
     * 评语
     */
    private String comments;

    /**
     * 驾驶评语
     */
    private String travelComments;

    /**
     * 不良行为评语
     */
    private String badBehaviorComments;

    public static AdasDriverScoreProfessionalInfoExport getInstance(List<AdasDriverScoreEventShow> eventShows,
        AdasDriverScoreProfessionalInfoShow dspi, AdasCommonHelper adasCommonHelper) {
        AdasDriverScoreProfessionalInfoExport export = new AdasDriverScoreProfessionalInfoExport();

        if (CollectionUtils.isNotEmpty(eventShows)) {
            int number = 0;
            for (AdasDriverScoreEventShow event : eventShows) {
                event.setNumber(++number);
            }
            export.eventShows = eventShows;
        }
        if (CollectionUtils.isNotEmpty(dspi.getEventInfos())) {
            int number = 0;
            for (Map<String, String> eventInfo : dspi.getEventInfos()) {
                eventInfo.put("number", (++number) + "");
                eventInfo.put("riskType", adasCommonHelper.getMergeRiskType(eventInfo.get("name")));
            }

        }
        export.eventInfos = dspi.getEventInfos();
        export.professional = dspi.getAdasProfessionalShow();
        export.score = PrecisionUtils.getNullOrHorizontalLine(dspi.getScore(), 0);
        export.badBehavior = dspi.getBadBehavior() + "";
        export.hundredMileBadBehavior = PrecisionUtils.getNullOrHorizontalLine(dspi.getHundredMileBadBehavior(), 2);
        export.badBehaviorRingRatio = PrecisionUtils.getNullOrHorizontalLine(dspi.getBadBehaviorRingRatio());
        export.hundredMileBadBehaviorRingRatio =
            PrecisionUtils.getNullOrHorizontalLine(dspi.getHundredMileBadBehaviorRingRatio());
        export.driverMile = PrecisionUtils.getNullOrHorizontalLine(dspi.getDriverMile(), 1);
        //评语
        export.comments = getCommentsVal(dspi.getScoreRange(), dspi.getScoreRingRatio(), adasCommonHelper);
        export.travelComments = getTravelCommentsVal(dspi, adasCommonHelper);
        export.badBehaviorComments = getBadBehaviorCommentsVal(dspi.getBadBehaviorRingRatio());
        //环比
        export.badBehaviorRingRatio = geRingRatioVal(dspi.getBadBehaviorRingRatio());
        export.hundredMileBadBehaviorRingRatio = geRingRatioVal(dspi.getHundredMileBadBehaviorRingRatio());
        export.dayOfDriverTime = dspi.getDayOfDriverTime();
        return export;

    }

    private static String geRingRatioVal(Double ringRatio) {
        if (ringRatio == null) {
            return "-";
        }
        if (ringRatio > 0) {
            return String.format("上升%s%%", PrecisionUtils.roundByScale(Math.abs(ringRatio), 2));
        } else {
            return String.format("下降%s%%", PrecisionUtils.roundByScale(Math.abs(ringRatio), 2));
        }
    }

    private static String getBadBehaviorCommentsVal(Double badBehaviorRingRatio) {
        if (badBehaviorRingRatio == null) {
            return "-";
        }
        if (badBehaviorRingRatio > 0) {
            return String.format("不良驾驶行为驾驶上升%s%%", PrecisionUtils.roundByScale(Math.abs(badBehaviorRingRatio), 2));
        } else {
            return String.format("不良驾驶行为驾驶下降%s%%", PrecisionUtils.roundByScale(Math.abs(badBehaviorRingRatio), 2));
        }
    }

    private static String getCommentsVal(Integer scoreRange, Double scoreRingRatio, AdasCommonHelper adasCommonHelper) {
        String templateStr;
        if (scoreRange == null || scoreRingRatio == null) {
            return "-";
        }
        if (scoreRingRatio == 0) {
            return adasCommonHelper.getComments(scoreRange + "_0");
        }
        if (scoreRingRatio > 0) {
            templateStr = adasCommonHelper.getComments(scoreRange + "_1");
        } else {
            templateStr = adasCommonHelper.getComments(scoreRange + "_-1");
        }
        return String.format(templateStr, PrecisionUtils.roundByScale(Math.abs(scoreRingRatio), 2));
    }

    private static void initTraitMap(Map<String, String> traitMap, String traitIndex, Double traitVal,
        List<Double> traitValues) {

        if (traitVal != null) {
            String traitMapVal = traitMap.get(traitVal.toString());
            if (traitMapVal != null) {
                traitMap.put(traitVal.doubleValue() + "", traitMapVal + "," + traitIndex);
            } else {
                traitMap.put(traitVal.doubleValue() + "", traitIndex);
            }
            traitValues.add(traitVal);
        }

    }

    private static String getTravelCommentsVal(AdasDriverScoreProfessionalInfoShow show,
        AdasCommonHelper adasCommonHelper) {
        StringBuilder sb = new StringBuilder();
        Map<String, String> traitMap = new HashMap<>();
        List<Double> traitValues = new ArrayList<>();

        if (show.getScoreRange() != null) {
            sb.append(adasCommonHelper.getComments(show.getScoreRange() + ""));
        }
        initTraitMap(traitMap, "0", show.getLucidity(), traitValues);
        initTraitMap(traitMap, "1", show.getVigilance(), traitValues);
        initTraitMap(traitMap, "2", show.getFocus(), traitValues);
        initTraitMap(traitMap, "3", show.getConsciousness(), traitValues);
        initTraitMap(traitMap, "4", show.getStationarity(), traitValues);
        traitValues.sort(Double::compareTo);

        if (CollectionUtils.isNotEmpty(traitValues)) {
            int dataSize = traitValues.size();
            if (dataSize == 1) {
                return sb.toString();
            }
            boolean maxEquals = traitValues.get(dataSize - 1).equals(traitValues.get(dataSize - 2));
            boolean minEquals = traitValues.get(0).equals(traitValues.get(1));

            if (maxEquals || minEquals) {
                return sb.toString();
            }

            double max = traitValues.get(dataSize - 1);
            double min = traitValues.get(0);
            //组装结果
            sb.append(adasCommonHelper.getTravelComments(traitMap.get(max + "") + "_1"));
            sb.append(adasCommonHelper.getTravelComments(traitMap.get(min + "") + "_-1"));

        }

        return sb.toString();
    }

}

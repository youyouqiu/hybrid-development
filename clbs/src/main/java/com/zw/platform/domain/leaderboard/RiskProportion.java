package com.zw.platform.domain.leaderboard;

import com.zw.platform.util.common.ComputingUtils;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class RiskProportion implements Serializable {
    /**
     * 风险类型
     */
    private int tired;//疲劳驾驶

    private int crash;//碰撞报警

    private int exception;//异常报警

    private int distraction;//分心驾驶

    private int cluster;//集束风险
    private int intenseDriving;//激烈驾驶风险

    /**
     * 各种类型占比
     */

    private String tiredProportion;//疲劳驾驶占比

    private String crashProportion;//碰撞报警占比

    private String exceptionProportion;//异常报警占比

    private String distractionProportion;//分心驾驶占比

    private String clusterProportion;//集束风险占比

    private String intenseDrivingProportion;//激烈驾驶风险占比

    /**
     * 各种类型环比
     */

    private String tiredRingRatio;//疲劳驾驶环比

    private String crashRingRatio;//碰撞报警环比

    private String exceptionRingRatio;//异常报警环比

    private String distractionRingRatio;//分心驾驶环比

    private String clusterRingRatio;//集束风险环比
    private String intenseDrivingRingRatio;//激烈驾驶风险环比

    /**
     * 风险等级
     */
    private int general;//风险等级一般

    private int heavier;//风险等级较重

    private int serious;//风险等级严重

    private int specialSerious;//风险等级特重

    /**
     * 各种等级占比
     */
    private String generalProportion;//风险等级一般占比

    private String heavierProportion;//风险等级较重占比

    private String seriousProportion;//风险等级严重占比

    private String specialSeriousProportion;//风险等级特重占比

    /**
     * 各种风险等级环比
     */
    private String generalRingRatio;//风险等级一般环比

    private String heavierRingRatio;//风险等级较重环比

    private String seriousRingRatio;//风险等级严重环比

    private String specialSeriousRingRatio;//风险等级特重环比

    /**
     * 公有属性
     */

    private int total;//报警总数

    private int time;//统计的时间

    /**
     * 计算占比(是百分比，并且保留两位小数)
     */
    public void calculateProportion() {
        //风险类型
        tiredProportion = ComputingUtils.calProportion(tired, total);
        crashProportion = ComputingUtils.calProportion(crash, total);
        exceptionProportion = ComputingUtils.calProportion(exception, total);
        distractionProportion = ComputingUtils.calProportion(distraction, total);
        clusterProportion = ComputingUtils.calProportion(cluster, total);
        intenseDrivingProportion = ComputingUtils.calProportion(intenseDriving, total);
        //风险等级
        generalProportion = ComputingUtils.calProportion(general, total);
        heavierProportion = ComputingUtils.calProportion(heavier, total);
        seriousProportion = ComputingUtils.calProportion(serious, total);
        specialSeriousProportion = ComputingUtils.calProportion(specialSerious, total);

    }

    /**
     * 计算环比(是百分比，并且保留两位小数)
     */
    public void calculateRingRatio(RiskProportion riskProportion) {
        if (riskProportion == null) {
            riskProportion = new RiskProportion();
        }
        //风险类型
        this.tiredRingRatio = ComputingUtils.calRingRatio(this.tired, riskProportion.tired);
        this.crashRingRatio = ComputingUtils.calRingRatio(this.crash, riskProportion.crash);
        this.exceptionRingRatio = ComputingUtils.calRingRatio(this.exception, riskProportion.exception);
        this.distractionRingRatio = ComputingUtils.calRingRatio(this.distraction, riskProportion.distraction);
        this.clusterRingRatio = ComputingUtils.calRingRatio(this.cluster, riskProportion.cluster);
        this.intenseDrivingRingRatio = ComputingUtils.calRingRatio(this.intenseDriving, riskProportion.intenseDriving);
        //风险等级
        this.generalRingRatio = ComputingUtils.calRingRatio(this.general, riskProportion.general);
        this.heavierRingRatio = ComputingUtils.calRingRatio(this.heavier, riskProportion.heavier);
        this.seriousRingRatio = ComputingUtils.calRingRatio(this.serious, riskProportion.serious);
        this.specialSeriousRingRatio = ComputingUtils.calRingRatio(this.specialSerious, riskProportion.specialSerious);
    }

    public List<Map<String, String>> getRiskProportionList(boolean isVip) {
        List<Map<String, String>> result = new ArrayList<>();
        /**
         * 设置风险类型
         */
        result.add((getTypeProportion(RiskType.TIRED, tiredProportion, tiredRingRatio, tired)));
        result.add((getTypeProportion(RiskType.CRASH, crashProportion, crashRingRatio, crash)));
        result.add((getTypeProportion(RiskType.EXCEPTION, exceptionProportion, exceptionRingRatio, exception)));
        result.add((getTypeProportion(RiskType.DISTRACTION, distractionProportion, distractionRingRatio, distraction)));
        result.add((getTypeProportion(RiskType.CLUSTER, clusterProportion, clusterRingRatio, cluster)));
        result.add((getTypeProportion(RiskType.INTENSE_DRIVING, intenseDrivingProportion, intenseDrivingRingRatio,
            intenseDriving)));
        /**
         * 设置风险等级
         */
        result.add((getLevelProportion(RISKLEVEL.GENERAL, generalProportion, generalRingRatio, general, isVip)));
        result.add((getLevelProportion(RISKLEVEL.HEAVIER, heavierProportion, heavierRingRatio, heavier, isVip)));
        result.add((getLevelProportion(RISKLEVEL.SERIOUS, seriousProportion, seriousRingRatio, serious, isVip)));
        result.add((getLevelProportion(RISKLEVEL.SPECIAL_SERIOUS, specialSeriousProportion, specialSeriousProportion,
            specialSerious, isVip)));
        return result;
    }

    private Map<String, String> getTypeProportion(RiskType riskType, String proportion, String ringRatio, long total) {
        Map<String, String> result = new HashMap<>();
        result.put("name", riskType.getType());
        result.put("proportion", proportion);
        result.put("ringRatio", ringRatio);
        result.put("total", total + "");
        return result;
    }

    private Map<String, String> getLevelProportion(RISKLEVEL risklevel, String proportion, String ringRatio, long total,
        boolean isVip) {
        Map<String, String> result = new HashMap<>();
        result.put("name", isVip ? risklevel.getLevel() : risklevel.getRange());
        result.put("proportion", proportion);
        result.put("ringRatio", ringRatio);
        result.put("total", total + "");
        return result;
    }

}

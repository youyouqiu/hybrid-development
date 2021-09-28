package com.zw.platform.util.common;


import java.text.DecimalFormat;


/**
 * 能耗工具类
 * @author lifudong
 */
public class EnergyUtil {

    /**
     * 计算co2排放量
     * @param fuel(油耗量,单位L)
     * @param fuelCategory(燃油类型)
     * @return co2(co2排放量,单位kg)
     */
    public static Double embodiedCarbon(Double fuel, String fuelCategory) {
        Double co2 = null;
        DecimalFormat df = new DecimalFormat("#.###");
        if (fuelCategory == null || "".equals(fuelCategory)) {
            fuelCategory = "柴油";
        }
        if (fuelCategory.contains("柴油")) {
            co2 = (fuel / 1163) * 3.0581 * 1000;
        } else if (fuelCategory.contains("汽油")) {
            co2 = (fuel / 1370) * 3.1507 * 1000;
        }
        if (co2 != null) {
            co2 = Double.parseDouble(df.format(co2));
        }
        return co2;
    }

    /**
     * 燃油量转换为标准煤
     * @param fuel(油耗量,单位L)
     * @param feulType(燃油类型)
     * @return coal(co2排放量,单位kg)
     */
    public static Double fuelToCoal(Double fuel, String feulType) {
        Double coal = null;
        if (feulType == null || "".equals(feulType)) {
            feulType = "柴油";
        }
        if (feulType.contains("柴油")) {
            coal = fuel / 1163 * 1.4571 * 1000;
        } else if (feulType.contains("汽油")) {
            coal = fuel / 1370 * 1.4714 * 1000;
        }
        return coal;
    }
}

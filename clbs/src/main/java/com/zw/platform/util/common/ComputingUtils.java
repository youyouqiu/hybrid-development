package com.zw.platform.util.common;

import java.math.RoundingMode;
import java.text.DecimalFormat;


public class ComputingUtils {
    /**
     * 计算占比(是百分比，并且保留两位小数)
     */
    public static String calProportion(int divisor, int dividend) {
        double result = dividend == 0 ? 0.00 : divisor * 1.0 / dividend * 100;
        return PrecisionUtils.roundByScale(result, 2);
    }

    /**
     * 计算占比(是百分比，并且保留两位小数)
     */
    public static String calProportion(double divisor, double dividend) {
        double result = dividend == 0.00 ? 0.00 : divisor / dividend * 100;
        return PrecisionUtils.roundByScale(result, 2);
    }

    /**
     * 计算环比(是百分比，并且保留两位小数)
     */
    public static String calRingRatio(double today, double yesterday) {
        double result = 0.00;
        if (yesterday == 0 && today == 0) {
            result = 0.00;
        } else if (yesterday == 0 && today > 0) {
            result = 1.0 * 100;
        } else if (yesterday == 0 && today < 0) {
            result = -1.0 * 100;
        } else {
            result = (today - yesterday) * 1.0 / yesterday * 100;
        }
        String number1 = PrecisionUtils.roundByScale(result, 2);
        double number2 = Double.parseDouble(number1);//類型轉換
        if (Math.round(number2) - number2 == 0) {
            return String.valueOf((long) number2);
        }
        return String.valueOf(number2);
    }

    /**
     * 计算环比  （up上升,down下降,same一样）
     *
     * @param today
     * @param yesterday
     * @return
     */
    public static String ringRatio(int today, int yesterday) {
        if (today - yesterday > 0) {
            return "up";
        } else if (today - yesterday < 0) {
            return "down";
        } else {
            return "same";
        }
    }

    /**
     * 保留两位小数
     *
     * @param number
     * @return
     */
    public static double numberDataDis(double number) {
        DecimalFormat decimalFormat = new DecimalFormat();// 数字格式化类 保留两位小数
        decimalFormat.setMaximumFractionDigits(2);
        decimalFormat.setGroupingSize(0);
        decimalFormat.setRoundingMode(RoundingMode.FLOOR);
        return Double.parseDouble(decimalFormat.format(number));
    }

    public static void main(String[] args) {
        System.err.println(calProportion(0, 0));
    }
}



package com.zw.platform.util.common;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class PrecisionUtils {

    /**
     * 用于进行格式化
     * @param v
     * @param scale
     * @return
     */
    public static String roundByScale(double v, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("The   scale   must   be   a   positive   integer   or   zero");
        }
        v = roundVal(v, scale);
        if (scale == 0) {
            return new DecimalFormat("0").format(v);
        }
        StringBuilder formatStr = new StringBuilder("0.");
        for (int i = 0; i < scale; i++) {
            formatStr.append("0");
        }
        return new DecimalFormat(formatStr.toString()).format(v);
    }

    /**
     * 用来进行四舍五入
     * @param v
     * @param scale
     * @return
     */
    public static Double roundVal(double v, int scale) {
        BigDecimal b = new BigDecimal(String.valueOf(v));
        return b.setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * 判断是否为null,是返回---，否则保留两位小数
     * @param value
     * @return
     */
    public static String getNullOrHorizontalLine(Double value) {
        if (value == null) {
            return "-";
        } else {
            return PrecisionUtils.roundByScale(value, 2);
        }
    }

    /**
     * 判断是否为null,是返回---，否则保留两位小数
     * @param value
     * @return
     */
    public static String getNullOrHorizontalLine(Double value, int scale) {
        if (value == null) {
            return "-";
        } else {
            return PrecisionUtils.roundByScale(value, scale);
        }
    }

    public static void main(String[] args) {
        double value = 60.525;
        int scale = 2;
        System.out.println(roundByScale(value, scale));
    }

}

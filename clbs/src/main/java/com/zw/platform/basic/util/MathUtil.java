package com.zw.platform.basic.util;

import java.math.BigDecimal;
import java.util.Objects;

public class MathUtil {

    /**
     * 两个double类型的数据相加
     * @param num1 num1
     * @param num2 num2
     * @return 相加结果
     */
    public static double add(Double num1, Double num2) {
        if (Objects.isNull(num1) && Objects.isNull(num2)) {
            return 0.0;
        }
        num1 = Objects.isNull(num1) ? 0.0 : num1;
        num2 = Objects.isNull(num2) ? 0.0 : num2;
        BigDecimal bigDecimal1 = new BigDecimal(String.valueOf(num1));
        BigDecimal bigDecimal2 = new BigDecimal(String.valueOf(num2));
        return bigDecimal1.add(bigDecimal2).doubleValue();
    }
}

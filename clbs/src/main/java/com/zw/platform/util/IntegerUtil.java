package com.zw.platform.util;

/**
 * @Author: zjc
 * @Description:
 * @Date: create in 2020/12/10 14:03
 */
public class IntegerUtil {

    /**
     * @param val        传进来的参数
     * @param errorVal   不正确的值
     * @param defaultVal 默认值
     * @return
     */
    public static Integer getOrDefault(Integer val, Integer errorVal, Integer defaultVal) {
        if (val == null || val.equals(errorVal)) {
            return defaultVal;
        }
        return val;
    }

    /**
     * @param val        传进来的参数
     * @param defaultVal 默认值
     * @return
     */
    public static Integer getOrDefault(String val, Integer defaultVal) {
        if (val == null) {
            return defaultVal;
        }
        return Integer.valueOf(val);
    }

    /**
     * 获取主动安全胎压监测模块的值
     * @param val
     * @return
     */
    public static Integer getTireVal(Integer val) {
        return getOrDefault(val, -1, 0XFFFF);
    }

    /**
     * 获取主动安全盲区监测模块的值
     * @param val
     * @return
     */
    public static Integer getBlindVal(Integer val) {
        return getOrDefault(val, -1, 0XFF);
    }


}

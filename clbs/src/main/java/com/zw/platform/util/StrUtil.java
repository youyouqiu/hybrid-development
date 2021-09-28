package com.zw.platform.util;

import java.util.Optional;

/***
 @Author zhengjc
 @Date 2019/6/5 13:51
 @Description 字符串工具类
 @version 1.0
 **/
public class StrUtil {
    /**
     * 只要有一个为空就不满足条件
     * @param checkDatas
     * @return
     */
    public static boolean areNotBlank(String... checkDatas) {
        boolean result = true;
        for (String checkData : checkDatas) {
            result = isNotBlank(checkData);
            if (result == false) {
                break;
            }

        }
        return result;
    }

    /**
     * 只要一个不为空就满足条件
     * @param checkDatas
     * @return
     */
    public static boolean moreOneNotBlank(String... checkDatas) {
        boolean result = false;
        for (String checkData : checkDatas) {
            result = isNotBlank(checkData);
            if (result == true) {
                break;
            }

        }
        return result;
    }

    public static boolean isNotBlank(String checkData) {
        return !StringUtil.isNullOrBlank(checkData);
    }

    public static boolean isBlank(String checkData) {
        return StringUtil.isNullOrBlank(checkData);
    }

    public static String getOrBlank(String str) {
        return Optional.ofNullable(str).orElse("");
    }

    public static String getFixedLenStr(String str, int len, String fillChar) {
        if (str.length() == len) {
            return str;
        }
        if (str.length() > len) {
            return str.substring(0, len);
        }
        StringBuffer sb = new StringBuffer(str);
        for (int i = 0, length = len - str.length(); i < length; i++) {
            sb.append(fillChar);
        }
        return sb.toString();
    }

    public static String getFixPrefixStr(String str, int len, String fillChar) {
        if (str.length() == len) {
            return str;
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0, length = len - str.length(); i < length; i++) {
            sb.append(fillChar);
        }
        sb.append(str);
        return sb.toString();
    }

    public static String getTimeStr(int str) {
        return getFixPrefixStr(str + "", 2, "0");
    }

    public static void main(String[] args) {
        String data = "2";
        String data1 = "1";
        System.out.println(areNotBlank(data, data1));
        System.out.println(getOrBlank("sfdf"));

    }

    /**
     * 获取字符串的长度，如果为null，则返回0
     * @param data
     * @return
     */
    public static int getLen(String data) {
        if (data == null) {
            return 0;
        }
        return data.length();
    }

    /**
     * 获取最终字符串,适用于字符串拼接，按照分隔符分割的，最终去掉末尾分隔符的场景
     * @return
     */
    public static String getFinalStr(StringBuilder str) {
        if (str.length() == 0) {
            return str.toString();
        }
        return str.substring(0, str.length() - 1);
    }
}

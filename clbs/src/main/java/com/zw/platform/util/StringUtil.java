package com.zw.platform.util;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


/**
 * 工具类-》基础工具类-》字符串工具类
 * <p>
 * [依赖 jodd.jar]
 * </p>
 */
public final class StringUtil {
    private StringUtil() {
        throw new Error("工具类不能实例化！");
    }

    public static final String FUZZY = "%";

    /**
     * 将字符串第一个字母转化为大写
     *
     * @param str 需要被转换的字符串
     * @return 转换后的字符串
     */
    public static String capitalize(final String str) {
        return jodd.util.StringUtil.capitalize(str);
    }

    /**
     * 压缩字符串 如：abcccd 压缩为abcd
     *
     * @param str 需要被压缩的字符串
     * @param c   需要压缩的字符
     * @return 压缩后的字符串
     */
    public static String compressChars(final String str, final char c) {
        return jodd.util.StringUtil.compressChars(str, c);
    }

    /**
     * 转化字符串的字符集
     *
     * @param str            需要被转换字符集的字符串
     * @param srcCharsetName 原始的字符集
     * @param newCharsetName 要转换为的字符集
     * @return 转换后的字符串
     */
    public static String convertCharset(final String str, final String srcCharsetName, final String newCharsetName) {
        return jodd.util.StringUtil.convertCharset(str, srcCharsetName, newCharsetName);
    }

    /**
     * 统计字符串中包含的子字符串
     *
     * @param str        需要被统计的字符串
     * @param sub        需要统计的子字符串
     * @param startIndex 开始统计的位置
     * @param ignoreCase 是否忽略大小写
     * @return 统计的数量
     */
    public static int count(final String str, final String sub, final int startIndex, final boolean ignoreCase) {
        if (ignoreCase) {
            return jodd.util.StringUtil.countIgnoreCase(str.substring(startIndex), sub);
        } else {
            return jodd.util.StringUtil.count(str, sub, startIndex);
        }
    }

    /**
     * 切除字符串
     *
     * @param str    需要被切除的字符串
     * @param prefix 需要切掉的前缀 可以为null
     * @param suffix 需要切掉的后缀 可以为null
     * @return 切除后的字符串
     */
    public static String cut(final String str, final String prefix, final String suffix) {
        String source = str;
        if (!StringUtil.isNullOrBlank(source)) {
            if (jodd.util.StringUtil.isNotEmpty(prefix)) {
                source = jodd.util.StringUtil.cutPrefix(source, prefix);
            }
            if (jodd.util.StringUtil.isNotEmpty(suffix)) {
                source = jodd.util.StringUtil.cutSuffix(source, suffix);
            }
        }
        return source;
    }

    /**
     * cut字符串
     */
    public static String cutFrom(final String str, final String substring) {
        return jodd.util.StringUtil.cutFromIndexOf(str, substring);
    }

    /**
     * cut字符串
     */
    public static String cutTo(final String str, final String substring) {
        return jodd.util.StringUtil.cutToIndexOf(str, substring);
    }

    /**
     * join字符串
     */
    public static String join(final Iterable<?> elements, final String separator) {
        return jodd.util.StringUtil.join(elements, separator);
    }

    /**
     * 是否为null或者""
     *
     * @param str 需要判断的字符串
     * @return null或者""返回true
     */
    public static boolean isNullOrEmpty(final String str) {
        return jodd.util.StringUtil.isEmpty(str);
    }

    /**
     * 是否为null或者""或者空白字符
     *
     * @param str 需要判断的字符串
     * @return null或者""或者空白字符返回true
     */
    public static boolean isNullOrBlank(final String str) {
        return jodd.util.StringUtil.isBlank(str);
    }

    /**
     * 取两个字符串前面最大的相同部分
     *
     * @param str1 字符串1
     * @param str2 字符串2
     * @return 前面相同的部分
     */
    public static String maxCommonPrefix(final String str1, final String str2) {
        return jodd.util.StringUtil.maxCommonPrefix(str1, str2);
    }

    /**
     * 添加前缀，如果没有前缀添加前缀
     *
     * @param str    需要处理的字符串
     * @param prefix 前缀
     * @return 添加前缀后的字符串
     */
    public static String prefix(final String str, final String prefix) {
        return jodd.util.StringUtil.prefix(str, prefix);
    }

    /**
     * 反转字符串
     *
     * @param str 需要处理的字符串
     * @return 反转后的字符串
     */
    public static String reverse(final String str) {
        return jodd.util.StringUtil.reverse(str);
    }

    /**
     * 添加后缀，如果没有后缀添加后缀
     *
     * @param str    需要处理的字符串
     * @param suffix 后缀
     * @return 添加后缀后的字符串
     */
    public static String suffix(final String str, final String suffix) {
        return jodd.util.StringUtil.suffix(str, suffix);
    }

    /**
     * surround
     *
     * @param str    需要处理的字符串
     * @param prefix 前缀
     * @param suffix 后缀
     * @return surround后的字符串
     */
    public static String surround(final String str, final String prefix, final String suffix) {
        return jodd.util.StringUtil.surround(str, prefix, suffix);
    }

    /**
     * 去掉字符串左侧的空白字符
     *
     * @param str 需要处理的字符串
     * @return 去掉左侧空白字符的字符串
     */
    public static String trimLeft(final String str) {
        return jodd.util.StringUtil.trimLeft(str);
    }

    /**
     * 去掉字符串右侧的空白字符
     *
     * @param str 需要处理的字符串
     * @return 去掉右侧空白字符的字符串
     */
    public static String trimRight(final String str) {
        return jodd.util.StringUtil.trimRight(str);
    }

    /**
     * 判断前台传入数据是否有效
     */
    public static boolean isNull(final String str) {
        return str.trim() == null || "".equals(str.trim()) || "null".equals(str.trim());
    }

    /**
     * 将字符串第一个字母转化为小写
     *
     * @param str 需要被转换的字符串
     * @return 转换后的字符串
     */
    public static String uncapitalize(final String str) {
        return jodd.util.StringUtil.uncapitalize(str);
    }

    public static String replaceAssignName(String names, String source, String replace) {
        names = names.replaceFirst("^" + source + "$", replace);
        names = names.replaceFirst("^" + source + ",", replace + ",");
        names = names.replaceFirst("," + source + ",", "," + replace + ",");
        names = names.replaceFirst("," + source + "$", "," + replace);
        return names;
    }

    /**
     * 随机生成指定长度的字符串（由大小写英文字母和数字组成）
     * @author hujun
     * @since 创建时间：2018年2月9日 上午11:21:38
     */
    public static String getRandomStringByLength(int length) {
        char[] ss = new char[length];
        int i = 0;
        while (i < length) {
            int f = (int) (Math.random() * 3);
            switch (f) {
                case 0:
                    ss[i] = (char) ('A' + Math.random() * 26);
                    break;
                case 1:
                    ss[i] = (char) ('a' + Math.random() * 26);
                    break;
                case 2:
                    ss[i] = (char) ('0' + Math.random() * 10);
                    break;

                default:
                    break;
            }
            i++;
        }
        return new String(ss);
    }

    public static String encodingString(String str, String encodingCharset, String decodingCharset) {
        String codingResult = "";
        try {
            codingResult = new String(str.getBytes(encodingCharset), decodingCharset);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return codingResult;

    }

    public static String encodingFtpFileName(String fileName) {
        return encodingString(fileName, "UTF-8", "ISO8859-1");
    }

    public static String encodingDownloadFileName(String fileName) {
        return encodingString(fileName, "UTF-8", "ISO8859-1");
    }

    /**
     * MySql使用 LIKE查询时， '_''%'（通配符)需要进行转译否则会查询出所有数据,转译后'\_','\%', 建议前端进行输入限制，此处暂时先处理两个通配符
     *
     * @param simpleQueryParam 查询参数
     * @return 查询参数
     */
    public static String mysqlLikeWildcardTranslation(String simpleQueryParam) {
        if (StringUtils.isNotBlank(simpleQueryParam)) {
            if (simpleQueryParam.contains("_")) {
                simpleQueryParam = simpleQueryParam.replaceAll("_", "\\\\_");
            }
            if (simpleQueryParam.contains("%")) {
                simpleQueryParam = simpleQueryParam.replaceAll("%", "\\\\%");
            }
        }
        return simpleQueryParam;
    }

    /**
     * 截取字符串(以.0结尾的)
     */
    public static String cutString(String value) {
        return value.endsWith(".00") ? value.substring(0, value.lastIndexOf(".00")) : value;
    }

    /**
     * 判断字符串是否包含某些字符,包含一个则返回false,全部不包含返回true
     */
    public static boolean judgeStrIsContainAppointStr(String resultStr, List<String> strList) {
        boolean judgeFlag = true;
        if (StringUtils.isNotBlank(resultStr) && CollectionUtils.isNotEmpty(strList)) {
            for (String str : strList) {
                if (resultStr.contains(str)) {
                    judgeFlag = false;
                    break;
                }
            }
        }
        return judgeFlag;
    }

    public static String getBlankIfNull(String str) {
        return str == null ? "" : str;
    }

    /**
     * 通过偏移量拆分将一个list均分成n个list
     *
     * @param source  需要拆分的数据源
     * @param copyNum 拆分的份数
     * @return 拆分后的集合
     */
    public static <T> List<List<T>> averageAssign(List<T> source, int copyNum) {
        List<List<T>> result = new ArrayList<>();
        // (先计算出余数)
        int remainder = source.size() % copyNum;
        //然后是商
        int number = source.size() / copyNum;
        // //偏移量
        int offset = 0;
        for (int i = 0; i < copyNum; i++) {
            List<T> value = null;
            if (remainder > 0) {
                value = source.subList(i * number + offset, (i + 1) * number + offset + 1);
                remainder--;
                offset++;
            } else {
                value = source.subList(i * number + offset, (i + 1) * number + offset);
            }
            result.add(value);
        }
        return result;
    }

    /**
     * 将字符串以GBK编码转换
     */
    public static byte[] gbkStringToBytes(String str) {

        return str.getBytes(ConstantUtil.T808_STRING_CODE);
    }

    public static boolean areNotBlank(String... checkDatas) {
        boolean result = true;
        for (String checkData : checkDatas) {
            result = !isNullOrBlank(checkData);
            if (!result) {
                break;
            }

        }
        return result;
    }

    /**
     * 模糊查询key组装
     * @param keyword 模糊查询关键字
     * @return 适配数据库的模糊查询key
     */
    public static String fuzzyKeyword(String keyword) {
        if (StringUtils.isBlank(keyword)) {
            return keyword;
        }

        return FUZZY + mysqlLikeWildcardTranslation(keyword) + FUZZY;
    }


    /**
     * 返回以不为“0” 开头的字符串  如 "00011012000" 返回 "11012000"
     *
     * @param str str
     * @return 截取后的str
     */
    public static String getStartWithOutZeroString(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (str.startsWith("0")) {
                str = str.substring(1);
            } else {
                break;
            }
        }
        return str;
    }
}

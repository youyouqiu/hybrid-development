package com.zw.platform.util.common;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.sql.Clob;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Date;

public class Converter {
    private static final Logger log = LogManager.getLogger(Converter.class);

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        .withZone(ZoneId.of("UTC+8"));

    private Converter() {
    }

    public static Float toFloat(Object obj, Float defValue) {
        try {
            String longString = Converter.toBlank(obj);
            return Float.valueOf(longString);
        } catch (Exception e) {
            log.error("Failed to convert object to Float.", e);
            return defValue;
        }
    }

    public static Float toFloat(Object obj) {
        return toFloat(obj, null);
    }

    public static String toBlank(Object obj, String defValue) {
        String ret = toBlank(obj);
        if (ret.equals("")) {
            return toBlank(defValue);
        }
        return ret;
    }

    public static Long toLong(Object obj) {
        try {
            String longString = Converter.toBlank(obj);
            return Long.valueOf(longString);
        } catch (Exception e) {
            log.error("Failed to convert object to Long.", e);
            return null;
        }
    }

    /**
     * <b>将入参根据其类型进行向字符串的转化</b><br>
     * 注：一般只转换常见、公有类型,不转换自定义类型<br>
     * 当前可转特殊类型有:java.util.Date、org.hibernate.lob.SerializableClob、oracle.sql. CLOB，
     * 其余类型直接调用入参的toString()方法获取
     * @deprecated bad practice 职责不明（不看实现谁也不知道做了什么，看实现发现不需要调用），不建议再使用该方法，
     * 会增加维护难度（试想所有字段皆String），建议每种类型分不同方法处理
     * @param obj 任意Object及其子类
     * @return String
     */
    public static String toBlank(Object obj) {
        if (obj == null) {
            return "";
        }
        String result;
        if (obj instanceof Date) {
            try {
                result = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(obj).trim();
            } catch (Exception e) {
                try {
                    result = new SimpleDateFormat("yyyy-MM-dd").format(obj).trim();
                } catch (Exception e1) {
                    result = "";
                }
            }
        } else if (obj instanceof Clob) {
            try {
                Clob clob = (Clob) obj;
                result = clob.getSubString(1L, (int) clob.length()).trim();
            } catch (SQLException e) {
                result = "";
            }
        } else {
            result = obj.toString().trim();
        }
        return result;
    }

    /**
     * @deprecated 请勿再作为公共方法使用，这是个 bad practice，而且性能很差
     */
    public static Date toDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }
        dateString = Converter.toBlank(dateString);
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateString);
        } catch (ParseException e) {
            try {
                return new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
            } catch (ParseException e1) {
                log.error("Failed to convert {} to Date.", dateString, e);
            }
        }
        return null;
    }

    /**
     * @deprecated 请勿再作为公共方法使用，这是个 bad practice，而且性能很差
     */
    public static Date toDate(String dateString, String pattern) {
        if (StringUtils.isBlank(dateString)) {
            return null;
        }
        dateString = Converter.toBlank(dateString);
        try {
            return new SimpleDateFormat(pattern).parse(dateString);
        } catch (ParseException e) {
            log.error("Failed to convert {} to Date.", dateString, e);
            return toDate(dateString);
        }
    }

    public static String toString(Date date) {
        if (date == null) {
            return "";
        } else {
            try {
                return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(date);
            } catch (Exception e) {
                try {
                    return new SimpleDateFormat("yyyy-MM-dd").format(date);
                } catch (Exception e1) {
                    return "";
                }
            }
        }
    }

    /**
     * 将日期对象date按照pattern格式进行字符串转换，date为null时返回空串，转换异常时调用{@link #toString(Date date)}方法来处理
     * @param date {@link java.util.Date}
     * @param pattern {@link java.lang.String}
     * @return {@link java.lang.String}
     */
    @Deprecated
    public static String toString(Date date, String pattern) {
        if (date == null) {
            return "";
        } else {
            try {
                return new SimpleDateFormat(pattern).format(date);
            } catch (Exception e) {
                return Converter.toString(date);
            }
        }
    }

    @Deprecated
    public static String toString(Object obj, SimpleDateFormat format1, SimpleDateFormat format2) {
        String temp = "";
        if (obj != null) {
            try {
                temp = format1.format(format2.parse(obj.toString()));
            } catch (Exception e) {
                return "";
            }
        }
        return temp.trim();
    }

    public static Integer toInteger(Object obj) {
        try {
            String integerString = Converter.toBlank(obj);
            return Integer.valueOf(integerString);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取文件的简易名称，如果有后缀名则包括后缀名。如果入参为null或空串直接返回null<br> 例如：filePath="..\..\file\demo\xxx.text"被传入方法则返回xxx.text<br>
     * 又如：filePath="..\..\file\demo\xxx"被传入方法则返回xxx<br> 又如：filePath="..\..\file\demo\"被传入方法则返回空串<br> 注：本方法不检测文件是否实际存在
     * @param filePath 文件路径
     * @return 文件的简易名称
     */
    public static String getFileSimpleName(String filePath) {
        if (filePath == null || filePath.trim().length() <= 0) {
            return null;
        }
        filePath = filePath.replace("\\", "/");
        filePath = filePath.substring(filePath.lastIndexOf("/") + 1);
        return filePath;
    }

    /**
     * 获取文件的简易名称，如果有后缀名则包括后缀名。如果入参为null直接返回null<br> 例如：file=new File("..\..\file\demo\xxx.text")被传入方法则返回xxx.text<br>
     * 又如：file=new File("..\..\file\demo\xxx")被传入方法则返回xxx 又如：file=new File("..\..\file\demo\")被传入方法则返回空串<br>
     * 注：本方法不检测文件是否实际存在
     * @param file 文件对象
     * @return 文件的简易名称
     */
    public static String getFileSimpleName(File file) {
        return Converter.getFileSimpleName(file.getPath());
    }

    public static Long toLong(Object obj, Long defValue) {
        try {
            String longString = Converter.toBlank(obj);
            return Long.valueOf(longString);
        } catch (Exception e) {
            return defValue;
        }
    }

    public static Double toDouble(Object obj) {
        try {
            String longString = Converter.toBlank(obj);
            return Double.valueOf(longString);
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * 将obj转化为Boolean类型的数据，如果obj为空或null则返回false，如果不能转换则抛出异常
     * @return Boolean
     */
    public static Boolean toBoolean(Object obj) {
        String s = Converter.toBlank(obj).toLowerCase();
        if (s.equals("")) {
            return Boolean.FALSE;
        } else {
            return Boolean.valueOf(s);
        }
    }

    public static Integer toInteger(Object obj, Integer defValue) {
        try {
            String integerString = Converter.toBlank(obj);
            return Integer.valueOf(integerString);
        } catch (Exception e) {
            return defValue;
        }
    }

    public static Double toDouble(Object obj, Double defValue) {
        try {
            String longString = Converter.toBlank(obj);
            return Double.valueOf(longString);
        } catch (Exception e) {
            return defValue;
        }
    }

    /**
     * 时间戳转换成日期格式字符串
     * @param seconds 精确到秒的字符串
     */
    public static String timeStamp2Date(String seconds, String format) {
        if (seconds == null || seconds.isEmpty() || seconds.equals("null")) {
            return "";
        }
        if (format == null || format.isEmpty()) {
            format = "yyyy-MM-dd HH:mm:ss";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date(Long.parseLong(seconds) * 1000));
    }

    /**
     * 日期格式字符串转换成时间戳
     * @param dateStr 字符串日期
     * @param format 如：yyyy-MM-dd HH:mm:ss
     */
    public static String date2TimeStamp(String dateStr, String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return String.valueOf(sdf.parse(dateStr).getTime() / 1000);
        } catch (Exception e) {
            log.error("Failed to convert {} with format {} to timestamp string.", dateStr, format, e);
        }
        return "";
    }

    /**
     * 将日期转换为设备使用的BCD时间
     */
    public static String getBcdDate(Date date) {
        if (date == null) {
            return null;
        }
        return new SimpleDateFormat("yyMMddHHmmss").format(date);
    }

    /**
     * Unix时间转换为普通时间
     * @author Jiangxiaoqiang
     */
    public static String convertUnixToDatetime(long unixTimestamp) {
        return formatter.format(Instant.ofEpochSecond(unixTimestamp));
    }

    public static String formatDateTime(Calendar datetime) {
        return formatter.format(datetime.toInstant());
    }

    /**
     * 普通时间转换为Unix时间戳
     */
    public static long convertToUnixTimeStamp(String time) {
        long unixTime = 0;
        try {
            unixTime = LocalDateTime.parse(time, formatter).atZone(formatter.getZone()).toEpochSecond();
        } catch (DateTimeParseException e) {
            log.error("Failed to convert {} to unixTime.", time, e);
        }
        return unixTime;
    }

    /**
     * 去掉string的最后一个字符，主要用于去除最后一个分隔符：如"#"，","，";"等等
     * @author Liubangquan
     */
    public static String removeStringLastChar(String str) {
        if (!Converter.toBlank(str).equals("") && str.length() > 0) {
            str = str.substring(0, str.length() - 1);
        } else {
            str = "";
        }
        return str;
    }

    /**
     * 判断是否是整数
     * @author Liubangquan
     */
    public static boolean isInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            log.error("Failed to test if {} is Integer.", value);
            return false;
        }
    }

    /**
     * 判断是否是浮点数
     * @author Liubangquan
     */
    public static boolean isDouble(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            log.error("Failed to test if {} is Double.", value);
            return false;
        }
    }

    /**
     * 判断是否为数字
     * @author Liubangquan
     */
    public static boolean isNumber(String value) {
        return isInteger(value) || isDouble(value);
    }
}

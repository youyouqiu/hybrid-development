package com.zw.platform.util;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Java 8 日期处理
 * @author Created by zhouzongbo on 2018/3/25.
 */
public class LocalDateUtils {

    /**
     * yyyy-MM-dd
     * 说明：
     */
    private static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ISO_DATE;
    /**
     * yyyy-MM-dd
     * <p>
     * DateTimeFormatter.ofPattern(BaseConstant.DATE_FORMAT)
     */
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    /**
     * yyyy年MM月dd
     */
    private static final DateTimeFormatter DATE_FORMATTER_ZH = DateTimeFormatter.ofPattern("yyyy年MM月dd日");

    /**
     * yyyy-MM-dd HH:mm:ss
     */
    private static final DateTimeFormatter DATE_TIMESTAMP_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * yyyyMMddHHmmss
     */
    private static final DateTimeFormatter DATE_TIMESTAMP_FORMATTER_FILE_NAME =
        DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    /**
     * yyyyMMddHHmmss
     */
    private static final DateTimeFormatter DATE_TIMESTAMP_FORMATTER_YY_MM = DateTimeFormatter.ofPattern("yyMMddHHmmss");
    /**
     * HH:mm:ss
     */
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * HH:mm
     */
    private static final DateTimeFormatter TIME_SCOPE_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 毫秒转换为秒
     */
    public static final int SECOND = 1000;

    /**
     * Date转换为 ZonedDateTime转换为 LocalDateTime
     * 这是一个包含时区的完整的日期时间，偏移量是以UTC/格林威治时间为基准
     * @param date this date
     * @return ZonedDateTime
     */
    public static ZonedDateTime dateTransferZonedDateTime(final Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault());
    }

    public static Date localDateTimeToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * localDateTime 转换 yyyy-MM-dd HH:mm:ss
     * @param localDateTime this localDateTime
     * @return formatter yyyy-MM-dd HH:mm:ss
     */
    public static String dateTimeFormat(final LocalDateTime localDateTime) {
        return localDateTime.format(DATE_TIMESTAMP_FORMATTER);
    }

    /**
     * localDateTime 转换 yyyy-MM-dd
     * @param localDate this localDateTime
     * @return formatter yyyy-MM-dd HH:mm:ss
     */
    public static String dateFormat(final LocalDate localDate) {
        return localDate.format(DATE_TIME_FORMATTER);
    }

    /**
     * localDateTime 转换 yyMMddHHmmss
     * @param localDateTime this localDateTime
     * @return formatter yyyy-MM-dd HH:mm:ss
     */
    public static String dateTimeFormatYYMM(final LocalDateTime localDateTime) {
        return localDateTime.format(DATE_TIMESTAMP_FORMATTER_YY_MM);
    }

    /**
     * date转换 yyyy-MM-dd HH:mm:ss
     * @param date
     * @return
     */
    public static String dateTimeFormat(final Date date) {
        return dateTransferZonedDateTime(date).format(DATE_TIMESTAMP_FORMATTER);
    }

    public static String dateFormate(final Date date) {
        return dateTransferZonedDateTime(date).format(DATE_TIME_FORMATTER);
    }

    /**
     * yyyy年MM月dd日
     * @param localDateTime
     * @return
     */
    public static String dateTimeFormatZh(final LocalDateTime localDateTime) {
        return localDateTime.format(DATE_FORMATTER_ZH);
    }

    /**
     * date转换 yyyy年MM月dd日
     * @param date
     * @return
     */
    public static String dateTimeFormatZh(final Date date) {
        return dateTransferZonedDateTime(date).toLocalDate().format(DATE_FORMATTER_ZH);
    }

    /**
     * localDateTime 文件名格式DateTime
     * @param localDateTime
     * @return
     */
    public static String dateTimeFormatterFileName(final LocalDateTime localDateTime) {
        return localDateTime.format(DATE_TIMESTAMP_FORMATTER_FILE_NAME);
    }

    /**
     * Date文件名格式DateTime
     * @param date
     * @return
     */
    public static String dateTimeFormatterFileName(final Date date) {
        return dateTransferZonedDateTime(date).toLocalDateTime().format(DATE_TIMESTAMP_FORMATTER_FILE_NAME);
    }

    /**
     * HH:mm:ss
     * @param localDateTime
     * @return
     */
    public static String timeFormatter(final LocalDateTime localDateTime) {
        return localDateTime.format(TIME_FORMATTER);
    }

    /**
     * HH:mm:ss
     * @param date
     * @return
     */
    public static String timeFormatter(final Date date) {
        return dateTransferZonedDateTime(date).toLocalDateTime().format(TIME_FORMATTER);
    }

    /**
     * HH:mm
     * @param localDateTime
     * @return
     */
    public static String timeScopeFormatter(final LocalDateTime localDateTime) {
        return localDateTime.format(TIME_SCOPE_FORMAT);
    }

    /**
     * HH:mm
     * @param date
     * @return
     */
    public static String timeScopeFormatter(final Date date) {
        return dateTransferZonedDateTime(date).toLocalDateTime().format(TIME_SCOPE_FORMAT);
    }

    /**
     * @param dateTime         dateTime yyMMddHHmmss
     * @param yearBeforeNumber 年号前两位
     * @return str
     */
    public static String timeScopeFormatter(final String dateTime, final String yearBeforeNumber) {
        return dateTimeFormat(LocalDateUtils.localDateTimeScope(yearBeforeNumber + dateTime));
    }

    /*................................String转日期..........................................*/

    /**
     * yyyy-MM-dd
     * @param date String
     * @return LocalDate
     */
    public static LocalDate localDate(final String date) {
        return LocalDate.parse(date, DATE_FORMAT);
    }

    /**
     * parseDate
     * @param date String
     * @return Date
     */
    public static Date parseDate(final String date) {
        return Date.from(localDate(date).atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * yyyy-MM-dd HH:mm:ss
     * @param dateTime String
     * @return LocalDateTime
     */
    public static LocalDateTime localDateTime(final String dateTime) {
        return LocalDateTime.parse(dateTime, DATE_TIMESTAMP_FORMATTER);
    }

    /**
     * localDateTime 文件名格式DateTime
     * @param dateTime
     * @return time
     */
    public static LocalDateTime localDateTimeScope(final String dateTime) {
        return LocalDateTime.parse(dateTime, DATE_TIMESTAMP_FORMATTER_FILE_NAME);
    }

    public static LocalDate localDateParse(final String dateTime) {
        return LocalDate.parse(dateTime, DATE_FORMATTER);
    }

    /**
     * date
     * @param dateTime String
     * @return Date
     */
    public static Date parseDateTime(final String dateTime) {
        return Date.from(localDateTime(dateTime).atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * HH:mm:ss
     * @param time
     * @return
     */
    public static LocalTime localTime(final String time) {
        return LocalTime.parse(time);
    }

    /**
     * HH:mm:ss
     * atZone(ZoneId.systemDefault())// 时区设置
     * @param time
     * @return
     */
    public static Date parseTime(final String time) {
        return Date.from(LocalDateTime.of(LocalDate.now(), localTime(time)).atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * HH:mm
     * @param timeScope this timeScope
     * @return LocalTime
     */
    public static LocalTime localTimeScope(final String timeScope) {
        return LocalTime.parse(timeScope, TIME_SCOPE_FORMAT);
    }

    public static boolean twoDateIsEquals(final Date startDate, final Date endDate) {
        LocalDate startLocalDate = dateTransferZonedDateTime(startDate).toLocalDate();
        LocalDate endLocalDate = dateTransferZonedDateTime(endDate).toLocalDate();
        return startLocalDate.equals(endLocalDate);
    }

    public static String getFormatDayAndHourAndMinute(long millisecond) {
        StringBuilder timeBuilder = new StringBuilder();
        Long hour = millisecond / 60 / 60 / 1000;
        Long minute = millisecond / 60 / 1000 % 60;
        Long second = millisecond / 1000 % 60;
        if (hour > 0) {
            timeBuilder.append(hour.intValue()).append("小时");
        }
        if (minute > 0) {
            timeBuilder.append(minute.intValue()).append("分");
        }
        timeBuilder.append(second).append("秒");
        return timeBuilder.toString();
    }

    public static String formatDuring(long millisecond) {
        long days = millisecond / (1000 * 60 * 60 * 24);
        long hours = (millisecond % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
        long minutes = (millisecond % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = (millisecond % (1000 * 60)) / 1000;
        StringBuilder timeBuilder = new StringBuilder();
        if (days > 0) {
            timeBuilder.append(days).append("天");
        }
        if (hours > 0) {
            timeBuilder.append(hours).append("小时");
        }
        if (minutes > 0) {
            timeBuilder.append(minutes).append("分");
        }
        if (seconds > 0) {
            timeBuilder.append(seconds).append("秒");
        }
        String timeStr = timeBuilder.toString();
        return StringUtils.isNotEmpty(timeStr) ? timeStr : "0";
    }

    public static String formatHour(long millisecond) {
        BigDecimal bigDecimal = new BigDecimal(millisecond).divide(new BigDecimal(3600000), 1, RoundingMode.HALF_UP);
        double hours = bigDecimal.doubleValue();
        if (hours > 0) {
            return String.valueOf(hours);
        } else {
            return "0";
        }
    }

    public static String formatAppHour(long millisecond) {
        if (millisecond >= 10 * 60 * 1000) {
            BigDecimal bigDecimal =
                new BigDecimal(millisecond).divide(new BigDecimal(3600000), 1, RoundingMode.HALF_UP);
            double hours = bigDecimal.doubleValue();
            if (hours > 0) {
                return String.valueOf(hours) + "h";
            } else {
                return "0m0s";
            }
        } else {
            long min = millisecond / (60 * 1000);
            long sec = (millisecond % (60 * 1000)) / 1000;
            return min + "m" + sec + "s";
        }
    }

    /**
     * 比较日期
     * @param endTime endTime (yyyy-MM-dd)
     * @param amount  天数(Adds or subtracts)
     * @return dataFormat
     */
    public static String compareDate(String endTime, int amount) {
        Calendar currentCalendar = Calendar.getInstance();
        String currentDateFormat = LocalDateUtils.dateFormate(currentCalendar.getTime());
        if (currentDateFormat.equals(endTime)) {
            currentCalendar.add(Calendar.DAY_OF_YEAR, amount);
            endTime = LocalDateUtils.dateFormate(currentCalendar.getTime());
        }
        return endTime;
    }

    public static List<String> getBetweenDate(String startTime, String endTime) {
        // 声明保存日期集合
        List<String> list = new ArrayList<String>();
        try {
            // 转化成日期类型
            Date startDate = parseDate(startTime);
            Date endDate = parseDate(endTime);

            //用Calendar 进行日期比较判断
            Calendar calendar = Calendar.getInstance();
            while (startDate.getTime() <= endDate.getTime()) {
                // 把日期添加到集合
                list.add(dateFormate(startDate));
                // 设置日期
                calendar.setTime(startDate);
                //把日期增加一天
                calendar.add(Calendar.DATE, 1);
                // 获取增加后的日期
                startDate = calendar.getTime();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void main(String[] args) {
        LocalDateTime localDateTime = localDateTime("12-08-01 12:12:12");
        System.out.println(dateTimeFormatYYMM(localDateTime));

    }
}

package com.zw.platform.util.common;

import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class Date8Utils {
    public static String HOUR_FORMAT = "yyyyMMddHH";

    public static String DAY_FORMAT = "yyyyMMdd";

    public static String DATE_FORMAT_NOT_COLON = "yyyyMMddHHmmss";

    public static String MONTH_FORMAT = "yyyyMM";

    public static String YEAR_FORMAT = "yyyy";

    public static String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static long getDateVal(LocalDateTime dateTime, String formatter) {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(formatter);
        return Long.parseLong(dateTime.format(dateFormat));
    }

    public static long getValToHour(LocalDateTime dateTime) {
        return getDateVal(dateTime, HOUR_FORMAT);
    }

    public static long getValToTime(LocalDateTime dateTime) {
        return getDateVal(dateTime, DATE_FORMAT_NOT_COLON);
    }

    public static long getValToDay(LocalDateTime dateTime) {

        return getDateVal(dateTime, DAY_FORMAT);
    }

    public static long getValToMonth(LocalDateTime dateTime) {
        return getDateVal(dateTime, MONTH_FORMAT);
    }

    public static long getValToYear(LocalDateTime dateTime) {
        return getDateVal(dateTime, YEAR_FORMAT);
    }

    public static long getMidnightHour(LocalDateTime dateTime) {
        long nowHour = getDateVal(dateTime, HOUR_FORMAT);
        long hour = dateTime.getHour();
        if (hour == 0) {
            hour = getDateVal(dateTime.minusDays(1), HOUR_FORMAT);
        } else {
            hour = nowHour - hour;
        }
        return hour;

    }

    public static LocalDateTime getMidnightTime(LocalDateTime dateTime) {
        return dateTime.minusHours(dateTime.getHour()).minusMinutes(dateTime.getMinute())
            .minusSeconds(dateTime.getSecond());
    }

    public static long getStartTime(boolean isToday, LocalDateTime dateTime) {
        return isToday ? Date8Utils.getMidnightHour(dateTime) : Date8Utils.getMidnightHour(dateTime.minusDays(1));
    }

    public static long getEndTime(boolean isToday, LocalDateTime dateTime) {
        return isToday ? (Date8Utils.getValToHour(dateTime) + 1) : Date8Utils.getMidnightHour(dateTime);
    }

    public static String getDateHourTime(LocalDateTime localDateTime) {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
        String dateString = localDateTime.format(dateFormat);
        String[] dateArr = dateString.split(" ");
        dateString = dateArr[0] + " " + dateArr[1].split(":")[0] + ":00:00";
        return dateString;
    }

    public static String getMidnightHourTime(LocalDateTime localDateTime) {
        long hour = localDateTime.getHour();
        if (hour == 0) {
            localDateTime = localDateTime.minusDays(1);
        } else {
            localDateTime = localDateTime.minusHours(hour);
        }
        return getDateHourTime(localDateTime);
    }

    public static String getMidnightDayTime(LocalDateTime localDateTime) {
        localDateTime = localDateTime.minusDays(1);
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
        String dateString = localDateTime.format(dateFormat);
        String[] dateArr = dateString.split(" ");
        return dateArr[0];
    }

    public static Map<String, Integer> getDayMap(String startTime, String endTime) {
        Map<String, Integer> map = new LinkedHashMap<>();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String localTime = startTime;
        map.put(localTime, 0);
        while (!localTime.equals(endTime)) {
            localTime = df.format(LocalDate.parse(localTime, df).plusDays(1));
            map.put(localTime, 0);
        }
        return map;
    }

    public static String[] getTimes(int type, LocalDateTime localDateTime) {
        String[] time = new String[2];
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
        switch (type) {
            case 0:
                //今日此时
                time[0] = getMidnightHourTime(localDateTime);
                time[1] = localDateTime.format(dateFormat);
                break;
            case 1:
                //昨日整天
                time[0] = getMidnightHourTime(localDateTime.minusDays(1));
                String dateString = localDateTime.minusDays(1).format(dateFormat);
                String[] dateArr = dateString.split(" ");
                time[1] = dateArr[0] + " 23:59:59";
                break;
            case 2:
                //今日此时上个计算节点
                time[0] = getMidnightHourTime(localDateTime);
                time[1] = getDateHourTime(localDateTime);
                break;
            case 3:
                //昨日此时上个计算节点
                time[0] = getMidnightHourTime(localDateTime.minusDays(1));
                time[1] = getDateHourTime(localDateTime.minusDays(1));
                break;
            default:
                break;

        }
        return time;
    }

    public static String getCurrentTime(LocalDateTime localDateTime) {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
        return localDateTime.format(dateFormat);
    }

    public static String formatter(String dateTime) {
        return dateTime.split(" ")[0];
    }

    public static LocalDateTime fromLongTime(String longTime) {
        long time = Long.parseLong(longTime);
        return fromLongTime(time);
    }

    public static LocalDateTime fromLongTime(long time) {
        Date date = DateUtil.getLongToDate(time);
        Instant instant = date.toInstant();
        ZoneId zone = ZoneId.systemDefault();
        return LocalDateTime.ofInstant(instant, zone);
    }

    public static long getLongTime(LocalDateTime dateTime) {
        return dateTime.toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
    }

    public static Long getSubLongTime(LocalDateTime endTime, LocalDateTime startTime) {
        //过滤异常数据（结束时间大于开始时间）设置为0
        long result = getLongTime(endTime) - getLongTime(startTime);
        return result > 0 ? result : 0;

    }

    public static Long getTime(String gpsTime) {
        try {
            return DateUtils.parseDate("20" + gpsTime, DATE_FORMAT_NOT_COLON).getTime();
        } catch (ParseException e) {
            return 0L;
        }
    }

    public static LocalDateTime getLocalDateTime(String dateTime, String dateFormat) {
        Date date = DateUtil.getStringToDate(dateTime, dateFormat);
        return fromLongTime(date.getTime());
    }

    public static LocalDateTime getLocalDateTime(Long yearMonthVal) {
        long year = yearMonthVal / 100;
        long month = yearMonthVal % 100;
        return LocalDateTime.of((int) year, (int) month, 1, 0, 0, 0);
    }

    public static String getFirstMonthDateTime(Long yearMonthVal, String dateFormat) {
        LocalDateTime dateTime = getLocalDateTime(yearMonthVal);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
        return dateTime.format(formatter);
    }

    public static String getNextMonthDateTime(Long yearMonthVal, String dateFormat) {
        LocalDateTime dateTime = getLocalDateTime(yearMonthVal);
        dateTime = dateTime.plusMonths(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
        return dateTime.format(formatter);
    }

    public static String getLastMonthDateTime(Long yearMonthVal, String dateFormat) {
        LocalDateTime dateTime = getLocalDateTime(yearMonthVal);
        dateTime = dateTime.plusMonths(1).minusDays(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
        return dateTime.format(formatter);
    }

    public static String getFormatDateTimeFromTimeVal(String time, String dateFormat) {

        SimpleDateFormat sdfShort = new SimpleDateFormat(DATE_FORMAT_NOT_COLON);
        SimpleDateFormat resultFormats = new SimpleDateFormat(dateFormat);
        Date date = new Date();
        try {
            date = sdfShort.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return resultFormats.format(date);
    }

    public static long getTodayStartSecond() {
        LocalDateTime now = LocalDateTime.now();
        return now.withHour(0).withMinute(0).withSecond(0).toEpochSecond(ZoneOffset.of("+8"));
    }
}

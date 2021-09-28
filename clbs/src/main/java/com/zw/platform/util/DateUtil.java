package com.zw.platform.util;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjusters;
import java.util.Optional;

/**
 * 日期处理，顾名思义
 * <p>这里完全取消了对java.util.Date的支持
 *
 * @author Zhang Yanhui
 * @since 2020/5/25 11:15
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DateUtil {
    /**
     * 时间戳 转 LocalDateTime
     *
     * @param timestamp 时间戳
     * @return LocalDateTime
     */
    public static LocalDateTime fromTimestamp(long timestamp) {
        return Instant.ofEpochMilli(timestamp).atZone(ZoneId.of("+8")).toLocalDateTime();
    }


    /**
     * TemporalAccessor 转 时间戳
     *
     * @param temporal LocalDateTime 或 LocalDate 或 YearMonth
     * @return 时间戳
     */
    public static long toTimestamp(TemporalAccessor temporal) {
        final LocalDateTime localDateTime;
        if (temporal instanceof LocalDateTime) {
            localDateTime = (LocalDateTime) temporal;
        } else if (temporal instanceof LocalDate) {
            localDateTime = ((LocalDate) temporal).atStartOfDay();
        } else if (temporal instanceof YearMonth) {
            localDateTime = ((YearMonth) temporal).atDay(1).atStartOfDay();
        } else {
            throw new RuntimeException("不支持从[" + temporal.getClass().getSimpleName() + "]向时间戳的转换");
        }
        return localDateTime.toEpochSecond(ZoneOffset.of("+8")) * MS_PER_SEC;
    }

    /**
     * 转换器，
     */
    @AllArgsConstructor
    public static class Formatter {

        @Getter
        private final DateTimeFormatter formatter;

        public Formatter(String format) {
            this.formatter = DateTimeFormatter.ofPattern(format);
        }

        public Optional<String> format(@Nullable TemporalAccessor temporal) {
            return Optional.ofNullable(temporal).map(formatter::format);
        }

        public Optional<LocalDate> ofDate(@Nullable String dateStr) {
            return Optional.ofNullable(dateStr)
                    .filter(StringUtils::isNotEmpty)
                    .map(o -> formatter.parse(o, LocalDate::from));
        }

        public Optional<LocalDateTime> ofDateTime(@Nullable String dateTimeStr) {
            return Optional.ofNullable(dateTimeStr)
                    .filter(StringUtils::isNotEmpty)
                    .map(o -> formatter.parse(o, LocalDateTime::from));
        }

        public Optional<LocalTime> ofTime(@Nullable String timeStr) {
            return Optional.ofNullable(timeStr)
                    .filter(StringUtils::isNotEmpty)
                    .map(o -> formatter.parse(o, LocalTime::from));
        }
    }

    /**
     * 格式[_格式]*[_备注]*（命名力求简明扼要）
     * <p>注：YMD 代表 yyyy-MM-dd
     */
    public static final Formatter YMD = new Formatter(DateTimeFormatter.ISO_LOCAL_DATE);
    public static final Formatter YMD_SHORT = new Formatter("yyyyMMdd");
    public static final Formatter YMD_ZH = new Formatter("yyyy年MM月dd日");
    public static final Formatter MD_ZH = new Formatter("M月d日");

    public static final Formatter YM = new Formatter("yyyy-MM");

    public static final Formatter YMD_HMS = new Formatter("yyyy-MM-dd HH:mm:ss");
    public static final Formatter YMD_HM = new Formatter("yyyy-MM-dd HH:mm");
    public static final Formatter YMD_HMS_SHORT = new Formatter("yyyyMMddHHmmss");
    public static final Formatter Y2MD_HMS_SHORT = new Formatter("yyMMddHHmmss");
    public static final Formatter Y2MD_HM_SHORT = new Formatter("yyMMddHHmm");
    public static final Formatter Y2MD_H_SHORT = new Formatter("yyMMddHH");
    public static final Formatter YMD_HMS_20 = new Formatter("yyMMddHHmmss");

    public static final Formatter HMS = new Formatter(DateTimeFormatter.ISO_LOCAL_TIME);
    public static final Formatter HM = new Formatter("HHmm");

    public static final Formatter YMD_HMS_SHORT_CUSTOM = new Formatter("yyMMddHHmmss") {
        @Override
        public Optional<LocalDateTime> ofDateTime(String dateTimeStr) {
            return Y2MD_HM_SHORT.ofDateTime("20" + dateTimeStr);
        }

        @Override
        public Optional<String> format(TemporalAccessor temporal) {
            return YMD_HMS.format(temporal);
        }
    };
    public static final Formatter YM_SHORT = new Formatter("yyyyMM");

    public static final int MS_PER_SEC = 1000;
    public static final int SEC_PER_MIN = 60;
    public static final int MIN_PER_HOUR = 60;
    public static final int HOUR_PER_DAY = 24;

    public static String formatDuringSec(long second) {
        return formatDuring(second * MS_PER_SEC);
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

    /**
     * 获取当前月第一天
     * @param yearAndMonth yyyy-MM
     * @return yyyy-MM-dd
     */
    public static String getFirstDayOfMonth(String yearAndMonth) {
        final YearMonth date = YearMonth.parse(yearAndMonth);
        final LocalDate localDate = date.atDay(1);
        return localDate.format(YMD.getFormatter());
    }

    /**
     * 获取当月最后一天
     * @param yearAndMonth yyyy-MM
     * @return yyyy-MM-dd
     */
    public static String getLastDayOfMonth(String yearAndMonth) {
        final LocalDate date = YearMonth.parse(yearAndMonth).atEndOfMonth();
        LocalDate monthLastDay = date.with(TemporalAdjusters.lastDayOfMonth());
        return monthLastDay.format(YMD.getFormatter());
    }

    /**
     * 获取范围内的时间
     * @param start 开始
     * @param end   结束
     * @return LocalDateTime
     */
    public static LocalDateTime getRandomTimeInRange(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return null;
        }
        return LocalDateTime.ofEpochSecond(
            RandomUtils.nextLong(start.toEpochSecond(ZoneOffset.of("+8")), end.toEpochSecond(ZoneOffset.of("+8"))), 0,
            ZoneOffset.ofHours(8));
    }
}

package com.zw.platform.util.common;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Calendar;
import java.util.Date;

/**
 * 时间处理工具类
 * @author wangying
 * @deprecated 大部分方法的性能、可读性（无法顾名思义，必须阅读实现）、可扩展性、健壮性都欠佳
 */
public class DateUtil {
    private static Logger logger = LogManager.getLogger(DateUtil.class);

    public static final String DATE_FORMAT_SHORT = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_HH_MM_SS = "HH:mm:ss";
    public static final String DATE_FORMAT_SHORT_MINUTE_CN = "yyyy年MM月dd日HH时mm分";
    public static final String DATE_FORMAT = "yyyyMMddHHmmss";
    public static final String DATE_FORMAT_SSS = "yyyyMMddHHmmssSSS";
    public static final String DATE_Y_M_D_FORMAT = "yyyy-MM-dd";
    public static final String DATE_Y_M_FORMAT = "yyyy-MM";
    public static final String DATE_YMD_FORMAT = "yyyyMMdd";
    public static final String DATE_GMT8_TIME = "yy-MM-dd-HH-mm-ss";
    public static final String DATE_YYMMDDHHMMSS = "yyMMddHHmmss";
    public static final String DATE_YYMMDDHHMMSS_SSS = "yyMMddHHmmssSSS";
    private static final long THOUSAND_MILLISECOND = 1000;

    /**
     * 将时间转为指定时间格式的字符串
     * @param date
     * @return Date: 2012-5-14下午03:53:37
     * @deprecated 性能不好
     */
    public static String getDateToString(Date date, String format) {
        try {
            SimpleDateFormat sdfShort = new SimpleDateFormat(DATE_FORMAT_SHORT);
            if (format != null && !"".equals(format)) {
                sdfShort = new SimpleDateFormat(format);
            }
            return sdfShort.format(date);
        } catch (Exception e) {
            logger.error("将时间转为指定时间格式的字符串", e);
        }
        return null;
    }

    /**
     * 得到的是毫秒值
     * @param time
     * @param format
     * @return
     */
    public static Long getStringToLong(String time, String format) {
        try {
            if (StringUtils.isEmpty(format)) {
                format = DATE_FORMAT_SHORT;
            }
            return DateUtils.parseDate(time, format).getTime();
        } catch (Exception e) {
            logger.error("时间字符串转数字错误！", e);
            return 0L;
        }
    }

    /**
     * 取得当前月第一天
     * @param date
     * @return Date: 2012-5-14下午03:53:37
     * @deprecated 性能不好
     */
    public static Date getMonthFirst(Date date) {
        try {
            SimpleDateFormat sdfShort = new SimpleDateFormat(DATE_FORMAT_SHORT);
            SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy");
            SimpleDateFormat sdfMonth = new SimpleDateFormat("MM");
            return sdfShort.parse(sdfYear.format(date) + "-" + sdfMonth.format(date) + "-01 00:00:00");
        } catch (ParseException e) {
            logger.error("取得当前月第一天异常", e);
        }
        return null;
    }

    public static Long getFirstDayOfCurrentMonth() {
        return ZonedDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).toEpochSecond();
    }

    /**
     * 取得当前月最后一天
     * @param date
     * @return Date: 2012-5-14下午03:53:57
     * @deprecated 性能不好
     */
    public static Date getMonthLast(Date date) {
        try {
            SimpleDateFormat sdfShort = new SimpleDateFormat(DATE_FORMAT_SHORT);
            SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy");
            SimpleDateFormat sdfMonth = new SimpleDateFormat("MM");
            int year = Integer.valueOf(sdfYear.format(date));
            int month = Integer.valueOf(sdfMonth.format(date));
            int day = getMonthDays(year, month);
            return sdfShort.parse(year + "-" + month + "-" + day + " 23:59:59");
        } catch (ParseException e) {
            logger.error("取得当前月最后一天异常", e);
        }
        return null;
    }

    public static Long getLastDayOfCurrentMonth() {
        ZonedDateTime today = ZonedDateTime.now();
        return today.with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59)
            .toEpochSecond();
    }

    // 获取现在月份的上一个月的第一天
    public static Long getFirstDayOfCurrentLastMonth() {
        ZonedDateTime zd = ZonedDateTime.now();
        int month = zd.getMonthValue(); // 当前时间月份
        Long time;
        if (month == 1) { // 如果是1月份,获取上一年12月份的第一天
            int year = zd.getYear() - 1;
            time = zd.withYear(year).withMonth(12).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0)
                .toEpochSecond();
        } else {
            time = zd.withMonth(month - 1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).toEpochSecond();
        }
        return time;
    }

    // 获取现在月份的上一个月的最后一天
    public static Long getLastDayOfCurrentLastMonth() {
        ZonedDateTime today = ZonedDateTime.now();
        int month = today.getMonthValue(); // 当前时间的月份
        Long time;
        if (month == 1) { // 如果是1月份,获取上一年12月份的最后一天
            int year = today.getYear() - 1;
            time =
                today.withYear(year).withMonth(12).with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59)
                    .withSecond(59).toEpochSecond();
        } else {
            time = today.withMonth(month - 1).with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59)
                .withSecond(59).toEpochSecond();
        }
        return time;
    }

    /**
     * 取得某月天数
     * @param year
     * @param month
     * @return Date: 2012-5-14下午04:13:41
     */
    public static int getMonthDays(int year, int month) {
        Calendar a = Calendar.getInstance();
        a.set(Calendar.YEAR, year);
        a.set(Calendar.MONTH, month - 1);
        a.set(Calendar.DATE, 1);// 把日期设置为当月第一天
        a.roll(Calendar.DATE, -1);// 日期回滚一天，也就是最后一天
        int maxDate = a.get(Calendar.DATE);
        return maxDate;
    }

    /**
     * 秒转换为时分秒
     * @param seconds
     * @return
     */
    public static String secToTime(int seconds) {
        int hour = seconds / 3600;
        int minute = (seconds - hour * 3600) / 60;
        int second = (seconds - hour * 3600 - minute * 60);

        StringBuffer sb = new StringBuffer();
        if (hour > 0) {
            sb.append(hour + "小时");
        }
        if (minute > 0) {
            sb.append(minute + "分");
        }
        if (second > 0) {
            sb.append(second + "秒");
        }
        if (second == 0) {
            sb.append("<1秒");
        }
        return sb.toString();
    }

    public static String unitFormat(int i) {
        // 时分秒的格式转换
        String retStr;
        if (i >= 0 && i < 10) {
            retStr = "0" + i;
        } else {
            retStr = "" + i;
        }
        return retStr;
    }

    /**
     * 获取所传入时间的上月
     * @param date
     * @return
     */
    public static Date getLastDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, -1);
        return cal.getTime();
    }

    /**
     * 毫秒转化为日期
     * @param ms
     * @return
     */
    public static String formatTime(long ms) {
        int ss = 1000;
        int mi = ss * 60;
        int hh = mi * 60;
        int dd = hh * 24;

        long day = ms / dd;
        long hour = (ms - day * dd) / hh;
        long minute = (ms - day * dd - hour * hh) / mi;
        long second = (ms - day * dd - hour * hh - minute * mi) / ss;
        long milliSecond = ms - day * dd - hour * hh - minute * mi - second * ss;

        String strDay = Long.toString(day);
        String strHour = Long.toString(hour);
        String strMinute = Long.toString(minute);
        String strSecond = Long.toString(second);
        String strMilliSecond = milliSecond < 10 ? "0" + milliSecond : "" + milliSecond;// 毫秒
        strMilliSecond = milliSecond < 100 ? "0" + strMilliSecond : "" + strMilliSecond;
        if (strDay.equals("0") && strHour.equals("0") && strMinute.equals("0")) {
            return strSecond + "秒";
        }
        if (strDay.equals("0") && strHour.equals("0")) {
            return strMinute + "分" + strSecond + "秒";
        }
        if (strDay.equals("0")) {
            return strHour + "小时" + strMinute + "分" + strSecond + "秒";
        }
        return strDay + "天" + strHour + "小时" + strMinute + "分" + strSecond + "秒";
    }

    /**
     * 分钟转换日期
     */
    public static String formatMinToString(long min) {
        int mi = 1;
        int hh = mi * 60;
        long hour = min / hh;
        long minute = (min - hour * hh) / mi;
        String strHour = hour + "";
        String strMinute = minute + "";
        return strHour + "小时" + strMinute + "分钟";
    }

    /**
     * 根据传入的时间字符串及格式化转换时间
     * @param dateTime
     * @param dateFormat
     * @return
     */
    public static Date getStringToDate(String dateTime, String dateFormat) {
        if (dateFormat == null || "".equals(dateFormat)) {
            dateFormat = DATE_FORMAT_SHORT;
        }
        SimpleDateFormat sdfShort = new SimpleDateFormat(dateFormat);
        try {
            return sdfShort.parse(dateTime);
        } catch (Exception e) {
            logger.error("根据传入的时间字符串及格式化转换时间异常", e);
        }
        return null;
    }

    /**
     * 根据传入的时间Long值转换为时间
     * @param datetime
     * @return
     */
    public static Date getLongToDate(Long datetime) {
        Date date = new Date();
        date.setTime(datetime);
        return date;
    }

    /**
     * 将时间值转换为HH:mm:ss
     * @param data
     * @return
     */

    public static String getToHhMmSs(double data) {
        double totalSeconds = data * 60 * 60;
        Double hour = Math.floor(totalSeconds / 60 / 60);
        Double minute = Math.floor(totalSeconds / 60 % 60);
        Double second = Math.floor(totalSeconds % 60);
        return hour.intValue() + "小时" + minute.intValue() + "分" + second.intValue() + "秒";
    }

    /**
     * 计算两个时间指定内心之间的相差值
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param diffType  处理类型 second minute hour day
     * @return
     */
    public static Integer getDateDiff(Date startTime, Date endTime, String diffType) {
        // 将计算间隔类性字符转换为小写
        diffType = diffType.toLowerCase();
        // 作为除数的数字
        Integer divNum = 1;
        switch (diffType) {
            case "second":
                divNum = 1000;
                break;
            case "minute":
                divNum = 1000 * 60;
                break;
            case "hour":
                divNum = 1000 * 3600;
                break;
            case "day":
                divNum = 1000 * 3600 * 24;
                break;
            default:
                break;
        }
        return Integer.valueOf(((endTime.getTime() - startTime.getTime()) / divNum) + ""); //
    }

    /**
     * 将时间值转换为yyyy-MM-dd HH:mm:ss
     * @param longTime param dateFormat
     * @deprecated 性能不好
     */

    public static String getLongToDateStr(String longTime, String dateFormat) {
        if (dateFormat == null) {
            dateFormat = "yyyy-MM-dd HH:mm:ss";
        }
        if (longTime != null && !"".equals(longTime.trim()) && !longTime.equals("null")) {
            Long time = Long.parseLong(longTime);
            SimpleDateFormat dformat = new SimpleDateFormat(dateFormat);
            String dateStr = dformat.format(time);
            return dateStr;
        }
        return "";

    }

    /**
     * 将一个Object强制转换Date然后获取String字符串
     * @deprecated 性能不好
     */
    public static String getLongToDateStr(Object dateObj, String dateFormat) {
        if (dateObj != null) {
            Date date = (Date) dateObj;
            return getLongToDateStr(date.getTime() + "", dateFormat);
        }
        return "";
    }

    /**
     * @deprecated 性能不好
     */
    public static String getLongToDateStr(Long longTime, String dateFormat) {
        if (dateFormat == null) {
            dateFormat = "yyyy-MM-dd HH:mm:ss";
        }
        if (longTime != null && longTime > 0L) {
            SimpleDateFormat df = new SimpleDateFormat(dateFormat);
            String dateStr = df.format(longTime);
            return dateStr;
        }
        return "";
    }

    /**
     * 时间格式转换
     * @param time
     * @param beforeFormat 转换前的时间格式
     * @param afterFormat  转换后的时间格式
     * @return
     * @throws Exception
     * @deprecated 性能不好
     */
    public static String formatDate(String time, String beforeFormat, String afterFormat) throws Exception {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(beforeFormat);
        Date date = simpleDateFormat.parse(time);
        SimpleDateFormat sdf = new SimpleDateFormat(afterFormat);
        String dateStr = sdf.format(date);
        return dateStr;
    }

    /**
     * 按照传递的格式格式化时间
     * @param time
     * @param format
     * @return
     * @throws Exception
     * @deprecated 性能不好
     */
    public static String formatDate(String time, String format) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        Date date = simpleDateFormat.parse(time);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    /**
     * @deprecated 性能不好
     */
    public static String formatDate(Date date, String format) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        return simpleDateFormat.format(date);
    }

    /**
     * 获取当前时间00：00：00的时间
     * @return
     * @author hujun
     * @Date 创建时间：2018年4月14日 下午2:31:52
     */
    public static Date todayFirstDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * 获取当前时间23：59：59的时间
     * @return
     * @author hujun
     * @Date 创建时间：2018年4月14日 下午2:32:34
     */
    public static Date todayLastDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    /**
     * 根据传入的时间戳返回毫秒时间
     * @return
     */
    public static Long getMillisecond(Long timestamp) {
        if (timestamp != null) {
            String a = timestamp.toString();
            if (10 == a.length()) {
                timestamp *= 1000;
            }
        }
        return timestamp;
    }

    /**
     * 根据传入的时间戳返回秒时间
     * @return
     */
    public static Long getSecond(Long timestamp) {
        if (timestamp != null) {
            String a = timestamp.toString();
            if (13 == a.length()) {
                timestamp /= 1000;
            }
        }
        return timestamp;
    }

    /**
     * 转换秒为小时（保留一位小数）
     * @param second
     * @return
     */
    public static Double getHour(Integer second) {
        Double hour = 0.0;
        if (second != null) {
            BigDecimal bigDecimal = new BigDecimal(second);
            hour = bigDecimal.divide(new BigDecimal(3600), 1, BigDecimal.ROUND_HALF_UP).doubleValue();
        }
        return hour;
    }

    /**
     * 返回传入时间的年月日字符串
     * 时间格式列如180814120626
     */
    public static String getDateYearMouthDay(String paramDate) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMddHHmmss");
            Date date = simpleDateFormat.parse(paramDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            String year = String.valueOf(calendar.get(Calendar.YEAR)); // 年
            String month = String.valueOf(calendar.get(Calendar.MONTH) + 1); // 月
            if (calendar.get(Calendar.MONTH) + 1 < 10) {
                month = "0" + month;
            }
            String day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));//日
            if (calendar.get(Calendar.DAY_OF_MONTH) < 10) {
                day = "0" + day;
            }
            StringBuffer result = new StringBuffer();
            result.append(year).append(month).append(day);
            return result.toString();
        } catch (Exception e) {
            logger.error("获取时间的年月日字符串异常", e);
            return "";
        }
    }

    /**
     * 获取指定日期指定天数之前的时间戳(想获取什么时候(自己指定)多少天之前(自己指定)的日期)
     * 返回精确到毫秒的时间戳
     */
    public static Long getPreviousData(Long appointedTime, int appointedNumber) {
        Long expectResult = 0L;
        if (appointedTime != 0) {
            int dataLength = String.valueOf(appointedTime).length();
            if (dataLength == 10) { // 如果时间戳精确到秒
                appointedTime = appointedTime * 1000;
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date(appointedTime));
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1; // calendar获取到月份为0-11 要加1
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            int second = calendar.get(Calendar.SECOND);
            LocalDateTime time = LocalDateTime.of(year, month, day, hour, minute, second);
            LocalDateTime localDateTime = time.minusDays(appointedNumber);
            ZoneId zoneId = ZoneId.systemDefault();
            ZonedDateTime zdt = localDateTime.atZone(zoneId);
            expectResult = Date.from(zdt.toInstant()).getTime();
        }
        return expectResult;
    }

    /**
     * 计算两个时间戳之间相差多少天(包含开始那天)
     */
    public static Long getTwoAtTimeDifference(Long starTime, Long endTime) {
        Long differenceDay = 0L;
        if (starTime != null && starTime != 0 && endTime != null && endTime != 0) {
            int stLength = String.valueOf(starTime).length();
            if (stLength == 10) { // 如果时间戳精确到秒
                starTime = starTime * 1000;
            }
            int edLength = String.valueOf(endTime).length();
            if (edLength == 10) {
                endTime = endTime * 1000;
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date(starTime));
            Integer startYear = calendar.get(Calendar.YEAR);
            Integer startMonth = calendar.get(Calendar.MONTH) + 1; // calendar类获取的月份为0-11 所以这里要加1
            Integer startDay = calendar.get(Calendar.DAY_OF_MONTH);
            calendar.setTime(new Date(endTime));
            Integer endYear = calendar.get(Calendar.YEAR);
            Integer endMonth = calendar.get(Calendar.MONTH) + 1;
            Integer endDay = calendar.get(Calendar.DAY_OF_MONTH);
            LocalDate start = LocalDate.of(startYear, startMonth, startDay);
            LocalDate end = LocalDate.of(endYear, endMonth, endDay);
            differenceDay = end.toEpochDay() - start.toEpochDay();
        }
        return differenceDay;
    }

    /**
     * 计算两个时间戳之间的日期
     */
    public static int getTwoTimeDifference(Long starTime, Long endTime) {
        int differenceDay = 0;
        if (starTime != null && starTime != 0 && endTime != null && endTime != 0) {
            int stLength = String.valueOf(starTime).length();
            if (stLength == 13) { // 如果时间戳精确到毫秒
                starTime = starTime / 1000;
            }
            int edLength = String.valueOf(endTime).length();
            if (edLength == 13) {
                endTime = endTime / 1000;
            }
            differenceDay = (int) (endTime - starTime) / 86399;
        }
        return differenceDay;
    }

    /**
     * 获取指定月份的第一天的时间戳(00:00:00)
     */
    public static Long getMonthFistDayTime(int year, int month) {
        return ZonedDateTime.now().withYear(year).withMonth(month).withDayOfMonth(1).withHour(0).withMinute(0)
            .withSecond(0).toEpochSecond();
    }

    /**
     * 获取指定月份的最后一天的时间戳
     */
    public static Long getMonthLastDayTime(int year, int month) {
        return ZonedDateTime.now().withYear(year).withMonth(month).with(TemporalAdjusters.lastDayOfMonth()).withHour(0)
            .withMinute(0).withSecond(0).toEpochSecond();
    }

    /**
     * 获取指定年份的指定月的指定天的0点0分0秒时间戳(时间精度为毫秒)
     */
    public static Long getScheduleTime(int year, int month, int day) {
        return ZonedDateTime.now().withYear(year).withMonth(month).withDayOfMonth(day).withHour(0).withMinute(0)
            .withSecond(0).toEpochSecond();
    }

    /**
     * 获取指定年份的指定月的的指定天的23点59分59秒时间戳
     */
    public static Long getScheduleDayEndTime(int year, int month, int day) {
        return ZonedDateTime.now().withYear(year).withMonth(month).withDayOfMonth(day).withHour(23).withMinute(59)
            .withSecond(59).toEpochSecond();
    }

    /**
     * 计算传入的时间距当天的23点59分59秒还有多少秒
     */
    public static Long getDistanceEndTime(Date time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        // 返回的时间戳时间精度是精确到秒,Date.getTime()获取的时间戳的时间精度精确到毫秒
        Long endTime = getScheduleDayEndTime(year, month, day);
        Long starTime = time.getTime() / 1000;
        return endTime - starTime + 1;

    }

    /**
     * 计算传入的时间距当天的0点0分0秒过去了多少秒
     * @param time
     * @return
     */
    public static Long getDistanceStartTime(Date time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        Long starTime = getScheduleTime(year, month, day);
        Long endTime = time.getTime() / 1000;
        return endTime - starTime;
    }

    /**
     * 获取今天的23点59分59秒的时间戳
     */
    public static Long getTodayEndTime() {
        return Timestamp.valueOf(LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 59, 59))).getTime()
            / THOUSAND_MILLISECOND;
    }

    public static Date parseDate(String dateStr) throws ParseException {
        if (StringUtils.isBlank(dateStr)) {
            return null;
        }
        Date date = null;
        int length = dateStr.length();
        if (length == 19) {
            date = DateUtils.parseDate(dateStr, DATE_FORMAT_SHORT);
        } else if (length == 12) {
            date = DateUtils.parseDate("20" + dateStr, DATE_FORMAT);
        } else if (length == 10) {
            date = DateUtils.parseDate(dateStr, DATE_Y_M_D_FORMAT);
        } else if (length == 14) {
            date = DateUtils.parseDate(dateStr, DATE_FORMAT);
        }
        return date;
    }

    public static String minusSecond(String dateStr, long second) throws Exception {
        long time = getStringToLong(dateStr, null);
        time -= second * 1000L;
        return getLongToDateStr(time, null);

    }

    /**
     * 将毫秒值转换为HH:mm:ss
     * @param millis
     * @return
     */
    public static String milliscondToHhMmSs(double millis) {
        Double hour = Math.floor(millis / 60 / 60);
        Double minute = Math.floor(millis / 60 % 60);
        Double second = Math.floor(millis % 60);
        return hour.intValue() + "小时" + minute.intValue() + "分" + second.intValue() + "秒";
    }

    public static void main(String[] args) throws Exception {
        System.out.println(secondsToHhMmSs(47719));
    }

    /**
     * 将秒值转换为HH:mm:ss
     * @param seconds
     * @return
     */
    public static String secondsToHhMmSs(long seconds) {
        StringBuffer resultDate = new StringBuffer();
        long hour = seconds / 60 / 60;
        formatDate(resultDate, hour);
        long minute = seconds / 60 % 60;
        formatDate(resultDate, minute);
        long second = seconds % 60;
        formatDate(resultDate, second);
        return resultDate.toString().substring(0, resultDate.length() - 1);
    }

    private static void formatDate(StringBuffer resultDate, Long time) {
        if (time == 0) {
            resultDate.append("00:");
        } else if (time < 10) {
            resultDate.append("0" + time + ":");
        } else {
            resultDate.append(time + ":");
        }
    }

    /**
     * 秒数转时间
     * @param second 秒
     * @return String
     */
    public static String timeConversion(Long second) {
        if (second != null && second != 0) {
            Long hour = second / (60 * 60);
            Long minute = (second - hour * 60 * 60) % 60 == 0 ? (second - hour * 60 * 60) / 60 :
                (second - hour * 60 * 60) / 60 + 1;
            if (minute == 60) {
                hour += 1;
                minute = 0L;
            }
            if (hour != 0 && minute != 0) {
                return hour + "小时" + minute + "分";
            }
            if (hour != 0) {
                return hour + "小时" + minute + "分";
            }
            if (minute != 0) {
                return minute + "分";
            }
        }
        return "0";
    }

    /**
     * 判断今天是星期几
     */
    public static String getWeekDay() {
        Date today = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(today);
        int weekday = c.get(Calendar.DAY_OF_WEEK);
        if ((weekday - 1) == 0) {
            return "7";
        }
        return (weekday - 1) + "";
    }

    public static String getLongToDayStr(String longTime) {
        if (StringUtils.isNotBlank(longTime) && !"".equals(longTime.trim())) {
            Long time = Long.parseLong(longTime);
            SimpleDateFormat dayFormat = getDayFormatter();
            return dayFormat.format(time);
        }
        return "";
    }

    private static SimpleDateFormat getDayFormatter() {
        return new SimpleDateFormat(DATE_Y_M_D_FORMAT);
    }

    public static String getDayStr(Date date) {
        if (date != null) {
            Long time = date.getTime();
            SimpleDateFormat dayFormat = getDayFormatter();
            return dayFormat.format(time);
        }
        return "";
    }

    public static String getStringToString(String time, String formatFrom, String formatTo) {
        Date stringToDate = getStringToDate(time, formatFrom);
        return getDateToString(stringToDate, formatTo);
    }

    /**
     * 获取昨天0点的时间
     * @param formatTo 返回的格式
     * @return 昨天0点0分0秒的时间
     */
    public static String getYesterdayStartTime(String formatTo) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH) - 1;
        return DateFormatUtils.format(getScheduleTime(year, month, day) * 1000, formatTo);
    }

    /**
     * 获取昨天23点59分59秒的时间
     * @param formatTo 返回的时间格式
     * @return 昨天23点59分59秒的时间
     */
    public static String getYesterdayEndTime(String formatTo) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH) - 1;
        return DateFormatUtils.format(getScheduleDayEndTime(year, month, day) * 1000, formatTo);
    }

    /**
     * 本地时间转换成UTC
     * @param localTime 本地时间
     * @param format    本地时间的格式
     * @return UTC时间戳，ms
     */
    public static long localToUTCTime(String localTime, String format) {
        long localTimeInMillis = getStringToLong(localTime, format);
        //long时间转换成Calendar
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(localTimeInMillis);
        //取得时间偏移量
        int zoneOffset = calendar.get(java.util.Calendar.ZONE_OFFSET);
        //取得夏令时差
        int dstOffset = calendar.get(java.util.Calendar.DST_OFFSET);
        //从本地时间里扣除这些差量，即可以取得UTC时间
        calendar.add(java.util.Calendar.MILLISECOND, -(zoneOffset + dstOffset));
        return calendar.getTimeInMillis();
    }
}
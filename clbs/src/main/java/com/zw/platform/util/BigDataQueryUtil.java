package com.zw.platform.util;

import com.zw.platform.domain.BigDataReport.BigDataQueryDate;
import com.zw.platform.domain.BigDataReport.DateBean;
import com.zw.platform.domain.reportManagement.query.BigDataReportQuery;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.UuidUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 大数据查询工具类
 * @author hujun
 * @date 2018/9/28 13:51
 */
public class BigDataQueryUtil {

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd";

    private static final String DATE_MONTH_FORMAT = "yyyyMM";

    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 获取大数据月表查询参数list
     * @param vehicleIds
     * @param startTime
     * @param endTime
     * @return
     * @throws Exception
     */
    public static List<BigDataReportQuery> getBigDataReportQuery(List<String> vehicleIds, String startTime,
        String endTime) throws Exception {
        /** 初始化查询参数 */
        List<BigDataReportQuery> queries = new ArrayList<>();
        /** 判断查询时间是否跨月并获取查询日期参数 */
        List<BigDataQueryDate> bigDataQueryDates;
        if (startTime.length() == 19) {
            bigDataQueryDates = getBigDataQueryDate(startTime, endTime, DATE_FORMAT, 0, 0, 0);
        } else {
            bigDataQueryDates = getBigDataQueryDate(startTime, endTime, DATE_TIME_FORMAT, 0, 0, 0);
        }
        List<byte[]> monitor = UuidUtils.batchTransition(vehicleIds);
        /** 组装查询参数 */
        for (BigDataQueryDate queryDate : bigDataQueryDates) {
            BigDataReportQuery bigDataReportQuery = new BigDataReportQuery();
            bigDataReportQuery.setMonitorIds(monitor);
            bigDataReportQuery.setStartTime(queryDate.getStartTime());
            bigDataReportQuery.setEndTime(queryDate.getEndTime());
            bigDataReportQuery.setMonth(queryDate.getMonth());
            queries.add(bigDataReportQuery);
        }
        return queries;
    }

    /**
     * 根据传入时间范围判断是否跨月并返回查询参数数据
     * @param startTime
     * @param endTime
     * @return
     * @throws Exception
     */
    public static List<BigDataQueryDate> getBigDataQueryDate(String startTime, String endTime, String pattern,
        Integer hourOfDay, Integer minute, Integer second) throws Exception {
        /** 初始化参数 */
        List<BigDataQueryDate> bigDataQueryDates = new ArrayList<>();
        long start;//查询开始时间
        long end;//查询结束时间
        Date startDate = DateUtils.parseDate(startTime, pattern);
        Date endDate = DateUtils.parseDate(endTime, pattern);
        /** 判断查询时间是否跨月 */
        String startMonth = startTime.substring(5, 7);
        String endMonth = endTime.substring(5, 7);
        if (startMonth.equals(endMonth)) {
            //不跨月
            BigDataQueryDate queryDate = new BigDataQueryDate();
            //查询年月
            String month = DateFormatUtils.format(startDate, DATE_MONTH_FORMAT);
            queryDate.setMonth(month);
            //查询开始、结束时间
            start = startDate.getTime() / 1000;
            end = endDate.getTime() / 1000;
            queryDate.setStartTime(start);
            queryDate.setEndTime(end);
            //存入查询list
            bigDataQueryDates.add(queryDate);
        } else {
            //跨月
            /** 结束月查询范围 */
            //获取结束月第一天时间
            BigDataQueryDate endQueryData = new BigDataQueryDate();
            Calendar endCalendar = Calendar.getInstance();
            endCalendar.setTime(endDate);
            endCalendar.set(Calendar.DAY_OF_MONTH, 1);
            endCalendar.set(Calendar.HOUR_OF_DAY, 0);
            endCalendar.set(Calendar.MINUTE, 0);
            endCalendar.set(Calendar.SECOND, 0);
            //查询开始、结束时间
            endQueryData.setStartTime(endCalendar.getTimeInMillis() / 1000);
            endQueryData.setEndTime(endDate.getTime() / 1000);
            //查询年月
            endQueryData.setMonth(DateFormatUtils.format(endDate, DATE_MONTH_FORMAT));
            bigDataQueryDates.add(endQueryData);
            /** 开始月查询范围 */
            //获取开始月最后一天时间
            BigDataQueryDate startQueryData = new BigDataQueryDate();
            Calendar startCalendar = Calendar.getInstance();
            startCalendar.setTime(startDate);
            startCalendar.set(Calendar.DAY_OF_MONTH, startCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            startCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            startCalendar.set(Calendar.MINUTE, minute);
            startCalendar.set(Calendar.SECOND, second);
            //查询开始、结束时间
            startQueryData.setStartTime(startDate.getTime() / 1000);
            startQueryData.setEndTime(startCalendar.getTimeInMillis() / 1000);
            //查询年月
            startQueryData.setMonth(DateFormatUtils.format(startDate, DATE_MONTH_FORMAT));
            bigDataQueryDates.add(startQueryData);
            /** 判断是否为跨三月特殊情况 1、2、3平年的时候会出现此情况 */
            BigDecimal bs = new BigDecimal(startDate.getTime());
            BigDecimal be = new BigDecimal(endDate.getTime());
            long centerTime = bs.add(be).divide(new BigDecimal(2)).setScale(0, BigDecimal.ROUND_HALF_UP).longValue();
            String centerMonth = DateFormatUtils.format(centerTime, pattern);
            centerMonth = centerMonth.substring(5, 7);
            if (!startMonth.equals(centerMonth) && !endMonth.equals(centerMonth)) {
                //跨三月
                BigDataQueryDate centerQueryData = new BigDataQueryDate();
                /** 中间月查询范围 */
                //获取中间月第一天及最后一天时间
                Calendar centerStartCalendar = Calendar.getInstance();
                centerStartCalendar.setTimeInMillis(centerTime);
                centerStartCalendar.set(Calendar.DAY_OF_MONTH, 1);
                centerStartCalendar.set(Calendar.HOUR_OF_DAY, 0);
                centerStartCalendar.set(Calendar.MINUTE, 0);
                centerStartCalendar.set(Calendar.SECOND, 0);
                Calendar centerEndCalendar = Calendar.getInstance();
                centerEndCalendar.setTimeInMillis(centerTime);
                centerEndCalendar.set(Calendar.DAY_OF_MONTH, centerEndCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                centerEndCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                centerEndCalendar.set(Calendar.MINUTE, minute);
                centerEndCalendar.set(Calendar.SECOND, second);
                //查询开始、结束时间
                centerQueryData.setStartTime(centerStartCalendar.getTimeInMillis() / 1000);
                centerQueryData.setEndTime(centerEndCalendar.getTimeInMillis() / 1000);
                //查询年月
                centerQueryData.setMonth(DateFormatUtils.format(centerTime, DATE_MONTH_FORMAT));
                bigDataQueryDates.add(centerQueryData);
            }
        }
        return bigDataQueryDates;
    }

    /**
     * 获取大数据月表查询参数list(与上面那个方面不同的是,此方法的时间可以是跨年,跨多月的)
     */
    public static List<BigDataReportQuery> getBigMonthDataReportQuery(List<String> vehicleIds, Long startTime,
        Long endTime) {
        /** 初始化查询参数 */
        List<BigDataReportQuery> queries = new ArrayList<>();
        /** 判断查询时间是否跨月并获取查询日期参数 */
        List<DateBean> bigDataQueryDates = getQueryTime(startTime, endTime);
        List<byte[]> monitor = UuidUtils.batchTransition(vehicleIds);
        /** 组装查询参数 */
        for (DateBean date : bigDataQueryDates) {
            BigDataReportQuery bigDataReportQuery = new BigDataReportQuery();
            bigDataReportQuery.setMonitorIds(monitor);
            bigDataReportQuery.setStartTime(date.getStartTime() / 1000);
            bigDataReportQuery.setEndTime(date.getEndTime() / 1000);
            bigDataReportQuery.setMonth(DateFormatUtils.format(date.getStartTime(), DATE_MONTH_FORMAT));
            queries.add(bigDataReportQuery);
        }
        return queries;
    }

    /**
     * 获取两个时间戳相差的每个月份的每月的第一天和最后一天,主要解决的问题是,大数据月表跨多月查询的问题 emmmmmm....
     * eg: 有两个时间 2018-04-05  2019-04-05 如果要获取这两个时间间隔的每个月份第一天和最后一天的时间戳的话就可以调这个方法
     * 但开始时间那个月获取的是05号和月的最后一天,结束时间那个月获取的是04月第一天和05号,中间相隔的月份就是从月的第一天和最后一天的时间戳
     */
    private static List<DateBean> getQueryTime(Long startTime, Long endTime) {
        /** 初始化参数 */
        List<DateBean> queryDates = new ArrayList<>();
        if (startTime != 0 && endTime != 0) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date(startTime));
            int startYear = calendar.get(Calendar.YEAR); // 开始时间的年
            calendar.setTime(new Date(endTime));
            int endYear = calendar.get(Calendar.YEAR); // 结束时间的年
            if (startYear == endYear) { // 年份相同
                queryDates.addAll(getTwoDataEveryMonthFistDayAndLastDay(startTime, endTime));
            } else { // 年份不同
                // 获取开始年的最后一个月的最后一天的时间戳
                Long yearLastDay = DateUtil.getScheduleTime(startYear, 12, 31) * 1000;
                queryDates.addAll(getTwoDataEveryMonthFistDayAndLastDay(startTime, yearLastDay));

                // 获取结束时间的年份的第一个月的第一天的时间戳
                Long yearFistDay = DateUtil.getScheduleTime(endYear, 1, 1) * 1000;
                queryDates.addAll(getTwoDataEveryMonthFistDayAndLastDay(yearFistDay, endTime));

            }
        }
        return queryDates;
    }

    private static List<DateBean> getTwoDataEveryMonthFistDayAndLastDay(Long startTime, Long endTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(startTime));
        int startYear = calendar.get(Calendar.YEAR); // 开始时间的年
        int startMonth = calendar.get(Calendar.MONTH) + 1; // 开始时间的月
        calendar.setTime(new Date(endTime));
        int endYear = calendar.get(Calendar.YEAR); // 结束时间的年
        int endMonth = calendar.get(Calendar.MONTH) + 1; // 结束时间的月
        List<DateBean> result = new ArrayList<>();
        if (endMonth == startMonth) { // 同一月
            DateBean data = new DateBean();
            data.setStartTime(startTime);
            data.setEndTime(endTime);
            result.add(data);
        } else {
            int value = endMonth - startMonth; // 两个月份的差值
            DateBean data;
            for (int index = 0; index <= value; index++) {
                data = new DateBean();
                int resultMonth = startMonth + index;
                Long resultMonthFistTime;
                Long resultMonthEndTime;
                if (resultMonth == startMonth) { // 第一个月
                    resultMonthFistTime = startTime;
                    resultMonthEndTime = DateUtil.getMonthLastDayTime(startYear, startMonth) * 1000;
                } else if (resultMonth == endMonth) { // 最后一个月
                    resultMonthFistTime = DateUtil.getMonthFistDayTime(endYear, endMonth) * 1000;
                    resultMonthEndTime = endTime;
                } else { // 中间的月
                    resultMonthFistTime = DateUtil.getMonthFistDayTime(startYear, resultMonth) * 1000;
                    resultMonthEndTime = DateUtil.getMonthLastDayTime(startYear, resultMonth) * 1000;
                }
                data.setStartTime(resultMonthFistTime);
                data.setEndTime(resultMonthEndTime);
                result.add(data);
            }
        }
        return result;
    }

    /**
     * 计算两个时间戳之间每天日期
     * @param startTime
     * @param endTime
     * @return
     */
    public static List<String> getTwoTimeMiddleEveryDayTime(Long startTime, Long endTime) {
        List<DateBean> everyMonthTime = getQueryTime(startTime, endTime);
        List<String> result = new ArrayList<>();
        if (everyMonthTime != null && everyMonthTime.size() > 0) {
            // 根据获取到的相差的月份的每个月份的第一天和最后一天的时间戳,解析出每一天的0点到23点59分59秒的时间戳
            for (DateBean dateBean : everyMonthTime) {
                Long start = dateBean.getStartTime(); // 月的开始时间
                Long end = dateBean.getEndTime(); // 月的结束时间 两个时间为同一月
                if (startTime != 0 && endTime != 0) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(new Date(start));
                    int startYear = calendar.get(Calendar.YEAR);
                    int startMonth = calendar.get(Calendar.MONTH) + 1;
                    int startDay = calendar.get(Calendar.DAY_OF_MONTH); // 月开始的日期
                    calendar.setTime(new Date(end));
                    int endDay = calendar.get(Calendar.DAY_OF_MONTH);
                    for (int index = startDay; index <= endDay; index++) {
                        String month = startMonth < 10 ? "0" + startMonth : String.valueOf(startMonth);
                        String day = index < 10 ? "0" + index : String.valueOf(index);
                        String onlineDate = startYear + "" + month + "" + day;
                        result.add(onlineDate);
                    }
                }
            }
        }
        return result;
    }
}

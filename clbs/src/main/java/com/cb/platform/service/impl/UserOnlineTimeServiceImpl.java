package com.cb.platform.service.impl;

import com.cb.platform.domain.MonthGroupOnlineTime;
import com.cb.platform.domain.MonthOnlineTime;
import com.cb.platform.domain.OnlineTimeGroupMonth;
import com.cb.platform.domain.OnlineTimeUserMonth;
import com.cb.platform.domain.UserLogin;
import com.cb.platform.repository.mysqlDao.OnlineTimeDao;
import com.cb.platform.service.UserOnlineTimeService;
import com.zw.platform.basic.dto.UserDTO;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.util.BigDataQueryUtil;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.excel.ExportExcel;
import com.zw.platform.util.excel.ExportExcelParam;
import com.zw.platform.util.excel.ExportExcelUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserOnlineTimeServiceImpl implements UserOnlineTimeService {

    private static Logger logger = LogManager.getLogger(UserOnlineTimeServiceImpl.class);

    @Autowired
    private OnlineTimeDao onlineTimeDao;

    @Autowired
    private UserService userService;

    @Autowired
    private OrganizationService organizationService;

    private static final Double DAY_SECOND = 86400.0;

    private static final long HOUR_SECOND = 3600;

    private static final Double DAY_HOUR = 24.0;

    private static final Double RESULT_DAY_HOUR = 23.99;

    private DecimalFormat decimalFormat = new DecimalFormat();// 数字格式化类 保留两位小数

    private static final String DATE_FORM = "yyyy-MM-dd HH:mm:ss";

    private static final String DATE_FORM1 = "yyyyMMdd";

    private static final String DATE_FORM2 = "yyyy-MM-dd";

    @Override
    public List<MonthGroupOnlineTime> findMonthGroupOnlineTime(String groupId, String day1, String day2)
        throws Exception {
        List<String> groupIds = Arrays.asList(groupId.split(","));
        // 企业下用户查询时间内的登录明细
        List<UserLogin> loginData = onlineTimeDao.getGroupUserOnlineData(groupIds, day1, day2);
        List<MonthGroupOnlineTime> result = new ArrayList<>();
        if (loginData.size() > 0) {
            Map<String, List<UserLogin>> everyGroupData = getEveryGroupLoginData(loginData);
            getEveryGroupDuration(everyGroupData, result, day1);
        }
        return result;
    }

    /**
     * 获取每个企业的所有用户的登录明细
     */
    private Map<String, List<UserLogin>> getEveryGroupLoginData(List<UserLogin> loginData) {
        Map<String, List<UserLogin>> everyGroupData = new HashMap<>();
        if (loginData != null && loginData.size() > 0) {
            loginData.forEach(login -> {
                String mapKey = login.getGroupId();
                List<UserLogin> userLogin = everyGroupData.getOrDefault(mapKey, new ArrayList<>());
                if (userLogin == null) {
                    userLogin = new ArrayList<>();
                }
                userLogin.add(login);
                everyGroupData.put(mapKey, userLogin);
            });
        }
        return everyGroupData;
    }

    /**
     * 计算每个企业的在线时长
     */
    private void getEveryGroupDuration(Map<String, List<UserLogin>> everyGroupData, List<MonthGroupOnlineTime> result,
        String day1) throws Exception {
        if (everyGroupData.size() > 0) {
            Set<String> orgIds = everyGroupData.keySet();
            Map<String, OrganizationLdap> orgMap = organizationService.getOrgByUuids(orgIds);
            for (Map.Entry<String, List<UserLogin>> entry1 : everyGroupData.entrySet()) {
                String mapKey = entry1.getKey(); //  组织id
                double value = 0;
                OrganizationLdap groupInfo = orgMap.get(mapKey); // 根据组织uuid查询组织详情
                if (groupInfo != null) {
                    List<UserLogin> mapValue = entry1.getValue();
                    if (mapValue != null && mapValue.size() > 0) {
                        MonthGroupOnlineTime monthOnlineTime = new MonthGroupOnlineTime();
                        value += groupMonthData(monthOnlineTime, day1, mapValue);
                        String groupName = groupInfo.getName();
                        monthOnlineTime.setGroupName(groupName);
                        monthOnlineTime.setSumNUmber(numberDataDis(value));
                        result.add(monthOnlineTime);
                    }
                }
            }
        }
    }

    /**
     * 计算在线时长(需要将企业下所有用户的登录明细取出来计算,如果取企业下在线时长最长的那个用户的在线时长作为企业当天的在线时长,会有误差
     * ,eg:某企业下用户小明1月20号0点到18点在线,跟小明同企业用户小红1月20号20点到22点在线,如果取企业下在线时长最长的那个用户在线时间最为企业当天
     * 在线时长的话,该企业1月20号在线时长为18h,但该企业下用户1月20号20点到22在线,所以该企业1月20号真正的在线时长应该是20h)
     * @return
     * @throws Exception
     */
    private double groupMonthData(Object resultType, String startTime, List<UserLogin> loginData) throws Exception {
        Map<String, Double> groupUserAcrossDay = new HashMap<>(); // 企业下用户查询时间段内登录的数据
        List<UserLogin> acrossDay = new ArrayList<>(); // 跨天的数据
        Map<String, List<UserLogin>> everyDayOnline = new HashMap<>(); // 每天的登录明显数据(不包含跨天)
        // 过滤掉下线时间为null的数据,并将跨天登录和没有跨天登录的数据分开,返回没有跨天的数据
        List<UserLogin> everyLogin = getGroupMonthEveryDayOnlineTime(loginData, acrossDay);
        disposeAcrossData(acrossDay, everyLogin, startTime); // 处理跨天的数据
        // 将everyLogin所有的登录明细数据根据上线时间排序,并以日期为key,将日期相同的数据存放到map
        orderByOnlineTime(everyLogin);
        dataAnalysis(everyLogin, everyDayOnline);
        disposeUserEveryDayOnlineData(everyDayOnline, groupUserAcrossDay);
        double value = 0;
        if (groupUserAcrossDay.size() > 0) {
            for (Map.Entry<String, Double> entry1 : groupUserAcrossDay.entrySet()) {
                Date onlineDay = DateUtils.parseDate(entry1.getKey(), DATE_FORM1);
                Calendar calOnline = Calendar.getInstance();
                calOnline.setTime(onlineDay);
                Double onTime = entry1.getValue();
                Class<?> cl = resultType.getClass();
                int onDay = calOnline.get(Calendar.DAY_OF_MONTH); // 上线时间的天
                if (cl == MonthGroupOnlineTime.class || cl == MonthOnlineTime.class) {
                    Method setFunc = cl.getMethod("setDayData", Integer.class, Double.class);
                    setFunc.invoke(resultType, onDay, onTime);
                }
                value += onTime; // 合计
            }
        }
        return value;
    }

    /**
     * 排序
     */
    private void orderByOnlineTime(List<UserLogin> everyLogin) {
        if (everyLogin.size() > 1) {
            // 升序排序
            Collections.sort(everyLogin, (obj0, obj1) -> {
                try {
                    Long obj0OnlineTime = cleanDate(obj0.getOnlineTime()).getTime();
                    Long obj1OnlineTime = cleanDate(obj1.getOnlineTime()).getTime();
                    if (obj1OnlineTime > obj0OnlineTime) {
                        return -1;
                    } else if (obj1OnlineTime < obj0OnlineTime) {
                        return 1;
                    } else {
                        return 0;
                    }
                } catch (Exception e) {
                    return -1;
                }

            });
        }
    }

    private void dataAnalysis(List<UserLogin> everyLogin, Map<String, List<UserLogin>> everyDayOnline) {
        if (everyLogin != null && everyLogin.size() > 0) {
            everyLogin.forEach(every -> {
                Date olTime = cleanDate(every.getOnlineTime());
                String acrossDayKey = DateFormatUtils.format(olTime, DATE_FORM1);
                List<UserLogin> dayData = everyDayOnline.putIfAbsent(acrossDayKey, new ArrayList<>());
                if (dayData == null) {
                    dayData = new ArrayList<>();
                }
                dayData.add(every);
                everyDayOnline.put(acrossDayKey, dayData);
            });

        }

    }

    /**
     * 处理没有跨天的用户登录数据,计算企业的在线时长
     */
    private void disposeUserEveryDayOnlineData(Map<String, List<UserLogin>> everyDayOnline,
        Map<String, Double> groupUserAcrossDay) {
        if (everyDayOnline != null && everyDayOnline.size() > 0) {
            for (Map.Entry<String, List<UserLogin>> entry1 : everyDayOnline.entrySet()) {
                String mapKey = entry1.getKey();
                List<UserLogin> data = entry1.getValue();
                if (data != null) {
                    Double resultTime = (double) computeEveryDayOnlineTime(data);
                    Double dayOnlineTime = numberDataDis(resultTime / HOUR_SECOND);
                    if (dayOnlineTime.doubleValue() == RESULT_DAY_HOUR.doubleValue()) {
                        dayOnlineTime = DAY_HOUR;
                    } else if (dayOnlineTime > RESULT_DAY_HOUR) {
                        dayOnlineTime = DAY_HOUR;
                    }
                    Double dayDistanceTime = groupUserAcrossDay.get(mapKey);
                    if (dayDistanceTime == null) {
                        groupUserAcrossDay.put(mapKey, dayOnlineTime);
                    } else {
                        if (dayOnlineTime > dayDistanceTime) {
                            groupUserAcrossDay.put(mapKey, dayOnlineTime);
                        }
                    }
                }
            }
        }
    }

    /**
     * 计算每天的在线时长
     */
    private Long computeEveryDayOnlineTime(List<UserLogin> resultDate) {
        Long onlineTime = 0L;
        Long onMaxTime = 0L;// 最大时间
        Long onMinTime = 0L; // 最小时间
        for (int onIndex = 0; onIndex <= resultDate.size() - 1; onIndex++) {
            UserLogin data1 = resultDate.get(onIndex);
            Long middleOnTime = timeDispose(data1.getOnlineTime());
            Long middleOfTime = timeDispose(data1.getOfflineTime());
            if (resultDate.size() == 1) { // 直接计算在线时间
                onlineTime = middleOfTime - middleOnTime;
            } else {
                if (onIndex + 1 != resultDate.size()) { // 不是最后一条数据
                    if (onMaxTime == 0 && onMinTime == 0) { // 开始计算
                        onMaxTime = middleOfTime;
                        onMinTime = middleOnTime;
                    } else {
                        if (middleOnTime > onMaxTime && middleOfTime > onMaxTime) { // 开始时间和结束时间不在区间内
                            onlineTime += onMaxTime - onMinTime;
                            onMinTime = middleOnTime;
                            onMaxTime = middleOfTime;
                        } else if (middleOnTime >= onMinTime && middleOnTime <= onMaxTime
                            && middleOfTime > onMaxTime) { // 开始时间在区间内,结束时间不在区间内,更新maxTime
                            onMaxTime = middleOfTime;
                        } else if (middleOnTime < onMinTime
                            && middleOfTime <= onMaxTime) { // 结束时间在区间内,开始时间不在区间内,更新mixTime
                            onMinTime = middleOnTime;
                        }
                    }
                } else { //  最后一条数据
                    if (onMaxTime != 0 && onMinTime != 0 && middleOnTime >= onMinTime && middleOnTime <= onMaxTime
                        && middleOfTime >= onMinTime && middleOfTime <= onMaxTime) { // 最后一条数据的开始时间和结束时间包含在上一条的区间内
                        onlineTime += onMaxTime - onMinTime;
                    } else if (onMaxTime != 0 && onMinTime != 0 && middleOnTime >= onMinTime
                        && middleOnTime <= onMaxTime && middleOfTime >= onMinTime
                        && middleOfTime > onMaxTime) { // 最后一条数据的结束时间不包含在上一条的区间内,但开始时间包含
                        onMaxTime = middleOfTime;
                        onlineTime += onMaxTime - onMinTime;
                    } else if (onMaxTime != 0 && onMinTime != 0 && middleOnTime > onMaxTime) { // 不包含在上一个区间内
                        onlineTime += onMaxTime - onMinTime; // 计算上一个区间的时长
                        onlineTime += middleOfTime - middleOnTime; // 计算最后一条记录的时长
                    } else if (onMaxTime == 0 && onMinTime == 0) {
                        onlineTime += middleOfTime - middleOnTime; // 计算最后一条记录的时长
                    }
                }
            }
        }
        return onlineTime;
    }

    /**
     * 处理跨天数据,将跨天的数据解析成每一天的登录时间(eg:2019-01-02 05:05:05- 2019-01-05 05:05:05 解析出来的数据为
     * 2019-01-02 05:05:05 - 2019-01-02 23:59:59,2019-01-03 00:00:00-2019-01-03 23:59:59 ...
     * 2019-01-05 00:00:00 2019-01-05 05:05:05)
     */
    private void disposeAcrossData(List<UserLogin> acrossDay, List<UserLogin> everyDayOnline, String monthStartTime)
        throws Exception {
        if (acrossDay.size() > 0) {
            List<UserLogin> resultData = new ArrayList<>();
            Long maxTime = 0L;// 最大时间
            Long minTime = 0L; // 最小时间
            for (int index = 0; index <= acrossDay.size() - 1; index++) {
                if (acrossDay.size() == 1) {
                    resultData.add(acrossDay.get(index));
                } else {
                    UserLogin data1 = acrossDay.get(index);
                    Long middleOnTime = timeDispose(data1.getOnlineTime());
                    Long middleOfTime = timeDispose(data1.getOfflineTime());
                    if (maxTime == 0 && minTime == 0) { // 开始计算
                        maxTime = middleOfTime;
                        minTime = middleOnTime;
                    } else {
                        if (index + 1 != acrossDay.size()) { // 不是最后一条数据
                            if (middleOnTime > minTime && middleOnTime > maxTime) { // 开始时间和结束时间不在区间内
                                UserLogin userLogin = new UserLogin();
                                userLogin.setOnlineTime(DateFormatUtils.format(minTime * 1000, DATE_FORM));
                                userLogin.setOfflineTime(DateFormatUtils.format(maxTime * 1000, DATE_FORM));
                                resultData.add(userLogin);
                                minTime = middleOnTime;
                                maxTime = middleOfTime;
                            } else if (middleOnTime >= minTime && middleOnTime <= maxTime
                                && middleOfTime > maxTime) { // 开始时间在区间内,结束时间不在区间内,更新maxTime
                                maxTime = middleOfTime;
                            } else if (middleOnTime < minTime
                                && middleOfTime <= maxTime) { // 结束时间在区间内,开始时间不在区间内,更新mixTime
                                minTime = middleOnTime;
                            }
                        } else { //  最后一条数据(分为几种情况,1.开始时间和结束时间不包含在上一条的区间内 2.开始时间和结束时间包含在上一条的区间内)
                            if (middleOnTime >= minTime && middleOnTime <= maxTime && middleOfTime >= minTime
                                && middleOfTime <= maxTime) { // 最后一条数据的开始时间和结束时间包含在上一条的区间内
                                UserLogin userLogin = new UserLogin();
                                userLogin.setOnlineTime(DateFormatUtils.format(minTime * 1000, DATE_FORM));
                                userLogin.setOfflineTime(DateFormatUtils.format(maxTime * 1000, DATE_FORM));
                                resultData.add(userLogin);
                            } else { // 不包含
                                // 结束上一个区间的
                                UserLogin userLogin = new UserLogin();
                                userLogin.setOnlineTime(DateFormatUtils.format(minTime * 1000, DATE_FORM));
                                userLogin.setOfflineTime(DateFormatUtils.format(maxTime * 1000, DATE_FORM));
                                resultData.add(userLogin);
                                // 开始最后一个区间
                                UserLogin lastUserLogin = new UserLogin();
                                lastUserLogin.setOnlineTime(DateFormatUtils.format(middleOnTime * 1000, DATE_FORM));
                                lastUserLogin.setOfflineTime(DateFormatUtils.format(middleOfTime * 1000, DATE_FORM));
                                resultData.add(lastUserLogin);
                            }
                        }
                    }
                }
            }
            if (resultData.size() > 0) {
                disposeResultData(resultData, everyDayOnline, monthStartTime);
            }
        }
    }

    /**
     * 计算跨天数据每天的在线时长
     */
    private void disposeResultData(List<UserLogin> acrossDay, List<UserLogin> everyDayOnline, String monthStartTime)
        throws Exception {
        for (UserLogin userLogin : acrossDay) { // 循环跨天的数据(得出最大时间)
            if (userLogin != null && StringUtils.isNotBlank(userLogin.getOnlineTime()) && StringUtils
                .isNotBlank(userLogin.getOfflineTime())) {
                Date loginDate = cleanDate(userLogin.getOnlineTime());
                Date offDate = cleanDate(userLogin.getOfflineTime());
                Long loginTime = loginDate.getTime();
                Long offTime = offDate.getTime();
                List<String> everyDayTime = BigDataQueryUtil.getTwoTimeMiddleEveryDayTime(loginTime, offTime);
                // 如果包含跨月登录的天,则去除掉
                if (everyDayTime != null && everyDayTime.size() > 0) {
                    String strOnlineTime = DateFormatUtils.format(loginTime, DATE_FORM1);
                    String strOfferTime = DateFormatUtils.format(offTime, DATE_FORM1);
                    Date monthTime = DateUtils.parseDate(monthStartTime, DATE_FORM2);
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(monthTime);
                    int month = calendar.get(Calendar.MONTH) + 1;
                    for (String day : everyDayTime) {
                        Date resultTime = DateUtils.parseDate(day, DATE_FORM1);
                        calendar.setTime(resultTime);
                        int resultYear = calendar.get(Calendar.YEAR);
                        int resultMonth = calendar.get(Calendar.MONTH) + 1;
                        int resultDay = calendar.get(Calendar.DAY_OF_MONTH);
                        if (resultMonth == month) {
                            UserLogin everyData = new UserLogin();
                            String online;
                            String offline;
                            if (strOnlineTime.equals(day)) { // 上线的天
                                online = userLogin.getOnlineTime();
                                offline = DateFormatUtils
                                    .format(DateUtil.getScheduleDayEndTime(resultYear, resultMonth, resultDay) * 1000,
                                        DATE_FORM);
                            } else if (strOfferTime.equals(day)) { // 下线的天
                                online = DateFormatUtils
                                    .format(DateUtil.getScheduleTime(resultYear, resultMonth, resultDay) * 1000,
                                        DATE_FORM);
                                offline = userLogin.getOfflineTime();
                            } else { // 中间的天
                                online = DateFormatUtils
                                    .format(DateUtil.getScheduleTime(resultYear, resultMonth, resultDay) * 1000,
                                        DATE_FORM);
                                offline = DateFormatUtils
                                    .format(DateUtil.getScheduleDayEndTime(resultYear, resultMonth, resultDay) * 1000,
                                        DATE_FORM);
                            }
                            everyData.setOnlineTime(online);
                            everyData.setOfflineTime(offline);
                            everyDayOnline.add(everyData);
                        }
                    }
                }
            }
        }
    }

    /**
     * 将企业下用户所有的登录明细分开跨天和不跨天的数据,并过滤掉下线时间为空的记录
     */
    private List<UserLogin> getGroupMonthEveryDayOnlineTime(List<UserLogin> loginData, List<UserLogin> acrossDay) {
        List<UserLogin> resultData = new ArrayList<>();
        if (loginData != null && loginData.size() > 0) {
            for (int i = 0; i < loginData.size(); i++) { // 把离线时长为null的数据过滤
                if (loginData.get(i).getOnlineTime() != null && !loginData.get(i).getOnlineTime().isEmpty()
                    && loginData.get(i).getOfflineTime() != null && !loginData.get(i).getOfflineTime().isEmpty()) {
                    String getOnTime = loginData.get(i).getOnlineTime(); // 上线时间
                    String belowOnTime = loginData.get(i).getOfflineTime(); // 下线时间
                    Date olTime = cleanDate(getOnTime);
                    Date ofTime = cleanDate(belowOnTime);
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(olTime);
                    int olYear = calendar.get(Calendar.YEAR); // 上下线记录的上线时间的年
                    int olMonth = calendar.get(Calendar.MONTH) + 1; // 上下线记录的上线时间的月
                    int olDay = calendar.get(Calendar.DAY_OF_MONTH); // 上下线记录的上线时间的天
                    calendar.setTime(ofTime);
                    int ofYear = calendar.get(Calendar.YEAR); // 上下线记录的下线时间的年
                    int ofMonth = calendar.get(Calendar.MONTH) + 1; // 上下线记录的下线时间的月
                    int ofDay = calendar.get(Calendar.DAY_OF_MONTH); // 上下线记录的下线时间的天
                    // 没有跨天
                    if (olYear == ofYear && olMonth == ofMonth && olDay == ofDay) {
                        resultData.add(loginData.get(i));
                    } else { // 跨天
                        acrossDay.add(loginData.get(i));
                    }
                }
            }
        }
        return resultData;
    }

    /**
     * 时间处理,返回时间戳
     * @return
     */
    private long timeDispose(String data) {
        return DateUtil.getStringToDate(data, "yyyy-MM-dd HH:mm:ss").getTime() / 1000;
    }

    @Override
    public boolean exportMonthGroup(String title, int type, HttpServletResponse response, String groupId,
        String nowMonth, String nextMonth) throws Exception {
        List<MonthGroupOnlineTime> data = findMonthGroupOnlineTime(groupId, nowMonth, nextMonth);
        List<OnlineTimeGroupMonth> resultData = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        Date monthTime = DateUtil.getStringToDate(nowMonth, "yyyy-MM-dd");
        calendar.setTime(monthTime != null ? monthTime : new Date());
        // 所选月份最大天数
        int lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        if (data != null && data.size() > 0) {
            for (MonthGroupOnlineTime monthGroupOnlineTime : data) {
                OnlineTimeGroupMonth onlineTimeGroupMonth = new OnlineTimeGroupMonth(monthGroupOnlineTime, lastDay);
                resultData.add(onlineTimeGroupMonth);
            }
        }
        ExportExcelUtil.setResponseHead(response, "道路运输企业月在线时长统计");
        export(title, type, response, resultData, lastDay);
        return true;
    }

    private void export(String title, int type, HttpServletResponse res, List<OnlineTimeGroupMonth> resultData,
        int lastDay) throws IOException {
        List<String> tableHeadList = new ArrayList<>();
        List<String> requiredList = new ArrayList<>();
        List<Object> exportList;
        tableHeadList.add("道路运输企业");
        if (resultData != null) {
            for (int i = 1; i <= lastDay; i++) {
                tableHeadList.add(String.valueOf(i));
            }
        }
        tableHeadList.add("合计");
        Map<String, String[]> selectMap = new HashMap<>();
        ExportExcel export = new ExportExcel(tableHeadList, requiredList, selectMap);
        CellStyle cellStyle = export.createStyle(); // 单元格样式
        DataFormat format = export.createDataFormat(); // 数据格式
        Row row;
        if (resultData != null && resultData.size() > 0) {
            for (int j = 0; j < resultData.size(); j++) {
                exportList = new ArrayList<>();
                OnlineTimeGroupMonth onlineTimeGroupMonth = resultData.get(j);
                exportList.add(onlineTimeGroupMonth.getGroupName());
                for (int k = 0; k < onlineTimeGroupMonth.getDays().length; k++) {
                    exportList.add(onlineTimeGroupMonth.getDays()[k]); // 每天的在线时长数据
                }
                exportList.add(onlineTimeGroupMonth.getSumNUmber()); // 合计
                row = export.addRow();
                for (int x = 0; x < exportList.size(); x++) {
                    export.addCell(row, x, exportList.get(x), HorizontalAlignment.GENERAL, Class.class, cellStyle,
                        format);
                }
            }
        }
        OutputStream out = res.getOutputStream();
        export.write(out);
        out.close();
    }

    @Override
    public List<MonthOnlineTime> findMonthGroupUserOnlineTime(String userId, String day1, String day2)
        throws Exception {
        List<String> groupIds = Arrays.asList(userId.split(","));
        List<UserLogin> userOnlineTimes = onlineTimeDao.getUserOnlineData(groupIds, day1, day2);
        if (userOnlineTimes.size() > 0) {
            return userMonthOnlineTime(userOnlineTimes, day1);
        }
        return null;
    }

    @Override
    public boolean exportMonthUser(String title, int type, HttpServletResponse response, String groupId,
        String nowMonth, String nextMonth) throws Exception {
        List<MonthOnlineTime> data = findMonthGroupUserOnlineTime(groupId, nowMonth, nextMonth); // 用户月在线数据
        List<OnlineTimeUserMonth> resultData = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        Date monthTime = DateUtil.getStringToDate(nowMonth, "yyyy-MM-dd");
        calendar.setTime(monthTime != null ? monthTime : new Date());
        // 所选月份最大天数
        int lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        if (data != null && data.size() > 0) {
            for (MonthOnlineTime monthOnlineTime : data) {
                OnlineTimeUserMonth onlineTimeUserMonth = new OnlineTimeUserMonth(monthOnlineTime, lastDay);
                resultData.add(onlineTimeUserMonth);
            }

        }
        ExportExcelUtil.setResponseHead(response, "道路运输企业用户月在线时长统计");
        exportUserTime(title, type, response, resultData, lastDay);
        return true;
    }

    private void exportUserTime(String title, int type, HttpServletResponse res, List<OnlineTimeUserMonth> resultData,
        int lastDay) throws Exception {
        List<String> tableHeadList = new ArrayList<>();
        List<String> requiredList = new ArrayList<>();
        List<Object> exportList;
        tableHeadList.add("用户名");
        tableHeadList.add("道路运输企业");
        if (resultData != null) {
            for (int i = 1; i <= lastDay; i++) {
                tableHeadList.add(String.valueOf(i));
            }
        }
        tableHeadList.add("合计");
        Map<String, String[]> selectMap = new HashMap<>();
        ExportExcel export = new ExportExcel(tableHeadList, requiredList, selectMap);
        CellStyle cellStyle = export.createStyle(); // 单元格样式
        DataFormat format = export.createDataFormat(); // 数据格式
        if (resultData != null && resultData.size() > 0) {
            for (int j = 0; j < resultData.size(); j++) {
                exportList = new ArrayList<>();
                OnlineTimeUserMonth onlineTimeUserMonth = resultData.get(j);
                exportList.add(onlineTimeUserMonth.getUserName()); // 用户名
                exportList.add(onlineTimeUserMonth.getGroupName()); // 所属企业
                for (int k = 0; k < onlineTimeUserMonth.getDays().length; k++) {
                    exportList.add(onlineTimeUserMonth.getDays()[k]); // 每天的在线时长数据
                }
                exportList.add(onlineTimeUserMonth.getSumNUmber()); // 合计
                Row row = export.addRow();
                for (int x = 0; x < exportList.size(); x++) {
                    export.addCell(row, x, exportList.get(x), HorizontalAlignment.GENERAL, Class.class, cellStyle,
                        format);
                }

            }
        }
        OutputStream out = res.getOutputStream();
        export.write(out);
        out.close();
    }

    /**
     * 数据处理
     * @param userOnlineTimes
     * @return
     */
    private List<MonthOnlineTime> userMonthOnlineTime(List<UserLogin> userOnlineTimes, String day1) throws Exception {
        List<MonthOnlineTime> result = new ArrayList<>();
        Map<String, List<UserLogin>> userData = new HashMap<>();
        for (UserLogin userOnlineTime : userOnlineTimes) {
            String userId = userOnlineTime.getUserId();
            List<UserLogin> dateData = userData.getOrDefault(userId, new ArrayList<>());
            dateData.add(userOnlineTime);
            userData.putIfAbsent(userId, dateData);
        }
        Set<String> orgIds = new HashSet<>();
        userData.values()
            .forEach(o -> orgIds.addAll(o.stream().map(UserLogin::getGroupId).collect(Collectors.toSet())));
        Map<String, OrganizationLdap> orgMap = organizationService.getOrgByUuids(orgIds);
        Map<String, String> userMap = userService.getUserListByUuids(userData.keySet()).stream()
            .collect(Collectors.toMap(UserDTO::getUuid, UserDTO::getUsername));
        for (Map.Entry<String, List<UserLogin>> entry1 : userData.entrySet()) {
            MonthOnlineTime monthOnlineTime = new MonthOnlineTime();
            List<UserLogin> userNumber = entry1.getValue();
            if (userNumber != null && userNumber.size() > 0) {
                String userUuid = entry1.getKey(); // 用户uuid
                String groupUuid = userNumber.get(0).getGroupId(); // 组织uuid
                OrganizationLdap groupInfo = orgMap.get(groupUuid);
                String userName = userMap.get(userUuid);
                monthOnlineTime.setUserName(userName);
                String groupName = groupInfo.getName(); // 根据组织uuid查询组织详情
                monthOnlineTime.setGroupName(groupName);
                double value = groupMonthData(monthOnlineTime, day1, userNumber); // 合计
                monthOnlineTime.setSumNUmber(numberDataDis(value));
                result.add(monthOnlineTime);
            }
        }
        return result;
    }

    @Override
    public List<UserLogin> findUserOnlineOfflineDate(String userIds, String startTime, String endTime)
        throws Exception {
        List<String> userId = Arrays.asList(userIds.split(","));
        List<UserLogin> data = onlineTimeDao.getOnlineDataByIds(userId, startTime, endTime);
        List<UserLogin> resultData = new ArrayList<>();
        Map<String, String> map = new HashMap<>();
        Set<String> orgIds = data.stream().map(UserLogin::getGroupId).collect(Collectors.toSet());
        Map<String, OrganizationLdap> orgMap = organizationService.getOrgByUuids(orgIds);
        Set<String> userIdSet = data.stream().map(UserLogin::getUserId).collect(Collectors.toSet());
        Map<String, String> userMap = userService.getUserListByUuids(userIdSet).stream()
            .collect(Collectors.toMap(UserDTO::getUuid, UserDTO::getUsername));
        if (data != null && data.size() > 0) {
            for (UserLogin userLogin : data) {
                String userUuid = userLogin.getUserId();
                String username = userMap.get(userUuid);
                UserLogin ul = new UserLogin();
                ul.setUserName(username);
                String groupUuid = userLogin.getGroupId();
                ul.setGroupName(orgMap.get(groupUuid).getName());
                if (userLogin.getOnlineTime() != null && !userLogin.getOnlineTime().isEmpty()) { // 去除时间字符串后面的.0
                    String onlineTime = userLogin.getOnlineTime();
                    onlineTime = onlineTime.substring(0, onlineTime.indexOf("."));
                    ul.setOnlineTime(onlineTime);
                }
                if (userLogin.getOfflineTime() != null && !userLogin.getOfflineTime().isEmpty()) { // 去除时间字符串后面的.0
                    String offlineTime = userLogin.getOfflineTime();
                    offlineTime = offlineTime.substring(0, offlineTime.indexOf("."));
                    ul.setOfflineTime(offlineTime);
                }
                if (userLogin.getOnlineDuration() != null) {
                    long onlineDuration = userLogin.getOnlineDuration();
                    long hour = onlineDuration / HOUR_SECOND; // 小时
                    long minute = (onlineDuration % HOUR_SECOND) / 60; // 分钟
                    long second = (onlineDuration % HOUR_SECOND) % 60; // 秒
                    String hours = hour < 10 ? "0" + hour : "" + hour;
                    String minutes = minute < 10 ? "0" + minute : "" + minute;
                    String seconds = second < 10 ? "0" + second : "" + second;
                    String onlineTime = hours + ":" + minutes + ":" + seconds; // 在线时长
                    ul.setFormatDuration(onlineTime);
                }
                String onlineDate = userLogin.getOnlineTime(); // 上线时间
                String offlineDate = userLogin.getOfflineTime(); // 下线时间
                if (onlineDate != null && offlineDate != null) {
                    long onlineTime = timeDispose(onlineDate); //上线时间
                    long offlineTime = timeDispose(offlineDate); // 下线时间
                    long querystartTime = timeDispose(startTime); // 查询开始时间
                    long queryEndTime = timeDispose(endTime); // 查询结束时间
                    if (onlineTime >= querystartTime && onlineTime < offlineTime && offlineTime > queryEndTime) {
                        ul.setOfflineTime("不在查询范围内");
                        ul.setOnlineDuration(null);
                        ul.setFormatDuration(null);
                    } else if (onlineTime < querystartTime && querystartTime < offlineTime
                        && queryEndTime >= offlineTime) {
                        ul.setOnlineTime("不在查询范围内");
                        ul.setOnlineDuration(null);
                        ul.setFormatDuration(null);
                    }
                }
                resultData.add(ul);
            }
            return resultData;
        }
        return null;
    }

    @Override
    public boolean exportUserLoginDetail(String title, int type, HttpServletResponse response,
        List<UserLogin> resultData) throws Exception {
        ExportExcelUtil.setResponseHead(response, "用户登录明细数据");
        return ExportExcelUtil
            .export(new ExportExcelParam(title, type, resultData, UserLogin.class, null, response.getOutputStream()));
    }

    /**
     * 去除字符串日期格式的后面的.0,并返回时间格式
     */
    private Date cleanDate(String date) {
        if (date.contains(".")) {
            date = date.substring(0, date.lastIndexOf("."));
        }
        return DateUtil.getStringToDate(date, "yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 保留两位小数
     * @return
     */
    private double numberDataDis(double number) {
        decimalFormat.setMaximumFractionDigits(2);
        decimalFormat.setGroupingSize(0);
        decimalFormat.setRoundingMode(RoundingMode.FLOOR);
        return Double.parseDouble(decimalFormat.format(number));
    }

}

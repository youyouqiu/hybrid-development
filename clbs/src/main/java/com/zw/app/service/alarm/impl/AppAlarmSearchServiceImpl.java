package com.zw.app.service.alarm.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zw.app.domain.alarm.AlarmMorePos;
import com.zw.app.domain.alarm.AlarmTime;
import com.zw.app.domain.alarm.AppAlarmAction;
import com.zw.app.domain.alarm.AppAlarmDetailInfo;
import com.zw.app.domain.alarm.AppAlarmInfo;
import com.zw.app.domain.alarm.AppAlarmQuery;
import com.zw.app.repository.mysql.webMaster.alarmType.AppAlarmTypeDao;
import com.zw.app.service.alarm.AppAlarmSearchService;
import com.zw.app.service.webMaster.alarmType.impl.WebMasterAlarmTypeServiceImpl;
import com.zw.app.util.AppParamCheckUtil;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.oil.AlarmHandle;
import com.zw.platform.domain.oil.Positional;
import com.zw.platform.util.VehicleUtils;
import com.zw.platform.util.common.AddressUtil;
import com.zw.platform.util.common.AlarmTypeUtil;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.UuidUtils;
import com.zw.platform.util.paas.PaasCloudUrlUtil;
import com.zw.platform.util.privilege.UserPrivilegeUtil;
import com.zw.platform.util.report.PaasCloudHBaseAccessEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AppAlarmSearchServiceImpl implements AppAlarmSearchService {


    @Autowired
    private UserService userService;

    @Autowired
    private WebMasterAlarmTypeServiceImpl webMasterAlarmTypeService;

    @Autowired
    private AppAlarmTypeDao appAlarmTypeDao;

    @Autowired
    private UserPrivilegeUtil userPrivilegeUtil;


    @Value("${alarm.number}")
    private String alarmNumbers;

    /**
     * ??????pos?????????????????????
     */
    private static final List<String> ALARM_MORE_POS = Arrays
        .asList(new String[] { "125", "126", "1271", "1272", "130",
            "651", "652", "661", "662", "70", "681", "682", "143"});

    private static final long DAYSECOND = 86399999;

    /**
     * ??????????????????
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private static final String DATE_FORMAT1 = "yyyy-MM-dd";

    @Override
    public List<AppAlarmInfo> getAlarmInfo(AppAlarmQuery alarmQuery) throws Exception {
        if (alarmQuery != null && AppParamCheckUtil.checkDate(alarmQuery.getStartTime(), 1) && AppParamCheckUtil
            .checkDate(alarmQuery.getEndTime(), 1) && StringUtils.isNotBlank(alarmQuery.getAlarmType())) {
            // String fuzzyParam = alarmQuery.getFuzzyParam();
            // String uniquenessFlag = alarmQuery.getUniquenessFlag();
            // List<AppAlarmInfo> alarmHandles = new ArrayList<>();
            // if (!StringUtils.isNotBlank(fuzzyParam) && StringUtils.isNotBlank(uniquenessFlag)) { // ????????????
            //     alarmHandles = preciseQuery(uniquenessFlag);
            // } else if (StringUtils.isNotBlank(fuzzyParam) && StringUtils.isNotBlank(uniquenessFlag)){ // ????????????
            //     alarmHandles = fuzzyQuery(fuzzyParam,uniquenessFlag);
            // }
            // List<?> resultData = new ArrayList<>();
            // if (alarmHandles.size() > 0) {
            //     resultData = listPage(alarmHandles,alarmQuery.getPage(),alarmQuery.getPageSize());
            // }
            // return resultData;
            boolean disResult = paramDispose(alarmQuery);
            if (disResult) {
                List<AppAlarmInfo> alarmHandles = new ArrayList<>();
                List<byte[]> monitorIds = getMonitor(alarmQuery);
                if (monitorIds != null && monitorIds.size() > 0) {
                    alarmQuery.setMonitorIds(monitorIds);
                    int alarmNumber = Integer.parseInt(alarmNumbers);
                    List<AlarmHandle> userVehicleAlarm;
                    if (alarmNumber == alarmQuery.getAlarmType().split(",").length) {
                        // ???????????????????????????????????????????????????
                        List<AlarmHandle> result = this.listUserVehicleLastAlarm(alarmQuery);
                        // ?????????????????????id  ????????????????????????
                        List<AlarmHandle> sorted =
                            result.stream().sorted(Comparator.comparing(AlarmHandle::getAlarmStartTime).reversed())
                                .collect(Collectors.toList());
                        int size = alarmQuery.getPageSize() + alarmQuery.getLineNumber();
                        int toSize = Math.min(size, sorted.size());
                        userVehicleAlarm = sorted.subList(alarmQuery.getLineNumber(), toSize);
                    } else {
                        // ?????????????????????
                        userVehicleAlarm = this.listUserVehicleAlarm(alarmQuery);
                    }
                    alarmHandles = otherDataDispose(userVehicleAlarm);
                }
                return alarmHandles;
            }
        }
        return null;
    }

    private List<AlarmHandle> listUserVehicleLastAlarm(AppAlarmQuery alarmQuery) {
        if (CollectionUtils.isEmpty(alarmQuery.getMonitorIds())) {
            return new ArrayList<>();
        }
        Map<String, String> params = new HashMap<>(2);
        params.put("value", JSON.toJSONString(alarmQuery));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.LIST_USER_VEHICLE_LAST_ALARM, params);
        return PaasCloudUrlUtil.getResultListData(str, AlarmHandle.class);
    }

    private List<AlarmHandle> listUserVehicleAlarm(AppAlarmQuery alarmQuery) {
        if (CollectionUtils.isEmpty(alarmQuery.getMonitorIds())
                || CollectionUtils.isEmpty(alarmQuery.getAlarmCode())) {
            return new ArrayList<>();
        }
        Map<String, String> params = new HashMap<>(2);
        params.put("value", JSON.toJSONString(alarmQuery));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.LIST_USER_VEHICLE_ALARM, params);
        return PaasCloudUrlUtil.getResultListData(str, AlarmHandle.class);
    }

    /**
     * ??????????????????
     */
    private boolean paramDispose(AppAlarmQuery alarmQuery) throws Exception {
        Long resultEnd = DateUtils.parseDate(alarmQuery.getEndTime(), DATE_FORMAT).getTime();
        long resultStart;
        if (StringUtils.isBlank(alarmQuery.getStartTime())) { // ??????????????????
            // ????????????????????????????????????????????????????????????????????????
            resultStart = getBeforeThirtyDate(resultEnd);
            if (resultStart == 0) {
                return false;
            }
        } else {
            if (AppParamCheckUtil.checkDate(alarmQuery.getStartTime(), 1)) {
                resultStart = DateUtils.parseDate(alarmQuery.getStartTime(), DATE_FORMAT).getTime();
            } else {
                return false;
            }
        }
        alarmQuery.setAlarmStartTime(resultStart);
        alarmQuery.setAlarmEndTime(resultEnd);
        int lineNumber = (alarmQuery.getPage() - 1) * alarmQuery.getPageSize();
        alarmQuery.setLineNumber(lineNumber);
        List<Integer> alarmPos = getAlarmPos(alarmQuery.getAlarmType());
        alarmQuery.setAlarmCode(alarmPos);
        return true;
    }

    /**
     * ??????????????????list
     * @param alarmType ?????????????????????
     * @return
     */
    private List<Integer> getAlarmPos(String alarmType) {
        HashSet<Integer> result = new HashSet<>();
        String[] alarmTypes = alarmType.split(",");
        for (String pos : alarmTypes) {
            if (ALARM_MORE_POS.contains(pos)) {
                AlarmMorePos alarmMorePos = AlarmMorePos.valueOf("pos_" + pos);
                result.addAll(alarmMorePos.getMorePos());
            } else {
                result.add(Integer.parseInt(pos));
            }
        }
        return new ArrayList<>(result);
    }

    /**
     * ?????????????????????????????????id
     */
    private List<byte[]> getMonitor(AppAlarmQuery alarmQuery) throws Exception {
        /** ???????????????????????????????????? */
        Set<String> vehicle = userPrivilegeUtil.getCurrentUserVehicles();
        /** ???????????????????????????????????????????????????????????? */
        if (vehicle != null && vehicle.size() > 0) {
            // ?????????????????????????????????????????????id
            if (StringUtils.isNotBlank(alarmQuery.getFuzzyParam())) {
                List<String> fuzzyIds = VehicleUtils.fuzzQueryMonitors(alarmQuery.getFuzzyParam(), false);
                vehicle.retainAll(fuzzyIds);
            }
            String str = StringUtils.join(vehicle.toArray(), ",");
            return UuidUtils.filterVid(str);
        }
        return null;
    }

    /**
     * ?????????????????????????????????????????????
     */
    private Long getBeforeThirtyDate(Long nowDate) throws Exception {
        //  ????????????????????????????????????????????????
        int alarMaxDate = getPlatformAppAlarmSet();
        if (alarMaxDate != -1) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(nowDate);
            calendar.add(Calendar.DAY_OF_MONTH, alarMaxDate * -1);
            String beforeStringDate = DateFormatUtils.format(calendar.getTime(), DATE_FORMAT1);
            if (beforeStringDate != null) {
                return DateUtils.parseDate(beforeStringDate, DATE_FORMAT1).getTime();
            }
        }
        return 0L;
    }

    /**
     * ?????????????????????????????????????????????
     * @return
     */
    private int getPlatformAppAlarmSet() {
        // ????????????????????????????????????
        OrganizationLdap currentUserOrg = userService.getCurrentUserOrg();
        if (currentUserOrg != null) {
            return appAlarmTypeDao.getAlarmMaxDateByGroupId(currentUserOrg.getUuid());
        }
        return -1;
    }

    /**
     * ??????????????????????????????????????????
     * @param alarmMonitorInfo
     * @return
     */
    private List<AppAlarmInfo> otherDataDispose(List<AlarmHandle> alarmMonitorInfo) throws Exception {
        List<AppAlarmInfo> processResult = new ArrayList<>();
        if (alarmMonitorInfo.size() > 0) {
            AppAlarmInfo alarmInfo;
            final List<String> lngLatList = new ArrayList<>(alarmMonitorInfo.size());
            for (AlarmHandle alarm : alarmMonitorInfo) {
                alarmInfo = new AppAlarmInfo();
                String location = ""; //??????????????????
                if (StringUtils.isBlank(alarm.getAlarmStartLocation())) {
                    location = "0.0,0.0";
                } else {
                    location = alarm.getAlarmStartLocation();
                }
                lngLatList.add(location);
                String monitorId = UuidUtils.getUUIDFromBytes(alarm.getVehicleIdHbase()).toString();
                alarmInfo.setId(monitorId);
                alarmInfo.setTime(alarm.getAlarmStartTime());
                String plateNumber = alarm.getPlateNumber();
                if (StringUtils.isBlank(plateNumber)) {
                    // ???????????????????????????????????????,???????????????APP?????????,???????????????????????????
                    plateNumber = RedisHelper.hget(RedisKeyEnum.MONITOR_INFO.of(monitorId), "name");
                }
                alarmInfo.setType(String.valueOf(alarm.getMonitorType()));
                alarmInfo.setName(plateNumber);
                processResult.add(alarmInfo);
            }
            Set<String> lngLatSet = new HashSet<>(lngLatList);
            final Map<String, String> addressMap = AddressUtil.batchInverseAddress(lngLatSet);
            for (int i = 0; i < processResult.size(); i++) {
                processResult.get(i).setAddress(addressMap.get(lngLatList.get(i)));
            }
        }
        return processResult;
    }

    @Override
    public List<AppAlarmAction> getMonitorAlarmAction(String vehicleId, AppAlarmQuery alarmQuery) throws Exception {
        if (AppParamCheckUtil.check64String(vehicleId) && alarmQuery != null) {
            if (StringUtils.isNotBlank(alarmQuery.getAlarmType()) && AppParamCheckUtil
                .checkDate(alarmQuery.getStartTime(), 2) && AppParamCheckUtil.checkDate(alarmQuery.getEndTime(), 2)) {
                List<Integer> alarmPos = getAlarmPos(alarmQuery.getAlarmType()); // ????????????
                alarmQuery.setAlarmCode(alarmPos);
                // ??????????????????
                long date1 = DateUtils.parseDate(alarmQuery.getStartTime(), DATE_FORMAT1).getTime();
                // ??????????????????
                long date2 = DateUtils.parseDate(alarmQuery.getEndTime(), DATE_FORMAT1).getTime() + DAYSECOND;
                alarmQuery.setAlarmStartTime(date1);
                alarmQuery.setAlarmEndTime(date2);
                // ????????????????????????????????????????????????0,????????????????????????????????????
                byte[] monitorId = UuidUtils.getBytesFromStr(vehicleId);
                alarmQuery.setVehicleId(monitorId);
                List<AppAlarmAction> result = this.listAlarmDate(alarmQuery);
                if (result.size() > 0) {
                    return actionDayDispose(result, alarmQuery.getAlarmStartTime(), alarmQuery.getAlarmEndTime());
                }
                return new ArrayList<>();
            }
        }
        return null;

    }

    private List<AppAlarmAction> listAlarmDate(AppAlarmQuery alarmQuery) {
        if (CollectionUtils.isEmpty(alarmQuery.getAlarmCode())) {
            return new ArrayList<>();
        }
        Map<String, String> params = new HashMap<>(2);
        params.put("value", JSON.toJSONString(alarmQuery));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.LIST_ALARM_DATE, params);
        return PaasCloudUrlUtil.getResultListData(str, AppAlarmAction.class);
    }

    /**
     * ????????????
     * @param actionData
     * @return
     */
    private List<AppAlarmAction> actionDayDispose(List<AppAlarmAction> actionData, Long thirtyDate, Long endTime)
        throws Exception {
        List<Long> allAlarmTime = new ArrayList<>();
        for (AppAlarmAction action : actionData) {
            // ??????????????????????????????
            Integer actionDay = action.getActionDay() != null ? action.getActionDay() : 0;
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(thirtyDate);
            calendar.add(Calendar.DAY_OF_MONTH, actionDay);
            //            String resultDate = DateFormatUtils.format(calendar.getTimeInMillis(), DATE_FORMAT1);
            action.setDate(calendar.getTimeInMillis());
            allAlarmTime.add(calendar.getTimeInMillis());
        }
        // ?????????????????????????????????????????????
        long differenceValue = (endTime - thirtyDate) / 86400000;
        for (int index = 0; index <= differenceValue; index++) {
            LocalDateTime now = LocalDateTime.now();
            now = now.minusDays(index);
            long time = now.withHour(0).withMinute(0).withSecond(0).toEpochSecond(ZoneOffset.of("+8")) * 1000;
            if (!allAlarmTime.contains(time)) {
                AppAlarmAction action = new AppAlarmAction();
                action.setAlarmCount(0);
                action.setDate(time);
                actionData.add(action);
                allAlarmTime.add(time);
            }
        }
        actionData.sort((AppAlarmAction o1, AppAlarmAction o2) -> { // ??????????????????
            long date1 = o1.getDate();
            long date2 = o2.getDate();
            if (date1 > date2) {
                return -1;
            } else if (date1 < date2) {
                return 1;
            } else {
                return 0;
            }
        });

        return actionData;
    }

    /**
     * ??????????????????
     * @param time
     * @return
     */
    private Double getTimeScale(String time) throws Exception {
        Date d = DateUtils.parseDate(time, "yyyy-MM-dd HH:mm:ss");
        String date = DateFormatUtils.format(d, "HH:mm:ss");
        String[] data = date.split(":");
        Integer hour = Integer.parseInt(data[0]);
        Integer minute = Integer.parseInt(data[1]);
        Integer second = Integer.parseInt(data[2]);
        Integer allSecond = ((hour * 60 * 60) + (minute * 60) + second);
        return allSecond / 86400.0;
    }

    /**
     * ????????????????????????
     */
    @Override
    public List<AppAlarmDetailInfo> getMonitorAlarmDetail(String vehicleId, AppAlarmQuery alarmQuery, String time)
        throws Exception {
        if (AppParamCheckUtil.check64String(vehicleId) && alarmQuery != null) {
            String startDate = "";
            String endDate = "";
            if (time.length() == 10) { // ?????????
                if (AppParamCheckUtil.checkDate(time, 2)) {
                    startDate = time + " 00:00:00";
                    endDate = time + " 23:59:59";
                }
            } else {
                if (AppParamCheckUtil.checkDate(time, 1)) {
                    startDate = time.split(" ")[0] + " 00:00:00";
                    endDate = time;
                }
            }
            if (StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)) {
                String alarmCode = alarmQuery.getAlarmType();
                List<Integer> alarmPos = getAlarmPos(alarmCode);
                if (alarmPos.size() > 0) {
                    Long startTime = DateUtils.parseDate(startDate, DATE_FORMAT).getTime();
                    Long endTime = DateUtils.parseDate(endDate, DATE_FORMAT).getTime();
                    Integer pageSize = alarmQuery.getPageSize(); // ??????????????????
                    Integer pageCount = alarmQuery.getPage(); // ??????
                    Integer lineNumber = (pageCount - 1) * pageSize;
                    alarmQuery.setAlarmStartTime(startTime);
                    alarmQuery.setAlarmEndTime(endTime);
                    alarmQuery.setLineNumber(lineNumber);
                    alarmQuery.setAlarmCode(alarmPos);
                    byte[] monitorId = UuidUtils.getBytesFromStr(vehicleId);
                    alarmQuery.setVehicleId(monitorId);
                    List<AlarmHandle> result = this.listAlarmDetail(alarmQuery);
                    if (result.size() > 0) {
                        // ????????????
                        return alarmDetailInfoDis(result);
                    }
                }
                return new ArrayList<>();
            }
        }
        return null;
    }

    private List<AlarmHandle> listAlarmDetail(AppAlarmQuery alarmQuery) {
        if (CollectionUtils.isEmpty(alarmQuery.getAlarmCode())) {
            return new ArrayList<>();
        }
        Map<String, String> params = new HashMap<>(2);
        params.put("value", JSON.toJSONString(alarmQuery));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.LIST_ALARM_DETAIL, params);
        return PaasCloudUrlUtil.getResultListData(str, AlarmHandle.class);
    }

    /**
     * ??????????????????????????????
     */
    private List<AppAlarmDetailInfo> alarmDetailInfoDis(List<AlarmHandle> alarmDetailInfo) throws Exception {
        List<AppAlarmDetailInfo> resultData = new ArrayList<>();
        AppAlarmDetailInfo detailInfo;
        for (AlarmHandle alarmHandle : alarmDetailInfo) {
            detailInfo = new AppAlarmDetailInfo();
            detailInfo.setMonitorId(UuidUtils.getUUIDFromBytes(alarmHandle.getVehicleIdHbase()).toString()); // ????????????id
            Integer alarmType = alarmHandle.getAlarmType(); // ????????????
            // ?????????id,??????,position???id??????????????????
            Positional positionInfo =
                    this.getSensorInfo(detailInfo.getMonitorId(), alarmHandle.getAlarmStartTime() / 1000);
            String alarmValue = "";
            if (positionInfo != null) {
                // ???????????????????????????
                switch (alarmType) {
                    // ?????????????????????
                    case 0: // ????????????
                        break;
                    case 1: // ????????????
                        alarmValue = positionInfo.getSpeed() + " km/h"; // ?????????
                        break;
                    case 2: // ????????????
                        alarmValue =
                            backStrTimeByStartEndTime(alarmHandle.getAlarmStartTime(), alarmHandle.getAlarmEndTime());
                        break;
                    case 18: // ????????????????????????
                        alarmValue =
                            backStrTimeByStartEndTime(alarmHandle.getAlarmStartTime(), alarmHandle.getAlarmEndTime());
                        break;
                    case 19: // ????????????
                        alarmValue =
                            backStrTimeByStartEndTime(alarmHandle.getAlarmStartTime(), alarmHandle.getAlarmEndTime());
                        break;
                    case 23: // ??????????????????
                    case 2011: // ?????????
                    case 2012: // ?????????
                    case 2111: // ?????????
                    case 2112: // ?????????
                        alarmValue =
                            StringUtils.isNotBlank(alarmHandle.getFenceName()) ? "??????: " + alarmHandle.getFenceName()
                                                                               : "";
                        break;
                    case 2211: // ????????????????????????
                        break;
                    case 2212: // ????????????????????????
                        break;
                    // ????????????
                    case 24: // ??????VSS??????
                        break;
                    case 25: // ??????????????????
                        break;
                    case 26: // ????????????
                        break;
                    case 27: // ??????????????????
                        break;
                    case 28: // ??????????????????
                        break;
                    case 31: // ??????????????????
                        break;
                    // ????????????
                    case 4: // GNSS??????????????????
                        break;
                    case 5: // GNSS????????????????????????
                        break;
                    case 6: // GNSS????????????
                        break;
                    case 7: // ?????????????????????
                        break;
                    case 8: // ?????????????????????
                        break;
                    case 9: // ??????LCD??????????????????
                        break;
                    case 10: // TTS????????????
                        break;
                    case 11: // ???????????????
                        break;
                    case 12: // ???????????????IC???????????????
                        break;
                    case 110: // ???????????????
                        break;
                    // F3?????????
                    case 67: // ????????????(F3)
                        alarmValue =
                            positionInfo.getMileageSpeed() != null ? String.valueOf(positionInfo.getMileageSpeed())
                                + " km/h" : "";
                        break;
                    case 69: // ????????????
                        break;
                    case 70: // ????????????
                        break;
                    case 6511: // ???????????????1????????????
                        alarmValue =
                            positionInfo.getTempValueOne() != null ? String.valueOf(positionInfo.getTempValueOne() / 10)
                                + " ??C" : "";
                        break;
                    case 6512: // ???????????????1????????????
                        alarmValue =
                            positionInfo.getTempValueOne() != null ? String.valueOf(positionInfo.getTempValueOne() / 10)
                                + " ??C" : "";
                        break;
                    case 6513:  // ???????????????1????????????
                        break;
                    case 6521:// ???????????????2????????????
                        alarmValue =
                            positionInfo.getTempValueTwo() != null ? String.valueOf(positionInfo.getTempValueTwo() / 10)
                                + " ??C" : "";
                        break;
                    case 6522: // ???????????????2????????????
                        alarmValue =
                            positionInfo.getTempValueTwo() != null ? String.valueOf(positionInfo.getTempValueTwo() / 10)
                                + " ??C" : "";
                        break;
                    case 6523: // ???????????????2????????????
                        break;
                    case 6531: // ???????????????3????????????
                        alarmValue = positionInfo.getTempValueThree() != null
                                     ? String.valueOf(positionInfo.getTempValueThree() / 10) + " ??C" : "";
                        break;
                    case 6532: // ???????????????3????????????
                        alarmValue = positionInfo.getTempValueThree() != null
                                     ? String.valueOf(positionInfo.getTempValueThree() / 10) + " ??C" : "";
                        break;
                    case 6533: // ???????????????3????????????
                        break;
                    case 6541: // ???????????????4????????????
                        alarmValue = positionInfo.getTempValueFour() != null
                                     ? String.valueOf(positionInfo.getTempValueFour() / 10) + " ??C" : "";
                        break;
                    case 6542: // ???????????????4????????????
                        alarmValue = positionInfo.getTempValueFour() != null
                                     ? String.valueOf(positionInfo.getTempValueFour() / 10) + " ??C" : "";
                        break;
                    case 6543: // ???????????????4????????????
                        break;
                    case 6551: // ???????????????5????????????
                        alarmValue = positionInfo.getTempValueFive() != null
                                     ? String.valueOf(positionInfo.getTempValueFive() / 10) + " ??C" : "";
                        break;
                    case 6552: // ???????????????5????????????
                        alarmValue = positionInfo.getTempValueFive() != null
                                     ? String.valueOf(positionInfo.getTempValueFive() / 10) + " ??C" : "";
                        break;
                    case 6553: // ???????????????5????????????
                        break;
                    case 6611: // ???????????????1???????????????
                        alarmValue = positionInfo.getWetnessValueOne() != null
                                     ? String.valueOf(positionInfo.getWetnessValueOne() / 10) + " %" : "";
                        break;
                    case 6612: // ???????????????1???????????????
                        alarmValue = positionInfo.getWetnessValueOne() != null
                                     ? String.valueOf(positionInfo.getWetnessValueOne() / 10) + " %" : "";
                        break;
                    case 6613: // ???????????????1????????????
                        break;
                    case 6621: // ???????????????2???????????????
                        alarmValue = positionInfo.getWetnessValueTwo() != null
                                     ? String.valueOf(positionInfo.getWetnessValueTwo()) + " %" : "";
                        break;
                    case 6622: // ???????????????2???????????????
                        alarmValue = positionInfo.getWetnessValueTwo() != null
                                     ? String.valueOf(positionInfo.getWetnessValueTwo()) + " %" : "";
                        break;
                    case 6623: // ???????????????2????????????
                        break;
                    case 6631: // ???????????????3???????????????
                        alarmValue = positionInfo.getWetnessValueThree() != null
                                     ? String.valueOf(positionInfo.getWetnessValueThree()) + " %" : "";
                        break;
                    case 6632: // ???????????????3???????????????
                        alarmValue = positionInfo.getWetnessValueThree() != null
                                     ? String.valueOf(positionInfo.getWetnessValueThree()) + " %" : "";
                        break;
                    case 6633: // ???????????????3????????????
                        break;
                    case 6641: // ???????????????4???????????????
                        alarmValue = positionInfo.getWetnessValueFour() != null
                                     ? String.valueOf(positionInfo.getWetnessValueFour()) + " %" : "";
                        break;
                    case 6642: // ???????????????4???????????????
                        alarmValue = positionInfo.getWetnessValueFour() != null
                                     ? String.valueOf(positionInfo.getWetnessValueFour()) + " %" : "";
                        break;
                    case 6643: // ???????????????4????????????
                        break;
                    case 6651: // ???????????????5???????????????
                        break;
                    case 6652: // ???????????????5???????????????
                        break;
                    case 6653: // ???????????????5????????????
                        break;
                    case 6811: // ?????????????????????
                        alarmValue = positionInfo.getFuelAmountOne() != null
                                     ? String.valueOf(Double.parseDouble(positionInfo.getFuelAmountOne())) + " L" : "";
                        break;
                    case 6812: // ?????????????????????
                        alarmValue = positionInfo.getFuelSpillOne() != null
                                     ? String.valueOf(Double.parseDouble(positionInfo.getFuelSpillOne())) + " L" : "";
                        break;
                    case 6813: // ?????????????????????
                        break;
                    case 6821: // ?????????????????????
                        alarmValue = positionInfo.getFuelAmountTwo() != null
                                     ? String.valueOf(Double.parseDouble(positionInfo.getFuelAmountTwo())) + " L" : "";
                        break;
                    case 6822: // ?????????????????????
                        alarmValue = positionInfo.getFuelSpillTwo() != null
                                     ? String.valueOf(Double.parseDouble(positionInfo.getFuelSpillTwo())) + " L" : "";
                        break;
                    case 6823: // ?????????????????????
                        break;
                    case 6831: // ??????3????????????
                        break;
                    case 6832: // ??????3????????????
                        break;
                    case 6833: // ??????3????????????
                        break;
                    case 6841: // ??????4????????????
                        break;
                    case 6842: // ??????4????????????
                        break;
                    case 6843: // ??????4????????????
                        break;
                    case 12411: // ??????????????????????????????
                        break;
                    case 124: //  ????????????
                        break;
                    //                    case 7111: // ????????????????????????
                    //                        break;
                    //                    case 7112: // ????????????????????????
                    //                        break;
                    //                    case 7113: // ????????????????????????
                    //                        break;
                    // ????????????
                    case 74: // ???????????????
                        alarmValue = positionInfo.getSpeed() + " km/h" + "  " + alarmHandle.getFenceName();
                        break;
                    case 75: // ??????????????????
                        alarmValue = alarmHandle.getFenceName();
                        break;
                    case 76: // ??????????????????
                        alarmValue = alarmHandle.getFenceName() != null ? positionInfo.getSpeed() + " km/h" + "(??????:"
                            + alarmHandle.getFenceName() + ")" : positionInfo.getSpeed() + " km/h";
                        break;
                    case 77: // ??????????????????
                        break;
                    case 78: // ????????????????????????
                        break;
                    case 79: // ??????????????????
                        alarmValue =
                            backStrTimeByStartEndTime(alarmHandle.getAlarmStartTime(), alarmHandle.getAlarmEndTime());
                        break;
                    case 80: // ????????????
                        break;
                    case 81: // ????????????
                        break;
                    case 82: // ?????????????????????
                        alarmValue =
                            backStrTimeByStartEndTime(alarmHandle.getAlarmStartTime(), alarmHandle.getAlarmEndTime());
                        break;
                    case 7702: // ????????????(??????)
                        break;
                    case 7703: // ????????????(??????)
                        break;
                    case 7211: // ???????????????
                    case 7212: // ???????????????
                    case 7311: // ???????????????
                    case 7312: // ???????????????
                    case 11911: // ?????????????????????
                    case 11912: //  ????????????????????????
                        alarmValue =
                            StringUtils.isNotBlank(alarmHandle.getFenceName()) ? "??????: " + alarmHandle.getFenceName()
                                                                               : "";
                        break;
                    // I/O ??????
                    case 14004: // ??????I/O??????
                        break;
                    case 141000: // I/O?????????1??????
                        break;
                    case 142000: // I/O?????????2??????
                        break;
                    // ???????????????
                    // ADAS??????
                    // DSM??????
                    default:
                        break;
                }
            }
            if (alarmType != null) {
                String alarmName = StringUtils.isNotBlank(alarmHandle.getDescription()) ? alarmHandle.getDescription()
                                                                                        : AlarmTypeUtil
                                       .getAlarmType(String.valueOf(alarmType));
                if (StringUtils.isNotBlank(alarmName)) {
                    String alarmStartLocation = alarmHandle.getAlarmStartLocation();
                    String locationResult = AddressUtil.inverseAddress(alarmStartLocation).getFormattedAddress();
                    detailInfo.setAddress(locationResult);
                    detailInfo.setAlarmValue(alarmValue);
                    detailInfo.setType(alarmHandle.getAlarmType());
                    detailInfo.setName(alarmName); // ????????????
                    detailInfo.setAlarmStarTime(alarmHandle.getAlarmStartTime()); // ??????????????????
                    detailInfo.setAlarmEndTime(alarmHandle.getAlarmEndTime()); // ??????????????????
                    detailInfo.setStatus(alarmHandle.getStatus());// ??????????????????
                    resultData.add(detailInfo);
                }
            }
        }
        return resultData;
    }

    private Positional getSensorInfo(String vehicleId, Long vtime) {
        Map<String, String> params = new HashMap<>(2);
        params.put("vehicleId", vehicleId);
        params.put("vtime", String.valueOf(vtime));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_SENSOR_INFO, params);
        return PaasCloudUrlUtil.getResultData(str, Positional.class);
    }

    @Override
    public JSONObject getAlarMonitorNumber(AppAlarmQuery alarmQuery) throws Exception {
        JSONObject msg = new JSONObject();
        int alarm = 0;
        if (alarmQuery != null && StringUtils.isNotBlank(alarmQuery.getAlarmType()) && AppParamCheckUtil
            .checkDate(alarmQuery.getEndTime(), 1)) {
            boolean disResult = paramDispose(alarmQuery);
            if (disResult) {
                List<byte[]> monitorIds = getMonitor(alarmQuery); // ????????????id
                if (monitorIds != null && monitorIds.size() > 0) {
                    alarmQuery.setMonitorIds(monitorIds);
                    List<AlarmTime> alarmData = this.listMonitorAlarm(alarmQuery);
                    alarm = alarmData.size();
                }
            }
        }
        msg.put("count", alarm);
        return msg;
    }

    private List<AlarmTime> listMonitorAlarm(AppAlarmQuery alarmQuery) {
        if (CollectionUtils.isEmpty(alarmQuery.getMonitorIds())
                || CollectionUtils.isEmpty(alarmQuery.getAlarmCode())) {
            return new ArrayList<>();
        }
        Map<String, String> params = new HashMap<>(2);
        params.put("value", JSON.toJSONString(alarmQuery));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.LIST_MONITOR_ALARM, params);
        return PaasCloudUrlUtil.getResultListData(str, AlarmTime.class);
    }


    @Override
    public JSONObject getUserAlarmSetting() throws Exception {
        JSONObject msg = new JSONObject();
        // ???????????????????????????
        OrganizationLdap currentUserOrg = userService.getCurrentUserOrg();
        if (currentUserOrg != null) {
            JSONObject queryResult = webMasterAlarmTypeService.getAlarmType(currentUserOrg.getUuid());
            if (queryResult != null && queryResult.getJSONArray("alarmTypes") != null) {
                msg.put("settings", queryResult.getJSONArray("alarmTypes"));
            }
            return msg;
        }
        return null;
    }

    /**
     * ????????????????????????????????????
     */
    private String backStrTimeByStartEndTime(Long alarmStartTime, Long alarmEndTime) {
        Long startTime = alarmStartTime != null ? alarmStartTime : new Date().getTime(); // ??????????????????
        Long endTime = alarmEndTime != null ? alarmEndTime : new Date().getTime(); // ??????????????????
        Long stopTime = endTime - startTime; // ????????????
        String stopTimeStr = "0???";
        if (stopTime > 0) {
            stopTimeStr = DateUtil.formatTime(stopTime);
        }
        return stopTimeStr;
    }
}


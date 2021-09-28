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
     * 报警pos存在多个的报警
     */
    private static final List<String> ALARM_MORE_POS = Arrays
        .asList(new String[] { "125", "126", "1271", "1272", "130",
            "651", "652", "661", "662", "70", "681", "682", "143"});

    private static final long DAYSECOND = 86399999;

    /**
     * 日期转换格式
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
            // if (!StringUtils.isNotBlank(fuzzyParam) && StringUtils.isNotBlank(uniquenessFlag)) { // 精确查询
            //     alarmHandles = preciseQuery(uniquenessFlag);
            // } else if (StringUtils.isNotBlank(fuzzyParam) && StringUtils.isNotBlank(uniquenessFlag)){ // 模糊查询
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
                        // 勾选全部报警，查询车辆最新的报警表
                        List<AlarmHandle> result = this.listUserVehicleLastAlarm(alarmQuery);
                        // 表的主键只有车id  查出来再排序分页
                        List<AlarmHandle> sorted =
                            result.stream().sorted(Comparator.comparing(AlarmHandle::getAlarmStartTime).reversed())
                                .collect(Collectors.toList());
                        int size = alarmQuery.getPageSize() + alarmQuery.getLineNumber();
                        int toSize = Math.min(size, sorted.size());
                        userVehicleAlarm = sorted.subList(alarmQuery.getLineNumber(), toSize);
                    } else {
                        // 查询所有报警表
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
     * 请求参数处理
     */
    private boolean paramDispose(AppAlarmQuery alarmQuery) throws Exception {
        Long resultEnd = DateUtils.parseDate(alarmQuery.getEndTime(), DATE_FORMAT).getTime();
        long resultStart;
        if (StringUtils.isBlank(alarmQuery.getStartTime())) { // 开始时间为空
            // 查询的开始时间就以平台设置的报警查询最大时间范围
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
     * 获取报警类型list
     * @param alarmType 报警类型字符串
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
     * 获取用户权限下监控对象id
     */
    private List<byte[]> getMonitor(AppAlarmQuery alarmQuery) throws Exception {
        /** 获取用户下的监控对象信息 */
        Set<String> vehicle = userPrivilegeUtil.getCurrentUserVehicles();
        /** 只当用户权限下有监控对象才进行下一步操作 */
        if (vehicle != null && vehicle.size() > 0) {
            // 若是模糊搜索则搜索对应监控对象id
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
     * 获取平台配置的报警最大查询时间
     */
    private Long getBeforeThirtyDate(Long nowDate) throws Exception {
        //  获取到平台配置的报警查询最高时间
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
     * 获取平台设置的报警最大时间范围
     * @return
     */
    private int getPlatformAppAlarmSet() {
        // 当前登录的用户名所在企业
        OrganizationLdap currentUserOrg = userService.getCurrentUserOrg();
        if (currentUserOrg != null) {
            return appAlarmTypeDao.getAlarmMaxDateByGroupId(currentUserOrg.getUuid());
        }
        return -1;
    }

    /**
     * 查询报警监控对象后的数据处理
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
                String location = ""; //报警开始位置
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
                    // 如果报警信息中的车牌号为空,为了不影响APP的显示,去缓存中查询车牌号
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
                List<Integer> alarmPos = getAlarmPos(alarmQuery.getAlarmType()); // 报警类型
                alarmQuery.setAlarmCode(alarmPos);
                // 报警开始时间
                long date1 = DateUtils.parseDate(alarmQuery.getStartTime(), DATE_FORMAT1).getTime();
                // 报警结束时间
                long date2 = DateUtils.parseDate(alarmQuery.getEndTime(), DATE_FORMAT1).getTime() + DAYSECOND;
                alarmQuery.setAlarmStartTime(date1);
                alarmQuery.setAlarmEndTime(date2);
                // 如果开始时间的时分秒转换为秒不为0,则计算占当天总秒数的比例
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
     * 数据处理
     * @param actionData
     * @return
     */
    private List<AppAlarmAction> actionDayDispose(List<AppAlarmAction> actionData, Long thirtyDate, Long endTime)
        throws Exception {
        List<Long> allAlarmTime = new ArrayList<>();
        for (AppAlarmAction action : actionData) {
            // 查询时间段内的第几天
            Integer actionDay = action.getActionDay() != null ? action.getActionDay() : 0;
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(thirtyDate);
            calendar.add(Calendar.DAY_OF_MONTH, actionDay);
            //            String resultDate = DateFormatUtils.format(calendar.getTimeInMillis(), DATE_FORMAT1);
            action.setDate(calendar.getTimeInMillis());
            allAlarmTime.add(calendar.getTimeInMillis());
        }
        // 获取到开始时间和结束时间的差值
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
        actionData.sort((AppAlarmAction o1, AppAlarmAction o2) -> { // 根据时间排序
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
     * 获取时间比例
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
     * 获取报警详细信息
     */
    @Override
    public List<AppAlarmDetailInfo> getMonitorAlarmDetail(String vehicleId, AppAlarmQuery alarmQuery, String time)
        throws Exception {
        if (AppParamCheckUtil.check64String(vehicleId) && alarmQuery != null) {
            String startDate = "";
            String endDate = "";
            if (time.length() == 10) { // 年月日
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
                    Integer pageSize = alarmQuery.getPageSize(); // 每页显示数量
                    Integer pageCount = alarmQuery.getPage(); // 页号
                    Integer lineNumber = (pageCount - 1) * pageSize;
                    alarmQuery.setAlarmStartTime(startTime);
                    alarmQuery.setAlarmEndTime(endTime);
                    alarmQuery.setLineNumber(lineNumber);
                    alarmQuery.setAlarmCode(alarmPos);
                    byte[] monitorId = UuidUtils.getBytesFromStr(vehicleId);
                    alarmQuery.setVehicleId(monitorId);
                    List<AlarmHandle> result = this.listAlarmDetail(alarmQuery);
                    if (result.size() > 0) {
                        // 数据处理
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
     * 报警详细信息数据处理
     */
    private List<AppAlarmDetailInfo> alarmDetailInfoDis(List<AlarmHandle> alarmDetailInfo) throws Exception {
        List<AppAlarmDetailInfo> resultData = new ArrayList<>();
        AppAlarmDetailInfo detailInfo;
        for (AlarmHandle alarmHandle : alarmDetailInfo) {
            detailInfo = new AppAlarmDetailInfo();
            detailInfo.setMonitorId(UuidUtils.getUUIDFromBytes(alarmHandle.getVehicleIdHbase()).toString()); // 监控对象id
            Integer alarmType = alarmHandle.getAlarmType(); // 报警类型
            // 根据车id,时间,position表id查询位置信息
            Positional positionInfo =
                    this.getSensorInfo(detailInfo.getMonitorId(), alarmHandle.getAlarmStartTime() / 1000);
            String alarmValue = "";
            if (positionInfo != null) {
                // 持续性报警特殊处理
                switch (alarmType) {
                    // 驾驶员引起报警
                    case 0: // 紧急报警
                        break;
                    case 1: // 超速报警
                        alarmValue = positionInfo.getSpeed() + " km/h"; // 速度值
                        break;
                    case 2: // 疲劳驾驶
                        alarmValue =
                            backStrTimeByStartEndTime(alarmHandle.getAlarmStartTime(), alarmHandle.getAlarmEndTime());
                        break;
                    case 18: // 当天累计驾驶超时
                        alarmValue =
                            backStrTimeByStartEndTime(alarmHandle.getAlarmStartTime(), alarmHandle.getAlarmEndTime());
                        break;
                    case 19: // 超时停车
                        alarmValue =
                            backStrTimeByStartEndTime(alarmHandle.getAlarmStartTime(), alarmHandle.getAlarmEndTime());
                        break;
                    case 23: // 路线偏离报警
                    case 2011: // 进区域
                    case 2012: // 出区域
                    case 2111: // 进线路
                    case 2112: // 出线路
                        alarmValue =
                            StringUtils.isNotBlank(alarmHandle.getFenceName()) ? "围栏: " + alarmHandle.getFenceName()
                                                                               : "";
                        break;
                    case 2211: // 路段行驶时间不足
                        break;
                    case 2212: // 路段行驶时间过长
                        break;
                    // 车辆报警
                    case 24: // 车辆VSS故障
                        break;
                    case 25: // 车辆油量异常
                        break;
                    case 26: // 车辆被盗
                        break;
                    case 27: // 车辆非法点火
                        break;
                    case 28: // 车辆非法位移
                        break;
                    case 31: // 非法开门报警
                        break;
                    // 故障报警
                    case 4: // GNSS模块发生故障
                        break;
                    case 5: // GNSS天线未接或被剪断
                        break;
                    case 6: // GNSS天线短路
                        break;
                    case 7: // 终端主电源欠压
                        break;
                    case 8: // 终端主电源掉电
                        break;
                    case 9: // 终端LCD或显示器故障
                        break;
                    case 10: // TTS模块故障
                        break;
                    case 11: // 摄像头故障
                        break;
                    case 12: // 道路运输证IC卡模块故障
                        break;
                    case 110: // 空压机报警
                        break;
                    // F3传感器
                    case 67: // 超速报警(F3)
                        alarmValue =
                            positionInfo.getMileageSpeed() != null ? String.valueOf(positionInfo.getMileageSpeed())
                                + " km/h" : "";
                        break;
                    case 69: // 胎压报警
                        break;
                    case 70: // 载重报警
                        break;
                    case 6511: // 温度传感器1高温报警
                        alarmValue =
                            positionInfo.getTempValueOne() != null ? String.valueOf(positionInfo.getTempValueOne() / 10)
                                + " °C" : "";
                        break;
                    case 6512: // 温度传感器1低温报警
                        alarmValue =
                            positionInfo.getTempValueOne() != null ? String.valueOf(positionInfo.getTempValueOne() / 10)
                                + " °C" : "";
                        break;
                    case 6513:  // 温度传感器1异常报警
                        break;
                    case 6521:// 温度传感器2高温报警
                        alarmValue =
                            positionInfo.getTempValueTwo() != null ? String.valueOf(positionInfo.getTempValueTwo() / 10)
                                + " °C" : "";
                        break;
                    case 6522: // 温度传感器2低温报警
                        alarmValue =
                            positionInfo.getTempValueTwo() != null ? String.valueOf(positionInfo.getTempValueTwo() / 10)
                                + " °C" : "";
                        break;
                    case 6523: // 温度传感器2异常报警
                        break;
                    case 6531: // 温度传感器3高温报警
                        alarmValue = positionInfo.getTempValueThree() != null
                                     ? String.valueOf(positionInfo.getTempValueThree() / 10) + " °C" : "";
                        break;
                    case 6532: // 温度传感器3低温报警
                        alarmValue = positionInfo.getTempValueThree() != null
                                     ? String.valueOf(positionInfo.getTempValueThree() / 10) + " °C" : "";
                        break;
                    case 6533: // 温度传感器3异常报警
                        break;
                    case 6541: // 温度传感器4高温报警
                        alarmValue = positionInfo.getTempValueFour() != null
                                     ? String.valueOf(positionInfo.getTempValueFour() / 10) + " °C" : "";
                        break;
                    case 6542: // 温度传感器4低温报警
                        alarmValue = positionInfo.getTempValueFour() != null
                                     ? String.valueOf(positionInfo.getTempValueFour() / 10) + " °C" : "";
                        break;
                    case 6543: // 温度传感器4异常报警
                        break;
                    case 6551: // 温度传感器5高温报警
                        alarmValue = positionInfo.getTempValueFive() != null
                                     ? String.valueOf(positionInfo.getTempValueFive() / 10) + " °C" : "";
                        break;
                    case 6552: // 温度传感器5低温报警
                        alarmValue = positionInfo.getTempValueFive() != null
                                     ? String.valueOf(positionInfo.getTempValueFive() / 10) + " °C" : "";
                        break;
                    case 6553: // 温度传感器5异常报警
                        break;
                    case 6611: // 湿度传感器1高湿度报警
                        alarmValue = positionInfo.getWetnessValueOne() != null
                                     ? String.valueOf(positionInfo.getWetnessValueOne() / 10) + " %" : "";
                        break;
                    case 6612: // 湿度传感器1低湿度报警
                        alarmValue = positionInfo.getWetnessValueOne() != null
                                     ? String.valueOf(positionInfo.getWetnessValueOne() / 10) + " %" : "";
                        break;
                    case 6613: // 湿度传感器1异常报警
                        break;
                    case 6621: // 湿度传感器2高湿度报警
                        alarmValue = positionInfo.getWetnessValueTwo() != null
                                     ? String.valueOf(positionInfo.getWetnessValueTwo()) + " %" : "";
                        break;
                    case 6622: // 湿度传感器2低湿度报警
                        alarmValue = positionInfo.getWetnessValueTwo() != null
                                     ? String.valueOf(positionInfo.getWetnessValueTwo()) + " %" : "";
                        break;
                    case 6623: // 湿度传感器2异常报警
                        break;
                    case 6631: // 湿度传感器3高湿度报警
                        alarmValue = positionInfo.getWetnessValueThree() != null
                                     ? String.valueOf(positionInfo.getWetnessValueThree()) + " %" : "";
                        break;
                    case 6632: // 湿度传感器3低湿度报警
                        alarmValue = positionInfo.getWetnessValueThree() != null
                                     ? String.valueOf(positionInfo.getWetnessValueThree()) + " %" : "";
                        break;
                    case 6633: // 湿度传感器3异常报警
                        break;
                    case 6641: // 湿度传感器4高湿度报警
                        alarmValue = positionInfo.getWetnessValueFour() != null
                                     ? String.valueOf(positionInfo.getWetnessValueFour()) + " %" : "";
                        break;
                    case 6642: // 湿度传感器4低湿度报警
                        alarmValue = positionInfo.getWetnessValueFour() != null
                                     ? String.valueOf(positionInfo.getWetnessValueFour()) + " %" : "";
                        break;
                    case 6643: // 湿度传感器4异常报警
                        break;
                    case 6651: // 湿度传感器5高湿度报警
                        break;
                    case 6652: // 湿度传感器5低湿度报警
                        break;
                    case 6653: // 湿度传感器5异常报警
                        break;
                    case 6811: // 主油箱加油报警
                        alarmValue = positionInfo.getFuelAmountOne() != null
                                     ? String.valueOf(Double.parseDouble(positionInfo.getFuelAmountOne())) + " L" : "";
                        break;
                    case 6812: // 主油箱漏油报警
                        alarmValue = positionInfo.getFuelSpillOne() != null
                                     ? String.valueOf(Double.parseDouble(positionInfo.getFuelSpillOne())) + " L" : "";
                        break;
                    case 6813: // 主油箱异常报警
                        break;
                    case 6821: // 副油箱加油报警
                        alarmValue = positionInfo.getFuelAmountTwo() != null
                                     ? String.valueOf(Double.parseDouble(positionInfo.getFuelAmountTwo())) + " L" : "";
                        break;
                    case 6822: // 副油箱漏油报警
                        alarmValue = positionInfo.getFuelSpillTwo() != null
                                     ? String.valueOf(Double.parseDouble(positionInfo.getFuelSpillTwo())) + " L" : "";
                        break;
                    case 6823: // 副油箱异常报警
                        break;
                    case 6831: // 油箱3加油报警
                        break;
                    case 6832: // 油箱3漏油报警
                        break;
                    case 6833: // 油箱3异常报警
                        break;
                    case 6841: // 油箱4加油报警
                        break;
                    case 6842: // 油箱4漏油报警
                        break;
                    case 6843: // 油箱4异常报警
                        break;
                    case 12411: // 正反转传感器异常报警
                        break;
                    case 124: //  反转报警
                        break;
                    //                    case 7111: // 门磁行驶门开报警
                    //                        break;
                    //                    case 7112: // 门磁未到门开报警
                    //                        break;
                    //                    case 7113: // 门磁超时门开报警
                    //                        break;
                    // 平台报警
                    case 74: // 围栏内超速
                        alarmValue = positionInfo.getSpeed() + " km/h" + "  " + alarmHandle.getFenceName();
                        break;
                    case 75: // 路线偏离报警
                        alarmValue = alarmHandle.getFenceName();
                        break;
                    case 76: // 平台超速报警
                        alarmValue = alarmHandle.getFenceName() != null ? positionInfo.getSpeed() + " km/h" + "(围栏:"
                            + alarmHandle.getFenceName() + ")" : positionInfo.getSpeed() + " km/h";
                        break;
                    case 77: // 普通异动报警
                        break;
                    case 78: // 车机疑似屏蔽报警
                        break;
                    case 79: // 疲劳驾驶报警
                        alarmValue =
                            backStrTimeByStartEndTime(alarmHandle.getAlarmStartTime(), alarmHandle.getAlarmEndTime());
                        break;
                    case 80: // 超速违章
                        break;
                    case 81: // 超速预警
                        break;
                    case 82: // 长时间下线报警
                        alarmValue =
                            backStrTimeByStartEndTime(alarmHandle.getAlarmStartTime(), alarmHandle.getAlarmEndTime());
                        break;
                    case 7702: // 异动报警(客运)
                        break;
                    case 7703: // 异动报警(山路)
                        break;
                    case 7211: // 进区域报警
                    case 7212: // 出区域报警
                    case 7311: // 进线路报警
                    case 7312: // 出线路报警
                    case 11911: // 到达关键点报警
                    case 11912: //  未到达关键点报警
                        alarmValue =
                            StringUtils.isNotBlank(alarmHandle.getFenceName()) ? "围栏: " + alarmHandle.getFenceName()
                                                                               : "";
                        break;
                    // I/O 报警
                    case 14004: // 终端I/O异常
                        break;
                    case 141000: // I/O采集板1异常
                        break;
                    case 142000: // I/O采集板2异常
                        break;
                    // 音视频报警
                    // ADAS报警
                    // DSM报警
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
                    detailInfo.setName(alarmName); // 报警名称
                    detailInfo.setAlarmStarTime(alarmHandle.getAlarmStartTime()); // 报警开始时间
                    detailInfo.setAlarmEndTime(alarmHandle.getAlarmEndTime()); // 报警结束时间
                    detailInfo.setStatus(alarmHandle.getStatus());// 报警处理状态
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
                List<byte[]> monitorIds = getMonitor(alarmQuery); // 监控对象id
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
        // 根据用户名查询企业
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
     * 计算持续性报警的持续时间
     */
    private String backStrTimeByStartEndTime(Long alarmStartTime, Long alarmEndTime) {
        Long startTime = alarmStartTime != null ? alarmStartTime : new Date().getTime(); // 停车开始时间
        Long endTime = alarmEndTime != null ? alarmEndTime : new Date().getTime(); // 停车结束时间
        Long stopTime = endTime - startTime; // 停车时长
        String stopTimeStr = "0秒";
        if (stopTime > 0) {
            stopTimeStr = DateUtil.formatTime(stopTime);
        }
        return stopTimeStr;
    }
}


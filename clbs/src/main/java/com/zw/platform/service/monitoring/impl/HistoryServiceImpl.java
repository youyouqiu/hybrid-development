package com.zw.platform.service.monitoring.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.cb.platform.domain.MileageStatisticInfo;
import com.cb.platform.domain.VehicleSpotCheckInfo;
import com.cb.platform.repository.mysqlDao.SpotCheckReportDao;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zw.app.domain.monitor.MonitorMileQueryParam;
import com.zw.app.domain.monitor.SwitchInfo;
import com.zw.app.domain.monitor.SwitchSignalInfo;
import com.zw.app.domain.monitor.WinchInfo;
import com.zw.app.util.common.AppResultBean;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.GroupDTO;
import com.zw.platform.basic.service.MonitorIconService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.enums.PlateColor;
import com.zw.platform.domain.basicinfo.form.OBDVehicleDataInfo;
import com.zw.platform.domain.basicinfo.form.OBDVehicleTypeForm;
import com.zw.platform.domain.enmu.ProtocolEnum;
import com.zw.platform.domain.oil.BdtdPosition;
import com.zw.platform.domain.oil.HistoryMileAndSpeed;
import com.zw.platform.domain.oil.HistoryOilMass;
import com.zw.platform.domain.oil.HistoryStopAndTravel;
import com.zw.platform.domain.oil.Positional;
import com.zw.platform.domain.oil.PositionalForm;
import com.zw.platform.domain.oil.PositionalQuery;
import com.zw.platform.domain.oil.RunPositional;
import com.zw.platform.domain.oil.StopPositional;
import com.zw.platform.domain.oil.TimeZoneExportForm;
import com.zw.platform.domain.oil.TimeZonePositional;
import com.zw.platform.domain.reportManagement.query.BigDataReportQuery;
import com.zw.platform.domain.vas.history.AreaInfo;
import com.zw.platform.domain.vas.history.HistoryStopData;
import com.zw.platform.domain.vas.history.TimeZoneQueryParam;
import com.zw.platform.domain.vas.history.TrackPlayBackChartDataQuery;
import com.zw.platform.domain.vas.monitoring.AlarmData;
import com.zw.platform.domain.vas.workhourmgt.SensorSettingInfo;
import com.zw.platform.push.handler.device.DeviceMessageHandler;
import com.zw.platform.repository.modules.OBDVehicleTypeDao;
import com.zw.platform.repository.vas.AlarmSettingDao;
import com.zw.platform.repository.vas.IoVehicleConfigDao;
import com.zw.platform.repository.vas.OilCalibrationDao;
import com.zw.platform.repository.vas.SensorPollingDao;
import com.zw.platform.repository.vas.SensorSettingsDao;
import com.zw.platform.service.alarm.AlarmSearchService;
import com.zw.platform.service.core.CustomColumnService;
import com.zw.platform.service.monitoring.HistoryService;
import com.zw.platform.service.obdManager.OBDVehicleTypeService;
import com.zw.platform.service.oil.PositionalService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.BigDataQueryUtil;
import com.zw.platform.util.CommonUtil;
import com.zw.platform.util.ConvertUtil;
import com.zw.platform.util.LocalDateUtils;
import com.zw.platform.util.TrackBackUtil;
import com.zw.platform.util.common.AlarmTypeUtil;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.UuidUtils;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.platform.util.common.ZipUtil;
import com.zw.platform.util.excel.ExportExcelParam;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.platform.util.paas.PaasCloudUrlUtil;
import com.zw.platform.util.report.PaasCloudHBaseAccessEnum;
import com.zw.platform.util.report.PaasCloudUrlEnum;
import com.zw.protocol.msg.Message;
import com.zw.protocol.msg.t808.T808Message;
import com.zw.protocol.msg.t808.body.LocationInfo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import static com.zw.platform.domain.oil.PositionalForm.STOP_STATE;
import static com.zw.platform.domain.oil.PositionalQuery.EXPORT_STATION;

/**
 * Created by LiaoYuecai on 2016/10/18.
 */
@Service
public class HistoryServiceImpl implements HistoryService {

    @Autowired
    RealTimeServiceImpl realTime;

    @Autowired
    private LogSearchService ls;

    @Autowired
    private UserService userService;

    @Autowired
    private AlarmSettingDao alarmSettingDao;

    @Autowired
    private SpotCheckReportDao spotCheckReportDao;

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    private SensorSettingsDao sensorSettingsDao;

    @Autowired
    private OBDVehicleTypeDao obdVehicleTypeDao;

    @Autowired
    private OBDVehicleTypeService obdVehicleTypeService;

    @Autowired
    private OilCalibrationDao oilCalibrationDao;

    @Autowired
    private SensorPollingDao sensorPollingDao;

    @Autowired
    private PositionalService positionalService;

    @Autowired
    private CustomColumnService customColumnService;

    @Autowired
    private IoVehicleConfigDao ioVehicleConfigDao;

    @Autowired
    private AlarmSearchService alarmSearchService;

    @Autowired
    private MonitorIconService monitorIconService;

    @Value("${positional.info.abnormal.data.filter.flag:false}")
    private boolean filterFlag;
    /**
     * ??????????????????
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * ??????
     */
    private static final Integer HALT_STATE = 0;

    /**
     * ??????
     */
    private static final Integer WORK_STATE = 1;

    /**
     * ??????
     */
    private static final Integer STANDBY_STATE = 2;

    private static final Logger logger = LogManager.getLogger(HistoryServiceImpl.class);

    /**
     * ????????????????????????
     */
    @Override
    public List<Positional> getHistory(String vehicleId, String startTime, String endTime) throws Exception {
        long stime;
        long ntime;
        stime = DateUtils.parseDate(startTime, DATE_FORMAT).getTime() / 1000;
        ntime = DateUtils.parseDate(endTime, DATE_FORMAT).getTime() / 1000;
        return getHistoryTrack(vehicleId, stime, ntime);
    }

    private List<Positional> getHistoryTrack(String vehicleId, long stime, long ntime) {
        final Map<String, String> params = new HashMap<>(8);
        params.put("vehicleId", vehicleId);
        params.put("startTime", String.valueOf(stime));
        params.put("endTime", String.valueOf(ntime));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_HISTORY_TRACK, params);
        return PaasCloudUrlUtil.getResultListData(str, Positional.class);
    }

    /**
     * ??????????????????????????????????????????????????????
     */
    @Override
    public JsonResultBean getHistoryVehicle(String vehicleId, String startTime, String endTime, Integer sensorFlag)
        throws Exception {
        Positional positional;
        JSONArray stops = new JSONArray();
        JSONArray resultful = new JSONArray();
        JSONArray stop = new JSONArray();
        HistoryStopData stopData = null;
        List<Positional> list = getHistory(vehicleId, startTime, endTime);
        // ???????????????????????????????????????
        if (list == null || list.isEmpty()) {
            return new JsonResultBean(false);
        }
        // ????????????????????????
        String functionalType = TrackBackUtil.getfunctionalType(vehicleId);
        // ?????????????????????
        Map<String, String> monitorIco = monitorIconService.getByMonitorId(Collections.singleton(vehicleId));
        BindDTO bindInfo = VehicleUtil.getBindInfoByRedis(vehicleId);
        String plateColor = PlateColor.getNameOrBlankByCode(String.valueOf(bindInfo.getPlateColor()));
        String simCard = bindInfo.getSimCardNumber();
        String icos = monitorIco.get(vehicleId);

        // ?????????????????????????????????????????????
        RedisKey redisKey = HistoryRedisKeyEnum.SENSOR_MESSAGE.of(vehicleId);
        boolean flogKey = RedisHelper.isContainsKey(redisKey);
        double longitudeOld = 0.0;
        double latitudeOld = 0.0;

        // ??????????????????????????????
        CommonUtil.positionalInfoAbnormalFilter(list, filterFlag);

        // ???????????????????????????,?????????????????????????????????????????????
        if (sensorFlag != null) {
            flogKey = sensorFlag == 1;
        }

        // ????????????,???????????????????????????
        for (int i = 0, n = list.size(); i < n; i++) {
            positional = list.get(i);
            positional.setPlateColor(plateColor);
            positional.setSimCard(simCard);
            positional.setIco(icos);
            // ???????????????0
            double longitude = Double.parseDouble(positional.getLongtitude());
            double latitude = Double.parseDouble(positional.getLatitude());
            String speed = "0.0";
            if (flogKey) {
                if (null != positional.getMileageSpeed()) {
                    speed = positional.getMileageSpeed().toString();
                }
            } else {
                if (null != positional.getSpeed()) {
                    speed = positional.getSpeed();
                }
            }
            if ((Math.abs(longitudeOld - longitude) < 0.00015 && Math.abs(latitudeOld - latitude) < 0.00015) || (
                ("0.0".equals(speed) || "0".equals(speed)) && i != 0 && i != list.size() - 1)) {
                if (stopData == null) {
                    stopData = new HistoryStopData();
                    // ????????????
                    stopData.setPositional(positional);
                    // ??????????????????
                    stopData.setStartTime(Converter.timeStamp2Date(String.valueOf(positional.getVtime()), null));
                }
                // ???????????????????????????
                if (i == n - 1) {
                    // ??????????????????
                    stopData.setEndTime(Converter.timeStamp2Date(String.valueOf(list.get(i - 1).getVtime()), null));
                    // ??????????????????
                    stopData.setStopTime(DateUtils.parseDate(stopData.getEndTime(), DATE_FORMAT).getTime() - DateUtils
                        .parseDate(stopData.getStartTime(), DATE_FORMAT).getTime());
                    stops.add(stopData);
                    stopData = null;
                }
                // ??????????????????????????????stop?????????
                stop.add(positional);
                if ("standby".equals(functionalType)) { // ?????????????????????????????????????????????
                    resultful.add(positional);
                }
            } else {
                // ?????????????????????
                if (stopData != null) {
                    stopData.setEndTime(Converter.timeStamp2Date(String.valueOf(list.get(i - 1).getVtime()), null));
                    stopData.setStopTime(DateUtils.parseDate(stopData.getEndTime(), DATE_FORMAT).getTime() - DateUtils
                        .parseDate(stopData.getStartTime(), DATE_FORMAT).getTime());
                    stops.add(stopData);
                    stopData = null;
                }
                longitudeOld = longitude;
                latitudeOld = latitude;
                resultful.add(positional);
            }
        }
        JSONObject msg = new JSONObject();
        msg.put("stops", stops);// ????????????
        msg.put("resultful", resultful);// ????????????
        msg.put("stop", stop);// ??????????????????
        msg.put("type", functionalType);// ????????????????????????
        msg.put("groups", realTime.getGroups(vehicleId));// ???????????????????????????
        msg.put("nowFlogKey", flogKey);
        String msgResult = JSON.toJSONString(msg, SerializerFeature.DisableCircularReferenceDetect);
        msgResult = ZipUtil.compress(msgResult);
        return new JsonResultBean(true, msgResult);
    }

    /**
     * ????????????????????????????????????
     */
    @Override
    public void addlog(String vehicleId, String ip) {
        // ??????id????????????????????????
        try {
            BindDTO bindInfo = VehicleUtil.getBindInfoByRedis(vehicleId);
            if (bindInfo != null) {
                String brand = bindInfo.getName();
                Integer plateColorInt = bindInfo.getPlateColor();
                String plateColor = plateColorInt == null ? "" : String.valueOf(plateColorInt);
                String groupName = bindInfo.getOrgName();
                String msg = "????????????:(" + brand + "@(" + groupName + ")" + ")????????????????????????";
                ls.addLog(ip, msg, "3", "MONITORING", brand, plateColor);
            }
        } catch (Exception e) {
            logger.error("?????????????????????????????????", e);
        }
    }

    /**
     * ???????????????????????????????????????????????????
     */
    @Override
    public JsonResultBean getHistoryPeople(String vehicleId, String startTime, String endTime, Integer sensorFlag)
        throws Exception {
        long stime;
        long ntime;
        BdtdPosition bdtdPosition;
        JSONArray stops = new JSONArray();
        JSONArray resultful = new JSONArray();
        JSONArray stop = new JSONArray();
        HistoryStopData stopData = null;
        JSONObject msg = new JSONObject();
        stime = DateUtils.parseDate(startTime, DATE_FORMAT).getTime() / 1000;
        ntime = DateUtils.parseDate(endTime, DATE_FORMAT).getTime() / 1000;
        List<BdtdPosition> list = getHistoryTrackPeople(vehicleId, stime, ntime);
        if (list == null || list.isEmpty()) {
            return new JsonResultBean(false);
        }
        double longtitudeOld = 0.0;
        double latitudeOld = 0.0;
        for (int i = 0, n = list.size(); i < n; i++) {
            bdtdPosition = list.get(i);
            // ???????????????0
            double longtitude =
                Double.parseDouble(bdtdPosition.getLongtitude() == null ? "0.0" : bdtdPosition.getLongtitude());
            double latitude =
                Double.parseDouble(bdtdPosition.getLatitude() == null ? "0.0" : bdtdPosition.getLatitude());
            if (longtitude != 0.0 && latitude != 0.0) {
                if ("0".equals(bdtdPosition.getSpeed()) || "0.0".equals(bdtdPosition.getSpeed())
                    || bdtdPosition.getSpeed() == null || (Math.abs(longtitudeOld - longtitude) < 0.00015
                    && Math.abs(latitudeOld - latitude) < 0.00015)) {
                    // ????????????????????????
                    if (stopData == null) {
                        stopData = new HistoryStopData();
                        // ????????????
                        stopData.setBdtdPosition(bdtdPosition);
                        // ??????????????????
                        stopData.setStartTime(Converter.timeStamp2Date(String.valueOf(bdtdPosition.getVtime()), null));
                    }
                    // ???????????????????????????
                    if (i == n - 1) {
                        // ??????????????????
                        stopData.setEndTime(Converter.timeStamp2Date(String.valueOf(list.get(i - 1).getVtime()), null));
                        // ??????????????????
                        stopData.setStopTime(
                            DateUtils.parseDate(stopData.getEndTime(), DATE_FORMAT).getTime() - DateUtils
                                .parseDate(stopData.getStartTime(), DATE_FORMAT).getTime());
                        stops.add(stopData);
                        stopData = null;
                    }
                    // ??????????????????????????????stop?????????
                    stop.add(bdtdPosition);
                } else {
                    // ?????????????????????
                    if (stopData != null) {
                        stopData.setEndTime(Converter.timeStamp2Date(String.valueOf(list.get(i - 1).getVtime()), null));
                        stopData.setStopTime(
                            DateUtils.parseDate(stopData.getEndTime(), DATE_FORMAT).getTime() - DateUtils
                                .parseDate(stopData.getStartTime(), DATE_FORMAT).getTime());
                        stops.add(stopData);
                        stopData = null;
                    }
                    longtitudeOld = longtitude;
                    latitudeOld = latitude;
                    resultful.add(bdtdPosition);
                }
            }
        }
        msg.put("stops", stops);// ??????????????????
        msg.put("resultful", resultful);// ????????????
        msg.put("stop", stop);// ????????????
        msg.put("groups", realTime.getGroups(vehicleId));// ???????????????????????????
        String msgResult = JSON.toJSONString(msg, SerializerFeature.DisableCircularReferenceDetect);
        msgResult = ZipUtil.compress(msgResult);
        return new JsonResultBean(true, msgResult);
    }

    private List<BdtdPosition> getHistoryTrackPeople(String vehicleId, long stime, long ntime) {
        final Map<String, String> params = new HashMap<>(8);
        params.put("vehicleId", vehicleId);
        params.put("startTime", String.valueOf(stime));
        params.put("endTime", String.valueOf(ntime));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_HISTORY_TRACK_PEOPLE, params);
        return PaasCloudUrlUtil.getResultListData(str, BdtdPosition.class);
    }

    /**
     * ?????????????????????????????????????????????
     * ????????????:
     * ??????:APP 2.1.0 ??????:APP????????????????????????????????????????????????
     */
    @Override
    public JsonResultBean changeHistoryActiveDate(String vehicleId, String nowMonth, String nextMonth, String type,
        Integer bigDataFlag, boolean isAppFlag) throws Exception {
        JSONObject msg = new JSONObject();
        JSONArray dates = new JSONArray();
        JSONArray dailyMiles = new JSONArray();
        JSONArray dailySensorFlag = new JSONArray();
        nowMonth = nowMonth + " 00:00:00";
        nextMonth = nextMonth + " 00:00:00";
        boolean offLineFlag = false;
        // ???????????? id????????????????????????????????????
        List<Map<String, String>> activeDate = null;
        RedisKey redisKey = RedisKeyEnum.MONITOR_INFO.of(vehicleId);
        if (!isAppFlag && RedisHelper.isContainsKey(redisKey)) {
            saveVehicleHistorySpotCheck(vehicleId);
        }
        /* ??????????????????????????????????????????????????? */
        if (Objects.equals(bigDataFlag, 1)) { //??????
            MonitorMileQueryParam param = new MonitorMileQueryParam();
            param.setMonitorId(vehicleId);
            param.setQueryStartMonth(nowMonth);
            param.setQueryEndMonth(nextMonth);
            param.setAppFlag(isAppFlag);
            // ????????????????????????
            List<MileageStatisticInfo> offLineData =
                getMonitorOffLineMileData(isAppFlag, nowMonth, nextMonth, vehicleId);
            param.setOffLineData(offLineData);
            // ????????????
            offLineFlag = getBigDataCalendarMileInfo(dates, dailyMiles, dailySensorFlag, param);
        } else {
            if (StringUtils.isNotBlank(type)) { //??????
                switch (type) {
                    case "0": // 808 2011??????
                    case "1": // 808 2013
                    case "2": // ??????
                    case "3": // ??????
                    case "6": // KKS
                    case "8": // BSJ-A5
                    case "11":
                    case "12":
                    case "13":
                        //  case "5":
                        boolean flogKey = RedisHelper.isContainsKey(HistoryRedisKeyEnum.SENSOR_MESSAGE.of(vehicleId));
                        if (flogKey) {
                            activeDate =
                                getDailyMileByDateSensorMessage(vehicleId, Converter.convertToUnixTimeStamp(nowMonth),
                                    Converter.convertToUnixTimeStamp(nextMonth));
                        } else {
                            activeDate = getDailyMileByDate(vehicleId, Converter.convertToUnixTimeStamp(nowMonth),
                                Converter.convertToUnixTimeStamp(nextMonth));
                        }
                        break;
                    case "5": // BDTD-SM
                        activeDate = getDailyMileByDatePeople(vehicleId, Converter.convertToUnixTimeStamp(nowMonth),
                            Converter.convertToUnixTimeStamp(nextMonth));
                        break;
                    case "9": // ASO
                    case "10": // F3????????????
                        activeDate = getDailyPointByDate(vehicleId, Converter.convertToUnixTimeStamp(nowMonth),
                            Converter.convertToUnixTimeStamp(nextMonth));
                        break;
                    default:
                        break;

                }
            }
            if (activeDate != null) {
                for (Map<String, String> date : activeDate) {
                    dates.add(date.get("ATIME"));
                    String mile = String.valueOf(date.get("MILE"));
                    if (type.equals("standby")) {
                        dailyMiles.add(mile);
                    } else {
                        double mileD = Double.parseDouble(mile) * 10;
                        double mileF = Math.round(mileD) / 10.0;
                        String mileS = String.valueOf(mileF);
                        dailyMiles.add(mileS);
                    }
                }
            }
        }
        msg.put("date", dates);
        msg.put("dailyMile", dailyMiles);
        msg.put("dailySensorFlag", dailySensorFlag);
        msg.put("type", type);
        if (offLineFlag || CollectionUtils.isNotEmpty(activeDate)) {
            return new JsonResultBean(true, JSON.toJSONString(msg, true));
        } else {
            return new JsonResultBean(false);
        }
    }

    /**
     * ????????????????????????????????????(????????????)
     */
    private List<MileageStatisticInfo> getMonitorOffLineMileData(boolean isAppFlag, String nowMonth, String nextMonth,
        String monitorId) throws Exception {
        List<MileageStatisticInfo> offLineData = new ArrayList<>();
        if (StringUtils.isBlank(nowMonth) || StringUtils.isBlank(nextMonth) || StringUtils.isBlank(monitorId)) {
            return offLineData;
        }
        if (isAppFlag) { // APP????????????????????????
            Long startTime = DateUtils.parseDate(nowMonth, "yyyy-MM-dd HH:mm:ss").getTime();
            Long endTime = DateUtils.parseDate(nextMonth, "yyyy-MM-dd HH:mm:ss").getTime();
            List<BigDataReportQuery> queryParam =
                BigDataQueryUtil.getBigMonthDataReportQuery(Arrays.asList(monitorId.split(",")), startTime, endTime);
            for (BigDataReportQuery query : queryParam) {
                try {
                    offLineData.addAll(getOffLineMileageStatisticData(query));
                } catch (BadSqlGrammarException e) {
                    // ??????????????????
                }
            }
        } else { // ?????????????????????????????????????????????
            nowMonth = nowMonth.substring(0, 7).replaceAll("-", "");
            offLineData = getMileageStatisticData(nowMonth, monitorId);
        }
        return offLineData;
    }

    private List<MileageStatisticInfo> getMileageStatisticData(String nowMonth, String monitorId) {
        final Map<String, String> params = new HashMap<>(8);
        params.put("vehicleId", monitorId);
        params.put("day", nowMonth);
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_MILEAGE_STATISTIC_DATA, params);
        return PaasCloudUrlUtil.getResultListData(str, MileageStatisticInfo.class);
    }

    private List<MileageStatisticInfo> getOffLineMileageStatisticData(BigDataReportQuery query) {
        Map<String, String> params = new HashMap<>(2);
        params.put("value", JSON.toJSONString(query));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_OFF_LINE_MILEAGE_STATISTIC_DATA, params);
        return PaasCloudUrlUtil.getResultListData(str, MileageStatisticInfo.class);
    }

    /**
     * ????????????????????????????????????(???????????? + ??????????????????)
     * @param dates           (????????????Array)
     * @param dailyMiles      ???????????????Array???
     * @param dailySensorFlag ??????????????????????????????Array???
     */
    private boolean getBigDataCalendarMileInfo(JSONArray dates, JSONArray dailyMiles, JSONArray dailySensorFlag,
        MonitorMileQueryParam param) throws Exception {
        if (param == null) {
            return false;
        }
        // ????????????id
        String monitorId = param.getMonitorId();
        String queryStartMonth = param.getQueryStartMonth();
        String queryEndMonth = param.getQueryEndMonth();
        if (StringUtils.isBlank(monitorId) || StringUtils.isBlank(queryStartMonth) || StringUtils
            .isBlank(queryEndMonth)) {
            return false;
        }
        List<MileageStatisticInfo> offLineData = param.getOffLineData();
        if (CollectionUtils.isEmpty(offLineData)) {
            offLineData = new ArrayList<>();
        }
        Calendar nowTime = Calendar.getInstance();
        nowTime.set(Calendar.HOUR_OF_DAY, 0);
        nowTime.set(Calendar.MINUTE, 0);
        nowTime.set(Calendar.SECOND, 0);
        nowTime.set(Calendar.HOUR_OF_DAY, 23);
        nowTime.set(Calendar.MINUTE, 59);
        nowTime.set(Calendar.SECOND, 59);
        if (param.isAppFlag()) {
            Calendar appNowMonth = Calendar.getInstance();
            appNowMonth.setTimeInMillis(DateUtils.parseDate(queryEndMonth, DATE_FORMAT).getTime());
            appNowMonth.add(Calendar.DAY_OF_MONTH, -1);
        }
        // ????????????????????????????????????????????????????????????????????????????????????????????????    ????????????flink??????????????????  ???????????????
        Long queryStartDateTime = DateUtils.parseDate(queryStartMonth, "yyyy-MM-dd HH:mm:ss").getTime();
        /* 6.???????????????????????? */
        for (MileageStatisticInfo mileageStatisticInfo : offLineData) {
            Long dayTime = mileageStatisticInfo.getDay();
            // ????????????????????????????????????????????????
            Integer date = DateUtil.getTwoTimeDifference(queryStartDateTime, dayTime);
            dates.add(date);
            Integer sensorFlag = mileageStatisticInfo.getSensorFlag();
            // ????????????????????????????????????, 3.8.2????????????sensorFlag = 0, ???????????????????????????milage=gpsMile, sensorFlag =1?????????????????????????????????milage=???????????????
            Double mileage = mileageStatisticInfo.getMileage();
            Double gpsMile = mileageStatisticInfo.getGpsMile();
            if (sensorFlag == 0) {
                // ??????????????????, gpsMile?????????, ???mileage??????,?????????mileage
                if (gpsMile == null && mileage != null) {
                    dailyMiles.add(mileage);
                } else {
                    dailyMiles.add(gpsMile == null ? 0.0 : gpsMile);
                }
            } else {
                // ??????????????????,mileage?????????, ???gpsMile??????, ?????????gpsMile
                if (mileage == null && gpsMile != null) {
                    dailyMiles.add(gpsMile);
                } else {
                    dailyMiles.add(mileage == null ? 0.0 : mileage);
                }
            }
            dailySensorFlag.add(sensorFlag);
        }

        /* 7.???????????????????????? */
        return offLineData.size() > 0;
    }

    /**
     * ????????????????????????????????????
     */
    private void saveVehicleHistorySpotCheck(String vehicleId) {
        String userName = SystemHelper.getCurrentUsername();
        VehicleSpotCheckInfo vehicleSpotCheckInfo = new VehicleSpotCheckInfo();  //????????????????????????
        vehicleSpotCheckInfo.setSpotCheckUser(userName);
        vehicleSpotCheckInfo.setVehicleId(vehicleId);
        vehicleSpotCheckInfo.setSpotCheckTime(new Date());
        vehicleSpotCheckInfo.setActualViewDate(new Date());
        vehicleSpotCheckInfo.setSpotCheckContent(1);
        Integer speedLimit = alarmSettingDao.getSpeedLimitByVehicleId(vehicleId);
        if (Objects.nonNull(speedLimit)) {
            vehicleSpotCheckInfo.setSpeedLimit(String.valueOf(speedLimit));
        }
        RedisKey locationRedisKey = HistoryRedisKeyEnum.MONITOR_LOCATION.of(vehicleId);
        String cacheLocationInfo = RedisHelper.getString(locationRedisKey);
        if (StringUtils.isNotEmpty(cacheLocationInfo)) {
            Message message = JSON.parseObject(cacheLocationInfo, Message.class);
            T808Message t808Message = JSON.parseObject(message.getData().toString(), T808Message.class);
            LocationInfo info = JSON.parseObject(t808Message.getMsgBody().toString(), LocationInfo.class);
            String y = String.valueOf(info.getLongitude());
            String x = String.valueOf(info.getLatitude());
            String timet = info.getGpsTime();
            Double gpsSpeed = info.getGpsSpeed();
            Long timeO = null;
            if (StringUtils.isNotBlank(timet)) {
                try {
                    if (timet.length() == 12) {
                        timeO = DateUtils.parseDate("20" + timet, "yyyyMMddHHmmss").getTime() / 1000;
                    } else if (timet.length() == 14) {
                        timeO = DateUtils.parseDate(timet, "yyyyMMddHHmmss").getTime() / 1000;
                    }
                } catch (ParseException e) {
                    timeO = System.currentTimeMillis() / 1000;
                }
            }
            if (timeO != null) {
                vehicleSpotCheckInfo.setLocationTime(new Date(timeO * 1000));
            }
            vehicleSpotCheckInfo.setLongtitude(y);
            vehicleSpotCheckInfo.setLatitude(x);
            vehicleSpotCheckInfo.setSpeed(gpsSpeed == null ? "" : gpsSpeed.toString());
        }
        spotCheckReportDao.addVehicleSpotCheckInfo(vehicleSpotCheckInfo);
    }

    @Override
    public List<Map<String, String>> getDailyMileByDate(String vehicleId, long date1, long date2) {
        final Map<String, String> params = new HashMap<>(8);
        params.put("vehicleId", vehicleId);
        params.put("startTime", String.valueOf(date1));
        params.put("endTime", String.valueOf(date2));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_DAILY_MILE_BY_DATE, params);
        return (List) PaasCloudUrlUtil.getResultListData(str, Map.class);
    }

    @Override
    public List<Map<String, String>> getDailyMileByDateSensorMessage(String vehicleId, long date1, long date2) {
        final Map<String, String> params = new HashMap<>(8);
        params.put("vehicleId", vehicleId);
        params.put("startTime", String.valueOf(date1));
        params.put("endTime", String.valueOf(date2));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_DAILY_MILE_BY_DATE_SENSOR_MESSAGE, params);
        return (List) PaasCloudUrlUtil.getResultListData(str, Map.class);
    }

    @Override
    public List<Map<String, String>> getDailyPointByDate(String vehicleId, long date1, long date2) {
        final Map<String, String> params = new HashMap<>(8);
        params.put("vehicleId", vehicleId);
        params.put("startTime", String.valueOf(date1));
        params.put("endTime", String.valueOf(date2));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_DAILY_POINT_BY_DATE, params);
        return (List) PaasCloudUrlUtil.getResultListData(str, Map.class);
    }

    @Override
    public List<Map<String, String>> getDailyMileByDatePeople(String vehicleId, long date1, long date2) {
        final Map<String, String> params = new HashMap<>(8);
        params.put("vehicleId", vehicleId);
        params.put("startTime", String.valueOf(date1));
        params.put("endTime", String.valueOf(date2));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_DAILY_MILE_BY_DATE_PEOPLE, params);
        return (List) PaasCloudUrlUtil.getResultListData(str, Map.class);
    }

    @Override
    public List<Positional> getQueryDetails(String vehicleId, long startTime, long endTime) {
        final Map<String, String> params = new HashMap<>(8);
        params.put("vehicleId", vehicleId);
        params.put("startTime", String.valueOf(startTime));
        params.put("endTime", String.valueOf(endTime));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_HISTORY_TRACK, params);
        return PaasCloudUrlUtil.getResultListData(str, Positional.class);
    }

    @Override
    public JsonResultBean getAlarmData(String vehicleId, Long startTime, Long endTime, Boolean isSaveRedisDataFlag,
        boolean isLkywTrackBack) throws Exception {
        String userUuid = userService.getCurrentUserUuid();
        RedisKey redisKey;
        if (isLkywTrackBack) {
            redisKey = HistoryRedisKeyEnum.LKYW_TRACK_PLAYBACK_ALARM_SUFFIX_KEY.of(userUuid, vehicleId);
        } else {
            redisKey = HistoryRedisKeyEnum.TRACK_PLAYBACK_ALARM_SUFFIX_KEY.of(userUuid, vehicleId);
        }
        if (isSaveRedisDataFlag) {
            TrackBackUtil.removeRedisData(redisKey);
        }
        List<Integer> allAlarmTypeList = getAllAlarmPosByRedis();
        List<AlarmData> alarmList = alarmSearchService.getAlarmInfo(vehicleId, StringUtils.join(allAlarmTypeList, ","),
            DateUtil.getLongToDateStr(startTime * 1000, null), DateUtil.getLongToDateStr(endTime * 1000, null), null,
            null, null, null, null, AlarmData.class, null);
        if (CollectionUtils.isEmpty(alarmList)) {
            return new JsonResultBean();
        }
        BindDTO bindDTO = VehicleUtil.getBindInfoByRedis(vehicleId);
        String assignmentName = null;
        if (bindDTO != null) {
            Map<String, String> userGroupIdAndNameMap = userService.getCurrentUserGroupList().stream()
                .collect(Collectors.toMap(GroupDTO::getId, GroupDTO::getName));
            String groupIds = bindDTO.getGroupId();
            assignmentName = Arrays.stream(groupIds.split(",")).map(userGroupIdAndNameMap::get).filter(Objects::nonNull)
                .collect(Collectors.joining(","));
        }
        final Set<Integer> nonIoAlarmTypes =
            alarmList.stream().map(AlarmData::getAlarmType).collect(Collectors.toSet());
        nonIoAlarmTypes.removeAll(AlarmTypeUtil.IO_ALARM);
        final Map<Integer, String> alarmCodeNameMap = AlarmTypeUtil.getAlarmType(nonIoAlarmTypes);

        for (AlarmData alarmData : alarmList) {
            alarmData.setAssignmentName(assignmentName);
            alarmData.setAlarmStatus(Objects.equals(alarmData.getStatus(), 0) ? "?????????" : "?????????");
            alarmData.setEndTime(DateUtil.getLongToDateStr(alarmData.getAlarmEndTime(), null));
            alarmData.setStartTime(DateUtil.getLongToDateStr(alarmData.getAlarmStartTime(), null));
            Integer alarmType = alarmData.getAlarmType();
            if (!AlarmTypeUtil.IO_ALARM.contains(alarmType)) {
                alarmData.setDescription(alarmCodeNameMap.get(alarmType));
            }
        }
        if (isSaveRedisDataFlag) {
            String compressData = ZipUtil.compress(JSON.toJSONString(alarmList));
            TrackBackUtil.addResultToRedis(redisKey, compressData);
        }
        return new JsonResultBean(alarmList);
    }

    /**
     * ???redis?????????????????????????????????
     */
    private List<Integer> getAllAlarmPosByRedis() {
        List<Integer> alarmPos = new ArrayList<>();
        //????????????key
        List<String> twoData = RedisHelper.scanKeys(HistoryRedisKeyEnum.ALARM_TYPE_INFO.of("*"));
        if (twoData != null) {
            for (String posKey : twoData) {
                if (StringUtils.isNotBlank(posKey)) {
                    String[] keyData = posKey.split("_");
                    if (keyData.length == 2) {
                        String pos = keyData[0];
                        if (StringUtils.isNotBlank(pos) && pos.trim().matches("^[0-9]*$")) {
                            alarmPos.add(Integer.parseInt(pos.trim()));
                        }
                    }
                }
            }
        }
        return alarmPos;
    }

    @Override
    public JsonResultBean findHistoryByTimeAndAddress(TimeZoneQueryParam queryParam) {
        // ????????????
        Map<String, List<TimeZonePositional>> resultMap = new ConcurrentHashMap<>(16);
        resultMap.put("areaOne", Lists.newArrayList());
        resultMap.put("areaTwo", Lists.newArrayList());
        // ????????????ID??????
        List<String> monitorIds =
            Arrays.stream(queryParam.getMonitorIds().split(",")).distinct().collect(Collectors.toList());
        Map<String, BindDTO> bindDTOMap = VehicleUtil.batchGetBindInfosByRedis(monitorIds, Lists.newArrayList("name"));
        List<byte[]> monitoryIdByteList = UuidUtils.batchTransition(monitorIds);
        // ?????? + ??????
        getResultMap(resultMap, queryParam, monitoryIdByteList);
        for (List<TimeZonePositional> list : resultMap.values()) {
            for (TimeZonePositional timeZonePositional : list) {
                String vehicleIdStr = timeZonePositional.getVehicleIdStr();
                BindDTO bindDTO = bindDTOMap.get(vehicleIdStr);
                if (bindDTO == null) {
                    continue;
                }
                timeZonePositional.setMonitorNumber(bindDTO.getName());
            }
        }
        // ??????: ????????????, ??????????????????
        resultMap.put("areaOne",
            resultMap.get("areaOne").stream().sorted(Comparator.comparing(TimeZonePositional::getMonitorNumber))
                .collect(Collectors.toList()));
        resultMap.put("areaTwo",
            resultMap.get("areaTwo").stream().sorted(Comparator.comparing(TimeZonePositional::getMonitorNumber))
                .collect(Collectors.toList()));
        // ???resultMap??????redis???????????????????????????
        RedisKey redisKey =
            HistoryRedisKeyEnum.TRACK_PLAYBACK_TIME_ZONE_SUFFIX_KEY.of(SystemHelper.getCurrentUsername());
        if (RedisHelper.isContainsKey(redisKey)) {
            RedisHelper.delete(redisKey);
        }
        RedisHelper.addMapToHash(redisKey, resultMap);
        //??????????????????30??????
        RedisHelper.expireKey(redisKey, 1800);
        return new JsonResultBean(resultMap);
    }

    @Override
    public JsonResultBean getOilConsumptionChartData(String vehicleId, String startTime, String endTime,
        Integer sensorFlag, Integer sensorNo) throws Exception {
        JSONObject result = new JSONObject();
        JSONArray oilConsumptionChartDataArr = new JSONArray();
        List<Integer> monitorBandOilExpendSensorNoList = sensorSettingsDao.getMonitorBandOilExpendSensorNo(vehicleId);
        List<Integer> sensorNoList =
            monitorBandOilExpendSensorNoList.stream().map(no -> no - 68).collect(Collectors.toList());
        // ????????????????????????
        if (CollectionUtils.isEmpty(sensorNoList)) {
            return new JsonResultBean(JsonResultBean.FAULT, "???????????????????????????????????????!");
        } else {
            if (sensorNo == null) {
                sensorNo = sensorNoList.get(0);
            }
            int sensorIndex = sensorNoList.indexOf(sensorNo);
            if (sensorIndex == -1) {
                sensorNo = null;
            }
        }
        boolean flogKey = RedisHelper.isContainsKey(HistoryRedisKeyEnum.SENSOR_MESSAGE.of(vehicleId));
        if (sensorFlag != null) {
            flogKey = sensorFlag == 1;
        }
        List<Positional> positionalList = getCachePositionalInfoList(vehicleId, startTime, endTime, sensorFlag);
        if (CollectionUtils.isNotEmpty(positionalList)) {
            for (Positional positional : positionalList) {
                JSONObject positionalJsonObj = new JSONObject();
                positionalJsonObj.put("time", positional.getVtime());
                positionalJsonObj.put("oilWear", sensorNo == null ? null :
                    (sensorNo == 1 ? positional.getTotalOilwearOne() : positional.getTotalOilwearTwo()));
                Double mileageTotal = positional.getMileageTotal();
                mileageTotal = mileageTotal == null ? 0.0 : mileageTotal;
                String gpsMile = positional.getGpsMile();
                gpsMile = StringUtils.isBlank(gpsMile) ? "0" : gpsMile;
                Double mileage = flogKey ? mileageTotal : Double.parseDouble(gpsMile);
                positionalJsonObj.put("mileage", mileage);
                oilConsumptionChartDataArr.add(positionalJsonObj);
            }
        }
        result.put("sensorNoList", sensorNoList);
        result.put("sensorDataList", oilConsumptionChartDataArr);
        return new JsonResultBean(result);
    }

    /**
     * ???????????????redis????????????????????????,??????????????????hbase
     */
    private List<Positional> getCachePositionalInfoList(String vehicleId, String startTime, String endTime,
        Integer sensorFlag) throws Exception {
        // ???????????????????????????id
        String username = SystemHelper.getCurrentUsername();
        RedisKey trackPlaybackBaseDataKey = HistoryRedisKeyEnum.TRACK_PLAYBACK_BASE_DATA.of(username, vehicleId);
        List<Positional> positionalList;
        if (RedisHelper.isContainsKey(trackPlaybackBaseDataKey)) {
            positionalList = RedisHelper.getList(trackPlaybackBaseDataKey, Positional.class);
        } else {
            positionalList =
                queryHistoryDataAndSaveRedis(vehicleId, startTime, endTime, sensorFlag, trackPlaybackBaseDataKey);
        }
        return positionalList;
    }

    /**
     * app????????????hbase??????
     */
    private List<Positional> getAppCachePositionalInfoList(String vehicleId, String startTime, String endTime,
        Integer sensorFlag) throws Exception {
        boolean flogKey = RedisHelper.isContainsKey(HistoryRedisKeyEnum.SENSOR_MESSAGE.of(vehicleId));
        if (sensorFlag != null) {
            flogKey = sensorFlag == 1;
        }
        return getHistoryData(vehicleId, startTime, endTime, flogKey, null);
    }

    @Override
    public JsonResultBean getTemperatureChartData(String vehicleId, String startTime, String endTime,
        Integer sensorFlag, Integer sensorNo) throws Exception {
        JSONObject result = new JSONObject();
        JSONArray temperatureChartDataArr = new JSONArray();
        // ????????????????????????????????????
        List<SensorSettingInfo> monitorBandSensorInfoBySensorType =
            sensorSettingsDao.getMonitorBandSensorInfoBySensorType(vehicleId, 1);
        // ???????????????
        List<Integer> sensorNoList =
            monitorBandSensorInfoBySensorType.stream().map(info -> Integer.parseInt(info.getSensorOutId(), 16) - 32)
                .collect(Collectors.toList());
        SensorSettingInfo sensorSettingInfo;
        if (CollectionUtils.isEmpty(sensorNoList)) {
            return new JsonResultBean(JsonResultBean.FAULT, "???????????????????????????????????????");
        } else {
            if (sensorNo == null) {
                sensorNo = sensorNoList.get(0);
            }
            int sensorIndex = sensorNoList.indexOf(sensorNo);
            if (sensorIndex == -1) {
                sensorNo = null;
            }
            sensorSettingInfo = sensorIndex == -1 || sensorIndex >= monitorBandSensorInfoBySensorType.size() ? null :
                monitorBandSensorInfoBySensorType.get(sensorIndex);
        }
        Double maxTemperature = null;
        Double minTemperature = null;
        // ????????????
        List<Positional> positionalList = getCachePositionalInfoList(vehicleId, startTime, endTime, sensorFlag);
        if (CollectionUtils.isNotEmpty(positionalList)) {
            for (Positional positional : positionalList) {
                JSONObject positionalJsonObj = new JSONObject();
                //??????
                positionalJsonObj.put("time", positional.getVtime());
                Integer tempValue = getTemperatureBySensorNo(sensorNo, positional);
                Double temperature = tempValue != null ? tempValue * 1.0 / 10.0 : null;
                //??????
                positionalJsonObj.put("temperature", temperature);
                temperatureChartDataArr.add(positionalJsonObj);
                if (temperature == null) {
                    continue;
                }
                maxTemperature =
                    maxTemperature == null ? temperature : temperature > maxTemperature ? temperature : maxTemperature;
                minTemperature =
                    minTemperature == null ? temperature : temperature < minTemperature ? temperature : minTemperature;
            }
        }
        result.put("sensorNoList", sensorNoList);
        result.put("sensorDataList", temperatureChartDataArr);
        // ?????????
        result.put("maxTemperature", maxTemperature);
        // ?????????
        result.put("minTemperature", minTemperature);
        // ?????????
        result.put("highTemperatureThreshold", sensorSettingInfo == null ? null : sensorSettingInfo.getAlarmUp());
        // ?????????
        result.put("lowTemperatureThreshold", sensorSettingInfo == null ? null : sensorSettingInfo.getAlarmDown());
        return new JsonResultBean(result);
    }

    /**
     * ????????????????????????????????????
     */
    private Integer getTemperatureBySensorNo(Integer sensorNo, Positional positional) {
        Integer tempValue;
        if (sensorNo == null) {
            return null;
        }
        if (sensorNo == 1) {
            tempValue = positional.getTempValueOne();
        } else if (sensorNo == 2) {
            tempValue = positional.getTempValueTwo();
        } else if (sensorNo == 3) {
            tempValue = positional.getTempValueThree();
        } else if (sensorNo == 4) {
            tempValue = positional.getTempValueFour();
        } else {
            tempValue = positional.getTempValueFive();
        }
        return tempValue;
    }

    /**
     * ????????????????????????????????????
     */
    private JSONObject getLoadWeightBySensorNo(Integer sensorNo, Positional positional) {
        JSONObject loadObj;
        if (sensorNo == null) {
            return null;
        }
        if (sensorNo == 1) {
            loadObj = JSONObject.parseObject(positional.getLoadObjOne());
        } else {
            loadObj = JSONObject.parseObject(positional.getLoadObjTwo());
        }
        return loadObj;
    }

    /**
     * ????????????????????????????????????
     */
    private JSONArray getLoadWeight(Positional positional) {
        JSONArray re = new JSONArray();
        JSONObject loadObjOne = JSONObject.parseObject(positional.getLoadObjOne());
        JSONObject loadObjTwo = JSONObject.parseObject(positional.getLoadObjTwo());
        if (loadObjOne != null) {
            re.add(loadObjOne);
        }
        if (loadObjTwo != null) {
            re.add(loadObjTwo);
        }
        return re;
    }

    /**
     * ????????????????????????????????????
     */
    private Integer getHumidityBySensorNo(Integer sensorNo, Positional positional) {
        Integer wetnessValue;
        if (sensorNo == null) {
            return null;
        }
        if (sensorNo == 1) {
            wetnessValue = positional.getWetnessValueOne();
        } else if (sensorNo == 2) {
            wetnessValue = positional.getWetnessValueTwo();
        } else if (sensorNo == 3) {
            wetnessValue = positional.getWetnessValueThree();
        } else {
            wetnessValue = positional.getWetnessValueFour();
        }
        return wetnessValue;
    }

    @Override
    public JsonResultBean getHumidityChartData(String vehicleId, String startTime, String endTime, Integer sensorFlag,
        Integer sensorNo) throws Exception {
        JSONObject result = new JSONObject();
        JSONArray humidityChartDataArr = new JSONArray();
        // ????????????????????????????????????
        List<SensorSettingInfo> monitorBandSensorInfoBySensorType =
            sensorSettingsDao.getMonitorBandSensorInfoBySensorType(vehicleId, 2);
        Double maxHumidity = null;
        Double minHumidity = null;
        // ???????????????
        List<Integer> sensorNoList =
            monitorBandSensorInfoBySensorType.stream().map(info -> Integer.parseInt(info.getSensorOutId(), 16) - 37)
                .collect(Collectors.toList());
        SensorSettingInfo sensorSettingInfo;
        if (CollectionUtils.isEmpty(sensorNoList)) {
            return new JsonResultBean(JsonResultBean.FAULT, "???????????????????????????????????????");
        } else {
            if (sensorNo == null) {
                sensorNo = sensorNoList.get(0);
            }
            int sensorIndex = sensorNoList.indexOf(sensorNo);
            if (sensorIndex == -1) {
                sensorNo = null;
            }
            sensorSettingInfo = sensorIndex == -1 || sensorIndex >= monitorBandSensorInfoBySensorType.size() ? null :
                monitorBandSensorInfoBySensorType.get(sensorIndex);
        }
        // ????????????
        List<Positional> positionalList = getCachePositionalInfoList(vehicleId, startTime, endTime, sensorFlag);
        if (CollectionUtils.isNotEmpty(positionalList)) {
            for (Positional positional : positionalList) {
                JSONObject positionalJsonObj = new JSONObject();
                Integer wetnessValue = getHumidityBySensorNo(sensorNo, positional);
                Double humidity = wetnessValue != null ? wetnessValue * 1.0 : null;
                //??????
                positionalJsonObj.put("humidity", humidity);
                //??????
                positionalJsonObj.put("time", positional.getVtime());
                humidityChartDataArr.add(positionalJsonObj);
                if (humidity == null) {
                    continue;
                }
                maxHumidity = maxHumidity == null ? humidity : humidity > maxHumidity ? humidity : maxHumidity;
                minHumidity = minHumidity == null ? humidity : humidity < minHumidity ? humidity : minHumidity;
            }
        }
        result.put("sensorNoList", sensorNoList);
        result.put("sensorDataList", humidityChartDataArr);
        // ?????????
        result.put("maxHumidity", maxHumidity);
        // ?????????
        result.put("minHumidity", minHumidity);
        // ?????????
        result.put("highHumidityThreshold", sensorSettingInfo == null ? null : sensorSettingInfo.getAlarmUp());
        // ?????????
        result.put("lowHumidityThreshold", sensorSettingInfo == null ? null : sensorSettingInfo.getAlarmDown());
        return new JsonResultBean(result);
    }

    @Override
    public JsonResultBean getWorkHourChartData(String vehicleId, String startTime, String endTime, Integer sensorFlag,
        Integer sensorNo) throws Exception {
        JSONObject result = new JSONObject();
        JSONArray workHourChartDataArr = new JSONArray();
        // ????????????????????????????????????
        List<SensorSettingInfo> monitorBandSensorInfoBySensorType =
            sensorSettingsDao.getMonitorBandSensorInfoBySensorType(vehicleId, 4);
        // ???????????????
        List<Integer> sensorNoList =
            monitorBandSensorInfoBySensorType.stream().map(info -> Integer.parseInt(info.getSensorOutId(), 16) - 127)
                .collect(Collectors.toList());
        SensorSettingInfo sensorSettingInfo;
        if (CollectionUtils.isEmpty(sensorNoList)) {
            return new JsonResultBean(JsonResultBean.FAULT, "???????????????????????????????????????");
        } else {
            if (sensorNo == null) {
                sensorNo = sensorNoList.get(0);
            }
            int sensorIndex = sensorNoList.indexOf(sensorNo);
            if (sensorIndex == -1) {
                sensorNo = null;
            }
            sensorSettingInfo = sensorIndex == -1 || sensorIndex >= monitorBandSensorInfoBySensorType.size() ? null :
                monitorBandSensorInfoBySensorType.get(sensorIndex);
        }
        // ?????????????????? 1:???????????????;2:???????????????;3:???????????????; mysql?????????????????????????????????????????????????????????1
        Integer detectionMode = sensorSettingInfo == null ? null : sensorSettingInfo.getDetectionMode();
        // ????????????
        long workDuration = 0L;
        // ????????????
        long standByDuration = 0L;
        // ????????????
        long haltDuration = 0L;
        // ????????????
        List<Positional> positionalList = getCachePositionalInfoList(vehicleId, startTime, endTime, sensorFlag);
        if (CollectionUtils.isNotEmpty(positionalList)) {
            // ?????????????????? ?????????????????????????????????????????????
            JSONArray installWorkHourData = installWorkHourData(sensorNo, sensorSettingInfo, positionalList, true);
            if (installWorkHourData.size() > 0) {
                JSONObject zeroJsonObj = installWorkHourData.getJSONObject(0);
                // ?????? 1:????????????; 2:??????????????????; 3:?????????;
                Integer zeroType = zeroJsonObj.getInteger("type");
                // ???????????? 0:??????; 1:??????; 2:??????;
                Integer zeroWorkingPosition = zeroJsonObj.getInteger("workingPosition");
                // ???????????? -1:??????????????????; 0:??????; 1:??????; 2:??????;
                int oldState = zeroType == 2 ? -1 : zeroWorkingPosition;
                int workStateStartIndex = 0;
                for (int i = 0, len = installWorkHourData.size(); i < len; i++) {
                    JSONObject workHourInfoJsonObj = installWorkHourData.getJSONObject(i);
                    Long nowTime = workHourInfoJsonObj.getLong("timeL");
                    workHourInfoJsonObj.put("time", nowTime);
                    // ?????? 1:????????????; 2:??????????????????; 3:?????????;
                    Integer type = workHourInfoJsonObj.getInteger("type");
                    // ???????????? 0:??????; 1:??????; 2:??????;
                    Integer workingPosition = workHourInfoJsonObj.getInteger("workingPosition");
                    int nowState = type == 2 ? -1 : workingPosition;
                    int previousIndex = Math.max(i - 1, 0);
                    JSONObject previousWorkHourInfoJsonObj = installWorkHourData.getJSONObject(previousIndex);
                    Long previousTime = previousWorkHourInfoJsonObj.getLong("timeL");
                    // ????????????????????????
                    long timeInterval = nowTime - previousTime;
                    // ??????????????????????????????
                    boolean isNeedAddNullData = timeInterval > 300;
                    // ???????????????????????????????????????????????????300s???????????????????????? ????????????
                    if (isNeedAddNullData || !Objects.equals(nowState, oldState) || i == len - 1) {
                        JSONObject workStateStartJsonObj = installWorkHourData.getJSONObject(workStateStartIndex);
                        Long workStateStartTime = workStateStartJsonObj.getLong("timeL");
                        //??????????????????
                        long duration = previousTime - workStateStartTime;
                        duration = timeInterval <= 300 ? nowTime - workStateStartTime : duration;
                        // ??????
                        if (Objects.equals(oldState, 0)) {
                            haltDuration += duration;
                            // ??????
                        } else if (Objects.equals(oldState, 1)) {
                            workDuration += duration;
                            // ??????
                        } else if (Objects.equals(oldState, 2)) {
                            standByDuration += duration;
                        }
                        workStateStartIndex = i;
                        oldState = nowState;
                        //?????????????????? ??????????????? ??????????????????????????????
                    }
                    workHourChartDataArr.add(workHourInfoJsonObj);
                }
            }
        }
        // ??????????????????
        result.put("workInspectionMethod", detectionMode);
        // ??????
        result.put("thresholdValue", Objects.equals(detectionMode, 1) ? sensorSettingInfo.getThresholdVoltage() :
            Objects.equals(detectionMode, 2) ? sensorSettingInfo.getThreshold() : null);
        // ????????????
        result.put("workDuration", workDuration);
        // ????????????
        result.put("standByDuration", standByDuration);
        // ????????????
        result.put("haltDuration", haltDuration);
        // ????????????
        result.put("workHourInfo", workHourChartDataArr);
        return new JsonResultBean(result);
    }

    /**
     * ??????????????????
     * ?????????????????????????????????????????? ??????????????????????????????
     * @param isNeedDistinguishSensorNo ?????????????????????????????????
     */
    public JSONArray installWorkHourData(Integer sensorNo, SensorSettingInfo sensorSettingInfo,
        List<Positional> positionalList, boolean isNeedDistinguishSensorNo) {
        JSONArray workHourChartDataArr = new JSONArray();
        boolean sensorSettingInfoIsNull = sensorSettingInfo == null;
        // ?????????????????? 1:???????????????;2:???????????????;3:???????????????; mysql?????????????????????????????????????????????????????????1
        Integer detectionMode = sensorSettingInfoIsNull ? null : sensorSettingInfo.getDetectionMode() - 1;
        // ???????????????km/h??? ???????????????
        Double speedThreshold = sensorSettingInfoIsNull ? null : sensorSettingInfo.getSpeedThreshold();
        // ???????????????
        Double baudRateThreshold = sensorSettingInfoIsNull ? null : sensorSettingInfo.getBaudRateThreshold();
        // ?????????????????????
        Integer baudRateCalculateNumber =
            sensorSettingInfoIsNull ? null : sensorSettingInfo.getBaudRateCalculateNumber();
        //??????????????????????????????
        Integer needSetStandbyNum = 0;
        // ????????????
        for (int i = positionalList.size() - 1; i >= 0; i--) {
            JSONObject workHourInfoJsonObj = new JSONObject();
            Positional positional = positionalList.get(i);
            // ???????????? ??????????????????????????????????????? ???????????????
            Integer workingPosition = detectionMode == null ? null : detectionMode == 2 ? WORK_STATE :
                (isNeedDistinguishSensorNo
                    ? (sensorNo == 1 ? positional.getWorkingPositionOne() : positional.getWorkingPosition()) :
                    positional.getWorkingPosition());
            //????????????
            Double checkData = sensorNo == null ? null : isNeedDistinguishSensorNo
                ? (sensorNo == 1 ? positional.getCheckDataOne() : positional.getCheckDataTwo()) :
                positional.getCheckData();
            // ??????????????????
            Integer workInspectionMethod = sensorNo == null ? null : isNeedDistinguishSensorNo
                ? (sensorNo == 1 ? positional.getWorkInspectionMethodOne()
                : positional.getWorkInspectionMethodTwo()) : positional.getWorkInspectionMethod();
            // ?????? 1:????????????; 2:??????????????????; 3:?????????;
            Integer type = workInspectionMethod == null || !Objects.equals(workInspectionMethod, detectionMode) ? 2 : 1;
            if (Objects.equals(2, workInspectionMethod) && Objects.equals(2, detectionMode)) {
                // ??????
                String speed = positional.getSpeed();
                // ?????????(??????)
                Double fluctuateValue = sensorNo == null ? null : isNeedDistinguishSensorNo
                    ? (sensorNo == 1 ? positional.getFluctuateValueOne() : positional.getFluctuateValueTwo())
                    : positional.getFluctuateValue();
                // ?????????????????????????????????????????????0 ?????? ???S<A????????????????????????????????????N???????????????????????????
                if (baudRateThreshold != null && (fluctuateValue == null || fluctuateValue < baudRateThreshold)
                    && !Objects.equals(checkData, 0.0)) {
                    needSetStandbyNum = baudRateCalculateNumber;
                }
                if (needSetStandbyNum > 0) {
                    workingPosition = STANDBY_STATE;
                    needSetStandbyNum--;
                }
                if (i != 0) {
                    Positional previousPositional = positionalList.get(i - 1);
                    //???????????????vTime?????? 300s ???,????????????????????????,?????????????????????????????????
                    if (positional.getVtime() - previousPositional.getVtime() > 300) {
                        needSetStandbyNum = 0;
                    }
                }
                //???????????????????????????????????????????????????
                if (speed != null && speedThreshold != null && Double.parseDouble(speed) > speedThreshold) {
                    workingPosition = WORK_STATE;
                }
                //??????????????????????????????0???, ???????????????????????????????????????????????????
                if (Objects.equals(checkData, 0.0)) {
                    workingPosition = HALT_STATE;
                    needSetStandbyNum = 0;
                }
                //???????????????????????????0????????????????????????5??? ???????????????????????? ?????????????????????????????????????????????
                if (Objects.equals(fluctuateValue, 0.0) && checkData > 5) {
                    Integer beforeIndex = i - 1 >= 0 ? i - 1 : null;
                    if (beforeIndex == null) {
                        workingPosition = WORK_STATE;
                        needSetStandbyNum = 0;
                    } else {
                        Positional previousPositional = positionalList.get(beforeIndex);
                        Double beforeCheckData = sensorNo == null ? null : isNeedDistinguishSensorNo
                            ? (sensorNo == 1 ? previousPositional.getCheckDataOne()
                            : previousPositional.getCheckDataTwo()) : positional.getCheckData();
                        Integer beforeWorkInspectionMethod = sensorNo == null ? null : isNeedDistinguishSensorNo
                            ? (sensorNo == 1 ? previousPositional.getWorkInspectionMethodOne()
                            : previousPositional.getWorkInspectionMethodTwo()) : positional.getWorkInspectionMethod();
                        //????????????????????????????????????????????????????????????????????????????????????????????????????????????,???????????????????????????????????????;
                        if (beforeWorkInspectionMethod == null || !Objects.equals(checkData, beforeCheckData)) {
                            workingPosition = WORK_STATE;
                            needSetStandbyNum = 0;
                        }
                    }
                }
                workHourInfoJsonObj.put("speed", speed);
            }
            workHourInfoJsonObj
                .put("time", new SimpleDateFormat(DATE_FORMAT).format(new Date(positional.getVtime() * 1000)));
            workHourInfoJsonObj.put("timeL", positional.getVtime());
            workHourInfoJsonObj.put("checkData", checkData);
            workHourInfoJsonObj.put("workingPosition", workingPosition);
            workHourInfoJsonObj.put("type", type);
            workHourChartDataArr.add(0, workHourInfoJsonObj);
        }
        return workHourChartDataArr;
    }

    /**
     * ??????????????????????????????????????????,?????????????????????redis;
     */
    private List<Positional> queryHistoryDataAndSaveRedis(String monitorId, String startTime, String endTime,
        Integer sensorFlag, RedisKey trackPlaybackBaseDataKey) throws Exception {
        boolean flogKey = RedisHelper.isContainsKey(HistoryRedisKeyEnum.SENSOR_MESSAGE.of(monitorId));
        if (sensorFlag != null) {
            flogKey = sensorFlag == 1;
        }
        RedisHelper.delete(trackPlaybackBaseDataKey);
        List<Positional> list = getHistoryData(monitorId, startTime, endTime, flogKey, null);
        RedisHelper.addToList(trackPlaybackBaseDataKey, list);
        RedisHelper.expireKey(trackPlaybackBaseDataKey, RedisHelper.THREE_HOUR_REDIS_EXPIRE);
        return list;
    }

    @Override
    public JsonResultBean getMonitorHistoryData(String vehicleId, String startTime, String endTime, Integer sensorFlag,
        Integer reissue) throws Exception {
        String username = SystemHelper.getCurrentUsername();
        RedisKey redisKey = HistoryRedisKeyEnum.TRACK_PLAYBACK_BASE_DATA.of(username, vehicleId);
        RedisHelper.delete(redisKey);
        // ????????????????????????
        String functionalType = TrackBackUtil.getfunctionalType(vehicleId);
        // ?????????????????????????????????????????????
        boolean flogKey = RedisHelper.isContainsKey(HistoryRedisKeyEnum.SENSOR_MESSAGE.of(vehicleId));
        // ???????????????????????????,?????????????????????????????????????????????
        if (sensorFlag != null) {
            flogKey = sensorFlag == 1;
        }
        List<Positional> positionalList = getHistoryData(vehicleId, startTime, endTime, flogKey, reissue);
        RedisHelper.addToList(redisKey, positionalList);
        RedisHelper.expireKey(redisKey, RedisHelper.THREE_HOUR_REDIS_EXPIRE);
        String historyDataJsonCompressStr = ZipUtil.compress(JSON.toJSONString(positionalList));
        JSONObject msg = new JSONObject();
        // ????????????
        msg.put("allData", historyDataJsonCompressStr);
        // ????????????????????????
        msg.put("type", functionalType);
        // ???????????????????????????
        msg.put("groups", realTime.getGroups(vehicleId));
        //???????????????????????????
        msg.put("nowFlogKey", flogKey);
        return new JsonResultBean(msg);
    }

    @Override
    public JsonResultBean getMonitorObdDate(String monitorId, String startTime, String endTime, Integer sensorFlag)
        throws Exception {
        List<OBDVehicleDataInfo> result = new ArrayList<>();
        JSONObject msg = new JSONObject();
        msg.put("isObdSet", "yes");
        msg.put("result", result);
        BindDTO bindInfo = VehicleUtil.getBindInfoByRedis(monitorId);
        // ???OBD??????????????????obd??????????????????????????????
        if (null == bindInfo
                || !ProtocolEnum.OBD_GB_2018.getDeviceType().equals(bindInfo.getDeviceType())
                && !ProtocolEnum.OBD_HZ_2018.getDeviceType().equals(bindInfo.getDeviceType())) {
            OBDVehicleTypeForm monitorObdSensorInfo = obdVehicleTypeDao.getObdSensorInfoByMonitorId(monitorId);
            if (monitorObdSensorInfo == null) {
                msg.put("isObdSet", "no");
                String msgResult = JSON.toJSONString(msg, SerializerFeature.DisableCircularReferenceDetect);
                msgResult = ZipUtil.compress(msgResult);
                return new JsonResultBean(msgResult);
            }
        }
        String username = SystemHelper.getCurrentUsername();
        RedisKey redisKey = HistoryRedisKeyEnum.TRACK_PLAYBACK_OBD_DATA.of(username, monitorId);
        RedisHelper.delete(redisKey);
        // ????????????
        List<Positional> positionalList = getCachePositionalInfoList(monitorId, startTime, endTime, sensorFlag);
        String groupName = bindInfo == null ? null : bindInfo.getOrgName();
        if (CollectionUtils.isNotEmpty(positionalList)) {
            for (Positional positional : positionalList) {
                OBDVehicleDataInfo info = new OBDVehicleDataInfo();
                info.setVtime(positional.getVtime());
                info.setPlateNumber(positional.getPlateNumber());
                info.setObdOriginalVehicleData(ObjectUtils.firstNonNull(
                        positional.getObdOriginalVehicleData(), positional.getObdObj()));
                info.setUploadtime(positional.getUploadTime());
                info.setGroupName(groupName);
                obdVehicleTypeService.installObdInfo(info);
                result.add(info);
            }
        }
        RedisHelper.addToList(redisKey, result);
        RedisHelper.expireKey(redisKey, RedisHelper.THREE_HOUR_REDIS_EXPIRE);
        String msgResult = JSON.toJSONString(msg, SerializerFeature.DisableCircularReferenceDetect);
        msgResult = ZipUtil.compress(msgResult);
        return new JsonResultBean(msgResult);
    }

    /**
     * ??????????????????
     */
    private List<Positional> getHistoryData(String monitorId, String startTime, String endTime, boolean flogKey,
        Integer reissue) throws Exception {
        Positional positional;
        Map<String, String> queryParam = Maps.newHashMap();
        queryParam.put("monitorId", monitorId);
        queryParam.put("startTime", DateUtil.formatDate(startTime, DateUtil.DATE_FORMAT_SHORT, DateUtil.DATE_FORMAT));
        queryParam.put("endTime", DateUtil.formatDate(endTime, DateUtil.DATE_FORMAT_SHORT, DateUtil.DATE_FORMAT));
        if (Objects.nonNull(reissue)) {
            queryParam.put("reissueFlag", String.valueOf(reissue));
        }
        String str = HttpClientUtil.send(PaasCloudUrlEnum.SENSOR_BASIC_POSITIONAL_HISTORY_URL, queryParam);
        List<Positional> list = PaasCloudUrlUtil.getResultListData(str, Positional.class);
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        List<Positional> sortList =
            list.stream().sorted(Comparator.comparingLong(Positional::getVtime)).collect(Collectors.toList());
        // ??????????????????
        Map<String, String> monitorIco = monitorIconService.getByMonitorId(Collections.singleton(monitorId));
        BindDTO bindInfo = VehicleUtil.getBindInfoByRedis(monitorId);
        boolean bindInfoIsNull = bindInfo == null;
        String plateColor =
            bindInfoIsNull ? "-" : PlateColor.getNameOrBlankByCode(String.valueOf(bindInfo.getPlateColor()));
        String simCard = bindInfoIsNull ? null : bindInfo.getSimCardNumber();
        String ico = bindInfoIsNull ? null : monitorIco.get(monitorId);
        // ??????????????????????????????
        CommonUtil.positionalInfoAbnormalFilter(sortList, filterFlag);
        // ????????????,???????????????????????????
        int listSize = sortList.size();
        positional = sortList.get(0);
        Integer totalTire;
        //????????????????????????????????????????????????????????????
        List<SensorSettingInfo> monitorBandSensorInfoBySensorType =
            sensorSettingsDao.getMonitorBandSensorInfoBySensorType(monitorId, 7);
        if (CollectionUtils.isEmpty(monitorBandSensorInfoBySensorType)) {
            totalTire = 0;
        } else {
            SensorSettingInfo sensorSettingInfo = monitorBandSensorInfoBySensorType.get(0);
            totalTire = sensorSettingInfo.getNumberOfTires();
        }
        // ??????????????????
        TrackBackUtil.initDrivingState(flogKey, positional);
        for (int i = 0; i < listSize; i++) {
            positional = sortList.get(i);
            positional.setPlateColor(plateColor);
            positional.setPlateNumber(positional.getMonitorName());
            positional.setSimCard(simCard);
            positional.setVtime(positional.getTime());
            positional.setIco(ico);
            positional.setAcc(TrackBackUtil.getAccAndStatus(positional.getStatus(), 0x1));
            positional.setLocationStatus(TrackBackUtil.getAccAndStatus(positional.getStatus(), 0x2));
            positional.setTotalTireNum(totalTire);
            positional.setLongtitude(positional.getLongitude());
            if (StringUtils.isBlank(positional.getLongitude())) {
                positional.setLongtitude("0.0");
            }
            if (StringUtils.isBlank(positional.getLatitude())) {
                positional.setLatitude("0.0");
            }
            // ??????????????????????????????????????????????????????
            if (StringUtils.isNotBlank(positional.getStationInfo())) {
                positional.setStationEnabled(true);
            }
            if (i > 0) {
                // ????????????
                Positional previousPositional = sortList.get(i - 1);
                // ?????????????????????????????????5?????? ???????????????????????????
                if (positional.getVtime() - previousPositional.getVtime() > 300) {
                    // ??????????????????
                    TrackBackUtil.initDrivingState(flogKey, positional);
                }
            }
            //??????????????????
            TrackBackUtil.calculateDrivingStatus(positional, sortList, flogKey, listSize, i);
        }
        return sortList;
    }

    private void getResultMap(Map<String, List<TimeZonePositional>> resultMap, TimeZoneQueryParam queryParam,
        List<byte[]> monitoryIdByteList) {
        // ??????????????????
        String areaListStr = queryParam.getAreaListStr();
        List<AreaInfo> areaInfoList = JSON.parseObject(areaListStr, new TypeReference<List<AreaInfo>>() {
        });
        if (CollectionUtils.isEmpty(areaInfoList)) {
            // ?????????????????????, ???????????????
            return;
        }
        // ????????????ID????????????????????????, ??????????????????????????????
        Long startTimeOne = queryParam.getStartTimeOne();
        Long endTimeOne = queryParam.getEndTimeOne();
        Long startTimeTwo = queryParam.getStartTimeTwo();
        Long endTimeTwo = queryParam.getEndTimeTwo();
        List<List<byte[]>> averageMonitorIdList = new ArrayList<>();
        int threadNum = TimeZoneQuery.averageMonitorId(monitoryIdByteList, averageMonitorIdList);
        // ????????????????????????:????????????????????????2, ????????????????????????4
        int doneSingle = areaInfoList.size() == 1 ? 2 : 4;
        CountDownLatch queryCountDownLatch = new CountDownLatch(doneSingle);

        multiThreadQueryArea(resultMap, areaInfoList, startTimeOne, endTimeOne, startTimeTwo, endTimeTwo,
            averageMonitorIdList, threadNum, queryCountDownLatch);
    }

    private void multiThreadQueryArea(Map<String, List<TimeZonePositional>> resultMap, List<AreaInfo> areaInfoList,
        Long startTimeOne, Long endTimeOne, Long startTimeTwo, Long endTimeTwo, List<List<byte[]>> averageMonitorIdList,
        int threadNum, CountDownLatch queryCountDownLatch) {
        try {
            for (AreaInfo areaInfo : areaInfoList) {
                addressAssemblyTimeThread(resultMap, startTimeOne, endTimeOne, averageMonitorIdList, threadNum,
                    queryCountDownLatch, areaInfo);
                addressAssemblyTimeThread(resultMap, startTimeTwo, endTimeTwo, averageMonitorIdList, threadNum,
                    queryCountDownLatch, areaInfo);
            }
            // ???????????????????????????, ????????????
            queryCountDownLatch.await();
        } catch (Exception e) {
            logger.error("??????????????????????????????", e);
        }
    }

    /**
     * ???????????????1+??????2????????????
     * @param resultMap            resultMap
     * @param startTime            startTime
     * @param endTime              endTime
     * @param averageMonitorIdList averageMonitorIdList
     * @param threadNum            threadNum
     * @param queryCountDownLatch  queryCountDownLatch
     * @param areaInfo             areaInfo
     */
    private void addressAssemblyTimeThread(Map<String, List<TimeZonePositional>> resultMap, Long startTime,
        Long endTime, List<List<byte[]>> averageMonitorIdList, int threadNum, CountDownLatch queryCountDownLatch,
        AreaInfo areaInfo) {
        if (Objects.nonNull(startTime) && Objects.nonNull(endTime)) {
            taskExecutor.execute(
                () -> getTimeDataByAddress(resultMap, averageMonitorIdList, areaInfo, startTime, endTime, threadNum,
                    queryCountDownLatch));
        } else {
            // ????????????
            queryCountDownLatch.countDown();
        }
    }

    /**
     * ??????????????????????????????
     * @param resultMap            resultMap
     * @param averageMonitorIdList ????????????????????????ID??????
     * @param areaInfo             ????????????
     * @param startTime            ????????????
     * @param endTime              ????????????
     * @param threadNum            ????????????
     * @param queryCountDownLatch  ????????????
     */
    private void getTimeDataByAddress(Map<String, List<TimeZonePositional>> resultMap,
        List<List<byte[]>> averageMonitorIdList, AreaInfo areaInfo, Long startTime, Long endTime, int threadNum,
        CountDownLatch queryCountDownLatch) {
        try {
            String startTimeFormat = LocalDateUtils.dateTimeFormat(new Date(startTime * LocalDateUtils.SECOND));
            String endTimeStrFormat = LocalDateUtils.dateTimeFormat(new Date(endTime * LocalDateUtils.SECOND));
            List<TimeZonePositional> areaOnePositionalList = new ArrayList<>();
            TimeZoneQuery timeZoneQuery =
                new TimeZoneQuery(startTime, endTime, averageMonitorIdList, threadNum, taskExecutor,
                    areaInfo);
            addTimeZoneList(startTimeFormat, endTimeStrFormat, areaOnePositionalList, areaInfo, timeZoneQuery);
            resultMap.computeIfAbsent(areaInfo.getAreaId(), x -> new ArrayList<>()).addAll(areaOnePositionalList);
        } finally {
            // ?????????????????????
            queryCountDownLatch.countDown();
        }
    }

    /**
     * ????????????????????????
     * @param startTimeFormat       startTimeFormat
     * @param endTimeStrFormat      endTimeStrFormat
     * @param areaOnePositionalList areaOnePositionalList
     * @param areaInfo              areaInfo
     * @param timeZoneQuery         timeZoneQuery
     */
    private void addTimeZoneList(String startTimeFormat, String endTimeStrFormat,
        List<TimeZonePositional> areaOnePositionalList, AreaInfo areaInfo, TimeZoneQuery timeZoneQuery) {
        List<TimeZonePositional> timeZonePositionalList = timeZoneQuery.queryPositionalList();
        if (CollectionUtils.isNotEmpty(timeZonePositionalList)) {
            timeZonePositionalList.forEach(timeZone -> {
                timeZone.setVehicleIdStr(UuidUtils.getUUIDStrFromBytes(timeZone.getVehicleId()));
                timeZone.setAreaName(areaInfo.getAreaId());
                timeZone.setStartTime(startTimeFormat);
                timeZone.setEndTime(endTimeStrFormat);
            });
        }
        areaOnePositionalList.addAll(timeZonePositionalList);
    }

    @Override
    public List<HistoryMileAndSpeed> getMileSpeedData(TrackPlayBackChartDataQuery query) throws Exception {
        List<HistoryMileAndSpeed> locationMileAndSpeedData = new ArrayList<>();
        // ???????????????????????????????????????,????????????????????????
        List<Positional> locationInfo = getMonitorLocationInfo(query);
        if (CollectionUtils.isEmpty(locationInfo)) {
            return locationMileAndSpeedData;
        }
        boolean flogKey = getMonitorBindSensorMessageStatus(query.getMonitorId(), query.getSensorFlag());
        locationMileAndSpeedData = locationInfo.stream().map((e) -> {
            double mileage;
            double speed;
            if (flogKey) { // ?????????????????????????????????
                mileage = e.getMileageTotal() == null ? 0.0 : e.getMileageTotal();
                speed = e.getMileageSpeed() == null ? 0.0 : e.getMileageSpeed();
            } else {
                mileage = e.getGpsMile() == null ? 0.0 : Double.parseDouble(e.getGpsMile());
                speed = e.getSpeed() == null ? 0.0 : Double.parseDouble(e.getSpeed());
            }
            HistoryMileAndSpeed data = new HistoryMileAndSpeed();
            data.setTime(e.getVtime());
            data.setMileage(mileage);
            data.setSpeed(speed);
            return data;
        }).collect(Collectors.toList());
        return locationMileAndSpeedData;
    }

    /**
     * ????????????????????????????????????????????????
     */
    private boolean getMonitorBindSensorMessageStatus(String monitorId, Integer sensorFlag) {
        boolean flogKey = RedisHelper.isContainsKey(HistoryRedisKeyEnum.SENSOR_MESSAGE.of(monitorId));
        if (sensorFlag != null) {
            flogKey = sensorFlag == 1;
        }
        return flogKey;
    }

    @Override
    public List<HistoryStopAndTravel> getTravelAndStopData(TrackPlayBackChartDataQuery query) throws Exception {
        List<HistoryStopAndTravel> locationTravelAndStopData = new ArrayList<>();
        List<Positional> locationInfo = getMonitorLocationInfo(query);
        // ???????????????????????????????????????
        if (CollectionUtils.isEmpty(locationInfo)) {
            return locationTravelAndStopData;
        }
        boolean flogKey = getMonitorBindSensorMessageStatus(query.getMonitorId(), query.getSensorFlag());
        locationTravelAndStopData = locationInfo.stream().map((e) -> {
            double mileage;
            if (flogKey) {
                mileage = e.getMileageTotal() == null ? 0.0 : e.getMileageTotal();
            } else {
                mileage = e.getGpsMile() == null ? 0.0 : Double.parseDouble(e.getGpsMile());
            }
            HistoryStopAndTravel data = new HistoryStopAndTravel();
            data.setTime(e.getVtime());
            data.setMileage(mileage);
            data.setStatus(e.getDrivingState());
            return data;
        }).collect(Collectors.toList());
        return locationTravelAndStopData;
    }

    @Override
    public JsonResultBean getOilMassData(TrackPlayBackChartDataQuery query) throws Exception {
        List<HistoryOilMass> locationOilMass = new ArrayList<>();
        JSONObject msg = new JSONObject();
        msg.put("oilMass", locationOilMass);
        // ???????????????????????????????????????
        int monitorOilBoxNumber = oilCalibrationDao.checkIsBondOilBox(query.getMonitorId());
        if (monitorOilBoxNumber == 0) {
            return new JsonResultBean(JsonResultBean.FAULT, "???????????????????????????????????????");
        }
        List<Positional> locationInfo = getMonitorLocationInfo(query);
        if (CollectionUtils.isEmpty(locationInfo)) {
            return new JsonResultBean(msg);
        }
        boolean tankOne = false;
        boolean tankTwo = false;
        if (monitorOilBoxNumber == 1) {
            tankOne = true;
        } else if (monitorOilBoxNumber == 2) {
            tankOne = true;
            tankTwo = true;
        }
        HistoryOilMass oilMass;
        for (Positional p : locationInfo) {
            oilMass = new HistoryOilMass();
            oilMass.setTime(p.getVtime());
            JSONArray oilTank = new JSONArray();
            JSONArray fuelAmount = new JSONArray();
            JSONArray fuelSpill = new JSONArray();
            if (tankOne) {
                Double oilTankOne = p.getOilTankOne() == null || "0".equals(p.getOilTankOne()) ? null :
                    Double.valueOf(p.getOilTankOne());
                Double fuelAmountOne = p.getFuelAmountOne() == null || "0".equals(p.getFuelAmountOne()) ? null :
                    Double.valueOf(p.getFuelAmountOne());
                Double fuelSpillOne = p.getFuelSpillOne() == null || "0".equals(p.getFuelSpillOne()) ? null :
                    Double.valueOf(p.getFuelSpillOne());
                oilTank.add(oilTankOne);
                fuelAmount.add(fuelAmountOne);
                fuelSpill.add(fuelSpillOne);
            }
            if (tankTwo) {
                Double oilTankTwo = p.getOilTankTwo() == null || "0".equals(p.getOilTankTwo()) ? null :
                    Double.valueOf(p.getOilTankTwo());
                Double fuelAmountTwo = p.getFuelAmountTwo() == null || "0".equals(p.getFuelAmountTwo()) ? null :
                    Double.valueOf(p.getFuelAmountTwo());
                Double fuelSpillTwo = p.getFuelSpillTwo() == null || "0".equals(p.getFuelSpillTwo()) ? null :
                    Double.valueOf(p.getFuelSpillTwo());
                oilTank.add(oilTankTwo);
                fuelAmount.add(fuelAmountTwo);
                fuelSpill.add(fuelSpillTwo);
            }
            oilMass.setOilTank(oilTank);
            oilMass.setFuelAmount(fuelAmount);
            oilMass.setFuelSpill(fuelSpill);
            locationOilMass.add(oilMass);
        }
        return new JsonResultBean(msg);
    }

    /**
     * ??????????????????????????????
     */
    private List<Positional> getMonitorLocationInfo(TrackPlayBackChartDataQuery query) throws Exception {
        List<Positional> locationInfo = new ArrayList<>();
        if (query == null) {
            return locationInfo;
        }
        String monitorId = query.getMonitorId();
        String startTime = query.getStartTime();
        String endTime = query.getEndTime();
        Integer sensorFlag = query.getSensorFlag();
        if (StringUtils.isBlank(monitorId) || StringUtils.isBlank(startTime) || StringUtils.isBlank(endTime)) {
            return locationInfo;
        }
        locationInfo = getCachePositionalInfoList(monitorId, startTime, endTime, sensorFlag);
        return locationInfo;
    }

    /**
     * ????????????????????????
     */
    @Override
    public JSONObject getSensorPollingListByMonitorId(String monitorId) {
        JSONObject msg = new JSONObject();
        if (StringUtils.isBlank(monitorId)) {
            return msg;
        }
        List<String> pollingList = sensorPollingDao.getSensorPollingListByMonitorId(monitorId);
        if (CollectionUtils.isEmpty(pollingList)) {
            return msg;
        }
        msg.put("sensorPollingList", pollingList);
        return msg;
    }

    @Override
    public JsonResultBean getPositiveInversionDate(String monitorId, String startTime, String endTime,
        Integer sensorFlag) throws Exception {
        List<WinchInfo> winchInfos = new ArrayList<>();
        // ????????????????????????????????????
        List<SensorSettingInfo> monitorBandSensorInfoBySensorType =
            sensorSettingsDao.getMonitorBandSensorInfoBySensorType(monitorId, 3);
        if (CollectionUtils.isEmpty(monitorBandSensorInfoBySensorType)) {
            return new JsonResultBean(JsonResultBean.FAULT, "??????????????????????????????????????????");
        }
        // ????????????
        List<Positional> positionalList = getCachePositionalInfoList(monitorId, startTime, endTime, sensorFlag);
        if (CollectionUtils.isNotEmpty(positionalList)) {
            for (Positional positional : positionalList) {
                WinchInfo info = new WinchInfo();
                info.setTime(String.valueOf(positional.getVtime()));
                info.setRotationTime(
                    positional.getWinchRotateTime() == null ? "" : String.valueOf(positional.getWinchRotateTime()));
                info.setRotationStatus(
                    positional.getWinchStatus() == null ? "" : String.valueOf(positional.getWinchStatus()));
                info.setOrientation(positional.getWinchOrientation());
                winchInfos.add(info);
            }

        }
        return new JsonResultBean(winchInfos);
    }

    @Override
    public void exportTrackPlay(HttpServletResponse response, PositionalQuery query) throws Exception {
        Integer tab = query.getTab();
        String vehicleId = query.getVehicleId();
        String username = SystemHelper.getCurrentUsername();
        ExportExcelUtil.setResponseHead(response, query.getTitle());
        // ??????????????????
        List<String> customColumnList = customColumnService.findCustomColumnTitleList(query.getMark());

        if (CollectionUtils.isEmpty(customColumnList)) {
            logger.error("???????????????????????????_???{}???", query.getTitle());
            return;
        }
        query.setCustomColumnList(customColumnList);
        RedisKey baseDataKey;
        switch (tab) {
            case 0:
                // ????????????
                baseDataKey = HistoryRedisKeyEnum.TRACK_PLAYBACK_BASE_DATA.of(username, vehicleId);
                exportPositionalFormList(baseDataKey, query, response);
                break;
            case 1:
                // OBD??????
                baseDataKey = HistoryRedisKeyEnum.TRACK_PLAYBACK_OBD_DATA.of(username, vehicleId);
                exportOBDList(baseDataKey, query, response);
                break;
            case 2:
                // ????????????
                baseDataKey =
                    HistoryRedisKeyEnum.TRACK_PLAYBACK_ALARM_SUFFIX_KEY.of(userService.getCurrentUserUuid(), vehicleId);
                exportAlarmList(baseDataKey, query, response);
                break;
            case 3:
                //????????????
                baseDataKey = HistoryRedisKeyEnum.TRACK_PLAYBACK_BASE_DATA.of(username, vehicleId);
                exportStopList(baseDataKey, query, response);
                break;
            case 4:
                //???????????????
                baseDataKey = HistoryRedisKeyEnum.TRACK_PLAYBACK_BASE_DATA.of(username, vehicleId);
                exportRunList(baseDataKey, query, response);
                break;
            default:
                break;
        }
    }

    private void exportRunList(RedisKey trackPlaybackBaseDataKey, PositionalQuery query, HttpServletResponse response)
        throws Exception {
        if (RedisHelper.isContainsKey(trackPlaybackBaseDataKey)) {
            List<Positional> positionalFormList = RedisHelper.getList(trackPlaybackBaseDataKey, Positional.class);

            String assignmentName = realTime.getGroups(query.getVehicleId());
            Integer exportLocation = query.getIsExportLocation();
            // ??????????????????
            List<RunPositional> exportList = new ArrayList<>();
            RunPositional runPositional = null;
            Positional positional;
            Positional nextPositional;
            int positionalFormListSize = positionalFormList.size();
            boolean isFirst = true;
            for (int i = 0; i < positionalFormListSize; i++) {
                // ????????????????????????
                positional = positionalFormList.get(i);
                if (!TrackBackUtil.RUNNING_STATE.equals(positional.getDrivingState())) {
                    continue;
                }
                //????????????
                if (isFirst) {
                    runPositional = runStartExport(assignmentName, exportLocation, positional);
                    isFirst = false;
                }
                if (i + 1 < positionalFormListSize) {
                    nextPositional = positionalFormList.get(i + 1);
                    if (STOP_STATE.equals(nextPositional.getDrivingState())) {
                        if (nextPositional.getVtime() - positional.getVtime() > 5 * 60) {
                            addRunEndToExportList(runPositional, positional, exportList, exportLocation);
                            calculateUseOil2(runPositional, positional);
                        } else {
                            addRunEndToExportList(runPositional, nextPositional, exportList, exportLocation);
                            calculateUserOil4(runPositional, nextPositional);
                        }
                        isFirst = true;
                        continue;
                    }
                    //??????????????????5??????,??????5??????,????????????????????????????????????
                    if (TrackBackUtil.RUNNING_STATE.equals(nextPositional.getDrivingState())
                        && nextPositional.getVtime() - positional.getVtime() > 5 * 60) {
                        calculateUseOil2(runPositional, positional);
                        addRunEndToExportList(runPositional, positional, exportList, exportLocation);
                        isFirst = true;
                        continue;
                    }
                    calculateUseOil3(runPositional, nextPositional);
                }
                if (i + 1 == positionalFormListSize && TrackBackUtil.RUNNING_STATE
                    .equals(positional.getDrivingState())) {
                    addRunEndToExportList(runPositional, positional, exportList, exportLocation);
                    if (positionalFormListSize == 1) {
                        runPositional.setUseOil("0");
                    } else {
                        calculateUseOil2(runPositional, positional);
                    }
                }
            }
            ExportExcelUtil.exportCustomData(
                new ExportExcelParam("", 1, exportList, RunPositional.class, null, response.getOutputStream(),
                    query.getCustomColumnList()));
        }
    }

    //???????????????????????????????????????????????????????????????1?????????2?????????1?????????2?????????1?????????2
    private void calculateUserOil4(RunPositional runPositional, Positional nextPositional) {
        //????????????
        Double oilTankOne = string2Double(nextPositional.getOilTankOne());
        Double oilTankTwo = string2Double(nextPositional.getOilTankTwo());
        //?????????
        Double fuelAmountOne = string2Double(nextPositional.getFuelAmountOne());
        Double fuelAmountTwo = string2Double(nextPositional.getFuelAmountTwo());
        //???????????????????????????????????????
        Double fuelSpillOne = string2Double(nextPositional.getFuelSpillOne());
        Double fuelSpillTwo = string2Double(nextPositional.getFuelSpillTwo());
        runPositional.setUseOilTemp(
            runPositional.getUseOilTemp() - oilTankOne - oilTankTwo + fuelAmountOne + fuelAmountTwo - fuelSpillOne
                - fuelSpillTwo);
        runPositional.setUseOil(
            new BigDecimal("" + (runPositional.getUseOilTemp())).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
    }

    // ?????????????????????????????????????????????????????????1?????????2?????????calculateUseOil3???????????????????????????????????????????????????
    private void calculateUseOil2(RunPositional runPositional, Positional positional) {
        Double oilTankOne = string2Double(positional.getOilTankOne());
        Double oilTankTwo = string2Double(positional.getOilTankTwo());
        runPositional.setUseOilTemp(runPositional.getUseOilTemp() - oilTankOne - oilTankTwo);
        runPositional.setUseOil(
            new BigDecimal("" + (runPositional.getUseOilTemp())).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
    }

    //???????????????????????????????????????????????????
    private void calculateUseOil3(RunPositional runPositional, Positional nextPositional) {
        //?????????
        Double fuelAmountOne = string2Double(nextPositional.getFuelAmountOne());
        Double fuelAmountTwo = string2Double(nextPositional.getFuelAmountTwo());
        //??????????????????????????????
        Double fuelSpillOne = string2Double(nextPositional.getFuelSpillOne());
        Double fuelSpillTwo = string2Double(nextPositional.getFuelSpillTwo());
        runPositional
            .setUseOilTemp(runPositional.getUseOilTemp() + fuelAmountOne + fuelAmountTwo - fuelSpillOne - fuelSpillTwo);
    }

    private void addRunEndToExportList(RunPositional runPositional, Positional positional,
        List<RunPositional> exportList, Integer exportLocation) {
        long gpsTime = positional.getVtime();
        runPositional.setRunEndTime(LocalDateUtils.dateTimeFormat(new Date(gpsTime * 1000)));
        runPositional.setRunTime(buildStopTime(runPositional.getGpsTime(), gpsTime));
        calculateConsumeOil(runPositional, positional);
        Double mileageTotal = positional.getMileageTotal();
        if (mileageTotal == null) {
            //gps????????????????????????????????????
            mileageTotal = Double.parseDouble(positional.getGpsMile());
        }
        runPositional.setRunMile(
            new BigDecimal(mileageTotal - runPositional.getRunMileTemp()).setScale(1, BigDecimal.ROUND_HALF_UP)
                .toString());
        if (exportLocation == PositionalQuery.EXPORT_LOCATION) {
            String address = positionalService.getAddress(positional.getLongtitude(), positional.getLatitude());
            runPositional.setRunEndLocation(address);
        }
        exportList.add(runPositional);
    }

    private void calculateConsumeOil(RunPositional runPositional, Positional positional) {
        String consumeOil = runPositional.getConsumeOil();
        String totalOilwearOne = positional.getTotalOilwearOne();
        String totalOilwearTwo = positional.getTotalOilwearTwo();
        Double totalOilwear = null;
        if (StringUtils.isNotEmpty(totalOilwearOne)) {
            totalOilwear = Double.parseDouble(totalOilwearOne);
        }
        if (StringUtils.isNotEmpty(totalOilwearTwo)) {
            totalOilwear += Double.parseDouble(totalOilwearTwo);
        }
        if (totalOilwear != null) {
            runPositional.setConsumeOil(new BigDecimal("" + (totalOilwear - Double.parseDouble(consumeOil)))
                .setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        }
    }

    private void exportStopList(RedisKey trackPlaybackBaseDataKey, PositionalQuery query, HttpServletResponse response)
        throws Exception {
        if (RedisHelper.isContainsKey(trackPlaybackBaseDataKey)) {
            List<Positional> positionalFormList = RedisHelper.getList(trackPlaybackBaseDataKey, Positional.class);

            String assignmentName = realTime.getGroups(query.getVehicleId());
            Integer exportLocation = query.getIsExportLocation();
            // ??????????????????
            List<StopPositional> exportList = new ArrayList<>();
            StopPositional stopPositional = null;
            Positional positional;
            Positional nextPositional;
            int positionalFormListSize = positionalFormList.size();
            boolean isFirst = true;
            for (int i = 0; i < positionalFormListSize; i++) {
                // ????????????????????????
                positional = positionalFormList.get(i);
                if (!TrackBackUtil.STOP_STATE.equals(positional.getDrivingState())) {
                    continue;
                }
                //????????????
                if (isFirst) {
                    stopPositional = stopStartExport(assignmentName, exportLocation, positional);
                    isFirst = false;
                }
                if (i + 1 < positionalFormListSize) {
                    nextPositional = positionalFormList.get(i + 1);
                    if (TrackBackUtil.RUNNING_STATE.equals(nextPositional.getDrivingState())) {
                        if (nextPositional.getVtime() - positional.getVtime() > 5 * 60) {
                            addStopEndToExportList(stopPositional, positional, exportList, exportLocation);
                        } else {
                            addStopEndToExportList(stopPositional, nextPositional, exportList, exportLocation);
                        }
                        isFirst = true;
                        continue;
                    }
                    //??????????????????5??????
                    if (TrackBackUtil.STOP_STATE.equals(nextPositional.getDrivingState())
                        && nextPositional.getVtime() - positional.getVtime() > 5 * 60) {
                        addStopEndToExportList(stopPositional, positional, exportList, exportLocation);
                        isFirst = true;
                        continue;
                    }
                }

                if (i + 1 == positionalFormListSize && TrackBackUtil.STOP_STATE.equals(positional.getDrivingState())) {
                    addStopEndToExportList(stopPositional, positional, exportList, exportLocation);
                }
            }
            ExportExcelUtil.exportCustomData(
                new ExportExcelParam("", 1, exportList, StopPositional.class, null, response.getOutputStream(),
                    query.getCustomColumnList()));
        }
    }

    private void addStopEndToExportList(StopPositional stopPositional, Positional positional,
        List<StopPositional> exportList, Integer exportLocation) {
        long gpsTime = positional.getVtime();
        stopPositional.setStopEndTime(LocalDateUtils.dateTimeFormat(new Date(gpsTime * 1000)));
        stopPositional.setStopTime(buildStopTime(stopPositional.getGpsTime(), gpsTime));
        if (exportLocation == PositionalQuery.EXPORT_LOCATION) {
            String address = positionalService.getAddress(positional.getLongtitude(), positional.getLatitude());
            stopPositional.setStopEndLocation(address);
        }
        exportList.add(stopPositional);
    }

    private String buildStopTime(long gpsT, long gpsTime) {
        StringBuilder stopTime = new StringBuilder();
        long h = (gpsTime - gpsT) / 3600;
        long m = (gpsTime - gpsT) % 3600 / 60;
        long s = (gpsTime - gpsT) % 60;
        if (h != 0) {
            stopTime.append(h).append("??????");
        }
        if (m != 0 || (h != 0)) {
            stopTime.append(m).append("???");
        }
        stopTime.append(s).append("???");
        return stopTime.toString();

    }

    private StopPositional stopStartExport(String assignmentName, Integer exportLocation, Positional positional) {
        StopPositional stopPositional = new StopPositional();
        stopPositional.setMonitorName(positional.getPlateNumber());
        stopPositional.setAssignmentName(assignmentName);
        long gpsTime = positional.getVtime();
        stopPositional.setStopStartTime(LocalDateUtils.dateTimeFormat(new Date(gpsTime * 1000)));
        stopPositional.setGpsTime(gpsTime);
        stopPositional.setDeviceNumber(positional.getDeviceNumber());
        stopPositional.setSimcardNumber(positional.getSimCard());
        stopPositional.setAcc(positional.getAcc());
        stopPositional.setLocationType(positional.getLocationType());
        stopPositional.setSatellitesNumber(positional.getSatelliteNumber());
        // ??????????????????
        if (exportLocation == PositionalQuery.EXPORT_LOCATION) {
            String longtitude = positional.getLongtitude();
            String latitude = positional.getLatitude();
            String address = positionalService.getAddress(longtitude, latitude);
            stopPositional.setStopStartLocation(address);
        }
        return stopPositional;
    }

    private RunPositional runStartExport(String assignmentName, Integer exportLocation, Positional positional) {
        RunPositional runPositional = new RunPositional();
        runPositional.setMonitorName(positional.getPlateNumber());
        runPositional.setAssignmentName(assignmentName);
        long gpsTime = positional.getVtime();
        runPositional.setRunStartTime(LocalDateUtils.dateTimeFormat(new Date(gpsTime * 1000)));
        runPositional.setGpsTime(gpsTime);
        runPositional.setDeviceNumber(positional.getDeviceNumber());
        runPositional.setSimcardNumber(positional.getSimCard());
        runPositional.setAcc(positional.getAcc());
        runPositional.setLocationType(positional.getLocationType());
        runPositional.setSatellitesNumber(positional.getSatelliteNumber());
        Double mileageTotal = positional.getMileageTotal();
        if (mileageTotal == null) {
            //gps????????????????????????????????????
            mileageTotal = Double.parseDouble(positional.getGpsMile());
        }
        runPositional.setRunMileTemp(mileageTotal);
        //????????????
        calculateConsumeOil(runPositional, positional);
        // ??????????????? ???????????????????????????????????? + ???????????????????????? ??? ????????????????????? ??? ?????????????????????????????????????????????????????????????????????
        calculateUseOil1(positional, runPositional);
        // ??????????????????
        if (exportLocation == PositionalQuery.EXPORT_LOCATION) {
            String longtitude = positional.getLongtitude();
            String latitude = positional.getLatitude();
            String address = positionalService.getAddress(longtitude, latitude);
            runPositional.setRunStartLocation(address);
        }
        return runPositional;
    }

    private void calculateUseOil1(Positional positional, RunPositional runPositional) {
        //??????
        Double oilTankOne = string2Double(positional.getOilTankOne());
        Double oilTankTwo = string2Double(positional.getOilTankTwo());
        //?????????
        Double fuelAmountOne = string2Double(positional.getFuelAmountOne());
        Double fuelAmountTwo = string2Double(positional.getFuelAmountTwo());

        //???????????????????????????????????????
        Double fuelSpillOne = string2Double(positional.getFuelSpillOne());
        Double fuelSpillTwo = string2Double(positional.getFuelSpillTwo());
        //?????????????????????
        runPositional
            .setUseOilTemp(oilTankTwo + oilTankOne + fuelAmountOne + fuelAmountTwo - fuelSpillOne - fuelSpillTwo);
        runPositional.setUseOil(
            new BigDecimal("" + (runPositional.getUseOilTemp())).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
    }

    private Double string2Double(String str) {

        if (StringUtils.isNotEmpty(str)) {
            return Double.valueOf(str);
        }
        return 0D;
    }

    /**
     * ??????????????????
     * @param baseDataKey redisKey
     * @param query       query
     * @param response    response
     */
    private void exportAlarmList(RedisKey baseDataKey, PositionalQuery query, HttpServletResponse response)
        throws Exception {
        if (RedisHelper.isContainsKey(baseDataKey)) {
            String obdStr = RedisHelper.getString(baseDataKey);
            if (StringUtils.isEmpty(obdStr)) {
                logger.info("??????????????????_???{}???", query.getTitle());
                return;
            }
            String obdUncompress =
                ZipUtil.uncompress(obdStr.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8.toString());
            List<AlarmData> alarmDataList = JSON.parseArray(obdUncompress, AlarmData.class);

            if (query.getIsExportLocation() == PositionalQuery.EXPORT_LOCATION) {
                alarmDataList.forEach(alarmData -> {
                    // ????????????
                    alarmData.setStartLocation(getLocation(alarmData.getAlarmStartLocation()));
                    // ????????????
                    alarmData.setEndLocation(getLocation(alarmData.getAlarmEndLocation()));
                });
            }
            ExportExcelUtil.exportCustomData(
                new ExportExcelParam("", 1, alarmDataList, AlarmData.class, null, response.getOutputStream(),
                    query.getCustomColumnList()));
        }
    }

    /**
     * ????????????
     * @param startLocation 106.495893,29.532302
     * @return ??????
     */
    private String getLocation(String startLocation) {
        if (StringUtils.isEmpty(startLocation)) {
            return "";
        }
        String[] locations = startLocation.split(",");
        String longitude = locations[0];
        String latitude = locations[1];
        return positionalService.getAddress(longitude, latitude);
    }

    /**
     * ??????OBD??????
     * @param baseDataKey redisKey
     * @param query       query
     * @param response    response
     */
    private void exportOBDList(RedisKey baseDataKey, PositionalQuery query, HttpServletResponse response)
        throws Exception {
        if (RedisHelper.isContainsKey(baseDataKey)) {
            List<OBDVehicleDataInfo> obdVehicleDataInfos = RedisHelper.getList(baseDataKey, OBDVehicleDataInfo.class);

            // ???????????????????????????
            obdVehicleDataInfos.forEach(obdVehicleDataInfo -> {
                Long vtime = obdVehicleDataInfo.getVtime();
                if (Objects.nonNull(vtime)) {
                    obdVehicleDataInfo.setGpsTime(LocalDateUtils.dateTimeFormat(new Date(vtime * 1000)));
                }
                String uploadTime = obdVehicleDataInfo.getUploadtime();
                if (StringUtils.isNotEmpty(uploadTime)) {
                    obdVehicleDataInfo
                        .setUploadtime(LocalDateUtils.dateTimeFormat(new Date(Long.parseLong(uploadTime) * 1000)));
                }

            });

            ExportExcelUtil.exportCustomData(
                new ExportExcelParam("", 1, obdVehicleDataInfos, OBDVehicleDataInfo.class, null,
                    response.getOutputStream(), query.getCustomColumnList()));
        }
    }

    /**
     * @param trackPlaybackBaseDataKey redisKey
     * @param query                    query
     */
    private void exportPositionalFormList(RedisKey trackPlaybackBaseDataKey, PositionalQuery query,
        HttpServletResponse response) throws Exception {
        if (RedisHelper.isContainsKey(trackPlaybackBaseDataKey)) {
            List<Positional> positionalFormList = RedisHelper.getList(trackPlaybackBaseDataKey, Positional.class);
            // ??????????????????????????????????????????(??????????????????????????????????????????????????????)
            if (!Objects.equals(query.getIsStationEnabled(), EXPORT_STATION)) {
                positionalFormList = positionalFormList.stream()
                        .filter(o -> Objects.equals(o.getStationEnabled(), Boolean.FALSE))
                        .collect(Collectors.toList());
            }
            String assignmentName = realTime.getGroups(query.getVehicleId());
            Integer flag = query.getFlag();
            Integer exportLocation = query.getIsExportLocation();
            // ??????????????????
            List<PositionalForm> exportList = new ArrayList<>();

            Positional positional;
            Positional nextPositional;
            int positionalFormListSize = positionalFormList.size();
            int nextIndex;
            PositionalState positionalState = new PositionalState();
            positionalState.setFlag(flag);
            for (int i = 0; i < positionalFormListSize; i++) {
                // ????????????????????????
                positional = positionalFormList.get(i);
                // ?????????????????????
                nextIndex = (i + 1);
                nextPositional = nextIndex < positionalFormListSize ? positionalFormList.get(nextIndex) : null;
                // ????????????????????????
                positionalState.setDrivingState(positional.getDrivingState());
                // ???????????????????????????
                String nextDrivingState = Objects.nonNull(nextPositional) ? nextPositional.getDrivingState() : "";
                positionalState.setNextDrivingState(nextDrivingState);
                addPositionalToExportList(assignmentName, exportLocation, exportList, positional, positionalState);
            }

            ExportExcelUtil.exportCustomData(
                new ExportExcelParam("", 1, exportList, PositionalForm.class, null, response.getOutputStream(),
                    query.getCustomColumnList()));
        }
    }

    private void addPositionalToExportList(String assignmentName, Integer exportLocation,
        List<PositionalForm> exportList, Positional positional, PositionalState positionalState) {
        PositionalForm positionalForm;
        if (isTravelBoolean(positionalState)) {
            long gpsTime = positional.getVtime() * 1000;
            positionalForm = new PositionalForm();
            positionalForm.setPlateNumber(positional.getPlateNumber());
            positionalForm.setTimeStr(LocalDateUtils.dateTimeFormat(new Date(gpsTime)));
            positionalForm.setIntervalTimeStr(getIntervalTimeStr(positionalState.getPreTime(), gpsTime));
            positionalForm.setAssignmentName(assignmentName);
            positionalForm.setDeviceNumber(positional.getDeviceNumber());
            positionalForm.setSimCard(positional.getSimCard());
            positionalForm.setDrivingState(positionalState.getDrivingState());
            positionalForm.setAcc(positional.getAcc());
            positionalForm.setLocationStatus(positional.getLocationStatus());
            positionalForm.setSpeed(positional.getSpeed());
            positionalForm.setRecorderSpeed(positional.getRecorderSpeed());
            Integer angle = null;
            if (positional.getAngle() != null) {
                angle = new BigDecimal(positional.getAngle()).intValue();
            }
            positionalForm.setAngle(DeviceMessageHandler.getDirectionStr(angle));
            positionalForm.setGpsMile(positional.getGpsMile());
            positionalForm.setLocationType(positional.getLocationType());
            positionalForm.setSatelliteNumber(positional.getSatelliteNumber());
            positionalForm.setReissue(
                Optional.ofNullable(positional.getReissue()).map(o -> o.equals(1) ? "??????" : "?????????").orElse(null));
            String longitude = positional.getLongtitude();
            String latitude = positional.getLatitude();
            positionalForm.setLongtitude(longitude);
            positionalForm.setLatitude(latitude);
            // ??????????????????
            if (exportLocation == PositionalQuery.EXPORT_LOCATION) {
                // TODO ??????????????????, ????????????1000???, ????????????
                String address = positionalService.getAddress(longitude, latitude);
                positionalForm.setLocation(address);
            }
            exportList.add(positionalForm);
            positionalState.setPreTime(gpsTime);
        }
    }

    static class PositionalState {
        private Integer flag;

        private String drivingState;

        private String nextDrivingState;

        /**
         * ?????????????????????
         */
        private Boolean firstStopFlag = false;

        /**
         * ??????????????????????????????
         */
        private Long preTime = 0L;

        private Long getPreTime() {
            return preTime;
        }

        private void setPreTime(Long preTime) {
            this.preTime = preTime;
        }

        private Integer getFlag() {
            return flag;
        }

        private void setFlag(Integer flag) {
            this.flag = flag;
        }

        private String getDrivingState() {
            return drivingState;
        }

        private void setDrivingState(String drivingState) {
            this.drivingState = drivingState;
        }

        private String getNextDrivingState() {
            return nextDrivingState;
        }

        private void setNextDrivingState(String nextDrivingState) {
            this.nextDrivingState = nextDrivingState;
        }

        public Boolean getFirstStopFlag() {
            return firstStopFlag;
        }

        public void setFirstStopFlag(Boolean firstStopFlag) {
            this.firstStopFlag = firstStopFlag;
        }
    }

    private String getIntervalTimeStr(Long preTime, long gpsTime) {
        return preTime == 0 ? "-" : LocalDateUtils.formatDuring(gpsTime - preTime);
    }

    /**
     * ??????????????????
     * @param state state
     * @return boolean
     */
    private boolean isTravelBoolean(PositionalState state) {
        Integer flag = state.getFlag();
        if (flag == 0) {
            return true;
        }

        // ????????????
        if (flag == 1 && PositionalForm.DRIVING_STATE.equals(state.getDrivingState())) {
            return true;
        }

        // ?????????????????????????????????
        if (StringUtils.isEmpty(state.getNextDrivingState())) {
            return true;
        }

        // ????????????????????????????????????????????????, ????????????????????????
        boolean nextFlag = state.getDrivingState().equals(state.getNextDrivingState());
        if (!state.getFirstStopFlag()) {
            if (nextFlag) {
                // ?????????????????????????????????????????????????????????
                state.setFirstStopFlag(true);
            }
            return true;
        }

        // ?????????????????????????????????????????????, ??????????????????????????????????????????????????????????????????????????????, ???????????????, ?????????????????????????????????
        if (!nextFlag) {
            state.setFirstStopFlag(false);
            return true;
        }

        return false;
    }

    /**
     * ??????????????????I/O??????(90 91 92)
     */
    @Override
    public JsonResultBean getSwitchData(TrackPlayBackChartDataQuery query) throws Exception {
        JSONObject msg = new JSONObject();
        if (query == null) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        String monitorId = query.getMonitorId();
        if (StringUtils.isBlank(monitorId)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        List<SwitchInfo> signal = ioVehicleConfigDao.getBindIoInfoByVehicleId(monitorId);
        if (CollectionUtils.isEmpty(signal)) { // ???????????????????????????I/O,?????????????????????
            return new JsonResultBean(JsonResultBean.FAULT, "????????????????????????I/O??????");
        }
        // ??????I/O??????
        Map<String, Map<Integer, String>> switchName = switchNameSet(signal, msg, monitorId);
        // ??????????????????id?????????????????????I/O??????
        List<Positional> locationInfo = getMonitorLocationInfo(query);
        if (CollectionUtils.isEmpty(locationInfo)) {
            return new JsonResultBean(msg);
        }
        List<SwitchSignalInfo> resultData = processPositionalInfo(locationInfo, switchName);
        msg.put("data", resultData);
        return new JsonResultBean(msg);
    }

    /**
     * ????????????????????????????????????
     */
    private Map<String, Map<Integer, String>> switchNameSet(List<SwitchInfo> signal, JSONObject msg, String monitorId) {
        Map<String, Map<Integer, String>> result = new HashMap<>();
        if (CollectionUtils.isEmpty(signal)) {
            return result;
        }
        Map<Integer, String> io90 = new HashMap<>();
        Map<Integer, String> io91 = new HashMap<>();
        Map<Integer, String> io92 = new HashMap<>();
        List<String> switchName = new ArrayList<>(); // ????????????
        JSONArray ioStatus = new JSONArray();
        List<String> alarmStatusList = new ArrayList<>(); // I/O ????????????
        Map<Integer, String> monitorIoAlarmSet = getMonitorIoAlarmSetting(monitorId);
        if (MapUtils.isEmpty(monitorIoAlarmSet)) {
            monitorIoAlarmSet = new HashMap<>();
        }
        for (SwitchInfo switchInfo : signal) {
            JSONObject status = new JSONObject();
            // ???????????????
            status.put("0",
                "1".equals(switchInfo.getHighSignalType()) ? switchInfo.getStateOne() : switchInfo.getStateTwo());
            // ???????????????
            status.put("1",
                "2".equals(switchInfo.getLowSignalType()) ? switchInfo.getStateTwo() : switchInfo.getStateOne());
            ioStatus.add(status);
            Integer ioSite = switchInfo.getIoSite();
            Integer ioType = switchInfo.getIoType();
            String highSignalType = switchInfo.getHighSignalType();
            if (ioType == 1) { // ??????IO
                io90.put(ioSite, highSignalType);
            } else if (switchInfo.getIoType() == 2) { //IO??????1
                io91.put(ioSite, highSignalType);
            } else if (switchInfo.getIoType() == 3) { //IO??????2
                io92.put(ioSite, highSignalType);
            }
            // ??????I/0?????????I/0??????????????????????????????
            Integer alarmPos = getAlarmStatus(ioType, ioSite);
            String alarmStatus = monitorIoAlarmSet.get(alarmPos) != null ? monitorIoAlarmSet.get(alarmPos) : "";
            alarmStatusList.add(alarmStatus);
            switchName.add(switchInfo.getName());
        }
        result.put("90", io90);
        result.put("91", io91);
        result.put("92", io92);
        msg.put("names", switchName.size() > 0 ? switchName : null);
        msg.put("IOStatus", ioStatus.size() > 0 ? ioStatus : null);
        msg.put("alarmStatuses", alarmStatusList);
        return result;
    }

    /**
     * ??????I/0?????????I/0??????????????????????????????
     */
    private Integer getAlarmStatus(Integer ioType, Integer ioSite) {
        Integer alarmPoss = 0;
        if (ioType == null || ioSite == null) {
            return alarmPoss;
        }
        if (ioType == 1) { // ??????I/O
            alarmPoss = AlarmTypeUtil.IO_0X90_ALARM.get(ioSite);
        }
        if (ioType == 2) { // I/O??????1
            alarmPoss = AlarmTypeUtil.IO_0X91_ALARM.get(ioSite);
        }
        if (ioType == 3) { // I/0??????2
            alarmPoss = AlarmTypeUtil.IO_0X92_ALARM.get(ioSite);
        }
        return alarmPoss;
    }

    private Map<Integer, String> getMonitorIoAlarmSetting(String monitorId) {
        if (StringUtils.isBlank(monitorId)) {
            return null;
        }
        List<Map<String, Object>> ioAlarmSetting = alarmSettingDao.findIoAlarmValueByVehicleId(monitorId);
        if (CollectionUtils.isEmpty(ioAlarmSetting)) {
            return null;
        }
        Map<Integer, String> ioAlarmStatus = new HashMap<>();
        ioAlarmSetting.forEach(data -> {
            if (MapUtils.isEmpty(data)) {
                return;
            }
            Integer poss = data.get("pos") != null ? Integer.parseInt(String.valueOf(data.get("pos"))) : null;
            String parameterValue =
                data.get("parameter_value") != null ? String.valueOf(data.get("parameter_value")) : null;
            if (poss != null && parameterValue != null) {
                ioAlarmStatus.put(poss, parameterValue);
            }
        });
        return ioAlarmStatus;
    }

    /**
     * ????????????????????????????????????
     */
    private List<SwitchSignalInfo> processPositionalInfo(List<Positional> queryResult,
        Map<String, Map<Integer, String>> ioSetting) {
        List<SwitchSignalInfo> resultData = new ArrayList<>();
        SwitchSignalInfo switchSignalInfo;
        List<Integer> status;
        Map<Integer, String> io90 = ioSetting.get("90");
        Map<Integer, String> io91 = ioSetting.get("91");
        Map<Integer, String> io92 = ioSetting.get("92");
        for (Positional ps : queryResult) {
            switchSignalInfo = new SwitchSignalInfo();
            status = new ArrayList<>();
            if (io90.containsKey(0)) {
                status.add(io90Data(ps.getIoOne()));
            }
            if (io90.containsKey(1)) {
                status.add(io90Data(ps.getIoTwo()));
            }
            if (io90.containsKey(2)) {
                status.add(io90Data(ps.getIoThree()));
            }
            if (io90.containsKey(3)) {
                status.add(io90Data(ps.getIoFour()));
            }
            if (io91 != null && io91.size() > 0) {
                status.addAll(ioDataDispose(ps.getIoObjOne(), io91));
            }
            if (io92 != null && io92.size() > 0) {
                status.addAll(ioDataDispose(ps.getIoObjTwo(), io92));
            }
            switchSignalInfo.setStatuses(status);
            switchSignalInfo.setTime(ps.getVtime());
            resultData.add(switchSignalInfo);
        }
        return resultData;
    }

    /**
     * ??????90 I/0 ????????????????????????
     */
    private Integer io90Data(Integer status) {
        Integer ioStatus = null;
        if (status != null) {
            if (status < 0) {
                ioStatus = 2;
            } else {
                ioStatus = status;
            }
        }
        return ioStatus;
    }

    /**
     * ?????????????????????????????????
     */
    private List<Integer> ioDataDispose(String positionalIoInfo, Map<Integer, String> dataSign) {
        List<Integer> ioStates = new ArrayList<>();
        if (StringUtils.isNotBlank(positionalIoInfo)) {
            // io???????????????
            JSONObject info = JSON.parseObject(positionalIoInfo);
            if (info.getInteger("unusual") == 1) { // Io??????
                // ???IO???????????? ?????????????????????
                for (int i = 0; i < dataSign.size(); i++) {
                    ioStates.add(2);
                }
            } else {
                JSONArray statusList = info.getJSONArray("statusList");
                if (statusList != null && statusList.size() != 0 && statusList.getJSONObject(0) != null
                    && statusList.getJSONObject(0).getInteger("ioStatus") != null) {
                    Integer ioStatus = statusList.getJSONObject(0).getInteger("ioStatus");
                    for (Map.Entry<Integer, String> entry : dataSign.entrySet()) {
                        Integer state = ConvertUtil.binaryIntegerWithOne(ioStatus, entry.getKey());
                        ioStates.add(state);
                    }
                } else {
                    // ?????????????????????io????????????,???????????????
                    for (int i = 0; i < dataSign.size(); i++) {
                        ioStates.add(2);
                    }
                }
            }
        } else {
            for (int i = 0; i < dataSign.size(); i++) {
                ioStates.add(null);
            }
        }
        return ioStates;
    }

    /**
     * ?????????????????????
     */
    @Override
    public void exportTimeZoneTrackPlay(HttpServletResponse response, String areaListStr, String groupName)
        throws Exception {
        ExportExcelUtil.setResponseHead(response, groupName + "??????????????????????????????");
        //???????????????????????????
        List<AreaInfo> areaInfoList = JSONObject.parseArray(areaListStr, AreaInfo.class);
        AreaInfo areaOne = null;
        AreaInfo areaTwo = null;
        for (AreaInfo area : areaInfoList) {
            if (("areaOne").equals(area.getAreaId())) {
                areaOne = area;
            } else {
                areaTwo = area;
            }
        }
        RedisKey redisKey =
            HistoryRedisKeyEnum.TRACK_PLAYBACK_TIME_ZONE_SUFFIX_KEY.of(SystemHelper.getCurrentUsername());
        Map<String, String> resultMap = RedisHelper.hgetAll(redisKey);
        List<TimeZonePositional> listOne = JSONObject.parseArray(resultMap.get("areaOne"), TimeZonePositional.class);
        List<TimeZonePositional> listTwo = JSONObject.parseArray(resultMap.get("areaTwo"), TimeZonePositional.class);
        List<TimeZoneExportForm> exportList = new ArrayList<>();
        if (null != listOne && listOne.size() != 0 && null != areaOne) {
            getTimeZoneDetails(areaOne, listOne, exportList);
        }
        if (null != listTwo && listTwo.size() != 0 && null != areaTwo) {
            getTimeZoneDetails(areaTwo, listTwo, exportList);
        }
        for (int id = 0; id < exportList.size(); id++) {
            exportList.get(id).setId(String.valueOf(id + 1));
        }
        ExportExcelUtil.exportTimeZoneData(
            new ExportExcelParam("", 1, exportList, TimeZoneExportForm.class, null, response.getOutputStream()));
    }

    /**
     * ???????????????????????????
     */
    private void getTimeZoneDetails(AreaInfo areaInfo, List<TimeZonePositional> list,
        List<TimeZoneExportForm> exportList) throws Exception {
        for (TimeZonePositional tzPositional : list) {
            long startTime = DateUtils.parseDate(tzPositional.getStartTime(), DATE_FORMAT).getTime() / 1000;
            long endTime = DateUtils.parseDate(tzPositional.getEndTime(), DATE_FORMAT).getTime() / 1000;
            //???????????????????????????????????????????????????????????????
            List<Positional> positionals = getQueryDetails(tzPositional.getVehicleIdStr(), startTime, endTime);
            boolean flog = true;
            boolean derail = true;
            TimeZoneExportForm form = new TimeZoneExportForm();
            //????????????????????????LIST???????????????????????????????????????
            List<TimeZoneExportForm> detailsList = new ArrayList<>();
            //??????????????????????????????????????????
            int intoAreaNumber = 0;
            int outAreaNumber = 0;
            //?????????????????????????????????????????????
            if (positionals.size() != 0) {
                for (Positional i : positionals) {
                    if (StringUtils.isEmpty(i.getLatitude()) || StringUtils.isEmpty(i.getLongtitude())) {
                        continue;
                    }
                    double latitude = Double.parseDouble(i.getLatitude());
                    double longitude = Double.parseDouble(i.getLongtitude());
                    if (latitude <= areaInfo.getRightFloorLatitude() && latitude >= areaInfo.getLeftTopLatitude()
                        && longitude >= areaInfo.getLeftTopLongitude() && longitude <= areaInfo
                        .getRightFloorLongitude()) {
                        if (flog) { // ????????????????????????????????????,???????????????????????????????????????
                            if (derail) {
                                derail = false;// ???????????????????????????
                                getTimeZoneInfo(form, tzPositional);
                                // intoAreaNumber++;
                                // long time = i.getVtime() * 1000;// ???????????????????????????
                                form.setIntoAreaTime("???????????????");
                            }
                        } else { // ????????????
                            getTimeZoneInfo(form, tzPositional);
                            intoAreaNumber++;
                            long time = i.getVtime() * 1000;// ???????????????????????????
                            form.setIntoAreaTime(LocalDateUtils.dateTimeFormat(new Date(time)));
                            flog = true;
                        }
                    } else {
                        if (flog) {
                            // ????????????
                            if (!derail) { // ???????????????????????????
                                long time = i.getVtime() * 1000;// ?????????????????????
                                form.setOutAreaTime(LocalDateUtils.dateTimeFormat(new Date(time)));
                                detailsList.add(form);
                                form = new TimeZoneExportForm();
                                outAreaNumber++;
                            } else {
                                derail = false;
                            }
                            flog = false;
                        }
                    }

                }
            }
            if (null == form.getOutAreaTime() && null != form.getIntoAreaTime()) {
                form.setOutAreaTime("????????????");
                detailsList.add(form);
            }
            //???????????????????????????????????????,??????????????????????????????????????????list???
            for (TimeZoneExportForm exportForm : detailsList) {
                exportForm.setOutAreaNumber(String.valueOf(outAreaNumber));
                exportForm.setIntoAreaNumber(String.valueOf(intoAreaNumber));
                exportList.add(exportForm);
            }
        }
    }

    /**
     * ???????????????????????????????????????
     */
    private void getTimeZoneInfo(TimeZoneExportForm form, TimeZonePositional positional) {
        form.setAreaName("areaOne".equals(positional.getAreaName()) ? "??????1" : "??????2");
        form.setMonitorName(positional.getMonitorNumber());
        form.setTimeRange(positional.getStartTime() + "--" + positional.getEndTime());
    }

    /**
     * ??????????????????
     * @param vehicleId  ????????????ID
     * @param startTime  ??????????????????
     * @param endTime    ??????????????????
     * @param sensorFlag ?????????
     * @return json
     * @throws Exception e
     */
    @Override
    public JsonResultBean getLoadWeightDate(String vehicleId, String startTime, String endTime, Integer sensorFlag,
        Integer sensorNo) throws Exception {
        JSONObject result = new JSONObject();
        JSONArray loadWeightChartDataArr = new JSONArray();
        // ????????????????????????????????????
        List<SensorSettingInfo> monitorBandSensorInfoBySensorType =
            sensorSettingsDao.getMonitorBandSensorInfoBySensorType(vehicleId, 6);
        JSONObject object = null;
        if (CollectionUtils.isEmpty(monitorBandSensorInfoBySensorType)) {
            return new JsonResultBean(JsonResultBean.FAULT, "???????????????????????????????????????");
        } else {
            if (monitorBandSensorInfoBySensorType.size() >= sensorNo) {
                SensorSettingInfo sensorSettingInfo = monitorBandSensorInfoBySensorType.get(sensorNo - 1);
                String individualityParameters = sensorSettingInfo.getIndividualityParameters();
                object = JSONObject.parseObject(individualityParameters);
            }
        }

        Double maxLoadWeight = null;
        Double minLoadWeight = null;
        // ????????????
        List<Positional> positionalList = getCachePositionalInfoList(vehicleId, startTime, endTime, sensorFlag);
        if (CollectionUtils.isNotEmpty(positionalList)) {
            for (Positional positional : positionalList) {
                JSONObject positionalJsonObj = new JSONObject();
                //??????
                positionalJsonObj.put("time", positional.getVtime());
                JSONObject loadObj = getLoadWeightBySensorNo(sensorNo, positional);
                //????????????
                String loadWeight = loadObj != null ? loadObj.getString("loadWeight") : null;
                //????????????
                String status = loadObj != null ? loadObj.getString("status") : null;
                //???????????????0-0.1Kg???1-1kg???2-10kg???3-100kg???
                String unit = loadObj != null ? loadObj.getString("unit") : null;
                //????????????(kg): loadWeight(????????????)/unit(????????????)
                Double instanceWeight = null;
                if (StringUtils.isNotBlank(loadWeight) && StringUtils.isNotBlank(unit)) {
                    instanceWeight = Double.parseDouble(loadWeight);
                }
                //??????
                positionalJsonObj.put("weight", instanceWeight);
                positionalJsonObj.put("status", status);
                loadWeightChartDataArr.add(positionalJsonObj);
                if (instanceWeight == null) {
                    continue;
                }
                maxLoadWeight = maxLoadWeight == null ? instanceWeight :
                    instanceWeight > maxLoadWeight ? instanceWeight : maxLoadWeight;
                minLoadWeight = minLoadWeight == null ? instanceWeight :
                    instanceWeight < minLoadWeight ? instanceWeight : minLoadWeight;
            }
        }
        result.put("sensorDataList", loadWeightChartDataArr);
        // ?????????
        result.put("maxLoadWeight", maxLoadWeight);
        // ?????????
        result.put("minLoadWeight", minLoadWeight);
        // ???????????????????????????????????????
        result.put("fullLoadValue", object == null ? null : object.getString("fullLoadValue"));
        result.put("lightLoadValue", object == null ? null : object.getString("lightLoadValue"));
        result.put("noLoadValue", object == null ? null : object.getString("noLoadValue"));
        result.put("overLoadValue", object == null ? null : object.getString("overLoadValue"));

        return new JsonResultBean(result);
    }

    /**
     * ??????????????????
     * @param vehicleId  ????????????ID
     * @param startTime  ??????????????????
     * @param endTime    ??????????????????
     * @param sensorFlag ?????????
     * @return json
     * @throws Exception e
     */
    @Override
    public AppResultBean appLoadWeightDate(String vehicleId, String startTime, String endTime, Integer sensorFlag)
        throws Exception {
        JSONObject re = new JSONObject();
        JSONArray sensorDataList = new JSONArray();
        // ????????????????????????????????????
        List<SensorSettingInfo> monitorBandSensorInfoBySensorType =
            sensorSettingsDao.getMonitorBandSensorInfoBySensorType(vehicleId, 6);
        String[] fullLoadValues = new String[monitorBandSensorInfoBySensorType.size()];
        String[] lightLoadValues = new String[monitorBandSensorInfoBySensorType.size()];
        String[] noLoadValues = new String[monitorBandSensorInfoBySensorType.size()];
        String[] overLoadValues = new String[monitorBandSensorInfoBySensorType.size()];
        if (!CollectionUtils.isEmpty(monitorBandSensorInfoBySensorType)) {
            for (int i = 0; i < monitorBandSensorInfoBySensorType.size(); i++) {
                SensorSettingInfo sensorSettingInfo = monitorBandSensorInfoBySensorType.get(i);
                String individualityParameters = sensorSettingInfo.getIndividualityParameters();
                JSONObject object = JSONObject.parseObject(individualityParameters);
                fullLoadValues[i] = object == null ? null : object.getString("fullLoadValue");
                lightLoadValues[i] = object == null ? null : object.getString("lightLoadValue");
                noLoadValues[i] = object == null ? null : object.getString("noLoadValue");
                overLoadValues[i] = object == null ? null : object.getString("overLoadValue");
            }
        } else {
            return new AppResultBean(AppResultBean.SUCCESS, "???????????????????????????????????????!");
        }

        Map<Integer, Double> maxLoadWeightMap = new HashMap<>();
        Map<Integer, Double> minLoadWeightMap = new HashMap<>();
        // ????????????
        List<Positional> positionalList = getAppCachePositionalInfoList(vehicleId, startTime, endTime, sensorFlag);
        if (CollectionUtils.isNotEmpty(positionalList)) {
            for (Positional positional : positionalList) {
                JSONObject positionalJsonObj = new JSONObject();
                //??????
                positionalJsonObj.put("time", positional.getVtime());
                JSONArray loadObjs = getLoadWeight(positional);
                Double[] weights = new Double[monitorBandSensorInfoBySensorType.size()];
                String[] status = new String[monitorBandSensorInfoBySensorType.size()];
                for (int i = 0; i < monitorBandSensorInfoBySensorType.size(); i++) {
                    if (loadObjs.size() == 0) {
                        weights[i] = null;
                        status[i] = null;
                        continue;
                    }
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = loadObjs.getJSONObject(i);
                    } catch (Exception e) {
                        //????????????
                    }
                    if (jsonObject != null) {
                        //????????????
                        String loadWeight = jsonObject.getString("loadWeight");
                        String st = jsonObject.getString("status");
                        //???????????????0-0.1Kg???1-1kg???2-10kg???3-100kg???
                        String unit = jsonObject.getString("unit");
                        //????????????(kg): loadWeight(????????????)/unit(????????????)
                        Double instanceWeight = null;
                        if (StringUtils.isNotBlank(loadWeight) && StringUtils.isNotBlank(unit)) {
                            instanceWeight = Double.parseDouble(loadWeight);
                        }
                        weights[i] = instanceWeight;
                        status[i] = st;
                        if (instanceWeight == null) {
                            continue;
                        }
                        if (maxLoadWeightMap.containsKey(i)) {
                            Double maxLoadWeight = maxLoadWeightMap.get(i);
                            maxLoadWeightMap.put(i, instanceWeight > maxLoadWeight ? instanceWeight : maxLoadWeight);
                        } else {
                            maxLoadWeightMap.put(i, instanceWeight);
                        }

                        if (minLoadWeightMap.containsKey(i)) {
                            Double minLoadWeight = minLoadWeightMap.get(i);
                            minLoadWeightMap.put(i, instanceWeight < minLoadWeight ? instanceWeight : minLoadWeight);
                        } else {
                            minLoadWeightMap.put(i, instanceWeight);
                        }
                    } else {
                        weights[i] = null;
                        status[i] = null;
                    }
                }
                positionalJsonObj.put("weight", weights);
                positionalJsonObj.put("status", status);
                sensorDataList.add(positionalJsonObj);
            }
        }

        re.put("sensorDataList", sensorDataList);
        // ???????????????????????????????????????
        re.put("fullLoadValue", fullLoadValues);
        re.put("lightLoadValue", lightLoadValues);
        re.put("noLoadValue", noLoadValues);
        re.put("overLoadValue", overLoadValues);
        Double[] maxLoadWeights = new Double[maxLoadWeightMap.size()];
        Double[] minLoadWeights = new Double[minLoadWeightMap.size()];
        for (int i = 0; i < maxLoadWeightMap.size(); i++) {
            maxLoadWeights[i] = maxLoadWeightMap.get(i);
            minLoadWeights[i] = minLoadWeightMap.get(i);
        }

        re.put("maxLoadWeight", maxLoadWeights);
        // ?????????
        re.put("minLoadWeight", minLoadWeights);
        return new AppResultBean(re);
    }

    /**
     * ??????????????????
     * @param vehicleId  ????????????ID
     * @param startTime  ??????????????????
     * @param endTime    ??????????????????
     * @param sensorFlag ?????????
     * @param tireNum    ????????????
     * @return json
     * @throws Exception e
     */
    @Override
    public JsonResultBean getTirePressureData(String vehicleId, String startTime, String endTime, Integer sensorFlag,
        Integer tireNum) throws Exception {
        JSONObject result = new JSONObject();
        JSONArray tirePressureChartDataArr = new JSONArray();
        // ????????????????????????????????????
        List<SensorSettingInfo> monitorBandSensorInfoBySensorType =
            sensorSettingsDao.getMonitorBandSensorInfoBySensorType(vehicleId, 7);
        JSONObject object;
        if (CollectionUtils.isEmpty(monitorBandSensorInfoBySensorType)) {
            return new JsonResultBean(JsonResultBean.FAULT, "???????????????????????????????????????");
        } else {
            SensorSettingInfo sensorSettingInfo = monitorBandSensorInfoBySensorType.get(0);
            String individualityParameters = sensorSettingInfo.getIndividualityParameters();
            object = JSONObject.parseObject(individualityParameters);
        }
        //????????????
        Double maxTirePressure = null;
        //????????????
        Double minTirePressure = null;
        // ????????????
        List<Positional> positionalList = getCachePositionalInfoList(vehicleId, startTime, endTime, sensorFlag);
        if (CollectionUtils.isNotEmpty(positionalList)) {
            for (Positional positional : positionalList) {
                JSONObject positionalJsonObj = new JSONObject();
                //??????
                positionalJsonObj.put("time", positional.getVtime());
                //????????????
                JSONObject tireObj = JSONObject.parseObject(positional.getTirePressureParameter());
                JSONArray list = tireObj != null ? tireObj.getJSONArray("list") : null;
                String pressure = null;
                //???????????????????????????
                if (list != null) {
                    positionalJsonObj.put("pressure", null);
                    for (int j = 0; j < list.size(); j++) {
                        JSONObject jo = list.getJSONObject(j);
                        int m = jo.getInteger("number");
                        if (tireNum - 1 == m) {
                            pressure = JSONObject.parseObject(jo.toString()).getString("pressure");
                            positionalJsonObj.put("pressure", pressure);
                        }
                    }
                } else {
                    positionalJsonObj.put("pressure", null);
                }
                tirePressureChartDataArr.add(positionalJsonObj);
                if (pressure == null) {
                    continue;
                }
                maxTirePressure = maxTirePressure == null ? Double.parseDouble(pressure) :
                    Double.parseDouble(pressure) > maxTirePressure ? Double.parseDouble(pressure) : maxTirePressure;
                minTirePressure = minTirePressure == null ? Double.parseDouble(pressure) :
                    Double.parseDouble(pressure) < minTirePressure ? Double.parseDouble(pressure) : minTirePressure;
            }
        }
        result.put("sensorDataList", tirePressureChartDataArr);
        // ?????????
        result.put("maxTirePressure", maxTirePressure);
        // ?????????
        result.put("minTirePressure", minTirePressure);
        // ?????????????????????
        result.put("lowPressure", object == null ? null : object.getString("lowPressure"));
        result.put("heighPressure", object == null ? null : object.getString("heighPressure"));
        return new JsonResultBean(result);
    }

    /**
     * ??????????????????
     * @param vehicleId  ????????????ID
     * @param startTime  ??????????????????
     * @param endTime    ??????????????????
     * @param sensorFlag ?????????
     * @return json
     * @throws Exception e
     */
    @Override
    public AppResultBean appTirePressureData(String vehicleId, String startTime, String endTime, Integer sensorFlag)
        throws Exception {
        JSONObject re = new JSONObject();
        JSONArray sensorDataList = new JSONArray();
        // ????????????????????????????????????
        List<SensorSettingInfo> monitorBandSensorInfoBySensorType =
            sensorSettingsDao.getMonitorBandSensorInfoBySensorType(vehicleId, 7);
        JSONObject object;
        Integer[] tireNum;
        if (CollectionUtils.isEmpty(monitorBandSensorInfoBySensorType)) {
            return new AppResultBean(AppResultBean.SUCCESS, "???????????????????????????????????????!");
        } else {
            SensorSettingInfo sensorSettingInfo = monitorBandSensorInfoBySensorType.get(0);
            String individualityParameters = sensorSettingInfo.getIndividualityParameters();
            tireNum = new Integer[sensorSettingInfo.getNumberOfTires()];
            for (int i = 0; i < tireNum.length; i++) {
                tireNum[i] = i + 1;
            }
            object = JSONObject.parseObject(individualityParameters);
        }

        Map<Integer, Double> maxTirePressureMap = new HashMap<>();
        Map<Integer, Double> minTirePressureMap = new HashMap<>();
        // ????????????
        List<Positional> positionalList = getAppCachePositionalInfoList(vehicleId, startTime, endTime, sensorFlag);
        if (CollectionUtils.isNotEmpty(positionalList)) {
            for (Positional positional : positionalList) {

                //????????????
                JSONObject tireObj = JSONObject.parseObject(positional.getTirePressureParameter());
                JSONArray list = tireObj != null ? tireObj.getJSONArray("list") : null;
                JSONObject positionalJsonObj = new JSONObject();
                Double[] pressures = new Double[tireNum.length];
                //??????
                positionalJsonObj.put("time", positional.getVtime());
                for (int i = 0; i < tireNum.length; i++) {
                    if (list == null) {
                        pressures[i] = null;
                        continue;
                    }
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = list.getJSONObject(i);
                    } catch (Exception e) {
                        //????????????
                        logger.error(e.getMessage(), e);
                    }
                    if (jsonObject != null) {
                        //???????????????????????????
                        String pressure = jsonObject.getString("pressure");
                        pressures[i] = Double.parseDouble(pressure);

                        if (maxTirePressureMap.containsKey(i)) {
                            Double maxTirePressure = maxTirePressureMap.get(i);
                            maxTirePressureMap.put(i,
                                Double.parseDouble(pressure) > maxTirePressure ? Double.parseDouble(pressure) :
                                    maxTirePressure);
                        } else {
                            maxTirePressureMap.put(i, Double.parseDouble(pressure));
                        }

                        if (minTirePressureMap.containsKey(i)) {
                            Double minTirePressure = minTirePressureMap.get(i);
                            minTirePressureMap.put(i,
                                Double.parseDouble(pressure) < minTirePressure ? Double.parseDouble(pressure) :
                                    minTirePressure);
                        } else {
                            minTirePressureMap.put(i, Double.parseDouble(pressure));
                        }
                    } else {
                        pressures[i] = null;
                    }
                }
                positionalJsonObj.put("pressure", pressures);
                sensorDataList.add(positionalJsonObj);
            }
        }

        re.put("sensorDataList", sensorDataList);
        // ?????????????????????
        re.put("lowPressure", object == null ? null : object.getString("lowPressure"));
        re.put("heighPressure", object == null ? null : object.getString("heighPressure"));
        Double[] maxTirePressure = new Double[maxTirePressureMap.size()];
        Double[] minTirePressure = new Double[minTirePressureMap.size()];
        for (int i = 0; i < maxTirePressureMap.size(); i++) {
            maxTirePressure[i] = maxTirePressureMap.get(i);
            minTirePressure[i] = minTirePressureMap.get(i);
        }
        // ?????????
        re.put("maxTirePressure", maxTirePressure);
        // ?????????
        re.put("minTirePressure", minTirePressure);
        re.put("tireNum", tireNum);
        return new AppResultBean(re);
    }

}

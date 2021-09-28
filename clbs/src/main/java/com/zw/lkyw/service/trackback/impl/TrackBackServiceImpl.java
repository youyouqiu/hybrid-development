package com.zw.lkyw.service.trackback.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.cb.platform.domain.MileageStatisticInfo;
import com.cb.platform.domain.VehicleSpotCheckInfo;
import com.cb.platform.repository.mysqlDao.SpotCheckReportDao;
import com.google.common.collect.Maps;
import com.zw.app.domain.monitor.MonitorMileQueryParam;
import com.zw.app.domain.monitor.SwitchInfo;
import com.zw.app.domain.monitor.SwitchSignalInfo;
import com.zw.app.domain.monitor.WinchInfo;
import com.zw.lkyw.domain.common.PaasCloudDTO;
import com.zw.lkyw.domain.common.PaasCloudZipDTO;
import com.zw.lkyw.domain.trackback.MileageDetailDO;
import com.zw.lkyw.domain.trackback.TimeZonePositionalDTO;
import com.zw.lkyw.service.trackback.TrackBackService;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.MonitorBaseDTO;
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
import com.zw.platform.domain.vas.history.AreaInfo;
import com.zw.platform.domain.vas.history.HistoryStopData;
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
import com.zw.platform.service.core.CustomColumnService;
import com.zw.platform.service.monitoring.impl.RealTimeServiceImpl;
import com.zw.platform.service.obdManager.OBDVehicleTypeService;
import com.zw.platform.service.oil.PositionalService;
import com.zw.platform.service.personalized.IcoService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.CommonUtil;
import com.zw.platform.util.ConvertUtil;
import com.zw.platform.util.LocalDateUtils;
import com.zw.platform.util.TrackBackUtil;
import com.zw.platform.util.common.AlarmTypeUtil;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.platform.util.common.ZipUtil;
import com.zw.platform.util.excel.ExportExcelParam;
import com.zw.platform.util.excel.ExportExcelUtil;
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
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.zw.platform.domain.oil.PositionalForm.STOP_STATE;

@Service
public class TrackBackServiceImpl implements TrackBackService {

    @Autowired
    IcoService icoService;

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
    private MonitorIconService monitorIconService;

    @Value("${positional.info.abnormal.data.filter.flag:false}")
    private boolean filterFlag;
    /**
     * 日期转换格式
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private static final String MONTH_DATE_FORMAT = "yyyyMM";

    /**
     * 轨迹回放基础数据key
     */
    private static final String LKYW_TRACK_PLAYBACK_BASE_DATA_SUFFIX_KEY = "lkyw_track_playback_list";

    /**
     * OBD数据
     */
    private static final String LKYW_TRACK_PLAYBACK_OBD_SUFFIX_KEY = "lkyw_track_playback_OBD_list";

    /**
     * 报警数据
     */
    private static final String LKYW_TRACK_PLAYBACK_ALARM_SUFFIX_KEY = "lkyw_track_playback_ALARM_list";

    /**
     * 定时定区域
     */
    private static final String LKYW_TRACK_PLAYBACK_TIME_ZONE_SUFFIX_KEY = "lkyw_track_playback_timeZone_list";

    /**
     * 停机
     */
    private static final Integer HALT_STATE = 0;

    /**
     * 工作
     */
    private static final Integer WORK_STATE = 1;

    /**
     * 待机
     */
    private static final Integer STANDBY_STATE = 2;

    private static Logger logger = LogManager.getLogger(TrackBackServiceImpl.class);

    /**
     * 轨迹回放，获取车辆行驶数据，停止数据
     */
    @Override
    public JsonResultBean getHistoryVehicle(String vehicleId, String startTime, String endTime, Integer sensorFlag,
        String ip, Integer reissue) throws Exception {
        Positional positional;
        JSONArray stops = new JSONArray();
        JSONArray resultful = new JSONArray();
        JSONArray stop = new JSONArray();
        HistoryStopData stopData = null;
        List<Positional> list = getQueryDetails(vehicleId, startTime, endTime, reissue);
        // 查询历史数据为空直接返回空
        if (list == null || list.isEmpty()) {
            return new JsonResultBean(false);
        }
        // 获取超长待机信息
        String functionalType = TrackBackUtil.getfunctionalType(vehicleId);
        // 查询车辆信息，
        Map<String, String> monitorIco = monitorIconService.getByMonitorId(Collections.singleton(vehicleId));
        BindDTO bindInfo = VehicleUtil.getBindInfoByRedis(vehicleId);
        String plateColor = PlateColor.getNameOrBlankByCode(String.valueOf(bindInfo.getPlateColor()));
        String simCard = bindInfo.getSimCardNumber();
        String icos = monitorIco.get(vehicleId);

        // 判断监控对象是否绑定里程传感器
        boolean flogKey = RedisHelper.isContainsKey(HistoryRedisKeyEnum.SENSOR_MESSAGE.of(vehicleId));
        double longitudeOld = 0.0;
        double latitudeOld = 0.0;

        // 位置信息异常数据过滤
        CommonUtil.positionalInfoAbnormalFilter(list, filterFlag);

        // 判断是否为离线数据,是则取当天是否绑定传感器的标识
        if (sensorFlag != null) {
            flogKey = sensorFlag == 1;
        }

        // 遍历集合,获取每一条历史轨迹
        for (int i = 0, n = list.size(); i < n; i++) {
            positional = list.get(i);
            positional.setPlateColor(plateColor);
            positional.setSimCard(simCard);
            positional.setIco(icos);
            // 如果速度为0
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
                    // 设置轨迹
                    stopData.setPositional(positional);
                    // 设置开始时间
                    stopData.setStartTime(Converter.timeStamp2Date(String.valueOf(positional.getTime()), null));
                }
                // 如果是最后一条数据
                if (i == n - 1) {
                    // 设置结束时间
                    stopData.setEndTime(Converter.timeStamp2Date(String.valueOf(list.get(i - 1).getTime()), null));
                    // 设置停车时间
                    stopData.setStopTime(DateUtils.parseDate(stopData.getEndTime(), DATE_FORMAT).getTime() - DateUtils
                        .parseDate(stopData.getStartTime(), DATE_FORMAT).getTime());
                    stops.add(stopData);
                    stopData = null;
                }
                // 将一条历史轨迹存储到stop集合中
                stop.add(positional);
                if ("standby".equals(functionalType)) { // 超长待机设备的停止点算做行驶点
                    resultful.add(positional);
                }
            } else {
                // 如果有停车记录
                if (stopData != null) {
                    stopData.setEndTime(Converter.timeStamp2Date(String.valueOf(list.get(i - 1).getTime()), null));
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
        msg.put("stops", stops);// 一条轨迹
        msg.put("resultful", resultful);// 多条轨迹
        msg.put("stop", stop);// 多条停车数据
        msg.put("type", functionalType);// 超长待机类型专用
        msg.put("groups", realTime.getGroups(vehicleId));// 当前用户的车辆分组
        msg.put("nowFlogKey", flogKey);
        String msgResult = JSON.toJSONString(msg, SerializerFeature.DisableCircularReferenceDetect);
        msgResult = ZipUtil.compress(msgResult);
        ls.addLog(vehicleId, 0, ip);
        return new JsonResultBean(true, msgResult);
    }

    /**
     * 轨迹回放，获取人行驶数据，停止数据
     */
    @Override
    public JsonResultBean getHistoryPeople(String vehicleId, String startTime, String endTime, Integer sensorFlag,
        String ip, Integer reissue) throws Exception {

        BdtdPosition bdtdPosition;
        JSONArray stops = new JSONArray();
        JSONArray resultful = new JSONArray();
        JSONArray stop = new JSONArray();
        HistoryStopData stopData = null;
        JSONObject msg = new JSONObject();
        Map<String, String> queryParam = Maps.newHashMap();
        queryParam.put("monitorId", vehicleId);
        queryParam.put("startTime", startTime + "");
        queryParam.put("endTime", endTime + "");
        if (Objects.nonNull(reissue)) {
            queryParam.put("reissueFlag", String.valueOf(reissue));
        }
        String str = HttpClientUtil.send(PaasCloudUrlEnum.POSITIONAL_HISTORY_URL, queryParam);
        PaasCloudZipDTO dto = null;
        if (!StringUtils.isEmpty(str)) {
            dto = JSON.parseObject(str, PaasCloudZipDTO.class);
        }
        if (dto == null) {
            return new JsonResultBean(false);
        }
        String data = dto.getData();
        if (StringUtils.isEmpty(data)) {
            return new JsonResultBean(false);
        }
        /*String locationUncompress =
            ZipUtil.uncompress(data.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8.toString());*/
        List<BdtdPosition> positionals = JSON.parseArray(data, BdtdPosition.class);
        if (CollectionUtils.isEmpty(positionals)) {
            return new JsonResultBean(false);
        }
        double longtitudeOld = 0.0;
        double latitudeOld = 0.0;
        for (int i = 0, n = positionals.size(); i < n; i++) {
            bdtdPosition = positionals.get(i);
            bdtdPosition.setPeopleId(bdtdPosition.getVehicleId());
            // 如果速度为0
            double longtitude =
                Double.parseDouble(bdtdPosition.getLongtitude() == null ? "0.0" : bdtdPosition.getLongtitude());
            double latitude =
                Double.parseDouble(bdtdPosition.getLatitude() == null ? "0.0" : bdtdPosition.getLatitude());
            if (longtitude != 0.0 && latitude != 0.0) {
                if ("0".equals(bdtdPosition.getSpeed()) || "0.0".equals(bdtdPosition.getSpeed())
                    || bdtdPosition.getSpeed() == null || (Math.abs(longtitudeOld - longtitude) < 0.00015
                    && Math.abs(latitudeOld - latitude) < 0.00015)) {
                    // 如果停车数据为空
                    if (stopData == null) {
                        stopData = new HistoryStopData();
                        // 设置轨迹
                        stopData.setBdtdPosition(bdtdPosition);
                        // 设置开始时间
                        stopData.setStartTime(Converter.timeStamp2Date(String.valueOf(bdtdPosition.getVtime()), null));
                    }
                    // 如果是最后一条数据
                    if (i == n - 1) {
                        // 设置结束时间
                        stopData.setEndTime(
                            Converter.timeStamp2Date(String.valueOf(positionals.get(i - 1).getVtime()), null));
                        // 设置停车时间
                        stopData.setStopTime(
                            DateUtils.parseDate(stopData.getEndTime(), DATE_FORMAT).getTime() - DateUtils
                                .parseDate(stopData.getStartTime(), DATE_FORMAT).getTime());
                        stops.add(stopData);
                        stopData = null;
                    }
                    // 将一条历史轨迹存储到stop集合中
                    stop.add(bdtdPosition);
                } else {
                    // 如果有停车记录
                    if (stopData != null) {
                        stopData.setEndTime(
                            Converter.timeStamp2Date(String.valueOf(positionals.get(i - 1).getVtime()), null));
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
        msg.put("stops", stops);// 多条停车数据
        msg.put("resultful", resultful);// 多条轨迹
        msg.put("stop", stop);// 一条轨迹
        msg.put("groups", realTime.getGroups(vehicleId));// 当前用户的车辆分组
        String msgResult = JSON.toJSONString(msg, SerializerFeature.DisableCircularReferenceDetect);
        msgResult = ZipUtil.compress(msgResult);
        ls.addLog(vehicleId, 1, ip);
        return new JsonResultBean(true, msgResult);
    }

    /**
     * 获取轨迹回放车辆行驶的日历数据
     * 修改记录:
     * 版本:APP 2.1.0 原因:APP监控对象日历里程需要支持跨月查询
     */
    @Override
    public JsonResultBean changeHistoryActiveDate(String vehicleId, String nowMonth, String nextMonth, String type,
        Integer bigDataFlag, boolean isAppFlag) throws Exception {
        JSONObject msg = new JSONObject();
        JSONArray dates = new JSONArray();
        JSONArray dailyMiles = new JSONArray();
        JSONArray dailySensorFlag = new JSONArray();
        boolean offLineFlag = false;
        // 根据车辆 id查询指定月份返回每日里程
        if (!isAppFlag && RedisHelper.isContainsKey(RedisKeyEnum.MONITOR_INFO.of(vehicleId))) {
            //车辆抽查表
            saveVehicleHistorySpotCheck(vehicleId);
        }
        MonitorMileQueryParam param = new MonitorMileQueryParam();
        param.setMonitorId(vehicleId);
        param.setQueryStartMonth(nowMonth);
        param.setQueryEndMonth(nextMonth);
        param.setAppFlag(isAppFlag);
        // 获取离线里程数据
        List<MileageStatisticInfo> offLineData = getMonitorOffLineMileData(isAppFlag, nowMonth, nextMonth, vehicleId);
        param.setOffLineData(offLineData);
        // 组装数据
        offLineFlag = getBigDataCalendarMileInfo(dates, dailyMiles, dailySensorFlag, param);
        msg.put("date", dates);
        msg.put("dailyMile", dailyMiles);
        msg.put("dailySensorFlag", dailySensorFlag);
        msg.put("type", type);
        if (offLineFlag) {
            return new JsonResultBean(true, JSON.toJSONString(msg, true));
        } else {
            return new JsonResultBean(false);
        }
    }

    /**
     * 获取监控对象里程统计数据(离线月表)
     */
    private List<MileageStatisticInfo> getMonitorOffLineMileData(boolean isAppFlag, String nowMonth, String nextMonth,
        String monitorId) throws Exception {
        List<MileageStatisticInfo> offLineData = new ArrayList<>();
        if (isAppFlag) { // APP需要支持跨月查询
            // TODO: 2020/1/9
        } else { // 轨迹回放日历里程不支持跨月查询
            nowMonth = nowMonth.replaceAll("-", "");
            nextMonth = nextMonth.replaceAll("-", "");
            Map<String, String> queryParam = Maps.newHashMap();
            queryParam.put("monitorId", monitorId);
            queryParam.put("startTime", nowMonth + "000000");
            queryParam.put("endTime", nextMonth + "000000");
            queryParam.put("month", nowMonth.substring(0, 6));
            String str = HttpClientUtil.send(PaasCloudUrlEnum.LKYW_DAILY_MILEAGE_URL, queryParam);
            if (StringUtils.isEmpty(str)) {
                return offLineData;
            }
            PaasCloudDTO<MileageDetailDO> dto =
                JSON.parseObject(str, new TypeReference<PaasCloudDTO<MileageDetailDO>>() {
                });
            if (dto == null) {
                return offLineData;
            }
            List<MileageDetailDO> data = dto.getData();
            if (data == null || data.size() == 0) {
                return offLineData;
            }
            //遍历
            MileageStatisticInfo mileageStatisticInfo;
            for (MileageDetailDO mileageDetail : data) {
                mileageStatisticInfo = new MileageStatisticInfo();
                mileageStatisticInfo.setSensorFlag(mileageDetail.getSensorFlag());
                mileageStatisticInfo.setDay(mileageDetail.getDay());
                if (Objects.nonNull(mileageDetail.getGpsMile())) {
                    mileageStatisticInfo.setGpsMile(
                        new BigDecimal(mileageDetail.getGpsMile()).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
                }
                if (Objects.nonNull(mileageDetail.getMileage())) {
                    mileageStatisticInfo.setMileage(
                        new BigDecimal(mileageDetail.getMileage()).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
                }
                offLineData.add(mileageStatisticInfo);
            }
        }
        return offLineData;
    }

    /**
     * 获取轨迹回放日历里程数据(离线数据 + 今天实时数据)
     * @param dates           (日历日期Array)
     * @param dailyMiles      （日历里程Array）
     * @param dailySensorFlag （日历里程传感器标识Array）
     */
    private boolean getBigDataCalendarMileInfo(JSONArray dates, JSONArray dailyMiles, JSONArray dailySensorFlag,
        MonitorMileQueryParam param) throws Exception {
        // 监控对象id
        String queryStartMonth = param.getQueryStartMonth();
        List<MileageStatisticInfo> offLineData = param.getOffLineData();
        if (CollectionUtils.isEmpty(offLineData)) {
            return false;
        }
        Long queryStartDateTime = DateUtils.parseDate(queryStartMonth, "yyyy-MM-dd").getTime();
        /* 6.组装日历里程数据 */
        for (MileageStatisticInfo mileageStatisticInfo : offLineData) {
            Long dayTime = mileageStatisticInfo.getDay();
            // 计算时间是查询时间范围内的第几天
            Integer date = DateUtil.getTwoTimeDifference(queryStartDateTime, dayTime);
            dates.add(date);
            Integer sensorFlag = mileageStatisticInfo.getSensorFlag();
            // 由于此处可能存在历史数据, 3.8.2版本如果sensorFlag = 0, 则表示未绑定传感器milage=gpsMile, sensorFlag =1则表示绑定了里程传感器milage=传感器里程
            Double mileage = mileageStatisticInfo.getMileage();
            Double gpsMile = mileageStatisticInfo.getGpsMile();
            if (sensorFlag == 0) {
                // 未绑定传感器, gpsMile不存在, 但mileage存在,则使用mileage
                if (gpsMile == null && mileage != null) {
                    dailyMiles.add(mileage);
                } else {
                    dailyMiles.add(Optional.ofNullable(gpsMile).orElse(0.0));
                }
            } else {
                // 绑定了传感器,mileage不存在, 但gpsMile存在, 则使用gpsMile
                if (mileage == null && gpsMile != null) {
                    dailyMiles.add(gpsMile);
                } else {
                    dailyMiles.add(Optional.ofNullable(mileage).orElse(0.0));
                }
            }
            dailySensorFlag.add(sensorFlag);
        }

        /* 7.判断是否有返回值 */
        return offLineData.size() > 0;
    }

    /**
     * 存储抽查车辆历史轨迹信息
     */
    private void saveVehicleHistorySpotCheck(String vehicleId) {
        String userName = SystemHelper.getCurrentUsername();
        VehicleSpotCheckInfo vehicleSpotCheckInfo = new VehicleSpotCheckInfo();  //存储抽查信息数据
        vehicleSpotCheckInfo.setSpotCheckUser(userName);
        vehicleSpotCheckInfo.setVehicleId(vehicleId);
        vehicleSpotCheckInfo.setSpotCheckTime(new Date());
        vehicleSpotCheckInfo.setActualViewDate(new Date());
        vehicleSpotCheckInfo.setSpotCheckContent(1);
        Integer speedLimit = alarmSettingDao.getSpeedLimitByVehicleId(vehicleId);
        if (Objects.nonNull(speedLimit)) {
            vehicleSpotCheckInfo.setSpeedLimit(String.valueOf(speedLimit));
        }
        String cacheLocationInfo = RedisHelper.getString(HistoryRedisKeyEnum.MONITOR_LOCATION.of(vehicleId));
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
    public List<Positional> getQueryDetails(String vehicleId, String startTime, String endTime, Integer reissue)
        throws IOException {
        Map<String, String> queryParam = Maps.newHashMap();
        queryParam.put("monitorId", vehicleId);
        queryParam.put("startTime", startTime);
        queryParam.put("endTime", endTime);
        if (Objects.nonNull(reissue)) {
            queryParam.put("reissueFlag", String.valueOf(reissue));
        }
        String str = HttpClientUtil.send(PaasCloudUrlEnum.POSITIONAL_HISTORY_URL, queryParam);
        PaasCloudZipDTO dto = null;
        if (!StringUtils.isEmpty(str)) {
            dto = JSON.parseObject(str, PaasCloudZipDTO.class);
        }
        if (dto == null) {
            return new ArrayList<>();
        }
        String data = dto.getData();
        if (StringUtils.isEmpty(data)) {
            return new ArrayList<>();
        }
        /*String locationUncompress =
            ZipUtil.uncompress(data.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8.toString());*/
        List<Positional> positionals = JSON.parseArray(data, Positional.class);
        if (CollectionUtils.isEmpty(positionals)) {
            return new ArrayList<>();
        }
        return positionals;
    }

    @Override
    public JsonResultBean findHistoryByTimeAndAddress(String areaListStr, String monitorIds, String startTimeOne,
        String endTimeOne, String startTimeTwo, String endTimeTwo) {
        Map<String, String> queryMap = Maps.newHashMap();
        queryMap.put("areaList", areaListStr);
        queryMap.put("monitorIds", monitorIds);
        queryMap.put("startTimeOne", startTimeOne);
        queryMap.put("endTimeOne", endTimeOne);
        queryMap.put("startTimeTwo", startTimeTwo);
        queryMap.put("endTimeTwo", endTimeTwo);
        String str = HttpClientUtil.send(PaasCloudUrlEnum.TIME_AREA_URL, queryMap);
        if (StringUtils.isEmpty(str)) {
            return new JsonResultBean(JsonResultBean.FAULT, "数据为空");
        }
        PaasCloudDTO<Map<String, List<TimeZonePositionalDTO>>> dto =
            JSON.parseObject(str, new TypeReference<PaasCloudDTO<Map<String, List<TimeZonePositionalDTO>>>>() {
            });
        if (dto == null) {
            return new JsonResultBean(JsonResultBean.FAULT, "数据为空");
        }
        List<Map<String, List<TimeZonePositionalDTO>>> data = dto.getData();
        if (data == null || data.size() == 0) {
            return new JsonResultBean(JsonResultBean.FAULT, "数据为空");
        }
        Map<String, List<TimeZonePositionalDTO>> resultMap = data.get(0);
        // 将resultMap存入redis，用于导出时获取。
        if (resultMap.size() > 0) {
            RedisKey redisKey =
                HistoryRedisKeyEnum.LKYW_TRACK_PLAYBACK_TIME_ZONE_SUFFIX_KEY.of(SystemHelper.getCurrentUsername());
            if (RedisHelper.isContainsKey(redisKey)) {
                RedisHelper.delete(redisKey);
            }
            RedisHelper.addMapToHash(redisKey, resultMap);
            //设置有效时间30分钟
            RedisHelper.expireKey(redisKey, 1800);
        } else {
            //适配前端接口
            resultMap.put("areaOne", new ArrayList<>());
            resultMap.put("areaTwo", new ArrayList<>());
        }
        return new JsonResultBean(resultMap);
    }

    @Override
    public JsonResultBean getOilConsumptionChartData(String vehicleId, String startTime, String endTime,
        Integer sensorFlag, Integer sensorNo, Integer reissue) throws Exception {
        JSONObject result = new JSONObject();
        JSONArray oilConsumptionChartDataArr = new JSONArray();
        List<Integer> monitorBandOilExpendSensorNoList = sensorSettingsDao.getMonitorBandOilExpendSensorNo(vehicleId);
        List<Integer> sensorNoList =
            monitorBandOilExpendSensorNoList.stream().map(no -> no - 68).collect(Collectors.toList());
        // 是否有绑定传感器
        if (CollectionUtils.isEmpty(sensorNoList)) {
            sensorNo = null;
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
        List<Positional> positionalList =
            getCachePositionalInfoList(vehicleId, startTime, endTime, sensorFlag, reissue);
        if (CollectionUtils.isNotEmpty(positionalList)) {
            for (Positional positional : positionalList) {
                JSONObject positionalJsonObj = new JSONObject();
                positionalJsonObj.put("time", positional.getTime());
                positionalJsonObj.put("oilWear", sensorNo == null ? null :
                    (sensorNo == 1 ? positional.getTotalOilwearOne() : positional.getTotalOilwearTwo()));
                Double mileageTotal = positional.getMileageTotal();
                mileageTotal = Optional.ofNullable(mileageTotal).orElse(0.0);
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
     * 获得存储在redis中的位置缓存信息,如果没有查询hbase
     */
    private List<Positional> getCachePositionalInfoList(String vehicleId, String startTime, String endTime,
        Integer sensorFlag, Integer reissue) throws Exception {
        // 根据用户名获取用户id
        String userUuid = userService.getCurrentUserUuid();
        RedisKey trackPlaybackBaseDataKey =
            HistoryRedisKeyEnum.LKYW_TRACK_PLAYBACK_BASE_DATA_SUFFIX_KEY.of(userUuid, vehicleId);
        List<Positional> positionalList = new ArrayList<>();
        if (RedisHelper.isContainsKey(trackPlaybackBaseDataKey)) {
            String historyDataJsonCompressStr = RedisHelper.getString(trackPlaybackBaseDataKey);
            if (StringUtils.isNotBlank(historyDataJsonCompressStr)) {
                positionalList = JSON.parseArray(
                    ZipUtil.uncompress(historyDataJsonCompressStr.getBytes(StandardCharsets.ISO_8859_1), "utf-8"),
                    Positional.class);
            }
        } else {
            positionalList =
                queryHistoryDataAndSaveRedis(vehicleId, startTime, endTime, sensorFlag, trackPlaybackBaseDataKey, null);
        }
        return positionalList;
    }

    @Override
    public JsonResultBean getTemperatureChartData(String vehicleId, String startTime, String endTime,
        Integer sensorFlag, Integer sensorNo) throws Exception {
        JSONObject result = new JSONObject();
        JSONArray temperatureChartDataArr = new JSONArray();
        // 监控对象绑定的传感器信息
        List<SensorSettingInfo> monitorBandSensorInfoBySensorType =
            sensorSettingsDao.getMonitorBandSensorInfoBySensorType(vehicleId, 1);
        // 传感器序号
        List<Integer> sensorNoList =
            monitorBandSensorInfoBySensorType.stream().map(info -> Integer.parseInt(info.getSensorOutId(), 16) - 32)
                .collect(Collectors.toList());
        SensorSettingInfo sensorSettingInfo = null;
        if (CollectionUtils.isEmpty(sensorNoList)) {
            sensorNo = null;
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
        // 位置信息
        List<Positional> positionalList = getCachePositionalInfoList(vehicleId, startTime, endTime, sensorFlag, null);
        if (CollectionUtils.isNotEmpty(positionalList)) {
            for (Positional positional : positionalList) {
                JSONObject positionalJsonObj = new JSONObject();
                //时间
                positionalJsonObj.put("time", positional.getTime());
                Integer tempValue = getTemperatureBySensorNo(sensorNo, positional);
                Double temperature = tempValue != null ? tempValue * 1.0 / 10.0 : null;
                //温度
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
        // 最高值
        result.put("maxTemperature", maxTemperature);
        // 最低值
        result.put("minTemperature", minTemperature);
        // 高阈值
        result.put("highTemperatureThreshold", sensorSettingInfo == null ? null : sensorSettingInfo.getAlarmUp());
        // 低阈值
        result.put("lowTemperatureThreshold", sensorSettingInfo == null ? null : sensorSettingInfo.getAlarmDown());
        return new JsonResultBean(result);
    }

    /**
     * 通过传感器序号获得温度值
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
     * 通过传感器序号获得载重值
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
     * 通过传感器序号获得湿度值
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
        // 监控对象绑定的传感器信息
        List<SensorSettingInfo> monitorBandSensorInfoBySensorType =
            sensorSettingsDao.getMonitorBandSensorInfoBySensorType(vehicleId, 2);
        Double maxHumidity = null;
        Double minHumidity = null;
        // 传感器序号
        List<Integer> sensorNoList =
            monitorBandSensorInfoBySensorType.stream().map(info -> Integer.parseInt(info.getSensorOutId(), 16) - 37)
                .collect(Collectors.toList());
        SensorSettingInfo sensorSettingInfo = null;
        if (CollectionUtils.isEmpty(sensorNoList)) {
            sensorNo = null;
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
        // 位置信息
        List<Positional> positionalList = getCachePositionalInfoList(vehicleId, startTime, endTime, sensorFlag, null);
        if (CollectionUtils.isNotEmpty(positionalList)) {
            for (Positional positional : positionalList) {
                JSONObject positionalJsonObj = new JSONObject();
                Integer wetnessValue = getHumidityBySensorNo(sensorNo, positional);
                Double humidity = wetnessValue != null ? wetnessValue * 1.0 : null;
                //湿度
                positionalJsonObj.put("humidity", humidity);
                //时间
                positionalJsonObj.put("time", positional.getTime());
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
        // 最高值
        result.put("maxHumidity", maxHumidity);
        // 最低值
        result.put("minHumidity", minHumidity);
        // 高阈值
        result.put("highHumidityThreshold", sensorSettingInfo == null ? null : sensorSettingInfo.getAlarmUp());
        // 低阈值
        result.put("lowHumidityThreshold", sensorSettingInfo == null ? null : sensorSettingInfo.getAlarmDown());
        return new JsonResultBean(result);
    }

    @Override
    public JsonResultBean getWorkHourChartData(String vehicleId, String startTime, String endTime, Integer sensorFlag,
        Integer sensorNo) throws Exception {
        JSONObject result = new JSONObject();
        JSONArray workHourChartDataArr = new JSONArray();
        // 监控对象绑定的传感器信息
        List<SensorSettingInfo> monitorBandSensorInfoBySensorType =
            sensorSettingsDao.getMonitorBandSensorInfoBySensorType(vehicleId, 4);
        // 传感器序号
        List<Integer> sensorNoList =
            monitorBandSensorInfoBySensorType.stream().map(info -> Integer.parseInt(info.getSensorOutId(), 16) - 127)
                .collect(Collectors.toList());
        SensorSettingInfo sensorSettingInfo = null;
        if (CollectionUtils.isEmpty(sensorNoList)) {
            sensorNo = null;
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
        // 工时检测方式 1:电压比较式;2:油耗阈值式;3:油耗波动式; mysql传感器设置的检测方式比传感器上传的要大1
        Integer detectionMode = sensorSettingInfo == null ? null : sensorSettingInfo.getDetectionMode();
        // 有效工时
        long workDuration = 0L;
        // 待机工时
        long standByDuration = 0L;
        // 停机工时
        long haltDuration = 0L;
        // 位置信息
        List<Positional> positionalList = getCachePositionalInfoList(vehicleId, startTime, endTime, sensorFlag, null);
        if (CollectionUtils.isNotEmpty(positionalList)) {
            // 组装工时数据 油耗波动式需要重新计算工作状态
            JSONArray installWorkHourData = installWorkHourData(sensorNo, sensorSettingInfo, positionalList, true);
            if (installWorkHourData.size() > 0) {
                JSONObject zeroJsonObj = installWorkHourData.getJSONObject(0);
                // 状态 1:正常数据; 2:无传感器数据; 3:无数据;
                Integer zeroType = zeroJsonObj.getInteger("type");
                // 工作状态 0:停机; 1:工作; 2:待机;
                Integer zeroWorkingPosition = zeroJsonObj.getInteger("workingPosition");
                // 时长状态 -1:无传感器数据; 0:停机; 1:工作; 2:待机;
                int oldState = zeroType == 2 ? -1 : zeroWorkingPosition;
                int workStateStartIndex = 0;
                for (int i = 0, len = installWorkHourData.size(); i < len; i++) {
                    JSONObject workHourInfoJsonObj = installWorkHourData.getJSONObject(i);
                    Long nowTime = workHourInfoJsonObj.getLong("timeL");
                    workHourInfoJsonObj.put("time", nowTime);
                    // 状态 1:正常数据; 2:无传感器数据; 3:无数据;
                    Integer type = workHourInfoJsonObj.getInteger("type");
                    // 工作状态 0:停机; 1:工作; 2:待机;
                    Integer workingPosition = workHourInfoJsonObj.getInteger("workingPosition");
                    int nowState = type == 2 ? -1 : workingPosition;
                    int previousIndex = Math.max(i - 1, 0);
                    JSONObject previousWorkHourInfoJsonObj = installWorkHourData.getJSONObject(previousIndex);
                    Long previousTime = previousWorkHourInfoJsonObj.getLong("timeL");
                    // 前后两点时间间隔
                    long timeInterval = nowTime - previousTime;
                    // 是否需要添加空白数据
                    boolean isNeedAddNullData = timeInterval > 300;
                    // 如果前后状态不一致或者数据相差大于300s或者是最后一个点 计算时长
                    if (isNeedAddNullData || !Objects.equals(nowState, oldState) || i == len - 1) {
                        JSONObject workStateStartJsonObj = installWorkHourData.getJSONObject(workStateStartIndex);
                        Long workStateStartTime = workStateStartJsonObj.getLong("timeL");
                        //状态持续时长
                        long duration = previousTime - workStateStartTime;
                        duration = timeInterval <= 300 ? nowTime - workStateStartTime : duration;
                        // 停机
                        if (Objects.equals(oldState, 0)) {
                            haltDuration += duration;
                            // 工作
                        } else if (Objects.equals(oldState, 1)) {
                            workDuration += duration;
                            // 待机
                        } else if (Objects.equals(oldState, 2)) {
                            standByDuration += duration;
                        }
                        workStateStartIndex = i;
                        oldState = nowState;
                        //添加空白数据 经前端确认 前端自己添加空白数据
                    }
                    workHourChartDataArr.add(workHourInfoJsonObj);
                }
            }
        }
        // 工时检测方式
        result.put("workInspectionMethod", detectionMode);
        // 阈值
        result.put("thresholdValue", Objects.equals(detectionMode, 1) ? sensorSettingInfo.getThresholdVoltage() :
            Objects.equals(detectionMode, 2) ? sensorSettingInfo.getThreshold() : null);
        // 有效工时
        result.put("workDuration", workDuration);
        // 待机工时
        result.put("standByDuration", standByDuration);
        // 停机工时
        result.put("haltDuration", haltDuration);
        // 工时数据
        result.put("workHourInfo", workHourChartDataArr);
        return new JsonResultBean(result);
    }

    /**
     * 组装工时数据
     * 如果工时检测方式是油耗波动式 需要重新计算工作状态
     * @param isNeedDistinguishSensorNo 是否需要区分传感器序号
     */
    public JSONArray installWorkHourData(Integer sensorNo, SensorSettingInfo sensorSettingInfo,
        List<Positional> positionalList, boolean isNeedDistinguishSensorNo) {
        JSONArray workHourChartDataArr = new JSONArray();
        boolean sensorSettingInfoIsNull = sensorSettingInfo == null;
        // 工时检测方式 1:电压比较式;2:油耗阈值式;3:油耗波动式; mysql传感器设置的检测方式比传感器上传的要大1
        Integer detectionMode = sensorSettingInfoIsNull ? null : sensorSettingInfo.getDetectionMode() - 1;
        // 速度阈值（km/h） 油耗波动式
        Double speedThreshold = sensorSettingInfoIsNull ? null : sensorSettingInfo.getSpeedThreshold();
        // 波动率阈值
        Double baudRateThreshold = sensorSettingInfoIsNull ? null : sensorSettingInfo.getBaudRateThreshold();
        // 波动率计算个数
        Integer baudRateCalculateNumber =
            sensorSettingInfoIsNull ? null : sensorSettingInfo.getBaudRateCalculateNumber();
        // List<Positional> filterList = positionalList.stream()
        //     .filter(info -> filterDataByDetectionMode(info, detectionMode, sensorNo, isNeedDistinguishSensorNo))
        //     .collect(Collectors.toList());
        //需要设置为待机的数量
        Integer needSetStandbyNum = 0;
        // 倒叙遍历
        for (int i = positionalList.size() - 1; i >= 0; i--) {
            JSONObject workHourInfoJsonObj = new JSONObject();
            Positional positional = positionalList.get(i);
            // 工作状态 工时检测方式为油耗波动式时 状态为工作
            Integer workingPosition =
                getWorkingPosition(sensorNo, isNeedDistinguishSensorNo, detectionMode, positional);
            //检测数据
            Double checkData = getCheckData(sensorNo, isNeedDistinguishSensorNo, positional);
            // 工时检测方式
            Integer workInspectionMethod = getWorkInspectionMethod(sensorNo, isNeedDistinguishSensorNo, positional);
            // 状态 1:正常数据; 2:无传感器数据; 3:无数据;
            Integer type = workInspectionMethod == null || !Objects.equals(workInspectionMethod, detectionMode) ? 2 : 1;
            if (Objects.equals(2, workInspectionMethod) && Objects.equals(2, detectionMode)) {
                // 速度
                String speed = positional.getSpeed();
                // 波动值(方差)
                Double fluctuateValue = getFluctuateValue(sensorNo, isNeedDistinguishSensorNo, positional);
                // 当需要设置为待机的数量的个数为0 并且 当S<A时，则包括该点在内左边的N个点的状态为待机。
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
                    //当两条数据vTime相差 300s 时,需要添加空白数据,所以待机状态要重新计算
                    if (positional.getTime() - previousPositional.getTime() > 300) {
                        needSetStandbyNum = 0;
                    }
                }
                //当速度大于速度阈值时工作状态为工作
                if (speed != null && speedThreshold != null && Double.parseDouble(speed) > speedThreshold) {
                    workingPosition = WORK_STATE;
                }
                //当传感器瞬时流量值为0时, 工作状态为停机并且重新计算工作状态
                if (Objects.equals(checkData, 0.0)) {
                    workingPosition = HALT_STATE;
                    needSetStandbyNum = 0;
                }
                //当传感器的波动值为0并且瞬时流量大于5时 重新计算工作状态 并且设置当前点的工作状态为工作
                if (Objects.equals(fluctuateValue, 0.0) && checkData > 5) {
                    Integer beforeIndex = i - 1 >= 0 ? i - 1 : null;
                    if (beforeIndex == null) {
                        workingPosition = WORK_STATE;
                        needSetStandbyNum = 0;
                    } else {
                        Positional previousPositional = positionalList.get(beforeIndex);
                        Double beforeCheckData =
                            getBeforeCheckData(sensorNo, isNeedDistinguishSensorNo, positional, previousPositional);
                        Integer beforeWorkInspectionMethod =
                            getBeforeWorkInspectionMethod(sensorNo, isNeedDistinguishSensorNo, positional,
                                previousPositional);
                        //如果前一个点和当前点的瞬时流量不相等或者前一个点为无效数据设置为工作状态,如果相等保持之前的状态不变;
                        if (beforeWorkInspectionMethod == null || !Objects.equals(checkData, beforeCheckData)) {
                            workingPosition = WORK_STATE;
                            needSetStandbyNum = 0;
                        }
                    }
                }
                workHourInfoJsonObj.put("speed", speed);
            }
            workHourInfoJsonObj
                .put("time", new SimpleDateFormat(DATE_FORMAT).format(new Date(positional.getTime() * 1000)));
            workHourInfoJsonObj.put("timeL", positional.getTime());
            workHourInfoJsonObj.put("checkData", checkData);
            workHourInfoJsonObj.put("workingPosition", workingPosition);
            workHourInfoJsonObj.put("type", type);
            workHourChartDataArr.add(0, workHourInfoJsonObj);
        }
        return workHourChartDataArr;
    }

    /**
     * 表达式替换：原表达式如下所示
     * Integer beforeWorkInspectionMethod = sensorNo == null ? null : isNeedDistinguishSensorNo ?
     * (sensorNo == 1 ? previousPositional.getWorkInspectionMethodOne() :
     * previousPositional.getWorkInspectionMethodTwo()) : positional.getWorkInspectionMethod();
     * @param sensorNo
     * @param isNeedDistinguishSensorNo
     * @param positional
     * @param previousPositional
     * @return
     */
    private Integer getBeforeWorkInspectionMethod(Integer sensorNo, boolean isNeedDistinguishSensorNo,
        Positional positional, Positional previousPositional) {
        if (sensorNo == null) {
            return null;
        }
        if (!isNeedDistinguishSensorNo) {
            return positional.getWorkInspectionMethod();
        }
        if (sensorNo.intValue() == 1) {
            return previousPositional.getWorkInspectionMethodOne();
        }
        return previousPositional.getWorkInspectionMethodTwo();
    }

    /**
     * 表达式替换：原表达式如下所示
     * Double beforeCheckData = sensorNo == null ? null : isNeedDistinguishSensorNo ?
     * (sensorNo == 1 ? previousPositional.getCheckDataOne() :
     * previousPositional.getCheckDataTwo()) : positional.getCheckData();
     * @param sensorNo
     * @param isNeedDistinguishSensorNo
     * @param positional
     * @param previousPositional
     * @return
     */
    private Double getBeforeCheckData(Integer sensorNo, boolean isNeedDistinguishSensorNo, Positional positional,
        Positional previousPositional) {
        if (sensorNo == null) {
            return null;
        }
        if (!isNeedDistinguishSensorNo) {
            return positional.getCheckData();
        }
        if (sensorNo == 1) {
            return previousPositional.getCheckDataOne();
        }
        return previousPositional.getCheckDataTwo();
    }

    /**
     * 表达式替换：原表达式如下所示
     * Double fluctuateValue= sensorNo == null ? null : isNeedDistinguishSensorNo ?
     * (sensorNo == 1 ? positional.getFluctuateValueOne() : positional.getFluctuateValueTwo()) :
     * positional.getFluctuateValue();
     * @param sensorNo
     * @param isNeedDistinguishSensorNo
     * @param positional
     * @return
     */
    private Double getFluctuateValue(Integer sensorNo, boolean isNeedDistinguishSensorNo, Positional positional) {
        if (sensorNo == null) {
            return null;
        }
        if (!isNeedDistinguishSensorNo) {
            return positional.getFluctuateValue();
        }
        if (sensorNo.intValue() == 1) {
            return positional.getFluctuateValueOne();
        }
        return positional.getFluctuateValueTwo();
    }

    /**
     * 表达式替换：原表达式如下所示
     * Integer workInspectionMethod = sensorNo == null ? null : isNeedDistinguishSensorNo ?
     * (sensorNo == 1 ? positional.getWorkInspectionMethodOne() : positional.getWorkInspectionMethodTwo()) :
     * positional.getWorkInspectionMethod();
     * @param sensorNo
     * @param isNeedDistinguishSensorNo
     * @param positional
     * @return
     */
    private Integer getWorkInspectionMethod(Integer sensorNo, boolean isNeedDistinguishSensorNo,
        Positional positional) {
        if (sensorNo == null) {
            return null;
        }
        if (!isNeedDistinguishSensorNo) {
            return positional.getWorkInspectionMethod();
        }
        if (sensorNo.intValue() == 1) {
            return positional.getWorkInspectionMethodOne();
        }
        return positional.getWorkInspectionMethodTwo();
    }

    /**
     * 表达式替换：原表达式如下所示
     * Double checkData = sensorNo == null ? null : isNeedDistinguishSensorNo
     * ? (sensorNo == 1 ? positional.getCheckDataOne() : positional.getCheckDataTwo()) :
     * positional.getCheckData();
     * @param sensorNo
     * @param isNeedDistinguishSensorNo
     * @param positional
     * @return
     */
    private Double getCheckData(Integer sensorNo, boolean isNeedDistinguishSensorNo, Positional positional) {
        if (sensorNo == null) {
            return null;
        }
        if (!isNeedDistinguishSensorNo) {
            return positional.getCheckData();

        }
        if (sensorNo.intValue() == 1) {
            return positional.getCheckDataOne();
        }

        return positional.getCheckDataTwo();

    }

    /**
     * 表达式替换：原表达式如下所示
     * Integer workingPosition = detectionMode == null ? null : detectionMode == 2 ? WORK_STATE :
     * (isNeedDistinguishSensorNo
     * ? (sensorNo == 1 ? positional.getWorkingPositionOne() : positional.getWorkingPosition()) :
     * positional.getWorkingPosition());
     * @param sensorNo
     * @param isNeedDistinguishSensorNo
     * @param detectionMode
     * @param positional
     * @return
     */
    private Integer getWorkingPosition(Integer sensorNo, boolean isNeedDistinguishSensorNo, Integer detectionMode,
        Positional positional) {

        if (detectionMode == null) {
            return null;
        }
        if (detectionMode.intValue() == 2) {
            return WORK_STATE;
        }

        if (!isNeedDistinguishSensorNo) {
            return positional.getWorkingPosition();
        }
        if (sensorNo.intValue() == 1) {
            return positional.getWorkingPositionOne();
        }
        return positional.getWorkingPosition();
    }

    /**
     * 当缓存中没有存储的历史数据时,重新查询并存储redis;
     */
    private List<Positional> queryHistoryDataAndSaveRedis(String monitorId, String startTime, String endTime,
        Integer sensorFlag, RedisKey trackPlaybackBaseDataKey, Integer reissue) throws Exception {
        boolean flogKey = RedisHelper.isContainsKey(HistoryRedisKeyEnum.SENSOR_MESSAGE.of(monitorId));
        if (sensorFlag != null) {
            flogKey = sensorFlag == 1;
        }
        List<Positional> list = getHistoryData(monitorId, startTime, endTime, flogKey, reissue);
        String historyDataJsonCompressStr = ZipUtil.compress(JSON.toJSONString(list));
        RedisHelper.setString(trackPlaybackBaseDataKey, historyDataJsonCompressStr, 2 * 60 * 60);
        return list;
    }

    @Override
    public JsonResultBean getMonitorHistoryData(String vehicleId, String startTime, String endTime, Integer sensorFlag,
        String ip, Integer reissue) throws Exception {
        ls.addLog(vehicleId, 0, ip);
        // 根据用户名获取用户id
        String userUuid = userService.getCurrentUserUuid();
        RedisKey trackPlaybackBaseDataKey =
            HistoryRedisKeyEnum.LKYW_TRACK_PLAYBACK_BASE_DATA_SUFFIX_KEY.of(userUuid, vehicleId);
        TrackBackUtil.removeRedisData(trackPlaybackBaseDataKey);
        // 获取超长待机信息
        String functionalType = TrackBackUtil.getfunctionalType(vehicleId);
        // 判断监控对象是否绑定里程传感器
        boolean flogKey = RedisHelper.isContainsKey(HistoryRedisKeyEnum.SENSOR_MESSAGE.of(vehicleId));
        // 判断是否为离线数据,是则取当天是否绑定传感器的标识
        if (sensorFlag != null) {
            flogKey = sensorFlag == 1;
        }
        List<Positional> list = getHistoryData(vehicleId, startTime, endTime, flogKey, reissue);
        String historyDataJsonCompressStr = ZipUtil.compress(JSON.toJSONString(list));
        TrackBackUtil.addResultToRedis(trackPlaybackBaseDataKey, historyDataJsonCompressStr);
        JSONObject msg = new JSONObject();
        // 全部数据
        msg.put("allData", historyDataJsonCompressStr);
        // 超长待机类型专用
        msg.put("type", functionalType);
        // 当前用户的车辆分组
        msg.put("groups", realTime.getGroups(vehicleId));
        //是否绑定里程传感器
        msg.put("nowFlogKey", flogKey);
        return new JsonResultBean(msg);
    }

    @Override
    public JsonResultBean getMonitorObdData(String monitorId, String startTime, String endTime, Integer sensorFlag)
        throws Exception {
        JSONArray result = new JSONArray();
        JSONObject msg = new JSONObject();
        msg.put("isObdSet", "yes");
        msg.put("result", result);
        BindDTO bindInfo = VehicleUtil.getBindInfoByRedis(monitorId);
        // 非OBD协议且未设置obd车型的直接返回空数据
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
        String userUuid = userService.getCurrentUserUuid();
        RedisKey redisKey = HistoryRedisKeyEnum.LKYW_TRACK_PLAYBACK_OBD_SUFFIX_KEY.of(userUuid, monitorId);
        // 移除Redis数据
        TrackBackUtil.removeRedisData(redisKey);
        // 位置信息
        List<Positional> positionalList = getCachePositionalInfoList(monitorId, startTime, endTime, sensorFlag, null);
        String groupName = Optional.ofNullable(bindInfo).map(MonitorBaseDTO::getOrgName).orElse("");

        if (CollectionUtils.isNotEmpty(positionalList)) {
            //组装obd数据
            for (Positional positional : positionalList) {
                OBDVehicleDataInfo info = new OBDVehicleDataInfo();
                info.setVtime(positional.getTime());
                info.setPlateNumber(positional.getMonitorName());
                info.setObdOriginalVehicleData(ObjectUtils.firstNonNull(
                        positional.getObdOriginalVehicleData(), positional.getObdObj()));
                info.setUploadtime(positional.getUploadTime());
                info.setGroupName(groupName);
                obdVehicleTypeService.installObdInfo(info);
                result.add(info);
            }
        }
        msg.put("result", result);
        String msgResult = JSON.toJSONString(msg, SerializerFeature.DisableCircularReferenceDetect);
        msgResult = ZipUtil.compress(msgResult);
        TrackBackUtil.addResultToRedis(redisKey, msgResult);
        return new JsonResultBean(msgResult);
    }

    /**
     * 获得历史数据
     */
    private List<Positional> getHistoryData(String monitorId, String startTime, String endTime, boolean flogKey,
        Integer reissue) throws Exception {
        Positional positional;
        Map<String, String> queryParam = Maps.newHashMap();
        queryParam.put("monitorId", monitorId);
        queryParam.put("startTime", startTime);
        queryParam.put("endTime", endTime);
        if (Objects.nonNull(reissue)) {
            queryParam.put("reissueFlag", String.valueOf(reissue));
        }
        String str = HttpClientUtil.send(PaasCloudUrlEnum.POSITIONAL_HISTORY_URL, queryParam);
        PaasCloudZipDTO dto = null;
        if (!StringUtils.isEmpty(str)) {
            dto = JSON.parseObject(str, PaasCloudZipDTO.class);
        }
        if (dto == null) {
            return new ArrayList<>();
        }
        String data = dto.getData();
        if (StringUtils.isEmpty(data)) {
            return new ArrayList<>();
        }
        /*String locationUncompress =
            ZipUtil.uncompress(data.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8.toString());
        if ("null".equals(locationUncompress) || StringUtils.isEmpty(locationUncompress)) {
            return new ArrayList<>();
        }*/
        List<Positional> positionals = JSON.parseArray(data, Positional.class);
        if (CollectionUtils.isEmpty(positionals)) {
            return new ArrayList<>();
        }
        List<Positional> sortList =
            positionals.stream().sorted(Comparator.comparingLong(Positional::getTime)).collect(Collectors.toList());
        // 查询车辆信息
        Map<String, String> monitorIco = monitorIconService.getByMonitorId(Collections.singleton(monitorId));
        BindDTO bindInfo = VehicleUtil.getBindInfoByRedis(monitorId);
        String plateColor = PlateColor.getNameOrBlankByCode(String.valueOf(bindInfo.getPlateColor()));
        String simCard = bindInfo.getSimCardNumber();
        String ico = monitorIco.get(monitorId);
        // 位置信息异常数据过滤
        CommonUtil.positionalInfoAbnormalFilterNew(sortList, filterFlag);
        // 遍历集合,获取每一条历史轨迹
        int listSize = sortList.size();
        positional = sortList.get(0);
        Integer totalTire;
        //通过车辆传感器关联表数据获取车辆轮胎总数
        List<SensorSettingInfo> monitorBandSensorInfoBySensorType =
            sensorSettingsDao.getMonitorBandSensorInfoBySensorType(monitorId, 7);
        if (CollectionUtils.isEmpty(monitorBandSensorInfoBySensorType)) {
            totalTire = 0;
        } else {
            SensorSettingInfo sensorSettingInfo = monitorBandSensorInfoBySensorType.get(0);
            totalTire = sensorSettingInfo.getNumberOfTires();
        }
        // 初始行驶状态
        TrackBackUtil.initDrivingState(flogKey, positional);
        for (int i = 0; i < listSize; i++) {
            positional = sortList.get(i);
            positional.setPlateColor(plateColor);
            positional.setPlateNumber(positional.getMonitorName());
            positional.setVtime(positional.getTime());
            positional.setSimCard(simCard);
            positional.setIco(ico);
            positional.setAcc(TrackBackUtil.getAccAndStatus(positional.getStatus(), 0x1));
            positional.setLocationStatus(TrackBackUtil.getAccAndStatus(positional.getStatus(), 0x2));
            positional.setTotalTireNum(totalTire);
            if (StringUtils.isBlank(positional.getLongtitude())) {
                positional.setLongtitude("0.0");
            }
            if (StringUtils.isBlank(positional.getLatitude())) {
                positional.setLatitude("0.0");
            }
            if (i > 0) {
                // 前一个点
                Positional previousPositional = sortList.get(i - 1);
                // 如果连个点的时间差大于5分钟 行驶状态重新初始化
                if (positional.getTime() - previousPositional.getTime() > 300) {
                    // 初始行驶状态
                    TrackBackUtil.initDrivingState(flogKey, positional);
                }
            }
            //计算行驶状态
            TrackBackUtil.calculateDrivingStatus(positional, sortList, flogKey, listSize, i);
        }
        return sortList;
    }

    @Override
    public List<HistoryMileAndSpeed> getMileSpeedData(TrackPlayBackChartDataQuery query) throws Exception {
        List<HistoryMileAndSpeed> locationMileAndSpeedData = new ArrayList<>();
        // 获取监控对象缓存的位置数据,获取里程速度数据
        List<Positional> locationInfo =
            getCachePositionalInfoList(query.getMonitorId(), query.getStartTime(), query.getEndTime(),
                query.getSensorFlag(), null);
        if (CollectionUtils.isEmpty(locationInfo)) {
            return locationMileAndSpeedData;
        }
        boolean flogKey = getMonitorBindSensorMessageStatus(query.getMonitorId(), query.getSensorFlag());
        locationMileAndSpeedData = locationInfo.stream().map((e) -> {
            double mileage;
            double speed;
            if (flogKey) { // 监控对象绑定里程传感器
                mileage = e.getMileageTotal() == null ? 0.0 : e.getMileageTotal();
                speed = e.getMileageSpeed() == null ? 0.0 : e.getMileageSpeed();
            } else {
                mileage = e.getGpsMile() == null ? 0.0 : Double.parseDouble(e.getGpsMile());
                speed = e.getSpeed() == null ? 0.0 : Double.parseDouble(e.getSpeed());
            }
            HistoryMileAndSpeed data = new HistoryMileAndSpeed();
            data.setTime(e.getTime());
            data.setMileage(mileage);
            data.setSpeed(speed);
            return data;
        }).collect(Collectors.toList());
        return locationMileAndSpeedData;
    }

    /**
     * 获取监控对象绑定里程传感器的状态
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
        List<Positional> locationInfo =
            getCachePositionalInfoList(query.getMonitorId(), query.getStartTime(), query.getEndTime(),
                query.getSensorFlag(), null);
        // 获取监控对象缓存的位置数据
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
            data.setTime(e.getTime());
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
        // 判断监控对象是否绑定了油箱
        int monitorOilBoxNumber = oilCalibrationDao.checkIsBondOilBox(query.getMonitorId());
        if (monitorOilBoxNumber == 0) { // 监控对象绑定油量数量为0
            return new JsonResultBean(JsonResultBean.FAULT, "监控对象没有绑定油量传感器");
        }
        List<Positional> locationInfo =
            getCachePositionalInfoList(query.getMonitorId(), query.getStartTime(), query.getEndTime(),
                query.getSensorFlag(), null);
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
            oilMass.setTime(p.getTime());
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
     * 获取外设轮询列表
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
        // 判断监控对象是否绑定了油箱
        // 监控对象绑定的传感器信息
        List<SensorSettingInfo> monitorBandSensorInfoBySensorType =
            sensorSettingsDao.getMonitorBandSensorInfoBySensorType(monitorId, 3);
        if (CollectionUtils.isEmpty(monitorBandSensorInfoBySensorType)) {
            return new JsonResultBean(JsonResultBean.FAULT, "监控对象没有绑定正反转传感器!");
        }
        // 位置信息
        List<Positional> positionalList = getCachePositionalInfoList(monitorId, startTime, endTime, sensorFlag, null);
        if (CollectionUtils.isNotEmpty(positionalList)) {
            for (Positional positional : positionalList) {
                WinchInfo info = new WinchInfo();
                info.setTime(String.valueOf(positional.getTime()));
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
        String userUuid = userService.getCurrentUserUuid();
        ExportExcelUtil.setResponseHead(response, query.getTitle());
        // 查询自定义列
        List<String> customColumnList = customColumnService.findCustomColumnTitleList(query.getMark());

        if (CollectionUtils.isEmpty(customColumnList)) {
            logger.error("导出用户自定列异常_【{}】", query.getTitle());
            return;
        }
        query.setCustomColumnList(customColumnList);
        RedisKey baseDataKey;
        switch (tab) {
            case 0:
                // 全部数据
                baseDataKey =
                    HistoryRedisKeyEnum.LKYW_TRACK_PLAYBACK_BASE_DATA_SUFFIX_KEY.of(userUuid, query.getVehicleId());
                exportPositionalFormList(baseDataKey, query, response);
                break;
            case 1:
                // OBD数据
                baseDataKey = HistoryRedisKeyEnum.LKYW_TRACK_PLAYBACK_OBD_SUFFIX_KEY.of(userUuid, query.getVehicleId());
                exportOBDList(baseDataKey, query, response);
                break;
            case 2:
                // 报警数据
                baseDataKey =
                    HistoryRedisKeyEnum.LKYW_TRACK_PLAYBACK_ALARM_SUFFIX_KEY.of(userUuid, query.getVehicleId());
                exportAlarmList(baseDataKey, query, response);
                break;
            case 3:
                //停止数据
                baseDataKey =
                    HistoryRedisKeyEnum.LKYW_TRACK_PLAYBACK_BASE_DATA_SUFFIX_KEY.of(userUuid, query.getVehicleId());
                exportStopList(baseDataKey, query, response);
                break;
            case 4:
                //行驶段报表
                baseDataKey =
                    HistoryRedisKeyEnum.LKYW_TRACK_PLAYBACK_BASE_DATA_SUFFIX_KEY.of(userUuid, query.getVehicleId());
                exportRunList(baseDataKey, query, response);
                break;
            default:
                break;
        }
    }

    private void exportRunList(RedisKey trackPlaybackBaseDataKey, PositionalQuery query, HttpServletResponse response)
        throws Exception {
        if (RedisHelper.isContainsKey(trackPlaybackBaseDataKey)) {
            String positionalStr = RedisHelper.getString(trackPlaybackBaseDataKey);
            if (StringUtils.isEmpty(positionalStr)) {
                logger.info("导出数据为空_【{}】", query.getTitle());
                return;
            }
            String positionalStrUncompress = ZipUtil
                .uncompress(positionalStr.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8.toString());
            List<Positional> positionalFormList = JSON.parseArray(positionalStrUncompress, Positional.class);
            String assignmentName = realTime.getGroups(query.getVehicleId());
            Integer exportLocation = query.getIsExportLocation();
            // 组装返回数据
            List<RunPositional> exportList = new ArrayList<>();
            RunPositional runPositional = null;
            Positional positional;
            Positional nextPositional;
            int positionalFormListSize = positionalFormList.size();
            boolean isFirst = true;
            for (int i = 0; i < positionalFormListSize; i++) {
                // 当前一条位置数据
                positional = positionalFormList.get(i);
                if (!TrackBackUtil.RUNNING_STATE.equals(positional.getDrivingState())) {
                    continue;
                }
                //开始存储
                if (isFirst) {
                    runPositional = runStartExport(assignmentName, exportLocation, positional);
                    isFirst = false;
                }
                if (i + 1 < positionalFormListSize) {
                    nextPositional = positionalFormList.get(i + 1);
                    if (STOP_STATE.equals(nextPositional.getDrivingState())) {
                        if (nextPositional.getTime() - positional.getTime() > 5 * 60) {
                            addRunEndToExportList(runPositional, positional, exportList, exportLocation);
                            calculateUseOil2(runPositional, positional);
                        } else {
                            addRunEndToExportList(runPositional, nextPositional, exportList, exportLocation);
                            calculateUserOil4(runPositional, nextPositional);
                        }
                        isFirst = true;
                        continue;
                    }
                    //判断是否超过5分钟,超过5分钟,才设置行驶段的的结束信息
                    if (TrackBackUtil.RUNNING_STATE.equals(nextPositional.getDrivingState())
                        && nextPositional.getTime() - positional.getTime() > 5 * 60) {
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

    //停止点是下一个点，那么就要将计算自己的油量1，油量2，漏油1，漏油2，加油1，加油2
    private void calculateUserOil4(RunPositional runPositional, Positional nextPositional) {
        //油量油量
        Double oilTankOne = string2Double(nextPositional.getOilTankOne());
        Double oilTankTwo = string2Double(nextPositional.getOilTankTwo());
        //加油量
        Double fuelAmountOne = string2Double(nextPositional.getFuelAmountOne());
        Double fuelAmountTwo = string2Double(nextPositional.getFuelAmountTwo());
        //计算行驶内的加油和漏油数据
        Double fuelSpillOne = string2Double(nextPositional.getFuelSpillOne());
        Double fuelSpillTwo = string2Double(nextPositional.getFuelSpillTwo());
        runPositional.setUseOilTemp(
            runPositional.getUseOilTemp() - oilTankOne - oilTankTwo + fuelAmountOne + fuelAmountTwo - fuelSpillOne
                - fuelSpillTwo);
        runPositional.setUseOil(
            new BigDecimal("" + (runPositional.getUseOilTemp())).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
    }

    // 如果停止点是自己，那么只减去自己的油量1，油量2，因为calculateUseOil3已经将自己加油量，漏油量已计算进去
    private void calculateUseOil2(RunPositional runPositional, Positional positional) {
        Double oilTankOne = string2Double(positional.getOilTankOne());
        Double oilTankTwo = string2Double(positional.getOilTankTwo());
        runPositional.setUseOilTemp(runPositional.getUseOilTemp() - oilTankOne - oilTankTwo);
        runPositional.setUseOil(
            new BigDecimal("" + (runPositional.getUseOilTemp())).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
    }

    //累计加油量，漏油量，从第二个点开始
    private void calculateUseOil3(RunPositional runPositional, Positional nextPositional) {
        //加油量
        Double fuelAmountOne = string2Double(nextPositional.getFuelAmountOne());
        Double fuelAmountTwo = string2Double(nextPositional.getFuelAmountTwo());
        //计算行驶内的漏油数据
        Double fuelSpillOne = string2Double(nextPositional.getFuelSpillOne());
        Double fuelSpillTwo = string2Double(nextPositional.getFuelSpillTwo());
        runPositional
            .setUseOilTemp(runPositional.getUseOilTemp() + fuelAmountOne + fuelAmountTwo - fuelSpillOne - fuelSpillTwo);
    }

    private void addRunEndToExportList(RunPositional runPositional, Positional positional,
        List<RunPositional> exportList, Integer exportLocation) {
        long gpsTime = positional.getTime();
        runPositional.setRunEndTime(LocalDateUtils.dateTimeFormat(new Date(gpsTime * 1000)));
        runPositional.setRunTime(buildStopTime(runPositional.getGpsTime(), gpsTime));
        calculateConsumeOil(runPositional, positional);
        Double mileageTotal = positional.getMileageTotal();
        if (mileageTotal == null) {
            //gps里程，优先使用传感器里程
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
            String positionalStr = RedisHelper.getString(trackPlaybackBaseDataKey);
            if (StringUtils.isEmpty(positionalStr)) {
                logger.info("导出数据为空_【{}】", query.getTitle());
                return;
            }
            String positionalStrUncompress = ZipUtil
                .uncompress(positionalStr.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8.toString());
            List<Positional> positionalFormList = JSON.parseArray(positionalStrUncompress, Positional.class);
            String assignmentName = realTime.getGroups(query.getVehicleId());
            Integer exportLocation = query.getIsExportLocation();
            // 组装返回数据
            List<StopPositional> exportList = new ArrayList<>();
            StopPositional stopPositional = null;
            Positional positional;
            Positional nextPositional;
            int positionalFormListSize = positionalFormList.size();
            boolean isFirst = true;
            for (int i = 0; i < positionalFormListSize; i++) {
                // 当前一条位置数据
                positional = positionalFormList.get(i);
                if (!TrackBackUtil.STOP_STATE.equals(positional.getDrivingState())) {
                    continue;
                }
                //开始存储
                if (isFirst) {
                    stopPositional = stopStartExport(assignmentName, exportLocation, positional);
                    isFirst = false;
                }
                if (i + 1 < positionalFormListSize) {
                    nextPositional = positionalFormList.get(i + 1);
                    if (TrackBackUtil.RUNNING_STATE.equals(nextPositional.getDrivingState())) {
                        if (nextPositional.getTime() - positional.getTime() > 5 * 60) {
                            addStopEndToExportList(stopPositional, positional, exportList, exportLocation);
                        } else {
                            addStopEndToExportList(stopPositional, nextPositional, exportList, exportLocation);
                        }
                        isFirst = true;
                        continue;
                    }
                    //判断是否超过5分钟
                    if (TrackBackUtil.STOP_STATE.equals(nextPositional.getDrivingState())
                        && nextPositional.getTime() - positional.getTime() > 5 * 60) {
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
        long gpsTime = positional.getTime();
        stopPositional.setStopEndTime(LocalDateUtils.dateTimeFormat(new Date(gpsTime * 1000)));
        stopPositional.setStopTime(buildStopTime(stopPositional.getGpsTime(), gpsTime));
        if (exportLocation == PositionalQuery.EXPORT_LOCATION) {
            String address = positionalService.getAddress(positional.getLongtitude(), positional.getLatitude());
            stopPositional.setStopEndLocation(address);
        }
        exportList.add(stopPositional);
    }

    private String buildStopTime(long gpsT, long gpsTime) {
        StringBuffer stopTime = new StringBuffer();
        long h = (gpsTime - gpsT) / 3600;
        long m = (gpsTime - gpsT) % 3600 / 60;
        long s = (gpsTime - gpsT) % 60;
        if (h != 0) {
            stopTime.append(h + "小时");
        }
        if (m != 0 || (h != 0)) {
            stopTime.append(m + "分");
        }
        stopTime.append(s + "秒");
        return stopTime.toString();

    }

    private StopPositional stopStartExport(String assignmentName, Integer exportLocation, Positional positional) {
        StopPositional stopPositional = new StopPositional();
        stopPositional.setMonitorName(positional.getMonitorName());
        stopPositional.setAssignmentName(assignmentName);
        long gpsTime = positional.getTime();
        stopPositional.setStopStartTime(LocalDateUtils.dateTimeFormat(new Date(gpsTime * 1000)));
        stopPositional.setGpsTime(gpsTime);
        stopPositional.setDeviceNumber(positional.getDeviceNumber());
        stopPositional.setSimcardNumber(positional.getSimCard());
        stopPositional.setAcc(positional.getAcc());
        stopPositional.setLocationType(positional.getLocationType());
        stopPositional.setSatellitesNumber(positional.getSatelliteNumber());
        // 查询位置数据
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
        runPositional.setMonitorName(positional.getMonitorName());
        runPositional.setAssignmentName(assignmentName);
        long gpsTime = positional.getTime();
        runPositional.setRunStartTime(LocalDateUtils.dateTimeFormat(new Date(gpsTime * 1000)));
        runPositional.setGpsTime(gpsTime);
        runPositional.setDeviceNumber(positional.getDeviceNumber());
        runPositional.setSimcardNumber(positional.getSimCard());
        runPositional.setAcc(positional.getAcc());
        runPositional.setLocationType(positional.getLocationType());
        runPositional.setSatellitesNumber(positional.getSatelliteNumber());
        Double mileageTotal = positional.getMileageTotal();
        if (mileageTotal == null) {
            //gps里程，优先使用传感器里程
            mileageTotal = Double.parseDouble(positional.getGpsMile());
        }
        runPositional.setRunMileTemp(mileageTotal);
        //计算油耗
        calculateConsumeOil(runPositional, positional);
        // 计算用油量 该行驶段内第一个点的油量 + 行驶段内的加油量 – 行驶段内漏油量 – 行驶段内最后一个点油量，（主油箱和副油箱之和）
        calculateUseOil1(positional, runPositional);
        // 查询位置数据
        if (exportLocation == PositionalQuery.EXPORT_LOCATION) {
            String longtitude = positional.getLongtitude();
            String latitude = positional.getLatitude();
            String address = positionalService.getAddress(longtitude, latitude);
            runPositional.setRunStartLocation(address);
        }
        return runPositional;
    }

    private void calculateUseOil1(Positional positional, RunPositional runPositional) {
        //油量
        Double oilTankOne = string2Double(positional.getOilTankOne());
        Double oilTankTwo = string2Double(positional.getOilTankTwo());
        //加油量
        Double fuelAmountOne = string2Double(positional.getFuelAmountOne());
        Double fuelAmountTwo = string2Double(positional.getFuelAmountTwo());

        //计算行驶内的加油和漏油数据
        Double fuelSpillOne = string2Double(positional.getFuelSpillOne());
        Double fuelSpillTwo = string2Double(positional.getFuelSpillTwo());
        //第一个点的油量
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
     * 导出报警数据
     * @param baseDataKey redisKey
     * @param query       query
     * @param response    response
     */
    private void exportAlarmList(RedisKey baseDataKey, PositionalQuery query, HttpServletResponse response)
        throws Exception {
        if (RedisHelper.isContainsKey(baseDataKey)) {
            String obdStr = RedisHelper.getString(baseDataKey);
            if (StringUtils.isEmpty(obdStr)) {
                logger.info("导出数据为空_【{}】", query.getTitle());
                return;
            }
            String obdUncompress =
                ZipUtil.uncompress(obdStr.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8.toString());
            List<AlarmData> alarmDataList = JSON.parseArray(obdUncompress, AlarmData.class);

            if (query.getIsExportLocation() == PositionalQuery.EXPORT_LOCATION) {
                alarmDataList.forEach(alarmData -> {
                    // 开始位置
                    alarmData.setStartLocation(getLocation(alarmData.getAlarmStartLocation()));
                    // 结束位置
                    alarmData.setEndLocation(getLocation(alarmData.getAlarmEndLocation()));
                });
            }
            ExportExcelUtil.exportCustomData(
                new ExportExcelParam("", 1, alarmDataList, AlarmData.class, null, response.getOutputStream(),
                    query.getCustomColumnList()));
        }
    }

    /**
     * 获取位置
     * @param startLocation 106.495893,29.532302
     * @return 位置
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
     * 导出OBD数据
     * @param baseDataKey redisKey
     * @param query       query
     * @param response    response
     */
    private void exportOBDList(RedisKey baseDataKey, PositionalQuery query, HttpServletResponse response)
        throws Exception {
        if (RedisHelper.isContainsKey(baseDataKey)) {
            String obdStr = RedisHelper.getString(baseDataKey);
            if (StringUtils.isEmpty(obdStr)) {
                logger.info("导出数据为空_【{}】", query.getTitle());
                return;
            }
            String obdUncompress =
                ZipUtil.uncompress(obdStr.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8.toString());
            JSONObject jsonObject = JSON.parseObject(obdUncompress);
            String result = jsonObject.getString("result");
            List<OBDVehicleDataInfo> obdVehicleDataInfos = JSON.parseArray(result, OBDVehicleDataInfo.class);

            // 数据组装，状态转换
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
            String positionalStr = RedisHelper.getString(trackPlaybackBaseDataKey);
            if (StringUtils.isEmpty(positionalStr)) {
                logger.info("导出数据为空_【{}】", query.getTitle());
                return;
            }
            String positionalStrUncompress = ZipUtil
                .uncompress(positionalStr.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8.toString());
            List<Positional> positionalFormList = JSON.parseArray(positionalStrUncompress, Positional.class);

            String assignmentName = realTime.getGroups(query.getVehicleId());
            Integer flag = query.getFlag();
            Integer exportLocation = query.getIsExportLocation();
            // 组装返回数据
            List<PositionalForm> exportList = new ArrayList<>();

            Positional positional;
            Positional nextPositional;
            int positionalFormListSize = positionalFormList.size();
            int nextIndex;
            PositionalState positionalState = new PositionalState();
            positionalState.setFlag(flag);
            for (int i = 0; i < positionalFormListSize; i++) {
                // 当前一条位置数据
                positional = positionalFormList.get(i);
                // 下一条位置数据
                nextIndex = (i + 1);
                nextPositional = nextIndex < positionalFormListSize ? positionalFormList.get(nextIndex) : null;
                // 当前这条数据状态
                positionalState.setDrivingState(positional.getDrivingState());
                // 下一条位置数据状态
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
            long gpsTime = positional.getTime() * 1000;
            positionalForm = new PositionalForm();
            positionalForm.setPlateNumber(positional.getMonitorName());
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
            int angle = new BigDecimal(positional.getAngle()).intValue();
            positionalForm.setAngle(DeviceMessageHandler.getDirectionStr(angle));
            positionalForm.setGpsMile(positional.getGpsMile());
            positionalForm.setLocationType(positional.getLocationType());
            positionalForm.setSatelliteNumber(positional.getSatelliteNumber());
            positionalForm.setReissue(
                    Optional.ofNullable(positional.getReissue()).map(o -> o.equals(1) ? "补传" : "非补传").orElse(null));
            //老经度，历史原因，无法修改
            String longtitude = positional.getLongtitude();
            //新经度，历史原因，无法修改
            String longitude = positional.getLongitude();
            String lon;
            if (StringUtils.isEmpty(longtitude) || "0.0".equals(longtitude) || "0".equals(longtitude)) {
                lon = longitude;
            } else {
                lon = longtitude;
            }
            String latitude = positional.getLatitude();
            positionalForm.setLongtitude(lon);
            positionalForm.setLatitude(latitude);
            // 查询位置数据
            if (exportLocation == PositionalQuery.EXPORT_LOCATION) {
                // TODO 最好批量查询, 每次查询1000条, 需要验证
                String address = positionalService.getAddress(lon, latitude);
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
         * 第一条停止数据
         */
        private Boolean firstStopFlag = false;

        /**
         * 前一条满足条件的时间
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
     * 判断数据组装
     * @param state state
     * @return boolean
     */
    private boolean isTravelBoolean(PositionalState state) {
        Integer flag = state.getFlag();
        if (flag == 0) {
            return true;
        }

        // 行驶状态
        if (flag == 1 && PositionalForm.DRIVING_STATE.equals(state.getDrivingState())) {
            return true;
        }

        // 是否存在下一条位置数据
        if (StringUtils.isEmpty(state.getNextDrivingState())) {
            return true;
        }

        // 当前这条位置数据为第一条停止数据, 记录当前停止数据
        boolean nextFlag = state.getDrivingState().equals(state.getNextDrivingState());
        if (!state.getFirstStopFlag()) {
            if (nextFlag) {
                // 如果下一条数据与第一条位置数据状态相同
                state.setFirstStopFlag(true);
            }
            return true;
        }

        // 如果当前数据不是第一条停止数据, 则判断当前位置数据的状态和下一条位置数据状态是否相同, 如果不相同, 则记录当前一条数据数据
        if (!nextFlag) {
            state.setFirstStopFlag(false);
            return true;
        }

        return false;
    }

    /**
     * 获取监控对象I/O数据(90 91 92)
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
        if (CollectionUtils.isEmpty(signal)) { // 监控对象不设置终端I/O,不返回任何数据
            return new JsonResultBean(JsonResultBean.FAULT, "监控对象没有绑定I/0");
        }
        // 组装I/O名称
        Map<String, Map<Integer, String>> switchName = switchNameSet(signal, msg, monitorId);
        // 根据监控对象id获取监控对象的I/O设置
        List<Positional> locationInfo =
            getCachePositionalInfoList(query.getMonitorId(), query.getStartTime(), query.getEndTime(),
                query.getSensorFlag(), null);
        if (CollectionUtils.isEmpty(locationInfo)) {
            return new JsonResultBean(msg);
        }
        List<SwitchSignalInfo> resultData = processPositionalInfo(locationInfo, switchName);
        msg.put("data", resultData);
        return new JsonResultBean(msg);
    }

    /**
     * 处理监控对象绑定开关信息
     */
    private Map<String, Map<Integer, String>> switchNameSet(List<SwitchInfo> signal, JSONObject msg, String monitorId) {
        Map<String, Map<Integer, String>> result = new HashMap<>();
        if (CollectionUtils.isEmpty(signal)) {
            return result;
        }
        Map<Integer, String> io90 = new HashMap<>();
        Map<Integer, String> io91 = new HashMap<>();
        Map<Integer, String> io92 = new HashMap<>();
        List<String> switchName = new ArrayList<>(); // 开关名称
        JSONArray ioStatus = new JSONArray();
        List<String> alarmStatusList = new ArrayList<>(); // I/O 报警状态
        Map<Integer, String> monitorIoAlarmSet = getMonitorIoAlarmSetting(monitorId);
        if (MapUtils.isEmpty(monitorIoAlarmSet)) {
            monitorIoAlarmSet = new HashMap<>();
        }
        for (SwitchInfo switchInfo : signal) {
            JSONObject status = new JSONObject();
            // 高电平状态
            status.put("0",
                "1".equals(switchInfo.getHighSignalType()) ? switchInfo.getStateOne() : switchInfo.getStateTwo());
            // 低电平状态
            status.put("1",
                "2".equals(switchInfo.getLowSignalType()) ? switchInfo.getStateTwo() : switchInfo.getStateOne());
            ioStatus.add(status);
            Integer ioSite = switchInfo.getIoSite();
            Integer ioType = switchInfo.getIoType();
            String highSignalType = switchInfo.getHighSignalType();
            if (ioType == 1) { // 终端IO
                io90.put(ioSite, highSignalType);
            } else if (switchInfo.getIoType() == 2) { //IO采集1
                io91.put(ioSite, highSignalType);
            } else if (switchInfo.getIoType() == 3) { //IO采集2
                io92.put(ioSite, highSignalType);
            }
            // 根据I/0类型和I/0位获取对应的报警状态
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
     * 根据I/0类型和I/0位获取对应的报警编号
     */
    private Integer getAlarmStatus(Integer ioType, Integer ioSite) {
        Integer alarmPoss = 0;
        if (ioType == null || ioSite == null) {
            return alarmPoss;
        }
        if (ioType == 1) { // 终端I/O
            alarmPoss = AlarmTypeUtil.IO_0X90_ALARM.get(ioSite);
        }
        if (ioType == 2) { // I/O采集1
            alarmPoss = AlarmTypeUtil.IO_0X91_ALARM.get(ioSite);
        }
        if (ioType == 3) { // I/0采集2
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
     * 处理监控对象开关信号信息
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
            switchSignalInfo.setTime(ps.getTime());
            resultData.add(switchSignalInfo);
        }
        return resultData;
    }

    /**
     * 处理90 I/0 会出现负数的情况
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
     * 位置信息采集板数据解析
     */
    private List<Integer> ioDataDispose(String positionalIoInfo, Map<Integer, String> dataSign) {
        List<Integer> ioStates = new ArrayList<>();
        if (StringUtils.isNotBlank(positionalIoInfo)) {
            // io传感器信息
            JSONObject info = JSON.parseObject(positionalIoInfo);
            if (info.getInteger("unusual") == 1) { // Io异常
                // 有IO异常标识 数据组装为异常
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
                    // 位置数据中没有io状态信息,组装为异常
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
     * 定时定区域导出
     * @param response
     * @param areaListStr
     * @param groupName
     * @param reissue
     * @throws Exception
     */
    @Override
    public void exportTimeZoneTrackPlay(HttpServletResponse response, String areaListStr, String groupName,
        Integer reissue)
        throws Exception {
        ExportExcelUtil.setResponseHead(response, groupName + "定时定区域轨迹查询表");
        //获取区域经纬度信息
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
            HistoryRedisKeyEnum.LKYW_TRACK_PLAYBACK_TIME_ZONE_SUFFIX_KEY.of(SystemHelper.getCurrentUsername());
        Map<String, String> resultMap = RedisHelper.hgetAll(redisKey);
        List<TimeZonePositionalDTO> listOne =
            JSONObject.parseArray(resultMap.get("areaOne"), TimeZonePositionalDTO.class);
        List<TimeZonePositionalDTO> listTwo =
            JSONObject.parseArray(resultMap.get("areaTwo"), TimeZonePositionalDTO.class);
        List<TimeZoneExportForm> exportList = new ArrayList<>();
        if (null != listOne && listOne.size() != 0 && null != areaOne) {
            getTimeZoneDetails(areaOne, listOne, exportList, "区域一", reissue);
        }
        if (null != listTwo && listTwo.size() != 0 && null != areaTwo) {
            getTimeZoneDetails(areaTwo, listTwo, exportList, "区域二", reissue);
        }
        for (int id = 0; id < exportList.size(); id++) {
            exportList.get(id).setId(String.valueOf(id + 1));
        }
        ExportExcelUtil.exportTimeZoneData(
            new ExportExcelParam("", 1, exportList, TimeZoneExportForm.class, null, response.getOutputStream()));
    }

    /**
     * 获取定时定区域详情
     * @param areaInfo
     * @param list
     * @param exportList
     * @param reissue
     */
    private void getTimeZoneDetails(AreaInfo areaInfo, List<TimeZonePositionalDTO> list,
        List<TimeZoneExportForm> exportList, String areaName, Integer reissue) throws Exception {
        for (TimeZonePositionalDTO tzPositional : list) {
            String startTime =
                tzPositional.getStartTime().replaceAll("-", "").replaceAll(":", "").replaceAll("\\s+", "");
            String endTime = tzPositional.getEndTime().replaceAll("-", "").replaceAll(":", "").replaceAll("\\s+", "");
            //获取每个监控对象对应时间段内的所有位置数据
            List<Positional> positionals = getQueryDetails(tzPositional.getMonitorId(), startTime, endTime, reissue);
            boolean flog = true;
            boolean derail = true;
            TimeZoneExportForm form = new TimeZoneExportForm();
            //临时进出区域详情LIST，用于最后添加进出区域次数
            List<TimeZoneExportForm> detailsList = new ArrayList<>();
            //进出区域次数，用于遍历时计算
            int intoAreaNumber = 0;
            int outAreaNumber = 0;
            //遍历位置数据，组装进出区域详情
            if (positionals.size() != 0) {
                for (Positional i : positionals) {
                    String latitude = i.getLatitude();
                    String longitude = i.getLongitude();
                    if (StringUtils.isEmpty(latitude) || StringUtils.isEmpty(longitude)) {
                        continue;
                    }
                    if (Double.valueOf(latitude) <= areaInfo.getRightFloorLatitude()
                        && Double.valueOf(latitude) >= areaInfo.getLeftTopLatitude()
                        && Double.valueOf(longitude) >= areaInfo.getLeftTopLongitude()
                        && Double.valueOf(longitude) <= areaInfo.getRightFloorLongitude()) {
                        if (flog) { // 当天第一条数据就在区域中,或者已经在区域中了，抛弃掉
                            if (derail) {
                                derail = false;// 关闭第一个点的开关
                                getTimeZoneInfo(form, tzPositional, areaName);
                                // intoAreaNumber++;
                                long time = i.getTime() * 1000;// 获取进入区域的时间
                                form.setIntoAreaTime("已在区域内");
                            }
                        } else { // 进区域了
                            getTimeZoneInfo(form, tzPositional, areaName);
                            intoAreaNumber++;
                            long time = i.getTime() * 1000;// 获取进入区域的时间
                            form.setIntoAreaTime(LocalDateUtils.dateTimeFormat(new Date(time)));
                            flog = true;
                        }
                    } else {
                        if (flog) {
                            // 出区域了
                            if (!derail) { // 判断是否是第一个点
                                long time = i.getTime() * 1000;// 获取出区域时间
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
                form.setOutAreaTime("未出区域");
                detailsList.add(form);
            }
            //遍历临时详情，添加进出次数,同时把最终数据依次添加到导出list中
            for (TimeZoneExportForm exportForm : detailsList) {
                exportForm.setOutAreaNumber(String.valueOf(outAreaNumber));
                exportForm.setIntoAreaNumber(String.valueOf(intoAreaNumber));
                exportList.add(exportForm);
            }
        }
    }

    /**
     * 组装定时定区域详情部分数据
     * @param form
     * @param positional
     */
    private void getTimeZoneInfo(TimeZoneExportForm form, TimeZonePositionalDTO positional, String areaName) {
        form.setAreaName(areaName);
        form.setMonitorName(positional.getMonitorName());
        form.setTimeRange(positional.getStartTime() + "--" + positional.getEndTime());
    }

    /**
     * 获取载重数据
     * @param vehicleId  监控对象ID
     * @param startTime  查询开始时间
     * @param endTime    查询结束时间
     * @param sensorFlag 传感器
     * @return json
     * @throws Exception e
     */
    @Override
    public JsonResultBean getLoadWeightDate(String vehicleId, String startTime, String endTime, Integer sensorFlag,
        Integer sensorNo) throws Exception {
        JSONObject result = new JSONObject();
        JSONArray loadWeightChartDataArr = new JSONArray();
        // 监控对象绑定的传感器信息
        List<SensorSettingInfo> monitorBandSensorInfoBySensorType =
            sensorSettingsDao.getMonitorBandSensorInfoBySensorType(vehicleId, 6);
        JSONObject object = null;
        if (CollectionUtils.isEmpty(monitorBandSensorInfoBySensorType)) {
            sensorNo = null;
        } else {
            if (monitorBandSensorInfoBySensorType.size() >= sensorNo) {
                SensorSettingInfo sensorSettingInfo = monitorBandSensorInfoBySensorType.get(sensorNo - 1);
                String individualityParameters = sensorSettingInfo.getIndividualityParameters();
                object = JSONObject.parseObject(individualityParameters);
            }
        }

        Double maxLoadWeight = null;
        Double minLoadWeight = null;
        // 位置信息
        List<Positional> positionalList = getCachePositionalInfoList(vehicleId, startTime, endTime, sensorFlag, null);
        if (CollectionUtils.isNotEmpty(positionalList)) {
            for (Positional positional : positionalList) {
                JSONObject positionalJsonObj = new JSONObject();
                //时间
                positionalJsonObj.put("time", positional.getTime());
                JSONObject loadObj = getLoadWeightBySensorNo(sensorNo, positional);
                //载荷重量
                String loadWeight = loadObj != null ? loadObj.getString("loadWeight") : null;
                //载重状态
                String status = loadObj != null ? loadObj.getString("status") : null;
                //重量单位：0-0.1Kg；1-1kg；2-10kg；3-100kg；
                String unit = loadObj != null ? loadObj.getString("unit") : null;
                //瞬时重量(kg): loadWeight(载荷重量)/unit(重量单位)
                Double instanceWeight = null;
                if (StringUtils.isNotBlank(loadWeight) && StringUtils.isNotBlank(unit)) {
                    //instanceWeight = Double.parseDouble(loadWeight) / Double.parseDouble(unit);
                    instanceWeight = Double.parseDouble(loadWeight);
                }
                //重量
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
        // 最高值
        result.put("maxLoadWeight", maxLoadWeight);
        // 最低值
        result.put("minLoadWeight", minLoadWeight);
        // 满载、轻载、空载、超载阈值
        result.put("fullLoadValue", object == null ? null : object.getString("fullLoadValue"));
        result.put("lightLoadValue", object == null ? null : object.getString("lightLoadValue"));
        result.put("noLoadValue", object == null ? null : object.getString("noLoadValue"));
        result.put("overLoadValue", object == null ? null : object.getString("overLoadValue"));

        return new JsonResultBean(result);
    }

    /**
     * 获取载重数据
     * @param vehicleId  监控对象ID
     * @param startTime  查询开始时间
     * @param endTime    查询结束时间
     * @param sensorFlag 传感器
     * @param tireNum    轮胎编号
     * @return json
     * @throws Exception e
     */
    @Override
    public JsonResultBean getTirePressureData(String vehicleId, String startTime, String endTime, Integer sensorFlag,
        Integer tireNum) throws Exception {
        JSONObject result = new JSONObject();
        JSONArray tirePressureChartDataArr = new JSONArray();
        // 监控对象绑定的传感器信息
        List<SensorSettingInfo> monitorBandSensorInfoBySensorType =
            sensorSettingsDao.getMonitorBandSensorInfoBySensorType(vehicleId, 7);
        JSONObject object;
        if (CollectionUtils.isEmpty(monitorBandSensorInfoBySensorType)) {
            return new JsonResultBean(JsonResultBean.FAULT, "监控对象没有绑定胎压传感器!");
        } else {
            SensorSettingInfo sensorSettingInfo = monitorBandSensorInfoBySensorType.get(0);
            String individualityParameters = sensorSettingInfo.getIndividualityParameters();
            object = JSONObject.parseObject(individualityParameters);
        }
        //最大胎压
        Double maxTirePressure = null;
        //最小胎压
        Double minTirePressure = null;
        // 位置信息
        List<Positional> positionalList = getCachePositionalInfoList(vehicleId, startTime, endTime, sensorFlag, null);
        if (CollectionUtils.isNotEmpty(positionalList)) {
            for (Positional positional : positionalList) {
                JSONObject positionalJsonObj = new JSONObject();
                //时间
                positionalJsonObj.put("time", positional.getTime());
                //胎压数据
                JSONObject tireObj = JSONObject.parseObject(positional.getTirePressureParameter());
                JSONArray list = tireObj != null ? tireObj.getJSONArray("list") : null;
                String pressure = null;
                //某条轮胎的胎压数据
                if (list != null) {
                    positionalJsonObj.put("pressure", null);
                    for (int j = 0; j < list.size(); j++) {
                        JSONObject jo = list.getJSONObject(j);
                        int m = jo.getInteger("number");
                        if (tireNum - 1 == m) {
                            pressure = JSONObject.parseObject(jo.toString()).getString("pressure");
                            positionalJsonObj.put("pressure", pressure);
                            continue;
                        }
                    }
                } else {
                    positionalJsonObj.put("pressure", null);
                }
                tirePressureChartDataArr.add(positionalJsonObj);
                if (pressure == null) {
                    continue;
                }
                Double pressureVal = Double.parseDouble(pressure);
                maxTirePressure = maxTirePressure == null ? pressureVal :
                    (pressureVal > maxTirePressure ? pressureVal : maxTirePressure);
                minTirePressure = minTirePressure == null ? pressureVal :
                    (pressureVal < minTirePressure ? pressureVal : minTirePressure);
            }
        }
        result.put("sensorDataList", tirePressureChartDataArr);
        // 最高值
        result.put("maxTirePressure", maxTirePressure);
        // 最低值
        result.put("minTirePressure", minTirePressure);
        // 低压、高压阈值
        result.put("lowPressure", object == null ? null : object.getString("lowPressure"));
        result.put("heighPressure", object == null ? null : object.getString("heighPressure"));
        return new JsonResultBean(result);
    }
}

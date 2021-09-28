package com.zw.platform.service.singleVehicle.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.domain.VehicleDO;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.repository.NewConfigDao;
import com.zw.platform.basic.repository.NewVehicleDao;
import com.zw.platform.basic.service.MonitorIconService;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.alarm.AlarmInfo;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.basicinfo.enums.PlateColor;
import com.zw.platform.domain.oil.Positional;
import com.zw.platform.domain.reportManagement.PassCloudMileageReport;
import com.zw.platform.domain.reportManagement.TerminalMileageDailyDetails;
import com.zw.platform.domain.singleVehicle.SingleLocationInfo;
import com.zw.platform.domain.singleVehicle.SingleVehicleAcount;
import com.zw.platform.domain.vas.alram.query.SingleVehicleAlarmSearchQuery;
import com.zw.platform.domain.vas.workhourmgt.SensorSettingInfo;
import com.zw.platform.push.redis.RedisVehicleServcie;
import com.zw.platform.repository.vas.SensorSettingsDao;
import com.zw.platform.service.alarm.AlarmSearchService;
import com.zw.platform.service.monitoring.impl.RealTimeServiceImpl;
import com.zw.platform.service.oil.PositionalService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.service.singleVehicle.SingleVehicleService;
import com.zw.platform.util.CommonUtil;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.TrackBackUtil;
import com.zw.platform.util.common.AddressUtil;
import com.zw.platform.util.common.AlarmTypeUtil;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.RedisUtil;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.platform.util.common.ZipUtil;
import com.zw.platform.util.paas.PaasCloudUrlUtil;
import com.zw.platform.util.report.PaasCloudHBaseAccessEnum;
import com.zw.platform.util.report.PaasCloudUrlEnum;
import com.zw.protocol.msg.Message;
import com.zw.protocol.msg.t808.T808Message;
import com.zw.protocol.msg.t808.body.LocationInfo;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.zw.platform.util.DateUtil.YMD_SHORT;

/**
 * 单车登录小程序业务层
 * 2020/05/08
 *
 * @author XK
 */
@Service
@Log4j2
public class SingleVehicleServiceImpl implements SingleVehicleService {

    private static Logger logger = LogManager.getLogger(SingleVehicleServiceImpl.class);

    @Autowired
    private NewVehicleDao newVehicleDao;

    @Autowired
    private NewConfigDao newConfigDao;

    @Autowired
    private PositionalService positionalService;

    @Autowired
    private RedisVehicleServcie redisVehicleServcie;

    @Autowired
    private LogSearchService logSearchServiceImpl;

    @Autowired
    private MonitorIconService monitorIconService;

    @Autowired
    RealTimeServiceImpl realTime;

    @Autowired
    private SensorSettingsDao sensorSettingsDao;

    @Autowired
    private AlarmSearchService alarmSearchService;

    @Value("${positional.info.abnormal.data.filter.flag:false}")
    private boolean filterFlag;


    /**
     * 日期转换格式
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";


    @Override
    public JsonResultBean addLogAndSingleVehicleLogin(String brand, String vehiclePassword,
                                                      HttpServletRequest request) {
        Map<String, String> map = newConfigDao.getConfigByBrand(brand);
        if (MapUtils.isEmpty(map)) {
            return new JsonResultBean(PageGridBean.FAULT, "账号或密码错误，请重新输入");
        }
        String message;
        if (StringUtils.isBlank(map.get("configId"))) {
            return new JsonResultBean(JsonResultBean.FAULT, "账号或密码错误，请重新输入");
        }
        if (!map.get("vehiclePassword").equals(vehiclePassword)) {
            return new JsonResultBean(JsonResultBean.FAULT, "账号或密码错误，请重新输入");
        }
        message = "单车登录小程序：" + brand + "登录";
        addLog(request, message, brand, map);
        String token = UUID.randomUUID().toString();
        JSONObject msg = new JSONObject();
        msg.put("token", token);
        final RedisKey key = HistoryRedisKeyEnum.SINGLE_VEHICLE_TOKEN.of(token);
        RedisHelper.setString(key, brand, RedisHelper.SIX_HOUR_REDIS_EXPIRE);
        return new JsonResultBean(msg);
    }

    @Override
    public JsonResultBean getSingleVehicleLocation(String brand) {
        VehicleInfo vehicleInfo = newVehicleDao.findByVehicle(brand);
        final RedisKey key = HistoryRedisKeyEnum.MONITOR_LOCATION.of(vehicleInfo.getId());
        boolean contains = RedisHelper.isContainsKey(key);
        if (!contains) {
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        String location = RedisHelper.getString(key);
        if (StringUtils.isNotBlank(location)) {
            Message message = JSON.parseObject(location, Message.class);
            T808Message t808Message = JSON.parseObject(message.getData().toString(), T808Message.class);
            LocationInfo info = JSON.parseObject(t808Message.getMsgBody().toString(), LocationInfo.class);

            SingleLocationInfo singleLocationInfo = new SingleLocationInfo();
            BeanUtils.copyProperties(info, singleLocationInfo);
            String y = String.valueOf(info.getLongitude());
            String x = String.valueOf(info.getLatitude());
            String formattedAddress = "";
            if (!"0.0".equals(x) && !"0.0".equals(y)) {
                formattedAddress = positionalService.getAddress(y, x);
            }
            try {
                // 获取车辆的基本信息
                redisVehicleServcie.getMonitorDetail(vehicleInfo.getId(), singleLocationInfo);
            } catch (Exception e) {
                log.error("获取组织ID异常", e);
            }
            singleLocationInfo.setPositionDescription(formattedAddress);
            return new JsonResultBean(singleLocationInfo);
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    @Override
    public JsonResultBean getSingleVehicleHistoryData(String brand, String startTime,
                                                      String endTime, Integer sensorFlag) throws Exception {
        Map<String, String> map = newConfigDao.getConfigByBrand(brand);
        if (MapUtils.isEmpty(map)) {
            return new JsonResultBean(PageGridBean.FAULT, "该车辆是未绑定的监控对象，请先进行绑定");
        }
        if (StringUtils.isBlank(map.get("configId"))) {
            return new JsonResultBean(JsonResultBean.FAULT, "该车辆是未绑定的监控对象，请先进行绑定");
        }
        String vehicleId = map.get("vehicleId");
        // 获取超长待机信息
        String functionalType = TrackBackUtil.getfunctionalType(vehicleId);
        // 判断监控对象是否绑定里程传感器
        boolean flogKey = RedisHelper.isContainsKey(HistoryRedisKeyEnum.SENSOR_MESSAGE.of(vehicleId));
        // 判断是否为离线数据,是则取当天是否绑定传感器的标识
        if (sensorFlag != null) {
            flogKey = sensorFlag == 1;
        }
        List<Positional> list = getHistoryData(vehicleId, startTime, endTime, flogKey);
        String historyDataJsonCompressStr = ZipUtil.compress(JSON.toJSONString(list));
        JSONObject msg = new JSONObject();
        // 全部数据
        msg.put("allData", historyDataJsonCompressStr);
        // 超长待机类型专用
        msg.put("type", functionalType);
        //是否绑定里程传感器
        msg.put("nowFlogKey", flogKey);
        return new JsonResultBean(msg);
    }


    private void addLog(HttpServletRequest request, String message, String brand, Map<String, String> map) {
        String ipAddress = new GetIpAddr().getIpAddr(request);
        logSearchServiceImpl.addLog(ipAddress, message, "4", "", brand, String.valueOf(map.get("plateColor")));
    }

    /**
     * 获得历史数据
     */
    private List<Positional> getHistoryData(String monitorId, String startTime, String endTime, boolean flogKey)
            throws Exception {
        Positional positional;
        final long startTime1 = DateUtils.parseDate(startTime, DATE_FORMAT).getTime() / 1000;
        final long endTime1 = DateUtils.parseDate(endTime, DATE_FORMAT).getTime() / 1000;
        List<Positional> list = getMonitorHistoryPositionalData(monitorId, startTime1, endTime1);
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        List<Positional> sortList =
                list.stream().sorted(Comparator.comparingLong(Positional::getVtime)).collect(Collectors.toList());
        // 查询车辆信息
        Map<String, String> monitorIco = monitorIconService.getByMonitorId(Collections.singleton(monitorId));
        BindDTO bindInfo = VehicleUtil.getBindInfoByRedis(monitorId);
        String plateColor = PlateColor.getNameOrBlankByCode(String.valueOf(bindInfo.getPlateColor()));
        String simCard = bindInfo.getSimCardNumber();
        String ico = monitorIco.get(monitorId);
        // 位置信息异常数据过滤
        CommonUtil.positionalInfoAbnormalFilter(sortList, filterFlag);
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
                if (positional.getVtime() - previousPositional.getVtime() > 300) {
                    // 初始行驶状态
                    TrackBackUtil.initDrivingState(flogKey, positional);
                }
            }
            //计算行驶状态
            TrackBackUtil.calculateDrivingStatus(positional, sortList, flogKey, listSize, i);
        }
        return sortList;
    }

    private List<Positional> getMonitorHistoryPositionalData(String monitorId, long startTime1, long endTime1) {
        final Map<String, String> params = new HashMap<>(8);
        params.put("vehicleId", monitorId);
        params.put("startTime", String.valueOf(startTime1));
        params.put("endTime", String.valueOf(endTime1));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_MONITOR_HISTORY_POSITIONAL_DATA, params);
        return PaasCloudUrlUtil.getResultListData(str, Positional.class);
    }

    @Override
    public JsonResultBean queryAlarmList(SingleVehicleAlarmSearchQuery query, String brand) {
        Map<String, String> map = newConfigDao.getConfigByBrand(brand);
        if (MapUtils.isEmpty(map)) {
            return new JsonResultBean(PageGridBean.FAULT, "该车辆是未绑定的监控对象，请先进行绑定");
        }
        if (StringUtils.isBlank(map.get("configId"))) {
            return new JsonResultBean(PageGridBean.FAULT, "该车辆是未绑定的监控对象，请先进行绑定");
        }
        String vehicleId = map.get("vehicleId");
        try {
            return queryAlarmInfo(query.getAlarmSource(), query.getAlarmType(), query.getStatus(),
                    query.getAlarmStartTime(), query.getAlarmEndTime(), vehicleId, query.getPushType());
        } catch (Exception e) {
            log.error("获取报警异常", e);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    /**
     * 组装调用PassCloud层接口获取报警信息的参数。
     *
     * @param alarmSource    报警来源
     * @param alarmTypes     报警类型
     * @param status         处理状态
     * @param alarmStartTime 查询开始时间
     * @param alarmEndTime   查询结束时间
     * @param monitorIds     车辆id
     * @param pushType       全局报警状态
     * @return list
     * @throws Exception e
     */
    public JsonResultBean queryAlarmInfo(Integer alarmSource, String alarmTypes, Integer status, String alarmStartTime,
                                         String alarmEndTime, String monitorIds, Integer pushType) throws Exception {
        //前端做了车的协议筛选，可能传空，直接返回
        if (StringUtils.isBlank(monitorIds)) {
            return null;
        }
        List<Integer> alarmTypeList = AlarmTypeUtil.typeList(alarmTypes);
        String alarmTypeStr = StringUtils.join(alarmTypeList, ",");
        return queryAlarmList(alarmSource, status, alarmStartTime, alarmEndTime, monitorIds, pushType, alarmTypeStr);
    }

    /**
     * 调用PassCloud层接口获取报警信息
     *
     * @param alarmSource    报警来源
     * @param alarmTypeStr   报警类型
     * @param status         处理状态
     * @param alarmStartTime 查询开始时间
     * @param alarmEndTime   查询结束时间
     * @param monitorIds     车辆id
     * @param pushType       全局报警状态
     * @return list
     * @throws Exception e
     */
    private JsonResultBean queryAlarmList(Integer alarmSource, Integer status, String alarmStartTime,
        String alarmEndTime, String monitorIds, Integer pushType, String alarmTypeStr) throws Exception {
        List<AlarmInfo> alarmList =
                alarmSearchService.getAlarmInfo(monitorIds, alarmTypeStr, alarmStartTime, alarmEndTime,
                        alarmSource, status, pushType, 5000, null, AlarmInfo.class, null);
        List<String> existMonitorIdList =
                alarmList.stream().map(AlarmInfo::getMonitorId).distinct().collect(Collectors.toList());
        final List<RedisKey> redisKeys =
                existMonitorIdList.stream().map(RedisKeyEnum.MONITOR_INFO::of).collect(Collectors.toList());
        final Map<String, Map<String, String>> configInfoMap = RedisHelper.batchGetHashMap(
                redisKeys, "id", "orgName", "professionalNames", "monitorType", "plateColor");
        for (AlarmInfo alarmInfo : alarmList) {
            Map<String, String> configInfo = configInfoMap.get(alarmInfo.getMonitorId());
            if (configInfo == null) {
                alarmInfo.setPlateColor("");
                alarmInfo.setEmployeeName("");
                alarmInfo.setAssignmentName("");
                alarmInfo.setName("");
                continue;
            }
            alarmInfo.setName(configInfo.get("orgName"));
            alarmInfo.setEmployeeName(configInfo.get("professionalNames"));
            String monitorType = configInfo.get("monitorType");
            alarmInfo.setMonitorType(Integer.valueOf(monitorType));
            String plateColor = configInfo.get("plateColor");
            if (Objects.equals(monitorType, "0")) {
                alarmInfo.setPlateColor(plateColor);
            }
            alarmInfo.setDescription(AlarmTypeUtil.getAlarmType(String.valueOf(alarmInfo.getAlarmType())));
        }
        final RedisKey key = HistoryRedisKeyEnum.SINGLE_VEHICLE_ALARM.of(monitorIds);
        RedisHelper.delete(key);
        RedisHelper.addObjectToList(key, alarmList, null);
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 获取分页数据。
     *
     * @param brand 车牌号
     * @param query 分页查询参数
     * @return PageGridBean page
     */
    @Override
    public PageGridBean getPage(SingleVehicleAlarmSearchQuery query, String brand) {
        Map<String, String> map = newConfigDao.getConfigByBrand(brand);
        if (MapUtils.isEmpty(map)) {
            return new PageGridBean(PageGridBean.FAULT, "该车辆是未绑定的监控对象，请先进行绑定");
        }
        if (StringUtils.isBlank(map.get("configId"))) {
            return new PageGridBean(PageGridBean.FAULT, "该车辆是未绑定的监控对象，请先进行绑定");
        }
        String vehicleId = map.get("vehicleId");
        final RedisKey key = HistoryRedisKeyEnum.SINGLE_VEHICLE_ALARM.of(vehicleId);
        List<AlarmInfo> alarmInfoList =
                RedisHelper.getListObj(key, (query.getStart() + 1), (query.getStart() + query.getLimit()));
        if (CollectionUtils.isEmpty(alarmInfoList)) {
            return new PageGridBean(new ArrayList<>());
        }
        List<String> lngLats = new ArrayList<>();
        for (AlarmInfo alarmInfo : alarmInfoList) {
            Long alarmStartTime = alarmInfo.getAlarmStartTime();
            if (alarmStartTime != null) {
                alarmInfo.setStartTime(DateUtil.getLongToDateStr(alarmStartTime, null));
            }
            Long alarmEndTime = alarmInfo.getAlarmEndTime();
            if (alarmEndTime != null) {
                alarmInfo.setEndTime(DateUtil.getLongToDateStr(alarmEndTime, null));
            }
            Long handleTime = alarmInfo.getHandleTime();
            if (handleTime != null) {
                alarmInfo.setHandleTimeStr(DateUtil.getLongToDateStr(handleTime * 1000, null));
            }
            String alarmStartLocation = alarmInfo.getAlarmStartLocation();
            if (StringUtils.isNotBlank(alarmStartLocation)) {
                lngLats.add(alarmStartLocation);
                String[] alarmStartLocationArr = alarmStartLocation.split(",");
                alarmInfo.setAlarmStartLongitude(alarmStartLocationArr[0]);
                alarmInfo.setAlarmStartLatitude(alarmStartLocationArr[1]);
            }
            String alarmEndLocation = alarmInfo.getAlarmEndLocation();
            if (StringUtils.isNotBlank(alarmEndLocation)) {
                lngLats.add(alarmEndLocation);
                String[] alarmEndLocationArr = alarmEndLocation.split(",");
                alarmInfo.setAlarmEndLongitude(alarmEndLocationArr[0]);
                alarmInfo.setAlarmEndLatitude(alarmEndLocationArr[1]);
            }
            alarmInfo.setDescription(AlarmTypeUtil.getAlarmType(String.valueOf(alarmInfo.getAlarmType())));
        }
        Map<String, String> addressMap = AddressUtil.batchInverseAddress(new HashSet<>(lngLats));
        for (AlarmInfo info : alarmInfoList) {
            info.setAlarmStartSpecificLocation(addressMap.get(info.getAlarmStartLocation()));
            info.setAlarmEndSpecificLocation(addressMap.get(info.getAlarmEndLocation()));
        }
        Page<AlarmInfo> resultPage = RedisUtil.queryPageList(alarmInfoList, query, key);
        return new PageGridBean(query, resultPage, true);
    }


    @Override
    public JsonResultBean getMaintenanceReminder(String brand) {
        JSONObject msg = new JSONObject();
        Map<String, String> map = newConfigDao.getConfigByBrand(brand);
        if (MapUtils.isEmpty(map)) {
            return new JsonResultBean(PageGridBean.FAULT, "该车辆是未绑定的监控对象，请先进行绑定");
        }
        if (StringUtils.isBlank(map.get("configId"))) {
            return new JsonResultBean(PageGridBean.FAULT, "该车辆是未绑定的监控对象，请先进行绑定");
        }
        String vehicleId = map.get("vehicleId");
        VehicleDO vehicleDO = newVehicleDao.getById(vehicleId);
        Integer maintainMileage = vehicleDO == null ? null : vehicleDO.getMaintainMileage();
        msg.put("maintainMileage", maintainMileage);
        final RedisKey key = HistoryRedisKeyEnum.MONITOR_LOCATION.of(vehicleId);
        double distance;

        boolean contains = RedisHelper.isContainsKey(key);
        if (!contains) {
            return new JsonResultBean(msg);
        }
        String locaton = RedisHelper.getString(key);
        if (StringUtils.isNotBlank(locaton)) {
            Message message = JSON.parseObject(locaton, Message.class);
            T808Message t808Message = JSON.parseObject(message.getData().toString(), T808Message.class);
            LocationInfo info = JSON.parseObject(t808Message.getMsgBody().toString(), LocationInfo.class);
            distance = info.getGpsMileage();
            msg.put("distance", distance);
        }
        return new JsonResultBean(msg);
    }

    @Override
    public JsonResultBean updatePassword(SingleVehicleAcount acount, String brand, String token) {
        Map<String, String> map = newConfigDao.getConfigByBrand(brand);
        if (MapUtils.isEmpty(map)) {
            return new JsonResultBean(PageGridBean.FAULT, "该车辆是未绑定的监控对象，请先进行绑定");
        }
        String vehiclePassword = map.get("vehiclePassword");
        String vehicleId = map.get("vehicleId");
        if (StringUtils.isEmpty(vehicleId)) {
            return new JsonResultBean(JsonResultBean.FAULT, "参数传递异常");
        }
        if (!acount.getOldVehiclePassword().equals(vehiclePassword)) {
            return new JsonResultBean(PageGridBean.FAULT, "修改失败，原密码不正确！");
        }
        boolean result = newConfigDao.updateVehiclePassword(acount.getNewVehiclePassword(), vehicleId);
        if (result) {
            RedisHelper.delete(HistoryRedisKeyEnum.SINGLE_VEHICLE_TOKEN.of(token));
        }
        return new JsonResultBean(result);
    }

    /**
     * 记录单车登录相关日志
     */
    @Override
    public void addLog(String brand, String ip, String message) {
        Map<String, String> map = newConfigDao.getConfigByBrand(brand);
        String vehicleId = map.get("vehicleId");
        // 根据id查询组织架构实体
        try {
            final RedisKey key = RedisKeyEnum.MONITOR_INFO.of(vehicleId);
            final Map<String, String> monitor = RedisHelper.getHashMap(key, "name", "plateColor", "orgName");
            if (monitor != null) {
                String plateColor = monitor.get("plateColor");
                String orgName = monitor.get("orgName");
                String msg = "监控对象:(" + brand + "@(" + orgName + ")" + ")" + message;
                logSearchServiceImpl.addLog(ip, msg, "5", "MONITORING", brand, plateColor);
            }
        } catch (Exception e) {
            logger.error("单车登录小程序，日志管理异常" + e);
        }
    }

    @Override
    public JsonResultBean logOut(String token, String brand) {
        final RedisKey key = HistoryRedisKeyEnum.SINGLE_VEHICLE_TOKEN.of(token);
        final String vehicleIdOfToken = RedisHelper.getString(key);
        if (!brand.equals(vehicleIdOfToken)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        RedisHelper.delete(key);
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    @Override
    public List<TerminalMileageDailyDetails> listMonthTerminalMileageDetail(String brand, YearMonth month) {
        Map<String, String> map = newConfigDao.getConfigByBrand(brand);
        if (MapUtils.isEmpty(map)) {
            return Collections.emptyList();
        }
        if (StringUtils.isBlank(map.get("configId"))) {
            return Collections.emptyList();
        }
        String vehicleId = map.get("vehicleId");
        final String startDate = YMD_SHORT.format(month.atDay(1)).orElse("");
        final String endDate = YMD_SHORT.format(month.atEndOfMonth()).orElse("");

        Map<String, String> queryParam = new HashMap<>(6);
        queryParam.put("monitorIds", vehicleId);
        queryParam.put("startTime", startDate);
        queryParam.put("endTime", endDate);
        String queryResult = HttpClientUtil.send(PaasCloudUrlEnum.TERMINAL_MILEAGE_DAILY_DETAILS_URL, queryParam);
        final List<PassCloudMileageReport> paasResult =
                PaasCloudUrlUtil.getResultListData(queryResult, PassCloudMileageReport.class);
        if (CollectionUtils.isEmpty(paasResult)) {
            return Collections.emptyList();
        }

        return paasResult.get(0).getDetail().stream().map(o -> {
            final TerminalMileageDailyDetails detail = new TerminalMileageDailyDetails();
            detail.setMonitorId(vehicleId);
            detail.setMonitorName(brand);
            detail.setDay(detail.getDay());
            detail.setDayDate(DateUtil.getLongToDateStr(detail.getDay() * 1000, DateUtil.DATE_Y_M_D_FORMAT));
            detail.setTotalMile(changeScale(detail.getTotalMile()));
            detail.setTravelMile(changeScale(detail.getTravelMile()));
            detail.setIdleSpeedMile(changeScale(detail.getIdleSpeedMile()));
            detail.setAbnormalMile(changeScale(detail.getAbnormalMile()));
            return detail;
        }).collect(Collectors.toList());
    }

    private static double changeScale(Double value) {
        double finalValue = value == null ? 0d : value;
        return BigDecimal.valueOf(finalValue).setScale(1, BigDecimal.ROUND_DOWN).doubleValue();
    }

}

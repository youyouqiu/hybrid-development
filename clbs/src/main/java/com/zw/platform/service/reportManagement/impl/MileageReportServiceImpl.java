package com.zw.platform.service.reportManagement.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zw.app.util.AppParamCheckUtil;
import com.zw.lkyw.domain.common.PaasCloudZipDTO;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.basicinfo.enums.PlateColor;
import com.zw.platform.domain.reportManagement.DrivingMileageDetails;
import com.zw.platform.domain.reportManagement.DrivingMileageLocationDetails;
import com.zw.platform.domain.reportManagement.DrivingMileageStatistics;
import com.zw.platform.domain.reportManagement.MileageReport;
import com.zw.platform.domain.reportManagement.PositionalDetail;
import com.zw.platform.domain.reportManagement.TravelDetail;
import com.zw.platform.domain.reportManagement.query.BigDataReportQuery;
import com.zw.platform.domain.statistic.TravelBaseInfo;
import com.zw.platform.domain.statistic.TravelBaseInfoCaretaker;
import com.zw.platform.domain.statistic.TravelBaseInfoOriginator;
import com.zw.platform.service.oil.PositionalService;
import com.zw.platform.service.reportManagement.MileageReportService;
import com.zw.platform.util.BigDataQueryUtil;
import com.zw.platform.util.CalculateUtil;
import com.zw.platform.util.CommonUtil;
import com.zw.platform.util.LocalDateUtils;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MonitorUtils;
import com.zw.platform.util.common.UuidUtils;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.platform.util.excel.ExportExcelParam;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.platform.util.paas.PaasCloudUrlUtil;
import com.zw.platform.util.report.PaasCloudHBaseAccessEnum;
import com.zw.platform.util.report.PaasCloudUrlEnum;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Administrator
 */
@Service
public class MileageReportServiceImpl implements MileageReportService {
    private static final Logger log = LogManager.getLogger(MileageReportServiceImpl.class);

    @Autowired
    private PositionalService positionalService;

    @Autowired
    private UserService userService;

    @Value("${no.location}")
    private String unknownLocation;

    // 查询车辆的行驶里程和油耗
    @Override
    public List<MileageReport> findMileageById(List<String> moIds, long startTimeL, long endTimeL) {
        //将uuid型车辆id转换为byte类型
        // 查询里程数据
        List<MileageReport> mileageReportList = getMileById(moIds, startTimeL, endTimeL);
        for (MileageReport mileageReport : mileageReportList) {
            mileageReport.setVehicleId(String.valueOf(UuidUtils.getUUIDFromBytes(mileageReport.getVehicleIdHbase())));
        }
        Map<String, List<MileageReport>> vehicleMileageReportListMap = mileageReportList
            .stream()
            .collect(Collectors.groupingBy(MileageReport::getVehicleId));
        Map<String, RedisKey> redisKeyMap = new HashMap<>(16);
        for (String moId : moIds) {
            redisKeyMap.put(moId, HistoryRedisKeyEnum.SENSOR_MESSAGE.of(moId));
        }
        Map<String, String> sensorMessageMap = RedisHelper.batchGetStringMap(redisKeyMap);
        List<MileageReport> resultList = new ArrayList<>();
        Map<String, BindDTO> bindInfoMap = VehicleUtil.batchGetBindInfosByRedis(moIds);
        for (String moId : moIds) {
            BindDTO bindDTO = bindInfoMap.get(moId);
            if (bindDTO == null) {
                continue;
            }
            MileageReport mileReport = new MileageReport();
            mileReport.setPlateNumber(bindDTO.getName());
            mileReport.setVehicleId(moId);
            mileReport.setVehicleColor(PlateColor.getNameOrBlankByCode(bindDTO.getPlateColor()));
            List<MileageReport> mileageReports = vehicleMileageReportListMap.get(moId);
            if (CollectionUtils.isNotEmpty(mileageReports)) {
                MileageReport lastMileageReport = mileageReports.get(mileageReports.size() - 1);
                String value = sensorMessageMap.get(moId);
                if (value != null) {
                    mileReport.setMileage(lastMileageReport.getTotalMileage());
                } else {
                    mileReport.setMileage(lastMileageReport.getTotalGpsMile());
                }
                mileReport.setTotalOilWearOne(lastMileageReport.getTotalOilWearOne());
            }
            resultList.add(mileReport);
        }
        // 对集合进行排序，把里程不为0的数据自然排序
        return sortList(resultList);
    }

    private List<MileageReport> getMileById(List<String> moIds, long startTimeL, long endTimeL) {
        final Map<String, String> params = new HashMap<>(8);
        params.put("vehicleId", JSON.toJSONString(moIds));
        params.put("startTime", String.valueOf(startTimeL));
        params.put("endTime", String.valueOf(endTimeL));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_MILE_BY_ID, params);
        return PaasCloudUrlUtil.getResultListData(str, MileageReport.class);
    }

    @Override
    public JSONObject getAssignMileageData(String assignNames) {
        if (StringUtils.isBlank(assignNames)) {
            return new JSONObject();

        }
        JSONObject jsonObject = new JSONObject();
        String userUuid = userService.getCurrentUserUuid();
        RedisKey redisKey = HistoryRedisKeyEnum.USER_DRIVING_MILEAGE_STATISTICS_ASSIGN_MILEAGE_DATA.of(userUuid);
        Map<String, String> assignDataMap = RedisHelper.hgetAll(redisKey);
        if (MapUtils.isEmpty(assignDataMap)) {
            return jsonObject;
        }
        List<String> assigns = new ArrayList<>();
        List<Double> miles = new ArrayList<>();
        for (String assign : assignNames.split(",")) {
            assigns.add(assign);
            miles.add(new BigDecimal(assignDataMap.get(assign)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        }
        jsonObject.put("assignNames", assigns);
        jsonObject.put("mileages", miles);
        return jsonObject;
    }

    private void getMonitorMileageReportDetail(List<String> vcId, String startTime, String endTime,
        List<MileageReport> resultData) throws Exception {
        // 初始化查询参数
        List<BigDataReportQuery> queries = BigDataQueryUtil.getBigDataReportQuery(vcId, startTime, endTime);
        // 查询数据
        for (BigDataReportQuery query : queries) {
            try {
                List<MileageReport> mileageDataList = getMileageData(query);
                // 处理经度丢失
                for (MileageReport mileageReport : mileageDataList) {
                    Double totalGpsMile = mileageReport.getTotalGpsMile();
                    if (totalGpsMile != null) {
                        mileageReport.setTotalGpsMile(
                            new BigDecimal(totalGpsMile).setScale(1, BigDecimal.ROUND_DOWN).doubleValue());
                    }
                    Double totalOilWearOne = mileageReport.getTotalOilWearOne();
                    if (totalOilWearOne != null) {
                        mileageReport.setTotalOilWearOne(
                            new BigDecimal(totalOilWearOne).setScale(1, BigDecimal.ROUND_DOWN).doubleValue());
                    }
                }
                resultData.addAll(mileageDataList);
            } catch (BadSqlGrammarException e) {
                // 暂时不作处理
            }
        }
    }

    private List<MileageReport> getMileageData(BigDataReportQuery query) {
        Map<String, String> params = new HashMap<>(2);
        params.put("value", JSON.toJSONString(query));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_MILEAGE_DATA, params);
        return PaasCloudUrlUtil.getResultListData(str, MileageReport.class);
    }

    @Override
    public List<PositionalDetail> findPositionalDetailsBy(String nowDay, List<String> vehicleIds) {
        String nowDayStartTime = nowDay + " 00:00:00";
        String nowDayEndTime = nowDay + " 23:59:59";
        long startTimeL = LocalDateUtils.parseDateTime(nowDayStartTime).getTime() / 1000;
        long endTimeL = LocalDateUtils.parseDateTime(nowDayEndTime).getTime() / 1000;

        return getMileageByVehicleIds(vehicleIds, startTimeL, endTimeL);
    }

    private List<PositionalDetail> getMileageByVehicleIds(List<String> vehicleIds, long startTimeL, long endTimeL) {
        final Map<String, String> params = new HashMap<>(8);
        params.put("vehicleIds", JSON.toJSONString(vehicleIds));
        params.put("startTime", String.valueOf(startTimeL));
        params.put("endTime", String.valueOf(endTimeL));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.FIND_MILEAGE_BY_VEHICLE_IDS, params);
        return PaasCloudUrlUtil.getResultListData(str, PositionalDetail.class);
    }

    @Override
    public void getMonitorsTravelDetail(List<TravelDetail> singleMonitorTravelDetails, List<TravelDetail> travelDetails,
        List<PositionalDetail> positionalList, int mark, int statusChangeTimes, boolean isAppSearch) {
        Map<String, List<PositionalDetail>> monitorPositionalMap = positionalList.stream().peek(positionalDetail -> {
            String vehicleId = UuidUtils.getUUIDFromBytes(positionalDetail.getVehicleId()).toString();
            positionalDetail.setMonitorId(vehicleId);
        }).sorted(Comparator.comparing(PositionalDetail::getVtime))
            .collect(Collectors.groupingBy(PositionalDetail::getMonitorId));

        for (List<PositionalDetail> positionalDetails : monitorPositionalMap.values()) {
            singleMonitorTravelDetails.clear();
            getTravelOrStopData(singleMonitorTravelDetails, positionalDetails, mark, statusChangeTimes, isAppSearch);
            travelDetails.addAll(singleMonitorTravelDetails);
        }
    }

    /**
     * list集合的排序方法
     */
    private List<MileageReport> sortList(List<MileageReport> mileageList) {
        mileageList.sort((mileageReport1, mileageReport2) -> {
            if (mileageReport1.getMileage() < mileageReport2.getMileage()) {
                return 1;
            } else if (mileageReport1.getMileage().equals(mileageReport2.getMileage())) {
                return 0;
            } else {
                return -1;
            }
        });
        return mileageList;
    }

    @Override
    public boolean export(String title, int type, HttpServletResponse response, List<MileageReport> fcsList,
        String fuzzyQuery) throws IOException {
        List<MileageReport> result = null;
        if (StringUtils.isNotBlank(fuzzyQuery)) {
            result = new ArrayList<>();
            for (MileageReport mileageReport : fcsList) {
                if (mileageReport.getPlateNumber().contains(fuzzyQuery)) {
                    result.add(mileageReport);
                }
            }
        }
        if (result != null) {
            fcsList = result;
        }
        return ExportExcelUtil
            .export(new ExportExcelParam(title, type, fcsList, MileageReport.class, null, response.getOutputStream()));
    }

    /**
     * 由于此方法是在原有的行驶报表方法上修改, 因此请"忽略方法名的不严谨"
     * 新数据组装方法
     * 计算“停止报表”和“行驶报表"
     * @param resultList        返回数据
     * @param positionalList    位置数据
     * @param mark              表示: 0: 行驶; 1: 停止;
     * @param statusChangeTimes 满足多少次连续行驶or停止进行计算;
     *                          mark = 0, statusChangeTimes = 3; mark = 1, statusChangeTimes = 5;
     * @param isAppSearch       用于控制行驶时长格式; true: App查询(h), false 平台查询(xx小时xxf分钟xx秒)
     */
    @Override
    public void getTravelOrStopData(List<TravelDetail> resultList, List<PositionalDetail> positionalList, int mark,
        int statusChangeTimes, boolean isAppSearch) {
        // 过滤异常点
        CommonUtil.getFilterPositionalDetail(positionalList);
        final int timeDifference = 5 * 60 * 1000;
        // 判断该监控对象是否绑定里程传感器
        PositionalDetail positionalDetail = positionalList.get(0);
        String monitorId = positionalDetail.getMonitorId();
        if (StringUtils.isEmpty(monitorId)) {
            byte[] vehicleId = positionalDetail.getVehicleId();
            monitorId = UuidUtils.getUUIDFromBytes(vehicleId).toString();
        }
        boolean flogKey = RedisHelper.isContainsKey(HistoryRedisKeyEnum.SENSOR_MESSAGE.of(monitorId));

        // true: 行驶统计; false: 停止统计
        boolean isTravelStatus = (mark == TravelBaseInfo.STATUS_TRAVEL);
        // 速度
        Double speed = getSpeed(positionalDetail, flogKey);

        TravelDetail travelDetail = null;
        // 设置初始状态
        TravelBaseInfo travelBaseInfo =
            new TravelBaseInfo(speed, isTravelStatus, timeDifference, flogKey, positionalDetail.getVtime());
        // 用于存储状态变换
        TravelBaseInfoCaretaker caretaker = new TravelBaseInfoCaretaker();
        TravelBaseInfoOriginator originator = new TravelBaseInfoOriginator();
        // 当前位置数据状态: speed >= 5km则为行驶, speed <5km 则为停止
        boolean currentStatusFlag;

        for (PositionalDetail positional : positionalList) {
            // 时间差大于5分钟 结束当前状态(使用上一个点状态的最后一个点, 计算行驶时长和里程)
            // 行驶: 连续3个为停止, 则变为停止
            // 停止: 连续5个点都为行驶, 则变更为行驶
            long lastVtime = positional.getVtime() * 1000;
            Double lastGpsMile = getLastGpsMile(travelBaseInfo, positional);
            travelBaseInfo.setTimeAndMileDifferenceFlag(lastVtime, lastGpsMile);
            speed = getSpeed(positional, travelBaseInfo.getFlogKey());
            currentStatusFlag = isCurrentStatusFlag(travelBaseInfo, speed);
            String location = positional.getLongtitude() + "," + positional.getLatitude();
            Integer countPosition = travelBaseInfo.getCountPosition();

            // 只要两个点的时间大于5分钟, 则计算前一个状态段的数据
            if (travelBaseInfo.getTwoPointTimeDifferenceFlag()) {
                if (countPosition > 0 && currentStatusFlag) {
                    // 1.存储当前段数据, 前提是, 当前状态连续行驶此处至少为1
                    setPreTravelDetail(resultList, isAppSearch, travelDetail, travelBaseInfo, caretaker, originator);
                }
                travelBaseInfo.setFirstPositionStatus(true);
                // 2.由于这是新的一个状态点, 因此需要存储该点的油量油耗、里程、时长等数据
                travelDetail =
                    getCurrentTravelDetail(travelBaseInfo, positional, lastVtime, location, lastGpsMile, speed);
            } else if (currentStatusFlag) {
                if (countPosition == 0) {
                    travelDetail = getTravelDetail(travelBaseInfo, positional, lastVtime, location);
                }
                setTravelBaseInfo(travelBaseInfo, countPosition, positional, location);
            } else {
                // 注: 如果status = mark, 记录第一次状态改变的具体数据(travelBaseInfo),
                // 如果满足状态改变的条件(1.行驶变停止: 连续5条位置数据都是"停止状态", 2.停止变行驶: 连续3条位置数都是"行驶状态"
                // )则恢复次时存储的数据,用于计算
                if (travelBaseInfo.getStatus() == mark) {
                    setTravelBaseInfo(travelBaseInfo, countPosition, positional, location);
                    setFirstStatusChangeData(travelBaseInfo, caretaker, originator, isTravelStatus);
                } else {
                    if (travelBaseInfo.getFirstPositionStatus()) {
                        // 恢复到第一次"停止"or"行驶"时的存储的对象
                        setPreTravelDetail(resultList, isAppSearch, travelDetail, travelBaseInfo, caretaker,
                            originator);
                        travelBaseInfo.setFirstPositionStatus(false);
                    } else if (countPosition >= statusChangeTimes) {
                        setPreTravelDetail(resultList, isAppSearch, travelDetail, travelBaseInfo, caretaker,
                            originator);
                    }
                    // 只有状态切换为"停止 or 行驶",才初始化基本参数
                    TravelBaseInfo.initTravelBaseInfo(travelBaseInfo);
                }
            }
            travelBaseInfo.setPreTime(lastVtime);
        }
        // 4.如果集合的最后一条数据为行驶数据(执行完循环后countPosition > 3), 计算行驶时长
        if (travelDetail != null && (travelBaseInfo.getFirstPositionStatus()
            || travelBaseInfo.getCountPosition() >= statusChangeTimes)) {
            TravelBaseInfo mementoTravelBaseInfo = originator.getTravelBaseInfo();
            saveBaseInfo(resultList, isAppSearch, travelDetail, travelBaseInfo, mementoTravelBaseInfo);
        }
    }

    private void setPreTravelDetail(List<TravelDetail> resultList, boolean isAppSearch, TravelDetail travelDetail,
        TravelBaseInfo travelBaseInfo, TravelBaseInfoCaretaker caretaker, TravelBaseInfoOriginator originator) {
        originator.restoreTravelBaseInfo(caretaker.retrieveTravelBaseInfo());
        TravelBaseInfo mementoTravelBaseInfo = originator.getTravelBaseInfo();
        saveBaseInfo(resultList, isAppSearch, travelDetail, travelBaseInfo, mementoTravelBaseInfo);
        originator.setTravelBaseInfo(null);
        caretaker.saveTravelBaseInfo(null);
    }

    /**
     * 如果绑定了传感器,则获取里程传感器的速度
     */
    private Double getSpeed(PositionalDetail positionalDetail, boolean flogKey) {
        double speed;
        if (flogKey) {
            speed = Double.parseDouble(positionalDetail.getMileageSpeed());
        } else {
            speed = Double.parseDouble(positionalDetail.getSpeed());
        }
        return speed;
    }

    private void saveBaseInfo(List<TravelDetail> resultList, boolean isAppSearch, TravelDetail travelDetail,
        TravelBaseInfo travelBaseInfo, TravelBaseInfo mementoTravelBaseInfo) {
        if (Objects.nonNull(mementoTravelBaseInfo)) {
            addResultList(resultList, travelDetail, mementoTravelBaseInfo, isAppSearch);
            travelBaseInfo.setPreTravelMile(mementoTravelBaseInfo.getPreTravelMile());
        } else {
            addResultList(resultList, travelDetail, travelBaseInfo, isAppSearch);
        }
    }

    /**
     * 设置第一次状态变换的数据
     * @param travelBaseInfo travelBaseInfo
     * @param caretaker      caretaker
     * @param originator     originator
     * @param isTravelStatus isTravelStatus
     */
    private void setFirstStatusChangeData(TravelBaseInfo travelBaseInfo, TravelBaseInfoCaretaker caretaker,
        TravelBaseInfoOriginator originator, boolean isTravelStatus) {
        if (isTravelStatus) {
            if (travelBaseInfo.getStopCountTimes() == 1) {
                saveMemento(travelBaseInfo, caretaker, originator);
            }
        } else {
            if (travelBaseInfo.getTravelCountTimes() == 1) {
                saveMemento(travelBaseInfo, caretaker, originator);
            }
        }
    }

    /**
     * 获取当前这条位置数据的状态
     */
    private boolean isCurrentStatusFlag(TravelBaseInfo travelBaseInfo, Double speed) {
        boolean currentStatusFlag;
        if (travelBaseInfo.getIsTravelStatus()) {
            // 行驶统计: currentStatusFlag = true 即为行驶, 反之为停止
            currentStatusFlag = TravelBaseInfo.checkIsTravelFlag(speed);
        } else {
            // 停止统计: currentStatusFlag = true 即为停止, 反之为行驶
            currentStatusFlag = TravelBaseInfo.checkIsStopFlag(speed);
        }
        judgeStatus(travelBaseInfo, currentStatusFlag);
        return currentStatusFlag;
    }

    private void saveMemento(TravelBaseInfo travelBaseInfo, TravelBaseInfoCaretaker caretaker,
        TravelBaseInfoOriginator originator) {
        originator.setTravelBaseInfo(travelBaseInfo);
        caretaker.saveTravelBaseInfo(originator.createTravelBaseInfo());
    }

    private void setTravelBaseInfo(TravelBaseInfo travelBaseInfo, Integer countPosition, PositionalDetail positional,
        String endLocation) {
        String totalOilWearOne = positional.getTotalOilwearOne();
        double totalOil = getTotalOilWear(totalOilWearOne);
        travelBaseInfo.setMaxTotalOilWearOne(totalOil);
        travelBaseInfo.setCountPosition(++countPosition);
        travelBaseInfo.setEndLocation(endLocation);
    }

    private TravelDetail getTravelDetail(TravelBaseInfo travelBaseInfo, PositionalDetail positional, long lastVTime,
        String location) {
        String totalOilWearOne = positional.getTotalOilwearOne();
        double totalOil = getTotalOilWear(totalOilWearOne);
        // 1.第一条速度大于5km/h的位置数据, 记录开始时间、里程、油耗、开始位置经纬度等
        travelBaseInfo.setFirstVTime(lastVTime);
        travelBaseInfo.setMinTotalOilWearOne(totalOil);
        return initTravelDetail(travelBaseInfo, positional, lastVTime, location, totalOil);
    }

    private TravelDetail getCurrentTravelDetail(TravelBaseInfo travelBaseInfo, PositionalDetail positional,
        long lastVTime, String location, Double lastGpsMile, Double speed) {
        String totalOilWearOne = positional.getTotalOilwearOne();
        double totalOil = getTotalOilWear(totalOilWearOne);
        travelBaseInfo.setFirstVTime(lastVTime);
        travelBaseInfo.setLastVTime(lastVTime);
        travelBaseInfo.setMinTotalOilWearOne(totalOil);
        travelBaseInfo.setMaxTotalOilWearOne(totalOil);
        // 里程
        travelBaseInfo.setLastGpsMile(Objects.nonNull(lastGpsMile) ? lastGpsMile : 0.0);
        // 状态
        boolean flag = TravelBaseInfo.checkIsTravelFlag(speed);
        travelBaseInfo.setCountPosition(1);
        if (flag) {
            travelBaseInfo.setTravelCountTimes(1);
            travelBaseInfo.setStopCountTimes(0);
            travelBaseInfo.setStatus(TravelBaseInfo.STATUS_TRAVEL);
        } else {
            travelBaseInfo.setTravelCountTimes(0);
            travelBaseInfo.setStopCountTimes(1);
            travelBaseInfo.setStatus(TravelBaseInfo.STATUS_STOP);
        }
        return initTravelDetail(travelBaseInfo, positional, lastVTime, location, totalOil);
    }

    private TravelDetail initTravelDetail(TravelBaseInfo travelBaseInfo, PositionalDetail positional, long lastVTime,
        String location, double totalOil) {
        TravelDetail travelDetail = new TravelDetail();
        travelDetail.setPlateNumber(positional.getPlateNumber());
        travelDetail.setStartDateTime(LocalDateUtils.dateTimeFormat(new Date(lastVTime)));
        travelDetail.setStartLocation(location);
        travelDetail.setEndLocation(location);
        travelDetail.setTotalOilWearOne(totalOil);
        travelDetail.setTotalMile(travelBaseInfo.getLastGpsMile());
        travelDetail.setVehicleIdHbase(positional.getVehicleId());
        return travelDetail;
    }

    private Double getLastGpsMile(TravelBaseInfo travelBaseInfo, PositionalDetail positional) {
        // 累计里程
        String gpsMile;
        // 根据是否有里程传感器取对应里程数据
        if (travelBaseInfo.getFlogKey()) {
            gpsMile = positional.getMileageTotal();
        } else {
            gpsMile = positional.getGpsMile();
        }
        // 如果不为空或异常里程数据则赋值
        if (StringUtils.isNotEmpty(gpsMile) && !"null".equals(gpsMile)) {
            return new BigDecimal(gpsMile).setScale(1, RoundingMode.HALF_UP).doubleValue();
        }
        return null;
    }

    private void addResultList(List<TravelDetail> resultList, TravelDetail travelDetail, TravelBaseInfo travelBaseInfo,
        boolean isAppSearch) {
        travelDetail.setEndDateTime(LocalDateUtils.dateTimeFormat(new Date(travelBaseInfo.getLastVTime())));
        if (Objects.nonNull(travelBaseInfo.getEndLocation())) {
            travelDetail.setEndLocation(travelBaseInfo.getEndLocation());
        }
        calculateMile(travelBaseInfo, travelDetail);
        setTotalOilWearOne(travelBaseInfo, travelDetail);
        addMileageReportToList(resultList, travelBaseInfo, travelDetail, isAppSearch);
    }

    /**
     * 状态变更
     * @param travelBaseInfo    travelBaseInfo
     * @param currentStatusFlag isTravelStatus= true: currentStatusFlag = true,则是行驶状态, currentStatusFlag = false:停止状态
     *                          isTravelStatus= false: currentStatusFlag = true,则是停止状态
     */
    private void judgeStatus(TravelBaseInfo travelBaseInfo, boolean currentStatusFlag) {
        Integer stopCountTimes = travelBaseInfo.getStopCountTimes();
        Integer travelCountTimes = travelBaseInfo.getTravelCountTimes();
        Integer status = travelBaseInfo.getStatus();
        if (travelBaseInfo.getIsTravelStatus()) {
            // 行驶
            if (currentStatusFlag) {
                setTravelCountTimes(travelBaseInfo, travelCountTimes, status);
            } else {
                setStopCountTimes(travelBaseInfo, stopCountTimes, status);
            }
        } else {
            // 停止
            if (currentStatusFlag) {
                setStopCountTimes(travelBaseInfo, stopCountTimes, status);
            } else {
                setTravelCountTimes(travelBaseInfo, travelCountTimes, status);
            }
        }

    }

    /**
     * @param travelBaseInfo travelBaseInfo
     * @param stopCountTimes 停止次数
     * @param status         true: 行驶; false: 停止
     */
    private void setStopCountTimes(TravelBaseInfo travelBaseInfo, Integer stopCountTimes, Integer status) {
        // 1.上一个状态是"行驶"
        if (status == TravelBaseInfo.STATUS_TRAVEL) {
            // 2.当前这条位置数据是"停止",则停止次数+1
            stopCountTimes += 1;
            travelBaseInfo.setStopCountTimes(stopCountTimes);
            if (stopCountTimes >= TravelBaseInfo.TRAVEL_CHANGE_STOP_MAX_TIMES) {
                travelBaseInfo.setStatus(TravelBaseInfo.STATUS_STOP);
                travelBaseInfo.setStopCountTimes(0);
            }
        } else {
            travelBaseInfo.setTravelCountTimes(0);
        }
    }

    /**
     * @param travelBaseInfo   travelBaseInfo
     * @param travelCountTimes 行驶次数
     * @param status           true: 行驶; false: 停止
     */
    private void setTravelCountTimes(TravelBaseInfo travelBaseInfo, Integer travelCountTimes, Integer status) {
        // 1.上一个状态是"停止"
        if (status == TravelBaseInfo.STATUS_STOP) {
            // 2.当前这条位置数据是"行驶",则行驶次数+1
            travelCountTimes += 1;
            travelBaseInfo.setTravelCountTimes(travelCountTimes);
            // 3.如果连续“5”条位置数据状态都为行驶: 变更状态为行驶并重置行驶次数
            if (travelCountTimes >= TravelBaseInfo.STOP_CHANGE_TRAVEL_TIMES) {
                travelBaseInfo.setStatus(TravelBaseInfo.STATUS_TRAVEL);
                travelBaseInfo.setTravelCountTimes(0);
            }
        } else {
            travelBaseInfo.setStopCountTimes(0);
        }
    }

    private void setTotalOilWearOne(TravelBaseInfo travelBaseInfo, TravelDetail travelDetail) {
        double totalOilWearOne =
            calculateTotalOilWearOne(travelBaseInfo.getMinTotalOilWearOne(), travelBaseInfo.getMaxTotalOilWearOne());
        travelDetail.setTotalOilWearOne(totalOilWearOne);
    }

    private double calculateTotalOilWearOne(double minTotalOilWearOne, double maxTotalOilWearOne) {
        return new BigDecimal(maxTotalOilWearOne).subtract(new BigDecimal(minTotalOilWearOne))
            .setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 累计里程 = 当前行驶里程 + 上一条数据的行驶里程
     * @param travelDetail mileageReport
     */
    private void calculateMile(TravelBaseInfo travelBaseInfo, TravelDetail travelDetail) {
        // 如果两条位置数据时间差大于5分钟, 使用当前状态最后一条里程 - 当前状态第一条里程
        double totalMile = travelDetail.getTotalMile();
        // 当前状态的行驶里程
        double travelMile = BigDecimal.valueOf(travelBaseInfo.getLastGpsMile()).subtract(BigDecimal.valueOf(totalMile))
            .setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        travelDetail.setTotalGpsMile(travelMile);

        // 当前状态的行驶里程
        double preTravelMile = BigDecimal.valueOf(travelMile).add(BigDecimal.valueOf(travelBaseInfo.getPreTravelMile()))
            .setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        travelDetail.setAccumulatedGpsMile(preTravelMile);
        travelBaseInfo.setPreTravelMile(preTravelMile);
    }

    private void addMileageReportToList(List<TravelDetail> resultList, TravelBaseInfo travelBaseInfo,
        TravelDetail travelDetail, boolean isAppSearch) {
        long travelTime = travelBaseInfo.getTravelTimeByTwoPointTimeDifferenceFlag();
        // x天x小时x分x秒(x=0时的时间维度不显示)
        if (isAppSearch) {
            travelDetail.setTravelTimeStr(LocalDateUtils.formatHour(travelTime));
        } else {
            travelDetail.setTravelTimeStr(LocalDateUtils.formatDuring(travelTime));
        }
        travelDetail.setTravelTime(travelTime);
        resultList.add(travelDetail);
    }

    private double getTotalOilWear(String totalOilWearOne) {
        BigDecimal totalOil = new BigDecimal("0.0");
        if (StringUtils.isNotEmpty(totalOilWearOne)) {
            totalOil = new BigDecimal(totalOilWearOne).setScale(1, BigDecimal.ROUND_HALF_UP);
        }
        return totalOil.doubleValue();
    }

    @Override
    public List<MileageReport> getSingleMonitorMileageData(List<String> monitorIds, String startTime, String endTime)
        throws Exception {
        List<MileageReport> resultData = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(monitorIds) && AppParamCheckUtil.checkDate(startTime, 1)
            && AppParamCheckUtil.checkDate(endTime, 1)) {
            getMonitorMileageReportDetail(monitorIds, startTime, endTime, resultData);
        }
        return resultData;
    }

    /**
     * 获得行驶里程统计
     * @param monitorIds  监控对象id 逗号分隔
     * @param startTime   开始日期
     * @param endTime     结束日期
     * @param isAppSearch 是否是app搜索
     */
    @Override
    public JsonResultBean getDrivingMileageStatistics(String monitorIds, String startTime, String endTime,
        boolean isAppSearch) throws Exception {
        if (StringUtils.isBlank(monitorIds) || StringUtils.isBlank(startTime) || StringUtils.isBlank(endTime)) {
            return new JsonResultBean(JsonResultBean.FAULT, "参数错误！");
        }
        // 2:传入的时间格式为yyyy-MM-dd 1:传入的时间格式为yyyy-MM-dd HH:mm:ss
        int dateType = startTime.length() == 10 ? 2 : 1;
        String beforeFormat = Objects.equals(dateType, 2) ? DateUtil.DATE_Y_M_D_FORMAT : DateUtil.DATE_FORMAT_SHORT;
        Map<String, String> queryParam = new HashMap<>(16);
        queryParam.put("monitorIds", monitorIds);
        startTime = DateUtil.formatDate(startTime, beforeFormat, DateUtil.DATE_YMD_FORMAT);
        queryParam.put("startTime", startTime);
        endTime = DateUtil.formatDate(endTime, beforeFormat, DateUtil.DATE_YMD_FORMAT);
        queryParam.put("endTime", endTime);
        String queryResult = HttpClientUtil.send(PaasCloudUrlEnum.DRIVING_MILEAGE_STATISTICS_URL, queryParam);

        JSONObject queryResultJsonObj = JSON.parseObject(queryResult);
        if (queryResultJsonObj == null || queryResultJsonObj.getInteger("code") != 10000) {
            return new JsonResultBean(JsonResultBean.FAULT, "查询数据异常！");
        }
        List<DrivingMileageStatistics> result = new ArrayList<>();
        List<DrivingMileageStatistics> mileageStatisticsList =
            JSONObject.parseArray(queryResultJsonObj.getString("data"), DrivingMileageStatistics.class);
        Map<String, DrivingMileageStatistics> monitorDrivingMileageStatisticsMap = mileageStatisticsList.stream()
            .collect(Collectors.toMap(DrivingMileageStatistics::getMonitorId, info -> info));
        Set<String> queryExistMonitorIdList = monitorDrivingMileageStatisticsMap.keySet();
        Set<String> monitorIdList = Arrays.stream(monitorIds.split(",")).collect(Collectors.toSet());
        Map<String, BindDTO> bindInfoMap = VehicleUtil.batchGetBindInfosByRedis(monitorIdList);
        Map<String, RedisKey> redisKeyMap = new HashMap<>(16);
        for (String moId : monitorIdList) {
            redisKeyMap.put(moId, HistoryRedisKeyEnum.SENSOR_MESSAGE.of(moId));
        }
        Map<String, String> sensorMessageMap = RedisHelper.batchGetStringMap(redisKeyMap);
        Map<String, Double> assignMileageData = new HashMap<>(16);
        for (String monitorId : monitorIdList) {
            // 平台查询:对于没有行驶统计数据的车辆需要补全基本信息返回页面
            // APP查询:对于没有行驶统计数据的车辆直接不反回
            if (isAppSearch && !queryExistMonitorIdList.contains(monitorId)) {
                continue;
            }
            BindDTO bindDTO = bindInfoMap.get(monitorId);
            if (bindDTO == null) {
                continue;
            }
            DrivingMileageStatistics drivingMileageStatistics =
                monitorDrivingMileageStatisticsMap.getOrDefault(monitorId, new DrivingMileageStatistics());
            // 组装行驶统计数据
            boolean isBindMileageSensor = sensorMessageMap.get(monitorId) != null;
            assembleMileageStatistics(isAppSearch, isBindMileageSensor, monitorId, drivingMileageStatistics, bindDTO);
            result.add(drivingMileageStatistics);
            Double totalMile = drivingMileageStatistics.getTotalMile();
            String groupNames = bindDTO.getGroupName();
            // 组装分组里程
            String[] assignmentNameArr = groupNames.split(",");
            for (String assignName : assignmentNameArr) {
                assignMileageData.put(assignName,
                    assignMileageData.getOrDefault(assignName, 0.0) + (totalMile != null ? totalMile : 0.0));
            }
        }
        String userUuid = userService.getCurrentUserUuid();
        RedisKey redisKey = HistoryRedisKeyEnum.USER_DRIVING_MILEAGE_STATISTICS_ASSIGN_MILEAGE_DATA.of(userUuid);
        RedisHelper.addMapToHash(redisKey, assignMileageData);
        RedisHelper.expireKey(redisKey, RedisHelper.SIX_HOUR_REDIS_EXPIRE);
        return new JsonResultBean(result);
    }

    /**
     * 组装行驶统计数据
     * @param isAppSearch         是否app搜索
     * @param isBindMileageSensor 是否绑定里程传感器
     * @param monitorId           监控对象id
     * @param bindDTO          监控对象信息配置参数
     */
    private void assembleMileageStatistics(boolean isAppSearch, boolean isBindMileageSensor, String monitorId,
        DrivingMileageStatistics drivingMileageStatistics, BindDTO bindDTO) {
        drivingMileageStatistics.setMonitorId(monitorId);
        drivingMileageStatistics.setMonitorName(bindDTO.getName());
        drivingMileageStatistics.setGroupName(bindDTO.getOrgName());
        drivingMileageStatistics.setAssignmentName(bindDTO.getGroupName());
        String monitorType = bindDTO.getMonitorType();
        drivingMileageStatistics.setMonitorType(MonitorUtils.getMonitorTypeName(monitorType));
        Integer plateColor = bindDTO.getPlateColor();
        drivingMileageStatistics
            .setSignColor(plateColor != null ? VehicleUtil.getPlateColorStr(String.valueOf(plateColor)) : null);
        Long duration = isBindMileageSensor ? drivingMileageStatistics.getSensorDuration() :
            drivingMileageStatistics.getDeviceDuration();
        drivingMileageStatistics.setDuration(duration);
        if (isAppSearch) {
            drivingMileageStatistics
                .setDurationStr(duration != null ? LocalDateUtils.formatHour(duration * 1000) : null);
        } else {
            drivingMileageStatistics
                .setDurationStr(duration != null ? LocalDateUtils.formatDuring(duration * 1000) : null);
        }
        drivingMileageStatistics.setTotalMile(
            isBindMileageSensor ? drivingMileageStatistics.getSensorMile() : drivingMileageStatistics.getDeviceMile());
        drivingMileageStatistics.setTravelNum(isBindMileageSensor ? drivingMileageStatistics.getSensorTravelNum() :
            drivingMileageStatistics.getDeviceTravelNum());
    }

    /**
     * 行驶里程明细
     * @param monitorId 监控对象id
     * @param startTime 开始日期(yyyy-MM-dd)
     * @param endTime   结束日期(yyyy-MM-dd)
     */
    @Override
    public JsonResultBean getDrivingMileageDetails(String monitorId, String startTime, String endTime) {
        String userUuid = userService.getCurrentUserUuid();
        RedisKey redisKey = HistoryRedisKeyEnum.USER_DRIVING_MILEAGE_DETAILS_LIST.of(userUuid);
        if (RedisHelper.isContainsKey(redisKey)) {
            RedisHelper.delete(redisKey);
        }
        if (StringUtils.isBlank(monitorId) || StringUtils.isBlank(startTime) || StringUtils.isBlank(endTime)) {
            return new JsonResultBean(JsonResultBean.FAULT, "参数错误！");
        }
        startTime = startTime.replaceAll("-", "") + "000000";
        endTime = endTime.replaceAll("-", "") + "235959";
        Map<String, String> queryParam = new HashMap<>(16);
        queryParam.put("startTime", startTime);
        queryParam.put("monitorId", monitorId);
        queryParam.put("endTime", endTime);
        // 数据类型(0: gps 1:里程传感器)
        queryParam.put("type", RedisHelper.isContainsKey(HistoryRedisKeyEnum.SENSOR_MESSAGE.of(monitorId)) ? "1" : "0");
        String queryResult = HttpClientUtil.send(PaasCloudUrlEnum.DRIVING_MILEAGE_DETAILS_URL, queryParam);
        JSONObject queryResultJsonObj = JSON.parseObject(queryResult);
        if (queryResultJsonObj == null || queryResultJsonObj.getInteger("code") != 10000) {
            return new JsonResultBean(JsonResultBean.FAULT, "查询数据异常！");
        }
        List<DrivingMileageDetails> drivingMileageDetailsList =
            JSONObject.parseArray(queryResultJsonObj.getString("data"), DrivingMileageDetails.class);
        if (CollectionUtils.isNotEmpty(drivingMileageDetailsList)) {
            for (DrivingMileageDetails drivingMileageDetails : drivingMileageDetailsList) {
                String startLocation = drivingMileageDetails.getStartLocation();
                drivingMileageDetails.setStartAddress(unknownLocation);
                if (StringUtils.isNotBlank(startLocation)) {
                    String[] startLocations = startLocation.split(",");
                    String address = positionalService.getAddress(startLocations[0], startLocations[1]);
                    drivingMileageDetails.setStartAddress(address);
                }
                String endLocation = drivingMileageDetails.getEndLocation();
                drivingMileageDetails.setEndAddress(unknownLocation);
                if (StringUtils.isNotBlank(endLocation)) {
                    String[] endLocations = endLocation.split(",");
                    String address = positionalService.getAddress(endLocations[0], endLocations[1]);
                    drivingMileageDetails.setEndAddress(address);
                }
            }
            RedisHelper.addToList(redisKey, drivingMileageDetailsList);
            RedisHelper.expireKey(redisKey, RedisHelper.SIX_HOUR_REDIS_EXPIRE);
        }
        return new JsonResultBean(drivingMileageDetailsList);
    }

    /**
     * 行驶里程位置明细
     * @param monitorId 监控对象id
     * @param startTime 开始日期
     * @param endTime   结束日期
     */
    @Override
    public JsonResultBean getDrivingMileageLocationDetails(String monitorId, String startTime, String endTime,
        Integer reissue) {
        String userUuid = userService.getCurrentUserUuid();
        RedisKey redisKey = HistoryRedisKeyEnum.USER_DRIVING_MILEAGE_LOCATION_DETAILS_LIST.of(userUuid);
        if (RedisHelper.isContainsKey(redisKey)) {
            RedisHelper.delete(redisKey);
        }
        if (StringUtils.isBlank(startTime) || StringUtils.isBlank(monitorId) || StringUtils.isBlank(endTime)) {
            return new JsonResultBean(JsonResultBean.FAULT, "参数错误！");
        }
        Map<String, String> queryParam = new HashMap<>(6);
        queryParam.put("monitorId", monitorId);
        queryParam.put("startTime", startTime.replaceAll("-", "").replaceAll(" ", "").replaceAll(":", ""));
        queryParam.put("endTime", endTime.replaceAll("-", "").replaceAll(" ", "").replaceAll(":", ""));
        if (Objects.nonNull(reissue)) {
            queryParam.put("reissueFlag", String.valueOf(reissue));
        }
        String queryResult = HttpClientUtil.send(PaasCloudUrlEnum.POSITIONAL_HISTORY_URL, queryParam);
        PaasCloudZipDTO dto = null;
        if (!StringUtils.isEmpty(queryResult)) {
            dto = JSON.parseObject(queryResult, PaasCloudZipDTO.class);
        }
        if (dto == null) {
            return new JsonResultBean(JsonResultBean.FAULT, "查询数据异常！");
        }
        String data = dto.getData();
        if (StringUtils.isEmpty(data)) {
            return new JsonResultBean((new ArrayList<>()));
        }
        List<DrivingMileageLocationDetails> drivingMileageLocationDetailsList =
            JSON.parseArray(data, DrivingMileageLocationDetails.class);
        if (CollectionUtils.isEmpty(drivingMileageLocationDetailsList)) {
            return new JsonResultBean(new ArrayList<>());
        }
        for (DrivingMileageLocationDetails drivingMileageLocationDetails : drivingMileageLocationDetailsList) {
            String status = drivingMileageLocationDetails.getStatus();
            if (StringUtils.isNotBlank(status)) {
                Integer accStatus = CalculateUtil.getStatus(status).getInteger("acc");
                drivingMileageLocationDetails.setAccStatusStr(Objects.equals(accStatus, 0) ? "关" : "开");
            }
            drivingMileageLocationDetails
                .setTimeStr(DateUtil.getLongToDateStr(drivingMileageLocationDetails.getTime() * 1000, null));
        }
        RedisHelper.addToList(redisKey, drivingMileageLocationDetailsList);
        RedisHelper.expireKey(redisKey, RedisHelper.SIX_HOUR_REDIS_EXPIRE);
        return new JsonResultBean(drivingMileageLocationDetailsList);
    }

    /**
     * 部标监管报表-行驶里程报表 导出
     * @param monitorId  监控对象id
     * @param startTime  开始日期
     * @param endTime    结束日期
     * @param exportType 2:行驶统计;3:行驶明细;4:位置明细;
     * @param queryParam 查询参数
     */
    @Override
    public void exportDrivingMileage(HttpServletResponse response, String monitorId, String startTime, String endTime,
        Integer exportType, String queryParam, Integer reissue) throws Exception {
        if (Objects.equals(exportType, 2)) {
            exportDrivingMileageStatistics(response, monitorId, startTime, endTime, queryParam);
        }
        if (Objects.equals(exportType, 3)) {
            exportDrivingMileageDetails(response, monitorId, startTime, endTime);
        }
        if (Objects.equals(exportType, 4)) {
            exportDrivingMileageLocationDetails(response, monitorId, startTime, endTime, reissue);
        }
    }

    /**
     * 导出行驶里程统计
     */
    private void exportDrivingMileageStatistics(HttpServletResponse response, String monitorId, String startTime,
        String endTime, String queryParam) throws Exception {
        List<DrivingMileageStatistics> mileageStatisticsList = new ArrayList<>();
        JsonResultBean getMileageStatisticsResult = getDrivingMileageStatistics(monitorId, startTime, endTime, false);
        if (getMileageStatisticsResult.isSuccess()) {
            mileageStatisticsList =
                JSON.parseArray(JSON.toJSONString(getMileageStatisticsResult.getObj()), DrivingMileageStatistics.class);
        } else {
            log.error("导出行驶统计查询数据异常！");
        }
        if (StringUtils.isNotBlank(queryParam)) {
            mileageStatisticsList.removeIf(info -> !info.getMonitorName().contains(queryParam));
        }
        ExportExcelUtil.setResponseHead(response, "行驶报表(行驶统计)");
        ExportExcelUtil.export(
            new ExportExcelParam(null, 1, mileageStatisticsList, DrivingMileageStatistics.class, null,
                response.getOutputStream()));
    }

    /**
     * 导出行驶里程明细
     */
    private void exportDrivingMileageDetails(HttpServletResponse response, String monitorId, String startTime,
        String endTime) throws IOException {
        List<DrivingMileageDetails> drivingMileageDetailsList = new ArrayList<>();
        String userUuid = userService.getCurrentUserUuid();
        RedisKey redisKey = HistoryRedisKeyEnum.USER_DRIVING_MILEAGE_DETAILS_LIST.of(userUuid);
        // 先查询是否有缓存;有的话从缓存取 没有直接查询
        if (RedisHelper.isContainsKey(redisKey)) {
            drivingMileageDetailsList = RedisHelper.getList(redisKey, DrivingMileageDetails.class);
        } else {
            JsonResultBean getDrivingMileageDetailsResult = getDrivingMileageDetails(monitorId, startTime, endTime);
            if (getDrivingMileageDetailsResult.isSuccess()) {
                drivingMileageDetailsList = JSON.parseArray(JSON.toJSONString(getDrivingMileageDetailsResult.getObj()),
                    DrivingMileageDetails.class);
            } else {
                log.error("导出行驶明细查询数据异常！");
            }
        }
        ExportExcelUtil.setResponseHead(response, "行驶报表(行驶明细)");
        ExportExcelUtil.export(
            new ExportExcelParam(null, 1, drivingMileageDetailsList, DrivingMileageDetails.class, null,
                response.getOutputStream()));
    }

    /**
     * 导出行驶里程位置明细
     */
    private void exportDrivingMileageLocationDetails(HttpServletResponse response, String monitorId, String startTime,
        String endTime, Integer reissue) throws IOException {
        List<DrivingMileageLocationDetails> drivingMileageLocationDetailsList = new ArrayList<>();
        String userUuid = userService.getCurrentUserUuid();
        RedisKey redisKey = HistoryRedisKeyEnum.USER_DRIVING_MILEAGE_LOCATION_DETAILS_LIST.of(userUuid);
        // 先查询是否有缓存;有的话从缓存取 没有直接查询
        if (!RedisHelper.isContainsKey(redisKey)) {
            JsonResultBean getDrivingMileageLocationDetailsResult =
                getDrivingMileageLocationDetails(monitorId, startTime, endTime, reissue);
            if (getDrivingMileageLocationDetailsResult.isSuccess()) {
                drivingMileageLocationDetailsList =
                    JSON.parseArray(JSON.toJSONString(getDrivingMileageLocationDetailsResult.getObj()),
                        DrivingMileageLocationDetails.class);
            } else {
                log.error("导出位置明细查询数据异常！");
            }
        } else {
            drivingMileageLocationDetailsList = RedisHelper.getList(redisKey, DrivingMileageLocationDetails.class);
        }
        if (CollectionUtils.isNotEmpty(drivingMileageLocationDetailsList)) {
            for (DrivingMileageLocationDetails drivingMileageLocationDetails : drivingMileageLocationDetailsList) {
                String longitude = drivingMileageLocationDetails.getLongitude();
                String latitude = drivingMileageLocationDetails.getLatitude();
                if (StringUtils.isNotEmpty(longitude) && StringUtils.isNotEmpty(latitude)) {
                    String address = positionalService.getAddress(longitude, latitude);
                    drivingMileageLocationDetails.setAddress(address);
                }
            }
        }
        ExportExcelUtil.setResponseHead(response, "行驶报表(位置明细)");
        ExportExcelUtil.export(
            new ExportExcelParam(null, 1, drivingMileageLocationDetailsList, DrivingMileageLocationDetails.class, null,
                response.getOutputStream()));
    }
}
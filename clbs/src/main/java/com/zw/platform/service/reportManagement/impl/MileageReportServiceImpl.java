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

    // ????????????????????????????????????
    @Override
    public List<MileageReport> findMileageById(List<String> moIds, long startTimeL, long endTimeL) {
        //???uuid?????????id?????????byte??????
        // ??????????????????
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
        // ???????????????????????????????????????0?????????????????????
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
        // ?????????????????????
        List<BigDataReportQuery> queries = BigDataQueryUtil.getBigDataReportQuery(vcId, startTime, endTime);
        // ????????????
        for (BigDataReportQuery query : queries) {
            try {
                List<MileageReport> mileageDataList = getMileageData(query);
                // ??????????????????
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
                // ??????????????????
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
     * list?????????????????????
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
     * ?????????????????????????????????????????????????????????, ?????????"???????????????????????????"
     * ?????????????????????
     * ??????????????????????????????????????????"
     * @param resultList        ????????????
     * @param positionalList    ????????????
     * @param mark              ??????: 0: ??????; 1: ??????;
     * @param statusChangeTimes ???????????????????????????or??????????????????;
     *                          mark = 0, statusChangeTimes = 3; mark = 1, statusChangeTimes = 5;
     * @param isAppSearch       ??????????????????????????????; true: App??????(h), false ????????????(xx??????xxf??????xx???)
     */
    @Override
    public void getTravelOrStopData(List<TravelDetail> resultList, List<PositionalDetail> positionalList, int mark,
        int statusChangeTimes, boolean isAppSearch) {
        // ???????????????
        CommonUtil.getFilterPositionalDetail(positionalList);
        final int timeDifference = 5 * 60 * 1000;
        // ????????????????????????????????????????????????
        PositionalDetail positionalDetail = positionalList.get(0);
        String monitorId = positionalDetail.getMonitorId();
        if (StringUtils.isEmpty(monitorId)) {
            byte[] vehicleId = positionalDetail.getVehicleId();
            monitorId = UuidUtils.getUUIDFromBytes(vehicleId).toString();
        }
        boolean flogKey = RedisHelper.isContainsKey(HistoryRedisKeyEnum.SENSOR_MESSAGE.of(monitorId));

        // true: ????????????; false: ????????????
        boolean isTravelStatus = (mark == TravelBaseInfo.STATUS_TRAVEL);
        // ??????
        Double speed = getSpeed(positionalDetail, flogKey);

        TravelDetail travelDetail = null;
        // ??????????????????
        TravelBaseInfo travelBaseInfo =
            new TravelBaseInfo(speed, isTravelStatus, timeDifference, flogKey, positionalDetail.getVtime());
        // ????????????????????????
        TravelBaseInfoCaretaker caretaker = new TravelBaseInfoCaretaker();
        TravelBaseInfoOriginator originator = new TravelBaseInfoOriginator();
        // ????????????????????????: speed >= 5km????????????, speed <5km ????????????
        boolean currentStatusFlag;

        for (PositionalDetail positional : positionalList) {
            // ???????????????5?????? ??????????????????(??????????????????????????????????????????, ???????????????????????????)
            // ??????: ??????3????????????, ???????????????
            // ??????: ??????5??????????????????, ??????????????????
            long lastVtime = positional.getVtime() * 1000;
            Double lastGpsMile = getLastGpsMile(travelBaseInfo, positional);
            travelBaseInfo.setTimeAndMileDifferenceFlag(lastVtime, lastGpsMile);
            speed = getSpeed(positional, travelBaseInfo.getFlogKey());
            currentStatusFlag = isCurrentStatusFlag(travelBaseInfo, speed);
            String location = positional.getLongtitude() + "," + positional.getLatitude();
            Integer countPosition = travelBaseInfo.getCountPosition();

            // ??????????????????????????????5??????, ????????????????????????????????????
            if (travelBaseInfo.getTwoPointTimeDifferenceFlag()) {
                if (countPosition > 0 && currentStatusFlag) {
                    // 1.?????????????????????, ?????????, ???????????????????????????????????????1
                    setPreTravelDetail(resultList, isAppSearch, travelDetail, travelBaseInfo, caretaker, originator);
                }
                travelBaseInfo.setFirstPositionStatus(true);
                // 2.?????????????????????????????????, ??????????????????????????????????????????????????????????????????
                travelDetail =
                    getCurrentTravelDetail(travelBaseInfo, positional, lastVtime, location, lastGpsMile, speed);
            } else if (currentStatusFlag) {
                if (countPosition == 0) {
                    travelDetail = getTravelDetail(travelBaseInfo, positional, lastVtime, location);
                }
                setTravelBaseInfo(travelBaseInfo, countPosition, positional, location);
            } else {
                // ???: ??????status = mark, ??????????????????????????????????????????(travelBaseInfo),
                // ?????????????????????????????????(1.???????????????: ??????5?????????????????????"????????????", 2.???????????????: ??????3??????????????????"????????????"
                // )??????????????????????????????,????????????
                if (travelBaseInfo.getStatus() == mark) {
                    setTravelBaseInfo(travelBaseInfo, countPosition, positional, location);
                    setFirstStatusChangeData(travelBaseInfo, caretaker, originator, isTravelStatus);
                } else {
                    if (travelBaseInfo.getFirstPositionStatus()) {
                        // ??????????????????"??????"or"??????"?????????????????????
                        setPreTravelDetail(resultList, isAppSearch, travelDetail, travelBaseInfo, caretaker,
                            originator);
                        travelBaseInfo.setFirstPositionStatus(false);
                    } else if (countPosition >= statusChangeTimes) {
                        setPreTravelDetail(resultList, isAppSearch, travelDetail, travelBaseInfo, caretaker,
                            originator);
                    }
                    // ?????????????????????"?????? or ??????",????????????????????????
                    TravelBaseInfo.initTravelBaseInfo(travelBaseInfo);
                }
            }
            travelBaseInfo.setPreTime(lastVtime);
        }
        // 4.????????????????????????????????????????????????(??????????????????countPosition > 3), ??????????????????
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
     * ????????????????????????,?????????????????????????????????
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
     * ????????????????????????????????????
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
     * ???????????????????????????????????????
     */
    private boolean isCurrentStatusFlag(TravelBaseInfo travelBaseInfo, Double speed) {
        boolean currentStatusFlag;
        if (travelBaseInfo.getIsTravelStatus()) {
            // ????????????: currentStatusFlag = true ????????????, ???????????????
            currentStatusFlag = TravelBaseInfo.checkIsTravelFlag(speed);
        } else {
            // ????????????: currentStatusFlag = true ????????????, ???????????????
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
        // 1.?????????????????????5km/h???????????????, ???????????????????????????????????????????????????????????????
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
        // ??????
        travelBaseInfo.setLastGpsMile(Objects.nonNull(lastGpsMile) ? lastGpsMile : 0.0);
        // ??????
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
        // ????????????
        String gpsMile;
        // ???????????????????????????????????????????????????
        if (travelBaseInfo.getFlogKey()) {
            gpsMile = positional.getMileageTotal();
        } else {
            gpsMile = positional.getGpsMile();
        }
        // ?????????????????????????????????????????????
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
     * ????????????
     * @param travelBaseInfo    travelBaseInfo
     * @param currentStatusFlag isTravelStatus= true: currentStatusFlag = true,??????????????????, currentStatusFlag = false:????????????
     *                          isTravelStatus= false: currentStatusFlag = true,??????????????????
     */
    private void judgeStatus(TravelBaseInfo travelBaseInfo, boolean currentStatusFlag) {
        Integer stopCountTimes = travelBaseInfo.getStopCountTimes();
        Integer travelCountTimes = travelBaseInfo.getTravelCountTimes();
        Integer status = travelBaseInfo.getStatus();
        if (travelBaseInfo.getIsTravelStatus()) {
            // ??????
            if (currentStatusFlag) {
                setTravelCountTimes(travelBaseInfo, travelCountTimes, status);
            } else {
                setStopCountTimes(travelBaseInfo, stopCountTimes, status);
            }
        } else {
            // ??????
            if (currentStatusFlag) {
                setStopCountTimes(travelBaseInfo, stopCountTimes, status);
            } else {
                setTravelCountTimes(travelBaseInfo, travelCountTimes, status);
            }
        }

    }

    /**
     * @param travelBaseInfo travelBaseInfo
     * @param stopCountTimes ????????????
     * @param status         true: ??????; false: ??????
     */
    private void setStopCountTimes(TravelBaseInfo travelBaseInfo, Integer stopCountTimes, Integer status) {
        // 1.??????????????????"??????"
        if (status == TravelBaseInfo.STATUS_TRAVEL) {
            // 2.???????????????????????????"??????",???????????????+1
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
     * @param travelCountTimes ????????????
     * @param status           true: ??????; false: ??????
     */
    private void setTravelCountTimes(TravelBaseInfo travelBaseInfo, Integer travelCountTimes, Integer status) {
        // 1.??????????????????"??????"
        if (status == TravelBaseInfo.STATUS_STOP) {
            // 2.???????????????????????????"??????",???????????????+1
            travelCountTimes += 1;
            travelBaseInfo.setTravelCountTimes(travelCountTimes);
            // 3.???????????????5????????????????????????????????????: ??????????????????????????????????????????
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
     * ???????????? = ?????????????????? + ??????????????????????????????
     * @param travelDetail mileageReport
     */
    private void calculateMile(TravelBaseInfo travelBaseInfo, TravelDetail travelDetail) {
        // ???????????????????????????????????????5??????, ???????????????????????????????????? - ???????????????????????????
        double totalMile = travelDetail.getTotalMile();
        // ???????????????????????????
        double travelMile = BigDecimal.valueOf(travelBaseInfo.getLastGpsMile()).subtract(BigDecimal.valueOf(totalMile))
            .setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        travelDetail.setTotalGpsMile(travelMile);

        // ???????????????????????????
        double preTravelMile = BigDecimal.valueOf(travelMile).add(BigDecimal.valueOf(travelBaseInfo.getPreTravelMile()))
            .setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        travelDetail.setAccumulatedGpsMile(preTravelMile);
        travelBaseInfo.setPreTravelMile(preTravelMile);
    }

    private void addMileageReportToList(List<TravelDetail> resultList, TravelBaseInfo travelBaseInfo,
        TravelDetail travelDetail, boolean isAppSearch) {
        long travelTime = travelBaseInfo.getTravelTimeByTwoPointTimeDifferenceFlag();
        // x???x??????x???x???(x=0???????????????????????????)
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
     * ????????????????????????
     * @param monitorIds  ????????????id ????????????
     * @param startTime   ????????????
     * @param endTime     ????????????
     * @param isAppSearch ?????????app??????
     */
    @Override
    public JsonResultBean getDrivingMileageStatistics(String monitorIds, String startTime, String endTime,
        boolean isAppSearch) throws Exception {
        if (StringUtils.isBlank(monitorIds) || StringUtils.isBlank(startTime) || StringUtils.isBlank(endTime)) {
            return new JsonResultBean(JsonResultBean.FAULT, "???????????????");
        }
        // 2:????????????????????????yyyy-MM-dd 1:????????????????????????yyyy-MM-dd HH:mm:ss
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
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????");
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
            // ????????????:???????????????????????????????????????????????????????????????????????????
            // APP??????:??????????????????????????????????????????????????????
            if (isAppSearch && !queryExistMonitorIdList.contains(monitorId)) {
                continue;
            }
            BindDTO bindDTO = bindInfoMap.get(monitorId);
            if (bindDTO == null) {
                continue;
            }
            DrivingMileageStatistics drivingMileageStatistics =
                monitorDrivingMileageStatisticsMap.getOrDefault(monitorId, new DrivingMileageStatistics());
            // ????????????????????????
            boolean isBindMileageSensor = sensorMessageMap.get(monitorId) != null;
            assembleMileageStatistics(isAppSearch, isBindMileageSensor, monitorId, drivingMileageStatistics, bindDTO);
            result.add(drivingMileageStatistics);
            Double totalMile = drivingMileageStatistics.getTotalMile();
            String groupNames = bindDTO.getGroupName();
            // ??????????????????
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
     * ????????????????????????
     * @param isAppSearch         ??????app??????
     * @param isBindMileageSensor ???????????????????????????
     * @param monitorId           ????????????id
     * @param bindDTO          ??????????????????????????????
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
     * ??????????????????
     * @param monitorId ????????????id
     * @param startTime ????????????(yyyy-MM-dd)
     * @param endTime   ????????????(yyyy-MM-dd)
     */
    @Override
    public JsonResultBean getDrivingMileageDetails(String monitorId, String startTime, String endTime) {
        String userUuid = userService.getCurrentUserUuid();
        RedisKey redisKey = HistoryRedisKeyEnum.USER_DRIVING_MILEAGE_DETAILS_LIST.of(userUuid);
        if (RedisHelper.isContainsKey(redisKey)) {
            RedisHelper.delete(redisKey);
        }
        if (StringUtils.isBlank(monitorId) || StringUtils.isBlank(startTime) || StringUtils.isBlank(endTime)) {
            return new JsonResultBean(JsonResultBean.FAULT, "???????????????");
        }
        startTime = startTime.replaceAll("-", "") + "000000";
        endTime = endTime.replaceAll("-", "") + "235959";
        Map<String, String> queryParam = new HashMap<>(16);
        queryParam.put("startTime", startTime);
        queryParam.put("monitorId", monitorId);
        queryParam.put("endTime", endTime);
        // ????????????(0: gps 1:???????????????)
        queryParam.put("type", RedisHelper.isContainsKey(HistoryRedisKeyEnum.SENSOR_MESSAGE.of(monitorId)) ? "1" : "0");
        String queryResult = HttpClientUtil.send(PaasCloudUrlEnum.DRIVING_MILEAGE_DETAILS_URL, queryParam);
        JSONObject queryResultJsonObj = JSON.parseObject(queryResult);
        if (queryResultJsonObj == null || queryResultJsonObj.getInteger("code") != 10000) {
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????");
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
     * ????????????????????????
     * @param monitorId ????????????id
     * @param startTime ????????????
     * @param endTime   ????????????
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
            return new JsonResultBean(JsonResultBean.FAULT, "???????????????");
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
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????");
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
                drivingMileageLocationDetails.setAccStatusStr(Objects.equals(accStatus, 0) ? "???" : "???");
            }
            drivingMileageLocationDetails
                .setTimeStr(DateUtil.getLongToDateStr(drivingMileageLocationDetails.getTime() * 1000, null));
        }
        RedisHelper.addToList(redisKey, drivingMileageLocationDetailsList);
        RedisHelper.expireKey(redisKey, RedisHelper.SIX_HOUR_REDIS_EXPIRE);
        return new JsonResultBean(drivingMileageLocationDetailsList);
    }

    /**
     * ??????????????????-?????????????????? ??????
     * @param monitorId  ????????????id
     * @param startTime  ????????????
     * @param endTime    ????????????
     * @param exportType 2:????????????;3:????????????;4:????????????;
     * @param queryParam ????????????
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
     * ????????????????????????
     */
    private void exportDrivingMileageStatistics(HttpServletResponse response, String monitorId, String startTime,
        String endTime, String queryParam) throws Exception {
        List<DrivingMileageStatistics> mileageStatisticsList = new ArrayList<>();
        JsonResultBean getMileageStatisticsResult = getDrivingMileageStatistics(monitorId, startTime, endTime, false);
        if (getMileageStatisticsResult.isSuccess()) {
            mileageStatisticsList =
                JSON.parseArray(JSON.toJSONString(getMileageStatisticsResult.getObj()), DrivingMileageStatistics.class);
        } else {
            log.error("???????????????????????????????????????");
        }
        if (StringUtils.isNotBlank(queryParam)) {
            mileageStatisticsList.removeIf(info -> !info.getMonitorName().contains(queryParam));
        }
        ExportExcelUtil.setResponseHead(response, "????????????(????????????)");
        ExportExcelUtil.export(
            new ExportExcelParam(null, 1, mileageStatisticsList, DrivingMileageStatistics.class, null,
                response.getOutputStream()));
    }

    /**
     * ????????????????????????
     */
    private void exportDrivingMileageDetails(HttpServletResponse response, String monitorId, String startTime,
        String endTime) throws IOException {
        List<DrivingMileageDetails> drivingMileageDetailsList = new ArrayList<>();
        String userUuid = userService.getCurrentUserUuid();
        RedisKey redisKey = HistoryRedisKeyEnum.USER_DRIVING_MILEAGE_DETAILS_LIST.of(userUuid);
        // ????????????????????????;????????????????????? ??????????????????
        if (RedisHelper.isContainsKey(redisKey)) {
            drivingMileageDetailsList = RedisHelper.getList(redisKey, DrivingMileageDetails.class);
        } else {
            JsonResultBean getDrivingMileageDetailsResult = getDrivingMileageDetails(monitorId, startTime, endTime);
            if (getDrivingMileageDetailsResult.isSuccess()) {
                drivingMileageDetailsList = JSON.parseArray(JSON.toJSONString(getDrivingMileageDetailsResult.getObj()),
                    DrivingMileageDetails.class);
            } else {
                log.error("???????????????????????????????????????");
            }
        }
        ExportExcelUtil.setResponseHead(response, "????????????(????????????)");
        ExportExcelUtil.export(
            new ExportExcelParam(null, 1, drivingMileageDetailsList, DrivingMileageDetails.class, null,
                response.getOutputStream()));
    }

    /**
     * ??????????????????????????????
     */
    private void exportDrivingMileageLocationDetails(HttpServletResponse response, String monitorId, String startTime,
        String endTime, Integer reissue) throws IOException {
        List<DrivingMileageLocationDetails> drivingMileageLocationDetailsList = new ArrayList<>();
        String userUuid = userService.getCurrentUserUuid();
        RedisKey redisKey = HistoryRedisKeyEnum.USER_DRIVING_MILEAGE_LOCATION_DETAILS_LIST.of(userUuid);
        // ????????????????????????;????????????????????? ??????????????????
        if (!RedisHelper.isContainsKey(redisKey)) {
            JsonResultBean getDrivingMileageLocationDetailsResult =
                getDrivingMileageLocationDetails(monitorId, startTime, endTime, reissue);
            if (getDrivingMileageLocationDetailsResult.isSuccess()) {
                drivingMileageLocationDetailsList =
                    JSON.parseArray(JSON.toJSONString(getDrivingMileageLocationDetailsResult.getObj()),
                        DrivingMileageLocationDetails.class);
            } else {
                log.error("???????????????????????????????????????");
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
        ExportExcelUtil.setResponseHead(response, "????????????(????????????)");
        ExportExcelUtil.export(
            new ExportExcelParam(null, 1, drivingMileageLocationDetailsList, DrivingMileageLocationDetails.class, null,
                response.getOutputStream()));
    }
}
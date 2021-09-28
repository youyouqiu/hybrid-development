package com.zw.app.service.webMaster.statistics.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Joiner;
import com.zw.app.annotation.AppMethodVersion;
import com.zw.app.annotation.AppServerVersion;
import com.zw.app.controller.AppVersionConstant;
import com.zw.app.service.webMaster.statistics.AppMileageReportService;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.domain.reportManagement.DrivingMileageStatistics;
import com.zw.platform.domain.reportManagement.MileageReport;
import com.zw.platform.service.reportManagement.MileageReportService;
import com.zw.platform.util.LocalDateUtils;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MonitorUtils;
import com.zw.platform.util.common.VehicleUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 行驶里程报表
 * @author zhouzongbo on 2019/1/7 18:21
 */
@Service
@AppServerVersion
public class AppMileageReportServiceImpl implements AppMileageReportService {

    @Autowired
    private MileageReportService mileageReportService;

    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_TWO,
        url = "/clbs/app/statistic/mileageReport/findTravelDetailList")
    public Map<String, Object> findTravelDetailList(String monitorIds, String startTime, String endTime)
        throws Exception {
        Map<String, Object> resultMap = new HashMap<>(16);
        List<String> monitorIdList = VehicleUtil.distinctMonitorIds(monitorIds);
        Map<String, BindDTO> bindDTOMap = MonitorUtils.getBindDTOMap(monitorIdList, "id", "name");
        List<MileageReport> mileageReportList = new ArrayList<>();
        JsonResultBean getMileageStatisticsResult =
            mileageReportService.getDrivingMileageStatistics(monitorIds, startTime, endTime, true);
        if (!getMileageStatisticsResult.isSuccess()) {
            setNonMileageReportList(resultMap, bindDTOMap);
            return resultMap;
        }
        List<DrivingMileageStatistics> mileageStatisticsList =
            JSON.parseArray(JSON.toJSONString(getMileageStatisticsResult.getObj()), DrivingMileageStatistics.class);
        if (CollectionUtils.isEmpty(mileageStatisticsList)) {
            setNonMileageReportList(resultMap, bindDTOMap);
            return resultMap;
        }
        // TODO 由于获取数据的方式改为从pass-cloud获取;返回的字段不一致;所以从新赋值对象返回至APP
        for (DrivingMileageStatistics mileageStatistics : mileageStatisticsList) {
            MileageReport mileageReport = new MileageReport();
            mileageReport.setPlateNumber(mileageStatistics.getMonitorName());
            mileageReport.setGroupName(mileageStatistics.getGroupName());
            mileageReport.setAssignmentName(mileageStatistics.getAssignmentName());
            mileageReport.setVehicleColor(mileageStatistics.getSignColor());
            mileageReport.setMonitorType(mileageStatistics.getMonitorType());
            mileageReport.setTravelTimeStr(mileageStatistics.getDurationStr());
            mileageReport.setTravelNum(mileageStatistics.getTravelNum());
            mileageReport.setVehicleId(mileageStatistics.getMonitorId());
            mileageReport.setMileage(mileageStatistics.getTotalMile());
            mileageReport.setTravelTime(mileageStatistics.getDuration());
            mileageReportList.add(mileageReport);
        }
        // 查询出来的车辆
        List<String> existMonitorIds =
            mileageStatisticsList.stream().map(DrivingMileageStatistics::getMonitorId).collect(Collectors.toList());
        for (Map.Entry<String, BindDTO> entry : bindDTOMap.entrySet()) {
            String monitorId = entry.getKey();
            if (!existMonitorIds.contains(monitorId)) {
                buildMileageReport(mileageReportList, entry.getValue());
            }
        }
        // 完成后根据行驶里程排序
        mileageReportList =
            mileageReportList.stream().sorted(Comparator.comparing(MileageReport::getMileage).reversed())
                .collect(Collectors.toList());
        double totalMileage = mileageReportList.stream().mapToDouble(MileageReport::getMileage).sum();
        BigDecimal totalMileageDecimal = new BigDecimal(totalMileage).setScale(1, RoundingMode.HALF_UP);
        resultMap.put("mileageReportList", mileageReportList);
        // 总行驶里程数
        resultMap.put("totalMileage", totalMileageDecimal.doubleValue());
        setMonitorsMileage(resultMap, mileageReportList);
        return resultMap;
    }

    private void setNonMileageReportList(Map<String, Object> resultMap,
        Map<String, BindDTO> bindDTOMap) {
        List<MileageReport> nonMileageReportList = new ArrayList<>();
        List<String> monitors = new ArrayList<>();
        buildConfig(bindDTOMap, nonMileageReportList, monitors);
        resultMap.put("mileageReportList", nonMileageReportList);
        resultMap.put("totalMileage", 0);
        resultMap.put("maxMileage", 0);
        resultMap.put("maxMileageMonitors", Joiner.on(",").join(monitors));
        resultMap.put("minMileage", 0);
        resultMap.put("minMileageMonitors", Joiner.on(",").join(monitors));
    }

    private void buildConfig(Map<String, BindDTO> bindDTOMap,
        List<MileageReport> nonMileageReportList, List<String> monitors) {
        for (BindDTO bindDTO : bindDTOMap.values()) {
            buildMileageReport(nonMileageReportList, bindDTO);
            monitors.add(bindDTO.getName());
        }
    }

    private void buildMileageReport(List<MileageReport> nonMileageReportList, BindDTO bindDTO) {
        MileageReport mileageReport = new MileageReport();
        mileageReport.setPlateNumber(bindDTO.getName());
        mileageReport.setMileage((double) 0);
        mileageReport.setVehicleId(bindDTO.getId());
        nonMileageReportList.add(mileageReport);
    }

    private void setMonitorsMileage(Map<String, Object> resultMap, List<MileageReport> mileageReportList) {
        //最高行驶里程及对应的监控对象
        List<String> maxMileageMonitors = new ArrayList<>();
        List<String> minMileageMonitors = new ArrayList<>();
        MileageReport maxMileageReport = mileageReportList.get(0);
        double maxMileage = maxMileageReport.getMileage();

        MileageReport minMileageReport = mileageReportList.get(mileageReportList.size() - 1);
        double minMileage = minMileageReport.getMileage();

        for (MileageReport mileageReport : mileageReportList) {
            if (maxMileage == mileageReport.getMileage()) {
                maxMileageMonitors.add(mileageReport.getPlateNumber());
            }
            if (minMileage == mileageReport.getMileage()) {
                minMileageMonitors.add(mileageReport.getPlateNumber());
            }
        }

        resultMap.put("maxMileage", maxMileage);
        resultMap.put("maxMileageMonitors", Joiner.on(",").join(maxMileageMonitors));
        //最低行驶里程及对应的监控对象

        resultMap.put("minMileage", minMileage);
        resultMap.put("minMileageMonitors", Joiner.on(",").join(minMileageMonitors));
    }

    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_TWO,
        url = "/clbs/app/statistic/mileageReport/findSingleMonitorList")
    public Map<String, Object> findSingleMonitorList(String monitorIds, String startTime, String endTime)
        throws Exception {
        Map<String, Object> resultMap = new HashMap<>(16);

        List<String> monitorIdList = VehicleUtil.distinctMonitorIds(monitorIds);
        List<MileageReport> mileageReportList =
            mileageReportService.getSingleMonitorMileageData(monitorIdList, startTime, endTime);
        List<String> dateList = LocalDateUtils.getBetweenDate(startTime.substring(0, 10), endTime.substring(0, 10));
        BindDTO bindDTO = MonitorUtils.getBindDTO(monitorIds, "id", "name");
        if (CollectionUtils.isNotEmpty(mileageReportList)) {

            double totalMileage = 0.0;
            double totalOilWearOne = 0.0;
            long totalTravelTime = 0L;
            int totalTravelNum = 0;
            Double totalOilWear;
            for (MileageReport mileageReport : mileageReportList) {
                double totalGpsMile = mileageReport.getTotalGpsMile() == null ? 0 : mileageReport.getTotalGpsMile();
                mileageReport.setMileage(totalGpsMile);
                totalMileage += totalGpsMile;
                totalOilWear = mileageReport.getTotalOilWearOne();
                totalOilWearOne += totalOilWear == null ? 0 : totalOilWear;
                totalTravelTime += mileageReport.getTravelTime();
                totalTravelNum += mileageReport.getTravelNum();
                Long day = mileageReport.getDay() * 1000;
                String dateStr = LocalDateUtils.dateFormate(new Date(day));
                mileageReport.setDayFormat(dateStr);
                if (dateList.contains(dateStr)) {
                    dateList.remove(dateStr);
                }
            }
            buildNonDataMileage(mileageReportList, dateList, bindDTO);

            mileageReportList = mileageReportList.stream().sorted(Comparator.comparing(MileageReport::getDayFormat))
                .collect(Collectors.toList());

            resultMap.put("mileageReportList", mileageReportList);
            resultMap.put("totalMileage", new BigDecimal(totalMileage).setScale(1, RoundingMode.HALF_UP));
            resultMap.put("totalOilWearOne", new BigDecimal(totalOilWearOne).setScale(1, RoundingMode.HALF_UP));
            resultMap.put("totalTravelTime", LocalDateUtils.formatAppHour(totalTravelTime * 1000));
            resultMap.put("totalTravelNum", totalTravelNum);
        } else {
            setSingleMileageReportList(resultMap, bindDTO, dateList);
        }
        return resultMap;
    }

    private void buildNonDataMileage(List<MileageReport> mileageReportList, List<String> dateList,
        BindDTO bindDTO) {
        if (CollectionUtils.isNotEmpty(dateList)) {
            for (String dateStr : dateList) {
                MileageReport mileageReport = new MileageReport();
                mileageReport.setPlateNumber(bindDTO.getName());
                mileageReport.setMileage((double) 0);
                mileageReport.setVehicleId(bindDTO.getId());
                mileageReport.setDayFormat(dateStr);
                mileageReportList.add(mileageReport);
            }
        }
    }

    private void setSingleMileageReportList(Map<String, Object> resultMap, BindDTO bindDTO,
        List<String> dateList) {
        List<MileageReport> nonMileageReportList = new ArrayList<>();
        buildNonDataMileage(nonMileageReportList, dateList, bindDTO);
        resultMap.put("mileageReportList", nonMileageReportList);
        resultMap.put("totalMileage", 0);
        resultMap.put("totalOilWearOne", 0);
        resultMap.put("totalTravelTime", 0);
        resultMap.put("totalTravelNum", 0);
    }
}
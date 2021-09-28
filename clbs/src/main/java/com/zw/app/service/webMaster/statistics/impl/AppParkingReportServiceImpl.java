package com.zw.app.service.webMaster.statistics.impl;

import com.google.common.base.Joiner;
import com.zw.app.annotation.AppMethodVersion;
import com.zw.app.annotation.AppServerVersion;
import com.zw.app.controller.AppVersionConstant;
import com.zw.app.service.webMaster.statistics.AppParkingReportService;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.domain.reportManagement.ParkingInfo;
import com.zw.platform.dto.reportManagement.ParkingInfoDto;
import com.zw.platform.service.oil.PositionalService;
import com.zw.platform.service.reportManagement.ParkingReportService;
import com.zw.platform.util.LocalDateUtils;
import com.zw.platform.util.common.MonitorUtils;
import com.zw.platform.util.common.VehicleUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zhouzongbo on 2019/1/8 14:12
 */
@Service
@AppServerVersion
public class AppParkingReportServiceImpl implements AppParkingReportService {

    @Autowired
    private ParkingReportService parkingReportService;

    @Autowired
    private PositionalService positionalService;

    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_ONE,
        url = "/clbs/app/statistic/parkingReport/findParkingDetailList")
    public Map<String, Object> findParkingDetailList(String monitorIds, String startTime, String endTime)
        throws Exception {
        Map<String, Object> resultMap = new HashMap<>(16);
        List<String> monitorIdList = VehicleUtil.distinctMonitorIds(monitorIds);
        Map<String, BindDTO> bindDTOMap = MonitorUtils.getBindDTOMap(monitorIdList, "id", "name");

        List<ParkingInfoDto> parkingInfoDtoList =
            parkingReportService.getStopBigDataFromPaas(monitorIds, startTime, endTime, true);
        //新建数据list，将ParkingInfoDto数据转为ParkingInfo数据
        List<ParkingInfo> parkingInfoList = new ArrayList<>();
        for (ParkingInfoDto pid : parkingInfoDtoList) {
            ParkingInfo pif = new ParkingInfo();
            pif.setPhone(pid.getEmployeePhone());
            pif.setPlateNumber(pid.getMonitorName());
            pif.setStopNumber(pid.getStopNum());
            pif.setMonitorId(pid.getMonitorId());
            pif.setStopLocation(pid.getAddress());
            pif.setAssignmentName(pid.getAssignmentName());
            pif.setProfessionalsName(pid.getEmployeeName());
            pif.setStopMile(pid.getIdleSpeedMile());
            pif.setStopTime(pid.getStopTime());
            parkingInfoList.add(pif);
        }
        if (CollectionUtils.isNotEmpty(parkingInfoList)) {
            addParkingInfo(bindDTOMap, parkingInfoList);
            parkingInfoList =
                parkingInfoList.stream().sorted(Comparator.comparing(ParkingInfo::getStopNumber).reversed())
                    .collect(Collectors.toList());
            // 总停止次数
            int totalStopNumber = parkingInfoList.stream().mapToInt(ParkingInfo::getStopNumber).sum();
            resultMap.put("totalStopNumber", totalStopNumber);
            resultMap.put("parkingInfoList", parkingInfoList);
            setParkingStopNumber(resultMap, parkingInfoList);
        } else {
            setNonParkingReportList(resultMap, bindDTOMap);
        }
        return resultMap;
    }

    private void addParkingInfo(Map<String, BindDTO> bindDTOMap, List<ParkingInfo> parkingInfoList) {
        List<String> existMonitorIds =
            parkingInfoList.stream().map(ParkingInfo::getMonitorId).collect(Collectors.toList());
        for (Map.Entry<String, BindDTO> entry : bindDTOMap.entrySet()) {
            String monitorId = entry.getKey();
            if (!existMonitorIds.contains(monitorId)) {
                ParkingInfo parkingInfo = getParkingInfo(entry.getValue(), monitorId);
                parkingInfoList.add(parkingInfo);
            }
        }
    }

    private ParkingInfo getParkingInfo(BindDTO bindDTO, String monitorId) {
        ParkingInfo parkingInfo = new ParkingInfo();
        parkingInfo.setPlateNumber(bindDTO.getName());
        parkingInfo.setStopNumber(0);
        parkingInfo.setMonitorId(monitorId);
        return parkingInfo;
    }

    private void setNonParkingReportList(Map<String, Object> resultMap,
        Map<String, BindDTO> bindDTOMap) {
        List<ParkingInfo> nonParkingReportList = new ArrayList<>();
        List<String> monitors = new ArrayList<>();
        buildConfig(bindDTOMap, nonParkingReportList, monitors);
        resultMap.put("maxStopNumber", 0);
        resultMap.put("minStopNumber", 0);
        resultMap.put("maxParkingMonitors", Joiner.on(",").join(monitors));
        resultMap.put("minParkingMonitors", Joiner.on(",").join(monitors));
        resultMap.put("totalStopNumber", 0);
        resultMap.put("parkingInfoList", nonParkingReportList);
    }

    private void buildConfig(Map<String, BindDTO> bindDTOMap,
        List<ParkingInfo> nonParkingReportList, List<String> monitors) {
        for (BindDTO bindDTO : bindDTOMap.values()) {
            // 如果勾选的车辆没有查询出数据, 默认给一个空值
            ParkingInfo parkingInfo = new ParkingInfo();
            parkingInfo.setPlateNumber(bindDTO.getName());
            parkingInfo.setStopNumber(0);
            parkingInfo.setMonitorId(bindDTO.getId());
            nonParkingReportList.add(parkingInfo);
            monitors.add(bindDTO.getName());
        }
    }

    private void setParkingStopNumber(Map<String, Object> resultMap, List<ParkingInfo> parkingInfoList) {
        List<String> maxParkingMonitors = new ArrayList<>();
        List<String> minParkingMonitors = new ArrayList<>();

        ParkingInfo maxParkingInfo = parkingInfoList.get(0);
        int maxStopNumber = maxParkingInfo.getStopNumber();

        ParkingInfo minParkingInfo = parkingInfoList.get(parkingInfoList.size() - 1);
        int minStopNumber = minParkingInfo.getStopNumber();

        for (ParkingInfo parkingInfo : parkingInfoList) {
            int stopNumber = parkingInfo.getStopNumber();
            String plateNumber = parkingInfo.getPlateNumber();
            if (maxStopNumber == stopNumber) {
                maxParkingMonitors.add(plateNumber);
            }
            if (minStopNumber == stopNumber) {
                minParkingMonitors.add(plateNumber);
            }
        }
        resultMap.put("maxStopNumber", maxStopNumber);
        resultMap.put("minStopNumber", minStopNumber);
        resultMap.put("maxParkingMonitors", Joiner.on(",").join(maxParkingMonitors));
        resultMap.put("minParkingMonitors", Joiner.on(",").join(minParkingMonitors));
    }

    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_TWO,
        url = "/clbs/app/statistic/parkingReport/findSingleMonitorParkingList")
    public Map<String, Object> findSingleMonitorParkingList(String monitorIds, String startTime, String endTime)
        throws Exception {
        Map<String, Object> resultMap = new HashMap<>(16);

        List<String> monitorIdList = VehicleUtil.distinctMonitorIds(monitorIds);
        List<ParkingInfo> parkingInfoList =
            parkingReportService.findSingleMonitorParkingList(monitorIdList, startTime, endTime);
        List<String> dateList = LocalDateUtils.getBetweenDate(startTime.substring(0, 10), endTime.substring(0, 10));
        BindDTO bindDTO = MonitorUtils.getBindDTO(monitorIds, "id", "name");
        if (CollectionUtils.isNotEmpty(parkingInfoList)) {
            int stopNumber = 0;
            long stopTimes = 0L;
            double stopMile = 0L;
            for (ParkingInfo parkingInfo : parkingInfoList) {
                stopNumber += parkingInfo.getStopNumber();
                stopTimes += parkingInfo.getStopTimeMs();
                stopMile += parkingInfo.getStopMile();
                Long day = parkingInfo.getDay() * 1000;
                String dateStr = LocalDateUtils.dateFormate(new Date(day));
                parkingInfo.setDayFormat(dateStr);
                if (dateList.contains(dateStr)) {
                    dateList.remove(dateStr);
                }
            }
            parkingInfoList = parkingInfoList.stream().sorted(Comparator.comparing(ParkingInfo::getDayFormat))
                .collect(Collectors.toList());
            // 最后停止位置, 有0200数据的数据
            ParkingInfo parkingInfo = parkingInfoList.get(parkingInfoList.size() - 1);
            String stopLocation = parkingInfo.getStopLocation();
            String lastStopLocation = "";
            if (StringUtils.isNotEmpty(stopLocation)) {
                String[] stopLocationArr = stopLocation.split(",");
                lastStopLocation = positionalService.getAddress(stopLocationArr[0], stopLocationArr[1]);
            }

            buildNonData(parkingInfoList, dateList, bindDTO);
            parkingInfoList = parkingInfoList.stream().sorted(Comparator.comparing(ParkingInfo::getDayFormat))
                .collect(Collectors.toList());

            resultMap.put("lastStopLocation", lastStopLocation);
            resultMap.put("stopNumber", stopNumber);
            resultMap.put("stopTimes", LocalDateUtils.formatAppHour(stopTimes * 1000));
            resultMap.put("stopMile", stopMile);
            resultMap.put("parkingInfoList", parkingInfoList);
        } else {
            setSingleParkingReportList(resultMap, bindDTO, dateList);
        }
        return resultMap;
    }

    private void buildNonData(List<ParkingInfo> parkingInfoList, List<String> dateList, BindDTO bindDTO) {
        if (CollectionUtils.isNotEmpty(dateList)) {
            for (String dateStr : dateList) {
                ParkingInfo parkingInfo = new ParkingInfo();
                parkingInfo.setStopNumber(0);
                parkingInfo.setMonitorId(bindDTO.getId());
                parkingInfo.setPlateNumber(bindDTO.getName());
                parkingInfo.setDayFormat(dateStr);
                parkingInfoList.add(parkingInfo);
            }
        }
    }

    private void setSingleParkingReportList(Map<String, Object> resultMap, BindDTO bindDTO,
        List<String> dateList) {
        List<ParkingInfo> nonParkingReportList = new ArrayList<>();
        buildNonData(nonParkingReportList, dateList, bindDTO);
        resultMap.put("lastStopLocation", 0);
        resultMap.put("stopNumber", 0);
        resultMap.put("stopTimes", 0);
        resultMap.put("stopMile", 0);
        resultMap.put("parkingInfoList", nonParkingReportList);
    }
}
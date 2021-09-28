package com.zw.app.service.personalCenter.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zw.app.annotation.AppMethodVersion;
import com.zw.app.annotation.AppServerVersion;
import com.zw.app.controller.AppVersionConstant;
import com.zw.app.service.personalCenter.AppTerminalMileageService;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.domain.reportManagement.TerminalMileReport;
import com.zw.platform.domain.reportManagement.TerminalMileageDailyDetails;
import com.zw.platform.domain.reportManagement.TerminalMileageStatistics;
import com.zw.platform.service.reportManagement.TerminalMileageReportService;
import com.zw.platform.util.StrUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MonitorUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/***
 @Author lijie
 @Date 2019/10/10 11:07
 @Description app终端里程统计
 @version 1.0
 **/
@Service
@AppServerVersion
public class AppTerminalMileageServiceImpl implements AppTerminalMileageService {

    @Autowired
    TerminalMileageReportService terminalMileageReportService;

    /**
     * app终端里程统计数据
     * @param vehicleId
     * @param startTime
     * @param endTime
     * @return
     */
    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_SIX, url = {
        "/clbs/app/reportManagement/terminalMileage/list" })
    public JSONObject showMileageReportList(String vehicleId, String startTime, String endTime) {
        JSONObject result = new JSONObject();
        List<TerminalMileReport> resultList = new ArrayList<>();
        List<String> vehicleIdList = Arrays.asList(vehicleId.split(","));
        List<TerminalMileageStatistics> terminalMileageStatisticsList = new ArrayList<>();
        result.put("totalMile", 0.0);
        result.put("highMile", 0.0);
        result.put("highMileVehicle", null);
        result.put("lowMile", 0.0);
        result.put("lowMileVehicle", null);
        startTime = startTime.substring(0, 10);
        endTime = endTime.substring(0, 10);
        JsonResultBean getTerminalMileageStatisticsResult =
            terminalMileageReportService.getTerminalMileageStatistics(vehicleId, startTime, endTime);
        if (getTerminalMileageStatisticsResult.isSuccess()) {
            terminalMileageStatisticsList =
                JSON.parseArray(JSON.toJSONString(getTerminalMileageStatisticsResult.getObj()),
                    TerminalMileageStatistics.class);
        }
        if (CollectionUtils.isNotEmpty(terminalMileageStatisticsList)) {
            double allTotalMile = 0.0;
            terminalMileageStatisticsList.sort((o1, o2) -> o2.getTotalMile().compareTo(o1.getTotalMile()));

            for (TerminalMileageStatistics terminalMileageStatistics : terminalMileageStatisticsList) {
                Double totalMile = terminalMileageStatistics.getTotalMile();
                allTotalMile += (totalMile != null ? totalMile : 0.0);
                TerminalMileReport terminalMileReport = new TerminalMileReport();
                terminalMileReport.setVehicleId(terminalMileageStatistics.getMonitorId());
                terminalMileReport.setCarLicense(terminalMileageStatistics.getMonitorName());
                terminalMileReport.setMonitorType(terminalMileageStatistics.getMonitorType());
                terminalMileReport.setGroupName(terminalMileageStatistics.getGroupName());
                terminalMileReport.setAssignmentName(terminalMileageStatistics.getAssignmentName());
                terminalMileReport.setTotalMile(terminalMileageStatistics.getTotalMile());
                terminalMileReport.setRunMile(terminalMileageStatistics.getTravelMile());
                terminalMileReport.setStopMile(terminalMileageStatistics.getIdleSpeedMile());
                terminalMileReport.setAbnormalMile(terminalMileageStatistics.getAbnormalMile());
                resultList.add(terminalMileReport);
            }
            result.put("totalMile", allTotalMile);

            // 有数据的车辆
            Set<String> existDataMonitorIds =
                terminalMileageStatisticsList.stream().map(TerminalMileageStatistics::getMonitorId)
                    .collect(Collectors.toSet());
            StringBuilder lowVehicle = new StringBuilder();
            // 没有查询到数据的车辆
            Set<String> noExistDataMonitorIds =
                vehicleIdList.stream().filter(obj -> !existDataMonitorIds.contains(obj)).collect(Collectors.toSet());
            // 没有查询到数据的车辆需要 组装成默认数据;
            resultList.addAll(getTerminalMileReports(noExistDataMonitorIds));
            result.put("lowMileVehicle", StrUtil.getFinalStr(lowVehicle));
        } else {
            StringBuilder brands = new StringBuilder();
            resultList.addAll(getTerminalMileReports(vehicleIdList));
            result.put("totalMile", 0.0);
            result.put("highMile", 0.0);
            result.put("lowMile", 0.0);
            result.put("highMileVehicle", StrUtil.getFinalStr(brands));
            result.put("lowMileVehicle", StrUtil.getFinalStr(brands));
        }
        resultList.sort((o1, o2) -> o2.getTotalMile().compareTo(o1.getTotalMile()));
        if (CollectionUtils.isNotEmpty(resultList)) {
            TerminalMileReport highTerminalMileage = resultList.get(0);
            Double highMile = highTerminalMileage.getTotalMile();
            result.put("highMile", highMile);
            TerminalMileReport lowTerminalMileage = resultList.get(resultList.size() - 1);
            Double lowMile = lowTerminalMileage.getTotalMile();
            result.put("lowMile", lowMile);
            StringBuilder highMileVehicle = new StringBuilder();
            StringBuilder lowMileVehicle = new StringBuilder();

            for (TerminalMileReport report : resultList) {
                if (Objects.equals(highMile, report.getTotalMile())) {
                    highMileVehicle.append(report.getCarLicense()).append(",");
                }
                if (Objects.equals(lowMile, report.getTotalMile())) {
                    lowMileVehicle.append(report.getCarLicense()).append(",");
                }
            }
            result.put("highMileVehicle", highMileVehicle.substring(0, highMileVehicle.length() - 1));
            result.put("lowMileVehicle", lowMileVehicle.substring(0, lowMileVehicle.length() - 1));
        }
        result.put("data", resultList);
        return result;
    }

    private List<TerminalMileReport> getTerminalMileReports(Collection<String> ids) {
        List<TerminalMileReport> terminalMileReports = new ArrayList<>();
        Map<String, BindDTO> bindDTOMap =
            MonitorUtils.getBindDTOMap(ids, "id", "name", "orgName", "groupName", "monitorType");
        for (Map.Entry<String, BindDTO> entry : bindDTOMap.entrySet()) {
            TerminalMileReport terminalMileReport = new TerminalMileReport();
            BindDTO bindDTO = entry.getValue();
            terminalMileReport.setVehicleId(bindDTO.getId());
            terminalMileReport.setCarLicense(bindDTO.getName());
            terminalMileReport.setGroupName(bindDTO.getOrgName());
            terminalMileReport.setAssignmentName(bindDTO.getGroupName());
            terminalMileReport.setMonitorType(bindDTO.getMonitorType());
            terminalMileReports.add(terminalMileReport);
        }
        return terminalMileReports;
    }

    /**
     * 终端里程报表数据
     * @param vehicleId
     * @param startTime
     * @param endTime
     * @return
     */
    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_SIX, url = {
        "/clbs/app/reportManagement/terminalMileage/detail" })
    public List<TerminalMileReport> showMileDetailReportList(String vehicleId, String startTime, String endTime) {
        List<TerminalMileReport> result = new ArrayList<>();
        List<TerminalMileageDailyDetails> terminalMileageDailyDetailsList = new ArrayList<>();
        startTime = startTime.substring(0, 10);
        endTime = endTime.substring(0, 10);
        List<String> dayList = getDayList(startTime.substring(0, 10), endTime.substring(0, 10));
        JsonResultBean getTerminalMileageDailyDetailResult =
            terminalMileageReportService.getTerminalMileageDailyDetail(vehicleId, startTime, endTime);
        if (getTerminalMileageDailyDetailResult.isSuccess()) {
            terminalMileageDailyDetailsList =
                JSON.parseArray(JSON.toJSONString(getTerminalMileageDailyDetailResult.getObj()),
                    TerminalMileageDailyDetails.class);
        }
        if (CollectionUtils.isNotEmpty(terminalMileageDailyDetailsList)) {
            for (TerminalMileageDailyDetails terminalMileageDailyDetails : terminalMileageDailyDetailsList) {
                TerminalMileReport terminalMileReport = new TerminalMileReport();
                terminalMileReport.setMonitorType(terminalMileageDailyDetails.getMonitorType());
                terminalMileReport.setVehicleId(terminalMileageDailyDetails.getMonitorId());
                terminalMileReport.setCarLicense(terminalMileageDailyDetails.getMonitorName());
                terminalMileReport.setGroupName(terminalMileageDailyDetails.getGroupName());
                terminalMileReport.setAssignmentName(terminalMileageDailyDetails.getAssignmentName());
                terminalMileReport.setTotalMile(terminalMileageDailyDetails.getTotalMile());
                terminalMileReport.setRunMile(terminalMileageDailyDetails.getTravelMile());
                terminalMileReport.setStopMile(terminalMileageDailyDetails.getIdleSpeedMile());
                terminalMileReport.setAbnormalMile(terminalMileageDailyDetails.getAbnormalMile());
                terminalMileReport.setDayDate(terminalMileageDailyDetails.getDayDate());
                result.add(terminalMileReport);
            }
            // 查询出来的日期
            List<String> days = terminalMileageDailyDetailsList.stream().map(TerminalMileageDailyDetails::getDayDate)
                .collect(Collectors.toList());
            TerminalMileReport terminalMileReport = getTerminalMileReports(Collections.singletonList(vehicleId)).get(0);
            for (String day : dayList) {
                if (!days.contains(day)) {
                    TerminalMileReport mileReport = new TerminalMileReport();
                    BeanUtils.copyProperties(terminalMileReport, mileReport);
                    mileReport.setDayDate(day);
                    result.add(mileReport);
                }
            }
            result.sort(new Comparator<TerminalMileReport>() {
                @Override
                public int compare(TerminalMileReport o1, TerminalMileReport o2) {
                    return o2.getDayDate().replace("-", "").compareTo(o1.getDayDate().replace("-", ""));
                }
            });
        } else {
            TerminalMileReport terminalMileReport = getTerminalMileReports(Collections.singletonList(vehicleId)).get(0);
            for (String day : dayList) {
                TerminalMileReport mileReport = new TerminalMileReport();
                BeanUtils.copyProperties(terminalMileReport, mileReport);
                mileReport.setDayDate(day);
                result.add(mileReport);
            }
        }
        return result;
    }

    /**
     * 获得 开始和结束时间之间的每天
     * @param startTime
     * @param endTime
     * @return
     */
    public static List<String> getDayList(String startTime, String endTime) {
        List<String> result = new ArrayList<>();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String localTime = endTime;
        result.add(localTime);
        while (true) {
            if (localTime.equals(startTime)) {
                break;
            }
            localTime = df.format(LocalDate.parse(localTime, df).minusDays(1));
            result.add(localTime);
        }
        return result;
    }
}

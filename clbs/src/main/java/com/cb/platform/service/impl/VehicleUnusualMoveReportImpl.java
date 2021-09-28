package com.cb.platform.service.impl;

import com.cb.platform.domain.OffLineExportBusinessId;
import com.cb.platform.domain.VehicleUnusualMoveDetailReport;
import com.cb.platform.domain.report.sichuan.VehicleAbnormalDrivingOrgReportDO;
import com.cb.platform.domain.report.sichuan.VehicleAbnormalDrivingVehicleReportDO;
import com.cb.platform.dto.report.sichuan.VehicleAbnormalDrivingReportQuery;
import com.cb.platform.service.VehicleUnusualMoveReportService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.OfflineExportInfo;
import com.zw.platform.domain.basicinfo.enums.PlateColor;
import com.zw.platform.dto.alarm.AlarmPageInfoDto;
import com.zw.platform.dto.paas.PaasCloudPageDataDTO;
import com.zw.platform.dto.paas.PaasCloudResultDTO;
import com.zw.platform.service.offlineExport.OfflineExportService;
import com.zw.platform.util.common.AddressUtil;
import com.zw.platform.util.common.Date8Utils;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.paas.PaasCloudUrlUtil;
import com.zw.platform.util.report.PaasCloudAlarmUrlEnum;
import com.zw.platform.util.report.PaasCloudUrlEnum;
import com.zw.talkback.common.ControllerTemplate;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * @author Administrator
 */
@Service
public class VehicleUnusualMoveReportImpl implements VehicleUnusualMoveReportService {

    @Autowired
    private UserService userService;

    @Autowired
    private OfflineExportService exportService;

    @Override
    public JsonResultBean getVehicleAbnormalDrivingOrgReport(VehicleAbnormalDrivingReportQuery query) throws Exception {
        Set<String> filterOrgIds = userService.fuzzySearchFilterOrgIds(query.getSimpleQueryParam(), query.getOrgIds());
        if (CollectionUtils.isEmpty(filterOrgIds)) {
            return new JsonResultBean(Collections.emptyList());
        }
        Map<String, String> params = new HashMap<>(10);
        params.put("userName", SystemHelper.getCurrentUsername());
        params.put("organizationIds", String.join(",", filterOrgIds));
        params.put("startTime", DateUtil.getStringToString(query.getStartTime(), null, DateUtil.DATE_FORMAT));
        params.put("endTime", DateUtil.getStringToString(query.getEndTime(), null, DateUtil.DATE_FORMAT));
        String paasResultStr = HttpClientUtil.send(PaasCloudUrlEnum.VEHICLE_ABNORMAL_DRIVING_ORG_REPORT, params);
        List<VehicleAbnormalDrivingOrgReportDO> resultListData =
            PaasCloudUrlUtil.getResultListData(paasResultStr, VehicleAbnormalDrivingOrgReportDO.class);
        return new JsonResultBean(resultListData);
    }

    @Override
    public JsonResultBean exportVehicleAbnormalDrivingOrgReport(VehicleAbnormalDrivingReportQuery query) {
        Set<String> filterOrgIds = userService.fuzzySearchFilterOrgIds(query.getSimpleQueryParam(), query.getOrgIds());
        if (CollectionUtils.isEmpty(filterOrgIds)) {
            return new JsonResultBean(JsonResultBean.FAULT, "模糊搜索后无数据");
        }
        String fileName = "车辆异常行驶道路运输企业统计表" + Date8Utils.getValToTime(LocalDateTime.now());
        OfflineExportInfo instance = OfflineExportInfo.getInstance("车辆异动统计", fileName + ".xls");
        TreeMap<String, String> params = new TreeMap<>();
        params.put("userName", SystemHelper.getCurrentUsername());
        params.put("organizationIds", String.join(",", filterOrgIds));
        params.put("startTime", DateUtil.getStringToString(query.getStartTime(), null, DateUtil.DATE_FORMAT));
        params.put("endTime", DateUtil.getStringToString(query.getEndTime(), null, DateUtil.DATE_FORMAT));
        instance.assembleCondition(params, OffLineExportBusinessId.VEHICLE_ABNORMAL_DRIVING_ORG_REPORT);
        return ControllerTemplate.addExportOffline(exportService, instance, "导出列表异常");

    }

    @Override
    public JsonResultBean getVehicleAbnormalDrivingVehicleReport(VehicleAbnormalDrivingReportQuery query)
        throws Exception {
        Set<String> filterMonitorIds =
            userService.fuzzySearchFilterMonitorIds(query.getSimpleQueryParam(), query.getMonitorIds());
        if (CollectionUtils.isEmpty(filterMonitorIds)) {
            return new JsonResultBean(Collections.emptyList());
        }
        Map<String, String> params = new HashMap<>(10);
        params.put("monitorIds", String.join(",", filterMonitorIds));
        params.put("startTime", DateUtil.getStringToString(query.getStartTime(), null, DateUtil.DATE_FORMAT));
        params.put("endTime", DateUtil.getStringToString(query.getEndTime(), null, DateUtil.DATE_FORMAT));
        String paasResultStr = HttpClientUtil.send(PaasCloudUrlEnum.VEHICLE_ABNORMAL_DRIVING_VEHICLE_REPORT, params);
        List<VehicleAbnormalDrivingVehicleReportDO> resultListData =
            PaasCloudUrlUtil.getResultListData(paasResultStr, VehicleAbnormalDrivingVehicleReportDO.class);
        for (VehicleAbnormalDrivingVehicleReportDO vehicleReportDO : resultListData) {
            vehicleReportDO.setPlateColorStr(PlateColor.getNameOrBlankByCode(vehicleReportDO.getPlateColor()));
        }
        return new JsonResultBean(resultListData);
    }

    @Override
    public JsonResultBean exportVehicleAbnormalDrivingVehicleReport(VehicleAbnormalDrivingReportQuery query) {
        Set<String> filterMonitorIds =
            userService.fuzzySearchFilterMonitorIds(query.getSimpleQueryParam(), query.getMonitorIds());
        if (CollectionUtils.isEmpty(filterMonitorIds)) {
            return new JsonResultBean(JsonResultBean.FAULT, "模糊搜索后无数据");
        }
        String fileName = "车辆异常行驶统计表" + Date8Utils.getValToTime(LocalDateTime.now());
        OfflineExportInfo instance = OfflineExportInfo.getInstance("车辆异动统计", fileName + ".xls");
        TreeMap<String, String> params = new TreeMap<>();
        params.put("monitorIds", String.join(",", filterMonitorIds));
        params.put("startTime", DateUtil.getStringToString(query.getStartTime(), null, DateUtil.DATE_FORMAT));
        params.put("endTime", DateUtil.getStringToString(query.getEndTime(), null, DateUtil.DATE_FORMAT));
        instance.assembleCondition(params, OffLineExportBusinessId.VEHICLE_ABNORMAL_DRIVING_VEHICLE_REPORT);
        return ControllerTemplate.addExportOffline(exportService, instance, "导出列表异常");
    }

    @Override
    public PageGridBean getVehicleAbnormalDrivingVehicleDetailReport(VehicleAbnormalDrivingReportQuery query)
        throws Exception {
        Set<String> filterMonitorIds =
            userService.fuzzySearchFilterMonitorIds(query.getSimpleQueryParam(), query.getMonitorIds());
        if (CollectionUtils.isEmpty(filterMonitorIds)) {
            return new PageGridBean();
        }
        Map<String, String> queryParam = new HashMap<>(16);
        queryParam.put("monitorIds", String.join(",", filterMonitorIds));
        Integer alarmType = query.getAlarmType();
        // 7703 暂时不做
        queryParam.put("alarmTypes", alarmType == null ? "7702" : alarmType.toString());
        queryParam.put("startTime", DateUtil.getStringToString(query.getStartTime(), null, DateUtil.DATE_FORMAT));
        queryParam.put("endTime", DateUtil.getStringToString(query.getEndTime(), null, DateUtil.DATE_FORMAT));
        queryParam.put("page", String.valueOf(query.getPage()));
        queryParam.put("pageSize", String.valueOf(query.getLength()));
        String paasResultStr = HttpClientUtil.send(PaasCloudAlarmUrlEnum.PAGE_QUERY_ALARM_INFO, queryParam);
        PaasCloudResultDTO<PaasCloudPageDataDTO<AlarmPageInfoDto>> resultListData =
            PaasCloudUrlUtil.pageResult(paasResultStr, AlarmPageInfoDto.class);
        List<AlarmPageInfoDto> alarmList = resultListData.getData().getItems();
        if (CollectionUtils.isEmpty(alarmList)) {
            return new PageGridBean();
        }
        Set<String> existLocationSet =
            alarmList.stream().map(AlarmPageInfoDto::getAlarmStartLocation).collect(Collectors.toSet());
        Map<String, String> addressPairMap = AddressUtil.batchInverseAddress(existLocationSet);
        List<VehicleUnusualMoveDetailReport> resultList = new ArrayList<>();
        for (AlarmPageInfoDto alarmInfo : alarmList) {
            VehicleUnusualMoveDetailReport vehicleUnusualMoveDetailReport = new VehicleUnusualMoveDetailReport();
            vehicleUnusualMoveDetailReport.setBrand(alarmInfo.getMonitorName());
            vehicleUnusualMoveDetailReport.setColor(PlateColor.getNameOrBlankByCode(alarmInfo.getPlateColor()));
            vehicleUnusualMoveDetailReport.setGroupName(alarmInfo.getGroupName());
            Date alarmStartTimeDate = DateUtil.getStringToDate(alarmInfo.getAlarmStartTime(), DateUtil.DATE_FORMAT_SSS);
            if (alarmStartTimeDate != null) {
                vehicleUnusualMoveDetailReport.setAlarmTime(DateUtil.getDateToString(alarmStartTimeDate, null));
                vehicleUnusualMoveDetailReport.setTime(alarmStartTimeDate.getTime());
            }
            vehicleUnusualMoveDetailReport.setSpeed(alarmInfo.getSpeed());
            vehicleUnusualMoveDetailReport.setLimitSpeed(alarmInfo.getSpeedLimit());
            vehicleUnusualMoveDetailReport.setAlarmType(alarmInfo.getAlarmType());
            vehicleUnusualMoveDetailReport.setAlarmTypeStr(alarmInfo.getDescription());
            String alarmStartLocation = alarmInfo.getAlarmStartLocation();
            vehicleUnusualMoveDetailReport.setAddress(addressPairMap.get(alarmStartLocation));
            resultList.add(vehicleUnusualMoveDetailReport);
        }
        return new PageGridBean(resultList, resultListData.getData().getPageInfo());
    }

    @Override
    public JsonResultBean exportVehicleAbnormalDrivingVehicleDetailReport(VehicleAbnormalDrivingReportQuery query) {
        Set<String> filterMonitorIds =
            userService.fuzzySearchFilterMonitorIds(query.getSimpleQueryParam(), query.getMonitorIds());
        if (CollectionUtils.isEmpty(filterMonitorIds)) {
            return new JsonResultBean(JsonResultBean.FAULT, "模糊搜索后无数据");
        }
        String fileName = "车辆异常行驶明细表" + Date8Utils.getValToTime(LocalDateTime.now());
        OfflineExportInfo instance = OfflineExportInfo.getInstance("车辆异动统计", fileName + ".xls");
        TreeMap<String, String> params = new TreeMap<>();
        params.put("monitorIds", String.join(",", filterMonitorIds));
        Integer alarmType = query.getAlarmType();
        // 7703 暂时不做
        params.put("alarmTypes", alarmType == null ? "7702" : alarmType.toString());
        params.put("startTime", DateUtil.getStringToString(query.getStartTime(), null, DateUtil.DATE_FORMAT));
        params.put("endTime", DateUtil.getStringToString(query.getEndTime(), null, DateUtil.DATE_FORMAT));
        instance.assembleCondition(params, OffLineExportBusinessId.VEHICLE_ABNORMAL_DRIVING_VEHICLE_DETAIL_REPORT);
        return ControllerTemplate.addExportOffline(exportService, instance, "导出列表异常");
    }
}

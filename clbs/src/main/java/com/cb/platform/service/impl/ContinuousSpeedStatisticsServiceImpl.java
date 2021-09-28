package com.cb.platform.service.impl;

import com.cb.platform.domain.ContinuousSpeedGroupStatistics;
import com.cb.platform.domain.ContinuousSpeedVehicleDetails;
import com.cb.platform.domain.ContinuousSpeedVehicleStatistics;
import com.cb.platform.domain.OffLineExportBusinessId;
import com.cb.platform.dto.report.sichuan.ContinuousSpeedStatisticsQuery;
import com.cb.platform.service.ContinuousSpeedStatisticsService;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author Administrator
 */
@Service
public class ContinuousSpeedStatisticsServiceImpl implements ContinuousSpeedStatisticsService {

    @Autowired
    private UserService userService;

    @Autowired
    private OfflineExportService exportService;

    @Override
    public JsonResultBean getContinuousSpeedOrgReport(ContinuousSpeedStatisticsQuery query) throws Exception {
        String orgIds = query.getOrgIds();
        String startTime = query.getStartTime();
        String endTime = query.getEndTime();
        if (StringUtils.isBlank(orgIds) || StringUtils.isBlank(startTime) || StringUtils.isBlank(endTime)) {
            return new JsonResultBean(JsonResultBean.FAULT, "参数不能为空");
        }
        Set<String> filterOrgIds = userService.fuzzySearchFilterOrgIds(query.getSimpleQueryParam(), orgIds);
        if (CollectionUtils.isEmpty(filterOrgIds)) {
            return new JsonResultBean(Collections.emptyList());
        }
        Map<String, String> params = new HashMap<>(16);
        params.put("userName", SystemHelper.getCurrentUsername());
        params.put("organizationIds", String.join(",", filterOrgIds));
        params.put("startTime", DateUtil.getStringToString(startTime, null, DateUtil.DATE_FORMAT));
        params.put("endTime", DateUtil.getStringToString(endTime, null, DateUtil.DATE_FORMAT));
        String paasResult = HttpClientUtil.send(PaasCloudUrlEnum.CONTINUOUS_SPEED_ORG_REPORT, params);
        List<ContinuousSpeedGroupStatistics> resultListData =
            PaasCloudUrlUtil.getResultListData(paasResult, ContinuousSpeedGroupStatistics.class);
        return new JsonResultBean(resultListData);

    }

    @Override
    public JsonResultBean exportContinuousSpeedOrgReport(ContinuousSpeedStatisticsQuery query) {
        String orgIds = query.getOrgIds();
        String startTime = query.getStartTime();
        String endTime = query.getEndTime();
        if (StringUtils.isBlank(orgIds) || StringUtils.isBlank(startTime) || StringUtils.isBlank(endTime)) {
            return new JsonResultBean(JsonResultBean.FAULT, "参数不能为空");
        }
        Set<String> filterOrgIds = userService.fuzzySearchFilterOrgIds(query.getSimpleQueryParam(), orgIds);
        if (CollectionUtils.isEmpty(filterOrgIds)) {
            return new JsonResultBean(JsonResultBean.FAULT, "模糊搜索后无数据");
        }
        TreeMap<String, String> params = new TreeMap<>();
        params.put("userName", SystemHelper.getCurrentUsername());
        params.put("organizationIds", String.join(",", filterOrgIds));
        params.put("startTime", DateUtil.getStringToString(query.getStartTime(), null, DateUtil.DATE_FORMAT));
        params.put("endTime", DateUtil.getStringToString(query.getEndTime(), null, DateUtil.DATE_FORMAT));
        String fileName = "持续超速道路运输企业统计报表" + Date8Utils.getValToTime(LocalDateTime.now());
        OfflineExportInfo instance = OfflineExportInfo.getInstance("持续超速统计", fileName + ".xls");
        instance.assembleCondition(params, OffLineExportBusinessId.CONTINUOUS_SPEED_ORG_REPORT);
        return ControllerTemplate.addExportOffline(exportService, instance, "导出列表异常");
    }

    @Override
    public JsonResultBean getContinuousSpeedVehicleReport(ContinuousSpeedStatisticsQuery query) throws Exception {
        String monitorIds = query.getMonitorIds();
        String startTime = query.getStartTime();
        String endTime = query.getEndTime();
        if (StringUtils.isBlank(monitorIds) || StringUtils.isBlank(startTime) || StringUtils.isBlank(endTime)) {
            return new JsonResultBean(JsonResultBean.FAULT, "参数不能为空");
        }
        Set<String> filterMonitorIds = userService.fuzzySearchFilterMonitorIds(query.getSimpleQueryParam(), monitorIds);
        if (CollectionUtils.isEmpty(filterMonitorIds)) {
            return new JsonResultBean(Collections.emptyList());
        }
        Map<String, String> params = new HashMap<>(16);
        params.put("monitorIds", String.join(",", filterMonitorIds));
        params.put("startTime", DateUtil.getStringToString(startTime, null, DateUtil.DATE_FORMAT));
        params.put("endTime", DateUtil.getStringToString(endTime, null, DateUtil.DATE_FORMAT));
        String paasResult = HttpClientUtil.send(PaasCloudUrlEnum.CONTINUOUS_SPEED_VEHICLE_REPORT, params);
        List<ContinuousSpeedVehicleStatistics> resultListData =
            PaasCloudUrlUtil.getResultListData(paasResult, ContinuousSpeedVehicleStatistics.class);
        if (CollectionUtils.isEmpty(resultListData)) {
            return new JsonResultBean(Collections.emptyList());
        }
        for (ContinuousSpeedVehicleStatistics vehicleStatistics : resultListData) {
            vehicleStatistics.setPlateColor(PlateColor.getNameOrBlankByCode(vehicleStatistics.getSignColor()));
        }
        return new JsonResultBean(resultListData);

    }

    @Override
    public JsonResultBean exportContinuousSpeedVehicleReport(ContinuousSpeedStatisticsQuery query) {
        String monitorIds = query.getMonitorIds();
        String startTime = query.getStartTime();
        String endTime = query.getEndTime();
        if (StringUtils.isBlank(monitorIds) || StringUtils.isBlank(startTime) || StringUtils.isBlank(endTime)) {
            return new JsonResultBean(JsonResultBean.FAULT, "参数不能为空");
        }
        Set<String> filterMonitorIds = userService.fuzzySearchFilterMonitorIds(query.getSimpleQueryParam(), monitorIds);
        if (CollectionUtils.isEmpty(filterMonitorIds)) {
            return new JsonResultBean(JsonResultBean.FAULT, "模糊搜索后无数据");
        }
        TreeMap<String, String> params = new TreeMap<>();
        params.put("monitorIds", String.join(",", filterMonitorIds));
        params.put("startTime", DateUtil.getStringToString(query.getStartTime(), null, DateUtil.DATE_FORMAT));
        params.put("endTime", DateUtil.getStringToString(query.getEndTime(), null, DateUtil.DATE_FORMAT));
        String fileName = "持续超速车辆统计表" + Date8Utils.getValToTime(LocalDateTime.now());
        OfflineExportInfo instance = OfflineExportInfo.getInstance("持续超速统计", fileName + ".xls");
        instance.assembleCondition(params, OffLineExportBusinessId.CONTINUOUS_SPEED_VEHICLE_REPORT);
        return ControllerTemplate.addExportOffline(exportService, instance, "导出列表异常");
    }

    @Override
    public PageGridBean getContinuousSpeedVehicleDetailReport(ContinuousSpeedStatisticsQuery query) throws Exception {
        String monitorIds = query.getMonitorIds();
        String startTime = query.getStartTime();
        String endTime = query.getEndTime();
        Long page = query.getPage();
        Long limit = query.getLimit();
        if (StringUtils.isBlank(monitorIds) || StringUtils.isBlank(startTime) || StringUtils.isBlank(endTime)
            || page == null || limit == null) {
            return new PageGridBean(PageGridBean.FAULT, "参数不能为空");
        }
        Set<String> filterMonitorIds = userService.fuzzySearchFilterMonitorIds(query.getSimpleQueryParam(), monitorIds);
        if (CollectionUtils.isEmpty(filterMonitorIds)) {
            return new PageGridBean();
        }
        Map<String, String> params = new HashMap<>(16);
        params.put("calStandard", "2");
        params.put("monitorIds", String.join(",", filterMonitorIds));
        params.put("startTime", DateUtil.getStringToString(query.getStartTime(), null, DateUtil.DATE_FORMAT));
        params.put("endTime", DateUtil.getStringToString(query.getEndTime(), null, DateUtil.DATE_FORMAT));
        params.put("page", String.valueOf(page));
        params.put("pageSize", String.valueOf(limit));
        String paasResult = HttpClientUtil.send(PaasCloudAlarmUrlEnum.PLATE_OVER_SPEED_PAGE_LIST, params);
        PaasCloudResultDTO<PaasCloudPageDataDTO<AlarmPageInfoDto>> paasCloudResultDTO =
            PaasCloudUrlUtil.pageResult(paasResult, AlarmPageInfoDto.class);
        List<AlarmPageInfoDto> alarmPageInfoList = paasCloudResultDTO.getData().getItems();
        if (CollectionUtils.isEmpty(alarmPageInfoList)) {
            return new PageGridBean();
        }
        Set<String> locationSet = new HashSet<>();
        for (AlarmPageInfoDto alarmPageInfoDto : alarmPageInfoList) {
            locationSet.add(alarmPageInfoDto.getAlarmStartLocation());
            locationSet.add(alarmPageInfoDto.getAlarmEndLocation());
        }
        Map<String, String> addressPairMap = AddressUtil.batchInverseAddress(locationSet);
        List<ContinuousSpeedVehicleDetails> pageResult = new ArrayList<>();
        for (AlarmPageInfoDto alarmPageInfo : alarmPageInfoList) {
            ContinuousSpeedVehicleDetails continuousSpeedVehicleDetails = new ContinuousSpeedVehicleDetails();
            continuousSpeedVehicleDetails.setPlateNumber(alarmPageInfo.getMonitorName());
            continuousSpeedVehicleDetails.setPlateColor(PlateColor.getNameOrBlankByCode(alarmPageInfo.getPlateColor()));
            continuousSpeedVehicleDetails.setVehicleType(alarmPageInfo.getVehicleType());
            continuousSpeedVehicleDetails.setGroupName(alarmPageInfo.getGroupName());
            String alarmStartTime = alarmPageInfo.getAlarmStartTime();
            continuousSpeedVehicleDetails
                .setAlarmStartTime(DateUtil.getStringToString(alarmStartTime, DateUtil.DATE_FORMAT, null));
            String alarmEndTime = alarmPageInfo.getAlarmEndTime();
            continuousSpeedVehicleDetails
                .setAlarmEndTime(DateUtil.getStringToString(alarmEndTime, DateUtil.DATE_FORMAT, null));
            continuousSpeedVehicleDetails.setMaxSpeed(alarmPageInfo.getMaxSpeed());
            continuousSpeedVehicleDetails.setSpeedTime(alarmPageInfo.getSpeedTime() / 1000);
            continuousSpeedVehicleDetails
                .setAlarmStartLocation(addressPairMap.get(alarmPageInfo.getAlarmStartLocation()));
            continuousSpeedVehicleDetails.setAlarmEndLocation(addressPairMap.get(alarmPageInfo.getAlarmEndLocation()));
            pageResult.add(continuousSpeedVehicleDetails);
        }
        return new PageGridBean(pageResult, paasCloudResultDTO.getData().getPageInfo());
    }

    @Override
    public JsonResultBean exportContinuousSpeedVehicleDetailReport(ContinuousSpeedStatisticsQuery query) {
        String monitorIds = query.getMonitorIds();
        String startTime = query.getStartTime();
        String endTime = query.getEndTime();
        if (StringUtils.isBlank(monitorIds) || StringUtils.isBlank(startTime) || StringUtils.isBlank(endTime)) {
            return new JsonResultBean(JsonResultBean.FAULT, "参数不能为空");
        }
        Set<String> filterMonitorIds = userService.fuzzySearchFilterMonitorIds(query.getSimpleQueryParam(), monitorIds);
        if (CollectionUtils.isEmpty(filterMonitorIds)) {
            return new JsonResultBean(JsonResultBean.FAULT, "模糊搜索后无数据");
        }
        TreeMap<String, String> params = new TreeMap<>();
        params.put("calStandard", "2");
        params.put("monitorIds", String.join(",", filterMonitorIds));
        params.put("startTime", DateUtil.getStringToString(query.getStartTime(), null, DateUtil.DATE_FORMAT));
        params.put("endTime", DateUtil.getStringToString(query.getEndTime(), null, DateUtil.DATE_FORMAT));
        String fileName = "持续超速车辆明细表" + Date8Utils.getValToTime(LocalDateTime.now());
        OfflineExportInfo instance = OfflineExportInfo.getInstance("持续超速统计", fileName + ".xls");
        instance.assembleCondition(params, OffLineExportBusinessId.CONTINUOUS_SPEED_VEHICLE_DETAIL_REPORT);
        return ControllerTemplate.addExportOffline(exportService, instance, "导出列表异常");
    }
}

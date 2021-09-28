package com.cb.platform.service.impl;

import com.cb.platform.domain.OffLineExportBusinessId;
import com.cb.platform.domain.sichuan.vehicleonlinerate.VehicleOnlineDetailsDO;
import com.cb.platform.domain.sichuan.vehicleonlinerate.VehicleOnlineRateOrgMonthReportDO;
import com.cb.platform.domain.sichuan.vehicleonlinerate.VehicleOnlineRateVehicleMonthReportDO;
import com.cb.platform.domain.sichuan.vehicleonlinerate.VehicleOnlineTimeSectionDO;
import com.cb.platform.dto.VehicleOnlineRateQuery;
import com.cb.platform.service.VehicleOnlineMonthService;
import com.cb.platform.vo.VehicleMonthVO;
import com.cb.platform.vo.VehicleOnlineVO;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.basicinfo.OfflineExportInfo;
import com.zw.platform.domain.basicinfo.enums.PlateColor;
import com.zw.platform.dto.paas.PaasCloudPageDataDTO;
import com.zw.platform.dto.paas.PaasCloudResultDTO;
import com.zw.platform.service.offlineExport.OfflineExportService;
import com.zw.platform.util.common.Date8Utils;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.platform.util.paas.PaasCloudUrlUtil;
import com.zw.platform.util.report.PaasCloudUrlEnum;
import com.zw.talkback.common.ControllerTemplate;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * @author zhangsq
 * @date 2018/5/3 13:54
 */
@Service
public class VehicleOnlineMonthServiceImpl implements VehicleOnlineMonthService {

    @Autowired
    private UserService userService;

    @Autowired
    private OfflineExportService exportService;

    @Override
    public PageGridBean getVehicleOnlineRateOrgMonthReport(VehicleOnlineRateQuery query) throws Exception {
        Set<String> filterOrgIds =
            userService.fuzzySearchFilterOrgIds(query.getSimpleQueryParam(), query.getEnterpriseIds());
        if (CollectionUtils.isEmpty(filterOrgIds)) {
            return new PageGridBean();
        }
        Map<String, String> params = new HashMap<>(16);
        params.put("organizationIds", String.join(",", filterOrgIds));
        String month = query.getMonth();
        month = month.replaceAll("-", "");
        params.put("startMonth", month);
        params.put("endMonth", month);
        params.put("page", query.getPage().toString());
        params.put("pageSize", query.getLimit().toString());
        String paasResult = HttpClientUtil.send(PaasCloudUrlEnum.VEHICLE_ONLINE_RATE_ORG_MONTH_REPORT, params);
        PaasCloudResultDTO<PaasCloudPageDataDTO<VehicleOnlineRateOrgMonthReportDO>> resultData =
            PaasCloudUrlUtil.pageResult(paasResult, VehicleOnlineRateOrgMonthReportDO.class);
        List<VehicleOnlineRateOrgMonthReportDO> list = resultData.getData().getItems();
        if (CollectionUtils.isEmpty(list)) {
            return new PageGridBean();
        }
        List<VehicleMonthVO> result = new ArrayList<>();
        for (VehicleOnlineRateOrgMonthReportDO orgMonthReportDO : list) {
            VehicleMonthVO vehicleMonthVO = new VehicleMonthVO();
            vehicleMonthVO.setEnterpriseName(orgMonthReportDO.getOrgName());
            Double total = orgMonthReportDO.getTotal();
            vehicleMonthVO.setMonthReport(total == null ? "0.00%" : total + "%");
            Double[] days = orgMonthReportDO.getDays();
            vehicleMonthVO
                .setDays(days == null ? null : Arrays.stream(days).map(day -> day + "%").toArray(String[]::new));
            result.add(vehicleMonthVO);
        }
        return new PageGridBean(result, resultData.getData().getPageInfo());
    }

    @Override
    public PageGridBean getVehicleOnlineRateVehicleMonthReport(VehicleOnlineRateQuery query) throws Exception {
        Set<String> filterMonitorIds =
            userService.fuzzySearchFilterMonitorIds(query.getSimpleQueryParam(), query.getVehicleIds());
        if (CollectionUtils.isEmpty(filterMonitorIds)) {
            return new PageGridBean();
        }
        Map<String, String> params = new HashMap<>(16);
        params.put("monitorIds", String.join(",", filterMonitorIds));
        String month = query.getMonth();
        month = month.replaceAll("-", "");
        params.put("startMonth", month);
        params.put("endMonth", month);
        params.put("page", query.getPage().toString());
        params.put("pageSize", query.getLimit().toString());
        String paasResult = HttpClientUtil.send(PaasCloudUrlEnum.VEHICLE_ONLINE_RATE_VEHICLE_MONTH_REPORT, params);
        PaasCloudResultDTO<PaasCloudPageDataDTO<VehicleOnlineRateVehicleMonthReportDO>> resultData =
            PaasCloudUrlUtil.pageResult(paasResult, VehicleOnlineRateVehicleMonthReportDO.class);
        List<VehicleOnlineRateVehicleMonthReportDO> list = resultData.getData().getItems();
        if (CollectionUtils.isEmpty(list)) {
            return new PageGridBean();
        }
        List<VehicleMonthVO> result = new ArrayList<>();
        for (VehicleOnlineRateVehicleMonthReportDO vehicleMonthReportDO : list) {
            VehicleMonthVO vehicleMonthVO = new VehicleMonthVO();
            vehicleMonthVO.setEnterpriseName(vehicleMonthReportDO.getOrgName());
            vehicleMonthVO.setVehicleBrandNumber(vehicleMonthReportDO.getMonitorName());
            vehicleMonthVO.setVehicleBrandColor(PlateColor.getNameOrBlankByCode(vehicleMonthReportDO.getPlateColor()));
            vehicleMonthVO.setVehicleType(vehicleMonthReportDO.getVehicleType());
            Double total = vehicleMonthReportDO.getTotal();
            vehicleMonthVO.setMonthReport(total == null ? "0.00%" : total + "%");
            Double[] days = vehicleMonthReportDO.getDays();
            vehicleMonthVO
                .setDays(days == null ? null : Arrays.stream(days).map(day -> day + "%").toArray(String[]::new));
            result.add(vehicleMonthVO);
        }
        return new PageGridBean(result, resultData.getData().getPageInfo());
    }

    @Override
    public JsonResultBean exportVehicleOnlineRateOrgMonthReport(VehicleOnlineRateQuery query) {
        String enterpriseIds = query.getEnterpriseIds();
        String month = query.getMonth();
        if (StringUtils.isBlank(enterpriseIds) || StringUtils.isBlank(month)) {
            return new JsonResultBean(JsonResultBean.FAULT, "参数不能为空");
        }
        Set<String> filterOrgIds = userService.fuzzySearchFilterOrgIds(query.getSimpleQueryParam(), enterpriseIds);
        if (CollectionUtils.isEmpty(filterOrgIds)) {
            return new JsonResultBean(JsonResultBean.FAULT, "模糊搜索后无数据");
        }
        String fileName = "车辆在线率道路运输企业统计月报表" + Date8Utils.getValToTime(LocalDateTime.now());
        OfflineExportInfo instance = OfflineExportInfo.getInstance("车辆在线率统计", fileName + ".xls");
        month = month.replaceAll("-", "");
        TreeMap<String, String> params = new TreeMap<>();
        params.put("organizationIds", String.join(",", filterOrgIds));
        params.put("startMonth", month);
        params.put("endMonth", month);
        instance.assembleCondition(params, OffLineExportBusinessId.VEHICLE_ONLINE_RATE_ORG_MONTH_REPORT);
        return ControllerTemplate.addExportOffline(exportService, instance, "导出列表异常");
    }

    @Override
    public JsonResultBean exportVehicleOnlineRateVehicleMonthReport(VehicleOnlineRateQuery query) {
        String vehicleIds = query.getVehicleIds();
        String month = query.getMonth();
        if (StringUtils.isBlank(vehicleIds) || StringUtils.isBlank(month)) {
            return new JsonResultBean(JsonResultBean.FAULT, "参数不能为空");
        }
        Set<String> filterMonitorIds = userService.fuzzySearchFilterMonitorIds(query.getSimpleQueryParam(), vehicleIds);
        if (CollectionUtils.isEmpty(filterMonitorIds)) {
            return new JsonResultBean(JsonResultBean.FAULT, "模糊搜索后无数据");
        }
        String fileName = "车辆在线率统计月报表" + Date8Utils.getValToTime(LocalDateTime.now());
        OfflineExportInfo instance = OfflineExportInfo.getInstance("车辆在线率统计", fileName + ".xls");
        month = month.replaceAll("-", "");
        TreeMap<String, String> params = new TreeMap<>();
        params.put("monitorIds", String.join(",", filterMonitorIds));
        params.put("startMonth", month);
        params.put("endMonth", month);
        instance.assembleCondition(params, OffLineExportBusinessId.VEHICLE_ONLINE_RATE_VEHICLE_MONTH_REPORT);
        return ControllerTemplate.addExportOffline(exportService, instance, "导出列表异常");
    }

    @Override
    public JsonResultBean exportVehicleOnlineDetails(VehicleOnlineRateQuery query) {
        String vehicleIds = query.getVehicleIds();
        String startTime = query.getStartTime();
        String endTime = query.getEndTime();
        if (StringUtils.isBlank(vehicleIds) || StringUtils.isBlank(startTime) || StringUtils.isBlank(endTime)) {
            return new JsonResultBean(JsonResultBean.FAULT, "参数不能为空");
        }
        Set<String> filterMonitorIds = userService.fuzzySearchFilterMonitorIds(query.getSimpleQueryParam(), vehicleIds);
        if (CollectionUtils.isEmpty(filterMonitorIds)) {
            return new JsonResultBean(JsonResultBean.FAULT, "模糊搜索后无数据");
        }
        String fileName = "车辆在线明细表" + Date8Utils.getValToTime(LocalDateTime.now());
        OfflineExportInfo instance = OfflineExportInfo.getInstance("车辆在线率统计", fileName + ".xls");
        TreeMap<String, String> params = new TreeMap<>();
        params.put("monitorIds", String.join(",", filterMonitorIds));
        params.put("startTime", startTime.replaceAll("-", "").replaceAll(" ", "").replaceAll(":", ""));
        params.put("endTime", endTime.replaceAll("-", "").replaceAll(" ", "").replaceAll(":", ""));
        instance.assembleCondition(params, OffLineExportBusinessId.VEHICLE_ONLINE_DETAILS);
        return ControllerTemplate.addExportOffline(exportService, instance, "导出列表异常");
    }

    @Override
    public List<VehicleOnlineVO> getVehicleOnlineDetails(VehicleOnlineRateQuery query) throws Exception {
        Set<String> filterMonitorIds =
            userService.fuzzySearchFilterMonitorIds(query.getSimpleQueryParam(), query.getVehicleIds());
        if (CollectionUtils.isEmpty(filterMonitorIds)) {
            return Collections.emptyList();
        }
        String startTime = query.getStartTime();
        String endTime = query.getEndTime();
        Map<String, String> params = new HashMap<>(16);
        params.put("monitorIds", String.join(",", filterMonitorIds));
        params.put("startTime", startTime.replaceAll("-", "").replaceAll(" ", "").replaceAll(":", ""));
        params.put("endTime", endTime.replaceAll("-", "").replaceAll(" ", "").replaceAll(":", ""));
        String paasResult = HttpClientUtil.send(PaasCloudUrlEnum.VEHICLE_ONLINE_DETAILS, params);
        List<VehicleOnlineDetailsDO> resultListData =
            PaasCloudUrlUtil.getResultListData(paasResult, VehicleOnlineDetailsDO.class);
        if (CollectionUtils.isEmpty(resultListData)) {
            return Collections.emptyList();
        }
        Set<String> existMoIds =
            resultListData.stream().map(VehicleOnlineDetailsDO::getMonitorId).collect(Collectors.toSet());
        Map<String, BindDTO> bindInfoMap = VehicleUtil.batchGetBindInfosByRedis(existMoIds);
        userService.setObjectTypeName(bindInfoMap.values());
        List<VehicleOnlineVO> result = new ArrayList<>();
        for (VehicleOnlineDetailsDO onlineDetailsDO : resultListData) {
            List<VehicleOnlineTimeSectionDO> timeSectionList = onlineDetailsDO.getDetailList();
            if (CollectionUtils.isEmpty(timeSectionList)) {
                continue;
            }
            String monitorId = onlineDetailsDO.getMonitorId();
            String monitorName = onlineDetailsDO.getMonitorName();
            Integer plateColor = onlineDetailsDO.getPlateColor();
            String orgName = onlineDetailsDO.getOrgName();
            BindDTO bindDTO = bindInfoMap.get(monitorId);
            String vehicleType = bindDTO == null ? null : bindDTO.getObjectTypeName();
            for (VehicleOnlineTimeSectionDO timeSectionDO : timeSectionList) {
                VehicleOnlineVO vehicleOnlineVO = new VehicleOnlineVO();
                vehicleOnlineVO.setVehicleBrandNumber(monitorName);
                vehicleOnlineVO.setVehicleBrandColor(PlateColor.getNameOrBlankByCode(plateColor));
                vehicleOnlineVO.setVehicleType(vehicleType);
                vehicleOnlineVO.setEnterpriseName(orgName);
                String timeSectionStartTime = timeSectionDO.getTimeSectionStartTime();
                timeSectionStartTime = DateUtil.getStringToString(timeSectionStartTime, DateUtil.DATE_FORMAT, null);
                String timeSectionEndTime = timeSectionDO.getTimeSectionEndTime();
                timeSectionEndTime = DateUtil.getStringToString(timeSectionEndTime, DateUtil.DATE_FORMAT, null);
                vehicleOnlineVO.setTimeSection(timeSectionStartTime + " - " + timeSectionEndTime);
                vehicleOnlineVO.setTimeSectionNumber(timeSectionDO.getLocationNum());
                result.add(vehicleOnlineVO);
            }
        }
        return result;
    }

}

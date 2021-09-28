package com.cb.platform.service.impl;

import com.cb.platform.domain.OffLineExportBusinessId;
import com.cb.platform.dto.MonitorOffRouteDetailPageQuery;
import com.cb.platform.dto.MonitorOffRoutePageQuery;
import com.cb.platform.dto.MonitorOffRouteQuery;
import com.cb.platform.dto.MonitorOffRouteStatisticsDTO;
import com.cb.platform.dto.MonitorOffRouteStatisticsDetailDTO;
import com.cb.platform.dto.MonitorOffRouteTrendDTO;
import com.cb.platform.dto.OrgOffRouteBasicInfoAndTrendDTO;
import com.cb.platform.dto.OrgOffRouteDetailDTO;
import com.cb.platform.dto.OrgOffRouteMonitorStatisticsDetailDTO;
import com.cb.platform.dto.OrgOffRoutePageQuery;
import com.cb.platform.dto.OrgOffRouteQuery;
import com.cb.platform.dto.OrgOffRouteStatisticsDetailDTO;
import com.cb.platform.dto.OrgOffRouteTrendDTO;
import com.cb.platform.service.OffRouteService;
import com.zw.platform.basic.constant.Vehicle;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.basicinfo.OfflineExportInfo;
import com.zw.platform.domain.basicinfo.enums.PlateColor;
import com.zw.platform.dto.paas.PaasCloudPageDataDTO;
import com.zw.platform.dto.paas.PaasCloudResultDTO;
import com.zw.platform.service.offlineExport.OfflineExportService;
import com.zw.platform.util.FuzzySearchUtil;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.Date8Utils;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.paas.PaasCloudUrlUtil;
import com.zw.platform.util.report.PaasCloudUrlEnum;
import com.zw.talkback.common.ControllerTemplate;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * @author penghj
 * @version 1.0
 * @date 2021/3/23 16:59
 */
@Service
public class OffRouteServiceImpl implements OffRouteService {

    private static final DateTimeFormatter YYYYMM =
            new DateTimeFormatterBuilder()
                    .appendValue(ChronoField.YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
                    .appendValue(ChronoField.MONTH_OF_YEAR, 2).toFormatter();

    @Autowired
    private OfflineExportService exportService;

    @Value("${max.number.assignment.monitor:100}")
    private Integer maxNumberAssignmentMonitor;

    @Override
    public PageGridBean getDataList(OrgOffRoutePageQuery query) throws Exception {
        String orgId = query.getOrgId();
        String month = query.getMonth();
        Long page = query.getPage();
        Long limit = query.getLimit();
        if (StringUtils.isBlank(orgId) || StringUtils.isBlank(month) || page == null || limit == null) {
            return new PageGridBean(PageGridBean.FAULT, "参数传递错误");
        }
        Map<String, String> params = assembleOrgRequestParam(orgId, month);
        params.put("page", String.valueOf(page));
        params.put("pageSize", String.valueOf(limit));
        String simpleQueryParam = query.getSimpleQueryParam();
        if (StringUtils.isNotBlank(simpleQueryParam)) {
            params.put("fuzzyQueryParam", simpleQueryParam);
        }
        String passResult = HttpClientUtil.send(PaasCloudUrlEnum.ORG_OFF_ROUTE_LIST, params);
        PaasCloudResultDTO<PaasCloudPageDataDTO<OrgOffRouteStatisticsDetailDTO>> passCloudResult =
            PaasCloudUrlUtil.pageResult(passResult, OrgOffRouteStatisticsDetailDTO.class);
        List<OrgOffRouteStatisticsDetailDTO> list = passCloudResult.getData().getItems();
        if (CollectionUtils.isEmpty(list)) {
            return new PageGridBean();
        }
        return new PageGridBean(list, passCloudResult.getData().getPageInfo());
    }

    @Override
    public JsonResultBean exportOrgDataList(OrgOffRoutePageQuery query) {
        String orgId = query.getOrgId();
        String month = query.getMonth();
        if (StringUtils.isBlank(orgId) || StringUtils.isBlank(month)) {
            return new JsonResultBean(JsonResultBean.FAULT, "参数传递错误");
        }
        String fileName = "路线偏离企业统计数据列表" + Date8Utils.getValToTime(LocalDateTime.now());
        OfflineExportInfo instance = OfflineExportInfo.getInstance("路线偏离报警统计", fileName + ".xls");
        TreeMap<String, String> params = new TreeMap<>(assembleOrgRequestParam(orgId, month));
        String simpleQueryParam = query.getSimpleQueryParam();
        if (StringUtils.isNotBlank(simpleQueryParam)) {
            params.put("fuzzyQueryParam", simpleQueryParam);
        }
        instance.assembleCondition(params, OffLineExportBusinessId.ORG_OFF_ROUTE_STATISTICS_LIST);
        return ControllerTemplate.addExportOffline(exportService, instance, "导出列表异常");
    }

    @Override
    public JsonResultBean getChartStatisticsData(OrgOffRouteQuery query) throws Exception {
        String orgId = query.getOrgId();
        String month = query.getMonth();
        if (StringUtils.isBlank(orgId) || StringUtils.isBlank(month)) {
            return new JsonResultBean(JsonResultBean.FAULT, "参数传递错误");
        }
        Map<String, String> params = assembleOrgRequestParam(orgId, month);
        params.put("isSingle", "1");
        String passResult = HttpClientUtil.send(PaasCloudUrlEnum.ORG_OFF_ROUTE_TREND, params);
        OrgOffRouteTrendDTO orgOffRouteTrendDTO = PaasCloudUrlUtil.getResultData(passResult, OrgOffRouteTrendDTO.class);
        return new JsonResultBean(orgOffRouteTrendDTO);
    }

    @Override
    public JsonResultBean getOrgDetailBasicInfoAndTrend(OrgOffRouteQuery query) throws Exception {
        String orgId = query.getOrgId();
        String month = query.getMonth();
        if (StringUtils.isBlank(orgId) || StringUtils.isBlank(month)) {
            return new JsonResultBean(JsonResultBean.FAULT, "参数传递错误");
        }
        Map<String, String> params = assembleOrgRequestParam(orgId, month);
        String passResult = HttpClientUtil.send(PaasCloudUrlEnum.ORG_OFF_ROUTE_STATISTICS, params);
        OrgOffRouteDetailDTO basicInfo = PaasCloudUrlUtil.getResultData(passResult, OrgOffRouteDetailDTO.class);
        params.put("isSingle", "0");
        passResult = HttpClientUtil.send(PaasCloudUrlEnum.ORG_OFF_ROUTE_TREND, params);
        OrgOffRouteTrendDTO alarmTrend = PaasCloudUrlUtil.getResultData(passResult, OrgOffRouteTrendDTO.class);
        OrgOffRouteBasicInfoAndTrendDTO result = new OrgOffRouteBasicInfoAndTrendDTO(basicInfo, alarmTrend);
        return new JsonResultBean(result);
    }

    @Override
    public PageGridBean getOrgDetailMonitorList(OrgOffRoutePageQuery query) throws Exception {
        String orgId = query.getOrgId();
        String month = query.getMonth();
        Long page = query.getPage();
        Long limit = query.getLimit();
        if (StringUtils.isBlank(orgId) || StringUtils.isBlank(month) || page == null || limit == null) {
            return new PageGridBean(PageGridBean.FAULT, "参数传递错误");
        }
        Map<String, String> params = assembleOrgRequestParam(orgId, month);
        params.put("page", String.valueOf(page));
        params.put("pageSize", String.valueOf(limit));
        String passResult = HttpClientUtil.send(PaasCloudUrlEnum.ORG_MONITOR_OFF_ROUTE_LIST, params);
        PaasCloudResultDTO<PaasCloudPageDataDTO<OrgOffRouteMonitorStatisticsDetailDTO>> passCloudResult =
            PaasCloudUrlUtil.pageResult(passResult, OrgOffRouteMonitorStatisticsDetailDTO.class);
        List<OrgOffRouteMonitorStatisticsDetailDTO> list = passCloudResult.getData().getItems();
        if (CollectionUtils.isEmpty(list)) {
            return new PageGridBean();
        }
        for (OrgOffRouteMonitorStatisticsDetailDTO obj : list) {
            Integer plateColor = obj.getPlateColor();
            obj.setPlateColorStr(PlateColor.getNameOrBlankByCode(plateColor));
        }
        return new PageGridBean(list, passCloudResult.getData().getPageInfo());
    }

    @Override
    public JsonResultBean exportOrgDetailMonitorList(OrgOffRoutePageQuery query) {
        String orgId = query.getOrgId();
        String month = query.getMonth();
        if (StringUtils.isBlank(orgId) || StringUtils.isBlank(month)) {
            return new JsonResultBean(JsonResultBean.FAULT, "参数传递错误");
        }
        String fileName = "路线偏离企业监控对象统计数据列表" + Date8Utils.getValToTime(LocalDateTime.now());
        OfflineExportInfo instance = OfflineExportInfo.getInstance("路线偏离报警统计", fileName + ".xls");
        TreeMap<String, String> params = new TreeMap<>(assembleOrgRequestParam(orgId, month));
        instance.assembleCondition(params, OffLineExportBusinessId.ORG_MONITOR_OFF_ROUTE_STATISTICS_LIST);
        return ControllerTemplate.addExportOffline(exportService, instance, "导出列表异常");
    }

    private static Map<String, String> assembleOrgRequestParam(String orgId, String month) {
        Map<String, String> map = new HashMap<>(16);
        map.put("organizationId", orgId);
        map.put("month", month.replaceAll("-", ""));
        return map;
    }

    private static Map<String, String> assembleMonitorRequestParam(Collection<String> monitorIds, String month) {
        Map<String, String> map = new HashMap<>(16);
        map.put("monitorIds", monitorIds.stream().filter(StringUtils::isNotBlank).collect(Collectors.joining(",")));
        map.put("month", month.replaceAll("-", ""));
        return map;
    }

    private static Map<String, String> assembleMonitorRequestParam(String monitorId, String month) {
        Map<String, String> map = new HashMap<>(16);
        map.put("monitorId", monitorId);
        map.put("month", month.replaceAll("-", ""));
        return map;
    }

    private static Map<String, String> assembleMonitorDetailRequestParam(String monitorId, String month) {
        Map<String, String> map = new HashMap<>(16);
        map.put("monitorId", monitorId);
        final YearMonth yearMonth = YearMonth.parse(month, YYYYMM);
        final int lastDay = yearMonth.lengthOfMonth();
        final String yyyyMm = month.replaceAll("-", "");
        map.put("startTime", yyyyMm + "01000000");
        map.put("endTime", yyyyMm + lastDay + "235959");
        return map;
    }

    @Override
    public PageGridBean getMonitorDataList(MonitorOffRoutePageQuery query) throws Exception {
        Set<String> monitorIds = query.getMonitorIds();
        String month = query.getMonth();
        Long page = query.getPage();
        Long limit = query.getLimit();
        if (CollectionUtils.isEmpty(monitorIds) || StringUtils.isBlank(month) || page == null || limit == null) {
            return new PageGridBean(PageGridBean.FAULT, "参数传递错误");
        }
        if (monitorIds.size() > maxNumberAssignmentMonitor) {
            return new PageGridBean(PageGridBean.FAULT, "最多勾选" + maxNumberAssignmentMonitor + "个监控对象");
        }
        Set<String> actualMonitorIds = this.filterMonitorByKeyword(monitorIds, query.getSimpleQueryParam());
        if (CollectionUtils.isEmpty(actualMonitorIds)) {
            return new PageGridBean();
        }
        Map<String, String> params = assembleMonitorRequestParam(actualMonitorIds, month);
        params.put("page", String.valueOf(page));
        params.put("pageSize", String.valueOf(limit));
        String passResult = HttpClientUtil.send(PaasCloudUrlEnum.MONITOR_OFF_ROUTE_LIST, params);
        PaasCloudResultDTO<PaasCloudPageDataDTO<MonitorOffRouteStatisticsDTO>> passCloudResult =
                PaasCloudUrlUtil.pageResult(passResult, MonitorOffRouteStatisticsDTO.class);
        List<MonitorOffRouteStatisticsDTO> list = passCloudResult.getData().getItems();
        if (CollectionUtils.isEmpty(list)) {
            return new PageGridBean();
        }
        for (MonitorOffRouteStatisticsDTO obj : list) {
            Integer plateColor = obj.getPlateColor();
            obj.setPlateColorStr(PlateColor.getNameOrBlankByCode(plateColor));
        }
        return new PageGridBean(list, passCloudResult.getData().getPageInfo());
    }

    private Set<String> filterMonitorByKeyword(Set<String> monitorIds, String keyword) {
        if (StringUtils.isBlank(keyword) || CollectionUtils.isEmpty(monitorIds)) {
            return monitorIds;
        }
        final Set<String> searched = FuzzySearchUtil.scanByMonitor(null, keyword, Vehicle.BindType.HAS_BIND);
        searched.retainAll(monitorIds);
        return searched;
    }

    @Override
    public JsonResultBean exportMonitorDataList(MonitorOffRoutePageQuery query) {
        Set<String> monitorIds = query.getMonitorIds();
        String month = query.getMonth();
        if (CollectionUtils.isEmpty(monitorIds) || StringUtils.isBlank(month)) {
            return new JsonResultBean(JsonResultBean.FAULT, "参数传递错误");
        }
        if (monitorIds.size() > maxNumberAssignmentMonitor) {
            return new JsonResultBean(JsonResultBean.FAULT, "最多勾选" + maxNumberAssignmentMonitor + "个监控对象");
        }
        Set<String> actualMonitorIds = this.filterMonitorByKeyword(monitorIds, query.getSimpleQueryParam());
        if (CollectionUtils.isEmpty(actualMonitorIds)) {
            return new JsonResultBean(JsonResultBean.FAULT, "参数传递错误");
        }
        String fileName = "路线偏离车辆统计数据列表" + Date8Utils.getValToTime(LocalDateTime.now());
        OfflineExportInfo instance = OfflineExportInfo.getInstance("路线偏离报警统计", fileName + ".xls");
        TreeMap<String, String> params = new TreeMap<>(assembleMonitorRequestParam(actualMonitorIds, month));
        instance.assembleCondition(params, OffLineExportBusinessId.MONITOR_OFF_ROUTE_STATISTICS_LIST);
        return ControllerTemplate.addExportOffline(exportService, instance, "导出列表异常");
    }

    @Override
    public JsonResultBean getMonitorChartStatisticsData(MonitorOffRouteQuery query) throws BusinessException {
        String monitorId = query.getMonitorId();
        String month = query.getMonth();
        if (StringUtils.isBlank(monitorId) || StringUtils.isBlank(month)) {
            return new JsonResultBean(JsonResultBean.FAULT, "参数传递错误");
        }
        Map<String, String> params = assembleMonitorRequestParam(monitorId, month);
        String passResult = HttpClientUtil.send(PaasCloudUrlEnum.MONITOR_OFF_ROUTE_STATISTICS, params);
        MonitorOffRouteTrendDTO result = PaasCloudUrlUtil.getResultData(passResult, MonitorOffRouteTrendDTO.class);
        result.setPlateColorStr(PlateColor.getNameOrBlankByCode(result.getPlateColor()));
        return new JsonResultBean(result);
    }

    @Override
    public PageGridBean getMonitorDetailList(MonitorOffRouteDetailPageQuery query) throws Exception {
        String monitorId = query.getMonitorId();
        String month = query.getMonth();
        Long page = query.getPage();
        Long limit = query.getLimit();
        if (StringUtils.isBlank(monitorId) || StringUtils.isBlank(month) || page == null || limit == null) {
            return new PageGridBean(PageGridBean.FAULT, "参数传递错误");
        }
        Map<String, String> params = assembleMonitorDetailRequestParam(monitorId, month);
        params.put("page", String.valueOf(page));
        params.put("pageSize", String.valueOf(limit));
        String passResult = HttpClientUtil.send(PaasCloudUrlEnum.MONITOR_OFF_ROUTE_DETAIL_LIST, params);
        PaasCloudResultDTO<PaasCloudPageDataDTO<MonitorOffRouteStatisticsDetailDTO>> passCloudResult =
                PaasCloudUrlUtil.pageResult(passResult, MonitorOffRouteStatisticsDetailDTO.class);
        List<MonitorOffRouteStatisticsDetailDTO> list = passCloudResult.getData().getItems();
        if (CollectionUtils.isEmpty(list)) {
            return new PageGridBean();
        }
        return new PageGridBean(list, passCloudResult.getData().getPageInfo());
    }

    @Override
    public JsonResultBean exportMonitorDetailList(MonitorOffRouteDetailPageQuery query) {
        String monitorId = query.getMonitorId();
        String month = query.getMonth();
        if (StringUtils.isBlank(monitorId) || StringUtils.isBlank(month)) {
            return new JsonResultBean(JsonResultBean.FAULT, "参数传递错误");
        }
        String fileName = "路线偏离车辆监控对象统计数据列表" + Date8Utils.getValToTime(LocalDateTime.now());
        OfflineExportInfo instance = OfflineExportInfo.getInstance("路线偏离报警统计", fileName + ".xls");
        TreeMap<String, String> params = new TreeMap<>(assembleMonitorDetailRequestParam(monitorId, month));
        instance.assembleCondition(params, OffLineExportBusinessId.MONITOR_OFF_ROUTE_DETAIL_LIST);
        return ControllerTemplate.addExportOffline(exportService, instance, "导出列表异常");
    }
}

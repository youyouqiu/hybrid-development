package com.zw.platform.service.reportManagement.impl;

import com.alibaba.fastjson.JSONObject;
import com.cb.platform.util.page.PassCloudResultBean;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.reportManagement.OilMassMile;
import com.zw.platform.dto.paas.PaasCloudPageDataDTO;
import com.zw.platform.dto.paas.PaasCloudResultDTO;
import com.zw.platform.dto.reportManagement.OilAmountAndSpillDTO;
import com.zw.platform.dto.reportManagement.OilAmountAndSpillQuery;
import com.zw.platform.service.reportManagement.OilMassMileReportServer;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.excel.ExportExcel;
import com.zw.platform.util.paas.PaasCloudUrlUtil;
import com.zw.platform.util.report.PaasCloudUrlEnum;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.zw.platform.util.common.DateUtil.DATE_FORMAT_SHORT;

/**
 * 油量里程报表server
 */
@Service
public class OilMassMileReportServerImpl implements OilMassMileReportServer {
    private final Logger logger = LogManager.getLogger(OilMassMileReportServerImpl.class);

    private static final String DATE_FORMAT = "yyyy-MM-dd";

    @Override
    public List<OilMassMile> getOilMassMileData(String vehicleIds, String queryStartDate, String queryEndDate)
        throws Exception {
        final RedisKey redisKey = HistoryRedisKeyEnum.STATS_OIL_MASS_MILE.of(SystemHelper.getCurrentUsername());
        RedisHelper.delete(redisKey);
        List<OilMassMile> result = new ArrayList<>();
        if (StringUtils.isNotBlank(vehicleIds) && StringUtils.isNotBlank(queryStartDate) && StringUtils
            .isNotBlank(queryEndDate)) {
            result = getOilAndMileResult(vehicleIds, queryStartDate, queryEndDate);
        }
        RedisHelper.addObjectToList(redisKey, result, RedisHelper.SIX_HOUR_REDIS_EXPIRE);
        return result;
    }

    /**
     * 从hbase获取到油量里程数据
     */
    private List<OilMassMile> getOilAndMileResult(String vehicleIds, String queryStartDate, String queryEndDate)
        throws Exception {
        List<OilMassMile> oilMileData = new ArrayList<>();
        Set<String> setVehicleId = new HashSet<>(Arrays.asList(vehicleIds.split(",")));
        List<String> monitorIds = new ArrayList<>(setVehicleId);
        if (CollectionUtils.isNotEmpty(monitorIds)) {
            Map<String, String> param = new HashMap<>();
            Date endDate = DateUtil.getStringToDate(queryEndDate + " 23:59:59", DATE_FORMAT_SHORT);
            Date startDate = DateUtil.getStringToDate(queryStartDate + " 00:00:00", DATE_FORMAT_SHORT);
            String endDateString = DateUtil.getDateToString(endDate, DateUtil.DATE_FORMAT);
            String startDateString = DateUtil.getDateToString(startDate, DateUtil.DATE_FORMAT);
            param.put("monitorIds", vehicleIds);
            param.put("startTime", startDateString);
            param.put("endTime", endDateString);
            String sendResult = HttpClientUtil.send(PaasCloudUrlEnum.OIL_MILE_REPORT_URL, param);
            PassCloudResultBean passCloudResultBean = PassCloudResultBean.getDataInstance(sendResult);
            Object data = passCloudResultBean.getData();
            if (Objects.isNull(data)) {
                return oilMileData;
            }
            oilMileData = JSONObject.parseArray(data.toString(), OilMassMile.class);
            return assemblyOtherInfo(oilMileData, queryStartDate, queryEndDate, monitorIds);
        }
        return oilMileData;
    }

    /**
     * 组装监控对象名称和所属企业等数据
     */
    private List<OilMassMile> assemblyOtherInfo(List<OilMassMile> oilMileData, String startTime, String endTime,
        List<String> vehicleIds) {
        List<OilMassMile> resultData = new ArrayList<>();
        Map<String, OilMassMile> oilMileMap =
            oilMileData.stream().collect(Collectors.toMap(OilMassMile::getMonitorStrId, Function.identity()));
        int day = DateUtil.getTwoTimeDifference(DateUtil.getStringToLong(startTime, DATE_FORMAT),
            DateUtil.getStringToLong(endTime, DATE_FORMAT)) + 1;
        final List<RedisKey> keys =
                vehicleIds.stream().map(RedisKeyEnum.MONITOR_INFO::of).collect(Collectors.toList());
        final Map<String, Map<String, String>> infoMap = RedisHelper.batchGetHashMap(keys, "id", "name", "orgName");

        if (infoMap != null && infoMap.size() > 0) {
            vehicleIds.forEach(vId -> {
                Map<String, String> monitorInfo = infoMap.get(vId);
                if (monitorInfo == null) {
                    return;
                }
                OilMassMile oilMassMile = oilMileMap.get(vId);
                if (oilMassMile == null) {
                    oilMassMile = new OilMassMile();
                }
                String monitorName = monitorInfo.get("name");
                String orgName = monitorInfo.get("orgName");
                oilMassMile.setMonitorName(monitorName);
                oilMassMile.setGroupName(orgName);
                oilMassMile.setDays(day);
                oilMassMile.setStartDate(startTime);
                oilMassMile.setEndDate(endTime);
                resultData.add(oilMassMile);
            });
        }
        return resultData.stream().sorted((e1, e2) -> {
            if (e1.getOilTank() == null && e2.getOilTank() == null) {
                return 0;
            } else if (e1.getOilTank() == null && e2.getOilTank() != null) {
                return 1;
            } else if (e1.getOilTank() != null && e2.getOilTank() == null) {
                return -1;
            } else if (e1.getOilTank().doubleValue() == e2.getOilTank().doubleValue()) {
                return 0;
            } else if (e1.getOilTank() > e2.getOilTank()) {
                return -1;
            } else {
                return 1;
            }
        }).collect(Collectors.toList()); // 排序

    }

    @Override
    public void exportOilMassMileData(HttpServletResponse response, String fuzzyParam) throws Exception {
        ExportExcel export = new ExportExcel(null, OilMassMile.class, 1);
        final RedisKey key = HistoryRedisKeyEnum.STATS_OIL_MASS_MILE.of(SystemHelper.getCurrentUsername());
        List<OilMassMile> redisData = RedisHelper.getListObj(key, 1, -1);
        List<OilMassMile> resultExportData = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(redisData)) {
            resultExportData.addAll(redisData);
            if (StringUtils.isNotBlank(fuzzyParam)) { //模糊搜索需要过滤 如果没有搜索则全部导出
                String upperCaseFuzzyParam = fuzzyParam.toUpperCase();
                resultExportData.clear();
                List<OilMassMile> filterExportList =
                    redisData.stream().filter(data -> data.getMonitorName().toUpperCase().contains(upperCaseFuzzyParam))
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(filterExportList)) {
                    resultExportData.addAll(filterExportList);
                }
            }
        }
        export.setDataList(resultExportData);
        OutputStream out = response.getOutputStream();
        export.write(out);
        out.close();
    }

    @Override
    public PageGridBean getAmountOrSpillData(OilAmountAndSpillQuery query) throws Exception {
        Map<String, String> params = new HashMap<>();
        //组装paas-cloud api入参
        params.put("startTime", query.getStartTime());
        params.put("endTime", query.getEndTime());
        params.put("monitorId", query.getVehicleId());
        params.put("page", query.getPage().toString());
        params.put("pageSize", query.getLimit().toString());
        params.put("type", query.getType().toString());
        //调用paas api 获取分页数据
        String paasResult = HttpClientUtil.send(PaasCloudUrlEnum.OIL_MILE_REPORT_DETAIL_URL, params);
        PaasCloudResultDTO<PaasCloudPageDataDTO<OilAmountAndSpillDTO>> resultData =
                PaasCloudUrlUtil.pageResult(paasResult, OilAmountAndSpillDTO.class);
        List<OilAmountAndSpillDTO> items = resultData.getData().getItems();
        if (CollectionUtils.isEmpty(items)) {
            return new PageGridBean();
        }
        return new PageGridBean(items, resultData.getData().getPageInfo());
    }
}

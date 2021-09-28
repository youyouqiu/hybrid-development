package com.cb.platform.service.speedingStatistics.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cb.platform.domain.OffLineExportBusinessId;
import com.cb.platform.domain.speedingStatistics.UpSpeedMonitorDetail;
import com.cb.platform.domain.speedingStatistics.quey.UpSpeedVehicleQuery;
import com.cb.platform.service.speedingStatistics.UpSpeedStatisticsVehicleService;
import com.cb.platform.util.page.PageResultBean;
import com.cb.platform.util.page.PassCloudResultBean;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.basicinfo.OfflineExportInfo;
import com.zw.platform.service.core.UserService;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.AddressUtil;
import com.zw.platform.util.common.Date8Utils;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.platform.util.report.PaasCloudUrlEnum;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @Author zhangqiang
 * @Date 2020/5/20 15:26
 */
@Service
public class UpSpeedStatisticsVehicleServiceImpl implements UpSpeedStatisticsVehicleService {

    private static final String SUCCESS_STATUS_CODE = "10000";
    private Logger logger = LogManager.getLogger(UpSpeedStatisticsVehicleServiceImpl.class);

    @Value("${address.search.flag}")
    private boolean addressSearchFlag;

    @Autowired
    private UserService userService;

    @Override
    public PassCloudResultBean speedingStatisticsList(UpSpeedVehicleQuery query) {
        Map<String, String> params = getUpSpeedVehiclePassQuery(query);
        if (params.get("monitorIds") != null) {
            String passResult = HttpClientUtil.send(PaasCloudUrlEnum.UP_SPEED_MONITOR_STATISTICS_LIST_URL, params);
            return PassCloudResultBean.getPageInstance(passResult);
        }
        return new PassCloudResultBean();
    }

    private Map<String, String> getUpSpeedVehiclePassQuery(UpSpeedVehicleQuery query) {
        Map<String, String> params = new HashMap<>();
        params.put("startMonth", query.getTime());
        params.put("endMonth", query.getTime());
        params.put("page", String.valueOf(query.getPage()));
        params.put("pageSize", String.valueOf(query.getLimit()));
        if (StringUtils.isNotEmpty(query.getFuzzyQueryParam())) {
            Set<String> vehicleIdSetByFuzzyParameter =
                findVehicleIdSetByFuzzyParameter(query.getFuzzyQueryParam(), query.getVehicleIds());
            params.put("monitorIds", StringUtils.join(vehicleIdSetByFuzzyParameter, ","));
            return params;
        }
        params.put("monitorIds", query.getVehicleIds());
        return params;
    }

    @Override
    public JsonResultBean upSpeedGraphicalInfo(UpSpeedVehicleQuery query) {
        Map<String, String> params = getUpSpeedVehicleInfoParams(query);
        String passResult = HttpClientUtil.send(PaasCloudUrlEnum.UP_SPEED_MONITOR_STATISTICS_GRAPHICAL_URL, params);
        JSONObject result = JSONObject.parseObject(passResult);
        if (result != null && SUCCESS_STATUS_CODE.equals(result.getString("code"))) {
            return new JsonResultBean(result);
        }
        return new JsonResultBean(JsonResultBean.FAULT, "查询数据超时，请稍后再试");
    }

    private Map<String, String> getUpSpeedVehicleInfoParams(UpSpeedVehicleQuery query) {
        int days =
            DateUtil.getMonthDays(Integer.parseInt(query.getTime()) / 100, Integer.parseInt(query.getTime()) % 100);
        Map<String, String> params = new HashMap<>();
        params.put("startDate", query.getTime() + "01");
        params.put("endDate", query.getTime() + days);
        params.put("monitorId", query.getVehicleIds());
        return params;
    }

    @Override
    public JsonResultBean rankInfo(UpSpeedVehicleQuery query) {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("startMonth", query.getTime());
            params.put("endMonth", query.getTime());
            params.put("monitorIds", query.getVehicleIds());
            String passResult = HttpClientUtil.send(PaasCloudUrlEnum.UP_SPEED_MONITOR_GRAPHICAL_RANK_INFO_URL, params);
            PassCloudResultBean passCloudResultBean = JSON.parseObject(passResult, PassCloudResultBean.class);
            if (passCloudResultBean != null && passCloudResultBean.getData() != null) {
                PageResultBean resultBean =
                    PageResultBean.getInstance(JSON.parseObject(String.valueOf(passCloudResultBean.getData())), null);
                return new JsonResultBean(resultBean.getItems().get(0));
            }
            return new JsonResultBean(JsonResultBean.FAULT, "车辆排名无数据可以查询");
        } catch (Exception e) {
            logger.error("查询车辆排名相关数据异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "查询数据超时，请稍后再试");
        }

    }

    @Override
    public PassCloudResultBean upSpeedInfoList(UpSpeedVehicleQuery query) {
        Map<String, String> params = getSpeedDetailsParamMap(query);
        String passResult = HttpClientUtil.send(PaasCloudUrlEnum.UP_SPEED_MONITOR_STATISTICS_INFO_LIST_URL, params);
        return PassCloudResultBean.getPageInstance(passResult, this::completionAddress);
    }

    private Map<String, String> getSpeedDetailsParamMap(UpSpeedVehicleQuery query) {
        Map<String, String> params = new HashMap<>();
        Long month = Long.parseLong(query.getTime());
        LocalDateTime startMonthDay = Date8Utils.getLocalDateTime(month);
        //报警前一天的数据
        LocalDateTime endMonthDay = LocalDateTime.now().minusDays(1);
        if (!sameMonth(startMonthDay, endMonthDay)) {
            endMonthDay = startMonthDay.with(TemporalAdjusters.lastDayOfMonth());

        }
        endMonthDay = endMonthDay.withHour(23).withMinute(59).withSecond(59);
        String startTime = Date8Utils.getValToTime(startMonthDay) + "";
        String endTime = Date8Utils.getValToTime(endMonthDay) + "";
        params.put("startTime", startTime);
        //当前时间小于10号时
        params.put("endTime", endTime);
        params.put("monitorIds", query.getVehicleIds());
        params.put("page", String.valueOf(query.getPage()));
        params.put("pageSize", String.valueOf(query.getLimit()));
        params.put("isAddress", "1");
        return params;
    }

    public List<UpSpeedMonitorDetail> completionAddress(String items) {
        List<UpSpeedMonitorDetail> result = JSON.parseArray(items, UpSpeedMonitorDetail.class);
        if (addressSearchFlag) {
            Set<String> locationSet = new HashSet<>();
            List<UpSpeedMonitorDetail> unStartAddressDetail = new ArrayList<>();
            List<UpSpeedMonitorDetail> unEndAddressDetail = new ArrayList<>();
            for (UpSpeedMonitorDetail detail : result) {
                detail.setAddressSearchFlag(addressSearchFlag);
                if (StringUtil.isNullOrEmpty(detail.getAlarmStartAddress())) {
                    unStartAddressDetail.add(detail);
                    locationSet.add(detail.getAlarmStartLocation());
                }
                if (StringUtil.isNullOrEmpty(detail.getAlarmEndAddress())) {
                    unEndAddressDetail.add(detail);
                    locationSet.add(detail.getAlarmEndLocation());
                }
            }
            Map<String, String> addressMap = AddressUtil.batchInverseAddress(locationSet);
            unStartAddressDetail.forEach(unStart -> unStart.setAlarmStartAddress(
                Optional.ofNullable(addressMap.get(unStart.getAlarmStartLocation())).orElse(" 未定位")));
            unEndAddressDetail.forEach(unEnd -> unEnd
                .setAlarmEndAddress(Optional.ofNullable(addressMap.get(unEnd.getAlarmEndLocation())).orElse(" 未定位")));
        }
        return result;
    }

    /**
     * 获取用户勾选范围内的模糊查询
     * @param fuzzyParameter
     * @param vehicleIds
     * @return
     */
    private Set<String> findVehicleIdSetByFuzzyParameter(String fuzzyParameter, String vehicleIds) {
        Set<String> result = new HashSet<>();
        Set<String> purviewSet = new HashSet<>(Arrays.asList(vehicleIds.split(",")));
        Map<String, BindDTO> bindInfosByRedis = VehicleUtil.batchGetBindInfosByRedis(purviewSet);
        bindInfosByRedis.forEach((k, v) -> {
            if (v.getName().contains(fuzzyParameter)) {
                result.add(k);
            }
        });
        return result;
    }

    @Override
    public OfflineExportInfo exportVehListData(UpSpeedVehicleQuery query) {
        OfflineExportInfo instance = getOfflineExportInfo(query, "车辆超速统计报表");
        Map<String, String> params = new HashMap<>();
        params.put("queryMonth", query.getTime());
        if (StringUtils.isNotEmpty(query.getFuzzyQueryParam())) {
            Set<String> vehicleIdSetByFuzzyParameter =
                findVehicleIdSetByFuzzyParameter(query.getFuzzyQueryParam(), query.getVehicleIds());
            params.put("monitorIds", StringUtils.join(vehicleIdSetByFuzzyParameter, ","));
        }
        params.put("monitorIds", query.getVehicleIds());
        TreeMap<String, String> param = new TreeMap<>(params);
        instance.assembleCondition(param, OffLineExportBusinessId.SpeedVehList);
        return instance;
    }

    @Override
    public OfflineExportInfo exportVehSpeedDetailsData(UpSpeedVehicleQuery query) {
        OfflineExportInfo instance = getOfflineExportInfo(query, "车辆超速明细报表");
        Map<String, String> params = new HashMap<>();
        Long month = Long.parseLong(query.getTime());
        LocalDateTime startMonthDay = Date8Utils.getLocalDateTime(month);
        //报警前一天的数据
        LocalDateTime endMonthDay = LocalDateTime.now().minusDays(1);
        if (!sameMonth(startMonthDay, endMonthDay)) {
            endMonthDay = startMonthDay.with(TemporalAdjusters.lastDayOfMonth());

        }
        endMonthDay = endMonthDay.withHour(23).withMinute(59).withSecond(59);
        String endTime = Date8Utils.getValToTime(endMonthDay) + "";

        params.put("queryMonth", query.getTime());
        params.put("alarmTime", endTime);
        params.put("monitorId", query.getVehicleIds());
        TreeMap<String, String> param = new TreeMap<>(params);
        instance.assembleCondition(param, OffLineExportBusinessId.SpeedVehDetail);
        return instance;
    }

    private boolean sameMonth(LocalDateTime one, LocalDateTime two) {
        return Date8Utils.getValToMonth(one) == Date8Utils.getValToMonth(two);
    }

    private OfflineExportInfo getOfflineExportInfo(UpSpeedVehicleQuery query, String name) {
        String fileName = name + Date8Utils.getValToTime(LocalDateTime.now());
        return OfflineExportInfo.getInstance(query.getModule(), fileName + ".xls");
    }
}

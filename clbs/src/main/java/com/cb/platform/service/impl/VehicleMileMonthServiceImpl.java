package com.cb.platform.service.impl;

import com.cb.platform.domain.OffLineExportBusinessId;
import com.cb.platform.domain.VehicleDailyMileageReportDO;
import com.cb.platform.domain.VehicleMileageStatisticsDO;
import com.cb.platform.service.VehicleMileMonthService;
import com.cb.platform.vo.VehicleMileDetailVO;
import com.cb.platform.vo.VehicleMileMonthVO;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.basicinfo.OfflineExportInfo;
import com.zw.platform.domain.basicinfo.enums.PlateColor;
import com.zw.platform.service.offlineExport.OfflineExportService;
import com.zw.platform.util.common.Date8Utils;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.paas.PaasCloudUrlUtil;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author zhangsq
 * @date 2018/5/10 16:35
 */
@Service
public class VehicleMileMonthServiceImpl implements VehicleMileMonthService {

    @Autowired
    private OfflineExportService exportService;

    @Autowired
    private UserService userService;

    @Override
    public List<VehicleMileMonthVO> getVehicleMileMonths(String vehicleIds, String month, String simpleQueryParam)
        throws Exception {
        Map<String, String> params = new HashMap<>(10);
        Set<String> filterMonitorIds = userService.fuzzySearchFilterMonitorIds(simpleQueryParam, vehicleIds);
        if (CollectionUtils.isEmpty(filterMonitorIds)) {
            return Collections.emptyList();
        }
        params.put("monitorIds", String.join(",", filterMonitorIds));
        params.put("month", month.replaceAll("-", ""));
        String passResult = HttpClientUtil.send(PaasCloudUrlEnum.VEHICLE_DAILY_MILEAGE_REPORT, params);
        List<VehicleDailyMileageReportDO> resultListData =
            PaasCloudUrlUtil.getResultListData(passResult, VehicleDailyMileageReportDO.class);
        if (CollectionUtils.isEmpty(resultListData)) {
            return Collections.emptyList();
        }
        List<VehicleMileMonthVO> result = new ArrayList<>();
        for (VehicleDailyMileageReportDO resultListDatum : resultListData) {
            VehicleMileMonthVO vehicleMileMonthVO = new VehicleMileMonthVO();
            vehicleMileMonthVO.setVehicleBrandNumber(resultListDatum.getMonitorName());
            vehicleMileMonthVO.setVehicleBrandColor(resultListDatum.getPlateColor());
            vehicleMileMonthVO.setVehicleType(resultListDatum.getVehicleType());
            vehicleMileMonthVO.setEnterpriseName(resultListDatum.getOrgName());
            vehicleMileMonthVO.setMonthReport(resultListDatum.getCount());
            vehicleMileMonthVO.setDays(resultListDatum.getDays());
            result.add(vehicleMileMonthVO);
        }
        return result;
    }

    @Override
    public JsonResultBean exportVehicleMonth(String vehicleIds, String month, String simpleQueryParam) {
        if (StringUtils.isBlank(vehicleIds) || StringUtils.isBlank(month)) {
            return new JsonResultBean(JsonResultBean.FAULT, "参数不能为空");
        }
        Set<String> filterMonitorIds = userService.fuzzySearchFilterMonitorIds(simpleQueryParam, vehicleIds);
        if (CollectionUtils.isEmpty(filterMonitorIds)) {
            return new JsonResultBean(JsonResultBean.FAULT, "模糊搜索后无数据");
        }
        String fileName = "车辆日里程统计报表" + Date8Utils.getValToTime(LocalDateTime.now());
        OfflineExportInfo instance = OfflineExportInfo.getInstance("车辆里程统计", fileName + ".xls");
        TreeMap<String, String> params = new TreeMap<>();
        params.put("monitorIds", String.join(",", filterMonitorIds));
        params.put("month", month.replaceAll("-", ""));
        instance.assembleCondition(params, OffLineExportBusinessId.VEHICLE_DAILY_MILEAGE_REPORT);
        return ControllerTemplate.addExportOffline(exportService, instance, "导出列表异常");
    }

    @Override
    public List<VehicleMileDetailVO> getDetailList(String vehicleIds, String startTime, String endTime,
        String simpleQueryParam) throws Exception {
        Map<String, String> params = new HashMap<>(6);
        Set<String> filterMonitorIds = userService.fuzzySearchFilterMonitorIds(simpleQueryParam, vehicleIds);
        if (CollectionUtils.isEmpty(filterMonitorIds)) {
            return Collections.emptyList();
        }
        params.put("monitorIds", String.join(",", filterMonitorIds));
        params.put("startTime", startTime.replaceAll("-", "").replaceAll(" ", "").replaceAll(":", ""));
        params.put("endTime", endTime.replaceAll("-", "").replaceAll(" ", "").replaceAll(":", ""));
        String passResult = HttpClientUtil.send(PaasCloudUrlEnum.VEHICLE_MILEAGE_STATISTICS, params);
        List<VehicleMileageStatisticsDO> resultListData =
            PaasCloudUrlUtil.getResultListData(passResult, VehicleMileageStatisticsDO.class);
        if (CollectionUtils.isEmpty(resultListData)) {
            return Collections.emptyList();
        }
        List<VehicleMileDetailVO> results = new ArrayList<>();
        for (VehicleMileageStatisticsDO resultListDatum : resultListData) {
            VehicleMileDetailVO vehicleMileDetailVO = new VehicleMileDetailVO();
            vehicleMileDetailVO.setVehicleBrandNumber(resultListDatum.getMonitorName());
            vehicleMileDetailVO.setVehicleBrandColor(PlateColor.getNameOrBlankByCode(resultListDatum.getPlateColor()));
            vehicleMileDetailVO.setVehicleType(resultListDatum.getVehicleType());
            vehicleMileDetailVO.setGroupName(resultListDatum.getOrgName());
            String driveStartTime = resultListDatum.getDriveStartTime();
            String driveEndTime = resultListDatum.getDriveEndTime();
            vehicleMileDetailVO.setTimeSection(
                DateUtil.getStringToString(driveStartTime, DateUtil.DATE_FORMAT, null) + " - " + DateUtil
                    .getStringToString(driveEndTime, DateUtil.DATE_FORMAT, null));
            Double gpsMile = resultListDatum.getGpsMile();
            vehicleMileDetailVO.setGpsMile(gpsMile == null ? "0.0" : String.valueOf(gpsMile));
            results.add(vehicleMileDetailVO);
        }
        return results;
    }

    @Override
    public JsonResultBean exportVehicleMileDetail(String vehicleIds, String startTime, String endTime,
        String simpleQueryParam) {
        if (StringUtils.isBlank(vehicleIds) || StringUtils.isBlank(startTime) || StringUtils.isBlank(endTime)) {
            return new JsonResultBean(JsonResultBean.FAULT, "参数不能为空");
        }
        Set<String> filterMonitorIds = userService.fuzzySearchFilterMonitorIds(simpleQueryParam, vehicleIds);
        if (CollectionUtils.isEmpty(filterMonitorIds)) {
            return new JsonResultBean(JsonResultBean.FAULT, "模糊搜索后无数据");
        }
        String fileName = "车辆里程统计报表" + Date8Utils.getValToTime(LocalDateTime.now());
        OfflineExportInfo instance = OfflineExportInfo.getInstance("车辆里程统计", fileName + ".xls");
        TreeMap<String, String> params = new TreeMap<>();
        params.put("monitorIds", String.join(",", filterMonitorIds));
        params.put("startTime", startTime.replaceAll("-", "").replaceAll(" ", "").replaceAll(":", ""));
        params.put("endTime", endTime.replaceAll("-", "").replaceAll(" ", "").replaceAll(":", ""));
        instance.assembleCondition(params, OffLineExportBusinessId.VEHICLE_MILEAGE_STATISTICS);
        return ControllerTemplate.addExportOffline(exportService, instance, "导出列表异常");
    }
}

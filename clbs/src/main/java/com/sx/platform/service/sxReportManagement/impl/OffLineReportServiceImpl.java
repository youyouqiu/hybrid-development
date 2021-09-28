package com.sx.platform.service.sxReportManagement.impl;

import com.alibaba.fastjson.JSONObject;
import com.sx.platform.domain.sxReport.OffLineReport;
import com.sx.platform.service.sxReportManagement.OffLineReportService;
import com.zw.lkyw.domain.OffLineReportExportDTO;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.domain.VehicleDO;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.GroupDTO;
import com.zw.platform.basic.repository.NewVehicleDao;
import com.zw.platform.basic.service.MonitorService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.domain.basicinfo.enums.PlateColor;
import com.zw.platform.util.DateUtil;
import com.zw.platform.util.common.AddressUtil;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.platform.util.excel.ExportExcelParam;
import com.zw.platform.util.excel.ExportExcelUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Administrator
 */
@Service
public class OffLineReportServiceImpl implements OffLineReportService {

    @Autowired
    private UserService userService;

    @Autowired
    private MonitorService monitorService;

    @Autowired
    private NewVehicleDao newVehicleDao;

    @Override
    public List<OffLineReport> getList(String vehicleList, Integer day) throws Exception {
        if (StringUtils.isBlank(vehicleList) || day == null) {
            return new ArrayList<>();
        }
        List<String> vehicleIds = Arrays.stream(vehicleList.split(",")).distinct().collect(Collectors.toList());
        // 离线的车辆
        Set<String> offLineMonIds = monitorService.getOffLineMonIds(vehicleIds);
        if (CollectionUtils.isEmpty(offLineMonIds)) {
            return new ArrayList<>();
        }
        List<RedisKey> redisKeys = HistoryRedisKeyEnum.MONITOR_LOCATION.ofs(offLineMonIds);
        List<String> monitorLocationJsonStrList = RedisHelper.batchGetString(redisKeys);
        Map<String, BindDTO> bindInfoList = VehicleUtil.batchGetBindInfosByRedis(offLineMonIds);
        Map<String, String> userGroupIdAndNameMap = userService.getCurrentUserGroupList().stream()
            .collect(Collectors.toMap(GroupDTO::getId, GroupDTO::getName));
        Map<String, VehicleDO> vehicleMap = newVehicleDao.getVehicleListByIds(offLineMonIds).stream()
            .collect(Collectors.toMap(VehicleDO::getId, Function.identity()));
        long nowTime = System.currentTimeMillis();
        List<OffLineReport> offLineReports = new ArrayList<>();
        OffLineReport offLineReport;
        for (String monitorLocationJsonStr : monitorLocationJsonStrList) {
            if (StringUtils.isBlank(monitorLocationJsonStr)) {
                continue;
            }
            JSONObject jsonObject = JSONObject.parseObject(monitorLocationJsonStr);
            JSONObject data = jsonObject.getJSONObject("data").getJSONObject("msgBody");
            JSONObject monitorInfo = data.getJSONObject("monitorInfo");
            if (monitorInfo == null) {
                continue;
            }
            String monitorId = monitorInfo.getString("monitorId");
            BindDTO bindDTO = bindInfoList.get(monitorId);
            if (bindDTO == null) {
                continue;
            }
            LocalDateTime gpsDateTime = DateUtil.YMD_HMS_20.ofDateTime(data.getString("gpsTime")).orElse(null);
            if (gpsDateTime == null) {
                continue;
            }
            long pastTime = gpsDateTime.toInstant(ZoneOffset.of("+8")).toEpochMilli();
            // 离线时长
            long duration = nowTime - pastTime;
            long dayTime = day * 86400000L;
            if (duration < dayTime) {
                continue;
            }
            String offTime = DateUtil.formatDuring(duration);
            int minuteIndex = offTime.indexOf("分");
            String offDate;
            if (duration > 60000 && minuteIndex != -1) {
                offDate = offTime.substring(0, minuteIndex + 1);
            } else {
                offDate = offTime;
            }
            String latitude = data.getString("latitude") != null ? data.getString("latitude") : "";
            String longitude = data.getString("longitude") != null ? data.getString("longitude") : "";
            offLineReport = new OffLineReport();
            offLineReport.setBrnad(bindDTO.getName());
            offLineReport.setColor(PlateColor.getNameOrBlankByCode(bindDTO.getPlateColor()));
            offLineReport.setGroupName(bindDTO.getOrgName());
            String groupIds = bindDTO.getGroupId();
            String groupNames =
                Arrays.stream(groupIds.split(",")).map(userGroupIdAndNameMap::get).filter(Objects::nonNull)
                    .collect(Collectors.joining(","));
            offLineReport.setAssignmentName(groupNames);
            offLineReport.setDeviceNumber(bindDTO.getDeviceNumber());
            offLineReport.setSimcardNumber(bindDTO.getSimCardNumber());
            offLineReport.setOffLineDay(offDate);
            offLineReport.setLastTime(DateUtil.YMD_HMS.format(gpsDateTime).orElse(""));
            offLineReport.setLastLocation(longitude + "," + latitude);
            VehicleDO vehicleDO = vehicleMap.get(monitorId);
            if (vehicleDO != null) {
                String vehicleOwner = vehicleDO.getVehicleOwner();
                offLineReport.setVehicleOwner(vehicleOwner);
                String vehicleOwnerPhone = vehicleDO.getVehicleOwnerPhone();
                offLineReport.setVehicleOwnerPhone(vehicleOwnerPhone);
            }
            offLineReports.add(offLineReport);
        }
        return offLineReports;
    }

    @Override
    public boolean export(HttpServletResponse res, String simpleQueryParam) throws Exception {
        ExportExcelUtil.setResponseHead(res, "离线查询报表");
        RedisKey redisKey = HistoryRedisKeyEnum.USER_OFF_LINE_REPORT_INFO_LIST.of(userService.getCurrentUserUuid());
        List<OffLineReport> offLineReport = RedisHelper.getList(redisKey, OffLineReport.class);
        if (StringUtils.isNotBlank(simpleQueryParam)) {
            offLineReport = offLineReport.stream()
                .filter(obj -> StringUtils.isNotBlank(obj.getBrnad()) && obj.getBrnad().contains(simpleQueryParam))
                .collect(Collectors.toList());
        }

        final Set<String> lngLats =
                offLineReport.stream().map(OffLineReport::getLastLocation).collect(Collectors.toSet());
        final Map<String, String> locationPairMap = AddressUtil.batchInverseAddress(lngLats);
        for (OffLineReport off : offLineReport) {
            off.setLastLocation(locationPairMap.get(off.getLastLocation()));
        }
        return ExportExcelUtil
            .export(new ExportExcelParam(null, 1, offLineReport, OffLineReport.class, null, res.getOutputStream()));
    }

    @Override
    public boolean exportForLkyw(HttpServletResponse res, String simpleQueryParam) throws Exception {
        ExportExcelUtil.setResponseHead(res, "两客一危-离线车辆");
        String username = userService.getCurrentUserInfo().getUsername();
        RedisKey redisKey = HistoryRedisKeyEnum.OFFLINE_REPORT_INFORMATION.of(username);
        List<OffLineReport> offLineReportList = RedisHelper.getList(redisKey, OffLineReport.class);
        List<OffLineReportExportDTO> offLineReport = new ArrayList<>();
        BeanCopier beanCopier = BeanCopier.create(OffLineReport.class, OffLineReportExportDTO.class, false);
        for (OffLineReport offLine : offLineReportList) {
            OffLineReportExportDTO exportDTO = new OffLineReportExportDTO();
            beanCopier.copy(offLine, exportDTO, null);
            offLineReport.add(exportDTO);
        }

        if (StringUtils.isNotBlank(simpleQueryParam)) {
            offLineReport = offLineReport.stream()
                .filter(obj -> StringUtils.isNotBlank(obj.getBrnad()) && obj.getBrnad().contains(simpleQueryParam))
                .collect(Collectors.toList());
        }

        final Set<String> lngLats =
                offLineReport.stream().map(OffLineReportExportDTO::getLastLocation).collect(Collectors.toSet());
        Map<String, String> locationPairMap = AddressUtil.batchInverseAddress(lngLats);
        for (OffLineReportExportDTO off : offLineReport) {
            off.setLastLocation(locationPairMap.get(off.getLastLocation()));
        }
        return ExportExcelUtil.export(
            new ExportExcelParam(null, 1, offLineReport, OffLineReportExportDTO.class, null, res.getOutputStream()));
    }

}

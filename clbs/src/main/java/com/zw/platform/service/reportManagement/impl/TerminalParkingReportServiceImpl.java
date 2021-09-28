package com.zw.platform.service.reportManagement.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import com.zw.app.util.AppParamCheckUtil;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.GroupDTO;
import com.zw.platform.basic.repository.NewProfessionalsDao;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.basicinfo.MonitorBindProfessionalDo;
import com.zw.platform.dto.reportManagement.ParkingInfoDto;
import com.zw.platform.dto.reportManagement.ParkingInfoPaasDto;
import com.zw.platform.service.reportManagement.TerminalParkingReportService;
import com.zw.platform.util.common.AddressUtil;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.platform.util.excel.ExportExcelParam;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.platform.util.report.PaasCloudUrlEnum;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Administrator
 */
@Service
public class TerminalParkingReportServiceImpl implements TerminalParkingReportService {

    @Autowired
    private NewProfessionalsDao newProfessionalsDao;

    @Autowired
    private UserService userService;

    @Override
    public List<ParkingInfoDto> getStopBigDataFromPaas(String vehicleIds, String startTime, String endTime)
        throws Exception {
        if (StringUtils.isBlank(vehicleIds) || !AppParamCheckUtil.checkDate(startTime, 2) || !AppParamCheckUtil
            .checkDate(endTime, 2)) {
            return null;
        }
        //转换时间格式为yyyyMMddHHmmss的字符串
        int dateType = startTime.length() == 10 ? 2 : 1;
        String beforeFormat = Objects.equals(dateType, 2) ? DateUtil.DATE_Y_M_D_FORMAT : DateUtil.DATE_FORMAT_SHORT;
        startTime = DateUtil.formatDate(startTime, beforeFormat, DateUtil.DATE_YMD_FORMAT);
        endTime = DateUtil.formatDate(endTime, beforeFormat, DateUtil.DATE_YMD_FORMAT);
        List<String> moIdList = Arrays.stream(vehicleIds.split(",")).distinct().collect(Collectors.toList());
        Map<String, String> params =
            ImmutableMap.of("monitorIds", StringUtils.join(moIdList, ","), "startTime", startTime, "endTime", endTime);
        //调用paas-cloud接口
        String resultStr = HttpClientUtil.send(PaasCloudUrlEnum.TERMINAL_STOP_MILEAGE_STATISTICS_URL, params);
        JSONObject obj = JSONObject.parseObject(resultStr);
        List<ParkingInfoPaasDto> initialList = JSONArray.parseArray(obj.getString("data"), ParkingInfoPaasDto.class);
        if (CollectionUtils.isEmpty(initialList)) {
            return new ArrayList<>();
        }
        Map<String, RedisKey> redisKeyMap = new HashMap<>(16);
        for (String moId : moIdList) {
            redisKeyMap.put(moId, HistoryRedisKeyEnum.SENSOR_MESSAGE.of(moId));
        }
        Map<String, String> sensorMessageMap = RedisHelper.batchGetStringMap(redisKeyMap);
        Map<String, String> userGroupIdAndNameMap = userService.getCurrentUserGroupList()
            .stream()
            .collect(Collectors.toMap(GroupDTO::getId, GroupDTO::getName));
        Map<String, List<MonitorBindProfessionalDo>> monitorProfessionalListMap =
            newProfessionalsDao.getMonitorBindProfessionalList(moIdList)
                .stream()
                .collect(Collectors.groupingBy(MonitorBindProfessionalDo::getMoId));
        Map<String, BindDTO> bindInfoMap = VehicleUtil.batchGetBindInfosByRedis(moIdList);
        List<ParkingInfoDto> resultList = new ArrayList<>();
        //遍历判断是否绑定里程传感器，筛选数据到最终集合中
        for (ParkingInfoPaasDto parkingInfoPaasDto : initialList) {
            String monitorId = parkingInfoPaasDto.getMonitorId();
            ParkingInfoDto parkingInfoDto = new ParkingInfoDto();
            parkingInfoDto.setMonitorId(monitorId);
            parkingInfoDto.setMonitorName(parkingInfoPaasDto.getMonitorName());
            parkingInfoDto.setAssignmentName(parkingInfoPaasDto.getAssignmentName());
            if (sensorMessageMap.get(monitorId) != null) {
                parkingInfoDto.setIdleSpeedMile(parkingInfoPaasDto.getSensorIdleSpeedMile());
                parkingInfoDto.setStopNum(parkingInfoPaasDto.getSensorStopNum());
                parkingInfoDto.setDuration(parkingInfoPaasDto.getSensorDuration());
                parkingInfoDto.setAddress(parkingInfoPaasDto.getSensorStopAddress());
                parkingInfoDto.setStopLocation(parkingInfoPaasDto.getSensorStopLocation());
            } else {
                parkingInfoDto.setIdleSpeedMile(parkingInfoPaasDto.getDeviceIdleSpeedMile());
                parkingInfoDto.setStopNum(parkingInfoPaasDto.getDeviceStopNum());
                parkingInfoDto.setDuration(parkingInfoPaasDto.getDeviceDuration());
                parkingInfoDto.setAddress(parkingInfoPaasDto.getDeviceStopAddress());
                parkingInfoDto.setStopLocation(parkingInfoPaasDto.getDeviceStopLocation());
            }
            parkingInfoDto.setStopTime(DateUtil.formatTime(parkingInfoDto.getDuration() * 1000));
            BindDTO bindDTO = bindInfoMap.get(monitorId);
            if (bindDTO != null) {
                String groupIds = bindDTO.getGroupId();
                String groupNames = Arrays
                    .stream(groupIds.split(","))
                    .map(userGroupIdAndNameMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(","));
                parkingInfoDto.setAssignmentName(groupNames);
            }
            List<MonitorBindProfessionalDo> monitorBindProfessionalDos = monitorProfessionalListMap.get(monitorId);
            if (CollectionUtils.isNotEmpty(monitorBindProfessionalDos)) {
                List<String> professionalsNameList = new ArrayList<>();
                List<String> phoneList = new ArrayList<>();
                boolean allPhoneIsBlank = true;
                for (MonitorBindProfessionalDo bindProfessionalDo : monitorBindProfessionalDos) {
                    String professionalName = bindProfessionalDo.getProfessionalName();
                    professionalsNameList.add(professionalName == null ? "" : professionalName);
                    String phone = bindProfessionalDo.getPhone();
                    phoneList.add(phone == null ? "" : phone);
                    if (StringUtils.isNotBlank(phone)) {
                        allPhoneIsBlank = false;
                    }
                }
                parkingInfoDto.setEmployeeName(StringUtils.join(professionalsNameList, ","));
                if (allPhoneIsBlank) {
                    parkingInfoDto.setEmployeePhone(null);
                } else {
                    parkingInfoDto.setEmployeePhone(StringUtils.join(phoneList, ","));
                }
            }
            resultList.add(parkingInfoDto);
        }
        final Set<String> lngLats = resultList.stream()
                .filter(o -> StringUtils.isEmpty(o.getAddress()))
                .map(ParkingInfoDto::getStopLocation)
                .collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(lngLats)) {
            final Map<String, String> addressMap = AddressUtil.batchInverseAddress(lngLats);
            resultList.stream()
                    .filter(o -> StringUtils.isEmpty(o.getAddress()))
                    .forEach(o -> o.setAddress(addressMap.get(o.getStopLocation())));
        }
        return resultList;
    }

    @Override
    public boolean exportQueryData(String vehicleId, String startTime, String endTime, String simpleQueryParam,
        int exportType) throws Exception {
        String userUuid = userService.getCurrentUserUuid();
        RedisKey redisKey = HistoryRedisKeyEnum.USER_STOP_DATA_LIST.of(userUuid);
        List<ParkingInfoDto> parkingInfoList = getStopBigDataFromPaas(vehicleId, startTime, endTime);
        if (StringUtils.isNotBlank(simpleQueryParam)) {
            parkingInfoList = parkingInfoList.stream()
                .filter(obj -> StringUtils.isNotBlank(obj.getMonitorName())
                    && obj.getMonitorName().contains(simpleQueryParam))
                .collect(Collectors.toList());
        }
        RedisHelper.delete(redisKey);
        RedisHelper.addToList(redisKey, parkingInfoList);
        RedisHelper.expireKey(redisKey, RedisHelper.SIX_HOUR_REDIS_EXPIRE);
        return true;
    }

    @Override
    public boolean export(HttpServletResponse res) throws IOException {
        ExportExcelUtil.setResponseHead(res, "停止报表");
        String userUuid = userService.getCurrentUserUuid();
        RedisKey redisKey = HistoryRedisKeyEnum.USER_STOP_DATA_LIST.of(userUuid);
        List<ParkingInfoDto> parkingInfoList = RedisHelper.getList(redisKey, ParkingInfoDto.class);
        return ExportExcelUtil
            .export(new ExportExcelParam(null, 1, parkingInfoList, ParkingInfoDto.class, null, res.getOutputStream()));
    }

}

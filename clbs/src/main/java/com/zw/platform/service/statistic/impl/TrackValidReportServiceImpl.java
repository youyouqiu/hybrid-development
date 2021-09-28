package com.zw.platform.service.statistic.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.core.TypeCacheManger;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.GroupDTO;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.enums.PlateColor;
import com.zw.platform.domain.statistic.TrackInfo;
import com.zw.platform.domain.statistic.TrackValidReportData;
import com.zw.platform.domain.statistic.TrackValidReportInfo;
import com.zw.platform.service.statistic.TrackValidReportService;
import com.zw.platform.util.LocalDateUtils;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.platform.util.excel.ExportExcelParam;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.platform.util.paas.PaasCloudUrlUtil;
import com.zw.platform.util.report.PaasCloudHBaseAccessEnum;
import com.zw.platform.util.report.PaasCloudUrlEnum;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author zhouzongbo on 2019/5/31 15:56
 */
@Service
public class TrackValidReportServiceImpl implements TrackValidReportService {

    @Autowired
    private UserService userService;

    private TypeCacheManger cacheManger = TypeCacheManger.getInstance();

    @Override
    public JsonResultBean trackValidityReportService(String monitorId, String startTime, String endTime) {
        // 返回集合
        List<TrackValidReportInfo> resultList = new ArrayList<>();
        RedisKey redisKey = HistoryRedisKeyEnum.TRACK_VALID_REPORT.of(SystemHelper.getCurrentUsername());
        if (RedisHelper.isContainsKey(redisKey)) {
            RedisHelper.delete(redisKey);
        }
        // 组装查询条件
        Long startTimeL = LocalDateUtils.parseDateTime(startTime).getTime() / 1000;
        Long endTimeL = LocalDateUtils.parseDateTime(endTime).getTime() / 1000;
        // 循环组装数据
        Map<String, String> configList = RedisHelper.getHashMap(RedisKeyEnum.MONITOR_INFO.of(monitorId));

        if (Objects.isNull(configList)) {
            return new JsonResultBean(resultList);
        }
        // 查询数据
        List<TrackValidReportInfo> trackValidReportInfoList =
                getPositionalByMonitorIdAndTime(monitorId, startTimeL, endTimeL);
        if (CollectionUtils.isEmpty(trackValidReportInfoList)) {
            return new JsonResultBean(resultList);
        }
        getResultList(resultList, configList, trackValidReportInfoList);
        // 存入数据到Redis, 用于导出
        RedisHelper.addToList(redisKey, resultList);
        RedisHelper.expireKey(redisKey, RedisHelper.SIX_HOUR_REDIS_EXPIRE);
        return new JsonResultBean(resultList);
    }

    private List<TrackValidReportInfo> getPositionalByMonitorIdAndTime(String monitorId, Long startTimeL,
                                                                       Long endTimeL) {
        final Map<String, String> params = new HashMap<>(8);
        params.put("vehicleId", monitorId);
        params.put("startTime", String.valueOf(startTimeL));
        params.put("endTime", String.valueOf(endTimeL));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.FIND_POSITIONAL_BY_MONITOR_ID_AND_TIME, params);
        return PaasCloudUrlUtil.getResultListData(str, TrackValidReportInfo.class);
    }

    @Override
    public JsonResultBean getTrackValidListByF3Pass(String monitorId, String startTime, String endTime) {
        List<TrackValidReportInfo> resultList = new ArrayList<>();
        RedisKey redisKey = HistoryRedisKeyEnum.TRACK_VALID_REPORT.of(SystemHelper.getCurrentUsername());
        if (RedisHelper.isContainsKey(redisKey)) {
            RedisHelper.delete(redisKey);
        }
        Map<String, String> queryParam = new HashMap<>(16);
        queryParam.put("monitorId", monitorId);
        startTime = startTime.replaceAll("-", "").replaceAll(":", "").replaceAll(" ", "");
        queryParam.put("startTime", startTime);
        endTime = endTime.replaceAll("-", "").replaceAll(":", "").replaceAll(" ", "");
        queryParam.put("endTime", endTime);
        String queryResult = HttpClientUtil.send(PaasCloudUrlEnum.TRACK_VALID_REPORT_URL, queryParam);
        if (StringUtils.isBlank(queryResult)) {
            return new JsonResultBean(false, "pass层数据查询异常");
        }
        JSONObject object = JSON.parseObject(queryResult);
        TrackValidReportData data = JSONObject.parseObject(object.getString("data"), TrackValidReportData.class);
        if (Objects.isNull(data)) {
            return new JsonResultBean(resultList);
        }
        List<TrackInfo> trackInfo = data.getTrackInfo();
        BindDTO bindDTO = VehicleUtil.getBindInfoByRedis(monitorId);
        userService.setObjectTypeName(bindDTO);
        boolean bindInfoIsNull = bindDTO == null;
        Map<String, String> userGroupIdAndNameMap = userService.getCurrentUserGroupList().stream()
            .collect(Collectors.toMap(GroupDTO::getId, GroupDTO::getName));
        String groupIds = bindInfoIsNull ? null : bindDTO.getGroupId();
        String assignmentName = groupIds == null ? null :
            Arrays.stream(groupIds.split(",")).map(userGroupIdAndNameMap::get).filter(Objects::nonNull)
                .collect(Collectors.joining(","));
        String brand =  bindInfoIsNull  ? null : bindDTO.getName();
        String groupName = bindInfoIsNull ? null : bindDTO.getOrgName();
        String plateColorStr = bindInfoIsNull ? null : PlateColor.getNameOrBlankByCode(bindDTO.getPlateColor());
        for (TrackInfo info : trackInfo) {
            TrackValidReportInfo reportInfo = new TrackValidReportInfo();
            reportInfo.setBrand(brand);
            reportInfo.setAssignmentName(assignmentName);
            reportInfo.setGroupName(groupName);
            reportInfo.setColor(plateColorStr);
            reportInfo.setVehicleType(bindInfoIsNull ? null : bindDTO.getObjectTypeName());
            reportInfo.setLatitude(info.getLatitude());
            reportInfo.setLongtitude(info.getLongitude());
            reportInfo.setTrackValid(info.getTrackValid());
            reportInfo.setTrackValidStr(Objects.equals(1, info.getTrackValid()) ? "正常" : "异常");
            reportInfo.setStartSpeed(info.getStartSpeed());
            reportInfo.setEndSpeed(info.getEndSpeed());
            reportInfo.setStartMileage(info.getStartMileage());
            reportInfo.setEndMileage(info.getEndMileage());
            resultList.add(reportInfo);
        }
        // 存入数据到Redis, 用于导出
        RedisHelper.addToList(redisKey, resultList);
        RedisHelper.expireKey(redisKey, RedisHelper.SIX_HOUR_REDIS_EXPIRE);
        return new JsonResultBean(resultList);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void exportTrackValidList(HttpServletResponse response, String simpleQueryParam) throws IOException {
        // 获取导出数据
        RedisKey redisKey = HistoryRedisKeyEnum.TRACK_VALID_REPORT.of(SystemHelper.getCurrentUsername());
        List<TrackValidReportInfo> trackValidReportInfoList = RedisHelper.getList(redisKey, TrackValidReportInfo.class);
        // 根据查询条件过滤数据
        if (CollectionUtils.isNotEmpty(trackValidReportInfoList) && StringUtils.isNotEmpty(simpleQueryParam)) {
            trackValidReportInfoList =
                trackValidReportInfoList.stream().filter(info -> info.getBrand().contains(simpleQueryParam))
                    .collect(Collectors.toList());
        }
        ExportExcelUtil.setResponseHead(response, "轨迹有效性报表");
        ExportExcelUtil.export(new ExportExcelParam("", 1, trackValidReportInfoList, TrackValidReportInfo.class, null,
            response.getOutputStream()));
    }

    private void getResultList(List<TrackValidReportInfo> resultList, Map<String, String> configList,
        List<TrackValidReportInfo> trackValidReportInfoList) {
        String brand = configList.get("name");
        String assignmentName = configList.get("groupName");
        String groupName = configList.get("orgName");
        String plateColorStr = VehicleUtil.getPlateColorStr(configList.get("plateColor"));
        String vehType = cacheManger.getVehicleType(configList.get("vehicleType")).getType();
        trackValidReportInfoList = trackValidReportInfoList.stream()
            .sorted(Comparator.comparingLong(TrackValidReportInfo::getStartTime).reversed())
            .collect(Collectors.toList());
        // 后一条数据, 计算前一条数据时间、里程以及轨迹有效性
        TrackValidReportInfo behindInfo = null;
        for (TrackValidReportInfo info : trackValidReportInfoList) {
            info.setBrand(brand);
            info.setAssignmentName(assignmentName);
            info.setGroupName(groupName);
            info.setColor(plateColorStr);
            info.setVehicleType(vehType);
            if (Objects.isNull(behindInfo)) {
                info.setEndMileage(info.getStartMileage());
                info.setEndSpeed(info.getStartSpeed());
                info.setEndTime(info.getStartTime());
                info.setTrackValid();
            } else {
                info.setEndMileage(behindInfo.getStartMileage());
                info.setEndSpeed(behindInfo.getStartSpeed());
                info.setEndTime(behindInfo.getStartTime());
                info.setTrackValid();
            }
            behindInfo = info;
            resultList.add(info);
        }
    }
}

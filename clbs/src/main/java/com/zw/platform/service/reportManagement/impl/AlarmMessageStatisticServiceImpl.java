package com.zw.platform.service.reportManagement.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.alarm.AlarmInfo;
import com.zw.platform.domain.reportManagement.AlarmMessageInfo;
import com.zw.platform.domain.reportManagement.PassCloudAlarmCount;
import com.zw.platform.domain.reportManagement.PassCloudAlarmNumInfo;
import com.zw.platform.domain.vas.alram.AlarmType;
import com.zw.platform.dto.reportManagement.AlarmDetailDto;
import com.zw.platform.dto.reportManagement.AlarmMessageDto;
import com.zw.platform.repository.vas.AlarmSearchDao;
import com.zw.platform.service.alarm.AlarmSearchService;
import com.zw.platform.service.reportManagement.AlarmMessageStatisticService;
import com.zw.platform.util.LocalDateUtils;
import com.zw.platform.util.common.AddressUtil;
import com.zw.platform.util.common.AlarmTypeUtil;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.UuidUtils;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.platform.util.excel.ExportExcelParam;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.platform.util.paas.PaasCloudUrlUtil;
import com.zw.platform.util.report.PaasCloudHBaseAccessEnum;
import com.zw.platform.util.report.PaasCloudUrlEnum;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author zhouzongbo on 2019/5/18 9:27
 */
@Service
public class AlarmMessageStatisticServiceImpl implements AlarmMessageStatisticService {

    private static Logger log = LogManager.getLogger(AlarmMessageStatisticServiceImpl.class);

    @Autowired
    private UserService userService;

    @Autowired
    private AlarmSearchDao alarmSearchDao;

    @Autowired
    private AlarmSearchService alarmSearchService;

    @Override
    public List<AlarmMessageInfo> getAlarmMessageList(String vehicleIds, String start, String end, String alarmTypes)
        throws Exception {
        // 返回数据
        List<AlarmMessageInfo> resultAlarmMessageInfoList = new ArrayList<>();
        // 车辆id转换为byte[]集合
        List<String> vehicleIdList = Arrays.asList(vehicleIds.split(","));
        start += " 00:00:00";
        end += " 23:59:59";
        Long startTime = DateUtil.getMillisecond(LocalDateUtils.parseDateTime(start).getTime());
        Long endTime = DateUtil.getMillisecond(LocalDateUtils.parseDateTime(end).getTime());
        // 报警类型
        List<Integer> alarmTypeList = AlarmTypeUtil.typeList(alarmTypes);
        List<AlarmMessageInfo> alarmMessageList =
                getAlarmMessageList(vehicleIdList, startTime, endTime, alarmTypeList);
        // RedisKey
        String userUuid = userService.getCurrentUserUuid();
        // 详情的key
        RedisKey alarmDetailRedisKey = HistoryRedisKeyEnum.ALARM_STATISTICS_DETAIL.of(userUuid);
        RedisKey alarmMessageListKey = HistoryRedisKeyEnum.ALARM_STATISTICS_LIST.of(userUuid);
        if (RedisHelper.isContainsKey(alarmDetailRedisKey)) {
            RedisHelper.delete(alarmDetailRedisKey);
        }
        if (RedisHelper.isContainsKey(alarmMessageListKey)) {
            RedisHelper.delete(alarmMessageListKey);
        }
        if (CollectionUtils.isEmpty(alarmMessageList)) {
            return resultAlarmMessageInfoList;
        }
        getAlarmResultData(vehicleIdList, alarmMessageList, alarmDetailRedisKey, resultAlarmMessageInfoList,
            alarmMessageListKey);
        return resultAlarmMessageInfoList;
    }

    private List<AlarmMessageInfo> getAlarmMessageList(List<String> vehicleIds, Long startTime, Long endTime,
                                                       List<Integer> alarmTypes) {
        if (CollectionUtils.isEmpty(vehicleIds) || CollectionUtils.isEmpty(alarmTypes)) {
            return new ArrayList<>();
        }
        Map<String, String> params = new HashMap<>(8);
        params.put("monitorIds", JSON.toJSONString(vehicleIds));
        params.put("startTime", String.valueOf(startTime));
        params.put("endTime", String.valueOf(endTime));
        params.put("alarmTypes", JSON.toJSONString(alarmTypes));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_ALARM_MESSAGE_LIST, params);
        return PaasCloudUrlUtil.getResultListData(str, AlarmMessageInfo.class);
    }

    @Override
    public List<AlarmMessageDto> getAlarmMessageListNew(String vehicleIds, String startTime, String endTime,
        String alarmTypes) throws Exception {
        List<AlarmMessageDto> alarmMessageList = new ArrayList<>();
        String userUuid = userService.getCurrentUserUuid();
        // 列表中的key
        RedisKey alarmMessageListKey = HistoryRedisKeyEnum.ALARM_STATISTICS_LIST.of(userUuid);
        RedisHelper.delete(alarmMessageListKey);
        //转换时间格式为yyyyMMddHHmmss的字符串
        startTime = startTime.replaceAll("-", "") + "000000";
        endTime = endTime.replaceAll("-", "") + "235959";
        // 报警类型转换
        List<Integer> alarmTypeList = AlarmTypeUtil.typeList(alarmTypes);
        alarmTypes = StringUtils.join(alarmTypeList.toArray(), ",");
        //组装http请求参数
        Map<String, String> params = ImmutableMap
            .of("monitorIds", vehicleIds, "startTime", startTime, "endTime", endTime, "alarmTypes", alarmTypes);
        //调用paas-cloud接口
        String resultStr = HttpClientUtil.send(PaasCloudUrlEnum.ALARM_MESSAGE_STATISTICS_COUNT_URL, params);
        JSONObject obj = JSONObject.parseObject(resultStr);
        if (Objects.isNull(obj) || obj.getInteger("code") != 10000) {
            return null;
        }
        List<PassCloudAlarmCount> passCloudAlarmCountList =
            JSONObject.parseArray(obj.getString("data"), PassCloudAlarmCount.class);
        if (CollectionUtils.isEmpty(passCloudAlarmCountList)) {
            return alarmMessageList;
        }
        Map<String, BindDTO> configInfoMap = VehicleUtil.batchGetBindInfosByRedis(Arrays.asList(vehicleIds.split(",")));
        for (PassCloudAlarmCount passCloudAlarmCount : passCloudAlarmCountList) {
            String monitorId = passCloudAlarmCount.getMonitorId();
            BindDTO configInfo = configInfoMap.get(monitorId);
            if (configInfo == null) {
                continue;
            }
            String assignmentName = configInfo.getGroupName();
            String monitorName = configInfo.getName();
            Integer plateColor = configInfo.getPlateColor();
            String plateColorStr = plateColor != null ? VehicleUtil.getPlateColorStr(String.valueOf(plateColor)) : null;
            for (PassCloudAlarmNumInfo passCloudAlarmNumInfo : passCloudAlarmCount.getNumInfo()) {
                AlarmMessageDto alarmMessageDto = new AlarmMessageDto();
                alarmMessageDto.setMonitorId(monitorId);
                alarmMessageDto.setAssignmentName(assignmentName);
                alarmMessageDto.setMonitorName(monitorName);
                alarmMessageDto.setPlateColor(plateColor);
                alarmMessageDto.setPlateColorStr(plateColorStr);
                Integer alarmType = passCloudAlarmNumInfo.getAlarmType();
                alarmMessageDto.setAlarmType(alarmType);
                alarmMessageDto.setDescription(AlarmTypeUtil.getAlarmType(String.valueOf(alarmType)));
                alarmMessageDto.setAlarmNumber(passCloudAlarmNumInfo.getTotalNum());
                alarmMessageDto.setHandleNumber(passCloudAlarmNumInfo.getProcessedNum());
                alarmMessageList.add(alarmMessageDto);
            }
        }
        RedisHelper.addToList(alarmMessageListKey, alarmMessageList);
        RedisHelper.expireKey(alarmMessageListKey, RedisHelper.SIX_HOUR_REDIS_EXPIRE);
        return alarmMessageList;
    }

    @Override
    public List<AlarmMessageInfo> getAlarmDetailMessageList(String vehicleId, String alarmType) {
        String userUuid = userService.getCurrentUserUuid();
        RedisKey alarmDetailRedisKey = HistoryRedisKeyEnum.ALARM_STATISTICS_DETAIL.of(userUuid);
        List<AlarmMessageInfo> resultList = new ArrayList<>();
        if (RedisHelper.isContainsKey(alarmDetailRedisKey)) {
            String field = vehicleId + alarmType;
            String str = RedisHelper.hget(alarmDetailRedisKey, field);
            resultList = JSONObject.parseArray(str, AlarmMessageInfo.class);
        }
        return resultList;
    }

    @Override
    public List<AlarmDetailDto> getAlarmDetailMessageListNew(String vehicleId, String alarmType, String startTime,
        String endTime) throws Exception {
        List<AlarmDetailDto> resultList = new ArrayList<>();
        startTime = startTime.replaceAll("-", "") + "000000";
        endTime = endTime.replaceAll("-", "") + "235959";
        List<AlarmInfo> alarmList = alarmSearchService
            .getAlarmInfo(vehicleId, alarmType, startTime, endTime, null, null, null, null, null, AlarmInfo.class,
                null);
        if (CollectionUtils.isEmpty(alarmList)) {
            return resultList;
        }
        Set<String> lngLats = new HashSet<>();
        for (AlarmInfo alarmInfo : alarmList) {
            AlarmDetailDto alarmDetailDto = new AlarmDetailDto();
            alarmDetailDto.setSpeed(alarmInfo.getSpeed());
            alarmDetailDto.setStatus(String.valueOf(alarmInfo.getStatus()));
            String alarmStartLocation = alarmInfo.getAlarmStartLocation();
            alarmDetailDto.setAlarmStartLocation(alarmStartLocation);
            if (null != alarmStartLocation) {
                lngLats.add(alarmStartLocation);
            }
            Long alarmStartTime = alarmInfo.getAlarmStartTime();
            alarmDetailDto.setAlarmStartTime(alarmStartTime);
            alarmDetailDto.setAlarmStartTimeStr(DateUtil.getLongToDateStr(alarmStartTime, null));
            resultList.add(alarmDetailDto);
        }
        Map<String, String> addressMap = AddressUtil.batchInverseAddress(lngLats);
        for (AlarmDetailDto dto : resultList) {
            dto.setAlarmStartAddress(addressMap.get(dto.getAlarmStartLocation()));
        }
        return resultList;
    }

    @Override
    public void getExportAlarmMessageList(HttpServletResponse response, String fuzzyQuery) throws IOException {
        String userUuid = userService.getCurrentUserUuid();
        RedisKey alarmMessageListKey = HistoryRedisKeyEnum.ALARM_STATISTICS_LIST.of(userUuid);
        List<AlarmMessageDto> exportList = RedisHelper.getList(alarmMessageListKey, AlarmMessageDto.class);
        if (StringUtils.isNotEmpty(fuzzyQuery) && CollectionUtils.isNotEmpty(exportList)) {
            // 如果存在查询条件, 则需要根据查询条件过滤
            exportList =
                exportList.stream().filter(alarmMessageInfo -> alarmMessageInfo.getMonitorName().contains(fuzzyQuery))
                    .collect(Collectors.toList());
        }
        ExportExcelUtil
            .export(new ExportExcelParam("", 1, exportList, AlarmMessageDto.class, null, response.getOutputStream()));
    }

    private void getAlarmResultData(List<String> vehicleIdList, List<AlarmMessageInfo> alarmMessageList,
        RedisKey alarmDetailRedisKey, List<AlarmMessageInfo> resultAlarmMessageInfoList, RedisKey alarmMessageListKey) {
        // 查询
        // 车辆的缓存信息
        // 从数据库中查询出所有的报警类型
        List<AlarmType> allAlarmType = alarmSearchDao.findAllAlarmType();
        if (CollectionUtils.isEmpty(allAlarmType)) {
            return;
        }
        Map<String, String> alarmTypeMap =
            allAlarmType.stream().collect(Collectors.toMap(AlarmType::getPos, AlarmType::getName));
        // 组装数据
        buildAlarmMessageList(vehicleIdList, alarmMessageList, alarmTypeMap);
        // 点击一个监控对象的报警类型时, 展示的具体报警类型数据, 存入Redis中
        buildResultDataAndSaveToRedis(alarmMessageList, alarmDetailRedisKey, resultAlarmMessageInfoList,
            alarmMessageListKey);
    }

    /**
     * 组装数据
     * @param vehicleIdList    vehicleIdList
     * @param alarmMessageList alarmMessageList
     * @param alarmTypeMap     alarmTypeMap
     */
    private void buildAlarmMessageList(List<String> vehicleIdList, List<AlarmMessageInfo> alarmMessageList,
        Map<String, String> alarmTypeMap) {
        Map<String, BindDTO> configLists = VehicleUtil.batchGetBindInfosByRedis(vehicleIdList);
        // 根据车辆ID进行分组, 然后根据报警类型进行分组
        for (AlarmMessageInfo alarm : alarmMessageList) {
            String vehicleId = UuidUtils.getUUIDStrFromBytes(alarm.getVehicleIdByte());
            alarm.setVehicleId(vehicleId);
            // 车牌颜色
            BindDTO configList = configLists.get(vehicleId);
            if (Objects.isNull(configList)) {
                continue;
            }
            alarm.setPlateColorStr(VehicleUtil.getPlateColorStr(String.valueOf(configList.getPlateColor())));
            // 报警类型转换
            String alarmType = String.valueOf(alarm.getAlarmType());
            alarm.setAlarmStartTimeStr(LocalDateUtils.dateTimeFormat(new Date(alarm.getAlarmStartTime())));
            // 查询出来的报警类型,
            alarm.setAlarmTypeName(alarmTypeMap.get(alarmType));
            alarm.setGroupByKey(vehicleId + alarmType);
        }
    }

    /**
     * 组装返回数据, 并存储详情数据到Redis
     * @param alarmMessageList           alarmMessageList
     * @param resultAlarmMessageInfoList 返回数据集合
     * @param alarmMessageListKey        alarmMessageListKey
     */
    private void buildResultDataAndSaveToRedis(List<AlarmMessageInfo> alarmMessageList, RedisKey redisKey,
        List<AlarmMessageInfo> resultAlarmMessageInfoList, RedisKey alarmMessageListKey) {
        Map<String, List<AlarmMessageInfo>> alarmDetailToRedis =
            alarmMessageList.stream().collect(Collectors.groupingBy(AlarmMessageInfo::getGroupByKey));
        Map<String, String> map = new HashMap<>();
        alarmDetailToRedis.forEach((key, value) -> {
            // 统计某个监控对象某个报警类型的报警数量
            AlarmMessageInfo alarmMessageInfo = value.get(0);
            // 求和
            Integer hasDealAlarmNum = value.stream().mapToInt(AlarmMessageInfo::getStatus).sum();
            alarmMessageInfo.setHasDealNum(hasDealAlarmNum);
            alarmMessageInfo.setAlarmNumber(value.size());
            resultAlarmMessageInfoList.add(alarmMessageInfo);
            // 存储具体的报警信息到Redis中
            map.put(key, JSON.toJSONString(value));
        });
        RedisHelper.addMapToHash(redisKey, map);
        // 设置详情的过期时间
        RedisHelper.expireKey(redisKey, RedisHelper.SIX_HOUR_REDIS_EXPIRE);

        RedisHelper.addToList(alarmMessageListKey, resultAlarmMessageInfoList);
        RedisHelper.expireKey(alarmMessageListKey, RedisHelper.SIX_HOUR_REDIS_EXPIRE);
    }
}

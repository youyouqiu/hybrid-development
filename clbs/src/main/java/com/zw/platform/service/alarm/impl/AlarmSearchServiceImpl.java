package com.zw.platform.service.alarm.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cb.platform.domain.OffLineExportBusinessId;
import com.cb.platform.util.page.PassCloudResultBean;
import com.github.pagehelper.Page;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.MonitorTypeEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.GroupDTO;
import com.zw.platform.basic.dto.UserDTO;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.alarm.AlarmInfo;
import com.zw.platform.domain.alarm.DispatchAlarmInfo;
import com.zw.platform.domain.alarm.ForwardAlarmInfo;
import com.zw.platform.domain.alarm.IoAlarmInfo;
import com.zw.platform.domain.basicinfo.OfflineExportInfo;
import com.zw.platform.domain.basicinfo.enums.PlateColor;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.multimedia.HandleAlarms;
import com.zw.platform.domain.multimedia.HandleMultiAlarms;
import com.zw.platform.domain.oil.AlarmHandle;
import com.zw.platform.domain.vas.alram.AlarmSetting;
import com.zw.platform.domain.vas.alram.AlarmType;
import com.zw.platform.domain.vas.alram.query.AlarmSearchQuery;
import com.zw.platform.domain.vas.alram.query.AlarmSearchQuery809;
import com.zw.platform.dto.alarm.AlarmPageInfoDto;
import com.zw.platform.dto.alarm.AlarmPageReq;
import com.zw.platform.dto.paas.PaasCloudPageDTO;
import com.zw.platform.dto.paas.PaasCloudPageDataDTO;
import com.zw.platform.dto.paas.PaasCloudResultDTO;
import com.zw.platform.push.factory.AlarmFactory;
import com.zw.platform.repository.vas.AlarmSearchDao;
import com.zw.platform.service.alarm.AlarmSearchService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.alarm.PassCloudAlarmUrlUtil;
import com.zw.platform.util.common.AddressUtil;
import com.zw.platform.util.common.AlarmTypeUtil;
import com.zw.platform.util.common.Date8Utils;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MethodLog;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.UuidUtils;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.platform.util.excel.ExportExcel;
import com.zw.platform.util.excel.ExportExcelParam;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.platform.util.paas.PaasCloudUrlUtil;
import com.zw.platform.util.report.PaasCloudAlarmUrlEnum;
import com.zw.platform.util.report.PaasCloudHBaseAccessEnum;
import com.zw.platform.util.report.PaasCloudUrlEnum;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @version 1.0
 * @author: fanlu
 * @date 2016???12???6?????????11???04
 */
@Service
public class AlarmSearchServiceImpl implements AlarmSearchService {
    private static final Logger logger = LogManager.getLogger(AlarmSearchServiceImpl.class);
    @Autowired
    private AlarmSearchDao alarmDao;
    @Autowired
    private UserService userService;
    @Resource
    private AlarmFactory alarmFactory;
    @Resource
    private LogSearchService logSearchService;

    @Override
    public List<AlarmHandle> getAlarmHandle(List<String> vehicleIds, AlarmSearchQuery query) {
        List<Integer> poss = AlarmTypeUtil.typeList(query.getType());
        List<AlarmHandle> list = getAlarmHandle(vehicleIds, query, poss);
        Set<String> monitorIds = new HashSet<>();
        List<AlarmHandle> alarmHandles = list.stream().peek(info -> {
            // ????????????id
            String uuid = String.valueOf(UuidUtils.getUUIDFromBytes(info.getVehicleIdHbase()));
            info.setVehicleId(uuid);
            monitorIds.add(uuid);
        }).collect(Collectors.toList());
        // 1.?????????????????????
        Map<String, BindDTO> bindInfoMap = VehicleUtil.batchGetBindInfosByRedis(monitorIds);
        // 2.???????????????????????????
        List<GroupDTO> userGroupList = userService.getCurrentUserGroupList();
        Map<String, String> userGroupIdAndNameMap =
            userGroupList.stream().collect(Collectors.toMap(GroupDTO::getId, GroupDTO::getName));
        for (AlarmHandle alarmHandle : alarmHandles) {
            String vehicleId = alarmHandle.getVehicleId();
            Double speedLimit = alarmHandle.getSpeedLimit();
            if (speedLimit != null) {
                BigDecimal speedBigDecimal = new BigDecimal(speedLimit).setScale(2, BigDecimal.ROUND_HALF_UP);
                alarmHandle.setSpeedLimit(speedBigDecimal.doubleValue());
            }
            Double roadNetSpeedLimit = alarmHandle.getRoadNetSpeedLimit();
            if (roadNetSpeedLimit != null) {
                BigDecimal speedBigDecimal = new BigDecimal(roadNetSpeedLimit).setScale(2, BigDecimal.ROUND_HALF_UP);
                alarmHandle.setRoadNetSpeedLimit(speedBigDecimal.doubleValue());
            }
            BindDTO bindDTO = bindInfoMap.get(vehicleId);
            if (bindDTO == null) {
                alarmHandle.setPlateColor("");
                alarmHandle.setProfessionalsName("");
                alarmHandle.setAssignmentName("");
                alarmHandle.setName("");
                continue;
            }
            String monitorType = bindDTO.getMonitorType();
            if (Objects.equals(monitorType, MonitorTypeEnum.THING.getType()) || Objects
                .equals(monitorType, MonitorTypeEnum.PEOPLE.getType())) {
                alarmHandle.setPlateColor("-");
            } else {
                Integer plateColor = bindDTO.getPlateColor();
                alarmHandle.setPlateColor(plateColor != null ? plateColor.toString() : "");
            }
            alarmHandle.setProfessionalsName(bindDTO.getProfessionalNames());
            String groupIds = bindDTO.getGroupId();
            String groupNames =
                Arrays.stream(groupIds.split(",")).map(userGroupIdAndNameMap::get).filter(StringUtils::isNotBlank)
                    .collect(Collectors.joining(","));
            alarmHandle.setAssignmentName(groupNames);
            alarmHandle.setName(bindDTO.getOrgName());
        }
        return alarmHandles;
    }

    private List<AlarmHandle> getAlarmHandle(List<String> vehicleIds, AlarmSearchQuery query, List<Integer> poss) {
        if (CollectionUtils.isEmpty(vehicleIds) || CollectionUtils.isEmpty(poss)) {
            return new ArrayList<>();
        }
        Map<String, String> params = new HashMap<>(16);
        params.put("vehicleIds", JSON.toJSONString(vehicleIds));
        params.put("poss", JSON.toJSONString(poss));
        params.put("alarmSource", String.valueOf(query.getAlarmSource()));
        params.put("status", String.valueOf(query.getStatus()));
        params.put("type", String.valueOf(query.getType()));
        params.put("pushType", String.valueOf(query.getPushType()));
        params.put("alarmStartTime", String.valueOf(query.getAlarmStartTime()));
        params.put("alarmEndTime", String.valueOf(query.getAlarmEndTime()));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_ALARM_HANDLE, params);
        return PaasCloudUrlUtil.getResultListData(str, AlarmHandle.class);
    }

    @Override
    public List<AlarmHandle> getAlarmList(String monitorId) {
        Map<String, String> params = new HashMap<>(2);
        params.put("monitorId", monitorId);
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_ALARM_LIST, params);
        return PaasCloudUrlUtil.getResultListData(str, AlarmHandle.class);
    }

    @Override
    @MethodLog(description = "??????????????????????????????????????????", name = "??????????????????????????????????????????")
    public String getLatestAlarmHandle(String vehicleId, int type, long startTime) {
        return getLatestAlarmEndTime(vehicleId, type, startTime);
    }

    private String getLatestAlarmEndTime(String vehicleId, int type, long startTime) {
        Map<String, String> params = new HashMap<>(8);
        params.put("monitorId", vehicleId);
        params.put("alarmType", String.valueOf(type));
        params.put("startTime", String.valueOf(startTime));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_LATEST_ALARM_HANDLE, params);
        return PaasCloudUrlUtil.getResultData(str, String.class);
    }

    @Override
    public List<AlarmSetting> findSpeedParameter(String vehicleId) {
        return alarmDao.findSpeedParameter(vehicleId);
    }

    @Override
    public String getAlarmTime(String vehicleIds) {
        Map<String, String> queryParam = new HashMap<>(6);
        queryParam.put("monitorIds", vehicleIds);
        String queryResult =
            HttpClientUtil.send(PaasCloudAlarmUrlEnum.QUERY_GLOBAL_ALARM_EARLIEST_START_TIME, queryParam);
        JSONObject queryResultJsonObj = JSON.parseObject(queryResult);
        if (queryResultJsonObj == null || !Objects
            .equals(queryResultJsonObj.getInteger(PassCloudAlarmUrlUtil.RETURN_RESULT_CODE_KEY),
                PassCloudAlarmUrlUtil.SUCCESS_CODE)) {
            logger.error("??????PassCloud??????????????????????????????????????????", new Exception(
                "??????PassCloud?????????????????????????????????????????????" + (queryResultJsonObj != null ? queryResultJsonObj.getString("message") :
                    null)));
            //?????????  ???????????????????????????????????????0????????????minTime
            LocalDateTime localDateTime = LocalDateTime.now();
            return Date8Utils.getDateHourTime(localDateTime.minusHours(localDateTime.getHour()));
        }
        JSONObject data = queryResultJsonObj.getJSONObject("data");
        Long minTime = data.getLong("minTime");
        return DateUtil.getLongToDateStr(minTime, null);
    }

    @Override
    public List<AlarmType> getAlarmType() {
        return AlarmTypeCache.getOrLoad(() -> {
            List<AlarmType> alarms = alarmDao.getAlarmType("");
            //??????pos???event??????
            Map<String, String> posEventCommonNameMap = getPosEventCommonNamMap();
            // ?????????????????????????????????
            List<AlarmType> alarmTypeList =
                    AlarmTypeUtil.assemblyAlarmName(alarms, "alarmQuery", posEventCommonNameMap);
            if (CollectionUtils.isNotEmpty(alarmTypeList)) {
                // ???????????????????????????????????????????????????
                AlarmType notRule = null;
                Iterator<AlarmType> iterator = alarmTypeList.iterator();
                //??????????????????
                Integer deviationIndex = null;
                while (iterator.hasNext()) {
                    AlarmType alarmType = iterator.next();
                    if ("??????????????????????????????".equals(alarmType.getName())) {
                        notRule = alarmType;
                        iterator.remove();
                    }
                }
                for (int i = 0; i < alarmTypeList.size(); i++) {
                    AlarmType alarmType = alarmTypeList.get(i);
                    if ("??????????????????".equals(alarmType.getName()) && "platAlarm".equals(alarmType.getType())) {
                        deviationIndex = i + 1;
                    }
                }
                if (notRule != null && deviationIndex != null) {
                    //?????????????????????????????????
                    alarmTypeList.add(deviationIndex, notRule);
                }
            }
            return alarmTypeList;
        });
    }

    private Map<String, String> getPosEventCommonNamMap() {
        List<Map<String, String>> posEventCommonNameMapList = alarmDao.findPosEventCommonNameMap();
        Map<String, String> posEventCommonNameMap = new HashMap<>();
        for (Map<String, String> map : posEventCommonNameMapList) {
            if (map.get("name").contains("?????????") || map.get("name").contains("?????????")) {
                posEventCommonNameMap.put(map.get("pos"), "????????????");
            } else {
                posEventCommonNameMap.put(map.get("pos"), map.get("name"));
            }

        }
        return posEventCommonNameMap;
    }

    @Override
    public List<AlarmType> getDispatchAlarmType() {
        return alarmDao.getDispatchAlarmType();
    }

    @Override
    public void exportIoAlarm(HttpServletResponse response) throws Exception {
        String userUuid = userService.getCurrentUserUuid();
        RedisKey userIoAlarmDealListRedisKey = HistoryRedisKeyEnum.USER_IO_ALARM_DEAL_LIST.of(userUuid);
        List<IoAlarmInfo> ioAlarmInfoList = RedisHelper.getList(userIoAlarmDealListRedisKey, IoAlarmInfo.class);
        ExportExcelUtil.setResponseHead(response, "????????????(I/O????????????)");
        ExportExcel export = new ExportExcel(null, IoAlarmInfo.class, 1);
        if (CollectionUtils.isNotEmpty(ioAlarmInfoList)) {
            Set<String> locationSet = new HashSet<>(ioAlarmInfoList.size() * 2);
            for (IoAlarmInfo ioAlarmInfo : ioAlarmInfoList) {
                String alarmStartLocation = ioAlarmInfo.getAlarmStartLocation();
                if (StringUtils.isNotBlank(alarmStartLocation)) {
                    locationSet.add(alarmStartLocation);
                }
                String alarmEndLocation = ioAlarmInfo.getAlarmEndLocation();
                if (StringUtils.isNotBlank(alarmEndLocation)) {
                    locationSet.add(alarmEndLocation);
                }
            }
            Map<String, String> addressMap = AddressUtil.batchInverseAddress(locationSet);
            for (IoAlarmInfo alarm : ioAlarmInfoList) {
                //????????????
                alarm.setPlateColorString(VehicleUtil.getPlateColorStr(alarm.getPlateColor()));
                //????????????
                Long handleTime = alarm.getHandleTime();
                if (handleTime != null) {
                    alarm.setHandleTimeStr(DateUtil.getLongToDateStr(handleTime * 1000, null));
                }
                // ????????????
                alarm.setAlarmStatus(Objects.equals(alarm.getStatus(), 0) ? "?????????" : "?????????");
                //??????????????????????????????
                String alarmStartLocation = alarm.getAlarmStartLocation();
                if (StringUtils.isNotBlank(alarmStartLocation)) {
                    String[] address = alarmStartLocation.split(",");
                    String longitude = address[0];
                    String latitude = address[1];
                    alarm.setAlarmStartSpecificLocation(addressMap.get(alarmStartLocation));
                    Pair<String, String> lngAndLat = AddressUtil.formatLongitudeOrLatitude(longitude, latitude);
                    alarm.setAlarmStartLongitude(lngAndLat.getFirst());
                    alarm.setAlarmStartLatitude(lngAndLat.getSecond());
                }
                //??????????????????
                Long alarmEndTime = alarm.getAlarmEndTime();
                if (alarmEndTime != null) {
                    alarm.setEndTime(DateUtil.getLongToDateStr(alarmEndTime, null));
                }
                //??????????????????????????????
                String alarmEndLocation = alarm.getAlarmEndLocation();
                if (StringUtils.isNotBlank(alarmEndLocation)) {
                    String[] address = alarmEndLocation.split(",");
                    String longitude = address[0];
                    String latitude = address[1];
                    alarm.setAlarmEndSpecificLocation(addressMap.get(alarmEndLocation));
                    Pair<String, String> lngAndLat = AddressUtil.formatLongitudeOrLatitude(longitude, latitude);
                    alarm.setAlarmEndLongitude(lngAndLat.getFirst());
                    alarm.setAlarmEndLatitude(lngAndLat.getSecond());
                }
                //??????????????????
                Long alarmStartTime = alarm.getAlarmStartTime();
                if (alarmStartTime != null) {
                    alarm.setStartTime(DateUtil.getLongToDateStr(alarmStartTime, null));
                }
            }
        }
        export.setDataList(ioAlarmInfoList);
        OutputStream out;
        out = response.getOutputStream();
        export.write(out);
        out.flush();
        out.close();
    }

    @Override
    public JsonResultBean getIoAlarmHandle(String vehicleIds, String alarmTypeNames, Integer status, String startTime,
        String endTime, Integer pushType) {
        String userUuid = userService.getCurrentUserUuid();
        RedisKey userIoAlarmDealListRedisKey = HistoryRedisKeyEnum.USER_IO_ALARM_DEAL_LIST.of(userUuid);
        RedisHelper.delete(userIoAlarmDealListRedisKey);
        if (StringUtils.isBlank(alarmTypeNames) || StringUtils.isBlank(startTime) || StringUtils.isBlank(endTime)
            || StringUtils.isBlank(vehicleIds)) {
            return new JsonResultBean(JsonResultBean.FAULT, "???????????????");
        }
        Map<String, String> queryParam = new HashMap<>(16);
        queryParam.put("status", Objects.equals(status, -1) || status == null ? null : String.valueOf(status));
        queryParam.put("monitorIds", vehicleIds);
        queryParam.put("startTime", startTime.replaceAll("-", "").replaceAll(" ", "").replaceAll(":", ""));
        queryParam.put("endTime", endTime.replaceAll("-", "").replaceAll(" ", "").replaceAll(":", ""));
        queryParam.put("alarmTypeNames", alarmTypeNames);
        queryParam.put("limitNum", "5000");
        if (pushType != null && pushType != -1) {
            queryParam.put("pushType", String.valueOf(pushType));
        }
        String queryResult = HttpClientUtil.send(PaasCloudAlarmUrlEnum.QUERY_IO_ALARM_INFO, queryParam);
        JSONObject queryResultJsonObj = JSON.parseObject(queryResult);
        if (queryResultJsonObj == null || !Objects
            .equals(queryResultJsonObj.getInteger(PassCloudAlarmUrlUtil.RETURN_RESULT_CODE_KEY),
                PassCloudAlarmUrlUtil.SUCCESS_CODE)) {
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????");
        }
        List<IoAlarmInfo> alarmList = JSONObject.parseArray(queryResultJsonObj.getString("data"), IoAlarmInfo.class);
        if (CollectionUtils.isEmpty(alarmList)) {
            return new JsonResultBean();
        }
        List<String> existMonitorIdList =
            alarmList.stream().map(IoAlarmInfo::getMonitorId).collect(Collectors.toList());
        Map<String, BindDTO> bindInfoMap = VehicleUtil.batchGetBindInfosByRedis(existMonitorIdList);
        List<GroupDTO> userGroupList = userService.getCurrentUserGroupList();
        Map<String, String> userGroupIdAndNameMap =
            userGroupList.stream().collect(Collectors.toMap(GroupDTO::getId, GroupDTO::getName));
        for (IoAlarmInfo ioAlarmInfo : alarmList) {
            BindDTO bindDTO = bindInfoMap.get(ioAlarmInfo.getMonitorId());
            if (bindDTO == null) {
                continue;
            }
            String monitorType = bindDTO.getMonitorType();
            Integer plateColor = bindDTO.getPlateColor();
            if (Objects.equals(monitorType, MonitorTypeEnum.VEHICLE.getType())) {
                ioAlarmInfo.setPlateColor(plateColor != null ? String.valueOf(plateColor) : "");
            }
            ioAlarmInfo.setName(bindDTO.getOrgName());
            ioAlarmInfo.setMonitorType(Integer.valueOf(monitorType));
            ioAlarmInfo.setEmployeeName(bindDTO.getProfessionalNames());
            String groupIds = bindDTO.getGroupId();
            String groupNames =
                Arrays.stream(groupIds.split(",")).map(userGroupIdAndNameMap::get).filter(Objects::nonNull)
                    .collect(Collectors.joining(","));
            ioAlarmInfo.setAssignmentName(groupNames);
        }
        // ????????????????????????redis??????
        RedisHelper.addToList(userIoAlarmDealListRedisKey, alarmList);
        RedisHelper.expireKey(userIoAlarmDealListRedisKey, RedisHelper.SIX_HOUR_REDIS_EXPIRE);
        return new JsonResultBean(true);
    }

    @Override
    public PageGridBean getIoAlarmList(AlarmSearchQuery query) {
        String userUuid = userService.getCurrentUserUuid();
        RedisKey redisKey = HistoryRedisKeyEnum.USER_IO_ALARM_DEAL_LIST.of(userUuid);
        Page<IoAlarmInfo> pageList = RedisHelper.getPageList(redisKey, query, IoAlarmInfo.class);
        if (CollectionUtils.isEmpty(pageList)) {
            return new PageGridBean(new ArrayList<>());
        }
        Set<String> locationSet = new HashSet<>(pageList.size() * 2);
        for (IoAlarmInfo ioAlarmInfo : pageList) {
            String alarmStartLocation = ioAlarmInfo.getAlarmStartLocation();
            if (StringUtils.isNotBlank(alarmStartLocation)) {
                locationSet.add(alarmStartLocation);
            }
            String alarmEndLocation = ioAlarmInfo.getAlarmEndLocation();
            if (StringUtils.isNotBlank(alarmEndLocation)) {
                locationSet.add(alarmEndLocation);
            }
        }
        Map<String, String> addressMap = AddressUtil.batchInverseAddress(locationSet);
        for (IoAlarmInfo ioAlarmInfo : pageList) {
            //??????????????????
            ioAlarmInfo.setStartTime(DateUtil.getLongToDateStr(ioAlarmInfo.getAlarmStartTime(), null));
            //????????????
            Long handleTime = ioAlarmInfo.getHandleTime();
            if (handleTime != null) {
                ioAlarmInfo.setHandleTimeStr(DateUtil.getLongToDateStr(handleTime * 1000, null));
            }
            //??????????????????
            ioAlarmInfo.setEndTime(DateUtil.getLongToDateStr(ioAlarmInfo.getAlarmEndTime(), null));
            //??????????????????????????????
            String alarmStartLocation = ioAlarmInfo.getAlarmStartLocation();
            if (StringUtils.isNotBlank(alarmStartLocation)) {
                String[] address = alarmStartLocation.split(",");
                String longitude = address[0];
                String latitude = address[1];
                ioAlarmInfo.setAlarmStartSpecificLocation(addressMap.get(alarmStartLocation));
                ioAlarmInfo.setAlarmStartLongitude(longitude);
                ioAlarmInfo.setAlarmStartLatitude(latitude);
            }
            //??????????????????????????????
            String alarmEndLocation = ioAlarmInfo.getAlarmEndLocation();
            if (StringUtils.isNotBlank(alarmEndLocation)) {
                String[] address = alarmEndLocation.split(",");
                String longitude = address[0];
                String latitude = address[1];
                ioAlarmInfo.setAlarmEndSpecificLocation(addressMap.get(alarmEndLocation));
                ioAlarmInfo.setAlarmEndLongitude(longitude);
                ioAlarmInfo.setAlarmEndLatitude(latitude);
            }
        }
        return new PageGridBean(query, pageList, true);
    }

    @Override
    public JsonResultBean updateIoAlarm(HandleAlarms handleAlarms, String ip) throws Exception {
        if (handleAlarms != null) {
            String handleType = handleAlarms.getHandleType();
            List<String> alarmTypes = Arrays.asList(handleAlarms.getAlarm().split(","));
            String alarmStartTimeStr = handleAlarms.getStartTime();
            Date stringToDate = DateUtil.getStringToDate(alarmStartTimeStr, "yyyy-MM-dd HH:mm:ss");
            String alarmDay = DateUtil.getDateToString(stringToDate, "yyyyMMdd");
            String startTime = alarmDay + "000000";
            String endTime = alarmDay + "235959";

            // ??????PassCloud??????????????????
            handleAlarmBatch(handleAlarms, alarmTypes, true, startTime, endTime);
            alarmFactory.dealAlarm(handleAlarms);
            StringBuilder message = new StringBuilder();
            message.append("???????????? : ").append(handleAlarms.getPlateNumber());
            if ("??????????????????".equals(handleType)) {
                message.append(" ???????????? : ??????????????????");
            }
            if ("????????????".equals(handleType)) {
                message.append(" ???????????????????????????");
            }
            if ("????????????".equals(handleType)) {
                message.append(" ???????????????????????????");
            }
            logSearchService.addLog(ip, message.toString(), "3", "", handleAlarms.getPlateNumber(), "");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public JsonResultBean find809Alarms(String alarmType, String vehicleIds, String alarmStartTime,
        String alarmEndTime) {
        String userUuid = userService.getCurrentUserUuid();
        RedisKey userForward809AlarmListRedisKey = HistoryRedisKeyEnum.USER_FORWARD_809_ALARM_LIST.of(userUuid);
        RedisHelper.delete(userForward809AlarmListRedisKey);
        if (StringUtils.isBlank(alarmType) || StringUtils.isBlank(alarmStartTime) || StringUtils.isBlank(alarmEndTime)
            || StringUtils.isBlank(vehicleIds)) {
            return new JsonResultBean(JsonResultBean.FAULT, "???????????????");
        }
        Map<String, String> queryParam = new HashMap<>(16);
        queryParam.put("startTime", alarmStartTime.replaceAll("-", "").replaceAll(" ", "").replaceAll(":", ""));
        queryParam.put("endTime", alarmEndTime.replaceAll("-", "").replaceAll(" ", "").replaceAll(":", ""));
        queryParam.put("monitorIds", vehicleIds);
        List<Integer> alarmTypeList = AlarmTypeUtil.typeList(alarmType);
        queryParam.put("alarmTypes", StringUtils.join(alarmTypeList, ","));
        String queryResult = HttpClientUtil.send(PaasCloudAlarmUrlEnum.QUERY809_FORWARD_ALARM_INFO, queryParam);
        JSONObject queryResultJsonObj = JSON.parseObject(queryResult);
        if (queryResultJsonObj == null || !Objects
            .equals(queryResultJsonObj.getInteger(PassCloudAlarmUrlUtil.RETURN_RESULT_CODE_KEY),
                PassCloudAlarmUrlUtil.SUCCESS_CODE)) {
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????");
        }
        List<ForwardAlarmInfo> forwardAlarmInfoList =
            JSONObject.parseArray(queryResultJsonObj.getString("data"), ForwardAlarmInfo.class);
        if (CollectionUtils.isEmpty(forwardAlarmInfoList)) {
            return new JsonResultBean();
        }
        List<GroupDTO> userGroupList = userService.getCurrentUserGroupList();
        Map<String, String> userGroupIdAndNameMap =
            userGroupList.stream().collect(Collectors.toMap(GroupDTO::getId, GroupDTO::getName));
        List<String> existMonitorIdList =
            forwardAlarmInfoList.stream().map(ForwardAlarmInfo::getMonitorId).collect(Collectors.toList());
        Map<String, BindDTO> bindInfoMap = VehicleUtil.batchGetBindInfosByRedis(existMonitorIdList);
        for (ForwardAlarmInfo forwardAlarmInfo : forwardAlarmInfoList) {
            BindDTO bindDTO = bindInfoMap.get(forwardAlarmInfo.getMonitorId());
            if (bindDTO == null) {
                continue;
            }
            String monitorType = bindDTO.getMonitorType();
            if (Objects.equals(monitorType, MonitorTypeEnum.VEHICLE.getType())) {
                Integer plateColor = bindDTO.getPlateColor();
                forwardAlarmInfo.setPlateColor(plateColor != null ? String.valueOf(plateColor) : "");
            }
            forwardAlarmInfo.setProfessionalsName(bindDTO.getProfessionalNames());
            forwardAlarmInfo.setGroupName(bindDTO.getOrgName());
            String groupIds = bindDTO.getGroupId();
            String groupNames =
                Arrays.stream(groupIds.split(",")).map(userGroupIdAndNameMap::get).filter(Objects::nonNull)
                    .collect(Collectors.joining(","));
            forwardAlarmInfo.setAssignmentName(groupNames);
            forwardAlarmInfo.setMonitorType(monitorType);
        }
        RedisHelper.addToList(userForward809AlarmListRedisKey, forwardAlarmInfoList);
        RedisHelper.expireKey(userForward809AlarmListRedisKey, RedisHelper.SIX_HOUR_REDIS_EXPIRE);
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }


    /**
     * ??????adas?????? ??? 809??????
     */
    private void buildForwardAlarmInfo(List<ForwardAlarmInfo> alarmForwardInfoList) {
        Map<Integer, String> adasEventMap = getRiskEventMap();
        Set<String> locationSet =
            alarmForwardInfoList.stream().filter(obj -> StringUtils.isNotBlank(obj.getAlarmLocation()))
                .filter(obj -> StringUtils.isBlank(obj.getAlarmAddress())).map(ForwardAlarmInfo::getAlarmLocation)
                .collect(Collectors.toSet());
        Map<String, String> addressPairMap = AddressUtil.batchInverseAddress(locationSet);
        for (ForwardAlarmInfo forwardAlarmInfo : alarmForwardInfoList) {
            String plateColor = forwardAlarmInfo.getPlateColor();
            if (StringUtils.isNotBlank(plateColor)) {
                forwardAlarmInfo.setPlateColor(PlateColor.getNameOrBlankByCode(plateColor));
            }
            Long handleTime = forwardAlarmInfo.getHandleTime();
            if (handleTime != null) {
                if ((handleTime + "").length() == 10) {
                    handleTime = handleTime * 1000;
                }
                forwardAlarmInfo.setHandleTimeStr(DateUtil.getLongToDateStr(handleTime, null));
            }
            String alarmLocation = forwardAlarmInfo.getAlarmLocation();
            if (StringUtils.isNotEmpty(alarmLocation)) {
                if (StringUtils.isEmpty(forwardAlarmInfo.getAlarmAddress())) {
                    forwardAlarmInfo.setAlarmLocation(addressPairMap.get(alarmLocation));
                }
                String[] address = alarmLocation.split(",");
                forwardAlarmInfo.setAlarmLongitude(address[0]);
                forwardAlarmInfo.setAlarmLatitude(address[1]);
            }
            forwardAlarmInfo.setStartTimeStr(DateUtil.getLongToDateStr(forwardAlarmInfo.getAlarmTime(), null));
            forwardAlarmInfo
                .setAlarmStartTimeStr(DateUtil.getLongToDateStr(forwardAlarmInfo.getAlarmStartTime(), null));
            String riskEventId = forwardAlarmInfo.getRiskEventId();
            if (StringUtils.isNotBlank(riskEventId)) {
                assemblyAdasAlarm(forwardAlarmInfo, adasEventMap);
                continue;
            }
            assemblyForwardAlarm(forwardAlarmInfo);
            //??????????????? 76?????????164???????????????????????????????????????????????????
            Integer alarmType = forwardAlarmInfo.getAlarmType();
            boolean alarmTypeFlag = alarmType.equals(76) || alarmType.equals(164);
            if (!alarmTypeFlag) {
                forwardAlarmInfo.setOverSpeedTime(null);
            }
        }
    }

    /**
     * ??????adas??????
     */
    private void assemblyAdasAlarm(ForwardAlarmInfo forwardAlarmInfo, Map<Integer, String> adasEventMap) {
        //????????????
        String functionId = forwardAlarmInfo.getFunctionId();
        if (StringUtils.isNotBlank(functionId) && adasEventMap != null) {
            forwardAlarmInfo.setDescription(adasEventMap.get(Integer.parseInt(functionId)));
        }
        //????????????
        forwardAlarmInfo.setSeverityName(Objects.equals(forwardAlarmInfo.getSeverity(), 1.0) ? "????????????" : "????????????");
        //????????????
        forwardAlarmInfo.setAlarmSourceStr("????????????");
        forwardAlarmInfo.setStatus(!Objects.equals(forwardAlarmInfo.getStatus(), 1) ? 0 : 1);
        //????????????
        forwardAlarmInfo.setAlarmStatus(Objects.equals(forwardAlarmInfo.getStatus(), 1) ? "?????????" : "?????????");

    }

    /**
     * ??????????????????
     */
    private void assemblyForwardAlarm(ForwardAlarmInfo forwardAlarmInfo) {
        forwardAlarmInfo.setAlarmSourceStr(Objects.equals(forwardAlarmInfo.getAlarmSource(), 0) ? "????????????" : "????????????");
        forwardAlarmInfo.setAlarmStatus(Objects.equals(forwardAlarmInfo.getStatus(), 0) ? "?????????" : "?????????");
        forwardAlarmInfo.setRoadTypeStr(VehicleUtil.getRoadTypeStr(forwardAlarmInfo.getRoadType()));
    }

    private Map<Integer, String> getRiskEventMap() {
        Map<Integer, String> adasEventMap = new HashMap<>();
        List<Map<String, Object>> mapList = alarmDao.getAdasEventMap();
        for (Map<String, Object> map : mapList) {
            adasEventMap.put(Integer.parseInt(map.get("functionId").toString()), map.get("riskEvent").toString());
        }
        return adasEventMap;
    }

    @Override
    public void export809Alarms(HttpServletResponse response) throws Exception {
        String userUuid = userService.getCurrentUserUuid();
        RedisKey userForward809AlarmListRedisKey = HistoryRedisKeyEnum.USER_FORWARD_809_ALARM_LIST.of(userUuid);
        List<ForwardAlarmInfo> forwardAlarmInfoList =
            RedisHelper.getList(userForward809AlarmListRedisKey, ForwardAlarmInfo.class);
        buildForwardAlarmInfo(forwardAlarmInfoList);
        ExportExcelUtil.setResponseHead(response, "809??????");
        ExportExcelUtil.export(new ExportExcelParam("", 1, forwardAlarmInfoList, ForwardAlarmInfo.class, null,
            response.getOutputStream()));
    }

    /**
     * ??????????????????
     */
    @Override
    public JsonResultBean queryDispatchAlarm(String alarmType, Integer status, String alarmStartTime,
        String alarmEndTime, String monitorIds) throws Exception {
        String userUuid = userService.getCurrentUserUuid();
        RedisKey userDispatchAlarmDealListRedisKey = HistoryRedisKeyEnum.USER_DISPATCH_ALARM_DEAL_LIST.of(userUuid);
        RedisHelper.delete(userDispatchAlarmDealListRedisKey);
        if (StringUtils.isBlank(monitorIds) || StringUtils.isBlank(alarmStartTime) || StringUtils.isBlank(alarmEndTime)
            || StringUtils.isBlank(alarmType)) {
            return new JsonResultBean(JsonResultBean.FAULT, "???????????????");
        }
        List<AlarmInfo> alarmList =
            getAlarmInfo(monitorIds, alarmType, alarmStartTime, alarmEndTime, null, status, null, 5000, null,
                AlarmInfo.class, null);
        if (CollectionUtils.isEmpty(alarmList)) {
            return new JsonResultBean();
        }
        List<String> existMonitorIdList =
            alarmList.stream().map(AlarmInfo::getMonitorId).distinct().collect(Collectors.toList());
        Map<String, BindDTO> bindInfoMap = VehicleUtil.batchGetBindInfosByRedis(existMonitorIdList);
        List<GroupDTO> userGroupList = userService.getCurrentUserGroupList();
        Map<String, String> userGroupIdAndNameMap =
            userGroupList.stream().collect(Collectors.toMap(GroupDTO::getId, GroupDTO::getName));
        for (AlarmInfo alarmInfo : alarmList) {
            BindDTO bindDTO = bindInfoMap.get(alarmInfo.getMonitorId());
            if (bindDTO == null) {
                alarmInfo.setPlateColor("");
                alarmInfo.setEmployeeName("");
                alarmInfo.setAssignmentName("");
                alarmInfo.setName("");
                continue;
            }
            alarmInfo.setName(bindDTO.getOrgName());
            alarmInfo.setEmployeeName(bindDTO.getProfessionalNames());
            String monitorType = bindDTO.getMonitorType();
            alarmInfo.setMonitorType(Integer.valueOf(monitorType));
            Integer plateColor = bindDTO.getPlateColor();
            if (Objects.equals(monitorType, MonitorTypeEnum.VEHICLE.getType())) {
                alarmInfo.setPlateColor(plateColor != null ? String.valueOf(plateColor) : "");
            }
            String groupIds = bindDTO.getGroupId();
            String groupNames =
                Arrays.stream(groupIds.split(",")).map(userGroupIdAndNameMap::get).filter(Objects::nonNull)
                    .collect(Collectors.joining(","));
            alarmInfo.setAssignmentName(groupNames);
        }
        RedisHelper.addToList(userDispatchAlarmDealListRedisKey, alarmList);
        RedisHelper.expireKey(userDispatchAlarmDealListRedisKey, RedisHelper.SIX_HOUR_REDIS_EXPIRE);
        return new JsonResultBean();
    }

    /**
     * ????????????????????????(??????????????????sos??????)
     * @param alarmSearchQuery ????????????
     * @return PageGridBean
     */
    @Override
    public PageGridBean getDispatchAlarmList(AlarmSearchQuery alarmSearchQuery) {
        String userUuid = userService.getCurrentUserUuid();
        RedisKey redisKey = HistoryRedisKeyEnum.USER_DISPATCH_ALARM_DEAL_LIST.of(userUuid);
        List<DispatchAlarmInfo> dispatchAlarmInfoList = new ArrayList<>();
        Page<AlarmInfo> pageList = RedisHelper.getPageList(redisKey, alarmSearchQuery, AlarmInfo.class);
        if (CollectionUtils.isEmpty(pageList)) {
            return new PageGridBean(new ArrayList<>());
        }
        final Set<String> lngLats = new HashSet<>();
        for (AlarmInfo alarmInfo : pageList) {
            DispatchAlarmInfo dispatchAlarmInfo =
                JSON.parseObject(JSON.toJSONString(alarmInfo), DispatchAlarmInfo.class);
            Long alarmStartTime = dispatchAlarmInfo.getAlarmStartTime();
            dispatchAlarmInfo.setStartTime(DateUtil.getLongToDateStr(alarmStartTime, null));
            Long alarmEndTime = dispatchAlarmInfo.getAlarmEndTime();
            dispatchAlarmInfo.setEndTime(DateUtil.getLongToDateStr(alarmEndTime, null));
            if (alarmStartTime != null && alarmEndTime != null) {
                long alarmDuration = alarmEndTime - alarmStartTime;
                if (alarmDuration >= 0) {
                    dispatchAlarmInfo.setAlarmDuration(DateUtil.timeConversion(alarmDuration / 1000L));
                }
            }
            Long handleTime = dispatchAlarmInfo.getHandleTime();
            dispatchAlarmInfo
                .setHandleTimeStr(handleTime == null ? null : DateUtil.getLongToDateStr(handleTime * 1000, null));
            if (null != dispatchAlarmInfo.getAlarmStartLocation()) {
                lngLats.add(dispatchAlarmInfo.getAlarmStartLocation());
            }
            if (null != dispatchAlarmInfo.getAlarmEndLocation()) {
                lngLats.add(dispatchAlarmInfo.getAlarmEndLocation());
            }
            Integer alarmType = dispatchAlarmInfo.getAlarmType();
            String alarmName = AlarmTypeUtil.getAlarmType(String.valueOf(alarmType));
            // ????????????????????????????????? ???????????????SOS??????
            if (alarmType == 0) {
                alarmName = "SOS??????";
            }
            dispatchAlarmInfo.setDescription(alarmName);
            dispatchAlarmInfoList.add(dispatchAlarmInfo);
        }
        final Map<String, String> addressMap = AddressUtil.batchInverseAddress(lngLats);
        for (DispatchAlarmInfo info : dispatchAlarmInfoList) {
            if (null != info.getAlarmStartLocation()) {
                info.setAlarmStartSpecificLocation(addressMap.get(info.getAlarmStartLocation()));
            }
            if (null != info.getAlarmEndLocation()) {
                info.setAlarmEndSpecificLocation(addressMap.get(info.getAlarmEndLocation()));
            }
        }
        return new PageGridBean(alarmSearchQuery, pageList, true);
    }

    /**
     * ??????????????????
     */
    @Override
    public void exportDispatchAlarm(HttpServletResponse response) throws Exception {
        String userUuid = userService.getCurrentUserUuid();
        RedisKey userDispatchAlarmDealListRedisKey = HistoryRedisKeyEnum.USER_DISPATCH_ALARM_DEAL_LIST.of(userUuid);
        List<AlarmInfo> alarmInfoList = RedisHelper.getList(userDispatchAlarmDealListRedisKey, AlarmInfo.class);
        List<DispatchAlarmInfo> dataList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(alarmInfoList)) {
            ExportExcelUtil.setResponseHead(response, "????????????");
            final Set<String> lngLats = new HashSet<>();
            for (AlarmInfo alarmInfo : alarmInfoList) {
                DispatchAlarmInfo dispatchAlarmInfo = new DispatchAlarmInfo();
                dispatchAlarmInfo.setMonitorName(alarmInfo.getMonitorName());
                dispatchAlarmInfo.setName(alarmInfo.getName());
                dispatchAlarmInfo.setAssignmentName(alarmInfo.getAssignmentName());
                dispatchAlarmInfo.setFenceType(alarmInfo.getFenceType());
                dispatchAlarmInfo.setFenceName(alarmInfo.getFenceName());
                dispatchAlarmInfo.setPersonName(alarmInfo.getPersonName());
                dispatchAlarmInfo.setRemark(alarmInfo.getRemark());
                Long handleTime = alarmInfo.getHandleTime();
                if (handleTime != null) {
                    dispatchAlarmInfo.setHandleTimeStr(DateUtil.getLongToDateStr(handleTime * 1000L, null));
                }
                dispatchAlarmInfo.setAlarmStatus(alarmInfo.getStatus() == 0 ? "?????????" : "?????????");
                if (StringUtils.isNotBlank(alarmInfo.getAlarmStartLocation())) {
                    lngLats.add(alarmInfo.getAlarmStartLocation());
                } else {
                    dispatchAlarmInfo.setAlarmStartSpecificLocation("-");
                }
                if (StringUtils.isNotBlank(alarmInfo.getAlarmEndLocation())) {
                    lngLats.add(alarmInfo.getAlarmEndLocation());
                } else {
                    dispatchAlarmInfo.setAlarmEndSpecificLocation("-");
                }
                Long alarmStartTime = alarmInfo.getAlarmStartTime();
                Long alarmEndTime = alarmInfo.getAlarmEndTime();
                if (alarmStartTime != null && alarmEndTime != null) {
                    long alarmDuration = alarmEndTime - alarmStartTime;
                    if (alarmDuration >= 0) {
                        dispatchAlarmInfo.setAlarmDuration(DateUtil.timeConversion(alarmDuration / 1000L));
                    }
                }
                dispatchAlarmInfo.setStartTime(DateUtil.getLongToDateStr(alarmStartTime, null));
                dispatchAlarmInfo.setEndTime(DateUtil.getLongToDateStr(alarmEndTime, null));
                int alarmType = alarmInfo.getAlarmType();
                if (alarmType == 0) {
                    dispatchAlarmInfo.setDescription("SOS??????");
                } else {
                    dispatchAlarmInfo.setDescription(AlarmTypeUtil.getAlarmType(String.valueOf(alarmType)));
                }
                dataList.add(dispatchAlarmInfo);
            }
            final Map<String, String> addressMap = AddressUtil.batchInverseAddress(lngLats);
            for (DispatchAlarmInfo info : dataList) {
                if (StringUtils.isNotBlank(info.getAlarmStartLocation())) {
                    info.setAlarmStartSpecificLocation(addressMap.get(info.getAlarmStartLocation()));
                }
                if (StringUtils.isNotBlank(info.getAlarmEndLocation())) {
                    info.setAlarmEndSpecificLocation(addressMap.get(info.getAlarmEndLocation()));
                }
            }
        }
        ExportExcel export = new ExportExcel(null, DispatchAlarmInfo.class, 1);
        export.setDataList(dataList);
        // ???????????????
        OutputStream out = response.getOutputStream();
        // ????????????????????????????????????
        export.write(out);
        out.flush();
        out.close();
    }

    /**
     * ??????????????????
     */
    @Override
    public JsonResultBean updateDispatchAlarm(HandleAlarms handleAlarms, String ipAddress) throws Exception {
        String alarm = handleAlarms.getAlarm();
        String alarmStartTimeStr = handleAlarms.getStartTime();
        long alarmStartTimeLong =
            StringUtils.isNotBlank(alarmStartTimeStr) ? DateUtil.getStringToLong(alarmStartTimeStr, null) : 0L;
        String[] alarmTypeArr = alarm.split(",");
        if (alarmTypeArr.length > 1) {
            return new JsonResultBean(JsonResultBean.FAULT, "???????????????????????????");
        }
        handleAlarmSingle(handleAlarms, alarmStartTimeLong, alarmTypeArr[0], true);
        alarmFactory.dealAlarm(handleAlarms);
        String monitorName = handleAlarms.getPlateNumber();
        // ????????????ip
        String message = "???????????? : " + monitorName + " ??????????????? " + handleAlarms.getRemark();
        logSearchService.addLog(ipAddress, message, "3", "MONITORING", monitorName, "");
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    @Override
    public PageGridBean alarmPageList(AlarmPageReq alarmPageReq) throws Exception {
        String vehicleIds = alarmPageReq.getVehicleIds();
        //????????????????????????????????????????????????????????????
        if (StringUtils.isBlank(vehicleIds)) {
            return new PageGridBean();
        }
        String alarmTypes = alarmPageReq.getAlarmTypes();
        String startTime = alarmPageReq.getAlarmStartTime();
        String endTime = alarmPageReq.getAlarmEndTime();
        if (StringUtils.isBlank(alarmTypes) || StringUtils.isBlank(startTime) || StringUtils.isBlank(endTime)) {
            return new PageGridBean(PageGridBean.FAULT, "????????????!");
        }
        // ???????????????????????????
        alarmPageReq.setAlarmTypes(StringUtils.join(AlarmTypeUtil.typeList(alarmTypes), ","));
        String queryResult =
            HttpClientUtil.send(PaasCloudAlarmUrlEnum.PAGE_QUERY_ALARM_INFO, assemblePageQueryAlarmParam(alarmPageReq));
        PaasCloudResultDTO<PaasCloudPageDataDTO<AlarmPageInfoDto>> resultData =
            PaasCloudUrlUtil.pageResult(queryResult, AlarmPageInfoDto.class);
        PaasCloudPageDataDTO<AlarmPageInfoDto> paasCloudPageData = resultData.getData();
        List<AlarmPageInfoDto> alarmPageInfoList = paasCloudPageData.getItems();
        if (CollectionUtils.isEmpty(alarmPageInfoList)) {
            return new PageGridBean();
        }
        PaasCloudPageDTO pageInfo = paasCloudPageData.getPageInfo();
        Set<String> vehicleIdSet = Arrays.stream(vehicleIds.split(",")).collect(Collectors.toSet());
        Map<String, BindDTO> bindInfoMap = VehicleUtil.batchGetBindInfosByRedis(vehicleIdSet);
        List<GroupDTO> userGroupList = userService.getCurrentUserGroupList();
        Map<String, String> userGroupIdAndNameMap =
            userGroupList.stream().collect(Collectors.toMap(GroupDTO::getId, GroupDTO::getName));
        final List<AlarmPageInfoDto> needStartAddressDTOList = new ArrayList<>();
        final List<AlarmPageInfoDto> needEndAddressDTOList = new ArrayList<>();
        for (AlarmPageInfoDto alarmInfo : alarmPageInfoList) {
            alarmInfo.setContinuousTime(alarmInfo.getSpeedTime());
            String alarmEndTime = alarmInfo.getAlarmEndTime();
            if (StringUtils.isNotBlank(alarmEndTime)) {
                alarmInfo.setEndTime(DateUtil.getStringToString(alarmEndTime, DateUtil.DATE_FORMAT_SSS, null));
            }
            String handleTime = alarmInfo.getHandleTime();
            if (StringUtils.isNotBlank(handleTime)) {
                alarmInfo.setHandleTimeStr(DateUtil.getStringToString(handleTime, DateUtil.DATE_FORMAT, null));
            }
            String alarmStartLocation = alarmInfo.getAlarmStartLocation();
            if (StringUtils.isNotBlank(alarmStartLocation)) {
                String alarmStartAddress = alarmInfo.getAlarmStartAddress();
                if (StringUtils.isBlank(alarmStartAddress)) {
                    needStartAddressDTOList.add(alarmInfo);
                }
                String[] alarmStartLocationArr = alarmStartLocation.split(",");
                alarmInfo.setAlarmStartLongitude(alarmStartLocationArr[0]);
                alarmInfo.setAlarmStartLatitude(alarmStartLocationArr[1]);
            }
            String alarmEndLocation = alarmInfo.getAlarmEndLocation();
            if (StringUtils.isNotBlank(alarmEndLocation)) {
                String alarmEndAddress = alarmInfo.getAlarmEndAddress();
                if (StringUtils.isBlank(alarmEndAddress)) {
                    needEndAddressDTOList.add(alarmInfo);
                }
                String[] alarmEndLocationArr = alarmEndLocation.split(",");
                alarmInfo.setAlarmEndLongitude(alarmEndLocationArr[0]);
                alarmInfo.setAlarmEndLatitude(alarmEndLocationArr[1]);
            }
            // ??????????????? ??? ?????????????????????????????????????????????????????????????????????????????????
            if (Objects.equals(alarmInfo.getStatus(), 1) && "????????????".equals(alarmInfo.getHandleType())) {
                String remark = "";
                if (StringUtils.isNotBlank(alarmInfo.getRemark())) {
                    remark += alarmInfo.getRemark() + "???";
                }
                if (StringUtils.isNotEmpty(alarmInfo.getSendOfMsg())) {
                    remark += "???????????????" + alarmInfo.getSendOfMsg();
                }
                if (StringUtils.isNotEmpty(alarmInfo.getDealOfMsg())) {
                    remark += "???????????????" + alarmInfo.getDealOfMsg();
                }
                alarmInfo.setRemark(remark);
            }
            BindDTO bindDTO = bindInfoMap.get(alarmInfo.getMonitorId());
            if (bindDTO == null) {
                continue;
            }
            String monitorType = bindDTO.getMonitorType();
            alarmInfo.setMonitorType(Integer.valueOf(monitorType));
            Integer plateColor = bindDTO.getPlateColor();
            if (Objects.equals(monitorType, "0")) {
                alarmInfo.setPlateColor(plateColor);
            }
            alarmInfo.setName(bindDTO.getOrgName());
            String groupIds = bindDTO.getGroupId();
            String groupNames =
                Arrays.stream(groupIds.split(",")).map(userGroupIdAndNameMap::get).filter(Objects::nonNull)
                    .collect(Collectors.joining(","));
            alarmInfo.setAssignmentName(groupNames);
            alarmInfo.setEmployeeName(bindDTO.getProfessionalNames());
            String alarmStartTime = alarmInfo.getAlarmStartTime();
            if (StringUtils.isNotBlank(alarmStartTime)) {
                alarmInfo.setStartTime(DateUtil.getStringToString(alarmStartTime, DateUtil.DATE_FORMAT_SSS, null));
            }
            String deviceType = bindDTO.getDeviceType();
            if (StringUtils.isNotBlank(deviceType)) {
                alarmInfo.setDeviceType(Integer.parseInt(deviceType));
            }
            // ????????????????????????????????????????????????
            if (alarmInfo.getAlarmType() != null && StringUtils.isBlank(alarmInfo.getDescription())) {
                alarmInfo.setDescription(AlarmTypeUtil.getAlarmType(alarmInfo.getAlarmType()));
            }
        }
        // ?????????
        final Set<String> lngLats = Stream.concat(
                needStartAddressDTOList.stream().map(AlarmPageInfoDto::getAlarmStartLocation),
                needEndAddressDTOList.stream().map(AlarmPageInfoDto::getAlarmEndLocation)
        ).collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(lngLats)) {
            final Map<String, String> addressMap = AddressUtil.batchInverseAddress(lngLats);
            for (AlarmPageInfoDto dto : needEndAddressDTOList) {
                dto.setAlarmStartAddress(addressMap.get(dto.getAlarmStartLocation()));
            }
            for (AlarmPageInfoDto dto : needEndAddressDTOList) {
                dto.setAlarmEndAddress(addressMap.get(dto.getAlarmEndLocation()));
            }
        }
        for (AlarmPageInfoDto info : alarmPageInfoList) {
            info.setAlarmStartSpecificLocation(info.getAlarmStartAddress());
            info.setAlarmEndSpecificLocation(info.getAlarmEndAddress());
        }

        return new PageGridBean(alarmPageInfoList, pageInfo);
    }

    private Map<String, String> assemblePageQueryAlarmParam(AlarmPageReq alarmPageReq) {
        Map<String, String> queryParam = new HashMap<>(16);
        queryParam.put("monitorIds", alarmPageReq.getVehicleIds());
        queryParam.put("alarmTypes", alarmPageReq.getAlarmTypes());
        String alarmStartTime = alarmPageReq.getAlarmStartTime();
        String alarmEndTime = alarmPageReq.getAlarmEndTime();
        queryParam.put("startTime", alarmStartTime.replaceAll("-", "").replaceAll(" ", "").replaceAll(":", ""));
        queryParam.put("endTime", alarmEndTime.replaceAll("-", "").replaceAll(" ", "").replaceAll(":", ""));
        Integer status = alarmPageReq.getStatus();
        if (status != null && status != -1) {
            queryParam.put("status", String.valueOf(status));
        }
        Integer alarmSource = alarmPageReq.getAlarmSource();
        if (alarmSource != null && alarmSource != -1) {
            queryParam.put("alarmSource", String.valueOf(alarmSource));
        }
        queryParam.put("page", String.valueOf(alarmPageReq.getPage()));
        queryParam.put("pageSize", String.valueOf(alarmPageReq.getLength()));
        return queryParam;
    }

    /**
     * ??????????????????
     * @param monitorIds      ????????????id
     * @param alarmTypeStr    ????????????
     * @param startTime       ??????????????????
     * @param endTime         ??????????????????
     * @param alarmSource     ????????????
     * @param status          ????????????
     * @param pushType        ????????????
     * @param limit           ????????????
     * @param inTimeRangeFlag 0: ????????????????????????????????????????????????; null:???????????????????????????????????????????????????????????????
     * @param sort            ????????????;0:????????????;1:???????????? ?????????:0
     */
    @Override
    public <T> List<T> getAlarmInfo(String monitorIds, String alarmTypeStr, String startTime, String endTime,
        Integer alarmSource, Integer status, Integer pushType, Integer limit, Integer inTimeRangeFlag, Class<T> clazz,
        Integer sort) throws Exception {
        Map<String, String> queryParam = new HashMap<>(16);
        queryParam.put("monitorIds", monitorIds);
        queryParam.put("alarmTypes", alarmTypeStr);
        if (status != null && status != -1) {
            queryParam.put("status", String.valueOf(status));
        }
        if (alarmSource != null && alarmSource != -1) {
            queryParam.put("alarmSource", String.valueOf(alarmSource));
        }
        if (pushType != null && pushType != -1) {
            queryParam.put("pushType", String.valueOf(pushType));
        }
        queryParam.put("startTime", startTime.replaceAll("-", "").replaceAll(" ", "").replaceAll(":", ""));
        queryParam.put("endTime", endTime.replaceAll("-", "").replaceAll(" ", "").replaceAll(":", ""));
        if (limit != null) {
            queryParam.put("limitNum", String.valueOf(limit));
        }
        if (inTimeRangeFlag != null) {
            queryParam.put("timeRangeFlag", String.valueOf(inTimeRangeFlag));
        }
        if (sort != null) {
            queryParam.put("sort", String.valueOf(sort));
        }
        String queryResult = HttpClientUtil.send(PaasCloudAlarmUrlEnum.QUERY_ALARM_INFO, queryParam);
        JSONObject queryResultJsonObj = JSON.parseObject(queryResult);
        if (queryResultJsonObj == null || !Objects
            .equals(queryResultJsonObj.getInteger(PassCloudAlarmUrlUtil.RETURN_RESULT_CODE_KEY),
                PassCloudAlarmUrlUtil.SUCCESS_CODE)) {
            // ????????????id??????????????????
            queryParam.remove("monitorIds");
            throw new Exception(
                "??????PassCloud???????????????????????????" + (queryResultJsonObj != null ? queryResultJsonObj.getString("message") : null)
                    + "????????????" + JSONObject.toJSONString(queryParam));
        }
        return JSONObject.parseArray(queryResultJsonObj.getString("data"), clazz);
    }

    /**
     * ????????????????????????????????????
     * @param monitorIds     ????????????id
     * @param alarmTypeStr   ????????????
     * @param alarmStartTime ??????????????????
     * @param limitNum       ??????????????????
     */
    @Override
    public List<AlarmInfo> getTheSameTimeAlarmInfo(String monitorIds, String alarmTypeStr, String alarmStartTime,
        Integer limitNum) throws Exception {
        Map<String, String> queryParam = new HashMap<>(16);
        queryParam.put("monitorIds", monitorIds);
        queryParam.put("time", alarmStartTime.replaceAll("-", "").replaceAll(" ", "").replaceAll(":", ""));
        queryParam.put("alarmTypes", alarmTypeStr);
        if (limitNum != null && limitNum > 0) {
            queryParam.put("limitNum", String.valueOf(limitNum));
        }
        String queryResult = HttpClientUtil.send(PaasCloudAlarmUrlEnum.QUERY_THE_SAME_TIME_ALARM_INFO, queryParam);
        JSONObject queryResultJsonObj = JSON.parseObject(queryResult);
        if (queryResultJsonObj == null || !Objects
            .equals(queryResultJsonObj.getInteger(PassCloudAlarmUrlUtil.RETURN_RESULT_CODE_KEY),
                PassCloudAlarmUrlUtil.SUCCESS_CODE)) {
            // ????????????id??????????????????
            queryParam.remove("monitorIds");
            throw new Exception(
                "??????PassCloud??????????????????????????????????????????" + (queryResultJsonObj != null ? queryResultJsonObj.getString("message") :
                    null) + "????????????" + JSONObject.toJSONString(queryParam));
        }
        return JSONObject.parseArray(queryResultJsonObj.getString("data"), AlarmInfo.class);
    }

    @Override
    public PassCloudResultBean getAlarmPage(AlarmSearchQuery809 query) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("startTime",
            DateUtil.formatDate(query.getAlarmStartTime(), DateUtil.DATE_FORMAT_SHORT, DateUtil.DATE_FORMAT));
        params.put("endTime",
            DateUtil.formatDate(query.getAlarmEndTime(), DateUtil.DATE_FORMAT_SHORT, DateUtil.DATE_FORMAT));
        params.put("alarmTypes", StringUtils.join(AlarmTypeUtil.typeList(query.getAlarmType()), ","));
        params.put("monitorIds", query.getVehicleIds());
        params.put("page", String.valueOf(query.getPage()));
        params.put("pageSize", String.valueOf(query.getLimit()));
        String passResult = HttpClientUtil.send(PaasCloudUrlEnum.ALARM_809_FORWARD, params);
        return PassCloudResultBean.getPageInstance(passResult, (items) -> assemblyData(items));
    }

    private List<ForwardAlarmInfo> assemblyData(String items) {
        List<ForwardAlarmInfo> forwardAlarmInfoList = JSONObject.parseArray(items, ForwardAlarmInfo.class);
        List<String> existMonitorIdList =
            forwardAlarmInfoList.stream().map(ForwardAlarmInfo::getMonitorId).distinct().collect(Collectors.toList());
        Map<String, BindDTO> bindInfoMap = VehicleUtil.batchGetBindInfosByRedis(existMonitorIdList);
        List<GroupDTO> userGroupList = userService.getCurrentUserGroupList();
        Map<String, String> userGroupIdAndNameMap =
            userGroupList.stream().collect(Collectors.toMap(GroupDTO::getId, GroupDTO::getName));
        for (ForwardAlarmInfo forwardAlarmInfo : forwardAlarmInfoList) {
            BindDTO bindDTO = bindInfoMap.get(forwardAlarmInfo.getMonitorId());
            if (bindDTO == null) {
                continue;
            }
            String monitorType = bindDTO.getMonitorType();
            if (Objects.equals(monitorType, MonitorTypeEnum.VEHICLE.getType())) {
                Integer plateColor = bindDTO.getPlateColor();
                forwardAlarmInfo.setPlateColor(PlateColor.getNameOrBlankByCode(plateColor));
            }
            forwardAlarmInfo.setProfessionalsName(bindDTO.getProfessionalNames());
            forwardAlarmInfo.setGroupName(bindDTO.getOrgName());
            String groupIds = bindDTO.getGroupId();
            String groupNames =
                Arrays.stream(groupIds.split(",")).map(userGroupIdAndNameMap::get).filter(Objects::nonNull)
                    .collect(Collectors.joining(","));
            forwardAlarmInfo.setAssignmentName(groupNames);
            forwardAlarmInfo.setMonitorType(monitorType);
            forwardAlarmInfo.setAlarmSourceStr(Objects.equals(forwardAlarmInfo.getAlarmSource(), 0) ? "????????????" : "????????????");
            forwardAlarmInfo.setAlarmStatus(Objects.equals(forwardAlarmInfo.getStatus(), 0) ? "?????????" : "?????????");
            forwardAlarmInfo.setRoadTypeStr(VehicleUtil.getRoadTypeStr(forwardAlarmInfo.getRoadType()));
            Long alarmTime = forwardAlarmInfo.getAlarmTime();
            if (alarmTime != null && alarmTime > 0) {
                String startTimeStr = DateUtil
                    .getStringToString(String.valueOf(alarmTime), DateUtil.DATE_FORMAT_SSS, DateUtil.DATE_FORMAT_SHORT);
                forwardAlarmInfo.setStartTimeStr(startTimeStr);
            }
            Long handleTime = forwardAlarmInfo.getHandleTime();
            if (handleTime != null && handleTime > 0) {
                String handleTimeStr = DateUtil
                    .getStringToString(String.valueOf(handleTime), DateUtil.DATE_FORMAT, DateUtil.DATE_FORMAT_SHORT);
                forwardAlarmInfo.setHandleTimeStr(handleTimeStr);
            }
            String alarmLocation = forwardAlarmInfo.getAlarmLocation();
            if (StringUtils.isNotEmpty(alarmLocation)) {
                String[] address = alarmLocation.split(",");
                forwardAlarmInfo.setAlarmLongitude(address[0]);
                forwardAlarmInfo.setAlarmLatitude(address[1]);
            }
            String riskEventId = forwardAlarmInfo.getRiskEventId();
            if (StringUtils.isNotBlank(riskEventId)) {
                assemblyAdasAlarm(forwardAlarmInfo, null);
            }
            try {
                forwardAlarmInfo.setAlarmStartTimeStr(DateUtil
                    .formatDate(String.valueOf(forwardAlarmInfo.getAlarmStartTime()), DateUtil.DATE_FORMAT_SSS,
                        DateUtil.DATE_FORMAT_SHORT));
            } catch (Exception e) {
                logger.error("??????????????????", e);
            }
            //??????????????? 76?????????164???????????????????????????????????????????????????
            Integer alarmType = forwardAlarmInfo.getAlarmType();
            boolean alarmTypeFlag = alarmType.equals(76) || alarmType.equals(164);
            if (!alarmTypeFlag) {
                forwardAlarmInfo.setOverSpeedTime(null);
            }
        }
        return forwardAlarmInfoList;
    }

    @Override
    public OfflineExportInfo export(AlarmSearchQuery809 query) throws Exception {
        OfflineExportInfo instance = getOfflineExportInfo();
        TreeMap<String, String> params = new TreeMap<>();
        params.put("startTime",
            DateUtil.formatDate(query.getAlarmStartTime(), DateUtil.DATE_FORMAT_SHORT, DateUtil.DATE_FORMAT));
        params.put("endTime",
            DateUtil.formatDate(query.getAlarmEndTime(), DateUtil.DATE_FORMAT_SHORT, DateUtil.DATE_FORMAT));
        params.put("alarmTypes", query.getAlarmType());
        params.put("monitorIds", query.getVehicleIds());
        params.put("flag", String.valueOf(System.currentTimeMillis()));
        TreeMap<String, String> param = new TreeMap<>(params);
        instance.assembleCondition(param, OffLineExportBusinessId.ALARM_809_FORWARD);
        return instance;
    }

    @Override
    public JsonResultBean get809ForwardAlarmName() {
        List<AlarmType> alarmTypeList = getAlarmType();
        Map<String, String> forwardAlarmTypeAndNameMap = new HashMap<>(100);
        for (AlarmType alarm : alarmTypeList) {
            Arrays.stream(alarm.getPos().split(",")).collect(Collectors.toList()).forEach(obj -> {
                forwardAlarmTypeAndNameMap.put(obj, alarm.getName());
            });
        }
        return new JsonResultBean(forwardAlarmTypeAndNameMap);
    }

    private OfflineExportInfo getOfflineExportInfo() {
        String fileName = "809???????????????" + Date8Utils.getValToTime(LocalDateTime.now());
        return OfflineExportInfo.getInstance("809??????????????????", fileName + ".xls");
    }

    /**
     * ??????PassCloud??????????????????
     * @param handleAlarms            ????????????
     * @param needHandleAlarmTypeList ???????????????????????????
     * @param isHandleIoAlarm         ???????????????io??????
     * @param startTime               ????????????
     * @param endTime                 ????????????
     */
    @Override
    public void handleAlarmBatch(HandleAlarms handleAlarms, List<String> needHandleAlarmTypeList,
        boolean isHandleIoAlarm, String startTime, String endTime) throws Exception {
        Map<String, String> queryParam = new HashMap<>(16);
        queryParam.put("monitorId", handleAlarms.getVehicleId());
        queryParam.put("alarmTypes", StringUtils.join(needHandleAlarmTypeList, ","));
        //????????????
        if (handleAlarms.getIsAutoDeal() == 1) {
            UserDTO adminUserInfo = userService.getAdminUserInfo();
            queryParam.put("personId", adminUserInfo.getUuid());
            queryParam.put("personName", adminUserInfo.getUsername());
            queryParam.put("groupName", adminUserInfo.getOrgName());
        } else {
            UserDTO currentUserInfo = userService.getCurrentUserInfo();
            queryParam.put("personId", currentUserInfo.getUuid());
            queryParam.put("personName", currentUserInfo.getUsername());
            queryParam.put("groupName", userService.getCurrentUserOrg().getName());
        }
        int handleType = getHandleType(handleAlarms.getHandleType());
        queryParam.put("handleType", String.valueOf(handleType));
        queryParam.put("time", DateUtil.getDateToString(new Date(), DateUtil.DATE_FORMAT));
        if (isHandleIoAlarm) {
            queryParam.put("description", handleAlarms.getDescription());
        }
        queryParam.put("remark", handleAlarms.getRemark());
        if (Objects.equals(handleType, 2)) {
            queryParam.put("dealOfMsg", handleAlarms.getDealOfMsg());
        }
        // ????????????????????????????????????????????????????????????
        if (StringUtils.isNotBlank(startTime) && StringUtils.isNotBlank(endTime)) {
            queryParam.put("startTime", startTime);
            queryParam.put("endTime", endTime);
        }
        sendRequestHandleAlarm(queryParam, PaasCloudAlarmUrlEnum.HANDLE_ALARM_BATCH);
    }

    /**
     * ??????PassCloud????????????????????????
     * @param handleAlarms              ????????????
     * @param startTimeL                ??????????????????
     * @param alarmType                 ????????????
     * @param needTranslationHandleType ??????????????????	????????????
     */
    @Override
    public void handleAlarmSingle(HandleAlarms handleAlarms, Long startTimeL, String alarmType,
        Boolean needTranslationHandleType) throws Exception {
        Map<String, String> queryParam = new HashMap<>(16);
        queryParam.put("monitorId", handleAlarms.getVehicleId());
        queryParam.put("alarmType", alarmType);
        queryParam.put("alarmStartTime", DateUtil.getLongToDateStr(startTimeL, DateUtil.DATE_FORMAT_SSS));
        String handleType = handleAlarms.getHandleType();
        if (needTranslationHandleType) {
            handleType = String.valueOf(getHandleType(handleAlarms.getHandleType()));
        }
        UserDTO currentUserInfo = userService.getCurrentUserInfo();
        OrganizationLdap currentUserOrg = userService.getCurrentUserOrg();
        queryParam.put("handleType", handleType);
        queryParam.put("personId", currentUserInfo.getUuid());
        queryParam.put("personName", currentUserInfo.getUsername());
        queryParam.put("groupName", currentUserOrg.getName());
        queryParam.put("time", DateUtil.getDateToString(new Date(), DateUtil.DATE_FORMAT));
        queryParam.put("remark", handleAlarms.getRemark());
        if (AlarmTypeUtil.IO_ALARM.contains(Integer.valueOf(alarmType))) {
            queryParam.put("description", handleAlarms.getDescription());
        }
        if (Objects.equals(handleType, "2")) {
            queryParam.put("dealOfMsg", handleAlarms.getDealOfMsg());
        }
        sendRequestHandleAlarm(queryParam, PaasCloudAlarmUrlEnum.HANDLE_ALARM_SINGLE);
    }

    /**
     * ??????PassCloud????????????????????????
     * @param handleAlarms ????????????
     */
    @Override
    public void handleAlarmMulti(HandleMultiAlarms handleAlarms) throws Exception {
        Map<String, String> queryParam = new HashMap<>(16);
        int handleType = getHandleType(handleAlarms.getHandleType());
        queryParam.put("primaryKeys", handleAlarms.getPrimaryKeyStr());
        queryParam.put("handleType", String.valueOf(handleType));
        UserDTO currentUserInfo = userService.getCurrentUserInfo();
        queryParam.put("personId", currentUserInfo.getUuid());
        queryParam.put("personName", currentUserInfo.getUsername());
        OrganizationLdap currentUserOrg = userService.getCurrentUserOrg();
        queryParam.put("personGroupId", currentUserOrg.getUuid());
        queryParam.put("personGroupName", currentUserOrg.getName());
        queryParam.put("time", DateUtil.getDateToString(new Date(), DateUtil.DATE_FORMAT));
        queryParam.put("remark", handleAlarms.getRemark());
        if (Objects.equals(handleType, 2)) {
            queryParam.put("dealOfMsg", handleAlarms.getDealOfMsg());
        }
        sendRequestHandleAlarm(queryParam, PaasCloudAlarmUrlEnum.ALARM_HANDLE_BATCH);
    }

    /**
     * ????????????????????????
     * @param queryParam ??????
     * @param url        ??????url
     * @throws Exception Exception
     */
    private void sendRequestHandleAlarm(Map<String, String> queryParam, PaasCloudAlarmUrlEnum url) throws Exception {
        String queryResult = HttpClientUtil.send(url, queryParam);
        JSONObject queryResultJsonObj = JSON.parseObject(queryResult);
        if (queryResultJsonObj == null || !Objects
            .equals(queryResultJsonObj.getInteger(PassCloudAlarmUrlUtil.RETURN_RESULT_CODE_KEY),
                PassCloudAlarmUrlUtil.SUCCESS_CODE)) {
            String errorMsg =
                "??????PassCloud???????????????????????????" + (queryResultJsonObj != null ? queryResultJsonObj.getString("message") : null);
            throw new Exception(errorMsg);
        }
    }

    /**
     * ????????????????????????
     * @param handleTypeStr handleTypeStr
     * @return 0:??????; 1:??????; 2:????????????; 3:??????????????????; 4:????????????; 5:????????????
     */
    private int getHandleType(String handleTypeStr) {
        if (StringUtils.isBlank(handleTypeStr)) {
            return 3;
        }
        if (Objects.equals(handleTypeStr, "??????")) {
            return 0;
        }
        if (Objects.equals(handleTypeStr, "??????")) {
            return 1;
        }
        if (Objects.equals(handleTypeStr, "????????????")) {
            return 2;
        }
        if (Objects.equals(handleTypeStr, "??????????????????")) {
            return 3;
        }
        if (Objects.equals(handleTypeStr, "????????????")) {
            return 4;
        }
        if (Objects.equals(handleTypeStr, "????????????")) {
            return 5;
        }
        return 3;
    }

    /**
     * ?????????????????????
     */
    @Data
    private static class AlarmTypeCache {
        private static final long REFRESH_INTERVAL = 300L;
        private static final AtomicLong lastUpdateTime = new AtomicLong(0L);
        private static List<AlarmType> cache;

        public static List<AlarmType> getOrLoad(Callable<List<AlarmType>> loadFunction) {
            if (System.currentTimeMillis() - lastUpdateTime.get() > REFRESH_INTERVAL) {
                reloadCache(loadFunction);
            }
            return cache;
        }

        private static synchronized void reloadCache(Callable<List<AlarmType>> loadFunction) {
            if (System.currentTimeMillis() - lastUpdateTime.get() > REFRESH_INTERVAL) {
                try {
                    cache = loadFunction.call();
                } catch (Exception e) {
                    logger.error(e);
                }
                lastUpdateTime.set(System.currentTimeMillis());
            }
        }
    }
}

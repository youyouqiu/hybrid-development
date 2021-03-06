package com.zw.app.service.personalCenter.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zw.app.annotation.AppMethodVersion;
import com.zw.app.annotation.AppServerVersion;
import com.zw.app.controller.AppVersionConstant;
import com.zw.app.domain.personalCenter.WorkHourStatisticsInfo;
import com.zw.app.service.personalCenter.AppWorkHourReportService;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.GroupDTO;
import com.zw.platform.basic.service.MonitorIconService;
import com.zw.platform.basic.service.UserGroupService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.BigDataReport.BigDataQueryDate;
import com.zw.platform.domain.oil.Positional;
import com.zw.platform.domain.vas.workhourmgt.SensorSettingInfo;
import com.zw.platform.repository.vas.SensorPollingDao;
import com.zw.platform.repository.vas.SensorSettingsDao;
import com.zw.platform.service.monitoring.impl.HistoryServiceImpl;
import com.zw.platform.util.BigDataQueryUtil;
import com.zw.platform.util.common.MonitorUtils;
import com.zw.platform.util.common.UuidUtils;
import com.zw.platform.util.paas.PaasCloudUrlUtil;
import com.zw.platform.util.report.PaasCloudHBaseAccessEnum;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.CollationKey;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * @author penghj
 * @version 1.0
 * @date 2019/7/12 9:59
 */
@Service
@AppServerVersion
public class AppWorkHourReportServiceImpl implements AppWorkHourReportService {
    private static Logger log = LogManager.getLogger(AppWorkHourReportServiceImpl.class);

    @Autowired
    private SensorPollingDao sensorPollingDao;

    @Autowired
    private SensorSettingsDao sensorSettingsDao;

    @Autowired
    UserService userService;

    @Autowired
    private MonitorIconService monitorIconService;

    @Autowired
    private HistoryServiceImpl historyServiceImpl;

    @Autowired
    private UserGroupService userGroupService;

    private static final String YYYY_MM_DD = "yyyy-MM-dd";

    /**
     * ?????????????????????????????????????????????????????????????????????
     * @return
     * @throws Exception
     */
    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_FIVE, url = {
        "/clbs/app/reportManagement/workHourReport/judgeUserIfOwnSendWorkHourPollsMonitor" })
    public boolean judgeUserIfOwnSendWorkHourPollsMonitor() throws Exception {
        return judgeUserIfOwnSendPollsMonitor(Arrays.asList("0x80", "0x81"));
    }

    /**
     * ?????????????????????????????????????????????????????????????????????
     * @param sensorIdList ??????id??????
     * @return
     * @throws Exception
     */
    @Override
    public boolean judgeUserIfOwnSendPollsMonitor(List<String> sensorIdList) throws Exception {
        Set<String> userAssignMonitorIds = userService.getCurrentUserMonitorIds();
        if (CollectionUtils.isEmpty(userAssignMonitorIds) || CollectionUtils.isEmpty(sensorIdList)) {
            return false;
        }
        // ??????????????????????????????????????????????????????id
        Set<String> pollMonitorIdList =
            sensorPollingDao.getPollMonitorIdListBySensorIdAndOwnMonitor(sensorIdList, userAssignMonitorIds);
        return CollectionUtils.isNotEmpty(pollMonitorIdList);
    }

    /**
     * ?????????????????????????????????????????????????????????
     * @param page     ?????????
     * @param pageSize ??????????????????(4.1.3)
     * @return
     * @throws Exception
     */
    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_SEVEN, url = {
        "/clbs/app/reportManagement/workHourReport/getSendWorkHourPollsMonitorInfo" })
    public JSONObject getSendWorkHourPollsMonitorInfoSeven(Long page, Long pageSize, Long defaultSize)
        throws Exception {
        return getSendSensorPollingMonitorInfoSeven(page, pageSize, defaultSize, Arrays.asList("0x80", "0x81"));
    }

    @Override
    public JSONObject getSendSensorPollingMonitorInfoSeven(Long page, Long pageSize, Long defaultSize,
        List<String> sensorId) throws Exception {
        JSONObject result = new JSONObject();
        JSONArray assignmentList = new JSONArray();
        //???????????????????????????
        boolean anythingElse = false;

        // ????????????-???????????? ??????????????????
        List<GroupDTO> currentUserGroupList =
            userGroupService.getByGroupIdsAndUserId(userService.getCurrentUserUuid(), null).stream()
                .sorted(Comparator.comparing(GroupDTO::getCreateDataTime).reversed()).collect(Collectors.toList());

        boolean isAlreadySetDefaultMonitorId = false;
        Set<String> defaultCheckMonitorIdList = new HashSet<>();
        if (CollectionUtils.isNotEmpty(currentUserGroupList)) {
            if (CollectionUtils.isNotEmpty(currentUserGroupList)) {
                List<String> groupIds = currentUserGroupList.stream().map(GroupDTO::getId).collect(Collectors.toList());
                Map<String, Set<String>> assignMonitorIdsMap =
                    RedisHelper.batchGetSetReturnMap(RedisKeyEnum.GROUP_MONITOR.ofs(groupIds));
                Set<String> allMonitorIdList = assignMonitorIdsMap.get("allMonitorIdList");
                // ??????????????????????????????????????????????????????id
                Set<String> pollMonitorIdList =
                    sensorPollingDao.getPollMonitorIdListBySensorIdAndOwnMonitor(sensorId, allMonitorIdList);
                // ????????????????????????
                Map<String, BindDTO> bindDTOMap =
                    MonitorUtils.getBindDTOMap(pollMonitorIdList, "id", "name", "monitorType");
                // ??????????????????
                Map<String, String> iconMap = monitorIconService.getByMonitorId(pollMonitorIdList);

                Map<String, Set<String>> assignPollMonitorIdsMap = new HashMap<>();
                assignMonitorIdsMap.remove("allMonitorIdList");
                for (Map.Entry<String, Set<String>> entry : assignMonitorIdsMap.entrySet()) {
                    Set<String> value = entry.getValue();
                    value.retainAll(pollMonitorIdList);
                    if (value.size() > 0) {
                        assignPollMonitorIdsMap.put(entry.getKey(), value);
                    }
                }

                int startIndex = 0;
                for (GroupDTO groupDTO : currentUserGroupList) {
                    JSONObject assignmentJsonObj = new JSONObject();
                    String id = groupDTO.getId();
                    assignmentJsonObj.put("assId", id);
                    String name = groupDTO.getName();
                    assignmentJsonObj.put("assName", name);
                    Set<String> monitorIdList = assignPollMonitorIdsMap.get(id);
                    if (monitorIdList == null || monitorIdList.size() == 0) {
                        continue;
                    }
                    startIndex += 1;
                    if (((page - 1) * pageSize + 1) <= startIndex && page * pageSize >= startIndex) {

                        int size = monitorIdList.size();
                        if (defaultSize != null && size > 0 && size <= defaultSize && !isAlreadySetDefaultMonitorId) {
                            isAlreadySetDefaultMonitorId = true;
                            defaultCheckMonitorIdList.addAll(monitorIdList);
                        }
                        assignmentJsonObj.put("total", size);
                        JSONArray monitorInfoJsonArr = installAssignmentMonitorInfo(bindDTOMap, iconMap, monitorIdList);
                        //???????????????????????????
                        Collections.sort(monitorInfoJsonArr, new Comparator<Object>() {
                            Collator collator = Collator.getInstance(Locale.CHINA);

                            @Override
                            public int compare(Object o1, Object o2) {
                                CollationKey key1 = collator.getCollationKey(((JSONObject) o1).getString("name"));
                                CollationKey key2 = collator.getCollationKey(((JSONObject) o2).getString("name"));
                                return key1.compareTo(key2);
                            }
                        });

                        assignmentJsonObj.put("monitors", monitorInfoJsonArr);
                        assignmentList.add(assignmentJsonObj);
                    }

                    if (startIndex > (page * pageSize)) {
                        anythingElse = true;
                        break;
                    }
                }
            }
        }
        // ??????
        // List<Object> pageList = assignmentList.stream().skip(pageSize * (page - 1)).limit(pageSize.longValue())
        //     .collect(Collectors.toList());
        result.put("defaultCheckMonitorIdList", defaultCheckMonitorIdList);
        result.put("anythingElse", anythingElse);
        result.put("assignmentList", assignmentList);
        return result;
    }

    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_FIVE, url = {
        "/clbs/app/reportManagement/workHourReport/getWorkHourStatisticsInfo" })
    public JSONArray getWorkHourStatisticsInfo(String monitorIds, String startTime, String endTime, Integer sensorNo)
        throws Exception {
        JSONArray result = new JSONArray();
        SimpleDateFormat sdf = new SimpleDateFormat(YYYY_MM_DD);
        long startTimeLong = sdf.parse(startTime).getTime() / 1000;
        long endTimeLong = sdf.parse(endTime).getTime() / 1000;
        List<WorkHourStatisticsInfo> workHourStatisticsInfoList = new ArrayList<>();
        List<String> monitorIdList = Arrays.asList(monitorIds.split(","));
        Map<String, BindDTO> bindDTOMap = MonitorUtils.getBindDTOMap(monitorIdList, "id", "name");
        List<BigDataQueryDate> bigDataQueryDates =
            BigDataQueryUtil.getBigDataQueryDate(startTime, endTime, YYYY_MM_DD, 0, 0, 0);
        for (BigDataQueryDate queryCondition : bigDataQueryDates) {
            workHourStatisticsInfoList.addAll(getWorkHourStatisticsInfo(sensorNo, monitorIdList, queryCondition));
        }
        workHourStatisticsInfoList
            .forEach(info -> info.setMonitorId(UuidUtils.getUUIDFromBytes(info.getMonitorIdByte()).toString()));
        // ???????????????????????????????????? ?????????????????? ?????????????????????????????????
        if (Objects.equals(sdf.format(new Date()), endTime)) {
            // ??????????????????????????????????????????
            List<SensorSettingInfo> monitorWorkHourSensorInfo =
                sensorSettingsDao.getMonitorListBandSensorInfoBySensorType(monitorIdList, 4);
            if (CollectionUtils.isNotEmpty(monitorWorkHourSensorInfo)) {
                List<Positional> workHourDataList = getWorkHourData(sensorNo, endTimeLong, monitorIdList);
                if (CollectionUtils.isNotEmpty(workHourDataList)) {
                    for (String monitorId : monitorIdList) {
                        WorkHourStatisticsInfo workHourStatisticsInfo = new WorkHourStatisticsInfo();
                        workHourStatisticsInfo.setMonitorId(monitorId);
                        workHourStatisticsInfo.setDay(endTimeLong);
                        // ????????????
                        calculationWorkHourStatisticsDuration(sensorNo, monitorWorkHourSensorInfo, workHourDataList,
                            monitorId, workHourStatisticsInfo);
                        workHourStatisticsInfoList.add(workHourStatisticsInfo);
                    }
                }
            }
        }
        for (String monitorId : monitorIdList) {
            JSONObject workHourStatisticsData = new JSONObject();
            workHourStatisticsData.put("id", monitorId);
            if (!bindDTOMap.containsKey(monitorId)) {
                continue;
            }
            //??????????????????
            String monitorName = bindDTOMap.get(monitorId).getName();
            if (StringUtils.isEmpty(monitorName)) {
                continue;
            }
            workHourStatisticsData.put("monitorName", monitorName);
            List<WorkHourStatisticsInfo> monitorWorkHourStatisticsInfo =
                workHourStatisticsInfoList.stream().filter(info -> Objects.equals(info.getMonitorId(), monitorId))
                    .sorted(Comparator.comparingLong(WorkHourStatisticsInfo::getDay)).collect(Collectors.toList());
            installWorkHourStatisticsData(sdf, workHourStatisticsData, monitorWorkHourStatisticsInfo, startTimeLong,
                endTimeLong);
            result.add(workHourStatisticsData);
        }
        return result;
    }

    private List<Positional> getWorkHourData(Integer sensorNo, long endTimeLong, List<String> vehicleIds) {
        final Map<String, String> params = new HashMap<>(8);
        params.put("vehicleIds", JSON.toJSONString(vehicleIds));
        params.put("startTime", String.valueOf(endTimeLong));
        params.put("endTime", String.valueOf(System.currentTimeMillis()));
        params.put("sensorNo", String.valueOf(sensorNo));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_WORK_HOUR_DATA, params);
        return PaasCloudUrlUtil.getResultListData(str, Positional.class);
    }

    private List<WorkHourStatisticsInfo> getWorkHourStatisticsInfo(Integer sensorNo, List<String> vehicleIds,
                                                                   BigDataQueryDate queryCondition) {
        final Map<String, String> params = new HashMap<>(8);
        params.put("vehicleIds", JSON.toJSONString(vehicleIds));
        params.put("startTime", String.valueOf(queryCondition.getStartTime()));
        params.put("endTime", String.valueOf(queryCondition.getEndTime()));
        params.put("month", queryCondition.getMonth());
        params.put("sensorNo", String.valueOf(sensorNo));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_WORK_HOUR_STATISTICS_INFO, params);
        return PaasCloudUrlUtil.getResultListData(str, WorkHourStatisticsInfo.class);
    }

    /**
     * ????????????????????????
     * @param sdf
     * @param workHourStatisticsData
     * @param monitorWorkHourStatisticsInfo
     * @param startTimeLong
     * @param endTimeLong
     */
    private void installWorkHourStatisticsData(SimpleDateFormat sdf, JSONObject workHourStatisticsData,
        List<WorkHourStatisticsInfo> monitorWorkHourStatisticsInfo, Long startTimeLong, Long endTimeLong) {
        // ????????????
        long workDuration = 0L;
        // ????????????
        long standByDuration = 0L;
        // ????????????
        long haltDuration = 0L;
        // ??????????????????
        Map<String, Long> dailyWorkDuration = new TreeMap<>();
        // ??????????????????
        Map<String, Long> dailyStandByDuration = new TreeMap<>();
        // ??????????????????
        Map<String, Long> dailyHaltDuration = new TreeMap<>();
        for (long i = startTimeLong; i <= endTimeLong; i += 60 * 60 * 24) {
            String dateStr = sdf.format(new Date(i * 1000));
            dailyWorkDuration.put(dateStr, 0L);
            dailyStandByDuration.put(dateStr, 0L);
            dailyHaltDuration.put(dateStr, 0L);
        }
        for (WorkHourStatisticsInfo info : monitorWorkHourStatisticsInfo) {
            String dateStr = sdf.format(new Date(info.getDay() * 1000));
            Long workTime = info.getWorkTime();
            workDuration += workTime == null ? 0L : workTime;
            dailyWorkDuration.put(dateStr, workTime);
            Long standByTime = info.getAwaitTime();
            standByDuration += standByTime == null ? 0L : standByTime;
            dailyStandByDuration.put(dateStr, standByTime);
            Long haltTime = info.getStopTime();
            haltDuration += haltTime == null ? 0L : haltTime;
            dailyHaltDuration.put(dateStr, haltTime);
        }
        workHourStatisticsData.put("workDuration", workDuration);
        workHourStatisticsData.put("dailyWorkDuration", dailyWorkDuration);
        workHourStatisticsData.put("standByDuration", standByDuration);
        workHourStatisticsData.put("dailyStandByDuration", dailyStandByDuration);
        workHourStatisticsData.put("haltDuration", haltDuration);
        workHourStatisticsData.put("dailyHaltDuration", dailyHaltDuration);
    }

    /**
     * ????????????????????????
     * @param sensorNo
     * @param monitorWorkHourSensorInfo
     * @param workHourDataList
     * @param monitorId
     * @param workHourStatisticsInfo
     */
    private void calculationWorkHourStatisticsDuration(Integer sensorNo,
        List<SensorSettingInfo> monitorWorkHourSensorInfo, List<Positional> workHourDataList, String monitorId,
        WorkHourStatisticsInfo workHourStatisticsInfo) {
        // ????????????
        long workDuration = 0L;
        // ????????????
        long standByDuration = 0L;
        // ????????????
        long haltDuration = 0L;
        // ?????????????????????????????????????????????????????????
        Optional<SensorSettingInfo> sensorOptional = monitorWorkHourSensorInfo.stream().filter(
            sensorInfo -> Objects.equals(monitorId, sensorInfo.getVehicleId()) && Objects
                .equals(Integer.parseInt(sensorInfo.getSensorOutId(), 16) - 127, sensorNo)).findFirst();
        if (sensorOptional.isPresent()) {
            SensorSettingInfo sensorSettingInfo = sensorOptional.get();
            List<Positional> monitorWorkHourData = workHourDataList.stream()
                .filter(info -> Objects.equals(monitorId, UuidUtils.getUUIDFromBytes(info.getVehicleId()).toString()))
                .sorted(Comparator.comparingLong(Positional::getVtime)).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(monitorWorkHourData)) {
                // ?????????????????? ?????????????????????????????????????????????
                JSONArray installWorkHourData =
                    historyServiceImpl.installWorkHourData(sensorNo, sensorSettingInfo, monitorWorkHourData, false);
                if (installWorkHourData.size() > 0) {
                    JSONObject zeroJsonObj = installWorkHourData.getJSONObject(0);
                    // ?????? 1:????????????; 2:??????????????????; 3:?????????;
                    Integer zeroType = zeroJsonObj.getInteger("type");
                    // ???????????? 0:??????; 1:??????; 2:??????;
                    Integer zeroWorkingPosition = zeroJsonObj.getInteger("workingPosition");
                    int workStateStartIndex = 0;
                    // ???????????? -1:??????????????????; 0:??????; 1:??????; 2:??????;
                    int oldState = zeroType == 2 ? -1 : zeroWorkingPosition;
                    // ?????????????????????????????? 0:??????; 1:??????; 2:??????;
                    Integer oldAddDurationState = null;
                    for (int i = 0, len = installWorkHourData.size(); i < len; i++) {
                        JSONObject workHourInfoJsonObj = installWorkHourData.getJSONObject(i);
                        Long nowTime = workHourInfoJsonObj.getLong("timeL");
                        // ?????? 1:????????????; 2:??????????????????; 3:?????????;
                        Integer type = workHourInfoJsonObj.getInteger("type");
                        // ???????????? 0:??????; 1:??????; 2:??????;
                        Integer workingPosition = workHourInfoJsonObj.getInteger("workingPosition");
                        int nowState = type == 2 ? -1 : workingPosition;
                        int previousIndex = i - 1 >= 0 ? i - 1 : 0;
                        JSONObject previousWorkHourInfoJsonObj = installWorkHourData.getJSONObject(previousIndex);
                        Long previousTime = previousWorkHourInfoJsonObj.getLong("timeL");
                        // ????????????????????????
                        long timeInterval = nowTime - previousTime;
                        // ??????????????????????????????
                        boolean isNeedAddNullData = timeInterval > 300;
                        // ???????????????????????????????????????????????????300s???????????????????????? ???????????? ?????? ????????????????????????????????????
                        if (isNeedAddNullData || !Objects.equals(nowState, oldState) || i == len - 1) {
                            JSONObject workStateStartJsonObj = installWorkHourData.getJSONObject(workStateStartIndex);
                            Long workStateStartTime = workStateStartJsonObj.getLong("timeL");
                            //??????????????????
                            long duration = previousTime - workStateStartTime;
                            // ???????????????????????????????????? ??????????????????,?????????300s?????????????????????
                            if (Objects.equals(oldState, -1)) {
                                duration = isNeedAddNullData ? duration + timeInterval : duration;
                                //??????5??????????????????????????????????????????, ?????????????????????????????????
                                if (duration <= 300) {
                                    if (Objects.equals(oldAddDurationState, 0)) {
                                        haltDuration += duration;
                                    } else if (Objects.equals(oldAddDurationState, 1)) {
                                        workDuration += duration;
                                    } else if (Objects.equals(oldAddDurationState, 2)) {
                                        standByDuration += duration;
                                    }
                                }
                            } else {
                                duration = timeInterval <= 300 ? nowTime - workStateStartTime : duration;
                                // ??????
                                if (Objects.equals(oldState, 0)) {
                                    haltDuration += duration;
                                    oldAddDurationState = 0;
                                    // ??????
                                } else if (Objects.equals(oldState, 1)) {
                                    workDuration += duration;
                                    oldAddDurationState = 1;
                                    // ??????
                                } else if (Objects.equals(oldState, 2)) {
                                    standByDuration += duration;
                                    oldAddDurationState = 2;
                                }
                            }
                            workStateStartIndex = i;
                            oldState = nowState;
                        }
                    }
                }
            }
        }
        workHourStatisticsInfo.setWorkTime(workDuration);
        workHourStatisticsInfo.setStopTime(haltDuration);
        workHourStatisticsInfo.setAwaitTime(standByDuration);
    }

    /**
     * ?????????????????????????????????
     * @param bindDTOMap
     * @param iconMap
     * @param monitorIdList
     * @return
     */
    public static JSONArray installAssignmentMonitorInfo(Map<String, BindDTO> bindDTOMap, Map<String, String> iconMap,
        Set<String> monitorIdList) {
        JSONArray monitorInfoJsonArr = new JSONArray();
        for (String monitorId : monitorIdList) {
            JSONObject monitorInfo = new JSONObject();
            monitorInfo.put("id", monitorId);
            // ??????????????????
            String monitorName = null;
            // ??????????????????
            String monitorType = null;
            if (bindDTOMap.containsKey(monitorId)) {
                monitorName = bindDTOMap.get(monitorId).getName();
                monitorType = bindDTOMap.get(monitorId).getMonitorType();
            }
            monitorInfo.put("name", monitorName);
            monitorInfo.put("type", monitorType);
            // ??????????????????
            String monitorIcon = iconMap.get(monitorId);

            monitorInfo.put("icon", monitorIcon);
            monitorInfoJsonArr.add(monitorInfo);
        }
        return monitorInfoJsonArr;
    }
}

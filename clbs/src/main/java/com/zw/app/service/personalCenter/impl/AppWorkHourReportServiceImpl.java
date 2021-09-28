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
     * 判断用户是否拥有下发了工时传感器轮询的监控对象
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
     * 判断用户是否拥有下发了对应传感器轮询的监控对象
     * @param sensorIdList 外设id集合
     * @return
     * @throws Exception
     */
    @Override
    public boolean judgeUserIfOwnSendPollsMonitor(List<String> sensorIdList) throws Exception {
        Set<String> userAssignMonitorIds = userService.getCurrentUserMonitorIds();
        if (CollectionUtils.isEmpty(userAssignMonitorIds) || CollectionUtils.isEmpty(sensorIdList)) {
            return false;
        }
        // 获得权限下轮询了对应传感器的监控对象id
        Set<String> pollMonitorIdList =
            sensorPollingDao.getPollMonitorIdListBySensorIdAndOwnMonitor(sensorIdList, userAssignMonitorIds);
        return CollectionUtils.isNotEmpty(pollMonitorIdList);
    }

    /**
     * 获得下发了工时传感器轮询的监控对象信息
     * @param page     第几页
     * @param pageSize 每页显示数量(4.1.3)
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
        //判断是否还有下一页
        boolean anythingElse = false;

        // 得到用户-分组缓存 创建时间倒序
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
                // 获得权限下轮询了对应传感器的监控对象id
                Set<String> pollMonitorIdList =
                    sensorPollingDao.getPollMonitorIdListBySensorIdAndOwnMonitor(sensorId, allMonitorIdList);
                // 监控对象信息配置
                Map<String, BindDTO> bindDTOMap =
                    MonitorUtils.getBindDTOMap(pollMonitorIdList, "id", "name", "monitorType");
                // 监控对象图标
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
                        //监控对象按车牌排序
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
        // 分页
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
        // 判断查询日期是否包含当天 如果包含当天 当天的工时需要实时计算
        if (Objects.equals(sdf.format(new Date()), endTime)) {
            // 监控对象绑定的工时传感器信息
            List<SensorSettingInfo> monitorWorkHourSensorInfo =
                sensorSettingsDao.getMonitorListBandSensorInfoBySensorType(monitorIdList, 4);
            if (CollectionUtils.isNotEmpty(monitorWorkHourSensorInfo)) {
                List<Positional> workHourDataList = getWorkHourData(sensorNo, endTimeLong, monitorIdList);
                if (CollectionUtils.isNotEmpty(workHourDataList)) {
                    for (String monitorId : monitorIdList) {
                        WorkHourStatisticsInfo workHourStatisticsInfo = new WorkHourStatisticsInfo();
                        workHourStatisticsInfo.setMonitorId(monitorId);
                        workHourStatisticsInfo.setDay(endTimeLong);
                        // 计算时长
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
            //监控对象名称
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
     * 组装工时统计数据
     * @param sdf
     * @param workHourStatisticsData
     * @param monitorWorkHourStatisticsInfo
     * @param startTimeLong
     * @param endTimeLong
     */
    private void installWorkHourStatisticsData(SimpleDateFormat sdf, JSONObject workHourStatisticsData,
        List<WorkHourStatisticsInfo> monitorWorkHourStatisticsInfo, Long startTimeLong, Long endTimeLong) {
        // 有效工时
        long workDuration = 0L;
        // 待机工时
        long standByDuration = 0L;
        // 停机工时
        long haltDuration = 0L;
        // 每天有效工时
        Map<String, Long> dailyWorkDuration = new TreeMap<>();
        // 每天待机工时
        Map<String, Long> dailyStandByDuration = new TreeMap<>();
        // 每天停机工时
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
     * 计算工时统计时长
     * @param sensorNo
     * @param monitorWorkHourSensorInfo
     * @param workHourDataList
     * @param monitorId
     * @param workHourStatisticsInfo
     */
    private void calculationWorkHourStatisticsDuration(Integer sensorNo,
        List<SensorSettingInfo> monitorWorkHourSensorInfo, List<Positional> workHourDataList, String monitorId,
        WorkHourStatisticsInfo workHourStatisticsInfo) {
        // 有效工时
        long workDuration = 0L;
        // 待机工时
        long standByDuration = 0L;
        // 停机工时
        long haltDuration = 0L;
        // 判断监控对象是否绑定了对应序号的传感器
        Optional<SensorSettingInfo> sensorOptional = monitorWorkHourSensorInfo.stream().filter(
            sensorInfo -> Objects.equals(monitorId, sensorInfo.getVehicleId()) && Objects
                .equals(Integer.parseInt(sensorInfo.getSensorOutId(), 16) - 127, sensorNo)).findFirst();
        if (sensorOptional.isPresent()) {
            SensorSettingInfo sensorSettingInfo = sensorOptional.get();
            List<Positional> monitorWorkHourData = workHourDataList.stream()
                .filter(info -> Objects.equals(monitorId, UuidUtils.getUUIDFromBytes(info.getVehicleId()).toString()))
                .sorted(Comparator.comparingLong(Positional::getVtime)).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(monitorWorkHourData)) {
                // 组装工时数据 油耗波动式需要重新计算工作状态
                JSONArray installWorkHourData =
                    historyServiceImpl.installWorkHourData(sensorNo, sensorSettingInfo, monitorWorkHourData, false);
                if (installWorkHourData.size() > 0) {
                    JSONObject zeroJsonObj = installWorkHourData.getJSONObject(0);
                    // 状态 1:正常数据; 2:无传感器数据; 3:无数据;
                    Integer zeroType = zeroJsonObj.getInteger("type");
                    // 工作状态 0:停机; 1:工作; 2:待机;
                    Integer zeroWorkingPosition = zeroJsonObj.getInteger("workingPosition");
                    int workStateStartIndex = 0;
                    // 时长状态 -1:无传感器数据; 0:停机; 1:工作; 2:待机;
                    int oldState = zeroType == 2 ? -1 : zeroWorkingPosition;
                    // 上一次添加时长的状态 0:停机; 1:工作; 2:待机;
                    Integer oldAddDurationState = null;
                    for (int i = 0, len = installWorkHourData.size(); i < len; i++) {
                        JSONObject workHourInfoJsonObj = installWorkHourData.getJSONObject(i);
                        Long nowTime = workHourInfoJsonObj.getLong("timeL");
                        // 状态 1:正常数据; 2:无传感器数据; 3:无数据;
                        Integer type = workHourInfoJsonObj.getInteger("type");
                        // 工作状态 0:停机; 1:工作; 2:待机;
                        Integer workingPosition = workHourInfoJsonObj.getInteger("workingPosition");
                        int nowState = type == 2 ? -1 : workingPosition;
                        int previousIndex = i - 1 >= 0 ? i - 1 : 0;
                        JSONObject previousWorkHourInfoJsonObj = installWorkHourData.getJSONObject(previousIndex);
                        Long previousTime = previousWorkHourInfoJsonObj.getLong("timeL");
                        // 前后两点时间间隔
                        long timeInterval = nowTime - previousTime;
                        // 是否需要添加空白数据
                        boolean isNeedAddNullData = timeInterval > 300;
                        // 如果前后状态不一致或者数据相差大于300s或者是最后一个点 计算时长 或者 判读是否需要移除无效数据
                        if (isNeedAddNullData || !Objects.equals(nowState, oldState) || i == len - 1) {
                            JSONObject workStateStartJsonObj = installWorkHourData.getJSONObject(workStateStartIndex);
                            Long workStateStartTime = workStateStartJsonObj.getLong("timeL");
                            //状态持续时长
                            long duration = previousTime - workStateStartTime;
                            // 之前的状态是无传感器数据 判断间隔时长,小等于300s移除无效数据段
                            if (Objects.equals(oldState, -1)) {
                                duration = isNeedAddNullData ? duration + timeInterval : duration;
                                //小于5分钟的无传感器数据段需要移除, 时长统计在之前一个状态
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
                                // 停机
                                if (Objects.equals(oldState, 0)) {
                                    haltDuration += duration;
                                    oldAddDurationState = 0;
                                    // 工作
                                } else if (Objects.equals(oldState, 1)) {
                                    workDuration += duration;
                                    oldAddDurationState = 1;
                                    // 待机
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
     * 组装分组下监控对象信息
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
            // 监控对象名称
            String monitorName = null;
            // 监控对象类型
            String monitorType = null;
            if (bindDTOMap.containsKey(monitorId)) {
                monitorName = bindDTOMap.get(monitorId).getName();
                monitorType = bindDTOMap.get(monitorId).getMonitorType();
            }
            monitorInfo.put("name", monitorName);
            monitorInfo.put("type", monitorType);
            // 监控对象图标
            String monitorIcon = iconMap.get(monitorId);

            monitorInfo.put("icon", monitorIcon);
            monitorInfoJsonArr.add(monitorInfo);
        }
        return monitorInfoJsonArr;
    }
}

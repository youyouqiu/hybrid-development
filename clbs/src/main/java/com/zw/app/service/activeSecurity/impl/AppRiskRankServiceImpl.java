package com.zw.app.service.activeSecurity.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Sets;
import com.zw.adas.utils.elasticsearch.AdasElasticSearchUtil;
import com.zw.app.annotation.AppMethodVersion;
import com.zw.app.annotation.AppServerVersion;
import com.zw.app.controller.AppVersionConstant;
import com.zw.app.domain.alarm.Assignment;
import com.zw.app.domain.alarm.Monitor;
import com.zw.app.domain.alarm.MonitorAppInfo;
import com.zw.app.domain.alarm.PercentageOfRank;
import com.zw.app.domain.alarm.RiskRankResult;
import com.zw.app.repository.mysql.alarm.AppRiskRankDao;
import com.zw.app.service.activeSecurity.AppRiskRankService;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.GroupDTO;
import com.zw.platform.basic.service.UserGroupService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.repository.vas.SensorPollingDao;
import com.zw.platform.service.riskManagement.RiskEventConfigService;
import com.zw.platform.util.common.Date8Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Response;

import java.io.IOException;
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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AppServerVersion
public class AppRiskRankServiceImpl implements AppRiskRankService {

    private static Logger log = LogManager.getLogger(AppRiskRankServiceImpl.class);

    @Autowired
    private AppRiskRankDao appRiskRankDao;

    @Autowired
    private AdasElasticSearchUtil adasElasticSearchUtil;

    @Autowired
    private RiskEventConfigService riskEventConfigService;

    @Autowired
    private UserService userService;

    @Autowired
    private SensorPollingDao sensorPollingDao;

    @Autowired
    UserGroupService userGroupService;

    /**
     * 适用于上线统计和超速统计模块
     * @param page     第几页
     * @param pageSize 每页显示数量
     * @param type     要筛选的监控对象类型（0车，1人，2物，3所有）
     * @return
     * @throws Exception
     */
    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_ONE, url = {
        "/clbs/app/risk/riskRank/getMonitorOfUser" })
    public JSONObject getAssList(Integer page, Integer pageSize, String type) {
        JSONObject result = new JSONObject();
        List<Assignment> assignmentList = new ArrayList<>();
        if (page != null && pageSize != null) {
            //判断是否还有下一页
            boolean anythingElse = false;
            //根据前端传的参数选取分组
            List<GroupDTO> groupDTOS = userGroupService.getByGroupIdsAndUserId(userService.getCurrentUserUuid(), null);
            List<Assignment> assignmentIds = new ArrayList<>();
            if (groupDTOS != null) {
                int startIndex;
                int endIndex;
                if (groupDTOS.size() > (page * pageSize)) {
                    anythingElse = true;
                    startIndex = pageSize * (page - 1);
                    endIndex = pageSize * page - 1;
                } else {
                    anythingElse = false;
                    startIndex = pageSize * (page - 1);
                    endIndex = groupDTOS.size() - 1;
                }
                groupDTOS.subList(startIndex, endIndex).forEach(o -> {
                    Assignment assignment = new Assignment();
                    assignment.setAssId(o.getId());
                    assignment.setAssName(o.getName());
                    assignmentIds.add(assignment);
                });
            }
            buildData(type, result, assignmentList, anythingElse, assignmentIds, null);
            return result;
        }
        return result;
    }

    /**
     * 适用于上线统计和超速统计模块(4.1.3)
     * @param page     第几页
     * @param pageSize 每页显示数量
     * @param type     要筛选的监控对象类型（0车，1人，2物，3所有）
     * @return
     * @throws Exception
     */
    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_SEVEN, url = {
        "/clbs/app/risk/riskRank/getMonitorOfUser" })
    public JSONObject getAssListSeven(Integer page, Integer pageSize, String type) {
        JSONObject result = new JSONObject();
        if (page != null && pageSize != null) {
            return getSearchMonitor(page, pageSize, type, null, null, null);
        }
        return result;
    }

    /**
     * 适用于上线统计和超速统计模块
     * @param page       第几页
     * @param pageSize   每页显示数量
     * @param type       要筛选的监控对象类型（0车，1人，2物，3所有）
     * @param search     搜索的内容
     * @param searchType 搜索的类型（0 按监控对象，1 按分组)
     * @return
     * @throws Exception
     */
    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_SEVEN, url = {
        "/clbs/app/risk/riskRank/getFuzzyMonitorOfUser" })
    public JSONObject getFuzzyAssList(Integer page, Integer pageSize, String type, String search, Integer searchType) {
        if (page != null && pageSize != null) {
            return getSearchMonitor(page, pageSize, type, search, null, searchType);
        }
        return new JSONObject();
    }

    private JSONObject getSearchMonitor(Integer page, Integer pageSize, String type, String search,
        Set<String> filterIds, Integer searchType) {
        JSONObject result = new JSONObject();
        String userId = SystemHelper.getCurrentUser().getId().toString();
        String uuid = userService.getUserUuidByDn(userId);
        List<MonitorAppInfo> monitorAppInfoList = appRiskRankDao.searchMonitor(uuid, type, search, searchType);
        Set<String> assignmentIds = new HashSet<>();
        List<Assignment> assignmentList = new ArrayList<>();
        Map<String, List<Monitor>> map = new LinkedHashMap<>();
        Map<String, String> assignmentNameMap = new HashMap<>();
        boolean anythingElse = false;
        if (monitorAppInfoList != null) {
            int start = (page - 1) * pageSize;
            int end = start + pageSize;
            for (MonitorAppInfo info : monitorAppInfoList) {
                if (filterIds == null || filterIds.contains(info.getMonitorId())) {
                    assignmentIds.add(info.getAssignmentId());
                } else {
                    continue;
                }
                if (assignmentIds.size() < start || (start != 0 && assignmentIds.size() == start)) {
                    continue;
                }
                if (assignmentIds.size() > end) {
                    anythingElse = true;
                    break;
                }
                List<Monitor> monitorList = map.get(info.getAssignmentId());
                if (monitorList == null) {
                    monitorList = new ArrayList<>();
                }
                Monitor monitor = new Monitor();
                monitor.setType(info.getMonitorType());
                monitor.setName(info.getMonitorName());
                monitor.setId(info.getMonitorId());
                monitorList.add(monitor);
                map.put(info.getAssignmentId(), monitorList);
                assignmentNameMap.put(info.getAssignmentId(), info.getAssignmentName());
            }
        }
        for (Map.Entry<String, List<Monitor>> entry : map.entrySet()) {
            Assignment assignment = new Assignment();
            List<Monitor> monitors = entry.getValue();
            Collections.sort(monitors, new Comparator<Monitor>() {
                Collator collator = Collator.getInstance(Locale.CHINA);

                @Override
                public int compare(Monitor o1, Monitor o2) {
                    CollationKey key1 = collator.getCollationKey(o1.getName());
                    CollationKey key2 = collator.getCollationKey(o2.getName());
                    return key1.compareTo(key2);
                }
            });
            assignment.setMonitors(monitors);
            assignment.setTotal(monitors.size());
            assignment.setAssId(entry.getKey());
            assignment.setAssName(assignmentNameMap.get(entry.getKey()));
            assignmentList.add(assignment);
        }
        result.put("anythingElse", anythingElse);
        result.put("assignmentList", assignmentList);
        return result;
    }

    private JSONArray searchAssignment(JSONArray array, String search) {
        JSONArray re = new JSONArray();
        if (search == null || search.equals("")) {
            return re;
        }
        for (Object o : array) {
            JSONObject jsonObject = JSON.parseObject(o.toString());
            String name = jsonObject.getString("name");
            if (name != null && name.contains(search)) {
                re.add(jsonObject);
            }
        }
        return re;
    }

    private void buildData(String type, JSONObject result, List<Assignment> assignmentList, boolean anythingElse,
        List<Assignment> assignmentIds, Set<String> filterIds) {
        //组装每个分组的数据
        if (assignmentIds.size() > 0) {
            Jedis jedis1 = null;
            for (int i = 0; i < assignmentIds.size(); i++) {
                Map<String, Monitor> assMap = new LinkedHashMap<>();//分组中监控对象实体map
                List<Monitor> monitors = new LinkedList<>();
                Assignment assignment = assignmentIds.get(i);
                String assId = assignment.getAssId();
                List<String> mids = new ArrayList<>(RedisHelper.getSet(RedisKeyEnum.GROUP_MONITOR.of(assId)));
                if (mids.isEmpty()) {
                    assignment.setTotal(0);

                    assignment.setMonitors(monitors);
                    assignment.setMids(new ArrayList<>());
                    assignmentList.add(assignment);
                    continue;
                }
                //组装分组中的监控对象信息
                assemblyMonitors(type, monitors, mids, assMap, filterIds);
                assignment.setTotal(monitors.size());
                assignment.setMonitors(monitors);
                assignment.setMids(mids);
                assignment.setAssApp(assMap);
                assignmentList.add(assignment);
            }
            result.put("anythingElse", anythingElse);
            result.put("assignmentList", assignmentList);
        }
    }

    private void assemblyMonitors(String type, List<Monitor> monitors, List<String> monitorIds,
        Map<String, Monitor> assMap, Set<String> filterIds) {
        List<RedisKey> redisKeys = new ArrayList<>();
        for (String mid : monitorIds) {
            if ((filterIds != null && filterIds.contains(mid)) || filterIds == null) {
                redisKeys.add(RedisKeyEnum.MONITOR_INFO.of(mid));
            }
        }

        List<Map<String, String>> configInfos =
            RedisHelper.batchGetHashMap(redisKeys, Arrays.asList(new String[] { "name", "id", "monitorType" }));

        for (Map<String, String> coconfigInfo : configInfos) {
            String mid = coconfigInfo.get("id");
            String monitorType = coconfigInfo.get("monitorType");
            if ((monitorType != null && monitorType.equals(type)) || type.equals("3")) {
                Monitor monitor = new Monitor();
                monitor.setId(mid);
                monitor.setName(coconfigInfo.get("name"));
                monitor.setType(monitorType);
                assMap.put(mid, monitor);
                monitors.add(monitor);
            }
        }
    }

    private List<Assignment> getAssignments(Integer startIndex, Integer endIndex, JSONArray assignmentArray) {
        List<Assignment> assignmentIds = new ArrayList<>();
        for (int i = startIndex; i <= endIndex; i++) {
            JSONObject assign = (JSONObject) assignmentArray.get(i);
            String assignmentId = assign.getString("id");
            String assName = assign.getString("name");
            Assignment assignmentInfo = new Assignment();
            assignmentInfo.setAssId(assignmentId);
            assignmentInfo.setAssName(assName);
            assignmentIds.add(assignmentInfo);
        }
        return assignmentIds;
    }

    /**
     * 适用于报警排行和报警处置模块
     * @param page     第几页
     * @param pageSize 每页数量
     * @return
     */
    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_ONE, url = {
        "/clbs/app/risk/riskRank/getVehiclesOfUser" })
    public JSONObject getAssVeList(Integer page, Integer pageSize) throws Exception {
        JSONObject result = new JSONObject();
        List<Assignment> assignmentList = new ArrayList<>();
        JSONObject jsonObject = getAssList(page, pageSize, "0");
        boolean anythingElse = jsonObject.getBoolean("anythingElse");
        JSONArray array = jsonObject.getJSONArray("assignmentList");
        if (array == null) {
            result.put("anythingElse", anythingElse);
            result.put("assignmentList", assignmentList);
            return result;
        }
        //获得黑标有参数设置的车辆ids
        Set<String> allSetVids = appRiskRankDao.findAllVids();
        //获得川冀标有参数设置的车辆ids
        allSetVids.addAll(appRiskRankDao.findOtherVids());
        allSetVids.addAll(appRiskRankDao.findJingStandardVids());

        assignmentList = array.toJavaList(Assignment.class);
        for (Assignment assignment : assignmentList) {
            List<String> mids = assignment.getMids();
            List<Monitor> monitors = new LinkedList<>();
            Map<String, Monitor> assMap = assignment.getAssApp();
            if (mids.size() == 0) {
                continue;
            }
            List<Monitor> monitors1 = assignment.getMonitors();
            Set<String> vehicleIds = Sets.intersection(allSetVids, new HashSet<>(mids));
            for (Monitor monitor : monitors1) {
                if (vehicleIds.contains(monitor.getId())) {
                    monitors.add(assMap.get(monitor.getId()));
                }
            }
            assignment.setMids(new ArrayList<>(vehicleIds));
            assignment.setMonitors(monitors);
            assignment.setTotal(monitors.size());
        }
        result.put("anythingElse", anythingElse);
        result.put("assignmentList", assignmentList);
        return result;
    }

    /**
     * 适用于报警排行和报警处置模块(4.1.3)
     * @param page     第几页
     * @param pageSize 每页数量
     * @return
     */
    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_SEVEN, url = {
        "/clbs/app/risk/riskRank/getVehiclesOfUser" })
    public JSONObject getAssVeListSeven(Integer page, Integer pageSize) throws Exception {
        JSONObject result = new JSONObject();
        if (page != null && pageSize != null) {
            Set<String> filterIds = new HashSet<>();
            //获得黑标有参数设置的车辆ids
            filterIds.addAll(appRiskRankDao.findAllVids());
            //获得其他协议标有参数设置的车辆ids
            filterIds.addAll(appRiskRankDao.findOtherVids());
            //获得京标有参数设置的车辆ids
            filterIds.addAll(appRiskRankDao.findJingStandardVids());
            return getSearchMonitor(page, pageSize, "0", null, filterIds, null);
        }
        return result;
    }

    /**
     * 适用于报警排行和报警处置模块
     * @param page       第几页
     * @param pageSize   每页显示数量
     * @param search     搜索的内容
     * @param searchType 搜索的类型（0 按监控对象，1 按分组)
     * @return
     * @throws Exception
     */
    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_SEVEN, url = {
        "/clbs/app/risk/riskRank/getFuzzyVehicleOfUser" })
    public JSONObject getFuzzyVehicleList(Integer page, Integer pageSize, String type, String search,
        Integer searchType) {
        if (page != null && pageSize != null) {
            Set<String> filterIds = new HashSet<>();
            //获得黑标有参数设置的车辆ids
            filterIds.addAll(appRiskRankDao.findAllVids());
            //获得其他协议标有参数设置的车辆ids
            filterIds.addAll(appRiskRankDao.findOtherVids());
            //获得京标有参数设置的车辆ids
            filterIds.addAll(appRiskRankDao.findJingStandardVids());
            return getSearchMonitor(page, pageSize, type, search, filterIds, searchType);
        }
        return new JSONObject();
    }

    /**
     * 模糊查询下发了工时传感器轮询的监控对象信息
     * @param page       第几页
     * @param pageSize   每页显示数量
     * @param search     搜索的内容
     * @param searchType 搜索的类型（0 按监控对象，1 按分组)
     * @return
     * @throws Exception
     */
    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_SEVEN, url = {
        "/clbs/app/risk/riskRank/getFuzzyPollingOilOfUser" })
    public JSONObject getFuzzyPollingOilList(Integer page, Integer pageSize, String type, String search,
        Integer searchType) {
        if (page != null && pageSize != null) {
            Set<String> filterIds =
                sensorPollingDao.getPollMonitorIdListBySensorIdAndOwnMonitor(Arrays.asList("0x41", "0x42"), null);
            return getSearchMonitor(page, pageSize, type, search, filterIds, searchType);
        }
        return new JSONObject();
    }

    /**
     * 模糊查询app油耗里程绑定了传感器的监控对象
     * @param page       第几页
     * @param pageSize   每页显示数量
     * @param search     搜索的内容
     * @param searchType 搜索的类型（0 按监控对象，1 按分组)
     * @return
     * @throws Exception
     */
    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_SEVEN, url = {
        "/clbs/app/risk/riskRank/getFuzzyOilSensorOfUser" })
    public JSONObject getFuzzyOilSensorList(Integer page, Integer pageSize, String type, String search,
        Integer searchType) {
        if (page != null && pageSize != null) {
            Set<String> filterIds =
                sensorPollingDao.getPollMonitorIdListBySensorIdAndOwnMonitor(Arrays.asList("0x45", "0x46"), null);
            return getSearchMonitor(page, pageSize, type, search, filterIds, searchType);
        }
        return new JSONObject();
    }

    /**
     * 模糊查询下发了工时传感器轮询的监控对象信息
     * @param page       第几页
     * @param pageSize   每页显示数量
     * @param search     搜索的内容
     * @param searchType 搜索的类型（0 按监控对象，1 按分组)
     * @return
     * @throws Exception
     */
    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_SEVEN, url = {
        "/clbs/app/risk/riskRank/getFuzzyHourPollsOfUser" })
    public JSONObject getFuzzyHourPollsList(Integer page, Integer pageSize, String type, String search,
        Integer searchType) {
        if (page != null && pageSize != null) {
            Set<String> filterIds =
                sensorPollingDao.getPollMonitorIdListBySensorIdAndOwnMonitor(Arrays.asList("0x80", "0x81"), null);
            return getSearchMonitor(page, pageSize, type, search, filterIds, searchType);
        }
        return new JSONObject();
    }

    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_ONE, url = { "/clbs/app/risk/riskRank/getRiskRank" })
    public List<RiskRankResult> getRiskRank(String vehicleIds, String startTime, String endTime, Integer riskType)
        throws Exception {
        List<RiskRankResult> riskRankResults = new ArrayList<>();
        try {
            if (vehicleIds != null && startTime != null && endTime != null) {
                String[] vids = vehicleIds.split(",");
                String start = startTime + " 00:00:00";
                String end = endTime + " 23:59:59";
                Map<String, Integer> dayMap = Date8Utils.getDayMap(startTime, endTime);
                Map<String, String> moniorsMap = new HashMap<>();
                riskRankResults = getAllMonitors(vids, moniorsMap, dayMap);
                SearchResponse searchResponse = adasElasticSearchUtil.getRiskRank(vids, start, end, riskType);
                SearchScrollRequest scroll = new SearchScrollRequest();

                if (searchResponse != null) {
                    scroll.scrollId(searchResponse.getScrollId());
                    JSONObject object = JSONObject.parseObject(String.valueOf(searchResponse));
                    searchResponse.getAggregations().asMap();
                    JSONArray jsonArray =
                        object.getJSONObject("aggregations").getJSONObject("sterms#vehicleId").getJSONArray("buckets");
                    List<RiskRankResult> riskRankResult =
                        assemblyData(jsonArray, moniorsMap, dayMap, "date_histogram#warning_time");
                    Set<RiskRankResult> set = new LinkedHashSet<>(riskRankResult);
                    set.addAll(riskRankResults);
                    return new ArrayList<>(set);
                }
                return riskRankResults;
            }
        } catch (Exception e) {
            throw e;
        }
        return riskRankResults;
    }

    @Override
    public List<RiskRankResult> assemblyData(JSONArray jsonArray, Map<String, String> moniorsMap,
        Map<String, Integer> dayMap, String conditions) {
        List<RiskRankResult> riskRankResults = new ArrayList<>();
        for (Object object : jsonArray) {
            Map<String, Integer> dayToalMap = new LinkedHashMap<>();
            dayToalMap.putAll(dayMap);
            JSONObject jsonObject = (JSONObject) object;
            String id = jsonObject.getString("key");
            int total = jsonObject.getInteger("doc_count");
            JSONArray array = jsonObject.getJSONObject(conditions).getJSONArray("buckets");
            for (Object object1 : array) {
                JSONObject jsonObject1 = (JSONObject) object1;
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                long lt = jsonObject1.getLongValue("key");
                Date date = new Date(lt);
                dayToalMap.put(simpleDateFormat.format(date), jsonObject1.getInteger("doc_count"));
            }
            RiskRankResult riskRankResult = new RiskRankResult();
            riskRankResult.setId(id);
            riskRankResult.setName(moniorsMap.get(id));
            riskRankResult.setRiskToal(total);
            riskRankResult.setDayToal(dayToalMap);
            riskRankResults.add(riskRankResult);
        }
        return riskRankResults;
    }

    @Override
    public List<RiskRankResult> getAllMonitors(String[] vids, Map<String, String> moniorsMap,
        Map<String, Integer> dayMap) throws Exception {
        List<RiskRankResult> riskRankResults = new ArrayList<>();
        Jedis jedis = null;
        List<Response<String>> responseList = new ArrayList<>();
        List<RedisKey> redisKeys = new ArrayList<>();
        for (int i = 0; i < vids.length; i++) {
            redisKeys.add(RedisKeyEnum.MONITOR_INFO.of(vids[i]));
        }
        List<Map<String, String>> mapList =
            RedisHelper.batchGetHashMap(redisKeys, Arrays.asList(new String[] { "id", "name" }));
        for (Map<String, String> response : mapList) {
            RiskRankResult riskRankResult = new RiskRankResult();
            String id = response.get("id");
            String name = response.get("name");
            riskRankResult.setId(id);
            riskRankResult.setName(name);
            riskRankResult.setRiskToal(0);
            Map<String, Integer> map = new HashMap<>();
            map.putAll(dayMap);
            riskRankResult.setDayToal(map);
            riskRankResults.add(riskRankResult);
            moniorsMap.put(id, name);
        }
        return riskRankResults;
    }

    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_ONE, url = {
        "/clbs/app/risk/riskRank/getPercentageOfRank" })
    public PercentageOfRank getPercentageOfRank(String vehicleIds, String startTime, String endTime) throws Exception {
        PercentageOfRank percentage = new PercentageOfRank();
        try {
            if (vehicleIds != null && startTime != null && endTime != null) {
                String[] vids = vehicleIds.split(",");
                String start = startTime + " 00:00:00";
                String end = endTime + " 23:59:59";
                SearchResponse searchResponse = adasElasticSearchUtil.getPercentageOfRank(vids, start, end);
                SearchScrollRequest scroll = new SearchScrollRequest();
                if (searchResponse != null) {
                    scroll.scrollId(searchResponse.getScrollId());
                    JSONObject object = JSONObject.parseObject(String.valueOf(searchResponse));
                    JSONArray jsonArray =
                        object.getJSONObject("aggregations").getJSONObject("lterms#riskType").getJSONArray("buckets");
                    for (Object object1 : jsonArray) {
                        JSONObject jsonObject = (JSONObject) object1;
                        Integer type = jsonObject.getInteger("key");
                        Integer num = jsonObject.getInteger("doc_count");
                        switch (type) {
                            case 1:
                                percentage.setTired(num);
                                break;
                            case 2:
                                percentage.setDistraction(num);
                                break;
                            case 3:
                                percentage.setException(num);
                                break;
                            case 4:
                                percentage.setCrash(num);
                                break;
                            case 5:
                                percentage.setCluster(num);
                                break;
                            case 6:
                                percentage.setIntenseDriving(num);
                                break;
                            default:
                                break;
                        }
                    }
                    return percentage;
                }
            }
        } catch (IOException e) {
            throw e;
        }
        return percentage;
    }

    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_SEVEN, url = {
        "/clbs/app/risk/riskRank/getDefaultMonitorIds" })
    public List<String> getDefaultMonitorIdsSeven(String type, Integer defaultSize, Boolean isFilter) {
        List<String> mids = new ArrayList<>();
        Set<String> allSetVids = new LinkedHashSet<>();
        boolean upDefault = true;
        // 得到用户——分组缓存 创建时间倒序
        List<GroupDTO> groupDTOS =
            userGroupService.getByGroupIdsAndUserId(userService.getCurrentUserUuid(), null).stream()
                .sorted(Comparator.comparing(GroupDTO::getCreateDataTime).reversed()).collect(Collectors.toList());
        if (groupDTOS.size() == 0) {
            return mids;
        }
        if (isFilter) {
            //获得黑标有参数设置的车辆ids
            allSetVids = appRiskRankDao.findAllVids();
            //获得川冀标有参数设置的车辆ids
            allSetVids.addAll(appRiskRankDao.findOtherVids());
            //获得京标有参数设置的车辆ids
            allSetVids.addAll(appRiskRankDao.findJingStandardVids());
        }
        List<RedisKey> redisKeys = new LinkedList<>();
        List<String> sortGroupIdList = new ArrayList<>();
        for (GroupDTO groupDTO : groupDTOS) {
            String groupId = groupDTO.getId();
            redisKeys.add(RedisKeyEnum.GROUP_MONITOR.of(groupId));
            sortGroupIdList.add(groupId);
        }
        Map<String, Set<String>> stringSetMap = RedisHelper.batchGetSetReturnMap(redisKeys);
        List<String> midslist = appRiskRankDao.findMidsBytype(type);
        for (String groupId : sortGroupIdList) {
            Set<String> monitors = stringSetMap.getOrDefault(groupId, new HashSet<>());
            mids = new ArrayList<>(Sets.intersection(new HashSet<>(monitors), new LinkedHashSet<>(midslist)));
            if (mids.size() == 0) {
                continue;
            }
            if (isFilter) {
                mids = new ArrayList<>(Sets.intersection(new LinkedHashSet<>(mids), allSetVids));
            }
            if (mids.size() != 0) {
                return mids;
            }
        }
        if (upDefault && mids.size() > defaultSize) {
            return mids.subList(0, defaultSize);
        }
        return new ArrayList<>();
    }

    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_SEVEN, url = {
        "/clbs/app/risk/riskRank/getAdasMonitorFlag" })
    public JSONObject adasMonitorFlag() {
        JSONObject jsonObject = new JSONObject();
        List<String> vid = getDefaultMonitorIdsSeven("0", 1, true);
        if (vid.size() > 0) {
            jsonObject.put("adasMonitorFlag", true);
        } else {
            jsonObject.put("adasMonitorFlag", false);
        }
        return jsonObject;
    }

    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_TWO,
        url = "/clbs/app/risk/riskRank/getVehicleIsBindRiskDefined")
    public boolean getVehicleIsBindRiskDefined(String vehicleId) {
        //获得黑标有参数设置的车辆ids
        Set<String> allSetVids = appRiskRankDao.findAllVids();
        //获得川冀标有参数设置的车辆ids
        allSetVids.addAll(appRiskRankDao.findOtherVids());
        //获得京标有参数设置的车辆ids
        allSetVids.addAll(appRiskRankDao.findJingStandardVids());
        // Integer riskDefined = riskEventConfigService.getVehicleIsBindRiskDefined(vehicleId);
        return allSetVids.contains(vehicleId);
    }
}


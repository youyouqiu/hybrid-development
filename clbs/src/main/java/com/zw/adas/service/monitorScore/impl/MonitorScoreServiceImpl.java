package com.zw.adas.service.monitorScore.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cb.platform.util.page.PassCloudResultUtil;
import com.github.tobato.fastdfs.domain.conn.FdfsWebServer;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.zw.adas.domain.monitorScore.MonitorAggregateInfo;
import com.zw.adas.domain.monitorScore.MonitorAlarmInfo;
import com.zw.adas.domain.monitorScore.MonitorScore;
import com.zw.adas.domain.monitorScore.MonitorScoreEventInfo;
import com.zw.adas.domain.monitorScore.MonitorScoreInfo;
import com.zw.adas.domain.monitorScore.MonitorScoreQuery;
import com.zw.adas.domain.monitorScore.MonitorScoreResult;
import com.zw.adas.repository.mysql.riskdisposerecord.AdasRiskEventDao;
import com.zw.adas.repository.mysql.riskdisposerecord.MonitorScoreDao;
import com.zw.adas.service.monitorScore.MonitorScoreService;
import com.zw.adas.utils.elasticsearch.AdasElasticSearchUtil;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.commons.ParallelWorker;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.util.LocalDateUtils;
import com.zw.platform.util.StrUtil;
import com.zw.platform.util.common.ComputingUtils;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.platform.util.common.ZipUtil;
import com.zw.platform.util.report.PaasCloudAdasUrlEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class MonitorScoreServiceImpl implements MonitorScoreService {
    private static final Logger logger = LogManager.getLogger(MonitorScoreServiceImpl.class);

    @Autowired
    private MonitorScoreDao monitorScoreDao;

    @Autowired
    private UserService userService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private FdfsWebServer fdfsWebServer;

    @Autowired
    private AdasElasticSearchUtil adasElasticSearchUtil;

    @Autowired
    private AdasRiskEventDao adasRiskEventDao;

    //单个详情导出最大导出数量
    public static final int EXPORT_MAX_SIZE = 5000;

    @Override
    public MonitorScoreResult list(String orgId, int time) {

        RedisKey redisKey = HistoryRedisKeyEnum.ORG_MONITOR_SCORE.of(time + "_" + orgId);
        String data = RedisHelper.getString(redisKey);
        if (data != null) {
            try {
                byte[] bytes = data.getBytes(StandardCharsets.ISO_8859_1);
                return JSON.parseObject(ZipUtil.uncompress(bytes, "utf-8"), MonitorScoreResult.class);
            } catch (Exception e) {
                logger.error("解压缩监控对象评分缓存数据异常", e);
                return getMonitorScoreResult(orgId, time, redisKey);
            }
        }
        return getMonitorScoreResult(orgId, time, redisKey);
    }

    private MonitorScoreResult getMonitorScoreResult(String orgId, int time, RedisKey redisKey) {
        MonitorScoreResult result = new MonitorScoreResult();
        Set<String> orgIdSet = new HashSet<>();
        Map<String, OrganizationLdap> orgMap = getOrganizationLdapMap(orgId, orgIdSet);
        List<MonitorScore> monitorScoreList = monitorScoreDao.getMonitorScoreDataListByGroupIds(orgIdSet, time);
        //查询月没有数据
        if (monitorScoreList.size() == 0) {
            return result;
        }
        //查询月
        MonitorAggregateInfo aggregateInfo = buildData(orgMap, monitorScoreList, time);
        //查询月上月
        MonitorAggregateInfo beforeAggregateInfo = getBeforeMonitorAggregateInfo(orgIdSet, time);
        //查询月上月有数据才计算环比
        if (beforeAggregateInfo != null) {
            //综合得分环比
            aggregateInfo.setScoreRingRatio(
                ComputingUtils.calRingRatio(aggregateInfo.getScore(), beforeAggregateInfo.getScore()));
            //报警数量环比
            aggregateInfo.setAlarmRingRatio(
                ComputingUtils.calRingRatio(aggregateInfo.getAlarmTotal(), beforeAggregateInfo.getAlarmTotal()));
            //百公里环比
            double beforeHundredsAlarmTotal =
                beforeAggregateInfo.getHundredsAlarmTotal() != null ? beforeAggregateInfo.getHundredsAlarmTotal() : 0.0;
            aggregateInfo.setHundredsAlarmRingRatio(
                ComputingUtils.calRingRatio(aggregateInfo.getHundredsAlarmTotal(), beforeHundredsAlarmTotal));
        } else {
            aggregateInfo.setScoreRingRatio("-");
            aggregateInfo.setAlarmRingRatio("-");
            aggregateInfo.setHundredsAlarmRingRatio("-");
        }
        result.setMonitorScoreList(monitorScoreList);
        result.setMonitorAggregateInfo(aggregateInfo);
        try {
            String compressResult = ZipUtil.compress(JSON.toJSONString(result));
            //保存最近三个月的数据
            RedisHelper.setString(redisKey, compressResult, 60 * 60 * 24 * 30 * 3);
        } catch (Exception e) {
            logger.error("压缩监控对象评分缓存数据异常", e);
        }
        return result;
    }

    @Override
    public List<MonitorScore> sort(String orgId, int time, String parameter, boolean isDownSort) {
        Set<String> orgIdSet = new HashSet<>();
        Map<String, OrganizationLdap> orgMap = getOrganizationLdapMap(orgId, orgIdSet);
        List<MonitorScore> monitorScoreList =
            monitorScoreDao.sortByAverageTravelTime(orgIdSet, time, parameter, isDownSort);
        //查询月没有数据
        if (monitorScoreList.size() == 0) {
            return null;
        }
        Map<String, MonitorScore> monitorScoreMap = new HashMap<>();
        Set<String> monitorIds = Sets.newHashSet();
        try {
            for (MonitorScore monitorScore : monitorScoreList) {
                monitorScoreMap.put(monitorScore.getVehicleId(), monitorScore);
                monitorIds.add(monitorScore.getVehicleId());
            }
            setMonitorInfo(orgMap, monitorIds, monitorScoreMap, false);
        } catch (Exception e) {
            logger.error("查询监控对象评分基础信息数据异常", e);
        }
        return monitorScoreList;
    }

    @Override
    public List<MonitorScoreInfo> scoreInfoList(Set<String> vehicleIdSet, int time) {
        List<MonitorScoreInfo> monitorScoreInfoList = monitorScoreDao.getMonitorScoreInfoList(vehicleIdSet, time);
        Map<String, String> orgNameMap = userService.getCurrentUserOrgIdOrgNameMap();
        for (MonitorScoreInfo info : monitorScoreInfoList) {
            setDateTime(info);
            info.setGroupName(orgNameMap.get(info.getGroupId()));
            setAlarmRatioStr(info);
            info.setScoreStr(Math.round(info.getScore()) + "");
            setScoreRingRatioStr(info);
            setHundredsAlarmRingRatioStr(info);
            if (info.getVehiclePhoto() != null && !"".equals(info.getVehiclePhoto())) {
                info.setVehiclePhotoPath(info.getVehiclePhoto());
                info.setVehiclePhoto(fdfsWebServer.getWebServerUrl() + info.getVehiclePhoto());
            }
        }
        return monitorScoreInfoList;
    }

    @Override
    public MonitorScoreInfo scoreInfo(String vehicleId, int time) {
        MonitorScoreInfo monitorScoreInfo = monitorScoreDao.getMonitorScoreInfo(vehicleId, time);
        if (monitorScoreInfo == null) {
            return null;
        }
        setDateTime(monitorScoreInfo);
        if (monitorScoreInfo.getGroupId() != null) {
            monitorScoreInfo.setGroupName(organizationService.getOrgNameByUuid(monitorScoreInfo.getGroupId()));
        }
        if (monitorScoreInfo.getVehiclePhoto() != null && !"".equals(monitorScoreInfo.getVehiclePhoto())) {
            monitorScoreInfo.setVehiclePhotoPath(monitorScoreInfo.getVehiclePhoto());
            monitorScoreInfo.setVehiclePhoto(fdfsWebServer.getWebServerUrl() + monitorScoreInfo.getVehiclePhoto());
        }
        Map<String, Integer> alarmMap = JSON.parseObject(monitorScoreInfo.getEventInfos(), Map.class);
        monitorScoreInfo.setAlarmMap(alarmMap);
        return monitorScoreInfo;
    }

    private void setDateTime(MonitorScoreInfo monitorScoreInfo) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        if (monitorScoreInfo.getLicenseIssuanceFormDate() != null) {
            monitorScoreInfo.setLicenseIssuanceDate(formatter.format(monitorScoreInfo.getLicenseIssuanceFormDate()));
        }
        if (monitorScoreInfo.getRegistrationEndFormDate() != null) {
            monitorScoreInfo.setRegistrationEndDate(formatter.format(monitorScoreInfo.getRegistrationEndFormDate()));
        }
    }

    @Override
    public Map<String, Object> monitorAlarmInfo(MonitorScoreQuery query) {
        Map<String, Object> resultMap = new HashMap<>();
        Map<String, Object> alarmMap = adasElasticSearchUtil.selectMonitorAlarmIds(query);
        Map<String, String> param = new HashMap<>();
        List<MonitorAlarmInfo> monitorAlarmInfoList = new ArrayList<>();
        if (alarmMap.get("ids") != null && ((List<byte[]>) alarmMap.get("ids")).size() != 0) {
            param.put("eventMaps", JSON.toJSONString(getScoreEventMaps()));
            param.put("alarmIdsStr", JSON.toJSONString(alarmMap.get("ids")));
            String sendResult =
                HttpClientUtil.send(PaasCloudAdasUrlEnum.GET_MONITOR_SCORE_RISK_EVENT_FROM_HBASE, param);
            monitorAlarmInfoList = PassCloudResultUtil.getListResult(sendResult, MonitorAlarmInfo.class);
        }

        resultMap.put("searchAfter", alarmMap.get("searchAfter"));
        resultMap.put("monitorAlarmInfoList", monitorAlarmInfoList);
        return resultMap;
    }

    private void alarmInfoMapFromHbase(Map<String, List<MonitorAlarmInfo>> alarmInfoMap, List<byte[]> alarmIds) {
        Map<String, String> eventMaps = getScoreEventMaps();
        Map<String, Integer> indexMap = new HashMap<>();
        ParallelWorker.invoke(alarmIds, 100, list -> hbaseDataBuild(alarmInfoMap, eventMaps, indexMap, list));
    }

    private void hbaseDataBuild(Map<String, List<MonitorAlarmInfo>> alarmInfoMap, Map<String, String> eventMaps,
        Map<String, Integer> indexMap, List<byte[]> alarmIdList) {
        List<MonitorAlarmInfo> monitorAlarmInfoList = new ArrayList<>();
        Map<String, String> param = new HashMap<>();
        if (alarmIdList.size() != 0) {
            param.put("eventMaps", JSON.toJSONString(eventMaps));
            param.put("alarmIdsStr", JSON.toJSONString(alarmIdList));
            String sendResult =
                HttpClientUtil.send(PaasCloudAdasUrlEnum.GET_MONITOR_SCORE_RISK_EVENT_FROM_HBASE, param);
            monitorAlarmInfoList = PassCloudResultUtil.getListResult(sendResult, MonitorAlarmInfo.class);
        }
        for (MonitorAlarmInfo monitorAlarmInfo : monitorAlarmInfoList) {
            String vehicleId = monitorAlarmInfo.getVehicleId();
            if (StrUtil.isBlank(vehicleId)) {
                logger.info("异常数据空车辆id了" + JSONObject.toJSONString(monitorAlarmInfo));
                continue;
            }
            List<MonitorAlarmInfo> list = alarmInfoMap.get(vehicleId);
            if (list == null) {
                list = new ArrayList<>();
                indexMap.put(vehicleId, 0);
            }
            indexMap.put(vehicleId, indexMap.get(vehicleId) + 1);
            monitorAlarmInfo.setIndex(indexMap.get(vehicleId));
            list.add(monitorAlarmInfo);
            alarmInfoMap.put(vehicleId, list);
        }
    }

    public Map<String, String> getScoreEventMaps() {
        Map<String, String> eventMaps = new HashMap<>();
        List<Map<String, String>> events = adasRiskEventDao.getRiskEventMap();
        for (Map<String, String> event : events) {
            eventMaps.put(event.get("functionId"), event.get("riskEvent") + "(" + event.get("riskType") + ")");
        }
        return eventMaps;
    }

    @Override
    public List<MonitorScoreEventInfo> eventTypeList(String vehicleId, int time) {
        List<String> timeList = conversionTime(time, true);
        Set<String> vehicleIdSet = new HashSet<>();
        vehicleIdSet.add(vehicleId);
        return eventTypeBuild(timeList.get(0), timeList.get(1), vehicleIdSet).get(vehicleId);
    }

    @Override
    public Map<String, List<MonitorScoreEventInfo>> eventTypeMap(Set<String> vehicleIdSet, int time) {
        List<String> timeList = conversionTime(time, true);
        return eventTypeBuild(timeList.get(0), timeList.get(1), vehicleIdSet);
    }

    @Override
    public List<MonitorScore> exportList(String orgId, int time) {
        Set<String> orgIdSet = new HashSet<>();
        Map<String, OrganizationLdap> orgMap = getOrganizationLdapMap(orgId, orgIdSet);
        List<MonitorScore> monitorScoreList = monitorScoreDao.getMonitorScoreDataListByGroupIds(orgIdSet, time);
        monitorScoreList = monitorScoreList.stream().sorted(Comparator.comparing(MonitorScore::getScore).reversed())
            .collect(Collectors.toList());
        //查询月没有数据
        if (monitorScoreList.isEmpty()) {
            return null;
        }
        Map<String, MonitorScore> monitorScoreMap = new HashMap<>();
        Set<String> monitorIds = Sets.newHashSet();
        try {
            int index = 0;
            for (MonitorScore monitorScore : monitorScoreList) {
                monitorScore.setIndex(++index);
                monitorIds.add(monitorScore.getVehicleId());
                monitorScoreMap.put(monitorScore.getVehicleId(), monitorScore);
            }
            setMonitorInfo(orgMap, monitorIds, monitorScoreMap, true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return monitorScoreList;
    }

    private void setMonitorInfo(Map<String, OrganizationLdap> orgMap, Set<String> monitorIds,
        Map<String, MonitorScore> monitorScoreMap, boolean isExport) {
        Map<String, VehicleDTO> monitorMap =
            VehicleUtil.batchGetVehicleInfosFromRedis(monitorIds, Lists.newArrayList("vehiclePurposeName", "name"));
        Set<Map.Entry<String, VehicleDTO>> entries = monitorMap.entrySet();
        for (Map.Entry<String, VehicleDTO> entry : entries) {
            String monitorId = entry.getKey();
            if (StringUtils.isBlank(monitorId)) {
                continue;
            }
            MonitorScore monitorScore = monitorScoreMap.get(monitorId);
            if (isExport) {
                monitorScore.setScore(Double.parseDouble(Math.round(monitorScore.getScore()) + ""));
            }
            VehicleDTO value = entry.getValue();
            if (value == null) {
                monitorScore.setVehicleName("-");
                monitorScore.setPurposeCategoryName("-");
                monitorScore.setGroupName(orgMap.get(monitorScore.getGroupId()).getName());
                continue;
            }
            monitorScore.setVehicleName(value.getName());
            monitorScore.setPurposeCategoryName(value.getVehiclePurposeName());
            monitorScore.setGroupName(orgMap.get(monitorScore.getGroupId()).getName());
        }
    }

    public Map<String, List<MonitorScoreEventInfo>> eventTypeBuild(String startTime, String endTime,
        Set<String> vehicleIdSet) {
        Map<String, List<MonitorScoreEventInfo>> result = new HashMap<>();
        SearchResponse response = adasElasticSearchUtil.getMonitorScoreSearch(startTime, endTime, vehicleIdSet);
        if (response != null) {
            Map<String, String> eventMaps = new HashMap<>();
            Map<String, String> eventTypeMaps = new HashMap<>();
            List<Map<String, String>> events = adasRiskEventDao.getRiskEventMap();
            for (Map<String, String> event : events) {
                eventMaps.put(event.get("functionId"), event.get("riskEvent"));
                eventTypeMaps.put(event.get("functionId"), event.get("riskType"));
            }
            Terms terms = response.getAggregations().get("vehicleId");
            Terms terms1;
            for (Terms.Bucket alarmBucket : terms.getBuckets()) {
                List<MonitorScoreEventInfo> list = new ArrayList<>();
                int index = 0;
                terms1 = alarmBucket.getAggregations().get("eventType");
                for (Terms.Bucket typeBucket : terms1.getBuckets()) {
                    MonitorScoreEventInfo eventInfo = new MonitorScoreEventInfo();
                    eventInfo.setEventName(eventMaps.get(typeBucket.getKeyAsString()));
                    eventInfo.setRiskName(eventTypeMaps.get(typeBucket.getKeyAsString()));
                    eventInfo.setTotal((int) typeBucket.getDocCount());
                    eventInfo.setFunctionId(typeBucket.getKeyAsString());
                    eventInfo.setIndex(++index);
                    list.add(eventInfo);
                }
                result.put(alarmBucket.getKeyAsString(), list);
            }
        }
        return result;
    }

    @Override
    public List<MonitorAlarmInfo> monitorAlarmInfoList(String vehicleId, int time) {
        List<MonitorAlarmInfo> monitorAlarmInfoList = new ArrayList<>();
        List<byte[]> allAlarmIds = adasElasticSearchUtil.selectAlarmIds(vehicleId, time, EXPORT_MAX_SIZE);
        Map<String, String> param = new HashMap<>();
        if (allAlarmIds != null && allAlarmIds.size() != 0) {
            param.put("eventMaps", JSON.toJSONString(getScoreEventMaps()));
            param.put("alarmIdsStr", JSON.toJSONString(allAlarmIds));
            String sendResult =
                HttpClientUtil.send(PaasCloudAdasUrlEnum.GET_MONITOR_SCORE_RISK_EVENT_FROM_HBASE, param);
            monitorAlarmInfoList = PassCloudResultUtil.getListResult(sendResult, MonitorAlarmInfo.class);
        }
        return monitorAlarmInfoList;
    }

    @Override
    public Map<String, List<MonitorAlarmInfo>> monitorAlarmInfoMap(Set<String> vehicleIdSet, int time) {
        Map<String, List<MonitorAlarmInfo>> result = new ConcurrentHashMap<>();
        List<String> vehicleIdList = new ArrayList<>(vehicleIdSet);
        List<byte[]> allAlarmIds = new ArrayList<>();
        ParallelWorker.invoke(vehicleIdList, 100, list -> buildAllArmIds(list, time, allAlarmIds));
        if (CollectionUtils.isEmpty(allAlarmIds)) {
            return result;
        }

        alarmInfoMapFromHbase(result, allAlarmIds);
        return result;
    }

    private void buildAllArmIds(List<String> vehicleIdList, int time, List<byte[]> allAlarmIds) {
        for (String vehicleId : vehicleIdList) {
            allAlarmIds.addAll(adasElasticSearchUtil.selectAlarmIds(vehicleId, time, EXPORT_MAX_SIZE));
        }
    }

    private MonitorAggregateInfo getBeforeMonitorAggregateInfo(Set<String> orgIdSet, int time) {
        int beforeTime = getBeforeTime(time);
        MonitorAggregateInfo before = new MonitorAggregateInfo();
        List<MonitorScore> beforeMonitorScoreList =
            monitorScoreDao.getMonitorScoreDataListByGroupIds(orgIdSet, beforeTime);
        if (beforeMonitorScoreList.size() == 0) {
            return null;
        }
        float scoreTotal = 0;
        float travelMileTotal = 0;
        for (MonitorScore monitorScore : beforeMonitorScoreList) {
            //所有综合得分
            scoreTotal += monitorScore.getScore();
            //所有报警数
            before.setAlarmTotal(before.getAlarmTotal() + monitorScore.getAlarmTotal());
            //所有行驶里程
            travelMileTotal += monitorScore.getTravelMile();
        }
        before.setMonitorSize(beforeMonitorScoreList.size());
        before.setScore(keepTwoFloat(scoreTotal / beforeMonitorScoreList.size(), 2));
        if (travelMileTotal != 0) {
            before.setHundredsAlarmTotal(keepTwoFloat(before.getAlarmTotal() / travelMileTotal * 100, 2));
        }
        return before;
    }

    private MonitorAggregateInfo buildData(Map<String, OrganizationLdap> orgMap, List<MonitorScore> monitorScoreList,
        int time) {
        MonitorAggregateInfo result = new MonitorAggregateInfo();
        Set<String> monitorIds = Sets.newHashSet();
        Map<String, MonitorScore> monitorScoreMap = new HashMap<>();
        try {
            double scoreTotal = 0;
            long travelTimeTotal = 0;
            double travelMileTotal = 0;
            for (MonitorScore monitorScore : monitorScoreList) {
                monitorIds.add(monitorScore.getVehicleId());
                monitorScoreMap.put(monitorScore.getVehicleId(), monitorScore);
                //所有综合得分
                scoreTotal += monitorScore.getScore();
                //统计得分区间
                setScoreDistribution(result, monitorScore);
                //所有报警数
                result.setAlarmTotal(result.getAlarmTotal() + monitorScore.getAlarmTotal());
                //所有行驶时长
                travelTimeTotal += monitorScore.getTravelTime();
                //所有行驶里程
                travelMileTotal += monitorScore.getTravelMile();
            }
            result.setMonitorSize(monitorScoreList.size());
            result.setScore(keepTwoFloat(scoreTotal / monitorScoreList.size(), 2));
            result.setTravelTime(ComputingUtils.numberDataDis(travelTimeTotal));
            result.setAverageTravelTime(
                LocalDateUtils.formatDuring(travelTimeTotal * 1000 / monitorScoreList.size() / getMonthDays(time)));
            if (travelMileTotal != 0) {
                result.setHundredsAlarmTotal(keepTwoFloat(result.getAlarmTotal() / travelMileTotal * 100, 2));
            } else {
                result.setHundredsAlarmTotal(0.0);
            }
            if (travelTimeTotal != 0) {
                result.setTravelSpeed(keepTwoFloat(travelMileTotal / (travelTimeTotal / 3600.0), 1));
            } else {
                result.setTravelSpeed(0.0);
            }
            setMonitorInfo(orgMap, monitorIds, monitorScoreMap, false);
            result.setScoreDistributionStr(compare(result));
        } catch (Exception e) {
            logger.error("查询监控对象评分基础信息异常", e);
        }
        return result;
    }

    private String compare(MonitorAggregateInfo result) {
        Map<String, Integer> integerMap = result.getScoreDistribution();
        if (integerMap.size() > 0) {
            List<Map.Entry<String, Integer>> list = new LinkedList<>(integerMap.entrySet());
            list.sort((o1, o2) -> {
                int compare = (o1.getValue()).compareTo(o2.getValue());
                return -compare;
            });
            StringBuilder buffer = new StringBuilder();
            int flag = 0;
            for (Map.Entry<String, Integer> entry : list) {
                if (entry.getValue() > flag) {
                    buffer.append(entry.getKey()).append(",");
                    flag = entry.getValue();
                }
            }
            return buffer.toString().substring(0, buffer.length() - 1);
        }
        return "";
    }

    private Map<String, OrganizationLdap> getOrganizationLdapMap(String orgId, Set<String> orgIdSet) {
        Map<String, OrganizationLdap> orgMap = new HashMap<>();
        List<OrganizationLdap> orgChild = organizationService.getOrgListByUuid(orgId);
        if (orgChild == null) {
            return orgMap;
        }
        for (OrganizationLdap child : orgChild) {
            orgIdSet.add(child.getUuid());
            orgMap.put(child.getUuid(), child);
        }
        return orgMap;
    }

    private void setScoreDistribution(MonitorAggregateInfo info, MonitorScore monitorScore) {
        //得分区间
        String interval = "";
        if (monitorScore.getScore() < 20) {
            interval = "0-20";
        }
        if (monitorScore.getScore() >= 20 && monitorScore.getScore() < 40) {
            interval = "20-40";
        }
        if (monitorScore.getScore() >= 40 && monitorScore.getScore() < 60) {
            interval = "40-60";
        }
        if (monitorScore.getScore() >= 60 && monitorScore.getScore() < 80) {
            interval = "60-80";
        }
        if (monitorScore.getScore() >= 80) {
            interval = "80-100";
        }
        Integer size = info.getScoreDistribution().get(interval);
        if (size == null) {
            size = 0;
        }
        info.getScoreDistribution().put(interval, size + 1);
    }

    //获得上月时间格式(201910)
    private int getBeforeTime(int time) {
        int beforeTime = time - 1;
        if (String.valueOf(beforeTime).endsWith("00")) {
            beforeTime = (beforeTime - 100) + 12;
        }
        return beforeTime;
    }

    private double keepTwoFloat(double number, int scale) {
        DecimalFormat decimalFormat = new DecimalFormat();// 数字格式化类 保留两位小数
        decimalFormat.setMaximumFractionDigits(scale);
        decimalFormat.setGroupingSize(0);
        decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
        return Double.parseDouble(decimalFormat.format(number));
    }

    //time时间格式201910
    private int getMonthDays(int time) {
        int month = Integer.parseInt(String.valueOf(time).substring(4));
        return DateUtil.getMonthDays(time / 100, month);
    }

    //time时间格式201910
    @Override
    public List<String> conversionTime(int time, boolean type) {
        List<String> list = new ArrayList<>();
        String timeStr = String.valueOf(time);
        if (type) {
            list.add(timeStr.substring(0, 4) + "-" + timeStr.substring(4, 6) + "-01 00:00:00");
            list.add(timeStr.substring(0, 4) + "-" + timeStr.substring(4, 6) + "-" + getMonthDays(time) + " 23:59:59");
        } else {
            list.add(timeStr.substring(0, 4) + "-" + timeStr.substring(4, 6) + "-01");
            list.add(timeStr.substring(0, 4) + "-" + timeStr.substring(4, 6) + "-" + getMonthDays(time));
        }
        return list;
    }

    @Override
    public void setAlarmRatioStr(MonitorScoreInfo monitorScoreInfo) {
        String alarmRatio = monitorScoreInfo.getAlarmRingRatio();
        if ("-".equals(alarmRatio)) {
            return;
        }
        if (!"0.00".equals(alarmRatio)) {
            if (alarmRatio.startsWith("-")) {
                monitorScoreInfo.setAlarmRingRatioStr("触发报警数下降" + alarmRatio.substring(1) + "%");
                monitorScoreInfo.setAlarmRingRatio("下降" + alarmRatio.substring(1) + "%");
                return;
            }
            monitorScoreInfo.setAlarmRingRatioStr("触发报警数上升" + alarmRatio + "%");
            monitorScoreInfo.setAlarmRingRatio("上升" + alarmRatio + "%");
            return;
        }
        monitorScoreInfo.setAlarmRingRatioStr("触发报警数与上月持平");
        monitorScoreInfo.setAlarmRingRatio("持平");
    }

    @Override
    public void setHundredsAlarmRingRatioStr(MonitorScoreInfo monitorScoreInfo) {
        String hundredsAlarmRingRatio = monitorScoreInfo.getHundredsAlarmRingRatio();
        if ("-".equals(hundredsAlarmRingRatio)) {
            return;
        }
        if (!"0.00".equals(hundredsAlarmRingRatio)) {
            if (hundredsAlarmRingRatio.startsWith("-")) {
                monitorScoreInfo.setHundredsAlarmRingRatio("下降" + hundredsAlarmRingRatio.substring(1) + "%");
                return;
            }
            monitorScoreInfo.setHundredsAlarmRingRatio("上升" + hundredsAlarmRingRatio + "%");
            return;
        }
        monitorScoreInfo.setHundredsAlarmRingRatio("持平");
    }

    @Override
    public void setScoreRingRatioStr(MonitorScoreInfo monitorScoreInfo) {
        String scoreRatio = monitorScoreInfo.getScoreRingRatio();
        if ("-".equals(scoreRatio)) {
            return;
        }
        if (!"0.00".equals(scoreRatio)) {
            if (scoreRatio.startsWith("-")) {
                monitorScoreInfo.setScoreRingRatioStr("监控对象综合得分下降" + scoreRatio.substring(1) + "%,请加强管理力度！");
                return;
            }
            monitorScoreInfo.setScoreRingRatioStr("监控对象综合得分上升" + scoreRatio + "%,请继续加强管理力度！");
            return;
        }
        monitorScoreInfo.setScoreRingRatioStr("监控对象综合得分与上月持平,请继续加强管理力度！");
    }

}

package com.zw.adas.service.supersive.impl;

import com.alibaba.fastjson.JSON;
import com.cb.platform.util.page.PassCloudResultUtil;
import com.zw.adas.domain.enums.AdasRiskEventEnum;
import com.zw.adas.domain.leardboard.AdasAlarmAnalysisData;
import com.zw.adas.domain.leardboard.AdasAlarmTimesData;
import com.zw.adas.domain.leardboard.AdasEventData;
import com.zw.adas.domain.leardboard.AdasRiskData;
import com.zw.adas.domain.riskManagement.AdasRiskItem;
import com.zw.adas.domain.riskManagement.param.AdasRiskBattleParam;
import com.zw.adas.domain.riskManagement.show.AdasDriverShow;
import com.zw.adas.domain.riskManagement.show.AdasEventShow;
import com.zw.adas.domain.riskManagement.show.AdasRiskShow;
import com.zw.adas.repository.mysql.modules.AdasRiskManageDao;
import com.zw.adas.repository.mysql.riskdisposerecord.AdasRiskEventDao;
import com.zw.adas.service.riskdisposerecord.AdasRiskService;
import com.zw.adas.service.supersive.AdasRiskManageIntelligenceService;
import com.zw.adas.utils.elasticsearch.AdasElasticSearchUtil;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.domain.ProfessionalDO;
import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.platform.basic.repository.NewProfessionalsDao;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.multimedia.Photograph;
import com.zw.platform.domain.riskManagement.RiskEvent;
import com.zw.platform.repository.vas.RiskEventDao;
import com.zw.platform.service.realTimeVideo.VideoOrderSendService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.Reflections;
import com.zw.platform.util.StrUtil;
import com.zw.platform.util.common.Date8Utils;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.MonitorUtils;
import com.zw.platform.util.paas.PaasCloudUrlUtil;
import com.zw.platform.util.privilege.UserPrivilegeUtil;
import com.zw.platform.util.report.PaasCloudAdasUrlEnum;
import com.zw.platform.util.report.PaasCloudHBaseAccessEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdasRiskManageIntelligenceServiceImpl implements AdasRiskManageIntelligenceService {
    private static final Logger log = LogManager.getLogger(AdasRiskManageIntelligenceServiceImpl.class);

    @Value("${adas.mediaServer}")
    private String mediaServer;

    @Value("${system.ssl.enable}")
    private boolean sslEnabled;

    @Autowired
    private UserPrivilegeUtil userPrivilegeUtil;

    @Autowired
    private AdasElasticSearchUtil elasticSearchUtil;

    @Autowired
    private AdasRiskManageDao riskManageDao;

    @Autowired
    private RiskEventDao riskEventDao;

    @Autowired
    private NewProfessionalsDao newProfessionalsDao;

    @Autowired
    VideoOrderSendService videoOrderSendService;

    @Autowired
    LogSearchService logSearchService;

    @Autowired
    AdasRiskService adasRiskService;

    @Autowired
    private AdasRiskEventDao adasRiskEventDao;

    private static final String HOUR_FORMAT = "yyyyMMddHH";

    /**
     * 已处理
     */
    private static final String IS_DEAL = "6";

    @Override
    public AdasAlarmAnalysisData getAlarmAnalysisData() throws IOException {
        Set<String> set = userPrivilegeUtil.getCurrentUserVehicles();
        SearchResponse searchResponse = elasticSearchUtil.alarmAnalysisData(set);
        //获得风险事件的相关数据list集合
        List<AdasEventData> eventDataList = getEventDataList(searchResponse);
        AdasAlarmAnalysisData alarmAnalysisData = new AdasAlarmAnalysisData();
        AdasRiskData crashData = new AdasRiskData("crash");
        AdasRiskData exceptionData = new AdasRiskData("exception");
        AdasRiskData distractionData = new AdasRiskData("distraction");
        AdasRiskData tiredData = new AdasRiskData("tired");
        AdasRiskData acuteData = new AdasRiskData("acute");
        //获取所有的风险类型和对应的functionId 用于下面数据的组装
        Map<String, Set<String>> riskEventMaps = initAdasRiskEvent();
        //风险事件总数
        long total = 0;
        //碰撞
        long crash = 0;
        //异常
        long exception = 0;
        //分心
        long distraction = 0;
        //疲劳
        long tired = 0;
        //激烈驾驶
        long acute = 0;
        long dealed = 0;
        long undeal = 0;
        String type = null;
        for (AdasEventData eventData : eventDataList) {
            total += eventData.getTotal();
            dealed += eventData.getDealed();
            undeal += eventData.getUndeal();
            for (Map.Entry<String, Set<String>> entry : riskEventMaps.entrySet()) {
                if (entry.getValue().contains(eventData.getEventId())) {
                    type = entry.getKey();
                    break;
                }
            }
            if ("crash".equals(type)) {
                crash += eventData.getTotal();
                assemblyRiskData(eventData, crashData);
            } else if ("exception".equals(type)) {
                exception += eventData.getTotal();
                assemblyRiskData(eventData, exceptionData);
            } else if ("distraction".equals(type)) {
                distraction += eventData.getTotal();
                assemblyRiskData(eventData, distractionData);
            } else if ("tired".equals(type)) {
                tired += eventData.getTotal();
                assemblyRiskData(eventData, tiredData);
            } else if ("acute".equals(type)) {
                acute += eventData.getTotal();
                assemblyRiskData(eventData, acuteData);
            }
        }
        crashData.setTotal(crash);
        exceptionData.setTotal(exception);
        distractionData.setTotal(distraction);
        tiredData.setTotal(tired);
        acuteData.setTotal(acute);
        List<AdasRiskData> list = new ArrayList<>();
        list.add(crashData);
        list.add(exceptionData);
        list.add(distractionData);
        list.add(tiredData);
        list.add(acuteData);
        alarmAnalysisData.setTotal(total);
        alarmAnalysisData.setDealed(dealed);
        alarmAnalysisData.setUndeal(undeal);
        alarmAnalysisData.setRiskDataList(list);
        return alarmAnalysisData;
    }

    /**
     * 获取所有的各种风险类型和所对应的functionId
     * @return map
     */
    private Map<String, Set<String>> initAdasRiskEvent() {
        Map<String, Set<String>> riskEventMaps = new HashMap<>();
        List<Map<String, Object>> eventList = adasRiskEventDao.findAllEvent();
        Set<String> eventIds;
        String name;
        for (Map<String, Object> map : eventList) {
            name = map.get("riskType").toString();
            eventIds = riskEventMaps.get(AdasRiskEventEnum.getTypeByName(name));
            if (eventIds != null) {
                eventIds.add(map.get("eventId").toString());
            } else {
                eventIds = new HashSet<>();
                eventIds.add(map.get("eventId").toString());
            }
            riskEventMaps.put(AdasRiskEventEnum.getTypeByName(name), eventIds);
        }
        return riskEventMaps;
    }

    private List<AdasEventData> getEventDataList(SearchResponse searchResponse) {
        Terms terms = searchResponse.getAggregations().get("eventType");
        Terms terms1;
        List<AdasEventData> eventDataList = new ArrayList<>();
        for (Terms.Bucket bucket : terms.getBuckets()) {
            AdasEventData eventData = new AdasEventData();
            eventData.setEventId(String.valueOf(bucket.getKey()));
            eventData.setTotal(bucket.getDocCount());
            terms1 = bucket.getAggregations().get("status");
            for (Terms.Bucket bucket1 : terms1.getBuckets()) {
                long number = Long.parseLong(String.valueOf(bucket1.getDocCount()));
                if (IS_DEAL.equals(String.valueOf(bucket1.getKey()))) {
                    eventData.setDealed(eventData.getDealed() + number);
                } else {
                    eventData.setUndeal(eventData.getUndeal() + number);
                }
            }
            eventDataList.add(eventData);
        }
        return eventDataList;
    }

    private void assemblyRiskData(AdasEventData eventData, AdasRiskData riskData) {
        long dealed = eventData.getDealed() + riskData.getDealed();
        long undeal = eventData.getUndeal() + riskData.getUndeal();
        riskData.setDealed(dealed);
        riskData.setUndeal(undeal);
    }

    @Override
    public List<AdasAlarmTimesData> getAlarmTimesData() throws Exception {
        // boolean isReady = true;
        LocalDateTime dateTime = LocalDateTime.now();
        // if (!OrgShowUtils.orgLbJobIsDone(dateTime)) {
        //     isReady = false;
        // }
        // List<AdasAlarmTimesData> historyAlarmTimes = getHistoryAlarmTimes(dateTime);
        // List<AdasAlarmTimesData> nowAlarmTimes = getNowAlarmTimes(dateTime, isReady);
        return getHistoryAlarmTimes(dateTime);
    }

    @Override
    public List<AdasRiskShow> getRisks(AdasRiskBattleParam rbp) {
        //获取当前用户权限下的车辆集合
        Set<String> vehicleList = userPrivilegeUtil.getCurrentUserVehicles();
        //获取当天未处理的报警 riskId
        List<String> riskIdList = elasticSearchUtil.getTodayUntreatedRisks(vehicleList, rbp);
        //得到当前分页的idList
        List<String> riskIds = riskIdList.stream()
                .skip(0)
                .limit(rbp.getPageSize())
                .collect(Collectors.toList());
        if (riskIds.isEmpty()) {
            return Collections.emptyList();
        }
        //从HBase获取报警数据， 和从redis中获取分组数据，组装报警所有数据并返回
        return getRiskList(riskIds);
    }

    private List<AdasRiskShow> getRiskList(List<String> riskIds) {
        final List<AdasRiskItem> risks = adasRiskService.getRiskList(riskIds);
        final Set<String> vehicleIds = risks.stream().map(AdasRiskItem::getVehicleId).collect(Collectors.toSet());
        final Map<String, VehicleDTO> vehicleMap =
                MonitorUtils.getVehicleMap(vehicleIds, "id", "vehiclePurposeName", "orgName");
        return risks.stream().map(o -> {
            final VehicleDTO vehicleDTO = vehicleMap.get(o.getVehicleId());
            final AdasRiskShow adasRiskShow = new AdasRiskShow();
            adasRiskShow.setVehicleId(o.getVehicleId());
            adasRiskShow.setBrand(o.getBrand());
            adasRiskShow.setRiskId(o.getId());
            adasRiskShow.setRiskLevel(Integer.valueOf(o.getRiskLevel()));
            adasRiskShow.setRiskType(o.getRiskType());
            adasRiskShow.setWarningTime(DateUtil.getLongToDateStr(o.getWarningTime(), DateUtil.DATE_FORMAT_SHORT));
            adasRiskShow.setVideoFlag(o.getVideoFlag());
            adasRiskShow.setPicFlag(o.getPicFlag());
            adasRiskShow.setStatus(o.getStatus());
            adasRiskShow.setRiskNumber(o.getRiskNumber());
            adasRiskShow.setGroupName(vehicleDTO.getOrgName());
            adasRiskShow.setVehiclePurpose(vehicleDTO.getVehiclePurposeName());
            return adasRiskShow;
        }).collect(Collectors.toList());
    }

    @Override
    public List<AdasEventShow> getEvents(String riskId) {
        Map<String, String> eventMap = getEventRiskTypeMap();
        List<String> eventIds = elasticSearchUtil.esGetRiskEventIdByRiskIds(Collections.singletonList(riskId));
        Map<String, String> param = new HashMap<>();
        param.put("eventMap", JSON.toJSONString(eventMap));
        param.put("eventIds", JSON.toJSONString(eventIds));
        String sendResult = HttpClientUtil.send(PaasCloudAdasUrlEnum.GET_RISK_LIST, param);
        List<AdasEventShow> riskList = PassCloudResultUtil.getListResult(sendResult, AdasEventShow.class);
        if (CollectionUtils.isNotEmpty(riskList)) {
            String vehicleId = riskList.get(0).getVehicleId();
            Map<String, String> monitorInfo =
                RedisHelper.getHashMap(RedisKeyEnum.MONITOR_INFO.of(vehicleId), Arrays.asList("deviceType"));
            if (monitorInfo == null) {
                return riskList;
            }

            String deviceType = monitorInfo.get("deviceType");
            if (StringUtils.isEmpty(deviceType)) {
                return riskList;
            }
            for (AdasEventShow show : riskList) {
                show.setEventProtocolType(show.getProtocolType());
                show.setDeviceType(Integer.valueOf(deviceType));
            }
        }
        return riskList;

    }


    @Override
    public Map<String, List<AdasDriverShow>> getDrivers(String riskId, String vehicleId) {
        Map<String, List<AdasDriverShow>> result = new HashMap<>();
        List<AdasDriverShow> riskDrivers = getRiskDrivers(riskId);
        List<AdasDriverShow> platFormDrivers = getPlatFormDrivers(vehicleId);
        if (!platFormDrivers.isEmpty()) {
            riskDrivers.addAll(platFormDrivers);
        }
        result.put("riskDrivers", riskDrivers);
        return result;
    }

    private List<AdasDriverShow> getRiskDrivers(String riskId) {
        List<AdasDriverShow> riskDrivers = new ArrayList<>();
        AdasRiskItem driverMark = getRiskDriver(riskId);
        if (driverMark != null) {
            String certificationCode = driverMark.getCertificationCode();
            String driverNames = driverMark.getDriverNames();
            String icTypeId = newProfessionalsDao.getIcTypeId();
            ProfessionalDO professional =
                newProfessionalsDao.findByCarNumberNameAndPositionType(certificationCode, driverNames, icTypeId);
            if (professional != null) {
                if (sslEnabled) {
                    riskDrivers.add(AdasDriverShow.getInstance(professional, "/mediaserver"));
                } else {
                    riskDrivers.add(AdasDriverShow.getInstance(professional, mediaServer));
                }
            }
        }
        return riskDrivers;
    }

    private AdasRiskItem getRiskDriver(String riskId) {
        Map<String, String> params = new HashMap<>(2);
        params.put("riskId", riskId);
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_RISK_DRIVER, params);
        return PaasCloudUrlUtil.getResultData(str, AdasRiskItem.class);
    }

    private List<AdasDriverShow> getPlatFormDrivers(String vehicleId) {
        List<Map<String, String>> driverInfos = getDriverInfo(vehicleId);
        if (sslEnabled) {
            return AdasDriverShow.getDrivers(driverInfos, "/mediaserver");
        }
        return AdasDriverShow.getDrivers(driverInfos, mediaServer);
    }

    private List<Map<String, String>> getDriverInfo(String vehicleId) {
        List<Map<String, String>> proInfos = new ArrayList<>();

        try {
            Map<String, String> monitorInfo = RedisHelper.getHashMap(RedisKeyEnum.MONITOR_INFO.of(vehicleId),
                        Collections.singletonList("professionalIds"));

            String driverIds = Optional.ofNullable(monitorInfo.get("professionalIds")).orElse("");
            if (StrUtil.isBlank(driverIds)) {
                return proInfos;
            }
            List<RedisKey> professionalKeys = Arrays.stream(driverIds.split(","))
                    .map(RedisKeyEnum.PROFESSIONAL_INFO::of)
                    .collect(Collectors.toList());
            return RedisHelper.batchGetHashMap(professionalKeys);

        } catch (Exception e) {
            log.error("从redis获取司机详情报错", e);
        }

        return proInfos;

    }

    /**
     * 在mysql或redis中获取各个整点数据
     */
    private List<AdasAlarmTimesData> getHistoryAlarmTimes(LocalDateTime dateTime) throws Exception {
        long endTime = Date8Utils.getValToHour(dateTime);
        long startTime = Date8Utils.getValToHour(LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0)));
        //分组id集合
        Set<String> assignSet = userPrivilegeUtil.getCurrentUserAssignSet();
        if (assignSet == null || assignSet.isEmpty()) {
            assignSet = new HashSet<>();
            assignSet.add("");
        }
        List<AdasAlarmTimesData> list = riskManageDao.getHistoryAlarmTimes(assignSet, startTime, endTime);
        //特殊处理当查出数据少了时段或没有数据时
        if (CollectionUtils.isNotEmpty(list)) {
            if (endTime < startTime) {
                return new ArrayList<>();
            }
            List<AdasAlarmTimesData> nowList = new ArrayList<>();
            int endTimes = Integer.parseInt((endTime + "").substring(8, 10));
            AdasAlarmTimesData alarmTimesData;
            for (int i = 0; i <= endTimes; i++) {
                alarmTimesData = new AdasAlarmTimesData();
                alarmTimesData.setTime((endTime - endTimes + i + 1) + "");
                nowList.add(alarmTimesData);
            }
            for (AdasAlarmTimesData adasAlarmTimesData : list) {
                alarmTimesData = adasAlarmTimesData;
                int a = Integer.parseInt(alarmTimesData.getTime().substring(8, 10));
                nowList.remove(a);
                alarmTimesData.setTime(startTime + a + 1 + "");
                nowList.add(a, alarmTimesData);
            }
            list = nowList;
        }
        List<RiskEvent> eventList = riskEventDao.findAllEventAndEventCommonFiled();
        for (int n = 0; n < list.size(); n++) {
            AdasAlarmTimesData timesData = list.get(n);
            if (n != list.size() - 1) {
                timesData.setTimeStr(DateUtil.formatDate(timesData.getTime(), HOUR_FORMAT));
            } else {
                timesData.setTimeStr(Date8Utils.getCurrentTime(dateTime));
            }
            timesData.setTime((Integer.valueOf(timesData.getTime().substring(8, 10))) + "");
            //组装当前这小时的各个风险类型的数量
            timesData.setOrgRiskList(setOrgRiskListData(timesData, eventList));
        }
        List<AdasAlarmTimesData> resultAlarmTimes = new ArrayList<>(list);
        for (int i = list.size(); i < 24; i++) {
            AdasAlarmTimesData adasAlarmTimesData = new AdasAlarmTimesData();
            adasAlarmTimesData.setTimeStr(DateUtil.formatDate(startTime + i + "", HOUR_FORMAT));
            adasAlarmTimesData.setTime(i + 1 + "");
            resultAlarmTimes.add(adasAlarmTimesData);
            adasAlarmTimesData.setOrgRiskList(setOrgRiskListData(adasAlarmTimesData, eventList));
        }
        return resultAlarmTimes;
    }

    /**
     * 設置高风险时段的风险类型数量
     * @param timesData 从mysql或者redis中获取到整点的数据
     * @param list      风险类型和对应的 function_id
     * @return map
     */
    private Map<String, Integer> setOrgRiskListData(AdasAlarmTimesData timesData, List<RiskEvent> list) {
        Map<String, Integer> map = new HashMap<>();
        for (RiskEvent riskEvent : list) {
            //获取map中对应的风险类型是否为空
            String riskType = AdasRiskEventEnum.getTypeByName(riskEvent.getRiskType());
            if (map.get(riskType) != null) {
                //不为空时先获取到前面同风险类型的报警的总数
                int sum = map.get(riskType);
                //更新数据
                map.put(riskType,
                    sum + (Integer) Reflections.getFieldValue(timesData, riskEvent.getEventCommonFiled()));
            } else {
                //直接放入当前风险类型的报警数量
                map.put(riskType, (Integer) Reflections.getFieldValue(timesData, riskEvent.getEventCommonFiled()));
            }
        }
        return map;
    }

    private Map<String, String> getEventRiskTypeMap() {
        Map<String, String> eventRiskTypeMap = new HashMap<>();
        List<Map<String, String>> eventList = riskEventDao.findAllEventTypeMap();
        for (Map<String, String> eventInfo : eventList) {
            eventRiskTypeMap.put(eventInfo.get("id"), eventInfo.get("event"));
        }
        return eventRiskTypeMap;
    }

    @Override
    public void setPhotoParam(Photograph photograph) {
        String currentUsername = SystemHelper.getCurrentUsername();
        RedisHelper.setString(HistoryRedisKeyEnum.INTELLIGENCE_PHOTO_PARAM_SETTING.of(currentUsername),
            JSON.toJSONString(photograph));
    }

    @Override
    public Photograph getPhotoParam(String userName) {
        String userPhoto = RedisHelper.getString(HistoryRedisKeyEnum.INTELLIGENCE_PHOTO_PARAM_SETTING.of(userName));
        if (StringUtils.isNotBlank(userPhoto)) {
            return JSON.parseObject(userPhoto, Photograph.class);
        }
        return null;
    }
}

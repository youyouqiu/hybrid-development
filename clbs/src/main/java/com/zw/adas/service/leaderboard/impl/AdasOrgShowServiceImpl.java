package com.zw.adas.service.leaderboard.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zw.adas.domain.leardboard.AdasOrgRiskEvent;
import com.zw.adas.domain.leardboard.AdasOrgRiskType;
import com.zw.adas.domain.leardboard.AdasOrgVehOnline;
import com.zw.adas.domain.riskManagement.bean.AdasOrgEvent;
import com.zw.adas.repository.mysql.riskdisposerecord.AdasOrgShowDao;
import com.zw.adas.service.elasticsearch.AdasElasticSearchService;
import com.zw.adas.service.leaderboard.AdasOrgShowService;
import com.zw.adas.utils.AdasCommonHelper;
import com.zw.adas.utils.elasticsearch.AdasElasticSearchUtil;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.leaderboard.CustomerService;
import com.zw.platform.domain.leaderboard.RiskProportion;
import com.zw.platform.functional.OrgShowQueryData;
import com.zw.platform.service.basicinfo.VehicleService;
import com.zw.platform.util.StrUtil;
import com.zw.platform.util.common.ComputingUtils;
import com.zw.platform.util.common.Date8Utils;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.UuidUtils;
import com.zw.platform.util.privilege.OrgShowQuery;
import com.zw.platform.util.privilege.OrgShowUtils;
import com.zw.platform.util.privilege.UserPrivilegeUtil;
import com.zw.platform.util.report.PaasCloudAdasUrlEnum;
import com.zw.platform.util.spring.EsPrefixConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdasOrgShowServiceImpl implements AdasOrgShowService {
    private static final Logger log = LogManager.getLogger(AdasOrgShowServiceImpl.class);

    @Value("${adas.isVip}")
    private boolean isVip;

    public static String RISK_EVENT_INDEX;

    public static String RISK_INDEX;

    @Autowired
    private EsPrefixConfig esPrefixConfig;
    @Autowired
    private AdasElasticSearchUtil adasElasticSearchUtil;

    @Autowired
    private AdasOrgShowDao adasOrgShowDao;

    @Autowired
    private AdasElasticSearchService elasticSearchService;

    @Autowired
    private AdasCommonHelper adasCommonHelper;

    @Autowired
    private VehicleService vehicleService;

    private static final int TODAY_NOW_TIME = 0;

    private static final int YESTERTODAY_ALL_TIME = 1;

    private static final int ONE_DAY_MILLISECOND = 86400000;

    public static final int ONE_HOUR_MILLISECOND = 3600000;

    private static final int TWO_HOUR_SECOND = 7200;

    private static final int ONE_DAY_SECOND = 86400;

    @Autowired
    private UserService userService;
    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private OrgShowUtils orgShowUtils;

    @Autowired
    private UserPrivilegeUtil userPrivilegeUtil;

    @PostConstruct
    public void initIndex() {
        String prefix = esPrefixConfig.getPrefix();
        RISK_EVENT_INDEX = prefix + "adas_risk_event";
        RISK_INDEX = prefix + "adas_risk";
    }

    @Override
    public List<Map<String, String>> getEventRanking(String groupId, boolean isToday) {
        OrgShowQueryData<Map<String, String>> orgShowQueryData = this::queryEventRanking;
        OrgShowQuery<Map<String, String>> orgShowQuery =
            OrgShowQuery.parseOrgShowQuery(groupId, isToday, OrgShowUtils.EVENT_RANKING, orgShowQueryData);
        return orgShowUtils.getDataByTemplate(orgShowQuery);
    }

    private List<Map<String, String>> queryEventRanking(String groupId, LocalDateTime dateTime, boolean isToday) {
        dateTime = isToday ? dateTime : Date8Utils.getMidnightTime(dateTime);
        long startTime = Date8Utils.getMidnightHour(dateTime);
        long endTime = Date8Utils.getValToHour(dateTime);
        List<Map<String, String>> result;
        List<String> groupIds = organizationService.getChildOrgIdByUuid(groupId);
        String userId = userService.getCurrentUserUuid();
        AdasOrgEvent orgEvent =
            Optional.ofNullable(adasOrgShowDao.getEventRanking(startTime, endTime, userId, groupIds))
                .orElse(new AdasOrgEvent());
        result = orgEvent.getOrgEventList(adasCommonHelper.getEventCommonNameAndFieldMap());
        result.sort((o1, o2) -> Integer.valueOf(o2.get("value")).compareTo(Integer.valueOf(o1.get("value"))));
        return result;
    }

    @Override
    public List<Map<String, String>> getRiskProportion(String groupId, boolean isToday) {
        OrgShowQueryData<Map<String, String>> orgShowQueryData = this::queryRiskProportion;
        OrgShowQuery<Map<String, String>> orgShowQuery =
            OrgShowQuery.parseOrgShowQuery(groupId, isToday, OrgShowUtils.RISK_PROPORTION, orgShowQueryData);
        return orgShowUtils.getDataByTemplate(orgShowQuery);
    }

    private List<Map<String, String>> queryRiskProportion(String groupId, LocalDateTime dateTime, boolean isToday) {
        List<String> groupIds = organizationService.getChildOrgIdByUuid(groupId);
        String userId = userService.getCurrentUserUuid();
        dateTime = isToday ? dateTime : Date8Utils.getMidnightTime(dateTime);
        RiskProportion todayData = getRiskProportionByTime(dateTime, userId, groupIds);
        RiskProportion yesterdayData = getRiskProportionByTime(dateTime.minusDays(1), userId, groupIds);
        assembly(todayData, yesterdayData);
        return todayData.getRiskProportionList(isVip);
    }

    private RiskProportion getRiskProportionByTime(LocalDateTime dateTime, String userId, List<String> groupIds) {
        long startTime = Date8Utils.getMidnightHour(dateTime);
        long endTime = Date8Utils.getValToHour(dateTime);
        return Optional.ofNullable(adasOrgShowDao.getRiskProportion(startTime, endTime, userId, groupIds))
            .orElse(new RiskProportion());
    }

    private void assembly(RiskProportion todayData, RiskProportion yesterdayData) {
        todayData.calculateProportion();
        todayData.calculateRingRatio(yesterdayData);
    }

    @Override
    public List<AdasOrgVehOnline> getVehOnlineTrend(String groupId, boolean isToday) {
        OrgShowQueryData<AdasOrgVehOnline> orgShowQueryData = this::queryVehOnlineTrend;
        OrgShowQuery<AdasOrgVehOnline> orgShowQuery =
            OrgShowQuery.parseOrgShowQuery(groupId, isToday, OrgShowUtils.VEH_ONLINE_TREND, orgShowQueryData);
        return orgShowUtils.getDataByTemplate(orgShowQuery);

    }

    private List<AdasOrgVehOnline> queryVehOnlineTrend(String groupId, LocalDateTime dateTime, boolean isToday) {
        long startTime = Date8Utils.getStartTime(isToday, dateTime);
        long endTime = Date8Utils.getEndTime(isToday, dateTime);
        List<String> groupIds = organizationService.getChildOrgIdByUuid(groupId);
        String userId = userService.getCurrentUserUuid();
        List<AdasOrgVehOnline> orgVeOnlineList = adasOrgShowDao.getVehOnlineTrend(startTime, endTime, groupIds, userId);
        orgVeOnlineList.forEach(AdasOrgVehOnline::calOnlineRate);
        return orgVeOnlineList;
    }

    /**
     * 获取今日此时风控预警数
     */
    @Override
    public int getNowRiskNum() {
        Set<String> set = userPrivilegeUtil.getCurrentUserVehicles();
        int nowRiskNum = 0;
        if (set.size() > 0) {
            String[] times = Date8Utils.getTimes(TODAY_NOW_TIME, LocalDateTime.now());
            try {
                long re = adasElasticSearchUtil.getAllVehicleNum(set, RISK_EVENT_INDEX, times[0], times[1]);
                nowRiskNum = Integer.parseInt(Long.toString(re));
            } catch (Exception e) {
                log.error("查询es表中今日风控事件数异常！", e);
            }
            return nowRiskNum;
        } else {
            return nowRiskNum;
        }
    }

    /**
     * 昨日风控预警数
     */
    @Override
    public int getYesterdayRiskNum() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String uid = SystemHelper.getCurrentUserId();
        boolean change = userPrivilegeUtil.judgeUserPrivilege();
        int yesterdayRiskNum = 0;
        String result;
        RedisKey key = HistoryRedisKeyEnum.ADAS_REPORT_YESTERDAY_RISK_NUM.of(uid, sdf.format(date));
        if (!change) {
            result = RedisHelper.getString(key);
            //从缓存redis中取
            if (result != null) {
                return Integer.parseInt(result);
            }
        }
        //没取到在ES查询，维护redis
        Set<String> set = userPrivilegeUtil.getCurrentUserVehicles();
        if (set.size() > 0) {
            String[] times = Date8Utils.getTimes(YESTERTODAY_ALL_TIME, LocalDateTime.now());
            try {
                long re = adasElasticSearchUtil.getAllVehicleNum(set, RISK_EVENT_INDEX, times[0], times[1]);
                yesterdayRiskNum = Integer.parseInt(Long.toString(re));
                RedisHelper.setString(key, yesterdayRiskNum + "", ONE_DAY_SECOND);
            } catch (Exception e) {
                log.error("查询es表中昨日风控事件数异常！", e);
            }
            return yesterdayRiskNum;
        } else {
            return yesterdayRiskNum;
        }
    }

    /**
     * 获取当日此时风险事件数环比增长
     */
    @Override
    public JsonResultBean getRingRatioRiskEvent() {
        String uid = SystemHelper.getCurrentUserId();
        LocalDateTime localDateTime = LocalDateTime.now();
        boolean change = userPrivilegeUtil.judgeUserPrivilege();
        Date date = new Date();
        JSONObject jsonObject = new JSONObject();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH");
        RedisKey key = HistoryRedisKeyEnum.ADAS_REPORT_RING_RATIO_INFO.of(uid, sdf.format(date));
        if (change) {
            String result = RedisHelper.getString(key);
            //从缓存redis中取
            if (result != null) {
                jsonObject = JSON.parseObject(result);
                return new JsonResultBean(jsonObject);
            }
        }
        //没取到在ES查询，维护redis
        Set<String> set = userPrivilegeUtil.getCurrentUserVehicles();
        if (set.size() > 0) {
            try {
                String[] times = Date8Utils.getTimes(2, localDateTime);
                long ret = adasElasticSearchUtil.getAllVehicleNum(set, RISK_EVENT_INDEX, times[0], times[1]);
                int thisTimeNum = Integer.parseInt(Long.toString(ret));
                times = Date8Utils.getTimes(3, localDateTime);
                long rey = adasElasticSearchUtil.getAllVehicleNum(set, RISK_EVENT_INDEX, times[0], times[1]);
                int yesterdayTimeNum = Integer.parseInt(Long.toString(rey));
                jsonObject.put("ringRatio", getRingRatio(thisTimeNum, yesterdayTimeNum));
                jsonObject.put("trend", String.valueOf(Integer.compare(thisTimeNum - yesterdayTimeNum, 0)));
            } catch (Exception e) {
                log.error("从es获取当日此时风险事件数或昨日此时风险数异常！", e);
            }
        } else {
            jsonObject.put("ringRatio", "0.00%");
            jsonObject.put("trend", 0);
        }
        RedisHelper.setString(key, jsonObject.toJSONString(), TWO_HOUR_SECOND);
        return new JsonResultBean(jsonObject);
    }

    /**
     * 环比
     */
    private String getRingRatio(double now, double ytdNow) {
        if (now != 0 && ytdNow != 0) {
            return ComputingUtils.calRingRatio(now, ytdNow) + "%";
        } else if (now == 0 && ytdNow == 0) {
            return "0.00%";
        } else if (ytdNow == 0) {
            return "100.00%";
        }
        return "0.00%";
    }

    /**
     * 百分比计算
     */
    private String getPercentage(double num, double total) {
        if (num != 0 && total != 0) {
            return ComputingUtils.calProportion(num, total) + "%";
        } else if (total == 0 && num == 0) {
            return "0.00%";
        } else if (total == 0) {
            return "100.00%";
        }
        return "0.00%";
    }

    /**
     * 获取当前用户权限下车辆在线数
     */
    @Override
    public int getVehicleOnlie() {
        Set<String> set = userPrivilegeUtil.getCurrentUserVehicles();
        Set<String> keys = getVehicleOnlineKey();
        int onLine = 0;
        for (String key : keys) {
            String vid = key.split("-vehiclestatus")[0];
            if (set.contains(vid)) {
                onLine = onLine + 1;
            }
        }
        return onLine;
    }

    /**
     * 在线车辆redis缓存keys
     */
    private Set<String> getVehicleOnlineKey() {
        return new HashSet<>(RedisHelper.scanKeys(HistoryRedisKeyEnum.MONITOR_STATUS.of("*")));
    }

    /**
     * 获取今日和昨日车辆上线率
     */
    @Override
    public JsonResultBean getLineRate() {
        JSONObject jsonObject = new JSONObject();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH");
            Date date = new Date();
            boolean change = userPrivilegeUtil.judgeUserPrivilege();
            String uid = SystemHelper.getCurrentUserId();
            RedisKey key = HistoryRedisKeyEnum.ADAS_REPORT_ONLINE_RATE.of(uid, sdf.format(date));
            if (!change) {
                String result = RedisHelper.getString(key);
                //从缓存redis中取
                if (result != null) {
                    return new JsonResultBean(JSONObject.parseObject(result));
                }
            }
            Set<String> set = userPrivilegeUtil.getCurrentUserVehicles();
            List<byte[]> vids = getVids(set);
            if (set.size() > 0) {
                String te = sdf.format(date);
                String ts = te.substring(0, 8) + "00";
                long timeStart = Long.parseLong(ts);
                long timeEnd = Long.parseLong(te);
                //组装查询HBase参数并调用pass端接口
                Map<String, String> param = new HashMap<>();
                param.put("timeStartStr", JSON.toJSONString(timeStart));
                param.put("timeEndStr", JSON.toJSONString(timeEnd));
                param.put("vidsStr", JSON.toJSONString(vids));
                String pass = HttpClientUtil.send(PaasCloudAdasUrlEnum.GET_VEHICLE_ONLINE_NUM, param);
                JSONObject result = JSON.parseObject(pass);
                if (result == null || result.getInteger("code") != 10000) {
                    return new JsonResultBean(JsonResultBean.FAULT, "查询数据异常！");
                }
                Integer today = result.getInteger("data");

                long yesterDayTime = sdf.parse(ts).getTime() - ONE_DAY_MILLISECOND;
                date = new Date(yesterDayTime);
                String ys = sdf.format(date);
                String ye = te.substring(0, 8) + "00";
                long yesterdayStart = Long.parseLong(ys);
                long yesterdayEnd = Long.parseLong(ye);
                //组装查询HBase参数并调用pass端接口
                param.clear();
                param.put("timeStartStr", JSON.toJSONString(yesterdayStart));
                param.put("timeEndStr", JSON.toJSONString(yesterdayEnd));
                param.put("vidsStr", JSON.toJSONString(vids));
                pass = HttpClientUtil.send(PaasCloudAdasUrlEnum.GET_VEHICLE_ONLINE_NUM, param);
                result = JSON.parseObject(pass);
                if (result == null || result.getInteger("code") != 10000) {
                    return new JsonResultBean(JsonResultBean.FAULT, "查询数据异常！");
                }
                Integer yesterday = result.getInteger("data");

                String todayOnLineRate = getPercentage(today, set.size());
                String yesterdayOnLineRate = getPercentage(yesterday, set.size());
                jsonObject.put("todayNumber", today);
                jsonObject.put("todayOnLineRate", todayOnLineRate);
                jsonObject.put("yesterdayNumber", yesterday);
                jsonObject.put("yesterdayOnLineRate", yesterdayOnLineRate);
            } else {
                jsonObject.put("todayNumber", 0);
                jsonObject.put("todayOnLineRate", 0);
                jsonObject.put("yesterdayNumber", "0.00%");
                jsonObject.put("yesterdayOnLineRate", "0.00%");
            }
            RedisHelper.setString(key, jsonObject.toJSONString(), ONE_DAY_SECOND);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new JsonResultBean(jsonObject);
    }

    /**
     * 将用户车id转为byte[]
     */
    private List<byte[]> getVids(Set<String> vids) {
        List<byte[]> v = new ArrayList<>();
        for (String vid : vids) {
            v.add(UuidUtils.getBytesFromUUID(UUID.fromString(vid)));
        }
        return v;
    }

    @Override
    public List<Map<String, String>> getOperCag() {
        List<Map<String, String>> result;
        Set<String> set = userPrivilegeUtil.getCurrentUserVehicles();
        List<String> vids = new ArrayList<>(set);
        //根据企业找到该企业运营类别List，
        result = findOperCagMap(vids);
        double sum = 0.0;
        for (Map<String, String> stringStringMap : result) {
            sum += Double.parseDouble(stringStringMap.get("number"));
        }
        for (Map<String, String> stringStringMap : result) {
            double num = Double.parseDouble(stringStringMap.get("number"));
            stringStringMap.put("ratio", getPercentage(num, sum));
        }
        return result;
    }

    private List<Map<String, String>> findOperCagMap(List<String> vids) {
        List<Map<String, String>> result = new ArrayList<>();
        Map<String, Integer> map = new HashMap<>();
        Map<String, String> vehPurposeMap = vehicleService.getVehPurposeMap();
        List<RedisKey> monitorIdKeys = vids.stream().map(RedisKeyEnum.MONITOR_INFO::of).collect(Collectors.toList());
        Map<String, String> monitorMap = RedisHelper.batchGetHashMap(monitorIdKeys, "id", "vehiclePurpose");

        for (String vehiclePurpose : monitorMap.values()) {
            if (StrUtil.isBlank(vehiclePurpose)) {
                continue;
            }
            Integer count = map.get(vehiclePurpose);
            map.put(vehiclePurpose, count == null ? 1 : count + 1);
        }
        for (Map.Entry<String, String> entry : vehPurposeMap.entrySet()) {
            String vid = entry.getKey();
            String purpose = entry.getValue();
            Integer number = map.get(purpose);
            if (number != null) {
                Map<String, String> map1 = new HashMap<>();
                map1.put("id", vid);
                map1.put("name", purpose);
                map1.put("number", number.toString());
                result.add(map1);
            }
        }

        return result;
    }

    @Override
    public List<AdasOrgRiskEvent> getEventTrend(String groupId, boolean isToday) {
        OrgShowQueryData<AdasOrgRiskEvent> orgShowQueryData = this::queryRiskEventTrend;
        OrgShowQuery<AdasOrgRiskEvent> orgShowQuery =
            OrgShowQuery.parseOrgShowQuery(groupId, isToday, OrgShowUtils.EVENT_TREND, orgShowQueryData);
        return orgShowUtils.getDataByTemplate(orgShowQuery);
    }

    private List<AdasOrgRiskEvent> queryRiskEventTrend(String groupId, LocalDateTime dateTime, boolean isToday) {
        long startTime = Date8Utils.getStartTime(isToday, dateTime);
        long endTime = Date8Utils.getEndTime(isToday, dateTime);
        List<String> groupIds = organizationService.getChildOrgIdByUuid(groupId);
        String userId = userService.getCurrentUserUuid();
        return adasOrgShowDao.getRiskTrend(startTime, endTime, groupIds, userId);
    }

    @Override
    public List<AdasOrgRiskType> getRiskTypeTrend(String groupId, boolean isToday) {
        OrgShowQueryData<AdasOrgRiskType> orgShowQueryData = this::queryRiskTypeTrend;
        OrgShowQuery<AdasOrgRiskType> orgShowQuery =
            OrgShowQuery.parseOrgShowQuery(groupId, isToday, OrgShowUtils.RISK_TYPE_TREND, orgShowQueryData);
        return orgShowUtils.getDataByTemplate(orgShowQuery);
    }

    private List<AdasOrgRiskType> queryRiskTypeTrend(String groupId, LocalDateTime dateTime, boolean isToday) {
        long startTime = Date8Utils.getStartTime(isToday, dateTime);
        long endTime = Date8Utils.getEndTime(isToday, dateTime);
        List<String> groupIds = organizationService.getChildOrgIdByUuid(groupId);
        String userId = userService.getCurrentUserUuid();
        return adasOrgShowDao.getRiskTypeTrend(startTime, endTime, groupIds, userId);
    }

    @Override
    public List<Map<String, String>> getRiskDealInfo(String groupId, boolean isToday) {
        LocalDateTime dateTime = LocalDateTime.now();
        return queryRiskDealInfo(groupId, dateTime, isToday);

    }

    @Override
    public List<CustomerService> getCustomerServiceTrend(boolean isToday) {
        OrgShowQueryData<CustomerService> orgShowQueryData =
            (gid, dateTime, todayFlag) -> queryCustomerServiceTrend(dateTime, todayFlag);
        OrgShowQuery<CustomerService> orgShowQuery =
            OrgShowQuery.parseOrgShowQuery("", isToday, OrgShowUtils.CUSTOMER_SERVICE_TREND, orgShowQueryData);
        return orgShowUtils.getDataByTemplate(orgShowQuery);

    }

    private List<CustomerService> queryCustomerServiceTrend(LocalDateTime dateTime, boolean isToday) {
        dateTime = isToday ? dateTime : dateTime.minusDays(1);
        long time = Date8Utils.getValToDay(dateTime);
        RedisKey timeKey = HistoryRedisKeyEnum.ADAS_REPORT_ONLINE_CUSTOMER_SERVICE.of(time);
        List<String> customerServiceList = RedisHelper.hvals(timeKey);
        return customerServiceList.stream().map(data -> JSON.parseObject(data, CustomerService.class))
            .sorted(Comparator.comparingLong(CustomerService::getTime)).collect(Collectors.toList());
    }

    private List<Map<String, String>> queryRiskDealInfo(String groupId, LocalDateTime dateTime, boolean isToday) {
        String userId = userService.getCurrentUserUuid();
        List<String> groupIds = organizationService.getChildOrgIdByUuid(groupId);
        List<String> vehicleIds = adasOrgShowDao.getUserVidByGroupId(groupIds, userId);
        return elasticSearchService.getRiskDealInfo(vehicleIds, dateTime, isToday);
    }

}

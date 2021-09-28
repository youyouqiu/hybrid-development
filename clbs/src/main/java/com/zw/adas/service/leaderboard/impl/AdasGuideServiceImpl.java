package com.zw.adas.service.leaderboard.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zw.adas.domain.riskManagement.bean.AdasGroupRank;
import com.zw.adas.domain.riskManagement.bean.AdasVehicleRank;
import com.zw.adas.repository.mysql.riskdisposerecord.AdasGuideDao;
import com.zw.adas.service.leaderboard.AdasGuideService;
import com.zw.adas.utils.elasticsearch.AdasElasticSearchUtil;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.domain.BaseKvDo;
import com.zw.platform.basic.service.MonitorService;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.RoleService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.core.Group;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.core.UserLdap;
import com.zw.platform.domain.leaderboard.DriverRank;
import com.zw.platform.domain.leaderboard.GroupRank;
import com.zw.platform.repository.core.ResourceDao;
import com.zw.platform.service.reportManagement.AdasAlarmRankService;
import com.zw.platform.util.common.ComputingUtils;
import com.zw.platform.util.common.Constants;
import com.zw.platform.util.common.Date8Utils;
import com.zw.platform.util.common.GeoHashUtil;
import com.zw.platform.util.privilege.UserPrivilegeUtil;
import org.elasticsearch.action.search.SearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.stereotype.Service;

import javax.naming.ldap.LdapName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdasGuideServiceImpl implements AdasGuideService {

    public static final int ONE_DAY_SECONDES = 86400;

    public static final int DAY_TYPE = 1;

    public static final int MONTH_TYPE = 2;

    public static final int YEAR_TYPE = 3;

    public static final int EXCEPTION_TYPE = -1;

    @Autowired
    private AdasGuideDao adasGuideDao;

    @Autowired
    private UserService userService;

    @Autowired
    private MonitorService monitorService;
    @Autowired
    private AdasElasticSearchUtil adasElasticSearchUtil;

    @Autowired
    private AdasAlarmRankService adasAlarmRankService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private UserPrivilegeUtil userPrivilegeUtil;

    @Autowired
    private RoleService roleService;
    @Autowired
    private ResourceDao resourceDao;

    @Override
    public List<AdasVehicleRank> getVehicleRank() {
        List<AdasVehicleRank> nowRank = new ArrayList<>();
        RedisKey vehicleRankKey = getRankKey(HistoryRedisKeyEnum.ADAS_REPORT_VEHICLE_RANK);
        //权限没有变化，且用户今天不是第一次进入该页面
        if (!userPrivilegeUtil.judgeUserPrivilege()) {
            JSONArray array = JSON.parseArray(RedisHelper.getString(vehicleRankKey));
            if (array != null) {
                nowRank = array.toJavaList(AdasVehicleRank.class);
                return nowRank;
            }
        }
        //权限变化，或今天第一次进入该页面
        Set<String> vids = userPrivilegeUtil.getCurrentUserVehicles();
        Set<String> vidsList = new HashSet<>();
        if (vids.size() == 0) {
            return nowRank;
        }
        nowRank = adasGuideDao.getRankOfVehicle(vids, (int) Date8Utils.getValToDay(LocalDateTime.now().minusDays(1)));
        //组装数据
        asbVehicleData(nowRank, vids, vidsList);
        List<Map<String, Integer>> listMap = adasGuideDao
            .getVehicleToal(new ArrayList<>(vidsList), (int) Date8Utils.getValToDay(LocalDateTime.now().minusDays(2)));
        Map<String, Integer> maps = new HashMap<>();
        for (Map<String, Integer> map : listMap) {
            maps.put(map.get("vid") + "", map.get("total"));
        }
        //设置占比
        setVehicleRankRatio(nowRank, maps);
        //缓存到redis
        RedisHelper.setString(vehicleRankKey, JSON.toJSONString(nowRank), ONE_DAY_SECONDES);
        return nowRank;
    }

    private void asbVehicleData(List<AdasVehicleRank> nowRank, Set<String> vids, Set<String> vidsList) {
        if (nowRank.size() != 0) {
            for (AdasVehicleRank vehicleRank : nowRank) {
                vidsList.add(vehicleRank.getVid());
            }
        }
        if (nowRank.size() >= 10) {
            return;
        }

        List<String> vehicleIds = new ArrayList<>();
        for (String vid : vids) {
            if (vidsList.add(vid)) {
                vehicleIds.add(vid);
            }
            if (vidsList.size() >= 10 || vidsList.size() == vids.size()) {
                Map<String, BaseKvDo<String, String>> monitorIdNameMap =
                    monitorService.getMonitorIdNameMap(vehicleIds, null);
                for (BaseKvDo<String, String> monitor : monitorIdNameMap.values()) {
                    AdasVehicleRank vehicleRank = new AdasVehicleRank();
                    vehicleRank.setVid(monitor.getKeyName());
                    vehicleRank.setBrand(monitor.getFirstVal());
                    nowRank.add(vehicleRank);
                }
                break;
            }

        }

    }

    private void setVehicleRankRatio(List<AdasVehicleRank> nowRank, Map<String, Integer> maps) {
        for (AdasVehicleRank vehicleRank : nowRank) {
            int yesTotal = maps.get(vehicleRank.getVid()) != null ? maps.get(vehicleRank.getVid()) : 0;
            if (vehicleRank.getTotal() - yesTotal > 0) {
                vehicleRank.setRatio("up");
            } else if (vehicleRank.getTotal() - yesTotal < 0) {
                vehicleRank.setRatio("down");
            } else {
                vehicleRank.setRatio("same");
            }
        }
    }

    @Override
    public List<AdasGroupRank> getGroupRank() {
        List<AdasGroupRank> groupRankList = new ArrayList<>();
        List<AdasGroupRank> groupYesList = new ArrayList<>();
        Set<String> groupIds = new HashSet<>();
        RedisKey groupRankKey = getRankKey(HistoryRedisKeyEnum.ADAS_REPORT_GROUP_RANK);
        //权限没有变，且不是第一次进入页面从redis中查询
        if (getGroupRankByRedis(groupRankKey).size() > 0) {
            return getGroupRankByRedis(groupRankKey);
        }
        //权限变化，或者用户今天第一次进入该页面
        List<String> assignmentIds = getAssignmentIdsByUser();
        Set<String> vids = userPrivilegeUtil.getCurrentUserVehicles();
        if (vids.size() == 0 || assignmentIds.size() == 0) {
            return groupRankList;
        }

        Map<String, GroupRank> orgInfo = getOrgInfo(userService.getCurrentUseOrgList());
        groupRankList = adasGuideDao
            .getRankOfGroup(vids, (int) Date8Utils.getValToDay(LocalDateTime.now().minusDays(1)), assignmentIds);
        //数据组装
        assemblyData(groupRankList, groupIds, orgInfo);
        groupYesList = adasGuideDao
            .getYesGroup(new ArrayList<>(groupIds), (int) Date8Utils.getValToDay(LocalDateTime.now().minusDays(2)),
                assignmentIds);
        Map<String, Integer> maps = new HashMap<>();
        for (AdasGroupRank groupRank : groupYesList) {
            maps.put(groupRank.getGroupId(), groupRank.getTotal());
        }
        //计算环比
        setRatio(groupRankList, maps);
        //缓存到redis
        RedisHelper.setString(groupRankKey, JSON.toJSONString(groupRankList), ONE_DAY_SECONDES);
        return groupRankList;
    }

    /**
     * 获得当前用户下级企业的相关信息
     * @return
     */
    private Map<String, GroupRank> getOrgInfo(List<OrganizationLdap> orgs) {
        Map<String, GroupRank> orgInfoMap = new HashMap<>();
        if (orgs != null && orgs.size() > 0) {
            for (OrganizationLdap org : orgs) {
                if (Constants.GROUP_UUID.equals(org.getUuid())) {
                    continue;
                }
                GroupRank groupRank = new GroupRank();
                groupRank.setGroupName(org.getName());
                groupRank.setGroupId(org.getUuid());
                String provinceName = org.getProvinceName() != null ? org.getProvinceName() : "-";
                String countyName = org.getCountyName() != null ? org.getCountyName() : "-";
                String cityName = org.getCityName() != null ? org.getCityName() : "-";
                groupRank.setArea(provinceName + "," + cityName + "," + countyName);
                orgInfoMap.put(org.getUuid(), groupRank);
            }
        }
        return orgInfoMap;
    }

    private void setRatio(List<AdasGroupRank> groupRankList, Map<String, Integer> maps) {
        for (AdasGroupRank groupRank : groupRankList) {
            int yesTotal = maps.get(groupRank.getGroupId()) != null ? maps.get(groupRank.getGroupId()) : 0;
            if (groupRank.getTotal() - yesTotal > 0) {
                groupRank.setRatio("up");
            } else if (groupRank.getTotal() - yesTotal < 0) {
                groupRank.setRatio("down");
            } else {
                groupRank.setRatio("same");
            }
        }
    }

    private void assemblyData(List<AdasGroupRank> groupRankList, Set<String> groupIds, Map<String, GroupRank> orgInfo) {
        if (groupRankList.size() != 0) {
            for (AdasGroupRank groupRank : groupRankList) {
                groupIds.add(groupRank.getGroupId());
                groupRank.setGroupName(orgInfo.get(groupRank.getGroupId()).getGroupName());
                groupRank.setArea(orgInfo.get(groupRank.getGroupId()).getArea());
            }
        }
        //报警数据不足10条时
        if (groupRankList.size() < 10) {
            for (Map.Entry<String, GroupRank> entry : orgInfo.entrySet()) {
                String gid = entry.getKey();
                GroupRank val = entry.getValue();
                if (groupIds.add(gid)) {
                    AdasGroupRank groupRank = new AdasGroupRank();
                    groupRank.setArea(val.getArea());
                    groupRank.setGroupName(val.getGroupName());
                    groupRank.setGroupId(val.getGroupId());
                    groupRankList.add(groupRank);
                }
                if (groupRankList.size() >= 10 || orgInfo.size() == groupRankList.size()) {
                    break;
                }
            }
        }
    }

    private RedisKey getRankKey(HistoryRedisKeyEnum rankEnum) {
        return rankEnum.of(SystemHelper.getCurrentUserId(), Date8Utils.getValToDay(LocalDateTime.now()));

    }

    @Override
    public Map<String, OrganizationLdap> getGroupInfo(List<AdasGroupRank> groupRankList) {
        Set<String> uuidSet = groupRankList.stream().map(AdasGroupRank::getGroupId).collect(Collectors.toSet());
        return organizationService.getOrgByUuids(uuidSet);
    }

    @Override
    public Integer isPermission(String moduleName) {
        UserLdap user = SystemHelper.getCurrentUser();
        LdapName name = LdapUtils.newLdapName(user.getId() + "," + userService.getBaseLdapPath().toString());
        List<Group> roles = (List<Group>) roleService.getByMemberName(name);
        Map<String, String> map = Maps.newHashMap();
        map.put("resourceName", moduleName);
        map.put("type", "0");
        List<String> ids = resourceDao.getIdByNameAndType(map);
        if (ids.size() == 0) {
            return null;
        }
        String resourceId = ids.get(0);
        List<String> roleIds = Lists.newArrayList();
        for (Group role : roles) {
            roleIds.add(role.getId().toString());
        }
        return adasGuideDao.countPermissionByRoles(roleIds, resourceId);
    }

    public List<AdasGroupRank> getGroupRankByRedis(RedisKey groupRankKey) {
        List<AdasGroupRank> groupRankList = new ArrayList<>();
        if (!userPrivilegeUtil.judgeUserPrivilege()) {
            JSONArray array = JSON.parseArray(RedisHelper.getString(groupRankKey));
            if (array != null) {
                groupRankList = array.toJavaList(AdasGroupRank.class);
                return groupRankList;
            }
        }
        return groupRankList;
    }

    public List<String> getAssignmentIdsByUser() {
        return new ArrayList<>(userService.getCurrentUserGroupIds());
    }

    /**
     * 获取热力图数据
     * @param type 1 昨日  2 上月  3 去年
     * @return
     */
    @Override
    public JSONArray getHotMapData(int type) {
        long time = getTimeByType(type);
        //位置type类型
        if (time == EXCEPTION_TYPE) {
            return new JSONArray();
        }
        //先从redis中获取
        String dataFromRedis = getDataFromRedis(time);
        if (dataFromRedis != null) {
            return JSONArray.parseArray(dataFromRedis);
        }
        //从数据库中查询组装 并放入redis
        return buildHotMapData(time, type);
    }

    private String getDataFromRedis(long time) {
        return RedisHelper.getString(HistoryRedisKeyEnum.HOT_MAP_DATA.of(time));
    }

    /**
     * 根据type类型获取查询时间
     * @param type 1 昨日 2 上月 3 去年
     * @return
     */
    private long getTimeByType(int type) {
        switch (type) {
            case DAY_TYPE:
                return Date8Utils.getValToDay(LocalDateTime.now().minusDays(1));
            case MONTH_TYPE:
                return Date8Utils.getValToMonth(LocalDateTime.now().minusMonths(1));
            case YEAR_TYPE:
                return Date8Utils.getValToYear(LocalDateTime.now().minusYears(1));
            default:
                return EXCEPTION_TYPE;
        }
    }

    /**
     * 根据type类型获得过期时间
     * @param type 1 昨日 2 上月 3 去年
     * @return
     */
    private int getOverdueSeconds(int type) {
        switch (type) {
            case DAY_TYPE:
                return 60 * 60 * 24;
            case MONTH_TYPE:
                return 60 * 60 * 24 * 31;
            case YEAR_TYPE:
                return 60 * 60 * 24 * 31 * 12;
            default:
                return 0;
        }
    }

    /**
     * 组装 hotMap返回数据
     * @param time 查询时间
     * @param type 1 昨日 2 上月 3 去年
     * @return
     */
    private JSONArray buildHotMapData(long time, int type) {
        JSONArray result = new JSONArray();
        String content = adasGuideDao.getHotMapContent(time);
        Map<String, BigDecimal> map = JSON.parseObject(content, Map.class);
        if (map == null) {
            return result;
        }
        for (Map.Entry<String, BigDecimal> entry : map.entrySet()) {
            double[] decodeDouble = GeoHashUtil.decode(entry.getKey());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("lat", decodeDouble[0]);
            jsonObject.put("lon", decodeDouble[1]);
            jsonObject.put("content", entry.getValue().setScale(0, BigDecimal.ROUND_HALF_UP).intValue());
            result.add(jsonObject);
        }
        RedisHelper
            .setString(HistoryRedisKeyEnum.HOT_MAP_DATA.of(time), JSON.toJSONString(result), getOverdueSeconds(type));
        return result;
    }

    /**
     * 插卡驾驶员报警排行
     * @param limitSize 每页条数
     * @return
     */
    @Override
    public List<DriverRank> getDriverRank(int limitSize) {
        LocalDateTime now = LocalDateTime.now();
        //昨日
        Map<String, Integer> yesterdayDriverAlarmMap = getDriverAlarmMap(now);
        //前日
        Map<String, Integer> beforeYesterdayDriverAlarmMap = getDriverAlarmMap(now.minusDays(1));
        //企业uuid和企业name关系map
        Map<String, String> orgNameMap = new HashMap<>();
        //用户所有权限的驾驶员基础信息
        List<DriverRank> allDriverInfo = adasAlarmRankService.getAllDriverRank(orgNameMap);
        String key;
        int alarmTotal = 0;
        for (DriverRank driverRank : allDriverInfo) {
            key = driverRank.getDriverName() + "_" + driverRank.getCardNumber();
            driverRank.setGroupName(orgNameMap.get(driverRank.getGroupId()));
            driverRank.setTotal(yesterdayDriverAlarmMap.get(key) != null ? yesterdayDriverAlarmMap.get(key) : 0);
            alarmTotal += driverRank.getTotal();
        }
        int finalAlarmTotal = alarmTotal;
        List<DriverRank> driverRankList = allDriverInfo.stream().map(driverRank -> {
            driverRank.setPercentageString(ComputingUtils.calProportion(driverRank.getTotal(), finalAlarmTotal) + "%");
            return driverRank;
        }).sorted(Comparator.comparing(DriverRank::getTotal).reversed()).collect(Collectors.toList());
        List<DriverRank> result = new ArrayList<>();
        if (driverRankList.size() >= limitSize) {
            result = driverRankList.subList(0, limitSize);
        } else {
            result = driverRankList.subList(0, driverRankList.size());
        }
        int beforTotal = 0;
        for (DriverRank driverRank : result) {
            key = driverRank.getDriverName() + "_" + driverRank.getCardNumber();
            beforTotal = beforeYesterdayDriverAlarmMap.get(key) != null ? beforeYesterdayDriverAlarmMap.get(key) : 0;
            driverRank.setRingRatio(ComputingUtils.ringRatio(driverRank.getTotal(), beforTotal));
        }
        return result;
    }

    private Map<String, Integer> getDriverAlarmMap(LocalDateTime now) {
        String[] times = Date8Utils.getTimes(1, now);
        SearchResponse searchResponse = adasElasticSearchUtil.getDriverSearchResponse(times[0], times[1]);
        return adasAlarmRankService.buildDriverAlarm(searchResponse);
    }
}

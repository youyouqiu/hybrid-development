package com.zw.platform.service.reportManagement.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.google.common.collect.Lists;
import com.zw.adas.domain.riskManagement.bean.AdasGroupRank;
import com.zw.adas.service.leaderboard.AdasGuideService;
import com.zw.adas.utils.elasticsearch.AdasElasticSearchUtil;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.core.UserLdap;
import com.zw.platform.domain.leaderboard.DriverRank;
import com.zw.platform.domain.leaderboard.GroupRank;
import com.zw.platform.domain.leaderboard.VehicleRank;
import com.zw.platform.domain.reportManagement.query.AdasAlarmRankQuery;
import com.zw.platform.repository.modules.AdasAlarmRankDao;
import com.zw.platform.service.reportManagement.AdasAlarmRankService;
import com.zw.platform.util.JsonUtil;
import com.zw.platform.util.PageHelperUtil;
import com.zw.platform.util.common.ComputingUtils;
import com.zw.platform.util.excel.ExportExcelParam;
import com.zw.platform.util.excel.ExportExcelUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdasAlarmRankServiceImpl implements AdasAlarmRankService {

    @Autowired
    private AdasAlarmRankDao adasAlarmRankDao;

    @Autowired
    private AdasGuideService guideService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private AdasElasticSearchUtil adasElasticSearchUtil;


    @Override
    public List<AdasGroupRank> getGroupRank(String orgIds, String startTime, String endTime) {
        List<AdasGroupRank> groupRankList = new ArrayList<>();
        Set<String> currentUserGroupIds = userService.getCurrentUserGroupIds();
        if (CollectionUtils.isEmpty(currentUserGroupIds)) {
            return groupRankList;
        }
        //获得用户权限的分组
        List<String> groupIds = new ArrayList<>(currentUserGroupIds);
        String[] orgIdsArray = orgIds.split(",");
        List<AdasGroupRank> allOrgList = getAllOrgList(orgIdsArray);
        groupRankList = adasAlarmRankDao.getGroupRank(orgIdsArray, startTime, endTime, groupIds);
        if (groupRankList.size() > 0) {
            Set<String> monitorIds = userService.getCurrentUserMonitorIds();
            int sum = adasAlarmRankDao.getRankTotal(monitorIds, startTime, endTime);
            //设置企业信息
            setGroupInfo(groupRankList, sum);
        }
        Set<AdasGroupRank> set = new LinkedHashSet<>(groupRankList);
        set.addAll(allOrgList);
        return new ArrayList<>(set);
    }

    private List<AdasGroupRank> getAllOrgList(String[] orgIdsArray) {

        List<AdasGroupRank> allOrgList = new ArrayList<>();
        List<RedisKey> orgKeyList = Lists.newArrayList();
        for (String orgId : orgIdsArray) {
            orgKeyList.add(RedisKeyEnum.ORGANIZATION_INFO.of(orgId));
        }
        List<Map<String, String>> list = RedisHelper.batchGetHashMap(orgKeyList, Lists.newArrayList("id", "name"));
        if (CollectionUtils.isEmpty(list)) {
            return allOrgList;
        }
        for (Map<String, String> map : list) {
            new AdasGroupRank(map.get("id"), map.get("name"));
        }
        return allOrgList;
    }

    private void setGroupInfo(List<AdasGroupRank> groupRankList, int sum) {
        Map<String, OrganizationLdap> groupInfoMap = guideService.getGroupInfo(groupRankList);
        if (groupInfoMap == null) {
            return;
        }
        OrganizationLdap groupInfo;
        for (AdasGroupRank groupRank : groupRankList) {
            groupInfo = groupInfoMap.get(groupRank.getGroupId());
            groupRank.setGroupName(groupInfo.getName());
            groupRank.setPercentageString(ComputingUtils.calProportion(groupRank.getTotal(), sum) + "%");
        }
    }

    @Override
    public List<VehicleRank> getVehicleRank(String vehicleIds, String startTime, String endTime) {
        Map<String, GroupRank> maps = userService.getCurrentUserOrgInfoList();
        String[] vehicleIdsArray = vehicleIds.split(",");
        List<VehicleRank> allVehicleList = getAllVehicleList(vehicleIdsArray, maps);
        List<VehicleRank> vehicleRankList = adasAlarmRankDao.getVehicleRank(vehicleIdsArray, startTime, endTime);
        if (vehicleRankList.size() > 0) {
            setVehicleRankInfo(startTime, endTime, vehicleRankList, maps);
        }
        Set<VehicleRank> set = new LinkedHashSet<>(vehicleRankList);
        set.addAll(allVehicleList);
        return new ArrayList<>(set);
    }

    private List<VehicleRank> getAllVehicleList(String[] vehicleIdsArray, Map<String, GroupRank> maps) {

        List<RedisKey> redisKeys = Lists.newArrayList();
        List<String> fieldList = Lists.newArrayList("id", "orgName", "name", "orgId");

        for (String vehicleId : vehicleIdsArray) {
            redisKeys.add(RedisKeyEnum.MONITOR_INFO.of(vehicleId));
        }
        List<Map<String, String>> list = RedisHelper.batchGetHashMap(redisKeys, fieldList);
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>(1); 
        }
        List<VehicleRank> vehicleRankList = new ArrayList<>();
        GroupRank groupRank;
        String area = "";
        for (Map<String, String> map : list) {
            groupRank = maps.get(map.get("orgId"));
            if (groupRank != null) {
                area = groupRank.getArea();
            }
            vehicleRankList.add(
                new VehicleRank(map.get("name"), map.get("orgName"), area, map.get("id"), map.get("orgId")));
        }
        return vehicleRankList;
    }

    private void setVehicleRankInfo(String startTime, String endTime, List<VehicleRank> vehicleRankList,
        Map<String, GroupRank> maps) {
        Set<String> currentUserMonitorIds = userService.getCurrentUserMonitorIds();
        int sum = adasAlarmRankDao.getRankTotal(currentUserMonitorIds, startTime, endTime);
        for (VehicleRank vehicleRank : vehicleRankList) {
            GroupRank groupInfo = maps.get(vehicleRank.getGroupId());
            vehicleRank.setGroupName(groupInfo.getGroupName());
            vehicleRank.setArea(groupInfo.getArea());
            vehicleRank.setPercentageString(ComputingUtils.calProportion(vehicleRank.getTotal(), sum) + "%");
        }

    }

    @Override
    public boolean exportGroupRank(String title, int type, HttpServletResponse res, List<AdasGroupRank> pis)
        throws IOException {
        return ExportExcelUtil
            .export(new ExportExcelParam(title, type, pis, GroupRank.class, null, res.getOutputStream()));
    }

    @Override
    public boolean exportVehicleRank(String title, int type, HttpServletResponse response,
        List<VehicleRank> vehicleRank) throws IOException {
        return ExportExcelUtil.export(
            new ExportExcelParam(title, type, vehicleRank, VehicleRank.class, null, response.getOutputStream()));
    }

    @Override
    public boolean exportDriverRank(String title, int type, HttpServletResponse response, List<DriverRank> driverRank)
        throws IOException {
        return ExportExcelUtil
            .export(new ExportExcelParam(title, type, driverRank, DriverRank.class, null, response.getOutputStream()));
    }


    @Override
    public Page<AdasGroupRank> getRankOfGroup(AdasAlarmRankQuery query) {
        query.setGroupIds(query.getGids().split(","));
        Page<AdasGroupRank> groupRankList = new Page<>();
        Set<String> currentUserGroupIds = userService.getCurrentUserGroupIds();
        if (CollectionUtils.isEmpty(currentUserGroupIds)) {
            return groupRankList;
        }
        //获得用户权限的分组
        List<String> assignmentIds = new ArrayList<>(currentUserGroupIds);
        if (StringUtils.isNotEmpty(query.getParam())) {
            List<String> groupIds = organizationService.getOrgIdsByOrgName(query.getParam(), null);
            query.setParmGroupIds(groupIds);
        }
        query.setAssignmentIds(assignmentIds);
        groupRankList = PageHelperUtil.doSelect(query, () -> adasAlarmRankDao.getRankOfGroup(query));
        if (groupRankList.size() > 0) {
            Set<String> vids = userService.getCurrentUserMonitorIds();
            int sum = adasAlarmRankDao.getRankTotal(vids, query.getStartTime(), query.getEndTime());
            //设置企业信息
            setGroupInfo(groupRankList, sum);
        }
        return groupRankList;
    }

    @Override
    public Page<VehicleRank> getRankOfVehicle(AdasAlarmRankQuery query) {
        Map<String, GroupRank> maps = userService.getCurrentUserOrgInfoList();
        query.setVehicleIds(query.getVids().split(","));
        Page<VehicleRank> vehicleRankList =
            PageHelperUtil.doSelect(query, () -> adasAlarmRankDao.getRankOfVehicle(query));
        if (vehicleRankList.size() > 0) {
            setVehicleRankInfo(query.getStartTime(), query.getEndTime(), vehicleRankList, maps);
        }
        return vehicleRankList;
    }

    @Override
    public List<DriverRank> getDriverRank(String driverIds, String startTime, String endTime) {
        List<DriverRank> result = new ArrayList<>();
        Set<String> driverIdSet = new HashSet<>(Arrays.asList(driverIds.split(",")));
        startTime = conversionTime(startTime) + " 00:00:00";
        endTime = conversionTime(endTime) + " 23:59:59";
        //驾驶员从业资格证号和报警数量关系
        SearchResponse searchResponse = adasElasticSearchUtil.getDriverSearchResponse(startTime, endTime);
        Map<String, Integer> driverAlarmMap = buildDriverAlarm(searchResponse);
        //企业uuid和企业name关系map
        Map<String, String> orgNameMap = new HashMap<>();
        //用户所有权限的驾驶员基础信息
        List<DriverRank> allDriverInfo = getAllDriverRank(orgNameMap);
        String key;
        int alarmTotal = 0;
        for (DriverRank driverRank : allDriverInfo) {
            key = driverRank.getDriverName() + "_" + driverRank.getCardNumber();
            alarmTotal += driverAlarmMap.get(key) != null ? driverAlarmMap.get(key) : 0;
            if (driverIdSet.contains(driverRank.getId())) {
                driverRank.setGroupName(orgNameMap.get(driverRank.getGroupId()));
                driverRank.setTotal(driverAlarmMap.get(key) != null ? driverAlarmMap.get(key) : 0);
                result.add(driverRank);
            }
        }
        //设置占比
        for (DriverRank driver : result) {
            driver.setPercentageString(ComputingUtils.calProportion(driver.getTotal(), alarmTotal) + "%");
        }
        return result.stream().sorted(Comparator.comparing(DriverRank::getTotal).reversed())
            .collect(Collectors.toList());
    }

    @Override
    public List<DriverRank> getAllDriverRank(Map<String, String> orgNameMap) {
        Set<String> orgUuidSet = new HashSet<>();
        UserLdap user = SystemHelper.getCurrentUser();
        List<OrganizationLdap> orgList = organizationService.getOrgListByUserDn(user.getId().toString());
        if (orgList != null) {
            for (OrganizationLdap org : orgList) {
                orgUuidSet.add(org.getUuid());
                orgNameMap.put(org.getUuid(), org.getName());
            }
        }
        return adasAlarmRankDao.findADriverByOrgUUidSet(orgUuidSet, null);
    }

    /**
     * 组装驾驶员报警Map
     * @param searchResponse
     * @return
     */
    @Override
    public Map<String, Integer> buildDriverAlarm(SearchResponse searchResponse) {
        Map<String, Integer> result = new HashMap<>();
        if (searchResponse == null) {
            return result;
        }
        Terms driverBuckets = searchResponse.getAggregations().get("driver");
        Terms driverNumberBuckets;
        for (Terms.Bucket bucket : driverBuckets.getBuckets()) {
            String name = bucket.getKeyAsString();
            driverNumberBuckets = bucket.getAggregations().get("driverNumber");
            for (Terms.Bucket numberBucket : driverNumberBuckets.getBuckets()) {
                String number = numberBucket.getKeyAsString();
                result.put(name + "_" + number, (int) numberBucket.getDocCount());
            }
        }
        return result;
    }

    @Override
    public JSONArray driverTree(String queryParam) {
        JSONArray result = new JSONArray();
        Map<String, OrganizationLdap> organizationLdapMap = new HashMap<>();
        List<OrganizationLdap> orgs = userService.getCurrentUseOrgList();
        // 遍历得到当前用户组织及下级组织id的list
        List<String> userOrgListId = new ArrayList<>();
        if (orgs != null && orgs.size() > 0) {
            for (OrganizationLdap org : orgs) {
                userOrgListId.add(org.getUuid());
                organizationLdapMap.put(org.getUuid(), org);
            }
        }
        //递归上级
        Set<OrganizationLdap> orgSet = new HashSet<>();
        // 组装组织树结构
        List<DriverRank> list = adasAlarmRankDao.findADriverByOrgUUidSet(new HashSet<>(userOrgListId), queryParam);
        for (DriverRank driverRank : list) {
            String pid = organizationLdapMap.get(driverRank.getGroupId()).getId().toString();
            JSONObject driverObj = new JSONObject();
            driverObj.put("id", driverRank.getId());
            driverObj.put("pId", pid);
            driverObj.put("name", driverRank.getDriverName());
            driverObj.put("type", "people");
            driverObj.put("iconSkin", "peopleSkin");
            driverObj.put("open", true);
            if (StringUtils.isNotEmpty(queryParam)) {
                organizationService.getParentOrgList(orgs, pid, orgSet);
            }
            result.add(driverObj);
        }
        if (orgSet.size() > 0) {
            orgs = new ArrayList<>(orgSet);
        }
        result.addAll(JsonUtil.getOrgTree(orgs, null));
        return result;
    }

    /**
     * 20191012格式的字符串转换为2019-10-12
     * @param time
     * @return
     */
    private String conversionTime(String time) {
        return time.substring(0, 4) + "-" + time.substring(4, 6) + "-" + time.substring(6, 8);
    }
}

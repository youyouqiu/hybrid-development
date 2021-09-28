package com.zw.platform.service.statistic.impl;

import com.github.pagehelper.Page;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.GroupDTO;
import com.zw.platform.basic.repository.NewLifecycleDao;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.Personalized;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.statistic.LifecycleExpireStatisticQuery;
import com.zw.platform.domain.statistic.info.LifecycleExpireStatisticInfo;
import com.zw.platform.repository.modules.PersonalizedDao;
import com.zw.platform.service.statistic.LifecycleExpireStatisticService;
import com.zw.platform.util.LocalDateUtils;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.VehicleUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author zhouzongbo on 2018/12/10 10:19
 */
@Service
public class LifecycleExpireStatisticServiceImpl implements LifecycleExpireStatisticService {

    @Autowired
    private NewLifecycleDao newLifecycleDao;

    @Autowired
    private PersonalizedDao personalizedDao;

    @Autowired
    private UserService userService;

    @Autowired
    private OrganizationService organizationService;

    @Override
    public List<LifecycleExpireStatisticInfo> findLifecycle(LifecycleExpireStatisticQuery query) throws Exception {
        setQuery(query);
        return getLifecycleExpireStatisticInfoList(query);
    }

    private Page<LifecycleExpireStatisticInfo> getLifecycleExpireStatisticInfoList(
        LifecycleExpireStatisticQuery query) {
        if (CollectionUtils.isEmpty(query.getMonitoryIds())) {
            return new Page<>();
        }
        Page<LifecycleExpireStatisticInfo> resultList = newLifecycleDao.findLifecycleExpireBy(query);

        final Integer expireRemindDays = query.getExpireRemindDays();
        if (CollectionUtils.isNotEmpty(resultList)) {
            // 查询
            List<String> monitorIds =
                resultList.stream().map(LifecycleExpireStatisticInfo::getMonitorId).collect(Collectors.toList());
            Map<String, BindDTO> bindInfoMap = VehicleUtil.batchGetBindInfosByRedis(monitorIds);
            resultList = buildResultMonitorList(resultList, expireRemindDays, bindInfoMap,
                new HashSet<>(query.getGroupIdList()));
        }
        return resultList;
    }

    private Page<LifecycleExpireStatisticInfo> buildResultMonitorList(Page<LifecycleExpireStatisticInfo> resultList,
        Integer expireRemindDays, Map<String, BindDTO> bindInfoMap, Set<String> groupIdList) {
        Page<LifecycleExpireStatisticInfo> resultPage = new Page<>();
        List<GroupDTO> currentUserGroupList = userService.getCurrentUserGroupList();
        Map<String, String> userGroupIdAndNameMap =
            currentUserGroupList.stream().collect(Collectors.toMap(GroupDTO::getId, GroupDTO::getName));
        for (LifecycleExpireStatisticInfo info : resultList) {
            String monitorId = info.getMonitorId();
            BindDTO bindDTO = bindInfoMap.get(monitorId);
            if (bindDTO == null || !groupIdList.contains(bindDTO.getOrgId())) {
                continue;
            }
            Integer expireDays = info.getExpireDays();
            if (expireDays == null) {
                continue;
            }
            if (expireDays > expireRemindDays) {
                info.setLifecycleStatus("未到期");
            } else if (expireDays > 0) {
                info.setLifecycleStatus("即将到期");
            } else {
                info.setLifecycleStatus("已到期");
            }
            info.setExpireDays(Math.abs(expireDays));
            info.setGroupName(bindDTO.getOrgName());
            String groupIds = bindDTO.getGroupId();
            String groupNames = Arrays.stream(groupIds.split(","))
                .map(userGroupIdAndNameMap::get)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(","));
            info.setAssignmentName(groupNames);
            info.setExpireDateStr(LocalDateUtils.dateFormate(info.getExpireDate()));
            String monitorType = info.getMonitorType();
            info.setMonitorType("0".equals(monitorType) ? "车" : "1".equals(monitorType) ? "人" : "物");
            resultPage.add(info);
        }
        return resultPage;
    }

    private void setQuery(LifecycleExpireStatisticQuery query) throws Exception {
        int expireRemindDays = getExpireRemindDays();
        String queryDateStr = query.getQueryDateStr();
        Date date = LocalDateUtils.parseDate(queryDateStr);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, expireRemindDays);
        query.setExpireRemindDate(calendar.getTime());
        query.setExpireRemindDays(expireRemindDays);
        filterMonitoryIdsByType(query);
    }

    private void filterMonitoryIdsByType(LifecycleExpireStatisticQuery query) {
        List<String> orgIds = Arrays.stream(query.getGroupId().split(",")).collect(Collectors.toList());
        query.setGroupIdList(orgIds);
        String username = SystemHelper.getCurrentUsername();
        // 拥有权限的分组
        Set<String> groupSet = RedisHelper.getSet(RedisKeyEnum.USER_GROUP.of(username));
        // 所选企业下的分组
        Set<String> orgGroupIds = RedisHelper.batchGetSet(RedisKeyEnum.ORG_GROUP.ofs(orgIds));
        groupSet.retainAll(orgGroupIds);
        // 满足条件的分组内的监控对象id
        Set<String> groupMonitorIds = RedisHelper.batchGetSet(RedisKeyEnum.GROUP_MONITOR.ofs(groupSet));
        if (query.getFilterType() == 1) {
            List<String> expireMonitorIds = RedisHelper.getList(HistoryRedisKeyEnum.LIFECYCLE_EXPIRE_LIST.of());
            if (CollectionUtils.isNotEmpty(expireMonitorIds)) {
                groupMonitorIds.retainAll(expireMonitorIds);
            }
        }
        query.setMonitoryIds(groupMonitorIds);
    }

    @Override
    public int getExpireRemindDays() throws Exception {
        OrganizationLdap topOrganization = organizationService.getByOu("");
        Personalized personalized = personalizedDao.find(topOrganization.getUuid());
        int expireRemindDays = 30;
        if (Objects.nonNull(personalized)) {
            expireRemindDays = personalized.getServiceExpireReminder();
        }
        return expireRemindDays;
    }

    @Override
    public JsonResultBean findExportLifecycle(LifecycleExpireStatisticQuery query) throws Exception {
        setQuery(query);
        Page<LifecycleExpireStatisticInfo> lifecycleExpireStatisticInfoList =
            getLifecycleExpireStatisticInfoList(query);
        if (CollectionUtils.isEmpty(lifecycleExpireStatisticInfoList)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        String username = SystemHelper.getCurrentUsername();
        RedisKey exportLifecycleListRedisKey = HistoryRedisKeyEnum.EXPORT_LIFECYCLE_LIST.of(username);
        if (RedisHelper.isContainsKey(exportLifecycleListRedisKey)) {
            RedisHelper.delete(exportLifecycleListRedisKey);
        }
        RedisHelper.addToList(exportLifecycleListRedisKey, lifecycleExpireStatisticInfoList);
        RedisHelper.expireKey(exportLifecycleListRedisKey, RedisHelper.SIX_HOUR_REDIS_EXPIRE);
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }
}

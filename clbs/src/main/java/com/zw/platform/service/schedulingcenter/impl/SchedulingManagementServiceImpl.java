package com.zw.platform.service.schedulingcenter.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.scheduledmanagement.SchedulingFrom;
import com.zw.platform.domain.scheduledmanagement.SchedulingInfo;
import com.zw.platform.domain.scheduledmanagement.SchedulingItemForm;
import com.zw.platform.domain.scheduledmanagement.SchedulingItemInfo;
import com.zw.platform.domain.scheduledmanagement.SchedulingQuery;
import com.zw.platform.domain.scheduledmanagement.SchedulingRelationMonitorForm;
import com.zw.platform.domain.scheduledmanagement.SchedulingRelationMonitorInfo;
import com.zw.platform.push.mqserver.ZMQFencePub;
import com.zw.platform.repository.modules.SchedulingManagementDao;
import com.zw.platform.service.core.UserService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.service.schedulingcenter.SchedulingManagementService;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author penghj
 * @version 1.0
 * @date 2019/11/7 13:49
 */
@Service
public class SchedulingManagementServiceImpl implements SchedulingManagementService {
    private static Logger logger = LogManager.getLogger(SchedulingManagementServiceImpl.class);

    /**
     * 排班状态 未开始
     */
    private static final int SCHEDULING_STATUS_NOT_START = 1;
    /**
     * 排班状态 已结束
     */
    private static final int SCHEDULING_STATUS_HAS_END = 2;
    /**
     * 排班状态 执行中
     */
    private static final int SCHEDULING_STATUS_IN_EXECUTION = 3;

    private static final String DATE_DUPLICATE_TYPE_EVERY_DAy = "8";

    @Autowired
    private LogSearchService logSearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private SchedulingManagementDao schedulingManagementDao;

    @Override
    public PageGridBean getSchedulingList(SchedulingQuery query) {
        // 用户当前组织和下级组织的组织id和组织名称map
        Map<String, String> organizationIdAndOrganizationNameMap = userService.getUserOrgNameMap();
        query.setOrganizationIdSet(organizationIdAndOrganizationNameMap.keySet());
        String simpleQueryParam = query.getSimpleQueryParam();
        if (StringUtils.isNotBlank(simpleQueryParam)) {
            query.setSimpleQueryParam(StringUtil.mysqlLikeWildcardTranslation(simpleQueryParam));
        }
        Page<SchedulingInfo> schedulingInfoList =
            PageHelper.startPage(query.getPage().intValue(), query.getLimit().intValue())
                .doSelectPage(() -> schedulingManagementDao.getSchedulingList(query));
        if (CollectionUtils.isEmpty(schedulingInfoList)) {
            return new PageGridBean(schedulingInfoList, true);
        }
        // 当天0点时间戳
        long toadyDateTimeLong = DateUtil.todayFirstDate().getTime();
        for (SchedulingInfo schedulingInfo : schedulingInfoList) {
            schedulingInfo.setGroupName(organizationIdAndOrganizationNameMap.get(schedulingInfo.getGroupId()));
            // 当前时间大于排班结束时间,排班已结束
            if (toadyDateTimeLong > schedulingInfo.getEndDate().getTime()) {
                schedulingInfo.setStatus(SCHEDULING_STATUS_HAS_END);
                continue;
            }
            // 当前时间小于排班开始时间,排班已未开始
            if (toadyDateTimeLong < schedulingInfo.getStartDate().getTime()) {
                schedulingInfo.setStatus(SCHEDULING_STATUS_NOT_START);
                continue;
            }
            // 当前时间在排班开始和结束时间范围内,排班执行中
            schedulingInfo.setStatus(SCHEDULING_STATUS_IN_EXECUTION);
        }
        return new PageGridBean(schedulingInfoList, true);
    }

    @Override
    public JsonResultBean addScheduling(SchedulingFrom schedulingFrom, String ipAddress) {
        if (schedulingFrom.getStartDate().getTime() < DateUtil.todayFirstDate().getTime()) {
            return new JsonResultBean(JsonResultBean.FAULT, "必须从当前系统日期第二天开始选择！");
        }
        schedulingFrom.setGroupId(userService.getOrgUuidByUser());
        schedulingFrom.setCreateDataUsername(SystemHelper.getCurrentUsername());
        if (!schedulingManagementDao.addScheduling(schedulingFrom)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        List<SchedulingRelationMonitorForm> schedulingRelationMonitorFormList =
            assemblyNeedAddSchedulingMonitorInfo(schedulingFrom.getId(), schedulingFrom.getStartDate(),
                schedulingFrom.getEndDate(), schedulingFrom.getDateDuplicateType(),
                Arrays.asList(schedulingFrom.getMonitorIds().split(",")));
        if (!schedulingManagementDao.addMonitorScheduling(schedulingRelationMonitorFormList)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        List<SchedulingItemForm> schedulingItemFormList = assemblyNeedAddSchedulingItemInfo(schedulingFrom);
        if (!schedulingManagementDao.addSchedulingItem(schedulingItemFormList)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        ZMQFencePub.pubChangeFence("22");
        String message = "排班管理：新增排班（" + schedulingFrom.getScheduledName() + "）";
        logSearchService.addLog(ipAddress, message, "3", "排班管理");
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 组装需要新增的监控对象排班信息
     */
    private List<SchedulingRelationMonitorForm> assemblyNeedAddSchedulingMonitorInfo(String scheduledInfoId,
        Date startDate, Date endDate, String dateDuplicateType, Collection<String> monitorIdList) {
        List<SchedulingRelationMonitorForm> monitorSchedulingInfoList = new ArrayList<>();
        for (String monitorId : monitorIdList) {
            SchedulingRelationMonitorForm schedulingRelationMonitorForm = new SchedulingRelationMonitorForm();
            schedulingRelationMonitorForm.setScheduledInfoId(scheduledInfoId);
            schedulingRelationMonitorForm.setMonitorId(monitorId);
            schedulingRelationMonitorForm.setStartDate(startDate);
            schedulingRelationMonitorForm.setEndDate(endDate);
            schedulingRelationMonitorForm.setDateDuplicateType(dateDuplicateType);
            monitorSchedulingInfoList.add(schedulingRelationMonitorForm);
        }
        return monitorSchedulingInfoList;
    }

    /**
     * 组装需要新增的排班项信息
     */
    private List<SchedulingItemForm> assemblyNeedAddSchedulingItemInfo(SchedulingFrom schedulingFrom) {
        List<SchedulingItemForm> schedulingItemList =
            JSON.parseArray(schedulingFrom.getSchedulingItemInfos(), SchedulingItemForm.class);
        String scheduledInfoId = schedulingFrom.getId();
        for (SchedulingItemForm schedulingItemForm : schedulingItemList) {
            schedulingItemForm.setScheduledInfoId(scheduledInfoId);
        }
        return schedulingItemList;
    }

    /**
     * 删除排班
     * @param scheduledInfoId 排班id
     * @param ipAddress       ip地址
     * @return JsonResultBean
     */
    @Override
    public JsonResultBean deleteScheduling(String scheduledInfoId, String ipAddress) {
        SchedulingInfo schedulingInfo = schedulingManagementDao.getSchedulingInfoById(scheduledInfoId);
        if (System.currentTimeMillis() > schedulingInfo.getStartDate().getTime()) {
            return new JsonResultBean(JsonResultBean.FAULT, "排班处于执行中或已结束，不能进行删除操作！");
        }
        if (!schedulingManagementDao.deleteScheduling(scheduledInfoId)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        if (!schedulingManagementDao.deleteMonitorScheduling(scheduledInfoId)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        if (!schedulingManagementDao.deleteSchedulingItem(scheduledInfoId)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        String message = "排班管理：删除排班（" + schedulingInfo.getScheduledName() + "）";
        logSearchService.addLog(ipAddress, message, "3", "排班管理");
        ZMQFencePub.pubChangeFence("22");
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 获得排班信息
     * @param scheduledInfoId 排班id
     * @return SchedulingInfo
     */
    @Override
    public SchedulingInfo getSchedulingInfoById(String scheduledInfoId) {
        return schedulingManagementDao.getSchedulingInfoById(scheduledInfoId);
    }

    /**
     * 获得排班管理监控对象id
     * @param scheduledInfoId 排班id
     * @return List<String>
     */
    @Override
    public List<SchedulingRelationMonitorInfo> getSchedulingRelationMonitorInfoList(String scheduledInfoId) {
        return schedulingManagementDao.getSchedulingRelationMonitorInfoListById(scheduledInfoId);
    }

    /**
     * 获得排班的排班项
     * @param scheduledInfoId 排班id
     * @return List<SchedulingItemInfo>
     */
    @Override
    public List<SchedulingItemInfo> getSchedulingItemInfoList(String scheduledInfoId) {
        return schedulingManagementDao.getSchedulingItemInfoListById(scheduledInfoId);
    }

    /**
     * 修改排班
     * @param schedulingFrom 排班信息
     * @param ipAddress      ip地址
     * @return JsonResultBean
     */
    @Override
    public JsonResultBean updateScheduling(SchedulingFrom schedulingFrom, String ipAddress) {
        String scheduledInfoId = schedulingFrom.getId();
        SchedulingInfo oldSchedulingInfo = schedulingManagementDao.getSchedulingInfoById(scheduledInfoId);
        long nowTimeLong = System.currentTimeMillis();
        // condition1:当前时间大于排班结束时间 排班已结束，不能进行修改操作
        if (nowTimeLong > oldSchedulingInfo.getEndDate().getTime()) {
            return new JsonResultBean(JsonResultBean.FAULT, "排班已结束，不能进行修改操作！");
        }
        // 当前修改排班的关联人员id
        List<String> nowSchedulingRelationMonitorIdList = Arrays.asList(schedulingFrom.getMonitorIds().split(","));
        // 之前排班的关联人员id
        List<String> oldSchedulingRelationMonitorIdList =
            schedulingManagementDao.getSchedulingRelationMonitorInfoListById(scheduledInfoId).stream()
                .map(SchedulingRelationMonitorInfo::getMonitorId).collect(Collectors.toList());
        // 现在的关联人员去除之前的关联人员, 剩下的就是需要新增的
        List<String> needAddSchedulingMonitorIdList = nowSchedulingRelationMonitorIdList.stream()
            .filter(monitorId -> !oldSchedulingRelationMonitorIdList.contains(monitorId)).collect(Collectors.toList());
        // condition2:当前时间大于排班开始时间 排班已开始，只允许新增排班人员
        if (nowTimeLong > oldSchedulingInfo.getStartDate().getTime()) {
            // 新增排班人员
            return addSchedulingMonitor(schedulingFrom, ipAddress, oldSchedulingInfo, needAddSchedulingMonitorIdList);
        }
        // condition3:当前时间小于于排班开始时间 排班未开始，可以修改排班信息
        // 之前的关联人员去除现在的关联人员, 剩下的就是需要删除的
        List<String> needDeleteSchedulingMonitorIdList = oldSchedulingRelationMonitorIdList.stream()
            .filter(monitorId -> !nowSchedulingRelationMonitorIdList.contains(monitorId)).collect(Collectors.toList());
        return updateSchedulingInfo(schedulingFrom, ipAddress, scheduledInfoId, needAddSchedulingMonitorIdList,
            needDeleteSchedulingMonitorIdList);
    }

    /**
     * 新增排班关联监控对象 (修改排班,当前时间大于排班开始时间 排班已开始，只允许新增排班人员)
     * @param schedulingFrom                 排班信息
     * @param ipAddress                      IP地址
     * @param oldSchedulingInfo              之前的排班信息
     * @param needAddSchedulingMonitorIdList 需要新增的关联监控对象id
     * @return JsonResultBean
     */
    private JsonResultBean addSchedulingMonitor(SchedulingFrom schedulingFrom, String ipAddress,
        SchedulingInfo oldSchedulingInfo, List<String> needAddSchedulingMonitorIdList) {
        // 判断排班关联人员是否新增
        if (CollectionUtils.isEmpty(needAddSchedulingMonitorIdList)) {
            // 没有新增关联人员
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        List<SchedulingRelationMonitorForm> schedulingRelationMonitorFormList =
            assemblyNeedAddSchedulingMonitorInfo(schedulingFrom.getId(), oldSchedulingInfo.getStartDate(),
                oldSchedulingInfo.getEndDate(), oldSchedulingInfo.getDateDuplicateType(),
                needAddSchedulingMonitorIdList);
        if (!schedulingManagementDao.addMonitorScheduling(schedulingRelationMonitorFormList)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        String message = "排班管理：修改排班（" + oldSchedulingInfo.getScheduledName() + "）";
        logSearchService.addLog(ipAddress, message, "3", "排班管理");
        ZMQFencePub.pubChangeFence("22");
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 修改排班信息
     * @param schedulingFrom                    排班信息
     * @param ipAddress                         IP地址
     * @param scheduledInfoId                   排班id
     * @param needAddSchedulingMonitorIdList    需要新增的关联监控对象id
     * @param needDeleteSchedulingMonitorIdList 需要删除的关联监控对象id
     * @return JsonResultBean
     */
    private JsonResultBean updateSchedulingInfo(SchedulingFrom schedulingFrom, String ipAddress, String scheduledInfoId,
        List<String> needAddSchedulingMonitorIdList, List<String> needDeleteSchedulingMonitorIdList) {
        schedulingFrom.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        // 修改排班项信息
        if (!schedulingManagementDao.updateScheduling(schedulingFrom)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        // 修改排班关联监控对象
        if (CollectionUtils.isNotEmpty(needAddSchedulingMonitorIdList)) {
            List<SchedulingRelationMonitorForm> schedulingRelationMonitorFormList =
                assemblyNeedAddSchedulingMonitorInfo(scheduledInfoId, schedulingFrom.getStartDate(),
                    schedulingFrom.getEndDate(), schedulingFrom.getDateDuplicateType(), needAddSchedulingMonitorIdList);
            if (!schedulingManagementDao.addMonitorScheduling(schedulingRelationMonitorFormList)) {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        }
        if (CollectionUtils.isNotEmpty(needDeleteSchedulingMonitorIdList)) {
            if (!schedulingManagementDao
                .deleteSchedulingRelationMonitor(scheduledInfoId, needDeleteSchedulingMonitorIdList)) {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        }
        // 修改排版项
        // 先删除之前排班项
        if (!schedulingManagementDao.deleteSchedulingItem(scheduledInfoId)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        // 添加现在的排班项
        List<SchedulingItemForm> schedulingItemFormList = assemblyNeedAddSchedulingItemInfo(schedulingFrom);
        if (!schedulingManagementDao.addSchedulingItem(schedulingItemFormList)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        String message = "排班管理：修改排班（" + schedulingFrom.getScheduledName() + "）";
        logSearchService.addLog(ipAddress, message, "3", "排班管理");
        ZMQFencePub.pubChangeFence("22");
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 修改排班结束时间为档期日期(强制结束)
     * @param scheduledInfoId 排班id
     * @param ipAddress       ip地址
     * @return JsonResultBean
     */
    @Override
    public JsonResultBean updateSchedulingEndDateToNowDate(String scheduledInfoId, String ipAddress) {
        SchedulingInfo schedulingInfo = schedulingManagementDao.getSchedulingInfoById(scheduledInfoId);
        // 当前日期
        Date nowDate = DateUtil.todayFirstDate();
        // 修改排班结束时间为当前日期(强制结束)
        if (!schedulingManagementDao
            .updateSchedulingEndDateToNowDate(scheduledInfoId, nowDate, SystemHelper.getCurrentUsername(),
                new Date())) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        // 修改排班关联监控对象的排班结束时间为当前日期
        if (!schedulingManagementDao.updateSchedulingRelationMonitorEndDateToNowDate(scheduledInfoId, nowDate)) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        //记录日志
        String message = "排班管理：强制结束（" + schedulingInfo.getScheduledName() + "）";
        logSearchService.addLog(ipAddress, message, "3", "排班管理");
        ZMQFencePub.pubChangeFence("22");
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 判断排班名称是否可以使用
     * @param scheduledName   排班名称
     * @param scheduledInfoId 排班id
     * @return true:可以使用; false: 不可以使用
     */
    @Override
    public boolean judgeScheduledNameIsCanBeUsed(String scheduledName, String scheduledInfoId) {
        List<SchedulingInfo> schedulingInfoList = schedulingManagementDao.getSchedulingInfoByName(scheduledName);
        if (schedulingInfoList.isEmpty()) {
            return true;
        }

        String groupId;
        String oldScheduledName;
        if (StringUtils.isBlank(scheduledInfoId)) {
            groupId = userService.getOrgUuidByUser();
            oldScheduledName = "";
        } else {
            SchedulingInfo schedulingInfoById = schedulingManagementDao.getSchedulingInfoById(scheduledInfoId);
            groupId = schedulingInfoById.getGroupId();
            oldScheduledName = schedulingInfoById.getScheduledName();
        }

        for (SchedulingInfo schedulingInfo : schedulingInfoList) {
            //相同组织已经存在该排班任务名称
            if (Objects.equals(schedulingInfo.getGroupId(), groupId) && !Objects
                .equals(scheduledName, oldScheduledName)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查排班冲突
     * @param schedulingFrom 排班信息
     * @return JsonResultBean
     */
    @Override
    public JsonResultBean checkSchedulingConflicts(SchedulingFrom schedulingFrom) {
        String scheduledInfoId = schedulingFrom.getIsUpdate() ? schedulingFrom.getId() : null;
        Date nowDate = DateUtil.todayFirstDate();
        // step1:判断排班时间是否冲突
        // 排班时间范围有交叉的排班信息
        List<SchedulingInfo> conflictsSchedulingInfoList = schedulingManagementDao
            .getConflictsSchedulingInfoList(schedulingFrom.getStartDate(), schedulingFrom.getEndDate(), scheduledInfoId,
                nowDate);
        if (CollectionUtils.isEmpty(conflictsSchedulingInfoList)) {
            return new JsonResultBean();
        }
        // step2:判断日期重复类型是否冲突
        // 日期重复类型（1星期一,2星期二,3星期三,4星期四,5星期五,6星期六,7星期天,8每天）
        String dateDuplicateType = schedulingFrom.getDateDuplicateType();
        // 日期重复类型冲突的排班信息
        List<SchedulingInfo> dateDuplicateTypeConflictsSchedulingInfoList = conflictsSchedulingInfoList.stream()
            .filter(info -> judgeDateDuplicateTypeIsConflicts(dateDuplicateType, info.getDateDuplicateType()))
            .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(dateDuplicateTypeConflictsSchedulingInfoList)) {
            return new JsonResultBean();
        }
        // 日期重复类型冲突的排班id
        List<String> dateDuplicateTypeConflictsSchedulingInfoIdList =
            dateDuplicateTypeConflictsSchedulingInfoList.stream().map(SchedulingInfo::getId)
                .collect(Collectors.toList());
        // step3:排班排班项冲突
        List<SchedulingItemForm> newSchedulingItemList =
            JSON.parseArray(schedulingFrom.getSchedulingItemInfos(), SchedulingItemForm.class);
        List<SchedulingItemInfo> exitSchedulingItemInfoList =
            schedulingManagementDao.getSchedulingItemInfoListByIdList(dateDuplicateTypeConflictsSchedulingInfoIdList);
        List<String> schedulingItemConflictsSchedulingInfoIdList = new ArrayList<>();
        String nowDateStr = DateUtil.getDateToString(new Date(), DateUtil.DATE_Y_M_D_FORMAT);
        for (SchedulingItemForm newSchedulingItemForm : newSchedulingItemList) {
            Long newStartTime =
                DateUtil.getStringToLong(nowDateStr + " " + newSchedulingItemForm.getStartTime() + ":00", null);
            Long newEndTime =
                DateUtil.getStringToLong(nowDateStr + " " + newSchedulingItemForm.getEndTime() + ":00", null);
            for (SchedulingItemInfo exitSchedulingItemInfo : exitSchedulingItemInfoList) {
                String exitScheduledInfoId = exitSchedulingItemInfo.getScheduledInfoId();
                if (schedulingItemConflictsSchedulingInfoIdList.contains(exitScheduledInfoId)) {
                    continue;
                }
                Long exitStartTime =
                    DateUtil.getStringToLong(nowDateStr + " " + exitSchedulingItemInfo.getStartTime() + ":00", null);
                Long exitEndTime =
                    DateUtil.getStringToLong(nowDateStr + " " + exitSchedulingItemInfo.getEndTime() + ":00", null);
                // 是否冲突
                boolean isConflicts =
                    (newStartTime >= exitStartTime && newStartTime <= exitEndTime) || (newEndTime >= exitStartTime
                        && newEndTime <= exitEndTime) || (newStartTime <= exitStartTime && newEndTime >= exitEndTime);
                if (isConflicts) {
                    schedulingItemConflictsSchedulingInfoIdList.add(exitScheduledInfoId);
                }
            }
        }
        if (CollectionUtils.isEmpty(schedulingItemConflictsSchedulingInfoIdList)) {
            return new JsonResultBean();
        }
        // step4:判断人员冲突
        List<String> newMonitorIdList = Arrays.asList(schedulingFrom.getMonitorIds().split(","));
        List<SchedulingRelationMonitorInfo> schedulingRelationMonitorIdList = schedulingManagementDao
            .getSchedulingRelationMonitorInfoListByIdList(schedulingItemConflictsSchedulingInfoIdList);
        // 冲突的排班关联监控对象信息
        List<SchedulingRelationMonitorInfo> conflictsSchedulingRelationMonitorInfoList =
            schedulingRelationMonitorIdList.stream().filter(info -> newMonitorIdList.contains(info.getMonitorId()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(conflictsSchedulingRelationMonitorInfoList)) {
            return new JsonResultBean();
        }
        return new JsonResultBean(conflictsSchedulingRelationMonitorInfoList);
    }

    /**
     * 定时保存需要计算离线报表的排班id到redis
     */
    @Override
    public void saveNeedCalculateOfflineReportScheduledIdToRedis() {
        try {
            // 当前日期 yyyy-MM-dd
            String nowDate = DateUtil.getDateToString(new Date(), DateUtil.DATE_Y_M_D_FORMAT);
            // 星期几
            String weekDay = DateUtil.getWeekDay();
            RedisKey key =
                HistoryRedisKeyEnum.SCHEDULED.of((DateUtil.getStringToLong(nowDate + " 00:00:00", null) / 1000L));
            com.zw.platform.basic.core.RedisHelper.delete(key);
            List<String> scheduledIdList =
                schedulingManagementDao.getNeedCalculateOfflineReportScheduledId(nowDate, weekDay);
            if (CollectionUtils.isEmpty(scheduledIdList)) {
                return;
            }
            com.zw.platform.basic.core.RedisHelper.addToSet(key, scheduledIdList);
            RedisHelper.expireKey(key, 2 * 24 * 60 * 60);
        } catch (Exception e) {
            logger.info("定时保存需要计算离线报表的排班id到redis异常", e);
        }

    }

    /**
     * 判断日期重复类型是否冲突
     * @param newDateDuplicateType   新的日期重复类型
     * @param existDateDuplicateType 存在冲突的排班的日期重复类型
     * @return boolean
     */
    private boolean judgeDateDuplicateTypeIsConflicts(String newDateDuplicateType, String existDateDuplicateType) {
        if (newDateDuplicateType.contains(DATE_DUPLICATE_TYPE_EVERY_DAy) || existDateDuplicateType
            .contains(DATE_DUPLICATE_TYPE_EVERY_DAy)) {
            return true;
        }
        List<String> newDateDuplicateTypeList =
            Arrays.stream(newDateDuplicateType.split(",")).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        List<String> existDateDuplicateTypeList =
            Arrays.stream(existDateDuplicateType.split(",")).filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        Optional<String> optional =
            newDateDuplicateTypeList.stream().filter(existDateDuplicateTypeList::contains).findFirst();
        return optional.isPresent();
    }
}

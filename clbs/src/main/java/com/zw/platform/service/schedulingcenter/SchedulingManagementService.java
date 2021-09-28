package com.zw.platform.service.schedulingcenter;

import com.zw.platform.domain.scheduledmanagement.SchedulingFrom;
import com.zw.platform.domain.scheduledmanagement.SchedulingInfo;
import com.zw.platform.domain.scheduledmanagement.SchedulingItemInfo;
import com.zw.platform.domain.scheduledmanagement.SchedulingQuery;
import com.zw.platform.domain.scheduledmanagement.SchedulingRelationMonitorInfo;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;

import java.util.List;

/**
 * @author penghj
 * @version 1.0
 * @date 2019/11/7 13:49
 */
public interface SchedulingManagementService {

    /**
     * 排班列表
     * @param query 查询条件
     * @return PageGridBean
     */
    PageGridBean getSchedulingList(SchedulingQuery query);

    /**
     * 新增排班
     * @param schedulingFrom 排班信息
     * @param ipAddress      ip地址
     * @return JsonResultBean
     */
    JsonResultBean addScheduling(SchedulingFrom schedulingFrom, String ipAddress);

    /**
     * 删除排班
     * @param scheduledInfoId 排班id
     * @param ipAddress       ip地址
     * @return JsonResultBean
     */
    JsonResultBean deleteScheduling(String scheduledInfoId, String ipAddress);

    /**
     * 获得排班信息
     * @param scheduledInfoId 排班id
     * @return SchedulingInfo
     */
    SchedulingInfo getSchedulingInfoById(String scheduledInfoId);

    /**
     * 获得排班管理监控对象id
     * @param scheduledInfoId 排班id
     * @return List<SchedulingRelationMonitorInfo>
     */
    List<SchedulingRelationMonitorInfo> getSchedulingRelationMonitorInfoList(String scheduledInfoId);

    /**
     * 获得排班的排班项
     * @param scheduledInfoId 排班id
     * @return List<SchedulingItemInfo>
     */
    List<SchedulingItemInfo> getSchedulingItemInfoList(String scheduledInfoId);

    /**
     * 修改排班
     * @param schedulingFrom 排班信息
     * @param ipAddress      ip地址
     * @return JsonResultBean
     */
    JsonResultBean updateScheduling(SchedulingFrom schedulingFrom, String ipAddress);

    /**
     * 修改排班结束时间为档期日期(强制结束)
     * @param scheduledInfoId 排班id
     * @param ipAddress       ip地址
     * @return JsonResultBean
     */
    JsonResultBean updateSchedulingEndDateToNowDate(String scheduledInfoId, String ipAddress);

    /**
     * 判断排班名称是否可以使用
     * @param scheduledName   排班名称
     * @param scheduledInfoId 排班id
     * @return true:可以使用; false: 不可以使用
     */
    boolean judgeScheduledNameIsCanBeUsed(String scheduledName, String scheduledInfoId);

    /**
     * 检查排班冲突
     * @param schedulingFrom 排班信息
     * @return JsonResultBean
     */
    JsonResultBean checkSchedulingConflicts(SchedulingFrom schedulingFrom);

    /**
     * 定时保存需要计算离线报表的排班id到redis
     */
    void saveNeedCalculateOfflineReportScheduledIdToRedis();
}

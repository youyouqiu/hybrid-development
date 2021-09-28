package com.zw.platform.repository.modules;

import com.github.pagehelper.Page;
import com.zw.platform.domain.scheduledmanagement.SchedulingFrom;
import com.zw.platform.domain.scheduledmanagement.SchedulingInfo;
import com.zw.platform.domain.scheduledmanagement.SchedulingItemForm;
import com.zw.platform.domain.scheduledmanagement.SchedulingItemInfo;
import com.zw.platform.domain.scheduledmanagement.SchedulingRelationMonitorForm;
import com.zw.platform.domain.scheduledmanagement.SchedulingQuery;
import com.zw.platform.domain.scheduledmanagement.SchedulingRelationMonitorInfo;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * @author penghj
 * @version 1.0
 */
public interface SchedulingManagementDao {

    /**
     * 排班列表
     * @param query 查询条件
     * @return Page<SchedulingInfo>
     */
    Page<SchedulingInfo> getSchedulingList(SchedulingQuery query);

    /**
     * 获得冲突的排班
     * @param startDate       排班开始日期
     * @param endDate         排班结束日期
     * @param scheduledInfoId 排班id
     * @param nowDate         当前日期
     * @return List<SchedulingInfo>
     */
    List<SchedulingInfo> getConflictsSchedulingInfoList(@Param("startDate") Date startDate,
        @Param("endDate") Date endDate, @Param("scheduledInfoId") String scheduledInfoId,
        @Param("nowDate") Date nowDate);

    /**
     * 新增排班
     * @param schedulingFrom 排班信息
     * @return boolean
     */
    boolean addScheduling(SchedulingFrom schedulingFrom);

    /**
     * 删除排班
     * @param scheduledInfoId 排班id
     * @return boolean
     */
    boolean deleteScheduling(String scheduledInfoId);

    /**
     * 修改排班
     * @param schedulingFrom 排班信息
     * @return boolean
     */
    boolean updateScheduling(SchedulingFrom schedulingFrom);

    /**
     * 新增监控对象排班
     * @param schedulingRelationMonitorFormList 监控对象排班信息
     * @return boolean
     */
    boolean addMonitorScheduling(List<SchedulingRelationMonitorForm> schedulingRelationMonitorFormList);

    /**
     * 删除排班关联监控对象
     * @param scheduledInfoId 排班id
     * @param monitorIdList   监控对象id
     * @return boolean
     */
    boolean deleteSchedulingRelationMonitor(@Param("scheduledInfoId") String scheduledInfoId,
        @Param("monitorIdList") List<String> monitorIdList);

    /**
     * 获得排班关联监控对象
     * @param scheduledInfoId 排班id
     * @return List<SchedulingRelationMonitorInfo>
     */
    List<SchedulingRelationMonitorInfo> getSchedulingRelationMonitorInfoListById(String scheduledInfoId);

    /**
     * 获得排班关联监控对象
     * @param scheduledInfoIdList 排班id集合
     * @return List<SchedulingRelationMonitorInfo>
     */
    List<SchedulingRelationMonitorInfo> getSchedulingRelationMonitorInfoListByIdList(List<String> scheduledInfoIdList);

    /**
     * 删除排班管理的监控对象
     * @param scheduledInfoId 排班id
     * @return boolean
     */
    boolean deleteMonitorScheduling(String scheduledInfoId);

    /**
     * 新增排班项
     * @param schedulingItemFormList 排班项信息
     * @return boolean
     */
    boolean addSchedulingItem(List<SchedulingItemForm> schedulingItemFormList);

    /**
     * 获得排班的排班项
     * @param scheduledInfoId 排班id
     * @return List<SchedulingItemInfo>
     */
    List<SchedulingItemInfo> getSchedulingItemInfoListById(String scheduledInfoId);

    /**
     * 获得排班的排班项
     * @param scheduledInfoIdList 排班id集合
     * @return List<SchedulingItemInfo>
     */
    List<SchedulingItemInfo> getSchedulingItemInfoListByIdList(List<String> scheduledInfoIdList);

    /**
     * 删除排班管理的排班项
     * @param scheduledInfoId 排班项
     * @return boolean
     */
    boolean deleteSchedulingItem(String scheduledInfoId);

    /**
     * 获得排班信息
     * @param scheduledInfoId 排班id
     * @return SchedulingInfo
     */
    SchedulingInfo getSchedulingInfoById(String scheduledInfoId);

    /**
     * 获得排班信息
     * @param scheduledName 排班名称
     * @return SchedulingInfo
     */
    List<SchedulingInfo> getSchedulingInfoByName(String scheduledName);

    /**
     * 修改排班结束时间为当前日期(强制结束)
     * @param scheduledInfoId    排班id
     * @param nowDate            当前日期
     * @param updateDataUsername 修改人
     * @param updateDataTime     修改时间
     * @return boolean
     */
    boolean updateSchedulingEndDateToNowDate(@Param("scheduledInfoId") String scheduledInfoId,
        @Param("nowDate") Date nowDate, @Param("updateDataUsername") String updateDataUsername,
        @Param("updateDataTime") Date updateDataTime);

    /**
     * 修改排班关联监控对象的排班结束时间为当前日期
     * @param scheduledInfoId 排班id
     * @param nowDate         当前日期
     * @return boolean
     */
    boolean updateSchedulingRelationMonitorEndDateToNowDate(@Param("scheduledInfoId") String scheduledInfoId,
        @Param("nowDate") Date nowDate);

    /**
     * 获得需要计算离线报表的排班id
     * @param nowDate 当前日期
     * @param weekDay 星期几
     * @return List<String>
     */
    List<String> getNeedCalculateOfflineReportScheduledId(@Param("nowDate") String nowDate,
        @Param("weekDay") String weekDay);

    void deleteMonitorScheduByMonitorIds(@Param("monitorIdList") List<String> monitorIdList);
}

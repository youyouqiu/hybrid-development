package com.zw.lkyw.service.positioningStatistics;

import com.zw.lkyw.domain.positioningStatistics.AllEnterpriseInfo;
import com.zw.lkyw.domain.positioningStatistics.ExceptionInfoQueryParam;
import com.zw.lkyw.domain.positioningStatistics.ExceptionInfoResult;
import com.zw.lkyw.domain.positioningStatistics.ExceptionListQueryParam;
import com.zw.lkyw.domain.positioningStatistics.ExceptionPositioningResult;
import com.zw.lkyw.domain.positioningStatistics.GroupListQueryParam;
import com.zw.lkyw.domain.positioningStatistics.GroupPositioningResult;
import com.zw.lkyw.domain.positioningStatistics.MonitorInterruptDetailInfo;
import com.zw.lkyw.domain.positioningStatistics.MonitorOfflineDetailInfo;
import com.zw.lkyw.domain.positioningStatistics.MonitorPositioningInfo;
import com.zw.lkyw.domain.positioningStatistics.MonthListQueryParam;
import com.zw.lkyw.domain.positioningStatistics.MonthPositioningResult;
import com.zw.platform.util.common.PageGridBean;

import java.util.List;

public interface PositioningStatisticsService {
    /**
     * 企业车辆定位统计
     * @param param 查询参数
     */
    PageGridBean enterpriseList(GroupListQueryParam param) throws Exception;


    /**
     * 企业车辆定位详情查询
     * @param param 查询参数
     */
    PageGridBean enterpriseLocationInfo(GroupListQueryParam param) throws Exception;

    PageGridBean enterpriseUnLocationInfo(GroupListQueryParam param) throws Exception;

    PageGridBean enterpriseInterruptInfo(GroupListQueryParam param) throws Exception;

    PageGridBean enterpriseOfflineInfo(GroupListQueryParam param) throws Exception;

    PageGridBean monthPositioningList(MonthListQueryParam param) throws Exception;

    PageGridBean exceptionPositioningList(ExceptionListQueryParam param) throws Exception;

    PageGridBean exceptionPositioningInfo(ExceptionInfoQueryParam param) throws Exception;

    List<GroupPositioningResult> exportGroupList(GroupListQueryParam param) throws Exception;

    AllEnterpriseInfo exportAllGroupPositioning(GroupListQueryParam param)throws Exception;

    List<MonitorPositioningInfo> exportLocationPositioning(GroupListQueryParam param) throws Exception;

    List<MonitorPositioningInfo> exportUnLocationPositioning(GroupListQueryParam param) throws Exception;

    List<MonitorInterruptDetailInfo> exportInterruptInfo(GroupListQueryParam param) throws Exception;

    List<MonitorOfflineDetailInfo> exportOfflineInfo(GroupListQueryParam param) throws Exception;

    List<MonthPositioningResult> exportMonthPositioningList(MonthListQueryParam param) throws Exception;

    List<ExceptionPositioningResult> exportExceptionList(ExceptionListQueryParam param) throws Exception;

    List<ExceptionInfoResult> exportExceptionInfo(ExceptionInfoQueryParam param) throws Exception;
}

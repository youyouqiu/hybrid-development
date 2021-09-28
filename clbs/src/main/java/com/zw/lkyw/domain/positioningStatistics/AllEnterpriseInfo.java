package com.zw.lkyw.domain.positioningStatistics;

import lombok.Data;

import java.util.List;

@Data
public class AllEnterpriseInfo {
    private List<MonitorPositioningInfo> locationResultList;
    private List<MonitorPositioningInfo> unLocationResultList;
    private List<MonitorInterruptDetailInfo> enterpriseInterruptList;
    private List<MonitorOfflineDetailInfo> offlineDetailResultList;
}

package com.zw.app.service.webMaster.statistics;

import java.util.Map;

/**
 * @author zhouzongbo on 2019/1/7 18:21
 */
public interface AppParkingReportService {

    Map<String,Object> findParkingDetailList(String monitorIds, String startTime, String endTime) throws Exception;

    Map<String,Object> findSingleMonitorParkingList(String monitorIds, String startTime, String endTime) throws Exception;
}

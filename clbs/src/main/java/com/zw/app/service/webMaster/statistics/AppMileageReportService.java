package com.zw.app.service.webMaster.statistics;

import java.util.Map;

/**
 * @author zhouzongbo on 2019/1/7 18:21
 */
public interface AppMileageReportService {

    Map<String,Object> findTravelDetailList(String monitorIds, String startTime, String endTime) throws Exception;

    Map<String,Object> findSingleMonitorList(String monitorIds, String startTime, String endTime) throws Exception;
}

package com.zw.platform.service.monitoring;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.domain.vas.monitoring.RefrigeratorForm;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * 温湿度监控service
 */
@Service
public interface HumitureService {
    /**
     * 根据车辆id、开始时间和结束时间查询温湿度数据
     */
    List<RefrigeratorForm> getTempDtaAndHumData(String vehicleId, String starTime, String endTime) throws Exception;

    /**
     * 根据监控对象id查询监控对象最后一条位置信息的温湿度数据
     */
    JSONObject getMonitorLastLocationById(String vehicleId) throws Exception;
}

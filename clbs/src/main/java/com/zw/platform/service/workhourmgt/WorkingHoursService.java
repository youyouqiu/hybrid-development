package com.zw.platform.service.workhourmgt;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.domain.vas.workhourmgt.VibrationSensorBind;
import com.zw.platform.domain.vas.workhourmgt.query.WorkHourDataSource;

import java.util.List;

/**
 * 工时统计
 * Created by Tdz on 2016/9/27.
 */
public interface WorkingHoursService {
    List<WorkHourDataSource> getAllInfo(String band, String startTime, String endTime) throws Exception;

    VibrationSensorBind getThresholds(String vehicleId);

    JSONObject getWorkHours(String type, String band, String startTime, String endTime) throws Exception;
}

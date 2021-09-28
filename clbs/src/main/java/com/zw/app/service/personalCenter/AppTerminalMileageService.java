package com.zw.app.service.personalCenter;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.domain.reportManagement.TerminalMileReport;

import java.util.List;

/**
 * @author lijie
 * @version 1.0
 * @date 2019/10/10 10:58
 */
public interface AppTerminalMileageService {

    /**
     * app终端里程统计数据
     *
     * @param vehicleId
     * @param startTime
     * @param endTime
     * @return
     */
    JSONObject showMileageReportList(String vehicleId, String startTime, String endTime);

    /**
     * app单个车的终端里程统计
     *
     * @param vehicleId
     * @param startTime
     * @param endTime
     * @return
     */
    List<TerminalMileReport> showMileDetailReportList(String vehicleId, String startTime, String endTime);


}

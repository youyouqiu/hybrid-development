package com.zw.platform.service.monitoring;


import com.alibaba.fastjson.JSONObject;
import com.zw.platform.domain.vas.monitoring.form.T808_0x8202;

import javax.servlet.http.HttpServletResponse;


/**
 * Created by LiaoYuecai on 2016/10/14.
 */
public interface RealTimeService {
    String getGroups(String vehicleId) throws Exception;

    String getParametersTrace(String vehicleId, T808_0x8202 ptf) throws Exception;

    void exportKML(String[] lineArr, HttpServletResponse response) throws Exception;

    JSONObject getFenceInfoBySendId(Integer sendId, String veicleId) throws Exception;
}

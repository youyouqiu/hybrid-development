package com.zw.app.service.personalCenter;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * @author lijie
 * @version 1.0
 * @date 2019/10/12 10:58
 */
public interface AppOilMileageService {

    /**
     * app油耗里程绑定了传感器的监控对象
     *
     * @param page
     * @param pageSize
     * @param defaultSize
     * @return
     */
    JSONObject findReferenceVehicleSeven(Long page, Long pageSize, Long defaultSize) throws Exception;


    /**
     * 获取app油耗里程数据
     *
     * @param vehicleId
     * @param startTime
     * @param endTime
     * @return
     */
    JSONObject getOilMileageInfos(String vehicleId, String startTime, String endTime) throws Exception;

    /**
     * 获取app油耗里程数据
     *
     * @param vehicleId
     * @param startTime
     * @param endTime
     * @return
     */
    JSONArray getOilMileageDetail(String vehicleId, String startTime, String endTime) throws Exception;


}

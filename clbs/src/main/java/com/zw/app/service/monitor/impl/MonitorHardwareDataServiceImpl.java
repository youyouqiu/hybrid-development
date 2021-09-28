package com.zw.app.service.monitor.impl;

import com.alibaba.fastjson.JSONObject;
import com.zw.app.domain.monitor.AppTravelAndStopData;
import com.zw.app.service.monitor.MonitorHardwareDataService;
import com.zw.app.util.AppParamCheckUtil;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.oil.Positional;
import com.zw.platform.util.paas.PaasCloudUrlUtil;
import com.zw.platform.util.report.PaasCloudHBaseAccessEnum;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @author CJY
 */
@Service
public class MonitorHardwareDataServiceImpl implements MonitorHardwareDataService {

    /**
     * 标准日期转换格式
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Override
    public JSONObject getStopHistoryData(String monitorId, String startTime, String endTime) throws Exception {
        if (!AppParamCheckUtil.check64String(monitorId) || !AppParamCheckUtil.checkDate(startTime, 1)
            || !AppParamCheckUtil.checkDate(endTime, 1)) {
            return null;
        }
        // 初始化参数
        //最终返回实体
        JSONObject result = new JSONObject();
        // 转换查询参数
        long start = DateUtils.parseDate(startTime, DATE_FORMAT).getTime() / 1000;
        long end = DateUtils.parseDate(endTime, DATE_FORMAT).getTime() / 1000;
        // 获取位置历史数据并计算停止数据
        // 获取位置历史数据
        List<Positional> locationInfos = this.listStopHistoryData(monitorId, start, end);
        if (locationInfos == null || locationInfos.size() <= 0) {
            return result;
        }
        List<AppTravelAndStopData> locationData = getCalculateNeedData(locationInfos, monitorId);
        locationInfos.clear();
        // 执行算法
        calculateDrivingOrStopStatus(locationData);
        // 组装返回值
        if (locationData.size() > 0) {
            result.put("track", locationData);
        }
        return result;
    }

    private List<Positional> listStopHistoryData(String monitorId, long start, long end) {
        Map<String, String> params = new HashMap<>(8);
        params.put("vehicleId", monitorId);
        params.put("startTime", String.valueOf(start));
        params.put("endTime", String.valueOf(end));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.LIST_STOP_HISTORY_DATA, params);
        return PaasCloudUrlUtil.getResultListData(str, Positional.class);
    }

    private List<AppTravelAndStopData> getCalculateNeedData(List<Positional> locationInfos, String vehicleId) {
        List<AppTravelAndStopData> locationData = new ArrayList<>();
        if (CollectionUtils.isEmpty(locationInfos)) {
            return locationData;
        }
        /* 判断监控对象是否绑定里程传感器 */
        Boolean flogKey = RedisHelper.isContainsKey(HistoryRedisKeyEnum.SENSOR_MESSAGE.of(vehicleId));
        locationData = locationInfos.stream().map((e) -> {
            double mileage;
            double speed;
            if (flogKey) {
                mileage = e.getMileageTotal() == null ? 0.0 : e.getMileageTotal();
                speed = e.getMileageSpeed() == null ? 0.0 : e.getMileageSpeed();
            } else {
                mileage = e.getGpsMile() == null ? 0.0 : Double.parseDouble(e.getGpsMile());
                speed = e.getSpeed() == null ? 0.0 : Double.parseDouble(e.getSpeed());
            }
            AppTravelAndStopData data = new AppTravelAndStopData();
            data.setTime(e.getVtime());
            data.setMileage(mileage);
            data.setSpeed(speed);
            return data;
        }).collect(Collectors.toList());
        return locationData;
    }

    /**
     * 计算行驶点和停止点
     */
    private void calculateDrivingOrStopStatus(List<AppTravelAndStopData> locationData) {
        /* 执行算法 */
        AppTravelAndStopData history;
        Integer pointStatus;
        // 初始化第一条数据的状态
        initLocationStatus(locationData.get(0));
        for (int index = 0, length = locationData.size(); index < length; index++) {
            history = locationData.get(index);
            if (index > 0) {
                // 对比前后两个点的时间间隔是否大于5分钟
                // 前一个点
                AppTravelAndStopData previousPositional = locationData.get(index - 1);
                // 如果两个点的时间差大于5分钟 行驶状态重新初始化
                if (history.getTime() - previousPositional.getTime() > 300) {
                    initLocationStatus(history);
                }
            }
            pointStatus = history.getStatus();
            if (pointStatus != null) {
                // 如果当前点是停止,取当前点的后三个点的速度,如果三个点的速度都大于5km/h,
                // 则这三个点是行驶状态,否则,三个点都是停止状态
                // 如果当前点是行驶,取当前点的后五个点的速度,如果五个点的速度都小于5km/h,
                // 则这五个点是停止状态,否则, 五个点都是行驶状态
                setFollowUpDataStatus(locationData, pointStatus, index);
            }
        }
    }

    private void initLocationStatus(AppTravelAndStopData history) {
        Double speed = history.getSpeed();
        // 停止
        if (speed <= 5) {
            history.setStatus(2);
        } else { // 行驶
            history.setStatus(1);
        }
    }

    /**
     * 设置后续数据的行驶/停止状态
     */
    private void setFollowUpDataStatus(List<AppTravelAndStopData> locationData, Integer status, Integer index) {
        if (CollectionUtils.isEmpty(locationData) || status == null || index == null) {
            return;
        }
        int subListNumber;
        // 当前点的状态为行驶
        if (status == 1) {
            subListNumber = 5;
        } else { // 当前点的装维为停止
            subListNumber = 3;
        }
        int subListStartIndex = index + 1;
        int subListEndIndex = subListStartIndex + subListNumber;
        if (subListEndIndex >= locationData.size()) {
            subListEndIndex = locationData.size();
        }
        List<AppTravelAndStopData> followUpData = locationData.subList(subListStartIndex, subListEndIndex);
        if (subListEndIndex == locationData.size() && (subListEndIndex - subListStartIndex) < subListNumber) {
            setFollowUpDataStatus(followUpData, status);
            return;
        }
        List<AppTravelAndStopData> conformSpeedData = followUpData.stream().filter((e) -> {
            if (status == 1) {
                return e.getSpeed() <= 5;
            } else {
                return e.getSpeed() > 5;
            }
        }).collect(Collectors.toList());
        if (conformSpeedData.size() == subListNumber) {
            // 1:行驶 2:停止
            int newStatus = 1;
            // 将取到的5个点置为停止
            if (status == 1) {
                newStatus = 2;
            }
            setFollowUpDataStatus(followUpData, newStatus);
        } else {
            setFollowUpDataStatus(followUpData, status);
        }
    }

    private void setFollowUpDataStatus(List<AppTravelAndStopData> followUpData, int status) {
        if (CollectionUtils.isEmpty(followUpData)) {
            return;
        }
        followUpData.forEach(data -> data.setStatus(status));
    }
}

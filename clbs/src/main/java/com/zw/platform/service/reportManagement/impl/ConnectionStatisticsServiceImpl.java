package com.zw.platform.service.reportManagement.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cb.platform.domain.query.ConnectionStaticsQuery;
import com.cb.platform.util.page.PassCloudResultBean;
import com.zw.platform.basic.domain.BaseKvDo;
import com.zw.platform.basic.service.MonitorService;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.service.reportManagement.ConnectionStatisticsService;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.report.PaasCloudUrlEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/***
 @Author lijie
 @Date 2021/1/19 14:04
 @Description 连接信息报表service
 @version 1.0
 **/
@Service
public class ConnectionStatisticsServiceImpl implements ConnectionStatisticsService {


    @Autowired
    MonitorService monitorService;

    /**
     * 时间转换类型 1：平台  2：车
     */
    private static final Integer FORMAT_TYPE_PLATFORM = 1;

    private static final Integer FORMAT_TYPE_MONITOR = 2;

    /**
     * 查询与政府监管平台连接情况
     * @param query
     * @return
     */
    @Override
    public PassCloudResultBean platformList(ConnectionStaticsQuery query) {
        String queryResult =
                HttpClientUtil.send(PaasCloudUrlEnum.CONNECTION_STATISTICS_PLATFORM_LIST_URL, query.getPlatformParam());
        return PassCloudResultBean.getPageInstanceSingle(queryResult, (item) -> setTimeFormat(item,
                FORMAT_TYPE_PLATFORM));
    }

    /**
     * 查询与政府监管平台连接情况详情
     * @param query
     * @return
     */
    @Override
    public PassCloudResultBean platformDetailList(ConnectionStaticsQuery query) {
        String queryResult = HttpClientUtil
                .send(PaasCloudUrlEnum.CONNECTION_STATISTICS_PLATFORM_DETAIL_LIST_URL, query.getPlatformDetailParam());
        return PassCloudResultBean.getPageInstanceSingle(queryResult, (item) -> setTimeFormat(item,
                FORMAT_TYPE_PLATFORM));
    }

    /**
     * 查询与车载终端连接情况
     * @param query
     * @return
     */
    @Override
    public PassCloudResultBean monitorList(ConnectionStaticsQuery query) {
        if (StringUtils.isNotBlank(query.getSimpleQueryParam())) {
            Map<String, BaseKvDo<String, String>> monitorIdNameMap = monitorService
                .getMonitorIdNameMap(Arrays.asList(query.getMonitorIds().split(",")), query.getSimpleQueryParam());
            query.setMonitorIds(StringUtils.join(monitorIdNameMap.keySet(), ","));
        }
        String queryResult =
            HttpClientUtil.send(PaasCloudUrlEnum.CONNECTION_STATISTICS_MONITOR_LIST_URL, query.getMonitorParam());
        return PassCloudResultBean.getDataInstance(queryResult, (item) -> setTimeFormat(item, FORMAT_TYPE_MONITOR));
    }

    /**
     * 查询与车载终端连接情况详情
     * @param query
     * @return
     */
    @Override
    public PassCloudResultBean monitorDetailList(ConnectionStaticsQuery query) {
        String queryResult = HttpClientUtil
            .send(PaasCloudUrlEnum.CONNECTION_STATISTICS_MONITOR_DETAIL_LIST_URL, query.getMonitorDetailParam());
        return PassCloudResultBean.getDataInstance(queryResult, this::setMonitorDetailTimeFormat);
    }

    public JSONObject setTimeFormat(String item, Integer formatType) {
        if (StringUtils.isEmpty(item)) {
            return null;
        }
        JSONObject object = JSONObject.parseObject(item, JSONObject.class);
        Long onlineDuration = object.getLong("onlineDuration");
        if (FORMAT_TYPE_MONITOR.equals(formatType)) {
            object.put("onlineDuration", onlineDuration == null
                    ? null : DateUtil.formatTime(onlineDuration * 1000 * 60));
            Long breakDuration = object.getLong("breakDuration");
            object.put("breakDuration", breakDuration == null
                    ? null : DateUtil.formatTime(breakDuration * 1000 * 60));
            Long offlineDuration = object.getLong("offlineDuration");
            object.put("offlineDuration", offlineDuration == null
                    ? null : DateUtil.formatTime(offlineDuration * 1000 * 60));
        } else {

            object.put("onlineDuration", onlineDuration == null
                    ? null : DateUtil.formatTime(onlineDuration * 1000));
            Long breakDuration = object.getLong("breakDuration");
            object.put("breakDuration", breakDuration == null
                    ? null : DateUtil.formatTime(breakDuration * 1000));
            Long offlineDuration = object.getLong("offlineDuration");
            object.put("offlineDuration", offlineDuration == null
                    ? null : DateUtil.formatTime(offlineDuration * 1000));
        }
        String connectionDate = object.getString("connectionDate");
        String day = object.getString("day");
        try {
            connectionDate = connectionDate == null ? null :
                DateUtil.formatDate(connectionDate, DateUtil.DATE_FORMAT, DateUtil.DATE_Y_M_D_FORMAT);
            day = day == null ? null : DateUtil.formatDate(day, DateUtil.DATE_YMD_FORMAT, DateUtil.DATE_Y_M_D_FORMAT);
            object.put("connectionDate", connectionDate);
            object.put("day", day);
        } catch (Exception e) {
            object.put("connectionDate", null);
            object.put("day", null);
        }
        return object;
    }

    public JSONObject setMonitorDetailTimeFormat(String item) {
        JSONObject jsonObject = JSON.parseObject(item, JSONObject.class);
        JSONArray jsonArray = jsonObject.getJSONArray("dayDetailList");
        final List<JSONObject> parsedArray = jsonArray.stream()
                .map(Object::toString)
                .map((String item1) -> setTimeFormat(item1, FORMAT_TYPE_MONITOR))
                .collect(Collectors.toList());
        jsonObject.put("dayDetailList", parsedArray);
        return jsonObject;
    }

}

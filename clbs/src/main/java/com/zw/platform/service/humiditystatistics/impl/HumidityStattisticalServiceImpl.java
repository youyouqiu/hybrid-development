package com.zw.platform.service.humiditystatistics.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.vas.f3.HumidityStatisics;
import com.zw.platform.service.humiditystatistics.HumidityStattisticalService;
import com.zw.platform.util.report.PaasCloudUrlEnum;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.zw.platform.basic.core.RedisHelper.SIX_HOUR_REDIS_EXPIRE;

/**
 * @author Created by Administrator on 2017/7/17.
 */
@Service
public class HumidityStattisticalServiceImpl implements HumidityStattisticalService {

    /**
     * 日期转换格式
     */
    private static final String DATE_FORMAT1 = "yyyy-MM-dd HH:mm:ss";

    @Override
    public List<HumidityStatisics> findHumidityByVehicleId(String startTime, String endTime, String vehicleId)
        throws Exception {
        long start = DateUtils.parseDate(startTime, DATE_FORMAT1).getTime() / 1000;
        long end = DateUtils.parseDate(endTime, DATE_FORMAT1).getTime() / 1000;
        if (start == 0 || end == 0 || StringUtils.isEmpty(vehicleId)) {
            return Collections.emptyList();
        }
        String startStr = startTime.replaceAll("-", "").replaceAll(":", "").replaceAll(" ", "");
        String endStr = endTime.replaceAll("-", "").replaceAll(":", "").replaceAll(" ", "");
        Map<String, String> param = new HashMap<>(16);
        param.put("monitorId", vehicleId);
        param.put("startTime", startStr);
        param.put("endTime", endStr);
        param.put("type", "0");
        //湿度所有数据
        String resultStr = HttpClientUtil.send(PaasCloudUrlEnum.HUMIDITY_SENSOR, param);
        JSONObject humidityData = JSONObject.parseObject(resultStr);
        param.put("type", "1");
        //高湿度数据
        String humHighStatistics = HttpClientUtil.send(PaasCloudUrlEnum.HUMIDITY_SENSOR, param);
        JSONObject highHumidityData = JSONObject.parseObject(humHighStatistics);
        param.put("type", "2");
        //低湿度数据
        String humLowStatistics = HttpClientUtil.send(PaasCloudUrlEnum.HUMIDITY_SENSOR, param);
        JSONObject lowHumidityData = JSONObject.parseObject(humLowStatistics);
        List<HumidityStatisics> humidityStatisticsList =
            JSONObject.parseArray(humidityData.getString("data"), HumidityStatisics.class);
        if (CollectionUtils.isEmpty(humidityStatisticsList)) {
            return Collections.emptyList();
        }
        List<HumidityStatisics> humHighStatisticsList =
            JSONObject.parseArray(highHumidityData.getString("data"), HumidityStatisics.class);

        List<HumidityStatisics> humLowStatisticsList =
            JSONObject.parseArray(lowHumidityData.getString("data"), HumidityStatisics.class);
        final RedisKey key = HistoryRedisKeyEnum.STATS_HUM.of(vehicleId, start, end, "");
        final RedisKey keyHigh = HistoryRedisKeyEnum.STATS_HUM.of(vehicleId, start, end, "-h");
        final RedisKey keyLow = HistoryRedisKeyEnum.STATS_HUM.of(vehicleId, start, end, "-l");
        // PAAS提供的数据是反的
        RedisHelper.delete(key);
        RedisHelper.delete(keyHigh);
        RedisHelper.delete(keyLow);
        RedisHelper.addObjectToList(key, humidityStatisticsList, SIX_HOUR_REDIS_EXPIRE);
        RedisHelper.addObjectToList(keyHigh, humHighStatisticsList, SIX_HOUR_REDIS_EXPIRE);
        RedisHelper.addObjectToList(keyLow, humLowStatisticsList, SIX_HOUR_REDIS_EXPIRE);
        // 这里前端自己做了取反，所以反着给前端
        return humidityStatisticsList;
    }

}
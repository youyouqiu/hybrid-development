package com.zw.platform.service.oilmgt.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.oil.F3HighPrecisionReport;
import com.zw.platform.domain.oil.VoltageInfo;
import com.zw.platform.service.oilmgt.F3HighPrecisionReportService;
import com.zw.platform.util.StrUtil;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.platform.util.excel.ExportExcelParam;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.platform.util.paas.PaasCloudUrlUtil;
import com.zw.platform.util.report.PaasCloudHBaseAccessEnum;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
 @Author zhengjc
 @Date 2019/11/15 16:40
 @Description f3高精度报表
 @version 1.0
 **/
@Service
public class F3HighPrecisionReportServiceImpl implements F3HighPrecisionReportService {
    private static Logger log = LogManager.getLogger(F3HighPrecisionReportServiceImpl.class);

    @Override
    public List<F3HighPrecisionReport> getF3HighPrecisionReport(String vehicleId, String startTime, String endTime) {
        List<F3HighPrecisionReport> list = new ArrayList<>();
        long start = getSecond(startTime);
        //如果查询开始时间大于当前时间 直接返回
        if (start > System.currentTimeMillis() / 1000) {
            return list;
        }
        long end = getSecond(endTime);
        final RedisKey redisCacheKey = HistoryRedisKeyEnum.STATS_F3.of(vehicleId, start, end);
        //查询今天的数据实时查和以前不知一整天的数据
        if (isQueryToday(startTime, endTime, new SimpleDateFormat("yyyy-MM-dd").format(new Date()))
                || !isWholeData(startTime, endTime)) {
            list = getF3HighPrecisionReports(vehicleId, start, end);
            assembleData(list, VehicleUtil.getBindInfoByRedis(vehicleId));
        } else {
            //查询以前的数据
            if (RedisHelper.isContainsKey(redisCacheKey)) {
                return RedisHelper.getListObj(redisCacheKey, 0, -1);
            }
            list = getF3HighPrecisionReports(vehicleId, start, end);
            assembleData(list, VehicleUtil.getBindInfoByRedis(vehicleId));
            RedisHelper.delete(redisCacheKey);
            RedisHelper.addObjectToList(redisCacheKey, Lists.reverse(list), RedisHelper.SIX_HOUR_REDIS_EXPIRE);
        }
        return list;
    }

    private List<F3HighPrecisionReport> getF3HighPrecisionReports(String vehicleId, long start, long end) {
        final Map<String, String> params = new HashMap<>(8);
        params.put("vehicleId", vehicleId);
        params.put("startTime", String.valueOf(start));
        params.put("endTime", String.valueOf(end));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_F3_HIGH_PRECISION_REPORTS, params);
        return PaasCloudUrlUtil.getResultListData(str, F3HighPrecisionReport.class);
    }

    private void assembleData(List<F3HighPrecisionReport> list, BindDTO vehicle) {
        for (F3HighPrecisionReport data : list) {
            data.initData(vehicle);
        }
    }

    private boolean isWholeData(String startTime, String endTime) {
        return startTime.substring(11).equals("00:00:00") && endTime.substring(11).equals("23:59:59");
    }

    private boolean isQueryToday(String startTime, String endTime, String date) {
        return startTime.contains(date) || endTime.contains(date);
    }

    private String getRedisCacheKey(String vehicleId, long start, long end) {
        return "f3hpr-" + vehicleId + "-" + start + "-" + end;
    }

    private static long getSecond(String time) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(time).getTime() / 1000;
        } catch (ParseException e) {
            log.error("日期转换异常", e);
        }
        return 0;
    }

    @Override
    public void exportF3HighPrecisionReport(String vehicleId, String startTime, String endTime,
        HttpServletResponse response) throws IOException {
        List<F3HighPrecisionReport> f3HighPrecisionReports = getF3HighPrecisionReport(vehicleId, startTime, endTime);
        ExportExcelUtil.setResponseHead(response, "F3高精度报表");
        ExportExcelUtil.export(new ExportExcelParam("", 1, f3HighPrecisionReports, F3HighPrecisionReport.class, null,
            response.getOutputStream()));
    }

    @Override
    public VoltageInfo getVoltageInfo(String vehicleId) {
        final RedisKey redisKey = HistoryRedisKeyEnum.MONITOR_LOCATION.of(vehicleId);
        String cacheInfo = RedisHelper.getString(redisKey);
        JSONObject terminalCheck = null;
        if (StrUtil.isNotBlank(cacheInfo)) {
            JSONObject jsonObject = JSONObject.parseObject(cacheInfo);
            terminalCheck = jsonObject.getJSONObject("data").getJSONObject("msgBody").getJSONObject("terminalcheck");
        }
        return VoltageInfo.getInstance(terminalCheck);
    }
}

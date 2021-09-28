package com.zw.platform.service.winchstatistics.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.vas.f3.TransdusermonitorSet;
import com.zw.platform.domain.vas.f3.WinchStatistics;
import com.zw.platform.repository.vas.VeerStatisticalDao;
import com.zw.platform.service.winchstatistics.VeerStatisticalService;
import com.zw.platform.util.report.PaasCloudUrlEnum;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class VeerStatisticalServiceImpl implements VeerStatisticalService {
    private static Logger log = LogManager.getLogger(VeerStatisticalServiceImpl.class);

    @Autowired
    private VeerStatisticalDao veerStatisticalDao;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public List<TransdusermonitorSet> getVehiceInfo(String groupId) {
        // 根据userName获取userId
        String userId = SystemHelper.getCurrentUser().getId().toString();
        List<TransdusermonitorSet> list = new ArrayList<TransdusermonitorSet>();
        try {
            list = veerStatisticalDao.getVehiceInfo(userId, groupId);
        } catch (Exception e) {
            log.error("获取车辆信息异常", e);
        }
        return list;
    }

    @Override
    public List<WinchStatistics> getInfoDtails(String vehicleId, String startTime, String endTime) throws Exception {
        List<WinchStatistics> winchStatisticsList = new ArrayList<>();
        // 正转数据
        List<WinchStatistics> positiveStatisticsList = new ArrayList<>();
        // 反转数据
        List<WinchStatistics> inversionStatisticsList = new ArrayList<>();
        boolean isUpdateData = false;
        long startTimes;
        long endTimes;
        startTimes = sdf.parse(startTime).getTime() / 1000;
        //如果查询开始时间大于当前时间 直接返回
        if (startTimes > (System.currentTimeMillis() / 1000)) {
            return winchStatisticsList;
        }
        endTimes = sdf.parse(endTime).getTime() / 1000;
        final RedisKey key = HistoryRedisKeyEnum.STATS_VEER.of(vehicleId, startTimes, endTimes, "");
        final RedisKey keyPos = HistoryRedisKeyEnum.STATS_VEER.of(vehicleId, startTimes, endTimes, "-p");
        final RedisKey keyInv = HistoryRedisKeyEnum.STATS_VEER.of(vehicleId, startTimes, endTimes, "-i");
        final RedisKey keySearch = HistoryRedisKeyEnum.STATS_VEER_SEARCH.of(vehicleId, startTimes, endTimes);
        boolean isContainsKey = RedisHelper.isContainsKey(key);
        boolean isContainsPosiKey = RedisHelper.isContainsKey(keyPos);
        boolean isContainsInverKey = RedisHelper.isContainsKey(keyInv);
        boolean isContainsSearchTimeKey = RedisHelper.isContainsKey(keySearch);
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        if (isContainsSearchTimeKey) {
            String searchTimeStr = RedisHelper.getString(keySearch);
            //如果查询时间不是当天你的就去hbase的数据
            if (!date.equals(searchTimeStr)) {
                isUpdateData = true;
            }
        }
        if (!isContainsKey || startTime.contains(date) || endTime.contains(date) || isUpdateData) {
            if (!"".equals(vehicleId) && startTimes != 0 && endTimes != 0) {

                String startStr = startTime.replaceAll("-", "").replaceAll(":", "").replaceAll(" ", "");
                String endStr = endTime.replaceAll("-", "").replaceAll(":", "").replaceAll(" ", "");
                Map<String, String> param = new HashMap<>();
                param.put("monitorId", vehicleId);
                param.put("startTime", startStr);
                param.put("endTime", endStr);
                param.put("type", "0");
                String result = HttpClientUtil.send(PaasCloudUrlEnum.FORWARD_AND_REVERSE_REPORT_URL, param);
                JSONObject obj = JSONObject.parseObject(result);
                //全部数据
                winchStatisticsList = JSONObject.parseArray(obj.getString("data"), WinchStatistics.class);
                param.put("type", "1");
                String positiveStatisticsResult =
                    HttpClientUtil.send(PaasCloudUrlEnum.FORWARD_AND_REVERSE_REPORT_URL, param);
                if (Objects.nonNull(positiveStatisticsResult)) {
                    JSONObject positiveStatisticsObj = JSONObject.parseObject(positiveStatisticsResult);
                    //正转数据
                    positiveStatisticsList =
                        JSONObject.parseArray(positiveStatisticsObj.getString("data"), WinchStatistics.class);
                }

                param.put("type", "2");
                String inversionStatisticsResult =
                    HttpClientUtil.send(PaasCloudUrlEnum.FORWARD_AND_REVERSE_REPORT_URL, param);
                if (Objects.nonNull(inversionStatisticsResult)) {
                    JSONObject inversionStatisticsObj = JSONObject.parseObject(inversionStatisticsResult);
                    //反转数据
                    inversionStatisticsList =
                        JSONObject.parseArray(inversionStatisticsObj.getString("data"), WinchStatistics.class);
                }

                if (isContainsKey) {
                    RedisHelper.delete(key);
                }
                if (isContainsPosiKey) {
                    RedisHelper.delete(keyPos);
                }
                if (isContainsInverKey) {
                    RedisHelper.delete(keyInv);
                }
                if (isContainsSearchTimeKey) {
                    RedisHelper.delete(keySearch);
                }
                RedisHelper.setString(keySearch, date);
                RedisHelper.addObjectToList(key, winchStatisticsList, RedisHelper.SIX_HOUR_REDIS_EXPIRE);
                RedisHelper.addObjectToList(keyPos, positiveStatisticsList, RedisHelper.SIX_HOUR_REDIS_EXPIRE);
                RedisHelper.addObjectToList(keyInv, inversionStatisticsList, RedisHelper.SIX_HOUR_REDIS_EXPIRE);
            }
        } else {
            winchStatisticsList = RedisHelper.getListObj(key, 1, -1);
        }
        return Lists.reverse(winchStatisticsList);
    }

}

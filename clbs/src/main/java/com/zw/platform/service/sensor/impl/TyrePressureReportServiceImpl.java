package com.zw.platform.service.sensor.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cb.platform.util.page.PassCloudResultBean;
import com.github.pagehelper.Page;
import com.google.common.collect.Lists;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.form.TyrePressureSettingForm;
import com.zw.platform.domain.statistic.StatisticQuery;
import com.zw.platform.domain.statistic.TyrePressureReportInfo;
import com.zw.platform.repository.vas.TyrePressureSettingDao;
import com.zw.platform.service.sensor.TyrePressureReportService;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.ZipUtil;
import com.zw.platform.util.report.PaasCloudUrlEnum;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class TyrePressureReportServiceImpl implements TyrePressureReportService {

    @Autowired
    private TyrePressureSettingDao tyrePressureSettingDao;

    @Override
    public JsonResultBean getTotalInfo(StatisticQuery query) throws Exception {
        TyrePressureSettingForm form = tyrePressureSettingDao.findTyrePressureSettingByVid(query.getVehicleId());
        Map<String, String> setMap = new HashMap<>();
        if (form != null) {
            Map<String, String> param = new HashMap<>();
            param.put("monitorId", query.getVehicleId());
            String startTime =
                DateUtil.formatDate(query.getStartTimeStr(), DateUtil.DATE_FORMAT_SHORT, DateUtil.DATE_FORMAT);
            String endTime =
                DateUtil.formatDate(query.getEndTimeStr(), DateUtil.DATE_FORMAT_SHORT, DateUtil.DATE_FORMAT);
            param.put("startTime", startTime);
            param.put("endTime", endTime);
            String sendResult = HttpClientUtil.send(PaasCloudUrlEnum.TYRE_PRESSURE_URL, param);
            PassCloudResultBean passCloudResultBean = PassCloudResultBean.getDataInstance(sendResult);
            Object data = passCloudResultBean.getData();
            if (!passCloudResultBean.isSuccess()) {
                return new JsonResultBean(JsonResultBean.FAULT, passCloudResultBean.getMessage());
            }
            if (Objects.nonNull(data)) {
                JSONArray jsonArray = JSONObject.parseArray(data.toString());
                setMap = assemblyData(jsonArray);
            }
            final RedisKey key = HistoryRedisKeyEnum.STATS_TIRE.of(SystemHelper.getCurrentUsername());
            RedisHelper.delete(key);
            RedisHelper.addToHash(key, setMap);
            return new JsonResultBean(form.getNumberOfTires());
        }
        return new JsonResultBean(JsonResultBean.FAULT, "改车胎压设置已解除");
    }

    /**
     * 组装胎压数据存入redis
     */
    private Map<String, String> assemblyData(JSONArray jsonArray) {
        Map<String, List<TyrePressureReportInfo>> resultMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(jsonArray)) {

            final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                JSONArray tirePressureList = object.getJSONArray("list");
                if (CollectionUtils.isNotEmpty(tirePressureList)) {
                    for (int j = 0; j < tirePressureList.size(); j++) {
                        JSONObject tirePressure = tirePressureList.getJSONObject(j);
                        TyrePressureReportInfo info = new TyrePressureReportInfo();
                        info.setPlateNumber(object.getString("monitorName"));
                        info.setVehicleId(object.getString("monitorId"));
                        Long vtime = object.getLong("vTime");
                        String time = format.format(new Date(vtime * 1000));
                        info.setVtime(vtime);
                        info.setVtimeStr(time);
                        info.setLongtitude(object.getString("longtitude"));
                        info.setLatitude(object.getString("latitude"));
                        info.setSpeed(object.getString("speed"));
                        info.setTotalMileage(object.getString("gpsMile"));
                        info.setTyreNumber(tirePressure.getInteger("number"));
                        info.setPressure(tirePressure.getString("pressure"));
                        info.setElectric(tirePressure.getString("electric"));
                        info.setTemperature(tirePressure.getString("temperature"));
                        //轮胎号为key
                        String key = "tyre_" + tirePressure.getString("number");
                        resultMap.computeIfAbsent(key, k -> new ArrayList<>()).add(info);
                    }
                }

            }
        }
        Map<String, String> setMap = new HashMap<>(resultMap.size());
        for (Map.Entry<String, List<TyrePressureReportInfo>> entry : resultMap.entrySet()) {
            List<TyrePressureReportInfo> list = Lists.reverse(entry.getValue());
            setMap.put(entry.getKey(), JSON.toJSONString(list));
        }
        return setMap;
    }

    @Override
    public JsonResultBean getChartInfo(StatisticQuery query) throws IOException {
        List<TyrePressureReportInfo> generalData = new ArrayList<>(); // 普通数据
        List<TyrePressureReportInfo> result = new ArrayList<>(); // 补充空数据的结果
        String field = "tyre_" + query.getTyreNumber();
        final RedisKey redisKey = HistoryRedisKeyEnum.STATS_TIRE.of(SystemHelper.getCurrentUsername());
        String value = RedisHelper.hget(redisKey, field);
        JSONArray jsonArray = JSONArray.parseArray(value);
        if (jsonArray != null) {
            for (Object o : jsonArray) {
                TyrePressureReportInfo info = JSON.parseObject(o.toString(), TyrePressureReportInfo.class);
                generalData.add(info);
            }
        }
        for (int i = generalData.size() - 1; i > 0; i--) {
            result.add(generalData.get(i));
            // 前一条数据时间
            Long beforeTime = generalData.get(i).getVtime();
            // 后一条数据时间
            Long afterTime = generalData.get(i - 1).getVtime();
            if ((afterTime - beforeTime) > 300) {
                //时间相差5分钟在中间插入空数据  30秒一条
                long num = ((afterTime - beforeTime)) / 300;
                for (long j = 0; j < num; j++) {
                    TyrePressureReportInfo blankData = new TyrePressureReportInfo();
                    result.add(blankData);
                }
            }
        }
        String resultZip = ZipUtil.compress(JSON.toJSONString(result));
        return new JsonResultBean(resultZip);
    }

    @Override
    public PageGridBean getFormInfo(StatisticQuery query) {
        Page<TyrePressureReportInfo> result = new Page<>();
        String field = "tyre_" + query.getTyreNumber();
        final RedisKey redisKey = HistoryRedisKeyEnum.STATS_TIRE.of(SystemHelper.getCurrentUsername());
        String value = RedisHelper.hget(redisKey, field);
        JSONArray jsonArray = JSONArray.parseArray(value);
        int start = query.getStart().intValue();
        int limit = query.getLimit().intValue();
        if (jsonArray != null) {
            for (int i = start, j = 1; i < jsonArray.size(); i++, j++) {
                if (j > limit) {
                    // 分页数
                    break;
                }
                TyrePressureReportInfo tyrePressureReportInfo =
                    JSON.parseObject(jsonArray.get(i).toString(), TyrePressureReportInfo.class);
                result.add(tyrePressureReportInfo);
            }
            return new PageGridBean(jsonArray.size(), result);
        }
        return new PageGridBean(result);
    }
}

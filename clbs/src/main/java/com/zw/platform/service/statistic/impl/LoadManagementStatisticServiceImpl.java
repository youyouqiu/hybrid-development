package com.zw.platform.service.statistic.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cb.platform.util.page.PassCloudResultBean;
import com.github.pagehelper.Page;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.statistic.StatisticQuery;
import com.zw.platform.domain.statistic.info.LoadManagementStatisticInfo;
import com.zw.platform.service.statistic.LoadManagementStatisticService;
import com.zw.platform.util.common.AddressUtil;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.RedisUtil;
import com.zw.platform.util.common.ZipUtil;
import com.zw.platform.util.excel.ExportExcelParam;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.platform.util.report.PaasCloudUrlEnum;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author zhouzongbo on 2018/9/10 15:30
 */
@Log4j2
@Service
public class LoadManagementStatisticServiceImpl implements LoadManagementStatisticService {

    @Override
    public JsonResultBean getLoadChartInfo(StatisticQuery query) throws Exception {
        Integer sensorSequence = query.getSensorSequence();
        JSONObject result = new JSONObject();
        Map<String, String> param = new HashMap<>();
        param.put("monitorId", query.getVehicleId());
        String startTime =
            DateUtil.formatDate(query.getStartTimeStr(), DateUtil.DATE_FORMAT_SHORT, DateUtil.DATE_FORMAT);
        String endTime = DateUtil.formatDate(query.getEndTimeStr(), DateUtil.DATE_FORMAT_SHORT, DateUtil.DATE_FORMAT);
        param.put("startTime", startTime);
        param.put("endTime", endTime);
        param.put("type", String.valueOf(sensorSequence));
        String sendResult = HttpClientUtil.send(PaasCloudUrlEnum.LOAD_INFO_URL, param);
        PassCloudResultBean passCloudResultBean = PassCloudResultBean.getDataInstance(sendResult);
        Object data = passCloudResultBean.getData();
        if (!passCloudResultBean.isSuccess()) {
            return new JsonResultBean(JsonResultBean.FAULT, passCloudResultBean.getMessage());
        }
        Map<Integer, List<LoadManagementStatisticInfo>> statusMap = new HashMap<>();
        // ??????????????????
        List<LoadManagementStatisticInfo> resultList = new ArrayList<>();
        if (Objects.nonNull(data)) {
            JSONObject jsonObject = JSONObject.parseObject(data.toString());
            if (Objects.nonNull(jsonObject)) {
                JSONArray loadList = JSONObject.parseArray(jsonObject.getString("loadList"));
                List<LoadManagementStatisticInfo> infos = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(loadList)) {
                    assemblyData(loadList, infos);
                }
                result.put("fullLoadTime", jsonObject.getString("fullLoadTime"));
                result.put("heavyLoadTime", jsonObject.getString("heavyLoadTime"));
                result.put("noLoadTime", jsonObject.getString("noLoadTime"));
                result.put("overLoadTime", jsonObject.getString("overLoadTime"));
                result.put("underLoadTime", jsonObject.getString("underLoadTime"));
                setBlankLoadInfo(infos, resultList);
                result.put("resultList", ZipUtil.compress(JSON.toJSONString(resultList)));
                // ??????????????????????????????, ??????redis????????????????????????
                resultList = resultList.stream().filter(load -> load != null && load.getEffectiveData() == 0)
                    .sorted(Comparator.comparing(LoadManagementStatisticInfo::getVtimeStr).reversed())
                    .collect(Collectors.toList());
                //?????????????????????,??????redis????????????????????????????????????????????????
                statusMap = resultList.stream().peek(this::setLoadStatus)
                    .collect(Collectors.groupingBy(LoadManagementStatisticInfo::getStatus));
            }
        }
        // sensorSequence ---> 0: ??????1; 1: ??????2
        final String username = SystemHelper.getCurrentUsername();
        final RedisKey infoKey = HistoryRedisKeyEnum.LOAD_SENSOR_INFO.of(username, sensorSequence);
        final boolean deleted = RedisHelper.delete(infoKey);
        if (deleted) {
            final RedisKey statusPatternKey =
                    HistoryRedisKeyEnum.LOAD_SENSOR_STATUS_PATTERN.of(sensorSequence, username);
            RedisHelper.delByPattern(statusPatternKey);
        }
        RedisHelper.addObjectToList(infoKey, resultList, RedisHelper.SIX_HOUR_REDIS_EXPIRE);
        for (Map.Entry<Integer, List<LoadManagementStatisticInfo>> entry : statusMap.entrySet()) {
            final RedisKey statusKey =
                    HistoryRedisKeyEnum.LOAD_SENSOR_STATUS.of(sensorSequence, username, entry.getKey());
            RedisHelper.addObjectToList(statusKey, entry.getValue(), RedisHelper.SIX_HOUR_REDIS_EXPIRE);
        }
        return new JsonResultBean(result);
    }

    private void assemblyData(JSONArray loadList, List<LoadManagementStatisticInfo> infos) throws Exception {
        for (int i = 0; i < loadList.size(); i++) {
            JSONObject object = loadList.getJSONObject(i);
            LoadManagementStatisticInfo info = new LoadManagementStatisticInfo();
            info.setPlateNumber(object.getString("monitorName"));
            info.setGroupName(object.getString("groupName"));
            info.setStatus(Integer.parseInt(object.getString("status")));
            info.setVtimeStr(
                DateUtil.formatDate(object.getString("vTimeStr"), DateUtil.DATE_FORMAT, DateUtil.DATE_FORMAT_SHORT));
            info.setContinueTimeStr(object.getString("continueTimeStr"));
            info.setInstanceWeight(object.getDouble("instanceWeight"));
            info.setAddress(object.getString("address"));
            info.setSpeed(object.getString("speed"));
            info.setLatitude(object.getString("latitude"));
            info.setLongtitude(object.getString("longtitude"));
            info.setWeightAd(object.getDouble("weightAd"));
            info.setOriginalAd(object.getDouble("originalAd"));
            info.setFloatAd(object.getDouble("floatAd"));
            info.setEffectiveData(object.getInteger("effectiveData"));
            infos.add(info);
        }
    }

    /**
     * ???????????? 01: ????????? 02: ????????? 03: ????????? 04: ????????? 05: ?????????06: ?????????07: ????????? null:????????????
     */
    private void setLoadStatus(LoadManagementStatisticInfo load) {
        if (load.getStatus() != null) {
            switch (load.getStatus()) {
                case 1:
                    load.setStatusStr("??????");
                    break;
                case 2:
                    load.setStatusStr("??????");
                    break;
                case 3:
                    load.setStatusStr("??????");
                    break;
                case 4:
                    load.setStatusStr("??????");
                    break;
                case 5:
                    load.setStatusStr("??????");
                    break;
                case 6:
                    load.setStatusStr("??????");
                    break;
                case 7:
                    load.setStatusStr("??????");
                    break;
                default:
                    load.setStatusStr("??????");
                    break;
            }
        }
    }

    /**
     * ??????????????????
     * @param loadInfoList loadInfoList
     * @param resultList   resultList
     */
    private void setBlankLoadInfo(List<LoadManagementStatisticInfo> loadInfoList,
        List<LoadManagementStatisticInfo> resultList) {
        long preTime = 0L;

        for (LoadManagementStatisticInfo loadInfo : loadInfoList) {
            long nowVTime = loadInfo.getVtime();
            // 1. ??????????????????????????????
            if (preTime != 0L) {
                // ?????????????????????????????????????????????5??????(300s), ???????????????????????????30s???????????????
                long timeQuantum = nowVTime - preTime;
                if (timeQuantum >= 300) {
                    int needAddBlank =
                        new BigDecimal(timeQuantum).divide(new BigDecimal(30), BigDecimal.ROUND_UP).intValue();
                    for (int j = 0; j < needAddBlank; j++) {
                        // ????????????,??????????????????
                        resultList.add(null);
                    }
                }
            }
            // ????????????????????????????????????
            preTime = nowVTime;
            resultList.add(loadInfo);
        }
    }

    @Override
    public PageGridBean getTotalLoadInfoList(StatisticQuery query) {
        Integer status = query.getStatus();
        final String username = SystemHelper.getCurrentUsername();
        RedisKey redisKey = status != null
                ? HistoryRedisKeyEnum.LOAD_SENSOR_STATUS.of(query.getSensorSequence(), username, status)
                : HistoryRedisKeyEnum.LOAD_SENSOR_INFO.of(username, query.getSensorSequence());
        List<LoadManagementStatisticInfo> resultList =
                RedisHelper.getListObj(redisKey, (query.getStart() + 1), (query.getStart() + query.getLimit()));
        Page<LoadManagementStatisticInfo> results = RedisUtil.queryPageList(resultList, query, redisKey);
        return new PageGridBean(query, results, true);
    }

    @Override
    public void export(HttpServletResponse response, Integer sensorSequence, Integer status) throws IOException {
        String fileName = getExportFileName(status);
        ExportExcelUtil.setResponseHead(response, fileName);
        final String username = SystemHelper.getCurrentUsername();
        RedisKey redisKey = status != null
                ? HistoryRedisKeyEnum.LOAD_SENSOR_STATUS.of(sensorSequence, username, status)
                : HistoryRedisKeyEnum.LOAD_SENSOR_INFO.of(username, sensorSequence);
        Map<String, List<String>> locationMap = new HashMap<>();
        List<LoadManagementStatisticInfo> result = this.lrangeLoadPipeline(redisKey, locationMap);
        //??????hbase????????????
        Map<String, String> addressMap = AddressUtil.batchInverseAddressFromHBase(locationMap, true);

        for (LoadManagementStatisticInfo info : result) {
            if (info.getAddressKey() != null) {
                info.setAddress(
                    addressMap.get(info.getAddressKey()) != null ? addressMap.get(info.getAddressKey()) : "-1");
            }
        }
        ExportExcelUtil.export(
            new ExportExcelParam(null, 1, result, LoadManagementStatisticInfo.class, null, response.getOutputStream()));
    }

    private List<LoadManagementStatisticInfo> lrangeLoadPipeline(RedisKey key, Map<String, List<String>> locationMap) {
        if (!RedisHelper.isContainsKey(key)) {
            return Collections.emptyList();
        }
        final List<LoadManagementStatisticInfo> listObj = RedisHelper.getListObj(key, 1, -1);
        List<LoadManagementStatisticInfo> list = new ArrayList<>(listObj.size());
        for (LoadManagementStatisticInfo loadObj : listObj) {
            String latitude = loadObj.getLatitude();
            String longitude = loadObj.getLongtitude();
            if (latitude == null || longitude == null || "".equals(latitude) || "".equals(longitude) || "0"
                    .equals(latitude) || "0".equals(longitude) || "0.0".equals(latitude) || "0.0"
                    .equals(longitude)) {
                loadObj.setAddress("?????????");
            } else {
                if (longitude.length() > 7 && latitude.length() > 6) {
                    longitude = longitude.substring(0, 7);
                    latitude = latitude.substring(0, 6);
                }
                float lon = Float.parseFloat(longitude);
                float lat = Float.parseFloat(latitude);
                String mapKey = lon + "," + lat;
                List<String> loadIds = locationMap.get(mapKey);
                if (loadIds == null) {
                    loadIds = new ArrayList<>();
                }
                loadObj.setAddressKey(mapKey);
                loadIds.add(loadObj.getId());
                locationMap.put(mapKey, loadIds);
            }
            list.add(loadObj);
        }
        return list;
    }

    /**
     * ???????????? 01: ????????? 02: ????????? 03: ????????? 04: ????????? 05: ?????????06: ?????????07: ????????? null:????????????
     */
    private String getExportFileName(Integer status) {
        if (status == null) {
            return "??????????????????????????????";
        }
        switch (status) {
            case 1:
                return "??????????????????????????????";
            case 2:
                return "??????????????????????????????";
            case 3:
                return "??????????????????????????????";
            case 4:
                return "??????????????????????????????";
            case 5:
                return "??????????????????????????????";
            case 6:
                return "??????????????????????????????";
            case 7:
                return "??????????????????????????????";
            default:
                return "????????????????????????????????????";
        }
    }
}

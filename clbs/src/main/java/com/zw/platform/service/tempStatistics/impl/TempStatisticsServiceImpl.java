package com.zw.platform.service.tempStatistics.impl;

import com.google.common.collect.Lists;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.oil.Positional;
import com.zw.platform.domain.vas.f3.TempStatistics;
import com.zw.platform.service.oil.PositionalService;
import com.zw.platform.service.tempStatistics.TempStatisticsService;
import com.zw.platform.util.common.UuidUtils;
import com.zw.platform.util.excel.ExportExcelParam;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.platform.util.paas.PaasCloudUrlUtil;
import com.zw.platform.util.report.PaasCloudHBaseAccessEnum;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.zw.platform.basic.core.RedisHelper.SIX_HOUR_REDIS_EXPIRE;

/**
 * Created by Administrator on 2017/7/12.
 */
@Service
public class TempStatisticsServiceImpl implements TempStatisticsService {

    @Autowired
    private PositionalService positionalService;

    private static final ThreadLocal<SimpleDateFormat> YMD_HMS =
            ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

    @Override
    public List<TempStatistics> findVehicleDataByBrand(String startTime, String endTime, String vehicleId)
        throws Exception {
        long startTimes;
        long endTimes;
        startTimes = YMD_HMS.get().parse(startTime).getTime() / 1000;
        final long current = System.currentTimeMillis() / 1000;
        //?????????????????????????????????????????? ????????????
        if (startTimes > current) {
            return Collections.emptyList();
        }
        endTimes = YMD_HMS.get().parse(endTime).getTime() / 1000;
        final RedisKey key = HistoryRedisKeyEnum.STATS_TEMP.of(vehicleId, startTimes, endTimes, "");
        final RedisKey keyNew = HistoryRedisKeyEnum.STATS_TEMP.of(vehicleId, startTimes, endTimes, "-n");
        final RedisKey keyHigh = HistoryRedisKeyEnum.STATS_TEMP.of(vehicleId, startTimes, endTimes, "-h");
        final RedisKey keyNewHigh = HistoryRedisKeyEnum.STATS_TEMP.of(vehicleId, startTimes, endTimes, "-n-h");
        final RedisKey keyLow = HistoryRedisKeyEnum.STATS_TEMP.of(vehicleId, startTimes, endTimes, "-l");
        final RedisKey keyNewLow = HistoryRedisKeyEnum.STATS_TEMP.of(vehicleId, startTimes, endTimes, "-n-l");
        List<TempStatistics> tempStatisticsList = new ArrayList<>();
        List<TempStatistics> tempHighStatisticsList = new ArrayList<>();
        List<TempStatistics> tempLowStatisticsList = new ArrayList<>();
        //??????????????????(????????????,?????????????????????)
        List<Integer> invalidList = new ArrayList<>();
        //?????????????????????
        boolean effectiveData;
        boolean isContainsKeyNew = RedisHelper.isContainsKey(keyNew);
        boolean isContainsKey = RedisHelper.isContainsKey(key);
        boolean isContainsHighKey = RedisHelper.isContainsKey(keyHigh);
        boolean isContainsLowKey = RedisHelper.isContainsKey(keyLow);
        if (!isContainsKeyNew || endTimes < current) {
            if (startTimes != 0 && endTimes != 0 && !"".equals(vehicleId)) {
                List<Positional> positionalList = getTempDateByVehicleId(vehicleId, startTimes, endTimes);
                boolean flogKey = RedisHelper.isContainsKey(HistoryRedisKeyEnum.SENSOR_MESSAGE.of(vehicleId));
                if (CollectionUtils.isNotEmpty(positionalList)) {
                    //?????????null?????????
                    List<Positional> filterList =
                        positionalList.stream().filter(Objects::nonNull).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(filterList)) {
                        for (int i = 0, size = filterList.size(); i < size; i++) {
                            effectiveData = false;
                            Positional positional = filterList.get(i);
                            TempStatistics tempStatistics = new TempStatistics();
                            List<Integer> tempHighLow = new ArrayList<>();
                            String latitude = positional.getLatitude();// ??????
                            String longtitude = positional.getLongtitude();// ??????
                            String location = longtitude + "," + latitude;
                            tempStatistics.setPositionCoordinates(location);
                            tempStatistics.setLatitude(latitude);
                            tempStatistics.setLongtitude(longtitude);
                            tempStatistics.setAddress(location);
                            if (flogKey) {
                                if (positional.getMileageTotal() != null) {
                                    tempStatistics.setGpsMile(positional.getMileageTotal());// ?????????
                                }
                                if (positional.getMileageSpeed() != null) {
                                    tempStatistics.setSpeed(positional.getMileageSpeed());// ??????
                                }
                            } else {
                                String gpsMile = positional.getGpsMile();
                                if (StringUtils.isNotBlank(gpsMile) && !"null".equals(gpsMile)) {
                                    tempStatistics.setGpsMile(Double.parseDouble(gpsMile));// ?????????
                                }
                                String speed = positional.getSpeed();
                                if (StringUtils.isNotBlank(speed) && !"null".equals(speed)) {
                                    tempStatistics.setSpeed(Double.parseDouble(speed));// ??????
                                }
                            }
                            if (positional.getVehicleId() != null) {
                                tempStatistics.setVehcileId(
                                    String.valueOf(UuidUtils.getUUIDFromBytes(positional.getVehicleId())));// ??????id
                            }
                            if (positional.getVtime() != 0) {
                                tempStatistics.setLocationTime(positional.getVtime());// ????????????
                            }
                            if (positional.getPlateNumber() != null && !"".equals(positional.getPlateNumber())) {
                                tempStatistics.setPlateNumber(positional.getPlateNumber());// ?????????
                            }

                            // ?????????????????????????????? -55??C???+125??C??????,????????????????????????
                            if (positional.getTempValueOne() != null && positional.getTempValueOne() / 10 <= 125
                                && positional.getTempValueOne() / 10 >= -55) {
                                tempStatistics.setTempValueOne((positional.getTempValueOne()) / 10.0);// ???????????????????????????
                                effectiveData = true;
                            }
                            if (positional.getTempValueTwo() != null && positional.getTempValueTwo() / 10 <= 125
                                && positional.getTempValueTwo() / 10 >= -55) {
                                tempStatistics.setTempValueTwo((positional.getTempValueTwo()) / 10.0);// ???????????????????????????
                                effectiveData = true;
                            }
                            if (positional.getTempValueThree() != null && positional.getTempValueThree() / 10 <= 125
                                && positional.getTempValueThree() / 10 >= -55) {
                                tempStatistics.setTempValueThree((positional.getTempValueThree()) / 10.0);// ???????????????????????????
                                effectiveData = true;
                            }
                            if (positional.getTempValueFour() != null && positional.getTempValueFour() / 10 <= 125
                                && positional.getTempValueFour() / 10 >= -55) {
                                tempStatistics.setTempValueFour((positional.getTempValueFour()) / 10.0);// ???????????????????????????
                                effectiveData = true;
                            }
                            if (positional.getTempValueFive() != null && positional.getTempValueFive() / 10 <= 125
                                && positional.getTempValueFive() / 10 >= -55) {
                                tempStatistics.setTempValueFive((positional.getTempValueFive()) / 10.0);// ???????????????????????????
                                effectiveData = true;
                            }
                            if (positional.getTempHighLowOne() != null) {
                                tempStatistics.setTempHighLowOne(positional.getTempHighLowOne());
                                tempHighLow.add(positional.getTempHighLowOne());
                            }
                            if (positional.getTempHighLowTwo() != null) {
                                tempStatistics.setTempHighLowTwo(positional.getTempHighLowTwo());
                                tempHighLow.add(positional.getTempHighLowTwo());
                            }
                            if (positional.getTempHighLowThree() != null) {
                                tempStatistics.setTempHighLowThree(positional.getTempHighLowThree());
                                tempHighLow.add(positional.getTempHighLowThree());
                            }
                            if (positional.getTempHighLowFour() != null) {
                                tempStatistics.setTempHighLowFour(positional.getTempHighLowFour());
                                tempHighLow.add(positional.getTempHighLowFour());
                            }
                            if (positional.getTempHighLowFive() != null) {
                                tempStatistics.setTempHighLowFive(positional.getTempHighLowFive());
                                tempHighLow.add(positional.getTempHighLowFive());
                            }
                            if (tempHighLow.size() > 0) {
                                if (tempHighLow.contains(1)) {
                                    tempHighStatisticsList.add(tempStatistics);
                                }
                                if (tempHighLow.contains(2)) {
                                    tempLowStatisticsList.add(tempStatistics);
                                }
                            }
                            //??????5???????????????????????? ???null????????????????????????????????? ??????????????????
                            if (!effectiveData) {
                                invalidList.add(i);
                            }
                            tempStatisticsList.add(tempStatistics);
                        }
                    }
                    removeInvalidData(tempStatisticsList, tempHighStatisticsList, tempLowStatisticsList, invalidList);
                    if (isContainsKey) {
                        RedisHelper.delete(key);
                    }
                    if (isContainsHighKey) {
                        RedisHelper.delete(keyHigh);
                    }
                    if (isContainsLowKey) {
                        RedisHelper.delete(keyLow);
                    }
                    if (!isContainsKeyNew) {
                        //???redis???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                        final RedisKey finalKey = endTimes < current ? keyNew : key;
                        final List<TempStatistics> reversed = Lists.reverse(tempStatisticsList);
                        RedisHelper.delete(finalKey);
                        RedisHelper.addObjectToList(finalKey, reversed, SIX_HOUR_REDIS_EXPIRE);

                        final RedisKey finalKeyHigh = endTimes < current ? keyNewHigh : keyHigh;
                        final List<TempStatistics> reversedHigh = Lists.reverse(tempHighStatisticsList);
                        RedisHelper.delete(finalKeyHigh);
                        RedisHelper.addObjectToList(finalKeyHigh, reversedHigh, SIX_HOUR_REDIS_EXPIRE);

                        final RedisKey finalKeyLow = endTimes < current ? keyNewLow : keyLow;
                        final List<TempStatistics> reversedLow = Lists.reverse(tempLowStatisticsList);
                        RedisHelper.delete(finalKeyLow);
                        RedisHelper.addObjectToList(finalKeyLow, reversedLow, SIX_HOUR_REDIS_EXPIRE);
                    }
                }
            }
        } else {
            tempStatisticsList = RedisHelper.getListObj(keyNew, 1, -1);
        }
        return tempStatisticsList.stream()
                .sorted(Comparator.comparingLong(TempStatistics::getLocationTime))
                .collect(Collectors.toList());
    }

    private List<Positional> getTempDateByVehicleId(String vehicleId, long startTimes, long endTimes) {
        final Map<String, String> params = new HashMap<>(8);
        params.put("vehicleId", vehicleId);
        params.put("startTime", String.valueOf(startTimes));
        params.put("endTime", String.valueOf(endTimes));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_TEMP_DATE_BY_VEHICLE_ID, params);
        return PaasCloudUrlUtil.getResultListData(str, Positional.class);
    }

    /**
     * ??????????????????
     * @param type     1:???????????? 2??????????????? 3:????????????
     */

    @Override
    public void exportTempStatisticsList(HttpServletResponse response, int type, RedisKey redisKey, String fileName)
            throws IOException {
        List<TempStatistics> exportList = RedisHelper.getListObj(redisKey, 1, -1);
        ExportExcelUtil.setResponseHead(response, fileName);

        exportList.forEach(e -> {
            long time = e.getLocationTime() * 1000;
            String timeStr = DateFormatUtils.format(time, "yyyy-MM-dd HH:mm:ss");
            e.setStime(timeStr);
            //???????????????????????????????????????
            String formattedAddress = positionalService.getAddress(e.getLongtitude(), e.getLatitude());
            e.setAddress(formattedAddress);
            //?????????????????????????????????????????????????????????????????????????????????????????????????????????
            if (type == 2) {
                if (!Objects.equals(1, e.getTempHighLowOne())) {
                    e.setTempValueOne(null);
                }
                if (!Objects.equals(1, e.getTempHighLowTwo())) {
                    e.setTempValueTwo(null);
                }
                if (!Objects.equals(1, e.getTempHighLowThree())) {
                    e.setTempValueThree(null);
                }
                if (!Objects.equals(1, e.getTempHighLowFour())) {
                    e.setTempValueFour(null);
                }
                if (!Objects.equals(1, e.getTempHighLowFive())) {
                    e.setTempValueFive(null);
                }
            } else if (type == 3) {
                if (!Objects.equals(2, e.getTempHighLowOne())) {
                    e.setTempValueOne(null);
                }
                if (!Objects.equals(2, e.getTempHighLowTwo())) {
                    e.setTempValueTwo(null);
                }
                if (!Objects.equals(2, e.getTempHighLowThree())) {
                    e.setTempValueThree(null);
                }
                if (!Objects.equals(2, e.getTempHighLowFour())) {
                    e.setTempValueFour(null);
                }
                if (!Objects.equals(2, e.getTempHighLowFive())) {
                    e.setTempValueFive(null);
                }
            }
        });
        ExportExcelUtil.export(new ExportExcelParam("", 1, exportList, TempStatistics.class,
                null, response.getOutputStream()));
    }

    /**
     * ???????????????????????????5??????????????????
     * @param tempStatisticsList     ????????????
     * @param tempHighStatisticsList ????????????
     * @param tempLowStatisticsList  ????????????
     */
    private void removeInvalidData(List<TempStatistics> tempStatisticsList, List<TempStatistics> tempHighStatisticsList,
        List<TempStatistics> tempLowStatisticsList, List<Integer> indexList) {
        //???????????????info??????
        List<TempStatistics> removeInfo = new ArrayList<>();
        Integer beginIndex = null;
        Integer endIndex = null;
        for (int i = 0; i < indexList.size(); i++) {
            //??????
            Integer index = indexList.get(i);
            if (beginIndex == null) {
                beginIndex = index;
            }
            //?????????
            Integer nextIndex = indexList.get(Math.min(i + 1, indexList.size() - 1));
            if (nextIndex - 1 != index) {
                endIndex = index;
            }
            if (endIndex != null) {
                long timeDifference =
                    tempStatisticsList.get(endIndex).getLocationTime() - tempStatisticsList.get(beginIndex)
                        .getLocationTime();
                if (timeDifference <= 300 && beginIndex != 0 && endIndex != tempStatisticsList.size() - 1) {
                    for (int j = beginIndex; j <= endIndex; j++) {
                        removeInfo.add(tempStatisticsList.get(j));
                    }
                }
                beginIndex = null;
                endIndex = null;
            }
        }
        tempStatisticsList.removeAll(removeInfo);
        tempHighStatisticsList.removeAll(removeInfo);
        tempLowStatisticsList.removeAll(removeInfo);
    }

}

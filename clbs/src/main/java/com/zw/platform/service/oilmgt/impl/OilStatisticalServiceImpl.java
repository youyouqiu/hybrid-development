package com.zw.platform.service.oilmgt.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.oil.FuelConsumptionStatistics;
import com.zw.platform.domain.oil.Positional;
import com.zw.platform.domain.vas.oilmgt.FuelVehicle;
import com.zw.platform.dto.reportManagement.OilWearStatistics;
import com.zw.platform.repository.vas.OilStatisticalDao;
import com.zw.platform.service.oil.PositionalService;
import com.zw.platform.service.oilmgt.OilStatisticalService;
import com.zw.platform.util.CalculateUtil;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.excel.ExportExcelParam;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.platform.util.paas.PaasCloudUrlUtil;
import com.zw.platform.util.report.PaasCloudHBaseAccessEnum;
import com.zw.platform.util.report.PaasCloudUrlEnum;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Tdz on 2016/9/18.
 */
@Service
public class OilStatisticalServiceImpl implements OilStatisticalService {
    private static Logger log = LogManager.getLogger(OilStatisticalServiceImpl.class);

    @Autowired
    private PositionalService positionalService;

    @Autowired
    private OilStatisticalDao oilStatisticalDao;

    @Override
    public List<Positional> getOilInfo(String brand, String startTime, String endTime) throws Exception {
        List<Positional> list = new ArrayList<>();
        long stime = DateUtils.parseDate(startTime, DateUtil.DATE_FORMAT_SHORT).getTime() / 1000;
        long ntime = DateUtils.parseDate(endTime, DateUtil.DATE_FORMAT_SHORT).getTime() / 1000;
        //?????????????????????????????????????????? ????????????
        if (stime > (System.currentTimeMillis() / 1000)) {
            return list;
        }
        String startTimeStr = DateUtil.getLongToDateStr(stime * 1000, DateUtil.DATE_FORMAT);
        String endTimeStr = DateUtil.getLongToDateStr(ntime * 1000, DateUtil.DATE_FORMAT);
        //??????paas-cloud api??????
        Map<String, String> params = new HashMap<>();
        params.put("startTime", startTimeStr);
        params.put("endTime", endTimeStr);
        params.put("monitorId", brand);
        //??????paas-cloud ??????????????????????????????
        String result = HttpClientUtil.send(PaasCloudUrlEnum.SENSOR_OIL_WEAR_REPORT_URL, params);
        String resultData = JSONObject.parseObject(result).getString("data");
        List<OilWearStatistics> resultList = JSON.parseArray(resultData, OilWearStatistics.class);
        if (CollectionUtils.isEmpty(resultList)) {
            return list;
        }
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        if (startTime.contains(date) || endTime.contains(date)) {
            list = resultList.stream().map(this::convertOilWear).collect(Collectors.toList());
        } else {
            if ("00:00:00".equals(startTime.substring(11)) && "23:59:59".equals(endTime.substring(11))) {
                final RedisKey key = HistoryRedisKeyEnum.STATS_FUEL.of(brand, stime, ntime);
                RedisHelper.delete(key);
                list = resultList.stream().map(this::convertOilWear).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(list)) {
                    RedisHelper.addObjectToList(key, Lists.reverse(list), RedisHelper.SIX_HOUR_REDIS_EXPIRE);
                }
            } else {
                list = resultList.stream().map(this::convertOilWear).collect(Collectors.toList());
            }
        }
        return list;
    }

    /**
     * ???paas-cloud????????????????????????CLBS?????????
     * @param oilWear
     * @return
     */
    private Positional convertOilWear(OilWearStatistics oilWear) {
        Positional p = new Positional();
        //??????????????????????????????????????????????????????????????????
        BeanUtils.copyProperties(oilWear, p);
        p.setPlateNumber(oilWear.getMonitorName());
        p.setVtime(oilWear.getVTime());
        p.setAcc(oilWear.getAcc().toString());
        return p;
    }

    @Override
    public List<Positional> getAppOilInfo(List<String> brands, String startTime, String endTime) throws Exception {
        List<Positional> list = new ArrayList<>();
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long stime = sdf.parse(startTime).getTime() / 1000;
        //?????????????????????????????????????????? ????????????
        if (stime > (System.currentTimeMillis() / 1000)) {
            return list;
        }
        long ntime = sdf.parse(endTime).getTime() / 1000;
        list = getAppOilInfo(brands, stime, ntime);
        return list;
    }

    private List<Positional> getAppOilInfo(List<String> brands, long stime, long ntime) {
        final Map<String, String> params = new HashMap<>(8);
        params.put("vehicleIds", JSON.toJSONString(brands));
        params.put("startTime", String.valueOf(stime));
        params.put("endTime", String.valueOf(ntime));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_APP_OIL_INFO, params);
        return PaasCloudUrlUtil.getResultListData(str, Positional.class);
    }

    @Override
    public List<FuelVehicle> getVehiceInfo(String groupId) throws Exception {
        // ??????userName??????userId
        String userId = SystemHelper.getCurrentUser().getId().toString();
        return oilStatisticalDao.getVehiceInfo(userId, groupId);
    }

    @Override
    public JSONObject getInfoDtails(List<Positional> oilInfo, String band) throws Exception {
        JSONObject obj = new JSONObject();
        List<FuelConsumptionStatistics> list = new ArrayList<FuelConsumptionStatistics>();
        boolean flag = false;// ??????????????????????????????
        boolean flag1 = false;// ????????????
        FuelConsumptionStatistics mileage = null;
        java.text.DecimalFormat df = new java.text.DecimalFormat("#.00");
        double mile = 0;
        int acc = 0;
        double speed = 0;
        double ctime = 0;
        String accOpen = "";
        String accClose = "";
        Positional temp = null;

        boolean flogKey = RedisHelper.isContainsKey(HistoryRedisKeyEnum.SENSOR_MESSAGE.of(band));

        for (int i = 0, len = oilInfo.size(); i < len; i++) {
            temp = oilInfo.get(i);
            if (flogKey) {
                if (temp.getMileageTotal() != null) {
                    mile = temp.getMileageTotal();
                } else {
                    mile = 0;
                }
                if (temp.getMileageSpeed() != null) {
                    speed = temp.getMileageSpeed();
                } else {
                    speed = 0;
                }
            } else {
                mile = Double.parseDouble(temp.getGpsMile());
                speed = Double.parseDouble(temp.getSpeed());
            }
            acc = CalculateUtil.getStatus(String.valueOf(temp.getStatus())).getInteger("acc");
            String date = null;
            date = Converter.timeStamp2Date(String.valueOf(temp.getVtime()), null);
            // ??????????????????????????????????????????????????????????????????????????????2??????????????????????????????
            if (flag) {
                if (speed == 0 || acc == 0) {
                    mileage = null;
                }
                flag = false;
            }
            if (acc == 1 && speed != 0 && mileage == null) {
                flag = true;
                mileage = new FuelConsumptionStatistics();
                mileage.setStartTime(date);
                mileage.setStartMileage(mile);
                // mileage.setStartOil(Double.parseDouble(temp.getTransientOilwearOne()));
                mileage.setStartPositonal(temp.getLongtitude() + "," + temp.getLatitude());
                mileage.setStartOil(Double.parseDouble(temp.getTotalOilwearOne()));

            }
            if (acc == 1 && accOpen.equals("")) {
                accOpen = date;
            }
            if (acc == 0) {
                if (accClose.equals("") && !accOpen.equals("")) {
                    accClose = date;
                    double duration = Double.parseDouble(CalculateUtil.toDateTime(accClose, accOpen));
                    ctime += duration;
                    accOpen = "";
                    accClose = "";
                }
            } else if (accClose.equals("") && !accOpen.equals("")) {
                if (i == oilInfo.size() - 1) {
                    accClose =
                        Converter.timeStamp2Date(String.valueOf(oilInfo.get(oilInfo.size() - 1).getVtime()), null);
                    double duration = Double.parseDouble(CalculateUtil.toDateTime(accClose, accOpen));
                    ctime += duration;
                    accOpen = "";
                    accClose = "";
                }
            }
            if (mileage != null && !flag) {
                // ?????????????????????????????????????????????
                if (acc == 1 && speed != 0) {
                    mileage.setEndTime(date);
                    mileage.setSteerTime(
                        String.valueOf(CalculateUtil.toDateTimeS(mileage.getEndTime(), mileage.getStartTime())));
                    mileage.setEndMileage(mile);
                    mileage.setSteerMileage((mileage.getEndMileage() - mileage.getStartMileage()));
                    // mileage.setMileageCount(Double.parseDouble(df.format(mileage.getEndMileage()
                    // - mileage.getStartMileage())));
                    mileage.setPlateNumber(String.valueOf(temp.getPlateNumber()));
                    mileage.setEndPositonal(temp.getLongtitude() + "," + temp.getLatitude());
                    mileage.setEndOil(Double.parseDouble(temp.getTotalOilwearOne()));
                    mileage.setFuelConsumption((mileage.getEndOil() - mileage.getStartOil()));
                    if (mileage.getFuelConsumption() != 0 && mileage.getSteerMileage() != 0) {
                        mileage
                            .setPerHundredKilimeters((mileage.getFuelConsumption() / mileage.getSteerMileage()) * 100);
                    }
                    // ?????????????????????????????????????????????list????????????????????????????????????list??????????????????????????????????????????????????????????????????
                    if (i == oilInfo.size() - 1) {
                        mileage.setPlateNumber(String.valueOf(temp.getPlateNumber()));
                        list.add(mileage);
                    }
                } else {
                    // ?????????????????????list
                    // ????????????????????????????????????????????????
                    if (mileage != null && mileage.getEndTime() != null) {
                        mileage.setPlateNumber(String.valueOf(temp.getPlateNumber()));
                        list.add(mileage);
                    }
                    mileage = null;
                }
            }
        }
        //??????????????????????????????????????????redis???1?????????????????????
        String username = SystemHelper.getCurrentUser().getUsername();
        final RedisKey key = HistoryRedisKeyEnum.STATS_FUEL_USER.of(username);
        RedisHelper.delete(key);
        RedisHelper.addObjectToList(key, list, RedisHelper.SIX_HOUR_REDIS_EXPIRE);
        obj.put("totalT", ctime);
        obj.put("infoDtail", list);
        return obj;
    }

    /**
     * ????????????????????????
     * @param response
     * @throws Exception
     */
    @Override
    public void exportFuelConsumptionReport(HttpServletResponse response) throws IOException {
        String username = SystemHelper.getCurrentUser().getUsername();
        final RedisKey key = HistoryRedisKeyEnum.STATS_FUEL_USER.of(username);
        List<FuelConsumptionStatistics> exportList = RedisHelper.getListObj(key, 1, -1);
        exportList.forEach((fcs) -> {
            //???????????????????????????????????????
            String startPositional = fcs.getStartPositonal();
            String endPositional = fcs.getEndPositonal();
            //????????????????????????????????????
            String formattedStartPositional = positionalService
                .getAddress(startPositional.substring(0, startPositional.indexOf(",")),
                    startPositional.substring(startPositional.indexOf(",") + 1));
            String formattedEndPositional = positionalService
                .getAddress(endPositional.substring(0, endPositional.indexOf(",")),
                    endPositional.substring(endPositional.indexOf(",") + 1));
            fcs.setStartPositonal(formattedStartPositional);
            fcs.setEndPositonal(formattedEndPositional);
            double millis = Double.parseDouble(fcs.getSteerTime());
            fcs.setSteerTime(DateUtil.milliscondToHhMmSs(millis));
            fcs.setPerHundredKilimeters(
                Double.parseDouble(new DecimalFormat("#.00").format(fcs.getPerHundredKilimeters())));
        });
        ExportExcelUtil.setResponseHead(response, "????????????");
        ExportExcelUtil.export(
            new ExportExcelParam("", 1, exportList, FuelConsumptionStatistics.class, null, response.getOutputStream()));
    }
}

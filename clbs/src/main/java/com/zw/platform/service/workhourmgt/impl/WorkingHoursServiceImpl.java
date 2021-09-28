package com.zw.platform.service.workhourmgt.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.vas.workhourmgt.VibrationSensorBind;
import com.zw.platform.domain.vas.workhourmgt.query.WorkHourDataSource;
import com.zw.platform.domain.vas.workhourmgt.query.WorkHourDetailStatistics;
import com.zw.platform.domain.vas.workhourmgt.query.WorkHourStatistics;
import com.zw.platform.repository.vas.VibrationSensorBindDao;
import com.zw.platform.service.workhourmgt.WorkingHoursService;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.ZipUtil;
import com.zw.platform.util.paas.PaasCloudUrlUtil;
import com.zw.platform.util.report.PaasCloudHBaseAccessEnum;
import org.apache.commons.lang3.time.DateUtils;
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

/**
 * Created by Tdz on 2016/9/27.
 */
@Service
public class WorkingHoursServiceImpl implements WorkingHoursService {
    private static Logger log = LogManager.getLogger(WorkingHoursServiceImpl.class);

    @Autowired
    private VibrationSensorBindDao vibrationSensorBindDao;

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 日期转换格式
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Override
    public List<WorkHourDataSource> getAllInfo(String band, String startTime, String endTime)
        throws Exception {
        List<WorkHourDataSource> list;
        long stime = sdf.parse(startTime).getTime() / 1000;
        long ntime = sdf.parse(endTime).getTime() / 1000;
        list = getWorkInfo(band, stime, ntime);
        return list;
    }

    private List<WorkHourDataSource> getWorkInfo(String band, long stime, long ntime) {
        final Map<String, String> params = new HashMap<>(8);
        params.put("brand", band);
        params.put("startTime", String.valueOf(stime));
        params.put("endTime", String.valueOf(ntime));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_WORK_INFO, params);
        return PaasCloudUrlUtil.getResultListData(str, WorkHourDataSource.class);
    }

    @Override
    public VibrationSensorBind getThresholds(String vehicleId) {
        return vibrationSensorBindDao.getThresholds(vehicleId);
    }

    @Override
    public JSONObject getWorkHours(String type, String band, String startTime, String endTime) throws Exception {
        JSONArray workHours = new JSONArray(); //工作时长
        JSONArray workRate = new JSONArray(); //工作频率
        JSONArray workDates = new JSONArray(); //柱状时间节点
        JSONArray detailDates = new JSONArray(); //折线时间节点
        List<WorkHourStatistics> statistics = new ArrayList<>(); //工时统计数据
        List<WorkHourDetailStatistics> detail = new ArrayList<>(); //明细数据
        List<WorkHourDataSource> dataSources = getAllInfo(band, startTime, endTime); //数据源
        switch (Integer.parseInt(type)) {
            case 0:
                histogram(dataSources, workHours, workDates);
                setStatisticses(dataSources, statistics);
                break;
            case 1:
                lineChart(dataSources, detailDates, workRate);
                setDetail(dataSources, detail);
                break;
            default:
                return null;
        }
        JSONObject message = new JSONObject();
        if (!workHours.isEmpty()) {
            message.put("workHours", workHours.toArray());
        }
        if (!workDates.isEmpty()) {
            message.put("workDates", workDates.toArray());
        }
        if (!statistics.isEmpty()) {
            String result = JSON.toJSONString(statistics);
            //压缩数据
            result = ZipUtil.compress(result);
            message.put("statisticses", result);
        }
        if (!detailDates.isEmpty()) {
            message.put("detailDates", detailDates);
        }
        if (!workRate.isEmpty()) {
            message.put("workRate", workRate);
        }
        if (!detail.isEmpty()) {
            message.put("detail", detail);
        }
        return message;
    }

    private void setDetail(List<WorkHourDataSource> dataSources, List<WorkHourDetailStatistics> detail) {
        try {
            if (dataSources == null || dataSources.isEmpty()) {
                return;
            }
            WorkHourDataSource source;
            WorkHourDetailStatistics detailStatistics = null;
            int no = 1;
            Date startTime = null;
            Date endTime = null;
            for (int i = 0, n = dataSources.size(); i < n; i++) {
                source = dataSources.get(i);
                if (i == n - 1) {
                    detailStatistics = detailStatistics == null ? new WorkHourDetailStatistics() : detailStatistics;
                    endTime = endTime != null ? endTime
                        : DateUtils.parseDate(Converter.timeStamp2Date(String.valueOf(source.getVtime()), null),
                            DATE_FORMAT);
                    detailStatistics.setEndTime(Converter.timeStamp2Date(String.valueOf(source.getVtime()), null));
                    detailStatistics.setWorkHours(getHours(startTime, endTime));
                    detailStatistics.setLatitude(source.getLatitude());
                    detailStatistics.setLongtitude(source.getLongtitude());
                    detailStatistics.setPosition(source.getPosition());
                    detail.add(detailStatistics);
                }
                if (detailStatistics == null) {
                    detailStatistics = new WorkHourDetailStatistics();
                    detailStatistics.setNo(no++);
                    detailStatistics.setBrand(source.getBrand());
                }
                if ("2".equals(source.getStatus())) {
                    if (startTime == null) {
                        startTime = DateUtils
                            .parseDate(Converter.timeStamp2Date(String.valueOf(source.getVtime()), null), DATE_FORMAT);
                        detailStatistics.setStartTime(
                            Converter.timeStamp2Date(String.valueOf(source.getVtime()), null));
                    }
                } else {
                    if (startTime != null && endTime == null) {
                        endTime = DateUtils.parseDate(Converter.timeStamp2Date(String.valueOf(source.getVtime()), null),
                            DATE_FORMAT);
                        detailStatistics.setEndTime(Converter.timeStamp2Date(String.valueOf(source.getVtime()), null));
                        detailStatistics.setWorkHours(getHours(startTime, endTime));
                        detailStatistics.setLatitude(source.getLatitude());
                        detailStatistics.setLongtitude(source.getLongtitude());
                        detailStatistics.setPosition(source.getPosition());
                        detail.add(detailStatistics);
                        detailStatistics = new WorkHourDetailStatistics();
                        detailStatistics.setNo(no++);
                        detailStatistics.setBrand(source.getBrand());
                        startTime = null;
                        endTime = null;
                    }
                }
            }
        } catch (Exception e) {
            log.error("setDetail异常", e);
        }
    }

    private void lineChart(List<WorkHourDataSource> dataSources, JSONArray detailDates, JSONArray workRate) {
        if (dataSources == null || dataSources.isEmpty()) {
            return;
        }
        WorkHourDataSource source;
        for (WorkHourDataSource dataSource : dataSources) {
            source = dataSource;
            detailDates.add(Converter.timeStamp2Date(String.valueOf(source.getVtime()), null));
            if (source.getRate().equals("0")) {
                workRate.add("");
            } else {
                workRate.add(Integer.parseInt(source.getRate()) - 95000);
            }

        }

    }

    private void setStatisticses(List<WorkHourDataSource> dataSources, List<WorkHourStatistics> statisticses) {
        try {
            if (dataSources == null || dataSources.isEmpty()) {
                return;
            }
            int workTimeds = 0;
            double workHoursData = 0d;
            WorkHourDataSource source;
            WorkHourStatistics statistics = null;
            int no = 1;
            Date startTime = null;
            Date endTime = null;
            WorkHourDataSource nextSource;
            for (int i = 0, n = dataSources.size(); i < n; i++) {
                source = dataSources.get(i);
                if ("2".equals(source.getStatus())) {
                    if (startTime == null) {
                        startTime = DateUtils
                            .parseDate(Converter.timeStamp2Date(String.valueOf(source.getVtime()), null), DATE_FORMAT);
                        workTimeds++;
                    }
                } else if (startTime != null && endTime == null) {
                    endTime = DateUtils.parseDate(Converter.timeStamp2Date(String.valueOf(source.getVtime()), null),
                        DATE_FORMAT);
                    workHoursData += getHours(startTime, endTime);
                    startTime = null;
                    endTime = null;
                }

                if (statistics == null) {
                    statistics = new WorkHourStatistics();
                    statistics.setNo(no++);
                    statistics.setTeam(source.getTeam());
                    statistics.setBrand(source.getBrand());
                }

                if (i == n - 1) {
                    endTime = endTime != null ? endTime
                        : DateUtils.parseDate(Converter.timeStamp2Date(String.valueOf(source.getVtime()), null),
                            DATE_FORMAT);
                    statistics.addWorkHours(workHoursData);
                    if (workTimeds != 0) {
                        statistics.addWorkTimes(workTimeds);
                    } else {
                        statistics.addWorkTimes(0);
                    }
                    statisticses.add(statistics);
                    continue;
                } else {
                    nextSource = dataSources.get(i + 1);
                }

                if (!source.getBrand().equals(nextSource.getBrand())) {
                    endTime = endTime != null ? endTime
                        : DateUtils.parseDate(Converter.timeStamp2Date(String.valueOf(source.getVtime()), null),
                            DATE_FORMAT);
                    workHoursData += getHours(startTime, endTime);
                    statistics.addWorkHours(workHoursData);
                    statistics.addWorkTimes(workTimeds);
                    statisticses.add(statistics);
                    statistics = new WorkHourStatistics();
                    statistics.setNo(no++);
                    statistics.setTeam(nextSource.getTeam());
                    statistics.setBrand(nextSource.getBrand());
                    startTime = null;
                    endTime = null;
                    workHoursData = 0d;
                    workTimeds = 0;
                }
            }
        } catch (Exception e) {
            log.error("setStatisticses异常", e);
        }
    }

    private void histogram(List<WorkHourDataSource> dataSources, JSONArray workHours, JSONArray workDates) {
        try {
            if (dataSources == null || dataSources.isEmpty()) {
                return;
            }
            WorkHourDataSource source;
            WorkHourDataSource nextSource;
            double workHoursData = 0d;
            String day = null;
            Date startTime = null;
            Date endTime = null;
            for (int i = 0, n = dataSources.size(); i < n; i++) {
                source = dataSources.get(i);
                if (i == n - 1) {
                    endTime = endTime != null ? endTime
                        : DateUtils.parseDate(Converter.timeStamp2Date(String.valueOf(source.getVtime()), null),
                            DATE_FORMAT);
                    workHoursData += getHours(startTime, endTime);
                    workHours.add(workHoursData);
                    continue;
                } else {
                    nextSource = dataSources.get(i + 1); // 获取到数据
                }

                if (day == null) {
                    day = Converter.timeStamp2Date(String.valueOf(source.getVtime()), null).substring(0, 10);
                    workDates.add(day);
                }

                if ("2".equals(source.getStatus())) {
                    if (startTime == null) {
                        startTime = DateUtils.parseDate(
                            Converter.timeStamp2Date(String.valueOf(source.getVtime()), null), DATE_FORMAT);
                    }
                    if (!day.equals(
                        Converter.timeStamp2Date(String.valueOf(source.getVtime()), null).substring(0, 10))) {
                        endTime =
                            DateUtils.parseDate(Converter.timeStamp2Date(
                                String.valueOf(dataSources.get(i - 1).getVtime()), null), DATE_FORMAT);
                        workHoursData += getHours(startTime, endTime);
                    }
                } else {
                    endTime = endTime != null ? endTime
                        : DateUtils.parseDate(Converter.timeStamp2Date(String.valueOf(source.getVtime()), null),
                            DATE_FORMAT);
                    workHoursData += getHours(startTime, endTime);
                    startTime = null;
                    endTime = null;
                }

                if (!day.equals(Converter.timeStamp2Date(String.valueOf(source.getVtime()), null).substring(0, 10))) {
                    startTime = null;
                    endTime = null;
                    workHours.add(workHoursData);
                    workHoursData = 0d;
                    day = Converter.timeStamp2Date(String.valueOf(source.getVtime()), null).substring(0, 10);
                    workDates.add(day);
                }

                if (!source.getBrand().equals(nextSource.getBrand())) {
                    endTime = endTime != null ? endTime
                        : DateUtils.parseDate(Converter.timeStamp2Date(String.valueOf(source.getVtime()), null),
                            DATE_FORMAT);
                    workHoursData += getHours(startTime, endTime);
                    startTime = null;
                    endTime = null;
                }
            }
        } catch (Exception e) {
            log.error("histogram异常", e);
        }
    }

    private double getHours(Date startTime, Date endTime) {
        if (startTime == null || endTime == null) {
            return 0d;
        }
        return Math.round((float) endTime.getTime() - startTime.getTime()) / 1000d / 3600d;
    }

}

package com.zw.app.service.personalCenter.impl;

import com.alibaba.fastjson.JSON;
import com.zw.app.annotation.AppMethodVersion;
import com.zw.app.annotation.AppServerVersion;
import com.zw.app.controller.AppVersionConstant;
import com.zw.app.domain.personalCenter.AppSpeedReportDetail;
import com.zw.app.service.personalCenter.AppSpeedReportService;
import com.zw.app.util.AppParamCheckUtil;
import com.zw.platform.basic.repository.NewConfigDao;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.infoconfig.ConfigList;
import com.zw.platform.domain.reportManagement.SpeedReport;
import com.zw.platform.domain.reportManagement.query.BigDataReportQuery;
import com.zw.platform.util.BigDataQueryUtil;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.UuidUtils;
import com.zw.platform.util.paas.PaasCloudUrlUtil;
import com.zw.platform.util.report.PaasCloudHBaseAccessEnum;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AppServerVersion
public class AppSpeedReportServiceImpl implements AppSpeedReportService {

    private static final String DATE_FORMAT = "yyyy-MM-dd";

    private static final String DATE_FORMAT_SEND = "yyyy-MM-dd HH:mm:ss";

    private static final long DAY_TIME_LONG = 24 * 60 * 60 * 1000;
    @Autowired
    private NewConfigDao newConfigDao;

    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_ONE,
        url = { "/clbs/app/reportManagement/speedReport/list" })
    public List<SpeedReport> getSpeedReport(String vehicleList, String startTime, String endTime, int type)
        throws Exception {
        if (StringUtils.isNotBlank(vehicleList) && StringUtils.isNotBlank(startTime) && StringUtils
            .isNotBlank(endTime)) {
            List<SpeedReport> re = new ArrayList<>();
            String[] vids = vehicleList.split(",");
            List<String> vehicleIds = Arrays.asList(vids);
            if (vehicleIds.size() > 0 && AppParamCheckUtil.checkDate(startTime, 1) && AppParamCheckUtil
                .checkDate(endTime, 1)) {
                /* 初始化查询参数 */
                List<ConfigList> list = newConfigDao.findOnline(vehicleIds);
                List<BigDataReportQuery> queries = BigDataQueryUtil
                    .getBigDataReportQuery(vehicleIds, startTime.substring(0, 10), endTime.substring(0, 10));
                List<Integer> alarmTypes = getSpeedType(type);
                /* 查询数据 */
                List<SpeedReport> speedReports = new ArrayList<>();
                for (BigDataReportQuery query : queries) {
                    try {
                        query.setAlarmTypes(alarmTypes);
                        speedReports.addAll(getSpeedReport(query));
                    } catch (BadSqlGrammarException e) {
                        // 暂时不作处理
                    }
                }
                Map<String, Integer> map = new HashMap<>(16);
                for (SpeedReport speedReport : speedReports) {
                    Integer speedNumber = speedReport.getSpeedNumber();
                    if (speedNumber == null) {
                        continue;
                    }
                    String moId = UuidUtils.getUUIDStrFromBytes(speedReport.getVehicleId());
                    Integer totalSpeedNumber = map.getOrDefault(moId, 0);
                    map.put(moId, totalSpeedNumber + speedNumber);
                }
                if (list != null && !list.isEmpty()) { // 对比两个集合之间匹配的VehicleId加入 speedNumber
                    for (ConfigList configList : list) { // 将2个集合组装到SpeedReport集合里
                        SpeedReport sr = new SpeedReport();
                        String vehicleId = configList.getVehicleId();
                        sr.setSpeedNumber(0);
                        nowDaySpeed(vehicleId, sr, endTime, alarmTypes);
                        final Integer speedNumber = map.getOrDefault(vehicleId, 0);
                        sr.setSpeedNumber(sr.getSpeedNumber() + speedNumber);
                        sr.setPlateNumber(configList.getCarLicense()); // 添加车牌号
                        sr.setVid(vehicleId); // 添加vid
                        re.add(sr);
                    }
                }
                re.sort((o1, o2) -> o2.getSpeedNumber().compareTo(o1.getSpeedNumber()));
            }
            return re;
        } else {
            return null;
        }
    }

    private List<SpeedReport> getSpeedReport(BigDataReportQuery query) {
        Map<String, String> params = new HashMap<>(2);
        params.put("value", JSON.toJSONString(query));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_SPEED_REPORT, params);
        return PaasCloudUrlUtil.getResultListData(str, SpeedReport.class);
    }

    //查询当前最新日期的数据
    private void nowDaySpeed(String vehicleId, SpeedReport sr, String endTime, List<Integer> alarmTypes)
        throws Exception {
        if (AppParamCheckUtil.nowDayFlag(endTime)) {
            long start = DateUtils.parseDate((endTime.substring(0, 10)), DATE_FORMAT).getTime();
            long end = DateUtils.parseDate(endTime, DATE_FORMAT_SEND).getTime();
            SpeedReport srt = this.getSpeedAlarm(vehicleId, alarmTypes, start, end);
            sr.setSpeedNumber(srt != null ? srt.getSpeedNumber() : 0);
        }
    }

    private SpeedReport getSpeedAlarm(String vehicleId, List<Integer> alarmTypes, long start, long end) {
        if (CollectionUtils.isEmpty(alarmTypes)) {
            return null;
        }
        Map<String, String> params = new HashMap<>(8);
        params.put("monitorId", vehicleId);
        params.put("startTime", String.valueOf(start));
        params.put("endTime", String.valueOf(end));
        params.put("alarmTypes", JSON.toJSONString(alarmTypes));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_SPEED_ALARM, params);
        return PaasCloudUrlUtil.getResultData(str, SpeedReport.class);
    }

    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_ONE,
        url = { "/clbs/app/reportManagement/speedReport/detail" })
    public List<AppSpeedReportDetail> getSpeedReportDetail(String vehicleId, String startTime, String endTime, int type)
        throws Exception {
        if (StringUtils.isNotBlank(vehicleId) && StringUtils.isNotBlank(startTime) && StringUtils.isNotBlank(endTime)) {
            List<AppSpeedReportDetail> appSpeedReportDetails = new ArrayList<>();
            List<Integer> alarmTypes = getSpeedType(type);
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_SEND);
            long start = sdf.parse(startTime).getTime();
            long end = sdf.parse(endTime).getTime();
            long time = start;
            while (time <= end) {
                String timeDay = DateUtil.getDateToString(new Date(time), DATE_FORMAT);
                AppSpeedReportDetail appSpeedReportDetail = new AppSpeedReportDetail();
                BigDataReportQuery query = new BigDataReportQuery();
                query.setAlarmTypes(alarmTypes);
                query.setMonitor(UuidUtils.getBytesFromStr(vehicleId));
                query.setStartTime(time / 1000);
                query.setEndTime((time + DAY_TIME_LONG) / 1000);
                assert timeDay != null;
                query.setMonth(timeDay.substring(0, 7).replace("-", ""));
                String speedNumber = null;
                try {
                    speedNumber = getSpeedReportByVid(query);
                } catch (BadSqlGrammarException e) {
                    // 暂时不作处理
                }
                appSpeedReportDetail.setSpeedNumber(speedNumber != null ? Integer.parseInt(speedNumber) : 0);
                appSpeedReportDetail.setTime(timeDay.substring(8, 10));
                appSpeedReportDetails.add(appSpeedReportDetail);
                time = time + DAY_TIME_LONG;
            }
            setNowDaySpeed(vehicleId, appSpeedReportDetails, endTime, alarmTypes);
            return appSpeedReportDetails;
        } else {
            return null;
        }
    }

    private String getSpeedReportByVid(BigDataReportQuery query) {
        Map<String, String> params = new HashMap<>(2);
        params.put("value", JSON.toJSONString(query));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_SPEED_REPORT_BY_VID, params);
        return PaasCloudUrlUtil.getResultData(str, String.class);
    }

    //查询当前最新日期的数据
    private void setNowDaySpeed(String vehicleId, List<AppSpeedReportDetail> appSpeedReportDetails, String endTime,
        List<Integer> alarmTypes) throws Exception {
        if (AppParamCheckUtil.nowDayFlag(endTime)) {
            long start = DateUtils.parseDate((endTime.substring(0, 10)), DATE_FORMAT).getTime();
            long end = DateUtils.parseDate(endTime, DATE_FORMAT_SEND).getTime();
            SpeedReport srt = this.getSpeedAlarm(vehicleId, alarmTypes, start, end);
            AppSpeedReportDetail appSpeedReportDetail = new AppSpeedReportDetail();
            appSpeedReportDetail.setTime(endTime.substring(8, 10));
            appSpeedReportDetail.setSpeedNumber(srt != null ? srt.getSpeedNumber() : 0);
            appSpeedReportDetails.set(appSpeedReportDetails.size() - 1, appSpeedReportDetail);
        }
    }

    //平台和终端的超速报警
    private List<Integer> getSpeedType(int type) {
        List<Integer> alarmTypes = new ArrayList<>();
        if (type == 0) {
            alarmTypes.add(1);
        } else {
            alarmTypes.add(67);
            alarmTypes.add(74);
            alarmTypes.add(76);
            alarmTypes.add(117);
        }
        return alarmTypes;
    }

}

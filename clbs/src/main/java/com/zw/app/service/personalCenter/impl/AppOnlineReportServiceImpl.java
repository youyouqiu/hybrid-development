package com.zw.app.service.personalCenter.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zw.app.annotation.AppMethodVersion;
import com.zw.app.annotation.AppServerVersion;
import com.zw.app.controller.AppVersionConstant;
import com.zw.app.domain.personalCenter.AppOlineReportDetail;
import com.zw.app.service.personalCenter.AppOnlineReportService;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.BigDataReport.OnlineReport;
import com.zw.platform.domain.BigDataReport.OnlineReportData;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.MonitorUtils;
import com.zw.platform.util.report.PaasCloudUrlEnum;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AppServerVersion
public class AppOnlineReportServiceImpl implements AppOnlineReportService {

    private Logger log = LogManager.getLogger(AppOnlineReportServiceImpl.class);

    private static final String DATE_FORMAT = "yyyy-MM-dd";

    private static final String DATE_FORMAT_SEND = "yyyy-MM-dd HH:mm:ss";

    private static final String SECOND_FORMAT = "yyMMddHHmmss";

    private static final long DAY_TIME_LONG = 24 * 60 * 60 * 1000;

    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_ONE, url = {
        "/clbs/app/reportManagement/onlineReport/list" })
    public List<OnlineReport> findOnlineList(String vehicleList, String startTime, String endTime) throws Exception {
        if (StringUtils.isNotBlank(vehicleList) && StringUtils.isNotBlank(startTime) && StringUtils
            .isNotBlank(endTime)) {
            List<OnlineReport> or = new ArrayList<>(); // 车辆上线率集合

            NumberFormat numberFormat = NumberFormat.getInstance();
            numberFormat.setMaximumFractionDigits(2); // 设置精确到小数点后2位
            long stime = DateUtils.parseDate(startTime, DATE_FORMAT_SEND).getTime() / 1000; // 起始时间
            long etime = DateUtils.parseDate(endTime, DATE_FORMAT_SEND).getTime() / 1000; // 结束时间
            long allday = (etime - stime) / 86400; // 通过秒数 计算天数
            int alldays = (int) allday + 1;
            Map<String, String> queryParam = new HashMap<>();
            queryParam.put("monitorIds", vehicleList);
            String startTimeStr = DateUtil.formatDate(startTime, DateUtil.DATE_FORMAT_SHORT, DateUtil.DATE_YMD_FORMAT);
            String endTimeStr = DateUtil.formatDate(endTime, DateUtil.DATE_FORMAT_SHORT, DateUtil.DATE_YMD_FORMAT);
            queryParam.put("startTime", startTimeStr);
            queryParam.put("endTime", endTimeStr);
            String queryResult = HttpClientUtil.send(PaasCloudUrlEnum.ON_LINE_REPORT_URL, queryParam);
            JSONObject queryResultJsonObj = JSON.parseObject(queryResult);
            if (Objects.isNull(queryResultJsonObj) || queryResultJsonObj.getInteger("code") != 10000) {
                return null;
            }
            List<String> vehicleIds = Arrays.asList(vehicleList.split(","));
            Map<String, BindDTO> bindDTOMap = MonitorUtils.getBindDTOMap(vehicleIds, "name");
            Map<String, OnlineReportData> dataMap = new HashMap<>();
            List<OnlineReportData> reportData =
                JSONObject.parseArray(queryResultJsonObj.getString("data"), OnlineReportData.class);
            if (CollectionUtils.isNotEmpty(reportData)) {
                dataMap =
                    reportData.stream().collect(Collectors.toMap(OnlineReportData::getMonitorId, Function.identity()));
            }
            for (String id : vehicleIds) {
                OnlineReport ort = new OnlineReport();
                BindDTO bindDTO = bindDTOMap.get(id);
                ort.setAllDays(alldays); // 添加总天数
                ort.setActiveDays(0);
                ort.setRatio("0");
                if (dataMap.containsKey(id)) {
                    OnlineReportData data = dataMap.get(id);
                    ort.setActiveDays(ort.getActiveDays() + data.getOnlineDayNumber());
                    ort.setFirstDataTimes(data.getFirstDataTime()); //每天上线时间（每天第一条数据时间）
                    ort.setOnlineDurationStr(DateUtil.milliscondToHhMmSs(data.getOnlineDuration())); //在线时长（HH:mm:ss）
                    ort.setOnlineCount(data.getOnlineCount()); //在线次数
                    ort.setRatio(data.getOnlineRate());
                }

                ort.setVehicleId(id); // 添加vid
                ort.setCarLicense(bindDTO.getName());
                or.add(ort); // 添加进集合
            }
            or.sort((o1, o2) -> o2.getActiveDays().compareTo(o1.getActiveDays()));
            return or;
        } else {
            return null;
        }
    }

    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_ONE, url = {
        "/clbs/app/reportManagement/onlineReport/detail" })
    @Transactional(propagation = Propagation.NESTED)
    public List<AppOlineReportDetail> getOnlineReportDetail(String vehicleId, String startTime, String endTime)
        throws Exception {
        if (StringUtils.isNotBlank(vehicleId) && StringUtils.isNotBlank(startTime) && StringUtils.isNotBlank(endTime)) {

            Map<String, String> queryParam = new HashMap<>(16);
            queryParam.put("monitorIds", vehicleId);
            String startTimeStr = DateUtil.formatDate(startTime, DateUtil.DATE_FORMAT_SHORT, DateUtil.DATE_YMD_FORMAT);
            String endTimeStr = DateUtil.formatDate(endTime, DateUtil.DATE_FORMAT_SHORT, DateUtil.DATE_YMD_FORMAT);
            queryParam.put("startTime", startTimeStr);
            queryParam.put("endTime", endTimeStr);
            String queryResult = HttpClientUtil.send(PaasCloudUrlEnum.ON_LINE_REPORT_URL, queryParam);
            JSONObject queryResultJsonObj = JSON.parseObject(queryResult);
            if (Objects.isNull(queryResultJsonObj) || queryResultJsonObj.getInteger("code") != 10000) {
                return null;
            }
            List<OnlineReportData> reportData =
                JSONObject.parseArray(queryResultJsonObj.getString("data"), OnlineReportData.class);
            Set<String> timeSet = new HashSet<>();
            if (CollectionUtils.isNotEmpty(reportData)) {
                List<String> firstDataTime = reportData.get(0).getFirstDataTime();
                for (String time : firstDataTime) {
                    Date date = DateUtil.getStringToDate(time, DATE_FORMAT_SEND);
                    String timeStr = DateUtil.getDateToString(date, DATE_FORMAT);
                    timeSet.add(timeStr);
                }
            }
            List<AppOlineReportDetail> appOlineReportDetails = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_SEND);
            long start = sdf.parse(startTime).getTime();
            long end = sdf.parse(endTime).getTime();
            long time = start;
            while (time <= end) {
                AppOlineReportDetail appOlineReportDetail = new AppOlineReportDetail();
                String timeDay = DateUtil.getDateToString(new Date(time), DATE_FORMAT);
                appOlineReportDetail.setTime(timeDay);
                if (timeSet.contains(timeDay)) {
                    appOlineReportDetail.setOnlineFlag(1);
                } else {
                    appOlineReportDetail.setOnlineFlag(0);
                }
                appOlineReportDetails.add(appOlineReportDetail);
                time = time + DAY_TIME_LONG;
            }
            return appOlineReportDetails;
        } else {
            return null;
        }
    }

}
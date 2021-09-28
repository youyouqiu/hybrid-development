package com.zw.platform.service.reportManagement.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.repository.NewConfigDao;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.BigDataReport.OnlineReport;
import com.zw.platform.domain.BigDataReport.OnlineReportData;
import com.zw.platform.domain.BigDataReport.PositionInfo;
import com.zw.platform.domain.basicinfo.enums.PlateColor;
import com.zw.platform.domain.infoconfig.ConfigList;
import com.zw.platform.domain.reportManagement.query.BigDataReportQuery;
import com.zw.platform.service.reportManagement.OnlineReportService;
import com.zw.platform.util.BigDataQueryUtil;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.UuidUtils;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.platform.util.excel.ExportExcelParam;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.platform.util.paas.PaasCloudUrlUtil;
import com.zw.platform.util.report.PaasCloudHBaseAccessEnum;
import com.zw.platform.util.report.PaasCloudUrlEnum;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OnlineReportServiceImpl implements OnlineReportService {

    /**
     * 日期转换格式
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    @Autowired
    private NewConfigDao newConfigDao;

    @Override
    public List<ConfigList> findOnline(List<String> vehicleList) throws Exception {
        return newConfigDao.findOnline(vehicleList);
    }

    @Override
    public List<PositionInfo> findOnlineDay(BigDataReportQuery query) throws Exception {
        Map<String, String> params = new HashMap<>(2);
        params.put("value", JSON.toJSONString(query));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.FIND_ONLINES, params);
        return PaasCloudUrlUtil.getResultListData(str, PositionInfo.class);
    }

    @Override
    public boolean export(String title, int type, HttpServletResponse res) throws Exception {
        RedisKey redisKey = HistoryRedisKeyEnum.ONLINE_REPORT.of(SystemHelper.getCurrentUsername());
        List<OnlineReport> onlineReport = RedisHelper.getList(redisKey, OnlineReport.class);
        return ExportExcelUtil
            .export(new ExportExcelParam(title, type, onlineReport, OnlineReport.class, null, res.getOutputStream()));
    }

    @Override
    public List<OnlineReport> findOnlineList(String vehicleList, String startTime, String endTime) throws Exception {
        List<OnlineReport> or = new ArrayList<>(); // 车辆上线率集合
        if (vehicleList != null && !vehicleList.isEmpty() && vehicleList.split(",").length > 0) {
            List<String> vehicleIds = Arrays.asList(vehicleList.split(","));
            long stime = dateToStamp(startTime) / 1000; // 起始时间
            long etime = dateToStamp(endTime) / 1000; // 结束时间
            NumberFormat numberFormat = NumberFormat.getInstance();
            numberFormat.setMaximumFractionDigits(2); // 设置精确到小数点后2位
            long allday = (etime - stime) / 86400; // 通过秒数 计算天数
            int alldays = (int) allday + 1;
            Map<String, BindDTO> bindInfosMap = VehicleUtil.batchGetBindInfosByRedis(vehicleIds);
            /** 初始化查询参数 */
            List<BigDataReportQuery> queries = BigDataQueryUtil.getBigDataReportQuery(vehicleIds, startTime, endTime);
            List<PositionInfo> pif = new ArrayList<>();
            for (BigDataReportQuery query : queries) {
                pif.addAll(findOnlineDay(query)); // 查询rides存入集合 pif
            }
            /** 合并多月数据 */
            Map<String, PositionInfo> positionInfoMap = new HashMap<>();
            for (PositionInfo pi : pif) {
                String vehicleId = UuidUtils.getUUIDFromBytes(pi.getVehicleIdHbase()).toString();
                PositionInfo positionInfo = positionInfoMap.get(vehicleId);
                if (positionInfo == null) {
                    //设置监控对象id
                    pi.setVehicleId(vehicleId);
                    //设置在线天数
                    pi.setActiveDays(1);
                    //设置每天上线时间
                    if (StringUtils.isNotBlank(pi.getFirstDataTime())) {
                        pi.getFirstDataTimes().addAll(Arrays.asList(pi.getFirstDataTime().split(",")));
                    }
                    //设置在线时长（HH:mm:ss）
                    pi.setOnlineDurationStr(DateUtil.milliscondToHhMmSs(pi.getOnlineDuration()));
                    //回写map
                    positionInfoMap.put(vehicleId, pi);
                } else {
                    //累加在线天数
                    positionInfo.setActiveDays(positionInfo.getActiveDays() + 1);
                    //累加每天上线时间
                    if (StringUtils.isNotBlank(pi.getFirstDataTime())) {
                        positionInfo.getFirstDataTimes().addAll(Arrays.asList(pi.getFirstDataTime().split(",")));
                    }
                    //累加在线时长
                    positionInfo.setOnlineDuration(positionInfo.getOnlineDuration() + pi.getOnlineDuration());
                    positionInfo.setOnlineDurationStr(DateUtil.milliscondToHhMmSs(positionInfo.getOnlineDuration()));
                    //累加在线次数
                    positionInfo.setOnlineCount(positionInfo.getOnlineCount() + pi.getOnlineCount());
                }
            }
            pif = new ArrayList<>(positionInfoMap.values());
            int activeDay = 0;
            if (MapUtils.isNotEmpty(bindInfosMap)) { // 对比两个集合之间匹配的VehicleId加入 getActiveDays
                Collection<BindDTO> list = bindInfosMap.values();
                for (BindDTO configList : list) { // 将2个集合组装到OnlineReport集合里
                    OnlineReport ort = new OnlineReport();
                    ort.setActiveDays(activeDay);
                    if (!pif.isEmpty()) {
                        for (PositionInfo pi : pif) {
                            if (configList.getId().equals(pi.getVehicleId())) {
                                ort.setActiveDays(pi.getActiveDays());
                                ort.setFirstDataTimes(pi.getFirstDataTimes()); //每天上线时间（每天第一条数据时间）
                                ort.setOnlineDurationStr(pi.getOnlineDurationStr()); //在线时长（HH:mm:ss）
                                ort.setOnlineCount(pi.getOnlineCount()); //在线次数
                                // 计算车辆上线率
                                String result = numberFormat.format((float) pi.getActiveDays() / (float) alldays * 100);
                                ort.setRatio(result + "%");
                            }
                        }
                    }
                    ort.setAllDays(alldays); // 添加总天数
                    ort.setAssignmentName(configList.getGroupName()); // 添加所属分组
                    ort.setCarLicense(configList.getName()); // 添加车牌号
                    String color = VehicleUtil.getPlateColorStr(String.valueOf(configList.getPlateColor()));
                    ort.setColor(color); // 添加车辆颜色
                    // 查询车辆关联信息
                    ort.setProfessionalNames(configList.getProfessionalNames()); // 添加从业人员
                    ort.setVehicleId(configList.getId()); // 添加vid
                    or.add(ort); // 添加进集合
                }
            }
            or.sort((o1, o2) -> o2.getActiveDays().compareTo(o1.getActiveDays()));
        }
        RedisKey redisKey = HistoryRedisKeyEnum.ONLINE_REPORT.of(SystemHelper.getCurrentUsername());
        RedisHelper.delete(redisKey);
        if (CollectionUtils.isNotEmpty(or)) {
            RedisHelper.addToList(redisKey, or);
            RedisHelper.expireKey(redisKey, RedisHelper.SIX_HOUR_REDIS_EXPIRE);
        }
        return or;
    }

    @Override
    public List<OnlineReport> onlineByF3Pass(String vehicleList, String startTime, String endTime) throws Exception {
        long stime = dateToStamp(startTime) / 1000; // 起始时间
        long etime = dateToStamp(endTime) / 1000; // 结束时间
        long allday = (etime - stime) / 86400; // 通过秒数 计算天数
        int alldays = (int) allday + 1;
        List<OnlineReport> or = new ArrayList<>();
        Map<String, String> queryParam = new HashMap<>(16);
        queryParam.put("monitorIds", vehicleList);
        startTime = DateUtil.formatDate(startTime, DateUtil.DATE_Y_M_D_FORMAT, DateUtil.DATE_YMD_FORMAT);
        queryParam.put("startTime", startTime);
        endTime = DateUtil.formatDate(endTime, DateUtil.DATE_Y_M_D_FORMAT, DateUtil.DATE_YMD_FORMAT);
        queryParam.put("endTime", endTime);
        String queryResult = HttpClientUtil.send(PaasCloudUrlEnum.ON_LINE_REPORT_URL, queryParam);
        JSONObject queryResultJsonObj = JSON.parseObject(queryResult);
        if (Objects.isNull(queryResultJsonObj) || queryResultJsonObj.getInteger("code") != 10000) {
            return null;
        }
        List<String> vehicleIds = Arrays.asList(vehicleList.split(","));
        Map<String, BindDTO> confidMap = VehicleUtil.batchGetBindInfosByRedis(vehicleIds);
        Map<String, OnlineReportData> dataMap = new HashMap<>();
        List<OnlineReportData> reportData =
            JSONObject.parseArray(queryResultJsonObj.getString("data"), OnlineReportData.class);
        if (CollectionUtils.isNotEmpty(reportData)) {
            dataMap =
                reportData.stream().collect(Collectors.toMap(OnlineReportData::getMonitorId, Function.identity()));
        }
        for (String id : vehicleIds) {
            OnlineReport ort = new OnlineReport();
            BindDTO configList = confidMap.get(id);
            ort.setAllDays(alldays); // 添加总天数
            ort.setActiveDays(0);
            if (dataMap.containsKey(id)) {
                OnlineReportData data = dataMap.get(id);
                ort.setActiveDays(data.getOnlineDayNumber());
                ort.setFirstDataTimes(data.getFirstDataTime()); //每天上线时间（每天第一条数据时间）
                ort.setOnlineDurationStr(DateUtil.milliscondToHhMmSs(data.getOnlineDuration())); //在线时长（HH:mm:ss）
                ort.setOnlineCount(data.getOnlineCount()); //在线次数
                ort.setRatio(data.getOnlineRate() + "%");
            }
            ort.setColor(PlateColor.getNameOrBlankByCode(configList.getPlateColor()));
            ort.setProfessionalNames(configList.getProfessionalNames()); // 添加从业人员
            ort.setAssignmentName(configList.getGroupName());
            ort.setVehicleId(id); // 添加vid
            ort.setCarLicense(configList.getName());
            or.add(ort); // 添加进集合
        }
        or.sort((o1, o2) -> o2.getActiveDays().compareTo(o1.getActiveDays()));
        return or;
    }

    public static long dateToStamp(String time) throws Exception {
        long ts = DateUtils.parseDate(time, DATE_FORMAT).getTime();
        return ts;
    }
}
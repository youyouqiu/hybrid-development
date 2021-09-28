package com.zw.platform.service.reportManagement.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import com.zw.app.util.AppParamCheckUtil;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.domain.ProfessionalDO;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.ProfessionalDTO;
import com.zw.platform.basic.repository.NewProfessionalsDao;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.oil.Positional;
import com.zw.platform.domain.reportManagement.ParkingInfo;
import com.zw.platform.domain.reportManagement.PositionalDetail;
import com.zw.platform.domain.reportManagement.TravelDetail;
import com.zw.platform.domain.reportManagement.query.BigDataReportQuery;
import com.zw.platform.domain.statistic.TravelBaseInfo;
import com.zw.platform.dto.reportManagement.ParkingInfoDto;
import com.zw.platform.dto.reportManagement.ParkingInfoPaasDto;
import com.zw.platform.service.monitoring.impl.HistoryServiceImpl;
import com.zw.platform.service.monitoring.impl.RealTimeServiceImpl;
import com.zw.platform.service.reportManagement.MileageReportService;
import com.zw.platform.service.reportManagement.ParkingReportService;
import com.zw.platform.util.BigDataQueryUtil;
import com.zw.platform.util.LocalDateUtils;
import com.zw.platform.util.StrUtil;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.UuidUtils;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.platform.util.excel.ExportExcelParam;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.platform.util.paas.PaasCloudUrlUtil;
import com.zw.platform.util.report.PaasCloudHBaseAccessEnum;
import com.zw.platform.util.report.PaasCloudUrlEnum;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class ParkingReportServiceImpl implements ParkingReportService {
    /**
     * 高德api接口key
     */
    @Value("${api.key.gaode}")
    private String key;

    /**
     * 日期转换格式
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Autowired
    HistoryServiceImpl historyService;

    @Autowired
    private NewProfessionalsDao newProfessionalsDao;

    @Autowired
    RealTimeServiceImpl realTime;

    @Autowired
    private MileageReportService mileageReportService;

    @Override
    public List<ParkingInfo> getStopData(List<String> ids, String startTime, String endTime) throws Exception {
        long start = DateUtils.parseDate(startTime, DATE_FORMAT).getTime() / 1000;
        long end = DateUtils.parseDate(endTime, DATE_FORMAT).getTime() / 1000;
        // 去除重复的车辆
        Set<String> set = new HashSet<String>();
        set.addAll(ids);
        List<String> vehicleIds = new ArrayList<String>(set);
        // 所有停车数据
        List<ParkingInfo> stops = new ArrayList<ParkingInfo>();
        List<Positional> positionals = getPositionalsByIds(start, end, vehicleIds);
        // 如果历史轨迹为空,则返回false
        if (positionals == null || positionals.isEmpty()) {
            return null;
        }
        // 一条停车数据
        ParkingInfo pi = null;
        // 停车次数
        int stopNumber = 0;
        // 记录停车时长
        long stopTime = 0;
        // 记录停车位置信息
        String longtitude = null;
        String latitude = null;

        StringBuilder professionalPhone = null;
        StringBuilder professionalName = null;

        for (int i = 0, n = positionals.size(); i < n; i++) {

            //professionalPhone.setLength(0);
            // 一条车辆历史轨迹
            Positional positional = positionals.get(i);
            // acc状态，0：关，1：开
            String acc = positional.getStatus();
            if (acc != null) {
                try {
                    acc = Integer.toBinaryString(Integer.parseInt(acc));
                } catch (Exception e) {
                    // 异常数据，删除该条数据，重新循环
                    positionals.remove(i);
                    i--;
                    n = positionals.size();
                    continue;
                }
                acc = acc.substring(acc.length() - 1);
            } else {
                // 异常数据，删除该条数据，重新循环
                positionals.remove(i);
                i--;
                n = positionals.size();
                continue;
            }
            if ("0".equals(acc) || "0".equals(positional.getSpeed()) || "0.0".equals(positional.getSpeed())) {
                // 如果停车数据为空
                if (pi == null) {
                    pi = new ParkingInfo();
                    // 设置开始时间
                    pi.setStartTime(positional.getVtime());
                }
                String acc1 = null;
                if (i != n - 1) {
                    acc1 = positionals.get(i + 1).getStatus();
                    if (acc1 != null) {
                        if (acc1.length() <= 9) {
                            try {
                                acc1 = Integer.toBinaryString(Integer.parseInt(acc1));
                            } catch (Exception e) {
                                // 异常数据，删除该条数据，重新循环
                                positionals.remove(i + 1);
                                i--;
                                n = positionals.size();
                                continue;
                            }
                        }
                        acc1 = acc1.substring(acc1.length() - 1);
                    } else {
                        // 异常数据，删除该条数据，重新循环
                        positionals.remove(i + 1);
                        i--;
                        n = positionals.size();
                        continue;
                    }
                }
                // 如果是最后一条数据或下一条数据速度不为0且acc状态处于开或车牌不同
                if (i == n - 1 || (0 != Double.parseDouble(positionals.get(i + 1).getSpeed()) && "1".equals(acc1))
                    || !positional.getPlateNumber().equals(positionals.get(i + 1).getPlateNumber())) {
                    // 设置结束时间
                    if (i == n - 1 || !positional.getPlateNumber().equals(positionals.get(i + 1).getPlateNumber())) {
                        pi.setEndTime(positional.getVtime());
                    } else if (0 != Double.parseDouble(positionals.get(i + 1).getSpeed()) && "1".equals(acc1)) {
                        pi.setEndTime(positionals.get(i + 1).getVtime());
                    }
                    // 如果前后时间超过3分钟则认为是停车
                    // if(pi.getEndTime()-pi.getStartTime()>=180){
                    // //设置位置信息
                    // longtitude=positional.getLongtitude();
                    // latitude=positional.getLatitude();
                    // //记录停车时长
                    // stopTime+=pi.getEndTime()-pi.getStartTime();
                    // stopNumber++;
                    // }
                    // 设置位置信息
                    longtitude = positional.getLongtitude();
                    latitude = positional.getLatitude();
                    // 记录停车时长
                    stopTime += pi.getEndTime() - pi.getStartTime();
                    if (pi.getEndTime() - pi.getStartTime() != 0) {
                        stopNumber++;
                    }
                    pi = null;
                }
            }
            if (i == n - 1 || !positional.getPlateNumber().equals(positionals.get(i + 1).getPlateNumber())) {
                professionalPhone = new StringBuilder();
                professionalName = new StringBuilder();
                // 一条停车数据
                ParkingInfo par = new ParkingInfo();
                // 查询车辆关联信息
                String vehicleId = String.valueOf(UuidUtils.getUUIDFromBytes(positional.getVehicleId()));
                BindDTO rc = VehicleUtil.getBindInfoByRedis(vehicleId);

                List<ProfessionalDO> professionalsInfo = newProfessionalsDao.findByVehicleId(vehicleId);
                if (professionalsInfo.size() > 0) {
                    for (int j = 0; j < professionalsInfo.size(); j++) {
                        professionalPhone.append(StrUtil.isNotBlank(professionalsInfo.get(j).getPhone())
                            ? professionalsInfo.get(j).getPhone() : "-").append("，");
                        professionalName.append(StrUtil.isNotBlank(professionalsInfo.get(j).getName())
                            ? professionalsInfo.get(j).getName() : "").append("，");
                    }
                }
                if (professionalPhone.length() != 0 && professionalPhone.toString().contains("，")) {
                    professionalPhone.deleteCharAt(professionalPhone.lastIndexOf("，"));
                }
                if (professionalName.length() != 0 && professionalName.toString().contains("，")) {
                    professionalName.deleteCharAt(professionalName.lastIndexOf("，"));
                }
                // 设置车牌号
                par.setPlateNumber(positional.getPlateNumber());
                // 设置分组
                par.setAssignmentName(rc.getGroupName());
                // 电话
                par.setPhone(professionalPhone.toString());
                // 设置从业人员
                par.setProfessionalsName(professionalName.toString());
                // 设置停车次数
                par.setStopNumber(stopNumber);
                // 设置停车时间
                par.setStopTime(DateUtil.formatTime(stopTime * 1000));
                // 设置停车地点
                if ("".equals(longtitude)) {
                    longtitude = "0";
                }
                if ("".equals(latitude)) {
                    latitude = "0";
                }
                par.setStopLocation(longtitude + "," + latitude);
                stops.add(par);
                // 初始化相关值以记录下一辆车
                pi = null;
                stopNumber = 0;
                stopTime = 0;
                longtitude = "";
                latitude = "";
            }
        }
        return stops;
    }

    private List<Positional> getPositionalsByIds(long start, long end, List<String> vehicleIds) {
        final Map<String, String> params = new HashMap<>(8);
        params.put("vehicleIds", JSON.toJSONString(vehicleIds));
        params.put("startTime", String.valueOf(start));
        params.put("endTime", String.valueOf(end));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_POSITIONALS_BY_IDS, params);
        return PaasCloudUrlUtil.getResultListData(str, Positional.class);
    }

    @Override
    public List<ParkingInfo> getStopBigData(List<String> vehicleIds, String startTime, String endTime,
        boolean isAppSearch) throws Exception {
        int timeType = 2;
        if (isAppSearch) {
            timeType = 1;
        }
        if (vehicleIds != null && vehicleIds.size() > 0 && AppParamCheckUtil.checkDate(startTime, timeType)
            && AppParamCheckUtil.checkDate(endTime, timeType)) {
            Map<String, ParkingInfo> datas = new HashMap<>(32);
            Map<String, String> professionalInfo = new HashMap<>(32);
            List<ParkingInfo> resultData =
                getParkingInfoList(vehicleIds, startTime, endTime, isAppSearch, datas, professionalInfo);

            if (resultData.size() > 0) {
                /* 查询从业人员信息 */
                getProfessionalInfoList(professionalInfo);

                /* 组装返回数据 */
                for (ParkingInfo parkingInfo : resultData) {
                    String plateNumber = parkingInfo.getPlateNumber();
                    ParkingInfo pi;
                    if (datas.containsKey(plateNumber)) {
                        pi = datas.get(plateNumber);
                    } else {
                        pi = new ParkingInfo();
                        pi.setPlateNumber(plateNumber);
                        pi.setMonitorId(UuidUtils.getUUIDStrFromBytes(parkingInfo.getMonitorIdByte()));
                    }
                    pi.setStopTimeMs(pi.getStopTimeMs() + parkingInfo.getStopTimeMs());
                    pi.setStopNumber(pi.getStopNumber() + parkingInfo.getStopNumber());
                    pi.setStopLocation(parkingInfo.getStopLocation());
                    pi.setStopMile(addStopMile(pi.getStopMile(), parkingInfo.getStopMile()));
                    datas.put(plateNumber, pi);
                }
            }
            /** 组装处理数据 */
            if (datas.size() > 0) {
                resultData.clear();
                Map<String, BindDTO> configLists = VehicleUtil.batchGetBindInfosByRedis(vehicleIds);
                for (ParkingInfo data : datas.values()) {
                    data.setStopTime(DateUtil.formatTime(data.getStopTimeMs() * 1000));
                    BindDTO configList = configLists.get(data.getMonitorId());
                    String professionalNames = configList.getProfessionalNames();
                    data.setAssignmentName(configList.getGroupName());
                    data.setProfessionalsName(professionalNames);
                    setProfessionalPhone(professionalInfo, data, professionalNames);
                    resultData.add(data);
                }
            }
            return resultData;
        }
        return null;
    }

    @Override
    public List<ParkingInfoDto> getStopBigDataFromPaas(String vehicleIds, String startTime, String endTime,
        boolean isAppSearch) throws Exception {
        int timeType = 2;
        if (isAppSearch) {
            timeType = 1;
        }
        String userId = SystemHelper.getCurrentUser().getId().toString();
        RedisKey redisKey = HistoryRedisKeyEnum.USER_VEHICLE_STOP_INFO.of(userId);
        RedisHelper.delete(redisKey);
        if (StringUtils.isNotBlank(vehicleIds) && AppParamCheckUtil.checkDate(startTime, timeType) && AppParamCheckUtil
            .checkDate(endTime, timeType)) {
            //获取从业人员名称和电话的map
            Map<String, String> professionalInfo = new HashMap<>(32);
            getProfessionalInfoList(professionalInfo);
            //转换时间格式为yyyyMMddHHmmss的字符串
            String beforeFormat = Objects.equals(timeType, 2) ? DateUtil.DATE_Y_M_D_FORMAT : DateUtil.DATE_FORMAT_SHORT;
            startTime = DateUtil.formatDate(startTime, beforeFormat, DateUtil.DATE_YMD_FORMAT);
            endTime = DateUtil.formatDate(endTime, beforeFormat, DateUtil.DATE_YMD_FORMAT);
            Map<String, String> params =
                ImmutableMap.of("monitorIds", vehicleIds, "startTime", startTime, "endTime", endTime);
            //调用paas-cloud接口
            String resultStr = HttpClientUtil.send(PaasCloudUrlEnum.TERMINAL_STOP_MILEAGE_STATISTICS_URL, params);
            JSONObject obj = JSONObject.parseObject(resultStr);
            List<ParkingInfoPaasDto> initialList =
                JSONArray.parseArray(obj.getString("data"), ParkingInfoPaasDto.class);
            //新建结果list
            List<ParkingInfoDto> resultList = new ArrayList<>();
            if (null != initialList && initialList.size() > 0) {
                //遍历判断是否绑定里程传感器，筛选数据到最终集合中
                for (ParkingInfoPaasDto pipd : initialList) {
                    ParkingInfoDto pid = new ParkingInfoDto();
                    pid.setMonitorId(pipd.getMonitorId());
                    pid.setMonitorName(pipd.getMonitorName());
                    pid.setAssignmentName(pipd.getAssignmentName());
                    if (RedisHelper.isContainsKey(HistoryRedisKeyEnum.SENSOR_MESSAGE.of(pipd.getMonitorId()))) {
                        pid.setIdleSpeedMile(pipd.getSensorIdleSpeedMile());
                        pid.setStopNum(pipd.getSensorStopNum());
                        pid.setDuration(pipd.getSensorDuration());
                        pid.setAddress(pipd.getSensorStopAddress());
                        pid.setStopLocation(pipd.getSensorStopLocation());
                    } else {
                        pid.setIdleSpeedMile(pipd.getDeviceIdleSpeedMile());
                        pid.setStopNum(pipd.getDeviceStopNum());
                        pid.setDuration(pipd.getDeviceDuration());
                        pid.setAddress(pipd.getDeviceStopAddress());
                        pid.setStopLocation(pipd.getDeviceStopLocation());
                    }
                    resultList.add(pid);
                }
            }
            //遍历组装处理数据
            if (resultList.size() > 0) {
                //获取信息配置绑定关系
                List<String> vehicleIdList = Arrays.asList(vehicleIds.split(","));
                Map<String, BindDTO> configLists = VehicleUtil.batchGetBindInfosByRedis(vehicleIdList);
                for (ParkingInfoDto pid : resultList) {
                    pid.setStopTime(DateUtil.formatTime(pid.getDuration() * 1000));
                    BindDTO configList = configLists.get(pid.getMonitorId());
                    String employeeNames = configList.getProfessionalNames();
                    pid.setAssignmentName(configList.getGroupName());
                    pid.setEmployeeName(employeeNames);
                    setEmployeePhone(professionalInfo, pid, employeeNames);
                }
                RedisHelper.addToList(redisKey, resultList);
                RedisHelper.expireKey(redisKey, RedisHelper.SIX_HOUR_REDIS_EXPIRE);
            }
            return resultList;
        }
        return null;
    }

    private Double addStopMile(Double stopMileOne, Double stopMileTwo) {
        return new BigDecimal(stopMileOne).add(new BigDecimal(stopMileTwo)).setScale(2, RoundingMode.HALF_UP)
            .doubleValue();
    }

    private List<ParkingInfo> getParkingInfoList(List<String> vehicleIds, String startTime, String endTime,
        boolean isAppSearch, Map<String, ParkingInfo> datas, Map<String, String> professionalInfo) throws Exception {
        String today = LocalDateUtils.dateFormate(new Date());
        List<ParkingInfo> resultData = new ArrayList<>();
        if (isAppSearch) {
            addData2Result(vehicleIds, startTime, endTime, resultData);
        } else {
            //web调用
            if (today.equals(endTime)) {
                endTime = LocalDateUtils.compareDate(endTime, -1);
                List<PositionalDetail> positionalList = mileageReportService.findPositionalDetailsBy(today, vehicleIds);
                dealData(isAppSearch, datas, professionalInfo, positionalList);
            }
            if (!today.equals(startTime)) {
                addData2Result(vehicleIds, startTime, endTime, resultData);
            }
        }
        return resultData;
    }

    private void addData2Result(List<String> vehicleIds, String startTime, String endTime, List<ParkingInfo> resultData)
        throws Exception {
        /** 初始化查询参数 */
        List<BigDataReportQuery> queries = BigDataQueryUtil.getBigDataReportQuery(vehicleIds, startTime, endTime);
        /** 查询数据 */

        for (BigDataReportQuery query : queries) {
            try {
                resultData.addAll(getStopBigData(query));
            } catch (BadSqlGrammarException e) {
                // 暂时不作处理
            }
        }
    }

    private List<ParkingInfo> getStopBigData(BigDataReportQuery query) {
        Map<String, String> params = new HashMap<>(2);
        params.put("value", JSON.toJSONString(query));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_STOP_BIG_DATA, params);
        return PaasCloudUrlUtil.getResultListData(str, ParkingInfo.class);
    }

    private void dealData(boolean isAppSearch, Map<String, ParkingInfo> datas, Map<String, String> professionalInfo,
        List<PositionalDetail> positionalList) {
        if (CollectionUtils.isNotEmpty(positionalList)) {
            List<TravelDetail> singleMonitorTravelDetails = new ArrayList<>();
            List<TravelDetail> travelDetails = new ArrayList<>();
            mileageReportService.getMonitorsTravelDetail(singleMonitorTravelDetails, travelDetails, positionalList,
                TravelBaseInfo.STATUS_STOP, TravelBaseInfo.TRAVEL_CHANGE_STOP_MAX_TIMES, isAppSearch);
            getParkingReportInfo(datas, travelDetails, professionalInfo);
        }
    }

    private void setProfessionalPhone(Map<String, String> professionalInfo, ParkingInfo pi, String professionalsName) {
        if (StringUtils.isNotBlank(professionalsName)) {
            String[] pnames = professionalsName.split(",");
            StringBuilder phones = new StringBuilder();
            for (String pname : pnames) {
                Object phone = professionalInfo.get(pname);
                if (phone != null) {
                    phones.append(phone).append(",");
                }
            }
            String data = phones.toString();
            if (StringUtils.isNotBlank(data)) {
                pi.setPhone(data.substring(0, data.length() - 1));
            }
        }
    }

    private void setEmployeePhone(Map<String, String> professionalInfo, ParkingInfoDto pid, String employeeNames) {
        if (StringUtils.isNotBlank(employeeNames)) {
            String[] pnames = employeeNames.split(",");
            StringBuilder phones = new StringBuilder();
            for (String pname : pnames) {
                Object phone = professionalInfo.get(pname);
                if (phone != null) {
                    phones.append(phone).append(",");
                }
            }
            String data = phones.toString();
            if (StringUtils.isNotBlank(data)) {
                pid.setEmployeePhone(data.substring(0, data.length() - 1));
            }
        }
    }

    private void getProfessionalInfoList(Map<String, String> info) {
        boolean flag = (info.size() <= 0);
        if (flag) {
            List<ProfessionalDTO> professionalsInfos = newProfessionalsDao.findAllProfessionals();
            if (professionalsInfos != null) {
                for (ProfessionalDTO p : professionalsInfos) {
                    if (StringUtils.isNotBlank(p.getPhone())) {
                        info.put(p.getName(), p.getPhone());
                    }
                }
            }
        }
    }

    /**
     * 获取当天数据
     * @param monitorIds       monitorIds
     * @param endTime          endTime
     * @param datas            datas
     * @param professionalInfo 监控对象
     * @param isAppSearch      isAppSearch
     * @return endTime
     */
    private String getTodayParkingReportData(List<String> monitorIds, String endTime, Map<String, ParkingInfo> datas,
        Map<String, String> professionalInfo, boolean isAppSearch) {
        String today = LocalDateUtils.dateFormate(new Date());
        if (today.equals(endTime)) {
            endTime = LocalDateUtils.compareDate(endTime, -1);

            List<PositionalDetail> positionalList = mileageReportService.findPositionalDetailsBy(today, monitorIds);
            dealData(isAppSearch, datas, professionalInfo, positionalList);
        }
        return endTime;
    }

    private void getParkingReportInfo(Map<String, ParkingInfo> datas, List<TravelDetail> travelDetails,
        Map<String, String> professionalInfo) {
        if (CollectionUtils.isNotEmpty(travelDetails)) {
            getProfessionalInfoList(professionalInfo);
            // 组装数据
            Date today = new Date();
            long day = today.getTime() / 1000;

            for (TravelDetail travelDetail : travelDetails) {
                String plateNumber = travelDetail.getPlateNumber();
                ParkingInfo pi;
                if (datas.containsKey(plateNumber)) {
                    pi = datas.get(plateNumber);
                } else {
                    pi = new ParkingInfo();
                    pi.setPlateNumber(plateNumber);
                    String monitorId = UuidUtils.getUUIDFromBytes(travelDetail.getVehicleIdHbase()).toString();
                    pi.setMonitorIdByte(travelDetail.getVehicleIdHbase());
                    pi.setMonitorId(monitorId);
                    pi.setDay(day);
                }
                pi.setStopLocation(travelDetail.getEndLocation());
                pi.setStopTimeMs(pi.getStopTimeMs() + travelDetail.getTravelTime() / 1000);
                int stopNumber = pi.getStopNumber();
                pi.setStopNumber(++stopNumber);
                setStopMile(travelDetail, pi);
                datas.put(plateNumber, pi);
            }
        }
    }

    private void setStopMile(TravelDetail travelDetail, ParkingInfo pi) {
        BigDecimal totalGpsMile = new BigDecimal(pi.getStopMile());
        BigDecimal gpsMile = new BigDecimal(travelDetail.getTotalGpsMile());
        double mile = totalGpsMile.add(gpsMile).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
        pi.setStopMile(mile);
    }

    @Override
    public boolean export(String title, int type, HttpServletResponse res) throws IOException {
        String userId = SystemHelper.getCurrentUser().getId().toString();
        RedisKey redisKey = HistoryRedisKeyEnum.USER_VEHICLE_STOP_INFO.of(userId);
        List<ParkingInfoDto> list = RedisHelper.getList(redisKey, ParkingInfoDto.class);
        return ExportExcelUtil
            .export(new ExportExcelParam(title, type, list, ParkingInfoDto.class, null, res.getOutputStream()));
    }

    @Override
    public List<ParkingInfo> findSingleMonitorParkingList(List<String> monitorIds, String startTime, String endTime)
        throws Exception {
        List<ParkingInfo> resultData = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(monitorIds) && AppParamCheckUtil.checkDate(startTime, 1) && AppParamCheckUtil
            .checkDate(endTime, 1)) {
            Map<String, ParkingInfo> datas = new HashMap<>(32);
            Map<String, String> professionalInfo = new HashMap<>(32);
            resultData = getParkingInfoList(monitorIds, startTime, endTime, true, datas, professionalInfo);
            if (datas.size() > 0) {
                resultData.addAll(datas.values());
            }
        }
        return resultData;
    }
}

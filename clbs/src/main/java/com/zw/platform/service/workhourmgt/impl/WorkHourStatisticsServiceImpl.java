package com.zw.platform.service.workhourmgt.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.google.common.collect.Lists;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.domain.VehicleDO;
import com.zw.platform.basic.repository.NewVehicleDao;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.enmu.ProtocolEnum;
import com.zw.platform.domain.vas.workhourmgt.SensorVehicleInfo;
import com.zw.platform.domain.vas.workhourmgt.WorkHourInfo;
import com.zw.platform.domain.vas.workhourmgt.WorkHourSettingInfo;
import com.zw.platform.domain.vas.workhourmgt.WorkHourStatistics;
import com.zw.platform.domain.vas.workhourmgt.query.WorkHourQuery;
import com.zw.platform.repository.vas.WorkHourSettingDao;
import com.zw.platform.service.workhourmgt.WorkHourStatisticsService;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.RedisUtil;
import com.zw.platform.util.common.UuidUtils;
import com.zw.platform.util.common.ZipUtil;
import com.zw.platform.util.report.PaasCloudUrlEnum;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.zw.platform.basic.core.RedisHelper.SIX_HOUR_REDIS_EXPIRE;

/**
 * @author ponghj
 */
@Service
public class WorkHourStatisticsServiceImpl implements WorkHourStatisticsService {
    private static Logger log = LogManager.getLogger(WorkHourStatisticsServiceImpl.class);

    @Resource
    private WorkHourSettingDao workHourSettingDao;

    @Resource
    private UserService userService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private NewVehicleDao vehicleDao;

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 停机
     */
    private static final int HALT_STATE = 0;
    /**
     * 工作
     */
    private static final int WORK_STATE = 1;
    /**
     * 待机
     */
    private static final int STANDBY_STATE = 2;

    /**
     * 当波动值为0且瞬时流量超过该限制值时,下一个点就重新计算工作状态
     * 仅油耗波动式需要重新计算工作状态
     */
    private static final double INSTANT_FLOW_LIMIT = 5;

    /**
     * 当两条数据的时间差超过该值时,需要添加空白数据,并且重新计算工作状态,表格重新计算持续时长; 当一段无效数据的总时长小于该值时,需要移除这一段无效数据;
     * 仅油耗波动式需要重新计算工作状态
     */
    private static final long TIME_INTERVAL = 300;

    @Override
    public PageGridBean getTotalDataFormInfo(WorkHourQuery query) throws Exception {
        Page<WorkHourInfo> result = null;
        try {
            //企业名称
            VehicleDO vehicleDO = vehicleDao.getById(query.getVehicleId());
            String groupName = organizationService.getOrgNameByUuid(vehicleDO.getOrgId());
            final RedisKey key = HistoryRedisKeyEnum.STATS_WORK.of(SystemHelper.getCurrentUsername());
            List<WorkHourInfo> workHourReportFormData = RedisHelper.getListObj(
                    key, (query.getStart() + 1), (query.getStart() + query.getLimit()));
            if (CollectionUtils.isNotEmpty(workHourReportFormData)) {
                for (int i = 0; i < workHourReportFormData.size(); i++) {
                    WorkHourInfo info =
                        JSON.parseObject(JSON.toJSONString(workHourReportFormData.get(i)), WorkHourInfo.class);
                    info.setGroupName(groupName);
                    workHourReportFormData.set(i, info);
                }
                result = RedisUtil.queryPageList(workHourReportFormData, query, key);
            }
            if (CollectionUtils.isNotEmpty(result)) {
                return new PageGridBean(query, result, true);
            } else {
                return new PageGridBean(Lists.newArrayList());
            }
        } finally {
            if (result != null) {
                result.close();
            }
        }
    }

    @Override
    public JSONObject getChartInfo(WorkHourQuery query, boolean isApp) throws Exception {
        final RedisKey key = HistoryRedisKeyEnum.STATS_WORK.of(SystemHelper.getCurrentUsername());
        RedisHelper.delete(key);
        typeChange(query);
        JSONObject msg = new JSONObject();
        List<WorkHourInfo> result = new ArrayList<>();
        List<WorkHourInfo> workHourInfo;
        //有效工时下标
        List<Integer> workIndex = new ArrayList<>();
        //停机工时下标
        List<Integer> haltIndex = new ArrayList<>();
        //待机工时下标
        List<Integer> standByIndex = new ArrayList<>();
        //无效工时下标
        List<Integer> invalidList = new ArrayList<>();
        //工时检测方式
        Integer inspectionMethod = null;
        //阈值
        String thresholdValue = null;
        //有效时长
        long workDuration = 0L;
        //停机时长
        long haltDuration = 0L;
        //待机时长
        long standByDuration = 0L;
        //速度阈值
        Double speedThreshold = null;
        //无效数据持续时长
        long invalidDuration = 0L;
        //工时传感器设置
        WorkHourSettingInfo workHourSettingInfo =
            workHourSettingDao.findSensorVehicleByVehicleIdAndSensorSequence(query);
        if (workHourSettingInfo != null) {

            String startStr = query.getStartTimeStr().replaceAll("-", "").replaceAll(":", "").replaceAll(" ", "");
            String endStr = query.getEndTimeStr().replaceAll("-", "").replaceAll(":", "").replaceAll(" ", "");
            HashMap<String, String> param = new HashMap<>();
            param.put("monitorId", query.getVehicleId());
            param.put("startTime", startStr);
            param.put("endTime", endStr);
            param.put("type",   query.getSensorSequence());
            String resultStr = HttpClientUtil.send(PaasCloudUrlEnum.WORK_HOUR_URL, param);
            List<WorkHourStatistics> workHourStatistics;
            List<WorkHourInfo> chartInfos = new ArrayList<>();
            if (StringUtils.isNotBlank(resultStr)) {
                JSONObject obj = JSONObject.parseObject(resultStr);
                workHourStatistics = JSONObject.parseArray(obj.getString("data"), WorkHourStatistics.class);
                for (WorkHourStatistics workHourStatistic : workHourStatistics) {
                    WorkHourInfo info = new WorkHourInfo();
                    BeanUtils.copyProperties(workHourStatistic, info);
                    info.setVtime(workHourStatistic.getVTime());
                    info.setPlateNumber(workHourStatistic.getMonitorName());
                    info.setVtimeStr(workHourStatistic.getVTimeStr());
                    chartInfos.add(info);
                }
            }

            //clbs传感器设置的工时检测方式(1:电压比较式;2:油耗阈值式;3.油耗波动式)
            Integer detectionMode = workHourSettingInfo.getDetectionMode();
            //电压阈值（V）
            String twoThresholdVoltage = workHourSettingInfo.getThresholdVoltage();
            //工作流量阈值（L/h）
            String thresholdWorkFlow = workHourSettingInfo.getThreshold();
            //波动阈值A
            Double a = workHourSettingInfo.getBaudRateThreshold() != null
                ? Double.parseDouble(workHourSettingInfo.getBaudRateThreshold()) : null;
            speedThreshold = workHourSettingInfo.getSpeedThreshold() != null
                ? Double.parseDouble(workHourSettingInfo.getSpeedThreshold()) : null;
            //波动计算个数N
            Integer n = workHourSettingInfo.getBaudRateCalculateNumber();
            //需要设置为待机的数量
            Integer needSetStandbyNum = 0;
            if (CollectionUtils.isNotEmpty(chartInfos)) {
                //mysql传感器设置的检测方式比传感器上传的要大1
                inspectionMethod = detectionMode - 1;
                //处理数据(当查询时间段内的数据的工时检测方式有两种以上时,需要过滤数据 只展示工时传感器设置的检测方式)
                Integer finalInspectionMethod = inspectionMethod;
                workHourInfo = chartInfos;
                // workHourInfo = chartInfos.stream().filter(
                //     info -> info.getWorkInspectionMethod() == null || info.getWorkInspectionMethod()
                //         .equals(finalInspectionMethod)).collect(Collectors.toList());
                //当工时检测方式为油耗波动式时 所有点初始化状态为工作
                if (inspectionMethod == 2) {
                    workHourInfo.forEach(info -> info.setWorkingPosition(WORK_STATE));
                }
                thresholdValue = inspectionMethod == 0 ? twoThresholdVoltage :
                    inspectionMethod == 1 ? thresholdWorkFlow : String.valueOf(a);
                for (int i = workHourInfo.size() - 1; i >= 0; i--) {
                    WorkHourInfo info = workHourInfo.get(i);
                    //阈值
                    info.setThresholdValue(thresholdValue);
                    //工时检测方式
                    Integer workInspectionMethod = info.getWorkInspectionMethod();
                    //如果工时检测方式为null 就表示该数据是无效数据(只有位置数据,无工时传感器数据)
                    if (workInspectionMethod == null || !Objects.equals(workInspectionMethod, inspectionMethod)) {
                        info.setEffectiveData(1);
                        invalidList.add(i);
                    }
                    info.setVtimeStr(new SimpleDateFormat(DATE_FORMAT).format(new Date(info.getVtime() * 1000)));
                    //波动值(方差)
                    Double s = info.getFluctuateValue();
                    //检测数据
                    Double checkData = info.getCheckData();
                    //油耗波动式需要单独计算工作状态
                    if (Objects.equals(2, inspectionMethod) && Objects.equals(2, workInspectionMethod)) {
                        if (workInspectionMethod != null && checkData != 0) {
                            //当需要设置为待机的数量的个数为0 并且 当S<A时，则包括该点在内左边的N个点的状态为待机。
                            if (a != null && (s == null || s < a)) {
                                needSetStandbyNum = n;
                            }
                        }
                        //当需要设置为待机的数量的个数大于0时,直接设置为待机
                        if (needSetStandbyNum > 0) {
                            if (workInspectionMethod != null) {
                                info.setWorkingPosition(STANDBY_STATE);
                            }
                            needSetStandbyNum--;
                        }
                        if (i != 0) {
                            WorkHourInfo infoNext = workHourInfo.get(i - 1);
                            //当两条数据vtime相差 300s 时,需要添加空白数据,所以待机状态要重新计算
                            if (info.getVtime() - infoNext.getVtime() > TIME_INTERVAL) {
                                needSetStandbyNum = 0;
                            }
                        }
                        if (workInspectionMethod != null) {
                            //当速度大于速度阈值时工作状态为工作
                            if (info.getSpeed() != null && speedThreshold != null
                                && Double.valueOf(info.getSpeed()) > speedThreshold) {
                                info.setWorkingPosition(WORK_STATE);
                            }
                            //当传感器瞬时流量值为0时, 工作状态为停机并且重新计算工作状态
                            if (checkData == 0) {
                                info.setWorkingPosition(HALT_STATE);
                                needSetStandbyNum = 0;
                            }
                            //当传感器的波动值为0并且瞬时流量大于5时 重新计算工作状态 并且设置当前点的工作状态为工作
                            if (s == 0 && checkData > INSTANT_FLOW_LIMIT) {
                                Integer beforeIndex = i - 1 >= 0 ? i - 1 : null;
                                if (beforeIndex == null) {
                                    info.setWorkingPosition(WORK_STATE);
                                    needSetStandbyNum = 0;
                                } else {
                                    WorkHourInfo beforeWorkInfo = workHourInfo.get(beforeIndex);
                                    Double beforeCheckData = beforeWorkInfo.getCheckData();
                                    //如果前一个点和当前点的瞬时流量不相等或者前一个点为无效数据设置为工作状态,如果相等保持之前的状态不变;
                                    if (beforeWorkInfo.getWorkInspectionMethod() == null || !Objects
                                        .equals(checkData, beforeCheckData)) {
                                        info.setWorkingPosition(WORK_STATE);
                                        needSetStandbyNum = 0;
                                    }
                                }
                            }
                        }
                    }
                }
                //工时报表表格数据 存入redis
                Integer workingPosition = null;
                //表格状态持续时长
                long continueTime = 0L;
                List<WorkHourInfo> totalDataFormInfo = workHourInfo.stream().filter(
                    info -> info.getEffectiveData() == 0 && info.getWorkInspectionMethod()
                        .equals(finalInspectionMethod)).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(totalDataFormInfo)) {
                    for (int i = 0; i < totalDataFormInfo.size(); i++) {
                        WorkHourInfo info = totalDataFormInfo.get(i);
                        Integer workingPositionNow = info.getWorkingPosition();
                        if (i > 0) {
                            WorkHourInfo beforeInfo = totalDataFormInfo.get(i - 1);
                            long duration = info.getVtime() - beforeInfo.getVtime();
                            //当下一条的工作状态不等于上一段的工作状态 或者 同一个工作状态的两条数据时间相差300s 重新计算持续时长
                            if (!workingPositionNow.equals(workingPosition) || duration > TIME_INTERVAL) {
                                continueTime = 0L;
                            } else {
                                continueTime += ((duration) * 1000);
                            }
                        }
                        info.setContinueTimeStr(DateUtil.formatTime(continueTime));
                        workingPosition = workingPositionNow;
                    }
                }
                if (CollectionUtils.isNotEmpty(totalDataFormInfo)) {
                    RedisHelper.addObjectToList(key, Lists.reverse(totalDataFormInfo), SIX_HOUR_REDIS_EXPIRE);
                }
                Collections.reverse(invalidList);
                //移除无效数据段小于5分钟的数据段
                invalidDuration += removeInvalidData(workHourInfo, invalidList);
                //App 不需要添加空白数据
                if (isApp) {
                    result.addAll(workHourInfo);
                } else {
                    //当两条数据vtime相差 300s 时,需要添加空白数据
                    invalidDuration += addBlankData(result, workHourInfo);
                }
                //工时检测方式  0:电压比较式  1:油耗阈值式  2:油耗波动式
                for (int i = 0; i < result.size(); i++) {
                    WorkHourInfo info = result.get(i);
                    workingPosition = info.getWorkingPosition();
                    if (info.getEffectiveData() == 0) {
                        if (workingPosition == HALT_STATE) {
                            haltIndex.add(i);
                        } else if (workingPosition == WORK_STATE) {
                            workIndex.add(i);
                        } else if (workingPosition == STANDBY_STATE) {
                            standByIndex.add(i);
                        }
                    }
                }
                workDuration = getDuration(result, workIndex);
                haltDuration = getDuration(result, haltIndex);
                standByDuration = getDuration(result, standByIndex);
            }
        }
        //App不需要压缩数据
        if (isApp) {
            msg.put("workHourInfo", result);
        } else {
            String resultZip = ZipUtil.compress(JSON.toJSONString(result));
            msg.put("workHourInfo", resultZip);
        }
        //工时检测方式
        msg.put("workInspectionMethod", inspectionMethod);
        //阈值
        msg.put("thresholdValue", thresholdValue);
        //速度阈值
        msg.put("speedThreshold", speedThreshold);
        // App 需要返回秒 不需要转换
        //无效时长
        msg.put("invalidDuration", isApp ? invalidDuration :
            DateUtil.formatMinToString(invalidDuration % 60 == 0 ? invalidDuration / 60 : (invalidDuration / 60) + 1));
        //工作时长
        msg.put("workDuration", isApp ? workDuration :
            DateUtil.formatMinToString(workDuration % 60 == 0 ? workDuration / 60 : (workDuration / 60) + 1));
        //停机时长
        msg.put("haltDuration", isApp ? haltDuration :
            DateUtil.formatMinToString(haltDuration % 60 == 0 ? haltDuration / 60 : (haltDuration / 60) + 1));
        //待机时长
        msg.put("standByDuration", isApp ? standByDuration :
            DateUtil.formatMinToString(standByDuration % 60 == 0 ? standByDuration / 60 : (standByDuration / 60) + 1));
        return msg;
    }

    /**
     * 获得时长
     * @param workHourInfo
     * @param indexList
     */
    private long getDuration(List<WorkHourInfo> workHourInfo, List<Integer> indexList) {
        Integer beginIndex = null;
        Integer endIndex = null;
        long duration = 0L;
        for (int i = 0; i < indexList.size(); i++) {
            //当前
            Integer index = indexList.get(i);
            if (beginIndex == null) {
                beginIndex = index;
            }
            //下一个
            Integer nextIndex = indexList.get(i + 1 > indexList.size() - 1 ? indexList.size() - 1 : i + 1);
            if (nextIndex - 1 != index) {
                endIndex = index;
            }
            if (endIndex != null) {
                if (endIndex + 1 < workHourInfo.size()) {
                    WorkHourInfo info = workHourInfo.get(endIndex + 1);
                    if (info.getVtime() != 0 && info.getVtime() - workHourInfo.get(endIndex).getVtime() < 300) {
                        endIndex += 1;
                    }
                }
                duration += (workHourInfo.get(endIndex).getVtime() - workHourInfo.get(beginIndex).getVtime());
                beginIndex = null;
                endIndex = null;
            }
        }
        return duration;
    }

    /**
     * 当两条数据vtime相差 300s 时,需要添加空白数据;
     * @param result
     * @param workHourInfo
     * @return
     */
    private long addBlankData(List<WorkHourInfo> result, List<WorkHourInfo> workHourInfo) {
        long invalidDuration = 0L;
        for (int i = 0; i < workHourInfo.size(); i++) {
            //当前 info
            WorkHourInfo info = workHourInfo.get(i);
            result.add(info);
            //后一条数据
            WorkHourInfo afterInfo = workHourInfo.get(i + 1 < workHourInfo.size() ? i + 1 : workHourInfo.size() - 1);
            //如果数据相差300s
            long timeDifference = afterInfo.getVtime() - info.getVtime();
            if (timeDifference > TIME_INTERVAL) {
                invalidDuration += timeDifference;
                long num = timeDifference % 30 == 0 ? timeDifference / 30 : ((timeDifference / 30) + 1);
                for (int j = 0; j < num; j++) {
                    WorkHourInfo nullInfo = new WorkHourInfo();
                    nullInfo.setEffectiveData(3);
                    result.add(nullInfo);
                }
            }
        }
        return invalidDuration;
    }

    /**
     * 移除无效数据段小于5分钟的数据段
     * @param workHourInfo
     * @param indexList
     * @return
     */
    private long removeInvalidData(List<WorkHourInfo> workHourInfo, List<Integer> indexList) {
        long duration = 0L;
        //需要移除的info集合
        List<WorkHourInfo> removeInfo = new ArrayList<>();
        Integer beginIndex = null;
        Integer endIndex = null;
        for (int i = 0; i < indexList.size(); i++) {
            //当前
            Integer index = indexList.get(i);
            if (beginIndex == null) {
                beginIndex = index;
            }
            //下一个
            Integer nextIndex = indexList.get(i + 1 > indexList.size() - 1 ? indexList.size() - 1 : i + 1);
            if (nextIndex - 1 != index) {
                endIndex = index;
            }
            if (endIndex != null) {
                long timeDifference = workHourInfo.get(endIndex).getVtime() - workHourInfo.get(beginIndex).getVtime();
                if (timeDifference <= TIME_INTERVAL) {
                    for (int j = beginIndex; j <= endIndex; j++) {
                        removeInfo.add(workHourInfo.get(j));
                    }
                } else {
                    duration += timeDifference;
                }
                beginIndex = null;
                endIndex = null;
            }
        }
        workHourInfo.removeAll(removeInfo);
        return duration;
    }

    @Override
    public List<SensorVehicleInfo> getBindVehicle(Integer sensorType) {
        List<SensorVehicleInfo> list = new ArrayList<>();
        String userId = userService.getCurrentUserInfo().getId().toString();
        // 获取当前用户所属组织及下级组织
        List<String> orgList = userService.getCurrentUserOrgIds();
        if (userId != null && !"".equals(userId) && CollectionUtils.isNotEmpty(orgList)) {
            List<String> reportDeviceTypes = Arrays.asList(ProtocolEnum.REPORT_DEVICE_TYPE);
            list = workHourSettingDao
                .getBindVehicle(userService.getCurrentUserUuid(), orgList, sensorType, reportDeviceTypes);
        }
        return list;
    }

    private void typeChange(WorkHourQuery query) throws Exception {
        query.setVehicleIdBytes(UuidUtils.getBytesFromUUID(UUID.fromString(query.getVehicleId())));
        query.setStartTime(DateUtils.parseDate(query.getStartTimeStr(), DATE_FORMAT).getTime() / 1000);
        query.setEndTime(DateUtils.parseDate(query.getEndTimeStr(), DATE_FORMAT).getTime() / 1000);
    }
}

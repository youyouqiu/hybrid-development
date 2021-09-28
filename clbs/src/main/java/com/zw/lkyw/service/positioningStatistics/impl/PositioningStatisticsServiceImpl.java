package com.zw.lkyw.service.positioningStatistics.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.google.common.collect.Sets;
import com.zw.lkyw.domain.common.PaasCloudZipDTO;
import com.zw.lkyw.domain.positioningStatistics.AllEnterpriseInfo;
import com.zw.lkyw.domain.positioningStatistics.ExceptionInfoQueryParam;
import com.zw.lkyw.domain.positioningStatistics.ExceptionInfoResult;
import com.zw.lkyw.domain.positioningStatistics.ExceptionListQueryParam;
import com.zw.lkyw.domain.positioningStatistics.ExceptionPositioningResult;
import com.zw.lkyw.domain.positioningStatistics.GroupListQueryParam;
import com.zw.lkyw.domain.positioningStatistics.GroupPositioningResult;
import com.zw.lkyw.domain.positioningStatistics.InterruptDetailInfo;
import com.zw.lkyw.domain.positioningStatistics.MonitorInterruptDetailInfo;
import com.zw.lkyw.domain.positioningStatistics.MonitorOfflineDetailInfo;
import com.zw.lkyw.domain.positioningStatistics.MonitorPositioningInfo;
import com.zw.lkyw.domain.positioningStatistics.MonthDetailInfo;
import com.zw.lkyw.domain.positioningStatistics.MonthListQueryParam;
import com.zw.lkyw.domain.positioningStatistics.MonthPositioningResult;
import com.zw.lkyw.domain.positioningStatistics.OfflineDetailInfo;
import com.zw.lkyw.repository.mysql.positioningStatistics.PositioningStatisticsDao;
import com.zw.lkyw.service.positioningStatistics.PositioningStatisticsService;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.core.TypeCacheManger;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.service.basicinfo.VehicleService;
import com.zw.platform.util.common.AddressUtil;
import com.zw.platform.util.common.BaseQueryBean;
import com.zw.platform.util.common.ComputingUtils;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.ZipUtil;
import jodd.util.StringUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.zw.platform.util.report.PaasCloudUrlEnum.BREAK_POSITIONING_URL;
import static com.zw.platform.util.report.PaasCloudUrlEnum.EXCEPTION_INFO_POSITIONING_URL;
import static com.zw.platform.util.report.PaasCloudUrlEnum.EXCEPTION_REPORT_POSITIONING_URL;
import static com.zw.platform.util.report.PaasCloudUrlEnum.GROUP_STATISTICS_URL;
import static com.zw.platform.util.report.PaasCloudUrlEnum.LAST_POSITIONING_URL;
import static com.zw.platform.util.report.PaasCloudUrlEnum.MONTH_POSITIONING_URL;
import static com.zw.platform.util.report.PaasCloudUrlEnum.OFF_POSITIONING_URL;

@Service
public class PositioningStatisticsServiceImpl implements PositioningStatisticsService {

    private final Logger logger =
        LogManager.getLogger(com.zw.lkyw.service.positioningStatistics.impl.PositioningStatisticsServiceImpl.class);

    @Autowired
    private PositioningStatisticsDao positioningStatisticsDao;

    @Autowired
    private UserService userService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    private OrganizationService organizationService;

    private TypeCacheManger cacheManger = TypeCacheManger.getInstance();

    public static final String DATE_FORMAT_SHORT = "yyyy-MM-dd HH:mm:ss";

    public static final String DATE_FORMAT_MONTH = "yyyy-MM";

    public static final String DATE_FORMAT = "yyyyMMddHHmmss";

    public static final String DAY_DATE_FORMAT = "yyyyMMdd";

    private static final String LOCATION_CHECK =
        "^((-?(([1-9]?[0-9])|(1[0-7][0-9]))([.])?\\d*)|(180))[,]((-?(([1-8]?[0-9])|)([.])?\\d*)|(90))$";

    /**
     * 第一次查询
     */
    public static final int IS_FIRST_SEARCH = 0;

    /**
     * 企业车辆定位统计报表查询
     * @param param 查询参数
     */
    @Override
    public PageGridBean enterpriseList(GroupListQueryParam param) throws Exception {
        Page<GroupPositioningResult> positioningResultPage = new Page<>();
        List<GroupPositioningResult> resultList = new ArrayList<>();
        RedisKey enterpriseListKey =
            HistoryRedisKeyEnum.VEHICLE_LOCATION_STATISTICS.of(SystemHelper.getCurrentUsername());
        Set<String> groupIdSet = new HashSet<>(Arrays.asList(param.getGroupIds().split(",")));
        if (param.getSearchType() == IS_FIRST_SEARCH) {
            RedisHelper.delete(enterpriseListKey);
            if (buildEnterpriseGroupList(param, resultList, groupIdSet)) {
                return getNoDataPageGridBean(param, positioningResultPage);
            }
            //维护缓存
            RedisHelper.addToList(enterpriseListKey, resultList);
            RedisHelper.expireKey(enterpriseListKey, 24 * 60 * 60);
        } else {
            resultList = RedisHelper.getList(enterpriseListKey, GroupPositioningResult.class);
        }
        resultList = getGroupPositioningResults(param, resultList, groupIdSet);
        return new PageGridBean(param, pageResult(param, positioningResultPage, resultList), true);
    }

    private boolean buildEnterpriseGroupList(GroupListQueryParam param, List<GroupPositioningResult> resultList,
        Set<String> groupIdSet) throws Exception {
        Map<String, OrganizationLdap> orgMap = organizationService.getOrgByUuids(groupIdSet);
        Map<String, List<MonitorPositioningInfo>> positioningInfoMap = getPositioningInfoMap(groupIdSet);
        List<Map.Entry<String, List<MonitorPositioningInfo>>> list = compare(positioningInfoMap);
        //监控对象和企业id关系
        Map<String, String> monitorIdGroupIdMap = new HashMap<>();
        //企业和result关系
        Map<String, GroupPositioningResult> map = new HashMap<>();
        //要查询的monitorIds
        StringBuilder monitorIds = getMonitorIdsParam(resultList, list, monitorIdGroupIdMap, map, orgMap);
        if (monitorIds.length() > 0) {
            Map<String, String> queryParam = getEnterpriseListQueryParam(param, monitorIds);
            JSONObject result = JSON.parseObject(HttpClientUtil.send(GROUP_STATISTICS_URL, queryParam));
            if (result != null) {
                buildEnterpriseResultList(param, resultList, monitorIdGroupIdMap, map, result);
            }
            return result == null;
        }
        return true;
    }

    /**
     * 根据企业id，获取监控对象和企业id的关系
     */
    private Map<String, List<MonitorPositioningInfo>> getPositioningInfoMap(Set<String> groupIdSet) {
        List<MonitorPositioningInfo> positioningInfoList =
            positioningStatisticsDao.findAllInfoByGroupIdSet(groupIdSet, null);
        Set<String> vids = vehicleService.getRedisAssignVid(SystemHelper.getCurrentUsername());
        return positioningInfoList.stream().filter(info -> vids.contains(info.getMonitorId()))
            .collect(Collectors.groupingBy(MonitorPositioningInfo::getGroupId));
    }

    /**
     * 企业车辆定位统计报表查询排序
     */
    private List<Map.Entry<String, List<MonitorPositioningInfo>>> compare(
        Map<String, List<MonitorPositioningInfo>> positioningInfoMap) {
        List<Map.Entry<String, List<MonitorPositioningInfo>>> list = new ArrayList<>(positioningInfoMap.entrySet());
        list.sort((o1, o2) -> o2.getValue().size() - o1.getValue().size());
        return list;
    }

    /**
     * 组装查询监控对象id
     */
    private StringBuilder getMonitorIdsParam(List<GroupPositioningResult> resultList,
        List<Map.Entry<String, List<MonitorPositioningInfo>>> list, Map<String, String> monitorIdGroupIdMap,
        Map<String, GroupPositioningResult> map, Map<String, OrganizationLdap> orgMap) {
        StringBuilder monitorIds = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            Map.Entry<String, List<MonitorPositioningInfo>> entry = list.get(i);
            GroupPositioningResult groupPositioningResult = new GroupPositioningResult();
            groupPositioningResult.setGroupId(entry.getKey());
            groupPositioningResult.setGroupName(orgMap.get(entry.getKey()).getName());
            groupPositioningResult.setVehicleNumbers(entry.getValue().size());
            groupPositioningResult.setIndex(i + 1);
            map.put(entry.getKey(), groupPositioningResult);
            resultList.add(groupPositioningResult);
            entry.getValue()
                .forEach(monitorPositioningInfo -> buildData(monitorIdGroupIdMap, monitorIds, monitorPositioningInfo));
        }
        return monitorIds;
    }

    /**
     * 组装查询参数
     */
    private Map<String, String> getEnterpriseListQueryParam(GroupListQueryParam param, StringBuilder monitorIds)
        throws Exception {
        String starTime = DateUtil.formatDate(param.getStartTime() + " 00:00:00", DATE_FORMAT_SHORT, DAY_DATE_FORMAT);
        String endTime = DateUtil.formatDate(param.getEndTime() + " 23:59:59", DATE_FORMAT_SHORT, DAY_DATE_FORMAT);
        return getParam(starTime, endTime, monitorIds);
    }

    /**
     * 组装缓存数据
     */
    private void buildEnterpriseResultList(GroupListQueryParam param, List<GroupPositioningResult> resultList,
        Map<String, String> monitorIdGroupIdMap, Map<String, GroupPositioningResult> map, JSONObject result) {
        Set<String> monitorIdSet = monitorIdGroupIdMap.keySet();
        JSONArray resultData = result.getJSONArray("data");
        long starTime = DateUtil.getStringToLong(param.getStartTime() + " 00:00:00", null) / 1000;
        if (resultData != null) {
            for (Object object : resultData) {
                JSONObject data = JSONObject.parseObject(object.toString());
                String monitorId = data.getString("monitorId");
                //合计定位总数
                int locationTotalNum = data.getIntValue("totalNum");
                //无效定位数
                int invalidLocationNum = data.getIntValue("invalidNum");
                //中断次数
                int locationBreakNum = data.getIntValue("interruptNum");
                //离线位移次数
                int offLineMoverNum = data.getIntValue("offLineMoverNum");
                long lastTime = data.getLongValue("lastTime");
                GroupPositioningResult positioningResult = map.get(monitorIdGroupIdMap.get(monitorId));
                positioningResult.setLocationTotal(positioningResult.getLocationTotal() + locationTotalNum);
                positioningResult.setInvalidLocations(positioningResult.getInvalidLocations() + invalidLocationNum);
                positioningResult.setInterruptNumber(positioningResult.getInterruptNumber() + locationBreakNum);
                positioningResult.setOfflineNumber(positioningResult.getOfflineNumber() + offLineMoverNum);
                if (locationBreakNum != 0) {
                    positioningResult.setInterruptVehicle(positioningResult.getInterruptVehicle() + 1);
                }
                if (offLineMoverNum != 0) {
                    positioningResult.setOfflineVehicle(positioningResult.getOfflineVehicle() + 1);
                }
                if (starTime > lastTime) {
                    positioningResult.setVehicleUnLocation(positioningResult.getVehicleUnLocation() + 1);
                }
                monitorIdSet.remove(monitorId);
            }
        }
        //存在从未定位过的数据
        if (monitorIdSet.size() > 0) {
            for (String monitorId : monitorIdSet) {
                GroupPositioningResult positioningResult = map.get(monitorIdGroupIdMap.get(monitorId));
                positioningResult.setVehicleUnLocation(positioningResult.getVehicleUnLocation() + 1);
            }
        }
        //计算定位统计有些率和无定位率
        calculatePercentage(resultList);
    }

    /**
     * 根据企业名模糊查询
     */
    private List<GroupPositioningResult> getGroupPositioningResults(GroupListQueryParam param,
        List<GroupPositioningResult> resultList, Set<String> groupIdSet) {
        if (StringUtil.isNotBlank(param.getSearch())) {
            List<String> finalGroupIdSet = organizationService.getOrgIdsByOrgName(param.getSearch(), groupIdSet);
            resultList = resultList.stream().filter(result -> finalGroupIdSet.contains(result.getGroupId()))
                .collect(Collectors.toList());
        }
        return resultList;
    }

    /**
     * 分页处理
     */
    private <T> Page<T> pageResult(BaseQueryBean param, Page<T> positioningResultPage, List<T> resultList) {
        //分页处理
        int startSize = (param.getPage().intValue() - 1) * param.getLimit().intValue();
        int endSize = getEndSize(param, resultList.size());
        int pages = (resultList.size() - 1) / param.getLimit().intValue() + 1; // 总页数
        if (resultList.size() <= startSize) {
            positioningResultPage.addAll(new ArrayList<>());
            positioningResultPage.setPages(0);
            positioningResultPage.setPageSize((param.getLimit().intValue()));
            positioningResultPage.setTotal(0);
        } else {
            positioningResultPage.addAll(resultList.subList(startSize, endSize));
            positioningResultPage.setPages(pages);
            positioningResultPage.setPageSize(param.getLimit().intValue());
            positioningResultPage.setTotal(resultList.size());
        }
        return positioningResultPage;
    }

    private Map<String, String> getParam(String starTime, String endTime, StringBuilder monitorIds) {
        Map<String, String> queryParam = new HashMap<>();
        queryParam.put("monitorIds", monitorIds.substring(0, monitorIds.length() - 1));
        queryParam.put("startTime", starTime);
        queryParam.put("endTime", endTime);
        return queryParam;
    }

    private Map<String, String> getExceptionParam(String starTime, String endTime, ExceptionListQueryParam param,
        StringBuffer monitorIds) {
        Map<String, String> queryParam = new HashMap<>();
        queryParam.put("monitorIds", monitorIds.substring(0, monitorIds.length() - 1));
        queryParam.put("startTime", starTime);
        queryParam.put("endTime", endTime);
        queryParam.put("locationThreshold", param.getLocationNumThreshold().toString());
        queryParam.put("invalidThreshold", param.getInvalidNumThreshold().toString());
        return queryParam;
    }

    /**
     * 企业车辆定位统计报表详情查询
     */
    @Override
    public PageGridBean enterpriseLocationInfo(GroupListQueryParam param) throws Exception {
        Page<MonitorPositioningInfo> resultPage = new Page<>();
        List<MonitorPositioningInfo> resultList = new ArrayList<>();
        RedisKey enterpriseLocationInfoKey =
            HistoryRedisKeyEnum.LOCATION_STATISTICS_DETAILS.of(SystemHelper.getCurrentUsername());
        if (param.getSearchType() == IS_FIRST_SEARCH) {
            RedisHelper.delete(enterpriseLocationInfoKey);
            if (buildLocationInfo(param, resultList)) {
                return getNoDataPageGridBean(param, resultPage);
            }
            //维护缓存
            RedisHelper.addToList(enterpriseLocationInfoKey, resultList);
            RedisHelper.expireKey(enterpriseLocationInfoKey, 24 * 60 * 60);
        } else {
            resultList = RedisHelper.getList(enterpriseLocationInfoKey, MonitorPositioningInfo.class);
        }
        resultList = getMonitorPositioningInfos(param, resultList);
        return new PageGridBean(param, pageResult(param, resultPage, resultList), true);
    }

    private boolean buildLocationInfo(GroupListQueryParam param, List<MonitorPositioningInfo> resultList)
        throws Exception {
        Set<String> groupIdSet = new HashSet<>(Arrays.asList(param.getGroupIds().split(",")));
        Map<String, OrganizationLdap> organizationLdapMap = organizationService.getOrgByUuids(groupIdSet);
        List<MonitorPositioningInfo> positioningInfoList = getMonitorPositioningInfos(groupIdSet);
        Map<String, MonitorPositioningInfo> monitorPositioningInfoMap = new HashMap<>();
        StringBuilder monitorIds = new StringBuilder();
        for (MonitorPositioningInfo monitorPositioningInfo : positioningInfoList) {
            monitorIds.append(monitorPositioningInfo.getMonitorId()).append(",");
            monitorPositioningInfoMap.put(monitorPositioningInfo.getMonitorId(), monitorPositioningInfo);
        }
        Map<String, String> queryParam = getEnterpriseListQueryParam(param, monitorIds);
        JSONObject result = JSON.parseObject(HttpClientUtil.send(GROUP_STATISTICS_URL, queryParam));
        if (result != null) {
            buildEnterpriseLocation(resultList, organizationLdapMap, monitorPositioningInfoMap, result);
        }
        return result == null;
    }

    /**
     * 模糊搜索车牌
     */
    private <T> List<T> getMonitorPositioningInfos(GroupListQueryParam param, List<T> resultList) {
        if (StringUtil.isNotEmpty(param.getSearch())) {
            Set<String> monitorIdSet =
                positioningStatisticsDao.findAllMonitorIdByGroupId(param.getGroupIds(), param.getSearch());
            Set<String> vids = userService.getCurrentUserMonitorIds();
            Set<String> finalMonitorIdSet = Sets.intersection(monitorIdSet, vids);
            resultList = resultList.stream()
                .filter(result -> finalMonitorIdSet.contains(getFieldValueByFieldName("monitorId", result)))
                .collect(Collectors.toList());
        }
        return resultList;
    }

    /**
     * 根据属性名获取属性值
     */
    public String getFieldValueByFieldName(String fieldName, Object object) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            //设置对象的访问权限，保证对private的属性的访问
            field.setAccessible(true);
            return field.get(object).toString();
        } catch (Exception e) {
            logger.error("反射设值异常", e);
            return null;
        }
    }

    /**
     * 组装数据（定位统计明细）
     */
    private void buildEnterpriseLocation(List<MonitorPositioningInfo> resultList,
        Map<String, OrganizationLdap> organizationLdapMap,
        Map<String, MonitorPositioningInfo> monitorPositioningInfoMap, JSONObject result) {
        JSONArray resultData = result.getJSONArray("data");
        if (resultData != null) {
            for (Object object : resultData) {
                JSONObject data = JSONObject.parseObject(object.toString());
                String monitorId = data.getString("monitorId");
                int locationTotalNum = data.getIntValue("totalNum");
                int invalidLocationNum = data.getIntValue("invalidNum");
                if (locationTotalNum == 0) {
                    continue;
                }
                MonitorPositioningInfo info = new MonitorPositioningInfo();
                BeanUtils.copyProperties(monitorPositioningInfoMap.get(monitorId), info);
                info.setLocationTotal(locationTotalNum);
                info.setInvalidLocations(invalidLocationNum);
                info.setGroupName(organizationLdapMap.get(info.getGroupId()).getName());
                info.setLocationEfficiency(Double.parseDouble(
                    ComputingUtils.calProportion(locationTotalNum - invalidLocationNum, locationTotalNum)));
                info.setLocationEfficiencyStr(formatRadioToStr(info.getLocationEfficiency()) + "%");
                resultList.add(info);
            }
            resultList.sort((o1, o2) -> o2.getLocationEfficiency().compareTo(o1.getLocationEfficiency()));
        }
    }

    private List<MonitorPositioningInfo> getMonitorPositioningInfos(Set<String> groupIdSet) {
        List<MonitorPositioningInfo> positioningInfoList =
            positioningStatisticsDao.findAllInfoByGroupIdSet(groupIdSet, null);
        Set<String> vids = userService.getCurrentUserMonitorIds();
        return positioningInfoList.stream().filter(info -> vids.contains(info.getMonitorId()))
            .collect(Collectors.toList());
    }

    @Override
    public PageGridBean enterpriseUnLocationInfo(GroupListQueryParam param) throws Exception {
        Page<MonitorPositioningInfo> resultPage = new Page<>();
        List<MonitorPositioningInfo> resultList = new ArrayList<>();
        RedisKey enterpriseUnLocationInfoKey =
            HistoryRedisKeyEnum.NO_LOCATION_STATISTICS_DETAILS.of(SystemHelper.getCurrentUsername());
        if (param.getSearchType() == IS_FIRST_SEARCH) {
            RedisHelper.delete(enterpriseUnLocationInfoKey);
            if (buildUnLocationInfoData(param, resultList)) {
                return getNoDataPageGridBean(param, resultPage);
            }
            //维护缓存
            RedisHelper.addToList(enterpriseUnLocationInfoKey, resultList);
            RedisHelper.expireKey(enterpriseUnLocationInfoKey, 24 * 60 * 60);
        } else {
            resultList = RedisHelper.getList(enterpriseUnLocationInfoKey, MonitorPositioningInfo.class);
        }
        resultList = getMonitorPositioningInfos(param, resultList);
        return new PageGridBean(param, pageResult(param, resultPage, resultList), true);
    }

    private boolean buildUnLocationInfoData(GroupListQueryParam param, List<MonitorPositioningInfo> resultList)
        throws Exception {
        Set<String> groupIdSet = new HashSet<>(Arrays.asList(param.getGroupIds().split(",")));
        Map<String, OrganizationLdap> organizationLdapMap = organizationService.getOrgByUuids(groupIdSet);
        List<MonitorPositioningInfo> positioningInfoList = getMonitorPositioningInfos(groupIdSet);
        Map<String, MonitorPositioningInfo> monitorPositioningInfoMap = new HashMap<>();
        StringBuilder monitorIds = new StringBuilder();
        for (MonitorPositioningInfo monitorPositioningInfo : positioningInfoList) {
            monitorIds.append(monitorPositioningInfo.getMonitorId()).append(",");
            monitorPositioningInfoMap.put(monitorPositioningInfo.getMonitorId(), monitorPositioningInfo);
        }
        return unLocationBuild(param, resultList, organizationLdapMap, monitorPositioningInfoMap, monitorIds);
    }

    private boolean unLocationBuild(GroupListQueryParam param, List<MonitorPositioningInfo> resultList,
        Map<String, OrganizationLdap> organizationLdapMap,
        Map<String, MonitorPositioningInfo> monitorPositioningInfoMap, StringBuilder monitorIds) throws Exception {
        Set<String> monitorIdSet = new HashSet<>(monitorPositioningInfoMap.keySet());
        String starTime = DateUtil.formatDate(param.getStartTime() + " 00:00:00", DATE_FORMAT_SHORT, DATE_FORMAT);
        Map<String, String> queryParam = new HashMap<>();
        queryParam.put("monitorIds", monitorIds.substring(0, monitorIds.length() - 1));
        JSONObject result = JSON.parseObject(HttpClientUtil.send(LAST_POSITIONING_URL, queryParam));
        if (result != null) {
            buildUnLocationInfo(resultList, organizationLdapMap, monitorPositioningInfoMap, starTime, monitorIdSet,
                result);
        }
        return result == null;
    }

    private void buildUnLocationInfo(List<MonitorPositioningInfo> resultList,
        Map<String, OrganizationLdap> organizationLdapMap,
        Map<String, MonitorPositioningInfo> monitorPositioningInfoMap, String starTime, Set<String> monitorIdSet,
        JSONObject result) {
        JSONArray resultData = result.getJSONArray("data");
        if (resultData != null) {
            Set<String> locationSet = new HashSet<>();
            Map<String, MonitorPositioningInfo> unAddress = new HashMap<>();
            for (Object object : resultData) {
                JSONObject data = JSONObject.parseObject(object.toString());
                //监控对象id
                String monitorId = data.getString("monitorId");
                //最后定位时间
                long lastLocationTime = data.getLongValue("lastTime");
                //最后定位位置(经度纬度)
                String lastLocation = data.getString("lastLocation");
                //最后定位位置
                String lastLocationAddress = data.getString("lastAddress");
                if (lastLocationTime < (DateUtil.getStringToLong(starTime, DATE_FORMAT) / 1000)) {
                    MonitorPositioningInfo info = new MonitorPositioningInfo();
                    BeanUtils.copyProperties(monitorPositioningInfoMap.get(monitorId), info);
                    info.setGroupName(organizationLdapMap.get(info.getGroupId()).getName());
                    if (StringUtil.isNotEmpty(lastLocationAddress)) {
                        info.setAddress(lastLocationAddress);
                    } else {
                        if (checkLocation(lastLocation)) {
                            locationSet.add(lastLocation);
                        }
                        unAddress.put(lastLocation, info);
                    }
                    info.setLocationDate(lastLocationTime);
                    info.setLocationDateStr(DateUtil.getLongToDateStr(lastLocationTime * 1000, null));
                    resultList.add(info);
                }
                monitorIdSet.remove(monitorId);
            }
            if (locationSet.size() > 0) {
                Map<String, String> addressMap = AddressUtil.batchInverseAddress(locationSet);
                for (Map.Entry<String, String> entry : addressMap.entrySet()) {
                    unAddress.get(entry.getKey()).setAddress(entry.getValue());
                }
            }
        }
        List<MonitorPositioningInfo> unPositioningInfoList = new ArrayList<>();
        //存在从未定位过的数据
        if (monitorIdSet.size() > 0) {
            for (String monitorId : monitorIdSet) {
                MonitorPositioningInfo info = monitorPositioningInfoMap.get(monitorId);
                info.setAddress("--");
                info.setLocationDate(0);
                info.setLocationDateStr("--");
                info.setGroupName(organizationLdapMap.get(info.getGroupId()).getName());
                unPositioningInfoList.add(info);
            }
        }
        //排序
        Comparator<MonitorPositioningInfo> byLocationDate =
            Comparator.comparing(MonitorPositioningInfo::getLocationDate);
        Comparator<MonitorPositioningInfo> byMonitorName = Comparator.comparing(MonitorPositioningInfo::getMonitorName);
        resultList.sort(byLocationDate);
        unPositioningInfoList.sort(byMonitorName);
        resultList.addAll(unPositioningInfoList);
    }

    @Override
    public PageGridBean enterpriseInterruptInfo(GroupListQueryParam param) throws Exception {
        Page<MonitorInterruptDetailInfo> resultPage = new Page<>();
        List<MonitorInterruptDetailInfo> resultList = new ArrayList<>();
        RedisKey enterpriseInterruptKey =
            HistoryRedisKeyEnum.LOCATION_INTERRUPT_STATISTICS_DETAILS.of(SystemHelper.getCurrentUsername());
        if (param.getSearchType() == IS_FIRST_SEARCH) {
            RedisHelper.delete(enterpriseInterruptKey);
            if (buildInterruptData(param, resultList)) {
                return getNoDataPageGridBean(param, resultPage);
            }
            //维护缓存
            RedisHelper.addToList(enterpriseInterruptKey, resultList);
            RedisHelper.expireKey(enterpriseInterruptKey, 24 * 60 * 60);
        } else {
            resultList = RedisHelper.getList(enterpriseInterruptKey, MonitorInterruptDetailInfo.class);
        }
        resultList = getMonitorPositioningInfos(param, resultList);
        return new PageGridBean(param, pageResult(param, resultPage, resultList), true);
    }

    private boolean buildInterruptData(GroupListQueryParam param, List<MonitorInterruptDetailInfo> resultList)
        throws Exception {
        Set<String> groupIdSet = new HashSet<>(Arrays.asList(param.getGroupIds().split(",")));
        Map<String, OrganizationLdap> organizationLdapMap = organizationService.getOrgByUuids(groupIdSet);
        List<MonitorPositioningInfo> positioningInfoList = getMonitorPositioningInfos(groupIdSet);
        Map<String, MonitorPositioningInfo> monitorPositioningInfoMap = new HashMap<>();
        StringBuilder monitorIds = new StringBuilder();
        for (MonitorPositioningInfo monitorPositioningInfo : positioningInfoList) {
            monitorIds.append(monitorPositioningInfo.getMonitorId()).append(",");
            monitorPositioningInfoMap.put(monitorPositioningInfo.getMonitorId(), monitorPositioningInfo);
        }
        return interruptInfoBuild(param, resultList, organizationLdapMap, monitorPositioningInfoMap, monitorIds);
    }

    private boolean interruptInfoBuild(GroupListQueryParam param, List<MonitorInterruptDetailInfo> resultList,
        Map<String, OrganizationLdap> organizationLdapMap,
        Map<String, MonitorPositioningInfo> monitorPositioningInfoMap, StringBuilder monitorIds) throws Exception {
        String starTime = DateUtil.formatDate(param.getStartTime() + " 00:00:00", DATE_FORMAT_SHORT, DATE_FORMAT);
        String endTime = DateUtil.formatDate(param.getEndTime() + " 23:59:59", DATE_FORMAT_SHORT, DATE_FORMAT);
        Map<String, String> queryParam = getQueryParamMap(starTime, endTime, monitorIds);
        String interruptInfoResult = HttpClientUtil.send(BREAK_POSITIONING_URL, queryParam);
        JSONArray resultData = null;
        if (interruptInfoResult != null) {
            PaasCloudZipDTO dto = JSON.parseObject(interruptInfoResult, PaasCloudZipDTO.class);
            if (dto != null && dto.getData() != null) {
                resultData = JSONArray.parseArray(ZipUtil
                    .uncompress(dto.getData().getBytes(StandardCharsets.ISO_8859_1),
                        StandardCharsets.UTF_8.toString()));
                buildEnterpriseInterruptData(resultList, organizationLdapMap, monitorPositioningInfoMap, resultData);
            }
        }
        return resultData == null;
    }

    private Map<String, String> getQueryParamMap(String starTime, String endTime, StringBuilder monitorIds) {
        Map<String, String> queryParam = new HashMap<>();
        queryParam.put("monitorIds", monitorIds.substring(0, monitorIds.length() - 1));
        queryParam.put("startTime", starTime);
        queryParam.put("endTime", endTime);
        return queryParam;
    }

    private void buildEnterpriseInterruptData(List<MonitorInterruptDetailInfo> resultList,
        Map<String, OrganizationLdap> organizationLdapMap,
        Map<String, MonitorPositioningInfo> monitorPositioningInfoMap, JSONArray resultData) {
        Map<String, List<MonitorInterruptDetailInfo>> noStartAddressMap = new HashMap<>();
        Map<String, List<MonitorInterruptDetailInfo>> noEndAddressMap = new HashMap<>();
        Set<String> locationSet = new HashSet<>();
        if (resultData != null) {
            for (Object object : resultData) {
                JSONObject data = JSONObject.parseObject(object.toString());
                //监控对象id
                String monitorId = data.getString("monitorId");
                List<InterruptDetailInfo> detailInfos =
                    JSONObject.parseArray(data.getString("detailInfo"), InterruptDetailInfo.class);
                for (InterruptDetailInfo detailInfo : detailInfos) {
                    MonitorInterruptDetailInfo info = new MonitorInterruptDetailInfo();
                    BeanUtils.copyProperties(monitorPositioningInfoMap.get(monitorId), info);
                    if (StringUtil.isEmpty(detailInfo.getStartAddress())) {
                        if (checkLocation(detailInfo.getStartLocation())) {
                            locationSet.add(detailInfo.getStartLocation());
                        }
                        List<MonitorInterruptDetailInfo> noAddressList =
                            noStartAddressMap.get(detailInfo.getStartLocation());
                        if (noAddressList == null) {
                            noAddressList = new ArrayList<>();
                        }
                        noAddressList.add(info);
                        noStartAddressMap.put(detailInfo.getStartLocation(), noAddressList);
                    }
                    if (StringUtil.isEmpty(detailInfo.getEndAddress())) {
                        if (checkLocation(detailInfo.getEndLocation())) {
                            locationSet.add(detailInfo.getEndLocation());
                        }
                        List<MonitorInterruptDetailInfo> noAddressList =
                            noEndAddressMap.get(detailInfo.getEndLocation());
                        if (noAddressList == null) {
                            noAddressList = new ArrayList<>();
                        }
                        noAddressList.add(info);
                        noEndAddressMap.put(detailInfo.getEndLocation(), noAddressList);
                    }
                    detailInfo.setStartTimeStr(DateUtil.getLongToDateStr(detailInfo.getStartTime() * 1000, null));
                    detailInfo.setEndTimeStr(DateUtil.getLongToDateStr(detailInfo.getEndTime() * 1000, null));
                    detailInfo.setDurationStr(DateUtil.secondsToHhMmSs(detailInfo.getDuration()));
                    info.setDetailInfo(detailInfo);
                    info.setGroupName(organizationLdapMap.get(info.getGroupId()).getName());
                    resultList.add(info);
                }
            }
            //处理逆地址未解析的数据

            if (locationSet.size() > 0) {
                Map<String, String> addressMap = AddressUtil.batchInverseAddress(locationSet);
                String address;
                for (Map.Entry<String, List<MonitorInterruptDetailInfo>> entry : noStartAddressMap.entrySet()) {
                    for (MonitorInterruptDetailInfo info : entry.getValue()) {
                        address = addressMap.get(info.getDetailInfo().getStartLocation());
                        info.getDetailInfo().setStartAddress(address != null ? address : "--");
                    }
                }
                for (Map.Entry<String, List<MonitorInterruptDetailInfo>> entry : noEndAddressMap.entrySet()) {
                    for (MonitorInterruptDetailInfo info : entry.getValue()) {
                        address = addressMap.get(info.getDetailInfo().getEndLocation());
                        info.getDetailInfo().setEndAddress(address != null ? address : "--");
                    }
                }
            }
        }
        //排序
        Comparator<MonitorInterruptDetailInfo> byLocationDate =
            Comparator.comparing(MonitorInterruptDetailInfo::getDetailInfoStarTime);
        resultList.sort(byLocationDate);
    }

    private boolean checkLocation(String location) {
        String[] locationArray = location.split(",");
        //中国国内的经纬度范围：
        // 经度：73.66 < x < 135.05
        // 纬度：3.86 < y < 53.55
        if (locationArray.length == 2 && location.matches(LOCATION_CHECK)) {
            return Double.parseDouble(locationArray[0]) > 73.66 && Double.parseDouble(locationArray[0]) < 135.05
                && Double.parseDouble(locationArray[1]) > 3.86 && Double.parseDouble(locationArray[1]) < 53.55;
        }
        return false;
    }

    @Override
    public PageGridBean enterpriseOfflineInfo(GroupListQueryParam param) throws Exception {
        Page<MonitorOfflineDetailInfo> resultPage = new Page<>();
        List<MonitorOfflineDetailInfo> resultList = new ArrayList<>();
        RedisKey enterpriseOfflineKey =
            HistoryRedisKeyEnum.OFFLINE_DISPLACEMENT_STATISTICS_DETAILS.of(SystemHelper.getCurrentUsername());
        if (param.getSearchType() == IS_FIRST_SEARCH) {
            RedisHelper.delete(enterpriseOfflineKey);
            if (buildOfflineData(param, resultList)) {
                return getNoDataPageGridBean(param, resultPage);
            }
            //维护缓存
            RedisHelper.addToList(enterpriseOfflineKey, resultList);
            RedisHelper.expireKey(enterpriseOfflineKey, 24 * 60 * 60);
        } else {
            resultList = RedisHelper.getList(enterpriseOfflineKey, MonitorOfflineDetailInfo.class);
        }
        resultList = getMonitorPositioningInfos(param, resultList);
        return new PageGridBean(param, pageResult(param, resultPage, resultList), true);
    }

    private boolean buildOfflineData(GroupListQueryParam param, List<MonitorOfflineDetailInfo> resultList)
        throws Exception {
        Set<String> groupIdSet = new HashSet<>(Arrays.asList(param.getGroupIds().split(",")));
        Map<String, OrganizationLdap> organizationLdapMap = organizationService.getOrgByUuids(groupIdSet);
        List<MonitorPositioningInfo> positioningInfoList = getMonitorPositioningInfos(groupIdSet);
        Map<String, MonitorPositioningInfo> monitorPositioningInfoMap = new HashMap<>();
        StringBuilder monitorIds = new StringBuilder();
        for (MonitorPositioningInfo monitorPositioningInfo : positioningInfoList) {
            monitorIds.append(monitorPositioningInfo.getMonitorId()).append(",");
            monitorPositioningInfoMap.put(monitorPositioningInfo.getMonitorId(), monitorPositioningInfo);
        }
        return offlineBuild(param, resultList, organizationLdapMap, monitorPositioningInfoMap, monitorIds);
    }

    private boolean offlineBuild(GroupListQueryParam param, List<MonitorOfflineDetailInfo> resultList,
        Map<String, OrganizationLdap> organizationLdapMap,
        Map<String, MonitorPositioningInfo> monitorPositioningInfoMap, StringBuilder monitorIds) throws Exception {
        String starTime = DateUtil.formatDate(param.getStartTime() + " 00:00:00", DATE_FORMAT_SHORT, DATE_FORMAT);
        String endTime = DateUtil.formatDate(param.getEndTime() + " 23:59:59", DATE_FORMAT_SHORT, DATE_FORMAT);
        Map<String, String> queryParam = getQueryParamMap(starTime, endTime, monitorIds);
        JSONObject result = JSON.parseObject(HttpClientUtil.send(OFF_POSITIONING_URL, queryParam));
        if (result != null) {
            buildEnterpriseOfflineData(resultList, organizationLdapMap, monitorPositioningInfoMap, result);
        }
        return result == null;
    }

    private void buildEnterpriseOfflineData(List<MonitorOfflineDetailInfo> resultList,
        Map<String, OrganizationLdap> organizationLdapMap,
        Map<String, MonitorPositioningInfo> monitorPositioningInfoMap, JSONObject result) {
        JSONArray resultData = result.getJSONArray("data");
        if (resultData != null) {
            for (Object object : resultData) {
                JSONObject data = JSONObject.parseObject(object.toString());
                //监控对象id
                String monitorId = data.getString("monitorId");
                List<OfflineDetailInfo> detailInfos =
                    JSONObject.parseArray(data.getString("detailInfo"), OfflineDetailInfo.class);
                for (OfflineDetailInfo detailInfo : detailInfos) {
                    MonitorOfflineDetailInfo info = new MonitorOfflineDetailInfo();
                    BeanUtils.copyProperties(monitorPositioningInfoMap.get(monitorId), info);
                    detailInfo
                        .setOffLineStartTimeStr(DateUtil.getLongToDateStr(detailInfo.getStartTime() * 1000, null));
                    detailInfo.setOffLineEndTimeStr(DateUtil.getLongToDateStr(detailInfo.getEndTime() * 1000, null));
                    BigDecimal mile = BigDecimal.valueOf(detailInfo.getDisplaceMile());
                    detailInfo.setDisplaceMile(mile.setScale(1, BigDecimal.ROUND_DOWN).doubleValue());
                    info.setDetailInfo(detailInfo);
                    info.setGroupName(organizationLdapMap.get(info.getGroupId()).getName());
                    resultList.add(info);
                }
            }
            //排序
            Comparator<MonitorOfflineDetailInfo> byLocationDate =
                Comparator.comparing(MonitorOfflineDetailInfo::getDetailInfoEndTime);
            resultList.sort(byLocationDate);
        }
    }

    @Override
    public PageGridBean monthPositioningList(MonthListQueryParam param) throws Exception {
        Page<MonthPositioningResult> resultPage = new Page<>();
        List<MonthPositioningResult> resultList = new ArrayList<>();
        List<String> monitorIdList = Arrays.asList(param.getMonitorIds().split(","));
        RedisKey monthPositioningKey =
            HistoryRedisKeyEnum.MONTH_LOCATION_STATISTICS.of(SystemHelper.getCurrentUsername());
        if (param.getSearchType() == IS_FIRST_SEARCH) {
            RedisHelper.delete(monthPositioningKey);
            if (buildMonthInfoData(param, resultList, monitorIdList)) {
                return getNoDataPageGridBean(param, resultPage);
            }
            //维护缓存
            RedisHelper.addToList(monthPositioningKey, resultList);
            RedisHelper.expireKey(monthPositioningKey, 24 * 60 * 60);
        } else {
            resultList = RedisHelper.getList(monthPositioningKey, MonthPositioningResult.class);
        }
        resultList = getMonthPositioningResultsBySearch(param, resultList, monitorIdList);
        return new PageGridBean(param, pageResult(param, resultPage, resultList), true);
    }

    private boolean buildMonthInfoData(MonthListQueryParam param, List<MonthPositioningResult> resultList,
        List<String> monitorIdList) throws Exception {
        StringBuilder monitorIds = new StringBuilder();
        int days = getMonthOfDays(param.getTime(), DATE_FORMAT_MONTH);
        Map<String, MonthPositioningResult> monthPositioningResultHashMap =
            getResultInfo(monitorIds, monitorIdList, days);
        String starTime = DateUtil.formatDate(param.getTime() + "-01 00:00:00", DATE_FORMAT_SHORT, DAY_DATE_FORMAT);
        String endTime =
            DateUtil.formatDate(param.getTime() + "-" + days + " 23:59:59", DATE_FORMAT_SHORT, DAY_DATE_FORMAT);
        Map<String, String> queryParam = getQueryParamMap(starTime, endTime, monitorIds);
        JSONObject result = JSON.parseObject(HttpClientUtil.send(MONTH_POSITIONING_URL, queryParam));
        if (result != null) {
            buildMonthData(resultList, monthPositioningResultHashMap, result);
        }
        return result == null;
    }

    private void buildMonthData(List<MonthPositioningResult> resultList,
        Map<String, MonthPositioningResult> monthPositioningResultHashMap, JSONObject result) {
        JSONArray resultData = result.getJSONArray("data");
        if (resultData != null) {
            for (Object object : resultData) {
                JSONObject data = JSONObject.parseObject(object.toString());
                //监控对象id
                String monitorId = data.getString("monitorId");
                List<MonthDetailInfo> detailInfos =
                    JSONObject.parseArray(data.getString("dayStatistic"), MonthDetailInfo.class);
                MonthPositioningResult info = monthPositioningResultHashMap.get(monitorId);
                //累计无效数
                int totalInvalidNum = 0;
                //累计定位总数
                int totalLocationNum = 0;
                List<MonthDetailInfo> monthDetailInfoList = info.getMonthDetailInfoList();
                for (MonthDetailInfo detailInfo : detailInfos) {
                    LocalDateTime dateTime = LocalDateTime.ofEpochSecond(detailInfo.getDay(), 0, ZoneOffset.ofHours(8));
                    int day = dateTime.getDayOfMonth();
                    MonthDetailInfo monthDetailInfo = monthDetailInfoList.get(day - 1);
                    detailInfo.setIndex(day);
                    totalInvalidNum += detailInfo.getInvalidNum();
                    totalLocationNum += detailInfo.getTotalNum();
                    detailInfo.setRatioStr(formatRadioToStr(detailInfo.getRatio()) + "%");
                    BeanUtils.copyProperties(detailInfo, monthDetailInfo);
                }
                info.setTotalInvalidNum(totalInvalidNum);
                info.setTotalLocationNum(totalLocationNum);
                info.setTotalRatio(Double
                    .parseDouble(ComputingUtils.calProportion(totalLocationNum - totalInvalidNum, totalLocationNum)));
                info.setTotalRatioStr(formatRadioToStr(info.getTotalRatio()) + "%");
                resultList.add(info);
            }
            //排序
            Comparator<MonthPositioningResult> byLocationNum =
                Comparator.comparing(MonthPositioningResult::getTotalLocationNum).reversed();
            resultList.sort(byLocationNum);
        }
    }

    private List<MonthPositioningResult> getMonthPositioningResultsBySearch(MonthListQueryParam param,
        List<MonthPositioningResult> resultList, List<String> monitorIdList) {
        if (StringUtil.isNotEmpty(param.getSearch())) {
            Set<String> searchMonitorIds = getSearchMonitorIds(monitorIdList, param.getSearch());
            resultList = resultList.stream().filter(result -> searchMonitorIds.contains(result.getMonitorId()))
                .collect(Collectors.toList());
        }
        return resultList;
    }

    @Override
    public PageGridBean exceptionPositioningList(ExceptionListQueryParam param) throws Exception {
        Page<ExceptionPositioningResult> resultPage = new Page<>();
        List<ExceptionPositioningResult> resultList = new ArrayList<>();
        List<String> monitorIdList = Arrays.asList(param.getMonitorIds().split(","));
        RedisKey exceptionPositioningKey =
            HistoryRedisKeyEnum.ANOMALY_LOCATION_STATISTICS.of(SystemHelper.getCurrentUsername());
        if (param.getSearchType() == IS_FIRST_SEARCH) {
            RedisHelper.delete(exceptionPositioningKey);
            if (buildExceptionListData(param, resultList, monitorIdList)) {
                return getNoDataPageGridBean(param, resultPage);
            }
            //维护缓存
            RedisHelper.addToList(exceptionPositioningKey, resultList);
            RedisHelper.expireKey(exceptionPositioningKey, 24 * 60 * 60);
        } else {
            resultList = RedisHelper.getList(exceptionPositioningKey, ExceptionPositioningResult.class);
        }
        if (StringUtil.isNotEmpty(param.getSearch())) {
            Set<String> searchMonitorIds = getSearchMonitorIds(monitorIdList, param.getSearch());
            resultList = resultList.stream().filter(result -> searchMonitorIds.contains(result.getMonitorId()))
                .collect(Collectors.toList());
        }
        return new PageGridBean(param, pageResult(param, resultPage, resultList), true);
    }

    private boolean buildExceptionListData(ExceptionListQueryParam param, List<ExceptionPositioningResult> resultList,
        List<String> monitorIdList) throws Exception {
        int days = getMonthOfDays(param.getTime(), DATE_FORMAT_MONTH);
        StringBuffer monitorIds = new StringBuffer();
        Map<String, ExceptionPositioningResult> exceptionPositioningResultHashMap =
            getExceptionResultInfo(monitorIds, monitorIdList);
        String starTime = DateUtil.formatDate(param.getTime() + "-01 00:00:00", DATE_FORMAT_SHORT, DAY_DATE_FORMAT);
        String endTime =
            DateUtil.formatDate(param.getTime() + "-" + days + " 23:59:59", DATE_FORMAT_SHORT, DAY_DATE_FORMAT);
        Map<String, String> queryParam = getExceptionParam(starTime, endTime, param, monitorIds);
        JSONObject result = JSON.parseObject(HttpClientUtil.send(EXCEPTION_REPORT_POSITIONING_URL, queryParam));
        if (result != null) {
            buildExceptionData(resultList, exceptionPositioningResultHashMap, result);
        }
        return result == null;
    }

    @Override
    public PageGridBean exceptionPositioningInfo(ExceptionInfoQueryParam param) throws Exception {
        Page<ExceptionInfoResult> resultPage = new Page<>();
        List<ExceptionInfoResult> resultList = new ArrayList<>();
        RedisKey exceptionInfoKey =
            HistoryRedisKeyEnum.EXCEPTION_LOCATION_DETAILS.of(SystemHelper.getCurrentUsername());
        if (param.getSearchType() == IS_FIRST_SEARCH) {
            RedisHelper.delete(exceptionInfoKey);
            if (buildExceptionInfoListData(param, resultList)) {
                return getNoDataPageGridBean(param, resultPage);
            }
            //维护缓存
            RedisHelper.addToList(exceptionInfoKey, resultList);
            RedisHelper.expireKey(exceptionInfoKey, 24 * 60 * 60);
        } else {
            resultList = RedisHelper.getList(exceptionInfoKey, ExceptionInfoResult.class);
        }
        return new PageGridBean(param, pageResult(param, resultPage, resultList), true);
    }

    private boolean buildExceptionInfoListData(ExceptionInfoQueryParam param, List<ExceptionInfoResult> resultList)
        throws Exception {
        int days = getMonthOfDays(param.getTime(), DATE_FORMAT_MONTH);
        ExceptionInfoResult exceptionInfoResult = new ExceptionInfoResult();
        Map<String, String> configMap = RedisHelper.getHashMap(RedisKeyEnum.MONITOR_INFO.of(param.getMonitorId()));
        if (configMap != null) {
            String monitorType = configMap.get("monitorType");
            exceptionInfoResult.setMonitorId(configMap.get("id"));
            exceptionInfoResult.setMonitorName(configMap.get("name"));
            exceptionInfoResult.setGroupId(configMap.get("orgId"));
            exceptionInfoResult.setGroupName(configMap.get("orgName"));
            exceptionInfoResult.setVehicleType(cacheManger.getVehicleType(configMap.get("vehicleType")).getType());
            exceptionInfoResult.setPlateColor(getPlateColor(configMap.get("plateColor"), monitorType));
        } else {
            return true;
        }
        String starTime = DateUtil.formatDate(param.getTime() + "-01 00:00:00", DATE_FORMAT_SHORT, DAY_DATE_FORMAT);
        String endTime =
            DateUtil.formatDate(param.getTime() + "-" + days + " 23:59:59", DATE_FORMAT_SHORT, DAY_DATE_FORMAT);
        Map<String, String> queryParam = new HashMap<>();
        queryParam.put("monitorIds", param.getMonitorId());
        queryParam.put("startTime", starTime);
        queryParam.put("endTime", endTime);
        JSONObject result = JSON.parseObject(HttpClientUtil.send(EXCEPTION_INFO_POSITIONING_URL, queryParam));
        if (result != null) {
            buildExceptionInfoData(resultList, exceptionInfoResult, result);
        }
        return result == null;
    }

    private void buildExceptionInfoData(List<ExceptionInfoResult> resultList, ExceptionInfoResult exceptionInfoResult,
        JSONObject result) {
        JSONArray resultData = result.getJSONArray("data");
        if (resultData != null) {
            for (Object object : resultData) {
                JSONObject data = JSONObject.parseObject(object.toString());
                List<MonthDetailInfo> detailInfos =
                    JSONObject.parseArray(data.getString("dayStatistic"), MonthDetailInfo.class);
                for (MonthDetailInfo detailInfo : detailInfos) {
                    ExceptionInfoResult infoResult = new ExceptionInfoResult();
                    BeanUtils.copyProperties(exceptionInfoResult, infoResult);
                    infoResult.setInvalidNum(detailInfo.getInvalidNum());
                    infoResult.setLocationNum(detailInfo.getTotalNum());
                    infoResult.setTime(detailInfo.getDay());
                    Date date = new Date(detailInfo.getDay() * 1000);
                    SimpleDateFormat sdf = new SimpleDateFormat(DAY_DATE_FORMAT);
                    String dateStr = sdf.format(date);
                    infoResult.setTimeStr(dateStr);
                    resultList.add(infoResult);
                }
            }
            //排序
            Comparator<ExceptionInfoResult> byLocationTime = Comparator.comparing(ExceptionInfoResult::getTime);
            resultList.sort(byLocationTime);
        }
    }

    private <T> PageGridBean getNoDataPageGridBean(BaseQueryBean param, Page<T> resultPage) {
        resultPage.addAll(new ArrayList<>());
        resultPage.setPages(0);
        resultPage.setPageSize(param.getLimit().intValue());
        resultPage.setTotal(0);
        return new PageGridBean(param, resultPage, true);
    }

    private void buildExceptionData(List<ExceptionPositioningResult> resultList,
        Map<String, ExceptionPositioningResult> exceptionPositioningResultHashMap, JSONObject result) {
        JSONArray resultData = result.getJSONArray("data");
        if (resultData != null) {
            for (Object object : resultData) {
                JSONObject data = JSONObject.parseObject(object.toString());
                String monitorId = data.getString("monitorId");
                ExceptionPositioningResult exceptionPositioningResult =
                    exceptionPositioningResultHashMap.get(monitorId);
                exceptionPositioningResult.setNoLocationDayNum(data.getIntValue("noLocationDayNum"));
                exceptionPositioningResult.setLocationRatio(data.getDoubleValue("locationRatio"));
                exceptionPositioningResult
                    .setLocationRatioStr(formatRadioToStr(exceptionPositioningResult.getLocationRatio()) + "%");
                exceptionPositioningResult.setInvalidDayNum(data.getIntValue("invalidDayNum"));
                exceptionPositioningResult.setInvalidLocationNum(data.getIntValue("invalidNum"));
                exceptionPositioningResult.setRation(data.getDoubleValue("ration"));
                exceptionPositioningResult.setRationStr(formatRadioToStr(exceptionPositioningResult.getRation()) + "%");
                resultList.add(exceptionPositioningResult);
            }
            //排序
            Comparator<ExceptionPositioningResult> byMonitorName =
                Comparator.comparing(ExceptionPositioningResult::getMonitorName);
            resultList.sort(byMonitorName);
        }
    }

    private Map<String, ExceptionPositioningResult> getExceptionResultInfo(StringBuffer monitorIds,
        List<String> monitorIdList) {
        Map<String, ExceptionPositioningResult> exceptionPositioningResultHashMap = new HashMap<>();
        Map<String, Map<String, String>> configMap =
            RedisHelper.batchGetHashMap(RedisKeyEnum.MONITOR_INFO.ofs(monitorIdList)).stream()
                .collect(Collectors.toMap(o -> o.get("id"), Function.identity()));
        for (String monitorId : monitorIdList) {
            Map<String, String> config = configMap.get(monitorId);
            ExceptionPositioningResult exceptionResult = new ExceptionPositioningResult();
            String monitorType = config.get("monitorType");
            exceptionResult.setMonitorId(config.get("id"));
            exceptionResult.setMonitorName(config.get("name"));
            exceptionResult.setGroupId(config.get("orgId"));
            exceptionResult.setGroupName(config.get("orgName"));
            exceptionResult.setVehicleType(cacheManger.getVehicleType(config.get("vehicleType")).getType());
            exceptionResult.setPlateColor(getPlateColor(config.get("plateColor"), monitorType));
            exceptionPositioningResultHashMap.put(config.get("id"), exceptionResult);
            monitorIds.append(config.get("id")).append(",");
        }
        return exceptionPositioningResultHashMap;
    }

    private Map<String, MonthPositioningResult> getResultInfo(StringBuilder monitorIds, List<String> monitorIdList,
        int days) {
        Map<String, MonthPositioningResult> monthPositioningResultHashMap = new HashMap<>();
        Map<String, Map<String, String>> configMap =
            RedisHelper.batchGetHashMap(RedisKeyEnum.MONITOR_INFO.ofs(monitorIdList)).stream()
                .collect(Collectors.toMap(o -> o.get("id"), Function.identity()));
        String monitorName;
        for (String monitorId : monitorIdList) {
            Map<String, String> config = configMap.get(monitorId);
            if (config != null) {
                monitorName = config.get("name");
                MonthPositioningResult monthResult = new MonthPositioningResult();
                List<MonthDetailInfo> monthDetailInfoList = new ArrayList<>(days);
                //每天的数据
                for (int i = 1; i <= days; i++) {
                    monthDetailInfoList.add(new MonthDetailInfo(i));
                }
                String monitorType = config.get("monitorType");
                monthResult.setMonitorId(config.get("id"));
                monthResult.setMonitorName(monitorName);
                monthResult.setGroupId(config.get("orgId"));
                monthResult.setGroupName(config.get("orgName"));
                monthResult.setVehicleType(cacheManger.getVehicleType(config.get("vehicleType")).getType());
                monthResult.setPlateColor(getPlateColor(config.get("plateColor"), monitorType));
                monthResult.setMonthDetailInfoList(monthDetailInfoList);
                monthPositioningResultHashMap.put(config.get("id"), monthResult);
                monitorIds.append(config.get("id")).append(",");
            }
        }
        return monthPositioningResultHashMap;
    }

    private Set<String> getSearchMonitorIds(List<String> monitorIdList, String search) {
        Set<String> result = new HashSet<>();
        Map<String, Map<String, String>> configMap =
            RedisHelper.batchGetHashMap(RedisKeyEnum.MONITOR_INFO.ofs(monitorIdList)).stream()
                .collect(Collectors.toMap(o -> o.get("id"), Function.identity()));
        for (String minitorId : monitorIdList) {
            Map<String, String> config = configMap.get(minitorId);
            if (config != null) {
                String monitorName = config.get("name");
                if (monitorName != null && monitorName.contains(search)) {
                    result.add(config.get("id"));
                }
            }
        }
        return result;
    }

    private void calculatePercentage(List<GroupPositioningResult> resultList) {
        //计算占比
        for (GroupPositioningResult groupPositioningResult : resultList) {
            //合计定位总数
            int locationTotal = groupPositioningResult.getLocationTotal();
            //合计无效定位数
            int invalidLocations = groupPositioningResult.getInvalidLocations();
            if (locationTotal != 0) {
                // 定位统计有效率
                groupPositioningResult.setLocationEfficiency(
                    Double.parseDouble(ComputingUtils.calProportion(locationTotal - invalidLocations, locationTotal)));
                groupPositioningResult
                    .setLocationEfficiencyStr(formatRadioToStr(groupPositioningResult.getLocationEfficiency()) + "%");
            }
            //车辆总数
            int vehicleNumbers = groupPositioningResult.getVehicleNumbers();
            //无定位车辆数
            int vehicleUnLocation = groupPositioningResult.getVehicleUnLocation();
            if (vehicleNumbers != 0) {
                //无定位率
                groupPositioningResult.setUnLocationRadio(
                    Double.parseDouble(ComputingUtils.calProportion(vehicleUnLocation, vehicleNumbers)));
                groupPositioningResult
                    .setUnLocationRadioStr(formatRadioToStr(groupPositioningResult.getUnLocationRadio()) + "%");
            }
        }
    }

    private String formatRadioToStr(Double number) {
        String radioStr = String.valueOf(number);
        if (radioStr.endsWith("0") && radioStr.length() != 1) {
            radioStr = radioStr.substring(0, radioStr.length() - 1);
            radioStr = radioStr.endsWith(".") ? radioStr.substring(0, radioStr.length() - 1) : radioStr;
        }
        return radioStr;
    }

    /**
     * 组装监控对象ids，维护监控对象id和企业id关系map
     */
    private void buildData(Map<String, String> monitorIdGroupIdMap, StringBuilder monitorIds,
        MonitorPositioningInfo info) {
        monitorIds.append(info.getMonitorId()).append(",");
        monitorIdGroupIdMap.put(info.getMonitorId(), info.getGroupId());
    }

    /**
     * 获取分页最后一条数据
     */
    private int getEndSize(BaseQueryBean param, int allSize) {
        int totalSize = param.getPage().intValue() * param.getLimit().intValue();
        return Math.min(allSize, totalSize);
    }

    private String getPlateColor(String plateColorCode, String monitorType) {
        //0代表监控对象是车
        if ("0".equals(monitorType)) {
            if ("1".equals(plateColorCode)) {
                return "蓝";
            }
            if ("2".equals(plateColorCode)) {
                return "黄";
            }
            if ("3".equals(plateColorCode)) {
                return "黑";
            }
            if ("4".equals(plateColorCode)) {
                return "白";
            }
            return "其他";
        }
        return "--";
    }

    private int getMonthOfDays(String time, String format) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        Date date = simpleDateFormat.parse(time);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    @Override
    public List<GroupPositioningResult> exportGroupList(GroupListQueryParam param) throws Exception {
        RedisKey enterpriseLocationInfoKey =
            HistoryRedisKeyEnum.VEHICLE_LOCATION_STATISTICS.of(SystemHelper.getCurrentUsername());
        List<GroupPositioningResult> resultList =
            RedisHelper.getList(enterpriseLocationInfoKey, GroupPositioningResult.class);
        if (CollectionUtils.isEmpty(resultList)) {
            return new ArrayList<>();
        }
        return resultList;
    }

    @Override
    public AllEnterpriseInfo exportAllGroupPositioning(GroupListQueryParam param) throws Exception {
        AllEnterpriseInfo allEnterpriseInfo = new AllEnterpriseInfo();
        Set<String> groupIdSet = new HashSet<>(Arrays.asList(param.getGroupIds().split(",")));
        List<MonitorPositioningInfo> positioningInfoList = getMonitorPositioningInfos(groupIdSet);
        Map<String, MonitorPositioningInfo> monitorPositioningInfoMap = new ConcurrentHashMap<>();
        StringBuilder monitorIds = new StringBuilder();
        for (MonitorPositioningInfo monitorPositioningInfo : positioningInfoList) {
            monitorIds.append(monitorPositioningInfo.getMonitorId()).append(",");
            monitorPositioningInfoMap.put(monitorPositioningInfo.getMonitorId(), monitorPositioningInfo);
        }
        Map<String, OrganizationLdap> organizationLdapMap = organizationService.getOrgByUuids(groupIdSet);
        //批量导出明细为4个明细报表(定位统计和无定位统计为同一数据源，数据一致用同一线程)
        CountDownLatch queryCountDownLatch = new CountDownLatch(4);
        //1.处理定位统计明细,2.处理无定位统计明细
        taskExecutor.execute(
            () -> setLocationExport(allEnterpriseInfo, monitorPositioningInfoMap, organizationLdapMap, param,
                monitorIds, queryCountDownLatch));
        //2.处理无定位统计明细
        taskExecutor.execute(
            () -> setUnLocationExport(allEnterpriseInfo, monitorPositioningInfoMap, organizationLdapMap, param,
                monitorIds, queryCountDownLatch));
        //3.处理定位中断统计明细
        taskExecutor.execute(
            () -> setEnterpriseInterruptExport(allEnterpriseInfo, monitorPositioningInfoMap, organizationLdapMap, param,
                monitorIds, queryCountDownLatch));
        //4.处理离线位移统计明细
        taskExecutor.execute(
            () -> setOfflineDetailExport(allEnterpriseInfo, monitorPositioningInfoMap, organizationLdapMap, param,
                monitorIds, queryCountDownLatch));
        //等待线程执行完毕
        queryCountDownLatch.await();
        return allEnterpriseInfo;
    }

    private void setUnLocationExport(AllEnterpriseInfo allEnterpriseInfo,
        Map<String, MonitorPositioningInfo> monitorPositioningInfoMap,
        Map<String, OrganizationLdap> organizationLdapMap, GroupListQueryParam param, StringBuilder monitorIds,
        CountDownLatch queryCountDownLatch) {
        try {
            List<MonitorPositioningInfo> unLocationResultList = new ArrayList<>();
            unLocationBuild(param, unLocationResultList, organizationLdapMap, monitorPositioningInfoMap, monitorIds);
            for (int i = 0; i < unLocationResultList.size(); i++) {
                unLocationResultList.get(i).setIndex(i + 1);
            }
            allEnterpriseInfo.setUnLocationResultList(unLocationResultList);
        } catch (Exception e) {
            logger.error("批量导出定位统计明细异常", e);
        } finally {
            queryCountDownLatch.countDown();
        }

    }

    @Override
    public List<MonitorPositioningInfo> exportLocationPositioning(GroupListQueryParam param) throws Exception {
        RedisKey enterpriseLocationInfoKey =
            HistoryRedisKeyEnum.LOCATION_STATISTICS_DETAILS.of(SystemHelper.getCurrentUsername());
        List<MonitorPositioningInfo> resultList =
            RedisHelper.getList(enterpriseLocationInfoKey, MonitorPositioningInfo.class);
        if (CollectionUtils.isEmpty(resultList)) {
            resultList = new ArrayList<>();
        }
        for (int i = 0; i < resultList.size(); i++) {
            resultList.get(i).setIndex(i + 1);
        }
        return resultList;
    }

    @Override
    public List<MonitorPositioningInfo> exportUnLocationPositioning(GroupListQueryParam param) throws Exception {
        RedisKey enterpriseLocationInfoKey =
            HistoryRedisKeyEnum.NO_LOCATION_STATISTICS_DETAILS.of(SystemHelper.getCurrentUsername());
        List<MonitorPositioningInfo> resultList =
            RedisHelper.getList(enterpriseLocationInfoKey, MonitorPositioningInfo.class);
        if (CollectionUtils.isEmpty(resultList)) {
            resultList = new ArrayList<>();
        }
        for (int i = 0; i < resultList.size(); i++) {
            resultList.get(i).setIndex(i + 1);
        }
        return resultList;
    }

    @Override
    public List<MonitorInterruptDetailInfo> exportInterruptInfo(GroupListQueryParam param) throws Exception {
        RedisKey enterpriseLocationInfoKey =
            HistoryRedisKeyEnum.LOCATION_INTERRUPT_STATISTICS_DETAILS.of(SystemHelper.getCurrentUsername());
        List<MonitorInterruptDetailInfo> resultList =
            RedisHelper.getList(enterpriseLocationInfoKey, MonitorInterruptDetailInfo.class);
        if (CollectionUtils.isEmpty(resultList)) {
            resultList = new ArrayList<>();
        }
        for (int i = 0; i < resultList.size(); i++) {
            resultList.get(i).setIndex(i + 1);
        }
        return resultList;
    }

    @Override
    public List<MonitorOfflineDetailInfo> exportOfflineInfo(GroupListQueryParam param) throws Exception {
        RedisKey enterpriseLocationInfoKey =
            HistoryRedisKeyEnum.OFFLINE_DISPLACEMENT_STATISTICS_DETAILS.of(SystemHelper.getCurrentUsername());
        List<MonitorOfflineDetailInfo> resultList =
            RedisHelper.getList(enterpriseLocationInfoKey, MonitorOfflineDetailInfo.class);
        if (CollectionUtils.isEmpty(resultList)) {
            resultList = new ArrayList<>();
        }
        for (int i = 0; i < resultList.size(); i++) {
            resultList.get(i).setIndex(i + 1);
        }
        return resultList;
    }

    @Override
    public List<MonthPositioningResult> exportMonthPositioningList(MonthListQueryParam param) throws Exception {
        RedisKey monthPositioningKey =
            HistoryRedisKeyEnum.MONTH_LOCATION_STATISTICS.of(SystemHelper.getCurrentUsername());
        List<MonthPositioningResult> resultList =
            RedisHelper.getList(monthPositioningKey, MonthPositioningResult.class);
        if (CollectionUtils.isEmpty(resultList)) {
            resultList = new ArrayList<>();
        }
        for (int i = 0; i < resultList.size(); i++) {
            resultList.get(i).setIndex(i + 1);
        }
        return resultList;
    }

    @Override
    public List<ExceptionPositioningResult> exportExceptionList(ExceptionListQueryParam param) throws Exception {
        RedisKey exceptionPositioningKey =
            HistoryRedisKeyEnum.ANOMALY_LOCATION_STATISTICS.of(SystemHelper.getCurrentUsername());
        List<ExceptionPositioningResult> resultList =
            RedisHelper.getList(exceptionPositioningKey, ExceptionPositioningResult.class);
        if (CollectionUtils.isEmpty(resultList)) {
            resultList = new ArrayList<>();
        }
        for (int i = 0; i < resultList.size(); i++) {
            resultList.get(i).setIndex(i + 1);
        }
        return resultList;
    }

    @Override
    public List<ExceptionInfoResult> exportExceptionInfo(ExceptionInfoQueryParam param) throws Exception {
        RedisKey enterpriseLocationInfoKey =
            HistoryRedisKeyEnum.EXCEPTION_LOCATION_DETAILS.of(SystemHelper.getCurrentUsername());
        List<ExceptionInfoResult> resultList =
            RedisHelper.getList(enterpriseLocationInfoKey, ExceptionInfoResult.class);
        if (CollectionUtils.isEmpty(resultList)) {
            resultList = new ArrayList<>();
        }
        for (int i = 0; i < resultList.size(); i++) {
            resultList.get(i).setIndex(i + 1);
        }
        return resultList;
    }

    private void setOfflineDetailExport(AllEnterpriseInfo allEnterpriseInfo,
        Map<String, MonitorPositioningInfo> monitorPositioningInfoMap,
        Map<String, OrganizationLdap> organizationLdapMap, GroupListQueryParam param, StringBuilder monitorIds,
        CountDownLatch queryCountDownLatch) {
        try {
            List<MonitorOfflineDetailInfo> offlineDetailResultList = new ArrayList<>();
            offlineBuild(param, offlineDetailResultList, organizationLdapMap, monitorPositioningInfoMap, monitorIds);
            for (int i = 0; i < offlineDetailResultList.size(); i++) {
                offlineDetailResultList.get(i).setIndex(i + 1);
            }
            allEnterpriseInfo.setOfflineDetailResultList(offlineDetailResultList);
        } catch (Exception e) {
            logger.error("批量导出定位统计明细异常", e);
        } finally {
            queryCountDownLatch.countDown();
        }
    }

    private void setEnterpriseInterruptExport(AllEnterpriseInfo allEnterpriseInfo,
        Map<String, MonitorPositioningInfo> monitorPositioningInfoMap,
        Map<String, OrganizationLdap> organizationLdapMap, GroupListQueryParam param, StringBuilder monitorIds,
        CountDownLatch queryCountDownLatch) {
        try {
            List<MonitorInterruptDetailInfo> enterpriseInterruptList = new ArrayList<>();
            interruptInfoBuild(param, enterpriseInterruptList, organizationLdapMap, monitorPositioningInfoMap,
                monitorIds);
            for (int i = 0; i < enterpriseInterruptList.size(); i++) {
                enterpriseInterruptList.get(i).setIndex(i + 1);
            }
            allEnterpriseInfo.setEnterpriseInterruptList(enterpriseInterruptList);
        } catch (Exception e) {
            logger.error("批量导出定位明细数据异常", e);
        } finally {
            queryCountDownLatch.countDown();
        }
    }

    private void setLocationExport(AllEnterpriseInfo allEnterpriseInfo,
        Map<String, MonitorPositioningInfo> monitorPositioningInfoMap,
        Map<String, OrganizationLdap> organizationLdapMap, GroupListQueryParam param, StringBuilder monitorIds,
        CountDownLatch queryCountDownLatch) {
        try {
            List<MonitorPositioningInfo> locationResultList = new ArrayList<>();
            Map<String, String> queryParam = getEnterpriseListQueryParam(param, monitorIds);
            JSONObject result = JSON.parseObject(HttpClientUtil.send(GROUP_STATISTICS_URL, queryParam));
            if (result != null) {
                buildEnterpriseLocation(locationResultList, organizationLdapMap, monitorPositioningInfoMap, result);
            }
            for (int i = 0; i < locationResultList.size(); i++) {
                locationResultList.get(i).setIndex(i + 1);
            }
            allEnterpriseInfo.setLocationResultList(locationResultList);
        } catch (Exception e) {
            logger.error("批量导出定位统计明细异常", e);
        } finally {
            queryCountDownLatch.countDown();
        }
    }
}

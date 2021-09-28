package com.cb.platform.service.impl;

import com.cb.platform.domain.GroupSpotCheckVehicleNumberCont;
import com.cb.platform.domain.UserOnline;
import com.cb.platform.domain.UserSpotCheckNumberAndPercentageInfo;
import com.cb.platform.domain.VehicleSpotCheckCont;
import com.cb.platform.domain.VehicleSpotCheckInfo;
import com.cb.platform.domain.VehicleSpotCheckNumberCountInfo;
import com.cb.platform.repository.mysqlDao.OnlineTimeDao;
import com.cb.platform.repository.mysqlDao.SpotCheckReportDao;
import com.cb.platform.service.SpotCheckReportService;
import com.google.common.collect.Lists;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.core.TypeCacheManger;
import com.zw.platform.basic.domain.VehicleDO;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.GroupDTO;
import com.zw.platform.basic.dto.UserDTO;
import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.platform.basic.repository.GroupDao;
import com.zw.platform.basic.repository.NewVehicleDao;
import com.zw.platform.basic.service.GroupService;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.enums.PlateColor;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.service.oil.PositionalService;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.ComputingUtils;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.platform.util.excel.ExportExcel;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author penghj
 * @version 1.0
 * @date 2019/2/21 10:31
 */
@Service
public class SpotCheckReportServiceImpl implements SpotCheckReportService {

    @Autowired
    private SpotCheckReportDao spotCheckReportDao;

    @Autowired
    private PositionalService positionalService;

    @Autowired
    private OnlineTimeDao onlineTimeDao;

    @Autowired
    private UserService userService;

    @Autowired
    private NewVehicleDao newVehicleDao;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private GroupDao groupDao;

    @Autowired
    private GroupService groupService;

    private final TypeCacheManger cacheManger = TypeCacheManger.getInstance();

    @Override
    public JsonResultBean getVehicleSpotCheckDetailList(String vehicleIds, String startTime, String endTime)
        throws Exception {
        RedisKey redisKey =
            HistoryRedisKeyEnum.VEHICLE_SPOT_CHECK_DETAIL_DATA_KEY.of(SystemHelper.getCurrentUsername());
        if (RedisHelper.isContainsKey(redisKey)) {
            RedisHelper.delete(redisKey);
        }
        List<VehicleSpotCheckInfo> result = new ArrayList<>();
        if (StringUtils.isNotBlank(vehicleIds) && StringUtils.isNotBlank(startTime) && StringUtils
            .isNotBlank(endTime)) {
            List<String> vehicleIdList = Arrays.asList(vehicleIds.split(","));
            result = spotCheckReportDao.getVehicleSpotCheckDetailList(vehicleIdList, startTime, endTime);
            if (CollectionUtils.isNotEmpty(result)) {
                List<String> vidList =
                    result.stream().map(VehicleSpotCheckInfo::getVehicleId).distinct().collect(Collectors.toList());
                Map<String, VehicleDTO> vehicleMap = VehicleUtil.batchGetVehicleInfosFromRedis(vidList,
                        Lists.newArrayList("name", "vehicleType", "plateColor", "orgName"));
                for (VehicleSpotCheckInfo info : result) {
                    if (info.getLocationTime() != null) {
                        info.setLocationTimeStr(DateUtil.getDateToString(info.getLocationTime(), null));
                    }
                    info.setSpotCheckTimeStr(DateUtil.getDateToString(info.getSpotCheckTime(), null));
                    VehicleDTO vehicleInfo = vehicleMap.get(info.getVehicleId());
                    if (Objects.nonNull(vehicleInfo)) {
                        info.setPlateNumber(vehicleInfo.getName());
                        String plateColor = PlateColor.getNameOrBlankByCode(vehicleInfo.getPlateColor());
                        if (plateColor != null) {
                            info.setPlateColor(plateColor);
                        }
                        String vehicleType = cacheManger.getVehicleType(vehicleInfo.getVehicleType()).getType();
                        info.setVehicleType(vehicleType);
                        String groupName = vehicleInfo.getOrgName();
                        info.setGroupName(groupName);
                    }
                    String speed = info.getSpeed();
                    if (StringUtils.isNotBlank(speed)) {
                        double speedDouble = Double.parseDouble(speed) * 10.0;
                        int speedInt = (int) speedDouble;
                        info.setSpeed(StringUtil.cutString(String.valueOf(speedInt * 1.0 / 10.0)));
                    }
                }
            }
        }
        if (CollectionUtils.isNotEmpty(result)) {
            RedisHelper.addToList(redisKey, result);
            RedisHelper.expireKey(redisKey, 60 * 60);
        }
        return new JsonResultBean(result);
    }

    @Override
    public void exportVehicleSpotCheckDetail(HttpServletResponse response, String simpleQueryParam) throws Exception {
        ExportExcel export = new ExportExcel(null, VehicleSpotCheckInfo.class, 1, null);
        RedisKey redisKey =
            HistoryRedisKeyEnum.VEHICLE_SPOT_CHECK_DETAIL_DATA_KEY.of(SystemHelper.getCurrentUsername());
        List<VehicleSpotCheckInfo> allExportList = RedisHelper.getList(redisKey, VehicleSpotCheckInfo.class);
        List<VehicleSpotCheckInfo> exportList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(allExportList)) {
            exportList.addAll(allExportList);
            //模糊搜索需要过滤 如果没有搜索则全部导出
            if (StringUtils.isNotBlank(simpleQueryParam)) {
                exportList.clear();
                List<VehicleSpotCheckInfo> filterExportList =
                    allExportList.stream().filter(info -> info.getPlateNumber().contains(simpleQueryParam))
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(filterExportList)) {
                    exportList.addAll(filterExportList);
                }
            }
            for (VehicleSpotCheckInfo info : exportList) {
                //设置抽查内容
                setSpotCheckContent(info);
                //逆地址解析
                String address = positionalService.getAddress(info.getLongtitude(), info.getLatitude());
                info.setAddress(StringUtils.isNotBlank(address) ? address : "未定位");
            }
        }
        export.setDataList(exportList);
        OutputStream out = response.getOutputStream();
        export.write(out);
        out.close();
    }

    @Override
    public JsonResultBean getUserSpotCheckNumberAndPercentageList(String userIds, String startTime, String endTime)
        throws Exception {
        RedisKey redisKey =
            HistoryRedisKeyEnum.USER_SPOT_CHECK_NUMBER_AND_PERCENTAGE_DATA_KEY.of(SystemHelper.getCurrentUsername());
        if (RedisHelper.isContainsKey(redisKey)) {
            RedisHelper.delete(redisKey);
        }
        List<UserSpotCheckNumberAndPercentageInfo> result = new ArrayList<>();
        if (StringUtils.isNotBlank(userIds) && StringUtils.isNotBlank(startTime) && StringUtils.isNotBlank(endTime)) {
            List<String> userIdList = Arrays.asList(userIds.split(","));
            //查询时间段内的用户在岗时段
            List<UserOnline> userOnlineTimeRangeList =
                onlineTimeDao.getUserOnlineTimeRange(userIdList, startTime, endTime);
            if (CollectionUtils.isNotEmpty(userOnlineTimeRangeList)) {
                List<String> userGroupIdList = userOnlineTimeRangeList.stream().map(UserOnline::getGroupId).distinct()
                    .collect(Collectors.toList());
                //用户id和用户名称、 用户企业id和企业名称的对应关系map
                Map<String, Map<String, String>> userNameAndGroupNameMap = getUserNameAndGroupNameMap(
                    userOnlineTimeRangeList.stream().map(UserOnline::getUserId).distinct().collect(Collectors.toList()),
                    userGroupIdList);
                Map<String, Integer> vehicleCountMap = getVehicleCountMapByUserGroupId(userGroupIdList);
                //查询时间段内的抽查明细
                List<VehicleSpotCheckInfo> spotCheckDetailList = spotCheckReportDao
                    .getSpotCheckDetailListByUserIds(new ArrayList<>(userNameAndGroupNameMap.get("userName").values()),
                        startTime, endTime);
                Date queryStartTime = DateUtil.getStringToDate(startTime, null);
                Date queryEndTime = DateUtil.getStringToDate(endTime, null);
                if (queryStartTime != null && queryEndTime != null) {
                    List<String> vehicleIdList = spotCheckDetailList.stream().map(VehicleSpotCheckInfo::getVehicleId)
                        .collect(Collectors.toList());
                    Map<String, BindDTO> vehicleInfoMap = VehicleUtil.batchGetBindInfosByRedis(vehicleIdList);
                    for (UserOnline userOnlineInfo : userOnlineTimeRangeList) {
                        String userId = userOnlineInfo.getUserId();
                        String userName = userNameAndGroupNameMap.get("userName").get(userId);
                        String groupId = userOnlineInfo.getGroupId();
                        String groupName = userNameAndGroupNameMap.get("groupName").get(groupId);
                        //车辆总数
                        Integer vehicleCount = vehicleCountMap.get(groupId);
                        if (StringUtils.isNotBlank(userName)) {
                            //上线时间
                            Date onlineTime = userOnlineInfo.getOnlineTime();
                            //下线时间
                            Date offlineTime = userOnlineInfo.getOfflineTime();
                            if (onlineTime == null || onlineTime.getTime() < queryStartTime.getTime()) {
                                onlineTime = queryStartTime;
                            }
                            if (offlineTime == null || offlineTime.getTime() > queryEndTime.getTime()) {
                                offlineTime = queryEndTime;
                            }
                            //在岗时段 日期
                            String onDutyDateStr = DateUtil.getDateToString(onlineTime, "yyyy-MM-dd");
                            //判断在岗时间是否跨天 如果跨天需拆分成多条记录
                            if (judgeOnDutyIfExceedOneDay(result, spotCheckDetailList, userName, groupName,
                                vehicleCount, onlineTime, offlineTime, queryStartTime, queryEndTime, vehicleInfoMap,
                                groupId)) {
                                continue;
                            }
                            UserSpotCheckNumberAndPercentageInfo info = new UserSpotCheckNumberAndPercentageInfo();
                            info.setUserName(userName);
                            info.setOnDutyTime(
                                onDutyDateStr + " " + DateUtil.getDateToString(onlineTime, "HH:mm:ss") + "-" + DateUtil
                                    .getDateToString(offlineTime, "HH:mm:ss"));
                            info.setStartTimeL(onlineTime.getTime());
                            info.setUserGroupName(groupName);
                            info.setVehicleCount(vehicleCount != null ? vehicleCount : 0);
                            //设置抽查次数和百分比
                            setSpotCheckNumAndPercentage(spotCheckDetailList, info, onlineTime, offlineTime,
                                vehicleCount, vehicleInfoMap, groupId);
                            result.add(info);
                        }
                    }
                }
            }
        }
        if (CollectionUtils.isNotEmpty(result)) {
            result = result.stream()
                .sorted(Comparator.comparingLong(UserSpotCheckNumberAndPercentageInfo::getStartTimeL).reversed())
                .collect(Collectors.toList());
            RedisHelper.addToList(redisKey, result);
            RedisHelper.expireKey(redisKey, 60 * 60);
        }
        return new JsonResultBean(result);
    }

    /**
     * 判断在岗时间是否跨天
     * @param result
     * @param spotCheckDetailList
     * @param userName            用户名
     * @param groupName           企业名称
     * @param vehicleCount        车辆树
     * @param onlineTime          上线时间
     * @param offlineTime         下线时间
     * @param queryStartTime
     * @param queryEndTime
     * @param vehicleInfoMap
     * @param groupId
     * @throws ParseException
     */
    private boolean judgeOnDutyIfExceedOneDay(List<UserSpotCheckNumberAndPercentageInfo> result,
        List<VehicleSpotCheckInfo> spotCheckDetailList, String userName, String groupName, Integer vehicleCount,
        Date onlineTime, Date offlineTime, Date queryStartTime, Date queryEndTime, Map<String, BindDTO> vehicleInfoMap,
        String groupId) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date onlineDate = sdf.parse(sdf.format(onlineTime));
        Date offlineDate = sdf.parse(sdf.format(offlineTime));
        long differDays = (offlineDate.getTime() - onlineDate.getTime()) / (1000 * 3600 * 24);
        if (differDays > 0) {
            Calendar c = Calendar.getInstance();
            for (int i = 0; i < differDays + 1; i++) {
                Date uptime;
                Date downtime;
                c.setTime(onlineDate);
                c.add(Calendar.DAY_OF_MONTH, i);
                uptime = DateUtil.getStringToDate(sdf.format(c.getTime()) + " 00:00:00", null);
                downtime = DateUtil.getStringToDate(sdf.format(c.getTime()) + " 23:59:59", null);
                c.clear();
                if (i == 0) {
                    uptime = onlineTime;
                } else if (i == differDays) {
                    downtime = offlineTime;
                }
                //是否在查询时间内;
                boolean ifInQueryTime =
                    (uptime.getTime() <= queryEndTime.getTime() && uptime.getTime() >= queryStartTime.getTime()) || (
                        downtime.getTime() <= queryEndTime.getTime() && downtime.getTime() >= queryStartTime.getTime());
                //是否包含查询时间
                boolean ifContainQueryTime =
                    uptime.getTime() <= queryStartTime.getTime() && downtime.getTime() >= queryEndTime.getTime();
                if (ifInQueryTime || ifContainQueryTime) {
                    UserSpotCheckNumberAndPercentageInfo info = new UserSpotCheckNumberAndPercentageInfo();
                    info.setUserName(userName);
                    info.setOnDutyTime(
                        sdf.format(uptime) + " " + DateUtil.getDateToString(uptime, "HH:mm:ss") + "-" + DateUtil
                            .getDateToString(downtime, "HH:mm:ss"));
                    info.setStartTimeL(uptime.getTime());
                    info.setUserGroupName(groupName);
                    info.setVehicleCount(vehicleCount != null ? vehicleCount : 0);
                    //设置抽查次数和百分比
                    setSpotCheckNumAndPercentage(spotCheckDetailList, info, uptime, downtime, vehicleCount,
                        vehicleInfoMap, groupId);
                    result.add(info);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void exportUserSpotCheckNumberAndPercentage(HttpServletResponse response, String simpleQueryParam)
        throws Exception {
        ExportExcel export = new ExportExcel(null, UserSpotCheckNumberAndPercentageInfo.class, 1, null);
        RedisKey key =
            HistoryRedisKeyEnum.USER_SPOT_CHECK_NUMBER_AND_PERCENTAGE_DATA_KEY.of(SystemHelper.getCurrentUsername());
        List<UserSpotCheckNumberAndPercentageInfo> allExportList =
            RedisHelper.getList(key, UserSpotCheckNumberAndPercentageInfo.class);
        List<UserSpotCheckNumberAndPercentageInfo> exportList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(allExportList)) {
            exportList.addAll(allExportList);
            //模糊搜索需要过滤 如果没有搜索则全部导出
            if (StringUtils.isNotBlank(simpleQueryParam)) {
                exportList.clear();
                List<UserSpotCheckNumberAndPercentageInfo> filterExportList =
                    allExportList.stream().filter(info -> info.getUserName().contains(simpleQueryParam))
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(filterExportList)) {
                    exportList.addAll(filterExportList);
                }
            }
        }
        export.shiftRows(0, 0, 1);
        export.setRowNum(export.getRowNum() + 1);
        Map<String, CellStyle> styles = export.getStyles();
        CellStyle style = styles.get("header");
        //添加首行数据
        Row row = export.addRow(0);
        row.createCell(0).setCellValue("用户");
        row.getCell(0).setCellStyle(style);
        row.createCell(1).setCellValue("在岗时段");
        row.getCell(1).setCellStyle(style);
        row.createCell(2).setCellValue("所属道路运输企业");
        row.getCell(2).setCellStyle(style);
        row.createCell(3).setCellValue("抽查内容(次数)");
        row.getCell(3).setCellStyle(style);
        row.createCell(7).setCellValue("车辆总数");
        row.getCell(7).setCellStyle(style);
        row.createCell(8).setCellValue("合计");
        row.getCell(8).setCellStyle(style);
        row.createCell(9).setCellValue("抽查内容(百分比)");
        row.getCell(9).setCellStyle(style);
        row.createCell(13).setCellValue("合计");
        row.getCell(13).setCellStyle(style);
        //合并单元格
        Sheet sheet = export.getSheet();
        sheet.addMergedRegion(new CellRangeAddress(0, 1, 0, 0));
        sheet.addMergedRegion(new CellRangeAddress(0, 1, 1, 1));
        sheet.addMergedRegion(new CellRangeAddress(0, 1, 2, 2));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 3, 6));
        sheet.addMergedRegion(new CellRangeAddress(0, 1, 7, 7));
        sheet.addMergedRegion(new CellRangeAddress(0, 1, 8, 8));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 9, 12));
        sheet.addMergedRegion(new CellRangeAddress(0, 1, 13, 13));
        export.setDataList(exportList);
        OutputStream out = response.getOutputStream();
        export.write(out);
        out.close();
    }

    @Override
    public JsonResultBean getVehicleSpotCheckNumberCountList(String vehicleIds, String startTime, String endTime)
        throws Exception {
        RedisKey key =
            HistoryRedisKeyEnum.VEHICLE_SPOT_CHECK_NUMBER_COUNT_DATA_KEY.of(SystemHelper.getCurrentUsername());
        if (RedisHelper.isContainsKey(key)) {
            RedisHelper.delete(key);
        }
        List<VehicleSpotCheckNumberCountInfo> result = new ArrayList<>();
        if (StringUtils.isNotBlank(vehicleIds) && StringUtils.isNotBlank(startTime) && StringUtils
            .isNotBlank(endTime)) {
            List<String> vehicleIdList = Arrays.asList(vehicleIds.split(","));
            List<VehicleSpotCheckInfo> vehicleSpotCheckDetailList =
                spotCheckReportDao.getVehicleSpotCheckDetailList(vehicleIdList, startTime, endTime);
            List<String> queryTimeList = installQueryTime(startTime, endTime);
            Map<String, Map<String, String>> configMap =
                RedisHelper.batchGetHashMap(RedisKeyEnum.MONITOR_INFO.ofs(vehicleIdList)).stream()
                    .collect(Collectors.toMap(o -> o.get("id"), Function.identity()));
            for (String vehicleId : vehicleIdList) {
                Map<String, String> config = configMap.get(vehicleId);
                if (config != null) {
                    VehicleSpotCheckNumberCountInfo info = new VehicleSpotCheckNumberCountInfo();
                    String groupName = config.get("orgName");
                    info.setGroupName(groupName);
                    String vehicleType = cacheManger.getVehicleType(config.get("vehicleType")).getType();
                    info.setVehicleType(vehicleType);
                    String carLicense = config.get("name");
                    info.setPlateNumber(carLicense);
                    String plateColor = config.get("plateColor");
                    if (StringUtils.isNotBlank(plateColor)) {
                        info.setPlateColor(VehicleUtil.getPlateColorStr(plateColor));
                    }
                    //设置车辆抽查次数
                    setVehicleSpotCheckNumber(info, vehicleSpotCheckDetailList.stream()
                            .filter(
                                checkInfo -> vehicleId.equals(checkInfo.getVehicleId())).collect(Collectors.toList()),
                                queryTimeList);
                    info.setTotalNumByAdd();
                    result.add(info);
                }
            }
        }
        if (CollectionUtils.isNotEmpty(result)) {
            RedisHelper.addToList(key, result);
            RedisHelper.expireKey(key, 60 * 60);
        }
        return new JsonResultBean(result);
    }

    @Override
    public void exportVehicleSpotCheckNumberCountList(HttpServletResponse response, String simpleQueryParam)
        throws Exception {
        ExportExcel export = new ExportExcel(null, VehicleSpotCheckNumberCountInfo.class, 1, null);
        RedisKey key =
            HistoryRedisKeyEnum.VEHICLE_SPOT_CHECK_NUMBER_COUNT_DATA_KEY.of(SystemHelper.getCurrentUsername());
        List<VehicleSpotCheckNumberCountInfo> allExportList =
            RedisHelper.getList(key, VehicleSpotCheckNumberCountInfo.class);
        List<VehicleSpotCheckNumberCountInfo> exportList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(allExportList)) {
            exportList.addAll(allExportList);
            //模糊搜索需要过滤 如果没有搜索则全部导出
            if (StringUtils.isNotBlank(simpleQueryParam)) {
                exportList.clear();
                List<VehicleSpotCheckNumberCountInfo> filterExportList =
                    allExportList.stream().filter(info -> info.getPlateNumber().contains(simpleQueryParam))
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(filterExportList)) {
                    exportList.addAll(filterExportList);
                }
            }
        }
        export.shiftRows(0, 0, 1);
        export.setRowNum(export.getRowNum() + 1);
        Map<String, CellStyle> styles = export.getStyles();
        CellStyle style = styles.get("header");
        //添加首行数据
        Row row = export.addRow(0);
        row.createCell(0).setCellValue("车牌号");
        row.getCell(0).setCellStyle(style);
        row.createCell(1).setCellValue("车牌颜色");
        row.getCell(1).setCellStyle(style);
        row.createCell(2).setCellValue("车辆类型");
        row.getCell(2).setCellStyle(style);
        row.createCell(3).setCellValue("所属道路运输企业");
        row.getCell(3).setCellStyle(style);
        row.createCell(4).setCellValue("抽查内容(次数)");
        row.getCell(4).setCellStyle(style);
        row.createCell(8).setCellValue("合计");
        row.getCell(8).setCellStyle(style);
        //合并单元格
        Sheet sheet = export.getSheet();
        sheet.addMergedRegion(new CellRangeAddress(0, 1, 0, 0));
        sheet.addMergedRegion(new CellRangeAddress(0, 1, 1, 1));
        sheet.addMergedRegion(new CellRangeAddress(0, 1, 2, 2));
        sheet.addMergedRegion(new CellRangeAddress(0, 1, 3, 3));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 4, 7));
        sheet.addMergedRegion(new CellRangeAddress(0, 1, 8, 8));
        export.setDataList(exportList);
        OutputStream out = response.getOutputStream();
        export.write(out);
        out.close();
    }

    /**
     * 设置抽查次数和百分比
     * @param spotCheckDetailList
     * @param info
     * @param onlineTime
     * @param offlineTime
     * @param vehicleCount
     * @param vehicleInfoMap
     * @param groupId
     */
    private void setSpotCheckNumAndPercentage(List<VehicleSpotCheckInfo> spotCheckDetailList,
        UserSpotCheckNumberAndPercentageInfo info, Date onlineTime, Date offlineTime, Integer vehicleCount,
        Map<String, BindDTO> vehicleInfoMap, String groupId) {
        Set<String> checkPositionInfoVehicleIds = new HashSet<>();
        Set<String> checkHistoricalTrackVehicleIds = new HashSet<>();
        Set<String> checkVideoVehicleIds = new HashSet<>();
        Set<String> violationHandlingVehicleIds = new HashSet<>();
        if (CollectionUtils.isNotEmpty(spotCheckDetailList)) {
            String userName = info.getUserName();
            for (VehicleSpotCheckInfo spotCheckInfo : spotCheckDetailList) {
                long spotCheckTimeL = spotCheckInfo.getSpotCheckTime().getTime();
                String vehicleId = spotCheckInfo.getVehicleId();
                BindDTO bindInfo = vehicleInfoMap.get(vehicleId);
                if (bindInfo == null) {
                    continue;
                }
                if (!Objects.equals(groupId, bindInfo.getOrgId())) {
                    continue;
                }
                if (!userName.equals(spotCheckInfo.getSpotCheckUser())) {
                    continue;
                }
                Integer spotCheckContent = spotCheckInfo.getSpotCheckContent();
                if (spotCheckTimeL < offlineTime.getTime() && spotCheckTimeL >= onlineTime.getTime()) {
                    if (spotCheckContent == 0) {
                        checkPositionInfoVehicleIds.add(vehicleId);
                    } else if (spotCheckContent == 1) {
                        checkHistoricalTrackVehicleIds.add(vehicleId);
                    } else if (spotCheckContent == 2) {
                        checkVideoVehicleIds.add(vehicleId);
                    } else if (spotCheckContent == 3) {
                        violationHandlingVehicleIds.add(vehicleId);
                    }
                }
            }
        }
        //查看定位信息(次数)
        int checkPositionInfoNum = checkPositionInfoVehicleIds.size();
        info.setCheckPositionInfoNum(checkPositionInfoNum);
        //查看历史轨迹(次数)
        int checkHistoricalTrackNum = checkHistoricalTrackVehicleIds.size();
        info.setCheckHistoricalTrackNum(checkHistoricalTrackNum);
        //查看视频(次数)
        int checkVideoNum = checkVideoVehicleIds.size();
        info.setCheckVideoNum(checkVideoNum);
        int violationHandlingNum = violationHandlingVehicleIds.size();
        //违章处理(次数)
        info.setViolationHandlingNum(violationHandlingNum);
        //合计(次数)
        int totalNum = checkPositionInfoNum + checkHistoricalTrackNum + checkVideoNum + violationHandlingNum;
        info.setTotalNum(totalNum);
        if (vehicleCount != null && vehicleCount != 0) {
            int checkPositionInfoPercentage =
                BigDecimal.valueOf(checkPositionInfoNum * 10000.0 / vehicleCount * 1.0).intValue();
            //查看定位信息(百分比)
            info.setCheckPositionInfoPercentage(
                StringUtil.cutString(String.valueOf(checkPositionInfoPercentage * 1.0 / 100.0)) + "%");
            int checkHistoricalTrackPercentage =
                BigDecimal.valueOf(checkHistoricalTrackNum * 10000.0 / vehicleCount * 1.0).intValue();
            //查看历史轨迹(百分比)
            info.setCheckHistoricalTrackPercentage(
                StringUtil.cutString(String.valueOf(checkHistoricalTrackPercentage * 1.0 / 100.0)) + "%");
            int checkVideoPercentage = BigDecimal.valueOf(checkVideoNum * 10000.0 / vehicleCount * 1.0).intValue();
            //查看视频(百分比)
            info.setCheckVideoPercentage(
                StringUtil.cutString(String.valueOf(checkVideoPercentage * 1.0 / 100.0)) + "%");
            int violationHandlingPercentage =
                BigDecimal.valueOf(violationHandlingNum * 10000.0 / vehicleCount * 1.0).intValue();
            //违章处理(百分比)
            info.setViolationHandlingPercentage(
                StringUtil.cutString(String.valueOf(violationHandlingPercentage * 1.0 / 100.0)) + "%");
            //合计(百分比)
            int totalPercentage = BigDecimal.valueOf(totalNum * 10000.0 / vehicleCount * 1.0).intValue();
            info.setTotalPercentage(StringUtil.cutString(String.valueOf(totalPercentage * 1.0 / 100.0)) + "%");
        }
    }

    /**
     * 获得用户id和用户名称、 用户企业id和企业名称的对应关系map
     * @param userIdList
     * @param userGroupIdList
     * @return
     */
    private Map<String, Map<String, String>> getUserNameAndGroupNameMap(List<String> userIdList,
        List<String> userGroupIdList) {
        Map<String, Map<String, String>> userNameAndGroupNameMap = new HashMap<>(16);
        List<UserDTO> userListByUuids = userService.getUserListByUuids(userIdList);
        Map<String, String> userNameMap =
            userListByUuids.stream().collect(Collectors.toMap(UserDTO::getUuid, UserDTO::getUsername));
        userNameAndGroupNameMap.put("userName", userNameMap);
        Set<String> orgIdList = new HashSet<>(userGroupIdList);
        Map<String, String> groupNameMap = organizationService.getOrgByUuids(orgIdList).values().stream()
            .collect(Collectors.toMap(OrganizationLdap::getUuid, OrganizationLdap::getName));
        userNameAndGroupNameMap.put("groupName", groupNameMap);
        return userNameAndGroupNameMap;
    }

    /**
     * 获得用户所属企业下的车辆数量
     * @param userGroupIdList 用户所属企业
     * @return
     */
    private Map<String, Integer> getVehicleCountMapByUserGroupId(List<String> userGroupIdList) {
        Map<String, Integer> vehicleCountMap = new HashMap<>(16);
        Set<String> currentUserGroupIds = userService.getCurrentUserGroupIds();
        if (CollectionUtils.isNotEmpty(currentUserGroupIds)) {
            for (String orgId : userGroupIdList) {
                //当前企业及下级企业id
                List<String> orgChildByOrgUuid = organizationService.getChildOrgIdByUuid(orgId);
                Integer groupVehicleCount = CollectionUtils.isEmpty(orgChildByOrgUuid) ? null :
                    groupDao.getGroupVehicleCount(orgChildByOrgUuid, currentUserGroupIds, orgId);
                vehicleCountMap.put(orgId, groupVehicleCount != null ? groupVehicleCount : 0);
            }
        }
        return vehicleCountMap;
    }

    /**
     * 设置抽查内容
     * @param info
     */
    private void setSpotCheckContent(VehicleSpotCheckInfo info) {
        if (info != null) {
            String spotCheckContentStr = null;
            switch (info.getSpotCheckContent()) {
                case 0:
                    spotCheckContentStr = "查看定位信息";
                    break;
                case 1:
                    spotCheckContentStr = "查看历史轨迹";
                    break;
                case 2:
                    spotCheckContentStr = "查看视频";
                    break;
                case 3:
                    spotCheckContentStr = "违章处理";
                    break;
                default:
                    break;
            }
            info.setSpotCheckContentStr(spotCheckContentStr);
        }
    }

    /**
     * 组装过滤时间
     * @param startTime
     * @param endTime
     * @return
     */
    private List<String> installQueryTime(String startTime, String endTime) {
        List<String> queryTimeList = new ArrayList<>();
        Date startDate = DateUtil.getStringToDate(startTime, "yyyy-MM-dd");
        Date endDate = DateUtil.getStringToDate(endTime, "yyyy-MM-dd");
        if (startDate != null && endDate != null) {
            if (startDate.getTime() == endDate.getTime()) {
                queryTimeList.add(startTime + "," + endTime);
            } else {
                Calendar c = Calendar.getInstance();
                //相差天数(如果相差一天以上就要分成两段来过滤抽查明细)
                long differDays = (endDate.getTime() - startDate.getTime()) / (1000 * 3600 * 24);
                for (int i = 0; i < differDays + 1; i++) {
                    String queryStartTime;
                    String queryEndTime;
                    c.setTime(startDate);
                    c.add(Calendar.DAY_OF_MONTH, i);
                    queryStartTime = DateUtil.getDateToString(c.getTime(), "yyyy-MM-dd") + " 00:00:00";
                    queryEndTime = DateUtil.getDateToString(c.getTime(), "yyyy-MM-dd") + " 23:59:59";
                    if (i == 0) {
                        queryStartTime = startTime;
                    } else if (i == differDays) {
                        queryEndTime = endTime;
                    }
                    queryTimeList.add(queryStartTime + "," + queryEndTime);
                    c.clear();
                }
            }
        }
        return queryTimeList;
    }

    /**
     * 设置车辆抽查次数
     * @param info
     * @param vehicleSpotCheckDetailList
     * @param queryTimeList
     */
    private void setVehicleSpotCheckNumber(VehicleSpotCheckNumberCountInfo info,
        List<VehicleSpotCheckInfo> vehicleSpotCheckDetailList, List<String> queryTimeList) {
        for (String queryTime : queryTimeList) {
            String[] queryTimeArr = queryTime.split(",");
            Long queryStartTimeL = DateUtil.getStringToDate(queryTimeArr[0], null).getTime();
            Long queryEndTimeL = DateUtil.getStringToDate(queryTimeArr[1], null).getTime();
            Integer checkPositionInfoNum = 0;
            Integer checkHistoricalTrackNum = 0;
            Integer checkVideoNum = 0;
            Integer violationHandlingNum = 0;
            for (VehicleSpotCheckInfo checkInfo : vehicleSpotCheckDetailList) {
                long spotCheckTimeL = checkInfo.getSpotCheckTime().getTime();
                if (spotCheckTimeL <= queryEndTimeL && spotCheckTimeL >= queryStartTimeL) {
                    Integer spotCheckContent = checkInfo.getSpotCheckContent();
                    if (spotCheckContent == 0) {
                        if (checkPositionInfoNum == 0) {
                            checkPositionInfoNum++;
                            info.addCheckPositionInfoNum();
                        }
                    } else if (spotCheckContent == 1) {
                        if (checkHistoricalTrackNum == 0) {
                            checkHistoricalTrackNum++;
                            info.addCheckHistoricalTrackNum();
                        }
                    } else if (spotCheckContent == 2) {
                        if (checkVideoNum == 0) {
                            checkVideoNum++;
                            info.addCheckVideoNum();
                        }
                    } else if (spotCheckContent == 3) {
                        if (violationHandlingNum == 0) {
                            violationHandlingNum++;
                            info.addViolationHandlingNum();
                        }
                    }
                    if ((checkPositionInfoNum + checkHistoricalTrackNum + checkVideoNum + violationHandlingNum) == 4) {
                        break;
                    }
                }
            }
        }
    }

    @Override
    public List<GroupSpotCheckVehicleNumberCont> getGroupSportCheckVehicleData(String groupIds, String startTime,
        String endTime) throws Exception {
        RedisKey key = HistoryRedisKeyEnum.GROUP_SPOT_CHECK_VEHICLE_NUMBER_KEY.of(SystemHelper.getCurrentUsername());
        if (RedisHelper.isContainsKey(key)) {
            RedisHelper.delete(key);
        }
        List<GroupSpotCheckVehicleNumberCont> resultData = new ArrayList<>();
        if (StringUtils.isNotBlank(groupIds) && StringUtils.isNotBlank(startTime) && StringUtils.isNotBlank(endTime)) {
            List<String> groupUuids = Arrays.asList(groupIds.split(","));
            if (groupUuids.size() > 0) {
                Map<String, List<String>> groupVehicleId = getNeedQueryVehicleId(groupUuids);
                // 遍历map,获取所有企业下的车辆id
                List<String> vehicleIds = getAllAssignIdByMap(groupVehicleId);
                Map<String, List<VehicleSpotCheckCont>> groupSpotCheckData = null;
                if (vehicleIds != null && vehicleIds.size() > 0) {
                    // 车id -> 对应车id查询时间段内的抽查数据
                    Map<String, VehicleSpotCheckCont> vehicleSpotCheck =
                        getVehicleSpotCheckInfo(vehicleIds, startTime, endTime);
                    // 组合数据.组合为企业下的抽查数据进行统计
                    groupSpotCheckData = getGroupSpotCheckData(groupVehicleId, vehicleSpotCheck);
                }
                resultData = countGroupSpotCheckVehicleNumber(groupSpotCheckData, groupVehicleId, groupUuids);
            }
        }
        if (CollectionUtils.isNotEmpty(resultData)) {
            RedisHelper.addToList(key, resultData);
            RedisHelper.expireKey(key, 60 * 60);
        }
        return resultData;
    }

    private Map<String, List<String>> getNeedQueryVehicleId(List<String> groupUuids) throws Exception {
        // 企业下的分组
        List<GroupDTO> groupsByOrgIds = groupService.getGroupsByOrgIds(groupUuids);
        if (CollectionUtils.isNotEmpty(groupsByOrgIds)) {
            Set<String> groupIdsByOrgs = groupsByOrgIds.stream().map(GroupDTO::getId).collect(Collectors.toSet());
            // 当前用户的分组
            Set<String> currentUserGroupIds = userService.getCurrentUserGroupIds();
            // 取交集
            currentUserGroupIds.retainAll(groupIdsByOrgs);
            if (CollectionUtils.isEmpty(currentUserGroupIds)) {
                return new HashMap<>();
            }
            Set<String> userVehicleId = RedisHelper.batchGetSet(RedisKeyEnum.GROUP_MONITOR.ofs(currentUserGroupIds));
            return getOwnedVehicleIdByGroup(groupUuids, userVehicleId);
        }
        return new HashMap<>();
    }

    /**
     * 根据企业id为key组装所属企业是此企业的车辆id数据
     */
    private Map<String, List<String>> getOwnedVehicleIdByGroup(List<String> groupUuids, Set<String> userVehicleId)
        throws Exception {
        Map<String, List<String>> groupOwnedVehicle = new HashMap<>();
        if (CollectionUtils.isNotEmpty(groupUuids) && CollectionUtils.isNotEmpty(userVehicleId)) {
            List<VehicleDO> vehicleList = newVehicleDao.findByOrgIds(groupUuids);
            if (CollectionUtils.isNotEmpty(vehicleList)) {
                Map<String, String> disposeGroupVehicle =
                    vehicleList.stream().collect(Collectors.toMap(VehicleDO::getId, VehicleDO::getOrgId));
                for (Map.Entry<String, String> entry : disposeGroupVehicle.entrySet()) {
                    String vehicleId = entry.getKey(); // 监控对象id
                    if (userVehicleId.contains(vehicleId)) { // 用户对车有权限
                        String groupId = entry.getValue(); // 企业id
                        List<String> vhIds = groupOwnedVehicle.get(groupId);
                        if (vhIds == null) {
                            vhIds = new ArrayList<>();
                        }
                        vhIds.add(vehicleId);
                        groupOwnedVehicle.put(groupId, vhIds);
                    }
                }
            }
        }
        return groupOwnedVehicle;
    }

    /**
     * 整合 企业下用户有权限的车辆和所属企业是此企业的车辆,取交集(
     * 也就是获取企业下用户有分组权限的、所属企业是该企业的车辆和
     * 其他企业的分组下,所属企业是查询的企业的车辆),也就是所属企业是查询企业的并且用户有车辆所属企业分组权限的车辆
     * em...........
     * @return
     */
    private Map<String, List<String>> combinationAssignVehicleAndGroupOwnedVehicle(
        Map<String, List<String>> groupVehicleId, Map<String, List<String>> groupOwnedVehicle) throws Exception {
        Map<String, List<String>> resultVehicleId = new HashMap<>();
        if (groupVehicleId != null && groupVehicleId.size() > 0 && groupOwnedVehicle != null
            && groupOwnedVehicle.size() > 0) {
            for (Map.Entry<String, List<String>> entry : groupVehicleId.entrySet()) {
                String groupId = entry.getKey();
                List<String> groupAssignVehicleId = entry.getValue();
                if (CollectionUtils.isNotEmpty(groupAssignVehicleId)) {
                    List<String> groupOwnedVehicleId = groupOwnedVehicle.get(groupId);
                    if (CollectionUtils.isNotEmpty(groupOwnedVehicleId)) {
                        groupAssignVehicleId.retainAll(groupOwnedVehicleId);
                        resultVehicleId.put(groupId, groupAssignVehicleId);
                    }
                }
            }
        }
        return resultVehicleId;
    }

    /**
     * 统计道路运输企业抽查车辆数量
     */
    private List<GroupSpotCheckVehicleNumberCont> countGroupSpotCheckVehicleNumber(
        Map<String, List<VehicleSpotCheckCont>> groupSpotCheckData, Map<String, List<String>> groupVehicleId,
        List<String> groupUuid) {
        List<GroupSpotCheckVehicleNumberCont> result = new ArrayList<>();
        if (groupSpotCheckData == null) {
            groupSpotCheckData = new HashMap<>();
        }
        Set<String> orgIdList = new HashSet<>(groupUuid);
        Map<String, OrganizationLdap> orgMap = organizationService.getOrgByUuids(orgIdList);
        for (String uuId : groupUuid) {
            // 企业名称
            String groupName = orgMap.get(uuId).getName();
            // 企业下车辆总数
            Integer groupVehicleSum = groupVehicleId.get(uuId) != null ? groupVehicleId.get(uuId).size() : 0;
            if (StringUtils.isNotBlank(groupName)) {
                GroupSpotCheckVehicleNumberCont numberCont = new GroupSpotCheckVehicleNumberCont();
                Integer groupCheckPositionNumber = 0;
                Integer groupCheckHistoricalTrackNumber = 0;
                Integer groupCheckVideoNumber = 0;
                Integer groupViolationHandingNumber = 0;
                Integer spotCheckVehicleSum = 0;
                // 企业下抽查数据
                List<VehicleSpotCheckCont> spotCheckInfo = groupSpotCheckData.get(uuId);
                if (spotCheckInfo != null && spotCheckInfo.size() > 0 && groupVehicleSum != 0) {
                    for (VehicleSpotCheckCont cont : spotCheckInfo) {
                        //  一个车辆查询范围内的抽查位置次数(一天一次/多次抽查均记为一次,如果查询范围为3天,车辆每天都有抽查,那么记为3)
                        int spotCheckPositionNumber = cont.getSpotCheckPositionNumber();
                        //  一个车辆查询范围内的抽查历史数据次数(一天一次/多次抽查均记为一次,如果查询范围为3天,车辆每天都有抽查,那么记为3)
                        int spotCheckHistoricalTrackNumber = cont.getSpotCheckHistoricalTrackNumber();
                        //  一个车辆查询范围内的抽查视频次数(一天一次/多次抽查均记为一次,如果查询范围为3天,车辆每天都有抽查,那么记为3)
                        int spotCheckVideoNumber = cont.getSpotCheckVideoNumber();
                        //  一个车辆查询范围内的违章处理次数(一天一次/多次抽查均记为一次,如果查询范围为3天,车辆每天都有抽查,那么记为3)
                        int violationHandingNumber = cont.getViolationHandingNumber();
                        // 车辆查询范围内所有抽查次数数据,获取到最大值,就是合计数据(使用list API来获取最大值)
                        List<Integer> allCheckNumber = new ArrayList<>();
                        allCheckNumber.add(spotCheckPositionNumber);
                        allCheckNumber.add(spotCheckHistoricalTrackNumber);
                        allCheckNumber.add(spotCheckVideoNumber);
                        allCheckNumber.add(violationHandingNumber);
                        // 一个车辆的合计
                        int summation = Collections.max(allCheckNumber);
                        spotCheckVehicleSum += summation;
                        groupCheckPositionNumber += spotCheckPositionNumber;
                        groupCheckHistoricalTrackNumber += spotCheckHistoricalTrackNumber;
                        groupCheckVideoNumber += spotCheckVideoNumber;
                        groupViolationHandingNumber += violationHandingNumber;
                    }
                    // 查看定位信息(百分比)
                    double checkPositionInfoPercentage =
                        numberDataDis(groupCheckPositionNumber * 100.0 / groupVehicleSum * 1.0);
                    numberCont.setGroupCheckPositionInfoPercentage(numberFormatNumber(checkPositionInfoPercentage));
                    // 查看历史轨迹(百分比)
                    double checkHistoricalTrackPercentage =
                        numberDataDis(groupCheckHistoricalTrackNumber * 100.0 / groupVehicleSum * 1.0);
                    numberCont
                        .setGroupCheckHistoricalTrackPercentage(numberFormatNumber(checkHistoricalTrackPercentage));
                    // 查看视频(百分比)
                    double checkVideoPercentage = numberDataDis(groupCheckVideoNumber * 100.0 / groupVehicleSum * 1.0);
                    numberCont.setGroupCheckVideoPercentage(numberFormatNumber(checkVideoPercentage));
                    // 处理违章(百分比)
                    double checkViolationHandingPercentage =
                        numberDataDis(groupViolationHandingNumber * 100.0 / groupVehicleSum * 1.0);
                    numberCont.setGroupViolationHandlingPercentage(numberFormatNumber(checkViolationHandingPercentage));
                    // 合计(百分比)
                    double groupTotalPercentage = numberDataDis(spotCheckVehicleSum * 100.0 / groupVehicleSum * 1.0);
                    numberCont.setGroupTotalPercentage(numberFormatNumber(groupTotalPercentage));
                }
                // 查看定位信息
                numberCont.setGroupCheckPositionNumber(groupCheckPositionNumber);
                // 查看历史轨迹
                numberCont.setGroupCheckHistoricalTrackNumber(groupCheckHistoricalTrackNumber);
                // 查看视频
                numberCont.setGroupCheckVideoNumber(groupCheckVideoNumber);
                // 违章处理
                numberCont.setGroupViolationHandingNumber(groupViolationHandingNumber);
                // 企业名称
                numberCont.setGroupName(groupName);
                // 车辆总数
                numberCont.setGroupVehicleSum(groupVehicleSum);
                // 被抽查的车辆总数
                numberCont.setGroupSpotCheckVehicleSummation(spotCheckVehicleSum);
                result.add(numberCont);
            }
        }
        return result;
    }

    /**
     * 组合数据(企业下的抽查数据)
     */
    private Map<String, List<VehicleSpotCheckCont>> getGroupSpotCheckData(Map<String, List<String>> groupVehicleId,
        Map<String, VehicleSpotCheckCont> vehicleSpotCheck) {
        Map<String, List<VehicleSpotCheckCont>> groupSpotCheckData = new HashMap<>();
        if (groupVehicleId != null && groupVehicleId.size() > 0 && vehicleSpotCheck != null
            && vehicleSpotCheck.size() > 0) {
            for (Map.Entry<String, List<String>> entry : groupVehicleId.entrySet()) {
                String groupId = entry.getKey();
                List<VehicleSpotCheckCont> resultCheckData = groupSpotCheckData.get(groupId);
                if (resultCheckData == null) {
                    resultCheckData = new ArrayList<>();
                }
                List<String> vehicleId = entry.getValue();
                if (vehicleId != null && vehicleId.size() > 0) {
                    for (String id : vehicleId) {
                        VehicleSpotCheckCont vehicleIdCheckData = vehicleSpotCheck.get(id);
                        if (vehicleIdCheckData != null) {
                            resultCheckData.add(vehicleIdCheckData);
                        }
                    }
                }
                groupSpotCheckData.put(groupId, resultCheckData);
            }
        }
        return groupSpotCheckData;
    }

    /**
     * 获取车辆抽查数据
     */
    private Map<String, VehicleSpotCheckCont> getVehicleSpotCheckInfo(List<String> vehicleIds, String startTime,
        String endTime) throws Exception {
        // 查询的所有企业下的的抽查数据
        List<VehicleSpotCheckInfo> allVehicleSpotCheckInfo =
            spotCheckReportDao.getVehicleSpotCheckDetailList(vehicleIds, startTime, endTime);
        // 每个车一条记录
        Map<String, VehicleSpotCheckCont> vehicleSpotCheck = new HashMap<>();
        //车辆id,车辆抽查数据对应关系map
        Map<String, List<VehicleSpotCheckInfo>> vehicleSpotCheckInfo =
            neatenVehicleSpotCheckData(allVehicleSpotCheckInfo);
        for (Map.Entry<String, List<VehicleSpotCheckInfo>> entry : vehicleSpotCheckInfo.entrySet()) {
            String key = entry.getKey(); // 车id
            List<VehicleSpotCheckInfo> mapValue = entry.getValue();
            if (mapValue != null && mapValue.size() > 0) {
                List<VehicleSpotCheckInfo> checkPosition = new ArrayList<>();
                List<VehicleSpotCheckInfo> checkHistoricalTrack = new ArrayList<>();
                List<VehicleSpotCheckInfo> checkVideo = new ArrayList<>();
                List<VehicleSpotCheckInfo> violationHanding = new ArrayList<>();
                for (VehicleSpotCheckInfo info : mapValue) {
                    // 将抽查内容0:定位信息; 1:历史轨迹; 2:查看视频; 3:违章处理
                    Integer checkContent = info.getSpotCheckContent();
                    if (checkContent != null) {
                        switch (checkContent) {
                            case 0: // 定位信息
                                checkPosition.add(info);
                                break;
                            case 1: // 历史轨迹
                                checkHistoricalTrack.add(info);
                                break;
                            case 2: // 查看视频
                                checkVideo.add(info);
                                break;
                            case 3: // 违章处理
                                violationHanding.add(info);
                                break;
                            default:
                                break;
                        }
                    }
                }
                // 一个车辆一天内对应同一个操作发生一次或多次均记为1次
                VehicleSpotCheckCont oneDayDate = new VehicleSpotCheckCont();
                int checkPositionNumber = countSpotCheckNumber(checkPosition);
                int checkHistoricalTrackNumber = countSpotCheckNumber(checkHistoricalTrack);
                int checkVideoNumber = countSpotCheckNumber(checkVideo);
                int violationHandingNumber = countSpotCheckNumber(violationHanding);
                oneDayDate.setSpotCheckPositionNumber(checkPositionNumber);
                oneDayDate.setSpotCheckHistoricalTrackNumber(checkHistoricalTrackNumber);
                oneDayDate.setSpotCheckVideoNumber(checkVideoNumber);
                oneDayDate.setViolationHandingNumber(violationHandingNumber);
                vehicleSpotCheck.put(key, oneDayDate);
            }
        }
        return vehicleSpotCheck;
    }

    /**
     * 统计抽查次数(道路运输企业抽查报表抽查次数的统计是一个车,一天内,一个抽查内容,不管内抽查几次,都只记为1)
     * @param spotCheckInfo
     */
    private int countSpotCheckNumber(List<VehicleSpotCheckInfo> spotCheckInfo) throws Exception {
        Map<Long, Integer> everyDayCheckData = new HashMap<>();
        if (spotCheckInfo != null && spotCheckInfo.size() > 0) {
            for (VehicleSpotCheckInfo checkInfo : spotCheckInfo) {
                Date checkTime = checkInfo.getSpotCheckTime();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(checkTime);
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH) + 1;
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                Long dayTime = DateUtil.getScheduleTime(year, month, day);
                if (!everyDayCheckData.containsKey(dayTime)) {
                    everyDayCheckData.put(dayTime, 1);
                }
            }
        }
        return everyDayCheckData.size();
    }

    /**
     * 将MAP<String,List<String>的value放入一个list
     */
    private List<String> getAllAssignIdByMap(Map<String, List<String>> map) {
        Collection<List<String>> collection = map.values();
        List<List<String>> resultMapValue = new ArrayList<>(collection);
        List<String> allMapValue = new ArrayList<>();
        for (List<String> list : resultMapValue) {
            allMapValue.addAll(list);
        }
        return allMapValue;
    }

    /**
     * 处理数据,将查询到的抽查数据按车辆id进行统计
     */
    private Map<String, List<VehicleSpotCheckInfo>> neatenVehicleSpotCheckData(List<VehicleSpotCheckInfo> data) {
        Map<String, List<VehicleSpotCheckInfo>> everyVehicleSpotCheck = new HashMap<>();
        if (CollectionUtils.isNotEmpty(data)) {
            data.forEach(checkData -> {
                String vehicleId = checkData.getVehicleId();
                List<VehicleSpotCheckInfo> infos = everyVehicleSpotCheck.get(vehicleId);
                if (infos == null) {
                    infos = new ArrayList<>();
                }
                infos.add(checkData);
                everyVehicleSpotCheck.put(vehicleId, infos);
            });
        }
        return everyVehicleSpotCheck;
    }

    /**
     * 保留两位小数
     * @param number
     * @return
     */
    private double numberDataDis(double number) {

        return ComputingUtils.numberDataDis(number);
    }

    /**
     * 数字转为字符串
     * @param formatNumber
     * @return
     */
    private String numberFormatNumber(double formatNumber) {
        String numberStr = String.valueOf(formatNumber);
        // 小数部分
        String decimals = numberStr.substring(numberStr.indexOf(".") + 1, numberStr.length());
        if (decimals.equals("0")) {
            numberStr = numberStr.substring(0, numberStr.indexOf("."));
        }
        return numberStr + "%";
    }

    /**
     * 导出道路运输企业抽查车辆数量统计数据
     * @param response
     * @param fuzzyParam
     * @throws Exception
     */
    @Override
    public void exportGroupSpotCheckVehicleNumberData(HttpServletResponse response, String fuzzyParam)
        throws Exception {
        ExportExcel export = new ExportExcel(null, GroupSpotCheckVehicleNumberCont.class, 1, null);
        RedisKey key = HistoryRedisKeyEnum.GROUP_SPOT_CHECK_VEHICLE_NUMBER_KEY.of(SystemHelper.getCurrentUsername());
        List<GroupSpotCheckVehicleNumberCont> redisData =
            RedisHelper.getList(key, GroupSpotCheckVehicleNumberCont.class);
        List<GroupSpotCheckVehicleNumberCont> resultExportData = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(redisData)) {
            resultExportData.addAll(redisData);
            if (StringUtils.isNotBlank(fuzzyParam)) { //模糊搜索需要过滤 如果没有搜索则全部导出
                String upperCaseFuzzyParam = fuzzyParam.toUpperCase();
                resultExportData.clear();
                List<GroupSpotCheckVehicleNumberCont> filterExportList =
                    redisData.stream().filter(data -> data.getGroupName().toUpperCase().contains(upperCaseFuzzyParam))
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(filterExportList)) {
                    resultExportData.addAll(filterExportList);
                }
            }
        }
        export.shiftRows(0, 0, 1);
        export.setRowNum(export.getRowNum() + 1);
        Map<String, CellStyle> styles = export.getStyles();
        CellStyle style = styles.get("header");
        //添加首行数据
        Row row = export.getRow(0);
        row.createCell(0).setCellValue("道路运输企业");
        row.getCell(0).setCellStyle(style);
        row.createCell(1).setCellValue("抽查内容(车辆数)");
        row.getCell(1).setCellStyle(style);
        row.createCell(5).setCellValue("车辆总数");
        row.getCell(5).setCellStyle(style);
        row.createCell(6).setCellValue("合计");
        row.getCell(6).setCellStyle(style);
        row.createCell(7).setCellValue("抽查内容(百分比)");
        row.getCell(7).setCellStyle(style);
        row.createCell(11).setCellValue("合计(百分比)");
        row.getCell(11).setCellStyle(style);
        //合并单元格
        Sheet sheet = export.getSheet();
        sheet.addMergedRegion(new CellRangeAddress(0, 1, 0, 0));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 1, 4));
        sheet.addMergedRegion(new CellRangeAddress(0, 1, 5, 5));
        sheet.addMergedRegion(new CellRangeAddress(0, 1, 6, 6));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 7, 10));
        sheet.addMergedRegion(new CellRangeAddress(0, 1, 11, 11));
        export.setDataList(resultExportData);
        OutputStream out = response.getOutputStream();
        export.write(out);
        out.close();
    }

}

package com.cb.platform.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.cb.platform.domain.EnterpriseDispatch;
import com.cb.platform.domain.VehicleScheduler;
import com.cb.platform.repository.mysqlDao.VehicleScheduleDao;
import com.cb.platform.service.VehicleDispatchReportService;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.core.TypeCacheManger;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.platform.util.excel.ExportExcel;
import com.zw.platform.util.excel.ExportExcelParam;
import com.zw.platform.util.excel.ExportExcelUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class VehicleDispatchReportServiceImpl implements VehicleDispatchReportService {

    @Autowired
    private VehicleScheduleDao vehicleScheduleDao;

    @Autowired
    private UserService userService;

    @Autowired
    private OrganizationService organizationService;

    private TypeCacheManger cacheManger = TypeCacheManger.getInstance();

    /**
     * 车辆调度信息道路运输企业统计月报表查询接口
     */
    @Override
    public List<EnterpriseDispatch> getEnterpriseList(String groupList, String month) {
        //获取权限内车辆ID
        Set<String> currentUserMonitorIds = userService.getCurrentUserMonitorIds();
        List<String> vehicleList = VehicleUtil.sortVehicles(currentUserMonitorIds);
        //权限内没有车辆就直接返回
        if (CollectionUtils.isEmpty(vehicleList)) {
            return new ArrayList<>();
        }
        String[] str = groupList.split(",");
        List<String> groupLists = Arrays.asList(str);
        List<EnterpriseDispatch> enterpriseDispatch = new ArrayList<>();
        List<VehicleScheduler> list = vehicleScheduleDao.getEnterpriseList(groupLists, vehicleList, month);
        String[] dateStr = month.split("-");
        int year = Integer.parseInt(dateStr[0]);
        int month1 = Integer.parseInt(dateStr[1]);
        EnterpriseDispatch ed;
        int count = 0;
        String[] list2 = getLastDayOfMonth(year, month1);
        Set<String> orgIds = list.stream().map(VehicleScheduler::getGroupId).collect(Collectors.toSet());
        Map<String, OrganizationLdap> orgMap = organizationService.getOrgByUuids(orgIds);
        for (int i = 0; i < list.size(); i++) {
            OrganizationLdap organizationLdap = orgMap.get(list.get(i).getGroupId());
            String groupName = organizationLdap.getName();
            String times = list.get(i).getTimes();
            Date dateTime = list.get(i).getSendDate();
            // 获取查询自然月天数
            String date = DateFormatUtils.format(dateTime, "dd");
            count += Integer.parseInt(times);
            int dateTimes = Integer.parseInt(date);
            // 数据组装
            if (i != list.size() - 1) {
                String groupId = list.get(i).getGroupId();
                String groupId1 = list.get(i + 1).getGroupId();
                // 如果下标1 等于 下标+1,存入list2集合 反之 组装实体类
                if (groupId.equals(groupId1)) {
                    Arrays.fill(list2, dateTimes - 1, dateTimes, times);
                } else {
                    Arrays.fill(list2, dateTimes - 1, dateTimes, times);
                    ed = new EnterpriseDispatch();
                    ed.setGroupName(groupName);
                    ed.setMonth(month);
                    ed.setDateTime(list2);
                    ed.setCount(count);
                    enterpriseDispatch.add(ed);
                    count = 0;
                    list2 = getLastDayOfMonth(year, month1);
                }
            } else {
                if (list.size() == 1) {
                    list2 = getLastDayOfMonth(year, month1);
                    Arrays.fill(list2, dateTimes - 1, dateTimes, times);
                    ed = new EnterpriseDispatch();
                    ed.setMonth(month);
                    ed.setGroupName(groupName);
                    ed.setDateTime(list2);
                    ed.setCount(count);
                    enterpriseDispatch.add(ed);
                } else {
                    String groupId = list.get(list.size() - 2).getGroupId();
                    String groupId1 = list.get(list.size() - 1).getGroupId();
                    if (groupId.equals(groupId1)) {
                        Arrays.fill(list2, dateTimes - 1, dateTimes, times);
                    } else {
                        list2 = getLastDayOfMonth(year, month1);
                        Arrays.fill(list2, dateTimes - 1, dateTimes, times);
                    }
                    ed = new EnterpriseDispatch();
                    ed.setMonth(month);
                    ed.setGroupName(groupName);
                    ed.setDateTime(list2);
                    ed.setCount(count);
                    enterpriseDispatch.add(ed);
                }

            }
        }
        String userId = userService.getCurrentUserUuid();
        RedisKey redisKey = HistoryRedisKeyEnum.USER_ENTERPRISE_DISPATCH.of(userId);
        RedisHelper.delete(redisKey);
        // 获取组装数据存入redis管道
        RedisHelper.addToList(redisKey, enterpriseDispatch);
        RedisHelper.expireKey(redisKey, 60 * 60);
        return enterpriseDispatch;
    }

    /**
     * 车辆调度信息统计月报表查询接口
     */
    @Override
    public List<EnterpriseDispatch> getVehicleList(String vehicleList, String month) {
        String[] str = vehicleList.split(",");
        List<String> vehicleLists = Arrays.asList(str);
        List<EnterpriseDispatch> enterpriseDispatch = new ArrayList<>();
        List<VehicleScheduler> list = vehicleScheduleDao.getVehicleList(vehicleLists, month);
        String[] dateStr = month.split("-");
        int year = Integer.parseInt(dateStr[0]);
        int month1 = Integer.parseInt(dateStr[1]);
        EnterpriseDispatch ed;
        int count = 0;
        String[] list2 = getLastDayOfMonth(year, month1);
        Map<String, Map<String, String>> vehicleMap =
            RedisHelper.batchGetHashMap(RedisKeyEnum.MONITOR_INFO.ofs(vehicleLists)).stream()
                .collect(Collectors.toMap(o -> o.get("id"), Function.identity()));
        for (int i = 0; i < list.size(); i++) {
            // 从redis 取车辆缓存 组装实体
            String vid = list.get(i).getVehicleId();
            Map<String, String> vehicleInfo = vehicleMap.get(vid);
            String brand = String.valueOf(vehicleInfo.get("name"));
            String plateColor = vehicleInfo.get("plateColor");
            plateColor = VehicleUtil.getPlateColorStr(plateColor);
            String vehType = cacheManger.getVehicleType(vehicleInfo.get("vehicleType")).getType();
            String name = vehicleInfo.get("orgName");
            String times = list.get(i).getTimes();
            Date dateTime = list.get(i).getSendDate();
            String date = DateFormatUtils.format(dateTime, "dd");
            count += Integer.parseInt(times);
            int dateTimes = Integer.parseInt(date);
            if (i != list.size() - 1) {
                String vehicleId = list.get(i).getVehicleId();
                String vehicleId1 = list.get(i + 1).getVehicleId();
                // 如果下标1 等于 下标+1,存入list2集合 反之 组装实体类
                if (vehicleId.equals(vehicleId1)) {
                    Arrays.fill(list2, dateTimes - 1, dateTimes, times);
                } else {
                    Arrays.fill(list2, dateTimes - 1, dateTimes, times);
                    ed = new EnterpriseDispatch();
                    ed.setBrand(brand);
                    ed.setColor(plateColor);
                    ed.setVehicleType(vehType);
                    ed.setGroupName(name);
                    ed.setMonth(month);
                    ed.setDateTime(list2);
                    ed.setCount(count);
                    enterpriseDispatch.add(ed);
                    count = 0;
                    list2 = getLastDayOfMonth(year, month1);
                }
            } else {
                if (list.size() == 1) {
                    list2 = getLastDayOfMonth(year, month1);
                    Arrays.fill(list2, dateTimes - 1, dateTimes, times);
                    ed = new EnterpriseDispatch();
                    ed.setMonth(month);
                    ed.setBrand(brand);
                    ed.setColor(plateColor);
                    ed.setVehicleType(vehType);
                    ed.setGroupName(name);
                    ed.setDateTime(list2);
                    ed.setCount(count);
                    enterpriseDispatch.add(ed);
                } else {
                    String vehicleId = list.get(list.size() - 2).getVehicleId();
                    String vehicleId1 = list.get(list.size() - 1).getVehicleId();
                    if (vehicleId.equals(vehicleId1)) {
                        Arrays.fill(list2, dateTimes - 1, dateTimes, times);
                    } else {
                        list2 = getLastDayOfMonth(year, month1);
                        Arrays.fill(list2, dateTimes - 1, dateTimes, times);
                    }
                    ed = new EnterpriseDispatch();
                    ed.setMonth(month);
                    ed.setBrand(brand);
                    ed.setColor(plateColor);
                    ed.setVehicleType(vehType);
                    ed.setGroupName(name);
                    ed.setDateTime(list2);
                    ed.setCount(count);
                    enterpriseDispatch.add(ed);
                }

            }
        }
        String userId = userService.getCurrentUserUuid();
        RedisKey redisKey = HistoryRedisKeyEnum.USER_VEHICLE_DISPATCH_FORMATION.of(userId);
        // 再次查询前删除 key
        RedisHelper.delete(redisKey);
        // 获取组装数据存入redis管道
        RedisHelper.addToList(redisKey, enterpriseDispatch);
        return enterpriseDispatch;
    }

    /**
     * 车辆调度信息明细表查询接口
     */
    @Override
    public List<VehicleScheduler> getDetailList(String vehicleList, String startTime, String endTime) {
        List<String> vehicleLists = VehicleUtil.distinctMonitorIds(vehicleList);
        List<VehicleScheduler> list = vehicleScheduleDao.getDetailList(vehicleLists, startTime, endTime);
        Map<String, Map<String, String>> vehicleMap =
            RedisHelper.batchGetHashMap(RedisKeyEnum.MONITOR_INFO.ofs(vehicleLists)).stream()
                .collect(Collectors.toMap(o -> o.get("id"), Function.identity()));
        String content;
        for (VehicleScheduler vs : list) {
            String vid = vs.getVehicleId();
            Map<String, String> vehicle = vehicleMap.get(vid);
            String brand = vehicle.get("name");
            String plateColor = vehicle.get("plateColor");
            plateColor = VehicleUtil.getPlateColorStr(plateColor);
            String vehType = cacheManger.getVehicleType(vehicle.get("vehicleType")).getType();
            String name = vehicle.get("orgName");
            vs.setBrand(brand);
            vs.setColor(plateColor);
            vs.setVehicleType(vehType);
            vs.setGroupName(name);
            content = vs.getContent();
            if (!StringUtils.isEmpty(content) && (content.contains("zw") || content.contains("ZW"))) {
                vs.setContent("");
            }
        }
        String userId = userService.getCurrentUserUuid();
        RedisKey redisKey = HistoryRedisKeyEnum.USER_VEHICLE_DETAIL_DISPATCH_FORMATION.of(userId);
        // 再次查询前删除 key
        RedisHelper.delete(redisKey);
        // 获取组装数据存入redis管道
        RedisHelper.addToList(redisKey, list);
        return list;
    }

    private static String[] getLastDayOfMonth(int year, int month) {
        Calendar cal = Calendar.getInstance();
        // 设置年份
        cal.set(Calendar.YEAR, year);
        // 设置月份
        cal.set(Calendar.MONTH, month - 1);
        // 获取某月最大天数
        int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        // 设置日历中月份的最大天数
        cal.set(Calendar.DAY_OF_MONTH, lastDay);
        // 格式化日期
        String lastDayOfMonth = DateFormatUtils.format(cal, "dd");
        int day = Integer.parseInt(lastDayOfMonth);
        String[] str = new String[day];
        Arrays.fill(str, "0");
        return str;
    }

    /**
     * 车辆调度信息道路运输企业统计月报表导出接口
     */
    @Override
    public boolean exportEnterpriseList(String title, int type, HttpServletResponse res) throws Exception {
        String userUuid = userService.getCurrentUserUuid();
        List<String> list = RedisHelper.getList(HistoryRedisKeyEnum.USER_ENTERPRISE_DISPATCH.of(userUuid));
        List<EnterpriseDispatch> enterpriseDispatch;
        if (CollectionUtils.isEmpty(list)) {
            enterpriseDispatch = new ArrayList<>();
        } else {
            enterpriseDispatch = list.stream().map(o -> JSONObject.parseObject(o, EnterpriseDispatch.class))
                .collect(Collectors.toList());
        }
        List<String> headList = new ArrayList<>();
        List<String> requiredList = new ArrayList<>();
        List<Object> exportList;
        headList.add("道路运输企业");
        int length = 30;
        if (enterpriseDispatch.size() > 0) {
            String month = enterpriseDispatch.get(0).getMonth();
            String[] dateStr = month.split("-");
            int year = Integer.parseInt(dateStr[0]);
            int month1 = Integer.parseInt(dateStr[1]);
            length = getLastDayOfMonth(year, month1).length;
            for (int i = 1; i <= length; i++) {
                headList.add(String.valueOf(i));
            }
        } else {
            for (int i = 1; i <= 30; i++) {
                headList.add(String.valueOf(i));
            }
        }
        headList.add("合计");
        Map<String, String[]> selectMap = new HashMap<>();
        ExportExcel export = new ExportExcel(headList, requiredList, selectMap);
        Row row;

        for (EnterpriseDispatch dispatch : enterpriseDispatch) {
            exportList = new ArrayList<>();
            exportList.add(dispatch.getGroupName());
            exportList.addAll(Arrays.asList(dispatch.getDateTime()).subList(0, length));
            exportList.add(dispatch.getCount());

            row = export.addRow();
            for (int x = 0; x < exportList.size(); x++) {
                export.addCell(row, x, exportList.get(x));
            }
        }
        // 输出导文件
        OutputStream out;
        out = res.getOutputStream();
        export.write(out);// 将文档对象写入文件输出流
        out.close();
        return true;
    }

    /**
     * 车辆调度信息统计月报表导出接口
     */
    @Override
    public boolean exportVehicleList(String title, int type, HttpServletResponse res) throws Exception {
        String userUuid = userService.getCurrentUserUuid();
        List<String> list = RedisHelper.getList(HistoryRedisKeyEnum.USER_VEHICLE_DISPATCH_FORMATION.of(userUuid));
        List<EnterpriseDispatch> enterpriseDispatch;
        if (CollectionUtils.isEmpty(list)) {
            enterpriseDispatch = new ArrayList<>();
        } else {
            enterpriseDispatch = list.stream().map(o -> JSONObject.parseObject(o, EnterpriseDispatch.class))
                .collect(Collectors.toList());
        }
        List<String> headList = new ArrayList<>();
        List<String> requiredList = new ArrayList<>();
        List<Object> exportList;
        headList.add("车牌号");
        headList.add("车辆颜色");
        headList.add("车辆类型");
        headList.add("所属道路运输企业");
        int length = 30;
        if (enterpriseDispatch.size() > 0) {
            String month = enterpriseDispatch.get(0).getMonth();
            String[] dateStr = month.split("-");
            int year = Integer.parseInt(dateStr[0]);
            int month1 = Integer.parseInt(dateStr[1]);
            length = getLastDayOfMonth(year, month1).length;
            for (int i = 1; i <= length; i++) {
                headList.add(String.valueOf(i));
            }
        } else {
            for (int i = 1; i <= 30; i++) {
                headList.add(String.valueOf(i));
            }
        }
        headList.add("合计");
        Map<String, String[]> selectMap = new HashMap<>();
        ExportExcel export = new ExportExcel(headList, requiredList, selectMap);
        Row row;

        for (EnterpriseDispatch dispatch : enterpriseDispatch) {
            exportList = new ArrayList<>();
            exportList.add(dispatch.getBrand());
            exportList.add(dispatch.getColor());
            exportList.add(dispatch.getVehicleType());
            exportList.add(dispatch.getGroupName());
            exportList.addAll(Arrays.asList(dispatch.getDateTime()).subList(0, length));
            exportList.add(dispatch.getCount());

            row = export.addRow();
            for (int x = 0; x < exportList.size(); x++) {
                export.addCell(row, x, exportList.get(x));
            }
        }
        // 输出导文件
        OutputStream out;
        out = res.getOutputStream();
        export.write(out);// 将文档对象写入文件输出流
        out.close();
        return true;
    }

    /**
     * 车辆调度信息明细表导出接口
     */
    @Override
    public boolean exportDetailList(String title, int type, HttpServletResponse res) throws Exception {
        String userUuid = userService.getCurrentUserUuid();
        List<String> list =
            RedisHelper.getList(HistoryRedisKeyEnum.USER_VEHICLE_DETAIL_DISPATCH_FORMATION.of(userUuid));
        List<VehicleScheduler> vehicleScheduler;
        if (CollectionUtils.isEmpty(list)) {
            vehicleScheduler = new ArrayList<>();
        } else {
            vehicleScheduler =
                list.stream().map(o -> JSONObject.parseObject(o, VehicleScheduler.class)).collect(Collectors.toList());
        }
        return ExportExcelUtil.export(
            new ExportExcelParam(title, type, vehicleScheduler, VehicleScheduler.class, null, res.getOutputStream()));
    }
}

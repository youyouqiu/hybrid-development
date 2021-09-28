package com.cb.platform.domain.query;

import com.cb.platform.domain.OffLineExportBusinessId;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.domain.basicinfo.OfflineExportInfo;
import com.zw.platform.util.StrUtil;
import com.zw.platform.util.common.BaseQueryBean;
import com.zw.platform.util.common.Date8Utils;
import com.zw.platform.util.common.MonitorUtils;
import joptsimple.internal.Strings;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * 车辆在线时长统计报表查询实体
 */
@Data
public class VehicleOnlineTimeQuery extends BaseQueryBean {

    /**
     * 企业ids
     */
    private String organizationIds;

    /**
     * 监控对象ids
     */
    private String monitorIds;

    /**
     * 查询的月，格式为202101
     */
    private long month;

    /**
     * 省编码如	50
     */
    private String provinceCode;
    /**
     * 市编码如	50（多个按照逗号隔开）
     */
    private String cityCodes;

    /**
     * 市编码如		0000 （多个按照逗号隔开）
     */
    private String countyCodes;

    /**
     * 用户自定义文件名称
     */
    private String fileName;

    /**
     * 模块名称
     */
    private String module;

    public VehicleOnlineTimeQuery convertOrgNameToOrgIds(UserService userService) {
        if (StrUtil.isNotBlank(getSimpleQueryParam())) {
            List<String> filterOrgIds = userService.fuzzSearchUserOrgIdsByOrgName(getSimpleQueryParam());
            List<String> totalOrgIds = Arrays.asList(organizationIds.split(","));
            filterOrgIds.retainAll(totalOrgIds);
            organizationIds = Strings.join(filterOrgIds, ",");
        }

        return this;
    }

    public VehicleOnlineTimeQuery convertMonitorNameToMonitorIds() {
        if (StrUtil.isNotBlank(getSimpleQueryParam())) {
            Set<String> filterMonitorIds = MonitorUtils.fuzzySearchBindMonitorIds(getSimpleQueryParam());
            List<String> totalOrgIds = Arrays.asList(monitorIds.split(","));
            filterMonitorIds.retainAll(totalOrgIds);
            monitorIds = Strings.join(filterMonitorIds, ",");
        }

        return this;
    }

    public Map<String, String> getOrgParam() {

        Map<String, String> param = new HashMap<>();
        param.put("organizationIds", organizationIds);
        param.put("startMonth", month + "");
        param.put("endMonth", month + "");
        param.put("page", getPage() + "");
        param.put("pageSize", getLimit() + "");
        return param;
    }

    public Map<String, String> getMonitorParam() {
        Map<String, String> param = new HashMap<>();
        param.put("monitorIds", monitorIds);
        param.put("startMonth", month + "");
        param.put("endMonth", month + "");
        param.put("page", getPage() + "");
        param.put("pageSize", getLimit() + "");
        return param;
    }

    public Map<String, String> getDivisionParam() {
        Map<String, String> param = new HashMap<>();
        param.put("provinceCode", provinceCode);
        param.put("cityCodes", cityCodes);
        param.put("countyCodes", countyCodes);
        param.put("startMonth", month + "");
        param.put("endMonth", month + "");

        return param;
    }

    private TreeMap<String, String> getExportOrgParam() {
        TreeMap<String, String> param = new TreeMap<>();
        param.put("organizationIds", organizationIds);
        param.put("startMonth", month + "");
        param.put("endMonth", month + "");
        param.put("flag", getFlag(month));
        return param;
    }

    public TreeMap<String, String> getExportMonitorParam() {
        TreeMap<String, String> param = new TreeMap<>();
        param.put("monitorIds", monitorIds);
        param.put("startMonth", month + "");
        param.put("endMonth", month + "");
        param.put("flag", getFlag(month));

        return param;
    }

    public TreeMap<String, String> getExportDivisionParam() {
        TreeMap<String, String> param = new TreeMap<>();
        param.put("provinceCode", provinceCode);
        param.put("cityCodes", cityCodes);
        param.put("countyCodes", countyCodes);
        param.put("startMonth", month + "");
        param.put("endMonth", month + "");

        param.put("flag", getFlag(month));

        return param;
    }

    /**
     * 生成一个唯一标识，避免不同时间生成的数据重复引用问题
     * 这里前端传递是202005只到月份，而我们的报表当月的数据，每天都会变
     * 当天查询的数据只要条件一样都是一样，上一个月的数据都是不变的，即只要
     * 前面的月份数据只要查询条件一样，那么生成的flag也是一样的，当月的数据，
     * 只要当天的查询条件一样，那么生成的flag也是一样的
     * @param month
     * @return
     */
    private String getFlag(long month) {
        LocalDateTime now = LocalDateTime.now();
        //如果不是当月的数据,直接以该条件作为标记
        if (Date8Utils.getValToMonth(now) > month) {
            return month + "";
        } else {
            //如果是当月的，每天一个唯一flag,因为每天数据一样，每一天数据都不一样
            return Date8Utils.getValToDay(now) + "";
        }
    }

    public OfflineExportInfo getOrgOffLineExport() {
        //默认名称
        String fileName = "车辆在线时长统计(按道路运行企业统计)" + Date8Utils.getValToTime(LocalDateTime.now());
        return getOffLineExportInfo(fileName, getExportOrgParam(), OffLineExportBusinessId.VEHICLE_ONLINE_TIME_ORG);

    }

    public OfflineExportInfo getMonitorOffLineExport() {
        //默认名称
        String fileName = "车辆在线时长统计(按车辆统计)" + Date8Utils.getValToTime(LocalDateTime.now());
        return getOffLineExportInfo(fileName, getExportMonitorParam(),
            OffLineExportBusinessId.VEHICLE_ONLINE_TIME_MONITOR);

    }

    public OfflineExportInfo getDivisionOffLineExport() {
        //默认名称
        String fileName = "车辆在线时长统计(按行政区域统计)" + Date8Utils.getValToTime(LocalDateTime.now());
        return getOffLineExportInfo(fileName, getExportDivisionParam(),
            OffLineExportBusinessId.VEHICLE_ONLINE_TIME_DIVISION);

    }

    private OfflineExportInfo getOffLineExportInfo(String fileName, TreeMap<String, String> param,
        OffLineExportBusinessId businessId) {
        OfflineExportInfo instance = OfflineExportInfo.getInstance(module, fileName + ".xls");
        //业务id后续替换
        instance.assembleCondition(param, businessId);
        return instance;

    }

}

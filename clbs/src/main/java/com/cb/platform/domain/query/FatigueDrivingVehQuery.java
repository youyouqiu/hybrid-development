package com.cb.platform.domain.query;

import com.cb.platform.domain.OffLineExportBusinessId;
import com.zw.platform.domain.basicinfo.OfflineExportInfo;
import com.zw.platform.util.common.BaseQueryBean;
import com.zw.platform.util.common.Date8Utils;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * 车辆疲劳查询实体
 */
@Data
public class FatigueDrivingVehQuery extends BaseQueryBean {

    /**
     * 监控对象id,多个按照逗号隔开
     */
    private String monitorIds;

    /**
     * 查询时间
     */
    private long month;
    /**
     * 模糊搜索参数
     */
    private String fuzzyQueryParam;
    /**
     * 用户自定义文件名称
     */
    private String fileName;

    /**
     * 模块名称
     */
    private String module;
    /**
     * 监控对象id
     */
    private String monitorId;

    public Map<String, String> getParam() {
        Map<String, String> param = new HashMap<>();
        param.put("monitorIds", monitorIds);
        param.put("startMonth", month + "");
        param.put("endMonth", month + "");
        param.put("page", getPage() + "");
        param.put("pageSize", getLimit() + "");
        return param;
    }

    public Map<String, String> getVehicleParam() {
        Map<String, String> param = new HashMap<>();
        param.put("monitorIds", monitorId);
        param.put("startMonth", month + "");
        param.put("endMonth", month + "");
        param.put("page", getPage() + "");
        param.put("pageSize", getLimit() + "");
        return param;
    }

    public TreeMap<String, String> getExportListParam() {
        TreeMap<String, String> param = new TreeMap<>();
        param.put("monitorIds", monitorIds);
        param.put("queryMonth", month + "");
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

    private String getFlags(long month) {
        LocalDateTime now = LocalDateTime.now();
        //如果不是当月的数据,直接以该条件作为标记
        if (Date8Utils.getValToTime(now) < month) {
            return month + "";
        } else {
            //如果是当月的，每天一个唯一flag,因为每天数据一样，每一天数据都不一样
            return Date8Utils.getValToDay(now) + "";
        }
    }

    public Map<String, String> getDetailParam() {
        Map<String, String> param = new HashMap<>();
        LocalDateTime startMonthDay = Date8Utils.getLocalDateTime(month);
        //报警前一天的数据
        LocalDateTime endMonthDay = LocalDateTime.now().minusDays(1);
        if (!sameMonth(startMonthDay, endMonthDay)) {
            endMonthDay = startMonthDay.with(TemporalAdjusters.lastDayOfMonth());

        }
        endMonthDay = endMonthDay.withHour(23).withMinute(59).withSecond(59);
        String startTime = Date8Utils.getValToTime(startMonthDay) + "";
        String endTime = Date8Utils.getValToTime(endMonthDay) + "";
        param.put("monitorIds", monitorIds);
        param.put("startTime", startTime);
        param.put("endTime", endTime);
        param.put("page", getPage() + "");
        param.put("pageSize", getLimit() + "");
        param.put("isAddress", "1");
        return param;
    }

    public TreeMap<String, String> getExportDetailParam() {
        TreeMap<String, String> param = new TreeMap<>();
        LocalDateTime startMonthDay = Date8Utils.getLocalDateTime(month);
        //报警前一天的数据
        LocalDateTime frontDay = LocalDateTime.now().minusDays(1);
        if (!sameMonth(startMonthDay, frontDay)) {
            frontDay = startMonthDay.with(TemporalAdjusters.lastDayOfMonth());
        }
        frontDay = frontDay.withHour(23).withMinute(59).withSecond(59);
        String alarmTime = Date8Utils.getValToTime(frontDay) + "";
        param.put("monitorId", monitorIds);
        param.put("queryMonth", month + "");
        param.put("alarmTime", alarmTime);
        param.put("isAddress", "1");
        param.put("flag", getFlags(month));
        return param;
    }

    public Map<String, String> getRankParam() {
        Map<String, String> param = new HashMap<>();
        param.put("monitorIds", monitorId);
        param.put("startMonth", month + "");
        param.put("endMonth", month + "");
        return param;
    }

    public Map<String, String> getGraphicsParam() {
        Map<String, String> param = new HashMap<>();
        LocalDateTime startMonthDay = Date8Utils.getLocalDateTime(month);
        LocalDateTime endMonthDay = startMonthDay.with(TemporalAdjusters.lastDayOfMonth());
        String startDate = Date8Utils.getValToDay(startMonthDay) + "";
        String endDate = Date8Utils.getValToDay(endMonthDay) + "";
        param.put("monitorId", monitorId);
        param.put("startDate", startDate);
        param.put("endDate", endDate);
        return param;
    }

    private boolean sameMonth(LocalDateTime one, LocalDateTime two) {
        return Date8Utils.getValToMonth(one) == Date8Utils.getValToMonth(two);
    }

    public OfflineExportInfo getOrgListOffLineExport() {
        //默认名称
        String fileName = "疲劳驾驶报警车辆统计报表" + Date8Utils.getValToTime(LocalDateTime.now());
        return getOffLineExportInfo(fileName, getExportListParam(), OffLineExportBusinessId.FatVehList);

    }

    private OfflineExportInfo getOffLineExportInfo(String fileName, TreeMap<String, String> param,
        OffLineExportBusinessId businessId) {
        OfflineExportInfo instance = OfflineExportInfo.getInstance(module, fileName + ".xls");
        //业务id后续替换
        instance.assembleCondition(param, businessId);
        return instance;

    }

    public OfflineExportInfo getOrgDetailOffLineExport() {
        //默认名称
        String fileName = "疲劳驾驶报警车辆明细统计报表" + Date8Utils.getValToDay(LocalDateTime.now());
        return getOffLineExportInfo(fileName, getExportDetailParam(), OffLineExportBusinessId.FatVehDetail);

    }
}

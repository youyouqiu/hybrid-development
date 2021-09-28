package com.cb.platform.domain.query;

import com.cb.platform.domain.OffLineExportBusinessId;
import com.zw.platform.domain.basicinfo.OfflineExportInfo;
import com.zw.platform.util.common.BaseQueryBean;
import com.zw.platform.util.common.Date8Utils;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * 连接信息统计报表查询实体
 */
@Data
public class ConnectionStaticsQuery extends BaseQueryBean {

    /**
     * 上级平台ids
     */
    private String t809platformIds;

    /**
     * 监控对象ids
     */
    private String monitorIds;

    /**
     * 查询的月，格式为202101
     */
    private long month;

    /**
     * 查询详情的开始时间，格式为20210123
     */
    private long startDate;

    /**
     * 查询详情的结束时间，格式为20210123
     */
    private long endDate;

    /**
     * 模糊搜索参数
     */
    private String fuzzyQueryParam;

    /**
     * 模块名称
     */
    private String module;

    public Map<String, String> getPlatformParam() {
        Map<String, String> param = new HashMap<>();
        param.put("t809platformIds", t809platformIds);
        param.put("startMonth", month + "");
        param.put("endMonth", month + "");
        param.put("page", getPage() + "");
        param.put("pageSize", getLimit() + "");
        param.put("linkType", "1");
        return param;
    }

    public Map<String, String> getPlatformDetailParam() {
        Map<String, String> param = new HashMap<>();
        param.put("t809platformIds", t809platformIds);
        param.put("startDate", startDate + "");
        param.put("endDate", endDate + "");
        param.put("page", getPage() + "");
        param.put("pageSize", getLimit() + "");
        param.put("linkType", "1");
        return param;
    }

    public Map<String, String> getMonitorParam() {
        Map<String, String> param = new HashMap<>();
        param.put("monitorIds", monitorIds);
        param.put("month", month + "");
        param.put("page", getPage() + "");
        param.put("pageSize", getLimit() + "");
        return param;
    }

    public Map<String, String> getMonitorDetailParam() {
        Map<String, String> param = new HashMap<>();
        param.put("monitorIds", monitorIds);
        param.put("month", month + "");
        return param;
    }

    public TreeMap<String, String> getExportPlatformParam() {
        TreeMap<String, String> param = new TreeMap<>();
        param.put("t809platformIds", t809platformIds);
        param.put("startMonth", month + "");
        param.put("endMonth", month + "");
        param.put("linkType", "1");
        param.put("flag", getMonthFlag(month));
        return param;
    }

    public TreeMap<String, String> getExportPlatformDetailParam() {
        TreeMap<String, String> param = new TreeMap<>();
        param.put("t809platformIds", t809platformIds);
        param.put("startDate", startDate + "");
        param.put("endDate", endDate + "");
        param.put("linkType", "1");
        param.put("flag", getDayFlag(endDate));
        return param;
    }

    public TreeMap<String, String> getExportMonitorParam() {
        TreeMap<String, String> param = new TreeMap<>();
        param.put("monitorIds", monitorIds);
        param.put("month", month + "");
        param.put("flag", getMonthFlag(month));
        return param;
    }

    public TreeMap<String, String> getExportMonitorDetailParam() {
        TreeMap<String, String> param = new TreeMap<>();
        param.put("monitorIds", monitorIds);
        param.put("month", month + "");
        param.put("flag", getMonthFlag(month));
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
    private String getMonthFlag(long month) {
        LocalDateTime now = LocalDateTime.now();
        //如果不是当月的数据,直接以该条件作为标记
        if (Date8Utils.getValToMonth(now) > month) {
            return month + "";
        } else {
            //如果是当月的，每天一个唯一flag,因为每天数据一样，每一天数据都不一样
            return Date8Utils.getValToDay(now) + "";
        }
    }

    private String getDayFlag(long day) {
        LocalDateTime now = LocalDateTime.now();
        //如果不是当月的数据,直接以该条件作为标记
        if (Date8Utils.getValToDay(now) > day) {
            return day + "";
        } else {
            //如果是当月的，每天一个唯一flag,因为每天数据一样，每一天数据都不一样
            return Date8Utils.getValToTime(now) + "";
        }
    }

    public OfflineExportInfo getOffLineExportPlatform() {
        OfflineExportInfo instance =
            OfflineExportInfo.getInstance(module, "与政府平台连接情况" + Date8Utils.getValToTime(LocalDateTime.now()) + ".xls");
        //业务id后续替换
        instance.assembleCondition(getExportPlatformParam(), OffLineExportBusinessId.CONNECTION_STATICS_PLATFORM);
        return instance;
    }

    public OfflineExportInfo getOffLineExportPlatformDetail() {
        OfflineExportInfo instance = OfflineExportInfo
            .getInstance(module, "与政府平台连接情况详情" + Date8Utils.getValToTime(LocalDateTime.now()) + ".xls");
        //业务id后续替换
        instance.assembleCondition(getExportPlatformDetailParam(),
            OffLineExportBusinessId.CONNECTION_STATICS_PLATFORM_DETAIL);
        return instance;
    }

    public OfflineExportInfo getOffLineExportMonitor() {
        OfflineExportInfo instance =
            OfflineExportInfo.getInstance(module, "与车载终端连接情况" + Date8Utils.getValToTime(LocalDateTime.now()) + ".xls");
        //业务id后续替换
        instance.assembleCondition(getExportMonitorParam(), OffLineExportBusinessId.CONNECTION_STATICS_MONITOR);
        return instance;
    }

    public OfflineExportInfo getOffLineExportMonitorDetail() {
        OfflineExportInfo instance = OfflineExportInfo
            .getInstance(module, "与车载终端连接情况详情" + Date8Utils.getValToTime(LocalDateTime.now()) + ".xls");
        //业务id后续替换
        instance.assembleCondition(getExportMonitorDetailParam(),
            OffLineExportBusinessId.CONNECTION_STATICS_MONITOR_DETAIL);
        return instance;
    }

}

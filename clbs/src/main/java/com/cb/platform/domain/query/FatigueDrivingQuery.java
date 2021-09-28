package com.cb.platform.domain.query;

import com.cb.platform.domain.OffLineExportBusinessId;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.domain.basicinfo.OfflineExportInfo;
import com.zw.platform.util.StrUtil;
import com.zw.platform.util.common.BaseQueryBean;
import com.zw.platform.util.common.Date8Utils;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Data
public class FatigueDrivingQuery extends BaseQueryBean {

    /**
     * 企业id
     */
    private String organizationId;

    private long month;

    /**
     * 模糊搜索参数
     */
    private String fuzzyQueryParam;

    /**
     * 用于查询图形图传递的参数
     */
    private int isSingle;

    /**
     * 用户自定义文件名称
     */
    private String fileName;

    /**
     * 模块名称
     */
    private String module;

    /**
     * 企业名称
     */
    private String groupName;

    public Map<String, String> getParam() {
        Map<String, String> param = new HashMap<>();
        param.put("organizationId", organizationId);
        param.put("startMonth", month + "");
        param.put("endMonth", month + "");
        param.put("page", getPage() + "");
        param.put("pageSize", getLimit() + "");
        param.put("fuzzyQueryParam", fuzzyQueryParam);
        return param;
    }

    private TreeMap<String, String> getExportListParam() {
        TreeMap<String, String> param = new TreeMap<>();
        param.put("organizationId", organizationId);
        param.put("queryMonth", month + "");
        param.put("fuzzyQueryParam", fuzzyQueryParam);
        param.put("flag", getFlag(month));
        return param;
    }

    public TreeMap<String, String> getExportDetailParam() {
        TreeMap<String, String> param = new TreeMap<>();
        param.put("organizationId", organizationId);
        param.put("queryMonth", month + "");
        param.put("fuzzyQueryParam", "");
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

    public OfflineExportInfo getOrgListOffLineExport() {
        //默认名称
        String fileName = getOrgName() + "企业疲劳驾驶报警统计报表" + Date8Utils.getValToTime(LocalDateTime.now());
        return getOffLineExportInfo(fileName, getExportListParam(), OffLineExportBusinessId.FatOrgList);

    }

    private String getOrgName() {
        String groupName = "";
        Map<String, String> groupInfo =
            RedisHelper.getHashMap(RedisKeyEnum.ORGANIZATION_INFO.of(organizationId), "id", "name");

        if (groupInfo != null) {
            groupName = StrUtil.getOrBlank(groupInfo.get("name"));
        }
        return groupName;
    }

    public OfflineExportInfo getOrgDetailOffLineExport() {
        //默认名称
        String fileName = getOrgName() + "企业疲劳驾驶报警明细统计报表" + Date8Utils.getValToTime(LocalDateTime.now());
        return getOffLineExportInfo(fileName, getExportDetailParam(), OffLineExportBusinessId.FatOrgDetail);

    }

    private OfflineExportInfo getOffLineExportInfo(String fileName, TreeMap<String, String> param,
        OffLineExportBusinessId businessId) {
        OfflineExportInfo instance = OfflineExportInfo.getInstance(module, fileName + ".xls");
        //业务id后续替换
        instance.assembleCondition(param, businessId);
        return instance;

    }

    public Map<String, String> getDetailParam() {
        Map<String, String> param = new HashMap<>();

        param.put("organizationId", organizationId);
        param.put("startMonth", month + "");
        param.put("endMonth", month + "");
        param.put("page", getPage() + "");
        param.put("pageSize", getLimit() + "");
        param.put("fuzzyQueryParam", fuzzyQueryParam);
        return param;
    }

    public Map<String, String> getRankParam() {
        Map<String, String> param = new HashMap<>();
        param.put("organizationIds", organizationId);
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
        param.put("organizationId", organizationId);
        param.put("startDate", startDate);
        param.put("endDate", endDate);
        param.put("isSingle", isSingle + "");
        return param;
    }

    private boolean sameMonth(LocalDateTime one, LocalDateTime two) {
        return Date8Utils.getValToMonth(one) == Date8Utils.getValToMonth(two);
    }

}

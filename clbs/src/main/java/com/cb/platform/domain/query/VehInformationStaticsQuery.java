package com.cb.platform.domain.query;

import com.cb.platform.domain.OffLineExportBusinessId;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.domain.basicinfo.OfflineExportInfo;
import com.zw.platform.util.StrUtil;
import com.zw.platform.util.common.BaseQueryBean;
import com.zw.platform.util.common.Date8Utils;
import joptsimple.internal.Strings;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 车辆信息统计报表查询实体
 */
@Data
public class VehInformationStaticsQuery extends BaseQueryBean {

    /**
     * 企业ids
     */
    private String organizationIds;

    /**
     * 企业id
     */
    private String organizationId;

    /**
     * 查询的月，格式为202101
     */
    private long month;

    /**
     * 查询的月，格式为20210123
     */
    private long date;

    /**
     * 用户自定义文件名称
     */
    private String fileName;

    /**
     * 模块名称
     */
    private String module;

    public VehInformationStaticsQuery convertOrgNameToOrgIds(UserService userService) {
        if (StrUtil.isNotBlank(getSimpleQueryParam())) {
            List<String> filterOrgIds = userService.fuzzSearchUserOrgIdsByOrgName(getSimpleQueryParam());
            List<String> totalOrgIds = Arrays.asList(organizationIds.split(","));
            filterOrgIds.retainAll(totalOrgIds);
            organizationIds = Strings.join(filterOrgIds, ",");
        }

        return this;
    }

    public Map<String, String> getParam() {

        Map<String, String> param = new HashMap<>();
        param.put("organizationIds", organizationIds);
        param.put("startMonth", month + "");
        param.put("endMonth", month + "");
        param.put("page", getPage() + "");
        param.put("pageSize", getLimit() + "");
        return param;
    }

    public Map<String, String> getDetailParam() {
        Map<String, String> param = new HashMap<>();
        param.put("organizationId", organizationId);
        param.put("startDate", date + "");
        param.put("endDate", date + "");
        param.put("page", getPage() + "");
        param.put("pageSize", getLimit() + "");
        return param;
    }

    private TreeMap<String, String> getExportListParam() {
        TreeMap<String, String> param = new TreeMap<>();
        param.put("organizationIds", organizationIds);
        param.put("startMonth", month + "");
        param.put("endMonth", month + "");
        param.put("flag", getFlag(month));
        return param;
    }

    public TreeMap<String, String> getExportDetailParam() {
        TreeMap<String, String> param = new TreeMap<>();
        param.put("organizationId", organizationId);
        param.put("startDate", date + "");
        param.put("endDate", date + "");
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
        String fileName = "车辆信息统计汇总" + Date8Utils.getValToTime(LocalDateTime.now());
        return getOffLineExportInfo(fileName, getExportListParam(),
            OffLineExportBusinessId.VEH_INFORMATION_STATICS_ORG);

    }

    public OfflineExportInfo getOrgDetailOffLineExport() {
        //默认名称
        String fileName = "车辆信息统计明细" + Date8Utils.getValToTime(LocalDateTime.now());
        return getOffLineExportInfo(fileName, getExportDetailParam(),
            OffLineExportBusinessId.VEH_INFORMATION_STATICS_ORG_DETAIL);

    }

    private OfflineExportInfo getOffLineExportInfo(String fileName, TreeMap<String, String> param,
        OffLineExportBusinessId businessId) {
        OfflineExportInfo instance = OfflineExportInfo.getInstance(module, fileName + ".xls");
        //业务id后续替换
        instance.assembleCondition(param, businessId);
        return instance;

    }

    public Map<String, String> getGraphicsParam() {
        Map<String, String> param = new HashMap<>();
        param.put("organizationIds", organizationIds);
        param.put("month", month + "");
        return param;

    }

    public Map<String, String> getDetailGraphicsParam() {
        Map<String, String> param = new HashMap<>();
        param.put("organizationId", organizationId);
        param.put("month", month + "");
        return param;
    }

}

package com.cb.platform.domain.query;

import com.cb.platform.domain.OffLineExportBusinessId;
import com.zw.platform.domain.basicinfo.OfflineExportInfo;
import com.zw.platform.service.functionconfig.FenceConfigService;
import com.zw.platform.util.common.BaseQueryBean;
import com.zw.platform.util.common.Date8Utils;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * 途经点统计报表查询实体
 */
@Data
public class PointQuery extends BaseQueryBean {

    /**
     * 企业ids
     */
    private String organizationIds;

    /**
     * 监控对象ids
     */
    private String monitorIds;

    /**
     * 查询开始时间,格式:yyyyMMdd
     */
    private String startDate;
    /**
     * 查询结束时间,格式:yyyyMMdd
     */
    private String endDate;

    /**
     * 查询开始时间,格式:yyyyMMddHHmmss
     */
    private String startTime;
    /**
     * 查询结束时间,格式:yyyyMMddHHmmss
     */
    private String endTime;

    /**
     * 用户自定义文件名称
     */
    private String fileName;

    /**
     * 途经点id,多个按照逗号隔开
     */
    private String passPointIds;

    /**
     * 模块名称
     */
    private String module;

    /**
     * 初始化企业途经点模糊搜索
     * @param
     */
    public PointQuery initOrgPassPointIds(FenceConfigService fenceConfigService) {
        this.passPointIds = fenceConfigService.getPointFenceIdByGroupIds(this);
        return this;
    }

    /**
     * 初始化监控搞对象途经点模糊搜索
     * @param
     */
    public PointQuery initMonitorPassPointIds(FenceConfigService fenceConfigService) {
        this.passPointIds = fenceConfigService.getPointFenceIdByMonitorIds(this);
        return this;
    }

    public Map<String, String> getOrgParam() {
        Map<String, String> param = getOrgBasicParam();
        initPageParam(param);
        return param;
    }

    public Map<String, String> getMonitorParam() {
        Map<String, String> param = getMonitorBasicParam();
        initPageParam(param);
        return param;
    }

    public Map<String, String> getMonitorDetailParam() {
        Map<String, String> param = getMonitorDetailBasicParam();
        initPageParam(param);
        return param;
    }

    private TreeMap<String, String> getExportOrgParam() {
        TreeMap<String, String> param = new TreeMap<>();
        param.putAll(getOrgBasicParam());
        param.put("flag", getFlag(startDate, endDate));
        return param;
    }

    public TreeMap<String, String> getExportMonitorParam() {
        TreeMap<String, String> param = new TreeMap<>();
        param.putAll(getMonitorBasicParam());
        param.put("flag", getFlag(startDate, endDate));

        return param;
    }

    public TreeMap<String, String> getExportMonitorDetailParam() {
        TreeMap<String, String> param = new TreeMap<>();
        param.putAll(getMonitorDetailBasicParam());
        param.put("flag", getFlag(startTime, endTime));

        return param;
    }

    private Map<String, String> getMonitorBasicParam() {
        Map<String, String> param = new HashMap<>();
        param.put("monitorIds", monitorIds);
        param.put("passPointIds", passPointIds);
        initDateParam(param);
        return param;
    }

    private Map<String, String> getOrgBasicParam() {
        Map<String, String> param = new HashMap<>();
        param.put("organizationIds", organizationIds);
        param.put("passPointIds", passPointIds);
        initDateParam(param);
        return param;
    }

    private Map<String, String> getMonitorDetailBasicParam() {
        Map<String, String> param = new HashMap<>();
        param.put("monitorIds", monitorIds);
        param.put("passPointIds", passPointIds);
        param.put("startTime", startTime);
        param.put("endTime", endTime);
        return param;
    }

    private void initPageParam(Map<String, String> param) {
        param.put("page", getPage() + "");
        param.put("pageSize", getLimit() + "");
    }

    private void initDateParam(Map<String, String> param) {
        param.put("startDate", startDate);
        param.put("endDate", endDate);
    }

    /**
     * 参数时间到达毫秒的时候，直接以开始时间和结束时间拼接即可
     * @param startTime
     * @param endTime
     * @return
     */
    private String getFlag(String startTime, String endTime) {
        return startTime + ":" + endTime;
    }

    public OfflineExportInfo getOrgOffLineExport() {
        //默认名称
        String fileName = "途经点统计(按道路运输企业统计)" + Date8Utils.getValToTime(LocalDateTime.now());
        return getOffLineExportInfo(fileName, getExportOrgParam(), OffLineExportBusinessId.POINT_ORG);

    }

    public OfflineExportInfo getMonitorOffLineExport() {
        //默认名称
        String fileName = "途经点统计(按车辆统计)" + Date8Utils.getValToTime(LocalDateTime.now());
        return getOffLineExportInfo(fileName, getExportMonitorParam(), OffLineExportBusinessId.POINT_MONITOR);

    }

    public OfflineExportInfo getMonitorDetailExport() {

        //默认名称
        String fileName = "途经点统计(按车辆途经顺序统计)" + Date8Utils.getValToTime(LocalDateTime.now());
        return getOffLineExportInfo(fileName, getExportMonitorDetailParam(),
            OffLineExportBusinessId.POINT_MONITOR_DETAIL);

    }

    private OfflineExportInfo getOffLineExportInfo(String fileName, TreeMap<String, String> param,
        OffLineExportBusinessId businessId) {
        OfflineExportInfo instance = OfflineExportInfo.getInstance(module, fileName + ".xls");
        //业务id后续替换
        instance.assembleCondition(param, businessId);
        return instance;

    }

}

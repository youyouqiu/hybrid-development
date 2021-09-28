package com.cb.platform.service.speedingStatistics.impl;

import com.alibaba.fastjson.JSONObject;
import com.cb.platform.domain.OffLineExportBusinessId;
import com.cb.platform.domain.speedingStatistics.quey.UpSpeedGroupQuery;
import com.cb.platform.service.speedingStatistics.UpSpeedStatisticsGroupService;
import com.cb.platform.util.page.PassCloudResultBean;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.basicinfo.OfflineExportInfo;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.service.core.UserService;
import com.zw.platform.util.common.Date8Utils;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.report.PaasCloudUrlEnum;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @Author zhangqiang
 * @Date 2020/5/18 10:39
 */
@Service
public class UpSpeedStatisticsGroupServiceImpl implements UpSpeedStatisticsGroupService {

    private static final String SUCCESS_STATUS_CODE = "10000";
    private Logger logger = LogManager.getLogger(UpSpeedStatisticsGroupServiceImpl.class);


    @Autowired
    private UserService userService;

    /*************查询企业超速图表数据*********/
    @Override
    public JsonResultBean findGraphicalStatistics(String groupId, String time, String isSingle) {
        Map<String, String> query = buildOrgOverSpeedQuery(groupId, time, isSingle);
        String passResult = HttpClientUtil.send(PaasCloudUrlEnum.UP_SPEED_GRAPHICAL_STATISTICS_URL, query);
        JSONObject result = JSONObject.parseObject(passResult);
        if (result != null && SUCCESS_STATUS_CODE.equals(result.getString("code"))) {
            return new JsonResultBean(result);
        }
        return new JsonResultBean(JsonResultBean.FAULT, "查询数据超时，请稍后再试");
    }

    private Map<String, String> buildOrgOverSpeedQuery(String groupId, String time, String isSingle) {
        Map<String, String> params = new HashMap<>();
        params.put("organizationId", groupId);
        params.put("isSingle", isSingle);
        params.put("startDate", time + "01");
        params.put("endDate", time + getDays(time));
        return params;
    }

    /*************分页查询企业超速列表数据*********/
    @Override
    public PassCloudResultBean speedingStatisticsList(UpSpeedGroupQuery query) {
        Map<String, String> params = getUpSpeedListParamsMap(query);
        String passResult = HttpClientUtil.send(PaasCloudUrlEnum.UP_SPEED_LIST_STATISTICS_URL, params);
        return PassCloudResultBean.getPageInstance(passResult);
    }

    /**
     * 获取分页查询超速报警列表数据
     * @param query
     * @return
     */
    private Map<String, String> getUpSpeedListParamsMap(UpSpeedGroupQuery query) {
        Map<String, String> params = new HashMap<>();
        params.put("organizationId", query.getGroupId());
        params.put("startMonth", query.getTime());
        params.put("endMonth", query.getTime());
        params.put("page", String.valueOf(query.getPage()));
        params.put("pageSize", String.valueOf(query.getLimit()));
        if (StringUtils.isNotEmpty(query.getFuzzyQueryParam())) {
            params.put("fuzzyQueryParam", query.getFuzzyQueryParam());
        }
        return params;
    }

    /**
     * 获取企业排名相关数据
     * @param query
     * @return
     */
    @Override
    public PassCloudResultBean rankInfo(UpSpeedGroupQuery query) {
        Map<String, String> params = getRankInfoParamsMap(query);
        String passResult = HttpClientUtil.send(PaasCloudUrlEnum.UP_SPEED_GRAPHICAL_RANK_INFO_URL, params);
        return PassCloudResultBean.getPageInstance(passResult);
    }

    private Map<String, String> getRankInfoParamsMap(UpSpeedGroupQuery query) {
        Map<String, String> params = new HashMap<>();
        params.put("organizationIds", query.getGroupId());
        params.put("startMonth", query.getTime());
        params.put("endMonth", query.getTime());
        params.put("page", String.valueOf(query.getPage()));
        params.put("pageSize", String.valueOf(query.getLimit()));
        return params;
    }

    /*************分页查询企业超速详情列表数据*********/

    @Override
    public PassCloudResultBean upSpeedInfoList(UpSpeedGroupQuery query) {
        Map<String, String> params = getUpSpeedListParamsMap(query);
        String passResult = HttpClientUtil.send(PaasCloudUrlEnum.UP_SPEED_INFO_LIST_URL, params);
        return PassCloudResultBean.getPageInstance(passResult);
    }

    /**
     * 获取指定月份天数
     * YYYY-MM
     * @param time
     * @return
     */
    private int getDays(String time) {
        return DateUtil.getMonthDays(Integer.parseInt(time) / 100, Integer.parseInt(time) % 100);
    }

    @Override
    public OfflineExportInfo exportOrgListData(UpSpeedGroupQuery query) {
        OfflineExportInfo instance = getOfflineExportInfo(query, "企业超速统计报表");
        Map<String, String> params = getExportParams(query);
        TreeMap<String, String> param = new TreeMap<>(params);
        instance.assembleCondition(param, OffLineExportBusinessId.SpeedOrgList);
        return instance;
    }

    @Override
    public OfflineExportInfo exportOrgSpeedDetailsData(UpSpeedGroupQuery query) {
        OfflineExportInfo instance = getOfflineExportInfo(query, "企业超速明细报表");
        Map<String, String> params = getExportParams(query);
        TreeMap<String, String> param = new TreeMap<>(params);
        instance.assembleCondition(param, OffLineExportBusinessId.SpeedOrgDetail);
        return instance;
    }

    private Map<String, String> getExportParams(UpSpeedGroupQuery query) {
        Map<String, String> params = new HashMap<>();
        params.put("organizationId", query.getGroupId());
        params.put("queryMonth", query.getTime());
        if (StringUtils.isNotEmpty(query.getFuzzyQueryParam())) {
            params.put("fuzzyQueryParam", query.getFuzzyQueryParam());
        }
        return params;
    }

    private OfflineExportInfo getOfflineExportInfo(UpSpeedGroupQuery query, String name) {
        OrganizationLdap org = userService.getOrgByUuid(query.getGroupId());
        String fileName = org.getName() + "_" + name + Date8Utils.getValToTime(LocalDateTime.now());
        return OfflineExportInfo.getInstance(query.getModule(), fileName + ".xls");
    }

}

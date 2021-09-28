package com.zw.platform.service.reportManagement.impl;

import com.alibaba.fastjson.JSON;
import com.cb.platform.util.page.PassCloudResultBean;
import com.zw.platform.basic.repository.NewConfigDao;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.reportManagement.query.OfflineDisplacementQuery;
import com.zw.platform.service.reportManagement.OfflineDisplacementService;
import com.zw.platform.util.report.PaasCloudAlarmUrlEnum;
import com.zw.platform.util.report.PaasCloudUrlEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author wanxing
 * @Title: 离线位移日报表
 * @date 2020/10/2011:03
 */
@Service
public class OfflineDisplacementServiceImpl implements OfflineDisplacementService {

    @Autowired
    private NewConfigDao newConfigDao;

    @Override
    public PassCloudResultBean queryList(OfflineDisplacementQuery query) {

        String brandKeyword = query.getSimpleQueryParam();
        if (!StringUtils.isEmpty(brandKeyword)) {
            String vehicleId = getFuzzyVehicleId(brandKeyword, query.getMonitorIds());
            if (StringUtils.isEmpty(vehicleId)) {
                PassCloudResultBean passCloudResultBean = new PassCloudResultBean();
                passCloudResultBean.setCode(10000);
                return PassCloudResultBean.getPageInstance(JSON.toJSONString(passCloudResultBean));
            }
            query.setMonitorIds(vehicleId);
        }
        Map<String, String> params = generatePassQuery(query);
        String passResult = HttpClientUtil.send(PaasCloudUrlEnum.OFFLINE_DISPLACEMENT_LIST_URL, params);
        return PassCloudResultBean.getPageInstance(passResult);
    }

    private String getFuzzyVehicleId(String brandKeyword, String chooseVehicleIds) {
        brandKeyword = com.zw.platform.util.StringUtil.mysqlLikeWildcardTranslation(brandKeyword);
        Set<String> filterMoIds = newConfigDao.getMoIdsByFuzzyMoName(brandKeyword);
        Set<String> set = new HashSet<>(Arrays.asList(chooseVehicleIds.split(",")));
        set.retainAll(filterMoIds);
        if (set.isEmpty()) {
            return "";
        }
        return StringUtils.join(set, ",");
    }

    @Override
    public void deal(String monitorId, String offlineMoveEndTime, String handleResult, String remark) {
        Map<String, String> map = new HashMap<>(12);
        map.put("offlineMoveEndTime", offlineMoveEndTime);
        map.put("monitorId", monitorId);
        map.put("handleResult", handleResult);
        map.put("remark", remark);
        HttpClientUtil.send(PaasCloudAlarmUrlEnum.OFFLINE_DISPLACEMENT_DEAL_URL, map);
    }

    @Override
    public void batchDeal(String primaryKeys, String handleResult, String remark) {
        Map<String, String> map = new HashMap<>(6);
        map.put("primaryKeys", primaryKeys);
        map.put("handleResult", handleResult);
        map.put("remark", remark);
        HttpClientUtil.send(PaasCloudAlarmUrlEnum.OFFLINE_DISPLACEMENT_BATCH_DEAL_URL, map);
    }

    private Map<String, String> generatePassQuery(OfflineDisplacementQuery query) {
        Map<String, String> params = new HashMap<>(16);
        params.put("monitorIds", query.getMonitorIds());
        params.put("date", query.getDate());
        params.put("moveDistance", String.valueOf(query.getMoveDistance()));
        params.put("offlineTime", String.valueOf(query.getOfflineTime()));
        params.put("page", String.valueOf(query.getPage()));
        params.put("pageSize", String.valueOf(query.getLimit()));
        return params;
    }

}

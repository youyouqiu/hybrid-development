package com.cb.platform.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.cb.platform.domain.fatiguedriving.monitor.MonitorFatigueDrivingDetailListDTO;
import com.cb.platform.domain.query.FatigueDrivingQuery;
import com.cb.platform.domain.query.FatigueDrivingVehQuery;
import com.cb.platform.service.FatigueDrivingService;
import com.cb.platform.util.page.ApiPageInfo;
import com.cb.platform.util.page.PageResultBean;
import com.cb.platform.util.page.PassCloudResultBean;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.util.StrUtil;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.platform.util.report.PaasCloudUrlEnum;
import joptsimple.internal.Strings;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.zw.platform.util.report.PaasCloudUrlEnum.FATIGUE_DRIVING_DETAIL_ORG_URL;
import static com.zw.platform.util.report.PaasCloudUrlEnum.FATIGUE_DRIVING_DETAIL_VEH_URL;
import static com.zw.platform.util.report.PaasCloudUrlEnum.FATIGUE_DRIVING_GRAPHICS_VEH_URL;

/***
 @Author lijie
 @Date 2020/5/18 16:05
 @Description 疲劳驾驶统计service
 @version 1.0
 **/
@Service
public class FatigueDrivingServiceImpl implements FatigueDrivingService {

    @Override
    public PassCloudResultBean getOrgDataList(FatigueDrivingQuery query) {
        String queryResult = HttpClientUtil.send(PaasCloudUrlEnum.FATIGUE_DRIVING_ORG_LIST_URL, query.getParam());
        // queryResult = getPageData(OrgFatigueDrivingStatisticListDTO.getList(query.getLimit()));
        return PassCloudResultBean.getPageInstance(queryResult);
    }

    @Override
    public PassCloudResultBean getVehDataList(FatigueDrivingVehQuery query) {
        dealFuzzSearchMonitorName(query);
        if (StrUtil.isBlank(query.getMonitorIds())) {
            return PassCloudResultBean.getDefaultPageInstance();
        }
        String queryResult = HttpClientUtil.send(PaasCloudUrlEnum.FATIGUE_DRIVING_VEH_LIST_URL, query.getParam());
        // queryResult = getPageData(MonitorFatigueDrivingListDTO.getList(query.getLimit()));
        return PassCloudResultBean.getPageInstance(queryResult);
    }

    private void dealFuzzSearchMonitorName(FatigueDrivingVehQuery query) {
        String brand = query.getFuzzyQueryParam();
        if (StrUtil.isBlank(brand)) {
            return;
        }
        List<String> monitorIds = Arrays.asList(query.getMonitorIds().split(","));
        Map<String, BindDTO> vehicleInfoMap = VehicleUtil.batchGetBindInfosByRedis(monitorIds);
        Set<String> vehIds =
            vehicleInfoMap.values().stream().filter(e -> e.getName().contains(brand)).map(e -> e.getId())
                .collect(Collectors.toSet());
        query.setMonitorIds(Strings.join(vehIds, ","));
    }

    @Override
    public PassCloudResultBean getOrgGraphicsData(FatigueDrivingQuery query) {
        String queryResult =
            HttpClientUtil.send(PaasCloudUrlEnum.FATIGUE_DRIVING_GRAPHICS_ORG_URL, query.getGraphicsParam());
        // queryResult = getTempData(OrgFatigueDrivingStatisticBO.getInstance());
        return PassCloudResultBean.getDataInstance(queryResult);
    }

    @Override
    public PassCloudResultBean getVehGraphicsData(FatigueDrivingVehQuery query) {
        String queryResult = HttpClientUtil.send(FATIGUE_DRIVING_GRAPHICS_VEH_URL, query.getGraphicsParam());
        // queryResult = getTempData(MonitorFatigueDrivingStatisticBO.getInstance());
        return PassCloudResultBean.getDataInstance(queryResult);
    }

    @Override
    public PassCloudResultBean getOrgRankData(FatigueDrivingQuery query) {
        String queryResult =
            HttpClientUtil.send(PaasCloudUrlEnum.FATIGUE_DRIVING_RANK_ORG_URL, query.getRankParam());
        // queryResult = getPageData(OrgMonthStatisticDTO.getList(query.getLimit()));

        return PassCloudResultBean.getDataInstanceFromPage(queryResult);
    }

    @Override
    public PassCloudResultBean getVehRankData(FatigueDrivingVehQuery query) {
        String queryResult =
            HttpClientUtil.send(PaasCloudUrlEnum.FATIGUE_DRIVING_VEH_LIST_URL, query.getVehicleParam());
        // queryResult = getPageData(MonitorMonthStatisticDTO.getList(query.getLimit()));

        return PassCloudResultBean.getDataInstanceFromPage(queryResult);
    }

    @Override
    public PassCloudResultBean getOrgDetailData(FatigueDrivingQuery query) {
        String queryResult = HttpClientUtil.send(FATIGUE_DRIVING_DETAIL_ORG_URL, query.getDetailParam());
        // queryResult = getPageData(OrgMonitorFatigueDrivingListDTO.getList(query.getLimit()));
        return PassCloudResultBean.getPageInstance(queryResult);
    }

    @Override
    public PassCloudResultBean getVehDetailData(FatigueDrivingVehQuery query) {
        String queryResult = HttpClientUtil.send(FATIGUE_DRIVING_DETAIL_VEH_URL, query.getDetailParam());
        // queryResult = getPageData(MonitorFatigueDrivingDetailListDTO.getList(query.getLimit()));
        return PassCloudResultBean.getPageInstance(queryResult, data -> assembleData(data));
    }

    private List<MonitorFatigueDrivingDetailListDTO> assembleData(String dataStr) {
        List<MonitorFatigueDrivingDetailListDTO> dataInfo =
            JSONObject.parseArray(dataStr, MonitorFatigueDrivingDetailListDTO.class);
        dataInfo.stream().forEach(e -> e.initData());
        return dataInfo;
    }

    /**
     * 假数据
     * @return
     */
    private <T> String getPageData(List<T> dataList) {
        PassCloudResultBean passCloudResultBean = new PassCloudResultBean();

        ApiPageInfo apiPageInfo = new ApiPageInfo();
        apiPageInfo.setPage(1);
        apiPageInfo.setPageSize(10);
        apiPageInfo.setTotal(100);
        apiPageInfo.setTotalPage(10);
        PageResultBean pageResultBean = new PageResultBean();
        pageResultBean.setItems(dataList);
        pageResultBean.setPageInfo(apiPageInfo);
        passCloudResultBean.setCode(10000);
        passCloudResultBean.setMessage("ok");
        passCloudResultBean.setData(pageResultBean);
        String queryResult = JSONObject.toJSONString(passCloudResultBean);
        System.err.println(passCloudResultBean);
        return queryResult;
    }

    private <T> String getTempData(T data) {

        PassCloudResultBean passCloudResultBean = new PassCloudResultBean();
        passCloudResultBean.setCode(10000);
        passCloudResultBean.setMessage("ok");
        passCloudResultBean.setData(data);
        String queryResult = JSONObject.toJSONString(passCloudResultBean);
        System.err.println(passCloudResultBean);
        return queryResult;
    }

}

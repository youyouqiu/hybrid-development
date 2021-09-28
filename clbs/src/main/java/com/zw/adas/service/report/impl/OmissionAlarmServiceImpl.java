package com.zw.adas.service.report.impl;

import com.alibaba.fastjson.JSON;
import com.cb.platform.util.page.PassCloudResultBean;
import com.zw.adas.service.report.OmissionAlarmService;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.reportManagement.query.OmissionAlarmQuery;
import com.zw.platform.util.report.PaasCloudUrlEnum;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author wanxing
 * @Title: 漏报报警实现类
 * @date 2021/1/1916:38
 */
@Service
public class OmissionAlarmServiceImpl implements OmissionAlarmService {


    @Autowired
    private OrganizationService organizationService;
    
    @Override
    public PassCloudResultBean getPageByKeyword(OmissionAlarmQuery query) {
        String orgNameKeyword = query.getSimpleQueryParam();
        if (!StringUtils.isEmpty(orgNameKeyword)) {
            String orgId = getFuzzyOrgId(orgNameKeyword, query.getOrganizationIds());
            if (StringUtils.isEmpty(orgId)) {
                PassCloudResultBean passCloudResultBean = new PassCloudResultBean();
                passCloudResultBean.setCode(10000);
                return PassCloudResultBean.getDataInstance(JSON.toJSONString(passCloudResultBean));
            }
            query.setOrganizationIds(orgId);
        }
        Map<String, String> params = generatePassQuery(query);
        String passResult = HttpClientUtil.send(PaasCloudUrlEnum.UNDER_REPORT_LIST_URL, params);
        return PassCloudResultBean.getDataInstance(passResult);
    }

    @Override
    public PassCloudResultBean orgDetail(OmissionAlarmQuery query) {
        Map<String, String> params = generatePassQuery(query);
        String passResult = HttpClientUtil.send(PaasCloudUrlEnum.UNDER_REPORT_DETAIL_URL, params);
        return PassCloudResultBean.getDataInstance(passResult);
    }

    @Override
    public PassCloudResultBean orgDayCount(OmissionAlarmQuery query) {
        Map<String, String> params = generatePassQuery(query);
        String passResult = HttpClientUtil.send(PaasCloudUrlEnum.UNDER_REPORT_COUNT_URL, params);
        return PassCloudResultBean.getDataInstance(passResult);
    }


    /**
     * 模糊查询企业的Id
     * @param orgNameKeyword
     * @param chooseOrganizationIds
     * @return
     */
    private String getFuzzyOrgId(String orgNameKeyword, String chooseOrganizationIds) {

        List<OrganizationLdap> organizationLdapList = organizationService.fuzzyOrgList(orgNameKeyword);
        if (CollectionUtils.isEmpty(organizationLdapList)) {
            return "";
        }
        List<String> orgIdList = organizationLdapList.stream()
            .map(OrganizationLdap::getUuid).collect(Collectors.toList());
        Set<String> set = new HashSet<>(Arrays.asList(chooseOrganizationIds.split(",")));
        set.retainAll(orgIdList);
        if (set.isEmpty()) {
            return "";
        }
        return StringUtils.join(set, ",");
    }

    /**
     * 组装接口数据
     * @param query
     * @return
     */
    private Map<String, String> generatePassQuery(OmissionAlarmQuery query) {
        Map<String, String> params = new HashMap<>(8);
        String organizationIds = query.getOrganizationIds();
        String organizationId = query.getOrganizationId();
        if (StringUtils.isNotEmpty(organizationIds)) {
            params.put("organizationIds", organizationIds);
        }
        if (StringUtils.isNotEmpty(organizationId)) {
            params.put("organizationId", organizationId);
        }
        params.put("month", query.getMonth());
        return params;
    }
}

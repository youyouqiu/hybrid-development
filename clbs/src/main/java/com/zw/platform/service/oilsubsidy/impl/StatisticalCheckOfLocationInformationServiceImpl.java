package com.zw.platform.service.oilsubsidy.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.oilsubsidy.locationinformation.OilSubsidyLocationInformationDTO;
import com.zw.platform.domain.oilsubsidy.locationinformation.OilSubsidyLocationInformationQuery;
import com.zw.platform.repository.oilsubsidy.StatisticalCheckOfLocationInformationDao;
import com.zw.platform.service.oilsubsidy.StatisticalCheckOfLocationInformationService;
import com.zw.platform.util.PageHelperUtil;
import com.zw.platform.util.excel.TemplateExportExcel;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author XK
 */
@Service
public class StatisticalCheckOfLocationInformationServiceImpl implements StatisticalCheckOfLocationInformationService {

    @Autowired
    private StatisticalCheckOfLocationInformationDao statisticalLocationInformationDao;

    @Autowired
    private UserService userService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private TemplateExportExcel templateExportExcel;

    @Override
    public Page<OilSubsidyLocationInformationDTO> getListByOrgIdAndTime(OilSubsidyLocationInformationQuery query) {
        List<String> orgIds = Arrays.asList(query.getForwardOrgId().split(","));
        Page<OilSubsidyLocationInformationDTO> informations = PageHelperUtil.doSelect(query, () ->
                statisticalLocationInformationDao.getByOrgIdAndTime(orgIds, query.getStartTime(), query.getEndTime()));
        Set<String> groupIdKeySet = new HashSet<>();
        //组装组织名称信息
        for (OilSubsidyLocationInformationDTO locationInformationDTO : informations) {
            groupIdKeySet.add(locationInformationDTO.getForwardOrgId());
        }
        Map<String, OrganizationLdap> groupInfos = organizationService.getOrgByUuids(groupIdKeySet);
        for (OilSubsidyLocationInformationDTO locationInformationDTO : informations) {
            OrganizationLdap groupInfo = groupInfos.get(locationInformationDTO.getForwardOrgId());
            if (groupInfo != null) {
                locationInformationDTO.setForwardOrgName(groupInfo.getName());
            } else {
                locationInformationDTO.setForwardOrgName("-");
            }
        }

        String userId = userService.getCurrentUserUuid();
        RedisKey redisKey = HistoryRedisKeyEnum.USER_OIL_SUBSIDY_LOCATION_INFORMATION.of(userId);
        if (RedisHelper.isContainsKey(redisKey)) {
            RedisHelper.delete(redisKey);
        }
        if (CollectionUtils.isNotEmpty(informations)) {
            RedisHelper.addToList(redisKey, informations);
        }
        return informations;
    }

    @Override
    public void export(HttpServletResponse res) {
        String userId = userService.getCurrentUserUuid();
        RedisKey redisKey = HistoryRedisKeyEnum.USER_OIL_SUBSIDY_LOCATION_INFORMATION.of(userId);
        List<OilSubsidyLocationInformationDTO> locationInformation = new ArrayList<>();
        if (RedisHelper.isContainsKey(redisKey)) {
            List<String> list = RedisHelper.getList(redisKey);
            locationInformation =
                list.stream().map(o -> JSONObject.parseObject(o, OilSubsidyLocationInformationDTO.class))
                    .collect(Collectors.toList());
        }
        if (locationInformation.size() == 0) {
            return;
        }
        Map<String, Object> data = new HashMap<>(8);
        data.put("locationInformation", locationInformation);
        String fileName = "定位信息统计报表";
        templateExportExcel
            .templateExportExcel(TemplateExportExcel.OIL_SUBSIDIES_LOCATION_INFORMATION_STATISTICAL, res, data,
                fileName);
    }

}

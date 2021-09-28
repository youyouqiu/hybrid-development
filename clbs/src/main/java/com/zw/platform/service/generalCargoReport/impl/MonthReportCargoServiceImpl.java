package com.zw.platform.service.generalCargoReport.impl;

import com.github.pagehelper.Page;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.domain.generalCargoReport.CargoMonthReportInfo;
import com.zw.platform.domain.generalCargoReport.CargoSearchQuery;
import com.zw.platform.repository.modules.CargoDao;
import com.zw.platform.service.generalCargoReport.MonthReportCargoService;
import com.zw.platform.util.PageHelperUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class MonthReportCargoServiceImpl implements MonthReportCargoService {

    @Autowired
    OrganizationService organizationService;

    @Autowired
    CargoDao cargoDao;

    @Override
    public Page<CargoMonthReportInfo> searchMonthData(CargoSearchQuery cargoSearchQuery) throws Exception {
        Set<String> groupIds = getOrgIds(cargoSearchQuery);
        return groupIds.size() == 0
            ? new Page<>(cargoSearchQuery.getPage().intValue(), cargoSearchQuery.getLimit().intValue(), false)
            : PageHelperUtil.doSelect(cargoSearchQuery, () -> cargoDao
            .getMonthData(groupIds, Integer.parseInt(cargoSearchQuery.getTime().replaceAll("-", ""))));
    }

    @Override
    public List<CargoMonthReportInfo> exportSearchMonthData(CargoSearchQuery cargoSpotCheckQuery) throws Exception {
        Set<String> groupIds = getOrgIds(cargoSpotCheckQuery);
        return cargoDao.getMonthData(groupIds, Integer.parseInt(cargoSpotCheckQuery.getTime().replaceAll("-", "")));
    }

    private Set<String> getOrgIds(CargoSearchQuery cargoSpotCheckQuery) {
        Set<String> groupIds = new HashSet<>(Arrays.asList(cargoSpotCheckQuery.getGroupIds().split(",")));
        if (StringUtils.isNotBlank(cargoSpotCheckQuery.getSearch())) {
            List<String> searchId = organizationService.getOrgIdsByOrgName(cargoSpotCheckQuery.getSearch(), null);
            groupIds.retainAll(searchId);
        }
        return groupIds;
    }
}

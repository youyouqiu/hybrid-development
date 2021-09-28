package com.zw.platform.service.oilsubsidy;

import com.github.pagehelper.Page;
import com.zw.platform.domain.oilsubsidy.locationinformation.OilSubsidyLocationInformationDTO;
import com.zw.platform.domain.oilsubsidy.locationinformation.OilSubsidyLocationInformationQuery;

import javax.servlet.http.HttpServletResponse;

/**
 * @author XK
 */
public interface StatisticalCheckOfLocationInformationService {
    Page<OilSubsidyLocationInformationDTO> getListByOrgIdAndTime(OilSubsidyLocationInformationQuery query);

    void export(HttpServletResponse res);
}

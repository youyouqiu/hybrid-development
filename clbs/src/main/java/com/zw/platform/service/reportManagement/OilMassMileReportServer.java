package com.zw.platform.service.reportManagement;

import com.zw.platform.domain.reportManagement.OilMassMile;
import com.zw.platform.dto.reportManagement.OilAmountAndSpillQuery;
import com.zw.platform.util.common.PageGridBean;

import javax.servlet.http.HttpServletResponse;
import java.util.List;


/**
 * 油量里程报表server
 */
public interface OilMassMileReportServer {
    List<OilMassMile> getOilMassMileData(String vehicleIds, String queryStartDate, String queryEndDate)
        throws Exception;

    /**
     * 导出油量里程报表
     */
    void exportOilMassMileData(HttpServletResponse response, String fuzzyParam) throws Exception;

    /**
     * 分页查询加漏油详情数据
     * @param query query
     * @return PageGridBean
     * @throws Exception
     */
    PageGridBean getAmountOrSpillData(OilAmountAndSpillQuery query) throws Exception;
}

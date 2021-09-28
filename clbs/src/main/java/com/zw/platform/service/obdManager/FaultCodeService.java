package com.zw.platform.service.obdManager;

import com.zw.platform.domain.statistic.FaultCodeQuery;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;

import javax.servlet.http.HttpServletResponse;

/**
 * @author zhouzongbo on 2018/12/28 16:36
 */
public interface FaultCodeService {
    /**
     * 查询故障码列表
     * @param query query
     * @return PageGridBean
     */
    PageGridBean getFaultCodeList(FaultCodeQuery query);

    /**
     * 导出
     * @param response response
     */
    void getExportFaultCode(HttpServletResponse response) throws Exception;

    /**
     * @param query
     * @return
     */
    JsonResultBean findExportFaultCode(FaultCodeQuery query);
}

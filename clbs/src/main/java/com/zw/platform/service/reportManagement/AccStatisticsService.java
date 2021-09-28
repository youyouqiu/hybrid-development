package com.zw.platform.service.reportManagement;

import com.zw.platform.dto.reportManagement.AccStatisticsDetailQuery;
import com.zw.platform.dto.reportManagement.AccStatisticsQuery;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;

/**
 * ACC统计报表service
 * @author tianzhangxu
 */
public interface AccStatisticsService {

    /**
     * 分页查询ACC统计报表列表
     * @param query query
     * @return PageGridBean
     * @throws Exception
     */
    PageGridBean getAccStatisticsInfo(AccStatisticsQuery query) throws Exception;

    /**
     * 导出ACC统计报表
     * @param query query
     * @return JsonResultBean
     */
    JsonResultBean exportAccStatisticsInfo(AccStatisticsQuery query);

    /**
     * 分页查询ACC统计报表详情列表
     * @param query query
     * @return PageGridBean
     * @throws Exception
     */
    PageGridBean getAccStatisticsDetailInfo(AccStatisticsDetailQuery query) throws Exception;
}

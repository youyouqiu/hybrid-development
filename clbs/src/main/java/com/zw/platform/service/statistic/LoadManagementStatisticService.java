package com.zw.platform.service.statistic;

import com.zw.platform.domain.statistic.StatisticQuery;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * 载重报表service
 *
 * @author zhouzongbo on 2018/9/10 15:29
 */
public interface LoadManagementStatisticService {

    /**
     * 获取载重图表数据
     *
     * @param query query
     * @return JsonResultBean
     */
    JsonResultBean getLoadChartInfo(StatisticQuery query) throws Exception;

    /**
     * 获取数据载重报表全部数据列表
     *
     * @param query query
     * @return PageGridBean
     */
    PageGridBean getTotalLoadInfoList(StatisticQuery query) throws Exception;

    /**
     * 导出载重数据
     */
    void export(HttpServletResponse response, Integer sensorSequence, Integer status)
        throws IOException;
}

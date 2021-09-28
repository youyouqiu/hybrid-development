package com.zw.adas.service.riskStatistics;

import com.zw.adas.domain.riskStatistics.bean.AdasStatisticsListBean;
import com.zw.adas.domain.riskStatistics.query.EventStatisticsRecordQuery;
import com.zw.adas.domain.riskStatistics.query.RiskStatisticsRecordQuery;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 *
 * @Author zhangqiang
 * @Date 2020/6/18 9:48
 */
public interface AdasRiskStatisticsRecordService {
    Map<String, Object> searchReportInfo(RiskStatisticsRecordQuery query, boolean exportFlag) throws IOException;

    /**
     * 获取数据列表
     * @param query
     * @return
     */
    List<AdasStatisticsListBean> getListData(EventStatisticsRecordQuery query);

    /**
     * 导出列表接口
     * @param query
     * @param response
     * @throws Exception
     */
    void exportData(EventStatisticsRecordQuery query, HttpServletResponse response) throws Exception;
}

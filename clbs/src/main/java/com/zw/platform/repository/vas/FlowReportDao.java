package com.zw.platform.repository.vas;


import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.zw.platform.domain.reportManagement.FlowReport;
import com.zw.platform.domain.reportManagement.query.FlowQuery;


public interface FlowReportDao {
    public List<FlowReport> getFlowReports(@Param("query") FlowQuery query);

}

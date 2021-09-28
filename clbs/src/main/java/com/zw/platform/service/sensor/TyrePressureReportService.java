package com.zw.platform.service.sensor;

import com.zw.platform.domain.statistic.StatisticQuery;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;

import java.io.IOException;

public interface TyrePressureReportService {

    JsonResultBean getTotalInfo(StatisticQuery query) throws Exception;

    JsonResultBean getChartInfo(StatisticQuery query) throws IOException;

    PageGridBean getFormInfo(StatisticQuery query);
}

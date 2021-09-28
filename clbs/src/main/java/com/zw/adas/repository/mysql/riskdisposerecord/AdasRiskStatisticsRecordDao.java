package com.zw.adas.repository.mysql.riskdisposerecord;

import com.zw.adas.domain.riskStatistics.bean.AdasStatisticsListBean;
import com.zw.adas.domain.riskStatistics.query.EventStatisticsRecordQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AdasRiskStatisticsRecordDao {

    List<AdasStatisticsListBean> getListData(@Param("query") EventStatisticsRecordQuery query);
}

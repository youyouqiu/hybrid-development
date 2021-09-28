package com.zw.app.repository.mysql.webMaster.Statistics;

import com.zw.app.domain.webMaster.statistics.StatisticsConfigInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface StatisticsDao {

    //根据组织id获取综合统计配置
    List<StatisticsConfigInfo> getStatistics(@Param("groupId") String groupId,
                                             @Param("groupDefault") int groupDefault);

    List<StatisticsConfigInfo> getStatisticsByVersion(@Param("groupId") String groupId,
                                                      @Param("groupDefault") int groupDefault,
                                                      @Param("appVersion")Integer appVersion);

    //根据组织id删除组织所有数据
    Boolean deleteStatisticsConfig(@Param("groupId") String groupId, @Param("groupDefault") int groupDefault);

    //一次添加多条数据（一个组织的）
    Boolean addStatisticsConfig(@Param("list") List<StatisticsConfigInfo> statisticsConfigInfos);

    String getGroupName(@Param("groupId") String groupId);//查找数据库中是否有该组织的数据，有就获取其组织名称
}

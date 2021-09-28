package com.zw.adas.repository.mysql.driverscore;

import com.zw.adas.domain.driverScore.show.AdasDriverGroupGeneralScoreListShow;
import com.zw.adas.domain.driverScore.show.AdasDriverGroupGeneralScoreShow;
import com.zw.adas.domain.driverScore.show.AdasDriverScoreProfessionalInfoShow;
import com.zw.adas.domain.driverScore.show.query.AdasDriverScoreQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/***
 @Author zhengjc
 @Date 2019/10/19 14:22
 @Description 司机评分
 @version 1.0
 **/
public interface AdasDriverScoreDao {
    /**
     * 获取插卡司机企业评分
     * @param groupId
     * @param time
     * @return
     */
    AdasDriverGroupGeneralScoreShow getGroupDriverGeneralScoreInfos(@Param("groupId") String groupId,
        @Param("time") long time);

    /**
     * 获取插卡司机评分详情
     * @param query
     * @return
     */
    List<AdasDriverGroupGeneralScoreListShow> getGroupDriverGeneralScoreInfoList(
        @Param("query") AdasDriverScoreQuery query);

    /**
     * 根据企业和插卡司机名称获取插卡司机的评分
     * @param query
     * @return
     */
    AdasDriverScoreProfessionalInfoShow getDriverScoreProfessionalInfo(@Param("query") AdasDriverScoreQuery query);

    List<AdasDriverScoreProfessionalInfoShow> getDriverScoreProfessionalInfos(
        @Param("query") AdasDriverScoreQuery query);

    /**
     * 获取当前页导出的数据
     * @param query
     * @return
     */
    List<AdasDriverScoreProfessionalInfoShow> getCurrentPageDriverScoreProfessionalInfos(
        @Param("query") AdasDriverScoreQuery query);

    /**
     * 获取最大得分范围
     * @param groupId
     * @param time
     * @return
     */
    Integer getMaxRangeByGroupIdsAndTime(@Param("groupId") String groupId, @Param("time") long time);
}

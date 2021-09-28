package com.zw.adas.service.driverScore;

import com.zw.adas.domain.driverScore.show.AdasDriverGroupGeneralScoreListShow;
import com.zw.adas.domain.driverScore.show.AdasDriverGroupGeneralScoreShow;
import com.zw.adas.domain.driverScore.show.AdasDriverScoreEventShow;
import com.zw.adas.domain.driverScore.show.AdasDriverScoreProfessionalInfoShow;
import com.zw.adas.domain.driverScore.show.query.AdasDriverScoreQuery;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/***
 @Author zhengjc
 @Date 2019/10/14 19:44
 @Description 司机评分service
 @version 1.0
 **/
public interface AdasDriverScoreService {
    /**
     * 查询司机评分企业综合评分
     * @param groupId
     * @param time
     * @return
     */
    AdasDriverGroupGeneralScoreShow getGroupDriverGeneralScoreInfos(String groupId, long time);

    /**
     * 查询司机评分列表数据
     * @param query
     * @return
     */
    List<AdasDriverGroupGeneralScoreListShow> getGroupDriverGeneralScoreInfoList(AdasDriverScoreQuery query);

    /**
     * 查询司机评分列表弹出框数据
     * @param query
     * @return
     */
    AdasDriverScoreProfessionalInfoShow getDriverScoreProfessionalInfo(AdasDriverScoreQuery query);

    /**
     * 获取弹出对应企业下司机所有报警
     * @param query
     * @return
     */
    List<AdasDriverScoreEventShow> getIcCardDriverEvents(AdasDriverScoreQuery query);

    Map<String, Object> selectIcCardDriverEvents(AdasDriverScoreQuery query);

    Map<String, String> getGroupIdMap();

    void exportIcCardDriverInfoList(AdasDriverScoreQuery adasDriverScoreQuery, HttpServletResponse response);

    void exportDriverScoreProfessionalDetail(AdasDriverScoreQuery query, HttpServletResponse response);

    void exportDriverScoreProfessionalDetails(AdasDriverScoreQuery query, HttpServletResponse response);
}

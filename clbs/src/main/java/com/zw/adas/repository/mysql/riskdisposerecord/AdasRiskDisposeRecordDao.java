package com.zw.adas.repository.mysql.riskdisposerecord;

import com.zw.adas.domain.riskManagement.form.*;
import com.zw.adas.domain.riskManagement.query.AdasRiskDisposeRecordQuery;
import com.zw.platform.domain.basicinfo.ProfessionalsInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


/**
 * 功能描述:
 *
 * @author zhengjc
 * @date 2019/5/30
 * @time 18:09
 */
public interface AdasRiskDisposeRecordDao {
    /**
     * 风险处置记录
     *
     * @param query 查询参数
     * @return
     */
    List<AdasRiskDisposeRecordForm> searchRiskDisposeRecords(@Param("query") AdasRiskDisposeRecordQuery query);

    /**
     * @param query
     * @return
     */
    List<AdasRiskDisposeRecordForm> queryRiskDisposeRecords(@Param("query") AdasRiskDisposeRecordQuery query);

    /**
     * 查询每一个风险的回访次数
     *
     * @param riskId 风险Id
     * @return
     */
    Integer searchVisitTime(@Param("riskId") String riskId);

    /**
     * 查询每一个风险编号对应的回访编号
     *
     * @return
     */
    List<Map<String, String>> searchRiskVisit();

    /**
     * 查询每一个风险Id对应的风险事件信息
     *
     * @param riskId 风险Id
     * @return
     */
    List<AdasRiskEventAlarmForm> searchEventByRiskId(@Param("riskId") String riskId);

    /**
     * 获得风险报告的信息
     */
    List<AdasRiskReportForm> searchRiskReportFormById(String id);

    /**
     * 获取司机信息
     */
    List<ProfessionalsInfo> searchDrivers(@Param("riskId") String riskId);
    // 根据回访id查找司机

    ProfessionalsInfo searchDriver(@Param("id") String id);

    /**
     * 获取处理和回访记录
     */
    List<AdasRiskVisitReportForm> searchRiskVisitForms(@Param("riskId") String riskId);

    List<AdasRiskEventAlarmReportForm> searchEventByRiskId2(String id);

    /**
     * 获取当前riskId或riskEventId下的事件Id和mediaId，vehicleId
     *
     * @param isEvent    是否事件
     * @param downLoadId riskId OR riskEventId
     * @return result
     */
    List<Map<String, String>> findMediaInfo(@Param("isEvent") boolean isEvent, @Param("downLoadId") String downLoadId);

    void updateAddress(@Param("id") String id, @Param("address") String address);

    List<Map<String, String>> findAllRiskCopaign();

    List<Map<String, String>> orderByCampaignRiskType();

    List<String> findTerminalVideo(@Param("isEvent") boolean isEvent, @Param("downLoadId") String downLoadId);

    List<String> findTerminalPic(@Param("isEvent") boolean isEvent, @Param("downLoadId") String downLoadId);

    AdasRiskDisposeRecordForm findRiskByRiskNumber(String riskNumber);
}

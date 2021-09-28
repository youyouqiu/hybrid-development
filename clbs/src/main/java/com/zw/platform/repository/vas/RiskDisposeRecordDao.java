package com.zw.platform.repository.vas;


import com.zw.platform.domain.basicinfo.ProfessionalsInfo;
import com.zw.platform.domain.riskManagement.form.RiskDisposeRecordForm;
import com.zw.platform.domain.riskManagement.form.RiskEventAlarmForm;
import com.zw.platform.domain.riskManagement.form.RiskEventAlarmReportForm;
import com.zw.platform.domain.riskManagement.form.RiskReportForm;
import com.zw.platform.domain.riskManagement.form.RiskVisitReportForm;
import com.zw.platform.domain.riskManagement.query.RiskDisposeRecordQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


/**
 * Created by PengFeng on 2017/8/23 10:59
 */
public interface RiskDisposeRecordDao {
    /**
     * 风险处置记录
     * @param query 查询参数
     * @return
     */
    List<RiskDisposeRecordForm> searchRiskDisposeRecords(@Param("query") RiskDisposeRecordQuery query);

    /**
     *
     * @param query
     * @return
     */
    List<RiskDisposeRecordForm> queryRiskDisposeRecords(@Param("query") RiskDisposeRecordQuery query);

    /**
     * 查询每一个风险的回访次数
     * @param riskId 风险Id
     * @return
     */
    Integer searchVisitTime(@Param("riskId") String riskId);

    /**
     * 查询每一个风险编号对应的回访编号
     * @return
     */
    List<Map<String, String>> searchRiskVisit();

    /**
     * 查询每一个风险Id对应的风险事件信息
     * @param riskId 风险Id
     * @return
     */
    List<RiskEventAlarmForm> searchEventByRiskId(@Param("riskId") String riskId);

    /**
     * 获得风险报告的信息
     */
    List<RiskReportForm> searchRiskReportFormById(String id);

    /**
     * 获取司机信息
     */
    List<ProfessionalsInfo> searchDrivers(@Param("riskId") String riskId);
    // 根据回访id查找司机

    ProfessionalsInfo searchDriver(@Param("id") String id);

    /**
     * 获取处理和回访记录
     */
    List<RiskVisitReportForm> searchRiskVisitForms(@Param("riskId") String riskId);

    List<RiskEventAlarmReportForm> searchEventByRiskId2(String id);

    /**
     * 获取当前riskId或riskEventId下的事件Id和mediaId，vehicleId
     * @param isEvent 是否事件
     * @param downLoadId riskId OR riskEventId
     * @return result
     */
    List<Map<String, String>> findMediaInfo(@Param("isEvent") boolean isEvent, @Param("downLoadId") String downLoadId);

    void updateAddress(@Param("id") String id, @Param("address") String address);

    List<Map<String, String>> findAllRiskCopaign();

    List<Map<String,String>> orderByCampaignRiskType();

    List<String> findTerminalVideo(@Param("isEvent") boolean isEvent, @Param("downLoadId") String downLoadId);

    List<String> findTerminalPic(@Param("isEvent") boolean isEvent, @Param("downLoadId") String downLoadId);

    RiskDisposeRecordForm findRiskByRiskNumber(String riskNumber);
}

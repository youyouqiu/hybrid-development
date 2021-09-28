package com.zw.platform.repository.vas;


import com.zw.platform.domain.basicinfo.ProfessionalsInfo;
import com.zw.platform.domain.multimedia.Media;
import com.zw.platform.domain.multimedia.form.MediaForm;
import com.zw.platform.domain.riskManagement.form.RiskCampaignForm;
import com.zw.platform.domain.riskManagement.form.RiskVisitForm;
import com.zw.platform.domain.riskManagement.query.RiskCampaignQuery;
import com.zw.platform.domain.riskManagement.query.RiskEventQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 风险表 @author  Tdz
 * @create 2017-08-22 11:22
 **/
public interface RiskCampaignDao {
    /**
     * 查询
     */
    List<RiskCampaignQuery> find(@Param("userId") String userId, @Param("groupList") List<String> groupList);

    List<RiskCampaignQuery> getAllRiskList(@Param("status") int status);

    List<RiskCampaignQuery> getRiskList(@Param("list")List<String> riskIds);

    RiskCampaignQuery getRiskByNum(@Param("num") String num);

    List<ProfessionalsInfo> getProfessionalsInfo(@Param("cid") String cid);

    List<Media> getMediaList(@Param("mediaIds")String [] fid, @Param("vid") String vid);

    Media getMediaListbyEventId(@Param("riskEventId") String riskEventId);

    List<RiskEventQuery> getRiskEventByNum(@Param("num") String num);

    boolean updateRiskType(@Param("type") Integer type, @Param("num") String num);

    boolean updateRisk(RiskCampaignForm riskCampaignForm);

    boolean dealRisk(RiskVisitForm riskVisitForm);

    boolean updateDealRisk(@Param("record") RiskVisitForm riskVisitForm);

    Integer cheakDealRisk(@Param("num") String riskId);

    List<RiskVisitForm> getDealRisk(@Param("num") String riskId);

    List<MediaForm> getMedas(@Param("vid") String vid);

    Map<String, String> findAlarmIdByTimeAndCode(@Param("time") String time, @Param("eventCode") String eventCode,
        @Param("vehicleId") String vehicleId);

    String queryMediaIdById(String id);

    List<String> getRiskEventIds(@Param("vehicleId") String vehicleId, @Param("mediaId") String mediaId);

    //通过risk_id 获取 risk_event_id
    List<String> queryRiskEventIds(String riskId);

    List<RiskEventQuery> getRiskEventTypes();

    List<String> getRiskLevels();
}

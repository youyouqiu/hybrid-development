package com.zw.adas.service.supersive;

import com.zw.adas.domain.leardboard.AdasAlarmAnalysisData;
import com.zw.adas.domain.leardboard.AdasAlarmTimesData;
import com.zw.adas.domain.riskManagement.param.AdasRiskBattleParam;
import com.zw.adas.domain.riskManagement.show.AdasDriverShow;
import com.zw.adas.domain.riskManagement.show.AdasEventShow;
import com.zw.adas.domain.riskManagement.show.AdasRiskShow;
import com.zw.platform.domain.multimedia.Photograph;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface AdasRiskManageIntelligenceService {
    /**
     * 获取当前报警分析数据
     * 每个风险类型和所对应的风险报警总数量、未处理数量、已处理数量，以及总数量、和总的未处理数量和总的已处理数量
     * @return AdasAlarmAnalysisData
     * @throws IOException e
     */
    AdasAlarmAnalysisData getAlarmAnalysisData()
        throws IOException;

    List<AdasAlarmTimesData> getAlarmTimesData()
        throws Exception;

    /**
     * 主动安全
     */
    List<AdasRiskShow> getRisks(AdasRiskBattleParam rbp);

    /**
     * 根据风险id查询风险事件信息
     */
    List<AdasEventShow> getEvents(String riskId);

    /**
     * 根据风险id查询司机
     */
    Map<String, List<AdasDriverShow>> getDrivers(String riskId, String vehicleId);

    void setPhotoParam(Photograph photograph);

    Photograph getPhotoParam(String userName);
}

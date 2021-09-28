package com.zw.adas.service.realTimeMonitoring;

import com.zw.adas.domain.driverStatistics.bean.AdasFaceCheckAuto;
import com.zw.adas.domain.driverStatistics.show.AdasProfessionalShow;
import com.zw.adas.domain.riskManagement.AdasRiskItem;
import com.zw.adas.domain.riskManagement.form.AdasRiskEventAlarmForm;
import com.zw.platform.domain.multimedia.Photograph;
import com.zw.platform.util.common.JsonResultBean;

import java.util.List;
import java.util.Map;

public interface AdasRealTimeMonitoringService {

    JsonResultBean getMediaInfo(String riskId) throws Exception;

    List<AdasRiskItem> listRisks(int pageNum, int pageSize, String riskIdStr, String eventField);

    Map<String, Long> getEventCountByEventFields(String eventFields);

    List<AdasRiskEventAlarmForm> getRiskEvents(String riskId);

    /**
     * 手动下发9208补传
     */
    JsonResultBean send9208(String riskEventId, String vehicleId) throws Exception;

    /**
     * 人证比对接口下发8801
     */
    JsonResultBean send8801(String vehicleId, Photograph photograph) throws Exception;

    /**
     * 检查ic卡照片
     */
    Boolean checkIcPhoto(String vehicleId, String icMediaUrl);

    /**
     * 百度人脸比对
     */
    JsonResultBean faceMatch(String vehicleId, String address, String mediaUrl, String icMediaUrl);

    /**
     * 获取从插卡从业人员信息
     * @param identity 从业资格证号
     * @param name       从业人员名称
     */
    AdasProfessionalShow getAdasProfessionalByIdentityAndName(String identity, String name);

    Map<String, String> getEventMaps();

    AdasProfessionalShow getAdasProfessionalShow(String professionalId);

    void sendFaceCheckAuto(String vehicleId, AdasFaceCheckAuto faceCheckAuto);
}

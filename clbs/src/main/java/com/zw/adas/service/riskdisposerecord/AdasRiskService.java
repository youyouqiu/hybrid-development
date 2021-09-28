package com.zw.adas.service.riskdisposerecord;

import com.alibaba.fastjson.JSONObject;
import com.zw.adas.domain.riskManagement.AdasAlarmDealInfo;
import com.zw.adas.domain.riskManagement.AdasRiskItem;
import com.zw.adas.domain.riskManagement.form.AdasDealRiskForm;
import com.zw.adas.domain.riskManagement.form.AdasEventForm;
import com.zw.adas.domain.riskManagement.form.AdasRiskDisposeRecordForm;
import com.zw.adas.domain.riskManagement.form.AdasRiskReportForm;
import com.zw.adas.domain.riskManagement.query.AdasRiskDisposeRecordQuery;
import com.zw.adas.domain.riskManagement.show.AdasMediaShow;
import com.zw.adas.domain.riskManagement.show.AdasRiskEventAlarmShow;
import com.zw.platform.util.common.PageGridBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 功能描述:
 * @author zhengjc
 * @date 2019/5/30
 * @time 18:04
 */
public interface AdasRiskService {
    /**
     * 获取风险报告列表
     * @param riskIds 查询参数
     * @return result
     */
    List<AdasRiskDisposeRecordForm> getRiskDisposeRecordsByIds(List<String> riskIds, String status);

    List<AdasRiskEventAlarmShow> searchRiskEvents(String riskId, String vehicleId);

    List<AdasRiskDisposeRecordForm> getExportData(List<String> eventIds);

    AdasRiskReportForm searchRiskReportFormById(byte[] riskId, String contextPath);

    JSONObject hasRiskEvidence(String riskId, String riskNumber);

    JSONObject downloadDeviceEvidence(String downLoadId, boolean isEvent, String numbe);



    void exportDocByBatch(AdasRiskDisposeRecordQuery query, HttpServletResponse response, HttpServletRequest request)
        throws Exception;

    /**
     * 风险处置记录列表查询接口
     * @param query
     * @return
     */
    PageGridBean getPageGridBean(AdasRiskDisposeRecordQuery query) throws Exception;

    AdasRiskDisposeRecordForm getRiskDisposeRecordsById(byte[] riskId);

    /**
     * 是否能够下载文件
     * @param mediaUrl
     * @return
     */
    boolean canDownload(String mediaUrl);

    /**
     * 获取多媒体信息
     * @param riskId    风险id
     * @param mediaType 多媒体类型 0代表终端图片  2代表终端视频
     * @return
     */
    List<AdasMediaShow> getMedias(String riskId, int mediaType);

    /**
     * 获取多媒体信息
     * @param eventId   事件id
     * @param mediaType 多媒体类型 0代表终端图片  2代表终端视频
     * @return
     */
    List<AdasMediaShow> getEventMedias(String eventId, int mediaType);

    /**
     * 风险处置记录模块导出
     * @param query
     * @param response
     * @param request
     */
    void addLogAndExportRiskDisposeRecord(AdasRiskDisposeRecordQuery query, HttpServletResponse response,
        HttpServletRequest request) throws Exception;

    void downLoadFileByPath(HttpServletResponse response, String filePath, boolean isRiskEvidence, String fileName);

    String getRiskStatus(String riskId);

    void updateRiskEvents(List<AdasEventForm> riskEvents);

    boolean saveRiskDealInfo(AdasDealRiskForm adasDealRiskForm) throws Exception;

    void exportDoc(HttpServletResponse response, HttpServletRequest request, String riskId, String riskNumber);

    List<AdasRiskItem> getRiskList(List<String> riskIds);

    boolean saveRiskDealInfos(AdasAlarmDealInfo adasAlarmDealInfo) throws IOException;

    /**
     * 获取commonName和对应functionId
     * @return map
     * @throws Exception
     */
    Map<String, Object> getNameAndFunctionIds() throws Exception;


    /**
     * 检查模糊搜索的车是否勾选的车中
     * @param query
     * @return
     */
    boolean checkBrandInSelected(AdasRiskDisposeRecordQuery query);
}

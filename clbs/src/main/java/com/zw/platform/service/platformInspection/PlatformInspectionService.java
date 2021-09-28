package com.zw.platform.service.platformInspection;

import java.io.IOException;
import java.util.List;

import com.github.pagehelper.Page;
import com.zw.adas.domain.platforminspection.PlatformInspectionDTO;
import com.zw.adas.domain.platforminspection.PlatformInspectionQuery;
import com.zw.platform.dto.platformInspection.PlatformInspectionParamDTO;
import com.zw.platform.dto.platformInspection.PlatformInspectionResultDTO;

import javax.servlet.http.HttpServletResponse;

/**
 * 平台巡检
 * @date
 * @author lijie
 */
public interface PlatformInspectionService {

    /**
     * 模糊查询
     * @param query
     * @return
     */
    Page<PlatformInspectionDTO> getListByKeyword(PlatformInspectionQuery query);

    /**
     * 驾驶员身份识别
     * @param platformInspectionParam
     * @return
     */
    boolean sendDriverIdentify(PlatformInspectionParamDTO platformInspectionParam, String sessionId, String ip);

    /**
     * 状态推送
     * @param status
     * @param sessionId
     * @param vehicleId
     */
    void sendInspectionStatus(Integer status, String sessionId, String vehicleId, Integer inspectionType, String brand);

    /**
     * 平台巡检
     * @param platformInspectionParams
     * @param sessionId
     * @return
     */
    boolean sendPlatformInspection(List<PlatformInspectionParamDTO> platformInspectionParams, String sessionId);

    /**
     * 导出
     * @param query
     * @param response
     * @return
     */
    boolean export(PlatformInspectionQuery query, HttpServletResponse response) throws IOException;

    /**
     * 获取巡检结果
     * @param id
     * @return
     */
    PlatformInspectionResultDTO getInspectionResult(String id, Integer inspectionType);

}

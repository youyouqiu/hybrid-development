package com.zw.platform.util.report;

import com.zw.platform.commons.UrlConvert;
import org.springframework.http.HttpMethod;

import java.util.HashMap;
import java.util.Map;

/**
 * pass cloud url 枚举类(现在的url太多了，以后会更多)
 * @author create by lijie on 2020/9/15.
 */
public enum PaasCloudAdasUrlEnum implements UrlConvert {
    /**
     * 主动安全处置报表
     * 查询风险列表
     */
    ADAS_REPORT_RISK_LIST("/adas/report/riskList", HttpMethod.POST),

    /**
     * 主动安全处置报表
     * 查询风险列表
     */
    ADAS_REPORT_EVENT_LIST("/adas/report/eventList", HttpMethod.POST),

    /**
     * 主动安全处置报表
     * 获取导出风险事件列表
     */
    ADAS_REPORT_EXPORT_EVENT("/adas/report/getExportData", HttpMethod.POST),

    /**
     * 主动安全处置报表
     * 根据风险id获取风险
     */
    SEARCH_RISK_REPORT_FORM_BY_ID("/adas/report/searchRiskReportFormById", HttpMethod.POST),

    /**
     * 主动安全处置报表
     * 获取风险的证据地址
     */
    GET_RISK_EVIDENCE_URL("/adas/report/getRiskEvidenceUrl", HttpMethod.POST),

    /**
     * 主动安全处置报表
     * 根据mediaId获取media信息
     */
    GET_MEDIA_BY_IDS("/adas/report/getMediaByIds", HttpMethod.POST),

    /**
     * 主动安全处置报表
     * 获取风险报告by 风险ids
     */
    GET_RISK_REPORTS_BY_IDS("/adas/report/getRiskReportsByIds", HttpMethod.POST),

    /**
     * 主动安全处置报表
     * 获取风险的状态
     */
    GET_RISK_STATUS("/adas/report/getRiskStatus", HttpMethod.POST),

    /**
     * 主动安全处置报表
     * 根据id获取多媒体信息
     */
    GET_MEDIA_LIST("/adas/report/getMediaList", HttpMethod.POST),

    /**
     * 主动安全处置报表
     * 根据id获取风险信息
     */
    GET_RISK_DISPOSE_RECORDS_BY_ID("/adas/report/getRiskDisposeRecordsById", HttpMethod.POST),

    /**
     * 主动安全处置报表
     * 修改事件的下发9028的状态
     */
    UPDATE_EVENT_ATTACHMENT_STATUS("/adas/report/updateEventAttachmentStatus", HttpMethod.POST),


    /**
     * 获取ic卡司机信息
     */
    GET_IC_CARD_DRIVER_LIST("/adas/icCardDriver/getDriverInfoList", HttpMethod.POST),
    /**
     * 查询单个ic卡司机信息（原生api方式）
     */
    GET_IC_CARD_DRIVER_STATICS_LIST("/adas/icCardDriver/getDriverStaticsDataList", HttpMethod.POST),
    /**
     * 通过id查询风险事件详情信息（原生api方式）
     */
    GET_IC_CARD_DRIVER_EVENT_LIST("/adas/driverScore/getDriverEventByIds", HttpMethod.POST),

    /**
     * 风险证据库
     * 根据id获取多媒体信息
     */
    GET_EVIDENCE_MEDIA_LIST("/adas/riskEvidence/getEvidenceMediaList", HttpMethod.POST),

    /**
     * 风险证据库
     * 根据id获取多媒体信息
     */
    GET_EVIDENCE_BY_RISK_EVENT_ID("/adas/riskEvidence/getEvidenceByRiskEventId", HttpMethod.POST),

    /**
     * 风险证据库
     * 根据媒体ids获取媒体信息
     */
    QUERY_EVIDENCE_MEDIA_INFO("/adas/riskEvidence/queryEvidenceMediaInfo", HttpMethod.POST),


    /**
     *风险监管
     *事件id获取报警事件
     */
    GET_RISK_LIST("/riskManage/intelligence/riskList/v1.1", HttpMethod.POST),

    /**
     * 风险监管
     * 存储风险事件处理结果
     */
    SAVE_RISK_EVENT_DEAL_INFO("/riskManage/intelligence/save/risk/event/deal/infos", HttpMethod.POST),

    /**
     * 风险监管
     * 存储风险处理结果
     */
    SAVE_RISK_DEAL_INFO("/riskManage/intelligence/save/risk/deal/infos", HttpMethod.POST),

    /**
     * 风险监管
     * 保存风险视频信息
     */
    SAVE_RISK_MEDIA_DEAL_INFO("/riskManage/intelligence/save/risk/media/deal/infos", HttpMethod.POST),


    /**
     *监控对象评分
     *通过企业id获取对应的监控对象评分数据
     */
    GET_MONITOR_SCORE_RISK_EVENT_FROM_HBASE("/monitor/score/select/riskEvent/from/hbase", HttpMethod.POST),

    /**
     *监控对象评分
     *通过车辆id集合和时间获取对应的监控对象评分
     */
    GET_VEHICLE_ONLINE_NUM("/adas/lbOrg/show/get/vehicle/onlineNum", HttpMethod.POST),



    ;

    /**
     * uri
     * 如: /positional/travel/report
     */
    private final String path;
    /**
     * 请求方法
     */
    private final HttpMethod httpMethod;

    PaasCloudAdasUrlEnum(String path, HttpMethod httpMethod) {
        this.path = path;
        this.httpMethod = httpMethod;
    }

    /**
     * path cloud api pair
     */
    private static final Map<String, String> API_URL = new HashMap<>(values().length);

    /**
     * 聚合address + path
     * @param address address
     */
    public static void assembleUrl(String address) {
        for (PaasCloudAdasUrlEnum value : values()) {
            API_URL.put(value.name(), address + value.getPath());
        }
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getUrl() {
        return API_URL.get(this.name());
    }

    @Override
    public HttpMethod getHttpMethod() {
        return httpMethod;
    }
}

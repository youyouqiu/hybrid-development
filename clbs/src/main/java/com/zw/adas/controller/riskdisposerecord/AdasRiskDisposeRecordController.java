package com.zw.adas.controller.riskdisposerecord;

import com.zw.adas.domain.riskManagement.form.AdasDealRiskForm;
import com.zw.adas.domain.riskManagement.form.AdasRiskReportForm;
import com.zw.adas.domain.riskManagement.query.AdasRiskDisposeRecordQuery;
import com.zw.adas.service.elasticsearch.AdasElasticSearchService;
import com.zw.adas.service.riskdisposerecord.AdasRiskService;
import com.zw.adas.utils.controller.AdasControllerTemplate;
import com.zw.platform.basic.util.RedisServiceUtils;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.UuidUtils;
import com.zw.talkback.common.ControllerTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * 功能描述:
 * @author zhengjc
 * @date 2019/5/30
 * @time 17:08
 */
@Controller
@RequestMapping(value = "/r/riskManagement/disposeReport")
public class AdasRiskDisposeRecordController {
    private static final Logger log = LogManager.getLogger(AdasRiskDisposeRecordController.class);

    private static final String LIST_PAGE = "modules/reportManagement/riskDisposeRecord";

    private static String DETAILS_PAGE = "modules/reportManagement/riskReportDetails";

    @Autowired
    private AdasRiskService adasRiskService;

    @Autowired
    private AdasElasticSearchService adasEsService;

    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String getListPage() {
        return LIST_PAGE;
    }

    /**
     * 风险处置记录分页查询
     * @param query 查询参数
     * @return 分页数据
     */
    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean queryRiskEvidenceFromHb(AdasRiskDisposeRecordQuery query) {
        return ControllerTemplate.execute(() -> adasRiskService.getPageGridBean(query), "获取风险处置记录模块失败", null);

    }

    /**
     * 风险对应风险事件查询
     * @param riskId 风险Id
     * @return 风险事件
     */
    @RequestMapping(value = { "/eventList" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean eventList(String riskId, String vehicleId) {
        return ControllerTemplate
            .getResultBean(() -> adasRiskService.searchRiskEvents(riskId, vehicleId), "获取Id为" + riskId + "的风险事件异常！");
    }

    /**
     * 导出(存数据)，风险处置记录
     * @param query    封装查询的条件
     * @param response response
     */
    @RequestMapping(value = "/export", method = RequestMethod.POST)
    @ResponseBody
    public void export(AdasRiskDisposeRecordQuery query, HttpServletResponse response, HttpServletRequest request) {

        ControllerTemplate
            .execute(() -> adasRiskService.addLogAndExportRiskDisposeRecord(query, response, request), "风险处置记录模块导出异常");

    }

    @RequestMapping(value = "/getExportEventSize", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getExportEventSize(AdasRiskDisposeRecordQuery query) {
        return AdasControllerTemplate
            .getResultBean(() -> adasEsService.esQueryExportRiskEventId(query).size(), "获取风险处置记录条数异常");
    }

    /**
     * 导出风险报告doc
     * @param response response
     * @param request  request
     * @param riskId   风险Id
     */
    @RequestMapping(value = "/exportDoc", method = RequestMethod.GET)
    @ResponseBody
    public void exportDoc(HttpServletResponse response, HttpServletRequest request, String riskId,
        String riskNumber) {
        ControllerTemplate.execute(() -> adasRiskService.exportDoc(response, request, riskId, riskNumber), "导出风控报告异常");
    }

    /**
     * 获取风控证据信息
     * @param riskId 风险Id
     * @return 风控信息
     */
    @RequestMapping(value = { "/riskEvidence" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean riskEvidence(String riskId, String riskNumber) {
        return ControllerTemplate.getResultBean(() -> adasRiskService.hasRiskEvidence(riskId, riskNumber), "导出风控证据异常");
    }

    /**
     * 下载终端证据
     * @param downLoadId riskId OR riskEventId
     * @param isEvent    是否事件
     * @return result
     */
    @RequestMapping(value = { "/terminalEvidence" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean terminalEvidence(String downLoadId, boolean isEvent, String number) {
        return ControllerTemplate
            .getResultBean(() -> adasRiskService.downloadDeviceEvidence(downLoadId, isEvent, number), "下载终端证据异常");
    }

    /**
     * 下载文件
     * @param response       response
     * @param isRiskEvidence 是否风控证据
     * @param filePath       文件路径
     * @param fileName       文件名称
     */
    @RequestMapping(value = { "/downloadFile" }, method = RequestMethod.GET)
    public void downloadFile(HttpServletResponse response, String filePath, String fileName, boolean isRiskEvidence) {
        ControllerTemplate
            .execute(() -> adasRiskService.downLoadFileByPath(response, filePath, isRiskEvidence, fileName),
                "导出多媒体文件异常");
    }

    /**
     * 风险报告详情
     */
    @RequestMapping(value = { "/getRiskReportDetails_{riskId}.gsp" }, method = RequestMethod.GET)
    @ResponseBody
    public ModelAndView getRiskReportDetails(@PathVariable final String riskId) {
        return ControllerTemplate.editPage(DETAILS_PAGE, () -> getDataMap(riskId));
    }

    public Map<String, String> getDataMap(String riskId) {
        Map<String, String> result = new HashMap<>();
        result.put("riskId", riskId);
        return result;
    }

    @RequestMapping(value = { "/getRiskReportDetails" }, method = RequestMethod.POST)
    @ResponseBody
    public AdasRiskReportForm getRiskReportDetailsInfo(String riskId) {
        AdasRiskReportForm riskReportFormDetail =
            adasRiskService.searchRiskReportFormById(UuidUtils.getBytesFromStr(riskId), null);
        return riskReportFormDetail;
    }

    @RequestMapping(value = { "/exportDocByBatch" }, method = RequestMethod.POST)
    @ResponseBody
    public void exportDocByBatch(AdasRiskDisposeRecordQuery query, HttpServletResponse response,
        HttpServletRequest request) {
        ControllerTemplate.execute(() -> adasRiskService.exportDocByBatch(query, response, request), "批量导出风险报告word异常");
    }

    @RequestMapping(value = { "/dealRisk" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean dealRisk(AdasDealRiskForm adasDealRiskForm) {

        try {
            if (RedisServiceUtils.lockRisk(adasDealRiskForm.getRiskId())) {
                String nowStatus = adasRiskService.getRiskStatus(adasDealRiskForm.getRiskId());
                if (nowStatus == null || !nowStatus.equals("6")) {
                    boolean flag = adasRiskService.saveRiskDealInfo(adasDealRiskForm);
                    return new JsonResultBean(flag);
                } else {
                    RedisServiceUtils.releaseRiskLock(adasDealRiskForm.getRiskId());
                }
            }
            return new JsonResultBean(JsonResultBean.FAULT, "不好意思,该报警已被处理了");
        } catch (Exception e) {
            log.error("实时监控页面处理风险异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, "实时监控页面处理风险异常!");
        }
    }

    @RequestMapping(value = "/canDownload", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean canDownload(String mediaUrl) {
        return ControllerTemplate.getResultBean(() -> adasRiskService.canDownload(mediaUrl), "能否进行多媒体文件下载接口异常");
    }

    /**
     * 获取终端多媒体接口
     */
    @RequestMapping(value = { "/getRiskMedia" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getRiskMedia(String riskId, int mediaType) {
        return ControllerTemplate.getResultBean(() -> adasRiskService.getMedias(riskId, mediaType), "查询风险终端多媒体接口报错了！");

    }

    /**
     * 获取终端多媒体接口
     */
    @RequestMapping(value = { "/getEventMedia" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getEventMedia(String eventId, int mediaType) {
        return ControllerTemplate
            .getResultBean(() -> adasRiskService.getEventMedias(eventId, mediaType), "查询事件终端多媒体接口报错了！");

    }

    @RequestMapping("/testGetRiskEventEsBeanByRiskIds")
    @ResponseBody
    public JsonResultBean downloadFileFromFastDfs(String riskIds) {
        return new JsonResultBean(adasEsService.getEventEsBeanByRiskId(riskIds.split(",")));
    }

}
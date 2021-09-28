package com.zw.adas.controller.riskEvidence;

import com.github.pagehelper.Page;
import com.google.common.collect.Sets;
import com.zw.adas.domain.riskManagement.form.AdasRiskDisposeRecordForm;
import com.zw.adas.domain.riskManagement.query.AdasRiskDisposeRecordQuery;
import com.zw.adas.service.elasticsearch.AdasElasticSearchService;
import com.zw.adas.service.riskEvidence.AdasRiskEvidenceService;
import com.zw.adas.service.riskdisposerecord.AdasRiskService;
import com.zw.platform.commons.Auth;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/adas/r/reportManagement/adasRiskEvidence")
public class AdasRiskEvidenceController {

    private static final Logger log = LogManager.getLogger(AdasRiskEvidenceController.class);

    private static final String LIST_PAGE = "modules/reportManagement/adasRiskEvidence";

    //只能一个用户风控证据库下载
    private static int count;

    @Autowired
    private AdasRiskEvidenceService adasRiskEvidenceService;

    @Autowired
    private LogSearchService logSearchService;

    @Autowired
    private AdasElasticSearchService esService;

    @Autowired
    private AdasRiskService adasRiskService;

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String getListPage() {
        return LIST_PAGE;
    }

    /**
     * 查询
     */
    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean queryRiskEvidenceFromHb(AdasRiskDisposeRecordQuery query) {

        try {
            long l = System.currentTimeMillis();
            if (StringUtil.isNullOrBlank(query.getVehicleIds())) {
                return new PageGridBean(new Page<AdasRiskDisposeRecordForm>());
            }
            query.setLimit(10L);
            // 判断是否输入车牌进行查询
            if (!adasRiskService.checkBrandInSelected(query)) {
                return new PageGridBean(new Page<AdasRiskDisposeRecordForm>(), true, "组织中未勾选查询的车!");
            }
            Page<AdasRiskDisposeRecordForm> result =
                new Page<>(query.getPage().intValue(), query.getLimit().intValue(), false);
            Map<String, Object> map = adasRiskEvidenceService.queryRiskEvidenceFromHb(query, false);
            // 设置数据dataList
            result.addAll((List<AdasRiskDisposeRecordForm>) map.get("list"));
            // 设置总数
            result.setTotal((long) map.get("total"));
            // 设置上一页,下一页游标
            log.info("风控证据库查询" + map.get("total") + "条记录用时:" + (System.currentTimeMillis() - l) + "毫秒");
            return new PageGridBean(result, (Object[]) map.get("search_after"));

        } catch (Exception e) {
            log.error("获取分控证据库记录（queryRiskRecords）失败！", e);
            return new PageGridBean(false);
        }
    }

    /**
     * 要改造一下
     * 根据索引的名称和id查询风险信息
     */
    @RequestMapping(value = { "/queryRiskInfo" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean queryRiskInfo(String id, String type) {

        try {
            AdasRiskDisposeRecordForm riskDisposeRecordForm = esService.esQueryInfoById(id, type);
            return new JsonResultBean(riskDisposeRecordForm);
        } catch (Exception e) {
            log.error("按条件查询风险信息失败！", e);
            return new JsonResultBean(false);
        }
    }

    /**
     * 从HBase查询所有车牌
     * @param query query
     * @return json
     */
    @RequestMapping(value = { "/queryBrands" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean queryBrandsFromHb(AdasRiskDisposeRecordQuery query) {

        try {
            if (!StringUtil.isNullOrBlank(query.getVehicleIds())) {
                Set<String> brands = Sets.newHashSet();
                if (StringUtils.isEmpty(query.getBrand())) {
                    brands = adasRiskEvidenceService.queryBrandsFromHb(query);
                } else {
                    brands.add(query.getBrand());
                }
                return new JsonResultBean(brands);
            }
            return new JsonResultBean(false, "请选择组织下的车辆");
        } catch (Exception e) {
            log.error("按条件查询风险证据库的所有车牌失败！", e);
            return new JsonResultBean(false);
        }
    }

    /**
     * 批量下载
     */
    @RequestMapping(value = { "/downloadBatch" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean downloadRiskEvidenceBatch(AdasRiskDisposeRecordQuery query, HttpServletResponse response,
        HttpServletRequest request) {

        if (!StringUtil.isNullOrBlank(query.getVehicleIds())) {
            try {
                if (count != 0) {
                    return new JsonResultBean(false, "当前已有用户正在导出风控证据库,请稍后!");
                }
                count++;
                adasRiskEvidenceService.export(response,
                    (List<AdasRiskDisposeRecordForm>) adasRiskEvidenceService.queryRiskEvidenceFromHb(query, true)
                        .get("list"), query.getEvidenceType());
                addLog(query, request);
            } catch (Exception e) {
                log.error("批量下载分控证据库记录（queryRiskRecords）失败！", e);
                return new JsonResultBean(false);
            } finally {
                count = 0;
            }
        }
        return new JsonResultBean(true);
    }

    /**
     * post下载过度接口
     */
    @RequestMapping(value = { "/face" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean face() {
        return new JsonResultBean(true);
    }

    private void addLog(AdasRiskDisposeRecordQuery query, HttpServletRequest request) {
        String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
        String currentUsername = SystemHelper.getCurrentUsername();
        String msg = "用户：" + currentUsername + "下载" + query.getStartTime() + "到" + query.getEndTime() + "时间段的风控证据";
        logSearchService.addLog(ip, msg, "3", "风控证据库下载");
    }

    @RequestMapping(value = { "/addRiskIds" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean addRiskIds() {
        try {
            adasRiskEvidenceService.updateRiskIds();
            return new PageGridBean(true);
        } catch (Exception e) {
            log.error("设置zw_m_media表的riskId失败！", e);
            return new PageGridBean(false);
        }

    }

    @RequestMapping(value = { "/addMediaIds" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean addMediaIds() {
        try {
            adasRiskEvidenceService.updateMediaIds();
            return new PageGridBean(true);
        } catch (Exception e) {
            log.error("设置zw_m_media表的MediaId失败！", e);
            return new PageGridBean(false);
        }

    }

    @RequestMapping(value = { "/addRiskEventIds" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean addRiskEventIds() {
        try {
            adasRiskEvidenceService.updateRiskEventIds();
            return new PageGridBean(true);
        } catch (Exception e) {
            log.error("设置zw_m_media表的riskEventId失败！", e);
            return new PageGridBean(false);
        }
    }

    @RequestMapping(value = { "/updateRiskEvidenceNameOnFtp" })
    @ResponseBody
    public JsonResultBean updateRiskEvidenceNameOnFtp() {
        try {
            return new JsonResultBean(adasRiskEvidenceService.updateRiskEvidenceNameOnFtp());
        } catch (Exception e) {
            log.error("更新ftp上面的文件名称失败！", e);
            return new JsonResultBean(false);
        }

    }

    @RequestMapping(value = "/canDownload", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean canDownload(String id, boolean isJpeg) {
        return new JsonResultBean(adasRiskEvidenceService.canDownload(id, isJpeg));
    }

    @RequestMapping(value = "/test")
    @ResponseBody
    public JsonResultBean test(String sql) {

        try {
            String currentUsername = SystemHelper.getCurrentUsername();
            if (currentUsername.equals("wanxing") || currentUsername.equals("zhengjc") || currentUsername.equals("feng")
                || currentUsername.equals("fanlu")) {
                return new JsonResultBean(esService.test(sql));
            }
            return new JsonResultBean(false);
        } catch (Exception e) {
            log.error("es 查询接口调用失败", e);
            return new JsonResultBean(false, e.getMessage());
        }
    }

    @RequestMapping(value = { "/getFunctionIdAndType" }, method = RequestMethod.GET)
    @ResponseBody
    public JsonResultBean getFunctionIdAndType() {
        try {
            return new JsonResultBean(adasRiskService.getNameAndFunctionIds());
        } catch (Exception e) {
            log.error("获取functionIds失败", e);
            return new JsonResultBean(false);
        }
    }
}



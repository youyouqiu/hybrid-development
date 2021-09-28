package com.zw.adas.service.riskdisposerecord.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.cb.platform.util.page.PassCloudResultUtil;
import com.github.pagehelper.Page;
import com.google.common.collect.Lists;
import com.zw.adas.domain.common.AdasRiskStatus;
import com.zw.adas.domain.riskManagement.AdasAlarmDealInfo;
import com.zw.adas.domain.riskManagement.AdasDealInfo;
import com.zw.adas.domain.riskManagement.AdasMedia;
import com.zw.adas.domain.riskManagement.AdasMediaInfo;
import com.zw.adas.domain.riskManagement.AdasRiskItem;
import com.zw.adas.domain.riskManagement.AlarmSign;
import com.zw.adas.domain.riskManagement.HBaseRiskDeal;
import com.zw.adas.domain.riskManagement.bean.AdasMediaEsBean;
import com.zw.adas.domain.riskManagement.bean.AdasRiskEsBean;
import com.zw.adas.domain.riskManagement.bean.AdasRiskEventEsBean;
import com.zw.adas.domain.riskManagement.form.AdasDealRiskForm;
import com.zw.adas.domain.riskManagement.form.AdasEventForm;
import com.zw.adas.domain.riskManagement.form.AdasMediaForm;
import com.zw.adas.domain.riskManagement.form.AdasRiskDisposeRecordForm;
import com.zw.adas.domain.riskManagement.form.AdasRiskEventAlarmForm;
import com.zw.adas.domain.riskManagement.form.AdasRiskEventAlarmReportForm;
import com.zw.adas.domain.riskManagement.form.AdasRiskForm;
import com.zw.adas.domain.riskManagement.form.AdasRiskReportForm;
import com.zw.adas.domain.riskManagement.form.AdasRiskVisitReportForm;
import com.zw.adas.domain.riskManagement.query.AdasRiskDisposeRecordQuery;
import com.zw.adas.domain.riskManagement.show.AdasMediaShow;
import com.zw.adas.domain.riskManagement.show.AdasRiskDisPoseRecordShow;
import com.zw.adas.domain.riskManagement.show.AdasRiskEventAlarmShow;
import com.zw.adas.push.cache.AdasSubcibeTable;
import com.zw.adas.repository.mysql.riskdisposerecord.AdasRiskEventDao;
import com.zw.adas.repository.mysql.riskdisposerecord.AdasRiskLevelDao;
import com.zw.adas.service.elasticsearch.AdasElasticSearchService;
import com.zw.adas.service.riskdisposerecord.AdasRiskService;
import com.zw.adas.utils.AdasCommonHelper;
import com.zw.adas.utils.FastDFSClient;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.MonitorTypeEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.platform.basic.repository.GroupDao;
import com.zw.platform.basic.repository.NewVehicleTypeDao;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.basic.service.VehicleService;
import com.zw.platform.basic.util.RedisServiceUtils;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.ProfessionalsInfo;
import com.zw.platform.domain.connectionparamsset_809.AlarmHandleParam;
import com.zw.platform.domain.leaderboard.RiskResultEnum;
import com.zw.platform.domain.riskManagement.RiskEvent;
import com.zw.platform.push.controller.SubcibeTable;
import com.zw.platform.repository.vas.RiskEventDao;
import com.zw.platform.service.connectionparamsset_809.ConnectionParamsSetService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.Base64Util;
import com.zw.platform.util.DocExportUtil;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.LogTimeUtil;
import com.zw.platform.util.StrUtil;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.UserTable;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.FtpClientUtil;
import com.zw.platform.util.common.MonitorUtils;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.UuidUtils;
import com.zw.platform.util.common.ZipUtility;
import com.zw.platform.util.excel.ExportExcelParam;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.platform.util.ffmpeg.FileUtils;
import com.zw.platform.util.multimedia.UploadUtil;
import com.zw.platform.util.paas.PaasCloudUrlUtil;
import com.zw.platform.util.report.PaasCloudAdasUrlEnum;
import com.zw.platform.util.report.PaasCloudHBaseAccessEnum;
import com.zw.platform.util.response.ResponseUtil;
import com.zw.platform.util.sleep.SleepUtils;
import com.zw.protocol.util.ProtocolTypeUtil;
import freemarker.template.TemplateException;
import joptsimple.internal.Strings;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

/**
 * 功能描述:
 * @author zhengjc
 * @date 2019/5/30
 * @time 18:00
 */
@Service
public class AdasRiskServiceImpl implements AdasRiskService {

    @Autowired
    private AdasRiskLevelDao adasRiskLevelDao;

    @Autowired
    private AdasRiskEventDao adasRiskEventDao;

    @Autowired
    private NewVehicleTypeDao newVehicleTypeDao;

    @Autowired
    private GroupDao groupDao;

    @Autowired
    private AdasElasticSearchService adasElasticSearchService;

    @Autowired
    private AdasCommonHelper adasCommonHelper;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private UserService userService;

    @Autowired
    private AdasElasticSearchService adasEsService;

    @Autowired
    private ConnectionParamsSetService connectionParamsSetService;

    @Autowired
    private LogSearchService logSearchService;

    @Autowired
    private RiskEventDao riskEventDao;

    private static final Logger logger = LogManager.getLogger(AdasRiskServiceImpl.class);

    @Value("${ftp.username}")
    private String ftpUserName;

    @Value("${ftp.password}")
    private String ftpPassword;

    @Value("${ftp.host.clbs}")
    private String ftpHostClbs;

    @Value("${ftp.port.clbs}")
    private int ftpPortClbs;

    @Value("${ftp.path}")
    private String ftpPath;

    @Value("${adas.mediaServer}")
    private String mediaServer;

    @Value("${adas.professionalFtpPath}")
    private String professionalFtpPath;

    @Value("${fdfs.webServerUrl}")
    private String fastDFSMediaServer;

    @Value("${system.ssl.enable}")
    private boolean sslEnabled;

    @Autowired
    private FastDFSClient fastDFSClient;

    @Autowired
    private AdasSubcibeTable adasSubcibeTable;

    @Override
    public List<AdasRiskDisposeRecordForm> getRiskDisposeRecordsByIds(List<String> riskIds, String status) {
        Map<String, String> param = new HashMap<>();
        param.put("riskIds", JSON.toJSONString(riskIds));
        String sendResult = HttpClientUtil.send(PaasCloudAdasUrlEnum.ADAS_REPORT_RISK_LIST, param);
        List<AdasRiskDisposeRecordForm> list =
                PassCloudResultUtil.getListResult(sendResult, AdasRiskDisposeRecordForm.class);
        assembleOrgName(list);
        transformRiskResult(list);
        return list;
    }

    /**
     * 通过主键RowKey 表名 以及get的列去得到HBase数据
     * @param ids       主键
     * @param tableName 表名
     * @param column    列
     * @return 集合
     */
    public List<AdasRiskDisposeRecordForm> getHbByIdAndNameAndColumn(List<String> ids, String tableName,
                                                                     HashMap<String, String> column) {
        Map<String, String> param = new HashMap<>();
        param.put("ids", JSON.toJSONString(ids));
        String sendResult = HttpClientUtil.send(PaasCloudAdasUrlEnum.GET_EVIDENCE_MEDIA_LIST, param);
        List<AdasRiskDisposeRecordForm> list =
            PassCloudResultUtil.getListResult(sendResult, AdasRiskDisposeRecordForm.class);
        assembleOrgName(list);
        return list;
    }

    @Override
    public AdasRiskDisposeRecordForm getRiskDisposeRecordsById(byte[] riskId) {

        Map<String, String> param = new HashMap<>();
        param.put("riskId", JSON.toJSONString(riskId));
        String sendResult = HttpClientUtil.send(PaasCloudAdasUrlEnum.GET_RISK_DISPOSE_RECORDS_BY_ID, param);
        AdasRiskDisposeRecordForm form =
            PassCloudResultUtil.getClassResult(sendResult, AdasRiskDisposeRecordForm.class);
        form.setGroupName(getGroupName(form.getVehicleId()));
        return form;
    }

    private void assembleOrgName(List<AdasRiskDisposeRecordForm> list) {
        List<RedisKey> vehicleKeys = list.stream()
                .map(e -> RedisKeyEnum.MONITOR_INFO.of(e.getVehicleId()))
                .collect(Collectors.toList());
        Map<String, String> vehicleGroupMap = RedisHelper.batchGetHashMap(vehicleKeys, "id", "orgName");
        list.forEach(data -> data.setGroupName(vehicleGroupMap.get(data.getVehicleId())));
    }

    @Override
    public boolean canDownload(String mediaUrl) {
        if (sslEnabled) {
            if (mediaUrl.contains("/mediaserver")) {
                return existFileOnFtpServer(mediaUrl);
            }
            mediaUrl = mediaUrl.substring(1);
            return fastDFSClient.existFile(mediaUrl);
        }
        if (mediaUrl.contains(mediaServer)) {
            return existFileOnFtpServer(mediaUrl);
        }
        return fastDFSClient.existFile(mediaUrl.split(fastDFSMediaServer)[1]);
    }

    public boolean existFileOnFtpServer(String mediaUrl) {
        InputStream in = getMediaInputStream(mediaUrl);
        if (in != null) {
            IOUtils.closeQuietly(in);
            return true;
        }
        return false;
    }

    private InputStream getMediaInputStream(String mediaUrl) {
        if (!sslEnabled) {
            mediaUrl = mediaUrl.split(mediaServer)[1];
        }
        return FtpClientUtil.getFileInputStream(ftpUserName, ftpPassword, ftpHostClbs, ftpPortClbs, ftpPath,
            StringUtil.encodingFtpFileName(mediaUrl));

    }

    @Override
    public List<AdasRiskEventAlarmShow> searchRiskEvents(String riskId, String vehicleId) {
        BindDTO bindDTO = MonitorUtils.getBindDTO(vehicleId, "deviceType");
        String protocolType = bindDTO.getDeviceType();
        List<String> eventIds = adasElasticSearchService.esQueryRiskEventIdsByRiskId(riskId);
        //es和hbase的插入存在先后顺序
        if (CollectionUtils.isEmpty(eventIds)) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            eventIds = adasElasticSearchService.esQueryRiskEventIdsByRiskId(riskId);
            if (CollectionUtils.isEmpty(eventIds)) {
                return new LinkedList<>();
            }
        }
        Map<String, String> param = new HashMap<>();
        param.put("eventIds", JSON.toJSONString(eventIds));
        String sendResult = HttpClientUtil.send(PaasCloudAdasUrlEnum.ADAS_REPORT_EVENT_LIST, param);
        List<AdasRiskEventAlarmForm> riskEventAlarmForms =
            PassCloudResultUtil.getListResult(sendResult, AdasRiskEventAlarmForm.class);

        riskEventAlarmForms.forEach((eventAlarmForm) -> eventAlarmForm.transFormData(adasCommonHelper));

        return getRiskEventAlarm(riskEventAlarmForms, protocolType);
    }

    private List<AdasRiskEventAlarmShow> getRiskEventAlarm(List<AdasRiskEventAlarmForm> riskEventAlarmForms,
        String protocolType) {
        List<AdasRiskEventAlarmShow> result = new ArrayList<>();
        AdasRiskEventAlarmShow riskEventAlarmXShow;
        for (AdasRiskEventAlarmForm areaf : riskEventAlarmForms) {
            riskEventAlarmXShow = new AdasRiskEventAlarmShow();
            BeanUtils.copyProperties(areaf, riskEventAlarmXShow);
            riskEventAlarmXShow.setEventId(areaf.getId());
            riskEventAlarmXShow.setVehicleStatus(getVehicleStatus(areaf.getVehicleStatus()));
            //设置事件中的协议类型（不是车辆实时的协议类型）
            riskEventAlarmXShow.setEventProtocolType(areaf.getProtocolType());
            setAttachmentStatus(riskEventAlarmXShow, protocolType);
            result.add(riskEventAlarmXShow);
        }
        //排序,按照时间、时间编号进行排序
        result.sort(
            comparing(AdasRiskEventAlarmShow::getEventTime).thenComparing(AdasRiskEventAlarmShow::getEventNumber));
        return result;
    }

    private void setAttachmentStatus(AdasRiskEventAlarmShow adasRiskEventAlarmShow, String protocolType) {
        if (adasRiskEventAlarmShow.getAttachmentStatus() != 1) {
            try {
                //验证此条风险是否超过三天
                long time = DateUtil.getStringToLong(adasRiskEventAlarmShow.getEventTime(), null);
                if (System.currentTimeMillis() - time >= 3 * 24 * 60 * 60 * 1000
                        || !checkMediaCount(adasRiskEventAlarmShow, protocolType)) {
                    adasRiskEventAlarmShow.setAttachmentStatus(Integer.valueOf(1).byteValue());
                    Map<String, String> param = new HashMap<>();
                    param.put("eventId", JSON.toJSONString(UuidUtils.getBytesFromStr(adasRiskEventAlarmShow.getId())));
                    param.put("attachmentStatus", "1");
                    String sendResult = HttpClientUtil.send(PaasCloudAdasUrlEnum.UPDATE_EVENT_ATTACHMENT_STATUS, param);
                    Boolean re = PassCloudResultUtil.getClassResult(sendResult, Boolean.class);
                    if (!re) {
                        logger.error("调用pass api 更新下发9208状态失败！");
                    }
                } else {
                    checkAttachmentStatus(adasRiskEventAlarmShow, protocolType);
                }
            } catch (Exception e) {
                logger.error("时间转换异常", e);
            }
        }
    }

    private boolean checkMediaCount(AdasRiskEventAlarmShow adasRiskEventAlarmShow, String protocolType) {
        boolean flag = true;
        switch (protocolType) {
            case ProtocolTypeUtil.ZHONG_WEI_PROTOCOL_808_2013:
                List<AdasMediaInfo> mediaInfoList =
                    JSONObject.parseArray(adasRiskEventAlarmShow.getMediaInfoStr(), AdasMediaInfo.class);
                if (mediaInfoList == null) {
                    flag = false;
                }
                break;
            case ProtocolTypeUtil.SI_CHUAN_PROTOCOL_808_2013:
            case ProtocolTypeUtil.JI_PROTOCOL_808_2013:
            case ProtocolTypeUtil.GUANG_XI_PROTOCOL_808_2013:
            case ProtocolTypeUtil.JIANG_SU_PROTOCOL_808_2013:
            case ProtocolTypeUtil.ZHE_JIANG_PROTOCOL_808_2013:
            case ProtocolTypeUtil.JI_LIN_PROTOCOL_808_2013:
            case ProtocolTypeUtil.JIANG_XI_PROTOCOL_808_2013:
            case ProtocolTypeUtil.SHANG_HAI_PROTOCOL_808_2019:
            case ProtocolTypeUtil.ZW_PROTOCOL_808_2019:
            case ProtocolTypeUtil.HEI_PROTOCOL_808_2019:
            case ProtocolTypeUtil.LU_PROTOCOL_808_2019:
            case ProtocolTypeUtil.XIANG_PROTOCOL_808_2013:
            case ProtocolTypeUtil.YUE_PROTOCOL_808_2019:
                if (adasRiskEventAlarmShow.getMediaCount() == null || adasRiskEventAlarmShow.getMediaCount() <= 0) {
                    flag = false;
                }
                break;
            case ProtocolTypeUtil.BEI_JING_PROTOCOL_808_2019:
                List<AlarmSign> alarmSignList =
                    JSONObject.parseArray(adasRiskEventAlarmShow.getMediaInfoStr(), AlarmSign.class);
                if (alarmSignList == null || alarmSignList.size() == 0) {
                    flag = false;
                }
                break;
            default:
                break;
        }
        return flag;
    }

    private void checkAttachmentStatus(AdasRiskEventAlarmShow adasRiskEventAlarmShow, String protocolType) {
        if (protocolType != null && !protocolType.equals("1")) {
            String eventIdKey = adasRiskEventAlarmShow.getEventId().replaceAll("-", "") + "_manual";
            Byte attachmentStatus = 0;
            if (adasSubcibeTable.get(eventIdKey) != null) {
                attachmentStatus = 2;
            }
            adasRiskEventAlarmShow.setAttachmentStatus(attachmentStatus);
        } else {
            String subcibeTableKey = RedisHelper
                .getString(HistoryRedisKeyEnum.ADAS_MANUAL_SEND_9208_EXPIRE_TIME.of(adasRiskEventAlarmShow.getId()));

            List<String> subcibeTableKeys =
                subcibeTableKey == null ? new ArrayList<>() : JSONArray.parseArray(subcibeTableKey, String.class);
            for (String key : subcibeTableKeys) {
                if (SubcibeTable.containsKey(key)) {
                    adasRiskEventAlarmShow.setAttachmentStatus((byte) 2);
                    break;
                }
            }
        }
    }

    @Override
    public List<AdasRiskDisposeRecordForm> getExportData(List<String> eventIds) {
        List<AdasRiskDisposeRecordForm> risks = new ArrayList<>();
        int size = eventIds.size();
        eventIds = eventIds.subList(0, Math.min(size, 20000));
        if (eventIds.size() > 0) {
            Map<String, String> param = new HashMap<>();
            param.put("eventIds", JSON.toJSONString(eventIds));
            String sendResult = HttpClientUtil.send(PaasCloudAdasUrlEnum.ADAS_REPORT_EXPORT_EVENT, param);
            risks = PassCloudResultUtil.getListResult(sendResult, AdasRiskDisposeRecordForm.class);
            assembleOrgName(risks);
            transExportData(risks);
        }
        //倒序进行排序
        risks.sort((o1, o2) -> (int) (o2.getOrderTime() - o1.getOrderTime()));
        return risks;
    }

    @Override
    public AdasRiskReportForm searchRiskReportFormById(byte[] riskId, String contextPath) {
        Map<String, String> param = new HashMap<>();
        param.put("riskId", JSON.toJSONString(riskId));
        String sendResult = HttpClientUtil.send(PaasCloudAdasUrlEnum.SEARCH_RISK_REPORT_FORM_BY_ID, param);
        AdasRiskReportForm report = PassCloudResultUtil.getClassResult(sendResult, AdasRiskReportForm.class);
        transeFormReport(report);
        setDriverPic(report.getDrivers(), contextPath);
        return report;
    }

    /**
     * 设置司机图片
     * @param drivers     实际信息
     * @param contextPath 项目的绝对路径
     */
    private void setDriverPic(List<ProfessionalsInfo> drivers, String contextPath) {
        if (drivers == null || drivers.isEmpty()) {
            return;
        }
        String fileFtpPath;
        for (ProfessionalsInfo driver : drivers) {
            if (StringUtil.isNullOrBlank(driver.getPhotograph())) {
                continue;
            }
            fileFtpPath = getFileFtpPath(contextPath, driver);
            driver.setPhotograph(fileFtpPath);
        }
    }

    private String getFileFtpPath(String contextPath, ProfessionalsInfo driver) {
        if (StringUtil.isNullOrBlank(contextPath)) {
            if (sslEnabled) {
                return "/mediaserver" + professionalFtpPath + driver.getPhotograph();
            }
            return mediaServer + professionalFtpPath + driver.getPhotograph();
        } else {
            InputStream proImgInputStream = FtpClientUtil
                .getFileInputStream(ftpUserName, ftpPassword, ftpHostClbs, ftpPortClbs, professionalFtpPath,
                    driver.getPhotograph());
            return Base64Util.getImageStr(proImgInputStream, contextPath);
        }
    }

    @Override
    public JSONObject hasRiskEvidence(String riskId, String riskNumber) {

        Map<String, String> param = new HashMap<>();
        param.put("riskId", riskId);
        String sendResult = HttpClientUtil.send(PaasCloudAdasUrlEnum.GET_RISK_EVIDENCE_URL, param);
        String filePath = PassCloudResultUtil.getClassResult(sendResult, String.class);
        JSONObject result = new JSONObject();
        result.put("riskId", riskId);
        if (StringUtil.isNullOrBlank(filePath)) {
            result.put("hasNotFile", true);
            result.put("msg", "没有风控证据文件！");
        } else {
            result.put("filePath", filePath);
            result.put("hasNotFile", false);
            result.put("fileName", "风控证据" + riskNumber);
            result.put("isRiskEvidence", true);
            result.put("classFlag", "riskEvidence");
        }
        return result;
    }

    @Override
    public JSONObject downloadDeviceEvidence(String downLoadId, boolean isEvent, String number) {
        JSONObject result = getDefaultFaultResult();

        List<String> mediaIds = getMediaIds(downLoadId, isEvent);
        if (mediaIds.size() == 0) {
            return result;
        }

        Map<String, String> param = new HashMap<>();
        param.put("mediaIds", JSONObject.toJSONString(mediaIds));
        String sendResult = HttpClientUtil.send(PaasCloudAdasUrlEnum.GET_MEDIA_BY_IDS, param);
        TypeReference<List<Map<String, String>>> type = new TypeReference<List<Map<String, String>>>() {};
        List<Map<String, String>> evidenceMedias = PassCloudResultUtil.getListResult(sendResult, type);
        if (CollectionUtils.isNotEmpty(evidenceMedias)) {
            result = packDeviceEvidence(downLoadId, evidenceMedias, number);
        }
        return result;
    }

    private List<String> getMediaIds(String downLoadId, boolean isEvent) {
        List<String> mediaIds;
        if (isEvent) {
            mediaIds = adasElasticSearchService.esGetTerminalEvidenceByEventId(downLoadId);
        } else {
            mediaIds = adasElasticSearchService.esGetTerminalEvidenceByRiskId(downLoadId);
        }
        return mediaIds;
    }

    @Override
    public void exportDocByBatch(AdasRiskDisposeRecordQuery query, HttpServletResponse response,
        HttpServletRequest request) throws Exception {

        // 定义相关的文件路径
        String exportPath = request.getServletContext().getRealPath("/") + "exportDocPath" + UUID.randomUUID();
        String zipTempPath = request.getServletContext().getRealPath("/") + "ziptemp";
        String zipFile = "风控报告.zip";
        // 1.生成可以导出doc文件的数据
        List<AdasRiskReportForm> result = getAdasRiskReportFormList(query);
        // 2.写doc文件到指定目录
        writeDocToTempPath(request, result, exportPath);
        FileUtils.createDirectory(zipTempPath);
        // 3.将该文件打包成指定的zip
        ZipUtility.zip(exportPath, zipTempPath, zipFile);
        String docZipPath = zipTempPath + File.separator + zipFile;
        UploadUtil.deleteDirectory(exportPath);
        //设置response的头
        ResponseUtil.setZipResponse(response, zipFile);
        FileUtils.writeFile(docZipPath, response.getOutputStream());
        UploadUtil.delete(docZipPath);
        addLog(query, request, "风控报告");
    }

    @Override
    public PageGridBean getPageGridBean(AdasRiskDisposeRecordQuery query) throws Exception {

        if (!StrUtil.isNotBlank(query.getVehicleIds())) {
            return new PageGridBean(new Page<AdasRiskDisposeRecordForm>());
        }
        //判断是否输入车牌进行查询

        if (!checkBrandInSelected(query)) {
            return new PageGridBean(new Page<AdasRiskDisposeRecordForm>(), true, "组织中未勾选查询的车!");
        }
        int startPage = query.getPage().intValue();
        int limit = query.getLimit().intValue();
        Page<AdasRiskDisPoseRecordShow> result = new Page<>(startPage, limit, false);
        //由于es比hbase存储比快,顾结束时间往前退30s
        // query.setEndTime(DateUtil.minusSecond(query.getEndTime(), 3));
        Map<String, Object> map = adasEsService.esQueryRiskInfo(query, false);
        long total = (long) map.get("total");
        List<String> ids = (List<String>) map.get("ids");
        List<AdasRiskDisposeRecordForm> risks = getRiskDisposeRecordsByIds(ids, query.getStatus());
        boolean flag = false;
        if (startPage == 1) {
            // 查询第一页的时候由于es和hbase 存在先插入和后插入的问题,第一页如果的pageSize比limit小,就要进行补偿,最多补偿5页数据
            removeRiskNumberIsNull(risks);
            if (risks.size() != limit && total > limit) {
                int index = 0;
                compensationRecord(query, result, risks, ids, total, index);
                flag = true;
            }
        }
        if (!flag) {
            // 设置数据dataList
            result.addAll(getRiskDisposeRecordShow(risks));
            // 设置总数
            result.setTotal((long) map.get("total"));
        }
        //设置上一页,下一页游标
        return new PageGridBean(result, (Object[]) map.get("search_after"));
    }

    private List<AdasRiskDisPoseRecordShow> getRiskDisposeRecordShow(List<AdasRiskDisposeRecordForm> risks) {
        List<AdasRiskDisPoseRecordShow> riskShowList = new ArrayList<>();
        AdasRiskDisPoseRecordShow riskDisPoseRecordXShow;
        AdasRiskDisposeRecordForm riskDisposeRecordXForm;
        for (AdasRiskDisposeRecordForm risk : risks) {
            riskDisPoseRecordXShow = new AdasRiskDisPoseRecordShow();
            riskDisposeRecordXForm = risk;
            riskDisposeRecordXForm.assembleMediaFlag();
            BeanUtils.copyProperties(riskDisposeRecordXForm, riskDisPoseRecordXShow);
            riskShowList.add(riskDisPoseRecordXShow);
        }
        return riskShowList;
    }

    /**
     * 移除riskNumber为null的数据
     */
    private void removeRiskNumberIsNull(List<AdasRiskDisposeRecordForm> risks) {
        risks.removeIf(risk -> !StrUtil.isNotBlank(risk.getRiskNumber()));
    }

    private void compensationRecord(AdasRiskDisposeRecordQuery query, Page<AdasRiskDisPoseRecordShow> result,
                                    List<AdasRiskDisposeRecordForm> risks, List<String> ids, long total, int index)
            throws Exception {

        Set<String> set = new HashSet<>();
        List<String> excludeIds = Lists.newLinkedList();
        for (AdasRiskDisposeRecordForm risk : risks) {
            set.add(UuidUtils.getUUIDStrFromBytes(risk.getIdByte()));
        }
        for (String id : ids) {
            if (!set.contains(id)) {
                excludeIds.add(id);
            }
        }
        if (excludeIds.isEmpty()) {
            return;
        }
        query.getExcludeIds().addAll(excludeIds);
        Map<String, Object> map = adasEsService.esQueryRiskInfo(query, false);

        List<String> ids1 = (List<String>) map.get("ids");
        List<AdasRiskDisposeRecordForm> riskList = getRiskDisposeRecordsByIds(ids1, query.getStatus());
        if (riskList.size() < query.getLimit()) {
            index++;
            if (index > 5) {
                //递归5次
                setResult(query, result, riskList, total);
                return;
            }
            // 再次补偿
            compensationRecord(query, result, riskList, ids1, total, index);
        } else {
            setResult(query, result, riskList, total);
        }

    }

    private void setResult(AdasRiskDisposeRecordQuery query, Page<AdasRiskDisPoseRecordShow> result,
        List<AdasRiskDisposeRecordForm> risks, long total) {
        result.addAll(getRiskDisposeRecordShow(risks));
        result.setTotal(total - query.getExcludeIds().size());
    }

    private void writeDocToTempPath(HttpServletRequest request, List<AdasRiskReportForm> result, String exportPath)
        throws IOException, TemplateException {
        FileUtils.createDirectory(exportPath);
        DocExportUtil.initTemplateOnWeb("/file/ftl", "demo20033.ftl", request.getServletContext());
        for (AdasRiskReportForm adasRiskReportForm : result) {
            String fileName = adasRiskReportForm.getRiskNumber() + "风控报告";
            Map<String, Object> dataMap = getExportDataMap(adasRiskReportForm);
            DocExportUtil.exportDocWithName(exportPath, dataMap, fileName);
        }
    }

    private Map<String, Object> getExportDataMap(AdasRiskReportForm adasRiskReportForm) {
        Map<String, Object> dataMap = new HashMap<>();
        transeFormReport(adasRiskReportForm);
        setDriverPic(adasRiskReportForm.getDrivers(), null);
        dataMap.put("rp", adasRiskReportForm);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return dataMap;
    }

    private List<AdasRiskReportForm> getAdasRiskReportFormList(AdasRiskDisposeRecordQuery query) {
        query.setStatus("已归档");
        checkBrandExistOrNot(query);
        List<AdasRiskReportForm> reports = null;
        try {
            Map<String, Object> map = adasElasticSearchService.esQueryRiskInfo(query, true);
            List<String> riskIds = (List<String>) map.get("ids");
            Map<String, String> param = new HashMap<>();
            param.put("riskIds", JSONObject.toJSONString(riskIds));
            String sendResult = HttpClientUtil.send(PaasCloudAdasUrlEnum.GET_RISK_REPORTS_BY_IDS, param);
            reports = PassCloudResultUtil.getListResult(sendResult, AdasRiskReportForm.class);

        } catch (Exception e) {
            logger.error("获取批量导出风险结合失败");
        }
        return reports;

    }

    public void checkBrandExistOrNot(AdasRiskDisposeRecordQuery query) {
        // 通过车牌查询
        if (!StringUtils.isEmpty(query.getBrand())) {
            // 如果查询条件带车牌
            Set<String> vehicleIds = vehicleService
                .fuzzyKeyword(query.getBrand(), userService.getCurrentUserMonitorIds(), MonitorTypeEnum.VEHICLE);
            query.setVehicleIds(Strings.join(vehicleIds, ","));
        }
    }

    private JSONObject packDeviceEvidence(String downLoadId, List<Map<String, String>> evidenceMedia, String number) {
        JSONObject result = getDefaultFaultResult();
        //指定一个存放压缩包的目录
        File storeZipDir = getStoreZipDir();
        // 创建要打包的目录
        String packDirPath = storeZipDir.getAbsolutePath() + File.separator + downLoadId + File.separator;
        File packDir = getPackDir(packDirPath);
        writeFileToZipDir(evidenceMedia, packDirPath);
        boolean packSuccess = packDir(downLoadId, storeZipDir, packDir);
        if (packSuccess) {
            String zipPath = storeZipDir.getAbsolutePath() + File.separator + downLoadId + ".zip";
            result = getSuccessResult(number, zipPath);
        }
        return result;
    }

    private boolean packDir(String downLoadId, File directory, File packDir) {
        boolean packSuccess = false;
        // 判断文件夹是否为空
        boolean isEmptyDir = isEmptyDir(packDir);
        // 将文件打包到zip
        try {
            if (!isEmptyDir) {
                String srcPath = directory.getAbsolutePath() + File.separator + downLoadId;
                String zipPath = directory.getAbsolutePath();
                String zipName = downLoadId + ".zip";
                packSuccess = ZipUtility.zip(srcPath, zipPath, zipName);
            }
        } catch (Exception e) {
            logger.error("压缩终端证据失败", e);
        }
        return packSuccess;
    }

    private JSONObject getSuccessResult(String number, String zipPath) {
        JSONObject result = new JSONObject();
        result.put("filePath", zipPath);
        result.put("fileName", "终端证据" + number);
        result.put("hasNotFile", false);
        result.put("msg", "");
        result.put("isRiskEvidence", false);
        return result;
    }

    private JSONObject getDefaultFaultResult() {
        JSONObject result = new JSONObject();
        result.put("hasNotFile", true);
        result.put("msg", "没有终端证据文件！");
        result.put("isRiskEvidence", false);
        return result;
    }

    private boolean isEmptyDir(File filePath) {
        boolean isEmptyDir = true;
        String[] files = filePath.list();
        if (files != null && files.length > 0) {
            isEmptyDir = false;
        }
        return isEmptyDir;
    }

    private void writeFileToZipDir(List<Map<String, String>> evidenceUrls, String packDirPath) {
        if (evidenceUrls != null && evidenceUrls.size() > 0) {
            for (Map<String, String> media : evidenceUrls) {
                writeMedia(media, packDirPath);
            }
        }
    }

    private void writeMedia(Map<String, String> media, String path) {
        String name = media.get("NAME");
        String url = media.get("URL");
        String filePath = path + name;
        boolean isStreamMedia = adasCommonHelper.isStreamMedia(media.get("PROTOCOLTYPE"));

        if (isStreamMedia) {
            FileUtils.writeFile(fastDFSClient.downloadFile(url), filePath);
        } else {
            cpFileFromFtp(url, filePath);
        }

    }

    private void cpFileFromFtp(String url, String filePath) {
        InputStream in = null;
        FileOutputStream out = null;
        try {

            in = FtpClientUtil.getFileInputStream(ftpUserName, ftpPassword, ftpHostClbs, ftpPortClbs,
                StringUtil.encodingFtpFileName(url));

            out = new FileOutputStream(filePath);
            FileUtils.writeFile(in, out);
        } catch (Exception e) {
            logger.error(url + "  Not Found!", e);
        } finally {
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(in);
        }
    }

    private File getPackDir(String path) {
        File filePath = new File(path);
        if (!filePath.exists()) {
            filePath.mkdirs();
        }
        return filePath;
    }

    private File getStoreZipDir() {
        // 新建文件夹
        File directory;
        if (System.getProperty("os.name").toLowerCase().indexOf("windows") != -1) {
            directory = new File("c:/adasterminalEvidence/");
        } else {
            directory = new File("/usr/local/terminalEvidence/");
        }
        // 执行操作前先清空上次打包的内容
        FileUtils.deleteDir(directory);
        // 执行创建
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return directory;
    }

    private void transeFormReport(AdasRiskReportForm report) {
        Map<String, String> levelMaps = getLevelMaps();
        report.setId(UuidUtils.getUUIDStrFromBytes(report.getIdByte()));
        report.setRiskLevel(levelMaps.get(report.getRiskLevel()));
        report.setGroupName(getGroupName(report.getVehicleId()));
        VehicleDTO vehicleDTO = vehicleService.getById(report.getVehicleId());

        transeRiskType(report);
        transeRiskResult(report);
        transVehType(report, vehicleDTO);
        transeRiskEvents(report);
        transGroupPhone(report, vehicleDTO);
        transeDriver(report);
        transeAllVisit(report);
    }

    private void transeAllVisit(AdasRiskReportForm report) {
        AdasRiskVisitReportForm dealVisit = getVisit(report.getVisit4());
        AdasRiskVisitReportForm visit1 = getVisit(report.getVisit1());
        AdasRiskVisitReportForm visit2 = getVisit(report.getVisit2());
        AdasRiskVisitReportForm visit3 = getVisit(report.getVisit3());
        List<AdasRiskVisitReportForm> visits = new ArrayList<>();
        List<AdasRiskVisitReportForm> dealVisitList = new ArrayList<>();
        addVisit(visit1, visits);
        addVisit(visit2, visits);
        addVisit(visit3, visits);
        addVisit(dealVisit, dealVisitList);
        setDealVisit(report, dealVisitList);
        report.setRiskVisits(transeGetRiskVists(visits));

    }

    private void setDealVisit(AdasRiskReportForm report, List<AdasRiskVisitReportForm> dealVisitList) {
        if (dealVisitList != null && dealVisitList.size() > 0) {
            report.setDealVisit(transeGetRiskVists(dealVisitList).get(0));
        }

    }

    private void addVisit(AdasRiskVisitReportForm visit, List<AdasRiskVisitReportForm> visits) {
        if (visit != null) {
            visits.add(visit);
        }
    }

    private AdasRiskVisitReportForm getVisit(String visitStr) {
        if (!StringUtil.isNullOrBlank(visitStr)) {
            JSONObject visit1 = JSONObject.parseObject(visitStr, JSONObject.class);
            AdasRiskVisitReportForm visit = new AdasRiskVisitReportForm();
            Object warningAccuracy = visit1.get("warningAccuracy");
            Object warnAfterStatus = visit1.get("warnAfterStatus");
            Object interventionPersonnel = visit1.get("interventionPersonnel");
            Object interventionAfterStatus = visit1.get("interventionAfterStatus");
            Object warningLevel = visit1.get("warningLevel");
            Object content = visit1.get("content");
            Object driverName = visit1.get("driverName");
            Object reason = visit1.get("reason");
            visit.setContent(content == null ? "" : content.toString());
            visit.setReason(reason == null ? "" : reason.toString());
            visit.setDriverName(driverName == null ? "" : driverName.toString());
            visit.setWarnAfterStatus(warnAfterStatus == null ? "" : warnAfterStatus.toString());
            visit.setWarningAccuracy(warningAccuracy == null ? "" : warningAccuracy.toString());
            visit.setWarningLevel(warningLevel == null ? "" : warningLevel.toString());
            visit.setInterventionAfterStatus(interventionAfterStatus == null ? "" : interventionAfterStatus.toString());
            visit.setInterventionPersonnel(interventionPersonnel == null ? "" : interventionPersonnel.toString());
            return visit;
        }
        return null;

    }

    private List<AdasRiskVisitReportForm> transeGetRiskVists(List<AdasRiskVisitReportForm> riskVisitReportForms) {
        Map<String, String> warningAccuracyMap = new HashMap<>();
        Map<String, String> warnAfterStatusMap = new HashMap<>();
        Map<String, String> interventionPersonnelMap = new HashMap<>();
        Map<String, String> interventionAfterStatusMap = new HashMap<>();
        Map<String, String> warningLevelMap = new HashMap<>();
        warningAccuracyMap.put("0", "风险预警信息准确");
        warningAccuracyMap.put("1", "风险预警信息不准确");
        warningAccuracyMap.put("2", "无");
        warnAfterStatusMap.put("0", "预警后车辆及驾驶员状态正常");
        warnAfterStatusMap.put("1", "预警后车辆及驾驶员状态异常");
        warnAfterStatusMap.put("2", "无");
        interventionPersonnelMap.put("0", "风控管理人员已人工干预");
        interventionPersonnelMap.put("1", "风控管理人员未人工干预");
        interventionPersonnelMap.put("2", "无");
        interventionAfterStatusMap.put("0", "风控管理人员干预后驾驶员配合");
        interventionAfterStatusMap.put("1", "风控管理人员干预后驾驶员不配合");
        interventionAfterStatusMap.put("2", "无");
        warningLevelMap.put("0", "本次风险预警后危险状态已解除");
        warningLevelMap.put("1", "本次风险预警后危险状态未解除");
        warningLevelMap.put("2", "无");

        if (CollectionUtils.isNotEmpty(riskVisitReportForms)) {
            // 转换处理和回访记录
            for (AdasRiskVisitReportForm riskVisitReportForm : riskVisitReportForms) {
                if (!StringUtil.isNullOrBlank(riskVisitReportForm.getWarningAccuracy())) {
                    riskVisitReportForm
                        .setWarningAccuracy(warningAccuracyMap.get(riskVisitReportForm.getWarningAccuracy()));
                }
                if (!StringUtil.isNullOrBlank(riskVisitReportForm.getWarnAfterStatus())) {
                    riskVisitReportForm
                        .setWarnAfterStatus(warnAfterStatusMap.get(riskVisitReportForm.getWarnAfterStatus()));
                }
                if (!StringUtil.isNullOrBlank(riskVisitReportForm.getInterventionPersonnel())) {
                    riskVisitReportForm.setInterventionPersonnel(
                        interventionPersonnelMap.get(riskVisitReportForm.getInterventionPersonnel()));
                }
                if (!StringUtil.isNullOrBlank(riskVisitReportForm.getInterventionAfterStatus())) {
                    riskVisitReportForm.setInterventionAfterStatus(
                        interventionAfterStatusMap.get(riskVisitReportForm.getInterventionAfterStatus()));
                }
                if (!StringUtil.isNullOrBlank(riskVisitReportForm.getWarningLevel())) {
                    riskVisitReportForm.setWarningLevel(warningLevelMap.get(riskVisitReportForm.getWarningLevel()));
                }
            }
        }
        return riskVisitReportForms;
    }

    private void transGroupPhone(AdasRiskReportForm report, VehicleDTO vehicleDTO) {
        StringBuilder phones = new StringBuilder();
        String groupId = vehicleDTO.getGroupId();
        if (StrUtil.isBlank(groupId)) {
            report.setGroupPhone("");
        }
        String telephone = groupDao.getGroupPhoneByIds(groupId);
        if (StringUtils.isEmpty(telephone)) {
            report.setGroupPhone("");
            return;
        }
        String[] phoneArr = telephone.split(",");
        for (String phone : phoneArr) {
            if (!StringUtil.isNullOrBlank(phone)) {
                phones.append(phone);
                phones.append(",");
            }
        }
        report.setGroupPhone(phones.toString().substring(0, phones.length() - 1));
    }

    private void transeDriver(AdasRiskReportForm report) {
        String drivids = report.getDriverIds();
        if (drivids != null) {
            List<ProfessionalsInfo> proList = new ArrayList<>();
            String[] driverList = drivids.split(",");
            for (String driverId : driverList) {
                ProfessionalsInfo pro = getProfessional(driverId);
                proList.add(pro);

            }
            report.setDrivers(proList);
        }

    }

    private ProfessionalsInfo getProfessional(String driverId) {
        List<String> fields = Arrays.asList("name", "phone", "emergencyContact", "emergencyContactPhone", "photograph");
        Map<String, String> driver = RedisHelper.getHashMap(RedisKeyEnum.PROFESSIONAL_INFO.of(driverId), fields);
        ProfessionalsInfo pro = new ProfessionalsInfo();
        pro.setName(driver.get("name"));
        pro.setPhone(driver.get("phone"));
        pro.setEmergencyContact(driver.get("emergencyContact"));
        pro.setEmergencyContactPhone(driver.get("emergencyContactPhone"));
        pro.setPhotograph(driver.get("photograph"));
        return pro;

    }

    private void transeRiskEvents(AdasRiskReportForm report) {
        List<AdasRiskEventAlarmReportForm> riskEvents = getRiskEvents(report.getId());
        Map<String, String> docEventsMap = getDocRiskEventsMaps();
        for (AdasRiskEventAlarmReportForm rearf : riskEvents) {
            String riskEventVal = docEventsMap.get(rearf.getRiskEvent());
            rearf.setRiskEvent(riskEventVal);
        }
        report.setReafList(riskEvents);

    }

    private List<AdasRiskEventAlarmReportForm> getRiskEvents(String id) {
        List<AdasRiskEventAlarmReportForm> result = new ArrayList<>();
        List<AdasRiskEventEsBean> riskEventEsBeans = adasElasticSearchService.getEventEsBeanByRiskId(id);
        if (riskEventEsBeans != null && riskEventEsBeans.size() > 0) {
            for (AdasRiskEventEsBean reeb : riskEventEsBeans) {
                AdasRiskEventAlarmReportForm rearf = new AdasRiskEventAlarmReportForm();
                rearf.setRiskEvent(reeb.getEventType() + "");
                rearf.setEventTime(reeb.getEventTime());
                result.add(rearf);
            }
        }
        return result;

    }

    private void transVehType(AdasRiskReportForm report, VehicleDTO vehicleDTO) {
        Map<String, String> vehTypeMaps = getVehTypeMaps();
        String vehTypeId = getVeTypeId(vehicleDTO);
        report.setVehicleType(vehTypeMaps.get(vehTypeId));
    }

    private String getVeTypeId(VehicleDTO vehicleDTO) {

        if (vehicleDTO != null) {
            return vehicleDTO.getVehicleType();
        }
        return "";
    }

    private Map<String, String> getVehTypeMaps() {
        Map<String, String> vehTypeMaps = new HashMap<>();
        List<Map<String, String>> typeMaps = newVehicleTypeDao.getTypeMaps();
        for (Map<String, String> typeMap : typeMaps) {
            vehTypeMaps.put(typeMap.get("id"), typeMap.get("type"));
        }
        return vehTypeMaps;
    }

    private Map<String, String> getDocRiskEventsMaps() {
        Map<String, String> vehTypeMaps = new HashMap<>();
        List<Map<String, String>> typeMaps = adasRiskEventDao.getDocRiskEventMap();
        for (Map<String, String> typeMap : typeMaps) {
            vehTypeMaps.put(typeMap.get("functionId"), typeMap.get("riskType"));
        }
        return vehTypeMaps;
    }

    private void transeRiskType(AdasRiskReportForm adasRiskReportForm) {
        Map<String, String> riskTypeMap = new HashMap<>();
        riskTypeMap.put("1", "疑似疲劳");
        riskTypeMap.put("2", "注意力分散");
        riskTypeMap.put("3", "违规异常");
        riskTypeMap.put("4", "碰撞危险");
        StringBuilder type = new StringBuilder();
        String riskType = adasRiskReportForm.getRiskType();
        if (!StringUtil.isNullOrBlank(riskType)) {
            String[] types = riskType.split(",");
            for (String typeN : types) {
                type.append(riskTypeMap.get(typeN)).append(",");
            }
            adasRiskReportForm.setRiskType(type.toString().substring(0, type.length() - 1));
        }
    }

    private void transeRiskResult(AdasRiskReportForm adasRiskReportForm) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("1", "接受督导高敏");
        resultMap.put("2", "接受督导低敏");
        resultMap.put("3", "事故发生");
        if (!StringUtil.isNullOrBlank(adasRiskReportForm.getRiskResult())) {
            adasRiskReportForm.setRiskResult(resultMap.get(adasRiskReportForm.getRiskResult()));
        }
    }

    private void transExportData(List<AdasRiskDisposeRecordForm> list) {
        Map<String, AdasRiskDisposeRecordForm> risk = new HashMap<>();
        AdasRiskDisposeRecordForm arrf;
        for (AdasRiskDisposeRecordForm rdrf : list) {
            arrf = risk.get(rdrf.getRiskId());
            if (arrf == null) {
                arrf = getRiskDisposeRecordsById(UuidUtils.getBytesFromStr(rdrf.getRiskId()));
                risk.put(rdrf.getRiskId(), arrf);
            }
            rdrf.setDriver(arrf.getDriver());
            rdrf.setDriverNo(arrf.getDriverNo());
            rdrf.setRiskType(arrf.getRiskType());
            rdrf.setRiskLevel(arrf.getRiskLevel());
            rdrf.setStatus(arrf.getStatus());
            rdrf.setRiskResult(arrf.getRiskResult());
            rdrf.setAddress(arrf.getFormattedAddress());
            rdrf.setVisitTime(arrf.getVisitTime());
            rdrf.setDealUser(arrf.getDealUser());
            rdrf.setJob("风控岗");
            rdrf.setBrand(arrf.getBrand());
            rdrf.setDealTime(arrf.getDealTime());
            rdrf.setFileTime(arrf.getFileTime());
            rdrf.setOrderTime(arrf.getOrderTime());
            rdrf.setWarTime(arrf.getWarTime());
            rdrf.setWeather(arrf.getWeather());
            rdrf.setRiskEvent(adasCommonHelper.geEventName(rdrf.getRiskEvent()));
            rdrf.transFormData(adasCommonHelper);
        }
    }

    private void transformRiskResult(List<AdasRiskDisposeRecordForm> list) {
        for (AdasRiskDisposeRecordForm rdrf : list) {
            rdrf.transFormData(adasCommonHelper);
        }
    }

    private Map<String, String> getLevelMaps() {
        Map<String, String> levelMap = new HashMap<>();
        List<Map<String, String>> riskLevels = adasRiskLevelDao.getRiskLevelMap();
        for (Map<String, String> level : riskLevels) {
            levelMap.put(level.get("riskValue"), level.get("riskLevel"));
        }
        return levelMap;
    }

    private String getGroupName(String vehicleId) {
        if (vehicleId != null && !vehicleId.equals("")) {

            Map<String, String> monitorInfo =
                RedisHelper.getHashMap(RedisKeyEnum.MONITOR_INFO.of(vehicleId), Arrays.asList("orgName"));
            if (monitorInfo != null) {
                return monitorInfo.get("orgName");
            }
        }
        return "";
    }

    @Override
    public String getRiskStatus(String riskId) {
        Map<String, String> param = new HashMap<>();
        param.put("riskId", riskId);
        String sendResult = HttpClientUtil.send(PaasCloudAdasUrlEnum.GET_RISK_STATUS, param);
        return PassCloudResultUtil.getClassResult(sendResult, String.class);
    }

    @Override
    public List<AdasMediaShow> getMedias(String riskId, int mediaType) {
        List<AdasMediaShow> result = new ArrayList<>();

        List<String> mediaIds = adasElasticSearchService.esGetMediaIdsByRiskId(mediaType, true, false, false, riskId);
        if (CollectionUtils.isNotEmpty(mediaIds)) {
            Map<String, String> param = new HashMap<>(2);
            param.put("mediaIds", JSONObject.toJSONString(mediaIds));
            String sendResult = HttpClientUtil.send(PaasCloudAdasUrlEnum.GET_MEDIA_LIST, param);
            List<AdasMedia> mediaList = PassCloudResultUtil.getListResult(sendResult, AdasMedia.class);
            List<AdasMedia> medias = setMediaPath(mediaList);
            cpMedia(result, medias);
        }
        return result;
    }

    @Override
    public List<AdasMediaShow> getEventMedias(String eventId, int mediaType) {
        List<AdasMediaShow> result = new ArrayList<>();

        List<String> mediaIds = adasElasticSearchService.esGetMediaIdsByEventIds(mediaType, true, false, eventId);
        if (CollectionUtils.isNotEmpty(mediaIds)) {
            Map<String, String> param = new HashMap<>();
            param.put("mediaIds", JSONObject.toJSONString(mediaIds));
            String sendResult = HttpClientUtil.send(PaasCloudAdasUrlEnum.GET_MEDIA_LIST, param);
            List<AdasMedia> mediaList = PassCloudResultUtil.getListResult(sendResult, AdasMedia.class);
            List<AdasMedia> medias = setMediaPath(mediaList);
            cpMedia(result, medias);
        }
        return result;
    }

    @Override
    public void addLogAndExportRiskDisposeRecord(AdasRiskDisposeRecordQuery query, HttpServletResponse response,
        HttpServletRequest request) throws Exception {
        ExportExcelUtil.setResponseHead(response, "主动安全处置报表");
        List<String> eventIds = adasEsService.esQueryExportRiskEventId(query);
        export(null, 1, response, getExportData(eventIds));
        addLog(query, request, "主动安全处置报表");
    }

    private boolean export(String title, int type, HttpServletResponse res, List<AdasRiskDisposeRecordForm> pis)
        throws IOException {
        return ExportExcelUtil.export(
            new ExportExcelParam(title, type, pis, AdasRiskDisposeRecordForm.class, null, res.getOutputStream()));
    }

    private void addLog(AdasRiskDisposeRecordQuery query, HttpServletRequest request, String module) {
        String ip = new GetIpAddr().getIpAddr(request);
        String currentUsername = SystemHelper.getCurrentUsername();
        String msg = "用户：" + currentUsername + "下载" + query.getStartTime() + "到" + query.getEndTime() + "时间段的" + module;
        logSearchService.addLog(ip, msg, "3", module);
    }

    private void cpMedia(List<AdasMediaShow> result, List<AdasMedia> medias) {
        AdasMediaShow mediaXShow;
        for (AdasMedia adasMedia : medias) {
            mediaXShow = new AdasMediaShow();
            BeanUtils.copyProperties(adasMedia, mediaXShow);
            RiskEvent riskEvent = riskEventDao.getRiskEventByFunctionId(adasMedia.getEventId());
            mediaXShow.setRiskType(riskEvent.getRiskType());
            mediaXShow.setRiskEventType(riskEvent.getRiskEvent());
            result.add(mediaXShow);
        }
    }

    public List<AdasMedia> setMediaPath(List<AdasMedia> medias) {
        if (sslEnabled) {
            fastDFSMediaServer = "/";
            mediaServer = "";
        }
        List<AdasMedia> newMedias = new ArrayList<>();
        for (AdasMedia media : medias) {
            boolean isStreamMedia = adasCommonHelper.isStreamMedia(media.getProtocolType());
            String prefix = isStreamMedia ? fastDFSMediaServer : mediaServer;
            String newPath = prefix + media.getMediaUrl();
            media.setMediaUrl(newPath);
            newMedias.add(media);
        }
        return newMedias;
    }

    @Override
    public void downLoadFileByPath(HttpServletResponse response, String filePath, boolean isRiskEvidence,
        String fileName) {
        ResponseUtil.setZipResponse(response, fileName + ".zip");
        InputStream fis = null;
        try {
            if (isRiskEvidence) {
                // 风控证据流对象
                fis = FtpClientUtil
                    .getFileInputStream(ftpUserName, ftpPassword, ftpHostClbs, ftpPortClbs, ftpPath, filePath);
            } else {
                // 终端证据流对象
                fis = new FileInputStream(new File(filePath));
            }
            FileUtils.writeFile(fis, response.getOutputStream());
        } catch (IOException e) {
            logger.error("导出风控证据异常", e);
        } finally {
            IOUtils.closeQuietly(fis);
        }
    }

    @Override
    public boolean saveRiskDealInfos(AdasAlarmDealInfo alarmDealInfo) throws IOException {

        String[] finalNotDealRisks = lockFinalNotDealRisks(alarmDealInfo);
        //没有报警则代表已经被处理
        if (ArrayUtils.isEmpty(finalNotDealRisks)) {
            return false;
        }
        int status = AdasRiskStatus.TREATED.getCode();
        //风险处理结果为空给个默认值：事故未发生
        Integer riskResult =
            Optional.ofNullable(alarmDealInfo.getRiskResult()).orElse(RiskResultEnum.SUCCESS_FILE.getCode());
        String handleType = alarmDealInfo.getHandleType();
        String name = SystemHelper.getCurrentUsername();
        long now = System.currentTimeMillis();
        Date nowDate = new Date(now);

        AdasDealInfo dealInfo = AdasDealInfo.of(status, name, nowDate, riskResult);
        List<String> riskEventIds =
            SleepUtils.waitAndDo(() -> adasEsService.esQueryRiskEventIdsByRiskId(finalNotDealRisks));
        List<String> mediaIds = adasEsService.esqueryMediaIdsByRiskId(finalNotDealRisks);
        List<AdasRiskForm> risks = Lists.newArrayList();
        List<AdasEventForm> events = Lists.newArrayList();
        List<AdasMediaForm> medias = Lists.newLinkedList();
        List<AdasRiskEsBean> riskEsBeans = Lists.newLinkedList();
        List<AdasRiskEventEsBean> eventEsBeans = Lists.newLinkedList();
        List<AdasMediaEsBean> mediaEsBeans = Lists.newLinkedList();

        //初始化相关信息
        for (String id : finalNotDealRisks) {
            risks.add(AdasRiskForm.of(riskResult, name, now, id, handleType));
            riskEsBeans.add(AdasRiskEsBean.getInstance(id, dealInfo));
        }

        for (String id : riskEventIds) {
            events.add(AdasEventForm.of(
                    status, name, now, riskResult, id, alarmDealInfo.getHandleType(), alarmDealInfo.getRemark()));
            eventEsBeans.add(AdasRiskEventEsBean.getInstance(id, dealInfo));
        }

        for (String id : mediaIds) {
            medias.add(AdasMediaForm.of(status, name, riskResult, id));
            mediaEsBeans.add(AdasMediaEsBean
                .getInstance(id, status, name, riskResult, dealInfo.getDriverName(), dealInfo.getDriverName()));
        }

        //更新risk相关信息
        this.updateRisk(risks);
        adasEsService.esUpdateRiskBatch(riskEsBeans);
        this.updateRiskEvents(events);
        adasEsService.esUpdateRiskEventBatch(eventEsBeans);
        this.updateRiskMedias(medias);
        adasEsService.esUpdateMediaBatch(mediaEsBeans);
        RedisServiceUtils.releaseDealRisksLock(finalNotDealRisks);

        return true;
    }

    private void updateRisk(List<AdasRiskForm> risks) {
        final Map<String, String> params = new HashMap<>(2);
        params.put("value", JSON.toJSONString(risks));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.UPDATE_RISK, params);
        if (!PaasCloudUrlUtil.getSuccess(str)) {
            logger.error("更新风险失败, result: {}, param: {}", str, params);
        }
    }

    @Override
    public void updateRiskEvents(List<AdasEventForm> riskEvents) {
        final Map<String, String> params = new HashMap<>(2);
        params.put("value", JSON.toJSONString(riskEvents));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.UPDATE_RISK_EVENT, params);
        if (!PaasCloudUrlUtil.getSuccess(str)) {
            logger.error("更新风险事件失败, result: {}, param: {}", str, params);
        }
    }

    private void updateRiskMedias(List<AdasMediaForm> riskMedias) {
        final Map<String, String> params = new HashMap<>(2);
        params.put("value", JSON.toJSONString(riskMedias));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.UPDATE_RISK_MEDIA, params);
        if (!PaasCloudUrlUtil.getSuccess(str)) {
            logger.error("更新风险媒体失败, result: {}, param: {}", str, params);
        }
    }

    /**
     * 根据事件id锁住没有处理的风险id
     */
    private String[] lockFinalNotDealRisks(AdasAlarmDealInfo alarmDealInfo) {
        Set<String> riskIdSet = new HashSet<>();
        if (alarmDealInfo.getEventIdSet() != null && alarmDealInfo.getEventIdSet().size() > 0) {
            riskIdSet = adasEsService.esGetRiskIdByEventId(AdasRiskStatus.UNTREATED.getCode(),
                alarmDealInfo.getEventIdSet().toArray(new String[] {}));
        }
        if (alarmDealInfo.getRiskIds() != null) {
            riskIdSet.addAll(Arrays.asList(alarmDealInfo.getRiskIds().split(",")));
        }
        return RedisServiceUtils.lockDealRisks(riskIdSet.toArray(new String[] {}));
    }

    @Override
    public boolean saveRiskDealInfo(AdasDealRiskForm adasDealRiskForm) throws Exception {
        //为空给个默认值
        Integer status = adasDealRiskForm.getStatus();
        String riskId = adasDealRiskForm.getRiskId();
        Integer riskResult = adasDealRiskForm.getRiskResult();
        riskResult = riskResult == null ? 0 : riskResult;
        String name = SystemHelper.getCurrentUsername();
        long now = System.currentTimeMillis();
        Date nowDate = new Date(now);
        AdasDealInfo dealInfo = new AdasDealInfo();
        //区分风险监管和主动安全或者App的风险处理
        BeanUtils.copyProperties(adasDealRiskForm, dealInfo);
        dealInfo.setDealTime(nowDate);
        dealInfo.setDealer(name);
        dealInfo.setHandleType("不做处理");
        //组装调用pass端接口更新Hbase的参数
        Map<String, String> param = new HashMap<>();
        param.put("dealInfoStr", JSON.toJSONString(dealInfo));
        param.put("nowStr", JSON.toJSONString(now));
        HttpClientUtil.send(PaasCloudAdasUrlEnum.SAVE_RISK_DEAL_INFO, param);
        boolean result = adasEsService.esUpdateRiskRealTime(AdasRiskEsBean.getInstance(riskId, dealInfo));

        // 更新事件相关信息
        List<String> riskEventIds = LogTimeUtil.ifSystemUserLogTimes("风险处理根据风险id在es中查询事件id",
            () -> SleepUtils.waitAndDo(() -> adasEsService.esQueryRiskEventIdsByRiskId(riskId)));
        List<AdasRiskEventEsBean> eventEsBeans = Lists.newLinkedList();
        //组装调用pass端接口更新Hbase的参数
        HBaseRiskDeal hbaseRiskDeal = HBaseRiskDeal
            .getInstance(dealInfo, riskEventIds, status, name, now, riskResult, UserTable.HTABLE_RISK_EVENT);
        param.clear();
        param.put("hBaseRiskDealStr", JSONObject.toJSONString(hbaseRiskDeal));

        Set<String> riskEventIdSet = new HashSet<>();
        for (String id : riskEventIds) {
            eventEsBeans.add(AdasRiskEventEsBean.getInstance(id, dealInfo));
            riskEventIdSet.add(id);
        }
        HttpClientUtil.send(PaasCloudAdasUrlEnum.SAVE_RISK_EVENT_DEAL_INFO, param);
        result = result && adasEsService.esUpdateRiskEventRealTimeBatch(eventEsBeans);
        if (result) {
            Predicate<String> predicate =
                redisKey -> riskEventIdSet.contains(redisKey.substring(0, redisKey.lastIndexOf("_")));
            RedisHelper.deleteScanKeys(HistoryRedisKeyEnum.FUZZY_ADAS_PLATFORM_REMIND.of(""), predicate);
        }
        // 更新多媒体相关信息
        List<String> mediaIds = adasEsService.esqueryMediaIdsByRiskId(riskId);
        List<AdasMediaEsBean> mediaEsBeans;
        //组装调用pass端接口更新Hbase的参数
        param.clear();
        param.put("mediaIdsStr", JSONObject.toJSONString(mediaIds));
        param.put("statusStr", JSONObject.toJSONString(status));
        param.put("nameStr", JSONObject.toJSONString(name));
        param.put("riskResultStr", JSONObject.toJSONString(riskResult));
        param.put("driverNameStr", JSONObject.toJSONString(dealInfo.getDriverName()));
        // 更新es 风险状态
        mediaEsBeans = mediaIds.stream()
                .map(id -> AdasMediaEsBean.getInstance(id, dealInfo))
                .collect(Collectors.toCollection(Lists::newLinkedList));
        HttpClientUtil.send(PaasCloudAdasUrlEnum.SAVE_RISK_MEDIA_DEAL_INFO, param);
        result = result && adasEsService.esUpdateMediaBatch(mediaEsBeans);
        //保证异常回滚
        if (result) {
            //成功只有进行解锁操作
            RedisServiceUtils.releaseRiskLock(riskId);
            //报警进行上报上级平台
            sendDealResultTo809Platform(riskId);

            return true;
        } else {
            throw new Exception();
        }
    }

    private void sendDealResultTo809Platform(String riskId) throws Exception {
        //主动上报809报警处理结果(固定为上报处理结果为“已处理完毕”、报警处理方式为“其他”)
        String handleType = "不做处理";
        AdasRiskEsBean adasRiskEsBean = adasEsService.esGetRiskById(riskId);
        //构建809上报上级平台所需要的参数实体
        AlarmHandleParam handleParam = AlarmHandleParam.getInstance(adasRiskEsBean.getVehicleId(), handleType, riskId);
        // 主动上报报警处理结果给上级平台
        connectionParamsSetService.initiativeSendAlarmHandle(handleParam);
    }

    @Override
    public void exportDoc(HttpServletResponse response, HttpServletRequest request, String riskId, String riskNumber) {
        try {
            String fileName = "风险报告" + riskNumber + ".doc";
            ResponseUtil.setWordResponse(response, fileName);
            Map<String, Object> dataMap = getDataMap(riskId);
            DocExportUtil.exportDocDefault(dataMap, response.getWriter(), request.getServletContext());
        } catch (MyBatisSystemException e) {
            logger.error("该车可能在多个分组或者多个组织中！", e);
        } catch (Exception e1) {
            logger.error("导出风险报告异常", e1);
        }
    }

    private Map<String, Object> getDataMap(String riskId) {
        String contextPath = System.getProperty("clbs.root");
        AdasRiskReportForm riskReportForm = searchRiskReportFormById(UuidUtils.getBytesFromStr(riskId), contextPath);
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("rp", riskReportForm);
        return dataMap;
    }

    @Override
    public List<AdasRiskItem> getRiskList(List<String> riskIds) {
        Map<String, String> params = new HashMap<>(8);
        params.put("riskIds", JSON.toJSONString(riskIds));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_RISK_LIST, params);
        return PaasCloudUrlUtil.getResultListData(str, AdasRiskItem.class);
    }

    private String getVehicleStatus(Integer vehicleStatus) {
        if (vehicleStatus == null) {
            return "";
        }

        StringBuilder stringBuffer = new StringBuilder();
        if (getNum(vehicleStatus, 0) == 1) {
            stringBuffer.append("ACC:打开,");
        } else {
            stringBuffer.append("ACC:关闭,");
        }
        if (getNum(vehicleStatus, 1) == 1) {
            stringBuffer.append("leftTurn:打开,");
        } else {
            stringBuffer.append("leftTurn:关闭,");
        }
        if (getNum(vehicleStatus, 2) == 1) {
            stringBuffer.append("rightTurn:打开,");
        } else {
            stringBuffer.append("rightTurn:关闭,");
        }
        if (getNum(vehicleStatus, 3) == 1) {
            stringBuffer.append("wiperWash:打开,");
        } else {
            stringBuffer.append("wiperWash:关闭,");
        }
        if (getNum(vehicleStatus, 4) == 1) {
            stringBuffer.append("braking:制动,");
        } else {
            stringBuffer.append("braking:未制动,");
        }
        if (getNum(vehicleStatus, 5) == 1) {
            stringBuffer.append("isCard:已插卡");
        } else {
            stringBuffer.append("isCard:未插卡");
        }
        return stringBuffer.toString();
    }

    /**
     * 获取一个Integer 类型的二进制的某一位是0还是1
     * @param num   Integer
     * @param index 二进制的哪一位
     * @return 0 Or 1
     */
    private Integer getNum(Integer num, Integer index) {
        return (num & (1 << index)) >> index;
    }

    @Override
    public Map<String, Object> getNameAndFunctionIds() {
        Map<String, Object> result = new HashMap<>();
        Map<String, Map<String, String>> commonNameAndFunctionIdMap = new HashMap<>();
        List<RiskEvent> list = riskEventDao.getNameAndFunctionIds();
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        Map<String, String> map;
        Map<String, String> riskTypeMap = new HashMap<>();
        for (RiskEvent riskEvent : list) {
            map = Optional.ofNullable(commonNameAndFunctionIdMap.get(riskEvent.getRiskType())).orElse(new HashMap<>());
            map.put(riskEvent.getEventCommonName(), riskEvent.getFunctionIds());
            commonNameAndFunctionIdMap.put(riskEvent.getRiskType(), map);
            riskTypeMap.putIfAbsent(riskEvent.getRiskType(), riskEvent.getRiskTypeNum() + "");
        }
        result.put("event", commonNameAndFunctionIdMap);
        result.put("riskType", riskTypeMap);
        return result;
    }

    @Override
    public boolean checkBrandInSelected(AdasRiskDisposeRecordQuery query) {
        if (StrUtil.isBlank(query.getBrand())) {
            return true;
        }
        String vehicleIds = query.getVehicleIds();
        if (StrUtil.isBlank(vehicleIds)) {
            return false;
        }
        // 检查是否通过车牌查询
        Set<String> selectVehicleIds = new HashSet<>(Arrays.asList(vehicleIds.split(",")));
        Set<String> finalVehicleIds =
            vehicleService.fuzzyKeyword(query.getBrand(), selectVehicleIds, MonitorTypeEnum.VEHICLE);
        return !finalVehicleIds.isEmpty();
    }

}


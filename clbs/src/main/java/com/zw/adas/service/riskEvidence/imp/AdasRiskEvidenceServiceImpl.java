package com.zw.adas.service.riskEvidence.imp;

import com.alibaba.fastjson.JSON;
import com.cb.platform.util.page.PassCloudResultUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zw.adas.domain.riskManagement.AdasMedia;
import com.zw.adas.domain.riskManagement.form.AdasRiskDisposeRecordForm;
import com.zw.adas.domain.riskManagement.query.AdasRiskDisposeRecordQuery;
import com.zw.adas.repository.mysql.riskEvidence.AdasRiskEvidenceDao;
import com.zw.adas.service.elasticsearch.AdasElasticSearchService;
import com.zw.adas.service.riskEvidence.AdasRiskEvidenceService;
import com.zw.adas.service.riskdisposerecord.impl.AdasRiskServiceImpl;
import com.zw.adas.utils.FastDFSClient;
import com.zw.adas.utils.elasticsearch.AdasElasticSearchUtil;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.multimedia.form.MediaForm;
import com.zw.platform.repository.modules.MediaDao;
import com.zw.platform.repository.vas.RiskCampaignDao;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.UserTable;
import com.zw.platform.util.common.FtpClientUtil;
import com.zw.platform.util.common.MonitorUtils;
import com.zw.platform.util.common.UuidUtils;
import com.zw.platform.util.common.WebPathUtil;
import com.zw.platform.util.common.ZipUtility;
import com.zw.platform.util.ffmpeg.FileUtils;
import com.zw.platform.util.paas.PaasCloudUrlUtil;
import com.zw.platform.util.report.PaasCloudAdasUrlEnum;
import com.zw.platform.util.report.PaasCloudHBaseAccessEnum;
import com.zw.platform.util.response.ResponseUtil;
import com.zw.protocol.util.ProtocolTypeUtil;
import org.apache.bval.jsr.util.IOs;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdasRiskEvidenceServiceImpl implements AdasRiskEvidenceService {

    private static final Logger log = LogManager.getLogger(AdasRiskEvidenceServiceImpl.class);

    /**
     * 1 终端图片
     * 2 终端视频
     * 3 风控音频
     * 4 风控视频
     */
    private static final List<String> evidenceTypes = Lists.newArrayList("1", "2", "3", "4");

    @Autowired
    private AdasRiskEvidenceDao adasRiskEvidenceDao;

    @Autowired
    private FastDFSClient fastDFSClient;

    @Autowired
    private MediaDao mediaDao;

    @Autowired
    private RiskCampaignDao riskCampaignDao;

    @Autowired
    private AdasRiskServiceImpl adasRiskService;

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

    @Value("${fdfs.webServerUrl}")
    private String webServerUrl;

    @Value("${system.ssl.enable}")
    private boolean sslEnabled;

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    private AdasElasticSearchService esService;

    private static final HashMap<String, String> RISK_MEDIA_COLUMN = new HashMap<>();

    static {
        RISK_MEDIA_COLUMN.put("ID", "0");
        RISK_MEDIA_COLUMN.put("RISK_ID", "0");
        RISK_MEDIA_COLUMN.put("RISK_EVENT_ID", "0");
        RISK_MEDIA_COLUMN.put("RISK_TIME", "0");
        RISK_MEDIA_COLUMN.put("EVENT_NUMBER", "0");
        RISK_MEDIA_COLUMN.put("EVENT_ID", "0");
        RISK_MEDIA_COLUMN.put("ADDRESS", "0");
        RISK_MEDIA_COLUMN.put("VEHICLE_ID", "0");
        RISK_MEDIA_COLUMN.put("BRAND", "0");
        RISK_MEDIA_COLUMN.put("URL", "0");
        RISK_MEDIA_COLUMN.put("NAME", "0");
        RISK_MEDIA_COLUMN.put("RISK_TYPE", "0");
        RISK_MEDIA_COLUMN.put("MEDIA_ID", "0");
        RISK_MEDIA_COLUMN.put("RISK_LEVEL", "0");
        RISK_MEDIA_COLUMN.put("DRIVER_NAME", "0");
        RISK_MEDIA_COLUMN.put("DEALER", "0");
        RISK_MEDIA_COLUMN.put("RESULT", "0");
        RISK_MEDIA_COLUMN.put("RISK_NUMBER", "0");
        RISK_MEDIA_COLUMN.put("PROTOCOL_TYPE", "0");
    }

    @Override
    public Map<String, Object> queryRiskEvidenceFromHb(AdasRiskDisposeRecordQuery query, boolean downloadOrNot)
        throws Exception {
        String evidenceType = query.getEvidenceType();
        if (StringUtils.isEmpty(evidenceType) && !evidenceTypes.contains(evidenceType)) {
            throw new RuntimeException("证据类型错误!");
        }
        Map<String, Object> map = esService.esQueryMediaInfo(query, downloadOrNot);
        //得到media_id
        List<String> ids = (List<String>) map.get("ids");

        long total = (long) map.get("total");
        List<AdasRiskDisposeRecordForm> datas = Lists.newLinkedList();
        if (CollectionUtils.isEmpty(ids)) {
            map.put("list", datas);
            return map;
        }
        datas = adasRiskService.getHbByIdAndNameAndColumn(ids, UserTable.HTABLE_RISK_MEDIA, RISK_MEDIA_COLUMN);
        if (!downloadOrNot && datas != null) {

            int startPage = query.getPage().intValue();
            int limit = query.getLimit().intValue();
            if (startPage == 1 && datas.size() != limit && total > limit) {
                // 查询第一页的时候由于es和hbase 存在先插入和后插入的问题,第一页如果的pageSize比limit小,就要进行补偿,最多补偿5页数据
                //补偿条数
                int index = 0;
                compensationRecord(query, datas, ids, total, index, map);
                datas = (List<AdasRiskDisposeRecordForm>) map.get("list");
            }

            List<String> vehicleIds = datas.stream()
                    .map(AdasRiskDisposeRecordForm::getVehicleId)
                    .collect(Collectors.toList());
            Map<String, BindDTO> monitorInfoMap = MonitorUtils.getBindDTOMap(vehicleIds, "id", "name", "orgName");

            String mediaName;
            for (AdasRiskDisposeRecordForm data : datas) {
                mediaName = getMediaName(data);
                data.setMediaName(mediaName);
                data.setId(UuidUtils.getUUIDStrFromBytes(data.getIdByte()));

                Map<String, String> param = new HashMap<>();
                param.put("eventId", JSON.toJSONString(UuidUtils.getBytesFromStr(data.getRiskEventId())));
                String sendResult = HttpClientUtil.send(PaasCloudAdasUrlEnum.GET_EVIDENCE_BY_RISK_EVENT_ID, param);
                AdasRiskDisposeRecordForm adasRiskDisposeRecordForm =
                    PassCloudResultUtil.getClassResult(sendResult, AdasRiskDisposeRecordForm.class);
                data.setSpeed(adasRiskDisposeRecordForm.getSpeed());
                data.setWeather(adasRiskDisposeRecordForm.getWeather());
                data.setAddress(adasRiskDisposeRecordForm.getAddress());
                BindDTO bindDTO = monitorInfoMap.get(data.getVehicleId());
                if (bindDTO == null) {
                    continue;
                }
                data.setGroupName(bindDTO.getOrgName());
                // 设置车牌
                if (StringUtils.isEmpty(data.getBrand())) {
                    data.setBrand(bindDTO.getName());
                }
            }

        }
        map.put("ids", null);
        map.put("list", datas);
        return map;
    }

    private String getMediaName(AdasRiskDisposeRecordForm data) {
        if (sslEnabled) {
            if (data.getProtocolType() != 1) {
                return "/" + data.getMediaUrlNew();
            }
            return "/mediaserver" + data.getMediaUrlNew();
        }
        String mediaName;
        if (data.getProtocolType() != 1) {
            mediaName = webServerUrl + data.getMediaUrlNew();
        } else {
            mediaName = mediaServer + data.getMediaUrlNew();
        }
        return mediaName;
    }

    private void compensationRecord(AdasRiskDisposeRecordQuery query, List<AdasRiskDisposeRecordForm> datas,
                                    List<String> ids, long total, int index, Map<String, Object> map) throws Exception {
        Set<String> set = new HashSet<>();
        List<String> excludeIds = Lists.newLinkedList();
        for (AdasRiskDisposeRecordForm risk : datas) {
            set.add(UuidUtils.getUUIDStrFromBytes(risk.getIdByte()));
        }
        for (String id : ids) {
            if (!set.contains(id)) {
                excludeIds.add(id);
            }
        }
        query.getExcludeIds().addAll(excludeIds);
        Map<String, Object> newMap = esService.esQueryMediaInfo(query, false);
        List<String> ids1 = (List<String>) newMap.get("ids");

        Map<String, String> param = new HashMap<>();
        param.put("mediaIds", JSON.toJSONString(ids1));
        String sendResult = HttpClientUtil.send(PaasCloudAdasUrlEnum.QUERY_EVIDENCE_MEDIA_INFO, param);
        List<AdasRiskDisposeRecordForm> riskMedia =
            PassCloudResultUtil.getListResult(sendResult, AdasRiskDisposeRecordForm.class);
        if (riskMedia.size() < query.getLimit()) {
            index++;
            if (index > 5) {
                //递归5次
                setResult(query, map, riskMedia, total);
                return;
            }
            // 再次补偿
            compensationRecord(query, riskMedia, ids1, total, index, map);
        } else {
            setResult(query, map, riskMedia, total);
        }
    }

    private void setResult(AdasRiskDisposeRecordQuery query, Map<String, Object> map,
        List<AdasRiskDisposeRecordForm> risks, long total) {
        map.put("list", risks);
        map.put("total", total - query.getExcludeIds().size());
    }

    /**
     * 获取车牌
     */
    @Override
    public Set<String> queryBrandsFromHb(AdasRiskDisposeRecordQuery query) throws Exception {
        String evidenceType = query.getEvidenceType();
        if (StringUtils.isEmpty(evidenceType) && !evidenceTypes.contains(evidenceType)) {
            throw new RuntimeException("证据类型错误!");
        }
        return new HashSet<>(esService.esGetAllBrands(query, AdasElasticSearchUtil.ADAS_MEDIA));
    }

    @Override
    public boolean export(HttpServletResponse response, List<AdasRiskDisposeRecordForm> adasRisks, String evidentType)
        throws Exception {

        if (adasRisks == null || adasRisks.size() == 0) {
            return false;
        }
        String fileName = generateFileName(evidentType);
        if (fileName == null) {
            return false;
        }
        //加上随机数,控制简答并发
        String riskEvidenceTempPath = WebPathUtil.webPath + "riskEvidenceTemp" + (int) (Math.random() * 100000);
        String riskEvidenceZipTempPath = WebPathUtil.webPath + "riskEvidenceZipTemp" + (int) (Math.random() * 100000);
        String mediaName;
        AdasRiskDisposeRecordForm rs;

        List<RedisKey> vehicleIdKeys = adasRisks.stream()
                .map(RedisKeyEnum.MONITOR_INFO::of)
                .collect(Collectors.toList());
        Map<String, String> monitorInfoMap = RedisHelper.batchGetHashMap(vehicleIdKeys, "id", "name");
        String newName;
        try {
            for (int i = 0; i < adasRisks.size(); i++) {
                rs = adasRisks.get(i);
                mediaName = rs.getMediaName();
                //设置车牌
                rs.setBrand(monitorInfoMap.get(rs.getVehicleId()));
                newName = getNewName(mediaName, rs);
                cpPicFromFtp(adasRisks, riskEvidenceTempPath, mediaName, newName, i);
            }
        } catch (Exception e) {
            log.error(">========压缩风控证据库失败!<============", e);
            throw e;
        }
        return executeZipAndDownload(response, fileName, riskEvidenceTempPath, riskEvidenceZipTempPath);
    }

    private String getNewName(String mediaName, AdasRiskDisposeRecordForm rs) {
        String newName;
        try {
            newName =
                rs.getBrand() + "_" + rs.getWarTime().replaceAll("-|:|\\s+", "_") + "_" + (int) (Math.random() * 1000)
                    + "." + mediaName.split("\\.")[1];
        } catch (Exception e) {
            //如果没有报警时间 说明是垃圾图片,为了程序后台不报错
            newName = mediaName;
        }
        return newName;
    }

    private void cpPicFromFtp(List<AdasRiskDisposeRecordForm> adasRiskDisposeRecordForms, String riskEvidenceTempPath,
        String mediaName, String newName, int i) {
        String fileDir = riskEvidenceTempPath + File.separator + adasRiskDisposeRecordForms.get(i).getBrand();
        boolean success = FileUtils.createDirectory(fileDir);
        FileOutputStream out = null;
        InputStream fis = null;
        try {
            if (success) {
                String destination = fileDir + File.separator + newName;
                fis = getMediaInputStream(adasRiskDisposeRecordForms.get(i).getMediaUrlNew());
                if (fis != null) {
                    out = new FileOutputStream(destination);
                    FileUtils.writeFile(fis, out);
                }
            }
        } catch (Exception e) {
            log.error("复制图片" + mediaName + "失败,图片不存在");
        } finally {
            IOs.closeQuietly(out);
            IOs.closeQuietly(fis);
        }
    }

    private boolean executeZipAndDownload(HttpServletResponse response, String fileName, String riskEvidenceTempPath,
        String riskEvidenceZipTempPath) throws Exception {

        boolean flag;//执行压缩
        flag = ZipUtility.zip(riskEvidenceTempPath, riskEvidenceZipTempPath, fileName);
        if (flag) {
            ResponseUtil.setZipResponse(response, fileName);
            String filePath = riskEvidenceZipTempPath + File.separator + fileName;
            FileUtils.writeFile(filePath, response.getOutputStream());
            //异步删除临时文件
            taskExecutor.execute(new DeleteFile(riskEvidenceTempPath, riskEvidenceZipTempPath));
        }
        return flag;
    }

    private String generateFileName(String evidentType) {
        String fileName = null;
        String date = LocalDateTime.now().toString().replaceAll("[-:T.]", "_");
        switch (evidentType) {
            case "1":
                fileName = "风控证据库_终端图片证据" + date + ".zip";
                break;
            case "2":
                fileName = "风控证据库_终端视频证据" + date + ".zip";
                break;
            case "3":
                fileName = "风控证据库_风控音频证据" + date + ".zip";
                break;
            case "4":
                fileName = "风控证据库_风控视频证据" + date + ".zip";
                break;
            default:
                break;
        }
        return fileName;
    }

    private static class DeleteFile implements Runnable {
        private final String riskEvidenceTempPath;

        private final String riskEvidenceZipTempPath;

        DeleteFile(String riskEvidenceTempPath, String riskEvidenceZipTempPath) {
            this.riskEvidenceTempPath = riskEvidenceTempPath;
            this.riskEvidenceZipTempPath = riskEvidenceZipTempPath;
        }

        @Override
        public void run() {
            try {
                FileUtils.deleteDir(new File(riskEvidenceTempPath));
                FileUtils.deleteDir(new File(riskEvidenceZipTempPath));
            } catch (Exception e) {
                log.error(">===========删除临时文件失败!============<", e);
            }
        }

    }

    @Override
    public void updateMediaIds() throws Exception {
        List<Map<String, String>> mp4Medias = adasRiskEvidenceDao.getMp4Medias();
        String id;
        long mediaId;
        for (Map<String, String> mp4Media : mp4Medias) {
            id = mp4Media.get("id");
            mediaId = Long.parseLong(mp4Media.get("mediaId").split(",")[0]);
            adasRiskEvidenceDao.updateMediaIds(id, mediaId + "");
        }
    }

    @Override
    public void updateRiskIds() throws Exception {
        List<Map<String, String>> riskIds = adasRiskEvidenceDao.getRiskIds();
        for (Map<String, String> riskId : riskIds) {
            adasRiskEvidenceDao.updateRiskIds(riskId);
        }
    }

    @Override
    public void updateRiskEventIds() throws Exception {
        List<MediaForm> mediaForms = mediaDao.queryAll();
        List<String> riskEventIds;
        Map<String, String> map = Maps.newHashMap();
        for (MediaForm mediaForm : mediaForms) {
            riskEventIds = riskCampaignDao.getRiskEventIds(mediaForm.getVehicleId(), mediaForm.getMediaId() + "");
            if (riskEventIds.size() > 0) {
                map.put("id", mediaForm.getId());
                map.put("riskEventId", riskEventIds.get(0));
                adasRiskEvidenceDao.updateRiskEventIds(map);
            }
        }

    }

    @Override
    public boolean updateRiskEvidenceNameOnFtp() {
        writeFileToFtp();
        return true;
    }

    @Override
    public boolean canDownload(String id, boolean isJpeg) {
        boolean result;
        Map<String, String> param = new HashMap<>();
        param.put("mediaId", id);
        String sendResult = HttpClientUtil.send(PaasCloudHBaseAccessEnum.RISK_MEDIA_URL_AND_PROTOCOL, param);
        AdasMedia resultData = PaasCloudUrlUtil.getResultData(sendResult, AdasMedia.class);
        String mediaUrl = resultData.getMediaUrl();
        if (ProtocolTypeUtil.ZHONG_WEI_PROTOCOL_808_2013.equals(resultData.getProtocolType().toString())) {
            result = adasRiskService.existFileOnFtpServer(mediaUrl);
        } else {
            result = fastDFSClient.existFile(mediaUrl);
        }
        return result;
    }

    private void writeFileToFtp() {
        List<String> riskEvidenceList = adasRiskEvidenceDao.findAllRiskEvidence();
        int i = 1;
        for (String mediaUrl : riskEvidenceList) {
            log.info("开始拷贝第" + i + "个文件：" + mediaUrl);
            copyFileToFtp(mediaUrl);
            log.info("结束拷贝第" + i + "个文件：" + mediaUrl);
            i++;
        }
    }

    private void copyFileToFtp(String mediaUrl) {

        String fileName = getFileName(mediaUrl);
        fileName = fileName.substring(1);
        String newFilePath = ftpPath + "/" + getFileName(mediaUrl);
        try {
            InputStream in = getMediaInputStream(fileName);
            if (in != null) {
                FtpClientUtil.uploadFile(ftpHostClbs, ftpPortClbs, ftpUserName, ftpPassword, ftpPath, newFilePath, in);
            }

        } catch (Exception e) {
            log.error("上传文件失败！");
            e.printStackTrace();
        }
    }

    private String getFileName(String mediaName) {
        String[] fileArr = mediaName.split("/");
        return fileArr[fileArr.length - 1];
    }

    private InputStream getMediaInputStream(String mediaUrl) {
        InputStream in;
        in = FtpClientUtil.getFileInputStream(ftpUserName, ftpPassword, ftpHostClbs, ftpPortClbs,
            StringUtil.encodingFtpFileName(mediaUrl));
        return in;
    }

}


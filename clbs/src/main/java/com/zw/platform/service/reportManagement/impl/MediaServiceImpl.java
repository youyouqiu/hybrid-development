package com.zw.platform.service.reportManagement.impl;

import com.github.pagehelper.Page;
import com.zw.adas.domain.riskManagement.bean.AdasMediaEsBean;
import com.zw.adas.service.elasticsearch.AdasElasticSearchService;
import com.zw.adas.utils.FastDFSClient;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.multimedia.Media;
import com.zw.platform.domain.multimedia.form.MediaForm;
import com.zw.platform.domain.multimedia.query.MediaQuery;
import com.zw.platform.push.common.WebClientHandleCom;
import com.zw.platform.repository.modules.MediaDao;
import com.zw.platform.repository.vas.RiskCampaignDao;
import com.zw.platform.service.reportManagement.MediaService;
import com.zw.platform.util.PageHelperUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.WebPathUtil;
import com.zw.platform.util.ffmpeg.FileUtils;
import com.zw.platform.util.response.ContentType;
import com.zw.platform.util.response.ResponseUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

@Service
public class MediaServiceImpl implements MediaService {

    private static final Logger log = LogManager.getLogger(MediaServiceImpl.class);

    @Autowired
    private MediaDao mediaDao;

    @Autowired
    private RiskCampaignDao riskCampaignDao;

    @Autowired
    private AdasElasticSearchService adasElasticSearchService;

    @Autowired
    FastDFSClient fastDFSClient;

    @Value("${fdfs.webServerUrl}")
    private String webServerUrl;

    @Value("${system.ssl.enable}")
    private boolean sslEnabled;

    @Override
    public Page<Media> findMedia(MediaQuery query) {
        return PageHelperUtil.doSelect(query, () -> mediaDao.findMedia(query));
    }

    @Override
    public boolean addMedia(MediaForm form) {
        form.setCreateDataTime(new Date());
        form.setCreateDataUsername(SystemHelper.getCurrentUsername());
        form.setUploadTime(new Date());
        if (form.getMediaId() != null && form.getMediaId() != 0 && StringUtils.isEmpty(form.getRiskEventId())) {
            List<String> riskEventIds = riskCampaignDao.getRiskEventIds(form.getVehicleId(), form.getMediaId() + "");
            if (riskEventIds.size() > 0) {
                form.setRiskEventId(riskEventIds.get(0));

                WebClientHandleCom.cacheMedia(form);
            }
        }
        return mediaDao.addMedia(form);
    }

    @Override
    public boolean deleteById(String id) {

        MediaForm media = mediaDao.findById(id);
        boolean flag = false;
        if (media != null) {
            String path = media.getMediaUrlNew();
            flag = mediaDao.deleteById(id);
            if (flag) {
                fastDFSClient.deleteFile(path);
            }
        }
        return flag;
    }

    @Override
    public boolean addZipMedia(MediaForm mediaForm) {
        return mediaDao.addMedia(mediaForm);
    }

    private AdasMediaEsBean generateMediaEsBean(MediaForm media) {

        AdasMediaEsBean mediaEsBean = new AdasMediaEsBean();
        String mediaName = media.getMediaName();
        if (!StringUtils.isEmpty(mediaName)) {
            if (mediaName.endsWith("mp4")) {
                mediaEsBean.setEvidenceType(2);
            } else if (mediaName.endsWith("jpg") || mediaName.endsWith("jpeg") || mediaName.endsWith("png")) {
                mediaEsBean.setEvidenceType(1);
            } else if (mediaName.endsWith("wav")) {
                mediaEsBean.setEvidenceType(3);
            }
        }
        mediaEsBean.setVehicleId(media.getVehicleId());
        mediaEsBean.setRiskId(media.getRiskId());
        mediaEsBean.setRiskEventId(media.getRiskEventId());
        mediaEsBean.setMediaType(media.getType());
        mediaEsBean.setMediaId(media.getMediaId());
        mediaEsBean.setEventType(media.getEventId());
        mediaEsBean.setRiskLevel(media.getRiskLevel());
        mediaEsBean.setRiskType(media.getRiskType());
        mediaEsBean.setId(media.getId());
        mediaEsBean.setAddress(media.getAddress());
        mediaEsBean.setVisitId(media.getVisitId());
        mediaEsBean.setRiskResult(media.getRiskResult());
        if (media.getRiskTime() == null) {
            mediaEsBean.setWarningTime(new Date());
        } else {
            mediaEsBean.setWarningTime(new Date(media.getRiskTime()));
        }
        mediaEsBean.setRiskNumber(media.getRiskNumber());
        mediaEsBean.setBrand(media.getBrand());
        mediaEsBean.setEventNumber(media.getEventNumber());
        mediaEsBean.setDriver(media.getDriverName());
        mediaEsBean.setDealer(media.getDealer());
        return mediaEsBean;
    }

    @Override
    public Media getMedia(String id) {
        return mediaDao.getMediaByMediaId(id);
    }

    @Override
    public JsonResultBean updateMediaDescription(String id, String description) {
        boolean flag = mediaDao.updateMediaDescription(id, description);
        if (flag) {
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } else {
            return new JsonResultBean(JsonResultBean.FAULT, "更新失败");
        }

    }

    @Override
    public void downMedia(String mediaUrl, String fileName, HttpServletResponse response) {
        if (mediaUrl != null && !mediaUrl.equals("")) {
            try {
                String fileType = fileName.split("\\.")[1];
                ResponseUtil.setDownloadpResponse(response, fileName, ContentType.valueOf(fileType.toUpperCase()));
                if (sslEnabled && mediaUrl.startsWith("/group")) {
                    mediaUrl = mediaUrl.substring(1);
                    byte [] re = fastDFSClient.downloadFile(mediaUrl.split(webServerUrl)[0]);
                    response.getOutputStream().write(re);
                    return;
                }
                if (mediaUrl.startsWith("http")) {
                    byte [] re = fastDFSClient.downloadFile(mediaUrl.split(webServerUrl)[1]);
                    response.getOutputStream().write(re);
                } else {
                    String  path = WebPathUtil.webPath + mediaUrl;
                    FileUtils.writeFile(path, response.getOutputStream());
                }
            } catch (Exception e) {
                log.error("媒体管理下载媒体异常！", e);
            }
        } else {
            log.info("媒体管理下载路径为空！");
        }
    }
}

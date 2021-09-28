package com.zw.adas.service.reportManagement.impl;

import com.alibaba.fastjson.JSON;
import com.zw.adas.domain.riskManagement.bean.AdasMediaEsBean;
import com.zw.adas.domain.riskManagement.form.AdasMediaFlagForm;
import com.zw.adas.domain.riskManagement.form.AdasMediaForm;
import com.zw.adas.service.elasticsearch.AdasElasticSearchService;
import com.zw.adas.service.reportManagement.AdasMediaService;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.util.report.PaasCloudHBaseAccessEnum;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class AdasMediaServiceImpl implements AdasMediaService {

    private Logger log = LogManager.getLogger(AdasMediaServiceImpl.class);

    @Autowired
    private AdasElasticSearchService adasElasticSearchService;

    private AdasMediaEsBean generateMediaEsBean(AdasMediaForm media) {

        AdasMediaEsBean mediaEsBean = new AdasMediaEsBean();
        String mediaName = media.getMediaUrl();
        if (!StringUtils.isEmpty(mediaName)) {
            if (mediaName.endsWith("mp4") || mediaName.endsWith("MP4") || mediaName.endsWith("h264")
                || mediaName.endsWith("H264") || mediaName.endsWith("avi") || mediaName.endsWith("AVI")) {
                mediaEsBean.setEvidenceType(2);
            } else if (mediaName.endsWith("jpg") || mediaName.endsWith("jpeg") || mediaName.endsWith("png")
                    || mediaName.endsWith("JPG") || mediaName.endsWith("JPEG") || mediaName.endsWith("PNG")) {
                mediaEsBean.setEvidenceType(1);
            } else if (mediaName.endsWith("wav") || mediaName.endsWith("mp3")
                    || mediaName.endsWith("WAV") || mediaName.endsWith("MP3")) {
                mediaEsBean.setEvidenceType(3);
            } else if (mediaName.endsWith("bin") || mediaName.endsWith("BIN")) {
                mediaEsBean.setEvidenceType(5);
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
        mediaEsBean.setStatus(media.getStatus());
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
    public boolean addMediaHbaseAndEsBatch(Set<AdasMediaForm> mediaForms) {
        //批量插入hbase媒体证据信息
        if (!CollectionUtils.isEmpty(mediaForms)) {
            Map<String, String> params = new HashMap<>(2);
            params.put("value", JSON.toJSONString(mediaForms));
            HttpClientUtil.send(PaasCloudHBaseAccessEnum.UPDATE_MEDIA, params);
        }
        // 插入es ,打包的zip不插入es中
        List<AdasMediaEsBean> mediaEsBeans = new ArrayList<>();
        for (AdasMediaForm media : mediaForms) {
            // 插入hbase
            if (!media.getMediaName().endsWith(".zip")) {
                AdasMediaEsBean mediaEsBean = generateMediaEsBean(media);
                mediaEsBeans.add(mediaEsBean);
            }
        }
        return adasElasticSearchService.esAddMediaBatch(mediaEsBeans);
    }

    @Override
    public void updateRiskMediaFlagBatch(Set<AdasMediaFlagForm> adasMediaFlagForms) {
        if (!CollectionUtils.isEmpty(adasMediaFlagForms)) {
            Map<String, String> params = new HashMap<>(2);
            params.put("value", JSON.toJSONString(adasMediaFlagForms));
            HttpClientUtil.send(PaasCloudHBaseAccessEnum.UPDATE_RISK_MEDIA_FLAG, params);
        }
    }

    @Override
    public void updateEventMediaFlagBatch(Set<AdasMediaFlagForm> adasMediaFlagForms) {
        if (!CollectionUtils.isEmpty(adasMediaFlagForms)) {
            Map<String, String> params = new HashMap<>(2);
            params.put("value", JSON.toJSONString(adasMediaFlagForms));
            HttpClientUtil.send(PaasCloudHBaseAccessEnum.UPDATE_EVENT_MEDIA_FLAG, params);
        }
    }
}
